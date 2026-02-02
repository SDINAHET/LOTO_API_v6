package com.fdjloto.api.controller;

import com.fdjloto.api.model.LoginRequest;
import com.fdjloto.api.model.User;
import com.fdjloto.api.repository.RefreshTokenRepository;
import com.fdjloto.api.repository.UserRepository;
import com.fdjloto.api.security.JwtUtils;
import com.fdjloto.api.service.RefreshTokenService;
import com.fdjloto.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication Controller (AuthController)
 *
 * RGPD-friendly session logging:
 * - Never logs JWT
 * - Never logs email
 * - Logs only: userId(UUID), sessionId(UUID serveur), IP masquée, durée
 */
@Tag(name = "Authentication", description = "Endpoints for user authentication, login, logout, and account management.")
@CrossOrigin(
        origins = {"http://127.0.0.1:5500", "http://localhost:5500"},
        allowCredentials = "true"
)
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String JWT_COOKIE_NAME = "jwtToken";
    private static final String SID_COOKIE_NAME = "sid";
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private static final int ACCESS_MAX_AGE_SEC = 10 * 60;              // 10 min (déjà ton cas)
    private static final int REFRESH_MAX_AGE_SEC = 30 * 24 * 60 * 60;   // 30 jours

    // ✅ Logger dédié au fichier sessions.log (logback: logger name="SESSION_LOG")
    private static final Logger SESSION_LOG = LoggerFactory.getLogger("SESSION_LOG");

    // Phase 1 (sans DB) : sessionId -> instant login (perdu au reboot)
    private final ConcurrentHashMap<String, Instant> loginTimes = new ConcurrentHashMap<>();

    // 🔐 Gestion des tentatives de login admin (Swagger)
    private final ConcurrentHashMap<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository,
                          RefreshTokenService refreshTokenService,
                          RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // -------------------------
    // RGPD helper: mask IP
    // -------------------------
    private String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return "-";
        // IPv4 simple masking: a.b.xxx.xxx
        String[] p = ip.split("\\.");
        if (p.length == 4) return p[0] + "." + p[1] + ".xxx.xxx";
        // IPv6 / autres: on masque tout
        return "-";
    }

    // Si tu es derrière Apache/Nginx, préfère X-Forwarded-For
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // Le premier = IP client
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "User login with JWT", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful login, returns JWT token."),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials.")
    })
    @PostMapping("/login4")
    public ResponseEntity<String> authenticateUser(@RequestParam String email, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication); // compat: access token
        return ResponseEntity.ok(jwt);
    }

    /**
     * ✅ LOGIN3 RGPD-friendly
     * - No email in logs
     * - No JWT in logs
     * - Creates a server-side sessionId (sid cookie) to link login/logout and compute duration
     *
     * NOTE sécurité: idéalement, ne renvoie pas le token dans le body (LocalStorage = risque XSS).
     * Je conserve ton comportement actuel (token renvoyé) parce que ton front semble l'utiliser.
     */
    @Operation(
            summary = "User login with JWT stored in cookies",
            description = "Authenticates a user and stores JWT in a secure cookie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful login, JWT stored in a secure cookie."),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials.")
    })
    @PostMapping("/login3")
    public ResponseEntity<Map<String, String>> authenticateUserWithCookieAndLocalStorage(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        try {
            // 🔐 Authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 🔑 Tokens (NE PAS LOGGER)
            String accessToken = jwtUtils.generateAccessToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getEmail());

            // ✅ Get userId (UUID pseudonymisé) depuis la DB
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String userId = user.getId().toString();

            // ✅ Save refresh token (hashé) en DB
            Instant refreshExpiresAt = Instant.now().plusSeconds(REFRESH_MAX_AGE_SEC);
            refreshTokenService.save(user, refreshToken, refreshExpiresAt);

            // ✅ Session serveur (pas le JWT)
            String sessionId = UUID.randomUUID().toString();

            // ✅ IP masquée
            String ipMasked = maskIp(getClientIp(request));

            // ✅ Save login time (phase 1)
            loginTimes.put(sessionId, Instant.now());

            // ✅ SESSION LOG (RGPD)
            MDC.put("userId", userId);
            MDC.put("sessionId", sessionId);
            MDC.put("ipMasked", ipMasked);
            SESSION_LOG.info("LOGIN_SUCCESS");
            MDC.clear();

            // 🍪 Access token cookie
            Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, accessToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);     // true en prod HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(ACCESS_MAX_AGE_SEC);
            response.addCookie(jwtCookie);

            // 🍪 Refresh token cookie
            Cookie refreshCookie = new Cookie(REFRESH_COOKIE_NAME, refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);     // true en prod HTTPS
            refreshCookie.setPath("/api/auth"); // refresh + logout
            refreshCookie.setMaxAge(REFRESH_MAX_AGE_SEC);
            response.addCookie(refreshCookie);

            // 🍪 sid cookie (technique)
            Cookie sidCookie = new Cookie(SID_COOKIE_NAME, sessionId);
            sidCookie.setHttpOnly(true);
            sidCookie.setSecure(false);
            sidCookie.setPath("/api/auth");
            sidCookie.setMaxAge(ACCESS_MAX_AGE_SEC);
            response.addCookie(sidCookie);

            // SameSite explicite (DEV)
            response.addHeader("Set-Cookie",
                    JWT_COOKIE_NAME + "=" + accessToken + "; HttpOnly; Path=/; Max-Age=" + ACCESS_MAX_AGE_SEC + "; SameSite=Lax");
            response.addHeader("Set-Cookie",
                    REFRESH_COOKIE_NAME + "=" + refreshToken + "; HttpOnly; Path=/api/auth; Max-Age=" + REFRESH_MAX_AGE_SEC + "; SameSite=Lax");
            response.addHeader("Set-Cookie",
                    SID_COOKIE_NAME + "=" + sessionId + "; HttpOnly; Path=/api/auth; Max-Age=" + ACCESS_MAX_AGE_SEC + "; SameSite=Lax");

            // JSON response (si ton front stocke le token)
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("token", accessToken);
            responseBody.put("message", "Login successful");
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            // ✅ Log échec sans email (RGPD)
            SESSION_LOG.warn("LOGIN_FAIL");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Login failed"));
        }
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with encrypted password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully."),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data.")
    })
    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAdmin(false);
        return ResponseEntity.ok(userService.createUser(user));
    }

    @Operation(
            summary = "Update a user",
            description = "Updates the details of an existing user by providing their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully updated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @Operation(
            summary = "Check AuthController status",
            description = "Returns a simple message to verify that the AuthController is operational.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AuthController is operational",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\"message\": \"AuthController OK\"}"))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("message", "AuthController OK"));
    }

    @Operation(
            summary = "Get JWT from cookie",
            description = "Retrieves the JWT token stored in the 'jwtToken' cookie.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JWT successfully retrieved",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\"jwtToken\": \"<token>\"}"))),
                    @ApiResponse(responseCode = "401", description = "JWT missing",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\"message\": \"JWT manquant\"}"))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getJwtFromCookie(
            @Parameter(name = "jwtToken", required = false, in = ParameterIn.COOKIE)
            @CookieValue(name = JWT_COOKIE_NAME, required = false) String jwtToken
    ) {
        if (jwtToken == null || jwtToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "JWT manquant"));
        }
        return ResponseEntity.ok(Map.of("jwtToken", jwtToken));
    }

    @Operation(summary = "Get authenticated user info", description = "Retrieves user details based on JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT."),
            @ApiResponse(responseCode = "404", description = "User not found.")
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getUserInfo(
            @CookieValue(name = JWT_COOKIE_NAME, required = false) String token
    ) {
        if (token == null || !jwtUtils.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String email = jwtUtils.getUserFromJwtToken(token);
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        User u = user.get();
        Map<String, String> res = new HashMap<>();
        res.put("id", u.getId().toString());
        res.put("email", email); // ⚠️ API response (pas log) : OK si ton app en a besoin
        res.put("first_name", u.getFirstName());
        res.put("last_name", u.getLastName());
        res.put("message", "User authenticated");
        return ResponseEntity.ok(res);
    }

    /**
     * Admin login for Swagger (avec blocage après 3 tentatives)
     */
    @Operation(
            summary = "Admin login for Swagger",
            description = "Authenticates an admin user and sets a JWT cookie to access Swagger UI."
    )
    @PostMapping("/login-swagger")
    public ResponseEntity<Map<String, String>> authenticateUserForSwagger(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        String email = loginRequest.getEmail();

        try {
            int attempts = loginAttempts.getOrDefault(email, 0);
            if (attempts >= MAX_ATTEMPTS) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Trop de tentatives. Accès bloqué."));
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès réservé aux administrateurs"));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            loginAttempts.remove(email);

            String jwt = jwtUtils.generateJwtToken(authentication);

            Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // true en prod
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(ACCESS_MAX_AGE_SEC);
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(Map.of("token", jwt, "message", "Admin login successful"));

        } catch (UsernameNotFoundException e) {
            int newAttempts = loginAttempts.getOrDefault(email, 0) + 1;
            loginAttempts.put(email, newAttempts);

            if (newAttempts >= MAX_ATTEMPTS) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Trop de tentatives. Accès bloqué."));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Utilisateur introuvable",
                            "tentativesRestantes", String.valueOf(MAX_ATTEMPTS - newAttempts)));
        } catch (Exception e) {
            int newAttempts = loginAttempts.getOrDefault(email, 0) + 1;
            loginAttempts.put(email, newAttempts);

            if (newAttempts >= MAX_ATTEMPTS) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Trop de tentatives. Accès bloqué."));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Identifiants invalides",
                            "tentativesRestantes", String.valueOf(MAX_ATTEMPTS - newAttempts)));
        }
    }

    /**
     * ✅ LOGOUT RGPD-friendly (avec durée)
     * - Uses sid cookie to compute duration
     * - Retrieves userId via token ONLY to log userId (token is never logged)
     */
    @Operation(summary = "User logout", description = "Clears JWT cookie and logs out the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User logged out successfully.")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutWithCookie(
            HttpServletResponse response,
            HttpServletRequest request,
            @CookieValue(name = SID_COOKIE_NAME, required = false) String sid,
            @CookieValue(name = JWT_COOKIE_NAME, required = false) String token,
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken
    ) {
        SecurityContextHolder.clearContext();

        // 🔒 Revoke refresh token in DB (RNCP6)
        if (refreshToken != null && !refreshToken.isBlank()) {
            String hash = refreshTokenService.hash(refreshToken);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(refreshTokenService::revoke);
        }

        if (sid != null && !sid.isBlank()) {
            Instant start = loginTimes.remove(sid);
            long durationSec = (start == null) ? -1 : Duration.between(start, Instant.now()).getSeconds();

            // userId via token (never log token)
            String userId = "-";
            if (token != null && jwtUtils.validateAccessToken(token)) {
                String email = jwtUtils.getUserFromJwtToken(token);
                Optional<User> u = userRepository.findByEmail(email);
                if (u.isPresent()) userId = u.get().getId().toString();
            }

            String ipMasked = maskIp(getClientIp(request));

            MDC.put("userId", userId);
            MDC.put("sessionId", sid);
            MDC.put("ipMasked", ipMasked);

            if (durationSec >= 0) {
                SESSION_LOG.info("LOGOUT durationSec={}", durationSec);
            } else {
                SESSION_LOG.info("LOGOUT durationSec=unknown");
            }

            MDC.clear();
        }

        // Clear SID cookie
        Cookie sidCookie = new Cookie(SID_COOKIE_NAME, null);
        sidCookie.setHttpOnly(true);
        sidCookie.setSecure(false); // true en prod
        sidCookie.setPath("/api/auth");
        sidCookie.setMaxAge(0);
        response.addCookie(sidCookie);
        response.addHeader("Set-Cookie", SID_COOKIE_NAME + "=; HttpOnly; Path=/api/auth; Max-Age=0; SameSite=Lax");

        // Clear JWT cookie
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // true en prod
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        response.addHeader("Set-Cookie", JWT_COOKIE_NAME + "=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax");

        // Clear Refresh cookie
        Cookie refreshCookie = new Cookie(REFRESH_COOKIE_NAME, null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // true en prod
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
        response.addHeader("Set-Cookie", REFRESH_COOKIE_NAME + "=; HttpOnly; Path=/api/auth; Max-Age=0; SameSite=Lax");

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            HttpServletResponse response,
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank() || !jwtUtils.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid refresh token"));
        }

        String email = jwtUtils.getUserFromJwtToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Vérif DB (hashé + pas révoqué + pas expiré)
        String hash = refreshTokenService.hash(refreshToken);
        var rtOpt = refreshTokenRepository.findByTokenHash(hash);

        if (rtOpt.isEmpty() || rtOpt.get().isRevoked() || rtOpt.get().getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh token expired/revoked"));
        }

        // Rotation
        refreshTokenService.revoke(rtOpt.get());
        String newRefresh = jwtUtils.generateRefreshToken(email);
        refreshTokenService.save(user, newRefresh, Instant.now().plusSeconds(REFRESH_MAX_AGE_SEC));

        // Nouveau access
        List<String> roles = List.of(user.getRole());
        String newAccess = jwtUtils.generateAccessTokenFromEmailAndRoles(email, roles);

        // Set cookies (addCookie + SameSite header)
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, newAccess);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // true en prod
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(ACCESS_MAX_AGE_SEC);
        response.addCookie(jwtCookie);

        Cookie refreshCookie = new Cookie(REFRESH_COOKIE_NAME, newRefresh);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // true en prod
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(REFRESH_MAX_AGE_SEC);
        response.addCookie(refreshCookie);

        response.addHeader("Set-Cookie",
                JWT_COOKIE_NAME + "=" + newAccess + "; HttpOnly; Path=/; Max-Age=" + ACCESS_MAX_AGE_SEC + "; SameSite=Lax");
        response.addHeader("Set-Cookie",
                REFRESH_COOKIE_NAME + "=" + newRefresh + "; HttpOnly; Path=/api/auth; Max-Age=" + REFRESH_MAX_AGE_SEC + "; SameSite=Lax");

        return ResponseEntity.ok(Map.of("token", newAccess, "message", "Token refreshed"));
    }
}
