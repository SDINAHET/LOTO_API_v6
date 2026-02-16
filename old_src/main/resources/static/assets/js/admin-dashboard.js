/* admin-dashboard.js
 * Requiert layout-dashboard.js (window.API_BASE + window.apiFetch)
 * - Ping: /admin/ping
 * - Logs: /api/admin/logs?lines=...
 * - CRUD: /api/admin/**
 */

(() => {
  "use strict";

  function setSessionExpiredUI() {
    const userEmail = document.getElementById("userEmail");
    const userChip = document.getElementById("userChip");
    const logoutBtn =
      document.getElementById("btnLogout") || document.getElementById("logoutBtn");

    if (userEmail) {
      userEmail.textContent = "Session expirée";
      userEmail.classList.add("session-expired");
    }
    if (userChip) {
      userChip.style.display = "inline-flex";
      userChip.classList.add("session-expired");
    }

    if (logoutBtn) {
      logoutBtn.disabled = true;
      logoutBtn.style.opacity = "0.5";
      logoutBtn.style.cursor = "not-allowed";
      logoutBtn.title = "Vous n'êtes plus connecté";
    }

    console.warn("[ADMIN] Session expirée (401/403).");
    // window.location.href = "/admin-login.html";
  }

  // ----------------------------
  // BOOTSTRAP (API_BASE + apiFetch)
  // ----------------------------
  const API_BASE = window.API_BASE;
  const apiFetch = window.apiFetch;

  console.log("ADMIN DASHBOARD JS VERSION: 2026-02-09-EXPORT-TIMER-7S");

  if (!API_BASE || typeof apiFetch !== "function") {
    console.error(
      "API_BASE / apiFetch manquant. Vérifie que layout-dashboard.js est chargé AVANT admin-dashboard.js"
    );
    return;
  }

  // ----------------------------
  // USER CHIP (topbar)
  // ----------------------------
  async function loadAdminUser() {
    const userEmail = document.getElementById("userEmail");
    const userChip = document.getElementById("userChip");

    try {
      const res = await apiFetch("/api/protected/userinfo", { method: "GET" });

      if (res.status === 401 || res.status === 403) {
        setSessionExpiredUI();
        showToast("Session expirée", "error");
        return;
      }

      if (!res.ok) {
        if (userEmail) userEmail.textContent = "Admin";
        if (userChip) userChip.style.display = "inline-flex";
        return;
      }

      const data = await res.json();
      const label = data.username || data.email || "Administrateur";

      if (userEmail) userEmail.textContent = label;
      if (userChip) userChip.style.display = "inline-flex";
    } catch (e) {
      console.warn("Impossible de charger l'utilisateur admin", e);
      if (userEmail) userEmail.textContent = "Admin";
      if (userChip) userChip.style.display = "inline-flex";
    }
  }

  // ----------------------------
  // NAV
  // ----------------------------
  const navItems = document.querySelectorAll(".nav-item");
  const sections = {
    swagger: document.getElementById("section-swagger"),
    logs: document.getElementById("section-logs"),
    info: document.getElementById("section-info"),
    owasp: document.getElementById("section-owasp"),
    db: document.getElementById("section-db"),
    stats: document.getElementById("section-stats"),
  };

  const pageTitle = document.getElementById("pageTitle");
  const pageSubtitle = document.getElementById("pageSubtitle");

  const subtitles = {
    swagger: "Documentation Swagger et tests de l’API.",
    logs: "Suivi en temps réel des logs Spring Boot.",
    info: "Debug / vérifications rapides.",
    db: "Exploration et modification des tables principales (CRUD).",
    stats: "Vue par joueur : tickets, gains, etc.",
  };

  function showSection(key) {
    Object.keys(sections).forEach((k) =>
      sections[k]?.classList.toggle("show", k === key)
    );
    navItems.forEach((btn) =>
      btn.classList.toggle("active", btn.dataset.section === key)
    );

    if (pageTitle) pageTitle.textContent = "Tableau de bord administrateur";
    if (pageSubtitle) pageSubtitle.textContent = subtitles[key] || "";

    if (key === "logs") refreshLogs();
    if (key === "owasp") fetchOwaspLast(false);

  }

  navItems.forEach((btn) =>
    btn.addEventListener("click", () => showSection(btn.dataset.section))
  );

  // ----------------------------
  // SWAGGER
  // ----------------------------
  const btnOpenSwagger = document.getElementById("btnOpenSwagger");
  const SWAGGER_URL = `${API_BASE}/swagger-ui/index.html`;

  btnOpenSwagger?.addEventListener("click", (e) => {
    e.preventDefault();
    window.open(SWAGGER_URL, "_blank", "noopener,noreferrer");
  });

  // ----------------------------
  // LOG COLORIZER (safe HTML)
  // ----------------------------
  function escapeHtml(str) {
    return String(str ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  function classifyLine(line) {
    if (line.includes(" ERROR ")) return "error";
    if (line.includes(" WARN ")) return "warn";
    if (line.includes(" INFO ")) return "info";
    if (line.includes(" DEBUG ")) return "debug";
    if (line.includes(" TRACE ")) return "trace";
    return "";
  }

  function colorizeLogText(raw) {
    const lines = String(raw || "").split("\n");

    return lines
      .map((line) => {
        const safe = escapeHtml(line);
        const level = classifyLine(line);

        const timeMatch = safe.match(
          /^(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})\s+/
        );
        let out = safe;

        if (timeMatch) {
          out = out.replace(
            timeMatch[1],
            `<span class="log-time">${timeMatch[1]}</span>`
          );
        }

        let badge = "";
        if (level) {
          badge = `<span class="log-badge ${level}">${level.toUpperCase()}</span>`;
        }

        return level ? `${badge}<span class="log-${level}">${out}</span>` : out;
      })
      .join("\n");
  }

  // ----------------------------
  // LOGS
  // ----------------------------
  const logsContainer = document.getElementById("logsContainer");
  const btnRefreshLogs = document.getElementById("btnRefreshLogs");
  const logLineCountSelect = document.getElementById("logLineCount");
  const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
  const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
  const logsLastRefresh = document.getElementById("logsLastRefresh");

  function getSelectedLines() {
    const allowed = new Set(["5000", "2000", "1000", "800", "400", "200"]);
    let lines = String(logLineCountSelect?.value || "400").trim();
    if (!allowed.has(lines)) lines = "400";
    return lines;
  }

  async function refreshLogs() {
    if (!logsContainer) return;

    const lines = getSelectedLines();
    const path = `/api/admin/logs?lines=${encodeURIComponent(lines)}`;

    try {
      const res = await apiFetch(path, {
        method: "GET",
        headers: { Accept: "text/plain" },
      });

      if (res.status === 401 || res.status === 403) {
        setSessionExpiredUI();
      }

      const text = await res.text().catch(() => "");

      if (!res.ok) {
        logsContainer.innerHTML = colorizeLogText(
          `[${res.status}] ${text || "Erreur logs"}`
        );
        if (logsLastRefresh) {
          logsLastRefresh.textContent =
            "Échec : " + new Date().toLocaleTimeString("fr-FR");
        }
        return;
      }

      logsContainer.innerHTML = colorizeLogText(
        text || "(Aucun log pour le moment)"
      );
      if (logsLastRefresh) {
        logsLastRefresh.textContent =
          "Dernier refresh : " + new Date().toLocaleTimeString("fr-FR");
      }

      if (autoScrollLogsCheckbox?.checked) {
        logsContainer.scrollTop = logsContainer.scrollHeight;
      }
    } catch (e) {
      console.error(e);
      logsContainer.innerHTML = colorizeLogText(
        "[ERREUR] Impossible de charger les logs."
      );
      if (logsLastRefresh) {
        logsLastRefresh.textContent =
          "Échec : " + new Date().toLocaleTimeString("fr-FR");
      }
    }
  }

  btnRefreshLogs?.addEventListener("click", refreshLogs);
  logLineCountSelect?.addEventListener("change", refreshLogs);

  setInterval(() => {
    if (autoRefreshLogsCheckbox?.checked) refreshLogs();
  }, 3000);

  // ----------------------------
  // INFO (Debug endpoints)
  // ----------------------------
  const apiBaseEl = document.getElementById("apiBase");
  if (apiBaseEl) apiBaseEl.textContent = API_BASE;

  const debugStatus = document.getElementById("debugStatus");
  const debugOutput = document.getElementById("debugOutput");
  const debugTableBody = document.getElementById("debugTableBody");

  const DEBUG_ENDPOINTS = [
    { name: "Ping", path: "/api/admin/ping", btnId: "btnPing" },
    { name: "Health", path: "/api/admin/health", btnId: "btnHealth" },
    { name: "Runtime", path: "/api/admin/runtime", btnId: "btnRuntime" },
    { name: "Uptime", path: "/api/admin/uptime", btnId: "btnUptime" },
    { name: "Cookies", path: "/api/admin/cookies", btnId: "btnCookies" },
    { name: "ReqCtx", path: "/api/admin/request-context", btnId: "btnReqCtx" },
  ];

  function pretty(obj) {
    try {
      return JSON.stringify(obj, null, 2);
    } catch {
      return String(obj);
    }
  }

  function setDebugUI(statusText, bodyText) {
    if (debugStatus) debugStatus.textContent = statusText || "-";
    if (debugOutput) debugOutput.textContent = bodyText || "";
  }

  function clearActiveButtons() {
    DEBUG_ENDPOINTS.forEach((e) => {
      document.getElementById(e.btnId)?.classList.remove("is-active");
    });
  }

  function setActiveButton(btnId) {
    clearActiveButtons();
    document.getElementById(btnId)?.classList.add("is-active");
  }

  function setButtonState(btnId, status) {
    const b = document.getElementById(btnId);
    if (!b) return;

    b.classList.remove("is-ok", "is-warn", "is-err");

    if (status >= 200 && status < 300) b.classList.add("is-ok");
    else if (status === 401 || status === 403) b.classList.add("is-warn");
    else b.classList.add("is-err");
  }

  function badgeClass(code) {
    if (code >= 200 && code < 300) return "ok";
    if (code === 401 || code === 403) return "warn";
    return "err";
  }

  function summarizeBody(body) {
    if (!body) return "";
    if (typeof body === "string") return body.slice(0, 140);
    if (body.status) return String(body.status);
    if (body.error) return String(body.error);
    if (body.message) return String(body.message);
    if (body.authenticated === false) return "Not authenticated";
    return "OK";
  }

  function renderTable(rows) {
    if (!debugTableBody) return;
    debugTableBody.innerHTML = rows
      .map((r) => {
        const cls = badgeClass(r.status);
        return `
          <tr>
            <td class="mono">${r.path}</td>
            <td>
              <span class="badge ${cls}">
                <span class="dot"></span>
                <span>${r.status}</span>
              </span>
            </td>
            <td class="mono">${r.ms} ms</td>
            <td>${r.summary || ""}</td>
          </tr>
        `;
      })
      .join("");
  }

  async function fetchDebug(path) {
    const t0 = performance.now();
    try {
      const res = await apiFetch(path, { method: "GET" });
      const ms = Math.round(performance.now() - t0);

      const ct = (res.headers.get("content-type") || "").toLowerCase();
      let body = null;

      if (ct.includes("application/json")) body = await res.json().catch(() => null);
      else body = await res.text().catch(() => "");

      return { path, status: res.status, ms, body, summary: summarizeBody(body) };
    } catch (e) {
      const ms = Math.round(performance.now() - t0);
      return { path, status: 0, ms, body: String(e), summary: "Erreur réseau" };
    }
  }

  async function runOne(endpoint) {
    setActiveButton(endpoint.btnId);
    setDebugUI("…", "Chargement…");
    const r = await fetchDebug(endpoint.path);
    setActiveButton(endpoint.btnId);
    setButtonState(endpoint.btnId, r.status);

    renderTable([r]);
    setDebugUI(
      `${r.status || "ERR"} - ${endpoint.path}`,
      typeof r.body === "string" ? r.body : pretty(r.body)
    );
  }

  async function runAll() {
    clearActiveButtons();
    document.getElementById("btnAll")?.classList.add("is-active");
    setDebugUI("…", "Tests en cours…");

    const results = await Promise.all(DEBUG_ENDPOINTS.map((e) => fetchDebug(e.path)));

    // ✅ colore chaque bouton selon le status
    DEBUG_ENDPOINTS.forEach((ep) => {
      const r = results.find(x => x.path === ep.path);
      if (r) setButtonState(ep.btnId, r.status);
    });

    results.sort((a, b) => {
      const ra =
        badgeClass(a.status) === "err" ? 0 : badgeClass(a.status) === "warn" ? 1 : 2;
      const rb =
        badgeClass(b.status) === "err" ? 0 : badgeClass(b.status) === "warn" ? 1 : 2;
      return ra - rb;
    });

    renderTable(results);

    const okCount = results.filter((r) => r.status >= 200 && r.status < 300).length;
    const warnCount = results.filter((r) => r.status === 401 || r.status === 403).length;
    const errCount = results.filter(
      (r) => r.status === 0 || (r.status >= 400 && r.status !== 401 && r.status !== 403)
    ).length;

    setDebugUI(
      `OK:${okCount}  WARN:${warnCount}  ERR:${errCount}`,
      pretty(
        results.reduce((acc, r) => {
          acc[r.path] = r.body;
          return acc;
        }, {})
      )
    );

    setTimeout(() => document.getElementById("btnAll")?.classList.remove("is-active"), 1000);
  }

  document.getElementById("btnAll")?.addEventListener("click", runAll);
  DEBUG_ENDPOINTS.forEach((ep) => {
    document.getElementById(ep.btnId)?.addEventListener("click", () => runOne(ep));
  });

  // ----------------------------
  // OWASP Score section
  // ----------------------------
  const owaspStatus = document.getElementById("owaspStatus");
  const owaspTableBody = document.getElementById("owaspTableBody");
  const owaspTips = document.getElementById("owaspTips");
  const owaspRaw = document.getElementById("owaspRaw");

  function scoreToBadge(score) {
    if (score >= 8) return "ok";
    if (score >= 5) return "warn";
    return "err";
  }

  function renderOwaspTable(scores) {
    if (!owaspTableBody) return;

    const labels = {
      A01: "Broken Access Control",
      A02: "Security Misconfiguration (Headers)",
      A03: "Supply Chain Failures",
      A04: "Cryptographic Failures",
      A05: "Injection",
      A06: "Insecure Design",
      A07: "Authentication Failures",
      A08: "Integrity Failures",
      A09: "Logging & Alerting",
      A10: "Exceptional Conditions",
    };

    const keys = Object.keys(labels);

    owaspTableBody.innerHTML = keys
      .map((k) => {
        const v = scores && typeof scores[k] === "number" ? scores[k] : 0;
        const cls = scoreToBadge(v);
        return `
          <tr>
            <td class="mono">${k} - ${labels[k]}</td>
            <td>
              <span class="badge ${cls}">
                <span class="dot"></span>
                <span>${v}/10</span>
              </span>
            </td>
            <td>${cls === "ok" ? "Bon" : cls === "warn" ? "À améliorer" : "Faible"}</td>
          </tr>
        `;
      })
      .join("");
  }

  function renderTips(tips) {
    if (!owaspTips) return;
    owaspTips.innerHTML = (tips || []).map((t) => `<li>${t}</li>`).join("");
  }

  // async function runOwasp(detail) {
  //   if (owaspStatus) owaspStatus.textContent = "Analyse en cours…";
  //   if (owaspRaw) owaspRaw.textContent = "Chargement…";

  //   try {
  //     const res = await apiFetch(
  //       `/api/admin/owasp-score?mode=safe&detail=${detail ? "true" : "false"}`,
  //       { method: "GET" }
  //     );

  //     if (res.status === 401 || res.status === 403) {
  //       if (owaspStatus) owaspStatus.textContent = `${res.status} - Accès refusé`;
  //       if (owaspRaw) owaspRaw.textContent = "Session expirée / pas ADMIN.";
  //       return;
  //     }

  //     const data = await res.json();

  //     owaspSetLastResult({
  //       total: data.total,
  //       grade: data.grade,
  //       mode: "safe",
  //       detail: !!detail,
  //       scores: data.scores || {},
  //       adviceFront: data.frontTips || [],
  //       frontUrl: window.location.origin,
  //       apiUrl: API_BASE,
  //     });

  //     const total = data.total;
  //     const grade = data.grade || "-";
  //     if (owaspStatus)
  //       owaspStatus.textContent = `TOTAL: ${total ?? "?"}/100  (Grade: ${grade})`;

  //     renderOwaspTable(data.scores || {});
  //     renderTips(data.frontTips || []);

  //     if (owaspRaw)
  //       owaspRaw.textContent = detail
  //         ? data.raw || ""
  //         : JSON.stringify(
  //             {
  //               total: data.total,
  //               grade: data.grade,
  //               scores: data.scores,
  //             },
  //             null,
  //             2
  //           );
  //   } catch (e) {
  //     if (owaspStatus) owaspStatus.textContent = "Erreur réseau";
  //     if (owaspRaw) owaspRaw.textContent = String(e);
  //   }
  // }

  async function fetchOwaspLast(detail) {
    if (owaspStatus) owaspStatus.textContent = "Chargement du dernier rapport…";
    if (owaspRaw) owaspRaw.textContent = "Chargement…";

    try {
      const res = await apiFetch(
        `/api/admin/owasp-score?detail=${detail ? "true" : "false"}`,
        { method: "GET" }
      );

      if (res.status === 401 || res.status === 403) {
        if (owaspStatus) owaspStatus.textContent = `${res.status} - Accès refusé`;
        if (owaspRaw) owaspRaw.textContent = "Session expirée / pas ADMIN.";
        return;
      }

      // si pas encore de rapport => 404 propre
      if (res.status === 404) {
        if (owaspStatus) owaspStatus.textContent = "Aucun rapport disponible";
        if (owaspRaw)
          owaspRaw.textContent =
            "Aucun audit n’a encore été généré. Utilise « Rafraîchir l’audit ».";
        renderOwaspTable({});
        renderTips([]);
        return;
      }

      const data = await res.json();

      owaspSetLastResult({
        total: data.total,
        grade: data.grade,
        detail: !!detail,
        scores: data.scores || {},
        adviceFront: data.frontTips || [],
        frontUrl: data.frontUrl || window.location.origin,
        apiUrl: data.apiUrl || API_BASE,
      });

      const total = data.total;
      const grade = data.grade || "-";
      if (owaspStatus)
        owaspStatus.textContent = `TOTAL: ${total ?? "?"}/100  (Grade: ${grade})`;

      renderOwaspTable(data.scores || {});
      renderTips(data.frontTips || []);

      if (owaspRaw) {
        owaspRaw.textContent = detail
          ? data.raw || ""
          : JSON.stringify(
              { total: data.total, grade: data.grade, scores: data.scores },
              null,
              2
            );
      }
    } catch (e) {
      if (owaspStatus) owaspStatus.textContent = "Erreur réseau";
      if (owaspRaw) owaspRaw.textContent = String(e);
    }
  }

  async function runOwaspAudit(detail) {
    if (owaspStatus) owaspStatus.textContent = "Exécution audit (SAFE)…";
    if (owaspRaw) owaspRaw.textContent = "Analyse en cours…";

    try {
      const res = await apiFetch(
        `/api/admin/owasp-score/run?detail=${detail ? "true" : "false"}`,
        { method: "POST" }
      );

      if (res.status === 401 || res.status === 403) {
        if (owaspStatus) owaspStatus.textContent = `${res.status} - Accès refusé`;
        if (owaspRaw) owaspRaw.textContent = "Session expirée / pas ADMIN.";
        return;
      }

      const data = await res.json().catch(() => null);

      if (!res.ok) {
        if (owaspStatus) owaspStatus.textContent = `Erreur ${res.status}`;
        if (owaspRaw) owaspRaw.textContent = JSON.stringify(data, null, 2);
        return;
      }

      // Après run, on affiche le résultat (retourne déjà latest)
      owaspSetLastResult({
        total: data.total,
        grade: data.grade,
        detail: !!detail,
        scores: data.scores || {},
        adviceFront: data.frontTips || [],
        frontUrl: data.frontUrl || window.location.origin,
        apiUrl: data.apiUrl || API_BASE,
      });

      const total = data.total;
      const grade = data.grade || "-";
      if (owaspStatus)
        owaspStatus.textContent = `TOTAL: ${total ?? "?"}/100  (Grade: ${grade})`;

      renderOwaspTable(data.scores || {});
      renderTips(data.frontTips || []);

      if (owaspRaw) {
        owaspRaw.textContent = detail
          ? data.raw || ""
          : JSON.stringify(
              { total: data.total, grade: data.grade, scores: data.scores },
              null,
              2
            );
      }
    } catch (e) {
      if (owaspStatus) owaspStatus.textContent = "Erreur réseau";
      if (owaspRaw) owaspRaw.textContent = String(e);
    }
  }


  // document.getElementById("btnOwaspRun")?.addEventListener("click", () => runOwasp(false));
  // document.getElementById("btnOwaspDetail")?.addEventListener("click", () => runOwasp(true));
  // document.getElementById("btnOwaspRun")?.addEventListener("click", () => fetchOwaspLast(false));
  // document.getElementById("btnOwaspDetail")?.addEventListener("click", () => fetchOwaspLast(true));
  document.getElementById("btnOwaspView")?.addEventListener("click", () => fetchOwaspLast(false));
  document.getElementById("btnOwaspViewDetail")?.addEventListener("click", () => fetchOwaspLast(true));
  document.getElementById("btnOwaspRefresh")?.addEventListener("click", () => runOwaspAudit(false));

  // ===============================
  // OWASP EXPORT (JSON / PDF)
  // ===============================
  window.__owaspLast = null;

  function owaspSetLastResult(obj) {
    window.__owaspLast = {
      generatedAt: new Date().toISOString(),
      ...obj,
    };
  }

  function downloadJson(filename, dataObj) {
    const json = JSON.stringify(dataObj, null, 2);
    const blob = new Blob([json], { type: "application/json;charset=utf-8" });
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();

    URL.revokeObjectURL(url);
  }

  function buildOwaspReportHtml(result) {
    const scores = result?.scores || {};
    const rows = Object.entries(scores)
      .map(([k, v]) => {
        const label = k;
        const score = `${v}/10`;
        return `<tr><td>${label}</td><td style="text-align:right">${score}</td></tr>`;
      })
      .join("");

    const advice = (result?.adviceFront || [])
      .map((a) => `<li>${escapeHtml(a)}</li>`)
      .join("");

    const total = result?.total ?? "-";
    const grade = result?.grade ?? "-";
    const mode = result?.mode ?? "safe";
    const frontUrl = result?.frontUrl ?? "-";
    const apiUrl = result?.apiUrl ?? "-";
    const ts = result?.generatedAt ?? new Date().toISOString();

    return `
<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>OWASP Report</title>
  <style>
    body{font-family:Arial,Helvetica,sans-serif;margin:24px;color:#111}
    h1{margin:0 0 6px 0;font-size:20px}
    .muted{color:#666;font-size:12px}
    .box{border:1px solid #ddd;border-radius:10px;padding:12px;margin-top:14px}
    table{width:100%;border-collapse:collapse;margin-top:8px}
    th,td{border-bottom:1px solid #eee;padding:8px;font-size:13px}
    th{text-align:left;background:#fafafa}
    .pill{display:inline-block;padding:4px 10px;border-radius:999px;background:#f3f3f3;font-size:12px}
    ul{margin:8px 0 0 18px}
    @media print {
      .no-print{display:none}
      body{margin:0}
    }
  </style>
</head>
<body>
  <div class="no-print" style="margin-bottom:10px">
    <button onclick="window.print()">Imprimer / Enregistrer en PDF</button>
  </div>

  <h1>Rapport Sécurité OWASP (SAFE)</h1>
  <div class="muted">Généré le : ${escapeHtml(ts)}</div>

  <div class="box">
    <div><b>Total</b> : <span class="pill">${escapeHtml(String(total))}/100</span></div>
    <div style="margin-top:6px"><b>Grade</b> : <span class="pill">${escapeHtml(String(grade))}</span></div>
    <div style="margin-top:6px"><b>Mode</b> : <span class="pill">${escapeHtml(String(mode))}</span></div>
    <div class="muted" style="margin-top:8px">
      Front : ${escapeHtml(frontUrl)}<br/>
      API : ${escapeHtml(apiUrl)}
    </div>
  </div>

  <div class="box">
    <b>Détails par catégorie</b>
    <table>
      <thead><tr><th>Catégorie</th><th style="text-align:right">Score</th></tr></thead>
      <tbody>${rows || `<tr><td colspan="2" class="muted">Aucune donnée</td></tr>`}</tbody>
    </table>
  </div>

  <div class="box">
    <b>Conseils d’amélioration (Front)</b>
    ${advice ? `<ul>${advice}</ul>` : `<div class="muted" style="margin-top:8px">Aucun conseil</div>`}
  </div>
</body>
</html>`;
  }

  function exportOwaspPdf(result) {
    const html = buildOwaspReportHtml(result);

    const iframe = document.createElement("iframe");
    iframe.style.position = "fixed";
    iframe.style.right = "0";
    iframe.style.bottom = "0";
    iframe.style.width = "0";
    iframe.style.height = "0";
    iframe.style.border = "0";
    iframe.setAttribute("aria-hidden", "true");

    document.body.appendChild(iframe);

    const doc = iframe.contentDocument || iframe.contentWindow.document;
    doc.open();
    doc.write(html);
    doc.close();

    setTimeout(() => {
      iframe.contentWindow.focus();
      iframe.contentWindow.print();
      setTimeout(() => iframe.remove(), 1000);
    }, 250);
  }

  // ----------------------------
  // Export status timer (7s) - CENTRALIZED
  // ----------------------------
  document.addEventListener("DOMContentLoaded", () => {
    const btnJson = document.getElementById("btnOwaspExportJson");
    const btnPdf = document.getElementById("btnOwaspExportPdf");
    const status = document.getElementById("owaspExportStatus");

    const DEFAULT_MS = 7000;
    let statusTimerId = null;

    function setStatus(msg, opts = {}) {
      const { autoClearMs = DEFAULT_MS, showTimer = true, clearTo = "-" } = opts;

      if (status) status.textContent = msg ?? "";

      const timer = document.getElementById("owaspExportTimer");
      const bar = timer?.querySelector(".export-timer-bar");

      // stop précédent countdown
      if (statusTimerId) {
        clearTimeout(statusTimerId);
        statusTimerId = null;
      }

      // pas de barre => on cache, et éventuellement auto clear simple
      if (!showTimer) {
        timer?.classList.add("hidden");
        if (autoClearMs > 0) {
          statusTimerId = setTimeout(() => {
            if (status) status.textContent = clearTo;
            statusTimerId = null;
          }, autoClearMs);
        }
        return;
      }

      // reset animation + show
      if (bar) {
        bar.style.animation = "none";
        bar.offsetHeight; // force reflow
        bar.style.animation = `export-countdown ${autoClearMs / 1000}s linear forwards`;
      }
      timer?.classList.remove("hidden");

      // auto clear après X ms
      if (autoClearMs > 0) {
        statusTimerId = setTimeout(() => {
          if (status) status.textContent = clearTo;
          timer?.classList.add("hidden");
          statusTimerId = null;
        }, autoClearMs);
      }
    }

    if (btnJson) {
      btnJson.addEventListener("click", () => {
        try {
          if (!window.__owaspLast) {
            setStatus("⚠️ Aucun résultat OWASP à exporter", {
              autoClearMs: DEFAULT_MS,
              showTimer: true,
            });
            return;
          }

          const name = `owasp_score_${new Date()
            .toISOString()
            .slice(0, 19)
            .replaceAll(":", "-")}.json`;

          downloadJson(name, window.__owaspLast);

          setStatus("✅ JSON exporté", {
            autoClearMs: DEFAULT_MS, // ✅ 7s
            showTimer: true,
          });
        } catch (e) {
          console.error(e);
          setStatus("❌ Erreur export JSON", {
            autoClearMs: DEFAULT_MS,
            showTimer: true,
          });
        }
      });
    }

    if (btnPdf) {
      btnPdf.addEventListener("click", () => {
        try {
          if (!window.__owaspLast) {
            setStatus("⚠️ Aucun résultat OWASP à exporter", {
              autoClearMs: DEFAULT_MS,
              showTimer: true,
            });
            return;
          }

          exportOwaspPdf(window.__owaspLast);

          setStatus("✅ PDF prêt (impression)", {
            autoClearMs: DEFAULT_MS, // ✅ 7s
            showTimer: true,
          });
        } catch (e) {
          console.error(e);
          setStatus("❌ Erreur export PDF", {
            autoClearMs: DEFAULT_MS,
            showTimer: true,
          });
        }
      });
    }
  });

  // ----------------------------
  // DB CRUD
  // ----------------------------
  const dbResourceSelect = document.getElementById("dbResourceSelect");
  const btnLoadData = document.getElementById("btnLoadData");
  const btnNewRow = document.getElementById("btnNewRow");
  const dbTableHead = document.getElementById("dbTableHead");
  const dbTableBody = document.getElementById("dbTableBody");
  const dbSearch = document.getElementById("dbSearch");
  const dbPageSize = document.getElementById("dbPageSize");
  const dbPagingInfo = document.getElementById("dbPagingInfo");
  const dbPrevPage = document.getElementById("dbPrevPage");
  const dbNextPage = document.getElementById("dbNextPage");
  const dbModalOverlay = document.getElementById("dbModalOverlay");
  const dbModalTitle = document.getElementById("dbModalTitle");
  const dbModalForm = document.getElementById("dbModalForm");
  const dbModalCancel = document.getElementById("dbModalCancel");
  const dbModalSave = document.getElementById("dbModalSave");
  const dbStatus = document.getElementById("dbStatus");

  const DB_ENDPOINTS = {
    users: `/api/admin/users`,
    tickets: `/api/admin/tickets`,
    ticket_gains: `/api/admin/ticket-gains`,
    refresh_tokens: `/api/admin/refresh-tokens`,
  };

  const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);
  const SENSITIVE_PARTS = [
    "password",
    "hashed",
    "hash",
    "token",
    "refresh",
    "access",
    "secret",
    "apikey",
    "api_key",
    "session",
    "cookie",
    "reset",
  ];

  function isSensitiveKey(key) {
    const k = String(key || "").toLowerCase();
    return SENSITIVE_PARTS.some((p) => k.includes(p));
  }

  function maskValue(v) {
    if (v === null || v === undefined || v === "") return "";
    const s = String(v);
    if (s.length <= 6) return "••••••";
    return s.slice(0, 3) + "••••••" + s.slice(-3);
  }

  function safeCellValue(key, value) {
    if (value === null || value === undefined) return "";
    if (isSensitiveKey(key)) return maskValue(value);
    if (typeof value === "object") return JSON.stringify(value);
    return String(value);
  }

  function setDbStatus(text, isError = false) {
    if (!dbStatus) return;
    dbStatus.textContent = text;
    dbStatus.classList.toggle("danger", isError);
  }

  const EDITABLE_FIELDS = {
    users: ["firstName", "lastName", "email", "role", "admin"],
    tickets: ["numbers", "chanceNumber", "drawDate", "drawDay", "userEmail"],
    ticket_gains: ["ticketId", "rank", "gainAmount", "drawDate", "userEmail"],
  };

  const PROTECTED_FIELDS = new Set([
    "id",
    "_id",
    "created_at",
    "updated_at",
    "createdAt",
    "updatedAt",
  ]);

  let dbCurrentResource = "users";
  let dbRawData = [];
  let dbFilteredData = [];
  let dbCurrentPage = 0;
  let dbEditingRow = null;

  function applyDbFilter() {
    const q = (dbSearch?.value || "").toLowerCase().trim();
    dbFilteredData = !q
      ? dbRawData.slice()
      : dbRawData.filter((r) => JSON.stringify(r).toLowerCase().includes(q));
    dbCurrentPage = 0;
    renderDbTable();
  }

  function renderDbTable() {
    if (!dbTableHead || !dbTableBody) return;
    dbTableHead.innerHTML = "";
    dbTableBody.innerHTML = "";

    const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
    if (btnNewRow) {
      btnNewRow.disabled = isReadOnly;
      btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
      btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";
    }

    if (!dbFilteredData.length) {
      dbTableBody.innerHTML =
        "<tr><td style='padding:10px;color:#9ca3af;'>Aucune donnée.</td></tr>";
      if (dbPagingInfo) dbPagingInfo.textContent = "Page 1 / 1";
      return;
    }

    const pageSize = parseInt(dbPageSize?.value || "20", 10);
    const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
    dbCurrentPage = Math.min(dbCurrentPage, totalPages - 1);

    const start = dbCurrentPage * pageSize;
    const end = Math.min(start + pageSize, dbFilteredData.length);
    const pageData = dbFilteredData.slice(start, end);

    const keys = Object.keys(pageData[0] || {});

    const headerRow = document.createElement("tr");
    keys.forEach((k) => {
      const th = document.createElement("th");
      th.textContent = k;
      th.className = "th";
      headerRow.appendChild(th);
    });

    if (!isReadOnly) {
      const th = document.createElement("th");
      th.textContent = "Actions";
      th.className = "th";
      headerRow.appendChild(th);
    }

    dbTableHead.appendChild(headerRow);

    pageData.forEach((row) => {
      const tr = document.createElement("tr");
      tr.className = "tr";

      keys.forEach((k) => {
        const td = document.createElement("td");
        td.className = "td";

        if (dbCurrentResource === "users" && k === "tickets") {
          const tickets = safeJsonParse(row[k]);
          const list = Array.isArray(tickets) ? tickets : [];

          // ✅ fix: email de la ligne (pas une variable inexistante)
          lastTicketsForCopy = list;
          lastTicketsUserEmail = row.email || "";

          const count = list.length;

          const btn = document.createElement("button");
          btn.type = "button";
          btn.className = "tickets-open";
          btn.textContent = count ? `Voir (${count})` : "Voir";

          btn.addEventListener("click", () => {
            openTicketsModal(list, row.email || "");
          });

          td.appendChild(btn);
        } else {
          td.textContent = safeCellValue(k, row[k]);
        }

        if (k === "id" || k === "_id") {
          td.classList.add("td-id");
          td.title = row[k] ? String(row[k]) : "";
        }

        tr.appendChild(td);
      });

      if (!isReadOnly) {
        const td = document.createElement("td");
        td.className = "td td-actions";

        const id = row.id || row._id;

        const btnEdit = document.createElement("button");
        btnEdit.className = "btn-mini btn-blue";
        btnEdit.textContent = "Modifier";
        btnEdit.addEventListener("click", () => openDbModal("edit", row));

        const btnDel = document.createElement("button");
        btnDel.className = "btn-mini btn-red";
        btnDel.textContent = "Supprimer";
        btnDel.addEventListener("click", async () => {
          if (!id) return showToast("ID introuvable.", "error");

          const ok = await confirmDialog({
            title: "Supprimer",
            message: `Supprimer ${dbCurrentResource} #${id} ?`,
            okText: "Supprimer",
            danger: true,
          });
          if (!ok) return;

          const url = `${DB_ENDPOINTS[dbCurrentResource]}/${id}`;
          const res = await apiFetch(url, { method: "DELETE" });
          const body = await res.text().catch(() => "");

          if (!res.ok) {
            showToast(`Erreur ${res.status} suppression`, "error");
            console.error("DELETE error:", res.status, body);
            return;
          }

          dbRawData = dbRawData.filter((r) => (r.id || r._id) !== id);
          applyDbFilter();
          showToast("Suppression OK", "success");
        });

        td.append(btnEdit, btnDel);
        tr.appendChild(td);
      }

      dbTableBody.appendChild(tr);
    });

    if (dbPagingInfo) dbPagingInfo.textContent = `Page ${dbCurrentPage + 1} / ${totalPages}`;
  }

  async function loadDbData() {
    if (!dbResourceSelect) return;
    dbCurrentResource = dbResourceSelect.value;

    const url = DB_ENDPOINTS[dbCurrentResource];
    if (!url) return setDbStatus("Endpoint non configuré", true);

    setDbStatus(`Chargement: ${dbCurrentResource}...`);
    if (dbTableBody) {
      dbTableBody.innerHTML =
        "<tr><td style='padding:10px;color:#9ca3af;'>Chargement...</td></tr>";
    }

    try {
      const res = await apiFetch(url, { method: "GET" });
      const raw = await res.text();

      console.log("[DB] GET", url, "status =", res.status);

      if (!res.ok) {
        setDbStatus(`Erreur ${res.status} sur ${dbCurrentResource}`, true);
        if (dbTableBody) {
          dbTableBody.innerHTML = `<tr><td style="padding:10px;color:#f97373;">${
            raw || "Erreur inconnue"
          }</td></tr>`;
        }
        return;
      }

      let data;
      try {
        data = JSON.parse(raw);
      } catch {
        setDbStatus("Réponse invalide (JSON attendu)", true);
        if (dbTableBody) {
          dbTableBody.innerHTML = `<tr><td style="padding:10px;color:#f97373;">Réponse non-JSON : ${raw.slice(
            0,
            250
          )}</td></tr>`;
        }
        return;
      }

      if (!Array.isArray(data)) {
        setDbStatus("Format inattendu (tableau attendu)", true);
        if (dbTableBody) {
          dbTableBody.innerHTML = `<tr><td style="padding:10px;color:#f97373;">${JSON.stringify(
            data
          ).slice(0, 500)}</td></tr>`;
        }
        return;
      }

      dbRawData = data;
      setDbStatus(`${dbRawData.length} lignes`);
      applyDbFilter();
    } catch (e) {
      console.error("[DB] exception:", e);
      setDbStatus("Erreur réseau / CORS / serveur", true);
      if (dbTableBody) {
        dbTableBody.innerHTML = `<tr><td style="padding:10px;color:#f97373;">${String(e)}</td></tr>`;
      }
    }
  }

  function openDbModal(mode, row) {
    if (READ_ONLY_RESOURCES.has(dbCurrentResource))
      return showToast("Lecture seule.", "error");
    if (!dbModalForm || !dbModalOverlay || !dbModalTitle) return;

    dbEditingRow = { mode, row: row || null };
    dbModalForm.innerHTML = "";

    const sample = row || dbRawData[0] || {};
    const allKeys = Object.keys(sample);
    const allowed = EDITABLE_FIELDS[dbCurrentResource] || allKeys;

    const displayKeys = [
      ...allKeys.filter((k) => k === "id" || k === "_id"),
      ...allowed.filter((k) => !["id", "_id"].includes(k)),
    ].filter((v, i, a) => a.indexOf(v) === i);

    displayKeys.forEach((k) => {
      const label = document.createElement("label");
      label.className = "modal-label";
      label.textContent = k + (isSensitiveKey(k) ? " (masqué)" : "");

      let input;
      if (k === "admin") {
        input = document.createElement("select");
        input.className = "modal-input";
        input.name = k;
        input.innerHTML = `<option value="true">true</option><option value="false">false</option>`;
        input.value = row ? String(!!row[k]) : "false";
      } else if (k === "role") {
        input = document.createElement("select");
        input.className = "modal-input";
        input.name = k;
        input.innerHTML = `<option value="ROLE_USER">ROLE_USER</option><option value="ROLE_ADMIN">ROLE_ADMIN</option>`;
        input.value = row?.[k] ? String(row[k]) : "ROLE_USER";
      } else {
        input = document.createElement("input");
        input.className = "modal-input";
        input.name = k;
        const v = row ? row[k] ?? "" : "";
        input.value = typeof v === "object" ? JSON.stringify(v) : String(v ?? "");
      }

      if (
        PROTECTED_FIELDS.has(k) ||
        isSensitiveKey(k) ||
        (EDITABLE_FIELDS[dbCurrentResource] &&
          !allowed.includes(k) &&
          k !== "id" &&
          k !== "_id")
      ) {
        input.disabled = true;
        input.style.opacity = "0.6";
        if (isSensitiveKey(k)) input.value = maskValue(input.value);
      }

      const wrap = document.createElement("div");
      wrap.className = "modal-field";
      wrap.append(label, input);
      dbModalForm.appendChild(wrap);
    });

    dbModalTitle.textContent =
      mode === "edit" ? `Modifier #${row?.id || row?._id || ""}` : "Nouvelle ligne";

    dbModalOverlay.style.display = "flex";
    dbModalOverlay.setAttribute("aria-hidden", "false");
  }

  function closeDbModal() {
    if (!dbModalOverlay) return;
    dbModalOverlay.style.display = "none";
    dbModalOverlay.setAttribute("aria-hidden", "true");
    dbEditingRow = null;
  }

  async function saveDbModal() {
    if (!dbEditingRow || !dbModalForm) return;

    const obj = {};
    const inputs = dbModalForm.querySelectorAll("input, select, textarea");
    inputs.forEach((el) => {
      if (!el.name) return;
      if (el.disabled) return;

      const v = el.value;
      if (el.name === "admin") {
        obj.admin = v === "true";
        return;
      }
      obj[el.name] = v === "" ? null : v;
    });

    const base = DB_ENDPOINTS[dbCurrentResource];
    let url = base;
    let method = "POST";

    if (dbEditingRow.mode === "edit") {
      const id = dbEditingRow.row.id || dbEditingRow.row._id;
      url = `${base}/${id}`;
      method = "PUT";
    }

    const res = await apiFetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(obj),
    });

    const body = await res.text().catch(() => "");
    if (!res.ok) {
      showToast(`Erreur ${res.status}`, "error");
      console.error("SAVE error:", res.status, body);
      return;
    }

    closeDbModal();
    await loadDbData();
    showToast("Enregistré ✅", "success");
  }

  btnLoadData?.addEventListener("click", loadDbData);
  dbSearch?.addEventListener("input", applyDbFilter);
  dbPageSize?.addEventListener("change", () => {
    dbCurrentPage = 0;
    renderDbTable();
  });
  dbPrevPage?.addEventListener("click", () => {
    if (dbCurrentPage > 0) {
      dbCurrentPage--;
      renderDbTable();
    }
  });
  dbNextPage?.addEventListener("click", () => {
    const pageSize = parseInt(dbPageSize?.value || "20", 10);
    const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
    if (dbCurrentPage < totalPages - 1) {
      dbCurrentPage++;
      renderDbTable();
    }
  });
  btnNewRow?.addEventListener("click", () => openDbModal("create", null));
  dbModalCancel?.addEventListener("click", closeDbModal);
  dbModalSave?.addEventListener("click", saveDbModal);
  dbModalOverlay?.addEventListener("click", (e) => {
    if (e.target === dbModalOverlay) closeDbModal();
  });

  // ---------- Tickets modal helpers ----------
  const ticketsOverlay = document.getElementById("ticketsOverlay");
  const ticketsClose = document.getElementById("ticketsClose");
  const ticketsCopy = document.getElementById("ticketsCopy");
  let lastTicketsForCopy = [];
  let lastTicketsUserEmail = "";
  const ticketsBody = document.getElementById("ticketsBody");
  const ticketsTitle = document.getElementById("ticketsTitle");
  const ticketsMeta = document.getElementById("ticketsMeta");

  function safeJsonParse(v) {
    if (v === null || v === undefined) return null;
    if (Array.isArray(v)) return v;
    if (typeof v === "object") return v;
    const s = String(v).trim();
    if (!s) return null;
    try {
      return JSON.parse(s);
    } catch {
      return null;
    }
  }

  function fmtDateTime(x) {
    if (!x) return "—";
    const d = new Date(x);
    if (Number.isNaN(d.getTime())) return String(x);
    return d.toLocaleString("fr-FR");
  }

  function renderBalls(numbers) {
    const wrap = document.createElement("div");
    wrap.className = "ball-row";
    const arr = String(numbers || "")
      .split("-")
      .map((n) => n.trim())
      .filter(Boolean);

    if (!arr.length) {
      wrap.textContent = "—";
      return wrap;
    }

    arr.forEach((n) => {
      const s = document.createElement("span");
      s.className = "ball blue";
      s.textContent = n;
      wrap.appendChild(s);
    });
    return wrap;
  }

  function openTicketsModal(tickets, userEmail) {
    if (!ticketsOverlay || !ticketsBody || !ticketsTitle) return;

    const list = Array.isArray(tickets) ? tickets : [];
    ticketsTitle.textContent = "Tickets";
    if (ticketsMeta) {
      ticketsMeta.textContent =
        `${list.length} ticket(s)` + (userEmail ? ` • ${userEmail}` : "");
    }

    ticketsBody.innerHTML = "";

    if (!list.length) {
      ticketsBody.innerHTML = `<tr><td colspan="6" class="muted" style="padding:12px;">Aucun ticket.</td></tr>`;
    } else {
      list.forEach((t) => {
        const tr = document.createElement("tr");

        const tdNumbers = document.createElement("td");
        tdNumbers.appendChild(renderBalls(t.numbers));

        const tdChance = document.createElement("td");
        const c = document.createElement("span");
        c.className = "ball red";
        c.textContent = t.chanceNumber ?? "—";
        tdChance.appendChild(c);

        const tdDate = document.createElement("td");
        tdDate.textContent = t.drawDate || "—";

        const tdDay = document.createElement("td");
        tdDay.textContent = t.drawDay || "—";

        const tdCreated = document.createElement("td");
        tdCreated.textContent = fmtDateTime(t.createdAt);

        const tdUpdated = document.createElement("td");
        tdUpdated.textContent = fmtDateTime(t.updatedAt);

        tr.append(tdNumbers, tdChance, tdDate, tdDay, tdCreated, tdUpdated);
        ticketsBody.appendChild(tr);
      });
    }

    ticketsOverlay.style.display = "flex";
    ticketsOverlay.setAttribute("aria-hidden", "false");
  }

  function closeTicketsModal() {
    if (!ticketsOverlay) return;
    ticketsOverlay.style.display = "none";
    ticketsOverlay.setAttribute("aria-hidden", "true");
    if (ticketsBody) ticketsBody.innerHTML = "";
  }

  ticketsClose?.addEventListener("click", closeTicketsModal);
  ticketsOverlay?.addEventListener("click", (e) => {
    if (e.target === ticketsOverlay) closeTicketsModal();
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeTicketsModal();
  });

  function buildTicketsText(list, email) {
    const lines = [];
    if (email) lines.push(`Tickets — ${email}`);
    lines.push(`Total: ${list.length}`);
    lines.push("");

    list.forEach((t, idx) => {
      const nums = t.numbers ? String(t.numbers) : "—";
      const chance = t.chanceNumber ?? "—";
      const date = t.drawDate || "—";
      const day = t.drawDay || "—";
      const created = fmtDateTime(t.createdAt);
      const updated = fmtDateTime(t.updatedAt);

      lines.push(
        `${String(idx + 1).padStart(2, "0")}. ${nums} | Chance:${chance} | ${date} (${day}) | C:${created} | U:${updated}`
      );
    });

    return lines.join("\n");
  }

  async function copyToClipboard(text) {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch {
      try {
        const ta = document.createElement("textarea");
        ta.value = text;
        ta.style.position = "fixed";
        ta.style.left = "-9999px";
        document.body.appendChild(ta);
        ta.select();
        const ok = document.execCommand("copy");
        ta.remove();
        return ok;
      } catch {
        return false;
      }
    }
  }

  ticketsCopy?.addEventListener("click", async () => {
    const txt = buildTicketsText(lastTicketsForCopy || [], lastTicketsUserEmail || "");
    const ok = await copyToClipboard(txt);
    showToast(ok ? "Tickets copiés ✅" : "Copie impossible ❌", ok ? "success" : "error");
  });

  // ----------------------------
  // STATS
  // ----------------------------
  const btnLoadStats = document.getElementById("btnLoadStats");
  const statsGrid = document.getElementById("statsGrid");
  const statsSearch = document.getElementById("statsSearch");
  const statsStatus = document.getElementById("statsStatus");
  const STATS_ENDPOINT = `/api/admin/users-stats`;

  let statsRaw = [];
  let statsFiltered = [];

  function setStatsStatus(text, isError = false) {
    if (!statsStatus) return;
    statsStatus.textContent = text;
    statsStatus.classList.toggle("danger", isError);
  }

  function renderStats() {
    if (!statsGrid) return;
    statsGrid.innerHTML = "";

    if (!statsFiltered.length) {
      statsGrid.innerHTML = "<div class='muted'>Aucune statistique.</div>";
      return;
    }

    statsFiltered.forEach((u) => {
      const card = document.createElement("article");
      card.className = "stat-card";
      card.innerHTML = `
        <div class="stat-name">${u.firstName || ""} ${u.lastName || ""}</div>
        <div class="stat-email">${u.email || ""}</div>
        <div class="stat-footer">
          Tickets: <b>${u.ticketsCount || 0}</b> • Gain total: <b>${u.totalGain || 0}€</b>
        </div>
      `;
      statsGrid.appendChild(card);
    });
  }

  function applyStatsFilter() {
    const q = (statsSearch?.value || "").toLowerCase().trim();
    statsFiltered = !q
      ? statsRaw.slice()
      : statsRaw.filter((u) => (u.email || "").toLowerCase().includes(q));
    renderStats();
  }

  async function loadStats() {
    if (!statsGrid) return;

    statsGrid.innerHTML = "<div class='muted'>Chargement...</div>";
    setStatsStatus("Chargement...");

    try {
      const res = await apiFetch(STATS_ENDPOINT, { method: "GET" });
      const data = await res.json().catch(() => null);
      if (!res.ok) throw new Error(JSON.stringify(data));

      statsRaw = Array.isArray(data) ? data : [];
      statsFiltered = statsRaw.slice();
      renderStats();
      setStatsStatus(`${statsRaw.length} joueurs`);
    } catch (e) {
      console.error(e);
      setStatsStatus("Erreur stats", true);
      statsGrid.innerHTML = "<div class='danger'>Erreur chargement</div>";
    }
  }

  btnLoadStats?.addEventListener("click", loadStats);
  statsSearch?.addEventListener("input", applyStatsFilter);

  // ----------------------------
  // INIT
  // ----------------------------
  showSection("swagger");
  loadAdminUser();
})();
