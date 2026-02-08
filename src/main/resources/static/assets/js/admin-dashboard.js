/* admin-dashboard.js
 * Requiert layout-dashboard.js (window.API_BASE + window.apiFetch)
 * - Ping: /admin/ping
 * - Logs: /api/admin/logs?lines=...
 * - CRUD: /api/admin/**
 */

(() => {
  "use strict";

  // ----------------------------
  // BOOTSTRAP (API_BASE + apiFetch)
  // ----------------------------
  const API_BASE = window.API_BASE;
  const apiFetch = window.apiFetch;

  console.log("ADMIN DASHBOARD JS VERSION: 2026-02-08-LOGS-FIX");

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
    try {
      const res = await apiFetch("/api/protected/userinfo", { method: "GET" });
      if (!res.ok) return;

      const data = await res.json();
      const label = data.username || data.email || "Administrateur";

      const userEmail = document.getElementById("userEmail");
      const userChip = document.getElementById("userChip");
      if (userEmail) userEmail.textContent = label;
      if (userChip) userChip.style.display = "inline-flex";
    } catch (e) {
      console.warn("Impossible de charger l'utilisateur admin", e);
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
  return String(str)
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

      // timestamp au début
      const timeMatch = safe.match(/^(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})\s+/);
      let out = safe;

      if (timeMatch) {
        out = out.replace(
          timeMatch[1],
          `<span class="log-time">${timeMatch[1]}</span>`
        );
      }

      // badge niveau
      let badge = "";
      if (level) {
        badge = `<span class="log-badge ${level}">${level.toUpperCase()}</span>`;
      }

      return level
        ? `${badge}<span class="log-${level}">${out}</span>`
        : out;
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
      headers: { Accept: "text/plain" }, // ✅ évite les 406 / "No acceptable"
    });

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

    logsContainer.innerHTML = colorizeLogText(text || "(Aucun log pour le moment)");
    if (logsLastRefresh) {
      logsLastRefresh.textContent =
        "Dernier refresh : " + new Date().toLocaleTimeString("fr-FR");
    }

    // ✅ auto-scroll (NE PAS écraser innerHTML)
    if (autoScrollLogsCheckbox?.checked) {
      logsContainer.scrollTop = logsContainer.scrollHeight;
    }
  } catch (e) {
    console.error(e);
    logsContainer.innerHTML = colorizeLogText("[ERREUR] Impossible de charger les logs.");
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
  // INFO (Ping + API base)
  // ----------------------------
  const apiBaseEl = document.getElementById("apiBase");
  if (apiBaseEl) apiBaseEl.textContent = API_BASE;

  const btnPing = document.getElementById("btnPing");
  const pingResult = document.getElementById("pingResult");

  btnPing?.addEventListener("click", async () => {
    if (pingResult) pingResult.textContent = "…";

    try {
      const res = await apiFetch("/admin/ping", { method: "GET" });

      let txt = "";
      try {
        const data = await res.json();
        txt = data?.status ? `{"status":"${data.status}"}` : JSON.stringify(data);
      } catch {
        txt = await res.text();
      }

      if (pingResult) pingResult.textContent = `${res.status} - ${txt}`;
    } catch (e) {
      console.error(e);
      if (pingResult) pingResult.textContent = "Erreur réseau";
    }
  });

  // ----------------------------
  // UI helpers (Toast + Confirm)
  // ----------------------------
  const toastEl = document.getElementById("toast");
  let toastTimer = null;

  function showToast(message, type = "info") {
    if (!toastEl) return;
    toastEl.textContent = message;
    toastEl.dataset.type = type;
    toastEl.classList.add("show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toastEl.classList.remove("show"), 2800);
  }

  const confirmOverlay = document.getElementById("confirmOverlay");
  const confirmTitle = document.getElementById("confirmTitle");
  const confirmText = document.getElementById("confirmText");
  const confirmCancel = document.getElementById("confirmCancel");
  const confirmOk = document.getElementById("confirmOk");

  function confirmDialog({
    title = "Confirmation",
    message = "Confirmer ?",
    okText = "Confirmer",
    danger = false,
  } = {}) {
    return new Promise((resolve) => {
      if (!confirmOverlay) return resolve(false);

      confirmTitle.textContent = title;
      confirmText.textContent = message;
      confirmOk.textContent = okText;
      confirmOk.classList.toggle("danger", !!danger);

      confirmOverlay.style.display = "flex";
      confirmOverlay.setAttribute("aria-hidden", "false");

      const cleanup = () => {
        confirmOverlay.style.display = "none";
        confirmOverlay.setAttribute("aria-hidden", "true");
        confirmCancel.removeEventListener("click", onCancel);
        confirmOk.removeEventListener("click", onOk);
        confirmOverlay.removeEventListener("click", onBg);
        document.removeEventListener("keydown", onKey);
      };
      const onCancel = () => {
        cleanup();
        resolve(false);
      };
      const onOk = () => {
        cleanup();
        resolve(true);
      };
      const onBg = (e) => {
        if (e.target === confirmOverlay) onCancel();
      };
      const onKey = (e) => {
        if (e.key === "Escape") onCancel();
      };

      confirmCancel.addEventListener("click", onCancel);
      confirmOk.addEventListener("click", onOk);
      confirmOverlay.addEventListener("click", onBg);
      document.addEventListener("keydown", onKey);
    });
  }

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
      : dbRawData.filter((r) =>
          JSON.stringify(r).toLowerCase().includes(q)
        );
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
        td.textContent = safeCellValue(k, row[k]);
        td.className = "td";

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

    if (dbPagingInfo)
      dbPagingInfo.textContent = `Page ${dbCurrentPage + 1} / ${totalPages}`;
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
        dbTableBody.innerHTML = `<tr><td style="padding:10px;color:#f97373;">${String(
          e
        )}</td></tr>`;
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

  // DB events
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
        <div class="stat-name">${(u.firstName || "")} ${(u.lastName || "")}</div>
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
      : statsRaw.filter((u) =>
          (u.email || "").toLowerCase().includes(q)
        );
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

  function escapeHtml(str) {
  return str
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function classifyLine(line) {
  // simple heuristique: on détecte INFO/WARN/ERROR/DEBUG/TRACE
  if (line.includes(" ERROR ")) return "error";
  if (line.includes(" WARN "))  return "warn";
  if (line.includes(" INFO "))  return "info";
  if (line.includes(" DEBUG ")) return "debug";
  if (line.includes(" TRACE ")) return "trace";
  return "";
}


function colorizeLogText(raw) {
  const lines = raw.split("\n");

  return lines.map((line) => {
    const safe = escapeHtml(line);
    const level = classifyLine(line);

    // option: extraire timestamp (début de ligne "YYYY-MM-DD HH:mm:ss")
    const timeMatch = safe.match(/^(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})\s+/);
    let out = safe;

    if (timeMatch) {
      out = out.replace(timeMatch[1], `<span class="log-time">${timeMatch[1]}</span>`);
    }

    // badge niveau
    let badge = "";
    if (level) {
      badge = `<span class="log-badge ${level}">${level.toUpperCase()}</span>`;
    }

    // couleur du reste
    if (level) {
      return `${badge}<span class="log-${level}">${out}</span>`;
    }
    return out;
  }).join("\n");
}

  // ----------------------------
  // INIT
  // ----------------------------
  showSection("swagger");
  loadAdminUser();
})();
