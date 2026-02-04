// ✅ API_BASE : dashboard (5500) parle au backend (8082) en local.
// En prod, tu pourras remplacer par ton domaine (ex: https://stephanedinahet.fr)
const API_BASE =
  (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
    ? "http://localhost:8082"
    : window.location.origin;

// ✅ JWT en cookie => pas d'Authorization header
function getAuthHeaders() { return {}; }

// ---------- NAV ----------
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
  stats: "Vue par joueur : tickets, gains, etc."
};

function showSection(key) {
  Object.keys(sections).forEach(k => sections[k].classList.toggle("show", k === key));
  navItems.forEach(btn => btn.classList.toggle("active", btn.dataset.section === key));

  pageTitle.textContent = "Tableau de bord administrateur";
  pageSubtitle.textContent = subtitles[key] || "";

  if (key === "logs") refreshLogs();
}

navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.dataset.section)));

// ---------- SWAGGER ----------
// const swaggerFrame = document.getElementById("swaggerFrame");
// const btnOpenSwagger = document.getElementById("btnOpenSwagger");

// ⚠️ L’iframe peut être bloquée si Swagger renvoie X-Frame-Options SAMEORIGIN.
// On met quand même l’URL ; si ça bloque, tu utilises le bouton "nouvel onglet".
// swaggerFrame.src = `${API_BASE}/swagger-ui/index.html`;

// btnOpenSwagger.addEventListener("click", () => {
//   window.open(`${API_BASE}/swagger-ui/index.html`, "_blank");
// });

// ---------- SWAGGER (sans iframe) ----------
const btnOpenSwagger = document.getElementById("btnOpenSwagger");
const SWAGGER_URL = `${API_BASE}/swagger-ui/index.html`;

if (btnOpenSwagger) {
  btnOpenSwagger.addEventListener("click", (e) => {
    e.preventDefault();

    // ✅ méthode fiable : clique programmatique sur un <a>
    const a = document.createElement("a");
    a.href = SWAGGER_URL;
    a.target = "_blank";
    a.rel = "noopener noreferrer";
    document.body.appendChild(a);
    a.click();
    a.remove();
  });
}


// ---------- LOGS ----------
const logsContainer = document.getElementById("logsContainer");
const btnRefreshLogs = document.getElementById("btnRefreshLogs");
const logLineCountSelect = document.getElementById("logLineCount");
const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
const logsLastRefresh = document.getElementById("logsLastRefresh");

async function refreshLogs() {
  const lines = logLineCountSelect.value;

  try {
    const res = await fetch(`${API_BASE}/admin/logs?lines=${encodeURIComponent(lines)}`, {
      method: "GET",
      headers: { ...getAuthHeaders() },
      credentials: "include"
    });

    const text = await res.text();
    logsContainer.textContent = text || "(Aucun log pour le moment)";
    logsLastRefresh.textContent = "Dernier refresh : " + new Date().toLocaleTimeString("fr-FR");

    if (autoScrollLogsCheckbox.checked) logsContainer.scrollTop = logsContainer.scrollHeight;
  } catch (e) {
    console.error(e);
    logsContainer.textContent = "[ERREUR] Impossible de charger les logs.";
  }
}

btnRefreshLogs.addEventListener("click", refreshLogs);
logLineCountSelect.addEventListener("change", refreshLogs);

setInterval(() => {
  if (autoRefreshLogsCheckbox.checked) refreshLogs();
}, 3000);

// ---------- INFO ----------
document.getElementById("apiBase").textContent = API_BASE;

const btnPing = document.getElementById("btnPing");
const pingResult = document.getElementById("pingResult");

btnPing.addEventListener("click", async () => {
  pingResult.textContent = "…";
  try {
    const res = await fetch(`${API_BASE}/admin/ping`, { credentials: "include" });
    const t = await res.text();
    pingResult.textContent = `${res.status} - ${t}`;
  } catch (e) {
    console.error(e);
    pingResult.textContent = "Erreur réseau";
  }
});

// ---------- LOGOUT ----------
document.getElementById("btnLogout").addEventListener("click", async () => {
  try {
    await fetch(`${API_BASE}/api/auth/logout`, {
      method: "POST",
      credentials: "include"
    });
  } catch (e) {
    console.error(e);
  }
  // page login en 5500
  window.location.href = `/admin-login.html`;
});

// =========================
// DB CRUD
// =========================
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
  users: `${API_BASE}/api/admin/users`,
  tickets: `${API_BASE}/api/admin/tickets`,
  ticket_gains: `${API_BASE}/api/admin/ticket-gains`,
  refresh_tokens: `${API_BASE}/api/admin/refresh-tokens`
};

const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);
const SENSITIVE_PARTS = ["password","hashed","hash","token","refresh","access","secret","apikey","api_key","session","cookie","reset"];

