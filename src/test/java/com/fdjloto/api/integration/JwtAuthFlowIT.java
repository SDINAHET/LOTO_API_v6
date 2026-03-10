// // package com.fdjloto.api.integration;

// // public class JwtAuthFlowIT {

// // }

// package com.fdjloto.api.integration;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.http.*;
// import org.springframework.test.context.ActiveProfiles;

// import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("test")
// class JwtAuthFlowIT {

//     // ✅ Ton endpoint login
//     private static final String LOGIN_URL = "/api/auth/login3";

//     // ⚠️ A MODIFIER : mets ici un endpoint réellement protégé par JWT
//     // Exemples possibles chez toi : "/api/tickets", "/api/user/me", "/api/protected"
//     private static final String PROTECTED_URL = "/api/auth/me";

//     @Autowired
//     private TestRestTemplate rest;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Test
//     void should_return_401_without_token_and_200_with_token() throws Exception {
//         // 1) Sans token => 401
//         ResponseEntity<String> noAuth = rest.getForEntity(PROTECTED_URL, String.class);
//         assertThat(noAuth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

//         // 2) Login => token
//         Tokens tokens = loginAndGetTokens("test@demo.fr", "Test1234!");
//         assertThat(tokens.jwtToken).isNotBlank();

//         // 3) Avec token => 200
//         HttpHeaders headers = new HttpHeaders();
//         headers.setBearerAuth(tokens.jwtToken);
//         HttpEntity<Void> entity = new HttpEntity<>(headers);

//         ResponseEntity<String> ok = rest.exchange(PROTECTED_URL, HttpMethod.GET, entity, String.class);
//         assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
//     }

//     private Tokens loginAndGetTokens(String email, String password) throws Exception {
//         // Payload login (adapte les noms si ton API attend autre chose)
//         String json = """
//           {"email":"%s","password":"%s"}
//         """.formatted(email, password);

//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);

//         ResponseEntity<String> res = rest.exchange(
//                 LOGIN_URL,
//                 HttpMethod.POST,
//                 new HttpEntity<>(json, headers),
//                 String.class
//         );

//         assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//         assertThat(res.getBody()).isNotBlank();

//         JsonNode root = objectMapper.readTree(res.getBody());

//         // ✅ Tu m’as dit: jwtToken
//         String jwtToken = textOrNull(root, "jwtToken");

//         // refresh token: tu n’es pas sûr du nom => on gère les 2
//         String refreshToken = firstNonBlank(
//                 textOrNull(root, "refresh_token"),
//                 textOrNull(root, "refreshToken")
//         );

//         assertThat(jwtToken).as("jwtToken manquant dans la réponse login").isNotBlank();

//         return new Tokens(jwtToken, refreshToken);
//     }

//     private static String textOrNull(JsonNode node, String field) {
//         JsonNode v = node.get(field);
//         return (v == null || v.isNull()) ? null : v.asText();
//     }

//     private static String firstNonBlank(String a, String b) {
//         if (a != null && !a.isBlank()) return a;
//         if (b != null && !b.isBlank()) return b;
//         return null;
//     }

//     private record Tokens(String jwtToken, String refreshToken) {}
// }
