package com.fdjloto.api.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class AdminDebugController {

    // @GetMapping("/admin/ping")
    @GetMapping("/api/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> ping(
            @CookieValue(name = "jwtToken", required = false) String jwtCookie,
            Authentication authentication
    ) {
        // IMPORTANT: ne jamais logger le token complet
        boolean cookiePresent = (jwtCookie != null && !jwtCookie.isBlank());

        List<String> roles = authentication == null
                ? List.of()
                : authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

        String user = authentication == null ? null : authentication.getName();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "authenticated", authentication != null && authentication.isAuthenticated(),
                "jwtCookiePresent", cookiePresent,
                "user", user,
                "roles", roles,
                "now", OffsetDateTime.now().toString()
        ));
    }
}
