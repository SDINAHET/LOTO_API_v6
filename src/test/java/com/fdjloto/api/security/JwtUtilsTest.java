
// package com.fdjloto.api.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import org.junit.jupiter.api.Test;
// import org.springframework.test.util.ReflectionTestUtils;

// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;

// import static org.junit.jupiter.api.Assertions.*;

// class JwtUtilsTest {

//     @Test void generateAndValidateToken_roundTrip() {
//         JwtUtils jwt = new JwtUtils();

//         // Set a deterministic secret key for tests
//         String secret = "0123456789abcdef0123456789abcdef"; // 32 chars
//         ReflectionTestUtils.setField(jwt, "jwtSecret", secret);
//         ReflectionTestUtils.setField(jwt, "jwtExpirationMs", 60_000);

//         String token = jwt.generateJwtToken("user@example.com", "u1", "user");
//         assertNotNull(token);
//         assertTrue(jwt.validateJwtToken(token));

//         assertEquals("user@example.com", jwt.getUserNameFromJwtToken(token));
//         assertEquals("u1", jwt.getUserIdFromJwtToken(token));
//         assertEquals("user", jwt.getUserRoleFromJwtToken(token));
//     }

//     @Test void validateJwtToken_invalidToken_returnsFalse() {
//         JwtUtils jwt = new JwtUtils();
//         ReflectionTestUtils.setField(jwt, "jwtSecret", "0123456789abcdef0123456789abcdef");
//         assertFalse(jwt.validateJwtToken("not-a-jwt"));
//     }

//     @Test void validateJwtToken_expired_returnsFalse() {
//         JwtUtils jwt = new JwtUtils();
//         String secret = "0123456789abcdef0123456789abcdef";
//         ReflectionTestUtils.setField(jwt, "jwtSecret", secret);

//         SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//         String token = Jwts.builder()
//             .subject("user@example.com")
//             .claim("userId", "u1")
//             .claim("role", "user")
//             .issuedAt(new Date(System.currentTimeMillis() - 10_000))
//             .expiration(new Date(System.currentTimeMillis() - 1))
//             .signWith(key)
//             .compact();

//         assertFalse(jwt.validateJwtToken(token));
//     }
// }