function isSensitiveKey(key) {
  const k = String(key || "").toLowerCase();
  return SENSITIVE_PARTS.some(p => k.includes(p));
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

let dbCurrentResource = "users";
let dbRawData = [];
let dbFilteredData = [];
let dbCurrentPage = 0;
let dbEditingRow = null;

function applyDbFilter() {
  const q = (dbSearch?.value || "").toLowerCase().trim();
  dbFilteredData = !q ? dbRawData.slice() : dbRawData.filter(r => JSON.stringify(r).toLowerCase().includes(q));
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
    dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
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
  keys.forEach(k => {
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

  pageData.forEach(row => {
    const tr = document.createElement("tr");
    tr.className = "tr";

    keys.forEach(k => {
      const td = document.createElement("td");
      td.textContent = safeCellValue(k, row[k]);
      td.className = "td";
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
        if (!id) return alert("ID introuvable.");
        if (!confirm(`Supprimer ${dbCurrentResource} #${id} ?`)) return;
        const url = `${DB_ENDPOINTS[dbCurrentResource]}/${id}`;
        const res = await fetch(url, { method: "DELETE", credentials: "include" });
        if (!res.ok) return alert(`Erreur ${res.status} suppression`);
        dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
        applyDbFilter();
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

  setDbStatus("Chargement...");
  dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";

  const res = await fetch(url, { method: "GET", credentials: "include" });
  if (!res.ok) return setDbStatus(`Erreur ${res.status}`, true);

  const data = await res.json();
  dbRawData = Array.isArray(data) ? data : [];
  setDbStatus(`${dbRawData.length} lignes`);
  applyDbFilter();
}

function openDbModal(mode, row) {
  if (READ_ONLY_RESOURCES.has(dbCurrentResource)) return alert("Lecture seule.");
  dbEditingRow = { mode, row: row || null };
  dbModalForm.innerHTML = "";

  const sample = row || (dbRawData[0] || {});
  const keys = Object.keys(sample);

  const protectedFields = ["id","_id","created_at","updated_at","createdAt","updatedAt"];

  keys.forEach(k => {
    const label = document.createElement("label");
    label.className = "modal-label";
    label.textContent = k + (isSensitiveKey(k) ? " (masqué)" : "");

    const input = document.createElement("input");
    input.className = "modal-input";
    input.name = k;
    input.value = row ? (row[k] ?? "") : "";

    if (protectedFields.includes(k) || isSensitiveKey(k)) {
      input.disabled = true;
      input.style.opacity = "0.6";
      if (isSensitiveKey(k)) input.value = maskValue(input.value);
    }

    const wrap = document.createElement("div");
    wrap.className = "modal-field";
    wrap.append(label, input);
    dbModalForm.appendChild(wrap);
  });

  dbModalTitle.textContent = (mode === "edit")
    ? `Modifier #${row.id || row._id}`
    : "Nouvelle ligne";

  dbModalOverlay.style.display = "flex";
  dbModalOverlay.setAttribute("aria-hidden", "false");
}

function closeDbModal() {
  dbModalOverlay.style.display = "none";
  dbModalOverlay.setAttribute("aria-hidden", "true");
  dbEditingRow = null;
}

async function saveDbModal() {
  if (!dbEditingRow) return;

  const formData = new FormData(dbModalForm);
  const obj = {};
  formData.forEach((v, k) => {
    const input = dbModalForm.querySelector(`input[name="${k}"]`);
    if (input && input.disabled) return;
    obj[k] = v === "" ? null : v;
  });

  const base = DB_ENDPOINTS[dbCurrentResource];
  let url = base;
  let method = "POST";

  if (dbEditingRow.mode === "edit") {
    const id = dbEditingRow.row.id || dbEditingRow.row._id;
    url = `${base}/${id}`;
    method = "PUT";
  }

  const res = await fetch(url, {
    method,
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(obj)
  });

  if (!res.ok) {
    const t = await res.text().catch(() => "");
    return alert(`Erreur ${res.status}\n${t}`);
  }

  closeDbModal();
  await loadDbData();
}

btnLoadData?.addEventListener("click", loadDbData);
dbSearch?.addEventListener("input", applyDbFilter);
dbPageSize?.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
dbPrevPage?.addEventListener("click", () => { if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); }});
dbNextPage?.addEventListener("click", () => {
  const pageSize = parseInt(dbPageSize?.value || "20", 10);
  const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
  if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
});
btnNewRow?.addEventListener("click", () => openDbModal("create", null));
dbModalCancel?.addEventListener("click", closeDbModal);
dbModalSave?.addEventListener("click", saveDbModal);
dbModalOverlay?.addEventListener("click", (e) => { if (e.target === dbModalOverlay) closeDbModal(); });


// =========================
// STATS
// =========================
const btnLoadStats = document.getElementById("btnLoadStats");
const statsGrid = document.getElementById("statsGrid");
const statsSearch = document.getElementById("statsSearch");
const statsStatus = document.getElementById("statsStatus");

const STATS_ENDPOINT = `${API_BASE}/api/admin/users-stats`;

let statsRaw = [];
let statsFiltered = [];

function setStatsStatus(text, isError=false) {
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

  statsFiltered.forEach(u => {
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
  statsFiltered = !q ? statsRaw.slice() : statsRaw.filter(u => (u.email || "").toLowerCase().includes(q));
  renderStats();
}

async function loadStats() {
  if (!statsGrid) return;

  statsGrid.innerHTML = "<div class='muted'>Chargement...</div>";
  setStatsStatus("Chargement...");

  const res = await fetch(STATS_ENDPOINT, { method: "GET", credentials: "include" });
  if (!res.ok) return setStatsStatus(`Erreur ${res.status}`, true);

  const data = await res.json();
  statsRaw = Array.isArray(data) ? data : [];
  setStatsStatus(`${statsRaw.length} users`);
  applyStatsFilter();
}

btnLoadStats?.addEventListener("click", loadStats);
statsSearch?.addEventListener("input", applyStatsFilter);


// ---------- INIT ----------
showSection("swagger");
if (key === "db") loadDbData();
if (key === "stats") loadStats();

