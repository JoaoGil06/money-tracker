package com.joaogil.money_tracker.adapter.in.web.auth;

import com.joaogil.money_tracker.application.port.in.auth.LoginResult;
import com.joaogil.money_tracker.application.port.in.auth.LoginUser;
import com.joaogil.money_tracker.application.port.in.auth.RegisterUser;
import com.joaogil.money_tracker.application.port.in.auth.RegisteredUserResult;
import com.joaogil.money_tracker.config.SecurityConfig;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import com.joaogil.money_tracker.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean RegisterUser registerUser;
    @MockitoBean
    LoginUser loginUser;

    private static final String VALID = """
            {"name":"João","email":"joao@example.com","password":"s3cret-pass"}""";

    private static final String LOGIN = """
        {"email":"joao@example.com","password":"s3cret-pass"}""";

    @Test
    void returns201WithLocationAndBody() throws Exception {
        var id = UUID.randomUUID();
        given(registerUser.register(any())).willReturn(
                new RegisteredUserResult(id, "João", "joao@example.com", Instant.parse("2026-11-05T10:00:00Z")));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/me"))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-05T10:00:00Z"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void returns400ProblemDetailWithErrorsMapOnInvalidBody() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email","password":"short"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void rejectsPasswordLongerThan72Characters() throws Exception {
        var body = """
                {"name":"João","email":"joao@example.com","password":"%s"}""".formatted("x".repeat(73));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void R11_returns409ProblemDetailWhenEmailTaken() throws Exception {
        given(registerUser.register(any())).willThrow(new ConflictException("email already registered"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void registerIsPublicButOtherApiRoutesRequireAuthentication() throws Exception {
        mvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturns200WithAccessTokenAndExpiresIn() throws Exception {
        given(loginUser.login(any())).willReturn(new LoginResult("jwt-value", 3600));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-value"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void loginWithBadCredentialsReturns401ProblemDetail() throws Exception {
        given(loginUser.login(any())).willThrow(new InvalidCredentialsException());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("invalid email or password"));
    }

    @Test
    void loginWithInvalidBodyReturns400WithErrorsMap() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"not-an-email","password":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginIsPublic() throws Exception {
        given(loginUser.login(any())).willReturn(new LoginResult("jwt-value", 3600));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk());   // no Authorization header needed
    }
}