package com.printScript.snippetService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestOAuth2ResourceServerSecurityConfiguration {

    @Bean(name = "testSecurityFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll());
        return http.build();
    }

    @Primary
    @Bean(name = "testJwtDecoder")
    public JwtDecoder jwtDecoder() {
        JwtDecoder mockJwtDecoder = mock(JwtDecoder.class);
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getClaimAsString("sub")).thenReturn("test-user");
        when(mockJwtDecoder.decode(anyString())).thenReturn(mockJwt);
        return mockJwtDecoder;
    }
}
