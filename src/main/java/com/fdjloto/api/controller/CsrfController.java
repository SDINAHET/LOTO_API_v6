package com.fdjloto.api.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class CsrfController {

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        // Appeler cet endpoint force Spring Security à générer le cookie XSRF-TOKEN
        return Map.of("token", token.getToken());
    }
}
