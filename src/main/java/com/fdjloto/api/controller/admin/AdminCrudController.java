// package com.fdjloto.api.controller.admin;

// import com.fdjloto.api.model.User;
// import com.fdjloto.api.model.Ticket;
// import com.fdjloto.api.model.TicketGain;
// import com.fdjloto.api.repository.UserRepository;
// import com.fdjloto.api.repository.TicketRepository;
// import com.fdjloto.api.repository.TicketGainRepository;
// import jakarta.validation.Valid;
// import org.springframework.beans.BeanUtils;
// import org.springframework.data.domain.Sort;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// /**
//  * Mini-HeidiSQL pour l'admin :
//  * CRUD simple sur les tables PostgreSQL users, tickets, ticket_gains.
//  *
//  * On ne touche jamais directement à l'ID dans les updates :
//  * - pour les POST, JPA / @PrePersist gère l'ID
//  * - pour les PUT, on charge l'entité existante puis on copie les champs modifiables
//  */
// @RestController
// @RequestMapping("/api/admin")
// @PreAuthorize("hasRole('ADMIN')")
// public class AdminCrudController {

//     private final UserRepository userRepository;
//     private final TicketRepository ticketRepository;
//     private final TicketGainRepository ticketGainRepository;

//     public AdminCrudController(UserRepository userRepository,
//                                TicketRepository ticketRepository,
//                                TicketGainRepository ticketGainRepository) {
//         this.userRepository = userRepository;
//         this.ticketRepository = ticketRepository;
//         this.ticketGainRepository = ticketGainRepository;
//     }

//     /* ========================= USERS ========================= */

//     @GetMapping("/users")
//     public List<User> getAllUsers() {
//         // ✅ derniers en premier (comme HeidiSQL)
//         return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
//     }

//     @GetMapping("/users/{id}")
//     public ResponseEntity<User> getUserById(@PathVariable String id) {
//         return userRepository.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @PostMapping("/users")
//     public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
//         User saved = userRepository.save(user);
//         return ResponseEntity.ok(saved);
//     }

//     @PutMapping("/users/{id}")
//     public ResponseEntity<User> updateUser(@PathVariable String id,
//                                            @Valid @RequestBody User incoming) {
//         return userRepository.findById(id)
//                 .map(existing -> {
//                     BeanUtils.copyProperties(
//                             incoming,
//                             existing,
//                             "id", "password", "createdAt", "updatedAt"
//                     );
//                     User saved = userRepository.save(existing);
//                     return ResponseEntity.ok(saved);
//                 })
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @DeleteMapping("/users/{id}")
//     public ResponseEntity<Void> deleteUser(@PathVariable String id) {
//         if (!userRepository.existsById(id)) {
//             return ResponseEntity.notFound().build();
//         }
//         userRepository.deleteById(id);
//         return ResponseEntity.noContent().build();
//     }

//     /* ========================= TICKETS ========================= */

//     @GetMapping("/tickets")
//     public List<Ticket> getAllTickets() {
//         // ✅ derniers en premier
//         return ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
//     }

//     @GetMapping("/tickets/{id}")
//     public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {
//         return ticketRepository.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @PostMapping("/tickets")
//     public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket) {
//         Ticket saved = ticketRepository.save(ticket);
//         return ResponseEntity.ok(saved);
//     }

//     @PutMapping("/tickets/{id}")
//     public ResponseEntity<Ticket> updateTicket(@PathVariable String id,
//                                                @Valid @RequestBody Ticket incoming) {
//         return ticketRepository.findById(id)
//                 .map(existing -> {
//                     BeanUtils.copyProperties(
//                             incoming,
//                             existing,
//                             "id", "createdAt", "updatedAt"
//                     );
//                     Ticket saved = ticketRepository.save(existing);
//                     return ResponseEntity.ok(saved);
//                 })
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @DeleteMapping("/tickets/{id}")
//     public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
//         if (!ticketRepository.existsById(id)) {
//             return ResponseEntity.notFound().build();
//         }
//         ticketRepository.deleteById(id);
//         return ResponseEntity.noContent().build();
//     }

