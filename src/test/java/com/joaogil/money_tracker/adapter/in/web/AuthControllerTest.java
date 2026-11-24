package com.joaogil.money_tracker.adapter.in.web;

import com.joaogil.money_tracker.adapter.in.web.auth.AuthController;
import com.joaogil.money_tracker.application.port.in.RegisterUser;
import com.joaogil.money_tracker.application.port.in.RegisteredUserResult;
import com.joaogil.money_tracker.config.SecurityConfig;
import com.joaogil.money_tracker.domain.exception.ConflictException;
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

    private static final String VALID = """
            {"name":"João","email":"joao@example.com","password":"s3cret-pass"}""";

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
}