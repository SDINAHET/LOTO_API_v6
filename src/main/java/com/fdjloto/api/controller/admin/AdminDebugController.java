// package com.fdjloto.api.controller.admin;

// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.web.bind.annotation.CookieValue;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.security.access.prepost.PreAuthorize;

// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.Map;

// @RestController
// public class AdminDebugController {

//     // @GetMapping("/admin/ping")
//     @GetMapping("/api/admin/ping")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<Map<String, Object>> ping(
//             @CookieValue(name = "jwtToken", required = false) String jwtCookie,
//             Authentication authentication
//     ) {
//         // IMPORTANT: ne jamais logger le token complet
//         boolean cookiePresent = (jwtCookie != null && !jwtCookie.isBlank());

//         List<String> roles = authentication == null
//                 ? List.of()
//                 : authentication.getAuthorities().stream()
//                     .map(GrantedAuthority::getAuthority)
//                     .toList();

//         String user = authentication == null ? null : authentication.getName();

//         return ResponseEntity.ok(Map.of(
//                 "status", "UP",
//                 "authenticated", authentication != null && authentication.isAuthenticated(),
//                 "jwtCookiePresent", cookiePresent,
//                 "user", user,
//                 "roles", roles,
//                 "now", OffsetDateTime.now().toString()
//         ));
//     }


//     // @GetMapping("/api/admin/auth-info")
//     // @PreAuthorize("hasRole('ADMIN')")
//     // public ResponseEntity<Map<String, Object>> authInfo(Authentication authentication) {

//     //     List<String> roles = authentication == null
//     //             ? List.of()
//     //             : authentication.getAuthorities().stream()
//     //                 .map(GrantedAuthority::getAuthority)
//     //                 .toList();

//     //     return ResponseEntity.ok(Map.of(
//     //             "authenticated", authentication != null && authentication.isAuthenticated(),
//     //             "user", authentication != null ? authentication.getName() : null,
//     //             "roles", roles,
//     //             "authClass", authentication != null ? authentication.getClass().getSimpleName() : null,
//     //             "timestamp", OffsetDateTime.now().toString()
//     //     ));
//     // }


//     // @GetMapping("/api/admin/health")
//     // @PreAuthorize("hasRole('ADMIN')")
//     // public ResponseEntity<Map<String, Object>> health() {

//     //     Runtime rt = Runtime.getRuntime();

//     //     return ResponseEntity.ok(Map.of(
//     //             "status", "UP",
//     //             "serverTime", OffsetDateTime.now().toString(),
//     //             "memoryUsedMB", (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024,
//     //             "memoryMaxMB", rt.maxMemory() / 1024 / 1024,
//     //             "processors", rt.availableProcessors()
//     //     ));
//     // }


//     // @GetMapping("/api/admin/runtime")
//     // @PreAuthorize("hasRole('ADMIN')")
//     // public ResponseEntity<Map<String, Object>> runtime() {

//     //     return ResponseEntity.ok(Map.of(
//     //             "javaVersion", System.getProperty("java.version"),
//     //             "javaVendor", System.getProperty("java.vendor"),
//     //             "osName", System.getProperty("os.name"),
//     //             "osArch", System.getProperty("os.arch"),
//     //             "osVersion", System.getProperty("os.version"),
//     //             "timezone", System.getProperty("user.timezone")
//     //     ));
//     // }


//     // @GetMapping("/api/admin/request-context")
//     // @PreAuthorize("hasRole('ADMIN')")
//     // public ResponseEntity<Map<String, Object>> requestContext(
//     //         jakarta.servlet.http.HttpServletRequest request
//     // ) {
//     //     return ResponseEntity.ok(Map.of(
//     //             "method", request.getMethod(),
//     //             "uri", request.getRequestURI(),
//     //             "remoteAddr", request.getRemoteAddr(),
//     //             "remoteHost", request.getRemoteHost(),
//     //             "userAgent", request.getHeader("User-Agent"),
//     //             "referer", request.getHeader("Referer")
//     //     ));
//     // }


//     // @GetMapping("/api/admin/cookies")
//     // @PreAuthorize("hasRole('ADMIN')")
//     // public ResponseEntity<Map<String, Object>> cookies(
//     //         jakarta.servlet.http.HttpServletRequest request
//     // ) {
//     //     var cookies = request.getCookies();

