package com.fdjloto.api.controller.admin;

import com.fdjloto.api.model.Ticket;
import com.fdjloto.api.model.TicketGain;
import com.fdjloto.api.model.User;
import com.fdjloto.api.repository.TicketGainRepository;
import com.fdjloto.api.repository.TicketRepository;
import com.fdjloto.api.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTicketController {

    private final TicketRepository ticketRepository;
    private final TicketGainRepository ticketGainRepository;
    private final UserRepository userRepository;

    public AdminTicketController(TicketRepository ticketRepository,
                                 TicketGainRepository ticketGainRepository,
                                 UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketGainRepository = ticketGainRepository;
        this.userRepository = userRepository;
    }

    /* =========================================================
       TICKETS CRUD (admin)
       Endpoints:
       - GET    /api/admin/tickets
       - GET    /api/admin/tickets/{id}
       - POST   /api/admin/tickets
       - PUT    /api/admin/tickets/{id}
       - DELETE /api/admin/tickets/{id}
       ========================================================= */

    // DTO for admin dashboard (because Ticket.user is @JsonIgnore)
    public static class AdminTicketDTO {
        public String id;
        public String numbers;
        public Integer chanceNumber;
        public LocalDate drawDate;
        public String drawDay;

        public String userId;
        public String userEmail;

        public AdminTicketDTO() {}

        public static AdminTicketDTO from(Ticket t) {
            AdminTicketDTO dto = new AdminTicketDTO();
            dto.id = t.getId();
            dto.numbers = t.getNumbers();
            dto.chanceNumber = t.getChanceNumber();
            dto.drawDate = t.getDrawDate();
            dto.drawDay = t.getDrawDay();

            if (t.getUser() != null) {
                dto.userId = t.getUser().getId();
                dto.userEmail = t.getUser().getEmail();
            }
            return dto;
        }
    }

    // Payload for create/update from dashboard
    public static class AdminTicketRequest {
        public String numbers;
        public Integer chanceNumber;
        public LocalDate drawDate;
        public String drawDay;

        // required for create / optional for update
        public String userId;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<AdminTicketDTO>> listTickets() {
        List<Ticket> tickets = ticketRepository.findAll(Sort.by(Sort.Order.desc("createdAt").nullsLast()));
        return ResponseEntity.ok(tickets.stream().map(AdminTicketDTO::from).toList());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<AdminTicketDTO> getTicket(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(t -> ResponseEntity.ok(AdminTicketDTO.from(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(@Valid @RequestBody AdminTicketRequest req) {
        if (req.userId == null || req.userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("userId is required");
        }
        Optional<User> uOpt = userRepository.findById(req.userId);
        if (uOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        if (req.numbers == null || req.numbers.isBlank() || req.chanceNumber == null || req.drawDate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("numbers, chanceNumber, drawDate are required");
        }

        Ticket t = new Ticket();
        t.setNumbers(req.numbers);
        t.setChanceNumber(req.chanceNumber);
        t.setDrawDate(req.drawDate);
        t.setDrawDay(req.drawDay);
        t.setUser(uOpt.get());

        Ticket saved = ticketRepository.save(t);
        return ResponseEntity.status(HttpStatus.CREATED).body(AdminTicketDTO.from(saved));
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable String id,
                                         @Valid @RequestBody AdminTicketRequest req) {

        Optional<Ticket> opt = ticketRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Ticket existing = opt.get();

        if (req.numbers != null) existing.setNumbers(req.numbers);
        if (req.chanceNumber != null) existing.setChanceNumber(req.chanceNumber);
        if (req.drawDate != null) existing.setDrawDate(req.drawDate);
        if (req.drawDay != null) existing.setDrawDay(req.drawDay);

        // If dashboard wants to reassign owner
        if (req.userId != null && !req.userId.isBlank()) {
            Optional<User> uOpt = userRepository.findById(req.userId);
            if (uOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
            }
            existing.setUser(uOpt.get());
        }

        // Do not touch id (Ticket.setId(UUID) only, so ignore)
        Ticket saved = ticketRepository.save(existing);
        return ResponseEntity.ok(AdminTicketDTO.from(saved));
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        if (!ticketRepository.existsById(id)) return ResponseEntity.notFound().build();
        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* =========================================================
       TICKET GAINS (READ ONLY)
       Endpoints:
       - GET /api/admin/ticket-gains
       - GET /api/admin/ticket-gains/{id}
       ========================================================= */

    @GetMapping("/ticket-gains")
    public ResponseEntity<List<TicketGain>> listTicketGains() {
        // TicketGain has no createdAt => sort by id
        List<TicketGain> gains = ticketGainRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(gains);
    }

    @GetMapping("/ticket-gains/{id}")
    public ResponseEntity<TicketGain> getTicketGain(@PathVariable String id) {
        return ticketGainRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
