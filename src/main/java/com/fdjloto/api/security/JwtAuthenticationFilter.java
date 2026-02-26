package com.fdjloto.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * **JWT Authentication Filter** - This filter is executed once per request (`OncePerRequestFilter`)
 * and is responsible for validating and setting the authentication context based on the JWT token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor for `JwtAuthenticationFilter`
     *
     * @param jwtUtils Utility class for handling JWT operations (validation, extraction).
     * @param userDetailsService Service to retrieve user details.
     */
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getServletPath();

            // Normalisation : supprime le slash final si présent (ex: /dernier-tirage/)
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }

            // 1) Ressources statiques
            boolean isStatic = PathRequest.toStaticResources()
                    .atCommonLocations()
                    .matches(request)
                    || path.startsWith("/assets")
                    || path.startsWith("/css")
                    || path.startsWith("/js")
                    || path.startsWith("/images")
                    || path.equals("/sitemap.xml")
                    || path.equals("/robots.txt");

            // 2) Endpoints publics
            boolean isPublic = path.equals("/dernier-tirage") || path.startsWith("/tirage");

            return isStatic || isPublic;
        }

    /**
     * **Main filter method** - Intercepts requests, extracts the JWT, validates it,
     * and sets the authentication in the Spring Security context.
     *
     * @param request Incoming HTTP request.
     * @param response HTTP response.
     * @param filterChain Chain of filters to execute next.
     * @throws ServletException If a servlet error occurs.
     * @throws IOException If an input/output error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 🔍 Look for the JWT in the Authorization header or the "jwtToken" cookie
        String token = getTokenFromHeader(request).orElseGet(() -> getTokenFromCookie(request).orElse(null));

        // 🔐 If a valid token is found, extract user details and set authentication
        // if (token != null && jwtUtils.validateJwtToken(token)) {
        if (token != null && jwtUtils.validateAccessToken(token)) {

            String username = jwtUtils.getUserFromJwtToken(token);

            // 🔥 Extract roles from the JWT token
            List<String> roles = jwtUtils.getRolesFromJwtToken(token);

            // 🔥 Convert roles to `GrantedAuthority`
            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 🔥 Create an authentication object with user details and roles
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 🔥 Set the authentication in the Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 🔄 Continue processing the request
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the **Authorization header** if present.
     *
     * @param request The incoming HTTP request.
     * @return An `Optional<String>` containing the JWT if found, otherwise empty.
     */
    private Optional<String> getTokenFromHeader(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return Optional.of(headerAuth.substring(7)); // Remove "Bearer " prefix
        }
        return Optional.empty();
    }

    /**
     * Extracts the JWT token from the **cookies** if present.
     *
     * @param request The incoming HTTP request.
     * @return An `Optional<String>` containing the JWT if found, otherwise empty.
     */
    private Optional<String> getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
        }
        return Optional.empty();
    }
}
