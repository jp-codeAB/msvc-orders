package com.springcloud.msvc_items.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayAuthFilter.class); // 💡 Logger añadido

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String USER_EMAIL_HEADER = "X-User-Email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String userRolesHeader = request.getHeader(USER_ROLES_HEADER);
        String userEmailHeader = request.getHeader(USER_EMAIL_HEADER);

        // 💡 DEBUG: Registrar los headers recibidos antes de validar
        log.info("Headers recibidos - ID: {}, Roles: {}, Email: {}", userIdHeader, userRolesHeader, userEmailHeader);

        // Su lógica de validación estricta (que ya tiene)
        if (userIdHeader == null || userRolesHeader == null || userIdHeader.isBlank() || userRolesHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthUser authUser = new AuthUser(userIdHeader, userEmailHeader, userRolesHeader);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authUser,
                    null,
                    authUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 💡 DEBUG: Registrar el rol que se creó en el contexto de seguridad
            log.info("Roles asignados al AuthUser: {}", authUser.getAuthorities());

        } catch (Exception e) {
            log.error("Error creando AuthUser: " + e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }
}