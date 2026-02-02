package com.fdjloto.api.controller;

import com.fdjloto.api.service.VisitService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(
  origins = {
    "http://127.0.0.1:5500",
    "http://localhost:5500",
    "https://stephanedinahet.fr"
  }
)
@RestController
@RequestMapping("/api")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/visits")
    public Map<String, Object> addVisitAndGetTotal() {
        long total = visitService.incrementAndGet();
        return Map.of("total", total);
    }

    @GetMapping("/visits/total")
    public Map<String, Object> getTotal() {
        return Map.of("total", visitService.get());
    }
}