//     /* ====================== TICKET GAINS ====================== */

//     @GetMapping("/ticket_gains")
//     public List<TicketGain> getAllTicketGains() {
//         // ✅ derniers en premier
//         return ticketGainRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
//     }

//     @GetMapping("/ticket_gains/{id}")
//     public ResponseEntity<TicketGain> getTicketGainById(@PathVariable String id) {
//         return ticketGainRepository.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @PostMapping("/ticket_gains")
//     public ResponseEntity<TicketGain> createTicketGain(@Valid @RequestBody TicketGain incoming) {
//         TicketGain saved = ticketGainRepository.save(incoming);
//         return ResponseEntity.ok(saved);
//     }

//     @PutMapping("/ticket_gains/{id}")
//     public ResponseEntity<TicketGain> updateTicketGain(@PathVariable String id,
//                                                        @Valid @RequestBody TicketGain incoming) {
//         return ticketGainRepository.findById(id)
//                 .map(existing -> {
//                     BeanUtils.copyProperties(
//                             incoming,
//                             existing,
//                             "id", "createdAt", "updatedAt"
//                     );
//                     TicketGain saved = ticketGainRepository.save(existing);
//                     return ResponseEntity.ok(saved);
//                 })
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @DeleteMapping("/ticket_gains/{id}")
//     public ResponseEntity<Void> deleteTicketGain(@PathVariable String id) {
//         if (!ticketGainRepository.existsById(id)) {
//             return ResponseEntity.notFound().build();
//         }
//         ticketGainRepository.deleteById(id);
//         return ResponseEntity.noContent().build();
//     }
// }

package com.fdjloto.api.controller.admin;

