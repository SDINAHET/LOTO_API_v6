package com.fdjloto.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {

    @GetMapping("/admin-login")
    public String adminLoginPage() {
        // Va chercher: src/main/resources/static/admin-login.html
        return "forward:/admin-login.html";
    }
}
