package com.fdjloto.api.controller.admin;

import com.fdjloto.api.model.TicketGain;
import com.fdjloto.api.repository.TicketGainRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket-gains")
public class AdminTicketGainController {

    private final TicketGainRepository ticketGainRepository;

    public AdminTicketGainController(TicketGainRepository ticketGainRepository) {
        this.ticketGainRepository = ticketGainRepository;
    }

    @GetMapping
    public ResponseEntity<List<TicketGain>> getAllTicketGains() {
        return ResponseEntity.ok(ticketGainRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketGainById(@PathVariable String id) {
        return ticketGainRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("TicketGain not found"));
    }
}

