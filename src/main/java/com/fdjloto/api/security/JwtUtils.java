// package com.fdjloto.api.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import io.jsonwebtoken.security.SignatureException;
// import io.jsonwebtoken.security.WeakKeyException;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.stereotype.Component;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;
// import java.util.List;
// import java.util.stream.Collectors;

// /**
//  * JWT Utility Class for handling JSON Web Tokens.
//  * Handles generation, validation and extraction of claims.
//  */
// @Component
// public class JwtUtils {

//     private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
//     private static final String CLAIM_TOKEN_TYPE = "type";
//     private static final String TOKEN_TYPE_ACCESS = "access";
//     private static final String TOKEN_TYPE_REFRESH = "refresh";

//     // Refresh token TTL (ex: 30 jours)
//     private static final long REFRESH_TOKEN_EXP_MS = 30L * 24 * 60 * 60 * 1000;


//     @Value("${app.jwtSecret}")
//     private String jwtSecret;

//     @Value("${app.jwtExpirationMs}")
//     private long jwtExpirationMs;

//     /**
//      * Build signing key from application secret
//      */
//     private SecretKey getKey() {
//         return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//     }

//     /**
//      * Generate JWT token for authenticated user
//      */
//     public String generateJwtToken(Authentication authentication) {
//         List<String> roles = authentication.getAuthorities().stream()
//                 .map(GrantedAuthority::getAuthority)
//                 .collect(Collectors.toList());

//         return Jwts.builder()
//                 .setSubject(authentication.getName())
//                 .claim("roles", roles)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                 .signWith(getKey())
//                 .compact();
//     }

//     /**
//      * Extract username (subject) from JWT
//      */
//     public String getUserFromJwtToken(String token) {
//         return Jwts.parser()
//                 .setSigningKey(getKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody()
//                 .getSubject();
//     }

//     /**
//      * Extract roles from JWT
//      */
//     @SuppressWarnings("unchecked")
//     public List<String> getRolesFromJwtToken(String token) {
//         Claims claims = Jwts.parser()
//                 .setSigningKey(getKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();

//         return claims.get("roles", List.class);
//     }

//     /**
//      * Validate JWT token signature & expiration
//      */
//     public boolean validateJwtToken(String token) {
//         try {
//             Jwts.parser()
//                     .setSigningKey(getKey())
//                     .build()
//                     .parseClaimsJws(token);
//             return true;
//         } catch (SignatureException e) {
//             logger.error("Invalid JWT signature", e);
//         } catch (WeakKeyException e) {
//             logger.error("JWT key too weak (HS512 requires long secret)", e);
//         } catch (JwtException e) {
//             logger.error("Invalid JWT token", e);
//         }
//         return false;
//     }

//     /**
//      * @deprecated use getUserFromJwtToken instead
//      */
//     @Deprecated
//     public String getUserNameFromJwtToken(String token) {
//         return getUserFromJwtToken(token);
//     }


//     /**
//      * Generate short-lived ACCESS token (used in Authorization header)
//      */
//     public String generateAccessToken(Authentication authentication) {
//         return generateToken(authentication, TOKEN_TYPE_ACCESS, jwtExpirationMs);
//     }

//     /**
//      * Generate long-lived REFRESH token (stored in HttpOnly cookie)
//      */
//     public String generateRefreshToken(Authentication authentication) {
//         return generateToken(authentication, TOKEN_TYPE_REFRESH, REFRESH_TOKEN_EXP_MS);
//     }

//     /**
//      * Generate access token directly from email (used after refresh)
//      * Note: roles are not included here unless you fetch them from DB.
//      */
//     public String generateAccessTokenFromEmail(String email) {
//         return Jwts.builder()
//                 .setSubject(email)
//                 .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                 .signWith(getKey())
//                 .compact();
//     }

//     /**
//      * Validate refresh token (signature + expiration + type=refresh)
//      */
//     public boolean validateRefreshToken(String token) {
//         if (!validateJwtToken(token)) {
//             return false;
//         }
//         String type = getTokenType(token);
//         return TOKEN_TYPE_REFRESH.equals(type);
//     }

//     /**
//      * Internal token generator for access/refresh
//      */
//     private String generateToken(Authentication authentication, String tokenType, long expirationMs) {
//         List<String> roles = authentication.getAuthorities().stream()
//                 .map(GrantedAuthority::getAuthority)
//                 .collect(Collectors.toList());

//         return Jwts.builder()
//                 .setSubject(authentication.getName())
//                 .claim("roles", roles)
//                 .claim(CLAIM_TOKEN_TYPE, tokenType)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//                 .signWith(getKey())
//                 .compact();
//     }

