package com.fdjloto.api.controller.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class AdminOwaspController {

    // Chemins par défaut (tu peux adapter)
    @Value("${owasp.front-url:http://127.0.0.1:5500}")
    private String frontUrl;

    @Value("${owasp.api-url:http://127.0.0.1:8082}")
    private String apiUrl;

    // Script à la racine du projet (répertoire d'exécution = working dir)
    private static final String SCRIPT = "./owasp_score_detail.sh";

    @GetMapping("/api/admin/owasp-score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> owaspScore(
            @RequestParam(name = "detail", defaultValue = "false") boolean detail,
            @RequestParam(name = "mode", defaultValue = "safe") String mode
    ) {
        // Sécurité : on autorise uniquement safe/strict
        if (!mode.equals("safe") && !mode.equals("strict")) {
            mode = "safe";
        }

        String output = runScript(detail, mode);
        Map<String, Object> parsed = parseOutput(output);

        // Conseils "front" basés sur le score A02 principalement + quelques autres
        @SuppressWarnings("unchecked")
        Map<String, Integer> scores = (Map<String, Integer>) parsed.getOrDefault("scores", Map.of());
        List<String> tips = buildFrontTips(scores);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", OffsetDateTime.now().toString());
        payload.put("frontUrl", frontUrl);
        payload.put("apiUrl", apiUrl);
        payload.putAll(parsed);
        payload.put("frontTips", tips);

        return ResponseEntity.ok(payload);
    }

    private String runScript(boolean detail, String mode) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add("bash");
            cmd.add(SCRIPT);
            if (detail) cmd.add("--detail");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            // Env pass-through + override variables
            Map<String, String> env = pb.environment();
            env.put("FRONT_URL", frontUrl);
            env.put("API_URL", apiUrl);
            env.put("MODE", mode);
            env.put("DETAIL", detail ? "1" : "0");

            pb.redirectErrorStream(true);

            Process p = pb.start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
            }

            int code = p.waitFor();
            // On renvoie quand même la sortie pour debug
            sb.append("\n__EXIT_CODE__=").append(code).append("\n");
            return sb.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private Map<String, Object> parseOutput(String out) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("raw", out);

        // Extract TOTAL: "TOTAL..........................: 62/100"
        Pattern totalP = Pattern.compile("TOTAL\\.+:\\s*(\\d+)\\s*/\\s*100", Pattern.CASE_INSENSITIVE);
        Matcher mt = totalP.matcher(out);
        Integer total = mt.find() ? Integer.parseInt(mt.group(1)) : null;
        root.put("total", total);

        // Extract A01..A10 : "A01 ...:  8/10"
        Pattern scoreP = Pattern.compile("^(A\\d{2})\\s+.*?:\\s*(\\d+)\\s*/\\s*10", Pattern.MULTILINE);
        Matcher ms = scoreP.matcher(out);

        Map<String, Integer> scores = new LinkedHashMap<>();
        while (ms.find()) {
            scores.put(ms.group(1), Integer.parseInt(ms.group(2)));
        }
        root.put("scores", scores);

        // Simple “grade”
        String grade = (total == null) ? "N/A"
                : total >= 85 ? "A"
                : total >= 70 ? "B"
                : total >= 55 ? "C"
                : total >= 40 ? "D" : "E";
        root.put("grade", grade);

        // Exit code
        Pattern exitP = Pattern.compile("__EXIT_CODE__=(\\d+)");
        Matcher me = exitP.matcher(out);
        if (me.find()) root.put("exitCode", Integer.parseInt(me.group(1)));

        return root;
    }

    private List<String> buildFrontTips(Map<String, Integer> scores) {
        int a02 = scores.getOrDefault("A02", 0);
        int a07 = scores.getOrDefault("A07", 0);
        int a10 = scores.getOrDefault("A10", 0);

        List<String> tips = new ArrayList<>();

        // A02 (headers / config front)
        if (a02 < 8) {
            tips.add("Renforcer la CSP côté FRONT: éviter 'unsafe-inline', utiliser des nonces/hashes, limiter connect-src aux domaines nécessaires.");
            tips.add("Ajouter/renforcer Strict-Transport-Security (HSTS) en production (HTTPS only).");
            tips.add("Vérifier X-Frame-Options / frame-ancestors (anti clickjacking) + X-Content-Type-Options + Referrer-Policy.");
            tips.add("Ajouter Permissions-Policy et idéalement COOP/COEP/CORP (au moins 2 sur 3) si compatible.");
        } else {
            tips.add("CSP/Headers front : très bon niveau. Prochaine étape : retirer 'unsafe-inline' si encore présent.");
        }

        // A07 (auth)
        if (a07 < 7) {
            tips.add("Vérifier la protection des endpoints sensibles et éviter d’exposer des endpoints techniques (ex: actuator) en public.");
        }

        // A10 (exceptions)
        if (a10 < 7) {
            tips.add("Uniformiser les réponses d’erreur côté API (pas de stacktrace/exception), et activer un handler global (ControllerAdvice).");
        } else {
            tips.add("Gestion d’erreurs OK : pas d’indices de stacktrace. Pense à standardiser un format JSON d’erreur.");
        }

        // Bonus UI/Front
        tips.add("Front: utiliser Subresource Integrity (SRI) pour les scripts CDN si utilisés, et versionner les assets.");
        tips.add("Front: éviter le stockage JWT en localStorage; préférer cookie HttpOnly (ce que tu fais déjà).");

        return tips;
    }
}
