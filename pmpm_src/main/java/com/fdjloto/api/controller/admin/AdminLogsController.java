// // package com.fdjloto.api.controller.admin;

// // import org.springframework.http.MediaType;
// // import org.springframework.http.ResponseEntity;
// // import org.springframework.security.access.prepost.PreAuthorize;
// // import org.springframework.web.bind.annotation.*;

// // import java.io.IOException;
// // import java.nio.charset.StandardCharsets;
// // import java.nio.file.*;
// // import java.util.List;

// // @RestController
// // @RequestMapping("/api/admin")
// // @PreAuthorize("hasRole('ADMIN')")
// // public class AdminLogsController {

// //     // ✅ Ajuste si besoin (tu as indiqué LOTO_API_v6/logs/application.log)
// //     private static final Path LOG_FILE = Paths.get("logs", "application.log");

// //     @GetMapping(value = "/logs", produces = MediaType.TEXT_PLAIN_VALUE)
// //     public ResponseEntity<String> tailLogs(@RequestParam(defaultValue = "400") int lines) throws IOException {
// //         if (lines < 1) lines = 1;
// //         if (lines > 2000) lines = 2000;

// //         if (!Files.exists(LOG_FILE)) {
// //             return ResponseEntity.ok("[INFO] Fichier introuvable : " + LOG_FILE.toAbsolutePath());
// //         }

// //         List<String> all = Files.readAllLines(LOG_FILE, StandardCharsets.UTF_8);
// //         int from = Math.max(0, all.size() - lines);
// //         String out = String.join("\n", all.subList(from, all.size()));
// //         return ResponseEntity.ok(out.isBlank() ? "(Aucun log pour le moment)" : out);
// //     }
// // }

// package com.fdjloto.api.controller.admin;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;
// import java.io.IOException;


// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.List;

// @RestController
// @RequestMapping("/api/admin")
// public class AdminLogsController {

//     // private static final Path LOG_DIR = Paths.get("LOTO_API_v6", "logs")
//     private static final Path LOG_DIR = Paths.get("logs")
//             .toAbsolutePath().normalize();

//     @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<String> getLogs(
//             @RequestParam(defaultValue = "2000") int lines,
//             @RequestParam(defaultValue = "application.log") String file
//     ) throws IOException {

//         // sécurité : clamp
//         lines = Math.max(50, Math.min(lines, 5000));

//         // sécurité : empêche ../
//         Path logFile = LOG_DIR.resolve(file).normalize();
//         if (!logFile.startsWith(LOG_DIR)) {
//             return ResponseEntity.badRequest().body("Invalid file name");
//         }
//         if (!Files.exists(logFile) || Files.isDirectory(logFile)) {
//             return ResponseEntity.status(404).body("Log file not found");
//         }

//         String content = tailLines(logFile, lines);
//         return ResponseEntity.ok(content);
//     }

//     // version simple (OK si logs pas énormes)
//     private String tailLines(Path file, int lines) throws IOException {
//         List<String> all = Files.readAllLines(file);
//         int from = Math.max(0, all.size() - lines);
//         return String.join("\n", all.subList(from, all.size()));
//     }
// }


package com.fdjloto.api.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminLogsController {

    // Dossier "logs" à la racine du projet (là où se trouve application.log)
    private static final Path LOG_DIR = Paths.get("logs")
            .toAbsolutePath()
            .normalize();

    // @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
    @GetMapping(
        value = "/logs",
        produces = { "text/plain;charset=UTF-8", "application/json" }
    )

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getLogs(
            @RequestParam(defaultValue = "2000") int lines,
            @RequestParam(defaultValue = "application.log") String file
    ) throws IOException {

        // clamp (évite abus)
        lines = Math.max(50, Math.min(lines, 5000));

        // sécurité : empêche ../ et chemins absolus
        Path logFile = LOG_DIR.resolve(file).normalize();
        if (!logFile.startsWith(LOG_DIR)) {
            return ResponseEntity.badRequest().body("Invalid file name");
        }

        if (!Files.exists(logFile) || Files.isDirectory(logFile)) {
            return ResponseEntity.status(404).body("Log file not found: " + logFile.getFileName());
        }

        String content = tailLinesUtf8(logFile, lines);
        if (content.isBlank()) content = "(Aucun log pour le moment)";
        return ResponseEntity.ok(content);
    }

    // Simple (OK tant que le fichier n'est pas énorme). Lecture UTF-8.
    private String tailLinesUtf8(Path file, int lines) throws IOException {
        List<String> all = Files.readAllLines(file, StandardCharsets.UTF_8);
        int from = Math.max(0, all.size() - lines);
        return String.join("\n", all.subList(from, all.size()));
    }
}
