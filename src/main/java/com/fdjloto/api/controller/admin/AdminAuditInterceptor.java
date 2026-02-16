package com.fdjloto.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuditInterceptor implements HandlerInterceptor {

    private static final Logger AUDIT = LoggerFactory.getLogger("ADMIN_AUDIT");

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        String path = req.getRequestURI();
        if (!path.startsWith("/api/admin/")) return;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String admin = (auth != null ? auth.getName() : "anonymous");

        AUDIT.info("admin={} method={} path={} status={} ip={}",
                admin, req.getMethod(), path, res.getStatus(), req.getRemoteAddr());
    }
}