import com.fdjloto.api.model.Ticket;
import com.fdjloto.api.model.TicketGain;
import com.fdjloto.api.model.User;
import com.fdjloto.api.repository.TicketGainRepository;
import com.fdjloto.api.repository.TicketRepository;
import com.fdjloto.api.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mini-HeidiSQL pour l'admin :
 * CRUD simple sur les tables users, tickets, ticket_gains.
 *
 * Endpoints "propres" (recommandés) :
 *  - /api/admin/users
 *  - /api/admin/tickets
 *  - /api/admin/ticket-gains
 *
 * Alias compat (si ancien front) :
 *  - /api/admin/ticket_gains
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCrudController {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketGainRepository ticketGainRepository;

    // public AdminCrudController(UserRepository userRepository,
    //                            TicketRepository ticketRepository,
    //                            TicketGainRepository ticketGainRepository) {
    //     this.userRepository = userRepository;
    //     this.ticketRepository = ticketRepository;
    //     this.ticketGainRepository = ticketGainRepository;
    // }

    private final ObjectMapper objectMapper;
    private static final ZoneId ADMIN_ZONE = ZoneId.of("Europe/Paris");

    public AdminCrudController(UserRepository userRepository,
                            TicketRepository ticketRepository,
                            TicketGainRepository ticketGainRepository,
                            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.ticketGainRepository = ticketGainRepository;
        this.objectMapper = objectMapper;
    }

    private OffsetDateTime toAdminOdt(Instant i) {
        return (i == null) ? null : i.atZone(ADMIN_ZONE).toOffsetDateTime();
    }

    private Map<String, Object> toAdminMap(Object entity) {
        Map<String, Object> map = objectMapper.convertValue(
                entity, new TypeReference<LinkedHashMap<String, Object>>() {}
        );

        // Convertit uniquement les champs qui finissent par At (createdAt, updatedAt, revokedAt, etc.)
        map.replaceAll((k, v) -> {
            if (v == null) return null;
            String key = k.toLowerCase();
            boolean isDateKey = key.endsWith("at") || key.endsWith("_at");
            if (!isDateKey) return v;

            if (v instanceof Instant inst) return toAdminOdt(inst);

            // Si Jackson a déjà converti en String ISO (souvent "....Z")
            if (v instanceof String s) {
                try {
                    Instant inst = Instant.parse(s);
                    return toAdminOdt(inst);
                } catch (Exception ignore) {
                    return v;
                }
            }
            return v;
        });

        return map;
    }


    /* ========================= USERS ========================= */

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Order.desc("createdAt").nullsLast()))
                .stream()
                .map(this::toAdminMap)
                .toList();
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toAdminMap(u)))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id,
                                           @Valid @RequestBody User incoming) {
        return userRepository.findById(id)
                .map(existing -> {
                    // ⚠️ protège id + champs sensibles + timestamps
                    BeanUtils.copyProperties(incoming, existing,
                            "id",
                            "password", "hashedPassword", "roles", // adapte selon tes champs
                            "createdAt", "updatedAt", "created_at", "updated_at"
                    );
                    User saved = userRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* ========================= TICKETS ========================= */

    @GetMapping("/tickets")
    public List<Map<String, Object>> getAllTickets() {
        return ticketRepository.findAll(Sort.by(Sort.Order.desc("createdAt").nullsLast()))
                .stream()
                .map(this::toAdminMap)
                .toList();
    }


    @GetMapping("/tickets/{id}")
    public ResponseEntity<Map<String, Object>> getTicketById(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(t -> ResponseEntity.ok(toAdminMap(t)))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/tickets")
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket) {
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable String id,
                                               @Valid @RequestBody Ticket incoming) {
        return ticketRepository.findById(id)
                .map(existing -> {
                    BeanUtils.copyProperties(incoming, existing,
                            "id",
                            "createdAt", "updatedAt", "created_at", "updated_at"
                    );
                    Ticket saved = ticketRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        if (!ticketRepository.existsById(id)) return ResponseEntity.notFound().build();
        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    /* ====================== TICKET GAINS ====================== */
    // ✅ Endpoint attendu par ton dashboard : /api/admin/ticket-gains

    @GetMapping("/ticket-gains")
    public List<TicketGain> getAllTicketGains() {
        // TicketGain n'a pas "createdAt" => tri simple par id
        return ticketGainRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @GetMapping("/ticket-gains/{id}")
    public ResponseEntity<TicketGain> getTicketGainById(@PathVariable String id) {
        return ticketGainRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ticket-gains")
    public ResponseEntity<TicketGain> createTicketGain(@Valid @RequestBody TicketGain incoming) {
        TicketGain saved = ticketGainRepository.save(incoming);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/ticket-gains/{id}")
    public ResponseEntity<TicketGain> updateTicketGain(@PathVariable String id,
                                                       @Valid @RequestBody TicketGain incoming) {
        return ticketGainRepository.findById(id)
                .map(existing -> {
                    BeanUtils.copyProperties(incoming, existing, "id");
                    TicketGain saved = ticketGainRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/ticket-gains/{id}")
    public ResponseEntity<Void> deleteTicketGain(@PathVariable String id) {
        if (!ticketGainRepository.existsById(id)) return ResponseEntity.notFound().build();
        ticketGainRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return """
    <!DOCTYPE html>
    <html lang="fr">
    <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Console admin</title>
    </head>
    <body style="background:#020617;color:#e5e7eb;font-family:system-ui,sans-serif;margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;">
    <div style="background:#0f172a;border:1px solid rgba(255,255,255,.08);border-radius:16px;padding:24px;max-width:560px;width:100%;box-shadow:0 20px 60px rgba(0,0,0,.45);">
        <h1 style="margin:0 0 10px;font-size:1.3rem;">Console admin</h1>
        <p style="margin:0 0 18px;color:#9ca3af;font-size:.95rem;">
        Connexion OK. Accès à Swagger :
        </p>

        <a href="/swagger-ui/index.html"
        style="display:inline-block;padding:10px 14px;border-radius:999px;background:#22c55e;color:#020617;text-decoration:none;font-weight:700;">
        Ouvrir Swagger
        </a>
    </div>
    </body>
    </html>
    """;
    }

}
