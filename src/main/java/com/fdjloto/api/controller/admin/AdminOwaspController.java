package com.fdjloto.api.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin")
public class AdminOwaspController {

    @Value("${owasp.front-url:http://127.0.0.1:5500}")
    private String frontUrl;

    @Value("${owasp.api-url:http://127.0.0.1:8082}")
    private String apiUrl;

    @Value("${owasp.reports-dir:reports/owasp}")
    private String reportsDir;

    @Value("${owasp.script:./owasp_score_detail.sh}")
    private String scriptPath;

    @Value("${owasp.timeout-seconds:25}")
    private long timeoutSeconds;

    @Value("${owasp.cooldown-seconds:600}")
    private long cooldownSeconds;

    private final ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private volatile long lastRunEpochSec = 0;

    private Path latestPath() {
        return Paths.get(reportsDir).toAbsolutePath().normalize().resolve("latest.json");
    }

    // ✅ RNCP6-friendly: lecture seule = "voir le dernier rapport"
    @GetMapping("/owasp-score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLast(@RequestParam(name="detail", defaultValue="false") boolean detail) {
        try {
            Path latest = latestPath();
            if (!Files.exists(latest)) {
                // 404 clair : aucun audit encore généré
                return ResponseEntity.status(404).body(Map.of(
                        "error", "No OWASP report yet. Run POST /api/admin/owasp-score/run first."
                ));
            }

            Map<String,Object> payload = om.readValue(
                    Files.readString(latest, StandardCharsets.UTF_8),
                    new TypeReference<Map<String,Object>>() {}
            );

            if (!detail) payload.remove("raw");
            return ResponseEntity.ok(payload);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Exécution contrôlée (ADMIN + cooldown + timeout)
    @PostMapping("/owasp-score/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> run(@RequestParam(name="detail", defaultValue="false") boolean detail) {
        try {
            long now = Instant.now().getEpochSecond();

            // anti-spam / anti-DoS (cooldown)
            if (now - lastRunEpochSec < cooldownSeconds && Files.exists(latestPath())) {
                // renvoie dernier rapport
                return getLast(detail);
            }

            String output = runScript(detail); // MODE fixé côté serveur
            Map<String, Object> parsed = parseOutput(output, detail);

            @SuppressWarnings("unchecked")
            Map<String, Integer> scores = (Map<String, Integer>) parsed.getOrDefault("scores", Map.of());
            List<String> tips = buildFrontTips(scores);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("timestamp", OffsetDateTime.now().toString());
            payload.put("frontUrl", frontUrl);
            payload.put("apiUrl", apiUrl);
            payload.putAll(parsed);
            payload.put("frontTips", tips);

            // persist latest.json
            Path latest = latestPath();
            Files.createDirectories(latest.getParent());
            Files.writeString(latest, om.writeValueAsString(payload), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            lastRunEpochSec = now;

            if (!detail) payload.remove("raw");
            return ResponseEntity.accepted().body(payload);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String runScript(boolean detail) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("bash");
        cmd.add(Paths.get(scriptPath).toAbsolutePath().normalize().toString());
        if (detail) cmd.add("--detail");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        env.put("FRONT_URL", frontUrl);
        env.put("API_URL", apiUrl);

        // ✅ MODE FIXE côté serveur (RNCP-friendly)
        env.put("MODE", "safe");
        env.put("DETAIL", detail ? "1" : "0");

        Process p = pb.start();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }

        boolean finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new RuntimeException("OWASP audit timeout after " + timeoutSeconds + "s");
        }

        int code = p.exitValue();
        sb.append("\n__EXIT_CODE__=").append(code).append("\n");

        if (code != 0) {
            throw new RuntimeException("OWASP audit failed exit=" + code + "\n" + sb);
        }

        return sb.toString();
    }

    private Map<String, Object> parseOutput(String out, boolean detail) {
        Map<String, Object> root = new LinkedHashMap<>();
        if (detail) root.put("raw", out);

        Pattern totalP = Pattern.compile("TOTAL\\.+:\\s*(\\d+)\\s*/\\s*100", Pattern.CASE_INSENSITIVE);
        Matcher mt = totalP.matcher(out);
        Integer total = mt.find() ? Integer.parseInt(mt.group(1)) : null;
        root.put("total", total);

        Pattern scoreP = Pattern.compile("^(A\\d{2})\\s+.*?:\\s*(\\d+)\\s*/\\s*10", Pattern.MULTILINE);
        Matcher ms = scoreP.matcher(out);

        Map<String, Integer> scores = new LinkedHashMap<>();
        while (ms.find()) scores.put(ms.group(1), Integer.parseInt(ms.group(2)));
        root.put("scores", scores);

        String grade = (total == null) ? "N/A"
                : total >= 85 ? "A"
                : total >= 70 ? "B"
                : total >= 55 ? "C"
                : total >= 40 ? "D" : "E";
        root.put("grade", grade);

        Pattern exitP = Pattern.compile("__EXIT_CODE__=(\\d+)");
        Matcher me = exitP.matcher(out);
        if (me.find()) root.put("exitCode", Integer.parseInt(me.group(1)));

        return root;
    }

    // ✅ tu peux garder ta méthode telle quelle
    private List<String> buildFrontTips(Map<String, Integer> scores) {
        int a02 = scores.getOrDefault("A02", 0);
        int a07 = scores.getOrDefault("A07", 0);
        int a10 = scores.getOrDefault("A10", 0);

        List<String> tips = new ArrayList<>();

        if (a02 < 8) {
            tips.add("Renforcer la CSP côté FRONT: éviter 'unsafe-inline', utiliser des nonces/hashes, limiter connect-src aux domaines nécessaires.");
            tips.add("Ajouter/renforcer Strict-Transport-Security (HSTS) en production (HTTPS only).");
            tips.add("Vérifier X-Frame-Options / frame-ancestors (anti clickjacking) + X-Content-Type-Options + Referrer-Policy.");
            tips.add("Ajouter Permissions-Policy et idéalement COOP/COEP/CORP (au moins 2 sur 3) si compatible.");
        } else {
            tips.add("CSP/Headers front : très bon niveau. Prochaine étape : retirer 'unsafe-inline' si encore présent.");
        }

        if (a07 < 7) tips.add("Vérifier la protection des endpoints sensibles et éviter d’exposer des endpoints techniques (ex: actuator) en public.");

        if (a10 < 7) {
            tips.add("Uniformiser les réponses d’erreur côté API (pas de stacktrace/exception), et activer un handler global (ControllerAdvice).");
        } else {
            tips.add("Gestion d’erreurs OK : pas d’indices de stacktrace. Pense à standardiser un format JSON d’erreur.");
        }

        tips.add("Front: utiliser Subresource Integrity (SRI) pour les scripts CDN si utilisés, et versionner les assets.");
        tips.add("Front: éviter le stockage JWT en localStorage; préférer cookie HttpOnly (ce que tu fais déjà).");

        return tips;
    }
}
