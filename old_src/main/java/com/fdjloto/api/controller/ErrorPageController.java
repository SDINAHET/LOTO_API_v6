package com.fdjloto.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

@Controller
@Tag(
    name = "Error Pages",
    description = "Custom HTML error pages (401, 403, 404, 500) used by the Loto Tracker frontend."
)
public class ErrorPageController {

    @Operation(
        summary = "Custom 401 error page",
        description = "Displays the custom HTML page for HTTP 401 (Unauthorized)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns the 401 HTML page.",
        content = @Content(mediaType = "text/html")
    )
    @GetMapping("/401")
    public String unauthorized() {
        return "forward:/errors/401.html";
    }

    @Operation(
        summary = "Custom 403 error page",
        description = "Displays the custom HTML page for HTTP 403 (Forbidden)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns the 403 HTML page.",
        content = @Content(mediaType = "text/html")
    )
    @GetMapping("/403")
    public String forbidden() {
        return "forward:/errors/403.html";
    }

    @Operation(
        summary = "Custom 404 error page",
        description = "Displays the custom HTML page for HTTP 404 (Not Found)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns the 404 HTML page.",
        content = @Content(mediaType = "text/html")
    )
    @GetMapping("/404")
    public String notFound() {
        return "forward:/errors/404.html";
    }

    @Operation(
        summary = "Custom 500 error page",
        description = "Displays the custom HTML page for HTTP 500 (Internal Server Error)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns the 500 HTML page.",
        content = @Content(mediaType = "text/html")
    )
    @GetMapping("/500")
    public String internalServerError() {
        return "forward:/errors/500.html";
    }
}
