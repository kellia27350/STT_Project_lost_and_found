package com.auca.lostandfound.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the X-User-Id and X-User-Role headers injected by the Android client
 * (or forwarded from a JWT-validated gateway) and populates the Spring Security
 * context so downstream role checks work without a full OAuth2/JWT round-trip
 * inside the test suite.
 *
 * Header format expected:
 *   X-User-Id:   <numeric userId>
 *   X-User-Role: STUDENT | ADMIN
 *
 * This keeps the integration tests simple — they just set the two headers
 * rather than generating and sending a JWT on every request.
 */
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String role   = request.getHeader("X-User-Role");

        if (userId != null && !userId.isBlank()) {
            // Default to STUDENT when the role header is absent
            String grantedRole = "ROLE_" + (role != null && !role.isBlank()
                    ? role.toUpperCase()
                    : "STUDENT");

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority(grantedRole)));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
