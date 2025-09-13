package com.jordi.booknook.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity // Enables @PreAuthorize etc. (e.g., SCOPE_/ROLE_ checks)
public class SecurityConfig {

    // CORS for your Vite dev server. Keep concrete origin if allowCredentials=true.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless API: no CSRF tokens/cookies
                .csrf(AbstractHttpConfigurer::disable)
                // Apply the CORS rules above
                .cors(Customizer.withDefaults())
                // Absolutely no HTTP session; every request must bring a Bearer token
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Public endpoints
                        .requestMatchers(
                                "/actuator/health", "/actuator/info",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/shelves/public/user/**",   // use ant pattern (no {user_id} syntax here)
                                "/api/v1/books"                     // add leading slash
                        ).permitAll()
                        // 🔒 Everything else requires a valid Bearer token from Entra ID
                        .anyRequest().authenticated()
                )
                // Disable legacy interactive auth mechanisms
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // ✅ The key line: enable JWT Resource Server (Nimbus) — validates tokens from issuer-uri
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        // No custom AuthenticationEntryPoint/AccessDeniedHandler:
        // Spring will return 401 (WWW-Authenticate: Bearer) or 403 as appropriate.

        return http.build();
    }
}
