package com.fdjloto.api.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers AdminAuditInterceptor for /api/admin/**
 */
@Configuration
public class AdminAuditWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuditInterceptor())
                .addPathPatterns("/api/admin/**");
    }
}
