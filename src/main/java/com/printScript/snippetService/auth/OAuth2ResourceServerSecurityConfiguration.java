package com.printScript.snippetService.auth;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class OAuth2ResourceServerSecurityConfiguration {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests.requestMatchers("/").permitAll()
                .requestMatchers(HttpMethod.GET, "/snippet").hasAuthority("SCOPE_read:snippets")
                .requestMatchers(HttpMethod.GET, "/snippet/*").hasAuthority("SCOPE_read:snippets")
                .requestMatchers(HttpMethod.POST, "/snippet").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.DELETE, "/snippet/*").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.PUT, "/format").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.GET, "/format").hasAuthority("SCOPE_read:snippets")
                .requestMatchers(HttpMethod.PUT, "/lint").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.GET, "/lint").hasAuthority("SCOPE_read:snippets")
                .requestMatchers(HttpMethod.GET, "/test/*").hasAuthority("SCOPE_read:snippets")
                .requestMatchers(HttpMethod.GET, "/test").hasAuthority("SCOPE_read:snippets")
                .requestMatchers(HttpMethod.POST, "/test/*").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.POST, "/test").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.DELETE, "/test/*").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.DELETE, "/test").hasAuthority("SCOPE_write:snippets")
                .requestMatchers(HttpMethod.GET, "/swagger-ui").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/*").permitAll().anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults())).cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuer).build();
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration
                .setAllowedOrigins(List.of("http://localhost:5173", "https://snippet-dev.westus2.cloudapp.azure.com",
                        "https://snippet-searcher.brazilsouth.cloudapp.azure.com"));
        configuration.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
