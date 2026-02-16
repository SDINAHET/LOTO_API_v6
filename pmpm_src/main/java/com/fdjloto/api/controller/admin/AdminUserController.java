package com.fdjloto.api.controller.admin;

import com.fdjloto.api.model.User;
import com.fdjloto.api.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create user (admin console)
     * - If id null => generated UUID string
     * - If password provided => BCrypt encode
     * - admin flag allowed (true/false)
     */
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // avoid saving blank password
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // default admin false if not set explicitly
        // (your model defaults to false anyway)

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update user (admin console)
     * - password is optional (only re-encode if provided)
     * - admin flag can be toggled
     * - id never changes
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String id,
                                       @Valid @RequestBody User incoming) {

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        User existing = opt.get();

        if (incoming.getFirstName() != null) existing.setFirstName(incoming.getFirstName());
        if (incoming.getLastName() != null) existing.setLastName(incoming.getLastName());
        if (incoming.getEmail() != null) existing.setEmail(incoming.getEmail());

        // allow admin toggle
        existing.setAdmin(incoming.isAdmin());

        // password optional
        if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(incoming.getPassword()));
        }

        User saved = userRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    /**
     * Hard delete (no RGPD soft delete since you don't want migrations)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
