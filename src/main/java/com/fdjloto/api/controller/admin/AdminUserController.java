package com.fdjloto.api.controller.admin;

import com.fdjloto.api.model.User;
import com.fdjloto.api.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<User>> list() {
        // return ResponseEntity.ok(userRepository.findAll());
        return ResponseEntity.ok(userRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User payload) {
        // Empêche un client de forcer un id
        payload.setId(null);

        // Encode password si présent
        if (payload.getPassword() != null && !payload.getPassword().isBlank()) {
            payload.setPassword(passwordEncoder.encode(payload.getPassword()));
        }

        // admin est déjà géré via payload.setAdmin(...) si tu envoies "admin": true/false
        User saved = userRepository.save(payload);
        return ResponseEntity.ok(saved);
    }


    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String id,
                                       @RequestBody Map<String, Object> patch) {

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        User existing = opt.get();

        if (patch.containsKey("firstName")) existing.setFirstName((String) patch.get("firstName"));
        if (patch.containsKey("lastName"))  existing.setLastName((String) patch.get("lastName"));
        if (patch.containsKey("email"))     existing.setEmail((String) patch.get("email"));

        // admin: true/false (facultatif)
        if (patch.containsKey("admin")) {
            Object v = patch.get("admin");
            if (v instanceof Boolean b) existing.setAdmin(b);
        }

        // password (facultatif)
        if (patch.containsKey("password")) {
            String raw = (String) patch.get("password");
            if (raw != null && !raw.isBlank()) {
                existing.setPassword(passwordEncoder.encode(raw));
            }
        }

        User saved = userRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    /**
     * Soft delete façon "User":
     * - anonymise les champs
     * - désactive admin
     * - garde l'user en DB (évite les soucis FK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        User u = opt.get();

        u.setFirstName("DELETED");
        u.setLastName("USER");
        u.setEmail("deleted_" + u.getId() + "@example.com");
        u.setPassword(passwordEncoder.encode("deleted"));
        u.setAdmin(false);

        userRepository.save(u);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String id,
                                        @RequestBody(required = false) Map<String, Object> body) {

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        User u = opt.get();

        // 1) soit l'admin fournit un mdp
        String newPassword = null;
        if (body != null && body.get("newPassword") != null) {
            newPassword = String.valueOf(body.get("newPassword")).trim();
            if (newPassword.isBlank()) newPassword = null;
        }

        // 2) sinon on génère un temporaire
        boolean generated = false;
        if (newPassword == null) {
            newPassword = generateTempPassword(14);
            generated = true;
        }

        u.setPassword(passwordEncoder.encode(newPassword));
        u = userRepository.save(u);

        // ✅ réponse : si généré -> on renvoie le temporaire (à copier 1 fois)
        if (generated) {
            return ResponseEntity.ok(Map.of(
                    "message", "Mot de passe réinitialisé",
                    "temporaryPassword", newPassword
            ));
        }

        // ✅ si fourni par admin : on ne renvoie rien de sensible
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé"));
    }

    private static String generateTempPassword(int len) {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@$!%*?";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        return sb.toString();
    }
}
