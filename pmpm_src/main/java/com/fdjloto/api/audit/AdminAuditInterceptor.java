package com.fdjloto.api.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

/**
 * AdminAuditInterceptor
 * - Logs every non-GET call on /api/admin/**
 * - RGPD: does NOT log JWT, passwords, or full emails.
 * - Logs: admin principal (name), roles, method, path, status, timestamp, masked IP.
 */
public class AdminAuditInterceptor implements HandlerInterceptor {

    private static final Logger ADMIN_AUDIT = LoggerFactory.getLogger("ADMIN_AUDIT");

    private String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return "-";
        String[] p = ip.split("\\.");
        if (p.length == 4) return p[0] + "." + p[1] + ".xxx.xxx";
        return "-";
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) return "-";
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String dom = parts[1];
        String l = local.length() <= 2 ? local.charAt(0) + "*" : local.substring(0, 2) + "***";
        return l + "@" + dom;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("_audit_start", Instant.now());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (!uri.startsWith("/api/admin/")) return;
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) return;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = (auth == null) ? "-" : auth.getName();
        String roles = (auth == null) ? "-" : auth.getAuthorities().toString();

        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) ip = ip.split(",")[0].trim();
        else ip = request.getRemoteAddr();

        MDC.put("admin", maskEmail(principal));
        MDC.put("roles", roles);
        MDC.put("method", method);
        MDC.put("path", uri);
        MDC.put("status", String.valueOf(response.getStatus()));
        MDC.put("ipMasked", maskIp(ip));

        ADMIN_AUDIT.info("ADMIN_API_CHANGE");

        MDC.clear();
    }
}