//     /**
//      * Read claim "type" from token
//      */
//     private String getTokenType(String token) {
//         Claims claims = Jwts.parser()
//                 .setSigningKey(getKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();

//         Object type = claims.get(CLAIM_TOKEN_TYPE);
//         return type != null ? type.toString() : null;
//     }

// }

// package com.fdjloto.api.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import io.jsonwebtoken.security.WeakKeyException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.stereotype.Component;

// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;
// import java.util.List;
// import java.util.stream.Collectors;

// /**
//  * JWT Utility Class for handling JSON Web Tokens.
//  * Handles generation, validation and extraction of claims.
//  */
// @Component
// public class JwtUtils {

//     private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

//     private static final String CLAIM_ROLES = "roles";
//     private static final String CLAIM_TOKEN_TYPE = "type";
//     private static final String TOKEN_TYPE_ACCESS = "access";
//     private static final String TOKEN_TYPE_REFRESH = "refresh";

//     // Refresh token TTL (30 jours)
//     private static final long REFRESH_TOKEN_EXP_MS = 30L * 24 * 60 * 60 * 1000;

//     @Value("${app.jwtSecret}")
//     private String jwtSecret;

//     // Access token TTL (ex: 10 min)
//     @Value("${app.jwtExpirationMs}")
//     private long jwtExpirationMs;

//     /**
//      * Build signing key from application secret
//      */
//     private SecretKey getKey() {
//         return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//     }

//     /* ---------------------------------------------------------------------- */
//     /* Token generation                                                       */
//     /* ---------------------------------------------------------------------- */

//     /**
//      * Legacy: Generate JWT token for authenticated user (no "type" claim).
//      * Prefer generateAccessToken / generateRefreshToken instead.
//      */
//     public String generateJwtToken(Authentication authentication) {
//         return Jwts.builder()
//                 .setSubject(authentication.getName())
//                 .claim(CLAIM_ROLES, extractRoles(authentication))
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                 .signWith(getKey())
//                 .compact();
//     }

//     /**
//      * Generate short-lived ACCESS token (used in Authorization header)
//      */
//     public String generateAccessToken(Authentication authentication) {
//         return generateToken(authentication.getName(), extractRoles(authentication), TOKEN_TYPE_ACCESS, jwtExpirationMs);
//     }

//     /**
//      * Generate long-lived REFRESH token (stored in HttpOnly cookie)
//      */
//     public String generateRefreshToken(Authentication authentication) {
//         // Tu peux choisir de NE PAS mettre les roles dans le refresh token.
//         // Ici je les mets pour cohérence, mais ce n'est pas obligatoire.
//         return generateToken(authentication.getName(), extractRoles(authentication), TOKEN_TYPE_REFRESH, REFRESH_TOKEN_EXP_MS);
//     }

//     /**
//      * Generate access token directly from email (used after refresh)
//      * ⚠️ Idéalement, récupère les rôles depuis la DB et passe-les ici.
//      * Sinon tu peux laisser roles=null, mais attention si ton app se base sur roles dans le JWT.
//      */
//     public String generateAccessTokenFromEmail(String email) {
//         return generateToken(email, null, TOKEN_TYPE_ACCESS, jwtExpirationMs);
//     }

//     /**
//      * Variante recommandée: générer l'access token après refresh avec roles
//      */
//     public String generateAccessTokenFromEmail(String email, List<String> roles) {
//         return generateToken(email, roles, TOKEN_TYPE_ACCESS, jwtExpirationMs);
//     }

//     private String generateToken(String subject, List<String> roles, String tokenType, long expirationMs) {
//         var builder = Jwts.builder()
//                 .setSubject(subject)
//                 .claim(CLAIM_TOKEN_TYPE, tokenType)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//                 .signWith(getKey());

//         if (roles != null) {
//             builder.claim(CLAIM_ROLES, roles);
//         }

//         return builder.compact();
//     }

//     private List<String> extractRoles(Authentication authentication) {
//         return authentication.getAuthorities().stream()
//                 .map(GrantedAuthority::getAuthority)
//                 .collect(Collectors.toList());
//     }

//     /* ---------------------------------------------------------------------- */
//     /* Claims extraction                                                      */
//     /* ---------------------------------------------------------------------- */

//     /**
//      * Extract username (subject) from JWT
//      */
//     public String getUserFromJwtToken(String token) {
//         return parseClaims(token).getSubject();
//     }

//     /**
//      * Extract roles from JWT
//      */
//     @SuppressWarnings("unchecked")
//     public List<String> getRolesFromJwtToken(String token) {
//         Claims claims = parseClaims(token);
//         return claims.get(CLAIM_ROLES, List.class);
//     }

