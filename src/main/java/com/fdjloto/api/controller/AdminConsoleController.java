// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.nio.file.*;
// import java.util.List;

// @RestController
// @RequestMapping("/admin")
// public class AdminConsoleController {

//     private static final Path LOG_PATH = Paths.get("logs/loto-api-dev.log");

//     /**
//      * Page HTML de la console admin (viewer des logs).
//      */
//     @GetMapping(value = "/console", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminConsolePage() {
//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//     <meta charset="UTF-8">
//     <title>Console admin - Loto Tracker</title>
//     <meta name="viewport" content="width=device-width, initial-scale=1.0">
// </head>
// <body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;margin:0;">
// <div style="display:flex;flex-direction:column;height:100vh;">
//     <header style="padding:10px 16px;border-bottom:1px solid #1f2937;display:flex;justify-content:space-between;align-items:center;">
//         <div>
//             <h1 style="margin:0;font-size:1.1rem;">Console Loto Tracker</h1>
//             <p style="margin:2px 0 0;font-size:0.8rem;color:#9ca3af;">
//                 Vue en lecture seule des logs du backend (scraping, prédictions, erreurs, etc.).
//             </p>
//         </div>
//         <div style="font-size:0.8rem;color:#9ca3af;">
//             <span id="lastRefresh">Dernier rafraîchissement : -</span>
//         </div>
//     </header>

//     <main style="flex:1;display:flex;flex-direction:column;padding:10px 12px;gap:8px;">
//         <div style="display:flex;gap:8px;align-items:center;">
//             <button id="btnRefresh" type="button"
//                     style="padding:6px 12px;border-radius:999px;border:none;background:#22c55e;color:#020617;font-weight:600;cursor:pointer;font-size:0.85rem;">
//                 🔄 Rafraîchir maintenant
//             </button>

//             <label style="font-size:0.8rem;color:#9ca3af;">
//                 Lignes :
//                 <select id="lineCount" style="background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:3px 8px;font-size:0.8rem;">
//                     <option value="200">200</option>
//                     <option value="400" selected>400</option>
//                     <option value="800">800</option>
//                 </select>
//             </label>

//             <label style="font-size:0.8rem;color:#9ca3af;display:flex;align-items:center;gap:4px;">
//                 <input type="checkbox" id="autoScroll" checked>
//                 Auto-scroll
//             </label>

//             <label style="font-size:0.8rem;color:#9ca3af;display:flex;align-items:center;gap:4px;">
//                 <input type="checkbox" id="autoRefresh" checked>
//                 Auto-refresh (3s)
//             </label>
//         </div>

//         <div id="log"
//              style="flex:1;overflow-y:auto;border-radius:8px;border:1px solid #1f2937;background:#020617;padding:10px;font-family:monospace;font-size:11px;line-height:1.4;white-space:pre-wrap;">
//             Chargement des logs...
//         </div>
//     </main>
// </div>

// <script>
//     const logDiv = document.getElementById("log");
//     const btnRefresh = document.getElementById("btnRefresh");
//     const lineCountSelect = document.getElementById("lineCount");
//     const autoScrollCheckbox = document.getElementById("autoScroll");
//     const autoRefreshCheckbox = document.getElementById("autoRefresh");
//     const lastRefreshSpan = document.getElementById("lastRefresh");

//     async function refreshLogs() {
//         const lines = lineCountSelect.value;
//         try {
//             const res = await fetch(`/admin/logs?lines=${lines}`);
//             const text = await res.text();
//             logDiv.textContent = text || "(Aucun log pour le moment)";

//             const now = new Date();
//             lastRefreshSpan.textContent = "Dernier rafraîchissement : " +
//                 now.toLocaleTimeString("fr-FR");

//             if (autoScrollCheckbox.checked) {
//                 logDiv.scrollTop = logDiv.scrollHeight;
//             }
//         } catch (e) {
//             console.error(e);
//             logDiv.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//         }
//     }

//     btnRefresh.addEventListener("click", refreshLogs);
//     lineCountSelect.addEventListener("change", refreshLogs);

//     // Auto-refresh toutes les 3 secondes si activé
//     setInterval(() => {
//         if (autoRefreshCheckbox.checked) {
//             refreshLogs();
//         }
//     }, 3000);

//     // Premier chargement
//     refreshLogs();
// </script>
// </body>
// </html>
// """;
//     }

//     /**
//      * Endpoint REST qui renvoie les X dernières lignes du fichier de log.
//      */
//     @GetMapping(value = "/logs", produces = MediaType.TEXT_PLAIN_VALUE)
//     public ResponseEntity<String> getLogs(@RequestParam(name = "lines", defaultValue = "400") int lines)
//             throws IOException {

//         if (!Files.exists(LOG_PATH)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + LOG_PATH.toAbsolutePath());
//         }

//         List<String> all = Files.readAllLines(LOG_PATH);
//         int fromIndex = Math.max(0, all.size() - lines);
//         List<String> lastLines = all.subList(fromIndex, all.size());
//         String content = String.join("\n", lastLines);

//         return ResponseEntity.ok(content);
//     }
// }
