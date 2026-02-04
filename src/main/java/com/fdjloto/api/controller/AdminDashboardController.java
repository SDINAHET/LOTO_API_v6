package com.fdjloto.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminDashboardController {

    private static final Path LOG_PATH = Paths.get("logs/application.log");

    // ✅ Logs backend (lecture seule)
    @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
    public ResponseEntity<String> getLogs(@RequestParam(defaultValue = "400") int lines) throws IOException {
        if (!Files.exists(LOG_PATH)) {
            return ResponseEntity.ok("Fichier de log introuvable : " + LOG_PATH.toAbsolutePath());
        }

        List<String> all = Files.readAllLines(LOG_PATH, StandardCharsets.UTF_8);
        int safeLines = Math.max(1, Math.min(lines, 20000));
        int fromIndex = Math.max(0, all.size() - safeLines);

        return ResponseEntity.ok(String.join("\n", all.subList(fromIndex, all.size())));
    }

    // ✅ Ping debug
    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return "admin ok";
    }
}
