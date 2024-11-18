package com.printScript.snippetService;

import java.time.Instant;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .cors(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> new Jwt("mockToken", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"),
                Map.of("sub", "mockUserId", "username", "mockUsername"));
    }
}
