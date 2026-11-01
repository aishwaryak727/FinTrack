package com.fintrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration permitting open access to API endpoints and enabling
 * H2 in-memory console framing for local development.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the main application security filter chain.
     *
     * @param http HttpSecurity configuration builder
     * @return configured SecurityFilterChain
     * @throws Exception if security filter configuration fails
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
