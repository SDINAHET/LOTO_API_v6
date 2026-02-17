/* admin-dashboard.js
 * Requiert layout-dashboard.js (window.API_BASE + window.apiFetch)
 * - Ping: /admin/ping
 * - Logs: /api/admin/logs?lines=...
 * - CRUD: /api/admin/**
 */

(() => {
  "use strict";

  // Fallback si showToast n'existe pas
  if (typeof window.showToast !== "function") {
    window.showToast = function (msg, type = "info") {
      console.log(`[${type}]`, msg);
    };
  }

  // ===============================
  // ✅ Status panel (encadré bas-droite)
  // Usage:
  //   const sp = getStatusPanel();
  //   sp.show("✅ OK", { variant:"ok", ms:5000, status:200 });
  //   sp.show("❌ Forbidden", { variant:"err", ms:8000, status:403, details:"JWT expired" });
  // ===============================
  // function getStatusPanel() {
  //   let wrap = document.getElementById("adminStatusPanel");

  //   if (!wrap) {
  //     wrap = document.createElement("div");
  //     wrap.id = "adminStatusPanel";
  //     wrap.className = "status-panel hidden";
  //     wrap.innerHTML = `
  //       <div class="status-panel-head">
  //         <span class="status-panel-badge">—</span>
  //         <span class="status-panel-title">Statut</span>
  //         <button type="button" class="status-panel-close" aria-label="Fermer">×</button>
  //       </div>
  //       <div class="status-panel-msg">—</div>
  //       <div class="status-panel-details"></div>
  //     `;
  //     document.body.appendChild(wrap);

  //     const btnClose = wrap.querySelector(".status-panel-close");
  //     btnClose?.addEventListener("click", () => wrap.classList.add("hidden"));
  //   }

  //   let timerId = null;

  //   // function notifyAction({ variant = "ok", title = "Action", status = 200, message = "", details = "", ms = 7000 }) {
  //   //   const sp = getStatusPanel();
  //   //   sp.show(message, { variant, title, status, details, ms });
  //   // }


  //   function show(message, options = {}) {
  //     const {
  //       variant = "ok",       // ok | warn | err
  //       ms = 6000,            // durée
  //       status = null,        // code HTTP
  //       details = "",         // texte complémentaire (body)
  //       title = "Statut",     // titre
  //     } = options;

  //     const badgeEl = wrap.querySelector(".status-panel-badge");
  //     const titleEl = wrap.querySelector(".status-panel-title");
  //     const msgEl = wrap.querySelector(".status-panel-msg");
  //     const detEl = wrap.querySelector(".status-panel-details");

  //     wrap.classList.remove("hidden", "ok", "warn", "err");
  //     wrap.classList.add(variant);

  //     if (badgeEl) badgeEl.textContent = status != null ? String(status) : "—";
  //     if (titleEl) titleEl.textContent = title;
  //     if (msgEl) msgEl.textContent = message ?? "";

  //     if (detEl) {
  //       detEl.textContent = details ? String(details).slice(0, 500) : "";
  //       detEl.style.display = details ? "block" : "none";
  //     }

  //     if (timerId) clearTimeout(timerId);
  //     timerId = setTimeout(() => {
  //       wrap.classList.add("hidden");
  //       timerId = null;
  //     }, ms);
  //   }

  //   return { show };
  // }

  const getStatusPanel = (() => {
    let instance = null;  // ✅ singleton
    let timerId = null;

    return function () {
      if (instance) return instance;

      let wrap = document.getElementById("adminStatusPanel");

      if (!wrap) {
        wrap = document.createElement("div");
        wrap.id = "adminStatusPanel";
        wrap.className = "status-panel hidden";
        wrap.innerHTML = `
          <div class="status-panel-head">
            <span class="status-panel-badge">—</span>
            <span class="status-panel-title">Statut</span>
            <button type="button" class="status-panel-close" aria-label="Fermer">×</button>
          </div>
          <div class="status-panel-msg">—</div>
          <div class="status-panel-details"></div>
        `;
        document.body.appendChild(wrap);

        wrap.querySelector(".status-panel-close")
          ?.addEventListener("click", () => wrap.classList.add("hidden"));
      }

      function show(message, options = {}) {
        const {
          variant = "ok",
          ms = 6000,
          status = null,
          details = "",
          title = "Statut",
        } = options;

        const badgeEl = wrap.querySelector(".status-panel-badge");
        const titleEl = wrap.querySelector(".status-panel-title");
        const msgEl = wrap.querySelector(".status-panel-msg");
        const detEl = wrap.querySelector(".status-panel-details");

        wrap.classList.remove("hidden", "ok", "warn", "err");
        wrap.classList.add(variant);

        if (badgeEl) badgeEl.textContent = status != null ? String(status) : "—";
        if (titleEl) titleEl.textContent = title;
        if (msgEl) msgEl.textContent = message ?? "";

        if (detEl) {
          detEl.textContent = details ? String(details).slice(0, 500) : "";
          detEl.style.display = details ? "block" : "none";
        }

        if (timerId) clearTimeout(timerId);
        timerId = setTimeout(() => {
          wrap.classList.add("hidden");
          timerId = null;
        }, ms);
      }

      instance = { show };
      return instance;
    };
  })();

  function notifyModal({ message="", type="success", ms=5000 } = {}) {
    setDbModalSaveStatus(message, type, ms);
  }



  // ✅ helper global (utilisable partout)
  function notifyAction({
    variant = "ok",
    title = "Action",
    status = 200,
    message = "",
    details = "",
    ms = 7000
  } = {}) {
    getStatusPanel().show(message, { variant, title, status, details, ms });
  }

  function notifyDashboard({ variant="ok", title="Info", status=null, message="", details="", ms=7000 } = {}) {
    getStatusPanel().show(message, { variant, title, status, details, ms });
  }



  // ✅ rester dans le modal "Modifier" après save (cas Tickets)
  let __stayInEditAfterSave = false;


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

  function notify({ where="dashboard", ...payload } = {}) {
    // where: "dashboard" | "modal" | "both"
    if (where === "dashboard" || where === "both") {
      notifyDashboard(payload);
    }
    if (where === "modal" || where === "both") {
      // on mappe variant -> type
      const type =
        payload.variant === "err"  ? "error" :
        payload.variant === "warn" ? "warn"  : "success";

      notifyModal({
        message: payload.message || "",
        type,
        ms: payload.ms ?? 5000
      });
    }
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
// ✅ Confirm dialog (Promise<boolean>)
// Compatible avec: await confirmDialog({title,message,okText,danger})
// ----------------------------
function confirmDialog(opts = {}) {
  const {
    title = "Confirmation",
    message = "Confirmer ?",
    okText = "OK",
    cancelText = "Annuler",
    danger = false,
  } = opts;

  return new Promise((resolve) => {
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.style.display = "flex";
    overlay.style.alignItems = "center";
    overlay.style.justifyContent = "center";

    const modal = document.createElement("div");
    modal.className = "modal modal-confirm";
    modal.setAttribute("role", "dialog");
    modal.setAttribute("aria-modal", "true");
    modal.setAttribute("aria-label", title);

    modal.innerHTML = `
      <h3 style="margin:0 0 10px;font-weight:900;">${escapeHtml(title)}</h3>
      <p style="margin:0 0 14px;color:rgba(255,255,255,.8);">${escapeHtml(message)}</p>
      <div style="display:flex;justify-content:flex-end;gap:10px;">
        <button type="button" class="btn btn-ghost" data-action="cancel">${escapeHtml(cancelText)}</button>
        <button type="button" class="btn ${danger ? "danger" : "btn-primary"}" data-action="ok">${escapeHtml(okText)}</button>
      </div>
    `;

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    const okBtn = modal.querySelector('[data-action="ok"]');
    const cancelBtn = modal.querySelector('[data-action="cancel"]');

    function cleanup(result) {
      document.removeEventListener("keydown", onKeyDown, true);
      overlay.remove();
      resolve(result);
    }

    function onKeyDown(e) {
      if (e.key === "Escape") cleanup(false);
      if (e.key === "Enter") cleanup(true);
    }

    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) cleanup(false);
    });

    okBtn?.addEventListener("click", () => cleanup(true));
    cancelBtn?.addEventListener("click", () => cleanup(false));
    document.addEventListener("keydown", onKeyDown, true);

    (okBtn || cancelBtn)?.focus?.();
  });
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

  // function renderTips(tips) {
  //   if (!owaspTips) return;
  //   owaspTips.innerHTML = (tips || []).map((t) => `<li>${t}</li>`).join("");
  // }
  function renderTips(tips) {
    if (!owaspTips) return;
    owaspTips.innerHTML = (tips || [])
      .map((t) => `<li>${escapeHtml(t)}</li>`)
      .join("");
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

  // const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);
  const READ_ONLY_RESOURCES = new Set([
    "refresh_tokens",
    "ticket_gains",   // 👈 AJOUTE ÇA
    // "tickets"
  ]);

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

  // ----------------------------
// USERS INDEX (pour badge + filtre tickets)
// ----------------------------
let __usersIndexLoaded = false;
let __userById = new Map();          // id -> user
let __deletedUserIds = new Set();    // ids "deleted"

// Détection soft-delete / anonymisation
function isUserDeleted(u) {
  if (!u) return false;
  const email = String(u.email || "").toLowerCase();
  const first = String(u.firstName || "").toUpperCase();

  // règles demandées + un fallback robuste
  if (email.startsWith("deleted_")) return true;
  if (first === "DELETED") return true;

  // si tu as un champ enabled un jour
  if (u.enabled === false) return true;

  // si tu utilises un status
  const status = String(u.status || "").toUpperCase();
  if (status === "DELETED" || status === "DISABLED") return true;

  return false;
}

async function ensureUsersIndex() {
  if (__usersIndexLoaded) return;

  try {
    const res = await apiFetch(DB_ENDPOINTS.users, { method: "GET" });
    const data = await res.json().catch(() => null);
    if (!res.ok || !Array.isArray(data)) {
      console.warn("[USERS INDEX] impossible de charger users");
      __usersIndexLoaded = true; // évite boucle
      return;
    }

    __userById = new Map();
    __deletedUserIds = new Set();

    data.forEach((u) => {
      const id = u.id || u._id;
      if (!id) return;
      __userById.set(String(id), u);
      if (isUserDeleted(u)) __deletedUserIds.add(String(id));
    });

    __usersIndexLoaded = true;
  } catch (e) {
    console.warn("[USERS INDEX] erreur", e);
    __usersIndexLoaded = true;
  }
}



let usersFilter = localStorage.getItem("usersFilter") || "active";

function ensureUsersFilterUI() {
  const search = document.getElementById("dbSearch");
  if (!search) return;

  const shouldShow = dbCurrentResource === "users";
  const existing = document.getElementById("usersFilterWrap");

  if (existing) {
    existing.style.display = shouldShow ? "inline-flex" : "none";
    const sel = existing.querySelector("#usersFilter");
    if (sel) sel.value = usersFilter;
    return;
  }

  const wrap = document.createElement("label");
  wrap.id = "usersFilterWrap";
  wrap.className = "db-toggle";
  wrap.style.display = shouldShow ? "inline-flex" : "none";

  wrap.innerHTML = `
    <span class="db-toggle-label">Users</span>
    <select id="usersFilter" class="select select-sm">
      <option value="active">Actifs</option>
      <option value="all">Tous</option>
      <option value="disabled">Désactivés</option>
    </select>
  `;

  search.parentElement?.appendChild(wrap);

  const sel = wrap.querySelector("#usersFilter");
  sel.value = usersFilter;

  sel.addEventListener("change", () => {
    usersFilter = sel.value;
    localStorage.setItem("usersFilter", usersFilter);
    applyDbFilter();
  });
}

// ----------------------------
// Toggle: masquer tickets de users deleted
// ----------------------------
// Filtre tickets : "all" | "active" | "disabled"
let ticketsUserFilter = localStorage.getItem("ticketsUserFilter") || "active";
// "active" par défaut = RNCP-friendly (les deleted ne s'affichent pas)

function ensureDeletedToggleUI() {
  const search = document.getElementById("dbSearch");
  if (!search) return;

  const shouldShow = dbCurrentResource === "tickets";

  // déjà injecté => on montre/cache + sync value
  const existing = document.getElementById("ticketsUserFilterWrap");
  if (existing) {
    existing.style.display = shouldShow ? "inline-flex" : "none";
    const sel = existing.querySelector("#ticketsUserFilter");
    if (sel) sel.value = ticketsUserFilter;
    return;
  }

  // injection UI (discrète)
  const wrap = document.createElement("label");
  wrap.id = "ticketsUserFilterWrap";
  wrap.className = "db-toggle";
  wrap.style.display = shouldShow ? "inline-flex" : "none";

  wrap.innerHTML = `
    <span class="db-toggle-label">Comptes</span>
    <select id="ticketsUserFilter" class="select select-sm">
      <option value="active">Actifs</option>
      <option value="all">Tous</option>
      <option value="disabled">Désactivés</option>
    </select>
  `;


  search.parentElement?.appendChild(wrap);

  const sel = wrap.querySelector("#ticketsUserFilter");
  sel.value = ticketsUserFilter;

  sel.addEventListener("change", () => {
    ticketsUserFilter = sel.value;
    localStorage.setItem("ticketsUserFilter", ticketsUserFilter);
    applyDbFilter();
  });
}



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
    tickets: ["numbers", "chanceNumber", "drawDate", "drawDay", "userId"],
    // tickets: ["numbers", "chanceNumber", "drawDate", "drawDay", "userEmail"],
    ticket_gains: ["ticketId", "rank", "gainAmount", "drawDate", "userEmail"],
  };

  const HIDDEN_FIELDS_BY_RESOURCE = {
    tickets: new Set(["drawDay"]),
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

  // function applyDbFilter() {
  //   const q = (dbSearch?.value || "").toLowerCase().trim();
  //   dbFilteredData = !q
  //     ? dbRawData.slice()
  //     : dbRawData.filter((r) => JSON.stringify(r).toLowerCase().includes(q));
  //   dbCurrentPage = 0;
  //   renderDbTable();
  // }

  function updateUsersFilterDot(value) {
  const el = document.getElementById("usersFilterDot");
  if (!el) return;
  el.className = `status-dot ${value === "disabled" ? "red" : "green"}`;
  el.title = value === "disabled" ? "Comptes désactivés" : "Comptes actifs";
}


  function applyDbFilter() {
    const q = (dbSearch?.value || "").toLowerCase().trim();

    let arr = !q
      ? dbRawData.slice()
      : dbRawData.filter((r) => JSON.stringify(r).toLowerCase().includes(q));

    // ✅ filtre tickets selon l'état (all / active / disabled)
    if (dbCurrentResource === "tickets" && ticketsUserFilter !== "all") {
      arr = arr.filter((r) => {
        const uid = String(r.userid || r.userId || r.user_id || "").trim();
        if (!uid) return true;

        const isDisabled = __deletedUserIds.has(uid);

        if (ticketsUserFilter === "active") return !isDisabled;
        if (ticketsUserFilter === "disabled") return isDisabled;
        return true;
      });
    }

    // ✅ filtre users selon l'état (all / active / disabled)  <-- AJOUT
    if (dbCurrentResource === "users" && usersFilter !== "all") {
      arr = arr.filter((u) => {
        const disabled = isUserDeleted(u);
        if (usersFilter === "active") return !disabled;
        if (usersFilter === "disabled") return disabled;
        return true;
      });
    }

    dbFilteredData = arr;
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

    const pageSize = parseInt(dbPageSize?.value || "8", 10);
    const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
    dbCurrentPage = Math.min(dbCurrentPage, totalPages - 1);

    const start = dbCurrentPage * pageSize;
    const end = Math.min(start + pageSize, dbFilteredData.length);
    const pageData = dbFilteredData.slice(start, end);

    // const keys = Object.keys(pageData[0] || {});
    const baseKeys = EDITABLE_FIELDS[dbCurrentResource] || [];
    const pageKeys = new Set();

    pageData.forEach((r) => {
      Object.keys(r || {}).forEach((k) => pageKeys.add(k));
    });

    // colonnes: id/_id en premier, puis tes champs attendus, puis le reste
    const keys = [
      ...["id", "_id"].filter((k) => pageKeys.has(k)),
      ...baseKeys.filter((k) => pageKeys.has(k)),
      ...Array.from(pageKeys).filter((k) => !["id", "_id"].includes(k) && !baseKeys.includes(k)),
    ];

    const hidden = HIDDEN_FIELDS_BY_RESOURCE[dbCurrentResource] || new Set();
    const keysFiltered = keys.filter(k => !hidden.has(k));


    // HEADER
    const headerRow = document.createElement("tr");
    // keys.forEach((k) => {
    //   const th = document.createElement("th");
    //   th.textContent = k;
    //   th.className = "th";
    //   headerRow.appendChild(th);
    // });
    keysFiltered.forEach((k) => {
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

    // ROWS
    pageData.forEach((row) => {
      const tr = document.createElement("tr");
      tr.className = "tr";

      // keys.forEach((k) => {
      keysFiltered.forEach((k) => {
        const td = document.createElement("td");
        td.className = "td";

        // ✅ users.tickets => bouton Voir(...)
        if (dbCurrentResource === "users" && k === "tickets") {
          const tickets = safeJsonParse(row[k]);
          const list = Array.isArray(tickets) ? tickets : [];
          const count = list.length;

          const btn = document.createElement("button");
          btn.type = "button";
          btn.className = "tickets-open";
          btn.textContent = count ? `Voir (${count})` : "Voir";
          // btn.addEventListener("click", () => openTicketsModal(list, row.email || ""));
          btn.addEventListener("click", () => openTicketsModal(list, row.email || "", row.id || row._id || ""));
          td.appendChild(btn);
        }

        // ✅ tickets.userid => badge ok/del + mini id
        else if (
          dbCurrentResource === "tickets" &&
          (k === "userid" || k === "userId" || k === "user_id")
        ) {
          const uid = String(row[k] || "").trim();
          const isDel =
            uid &&
            typeof __deletedUserIds !== "undefined" &&
            __deletedUserIds.has(uid);

          const badge = document.createElement("span");
          badge.className = `user-badge ${isDel ? "del" : "ok"}`;

          // RNCP-friendly : visible uniquement si "all" ou "disabled"
          const showBadge = ticketsUserFilter !== "active";

          badge.innerHTML = showBadge
            ? `<span class="dot"></span><span class="label"></span>`
            : `<span class="dot"></span><span class="label"></span>`;

          const mono = document.createElement("span");
          mono.className = "mono";
          mono.style.marginLeft = "10px";
          mono.style.opacity = ".85";
          mono.textContent = uid ? uid.slice(0, 8) + "…" : "—";

          td.appendChild(badge);
          td.appendChild(mono);
        }

        // ✅ users.email => dot actif/désactivé
        else if (dbCurrentResource === "users" && k === "email") {
          const disabled = isUserDeleted(row);

          const wrapper = document.createElement("div");
          wrapper.className = "user-status-cell";

          const dot = document.createElement("span");
          dot.className = `status-dot ${disabled ? "red" : "green"}`;
          dot.title = disabled ? "Compte désactivé" : "Compte actif";

          const email = document.createElement("span");
          email.textContent = safeCellValue(k, row[k]);

          wrapper.append(dot, email);
          td.appendChild(wrapper);
        }

        // ✅ default cell
        else {
          td.textContent = safeCellValue(k, row[k]);
        }

        if (k === "id" || k === "_id") {
          td.classList.add("td-id");
          td.title = row[k] ? String(row[k]) : "";
        }

        tr.appendChild(td);
      });

      // ACTIONS
      if (!isReadOnly) {
        const td = document.createElement("td");
        td.className = "td td-actions";

        const id = row.id || row._id;

        const btnEdit = document.createElement("button");
        btnEdit.className = "btn-mini btn-blue";
        btnEdit.textContent = "Modifier";
        btnEdit.addEventListener("click", () => openDbModal("edit", row));

        // const btnDel = document.createElement("button");
        // btnDel.className = "btn-mini btn-red";
        // btnDel.textContent = "Supprimer";

        const btnDel = document.createElement("button");
        btnDel.type = "button";
        btnDel.className = "btn-mini btn-red";
        btnDel.textContent = "Supprimer";

        btnDel.addEventListener("click", async () => {
          const id = row?.id || row?._id;
          if (!id) {
            showToast("Ligne sans id (impossible à supprimer)", "error");
            return;
          }

          const label =
            dbCurrentResource === "users" ? "l’utilisateur" :
            dbCurrentResource === "tickets" ? "le ticket" :
            "cette ligne";

          const ok = await confirmDialog({
            title: `Supprimer ${label}`,
            message: `Supprimer ${label} (#${id}) ?`,
            okText: "Supprimer",
            cancelText: "Annuler",
            danger: true,
          });
          if (!ok) return;

          const base = DB_ENDPOINTS[dbCurrentResource];
          if (!base) {
            showToast("Endpoint non configuré", "error");
            return;
          }

          const url = `${base}/${encodeURIComponent(String(id))}`;

          try {
            const res = await apiFetch(url, { method: "DELETE" });
            const body = await res.text().catch(() => "");

            if (!res.ok) {
              console.error("DELETE error:", dbCurrentResource, res.status, body);
              showToast(`Erreur suppression (${res.status})`, "error");
              return;
            }

            showToast("Suppression ✅", "success");

            notifyAction({
              variant: "ok",
              title: "Suppression",
              status: res.status,
              message:
                dbCurrentResource === "tickets"
                  ? `🗑️ Ticket #${id} supprimé`
                  : dbCurrentResource === "users"
                  ? `🗑️ User #${id} supprimé`
                  : `🗑️ Suppression effectuée (#${id})`,
              ms: 7000,
            });


            // si suppression user -> refresh index users
            if (dbCurrentResource === "users") {
              __usersIndexLoaded = false;
              await ensureUsersIndex().catch(() => {});
            }

            await loadDbData();
          } catch (e) {
            console.error(e);
            showToast("Erreur réseau", "error");
          }
        });





        td.append(btnEdit, btnDel);
        tr.appendChild(td);
      }

      dbTableBody.appendChild(tr);
    });

    if (dbPagingInfo) {
      dbPagingInfo.textContent = `Page ${dbCurrentPage + 1} / ${totalPages}`;
    }
  }


  async function loadDbData() {
    if (!dbResourceSelect) return;
    dbCurrentResource = dbResourceSelect.value;

    // ✅ toggles UI (montrer/cacher selon la ressource)
    ensureUsersFilterUI();     // <-- AJOUT
    ensureDeletedToggleUI();   // ticketsUserFilter (actifs/tous/désactivés)

    // ✅ index users nécessaire pour filtrer les tickets "désactivés"
    if (dbCurrentResource === "tickets") {
      await ensureUsersIndex();
    }

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
    const hidden = HIDDEN_FIELDS_BY_RESOURCE[dbCurrentResource] || new Set();
    const safeAllKeys = allKeys.filter(k => !hidden.has(k));
    // const allowed = EDITABLE_FIELDS[dbCurrentResource] || allKeys;
    let allowed = EDITABLE_FIELDS[dbCurrentResource] || allKeys;

    // ✅ Exception: en création de user, on ajoute password
    if (dbCurrentResource === "users" && mode === "create") {
      if (!allowed.includes("password")) allowed = [...allowed, "password"];
    }


    const displayKeys = [
      ...safeAllKeys.filter((k) => k === "id" || k === "_id"),
      ...allowed.filter((k) => !["id", "_id"].includes(k)),
    ].filter((v, i, a) => a.indexOf(v) === i)
    .filter(k => !hidden.has(k));

    displayKeys.forEach((k) => {
      const label = document.createElement("label");
      label.className = "modal-label";
      label.textContent = k + (isSensitiveKey(k) ? " (masqué)" : "");

      let input;
      if (k === "admin") {
        input = document.createElement("select");
        input.className = "modal-input";
        input.name = k;

        // =============================
        // ✅ VALIDATIONS (Admin modal)
        // =============================
        if (dbCurrentResource === "users") {
          if (k === "email") {
            input.type = "email";
            input.required = true;
            // simple + efficace
            input.pattern = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$";
            input.title = "Email invalide";
          }

          if (k === "password") {
            input.type = "password";
            input.required = true;
            // Exemple: 8+ avec 1 maj, 1 min, 1 chiffre (à adapter)
            input.pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
            input.title = "8+ caractères, 1 majuscule, 1 minuscule, 1 chiffre";
          }
        }

        if (dbCurrentResource === "tickets") {
          // 5 numéros de 1 à 49 au format "1-2-3-4-5" (ou "01-02-...")
          if (k === "numbers") {
            input.type = "text";
            input.required = true;
            input.pattern =
              "^(0?[1-9]|[1-3][0-9]|4[0-9])-(0?[1-9]|[1-3][0-9]|4[0-9])-(0?[1-9]|[1-3][0-9]|4[0-9])-(0?[1-9]|[1-3][0-9]|4[0-9])-(0?[1-9]|[1-3][0-9]|4[0-9])$";
            input.title = "Format: 5 numéros 1-49 séparés par des tirets (ex: 7-12-23-34-45)";
          }

          // chance 1..10
          if (k === "chanceNumber") {
            input.type = "text";
            input.required = true;
            input.pattern = "^([1-9]|10)$";
            input.title = "Chance entre 1 et 10";
          }

          // date
          if (k === "drawDate") {
            input.type = "date";
            input.required = true;
          }
        }


        input.innerHTML = `<option value="true">true</option><option value="false">false</option>`;
        input.value = row ? String(!!row[k]) : "false";
      } else if (k === "role") {
        input = document.createElement("select");
        input.className = "modal-input";
        input.name = k;
        input.innerHTML = `<option value="ROLE_USER">ROLE_USER</option><option value="ROLE_ADMIN">ROLE_ADMIN</option>`;
        input.value = row?.[k] ? String(row[k]) : "ROLE_USER";

        // ✅ role en lecture seule pour users
        if (dbCurrentResource === "users") {
          input.disabled = true;
          input.style.opacity = "0.6";
          input.title = "Modification du rôle désactivée";
        }
      // } else {
      //   input = document.createElement("input");
      //   input.className = "modal-input";
      //   input.name = k;
      //   const v = row ? row[k] ?? "" : "";
      //   input.value = typeof v === "object" ? JSON.stringify(v) : String(v ?? "");
      // }
      } else {
        input = document.createElement("input");
        input.className = "modal-input";
        input.name = k;

        // ✅ password field (create user)
        if (dbCurrentResource === "users" && mode === "create" && k === "password") {
          input.type = "password";
          input.placeholder = "Mot de passe";
          input.value = "";
        } else {
          input.type = "text";
          const v = row ? row[k] ?? "" : "";
          input.value = typeof v === "object" ? JSON.stringify(v) : String(v ?? "");
        }
      }


      // // ✅ tickets.userId => readonly (on ne change pas le propriétaire d'un ticket)
      // if (
      //   dbCurrentResource === "tickets" &&
      //   ["userid", "userId", "user_id"].includes(k)
      // ) {
      //   input.disabled = true;
      //   input.style.opacity = "0.6";
      //   input.title = "Lecture seule (lié à l'utilisateur)";
      // }

      // // ✅ tickets.userId : readonly en EDIT, mais editable en CREATE
      // if (
      //   dbCurrentResource === "tickets" &&
      //   ["userid", "userId", "user_id"].includes(k)
      // ) {
      //   if (mode === "edit") {
      //     input.disabled = true;
      //     input.style.opacity = "0.6";
      //     input.title = "Lecture seule (propriétaire du ticket)";
      //   } else {
      //     // CREATE: obligatoire
      //     input.disabled = false;
      //     input.placeholder = "UUID utilisateur (obligatoire)";
      //     input.title = "UUID utilisateur requis pour créer un ticket";
      //   }
      // }
      // ✅ tickets.userId :
      // - EDIT => readonly
      // - CREATE depuis "Voir tickets" => readonly (pré-rempli)
      // - CREATE depuis CRUD tickets => éditable (obligatoire)
      if (
        dbCurrentResource === "tickets" &&
        ["userid", "userId", "user_id"].includes(k)
      ) {
        const fromUserTicketsModal = (mode === "create" && __stayInEditAfterSave);

        if (mode === "edit" || fromUserTicketsModal) {
          input.disabled = true;
          input.style.opacity = "0.6";
          input.title = "Lecture seule (lié à l'utilisateur)";
        } else {
          input.disabled = false;
          input.placeholder = "UUID utilisateur (obligatoire)";
          input.title = "UUID utilisateur requis pour créer un ticket";
        }
      }




      // if (
      //   PROTECTED_FIELDS.has(k) ||
      //   isSensitiveKey(k) ||
      //   (EDITABLE_FIELDS[dbCurrentResource] &&
      //     !allowed.includes(k) &&
      //     k !== "id" &&
      //     k !== "_id")
      // ) {
      //   input.disabled = true;
      //   input.style.opacity = "0.6";
      //   if (isSensitiveKey(k)) input.value = maskValue(input.value);
      // }

      const isCreateUserPassword =
        dbCurrentResource === "users" && mode === "create" && k === "password";

      if (
        !isCreateUserPassword && (
          PROTECTED_FIELDS.has(k) ||
          isSensitiveKey(k) ||
          (EDITABLE_FIELDS[dbCurrentResource] &&
            !allowed.includes(k) &&
            k !== "id" &&
            k !== "_id")
        )
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

    // ✅ reset
    __stayInEditAfterSave = false;
  }


  // ===============================
  // ✅ Modal SAVE status (5s) sous le bouton "Enregistrer"
  // ===============================
  let __modalStatusTimerId = null;

  function ensureDbModalStatusUI() {
    const saveBtn = document.getElementById("dbModalSave");
    if (!saveBtn) return;

    // déjà créé
    if (document.getElementById("dbModalSaveStatusWrap")) return;

    // container sous le bouton Save
    const wrap = document.createElement("div");
    wrap.id = "dbModalSaveStatusWrap";
    wrap.style.marginTop = "10px";
    wrap.style.display = "none";

    wrap.innerHTML = `
      <div id="dbModalSaveStatusText" style="font-size:13px;font-weight:700;"></div>
      <div id="dbModalSaveStatusBar" style="
        margin-top:6px;
        height:4px;
        border-radius:999px;
        overflow:hidden;
        background: rgba(255,255,255,.12);
      ">
        <div id="dbModalSaveStatusBarInner" style="
          height:100%;
          width:100%;
          transform-origin:left;
        "></div>
      </div>
    `;

    // inject juste après le bouton
    saveBtn.parentElement?.appendChild(wrap);

    // inject CSS keyframes une seule fois
    if (!document.getElementById("dbModalSaveStatusStyle")) {
      const style = document.createElement("style");
      style.id = "dbModalSaveStatusStyle";
      style.textContent = `
        @keyframes modal-status-countdown {
          from { transform: scaleX(1); }
          to   { transform: scaleX(0); }
        }
      `;
      document.head.appendChild(style);
    }
  }

  function setDbModalSaveStatus(msg, type = "success", ms = 5000) {
    ensureDbModalStatusUI();

    const wrap = document.getElementById("dbModalSaveStatusWrap");
    const text = document.getElementById("dbModalSaveStatusText");
    const barInner = document.getElementById("dbModalSaveStatusBarInner");

    if (!wrap || !text || !barInner) return;

    // stop précédent
    if (__modalStatusTimerId) {
      clearTimeout(__modalStatusTimerId);
      __modalStatusTimerId = null;
    }

    wrap.style.display = "block";
    text.textContent = msg || "";

    // couleurs simples (sans toucher à ton CSS global)
    const color =
      type === "error" ? "#f97373" :
      type === "warn"  ? "#fbbf24" :
                        "#34d399";

    text.style.color = color;
    barInner.style.background = color;

    // reset anim
    barInner.style.animation = "none";
    barInner.offsetHeight; // force reflow
    barInner.style.animation = `modal-status-countdown ${ms / 1000}s linear forwards`;

    __modalStatusTimerId = setTimeout(() => {
      wrap.style.display = "none";
      __modalStatusTimerId = null;
    }, ms);
  }


  async function saveDbModal() {
    if (!dbEditingRow || !dbModalForm) return;

    const sp = getStatusPanel();
    const obj = {};
    const inputs = dbModalForm.querySelectorAll("input, select, textarea");
    inputs.forEach((el) => {
      if (!el.name) return;
      if (el.disabled) return;

    // ✅ si création ticket depuis modal Tickets, forcer userId même si champ disabled
    if (dbCurrentResource === "tickets" && dbEditingRow?.mode === "create" && __stayInEditAfterSave) {
      const ctx = window.__ticketsModalCtx || {};
      if (ctx.userId) obj.userId = ctx.userId;
    }


      const v = el.value;
      if (el.name === "admin") {
        obj.admin = v === "true";
        return;
      }
      obj[el.name] = v === "" ? null : v;
    });

    // // ✅ si création ticket depuis modal Tickets, forcer userId dans le payload
    // if (dbCurrentResource === "tickets" && dbEditingRow?.mode === "create") {
    //   const ctx = window.__ticketsModalCtx || {};
    //   if (ctx.userId && !obj.userId) obj.userId = ctx.userId;
    // }

    // ✅ Validation: création ticket => userId obligatoire
    if (dbCurrentResource === "tickets" && dbEditingRow?.mode === "create") {
      const uid = String(obj.userId || obj.userid || obj.user_id || "").trim();

      // normalise en userId (si jamais)
      if (!obj.userId && uid) obj.userId = uid;

      if (!uid) {
        showToast("❌ userId obligatoire pour créer un ticket", "error");
        setDbModalSaveStatus("❌ userId obligatoire", "error", 5000);
        return;
      }

      // optionnel: vérif UUID format simple
      const uuidRe = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
      if (!uuidRe.test(uid)) {
        showToast("❌ userId invalide (UUID attendu)", "error");
        setDbModalSaveStatus("❌ UUID invalide", "error", 5000);
        return;
      }
    }



    const base = DB_ENDPOINTS[dbCurrentResource];
    let url = base;
    let method = "POST";

    if (dbEditingRow.mode === "edit") {
      const id = dbEditingRow.row.id || dbEditingRow.row._id;
      // url = `${base}/${id}`;
      url = `${base}/${encodeURIComponent(String(id))}`;
      method = "PUT";
    }

    // UI: disable bouton save pendant la requête
    if (dbModalSave) {
      dbModalSave.disabled = true;
      dbModalSave.style.opacity = "0.65";
      dbModalSave.style.cursor = "not-allowed";
    }

    try {
      const res = await apiFetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(obj),
      });

      const bodyText = await res.text().catch(() => "");
      // ✅ essayer de récupérer l'objet renvoyé (souvent contient l'id)
      let bodyJson = null;
      try {
        bodyJson = bodyText ? JSON.parse(bodyText) : null;
      } catch {
        bodyJson = null;
      }

      // ✅ id renvoyé par l'API lors d'un CREATE (POST)
      const createdId = bodyJson?.id || bodyJson?._id || bodyJson?.uuid || null;


      if (!res.ok) {
        // const msg = data?.message || bodyText || `Erreur ${res.status}`;
        const msg = bodyText || `Erreur ${res.status}`;
        sp.show(`❌ ${msg}`, { variant: "err", ms: 9000, status: res.status, details: bodyText, title: "Sauvegarde" });
        showToast(`Erreur ${res.status}`, "error");
        console.error("SAVE error:", res.status, bodyText);
        setDbModalSaveStatus("❌ Erreur modification", "error", 5000);
        return; // ✅ IMPORTANT

        // ✅ message sous le bouton (5s)
        // sp.show("✅ Modif enregistrée", { variant: "ok", ms: 5000, status: res.status, title: "Sauvegarde" });
        // setDbModalSaveStatus("❌ Erreur modification", "error", 5000);
        // return;
      }

    // // ✅ succès
    // showToast("Modif avec succès ✅", "success");

    // // refresh table principale
    // await loadDbData();

    // // 🔥 CAS SPÉCIAL : on vient du modal Tickets
    // const ctx = window.__ticketsModalCtx;

    // if (dbCurrentResource === "tickets" && ctx?.email) {
    //   try {
    //     await refreshTicketsModalForEmail(ctx.email);
    //   } catch (e) {
    //     console.warn("refresh tickets modal failed", e);
    //   }

    //   // ferme le modal edit IMMÉDIATEMENT
    //   closeDbModal();

    //   // reset flag
    //   __stayInEditAfterSave = false;

    //   return;
    // }

    // // sinon comportement normal
    // setDbModalSaveStatus("✅ Modif avec succès", "success", 5000);
    // closeDbModal();


    // // ✅ succès
    // showToast("Modif avec succès ✅", "success");

    // // id concerné (edit) ou renvoyé par l’API si tu le gères
    // const editedId = dbEditingRow?.mode === "edit"
    //   ? (dbEditingRow?.row?.id || dbEditingRow?.row?._id)
    //   : null;

    // notifyAction({
    //   variant: "ok",
    //   title: "Sauvegarde",
    //   status: res.status,
    //   message:
    //     dbCurrentResource === "tickets"
    //       ? `✅ Ticket #${editedId || "?"} modifié avec succès`
    //       : dbCurrentResource === "users"
    //       ? `✅ User #${editedId || "?"} modifié avec succès`
    //       : `✅ Modification enregistrée`,
    //   details: "", // ou bodyText si tu veux
    //   ms: 7000,
    // });

    const isCreate = dbEditingRow?.mode === "create";

    const editedId = !isCreate
      ? (dbEditingRow?.row?.id || dbEditingRow?.row?._id)
      : null;

    const idToShow = createdId || editedId || "?";

    // ✅ Toast cohérent
    showToast(isCreate ? "Ajout avec succès ✅" : "Modif avec succès ✅", "success");

    // ✅ Notification cohérente + UUID
    notifyAction({
      variant: "ok",
      title: isCreate ? "Ajout" : "Sauvegarde",
      status: res.status,
      message:
        dbCurrentResource === "tickets"
          ? (isCreate
              ? `✅ Ticket #${idToShow} ajouté avec succès`
              : `✅ Ticket #${idToShow} modifié avec succès`)
          : dbCurrentResource === "users"
          ? (isCreate
              ? `✅ User #${idToShow} ajouté avec succès`
              : `✅ User #${idToShow} modifié avec succès`)
          : (isCreate ? `✅ Ajout effectué (#${idToShow})` : `✅ Modification enregistrée (#${idToShow})`),
      details: "",
      ms: 7000,
    });

    // ✅ Status sous le bouton (si tu veux différencier)
    setDbModalSaveStatus(isCreate ? "✅ Ajout avec succès" : "✅ Modif avec succès", "success", 5000);


    // // 🔥 on vient du modal tickets ?
    // const ctx = window.__ticketsModalCtx;

    // if (dbCurrentResource === "tickets" && ctx?.email) {

    //   // 1️⃣ recharge les tickets du user
    //   try {
    //     await refreshTicketsModalForEmail(ctx.email);
    //   } catch (e) {
    //     console.warn("refresh tickets modal failed", e);
    //   }

    //   // 2️⃣ fermer le modal edit
    //   closeDbModal();

    //   // 3️⃣ remettre le contexte sur USERS
    //   const sel = document.getElementById("dbResourceSelect");
    //   if (sel) sel.value = "users";
    //   dbCurrentResource = "users";

    //   // 4️⃣ reload table users
    //   await loadDbData();

    //   // reset contexte
    //   window.__ticketsModalCtx = null;
    //   __stayInEditAfterSave = false;

    //   return;
    // }
    const ctx = window.__ticketsModalCtx;

    // ✅ Seulement si on vient du modal Tickets (pas depuis DB tickets)
    if (dbCurrentResource === "tickets" && __stayInEditAfterSave && ctx?.email) {

      // 1) refresh la liste tickets dans le modal user
      try {
        await refreshTicketsModalForEmail(ctx.email);
      } catch (e) {
        console.warn("refresh tickets modal failed", e);
      }

      // 2) fermer le modal edit
      closeDbModal();

      // ✅ IMPORTANT : ici tu peux choisir quoi faire
      // Si tu veux revenir sur USERS uniquement dans ce cas spécial :
      const sel = document.getElementById("dbResourceSelect");
      if (sel) sel.value = "users";
      dbCurrentResource = "users";
      await loadDbData();

      // reset
      // window.__ticketsModalCtx = null;
      __stayInEditAfterSave = false;
      return;
    }


    // sinon comportement normal
    await loadDbData();
    setDbModalSaveStatus("✅ Modif avec succès", "success", 5000);
    closeDbModal();


    } catch (e) {
      console.error(e);
      showToast("Erreur réseau", "error");
      setDbModalSaveStatus("❌ Erreur réseau", "error", 5000);
    } finally {
      if (dbModalSave) {
        dbModalSave.disabled = false;
        dbModalSave.style.opacity = "1";
        dbModalSave.style.cursor = "pointer";
      }
    }
  }


  // // ✅ si on vient d'éditer un ticket depuis le modal Tickets, on refresh le modal Tickets
  // try {
  //   const ctx = window.__ticketsModalCtx;
  //   if (dbCurrentResource === "tickets" && ctx?.email) {
  //     refreshTicketsModalForEmail(ctx.email).catch((e) => {
  //       console.warn("refresh tickets modal after save failed", e);
  //     });
  //   }
  // } catch (e) {
  //   console.warn("refresh tickets modal after save failed", e);
  // }


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
    // const pageSize = parseInt(dbPageSize?.value || "20", 10);
    const pageSize = parseInt(dbPageSize?.value || "8", 10);
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
  const ticketsAdd = document.getElementById("ticketsAdd");
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

  // function openTicketsModal(tickets, userEmail) {
  function openTicketsModal(tickets, userEmail, userId) {

    if (!ticketsOverlay || !ticketsBody || !ticketsTitle) return;

    // const list = Array.isArray(tickets) ? tickets : [];
    const list = Array.isArray(tickets) ? tickets.slice() : [];

    // ✅ tri : dernier tirage d'abord
    // 1) drawDate desc (YYYY-MM-DD)
    // 2) updatedAt desc
    // 3) createdAt desc
    list.sort((a, b) => {
      const da = String(a.drawDate || "");
      const db = String(b.drawDate || "");
      if (da !== db) return db.localeCompare(da);

      const ua = new Date(a.updatedAt || 0).getTime() || 0;
      const ub = new Date(b.updatedAt || 0).getTime() || 0;
      if (ua !== ub) return ub - ua;

      const ca = new Date(a.createdAt || 0).getTime() || 0;
      const cb = new Date(b.createdAt || 0).getTime() || 0;
      return cb - ca;
    });

    // ✅ IMPORTANT : l’état "Copier" doit correspondre AU MODAL OUVERT
    lastTicketsForCopy = list;
    lastTicketsUserEmail = userEmail || "";

    // (optionnel mais utile si tu refresh ensuite)
    // window.__ticketsModalCtx = { email: lastTicketsUserEmail };
    // contexte: on veut refresh le modal tickets après save
    // window.__ticketsModalCtx = { email: userEmail };
    window.__ticketsModalCtx = { email: userEmail, userId: userId || null };

    // ✅ on veut rester dans le formulaire après save
    // __stayInEditAfterSave = true;

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

        // const tdDay = document.createElement("td");
        // tdDay.textContent = t.drawDay || "—";

        const tdCreated = document.createElement("td");
        tdCreated.textContent = fmtDateTime(t.createdAt);

        const tdUpdated = document.createElement("td");
        tdUpdated.textContent = fmtDateTime(t.updatedAt);

        // ✅ Actions (Modifier / Supprimer)
        const tdActions = document.createElement("td");
        const actions = document.createElement("div");
        actions.className = "tickets-actions";

        const btnEdit = document.createElement("button");
        btnEdit.type = "button";
        // btnEdit.className = "btn-mini";
        btnEdit.className = "btn-mini btn-blue";
        btnEdit.textContent = "Modifier";

        btnEdit.addEventListener("click", async () => {
          if (!t?.id) {
            showToast("Ticket sans id (impossible à modifier)", "error");
            return;
          }

          const sel = document.getElementById("dbResourceSelect");
          if (sel) sel.value = "tickets";
          dbCurrentResource = "tickets";

          closeTicketsModal();

          // window.__ticketsModalCtx = { email: userEmail };
          window.__ticketsModalCtx = { email: userEmail, userId: userId || null };
          __stayInEditAfterSave = true;

          try {
            // const res = await apiFetch(`/api/admin/tickets/${t.id}`);
            const res = await apiFetch(`${DB_ENDPOINTS.tickets}/${encodeURIComponent(String(t.id))}`);
            const full = await res.json();
            // openDbModal("edit", full);
            // const res = await apiFetch(`/api/admin/tickets/${t.id}`);
            // const full = await res.json();

            // const mapped = {
            //   id: full?.id ?? t.id,
            //   numbers: full?.numbers ?? t.numbers ?? null,
            //   chanceNumber: full?.chanceNumber ?? t.chanceNumber ?? null,
            //   drawDate: full?.drawDate ?? t.drawDate ?? null,
            //   drawDay: full?.drawDay ?? t.drawDay ?? null,

            //   // ⚠️ support entity->dto
            //   userId: full?.userId ?? full?.userid ?? full?.user?.id ?? null,
            // };
            const ctx = window.__ticketsModalCtx || {};
            const forcedUserId = userId || ctx.userId || null;

            const mapped = {
              id: full?.id ?? t.id,
              numbers: full?.numbers ?? t.numbers ?? null,
              chanceNumber: full?.chanceNumber ?? t.chanceNumber ?? null,
              drawDate: full?.drawDate ?? t.drawDate ?? null,
              // drawDay: full?.drawDay ?? t.drawDay ?? null,

              userId:
                full?.userId ??
                full?.userid ??
                full?.user_id ??
                full?.user?.id ??
                t?.userId ??
                t?.userid ??
                t?.user_id ??
                forcedUserId,
            };


            openDbModal("edit", mapped);

          } catch (e) {
            console.error(e);
            showToast("Impossible de charger le ticket complet", "error");
          }
        });


        const btnDel = document.createElement("button");
        btnDel.type = "button";
        // btnDel.className = "btn-mini btn-danger";
        btnDel.className  = "btn-mini btn-red";
        btnDel.textContent = "Supprimer";

        // btnDel.addEventListener("click", async () => {
        //   if (!t?.id) {
        //     showToast("Ticket sans id (impossible à supprimer)", "error");
        //     return;
        //   }

        // const ok = confirm("Supprimer ce ticket?  Suppriler cette user?");
        btnDel.addEventListener("click", async () => {
          const id = t?.id || t?._id;
          if (!id) {
            showToast("Ticket sans id (impossible à supprimer)", "error");
            return;
          }

          const ok = await confirmDialog({
            title: "Supprimer le ticket",
            message: `Supprimer ce ticket (#${id}) ?`,
            okText: "Supprimer",
            cancelText: "Annuler",
            danger: true,
          });
          if (!ok) return;

          const url = `${DB_ENDPOINTS.tickets}/${encodeURIComponent(String(id))}`;

          try {
            const res = await apiFetch(url, { method: "DELETE" });
            const body = await res.text().catch(() => "");

            if (!res.ok) {
              console.error("DELETE ticket error:", res.status, body);
              showToast(`Erreur suppression (${res.status})`, "error");
              return;
            }

            showToast("Ticket supprimé ✅", "success");

            notifyAction({
              variant: "ok",
              title: "Suppression",
              status: res.status,
              message: `🗑️ Ticket #${id} supprimé`,
              ms: 7000,
            });


            // refresh du modal tickets (si tu as la fonction)
            if (userEmail && typeof refreshTicketsModalForEmail === "function") {
              await refreshTicketsModalForEmail(userEmail);
            } else {
              // fallback: recharge la table principale si tu veux
              // await loadDbData();
            }
          } catch (e) {
            console.error(e);
            showToast("Erreur réseau", "error");
          }
        });



      //   if (!ok) return;

      //   try {
      //     const res = await apiFetch(`/api/admin/tickets/${t.id}`, { method: "DELETE" });
      //     if (!res.ok) {
      //       const body = await res.text().catch(() => "");
      //       console.error("DELETE ticket error:", res.status, body);
      //       showToast(`Erreur suppression (${res.status})`, "error");
      //       return;
      //     }

      //     showToast("Ticket supprimé ✅", "success");

      //     // refresh du modal tickets (tu as déjà cette fonction dans ton code)
      //     if (userEmail) {
      //       await refreshTicketsModalForEmail(userEmail);
      //     }
      //   } catch (e) {
      //     console.error(e);
      //     showToast("Erreur réseau", "error");
      //   }
      // });

      actions.append(btnEdit, btnDel);
      tdActions.appendChild(actions);

      // ✅ ajoute tdActions à la fin
      // tr.append(tdNumbers, tdChance, tdDate, tdDay, tdCreated, tdUpdated, tdActions);
      tr.append(tdNumbers, tdChance, tdDate, tdCreated, tdUpdated, tdActions);


      // tr.append(tdNumbers, tdChance, tdDate, tdDay, tdCreated, tdUpdated);
      ticketsBody.appendChild(tr);
    });
  }

    ticketsOverlay.style.display = "flex";
    ticketsOverlay.setAttribute("aria-hidden", "false");
}

  async function refreshTicketsModalForEmail(email) {
    const ctx = window.__ticketsModalCtx || {};
    let userId = ctx.userId || null;

    // fallback : retrouver l'id via l'index users si besoin
    if (!userId && email) {
      await ensureUsersIndex().catch(() => {});
      for (const [id, u] of __userById.entries()) {
        if (String(u.email || "").toLowerCase() === String(email).toLowerCase()) {
          userId = id;
          break;
        }
      }
    }

    if (!userId) {
      console.warn("[Tickets Modal] userId introuvable pour refresh");
      return;
    }

    const url = `${DB_ENDPOINTS.users}/${encodeURIComponent(String(userId))}`;
    const res = await apiFetch(url, { method: "GET" });
    const data = await res.json().catch(() => null);

    if (!res.ok || !data) {
      console.warn("[Tickets Modal] refresh failed", res.status, data);
      return;
    }

    const tickets = Array.isArray(data.tickets) ? data.tickets : [];
    openTicketsModal(tickets, email || data.email || "", userId);
  }


  function closeTicketsModal() {
    if (!ticketsOverlay) return;
    ticketsOverlay.style.display = "none";
    ticketsOverlay.setAttribute("aria-hidden", "true");
    if (ticketsBody) ticketsBody.innerHTML = "";

    // ✅ évite toute “fuite” de contexte
    lastTicketsForCopy = [];
    lastTicketsUserEmail = "";
    // window.__ticketsModalCtx = null;
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
      // const day = t.drawDay || "—";
      const created = fmtDateTime(t.createdAt);
      const updated = fmtDateTime(t.updatedAt);

      lines.push(
        `${String(idx + 1).padStart(2, "0")}. ${nums} | Chance:${chance} | ${date} (${day}) | C:${created} | U:${updated}`
      );
    });

    return lines.join("\n");
  }

  ticketsAdd?.addEventListener("click", () => {
    const ctx = window.__ticketsModalCtx || {};
    const userId = ctx.userId || null;
    const email = ctx.email || lastTicketsUserEmail || "";

    if (!userId) {
      showToast("UserId introuvable (impossible d’ajouter un ticket)", "error");
      return;
    }

    // on passe en mode tickets + on ouvre le CRUD create
    const sel = document.getElementById("dbResourceSelect");
    if (sel) sel.value = "tickets";
    dbCurrentResource = "tickets";

    // IMPORTANT: on veut revenir au modal Tickets après save
    __stayInEditAfterSave = true;

    // on ferme le modal tickets et on ouvre la création du ticket
    closeTicketsModal();

    // row "fake" juste pour que openDbModal ait des clés
    openDbModal("create", {
      numbers: "",
      chanceNumber: "",
      drawDate: "",
      // drawDay: "",
      userId: userId,
    });

    // optionnel: titre plus clair
    const title = document.getElementById("dbModalTitle");
    if (title) title.textContent = `Nouveau ticket • ${email}`;
  });


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