//     /**
//      * Read claim "type" from token
//      */
//     public String getTokenType(String token) {
//         Object type = parseClaims(token).get(CLAIM_TOKEN_TYPE);
//         return type != null ? type.toString() : null;
//     }

//     private Claims parseClaims(String token) {
//         return Jwts.parser()
//                 .setSigningKey(getKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     /* ---------------------------------------------------------------------- */
//     /* Validation                                                             */
//     /* ---------------------------------------------------------------------- */

//     /**
//      * Validate JWT token signature & expiration
//      */
//     public boolean validateJwtToken(String token) {
//         try {
//             parseClaims(token);
//             return true;
//         } catch (WeakKeyException e) {
//             logger.error("JWT key too weak", e);
//         } catch (JwtException e) {
//             // regroupe signature/expired/malformed/etc.
//             logger.error("Invalid JWT token", e);
//         }
//         return false;
//     }

//     /**
//      * Validate refresh token (signature + expiration + type=refresh)
//      */
//     public boolean validateRefreshToken(String token) {
//         if (!validateJwtToken(token)) {
//             return false;
//         }
//         return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
//     }

//     /**
//      * (Optionnel) Validate access token (type=access)
//      */
//     public boolean validateAccessToken(String token) {
//         if (!validateJwtToken(token)) {
//             return false;
//         }
//         // si token legacy sans type, tu peux décider de l'accepter ou non
//         String type = getTokenType(token);
//         return type == null || TOKEN_TYPE_ACCESS.equals(type);
//     }

//     /**
//      * @deprecated use getUserFromJwtToken instead
//      */
//     @Deprecated
//     public String getUserNameFromJwtToken(String token) {
//         return getUserFromJwtToken(token);
//     }
// }

// package com.fdjloto.api.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import io.jsonwebtoken.security.SignatureException;
// import io.jsonwebtoken.security.WeakKeyException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;

// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;
// import java.util.List;

// @Component
// public class JwtUtils {

//     private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

//     private static final String CLAIM_ROLES = "roles";
//     private static final String CLAIM_TOKEN_TYPE = "type";
//     private static final String TOKEN_TYPE_ACCESS = "access";
//     private static final String TOKEN_TYPE_REFRESH = "refresh";

//     // Refresh token TTL (30 jours)
//     private static final long REFRESH_TOKEN_EXP_MS = 30L * 24 * 60 * 60 * 1000;

//     @Value("${app.jwtSecret}")
//     private String jwtSecret;

//     // Access token TTL (ex: 10 minutes)
//     @Value("${app.jwtExpirationMs}")
//     private long jwtExpirationMs;

//     private SecretKey getKey() {
//         return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//     }

//     // ✅ Access token (court)
//     public String generateAccessToken(org.springframework.security.core.Authentication authentication) {
//         List<String> roles = authentication.getAuthorities().stream()
//                 .map(org.springframework.security.core.GrantedAuthority::getAuthority)
//                 .toList();

//         return Jwts.builder()
//                 .setSubject(authentication.getName())
//                 .claim(CLAIM_ROLES, roles)
//                 .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                 .signWith(getKey())
//                 .compact();
//     }

//     // ✅ Refresh token (long)
//     public String generateRefreshToken(org.springframework.security.core.Authentication authentication) {
//         // on peut stocker les roles aussi, c’est OK
//         List<String> roles = authentication.getAuthorities().stream()
//                 .map(org.springframework.security.core.GrantedAuthority::getAuthority)
//                 .toList();

//         return Jwts.builder()
//                 .setSubject(authentication.getName())
//                 .claim(CLAIM_ROLES, roles)
//                 .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP_MS))
//                 .signWith(getKey())
//                 .compact();
//     }

//     // ✅ Après refresh: générer un access token avec roles (recommandé)
//     public String generateAccessTokenFromEmail(String email, List<String> roles) {
//         return Jwts.builder()
//                 .setSubject(email)
//                 .claim(CLAIM_ROLES, roles)
//                 .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                 .signWith(getKey())
//                 .compact();
//     }

//     public String getUserFromJwtToken(String token) {
//         return parseClaims(token).getSubject();
//     }

//     @SuppressWarnings("unchecked")
//     public List<String> getRolesFromJwtToken(String token) {
//         return parseClaims(token).get(CLAIM_ROLES, List.class);
//     }

//     // ✅ Validation générique (signature + expiration)
//     public boolean validateJwtToken(String token) {
//         try {
//             Jwts.parser()
//                     .setSigningKey(getKey())
//                     .build()
//                     .parseClaimsJws(token);
//             return true;
//         } catch (SignatureException e) {
//             logger.error("Invalid JWT signature", e);
//         } catch (WeakKeyException e) {
//             logger.error("JWT key too weak", e);
//         } catch (JwtException e) {
//             logger.error("Invalid JWT token", e);
//         }
//         return false;
//     }