//     //     if (cookies == null) {
//     //         return ResponseEntity.ok(Map.of("cookies", List.of()));
//     //     }

//     //     List<Map<String, Object>> result = List.of(cookies).stream()
//     //             .map(c -> Map.of(
//     //                     "name", c.getName(),
//     //                     "httpOnly", c.isHttpOnly(),
//     //                     "secure", c.getSecure(),
//     //                     "path", c.getPath()
//     //             ))
//     //             .toList();

//     //     return ResponseEntity.ok(Map.of("cookies", result));
//     // }


//     // private static final long START_TIME = System.currentTimeMillis();

//     // @GetMapping("/api/admin/uptime")
//     // @PreAuthorize("hasRole('ADMIN')")
//     // public ResponseEntity<Map<String, Object>> uptime() {

//     //     long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;

//     //     return ResponseEntity.ok(Map.of(
//     //             "uptimeSeconds", uptimeSeconds,
//     //             "uptimeMinutes", uptimeSeconds / 60,
//     //             "uptimeHours", uptimeSeconds / 3600
//     //     ));
//     // }



// }


package com.fdjloto.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Admin Debug", description = "Endpoints de supervision/diagnostic (ADMIN uniquement). Aucun secret n'est renvoyé.")
@SecurityRequirement(name = "jwtCookie") // à déclarer dans ta config OpenAPI (voir plus bas)
public class AdminDebugController {

    private static final long START_TIME = System.currentTimeMillis();

