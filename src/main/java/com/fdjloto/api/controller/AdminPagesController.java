package com.fdjloto.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPagesController {

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        // sert: src/main/resources/static/admin/dashboard.html
        return "forward:/admin/dashboard.html";
    }
}