//     // ✅ Validation access token (JWT valide + type=access)
//     public boolean validateAccessToken(String token) {
//         if (!validateJwtToken(token)) return false;
//         return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
//     }

//     // ✅ Validation refresh token (JWT valide + type=refresh)
//     public boolean validateRefreshToken(String token) {
//         if (!validateJwtToken(token)) return false;
//         return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
//     }

//     private Claims parseClaims(String token) {
//         return Jwts.parser()
//                 .setSigningKey(getKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     private String getTokenType(String token) {
//         Object type = parseClaims(token).get(CLAIM_TOKEN_TYPE);
//         return type != null ? type.toString() : null;
//     }
// }

// package com.fdjloto.api.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import io.jsonwebtoken.security.SignatureException;
// import io.jsonwebtoken.security.WeakKeyException;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.stereotype.Component;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;
// import java.util.List;
// import java.util.stream.Collectors;

// /**
//  * JWT Utility Class for handling JSON Web Tokens.
//  * Handles generation, validation and extraction of claims.
//  */
// @Component
// public class JwtUtils {

//     private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

//     @Value("${app.jwtSecret}")
//     private String jwtSecret;

//     @Value("${app.jwtExpirationMs}")
//     private long jwtExpirationMs;

//     /**
//      * Build signing key from application secret
//      */
//     private SecretKey getKey() {
//         return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//     }

//     /**
//      * Generate JWT token for authenticated user
//      */
//     public String generateJwtToken(Authentication authentication) {
//         List<String> roles = authentication.getAuthorities().stream()
//                 .map(GrantedAuthority::getAuthority)
//                 .collect(Collectors.toList());

//         return Jwts.builder()
//                 .setSubject(authentication.getName())
//                 .claim("roles", roles)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                 .signWith(getKey())
//                 .compact();
//     }

//     /**
//      * Extract username (subject) from JWT
//      */
//     public String getUserFromJwtToken(String token) {
//         return Jwts.parser()
//                 .setSigningKey(getKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody()
//                 .getSubject();
//     }

//     /**
//      * Extract roles from JWT
//      */
//     @SuppressWarnings("unchecked")
//     public List<String> getRolesFromJwtToken(String token) {
//         Claims claims = Jwts.parser()
//                 .setSigningKey(getKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();

//         return claims.get("roles", List.class);
//     }

//     /**
//      * Validate JWT token signature & expiration
//      */
//     public boolean validateJwtToken(String token) {
//         try {
//             Jwts.parser()
//                     .setSigningKey(getKey())
//                     .build()
//                     .parseClaimsJws(token);
//             return true;
//         } catch (SignatureException e) {
//             logger.error("Invalid JWT signature", e);
//         } catch (WeakKeyException e) {
//             logger.error("JWT key too weak (HS512 requires long secret)", e);
//         } catch (JwtException e) {
//             logger.error("Invalid JWT token", e);
//         }
//         return false;
//     }

//     /**
//      * @deprecated use getUserFromJwtToken instead
//      */
//     @Deprecated
//     public String getUserNameFromJwtToken(String token) {
//         return getUserFromJwtToken(token);
//     }
// }


package com.fdjloto.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private long jwtExpirationMs; // access TTL (10 min chez toi)

    @Value("${app.refreshExpirationMs}")
    private long refreshExpirationMs; // refresh TTL (ex: 30 jours)

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ compat: ton code appelle generateJwtToken()
    public String generateJwtToken(Authentication authentication) {
        return generateAccessToken(authentication);
    }

    public String generateAccessToken(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(authentication.getName()) // email
                .claim("roles", roles)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getKey())
                .compact();
    }

    public String generateAccessTokenFromEmailAndRoles(String email, List<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getKey())
                .compact();
    }

    public String getUserFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody();
        return claims.get("roles", List.class);
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature", e);
        } catch (WeakKeyException e) {
            logger.error("JWT key too weak", e);
        } catch (JwtException e) {
            logger.error("Invalid JWT token", e);
        }
        return false;
    }

    public boolean validateAccessToken(String token) {
        if (!validateJwtToken(token)) return false;
        String type = getTokenType(token);
        // compat: si anciens tokens sans claim type, on les considère access
        return type == null || TOKEN_TYPE_ACCESS.equals(type);
    }

    public boolean validateRefreshToken(String token) {
        if (!validateJwtToken(token)) return false;
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    private String getTokenType(String token) {
        Claims claims = Jwts.parser().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody();
        Object t = claims.get(CLAIM_TOKEN_TYPE);
        return t == null ? null : t.toString();
    }
}
