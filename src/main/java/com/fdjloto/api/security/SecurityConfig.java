package com.fdjloto.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer; // si tu utilises Customizer.withDefaults()
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;


// import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        return http
                // ✅ Autoriser les iframes depuis la même origine (Swagger dans ton dashboard)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                // .headers(headers -> headers
                //     .frameOptions(frame -> frame.sameOrigin()) // ✅ Autoriser les iframes depuis la même origine
                //     .xssProtection(xss -> xss.disable()) // ✅ Désactiver la protection XSS si nécessaire
                // )
                // .csrf(csrf -> csrf.disable()) // 🔴 Désactive CSRF pour les APIs REST stateless
                // .csrf(AbstractHttpConfigurer::disable) // ✅ Version optimisée
                // .anonymous(anonymous -> anonymous.disable()) // Supprime l'authentification anonyme
                // .cors(cors -> cors.disable()) // 🔴 Désactive CORS (ajoute une config si nécessaire)
                // .cors(cors -> {}) // ✅ Active CORS, configuration à venir
                .csrf(csrf -> csrf
                    // ✅ CSRF token dans un cookie "XSRF-TOKEN" lisible par le front
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(requestHandler)

                    // ✅ évite de casser login/register au début
                    // (tu peux ensuite décider de les protéger aussi, mais d’abord: stable)
                    .ignoringRequestMatchers(
                        // "/api/auth/csrf",          // ✅ AJOUT IMPORTANT
                        "/api/auth/refresh",       // ✅ recommandé
                        "/api/auth/logout",   // ✅ AJOUTE ÇA
                        "/api/auth/login3",
                        "/api/auth/register",
                        "/api/auth/login4",
                        "/api/health",
                        "/api/hello",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/analytics/**",
                        "/api/auth/login-swagger",
                        "/admin-login.html"
                    )
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // .httpBasic(httpBasic -> httpBasic.disable()) // 🔴 Désactive l'authentification basique
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 🔴 JWT = stateless
                .exceptionHandling(ex -> ex
                    // Non authentifié (401)
                    .authenticationEntryPoint((request, response, authException) -> {
                        String uri = request.getRequestURI();
                        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
                            // 🔁 Redirection vers la page de login admin
                            response.sendRedirect("/admin-login.html");
                        } else {
                            // 🔁 Réponse JSON standard pour les appels API
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        }
                    })
                    // Authentifié mais pas le bon rôle (403)
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        String uri = request.getRequestURI();
                        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
                            response.sendRedirect("/admin-login.html");
                        } else {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Forbidden\"}");
                        }
                    })
                )

                .authorizeHttpRequests(auth -> auth
                        // =====================
                        // 🔓 PUBLIC ENDPOINTS
                        // =====================
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(
                            "/api/health",
                            "/api/hello"
                        ).permitAll()

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/visits/**").permitAll()
                        // .requestMatchers(
                        //     "/favicon.ico",
                        //     "/favicon-admin.ico",
                        //     "/admin-32.png",
                        //     "/admin-180.png",
                        //     "/admin.png"
                        // ).permitAll()


                        // ✅ CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/analytics/**").permitAll()


                        // --- AUTH PUBLIC ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login3").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 🔓 Pages d’erreur accessibles à tout le monde
                        .requestMatchers("/errors/**", "/401", "/403", "/404", "/500").permitAll()
                        .requestMatchers("/admin-login.html").permitAll()

                        // 🔓 le HTML du dashboard peut être public, les vraies données restent derrière /api/admin/**
                        .requestMatchers("/admin/**").permitAll()

                        // Swagger UI accessible sans authentification
                        // .requestMatchers("/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/v1/api-docs/**", "/swagger-ui.html", "/login-swagger").permitAll() // ✅ Swagger accessible sans JWT
                        // 🔒 Swagger accessible uniquement aux ADMIN
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v1/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html"
                        ).hasRole("ADMIN")

                        // .requestMatchers("/api/health").permitAll()
                        // Auth API accessible sans authentification
                        .requestMatchers("/api/hello", "/localhost:5500/**", "/api/loto/scrape").permitAll()
                        // Endpoints protégés par JWT
                        // .requestMatchers("/api/protected/**").permitAll()
                        // .requestMatchers("/api/tickets/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/**").hasAnyRole("ADMIN", "USER") // 🔥 GET accessible aux admins et utilisateurs
                        // .requestMatchers(HttpMethod.POST, "/api/tickets/**").hasAnyRole("ADMIN", "USER") // 🔥 POST accessible aux admins et utilisateurs
                        //.requestMatchers(HttpMethod.POST, "/api/tickets/**").permitAll() // 🔥 POST accessible tout le monde
                        .requestMatchers(HttpMethod.POST, "/api/tickets/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/**").hasAnyRole("ADMIN", "USER") // 🔥 PUT accessible aux admins et utilisateurs
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasAnyRole("ADMIN", "USER") // 🔥 PUT accessible aux admins et utilisateurs
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "USER") // 🔥 GET accessible aux admins et utilisateurs
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyRole("ADMIN", "USER") // 🔥 POST accessible aux admins et utilisateurs
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN", "USER") // 🔥 PUT accessible aux admins et utilisateurs
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("ADMIN", "USER") // 🔥 PUT accessible aux admins et utilisateurs
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasRole("ADMIN") // 🔥 DELETE réservé aux admins
                        // .requestMatchers("/api/tickets/**", "/api/tickets", "/api/tickets/{ticketId}").hasAnyRole("USER", "ADMIN") // 🔐 Accès USER et ADMIN
                        .requestMatchers("/api/historique/last20").permitAll()
                        .requestMatchers("/api/predictions/generate", "/api/generate", "/api/predictions/latest").permitAll()
                        .requestMatchers("/api/historique/last20/Detail/**").permitAll()
                        .requestMatchers("/api/tirages", "/api/tirages/**").permitAll()
                        .requestMatchers("/api/gains/calculate", "/api/gains","/api/gains/**").hasAnyRole("ADMIN", "USER") // 🔥 PUT accessible aux admins et utilisateurs
                        // .requestMatchers("/api/users/**", "/api/users").authenticated()  // Protégé par JWT
                        // .requestMatchers("/api/users/**").hasRole("ADMIN")

                        /* ========== 💾 ADMIN CRUD MINI-HEIDISQL ========== */
                        .requestMatchers(HttpMethod.GET,    "/api/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,    "/api/admin/tickets/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/admin/tickets/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/admin/tickets/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/tickets/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,    "/api/admin/ticket-gains/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/admin/ticket-gains/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/admin/ticket-gains/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/ticket-gains/**").hasRole("ADMIN")

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 🔐 Accès ADMIN
                        // .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN") // 🔐 Accès USER et ADMIN
                        // .requestMatchers("/api/users/**").hasRole("ADMIN")  // 🔐 Accès ADMIN
                        // .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN") // 🔐 Accès USER et ADMIN
                        // .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        // // 🔐 Accès USER et ADMIN
                        // .requestMatchers("/api/user/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        // .requestMatchers("/api/protected/userinfo").hasAuthority("SCOPE_user") // Vérifie si l'utilisateur a le bon scope
                        // .requestMatchers("/api/user/**").authenticated()  // Protégé par JWT
                        .requestMatchers("/api/protected/**").authenticated()  // Protégé par JWT
                        .anyRequest().authenticated()
                )
                // .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
                // .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 🔴 JWT = stateless
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // 🔐 Ajoute le filtre JWT
                // .httpBasic(httpBasic -> {})   // ✅ Active HTTP Basic (popup login/mdp du navigateur)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // configuration.setAllowedOriginPatterns(List.of(
        //     "http://localhost:*",
        //     "http://127.0.0.1:*",
            // "http://192.168.*.*:*",
        configuration.setAllowedOrigins(List.of(
            "http://127.0.0.1:5500", //live server
            "https://stephanedinahet.fr",
            // "http://localhost:8082", // add sd
	        "http://localhost:5500", // add sd
            "https://www.stephanedinahet.fr", // add sd
            "https://loto-api-black.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
            "Content-Type",
            "Authorization",
            "X-XSRF-TOKEN",
            "X-Requested-With",
            "Cache-Control",
            "Pragma"
        ));
        configuration.setExposedHeaders(List.of("Set-Cookie"));

        // configuration.setAllowedHeaders(List.of("*"));
        // configuration.setAllowedHeaders(List.of("Content-Type","Authorization","X-Requested-With"));
	configuration.setAllowCredentials(true); // Important pour cookies JWT

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
