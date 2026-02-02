package com.fdjloto.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
public class SecurityLogController {

    private static final Logger securityLog =
            LoggerFactory.getLogger("SECURITY_LOG");

    // Endpoint de test : appelle-le une fois pour vérifier security.log
    @PostMapping("/event")
    public ResponseEntity<Void> logSecurityEvent(@RequestParam String type,
                                                 @RequestParam(required = false) String user,
                                                 @RequestParam(required = false) String reason,
                                                 HttpServletRequest request) {

        String ip = extractClientIp(request);
        String ua = request.getHeader("User-Agent");

        // WARN obligatoire pour être écrit (vu ton logback)
        securityLog.warn(
            "security_event={} user={} reason={} ip={} ua=\"{}\" path={}",
            safe(type),
            safe(user),
            safe(reason),
            ip,
            safe(ua),
            request.getRequestURI()
        );

        return ResponseEntity.ok().build();
    }

    private static String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = req.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return req.getRemoteAddr();
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s.replace("\n"," ").replace("\r"," ");
    }
}
