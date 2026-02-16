package com.fdjloto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // 🔥 Autorise les requêtes vers les endpoints API
                        // .allowedOrigins("http://127.0.0.1:5500", "http://localhost:8082") // 🔥 Autorise les requêtes depuis le frontend
                        .allowedOrigins(
                            "http://127.0.0.1:5500",
                            "http://localhost:5500",
                            "https://stephanedinahet.fr",
                            "https://www.stephanedinahet.fr",
                            "https://loto-tracker.fr"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*") // 🔥 Autorise tous les headers
                        .exposedHeaders("Set-Cookie") // 🔑 Important pour voir le cookie dans le navigateur
                        .allowCredentials(true); // 🔥 Autorise l'envoi des cookies et headers d'authentification
            }
        };
    }
}
