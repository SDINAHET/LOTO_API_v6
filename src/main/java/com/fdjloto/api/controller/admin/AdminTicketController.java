package com.fdjloto.api.controller.admin;

import com.fdjloto.api.dto.TicketDTO;
import com.fdjloto.api.model.Ticket;
import com.fdjloto.api.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tickets")
public class AdminTicketController {

    private final TicketService ticketService;

    public AdminTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // LIST ALL (DTO)
    @GetMapping
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    // GET ONE (entity)
    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicketById(@PathVariable String ticketId) {
        try {
            Ticket t = ticketService.getTicketById(ticketId);
            return ResponseEntity.ok(t);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Ticket not found");
        }
    }

    /**
     * CREATE
     * Ici on utilise TicketService.createTicket(userId, ticketDTO)
     * => il faut donc que le body contienne userId (comme dans TicketDTO)
     */
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketDTO ticketDTO) {
        if (ticketDTO.getUserId() == null || ticketDTO.getUserId().isBlank()) {
            return ResponseEntity.badRequest().body("userId is required for admin ticket creation");
        }
        try {
            Ticket saved = ticketService.createTicket(ticketDTO.getUserId(), ticketDTO);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cannot create ticket: " + e.getMessage());
        }
    }

    // UPDATE (DTO)
    @PutMapping("/{ticketId}")
    public ResponseEntity<?> updateTicket(@PathVariable String ticketId, @RequestBody TicketDTO ticketDTO) {
        try {
            Ticket updated = ticketService.updateTicket(ticketId, ticketDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cannot update ticket: " + e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable String ticketId) {
        try {
            // côté admin : on n’a pas besoin de vérifier “owner”, donc on supprime direct
            // MAIS ton TicketService.deleteTicket() demande userId (et check admin via DB).
            // => on évite et on fait plus simple : supprimer via getTicketById + deleteTicket(owner)
            Ticket t = ticketService.getTicketById(ticketId);
            String ownerId = t.getUser().getId();
            ticketService.deleteTicket(ticketId, ownerId);
            return ResponseEntity.ok("Ticket deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cannot delete ticket: " + e.getMessage());
        }
    }
}