    @GetMapping(value = "/api/admin/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Ping admin",
            description = "Vérifie que l'utilisateur est authentifié (ADMIN), que le cookie JWT est présent, et retourne quelques infos de diagnostic."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "status": "UP",
                              "authenticated": true,
                              "jwtCookiePresent": true,
                              "user": "admin@fdjloto.fr",
                              "roles": ["ROLE_ADMIN"],
                              "now": "2026-02-08T20:53:59.172138274+01:00"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Authentifié mais pas ADMIN")
    })
    public ResponseEntity<Map<String, Object>> ping(
            @Parameter(description = "Cookie JWT (présence seulement, jamais renvoyé)", required = false)
            @CookieValue(name = "jwtToken", required = false) String jwtCookie,
            Authentication authentication
    ) {
        boolean cookiePresent = (jwtCookie != null && !jwtCookie.isBlank());

        List<String> roles = authentication == null
                ? List.of()
                : authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String user = authentication == null ? null : authentication.getName();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "authenticated", authentication != null && authentication.isAuthenticated(),
                "jwtCookiePresent", cookiePresent,
                "user", user,
                "roles", roles,
                "now", OffsetDateTime.now().toString()
        ));
    }

    @GetMapping(value = "/api/admin/auth-info", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Infos d'authentification",
            description = "Retourne l'utilisateur courant, les rôles, et le type d'objet Authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(examples = @ExampleObject(value = """
                    {
                      "authenticated": true,
                      "user": "admin@fdjloto.fr",
                      "roles": ["ROLE_ADMIN"],
                      "authClass": "UsernamePasswordAuthenticationToken",
                      "timestamp": "2026-02-08T23:40:00+01:00"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Pas ADMIN")
    })
    public ResponseEntity<Map<String, Object>> authInfo(Authentication authentication) {
        List<String> roles = authentication == null
                ? List.of()
                : authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(Map.of(
                "authenticated", authentication != null && authentication.isAuthenticated(),
                "user", authentication != null ? authentication.getName() : null,
                "roles", roles,
                "authClass", authentication != null ? authentication.getClass().getSimpleName() : null,
                "timestamp", OffsetDateTime.now().toString()
        ));
    }

    @GetMapping(value = "/api/admin/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Health étendu",
            description = "Santé backend + infos mémoire/processeurs (sans Actuator)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(examples = @ExampleObject(value = """
                    {
                      "status": "UP",
                      "serverTime": "2026-02-08T23:40:00+01:00",
                      "memoryUsedMB": 312,
                      "memoryMaxMB": 1024,
                      "processors": 8
                    }
                    """)))
    })
    public ResponseEntity<Map<String, Object>> health() {
        Runtime rt = Runtime.getRuntime();
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "serverTime", OffsetDateTime.now().toString(),
                "memoryUsedMB", (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024,
                "memoryMaxMB", rt.maxMemory() / 1024 / 1024,
                "processors", rt.availableProcessors()
        ));
    }

    @GetMapping(value = "/api/admin/runtime", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Runtime Java/OS",
            description = "Infos runtime (version Java, OS, timezone) utiles pour debug en prod."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(examples = @ExampleObject(value = """
                    {
                      "javaVersion": "21",
                      "javaVendor": "Eclipse Adoptium",
                      "osName": "Linux",
                      "osArch": "amd64",
                      "osVersion": "6.1.0",
                      "timezone": "Europe/Paris"
                    }
                    """)))
    })
    public ResponseEntity<Map<String, Object>> runtime() {
        return ResponseEntity.ok(Map.of(
                "javaVersion", System.getProperty("java.version"),
                "javaVendor", System.getProperty("java.vendor"),
                "osName", System.getProperty("os.name"),
                "osArch", System.getProperty("os.arch"),
                "osVersion", System.getProperty("os.version"),
                "timezone", System.getProperty("user.timezone")
        ));
    }

    @GetMapping("/api/admin/request-context")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> requestContext(
            @CookieValue(name = "jwtToken", required = false) String jwtToken, // juste pour Swagger
            jakarta.servlet.http.HttpServletRequest request
    ) {
        return ResponseEntity.ok(Map.of(
                "cookiePresent", jwtToken != null && !jwtToken.isBlank(),
                "method", request.getMethod(),
                "uri", request.getRequestURI(),
                "remoteAddr", request.getRemoteAddr(),
                "remoteHost", request.getRemoteHost(),
                "userAgent", request.getHeader("User-Agent"),
                "referer", request.getHeader("Referer")
        ));
    }

    public ResponseEntity<Map<String, Object>> requestContext(
            @Parameter(hidden = true) HttpServletRequest request
    ) {
        return ResponseEntity.ok(Map.of(
                "method", request.getMethod(),
                "uri", request.getRequestURI(),
                "remoteAddr", request.getRemoteAddr(),
                "remoteHost", request.getRemoteHost(),
                "userAgent", request.getHeader("User-Agent"),
                "referer", request.getHeader("Referer")
        ));
    }

    @GetMapping("/api/admin/cookies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cookies(
            @CookieValue(name = "jwtToken", required = false) String jwtToken, // juste pour Swagger
            jakarta.servlet.http.HttpServletRequest request
    ) {
        var cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return ResponseEntity.ok(Map.of("cookies", List.of()));
        }

        var result = java.util.Arrays.stream(cookies)
                .map(c -> Map.<String, Object>of(
                        "name", c.getName(),
                        "httpOnly", c.isHttpOnly(),
                        "secure", c.getSecure(),
                        "path", c.getPath()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "cookiePresent", jwtToken != null && !jwtToken.isBlank(),
                "cookies", result
        ));
    }

    public ResponseEntity<Map<String, Object>> cookies(
            @Parameter(hidden = true) HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return ResponseEntity.ok(Map.of("cookies", List.of()));
        }

        // ✅ Fix compilation: on force Map<String, Object>
        List<Map<String, Object>> result = Arrays.stream(cookies)
                .map(c -> Map.<String, Object>of(
                        "name", c.getName(),
                        "httpOnly", c.isHttpOnly(),
                        "secure", c.getSecure(),
                        "path", c.getPath()
                ))
                .toList();

        return ResponseEntity.ok(Map.of("cookies", result));
    }

    @GetMapping(value = "/api/admin/uptime", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Uptime application",
            description = "Temps depuis le démarrage de l'application."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(examples = @ExampleObject(value = """
                    {
                      "uptimeSeconds": 7420,
                      "uptimeMinutes": 123,
                      "uptimeHours": 2
                    }
                    """)))
    })
    public ResponseEntity<Map<String, Object>> uptime() {
        long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;
        return ResponseEntity.ok(Map.of(
                "uptimeSeconds", uptimeSeconds,
                "uptimeMinutes", uptimeSeconds / 60,
                "uptimeHours", uptimeSeconds / 3600
        ));
    }
}
