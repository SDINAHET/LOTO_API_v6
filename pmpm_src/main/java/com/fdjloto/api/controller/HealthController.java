package com.fdjloto.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "Endpoints to verify if the API is running.")
public class HealthController {

    @Operation(
        summary = "Health check",
        description = "Returns 200 OK when the API is up. Useful for reverse proxy checks and monitoring."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "API is running successfully."),
        @ApiResponse(responseCode = "503", description = "Service unavailable (optional if you implement such behavior).")
    })
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
