package com.fdjloto.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class CspNonceFilter extends OncePerRequestFilter {

    public static final String ATTR_NAME = "cspNonce";
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // nonce = base64 (sans caractères chelous)
        byte[] bytes = new byte[16];
        RNG.nextBytes(bytes);
        // String nonce = Base64.getEncoder().encodeToString(bytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        request.setAttribute(ATTR_NAME, nonce);

        filterChain.doFilter(request, response);
    }
}
