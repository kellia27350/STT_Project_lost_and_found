package com.auca.lostandfound.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * Rules (in priority order):
 *  1. Auth endpoints (/api/auth/**) are open to everyone — no token needed.
 *  2. Admin endpoints (/api/admin/**) require ADMIN role (IT-14, IT-15).
 *  3. All other /api/** endpoints require at least STUDENT role.
 *  4. CSRF is disabled — this is a stateless REST API consumed by Android.
 *  5. Session is STATELESS — authentication is re-established from headers
 *     on every request via HeaderAuthFilter.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless API, no browser sessions
            .csrf(csrf -> csrf.disable())

            // Stateless — no HttpSession created or used
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Insert our header-reading filter before Spring's default auth filter
            .addFilterBefore(new HeaderAuthFilter(),
                             UsernamePasswordAuthenticationFilter.class)

            .authorizeHttpRequests(auth -> auth

                // Public: registration and login
                .requestMatchers("/api/auth/**").permitAll()

                // H2 console (dev / test only)
                .requestMatchers("/h2-console/**").permitAll()

                // Public read: browsing and searching items requires no login
                .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/items").permitAll()

                // Admin-only moderation endpoints (IT-14 / IT-15)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Everything else under /api requires an authenticated user
                .requestMatchers("/api/**").hasAnyRole("STUDENT", "ADMIN")

                // Catch-all for non-API paths (Actuator, etc.)
                .anyRequest().permitAll()
            )

            // Allow H2 console to render frames (test/dev convenience)
            .headers(headers ->
                headers.frameOptions(fo -> fo.sameOrigin()));

        return http.build();
    }

    /**
     * BCrypt password encoder — used in UserService and AuthController.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
