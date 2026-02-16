package com.fdjloto.api.controller.admin;

import com.fdjloto.api.model.TicketGain;
import com.fdjloto.api.repository.TicketGainRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket-gains")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTicketGainController {

    private final TicketGainRepository ticketGainRepository;

    public AdminTicketGainController(TicketGainRepository ticketGainRepository) {
        this.ticketGainRepository = ticketGainRepository;
    }

    @GetMapping
    public ResponseEntity<List<TicketGain>> getAll() {
        List<TicketGain> gains = ticketGainRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(gains);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketGain> getById(@PathVariable String id) {
        return ticketGainRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
