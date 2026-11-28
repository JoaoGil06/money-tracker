package com.joaogil.money_tracker.config;

import com.joaogil.money_tracker.adapter.out.security.JwtTokenIssuer;
import com.joaogil.money_tracker.application.port.out.auth.TokenIssuer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    JwtEncoder jwtEncoder(JwtProperties props) {
        return NimbusJwtEncoder.withSecretKey(props.secretKey()).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties props) {
        var decoder = NimbusJwtDecoder.withSecretKey(props.secretKey()).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.issuer())); // exp/nbf + iss
        return decoder;
    }

    @Bean
    TokenIssuer tokenIssuer(JwtEncoder encoder, JwtProperties props) {
        return new JwtTokenIssuer(encoder, props.issuer(), props.ttl());
    }
}
