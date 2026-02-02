// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.nio.file.*;
// import java.util.List;

// @RestController
// @RequestMapping("/admin")
// public class AdminDashboardController {

//     // Adapt this path to your real log file
//     private static final Path LOG_PATH = Paths.get("logs/loto-api-dev.log");

//     @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminDashboard() {
//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//     <meta charset="UTF-8">
//     <title>Dashboard admin - Loto Tracker</title>
//     <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
//     <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
//     <meta name="viewport" content="width=device-width, initial-scale=1.0">
//     <style>
//         .nav-item {
//             width: 100%;
//             display: flex;
//             align-items: center;
//             gap: 8px;
//             padding: 8px 10px;
//             border-radius: 10px;
//             border: none;
//             cursor: pointer;
//             text-align: left;

//             background: transparent;
//             color: #9ca3af;
//             border-left: 3px solid transparent;

//             font-size: 0.85rem;
//             transition:
//                 background 0.18s ease,
//                 color 0.18s ease,
//                 border-color 0.18s ease,
//                 transform 0.10s ease;
//         }

//         .nav-item:hover {
//             background: #1e293b;
//             color: #e5e7eb;
//             border-left-color: #22c55e;
//             transform: translateX(2px);
//         }

//         .nav-item.active {
//             background: #334155;
//             color: #f9fafb;
//             font-weight: 600;
//             border-left-color: #22c55e;
//         }

//         /* tiny help for dates */
//         .cell-date {
//             font-variant-numeric: tabular-nums;
//             white-space: nowrap;
//         }
//     </style>
// </head>

// <body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;margin:0;">
// <div style="min-height:100vh;display:flex;">

//     <aside style="width:240px;background:#020617;border-right:1px solid #111827;
//                   padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
//                   position:sticky;top:0;align-self:flex-start;height:100vh;">
//         <div>
//             <div style="font-size:0.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">
//                 Loto Tracker API
//             </div>
//             <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">
//                 Admin console
//             </div>
//         </div>

//         <nav style="display:flex;flex-direction:column;gap:6px;font-size:0.85rem;">
//             <button class="nav-item active" data-section="swagger"><span>*</span><span>Swagger / API</span></button>
//             <button class="nav-item" data-section="logs"><span>*</span><span>Logs serveur</span></button>
//             <button class="nav-item" data-section="db"><span>*</span><span>Base de données (CRUD)</span></button>
//             <button class="nav-item" data-section="stats"><span>*</span><span>Stats joueurs & tickets</span></button>
//         </nav>

//         <div style="margin-top:auto;font-size:0.75rem;color:#6b7280;line-height:1.4;">
//             Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
//             Profil Spring : <span style="color:#22c55e;">dev</span>
//         </div>
//     </aside>

//     <div style="flex:1;display:flex;flex-direction:column;min-width:0;">

//         <header style="padding:14px 20px;border-bottom:1px solid #111827;
//                        display:flex;justify-content:space-between;align-items:center;">
//             <div>
//                 <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
//                 <p id="pageSubtitle" style="margin:2px 0 0;font-size:0.8rem;color:#9ca3af;">
//                     Vue générale : Swagger, logs serveur, base de données et statistiques.
//                 </p>
//             </div>
//             <button id="btnLogout" type="button"
//                     style="padding:6px 14px;border-radius:999px;border:1px solid #4b5563;
//                            background:#020617;color:#e5e7eb;font-size:0.8rem;cursor:pointer;">
//                 Déconnexion
//             </button>
//         </header>

//         <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

//             <section id="section-swagger" class="section-panel"
//                      style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
//                 <p style="margin:0 0 4px;font-size:0.85rem;color:#9ca3af;">
//                     Documentation Swagger de l'API Loto Tracker.
//                 </p>
//                 <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
//                     <button id="btnOpenSwagger" type="button"
//                             style="padding:6px 12px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;font-size:0.8rem;">
//                         Ouvrir Swagger dans un nouvel onglet
//                     </button>
//                     <span style="font-size:0.8rem;color:#6b7280;">ou utilisez la vue intégrée ci-dessous.</span>
//                 </div>
//                 <div style="flex:1;min-height:0;margin-top:8px;">
//                     <iframe id="swaggerFrame"
//                             src="/swagger-ui/index.html"
//                             style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;
//                                    background:#020617;"></iframe>
//                 </div>
//             </section>

//             <section id="section-logs" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">
//                 <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:0.8rem;">
//                     <span style="color:#9ca3af;">Logs du backend (lecture seule).</span>
//                     <button id="btnRefreshLogs" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;">
//                         🔄 Rafraîchir
//                     </button>
//                     <label style="color:#9ca3af;">
//                         Lignes :
//                         <select id="logLineCount"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="200">200</option>
//                             <option value="400" selected>400</option>
//                             <option value="800">800</option>
//                         </select>
//                     </label>
//                     <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//                         <input type="checkbox" id="autoScrollLogs" checked> Auto-scroll
//                     </label>
//                     <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//                         <input type="checkbox" id="autoRefreshLogs" checked> Auto-refresh (3s)
//                     </label>
//                     <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
//                 </div>

//                 <div id="logsContainer"
//                      style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
//                             background:#020617;padding:10px;font-family:monospace;font-size:11px;
//                             white-space:pre-wrap;">
//                     Chargement des logs...
//                 </div>
//             </section>

//             <section id="section-db" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:0.85rem;">
//                 <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//                     <span style="color:#9ca3af;">Vue base de données (CRUD simplifié).</span>
//                     <label style="color:#9ca3af;">
//                         Table :
//                         <select id="dbResourceSelect"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="tickets">public.tickets</option>
//                             <option value="users">public.users</option>
//                             <option value="ticket_gains">public.ticket_gains</option>
//                             <option value="refresh_tokens">public.refresh_tokens</option>
//                         </select>
//                     </label>
//                     <button id="btnLoadData" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;">
//                         Charger
//                     </button>
//                     <button id="btnNewRow" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:1px solid #4b5563;
//                                    background:#020617;color:#e5e7eb;cursor:pointer;">
//                         ➕ Nouvelle ligne
//                     </button>
//                     <span id="dbInfoText" style="color:#6b7280;font-size:0.8rem;">
//                         Lecture / édition / suppression. L’ID et les dates sont protégés par défaut.
//                     </span>
//                 </div>

//                 <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;font-size:0.8rem;">
//                     <label style="color:#9ca3af;">
//                         Recherche :
//                         <input id="dbSearch" type="text" placeholder="Texte à filtrer..."
//                                style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                       border:1px solid #4b5563;padding:4px 10px;font-size:0.8rem;">
//                     </label>
//                     <label style="color:#9ca3af;">
//                         Lignes / page :
//                         <select id="dbPageSize"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="20" selected>20</option>
//                             <option value="50">50</option>
//                             <option value="100">100</option>
//                         </select>
//                     </label>
//                     <span id="dbPagingInfo" style="color:#6b7280;">Page 1 / 1</span>
//                     <div style="display:flex;gap:4px;">
//                         <button id="dbPrevPage" type="button"
//                                 style="padding:3px 8px;border-radius:999px;border:1px solid #4b5563;
//                                        background:#020617;color:#e5e7eb;font-size:0.75rem;cursor:pointer;">◀</button>
//                         <button id="dbNextPage" type="button"
//                                 style="padding:3px 8px;border-radius:999px;border:1px solid #4b5563;
//                                        background:#020617;color:#e5e7eb;font-size:0.75rem;cursor:pointer;">▶</button>
//                     </div>
//                 </div>

//                 <div id="dbTableWrapper"
//                      style="flex:1;min-height:0;overflow:auto;border-radius:12px;border:1px solid #111827;
//                             background:#020617;">
//                     <table id="dbTable" style="border-collapse:collapse;width:100%;font-size:0.8rem;">
//                         <thead id="dbTableHead"></thead>
//                         <tbody id="dbTableBody"></tbody>
//                     </table>
//                 </div>

//                 <div id="dbModalOverlay"
//                      style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.7);
//                             align-items:center;justify-content:center;z-index:40;">
//                     <div style="background:#020617;border-radius:16px;border:1px solid #111827;
//                                 padding:16px 18px;width:420px;max-width:95%;">
//                         <h2 id="dbModalTitle" style="margin:0 0 8px;font-size:1rem;">Nouvelle ligne</h2>
//                         <form id="dbModalForm" style="display:flex;flex-direction:column;gap:8px;max-height:60vh;overflow:auto;"></form>
//                         <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;">
//                             <button id="dbModalCancel" type="button"
//                                     style="padding:5px 10px;border-radius:999px;border:1px solid #4b5563;
//                                            background:#020617;color:#e5e7eb;font-size:0.8rem;cursor:pointer;">Annuler</button>
//                             <button id="dbModalSave" type="button"
//                                     style="padding:5px 12px;border-radius:999px;border:none;background:#22c55e;
//                                            color:#020617;font-weight:600;font-size:0.8rem;cursor:pointer;">Enregistrer</button>
//                         </div>
//                     </div>
//                 </div>
//             </section>

//             <section id="section-stats" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:0.85rem;">
//                 <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//                     <span style="color:#9ca3af;">Analyse des joueurs et de leurs tickets.</span>
//                     <button id="btnLoadStats" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;">Recharger les stats</button>
//                     <label style="color:#9ca3af;">
//                         Tri :
//                         <select id="statsSort"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="ticketsDesc">Nb tickets ↓</option>
//                             <option value="gainDesc">Gain total ↓</option>
//                             <option value="lastTicketDesc">Dernier ticket ↓</option>
//                         </select>
//                     </label>
//                     <label style="color:#9ca3af;">
//                         Filtrer par email :
//                         <input id="statsSearch" type="text" placeholder="user@hbnb.com"
//                                style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                       border:1px solid #4b5563;padding:4px 10px;font-size:0.8rem;">
//                     </label>
//                 </div>

//                 <div id="statsGrid"
//                      style="flex:1;min-height:0;overflow:auto;display:grid;
//                             grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
//                             gap:10px;padding-top:4px;"></div>
//             </section>

//         </main>
//     </div>
// </div>

// <script>
//     const API_BASE = window.location.origin;

//     function getAuthHeaders() {
//         const token = localStorage.getItem("jwtToken");
//         if (!token) return {};
//         return { "Authorization": "Bearer " + token };
//     }

//     // ---------- NAV ----------
//     const navItems = document.querySelectorAll(".nav-item");
//     const sections = {
//         swagger: document.getElementById("section-swagger"),
//         logs: document.getElementById("section-logs"),
//         db: document.getElementById("section-db"),
//         stats: document.getElementById("section-stats")
//     };
//     const pageTitle = document.getElementById("pageTitle");
//     const pageSubtitle = document.getElementById("pageSubtitle");

//     const subtitles = {
//         swagger: "Documentation Swagger et tests de l’API.",
//         logs: "Suivi en temps réel des logs Spring Boot.",
//         db: "Exploration et modification des tables principales (lecture / écriture).",
//         stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
//     };

//     function showSection(key) {
//         Object.keys(sections).forEach(k => sections[k].style.display = (k === key) ? "flex" : "none");
//         navItems.forEach(btn => {
//             if (btn.getAttribute("data-section") === key) {
//                 btn.classList.add("active");
//                 btn.style.background = "#111827";
//                 btn.style.color = "#e5e7eb";
//             } else {
//                 btn.classList.remove("active");
//                 btn.style.background = "transparent";
//                 btn.style.color = "#9ca3af";
//             }
//         });
//         pageTitle.textContent = "Tableau de bord administrateur";
//         pageSubtitle.textContent = subtitles[key] || "";
//     }

//     navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.getAttribute("data-section"))));

//     // ---------- SWAGGER ----------
//     document.getElementById("btnOpenSwagger").addEventListener("click", () => window.open("/swagger-ui/index.html", "_blank"));

//     // ---------- LOGS ----------
//     const logsContainer = document.getElementById("logsContainer");
//     const btnRefreshLogs = document.getElementById("btnRefreshLogs");
//     const logLineCountSelect = document.getElementById("logLineCount");
//     const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
//     const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
//     const logsLastRefresh = document.getElementById("logsLastRefresh");

//     async function refreshLogs() {
//         const lines = logLineCountSelect.value;
//         try {
//             const res = await fetch(`/admin/logs?lines=${lines}`, {
//                 method: "GET",
//                 headers: { ...getAuthHeaders() },
//                 credentials: "include"
//             });
//             const text = await res.text();
//             logsContainer.textContent = text || "(Aucun log pour le moment)";
//             const now = new Date();
//             logsLastRefresh.textContent = "Dernier refresh : " + now.toLocaleTimeString("fr-FR");
//             if (autoScrollLogsCheckbox.checked) logsContainer.scrollTop = logsContainer.scrollHeight;
//         } catch (e) {
//             console.error(e);
//             logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//         }
//     }

//     btnRefreshLogs.addEventListener("click", refreshLogs);
//     logLineCountSelect.addEventListener("change", refreshLogs);
//     setInterval(() => { if (autoRefreshLogsCheckbox.checked) refreshLogs(); }, 3000);

//     // ---------- DB / CRUD ----------
//     const dbResourceSelect = document.getElementById("dbResourceSelect");
//     const btnLoadData = document.getElementById("btnLoadData");
//     const btnNewRow = document.getElementById("btnNewRow");
//     const dbTableHead = document.getElementById("dbTableHead");
//     const dbTableBody = document.getElementById("dbTableBody");
//     const dbSearch = document.getElementById("dbSearch");
//     const dbPageSize = document.getElementById("dbPageSize");
//     const dbPagingInfo = document.getElementById("dbPagingInfo");
//     const dbPrevPage = document.getElementById("dbPrevPage");
//     const dbNextPage = document.getElementById("dbNextPage");
//     const dbModalOverlay = document.getElementById("dbModalOverlay");
//     const dbModalTitle = document.getElementById("dbModalTitle");
//     const dbModalForm = document.getElementById("dbModalForm");
//     const dbModalCancel = document.getElementById("dbModalCancel");
//     const dbModalSave = document.getElementById("dbModalSave");

//     const DB_ENDPOINTS = {
//         users: "/api/admin/users",
//         tickets: "/api/admin/tickets",
//         ticket_gains: "/api/admin/ticket-gains",
//         refresh_tokens: "/api/admin/refresh-tokens"
//     };

//     const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);

//     // ✅ Column order like HeidiSQL (best-effort: we keep only columns that exist in the returned JSON)
//     const COLUMN_ORDERS = {
//         refresh_tokens: [
//             "id",
//             "user_id","userId",
//             "token_hash","tokenHash",
//             "created_at","createdAt",
//             "expires_at","expiresAt",
//             "revoked",
//             "revoked_at","revokedAt"
//         ],
//         users: [
//             "id","email","role","admin",
//             "firstName","lastName",
//             "created_at","createdAt",
//             "updated_at","updatedAt"
//         ],
//         tickets: [
//             "id","userId","user_id",
//             "drawDate","draw_date",
//             "created_at","createdAt",
//             "updated_at","updatedAt"
//         ],
//         ticket_gains: [
//             "id","ticketId","ticket_id",
//             "rank","amount","gain",
//             "created_at","createdAt"
//         ]
//     };

//     // ✅ Sort newest first (HeidiSQL-like browsing)
//     const SORT_FIELDS = {
//         refresh_tokens: ["created_at","createdAt","expires_at","expiresAt"],
//         users: ["created_at","createdAt","updated_at","updatedAt"],
//         tickets: ["created_at","createdAt","updated_at","updatedAt","drawDate","draw_date"],
//         ticket_gains: ["created_at","createdAt"]
//     };

//     let dbCurrentResource = "users";
//     let dbRawData = [];
//     let dbFilteredData = [];
//     let dbCurrentPage = 0;
//     let dbEditingRow = null;

//     function isDateKey(key) {
//         if (!key) return false;
//         const k = key.toLowerCase();
//         return k.endsWith("at") || k.endsWith("_at") || k.includes("date");
//     }

//     // ✅ handles:
//     // - epoch seconds (ex: 1768219900.136011)
//     // - epoch millis (13 digits)
//     // - ISO string
//     function formatDateValue(value) {
//         if (value === null || value === undefined || value === "") return "";

//         // ISO string
//         if (typeof value === "string") {
//             const t = Date.parse(value);
//             if (!Number.isNaN(t)) {
//                 return new Date(t).toLocaleString("fr-FR");
//             }
//             // numeric string?
//             const asNum = Number(value);
//             if (!Number.isNaN(asNum)) value = asNum;
//             else return value;
//         }

//         if (typeof value === "number") {
//             // heuristics
//             // if > 1e12 => ms, else seconds (maybe float)
//             const ms = (value > 1e12) ? value : (value * 1000);
//             const d = new Date(ms);
//             if (!Number.isNaN(d.getTime())) return d.toLocaleString("fr-FR");
//         }

//         return String(value);
//     }

//     function getSortMs(row) {
//         const fields = SORT_FIELDS[dbCurrentResource] || ["created_at","createdAt","updated_at","updatedAt","expires_at","expiresAt"];
//         for (const f of fields) {
//             const v = row?.[f];
//             if (v === null || v === undefined || v === "") continue;

//             // ISO
//             if (typeof v === "string") {
//                 const t = Date.parse(v);
//                 if (!Number.isNaN(t)) return t;
//                 const asNum = Number(v);
//                 if (!Number.isNaN(asNum)) {
//                     return (asNum > 1e12) ? asNum : asNum * 1000;
//                 }
//             }

//             if (typeof v === "number") {
//                 return (v > 1e12) ? v : v * 1000;
//             }
//         }
//         // fallback: no sort info
//         return 0;
//     }

//     function getColumns(pageData) {
//         if (!pageData || pageData.length === 0) return [];

//         const existing = new Set();
//         pageData.forEach(r => Object.keys(r || {}).forEach(k => existing.add(k)));

//         const preferred = COLUMN_ORDERS[dbCurrentResource];
//         let cols = [];

//         if (preferred && preferred.length) {
//             // keep preferred order for existing columns
//             cols = preferred.filter(k => existing.has(k));
//             // add remaining columns not in preferred (to not lose data)
//             const rest = Array.from(existing).filter(k => !cols.includes(k));
//             cols = cols.concat(rest);
//         } else {
//             // default: raw order
//             cols = Object.keys(pageData[0]);
//         }

//         return cols;
//     }

//     function applyDbFilter() {
//         const q = dbSearch.value.toLowerCase().trim();
//         if (!q) dbFilteredData = dbRawData.slice();
//         else {
//             dbFilteredData = dbRawData.filter(row =>
//                 JSON.stringify(row).toLowerCase().includes(q)
//             );
//         }

//         // ✅ sort newest first (DESC)
//         dbFilteredData.sort((a, b) => getSortMs(b) - getSortMs(a));

//         dbCurrentPage = 0;
//         renderDbTable();
//     }

//     function renderDbTable() {
//         dbTableHead.innerHTML = "";
//         dbTableBody.innerHTML = "";

//         const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);

//         if (!dbFilteredData || dbFilteredData.length === 0) {
//             dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
//             dbPagingInfo.textContent = "Page 1 / 1";
//             return;
//         }

//         const pageSize = parseInt(dbPageSize.value, 10) || 20;
//         const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//         if (dbCurrentPage >= totalPages) dbCurrentPage = totalPages - 1;

//         const start = dbCurrentPage * pageSize;
//         const end = Math.min(start + pageSize, dbFilteredData.length);
//         const pageData = dbFilteredData.slice(start, end);

//         const cols = getColumns(pageData);

//         // head
//         const headerRow = document.createElement("tr");
//         cols.forEach(k => {
//             const th = document.createElement("th");
//             th.textContent = k;
//             th.style.padding = "6px 8px";
//             th.style.borderBottom = "1px solid #111827";
//             th.style.textAlign = "left";
//             headerRow.appendChild(th);
//         });

//         if (!isReadOnly) {
//             const thActions = document.createElement("th");
//             thActions.textContent = "Actions";
//             thActions.style.padding = "6px 8px";
//             thActions.style.borderBottom = "1px solid #111827";
//             headerRow.appendChild(thActions);
//         }

//         dbTableHead.appendChild(headerRow);

//         // body
//         pageData.forEach(row => {
//             const tr = document.createElement("tr");
//             tr.style.borderBottom = "1px solid #020617";

//             cols.forEach(k => {
//                 const td = document.createElement("td");

//                 let value = row[k];
//                 if (value === null || value === undefined) value = "";
//                 if (typeof value === "object") value = JSON.stringify(value);

//                 // ✅ date formatting
//                 if (isDateKey(k)) {
//                     td.textContent = formatDateValue(value);
//                     td.className = "cell-date";
//                 } else {
//                     td.textContent = String(value);
//                 }

//                 td.style.padding = "6px 8px";
//                 td.style.verticalAlign = "top";
//                 td.style.maxWidth = "260px";
//                 td.style.wordBreak = "break-word";
//                 tr.appendChild(td);
//             });

//             if (!isReadOnly) {
//                 const tdActions = document.createElement("td");
//                 tdActions.style.padding = "6px 8px";
//                 tdActions.style.whiteSpace = "nowrap";

//                 const id = row.id || row._id;

//                 const btnEdit = document.createElement("button");
//                 btnEdit.textContent = "Modifier";
//                 btnEdit.style.padding = "3px 8px";
//                 btnEdit.style.borderRadius = "999px";
//                 btnEdit.style.border = "none";
//                 btnEdit.style.fontSize = "0.75rem";
//                 btnEdit.style.background = "#3b82f6";
//                 btnEdit.style.color = "#020617";
//                 btnEdit.style.cursor = "pointer";
//                 btnEdit.style.marginRight = "4px";
//                 btnEdit.addEventListener("click", () => openDbModal("edit", row));

//                 const btnDelete = document.createElement("button");
//                 btnDelete.textContent = "Supprimer";
//                 btnDelete.style.padding = "3px 8px";
//                 btnDelete.style.borderRadius = "999px";
//                 btnDelete.style.border = "none";
//                 btnDelete.style.fontSize = "0.75rem";
//                 btnDelete.style.background = "#ef4444";
//                 btnDelete.style.color = "#020617";
//                 btnDelete.style.cursor = "pointer";

//                 btnDelete.addEventListener("click", async () => {
//                     if (!id) { alert("ID introuvable."); return; }
//                     if (!confirm("Supprimer " + dbCurrentResource + " #" + id + " ?")) return;
//                     const url = DB_ENDPOINTS[dbCurrentResource] + "/" + id;
//                     try {
//                         const delRes = await fetch(url, {
//                             method: "DELETE",
//                             headers: { ...getAuthHeaders() },
//                             credentials: "include"
//                         });
//                         if (!delRes.ok) {
//                             alert("Erreur " + delRes.status + " lors de la suppression.");
//                             return;
//                         }
//                         dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
//                         applyDbFilter();
//                     } catch (e) {
//                         console.error(e);
//                         alert("Erreur réseau lors de la suppression.");
//                     }
//                 });

//                 tdActions.appendChild(btnEdit);
//                 tdActions.appendChild(btnDelete);
//                 tr.appendChild(tdActions);
//             }

//             dbTableBody.appendChild(tr);
//         });

//         dbPagingInfo.textContent = "Page " + (dbCurrentPage + 1) + " / " + totalPages;
//     }

//     async function loadDbData() {
//         dbCurrentResource = dbResourceSelect.value;

//         const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
//         btnNewRow.disabled = isReadOnly;
//         btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//         btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//         const url = DB_ENDPOINTS[dbCurrentResource];

//         dbTableHead.innerHTML = "";
//         dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";

//         if (!url) {
//             dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Endpoint non configuré.</td></tr>";
//             return;
//         }

//         try {
//             const res = await fetch(url, {
//                 method: "GET",
//                 headers: { ...getAuthHeaders() },
//                 credentials: "include"
//             });
//             if (!res.ok) {
//                 dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur " + res.status + ".</td></tr>";
//                 return;
//             }
//             const data = await res.json();
//             if (!Array.isArray(data) || data.length === 0) {
//                 dbRawData = [];
//                 applyDbFilter();
//                 return;
//             }
//             dbRawData = data;
//             applyDbFilter();
//         } catch (e) {
//             console.error(e);
//             dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur réseau.</td></tr>";
//         }
//     }

//     btnLoadData.addEventListener("click", loadDbData);
//     dbSearch.addEventListener("input", () => applyDbFilter());
//     dbPageSize.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
//     dbPrevPage.addEventListener("click", () => { if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); } });
//     dbNextPage.addEventListener("click", () => {
//         const pageSize = parseInt(dbPageSize.value, 10) || 20;
//         const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//         if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
//     });

//     function openDbModal(mode, row) {
//         if (READ_ONLY_RESOURCES.has(dbCurrentResource)) {
//             alert("Cette table est en lecture seule.");
//             return;
//         }

//         dbEditingRow = { resource: dbCurrentResource, mode, row: row || null };
//         dbModalForm.innerHTML = "";

//         const baseRow = row || (dbRawData[0] || {});
//         const keys = Object.keys(baseRow);

//         const protectedFields = ["id","_id","created_at","updated_at","createdAt","updatedAt"];

//         keys.forEach(k => {
//             const wrapper = document.createElement("div");
//             wrapper.style.display = "flex";
//             wrapper.style.flexDirection = "column";
//             wrapper.style.gap = "2px";

//             const label = document.createElement("label");
//             label.textContent = k;
//             label.style.fontSize = "0.8rem";
//             label.style.color = "#9ca3af";

//             const input = document.createElement("input");
//             input.name = k;
//             input.value = row ? (row[k] ?? "") : "";
//             input.style.background = "#020617";
//             input.style.color = "#e5e7eb";
//             input.style.borderRadius = "8px";
//             input.style.border = "1px solid #4b5563";
//             input.style.padding = "6px 8px";
//             input.style.fontSize = "0.8rem";

//             if (protectedFields.includes(k)) {
//                 input.disabled = true;
//                 input.style.opacity = "0.6";
//             }

//             wrapper.appendChild(label);
//             wrapper.appendChild(input);
//             dbModalForm.appendChild(wrapper);
//         });

//         dbModalTitle.textContent = (mode === "edit")
//             ? "Modifier la ligne #" + (row.id || row._id)
//             : "Nouvelle ligne";

//         dbModalOverlay.style.display = "flex";
//     }

//     function closeDbModal() {
//         dbModalOverlay.style.display = "none";
//         dbEditingRow = null;
//     }

//     dbModalCancel.addEventListener("click", closeDbModal);

//     dbModalSave.addEventListener("click", async () => {
//         if (!dbEditingRow) return;

//         const formData = new FormData(dbModalForm);
//         const obj = {};
//         formData.forEach((v, k) => {
//             const input = dbModalForm.querySelector('input[name="' + k + '"]');
//             if (input && input.disabled) return;
//             obj[k] = v === "" ? null : v;
//         });

//         const resource = dbEditingRow.resource;
//         const baseUrl = DB_ENDPOINTS[resource];
//         let method = "POST";
//         let url = baseUrl;

//         if (dbEditingRow.mode === "edit") {
//             const id = dbEditingRow.row.id || dbEditingRow.row._id;
//             method = "PUT";
//             url = baseUrl + "/" + id;
//         }

//         try {
//             const res = await fetch(url, {
//                 method,
//                 headers: {
//                     "Content-Type": "application/json",
//                     ...getAuthHeaders()
//                 },
//                 credentials: "include",
//                 body: JSON.stringify(obj)
//             });
//             if (!res.ok) {
//                 alert("Erreur " + res.status + " lors de l’enregistrement.");
//                 return;
//             }
//             closeDbModal();
//             await loadDbData();
//         } catch (e) {
//             console.error(e);
//             alert("Erreur réseau lors de l’enregistrement.");
//         }
//     });

//     btnNewRow.addEventListener("click", () => openDbModal("create", null));

//     // ---------- STATS ----------
//     const btnLoadStats = document.getElementById("btnLoadStats");
//     const statsGrid = document.getElementById("statsGrid");
//     const statsSort = document.getElementById("statsSort");
//     const statsSearch = document.getElementById("statsSearch");
//     const STATS_ENDPOINT = "/api/admin/users-stats";

//     let statsRaw = [];
//     let statsFiltered = [];

//     function applyStatsFilter() {
//         const q = statsSearch.value.toLowerCase().trim();
//         if (!q) statsFiltered = statsRaw.slice();
//         else {
//             statsFiltered = statsRaw.filter(u =>
//                 (u.email || "").toLowerCase().includes(q) ||
//                 (u.firstName || "").toLowerCase().includes(q) ||
//                 (u.lastName || "").toLowerCase().includes(q)
//             );
//         }

//         const sortKey = statsSort.value;
//         statsFiltered.sort((a, b) => {
//             const tA = a.ticketsCount || 0;
//             const tB = b.ticketsCount || 0;
//             const gA = a.totalGain || 0;
//             const gB = b.totalGain || 0;
//             const dA = a.lastTicketDate ? new Date(a.lastTicketDate).getTime() : 0;
//             const dB = b.lastTicketDate ? new Date(b.lastTicketDate).getTime() : 0;

//             if (sortKey === "ticketsDesc") return tB - tA;
//             if (sortKey === "gainDesc") return gB - gA;
//             if (sortKey === "lastTicketDesc") return dB - dA;
//             return 0;
//         });

//         renderStats();
//     }

//     function renderStats() {
//         statsGrid.innerHTML = "";
//         if (!statsFiltered || statsFiltered.length === 0) {
//             statsGrid.innerHTML = "<div style='color:#9ca3af;font-size:0.85rem;'>Aucune statistique disponible.</div>";
//             return;
//         }

//         statsFiltered.forEach(u => {
//             const card = document.createElement("article");
//             card.style.borderRadius = "14px";
//             card.style.border = "1px solid #111827";
//             card.style.background = "linear-gradient(135deg,#020617,#020617,#020617)";
//             card.style.padding = "10px 12px";
//             card.style.display = "flex";
//             card.style.flexDirection = "column";
//             card.style.gap = "6px";

//             const header = document.createElement("div");
//             header.style.display = "flex";
//             header.style.justifyContent = "space-between";
//             header.style.alignItems = "center";

//             const title = document.createElement("div");
//             title.innerHTML =
//                 "<div style='font-size:0.9rem;font-weight:600;'>" + (u.firstName || "") + " " + (u.lastName || "") + "</div>" +
//                 "<div style='font-size:0.78rem;color:#9ca3af;'>" + (u.email || "") + "</div>";

//             const badge = document.createElement("span");
//             badge.textContent = u.admin ? "ADMIN" : "USER";
//             badge.style.fontSize = "0.7rem";
//             badge.style.padding = "2px 8px";
//             badge.style.borderRadius = "999px";
//             badge.style.border = "1px solid " + (u.admin ? "#22c55e" : "#4b5563");
//             badge.style.color = u.admin ? "#22c55e" : "#9ca3af";

//             header.appendChild(title);
//             header.appendChild(badge);

//             const statsRow = document.createElement("div");
//             statsRow.style.display = "grid";
//             statsRow.style.gridTemplateColumns = "repeat(3,minmax(0,1fr))";
//             statsRow.style.gap = "6px";
//             statsRow.style.marginTop = "4px";

//             const blocks = [
//                 { label: "Tickets", value: u.ticketsCount || 0 },
//                 { label: "Gain total", value: (u.totalGain || 0) + " €" },
//                 { label: "Meilleur gain", value: (u.bestGain || 0) + " €" }
//             ];
//             blocks.forEach(b => {
//                 const div = document.createElement("div");
//                 div.style.fontSize = "0.75rem";
//                 div.style.color = "#9ca3af";
//                 div.innerHTML =
//                     "<div>" + b.label + "</div>" +
//                     "<div style='color:#e5e7eb;font-weight:600;font-size:0.86rem;'>" + b.value + "</div>";
//                 statsRow.appendChild(div);
//             });

//             const footer = document.createElement("div");
//             footer.style.marginTop = "4px";
//             footer.style.fontSize = "0.75rem";
//             footer.style.color = "#6b7280";
//             footer.textContent = "Dernier ticket : " + (u.lastTicketDate || "—") + " | ID utilisateur : " + (u.id || u._id || "—");

//             card.appendChild(header);
//             card.appendChild(statsRow);
//             card.appendChild(footer);
//             statsGrid.appendChild(card);
//         });
//     }

//     async function loadStats() {
//         statsGrid.innerHTML = "<div style='color:#9ca3af;font-size:0.85rem;'>Chargement des statistiques...</div>";
//         try {
//             const res = await fetch(STATS_ENDPOINT, {
//                 method: "GET",
//                 headers: { ...getAuthHeaders() },
//                 credentials: "include"
//             });
//             if (!res.ok) {
//                 statsGrid.innerHTML = "<div style='color:#f97373;font-size:0.85rem;'>Erreur " + res.status + ".</div>";
//                 return;
//             }
//             const data = await res.json();
//             if (!Array.isArray(data)) {
//                 statsGrid.innerHTML = "<div style='color:#f97373;font-size:0.85rem;'>Réponse invalide du serveur.</div>";
//                 return;
//             }
//             statsRaw = data;
//             applyStatsFilter();
//         } catch (e) {
//             console.error(e);
//             statsGrid.innerHTML = "<div style='color:#f97373;font-size:0.85rem;'>Erreur réseau.</div>";
//         }
//     }

//     btnLoadStats.addEventListener("click", loadStats);
//     statsSort.addEventListener("change", applyStatsFilter);
//     statsSearch.addEventListener("input", applyStatsFilter);

//     // ---------- LOGOUT ----------
//     document.getElementById("btnLogout").addEventListener("click", async () => {
//         try {
//             await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
//         } catch (e) { console.error(e); }
//         window.location.href = "/admin-login.html";
//     });

//     // ---------- INIT ----------
//     showSection("swagger");
//     refreshLogs();
//     loadDbData();
// </script>

// </body>
// </html>
// """;
//     }

//     @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
//     public ResponseEntity<String> getLogs(@RequestParam(defaultValue = "400") int lines) throws IOException {
//         if (!Files.exists(LOG_PATH)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + LOG_PATH.toAbsolutePath());
//         }

//         // UTF-8 pour éviter les "Ã©"
//         List<String> all = Files.readAllLines(LOG_PATH, java.nio.charset.StandardCharsets.UTF_8);
//         int fromIndex = Math.max(0, all.size() - lines);

//         String text = String.join("\n", all.subList(fromIndex, all.size()));

//         // Si le log contient des "\\n" (2 chars), on les convertit en vrais retours à la ligne
//         text = text.replace("\\n", "\n");

//         return ResponseEntity.ok(text);
//     }

// }

// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.*;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.Comparator;
// import java.util.List;
// import java.util.stream.Stream;
// import java.util.zip.GZIPInputStream;

// @RestController
// @RequestMapping("/admin")
// public class AdminDashboardController {

//     private static final Path LOG_DIR = Paths.get("logs");
//     // ✅ Ton log serveur principal
//     private static final String DEFAULT_LOG_FILE = "application.log";

//     // ---------------------------
//     // DASHBOARD (HTML)
//     // ---------------------------
//     @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminDashboard() {
//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//     <meta charset="UTF-8">
//     <title>Dashboard admin - Loto Tracker</title>
//     <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
//     <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
//     <meta name="viewport" content="width=device-width, initial-scale=1.0">
//     <style>
//         .nav-item {
//             width: 100%;
//             display: flex;
//             align-items: center;
//             gap: 8px;
//             padding: 8px 10px;
//             border-radius: 10px;
//             border: none;
//             cursor: pointer;
//             text-align: left;
//             background: transparent;
//             color: #9ca3af;
//             border-left: 3px solid transparent;
//             font-size: 0.85rem;
//             transition: background 0.18s ease, color 0.18s ease, border-color 0.18s ease, transform 0.10s ease;
//         }
//         .nav-item:hover {
//             background: #1e293b;
//             color: #e5e7eb;
//             border-left-color: #22c55e;
//             transform: translateX(2px);
//         }
//         .nav-item.active {
//             background: #334155;
//             color: #f9fafb;
//             font-weight: 600;
//             border-left-color: #22c55e;
//         }
//         .cell-date { font-variant-numeric: tabular-nums; white-space: nowrap; }
//     </style>
// </head>

// <body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;margin:0;">
// <div style="min-height:100vh;display:flex;">

//     <aside style="width:240px;background:#020617;border-right:1px solid #111827;
//                   padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
//                   position:sticky;top:0;align-self:flex-start;height:100vh;">
//         <div>
//             <div style="font-size:0.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">
//                 Loto Tracker API
//             </div>
//             <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">
//                 Admin console
//             </div>
//         </div>

//         <nav style="display:flex;flex-direction:column;gap:6px;font-size:0.85rem;">
//             <button class="nav-item active" data-section="swagger"><span>*</span><span>Swagger / API</span></button>
//             <button class="nav-item" data-section="logs"><span>*</span><span>Logs serveur</span></button>
//             <button class="nav-item" data-section="db"><span>*</span><span>Base de données (CRUD)</span></button>
//             <button class="nav-item" data-section="stats"><span>*</span><span>Stats joueurs & tickets</span></button>
//         </nav>

//         <div style="margin-top:auto;font-size:0.75rem;color:#6b7280;line-height:1.4;">
//             Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
//             Profil Spring : <span style="color:#22c55e;">dev</span>
//         </div>
//     </aside>

//     <div style="flex:1;display:flex;flex-direction:column;min-width:0;">

//         <header style="padding:14px 20px;border-bottom:1px solid #111827;
//                        display:flex;justify-content:space-between;align-items:center;">
//             <div>
//                 <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
//                 <p id="pageSubtitle" style="margin:2px 0 0;font-size:0.8rem;color:#9ca3af;">
//                     Vue générale : Swagger, logs serveur, base de données et statistiques.
//                 </p>
//             </div>
//             <button id="btnLogout" type="button"
//                     style="padding:6px 14px;border-radius:999px;border:1px solid #4b5563;
//                            background:#020617;color:#e5e7eb;font-size:0.8rem;cursor:pointer;">
//                 Déconnexion
//             </button>
//         </header>

//         <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

//             <section id="section-swagger" class="section-panel"
//                      style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
//                 <p style="margin:0 0 4px;font-size:0.85rem;color:#9ca3af;">
//                     Documentation Swagger de l'API Loto Tracker.
//                 </p>
//                 <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
//                     <button id="btnOpenSwagger" type="button"
//                             style="padding:6px 12px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;font-size:0.8rem;">
//                         Ouvrir Swagger dans un nouvel onglet
//                     </button>
//                     <span style="font-size:0.8rem;color:#6b7280;">ou utilisez la vue intégrée ci-dessous.</span>
//                 </div>
//                 <div style="flex:1;min-height:0;margin-top:8px;">
//                     <iframe id="swaggerFrame"
//                             src="/swagger-ui/index.html"
//                             style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;
//                                    background:#020617;"></iframe>
//                 </div>
//             </section>

//             <section id="section-logs" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">

//                 <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:0.8rem;">
//                     <span style="color:#9ca3af;">Logs du backend (lecture seule).</span>

//                     <label style="color:#9ca3af;">
//                         Fichier :
//                         <select id="logFileSelect"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;min-width:240px;">
//                             <option>Chargement...</option>
//                         </select>
//                     </label>

//                     <button id="btnRefreshLogs" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;">
//                         🔄 Rafraîchir
//                     </button>

//                     <label style="color:#9ca3af;">
//                         Lignes :
//                         <select id="logLineCount"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="200">200</option>
//                             <option value="400" selected>400</option>
//                             <option value="800">800</option>
//                             <option value="1500">1500</option>
//                         </select>
//                     </label>

//                     <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//                         <input type="checkbox" id="autoScrollLogs" checked> Auto-scroll
//                     </label>
//                     <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//                         <input type="checkbox" id="autoRefreshLogs" checked> Auto-refresh (3s)
//                     </label>

//                     <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
//                 </div>

//                 <div id="logsContainer"
//                      style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
//                             background:#020617;padding:10px;font-family:monospace;font-size:11px;
//                             white-space:pre-wrap;">
//                     Chargement des logs...
//                 </div>
//             </section>

//             <section id="section-db" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:0.85rem;">
//                 <div style="color:#9ca3af;">(Ton CRUD/DB ici — inchangé)</div>
//             </section>

//             <section id="section-stats" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:0.85rem;">
//                 <div style="color:#9ca3af;">(Tes stats ici — inchangé)</div>
//             </section>

//         </main>
//     </div>
// </div>

// <script>
//     const DEFAULT_LOG_FILE = "application.log";

//     function getAuthHeaders() {
//         const token = localStorage.getItem("jwtToken");
//         if (!token) return {};
//         return { "Authorization": "Bearer " + token };
//     }

//     // ---------- NAV ----------
//     const navItems = document.querySelectorAll(".nav-item");
//     const sections = {
//         swagger: document.getElementById("section-swagger"),
//         logs: document.getElementById("section-logs"),
//         db: document.getElementById("section-db"),
//         stats: document.getElementById("section-stats")
//     };
//     const pageTitle = document.getElementById("pageTitle");
//     const pageSubtitle = document.getElementById("pageSubtitle");

//     const subtitles = {
//         swagger: "Documentation Swagger et tests de l’API.",
//         logs: "Suivi en temps réel des logs Spring Boot (application.log).",
//         db: "Exploration et modification des tables principales (lecture / écriture).",
//         stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
//     };

//     function showSection(key) {
//         Object.keys(sections).forEach(k => sections[k].style.display = (k === key) ? "flex" : "none");
//         navItems.forEach(btn => {
//             if (btn.getAttribute("data-section") === key) {
//                 btn.classList.add("active");
//                 btn.style.background = "#111827";
//                 btn.style.color = "#e5e7eb";
//             } else {
//                 btn.classList.remove("active");
//                 btn.style.background = "transparent";
//                 btn.style.color = "#9ca3af";
//             }
//         });
//         pageTitle.textContent = "Tableau de bord administrateur";
//         pageSubtitle.textContent = subtitles[key] || "";

//         if (key === "logs") {
//             loadLogFiles().then(() => refreshLogs());
//         }
//     }

//     navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.getAttribute("data-section"))));
//     document.getElementById("btnOpenSwagger").addEventListener("click", () => window.open("/swagger-ui/index.html", "_blank"));

//     // ---------- LOGS ----------
//     const logsContainer = document.getElementById("logsContainer");
//     const btnRefreshLogs = document.getElementById("btnRefreshLogs");
//     const logLineCountSelect = document.getElementById("logLineCount");
//     const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
//     const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
//     const logsLastRefresh = document.getElementById("logsLastRefresh");
//     const logFileSelect = document.getElementById("logFileSelect");

//     async function loadLogFiles() {
//         try {
//             const res = await fetch("/admin/logs/files", {
//                 method: "GET",
//                 headers: { ...getAuthHeaders() },
//                 credentials: "include"
//             });

//             if (!res.ok) {
//                 logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur " + res.status + "</option>";
//                 return;
//             }

//             const files = await res.json();
//             logFileSelect.innerHTML = "";

//             if (!Array.isArray(files) || files.length === 0) {
//                 const o = document.createElement("option");
//                 o.value = DEFAULT_LOG_FILE;
//                 o.textContent = "Aucun fichier (logs/)";
//                 logFileSelect.appendChild(o);
//                 return;
//             }

//             let foundDefault = false;

//             files.forEach(f => {
//                 const o = document.createElement("option");
//                 o.value = f;
//                 o.textContent = f;
//                 if (f === DEFAULT_LOG_FILE) {
//                     o.selected = true;  // ✅ sélection automatique
//                     foundDefault = true;
//                 }
//                 logFileSelect.appendChild(o);
//             });

//             // si application.log n'existe pas, on garde le 1er
//             if (!foundDefault) {
//                 logFileSelect.value = files[0];
//             }

//         } catch (e) {
//             console.error(e);
//             logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur réseau</option>";
//         }
//     }

//     async function refreshLogs() {
//         const lines = logLineCountSelect.value;
//         const file = logFileSelect.value || DEFAULT_LOG_FILE;

//         try {
//             const res = await fetch(`/admin/logs?lines=${encodeURIComponent(lines)}&file=${encodeURIComponent(file)}`, {
//                 method: "GET",
//                 headers: { ...getAuthHeaders() },
//                 credentials: "include"
//             });

//             const text = await res.text();
//             logsContainer.textContent = text || "(Aucun log pour le moment)";

//             const now = new Date();
//             logsLastRefresh.textContent = "Dernier refresh : " + now.toLocaleTimeString("fr-FR");

//             if (autoScrollLogsCheckbox.checked) logsContainer.scrollTop = logsContainer.scrollHeight;
//         } catch (e) {
//             console.error(e);
//             logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//         }
//     }

//     btnRefreshLogs.addEventListener("click", refreshLogs);
//     logLineCountSelect.addEventListener("change", refreshLogs);
//     logFileSelect.addEventListener("change", refreshLogs);
//     setInterval(() => { if (autoRefreshLogsCheckbox.checked) refreshLogs(); }, 3000);

//     // ---------- LOGOUT ----------
//     document.getElementById("btnLogout").addEventListener("click", async () => {
//         try {
//             await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
//         } catch (e) { console.error(e); }
//         window.location.href = "/admin-login.html";
//     });

//     // ---------- INIT ----------
//     showSection("swagger");
//     // on prépare les logs dès le départ (au cas où)
//     loadLogFiles();
// </script>

// </body>
// </html>
// """;
//     }

//     // ---------------------------
//     // LOGS API
//     // ---------------------------

//     /**
//      * Liste les fichiers de logs disponibles dans /logs
//      * Inclut: .log / .log.1 / .log.gz / etc.
//      */
//     @GetMapping(value = "/logs/files", produces = MediaType.APPLICATION_JSON_VALUE)
//     public List<String> listLogFiles() throws IOException {
//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return List.of();
//         }

//         try (Stream<Path> s = Files.list(LOG_DIR)) {
//             return s.filter(Files::isRegularFile)
//                     .map(p -> p.getFileName().toString())
//                     .filter(this::isAllowedLogName)
//                     // tri: les plus récents en haut (simplement alphabétique reverse ici, OK pour tes noms)
//                     .sorted(Comparator.reverseOrder())
//                     .toList();
//         }
//     }

//     /**
//      * Retourne les X dernières lignes du fichier demandé.
//      * Supporte .gz (décompression).
//      * Sécurisé: empêche de sortir du dossier logs via ../
//      */
//     @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
//     public ResponseEntity<String> getLogs(
//             @RequestParam(defaultValue = "400") int lines,
//             @RequestParam(defaultValue = DEFAULT_LOG_FILE) String file
//     ) throws IOException {

//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return ResponseEntity.ok("Dossier logs introuvable : " + LOG_DIR.toAbsolutePath());
//         }

//         if (!isAllowedLogName(file)) {
//             return ResponseEntity.badRequest().body("Fichier log invalide.");
//         }

//         Path requested = LOG_DIR.resolve(file).normalize();
//         if (!requested.startsWith(LOG_DIR)) {
//             return ResponseEntity.badRequest().body("Fichier invalide.");
//         }

//         if (!Files.exists(requested) || !Files.isRegularFile(requested)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + requested.toAbsolutePath());
//         }

//         int safeLines = Math.max(1, Math.min(lines, 20000));

//         List<String> all = readAllLinesSmart(requested);

//         int fromIndex = Math.max(0, all.size() - safeLines);

//         String text = String.join("\n", all.subList(fromIndex, all.size()));

//         // Si le log contient des "\\n" (2 chars), on les convertit en vrais retours à la ligne
//         text = text.replace("\\\\n", "\n");

//         return ResponseEntity.ok(text);
//     }

//     // ---------------------------
//     // Helpers
//     // ---------------------------

//     private boolean isAllowedLogName(String name) {
//         if (name == null) return false;
//         // bloque toute tentative de path traversal
//         if (name.contains("..") || name.contains("/") || name.contains("\\\\")) return false;

//         // Autorise .log et archives
//         // ex: application.log, error.log, loto-api-dev.log, application.2026-01-15.log.gz, etc.
//         return name.endsWith(".log")
//                 || name.contains(".log.")
//                 || name.endsWith(".log.gz")
//                 || name.endsWith(".gz"); // au cas où tes rotations sont uniquement en .gz
//     }

//     private List<String> readAllLinesSmart(Path file) throws IOException {
//         String fn = file.getFileName().toString().toLowerCase();

//         if (fn.endsWith(".gz")) {
//             try (InputStream in = Files.newInputStream(file);
//                  GZIPInputStream gz = new GZIPInputStream(in);
//                  InputStreamReader isr = new InputStreamReader(gz, StandardCharsets.UTF_8);
//                  BufferedReader br = new BufferedReader(isr)) {

//                 return br.lines().toList();
//             }
//         }

//         return Files.readAllLines(file, StandardCharsets.UTF_8);
//     }
// }


// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.BufferedReader;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Stream;
// import java.util.zip.GZIPInputStream;

// @RestController
// @RequestMapping("/admin")
// public class AdminDashboardController {

//     private static final Path LOG_DIR = Paths.get("logs");

//     // ✅ d’après ta capture (logs/application.log)
//     private static final String DEFAULT_LOG_FILE = "application.log";

//     // ---------------------------
//     // DASHBOARD (HTML)
//     // ---------------------------
//     @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminDashboard() {
//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//   <meta charset="UTF-8">
//   <title>Dashboard admin - Loto Tracker</title>
//   <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
//   <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
//   <meta name="viewport" content="width=device-width, initial-scale=1.0">
//   <style>
//     .nav-item{
//       width:100%;display:flex;align-items:center;gap:8px;padding:8px 10px;border-radius:10px;border:none;cursor:pointer;text-align:left;
//       background:transparent;color:#9ca3af;border-left:3px solid transparent;font-size:.85rem;
//       transition:background .18s ease,color .18s ease,border-color .18s ease,transform .10s ease;
//     }
//     .nav-item:hover{background:#1e293b;color:#e5e7eb;border-left-color:#22c55e;transform:translateX(2px);}
//     .nav-item.active{background:#334155;color:#f9fafb;font-weight:600;border-left-color:#22c55e;}
//     .cell-date{font-variant-numeric:tabular-nums;white-space:nowrap;}
//     .pill{padding:5px 10px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;font-size:.8rem;}
//     .pill-green{border:none;background:#22c55e;color:#020617;font-weight:700;}
//     .input{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:4px 10px;font-size:.8rem;}
//     .select{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:3px 8px;}
//     .card{border-radius:12px;border:1px solid #111827;background:#020617;}
//     .muted{color:#9ca3af;}
//   </style>
// </head>

// <body style="background:#020617;color:#e5e7eb;font-family:system-ui,sans-serif;margin:0;">
// <div style="min-height:100vh;display:flex;">

//   <aside style="width:240px;background:#020617;border-right:1px solid #111827;
//                 padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
//                 position:sticky;top:0;align-self:flex-start;height:100vh;">
//     <div>
//       <div style="font-size:.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">Loto Tracker API</div>
//       <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">Admin console</div>
//     </div>

//     <nav style="display:flex;flex-direction:column;gap:6px;font-size:.85rem;">
//       <button class="nav-item active" data-section="swagger"><span>*</span><span>Swagger / API</span></button>
//       <button class="nav-item" data-section="logs"><span>*</span><span>Logs serveur</span></button>
//       <button class="nav-item" data-section="db"><span>*</span><span>Base de données (CRUD)</span></button>
//       <button class="nav-item" data-section="stats"><span>*</span><span>Stats joueurs & tickets</span></button>
//     </nav>

//     <div style="margin-top:auto;font-size:.75rem;color:#6b7280;line-height:1.4;">
//       Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
//       Profil Spring : <span style="color:#22c55e;">dev</span>
//     </div>
//   </aside>

//   <div style="flex:1;display:flex;flex-direction:column;min-width:0;">
//     <header style="padding:14px 20px;border-bottom:1px solid #111827;display:flex;justify-content:space-between;align-items:center;">
//       <div>
//         <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
//         <p id="pageSubtitle" style="margin:2px 0 0;font-size:.8rem;color:#9ca3af;">
//           Vue générale : Swagger, logs serveur, base de données et statistiques.
//         </p>
//       </div>
//       <button id="btnLogout" type="button" class="pill">Déconnexion</button>
//     </header>

//     <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

//       <!-- SWAGGER -->
//       <section id="section-swagger" class="section-panel" style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
//         <p class="muted" style="margin:0 0 4px;font-size:.85rem;">Documentation Swagger de l'API Loto Tracker.</p>
//         <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
//           <button id="btnOpenSwagger" type="button" class="pill pill-green">Ouvrir Swagger dans un nouvel onglet</button>
//           <span style="font-size:.8rem;color:#6b7280;">ou utilisez la vue intégrée ci-dessous.</span>
//         </div>
//         <div style="flex:1;min-height:0;margin-top:8px;">
//           <iframe id="swaggerFrame" src="/swagger-ui/index.html"
//                   style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;background:#020617;"></iframe>
//         </div>
//       </section>

//       <!-- LOGS -->
//       <section id="section-logs" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">
//         <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:.8rem;">
//           <span class="muted">Logs du backend (lecture seule).</span>

//           <label class="muted">
//             Fichier :
//             <select id="logFileSelect" class="select" style="min-width:240px;">
//               <option>Chargement...</option>
//             </select>
//           </label>

//           <button id="btnRefreshLogs" type="button" class="pill pill-green">🔄 Rafraîchir</button>

//           <label class="muted">
//             Lignes :
//             <select id="logLineCount" class="select">
//               <option value="200">200</option>
//               <option value="400" selected>400</option>
//               <option value="800">800</option>
//               <option value="1500">1500</option>
//             </select>
//           </label>

//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoScrollLogs" checked> Auto-scroll
//           </label>
//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoRefreshLogs" checked> Auto-refresh (3s)
//           </label>

//           <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
//         </div>

//         <div id="logsContainer"
//              style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
//                     background:#020617;padding:10px;font-family:monospace;font-size:11px;white-space:pre-wrap;">
//           Chargement des logs...
//         </div>
//       </section>

//       <!-- DB CRUD -->
//       <section id="section-db" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Vue base de données (CRUD simplifié).</span>

//           <label class="muted">
//             Table :
//             <select id="dbResourceSelect" class="select">
//               <option value="tickets">public.tickets</option>
//               <option value="users">public.users</option>
//               <option value="ticket_gains">public.ticket_gains</option>
//               <option value="refresh_tokens">public.refresh_tokens</option>
//             </select>
//           </label>

//           <button id="btnLoadData" type="button" class="pill pill-green">Charger</button>

//           <button id="btnNewRow" type="button" class="pill">➕ Nouvelle ligne</button>

//           <span id="dbInfoText" style="color:#6b7280;font-size:.8rem;">
//             Lecture / édition / suppression. L’ID et les dates sont protégés par défaut.
//           </span>
//         </div>

//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;font-size:.8rem;">
//           <label class="muted">
//             Recherche :
//             <input id="dbSearch" type="text" placeholder="Texte à filtrer..." class="input">
//           </label>

//           <label class="muted">
//             Lignes / page :
//             <select id="dbPageSize" class="select">
//               <option value="20" selected>20</option>
//               <option value="50">50</option>
//               <option value="100">100</option>
//             </select>
//           </label>

//           <span id="dbPagingInfo" style="color:#6b7280;">Page 1 / 1</span>

//           <div style="display:flex;gap:4px;">
//             <button id="dbPrevPage" type="button" class="pill" style="font-size:.75rem;">◀</button>
//             <button id="dbNextPage" type="button" class="pill" style="font-size:.75rem;">▶</button>
//           </div>
//         </div>

//         <div id="dbTableWrapper" class="card" style="flex:1;min-height:0;overflow:auto;">
//           <table id="dbTable" style="border-collapse:collapse;width:100%;font-size:.8rem;">
//             <thead id="dbTableHead"></thead>
//             <tbody id="dbTableBody"></tbody>
//           </table>
//         </div>

//         <!-- MODAL -->
//         <div id="dbModalOverlay"
//              style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.7);
//                     align-items:center;justify-content:center;z-index:40;">
//           <div style="background:#020617;border-radius:16px;border:1px solid #111827;
//                       padding:16px 18px;width:420px;max-width:95%;">
//             <h2 id="dbModalTitle" style="margin:0 0 8px;font-size:1rem;">Nouvelle ligne</h2>
//             <form id="dbModalForm" style="display:flex;flex-direction:column;gap:8px;max-height:60vh;overflow:auto;"></form>
//             <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;">
//               <button id="dbModalCancel" type="button" class="pill">Annuler</button>
//               <button id="dbModalSave" type="button" class="pill pill-green">Enregistrer</button>
//             </div>
//           </div>
//         </div>
//       </section>

//       <!-- STATS -->
//       <section id="section-stats" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Analyse des joueurs et de leurs tickets.</span>
//           <button id="btnLoadStats" type="button" class="pill pill-green">Recharger les stats</button>

//           <label class="muted">
//             Tri :
//             <select id="statsSort" class="select">
//               <option value="ticketsDesc">Nb tickets ↓</option>
//               <option value="gainDesc">Gain total ↓</option>
//               <option value="lastTicketDesc">Dernier ticket ↓</option>
//             </select>
//           </label>

//           <label class="muted">
//             Filtrer par email :
//             <input id="statsSearch" type="text" placeholder="user@hbnb.com" class="input">
//           </label>
//         </div>

//         <div id="statsGrid"
//              style="flex:1;min-height:0;overflow:auto;display:grid;
//                     grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
//                     gap:10px;padding-top:4px;"></div>
//       </section>

//     </main>
//   </div>
// </div>

// <script>
//   const DEFAULT_LOG_FILE = "application.log";

//   function getAuthHeaders() {
//     const token = localStorage.getItem("jwtToken");
//     if (!token) return {};
//     return { "Authorization": "Bearer " + token };
//   }

//   // ---------- NAV ----------
//   const navItems = document.querySelectorAll(".nav-item");
//   const sections = {
//     swagger: document.getElementById("section-swagger"),
//     logs: document.getElementById("section-logs"),
//     db: document.getElementById("section-db"),
//     stats: document.getElementById("section-stats")
//   };
//   const pageTitle = document.getElementById("pageTitle");
//   const pageSubtitle = document.getElementById("pageSubtitle");

//   const subtitles = {
//     swagger: "Documentation Swagger et tests de l’API.",
//     logs: "Suivi en temps réel des logs Spring Boot (application.log).",
//     db: "Exploration et modification des tables principales (lecture / écriture).",
//     stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
//   };

//   function showSection(key) {
//     Object.keys(sections).forEach(k => sections[k].style.display = (k === key) ? "flex" : "none");

//     navItems.forEach(btn => {
//       if (btn.getAttribute("data-section") === key) {
//         btn.classList.add("active");
//         btn.style.background = "#111827";
//         btn.style.color = "#e5e7eb";
//       } else {
//         btn.classList.remove("active");
//         btn.style.background = "transparent";
//         btn.style.color = "#9ca3af";
//       }
//     });

//     pageTitle.textContent = "Tableau de bord administrateur";
//     pageSubtitle.textContent = subtitles[key] || "";

//     if (key === "logs") {
//       loadLogFiles().then(() => refreshLogs());
//     }
//     if (key === "db") {
//       // facultatif: auto-charge à l'ouverture
//       // loadDbData();
//     }
//     if (key === "stats") {
//       loadStats();
//     }
//   }

//   navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.getAttribute("data-section"))));

//   // ---------- SWAGGER ----------
//   document.getElementById("btnOpenSwagger").addEventListener("click", () => window.open("/swagger-ui/index.html", "_blank"));

//   // ---------- LOGS ----------
//   const logsContainer = document.getElementById("logsContainer");
//   const btnRefreshLogs = document.getElementById("btnRefreshLogs");
//   const logLineCountSelect = document.getElementById("logLineCount");
//   const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
//   const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
//   const logsLastRefresh = document.getElementById("logsLastRefresh");
//   const logFileSelect = document.getElementById("logFileSelect");

//   async function loadLogFiles() {
//     try {
//       const res = await fetch("/admin/logs/files", {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur " + res.status + "</option>";
//         return;
//       }

//       const files = await res.json();
//       logFileSelect.innerHTML = "";

//       if (!Array.isArray(files) || files.length === 0) {
//         const o = document.createElement("option");
//         o.value = DEFAULT_LOG_FILE;
//         o.textContent = "Aucun fichier (logs/)";
//         logFileSelect.appendChild(o);
//         return;
//       }

//       let foundDefault = false;
//       files.forEach(f => {
//         const o = document.createElement("option");
//         o.value = f;
//         o.textContent = f;
//         if (f === DEFAULT_LOG_FILE) { o.selected = true; foundDefault = true; }
//         logFileSelect.appendChild(o);
//       });

//       if (!foundDefault) logFileSelect.value = files[0];

//     } catch (e) {
//       console.error(e);
//       logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur réseau</option>";
//     }
//   }

//   async function refreshLogs() {
//     const lines = logLineCountSelect.value;
//     const file = logFileSelect.value || DEFAULT_LOG_FILE;

//     try {
//       const res = await fetch(`/admin/logs?lines=${encodeURIComponent(lines)}&file=${encodeURIComponent(file)}`, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       const text = await res.text();
//       logsContainer.textContent = text || "(Aucun log pour le moment)";

//       const now = new Date();
//       logsLastRefresh.textContent = "Dernier refresh : " + now.toLocaleTimeString("fr-FR");

//       if (autoScrollLogsCheckbox.checked) logsContainer.scrollTop = logsContainer.scrollHeight;

//     } catch (e) {
//       console.error(e);
//       logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//     }
//   }

//   btnRefreshLogs.addEventListener("click", refreshLogs);
//   logLineCountSelect.addEventListener("change", refreshLogs);
//   logFileSelect.addEventListener("change", refreshLogs);
//   setInterval(() => { if (autoRefreshLogsCheckbox.checked) refreshLogs(); }, 3000);

//   // ---------- DB / CRUD ----------
//   const dbResourceSelect = document.getElementById("dbResourceSelect");
//   const btnLoadData = document.getElementById("btnLoadData");
//   const btnNewRow = document.getElementById("btnNewRow");
//   const dbTableHead = document.getElementById("dbTableHead");
//   const dbTableBody = document.getElementById("dbTableBody");
//   const dbSearch = document.getElementById("dbSearch");
//   const dbPageSize = document.getElementById("dbPageSize");
//   const dbPagingInfo = document.getElementById("dbPagingInfo");
//   const dbPrevPage = document.getElementById("dbPrevPage");
//   const dbNextPage = document.getElementById("dbNextPage");
//   const dbModalOverlay = document.getElementById("dbModalOverlay");
//   const dbModalTitle = document.getElementById("dbModalTitle");
//   const dbModalForm = document.getElementById("dbModalForm");
//   const dbModalCancel = document.getElementById("dbModalCancel");
//   const dbModalSave = document.getElementById("dbModalSave");
//   const dbInfoText = document.getElementById("dbInfoText");

//   const DB_ENDPOINTS = {
//     users: "/api/admin/users",
//     tickets: "/api/admin/tickets",
//     ticket_gains: "/api/admin/ticket-gains",
//     refresh_tokens: "/api/admin/refresh-tokens"
//   };

//   const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);

//   const COLUMN_ORDERS = {
//     refresh_tokens: ["id","user_id","userId","token_hash","tokenHash","created_at","createdAt","expires_at","expiresAt","revoked","revoked_at","revokedAt"],
//     users: ["id","email","role","admin","firstName","lastName","created_at","createdAt","updated_at","updatedAt"],
//     tickets: ["id","userId","user_id","drawDate","draw_date","created_at","createdAt","updated_at","updatedAt"],
//     ticket_gains: ["id","ticketId","ticket_id","rank","amount","gain","created_at","createdAt"]
//   };

//   const SORT_FIELDS = {
//     refresh_tokens: ["created_at","createdAt","expires_at","expiresAt"],
//     users: ["created_at","createdAt","updated_at","updatedAt"],
//     tickets: ["created_at","createdAt","updated_at","updatedAt","drawDate","draw_date"],
//     ticket_gains: ["created_at","createdAt"]
//   };

//   let dbCurrentResource = "users";
//   let dbRawData = [];
//   let dbFilteredData = [];
//   let dbCurrentPage = 0;
//   let dbEditingRow = null;

//   function isDateKey(key) {
//     if (!key) return false;
//     const k = key.toLowerCase();
//     return k.endsWith("at") || k.endsWith("_at") || k.includes("date");
//   }

//   // Supporte ISO, epoch sec/ms, et aussi tableaux Jackson LocalDateTime: [YYYY,MM,DD,HH,mm,ss]
//   function formatDateValue(value) {
//     if (value === null || value === undefined || value === "") return "";

//     if (Array.isArray(value) && value.length >= 3) {
//       const y = value[0] || 1970;
//       const m = (value[1] || 1) - 1;
//       const d = value[2] || 1;
//       const hh = value[3] || 0;
//       const mm = value[4] || 0;
//       const ss = value[5] || 0;
//       const dt = new Date(y, m, d, hh, mm, ss);
//       if (!Number.isNaN(dt.getTime())) return dt.toLocaleString("fr-FR");
//     }

//     if (typeof value === "string") {
//       const t = Date.parse(value);
//       if (!Number.isNaN(t)) return new Date(t).toLocaleString("fr-FR");

//       const asNum = Number(value);
//       if (!Number.isNaN(asNum)) value = asNum;
//       else return value;
//     }

//     if (typeof value === "number") {
//       const ms = (value > 1e12) ? value : (value * 1000);
//       const d = new Date(ms);
//       if (!Number.isNaN(d.getTime())) return d.toLocaleString("fr-FR");
//     }

//     return String(value);
//   }

//   function getSortMs(row) {
//     const fields = SORT_FIELDS[dbCurrentResource] || ["created_at","createdAt","updated_at","updatedAt","expires_at","expiresAt"];
//     for (const f of fields) {
//       const v = row?.[f];
//       if (v === null || v === undefined || v === "") continue;

//       if (Array.isArray(v) && v.length >= 3) {
//         const y = v[0] || 1970;
//         const m = (v[1] || 1) - 1;
//         const d = v[2] || 1;
//         const hh = v[3] || 0;
//         const mm = v[4] || 0;
//         const ss = v[5] || 0;
//         return new Date(y, m, d, hh, mm, ss).getTime();
//       }

//       if (typeof v === "string") {
//         const t = Date.parse(v);
//         if (!Number.isNaN(t)) return t;
//         const asNum = Number(v);
//         if (!Number.isNaN(asNum)) return (asNum > 1e12) ? asNum : asNum * 1000;
//       }

//       if (typeof v === "number") return (v > 1e12) ? v : v * 1000;
//     }
//     return 0;
//   }

//   function getColumns(pageData) {
//     if (!pageData || pageData.length === 0) return [];

//     const existing = new Set();
//     pageData.forEach(r => Object.keys(r || {}).forEach(k => existing.add(k)));

//     const preferred = COLUMN_ORDERS[dbCurrentResource];
//     let cols = [];

//     if (preferred && preferred.length) {
//       cols = preferred.filter(k => existing.has(k));
//       const rest = Array.from(existing).filter(k => !cols.includes(k));
//       cols = cols.concat(rest);
//     } else {
//       cols = Object.keys(pageData[0]);
//     }

//     return cols;
//   }

//   function applyDbFilter() {
//     const q = dbSearch.value.toLowerCase().trim();
//     if (!q) dbFilteredData = dbRawData.slice();
//     else dbFilteredData = dbRawData.filter(row => JSON.stringify(row).toLowerCase().includes(q));

//     dbFilteredData.sort((a, b) => getSortMs(b) - getSortMs(a));
//     dbCurrentPage = 0;
//     renderDbTable();
//   }

//   function renderDbTable() {
//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "";

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);

//     if (isReadOnly) dbInfoText.textContent = "Table en lecture seule (tokens sensibles).";
//     else dbInfoText.textContent = "Lecture / édition / suppression. L’ID et les dates sont protégés par défaut.";

//     if (!dbFilteredData || dbFilteredData.length === 0) {
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
//       dbPagingInfo.textContent = "Page 1 / 1";
//       return;
//     }

//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage >= totalPages) dbCurrentPage = totalPages - 1;

//     const start = dbCurrentPage * pageSize;
//     const end = Math.min(start + pageSize, dbFilteredData.length);
//     const pageData = dbFilteredData.slice(start, end);

//     const cols = getColumns(pageData);

//     const headerRow = document.createElement("tr");
//     cols.forEach(k => {
//       const th = document.createElement("th");
//       th.textContent = k;
//       th.style.padding = "6px 8px";
//       th.style.borderBottom = "1px solid #111827";
//       th.style.textAlign = "left";
//       headerRow.appendChild(th);
//     });

//     if (!isReadOnly) {
//       const thActions = document.createElement("th");
//       thActions.textContent = "Actions";
//       thActions.style.padding = "6px 8px";
//       thActions.style.borderBottom = "1px solid #111827";
//       headerRow.appendChild(thActions);
//     }

//     dbTableHead.appendChild(headerRow);

//     pageData.forEach(row => {
//       const tr = document.createElement("tr");
//       tr.style.borderBottom = "1px solid #020617";

//       cols.forEach(k => {
//         const td = document.createElement("td");
//         let value = row[k];
//         if (value === null || value === undefined) value = "";

//         if (isDateKey(k)) {
//           td.textContent = formatDateValue(value);
//           td.className = "cell-date";
//         } else {
//           if (typeof value === "object" && !Array.isArray(value)) value = JSON.stringify(value);
//           td.textContent = String(value);
//         }

//         td.style.padding = "6px 8px";
//         td.style.verticalAlign = "top";
//         td.style.maxWidth = "260px";
//         td.style.wordBreak = "break-word";
//         tr.appendChild(td);
//       });

//       if (!isReadOnly) {
//         const tdActions = document.createElement("td");
//         tdActions.style.padding = "6px 8px";
//         tdActions.style.whiteSpace = "nowrap";

//         const id = row.id || row._id;

//         const btnEdit = document.createElement("button");
//         btnEdit.textContent = "Modifier";
//         btnEdit.style.padding = "3px 8px";
//         btnEdit.style.borderRadius = "999px";
//         btnEdit.style.border = "none";
//         btnEdit.style.fontSize = "0.75rem";
//         btnEdit.style.background = "#3b82f6";
//         btnEdit.style.color = "#020617";
//         btnEdit.style.cursor = "pointer";
//         btnEdit.style.marginRight = "4px";
//         btnEdit.addEventListener("click", () => openDbModal("edit", row));

//         const btnDelete = document.createElement("button");
//         btnDelete.textContent = "Supprimer";
//         btnDelete.style.padding = "3px 8px";
//         btnDelete.style.borderRadius = "999px";
//         btnDelete.style.border = "none";
//         btnDelete.style.fontSize = "0.75rem";
//         btnDelete.style.background = "#ef4444";
//         btnDelete.style.color = "#020617";
//         btnDelete.style.cursor = "pointer";

//         btnDelete.addEventListener("click", async () => {
//           if (!id) { alert("ID introuvable."); return; }
//           if (!confirm("Supprimer " + dbCurrentResource + " #" + id + " ?")) return;

//           const url = DB_ENDPOINTS[dbCurrentResource] + "/" + id;
//           try {
//             const delRes = await fetch(url, {
//               method: "DELETE",
//               headers: { ...getAuthHeaders() },
//               credentials: "include"
//             });
//             if (!delRes.ok) {
//               alert("Erreur " + delRes.status + " lors de la suppression.");
//               return;
//             }
//             dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
//             applyDbFilter();
//           } catch (e) {
//             console.error(e);
//             alert("Erreur réseau lors de la suppression.");
//           }
//         });

//         tdActions.appendChild(btnEdit);
//         tdActions.appendChild(btnDelete);
//         tr.appendChild(tdActions);
//       }

//       dbTableBody.appendChild(tr);
//     });

//     dbPagingInfo.textContent = "Page " + (dbCurrentPage + 1) + " / " + totalPages;
//   }

//   async function loadDbData() {
//     dbCurrentResource = dbResourceSelect.value;

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
//     btnNewRow.disabled = isReadOnly;
//     btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//     btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//     const url = DB_ENDPOINTS[dbCurrentResource];

//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";

//     if (!url) {
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Endpoint non configuré.</td></tr>";
//       return;
//     }

//     try {
//       const res = await fetch(url, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur " + res.status + ".</td></tr>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data) || data.length === 0) {
//         dbRawData = [];
//         applyDbFilter();
//         return;
//       }

//       dbRawData = data;
//       applyDbFilter();

//     } catch (e) {
//       console.error(e);
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur réseau.</td></tr>";
//     }
//   }

//   btnLoadData.addEventListener("click", loadDbData);
//   dbSearch.addEventListener("input", () => applyDbFilter());
//   dbPageSize.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
//   dbPrevPage.addEventListener("click", () => { if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); } });
//   dbNextPage.addEventListener("click", () => {
//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
//   });

//   function openDbModal(mode, row) {
//     if (READ_ONLY_RESOURCES.has(dbCurrentResource)) {
//       alert("Cette table est en lecture seule.");
//       return;
//     }

//     dbEditingRow = { resource: dbCurrentResource, mode, row: row || null };
//     dbModalForm.innerHTML = "";

//     const baseRow = row || (dbRawData[0] || {});
//     const keys = Object.keys(baseRow);

//     const protectedFields = ["id","_id","created_at","updated_at","createdAt","updatedAt"];

//     keys.forEach(k => {
//       const wrapper = document.createElement("div");
//       wrapper.style.display = "flex";
//       wrapper.style.flexDirection = "column";
//       wrapper.style.gap = "2px";

//       const label = document.createElement("label");
//       label.textContent = k;
//       label.style.fontSize = "0.8rem";
//       label.style.color = "#9ca3af";

//       const input = document.createElement("input");
//       input.name = k;
//       input.value = row ? (row[k] ?? "") : "";
//       input.style.background = "#020617";
//       input.style.color = "#e5e7eb";
//       input.style.borderRadius = "8px";
//       input.style.border = "1px solid #4b5563";
//       input.style.padding = "6px 8px";
//       input.style.fontSize = "0.8rem";

//       if (protectedFields.includes(k)) {
//         input.disabled = true;
//         input.style.opacity = "0.6";
//       }

//       wrapper.appendChild(label);
//       wrapper.appendChild(input);
//       dbModalForm.appendChild(wrapper);
//     });

//     dbModalTitle.textContent = (mode === "edit")
//       ? "Modifier la ligne #" + (row.id || row._id)
//       : "Nouvelle ligne";

//     dbModalOverlay.style.display = "flex";
//   }

//   function closeDbModal() {
//     dbModalOverlay.style.display = "none";
//     dbEditingRow = null;
//   }

//   dbModalCancel.addEventListener("click", closeDbModal);

//   dbModalSave.addEventListener("click", async () => {
//     if (!dbEditingRow) return;

//     const formData = new FormData(dbModalForm);
//     const obj = {};

//     formData.forEach((v, k) => {
//       const input = dbModalForm.querySelector('input[name="' + k + '"]');
//       if (input && input.disabled) return;
//       obj[k] = v === "" ? null : v;
//     });

//     const resource = dbEditingRow.resource;
//     const baseUrl = DB_ENDPOINTS[resource];
//     let method = "POST";
//     let url = baseUrl;

//     if (dbEditingRow.mode === "edit") {
//       const id = dbEditingRow.row.id || dbEditingRow.row._id;
//       method = "PUT";
//       url = baseUrl + "/" + id;
//     }

//     try {
//       const res = await fetch(url, {
//         method,
//         headers: {
//           "Content-Type": "application/json",
//           ...getAuthHeaders()
//         },
//         credentials: "include",
//         body: JSON.stringify(obj)
//       });

//       if (!res.ok) {
//         alert("Erreur " + res.status + " lors de l’enregistrement.");
//         return;
//       }

//       closeDbModal();
//       await loadDbData();

//     } catch (e) {
//       console.error(e);
//       alert("Erreur réseau lors de l’enregistrement.");
//     }
//   });

//   btnNewRow.addEventListener("click", () => openDbModal("create", null));

//   // ---------- STATS ----------
//   const btnLoadStats = document.getElementById("btnLoadStats");
//   const statsGrid = document.getElementById("statsGrid");
//   const statsSort = document.getElementById("statsSort");
//   const statsSearch = document.getElementById("statsSearch");
//   const STATS_ENDPOINT = "/api/admin/users-stats";

//   let statsRaw = [];
//   let statsFiltered = [];

//   function applyStatsFilter() {
//     const q = statsSearch.value.toLowerCase().trim();
//     if (!q) statsFiltered = statsRaw.slice();
//     else {
//       statsFiltered = statsRaw.filter(u =>
//         (u.email || "").toLowerCase().includes(q) ||
//         (u.firstName || "").toLowerCase().includes(q) ||
//         (u.lastName || "").toLowerCase().includes(q)
//       );
//     }

//     const sortKey = statsSort.value;
//     statsFiltered.sort((a, b) => {
//       const tA = a.ticketsCount || 0;
//       const tB = b.ticketsCount || 0;
//       const gA = a.totalGain || 0;
//       const gB = b.totalGain || 0;
//       const dA = a.lastTicketDate ? new Date(a.lastTicketDate).getTime() : 0;
//       const dB = b.lastTicketDate ? new Date(b.lastTicketDate).getTime() : 0;

//       if (sortKey === "ticketsDesc") return tB - tA;
//       if (sortKey === "gainDesc") return gB - gA;
//       if (sortKey === "lastTicketDesc") return dB - dA;
//       return 0;
//     });

//     renderStats();
//   }

//   function renderStats() {
//     statsGrid.innerHTML = "";
//     if (!statsFiltered || statsFiltered.length === 0) {
//       statsGrid.innerHTML = "<div style='color:#9ca3af;font-size:.85rem;'>Aucune statistique disponible.</div>";
//       return;
//     }

//     statsFiltered.forEach(u => {
//       const card = document.createElement("article");
//       card.style.borderRadius = "14px";
//       card.style.border = "1px solid #111827";
//       card.style.background = "linear-gradient(135deg,#020617,#020617,#020617)";
//       card.style.padding = "10px 12px";
//       card.style.display = "flex";
//       card.style.flexDirection = "column";
//       card.style.gap = "6px";

//       const header = document.createElement("div");
//       header.style.display = "flex";
//       header.style.justifyContent = "space-between";
//       header.style.alignItems = "center";

//       const title = document.createElement("div");
//       title.innerHTML =
//         "<div style='font-size:.9rem;font-weight:600;'>" + (u.firstName || "") + " " + (u.lastName || "") + "</div>" +
//         "<div style='font-size:.78rem;color:#9ca3af;'>" + (u.email || "") + "</div>";

//       const badge = document.createElement("span");
//       badge.textContent = u.admin ? "ADMIN" : "USER";
//       badge.style.fontSize = ".7rem";
//       badge.style.padding = "2px 8px";
//       badge.style.borderRadius = "999px";
//       badge.style.border = "1px solid " + (u.admin ? "#22c55e" : "#4b5563");
//       badge.style.color = u.admin ? "#22c55e" : "#9ca3af";

//       header.appendChild(title);
//       header.appendChild(badge);

//       const statsRow = document.createElement("div");
//       statsRow.style.display = "grid";
//       statsRow.style.gridTemplateColumns = "repeat(3,minmax(0,1fr))";
//       statsRow.style.gap = "6px";
//       statsRow.style.marginTop = "4px";

//       const blocks = [
//         { label: "Tickets", value: u.ticketsCount || 0 },
//         { label: "Gain total", value: (u.totalGain || 0) + " €" },
//         { label: "Meilleur gain", value: (u.bestGain || 0) + " €" }
//       ];

//       blocks.forEach(b => {
//         const div = document.createElement("div");
//         div.style.fontSize = ".75rem";
//         div.style.color = "#9ca3af";
//         div.innerHTML =
//           "<div>" + b.label + "</div>" +
//           "<div style='color:#e5e7eb;font-weight:600;font-size:.86rem;'>" + b.value + "</div>";
//         statsRow.appendChild(div);
//       });

//       const footer = document.createElement("div");
//       footer.style.marginTop = "4px";
//       footer.style.fontSize = ".75rem";
//       footer.style.color = "#6b7280";
//       footer.textContent = "Dernier ticket : " + (u.lastTicketDate || "—") + " | ID utilisateur : " + (u.id || u._id || "—");

//       card.appendChild(header);
//       card.appendChild(statsRow);
//       card.appendChild(footer);

//       statsGrid.appendChild(card);
//     });
//   }

//   async function loadStats() {
//     statsGrid.innerHTML = "<div style='color:#9ca3af;font-size:.85rem;'>Chargement des statistiques...</div>";
//     try {
//       const res = await fetch(STATS_ENDPOINT, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         statsGrid.innerHTML = "<div style='color:#f97373;font-size:.85rem;'>Erreur " + res.status + ".</div>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data)) {
//         statsGrid.innerHTML = "<div style='color:#f97373;font-size:.85rem;'>Réponse invalide du serveur.</div>";
//         return;
//       }

//       statsRaw = data;
//       applyStatsFilter();

//     } catch (e) {
//       console.error(e);
//       statsGrid.innerHTML = "<div style='color:#f97373;font-size:.85rem;'>Erreur réseau.</div>";
//     }
//   }

//   btnLoadStats.addEventListener("click", loadStats);
//   statsSort.addEventListener("change", applyStatsFilter);
//   statsSearch.addEventListener("input", applyStatsFilter);

//   // ---------- LOGOUT ----------
//   document.getElementById("btnLogout").addEventListener("click", async () => {
//     try { await fetch("/api/auth/logout", { method: "POST", credentials: "include" }); }
//     catch (e) { console.error(e); }
//     window.location.href = "/admin-login.html";
//   });

//   // ---------- INIT ----------
//   showSection("swagger");
//   loadLogFiles().then(() => refreshLogs());
//   loadDbData();
//   loadStats();
// </script>

// </body>
// </html>
// """;
//     }

//     // ---------------------------
//     // LOGS API
//     // ---------------------------

//     /**
//      * Liste les fichiers de logs disponibles dans /logs
//      * Inclut: .log / .log.1 / .log.gz / etc.
//      */
//     @GetMapping(value = "/logs/files", produces = MediaType.APPLICATION_JSON_VALUE)
//     public List<String> listLogFiles() throws IOException {
//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return List.of();
//         }

//         try (Stream<Path> s = Files.list(LOG_DIR)) {
//             return s.filter(Files::isRegularFile)
//                     .map(p -> p.getFileName().toString())
//                     .filter(this::isAllowedLogName)
//                     .sorted(Comparator.reverseOrder())
//                     .toList();
//         }
//     }

//     /**
//      * Retourne les X dernières lignes du fichier demandé.
//      * Supporte .gz (décompression).
//      * Sécurisé: empêche de sortir du dossier logs via ../
//      */
//     @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
//     public ResponseEntity<String> getLogs(
//             @RequestParam(defaultValue = "400") int lines,
//             @RequestParam(defaultValue = DEFAULT_LOG_FILE) String file
//     ) throws IOException {

//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return ResponseEntity.ok("Dossier logs introuvable : " + LOG_DIR.toAbsolutePath());
//         }

//         if (!isAllowedLogName(file)) {
//             return ResponseEntity.badRequest().body("Fichier log invalide.");
//         }

//         Path requested = LOG_DIR.resolve(file).normalize();
//         if (!requested.startsWith(LOG_DIR)) {
//             return ResponseEntity.badRequest().body("Fichier invalide.");
//         }

//         if (!Files.exists(requested) || !Files.isRegularFile(requested)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + requested.toAbsolutePath());
//         }

//         int safeLines = Math.max(1, Math.min(lines, 20000));
//         List<String> all = readAllLinesSmart(requested);

//         int fromIndex = Math.max(0, all.size() - safeLines);
//         String text = String.join("\n", all.subList(fromIndex, all.size()));

//         // Si le log contient des "\\n" (2 chars), on les convertit en vrais retours à la ligne
//         text = text.replace("\\\\n", "\n");

//         return ResponseEntity.ok(text);
//     }

//     // ---------------------------
//     // Helpers
//     // ---------------------------

//     private boolean isAllowedLogName(String name) {
//         if (name == null) return false;
//         if (name.contains("..") || name.contains("/") || name.contains("\\\\")) return false;

//         return name.endsWith(".log")
//                 || name.contains(".log.")
//                 || name.endsWith(".log.gz")
//                 || name.endsWith(".gz");
//     }

//     private List<String> readAllLinesSmart(Path file) throws IOException {
//         String fn = file.getFileName().toString().toLowerCase();

//         if (fn.endsWith(".gz")) {
//             try (InputStream in = Files.newInputStream(file);
//                  GZIPInputStream gz = new GZIPInputStream(in);
//                  InputStreamReader isr = new InputStreamReader(gz, StandardCharsets.UTF_8);
//                  BufferedReader br = new BufferedReader(isr)) {
//                 return br.lines().toList();
//             }
//         }

//         return Files.readAllLines(file, StandardCharsets.UTF_8);
//     }
// }


// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Stream;
// import java.util.zip.GZIPInputStream;

// @RestController
// @RequestMapping("/admin")
// public class AdminDashboardController {

//     private static final Path LOG_DIR = Paths.get("logs");
//     private static final String DEFAULT_LOG_FILE = "application.log";

//     // ---------------------------
//     // DASHBOARD (HTML)
//     // ---------------------------
//     @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminDashboard() {
//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//   <meta charset="UTF-8">
//   <title>Dashboard admin - Loto Tracker</title>
//   <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
//   <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
//   <meta name="viewport" content="width=device-width, initial-scale=1.0">
//   <style>
//     .nav-item{
//       width:100%;display:flex;align-items:center;gap:8px;padding:8px 10px;border-radius:10px;border:none;cursor:pointer;text-align:left;
//       background:transparent;color:#9ca3af;border-left:3px solid transparent;font-size:.85rem;
//       transition:background .18s ease,color .18s ease,border-color .18s ease,transform .10s ease;
//     }
//     .nav-item:hover{background:#1e293b;color:#e5e7eb;border-left-color:#22c55e;transform:translateX(2px);}
//     .nav-item.active{background:#334155;color:#f9fafb;font-weight:600;border-left-color:#22c55e;}
//     .cell-date{font-variant-numeric:tabular-nums;white-space:nowrap;}
//     .pill{padding:5px 10px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;font-size:.8rem;}
//     .pill-green{border:none;background:#22c55e;color:#020617;font-weight:700;}
//     .input{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:4px 10px;font-size:.8rem;}
//     .select{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:3px 8px;}
//     .card{border-radius:12px;border:1px solid #111827;background:#020617;}
//     .muted{color:#9ca3af;}
//     .danger{color:#f97373;}
//     .ok{color:#22c55e;}
//     code{background:#0b1220;border:1px solid #111827;border-radius:8px;padding:2px 6px;}
//   </style>
// </head>

// <body style="background:#020617;color:#e5e7eb;font-family:system-ui,sans-serif;margin:0;">
// <div style="min-height:100vh;display:flex;">

//   <aside style="width:240px;background:#020617;border-right:1px solid #111827;
//                 padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
//                 position:sticky;top:0;align-self:flex-start;height:100vh;">
//     <div>
//       <div style="font-size:.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">Loto Tracker API</div>
//       <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">Admin console</div>
//     </div>

//     <nav style="display:flex;flex-direction:column;gap:6px;font-size:.85rem;">
//       <button class="nav-item active" data-section="swagger"><span>*</span><span>Swagger / API</span></button>
//       <button class="nav-item" data-section="logs"><span>*</span><span>Logs serveur</span></button>
//       <button class="nav-item" data-section="db"><span>*</span><span>Base de données (CRUD)</span></button>
//       <button class="nav-item" data-section="stats"><span>*</span><span>Stats joueurs & tickets</span></button>
//     </nav>

//     <div style="margin-top:auto;font-size:.75rem;color:#6b7280;line-height:1.4;">
//       Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
//       Profil Spring : <span style="color:#22c55e;">dev</span>
//     </div>
//   </aside>

//   <div style="flex:1;display:flex;flex-direction:column;min-width:0;">
//     <header style="padding:14px 20px;border-bottom:1px solid #111827;display:flex;justify-content:space-between;align-items:center;">
//       <div>
//         <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
//         <p id="pageSubtitle" style="margin:2px 0 0;font-size:.8rem;color:#9ca3af;">
//           Vue générale : Swagger, logs serveur, base de données et statistiques.
//         </p>
//       </div>
//       <button id="btnLogout" type="button" class="pill">Déconnexion</button>
//     </header>

//     <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

//       <!-- SWAGGER -->
//       <section id="section-swagger" class="section-panel" style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
//         <p class="muted" style="margin:0 0 4px;font-size:.85rem;">Documentation Swagger de l'API Loto Tracker.</p>
//         <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
//           <button id="btnOpenSwagger" type="button" class="pill pill-green">Ouvrir Swagger dans un nouvel onglet</button>
//           <span style="font-size:.8rem;color:#6b7280;">ou utilisez la vue intégrée ci-dessous.</span>
//         </div>
//         <div style="flex:1;min-height:0;margin-top:8px;">
//           <iframe id="swaggerFrame" src="/swagger-ui/index.html"
//                   style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;background:#020617;"></iframe>
//         </div>
//       </section>

//       <!-- LOGS -->
//       <section id="section-logs" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">
//         <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:.8rem;">
//           <span class="muted">Logs du backend (lecture seule).</span>

//           <label class="muted">
//             Fichier :
//             <select id="logFileSelect" class="select" style="min-width:240px;">
//               <option>Chargement...</option>
//             </select>
//           </label>

//           <button id="btnRefreshLogs" type="button" class="pill pill-green">🔄 Rafraîchir</button>

//           <label class="muted">
//             Lignes :
//             <select id="logLineCount" class="select">
//               <option value="200">200</option>
//               <option value="400" selected>400</option>
//               <option value="800">800</option>
//               <option value="1500">1500</option>
//             </select>
//           </label>

//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoScrollLogs" checked> Auto-scroll
//           </label>
//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoRefreshLogs" checked> Auto-refresh (3s)
//           </label>

//           <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
//         </div>

//         <div id="logsContainer"
//              style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
//                     background:#020617;padding:10px;font-family:monospace;font-size:11px;white-space:pre-wrap;">
//           Chargement des logs...
//         </div>
//       </section>

//       <!-- DB CRUD -->
//       <section id="section-db" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Vue base de données (CRUD simplifié).</span>

//           <label class="muted">
//             Table :
//             <select id="dbResourceSelect" class="select">
//               <option value="tickets">public.tickets</option>
//               <option value="users">public.users</option>

//               <!-- ✅ support ticket_gains / ticket_gain -->
//               <option value="ticket_gains">public.ticket_gains</option>
//               <option value="ticket_gain">public.ticket_gain (alias)</option>

//               <option value="refresh_tokens">public.refresh_tokens</option>
//             </select>
//           </label>

//           <button id="btnLoadData" type="button" class="pill pill-green">Charger</button>

//           <button id="btnNewRow" type="button" class="pill">➕ Nouvelle ligne</button>

//           <span id="dbInfoText" style="color:#6b7280;font-size:.8rem;">
//             Champs sensibles masqués (RGPD). refresh_tokens = lecture seule.
//           </span>
//         </div>

//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;font-size:.8rem;">
//           <label class="muted">
//             Recherche :
//             <input id="dbSearch" type="text" placeholder="Texte à filtrer..." class="input">
//           </label>

//           <label class="muted">
//             Lignes / page :
//             <select id="dbPageSize" class="select">
//               <option value="20" selected>20</option>
//               <option value="50">50</option>
//               <option value="100">100</option>
//             </select>
//           </label>

//           <span id="dbPagingInfo" style="color:#6b7280;">Page 1 / 1</span>

//           <div style="display:flex;gap:4px;">
//             <button id="dbPrevPage" type="button" class="pill" style="font-size:.75rem;">◀</button>
//             <button id="dbNextPage" type="button" class="pill" style="font-size:.75rem;">▶</button>
//           </div>

//           <span id="dbStatus" style="color:#6b7280;"></span>
//         </div>

//         <div id="dbTableWrapper" class="card" style="flex:1;min-height:0;overflow:auto;">
//           <table id="dbTable" style="border-collapse:collapse;width:100%;font-size:.8rem;">
//             <thead id="dbTableHead"></thead>
//             <tbody id="dbTableBody"></tbody>
//           </table>
//         </div>

//         <!-- MODAL -->
//         <div id="dbModalOverlay"
//              style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.7);
//                     align-items:center;justify-content:center;z-index:40;">
//           <div style="background:#020617;border-radius:16px;border:1px solid #111827;
//                       padding:16px 18px;width:420px;max-width:95%;">
//             <h2 id="dbModalTitle" style="margin:0 0 8px;font-size:1rem;">Nouvelle ligne</h2>
//             <form id="dbModalForm" style="display:flex;flex-direction:column;gap:8px;max-height:60vh;overflow:auto;"></form>
//             <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;">
//               <button id="dbModalCancel" type="button" class="pill">Annuler</button>
//               <button id="dbModalSave" type="button" class="pill pill-green">Enregistrer</button>
//             </div>
//           </div>
//         </div>
//       </section>

//       <!-- STATS -->
//       <section id="section-stats" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Analyse des joueurs et de leurs tickets.</span>
//           <button id="btnLoadStats" type="button" class="pill pill-green">Recharger les stats</button>

//           <label class="muted">
//             Tri :
//             <select id="statsSort" class="select">
//               <option value="ticketsDesc">Nb tickets ↓</option>
//               <option value="gainDesc">Gain total ↓</option>
//               <option value="lastTicketDesc">Dernier ticket ↓</option>
//             </select>
//           </label>

//           <label class="muted">
//             Filtrer par email :
//             <input id="statsSearch" type="text" placeholder="user@hbnb.com" class="input">
//           </label>

//           <span id="statsStatus" style="color:#6b7280;"></span>
//         </div>

//         <div id="statsGrid"
//              style="flex:1;min-height:0;overflow:auto;display:grid;
//                     grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
//                     gap:10px;padding-top:4px;"></div>
//       </section>

//     </main>
//   </div>
// </div>

// <script>
//   const DEFAULT_LOG_FILE = "application.log";

//     function getAuthHeaders() {
//     // JWT en cookie => pas de header Authorization
//     return {};
//     }


//   // ✅ RGPD/Sécurité : champs à masquer même en admin
//   const SENSITIVE_PARTS = [
//     "password","hashed","hash",
//     "token","refresh","access",
//     "secret","apikey","api_key",
//     "session","cookie","reset"
//   ];

//   function isSensitiveKey(key) {
//     if (!key) return false;
//     const k = String(key).toLowerCase();
//     return SENSITIVE_PARTS.some(p => k.includes(p));
//   }

//   function maskValue(v) {
//     if (v === null || v === undefined || v === "") return "";
//     const s = String(v);
//     if (s.length <= 6) return "••••••";
//     return s.slice(0, 3) + "••••••" + s.slice(-3);
//   }

//   // ✅ pour éviter [object Object]
//   function safeCellValue(key, value) {
//     if (value === null || value === undefined) return "";

//     if (isSensitiveKey(key)) return maskValue(value);

//     // relations / arrays
//     if (Array.isArray(value)) {
//       // ex users.tickets : mieux RGPD = compteur
//       if (String(key).toLowerCase().includes("tickets")) {
//         return "[array] (" + value.length + ")";
//       }
//       return JSON.stringify(value);
//     }

//     if (typeof value === "object") {
//       return JSON.stringify(value);
//     }

//     return String(value);
//   }

//   // ---------- NAV ----------
//   const navItems = document.querySelectorAll(".nav-item");
//   const sections = {
//     swagger: document.getElementById("section-swagger"),
//     logs: document.getElementById("section-logs"),
//     db: document.getElementById("section-db"),
//     stats: document.getElementById("section-stats")
//   };
//   const pageTitle = document.getElementById("pageTitle");
//   const pageSubtitle = document.getElementById("pageSubtitle");

//   const subtitles = {
//     swagger: "Documentation Swagger et tests de l’API.",
//     logs: "Suivi en temps réel des logs Spring Boot (fichiers + .gz).",
//     db: "Exploration et modification des tables principales (CRUD).",
//     stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
//   };

//   function showSection(key) {
//     Object.keys(sections).forEach(k => sections[k].style.display = (k === key) ? "flex" : "none");

//     navItems.forEach(btn => {
//       if (btn.getAttribute("data-section") === key) {
//         btn.classList.add("active");
//         btn.style.background = "#111827";
//         btn.style.color = "#e5e7eb";
//       } else {
//         btn.classList.remove("active");
//         btn.style.background = "transparent";
//         btn.style.color = "#9ca3af";
//       }
//     });

//     pageTitle.textContent = "Tableau de bord administrateur";
//     pageSubtitle.textContent = subtitles[key] || "";

//     if (key === "logs") loadLogFiles().then(() => refreshLogs());
//     if (key === "stats") loadStats();
//   }

//   navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.getAttribute("data-section"))));

//   // ---------- SWAGGER ----------
//   document.getElementById("btnOpenSwagger").addEventListener("click", () => window.open("/swagger-ui/index.html", "_blank"));

//   // ---------- LOGS ----------
//   const logsContainer = document.getElementById("logsContainer");
//   const btnRefreshLogs = document.getElementById("btnRefreshLogs");
//   const logLineCountSelect = document.getElementById("logLineCount");
//   const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
//   const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
//   const logsLastRefresh = document.getElementById("logsLastRefresh");
//   const logFileSelect = document.getElementById("logFileSelect");

//   async function loadLogFiles() {
//     try {
//       const res = await fetch("/admin/logs/files", {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur " + res.status + "</option>";
//         return;
//       }

//       const files = await res.json();
//       logFileSelect.innerHTML = "";

//       if (!Array.isArray(files) || files.length === 0) {
//         const o = document.createElement("option");
//         o.value = DEFAULT_LOG_FILE;
//         o.textContent = "Aucun fichier (logs/)";
//         logFileSelect.appendChild(o);
//         return;
//       }

//       let foundDefault = false;
//       files.forEach(f => {
//         const o = document.createElement("option");
//         o.value = f;
//         o.textContent = f;
//         if (f === DEFAULT_LOG_FILE) { o.selected = true; foundDefault = true; }
//         logFileSelect.appendChild(o);
//       });

//       if (!foundDefault) logFileSelect.value = files[0];

//     } catch (e) {
//       console.error(e);
//       logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur réseau</option>";
//     }
//   }

//   async function refreshLogs() {
//     const lines = logLineCountSelect.value;
//     const file = logFileSelect.value || DEFAULT_LOG_FILE;

//     try {
//       const res = await fetch(`/admin/logs?lines=${encodeURIComponent(lines)}&file=${encodeURIComponent(file)}`, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       const text = await res.text();
//       logsContainer.textContent = text || "(Aucun log pour le moment)";

//       const now = new Date();
//       logsLastRefresh.textContent = "Dernier refresh : " + now.toLocaleTimeString("fr-FR");

//       if (autoScrollLogsCheckbox.checked) logsContainer.scrollTop = logsContainer.scrollHeight;

//     } catch (e) {
//       console.error(e);
//       logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//     }
//   }

//   btnRefreshLogs.addEventListener("click", refreshLogs);
//   logLineCountSelect.addEventListener("change", refreshLogs);
//   logFileSelect.addEventListener("change", refreshLogs);
//   setInterval(() => { if (autoRefreshLogsCheckbox.checked) refreshLogs(); }, 3000);

//   // ---------- DB / CRUD ----------
//   const dbResourceSelect = document.getElementById("dbResourceSelect");
//   const btnLoadData = document.getElementById("btnLoadData");
//   const btnNewRow = document.getElementById("btnNewRow");
//   const dbTableHead = document.getElementById("dbTableHead");
//   const dbTableBody = document.getElementById("dbTableBody");
//   const dbSearch = document.getElementById("dbSearch");
//   const dbPageSize = document.getElementById("dbPageSize");
//   const dbPagingInfo = document.getElementById("dbPagingInfo");
//   const dbPrevPage = document.getElementById("dbPrevPage");
//   const dbNextPage = document.getElementById("dbNextPage");
//   const dbModalOverlay = document.getElementById("dbModalOverlay");
//   const dbModalTitle = document.getElementById("dbModalTitle");
//   const dbModalForm = document.getElementById("dbModalForm");
//   const dbModalCancel = document.getElementById("dbModalCancel");
//   const dbModalSave = document.getElementById("dbModalSave");
//   const dbInfoText = document.getElementById("dbInfoText");
//   const dbStatus = document.getElementById("dbStatus");

//   // ✅ endpoints (alias ticket_gain)
//   const DB_ENDPOINTS = {
//     users: "/api/admin/users",
//     tickets: "/api/admin/tickets",
//     ticket_gains: "/api/admin/ticket-gains",
//     ticket_gain: "/api/admin/ticket-gains",
//     refresh_tokens: "/api/admin/refresh-tokens"
//   };

//   // ✅ RGPD/sécurité: refresh_tokens en lecture seule
//   const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);

//   // ✅ colonne ticket_gains conforme à ta DB (HeidiSQL)
//   const COLUMN_ORDERS = {
//     refresh_tokens: ["id","user_id","userId","token_hash","tokenHash","created_at","createdAt","expires_at","expiresAt","revoked","revoked_at","revokedAt"],
//     users: ["id","email","role","admin","firstName","lastName","created_at","createdAt","updated_at","updatedAt"],
//     tickets: ["id","userId","user_id","drawDate","draw_date","created_at","createdAt","updated_at","updatedAt"],
//     ticket_gains: ["id","ticket_id","ticketId","matching_numbers","lucky_number_match","gain_amount","created_at","createdAt"],
//     ticket_gain: ["id","ticket_id","ticketId","matching_numbers","lucky_number_match","gain_amount","created_at","createdAt"]
//   };

//   const SORT_FIELDS = {
//     refresh_tokens: ["created_at","createdAt","expires_at","expiresAt"],
//     users: ["created_at","createdAt","updated_at","updatedAt"],
//     tickets: ["created_at","createdAt","updated_at","updatedAt","drawDate","draw_date"],
//     ticket_gains: ["created_at","createdAt"],
//     ticket_gain: ["created_at","createdAt"]
//   };

//   let dbCurrentResource = "users";
//   let dbRawData = [];
//   let dbFilteredData = [];
//   let dbCurrentPage = 0;
//   let dbEditingRow = null;

//   function isDateKey(key) {
//     if (!key) return false;
//     const k = key.toLowerCase();
//     return k.endsWith("at") || k.endsWith("_at") || k.includes("date");
//   }

//   function formatDateValue(value) {
//     if (value === null || value === undefined || value === "") return "";

//     // tableau Jackson: [YYYY,MM,DD,HH,mm,ss]
//     if (Array.isArray(value) && value.length >= 3) {
//       const y = value[0] || 1970;
//       const m = (value[1] || 1) - 1;
//       const d = value[2] || 1;
//       const hh = value[3] || 0;
//       const mm = value[4] || 0;
//       const ss = value[5] || 0;
//       const dt = new Date(y, m, d, hh, mm, ss);
//       if (!Number.isNaN(dt.getTime())) return dt.toLocaleString("fr-FR");
//     }

//     if (typeof value === "string") {
//       const t = Date.parse(value);
//       if (!Number.isNaN(t)) return new Date(t).toLocaleString("fr-FR");
//       const asNum = Number(value);
//       if (!Number.isNaN(asNum)) value = asNum; else return value;
//     }

//     if (typeof value === "number") {
//       const ms = (value > 1e12) ? value : (value * 1000);
//       const d = new Date(ms);
//       if (!Number.isNaN(d.getTime())) return d.toLocaleString("fr-FR");
//     }

//     return String(value);
//   }

//   function getSortMs(row) {
//     const fields = SORT_FIELDS[dbCurrentResource] || ["created_at","createdAt","updated_at","updatedAt"];
//     for (const f of fields) {
//       const v = row?.[f];
//       if (v === null || v === undefined || v === "") continue;

//       if (Array.isArray(v) && v.length >= 3) {
//         const y = v[0] || 1970;
//         const m = (v[1] || 1) - 1;
//         const d = v[2] || 1;
//         const hh = v[3] || 0;
//         const mm = v[4] || 0;
//         const ss = v[5] || 0;
//         return new Date(y, m, d, hh, mm, ss).getTime();
//       }

//       if (typeof v === "string") {
//         const t = Date.parse(v);
//         if (!Number.isNaN(t)) return t;
//         const asNum = Number(v);
//         if (!Number.isNaN(asNum)) return (asNum > 1e12) ? asNum : asNum * 1000;
//       }

//       if (typeof v === "number") return (v > 1e12) ? v : v * 1000;
//     }
//     return 0;
//   }

//   function getColumns(pageData) {
//     if (!pageData || pageData.length === 0) return [];
//     const existing = new Set();
//     pageData.forEach(r => Object.keys(r || {}).forEach(k => existing.add(k)));

//     const preferred = COLUMN_ORDERS[dbCurrentResource];
//     let cols = [];

//     if (preferred && preferred.length) {
//       cols = preferred.filter(k => existing.has(k));
//       const rest = Array.from(existing).filter(k => !cols.includes(k));
//       cols = cols.concat(rest);
//     } else {
//       cols = Object.keys(pageData[0]);
//     }
//     return cols;
//   }

//   function applyDbFilter() {
//     const q = dbSearch.value.toLowerCase().trim();
//     if (!q) dbFilteredData = dbRawData.slice();
//     else dbFilteredData = dbRawData.filter(row => JSON.stringify(row).toLowerCase().includes(q));

//     dbFilteredData.sort((a, b) => getSortMs(b) - getSortMs(a));
//     dbCurrentPage = 0;
//     renderDbTable();
//   }

//   function setDbStatus(text, isError=false) {
//     dbStatus.textContent = text;
//     dbStatus.className = isError ? "danger" : "muted";
//   }

//   function renderDbTable() {
//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "";

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);

//     btnNewRow.disabled = isReadOnly;
//     btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//     btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//     if (!dbFilteredData || dbFilteredData.length === 0) {
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
//       dbPagingInfo.textContent = "Page 1 / 1";
//       return;
//     }

//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage >= totalPages) dbCurrentPage = totalPages - 1;

//     const start = dbCurrentPage * pageSize;
//     const end = Math.min(start + pageSize, dbFilteredData.length);
//     const pageData = dbFilteredData.slice(start, end);

//     const cols = getColumns(pageData);

//     const headerRow = document.createElement("tr");
//     cols.forEach(k => {
//       const th = document.createElement("th");
//       th.textContent = k;
//       th.style.padding = "6px 8px";
//       th.style.borderBottom = "1px solid #111827";
//       th.style.textAlign = "left";
//       headerRow.appendChild(th);
//     });

//     if (!isReadOnly) {
//       const thActions = document.createElement("th");
//       thActions.textContent = "Actions";
//       thActions.style.padding = "6px 8px";
//       thActions.style.borderBottom = "1px solid #111827";
//       headerRow.appendChild(thActions);
//     }

//     dbTableHead.appendChild(headerRow);

//     pageData.forEach(row => {
//       const tr = document.createElement("tr");
//       tr.style.borderBottom = "1px solid #020617";

//       cols.forEach(k => {
//         const td = document.createElement("td");
//         let value = row[k];

//         if (isDateKey(k)) {
//           td.textContent = formatDateValue(value);
//           td.className = "cell-date";
//         } else {
//           td.textContent = safeCellValue(k, value);
//         }

//         td.style.padding = "6px 8px";
//         td.style.verticalAlign = "top";
//         td.style.maxWidth = "320px";
//         td.style.wordBreak = "break-word";
//         tr.appendChild(td);
//       });

//       if (!isReadOnly) {
//         const tdActions = document.createElement("td");
//         tdActions.style.padding = "6px 8px";
//         tdActions.style.whiteSpace = "nowrap";

//         const id = row.id || row._id;

//         const btnEdit = document.createElement("button");
//         btnEdit.textContent = "Modifier";
//         btnEdit.style.padding = "3px 8px";
//         btnEdit.style.borderRadius = "999px";
//         btnEdit.style.border = "none";
//         btnEdit.style.fontSize = "0.75rem";
//         btnEdit.style.background = "#3b82f6";
//         btnEdit.style.color = "#020617";
//         btnEdit.style.cursor = "pointer";
//         btnEdit.style.marginRight = "4px";
//         btnEdit.addEventListener("click", () => openDbModal("edit", row));

//         const btnDelete = document.createElement("button");
//         btnDelete.textContent = "Supprimer";
//         btnDelete.style.padding = "3px 8px";
//         btnDelete.style.borderRadius = "999px";
//         btnDelete.style.border = "none";
//         btnDelete.style.fontSize = "0.75rem";
//         btnDelete.style.background = "#ef4444";
//         btnDelete.style.color = "#020617";
//         btnDelete.style.cursor = "pointer";

//         btnDelete.addEventListener("click", async () => {
//           if (!id) { alert("ID introuvable."); return; }
//           if (!confirm("Supprimer " + dbCurrentResource + " #" + id + " ?")) return;

//           const url = DB_ENDPOINTS[dbCurrentResource] + "/" + id;
//           try {
//             const delRes = await fetch(url, {
//               method: "DELETE",
//               headers: { ...getAuthHeaders() },
//               credentials: "include"
//             });
//             if (!delRes.ok) {
//               const t = await delRes.text().catch(() => "");
//               alert("Erreur " + delRes.status + " lors de la suppression.\\n" + t);
//               return;
//             }
//             dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
//             applyDbFilter();
//           } catch (e) {
//             console.error(e);
//             alert("Erreur réseau lors de la suppression.");
//           }
//         });

//         tdActions.appendChild(btnEdit);
//         tdActions.appendChild(btnDelete);
//         tr.appendChild(tdActions);
//       }

//       dbTableBody.appendChild(tr);
//     });

//     dbPagingInfo.textContent = "Page " + (dbCurrentPage + 1) + " / " + totalPages;
//   }

//   async function loadDbData() {
//     dbCurrentResource = dbResourceSelect.value;

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
//     btnNewRow.disabled = isReadOnly;
//     btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//     btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//     const url = DB_ENDPOINTS[dbCurrentResource];

//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";
//     setDbStatus("Chargement...");

//     if (!url) {
//       setDbStatus("Endpoint non configuré", true);
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Endpoint non configuré.</td></tr>";
//       return;
//     }

//     try {
//       const res = await fetch(url, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         const txt = await res.text().catch(() => "");
//         setDbStatus("Erreur " + res.status, true);
//         dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur " + res.status + ". " + (txt || "") + "</td></tr>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data) || data.length === 0) {
//         dbRawData = [];
//         setDbStatus("0 ligne");
//         applyDbFilter();
//         return;
//       }

//       dbRawData = data;
//       setDbStatus(data.length + " lignes");
//       applyDbFilter();

//     } catch (e) {
//       console.error(e);
//       setDbStatus("Erreur réseau", true);
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur réseau.</td></tr>";
//     }
//   }

//   btnLoadData.addEventListener("click", loadDbData);
//   dbSearch.addEventListener("input", () => applyDbFilter());
//   dbPageSize.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
//   dbPrevPage.addEventListener("click", () => { if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); } });
//   dbNextPage.addEventListener("click", () => {
//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
//   });

//   function openDbModal(mode, row) {
//     if (READ_ONLY_RESOURCES.has(dbCurrentResource)) {
//       alert("Cette table est en lecture seule (données sensibles).");
//       return;
//     }

//     dbEditingRow = { resource: dbCurrentResource, mode, row: row || null };
//     dbModalForm.innerHTML = "";

//     const baseRow = row || (dbRawData[0] || {});
//     const keys = Object.keys(baseRow);

//     // ✅ champs protégés
//     const protectedFields = ["id","_id","created_at","updated_at","createdAt","updatedAt"];

//     keys.forEach(k => {
//       const wrapper = document.createElement("div");
//       wrapper.style.display = "flex";
//       wrapper.style.flexDirection = "column";
//       wrapper.style.gap = "2px";

//       const label = document.createElement("label");
//       label.textContent = k + (isSensitiveKey(k) ? " (masqué)" : "");
//       label.style.fontSize = "0.8rem";
//       label.style.color = "#9ca3af";

//       const input = document.createElement("input");
//       input.name = k;
//       input.value = row ? (row[k] ?? "") : "";
//       input.style.background = "#020617";
//       input.style.color = "#e5e7eb";
//       input.style.borderRadius = "8px";
//       input.style.border = "1px solid #4b5563";
//       input.style.padding = "6px 8px";
//       input.style.fontSize = "0.8rem";

//       // ✅ empêche édition champs sensibles et champs techniques
//       if (protectedFields.includes(k) || isSensitiveKey(k)) {
//         input.disabled = true;
//         input.style.opacity = "0.6";
//         if (isSensitiveKey(k)) input.value = maskValue(input.value);
//       }

//       wrapper.appendChild(label);
//       wrapper.appendChild(input);
//       dbModalForm.appendChild(wrapper);
//     });

//     dbModalTitle.textContent = (mode === "edit")
//       ? "Modifier la ligne #" + (row.id || row._id)
//       : "Nouvelle ligne";

//     dbModalOverlay.style.display = "flex";
//   }

//   function closeDbModal() {
//     dbModalOverlay.style.display = "none";
//     dbEditingRow = null;
//   }

//   dbModalCancel.addEventListener("click", closeDbModal);

//   dbModalSave.addEventListener("click", async () => {
//     if (!dbEditingRow) return;

//     const formData = new FormData(dbModalForm);
//     const obj = {};

//     formData.forEach((v, k) => {
//       const input = dbModalForm.querySelector('input[name="' + k + '"]');
//       if (input && input.disabled) return;
//       obj[k] = v === "" ? null : v;
//     });

//     const resource = dbEditingRow.resource;
//     const baseUrl = DB_ENDPOINTS[resource];
//     let method = "POST";
//     let url = baseUrl;

//     if (dbEditingRow.mode === "edit") {
//       const id = dbEditingRow.row.id || dbEditingRow.row._id;
//       method = "PUT";
//       url = baseUrl + "/" + id;
//     }

//     try {
//       const res = await fetch(url, {
//         method,
//         headers: { "Content-Type": "application/json", ...getAuthHeaders() },
//         credentials: "include",
//         body: JSON.stringify(obj)
//       });

//       if (!res.ok) {
//         const t = await res.text().catch(() => "");
//         alert("Erreur " + res.status + " lors de l’enregistrement.\\n" + t);
//         return;
//       }

//       closeDbModal();
//       await loadDbData();

//     } catch (e) {
//       console.error(e);
//       alert("Erreur réseau lors de l’enregistrement.");
//     }
//   });

//   btnNewRow.addEventListener("click", () => openDbModal("create", null));

//   // ---------- STATS ----------
//   const btnLoadStats = document.getElementById("btnLoadStats");
//   const statsGrid = document.getElementById("statsGrid");
//   const statsSort = document.getElementById("statsSort");
//   const statsSearch = document.getElementById("statsSearch");
//   const statsStatus = document.getElementById("statsStatus");
//   const STATS_ENDPOINT = "/api/admin/users-stats";

//   let statsRaw = [];
//   let statsFiltered = [];

//   function setStatsStatus(text, isError=false) {
//     statsStatus.textContent = text;
//     statsStatus.className = isError ? "danger" : "muted";
//   }

//   function applyStatsFilter() {
//     const q = statsSearch.value.toLowerCase().trim();
//     if (!q) statsFiltered = statsRaw.slice();
//     else {
//       statsFiltered = statsRaw.filter(u =>
//         (u.email || "").toLowerCase().includes(q) ||
//         (u.firstName || "").toLowerCase().includes(q) ||
//         (u.lastName || "").toLowerCase().includes(q)
//       );
//     }

//     const sortKey = statsSort.value;
//     statsFiltered.sort((a, b) => {
//       const tA = a.ticketsCount || 0;
//       const tB = b.ticketsCount || 0;
//       const gA = a.totalGain || 0;
//       const gB = b.totalGain || 0;
//       const dA = a.lastTicketDate ? new Date(a.lastTicketDate).getTime() : 0;
//       const dB = b.lastTicketDate ? new Date(b.lastTicketDate).getTime() : 0;

//       if (sortKey === "ticketsDesc") return tB - tA;
//       if (sortKey === "gainDesc") return gB - gA;
//       if (sortKey === "lastTicketDesc") return dB - dA;
//       return 0;
//     });

//     renderStats();
//   }

//   function renderStats() {
//     statsGrid.innerHTML = "";
//     if (!statsFiltered || statsFiltered.length === 0) {
//       statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Aucune statistique disponible.</div>";
//       return;
//     }

//     statsFiltered.forEach(u => {
//       const card = document.createElement("article");
//       card.style.borderRadius = "14px";
//       card.style.border = "1px solid #111827";
//       card.style.background = "linear-gradient(135deg,#020617,#020617,#020617)";
//       card.style.padding = "10px 12px";
//       card.style.display = "flex";
//       card.style.flexDirection = "column";
//       card.style.gap = "6px";

//       const header = document.createElement("div");
//       header.style.display = "flex";
//       header.style.justifyContent = "space-between";
//       header.style.alignItems = "center";

//       const title = document.createElement("div");
//       title.innerHTML =
//         "<div style='font-size:.9rem;font-weight:600;'>" + (u.firstName || "") + " " + (u.lastName || "") + "</div>" +
//         "<div style='font-size:.78rem;color:#9ca3af;'>" + (u.email || "") + "</div>";

//       const badge = document.createElement("span");
//       badge.textContent = u.admin ? "ADMIN" : "USER";
//       badge.style.fontSize = ".7rem";
//       badge.style.padding = "2px 8px";
//       badge.style.borderRadius = "999px";
//       badge.style.border = "1px solid " + (u.admin ? "#22c55e" : "#4b5563");
//       badge.style.color = u.admin ? "#22c55e" : "#9ca3af";

//       header.appendChild(title);
//       header.appendChild(badge);

//       const statsRow = document.createElement("div");
//       statsRow.style.display = "grid";
//       statsRow.style.gridTemplateColumns = "repeat(3,minmax(0,1fr))";
//       statsRow.style.gap = "6px";
//       statsRow.style.marginTop = "4px";

//       const blocks = [
//         { label: "Tickets", value: u.ticketsCount || 0 },
//         { label: "Gain total", value: (u.totalGain || 0) + " €" },
//         { label: "Meilleur gain", value: (u.bestGain || 0) + " €" }
//       ];

//       blocks.forEach(b => {
//         const div = document.createElement("div");
//         div.style.fontSize = ".75rem";
//         div.style.color = "#9ca3af";
//         div.innerHTML =
//           "<div>" + b.label + "</div>" +
//           "<div style='color:#e5e7eb;font-weight:600;font-size:.86rem;'>" + b.value + "</div>";
//         statsRow.appendChild(div);
//       });

//       const footer = document.createElement("div");
//       footer.style.marginTop = "4px";
//       footer.style.fontSize = ".75rem";
//       footer.style.color = "#6b7280";
//       footer.textContent = "Dernier ticket : " + (u.lastTicketDate || "—") + " | ID utilisateur : " + (u.id || u._id || "—");

//       card.appendChild(header);
//       card.appendChild(statsRow);
//       card.appendChild(footer);

//       statsGrid.appendChild(card);
//     });
//   }

//   async function loadStats() {
//     statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Chargement des statistiques...</div>";
//     setStatsStatus("Chargement...");

//     try {
//       const res = await fetch(STATS_ENDPOINT, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         const t = await res.text().catch(() => "");
//         setStatsStatus("Erreur " + res.status, true);
//         statsGrid.innerHTML =
//           "<div class='danger' style='font-size:.85rem;line-height:1.4;'>" +
//           "Endpoint stats indisponible : <code>" + STATS_ENDPOINT + "</code><br>" +
//           "Code : " + res.status + "<br>" +
//           "<span class='muted'>Crée un endpoint backend /api/admin/users-stats (DTO).</span><br>" +
//           (t ? ("<pre class='muted' style='white-space:pre-wrap;'>" + t + "</pre>") : "") +
//           "</div>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data)) {
//         setStatsStatus("Réponse invalide", true);
//         statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Réponse invalide du serveur.</div>";
//         return;
//       }

//       statsRaw = data;
//       setStatsStatus(data.length + " users");
//       applyStatsFilter();

//     } catch (e) {
//       console.error(e);
//       setStatsStatus("Erreur réseau", true);
//       statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Erreur réseau.</div>";
//     }
//   }

//   btnLoadStats.addEventListener("click", loadStats);
//   statsSort.addEventListener("change", applyStatsFilter);
//   statsSearch.addEventListener("input", applyStatsFilter);

//   // ---------- LOGOUT ----------
//   document.getElementById("btnLogout").addEventListener("click", async () => {
//     try { await fetch("/api/auth/logout", { method: "POST", credentials: "include" }); }
//     catch (e) { console.error(e); }
//     window.location.href = "/admin-login.html";
//   });

//   // ---------- INIT ----------
//   showSection("swagger");
//   loadLogFiles().then(() => refreshLogs());
//   loadDbData();
// </script>

// </body>
// </html>
// """;
//     }

//     // ---------------------------
//     // LOGS API
//     // ---------------------------

//     @GetMapping(value = "/logs/files", produces = MediaType.APPLICATION_JSON_VALUE)
//     public List<String> listLogFiles() throws IOException {
//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return List.of();
//         }

//         try (Stream<Path> s = Files.list(LOG_DIR)) {
//             return s.filter(Files::isRegularFile)
//                     .map(p -> p.getFileName().toString())
//                     .filter(this::isAllowedLogName)
//                     .sorted(Comparator.reverseOrder())
//                     .toList();
//         }
//     }

//     @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
//     public ResponseEntity<String> getLogs(
//             @RequestParam(defaultValue = "400") int lines,
//             @RequestParam(defaultValue = DEFAULT_LOG_FILE) String file
//     ) throws IOException {

//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return ResponseEntity.ok("Dossier logs introuvable : " + LOG_DIR.toAbsolutePath());
//         }

//         if (!isAllowedLogName(file)) {
//             return ResponseEntity.badRequest().body("Fichier log invalide.");
//         }

//         Path requested = LOG_DIR.resolve(file).normalize();
//         if (!requested.startsWith(LOG_DIR)) {
//             return ResponseEntity.badRequest().body("Fichier invalide.");
//         }

//         if (!Files.exists(requested) || !Files.isRegularFile(requested)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + requested.toAbsolutePath());
//         }

//         int safeLines = Math.max(1, Math.min(lines, 20000));
//         List<String> all = readAllLinesSmart(requested);

//         int fromIndex = Math.max(0, all.size() - safeLines);
//         String text = String.join("\n", all.subList(fromIndex, all.size()));

//         text = text.replace("\\n", "\n");
//         return ResponseEntity.ok(text);
//     }

//     // ---------------------------
//     // Helpers
//     // ---------------------------

//     private boolean isAllowedLogName(String name) {
//         if (name == null) return false;
//         if (name.contains("..") || name.contains("/") || name.contains("\\")) return false;

//         return name.endsWith(".log")
//                 || name.contains(".log.")
//                 || name.endsWith(".log.gz")
//                 || name.endsWith(".gz");
//     }

//     private List<String> readAllLinesSmart(Path file) throws IOException {
//         String fn = file.getFileName().toString().toLowerCase();

//         if (fn.endsWith(".gz")) {
//             try (InputStream in = Files.newInputStream(file);
//                  GZIPInputStream gz = new GZIPInputStream(in);
//                  InputStreamReader isr = new InputStreamReader(gz, StandardCharsets.UTF_8);
//                  BufferedReader br = new BufferedReader(isr)) {
//                 return br.lines().toList();
//             }
//         }

//         return Files.readAllLines(file, StandardCharsets.UTF_8);
//     }
// }


// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.Comparator;
// import java.util.List;
// import java.util.stream.Stream;
// import java.util.zip.GZIPInputStream;

// @RestController
// @RequestMapping("/admin")
// public class AdminDashboardController {

//     private static final Path LOG_DIR = Paths.get("logs");
//     private static final String DEFAULT_LOG_FILE = "application.log";

//     // ---------------------------
//     // DASHBOARD (HTML)
//     // ---------------------------
//     @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminDashboard() {
//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//   <meta charset="UTF-8">
//   <title>Dashboard admin - Loto Tracker</title>
//   <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
//   <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
//   <meta name="viewport" content="width=device-width, initial-scale=1.0">
//   <style>
//     .nav-item{
//       width:100%;display:flex;align-items:center;gap:8px;padding:8px 10px;border-radius:10px;border:none;cursor:pointer;text-align:left;
//       background:transparent;color:#9ca3af;border-left:3px solid transparent;font-size:.85rem;
//       transition:background .18s ease,color .18s ease,border-color .18s ease,transform .10s ease;
//     }
//     .nav-item:hover{background:#1e293b;color:#e5e7eb;border-left-color:#22c55e;transform:translateX(2px);}
//     .nav-item.active{background:#334155;color:#f9fafb;font-weight:600;border-left-color:#22c55e;}
//     .cell-date{font-variant-numeric:tabular-nums;white-space:nowrap;}
//     .pill{padding:5px 10px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;font-size:.8rem;}
//     .pill-green{border:none;background:#22c55e;color:#020617;font-weight:700;}
//     .input{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:4px 10px;font-size:.8rem;}
//     .select{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:3px 8px;}
//     .card{border-radius:12px;border:1px solid #111827;background:#020617;}
//     .muted{color:#9ca3af;}
//     .danger{color:#f97373;}
//     .ok{color:#22c55e;}
//     code{background:#0b1220;border:1px solid #111827;border-radius:8px;padding:2px 6px;}
//     .hint{color:#9ca3af;font-size:.8rem;line-height:1.4;}
//   </style>
// </head>

// <body style="background:#020617;color:#e5e7eb;font-family:system-ui,sans-serif;margin:0;">
// <div style="min-height:100vh;display:flex;">

//   <aside style="width:240px;background:#020617;border-right:1px solid #111827;
//                 padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
//                 position:sticky;top:0;align-self:flex-start;height:100vh;">
//     <div>
//       <div style="font-size:.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">Loto Tracker API</div>
//       <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">Admin console</div>
//     </div>

//     <nav style="display:flex;flex-direction:column;gap:6px;font-size:.85rem;">
//       <button class="nav-item active" data-section="swagger"><span>*</span><span>Swagger / API</span></button>
//       <button class="nav-item" data-section="logs"><span>*</span><span>Logs serveur</span></button>
//       <button class="nav-item" data-section="db"><span>*</span><span>Base de données (CRUD)</span></button>
//       <button class="nav-item" data-section="stats"><span>*</span><span>Stats joueurs & tickets</span></button>
//     </nav>

//     <div style="margin-top:auto;font-size:.75rem;color:#6b7280;line-height:1.4;">
//       Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
//       Profil Spring : <span style="color:#22c55e;">dev</span>
//     </div>
//   </aside>

//   <div style="flex:1;display:flex;flex-direction:column;min-width:0;">
//     <header style="padding:14px 20px;border-bottom:1px solid #111827;display:flex;justify-content:space-between;align-items:center;">
//       <div>
//         <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
//         <p id="pageSubtitle" style="margin:2px 0 0;font-size:.8rem;color:#9ca3af;">
//           Vue générale : Swagger, logs serveur, base de données et statistiques.
//         </p>
//       </div>
//       <button id="btnLogout" type="button" class="pill">Déconnexion</button>
//     </header>

//     <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

//       <!-- SWAGGER -->
//       <section id="section-swagger" class="section-panel" style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
//         <p class="muted" style="margin:0 0 4px;font-size:.85rem;">Documentation Swagger de l'API Loto Tracker.</p>
//         <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
//           <button id="btnOpenSwagger" type="button" class="pill pill-green">Ouvrir Swagger dans un nouvel onglet</button>
//           <span style="font-size:.8rem;color:#6b7280;">ou utilisez la vue intégrée ci-dessous.</span>
//         </div>
//         <div style="flex:1;min-height:0;margin-top:8px;">
//           <iframe id="swaggerFrame" src="/swagger-ui/index.html"
//                   style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;background:#020617;"></iframe>
//         </div>
//       </section>

//       <!-- LOGS -->
//       <section id="section-logs" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">
//         <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:.8rem;">
//           <span class="muted">Logs du backend (lecture seule).</span>

//           <label class="muted">
//             Fichier :
//             <select id="logFileSelect" class="select" style="min-width:240px;">
//               <option>Chargement...</option>
//             </select>
//           </label>

//           <button id="btnRefreshLogs" type="button" class="pill pill-green">🔄 Rafraîchir</button>

//           <label class="muted">
//             Lignes :
//             <select id="logLineCount" class="select">
//               <option value="200">200</option>
//               <option value="400" selected>400</option>
//               <option value="800">800</option>
//               <option value="1500">1500</option>
//             </select>
//           </label>

//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoScrollLogs" checked> Auto-scroll
//           </label>
//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoRefreshLogs" checked> Auto-refresh (3s)
//           </label>

//           <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
//         </div>

//         <div id="logsContainer"
//              style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
//                     background:#020617;padding:10px;font-family:monospace;font-size:11px;white-space:pre-wrap;">
//           Chargement des logs...
//         </div>
//       </section>

//       <!-- DB CRUD -->
//       <section id="section-db" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Vue base de données (CRUD simplifié).</span>

//           <label class="muted">
//             Table :
//             <select id="dbResourceSelect" class="select">
//               <option value="tickets">public.tickets</option>
//               <option value="users">public.users</option>
//               <option value="ticket_gains">public.ticket_gains</option>
//               <option value="refresh_tokens">public.refresh_tokens</option>
//             </select>
//           </label>

//           <button id="btnLoadData" type="button" class="pill pill-green">Charger</button>
//           <button id="btnNewRow" type="button" class="pill">➕ Nouvelle ligne</button>

//           <span id="dbInfoText" style="color:#6b7280;font-size:.8rem;">
//             Champs sensibles masqués (RGPD). refresh_tokens = lecture seule.
//           </span>
//         </div>

//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;font-size:.8rem;">
//           <label class="muted">
//             Recherche :
//             <input id="dbSearch" type="text" placeholder="Texte à filtrer..." class="input">
//           </label>

//           <label class="muted">
//             Lignes / page :
//             <select id="dbPageSize" class="select">
//               <option value="20" selected>20</option>
//               <option value="50">50</option>
//               <option value="100">100</option>
//             </select>
//           </label>

//           <span id="dbPagingInfo" style="color:#6b7280;">Page 1 / 1</span>

//           <div style="display:flex;gap:4px;">
//             <button id="dbPrevPage" type="button" class="pill" style="font-size:.75rem;">◀</button>
//             <button id="dbNextPage" type="button" class="pill" style="font-size:.75rem;">▶</button>
//           </div>

//           <span id="dbStatus" style="color:#6b7280;"></span>
//         </div>

//         <div id="dbTableWrapper" class="card" style="flex:1;min-height:0;overflow:auto;">
//           <table id="dbTable" style="border-collapse:collapse;width:100%;font-size:.8rem;">
//             <thead id="dbTableHead"></thead>
//             <tbody id="dbTableBody"></tbody>
//           </table>
//         </div>

//         <!-- MODAL -->
//         <div id="dbModalOverlay"
//              style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.7);
//                     align-items:center;justify-content:center;z-index:40;">
//           <div style="background:#020617;border-radius:16px;border:1px solid #111827;
//                       padding:16px 18px;width:420px;max-width:95%;">
//             <h2 id="dbModalTitle" style="margin:0 0 8px;font-size:1rem;">Nouvelle ligne</h2>
//             <form id="dbModalForm" style="display:flex;flex-direction:column;gap:8px;max-height:60vh;overflow:auto;"></form>
//             <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;">
//               <button id="dbModalCancel" type="button" class="pill">Annuler</button>
//               <button id="dbModalSave" type="button" class="pill pill-green">Enregistrer</button>
//             </div>
//           </div>
//         </div>
//       </section>

//       <!-- STATS -->
//       <section id="section-stats" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Analyse des joueurs et de leurs tickets.</span>
//           <button id="btnLoadStats" type="button" class="pill pill-green">Recharger les stats</button>

//           <label class="muted">
//             Tri :
//             <select id="statsSort" class="select">
//               <option value="ticketsDesc">Nb tickets ↓</option>
//               <option value="gainDesc">Gain total ↓</option>
//               <option value="lastTicketDesc">Dernier ticket ↓</option>
//             </select>
//           </label>

//           <label class="muted">
//             Filtrer par email :
//             <input id="statsSearch" type="text" placeholder="user@hbnb.com" class="input">
//           </label>

//           <span id="statsStatus" style="color:#6b7280;"></span>
//         </div>

//         <div id="statsGrid"
//              style="flex:1;min-height:0;overflow:auto;display:grid;
//                     grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
//                     gap:10px;padding-top:4px;"></div>
//       </section>

//     </main>
//   </div>
// </div>

// <script>
//   const DEFAULT_LOG_FILE = "application.log";

//   // JWT en cookie HttpOnly => on ne peut pas le lire en JS.
//   // Il suffit d'envoyer credentials: "include" pour que le cookie parte vers l'API.
//   function getAuthHeaders() { return {}; }

//   // ✅ RGPD/Sécurité : champs à masquer même en admin
//   const SENSITIVE_PARTS = [
//     "password","hashed","hash",
//     "token","refresh","access",
//     "secret","apikey","api_key",
//     "session","cookie","reset"
//   ];

//   function isSensitiveKey(key) {
//     if (!key) return false;
//     const k = String(key).toLowerCase();
//     return SENSITIVE_PARTS.some(p => k.includes(p));
//   }

//   function maskValue(v) {
//     if (v === null || v === undefined || v === "") return "";
//     const s = String(v);
//     if (s.length <= 6) return "••••••";
//     return s.slice(0, 3) + "••••••" + s.slice(-3);
//   }

//   function safeCellValue(key, value) {
//     if (value === null || value === undefined) return "";

//     if (isSensitiveKey(key)) return maskValue(value);

//     if (Array.isArray(value)) {
//       if (String(key).toLowerCase().includes("tickets")) {
//         return "[array] (" + value.length + ")";
//       }
//       return JSON.stringify(value);
//     }

//     if (typeof value === "object") return JSON.stringify(value);

//     return String(value);
//   }

//   // ---------- NAV ----------
//   const navItems = document.querySelectorAll(".nav-item");
//   const sections = {
//     swagger: document.getElementById("section-swagger"),
//     logs: document.getElementById("section-logs"),
//     db: document.getElementById("section-db"),
//     stats: document.getElementById("section-stats")
//   };
//   const pageTitle = document.getElementById("pageTitle");
//   const pageSubtitle = document.getElementById("pageSubtitle");

//   const subtitles = {
//     swagger: "Documentation Swagger et tests de l’API.",
//     logs: "Suivi en temps réel des logs Spring Boot (fichiers + .gz).",
//     db: "Exploration et modification des tables principales (CRUD).",
//     stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
//   };

//   function showSection(key) {
//     Object.keys(sections).forEach(k => sections[k].style.display = (k === key) ? "flex" : "none");

//     navItems.forEach(btn => {
//       if (btn.getAttribute("data-section") === key) {
//         btn.classList.add("active");
//         btn.style.background = "#111827";
//         btn.style.color = "#e5e7eb";
//       } else {
//         btn.classList.remove("active");
//         btn.style.background = "transparent";
//         btn.style.color = "#9ca3af";
//       }
//     });

//     pageTitle.textContent = "Tableau de bord administrateur";
//     pageSubtitle.textContent = subtitles[key] || "";

//     if (key === "logs") loadLogFiles().then(() => refreshLogs());
//     if (key === "stats") loadStats();
//   }

//   navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.getAttribute("data-section"))));

//   // ---------- SWAGGER ----------
//   document.getElementById("btnOpenSwagger").addEventListener("click", () => window.open("/swagger-ui/index.html", "_blank"));

//   // ---------- LOGS ----------
//   const logsContainer = document.getElementById("logsContainer");
//   const btnRefreshLogs = document.getElementById("btnRefreshLogs");
//   const logLineCountSelect = document.getElementById("logLineCount");
//   const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
//   const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
//   const logsLastRefresh = document.getElementById("logsLastRefresh");
//   const logFileSelect = document.getElementById("logFileSelect");

//   async function loadLogFiles() {
//     try {
//       const res = await fetch("/admin/logs/files", {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur " + res.status + "</option>";
//         return;
//       }

//       const files = await res.json();
//       logFileSelect.innerHTML = "";

//       if (!Array.isArray(files) || files.length === 0) {
//         const o = document.createElement("option");
//         o.value = DEFAULT_LOG_FILE;
//         o.textContent = "Aucun fichier (logs/)";
//         logFileSelect.appendChild(o);
//         return;
//       }

//       let foundDefault = false;
//       files.forEach(f => {
//         const o = document.createElement("option");
//         o.value = f;
//         o.textContent = f;
//         if (f === DEFAULT_LOG_FILE) { o.selected = true; foundDefault = true; }
//         logFileSelect.appendChild(o);
//       });

//       if (!foundDefault) logFileSelect.value = files[0];

//     } catch (e) {
//       console.error(e);
//       logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur réseau</option>";
//     }
//   }

//   async function refreshLogs() {
//     const lines = logLineCountSelect.value;
//     const file = logFileSelect.value || DEFAULT_LOG_FILE;

//     try {
//       const res = await fetch(`/admin/logs?lines=${encodeURIComponent(lines)}&file=${encodeURIComponent(file)}`, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       const text = await res.text();
//       logsContainer.textContent = text || "(Aucun log pour le moment)";

//       const now = new Date();
//       logsLastRefresh.textContent = "Dernier refresh : " + now.toLocaleTimeString("fr-FR");

//       if (autoScrollLogsCheckbox.checked) logsContainer.scrollTop = logsContainer.scrollHeight;

//     } catch (e) {
//       console.error(e);
//       logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//     }
//   }

//   btnRefreshLogs.addEventListener("click", refreshLogs);
//   logLineCountSelect.addEventListener("change", refreshLogs);
//   logFileSelect.addEventListener("change", refreshLogs);
//   setInterval(() => { if (autoRefreshLogsCheckbox.checked) refreshLogs(); }, 3000);

//   // ---------- DB / CRUD ----------
//   const dbResourceSelect = document.getElementById("dbResourceSelect");
//   const btnLoadData = document.getElementById("btnLoadData");
//   const btnNewRow = document.getElementById("btnNewRow");
//   const dbTableHead = document.getElementById("dbTableHead");
//   const dbTableBody = document.getElementById("dbTableBody");
//   const dbSearch = document.getElementById("dbSearch");
//   const dbPageSize = document.getElementById("dbPageSize");
//   const dbPagingInfo = document.getElementById("dbPagingInfo");
//   const dbPrevPage = document.getElementById("dbPrevPage");
//   const dbNextPage = document.getElementById("dbNextPage");
//   const dbModalOverlay = document.getElementById("dbModalOverlay");
//   const dbModalTitle = document.getElementById("dbModalTitle");
//   const dbModalForm = document.getElementById("dbModalForm");
//   const dbModalCancel = document.getElementById("dbModalCancel");
//   const dbModalSave = document.getElementById("dbModalSave");
//   const dbStatus = document.getElementById("dbStatus");

//   const DB_ENDPOINTS = {
//     users: "/api/admin/users",
//     tickets: "/api/admin/tickets",
//     ticket_gains: "/api/admin/ticket-gains",
//     ticket_gain: "/api/admin/ticket-gains",
//     refresh_tokens: "/api/admin/refresh-tokens"
//   };

//   const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);

//   const COLUMN_ORDERS = {
//     refresh_tokens: ["id","user_id","userId","token_hash","tokenHash","created_at","createdAt","expires_at","expiresAt","revoked","revoked_at","revokedAt"],
//     users: ["id","email","role","admin","firstName","lastName","createdAt","updatedAt","created_at","updated_at","password"],
//     tickets: ["id","userId","user_id","drawDate","draw_date","createdAt","updatedAt","created_at","updated_at"],
//     ticket_gains: ["id","ticket_id","ticketId","matching_numbers","matchingNumbers","lucky_number_match","luckyNumberMatch","gain_amount","gainAmount","createdAt","created_at"],
//     ticket_gain: ["id","ticket_id","ticketId","matching_numbers","matchingNumbers","lucky_number_match","luckyNumberMatch","gain_amount","gainAmount","createdAt","created_at"]
//   };

//   const SORT_FIELDS = {
//     refresh_tokens: ["created_at","createdAt","expires_at","expiresAt"],
//     users: ["updated_at","updatedAt","created_at","createdAt"],
//     tickets: ["updated_at","updatedAt","created_at","createdAt","drawDate","draw_date"],
//     ticket_gains: ["created_at","createdAt"],
//     ticket_gain: ["created_at","createdAt"]
//   };

//   let dbCurrentResource = "users";
//   let dbRawData = [];
//   let dbFilteredData = [];
//   let dbCurrentPage = 0;
//   let dbEditingRow = null;

//   function isDateKey(key) {
//     if (!key) return false;
//     const k = key.toLowerCase();
//     return k.endsWith("at") || k.endsWith("_at") || k.includes("date");
//   }

//   function formatDateValue(value) {
//     if (value === null || value === undefined || value === "") return "";

//     if (Array.isArray(value) && value.length >= 3) {
//       const y = value[0] || 1970;
//       const m = (value[1] || 1) - 1;
//       const d = value[2] || 1;
//       const hh = value[3] || 0;
//       const mm = value[4] || 0;
//       const ss = value[5] || 0;
//       const dt = new Date(y, m, d, hh, mm, ss);
//       if (!Number.isNaN(dt.getTime())) return dt.toLocaleString("fr-FR");
//     }

//     if (typeof value === "string") {
//       const t = Date.parse(value);
//       if (!Number.isNaN(t)) return new Date(t).toLocaleString("fr-FR");
//       return value;
//     }

//     if (typeof value === "number") {
//       const ms = (value > 1e12) ? value : (value * 1000);
//       const d = new Date(ms);
//       if (!Number.isNaN(d.getTime())) return d.toLocaleString("fr-FR");
//     }

//     return String(value);
//   }

//   function getSortMs(row) {
//     const fields = SORT_FIELDS[dbCurrentResource] || ["created_at","createdAt","updated_at","updatedAt"];
//     for (const f of fields) {
//       const v = row?.[f];
//       if (v === null || v === undefined || v === "") continue;

//       if (Array.isArray(v) && v.length >= 3) {
//         const y = v[0] || 1970;
//         const m = (v[1] || 1) - 1;
//         const d = v[2] || 1;
//         const hh = v[3] || 0;
//         const mm = v[4] || 0;
//         const ss = v[5] || 0;
//         return new Date(y, m, d, hh, mm, ss).getTime();
//       }

//       if (typeof v === "string") {
//         const t = Date.parse(v);
//         if (!Number.isNaN(t)) return t;
//       }

//       if (typeof v === "number") return (v > 1e12) ? v : v * 1000;
//     }
//     return 0;
//   }

//   function getColumns(pageData) {
//     if (!pageData || pageData.length === 0) return [];
//     const existing = new Set();
//     pageData.forEach(r => Object.keys(r || {}).forEach(k => existing.add(k)));

//     const preferred = COLUMN_ORDERS[dbCurrentResource];
//     let cols = [];

//     if (preferred && preferred.length) {
//       cols = preferred.filter(k => existing.has(k));
//       const rest = Array.from(existing).filter(k => !cols.includes(k));
//       cols = cols.concat(rest);
//     } else {
//       cols = Object.keys(pageData[0]);
//     }
//     return cols;
//   }

//   function applyDbFilter() {
//     const q = dbSearch.value.toLowerCase().trim();
//     if (!q) dbFilteredData = dbRawData.slice();
//     else dbFilteredData = dbRawData.filter(row => JSON.stringify(row).toLowerCase().includes(q));

//     dbFilteredData.sort((a, b) => getSortMs(b) - getSortMs(a));
//     dbCurrentPage = 0;
//     renderDbTable();
//   }

//   function setDbStatus(text, isError=false) {
//     dbStatus.textContent = text;
//     dbStatus.className = isError ? "danger" : "muted";
//   }

//   function authHintHtml() {
//     return `
//       <div class="hint">
//         <b>Astuce cookie :</b> si tu t'es loggé via <code>127.0.0.1</code> mais tu ouvres le dashboard en <code>localhost</code>,
//         le cookie <code>jwtToken</code> peut ne pas partir → 401.<br>
//         Utilise le même host partout (tout <code>localhost</code> OU tout <code>127.0.0.1</code>).
//       </div>`;
//   }

//   function renderDbTable() {
//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "";

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
//     btnNewRow.disabled = isReadOnly;
//     btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//     btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//     if (!dbFilteredData || dbFilteredData.length === 0) {
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
//       dbPagingInfo.textContent = "Page 1 / 1";
//       return;
//     }

//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage >= totalPages) dbCurrentPage = totalPages - 1;

//     const start = dbCurrentPage * pageSize;
//     const end = Math.min(start + pageSize, dbFilteredData.length);
//     const pageData = dbFilteredData.slice(start, end);

//     const cols = getColumns(pageData);

//     const headerRow = document.createElement("tr");
//     cols.forEach(k => {
//       const th = document.createElement("th");
//       th.textContent = k;
//       th.style.padding = "6px 8px";
//       th.style.borderBottom = "1px solid #111827";
//       th.style.textAlign = "left";
//       headerRow.appendChild(th);
//     });

//     if (!isReadOnly) {
//       const thActions = document.createElement("th");
//       thActions.textContent = "Actions";
//       thActions.style.padding = "6px 8px";
//       thActions.style.borderBottom = "1px solid #111827";
//       headerRow.appendChild(thActions);
//     }

//     dbTableHead.appendChild(headerRow);

//     pageData.forEach(row => {
//       const tr = document.createElement("tr");
//       tr.style.borderBottom = "1px solid #020617";

//       cols.forEach(k => {
//         const td = document.createElement("td");
//         const value = row[k];

//         if (isDateKey(k)) {
//           td.textContent = formatDateValue(value);
//           td.className = "cell-date";
//         } else {
//           td.textContent = safeCellValue(k, value);
//         }

//         td.style.padding = "6px 8px";
//         td.style.verticalAlign = "top";
//         td.style.maxWidth = "320px";
//         td.style.wordBreak = "break-word";
//         tr.appendChild(td);
//       });

//       if (!isReadOnly) {
//         const tdActions = document.createElement("td");
//         tdActions.style.padding = "6px 8px";
//         tdActions.style.whiteSpace = "nowrap";

//         const id = row.id || row._id;

//         const btnEdit = document.createElement("button");
//         btnEdit.textContent = "Modifier";
//         btnEdit.style.padding = "3px 8px";
//         btnEdit.style.borderRadius = "999px";
//         btnEdit.style.border = "none";
//         btnEdit.style.fontSize = "0.75rem";
//         btnEdit.style.background = "#3b82f6";
//         btnEdit.style.color = "#020617";
//         btnEdit.style.cursor = "pointer";
//         btnEdit.style.marginRight = "4px";
//         btnEdit.addEventListener("click", () => openDbModal("edit", row));

//         const btnDelete = document.createElement("button");
//         btnDelete.textContent = "Supprimer";
//         btnDelete.style.padding = "3px 8px";
//         btnDelete.style.borderRadius = "999px";
//         btnDelete.style.border = "none";
//         btnDelete.style.fontSize = "0.75rem";
//         btnDelete.style.background = "#ef4444";
//         btnDelete.style.color = "#020617";
//         btnDelete.style.cursor = "pointer";

//         btnDelete.addEventListener("click", async () => {
//           if (!id) { alert("ID introuvable."); return; }
//           if (!confirm("Supprimer " + dbCurrentResource + " #" + id + " ?")) return;

//           const url = DB_ENDPOINTS[dbCurrentResource] + "/" + id;
//           try {
//             const delRes = await fetch(url, {
//               method: "DELETE",
//               headers: { ...getAuthHeaders() },
//               credentials: "include"
//             });
//             if (!delRes.ok) {
//               const t = await delRes.text().catch(() => "");
//               alert("Erreur " + delRes.status + " lors de la suppression.\\n" + t);
//               return;
//             }
//             dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
//             applyDbFilter();
//           } catch (e) {
//             console.error(e);
//             alert("Erreur réseau lors de la suppression.");
//           }
//         });

//         tdActions.appendChild(btnEdit);
//         tdActions.appendChild(btnDelete);
//         tr.appendChild(tdActions);
//       }

//       dbTableBody.appendChild(tr);
//     });

//     dbPagingInfo.textContent = "Page " + (dbCurrentPage + 1) + " / " + totalPages;
//   }

//   async function loadDbData() {
//     dbCurrentResource = dbResourceSelect.value;

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
//     btnNewRow.disabled = isReadOnly;
//     btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//     btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//     const url = DB_ENDPOINTS[dbCurrentResource];

//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";
//     setDbStatus("Chargement...");

//     if (!url) {
//       setDbStatus("Endpoint non configuré", true);
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Endpoint non configuré.</td></tr>";
//       return;
//     }

//     try {
//       const res = await fetch(url, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         const txt = await res.text().catch(() => "");
//         setDbStatus("Erreur " + res.status, true);

//         let extra = "";
//         if (res.status === 401 || res.status === 403) extra = authHintHtml();

//         dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>" +
//           "Erreur " + res.status + ". " + (txt || "") + "<br>" + extra +
//           "</td></tr>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data) || data.length === 0) {
//         dbRawData = [];
//         setDbStatus("0 ligne");
//         applyDbFilter();
//         return;
//       }

//       dbRawData = data;
//       setDbStatus(data.length + " lignes");
//       applyDbFilter();

//     } catch (e) {
//       console.error(e);
//       setDbStatus("Erreur réseau", true);
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur réseau.</td></tr>";
//     }
//   }

//   btnLoadData.addEventListener("click", loadDbData);
//   dbSearch.addEventListener("input", () => applyDbFilter());
//   dbPageSize.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
//   dbPrevPage.addEventListener("click", () => { if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); } });
//   dbNextPage.addEventListener("click", () => {
//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
//   });

//   function openDbModal(mode, row) {
//     if (READ_ONLY_RESOURCES.has(dbCurrentResource)) {
//       alert("Cette table est en lecture seule (données sensibles).");
//       return;
//     }

//     dbEditingRow = { resource: dbCurrentResource, mode, row: row || null };
//     dbModalForm.innerHTML = "";

//     const baseRow = row || (dbRawData[0] || {});
//     const keys = Object.keys(baseRow);

//     const protectedFields = ["id","_id","created_at","updated_at","createdAt","updatedAt"];

//     keys.forEach(k => {
//       const wrapper = document.createElement("div");
//       wrapper.style.display = "flex";
//       wrapper.style.flexDirection = "column";
//       wrapper.style.gap = "2px";

//       const label = document.createElement("label");
//       label.textContent = k + (isSensitiveKey(k) ? " (masqué)" : "");
//       label.style.fontSize = "0.8rem";
//       label.style.color = "#9ca3af";

//       const input = document.createElement("input");
//       input.name = k;
//       input.value = row ? (row[k] ?? "") : "";
//       input.style.background = "#020617";
//       input.style.color = "#e5e7eb";
//       input.style.borderRadius = "8px";
//       input.style.border = "1px solid #4b5563";
//       input.style.padding = "6px 8px";
//       input.style.fontSize = "0.8rem";

//       if (protectedFields.includes(k) || isSensitiveKey(k)) {
//         input.disabled = true;
//         input.style.opacity = "0.6";
//         if (isSensitiveKey(k)) input.value = maskValue(input.value);
//       }

//       wrapper.appendChild(label);
//       wrapper.appendChild(input);
//       dbModalForm.appendChild(wrapper);
//     });

//     dbModalTitle.textContent = (mode === "edit")
//       ? "Modifier la ligne #" + (row.id || row._id)
//       : "Nouvelle ligne";

//     dbModalOverlay.style.display = "flex";
//   }

//   function closeDbModal() {
//     dbModalOverlay.style.display = "none";
//     dbEditingRow = null;
//   }

//   dbModalCancel.addEventListener("click", closeDbModal);

//   dbModalSave.addEventListener("click", async () => {
//     if (!dbEditingRow) return;

//     const formData = new FormData(dbModalForm);
//     const obj = {};

//     formData.forEach((v, k) => {
//       const input = dbModalForm.querySelector('input[name="' + k + '"]');
//       if (input && input.disabled) return;
//       obj[k] = v === "" ? null : v;
//     });

//     const resource = dbEditingRow.resource;
//     const baseUrl = DB_ENDPOINTS[resource];
//     let method = "POST";
//     let url = baseUrl;

//     if (dbEditingRow.mode === "edit") {
//       const id = dbEditingRow.row.id || dbEditingRow.row._id;
//       method = "PUT";
//       url = baseUrl + "/" + id;
//     }

//     try {
//       const res = await fetch(url, {
//         method,
//         headers: { "Content-Type": "application/json", ...getAuthHeaders() },
//         credentials: "include",
//         body: JSON.stringify(obj)
//       });

//       if (!res.ok) {
//         const t = await res.text().catch(() => "");
//         alert("Erreur " + res.status + " lors de l’enregistrement.\\n" + t);
//         return;
//       }

//       closeDbModal();
//       await loadDbData();

//     } catch (e) {
//       console.error(e);
//       alert("Erreur réseau lors de l’enregistrement.");
//     }
//   });

//   btnNewRow.addEventListener("click", () => openDbModal("create", null));

//   // ---------- STATS ----------
//   const btnLoadStats = document.getElementById("btnLoadStats");
//   const statsGrid = document.getElementById("statsGrid");
//   const statsSort = document.getElementById("statsSort");
//   const statsSearch = document.getElementById("statsSearch");
//   const statsStatus = document.getElementById("statsStatus");
//   const STATS_ENDPOINT = "/api/admin/users-stats";

//   let statsRaw = [];
//   let statsFiltered = [];

//   function setStatsStatus(text, isError=false) {
//     statsStatus.textContent = text;
//     statsStatus.className = isError ? "danger" : "muted";
//   }

//   function applyStatsFilter() {
//     const q = statsSearch.value.toLowerCase().trim();
//     if (!q) statsFiltered = statsRaw.slice();
//     else {
//       statsFiltered = statsRaw.filter(u =>
//         (u.email || "").toLowerCase().includes(q) ||
//         (u.firstName || "").toLowerCase().includes(q) ||
//         (u.lastName || "").toLowerCase().includes(q)
//       );
//     }

//     const sortKey = statsSort.value;
//     statsFiltered.sort((a, b) => {
//       const tA = a.ticketsCount || 0;
//       const tB = b.ticketsCount || 0;
//       const gA = a.totalGain || 0;
//       const gB = b.totalGain || 0;
//       const dA = a.lastTicketDate ? new Date(a.lastTicketDate).getTime() : 0;
//       const dB = b.lastTicketDate ? new Date(b.lastTicketDate).getTime() : 0;

//       if (sortKey === "ticketsDesc") return tB - tA;
//       if (sortKey === "gainDesc") return gB - gA;
//       if (sortKey === "lastTicketDesc") return dB - dA;
//       return 0;
//     });

//     renderStats();
//   }

//   function renderStats() {
//     statsGrid.innerHTML = "";
//     if (!statsFiltered || statsFiltered.length === 0) {
//       statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Aucune statistique disponible.</div>";
//       return;
//     }

//     statsFiltered.forEach(u => {
//       const card = document.createElement("article");
//       card.style.borderRadius = "14px";
//       card.style.border = "1px solid #111827";
//       card.style.background = "linear-gradient(135deg,#020617,#020617,#020617)";
//       card.style.padding = "10px 12px";
//       card.style.display = "flex";
//       card.style.flexDirection = "column";
//       card.style.gap = "6px";

//       const header = document.createElement("div");
//       header.style.display = "flex";
//       header.style.justifyContent = "space-between";
//       header.style.alignItems = "center";

//       const title = document.createElement("div");
//       title.innerHTML =
//         "<div style='font-size:.9rem;font-weight:600;'>" + (u.firstName || "") + " " + (u.lastName || "") + "</div>" +
//         "<div style='font-size:.78rem;color:#9ca3af;'>" + (u.email || "") + "</div>";

//       const badge = document.createElement("span");
//       badge.textContent = u.admin ? "ADMIN" : "USER";
//       badge.style.fontSize = ".7rem";
//       badge.style.padding = "2px 8px";
//       badge.style.borderRadius = "999px";
//       badge.style.border = "1px solid " + (u.admin ? "#22c55e" : "#4b5563");
//       badge.style.color = u.admin ? "#22c55e" : "#9ca3af";

//       header.appendChild(title);
//       header.appendChild(badge);

//       const statsRow = document.createElement("div");
//       statsRow.style.display = "grid";
//       statsRow.style.gridTemplateColumns = "repeat(3,minmax(0,1fr))";
//       statsRow.style.gap = "6px";
//       statsRow.style.marginTop = "4px";

//       const blocks = [
//         { label: "Tickets", value: u.ticketsCount || 0 },
//         { label: "Gain total", value: (u.totalGain || 0) + " €" },
//         { label: "Meilleur gain", value: (u.bestGain || 0) + " €" }
//       ];

//       blocks.forEach(b => {
//         const div = document.createElement("div");
//         div.style.fontSize = ".75rem";
//         div.style.color = "#9ca3af";
//         div.innerHTML =
//           "<div>" + b.label + "</div>" +
//           "<div style='color:#e5e7eb;font-weight:600;font-size:.86rem;'>" + b.value + "</div>";
//         statsRow.appendChild(div);
//       });

//       const footer = document.createElement("div");
//       footer.style.marginTop = "4px";
//       footer.style.fontSize = ".75rem";
//       footer.style.color = "#6b7280";
//       footer.textContent = "Dernier ticket : " + (u.lastTicketDate || "—") + " | ID utilisateur : " + (u.id || u._id || "—");

//       card.appendChild(header);
//       card.appendChild(statsRow);
//       card.appendChild(footer);

//       statsGrid.appendChild(card);
//     });
//   }

//   async function loadStats() {
//     statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Chargement des statistiques...</div>";
//     setStatsStatus("Chargement...");

//     try {
//       const res = await fetch(STATS_ENDPOINT, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         const t = await res.text().catch(() => "");
//         setStatsStatus("Erreur " + res.status, true);

//         let extra = "";
//         if (res.status === 401 || res.status === 403) extra = authHintHtml();

//         statsGrid.innerHTML =
//           "<div class='danger' style='font-size:.85rem;line-height:1.4;'>" +
//           "Endpoint stats indisponible : <code>" + STATS_ENDPOINT + "</code><br>" +
//           "Code : " + res.status + "<br>" +
//           extra +
//           (t ? ("<pre class='muted' style='white-space:pre-wrap;'>" + t + "</pre>") : "") +
//           "</div>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data)) {
//         setStatsStatus("Réponse invalide", true);
//         statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Réponse invalide du serveur.</div>";
//         return;
//       }

//       statsRaw = data;
//       setStatsStatus(data.length + " users");
//       applyStatsFilter();

//     } catch (e) {
//       console.error(e);
//       setStatsStatus("Erreur réseau", true);
//       statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Erreur réseau.</div>";
//     }
//   }

//   btnLoadStats.addEventListener("click", loadStats);
//   statsSort.addEventListener("change", applyStatsFilter);
//   statsSearch.addEventListener("input", applyStatsFilter);

//   // ---------- LOGOUT ----------
//   document.getElementById("btnLogout").addEventListener("click", async () => {
//     try { await fetch("/api/auth/logout", { method: "POST", credentials: "include" }); }
//     catch (e) { console.error(e); }
//     window.location.href = "/admin-login.html";
//   });

//   // ---------- INIT ----------
//   showSection("swagger");
//   loadLogFiles().then(() => refreshLogs());
//   loadDbData();
// </script>

// </body>
// </html>
// """;
//     }

//     // ---------------------------
//     // LOGS API
//     // ---------------------------

//     @GetMapping(value = "/logs/files", produces = MediaType.APPLICATION_JSON_VALUE)
//     public List<String> listLogFiles() throws IOException {
//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return List.of();
//         }

//         try (Stream<Path> s = Files.list(LOG_DIR)) {
//             return s.filter(Files::isRegularFile)
//                     .map(p -> p.getFileName().toString())
//                     .filter(this::isAllowedLogName)
//                     .sorted(Comparator.reverseOrder())
//                     .toList();
//         }
//     }

//     @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
//     public ResponseEntity<String> getLogs(
//             @RequestParam(defaultValue = "400") int lines,
//             @RequestParam(defaultValue = DEFAULT_LOG_FILE) String file
//     ) throws IOException {

//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return ResponseEntity.ok("Dossier logs introuvable : " + LOG_DIR.toAbsolutePath());
//         }

//         if (!isAllowedLogName(file)) {
//             return ResponseEntity.badRequest().body("Fichier log invalide.");
//         }

//         Path requested = LOG_DIR.resolve(file).normalize();
//         if (!requested.startsWith(LOG_DIR)) {
//             return ResponseEntity.badRequest().body("Fichier invalide.");
//         }

//         if (!Files.exists(requested) || !Files.isRegularFile(requested)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + requested.toAbsolutePath());
//         }

//         int safeLines = Math.max(1, Math.min(lines, 20000));
//         List<String> all = readAllLinesSmart(requested);

//         int fromIndex = Math.max(0, all.size() - safeLines);
//         String text = String.join("\n", all.subList(fromIndex, all.size()));

//         // Si le log contient "\\n" (2 chars), on les convertit en vrais retours à la ligne
//         text = text.replace("\\\\n", "\n");

//         return ResponseEntity.ok(text);
//     }

//     // ---------------------------
//     // Helpers
//     // ---------------------------

//     private boolean isAllowedLogName(String name) {
//         if (name == null) return false;
//         if (name.contains("..") || name.contains("/") || name.contains("\\")) return false;

//         return name.endsWith(".log")
//                 || name.contains(".log.")
//                 || name.endsWith(".log.gz")
//                 || name.endsWith(".gz");
//     }

//     private List<String> readAllLinesSmart(Path file) throws IOException {
//         String fn = file.getFileName().toString().toLowerCase();

//         if (fn.endsWith(".gz")) {
//             try (InputStream in = Files.newInputStream(file);
//                  GZIPInputStream gz = new GZIPInputStream(in);
//                  InputStreamReader isr = new InputStreamReader(gz, StandardCharsets.UTF_8);
//                  BufferedReader br = new BufferedReader(isr)) {
//                 return br.lines().toList();
//             }
//         }

//         return Files.readAllLines(file, StandardCharsets.UTF_8);
//     }
// }


// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.Comparator;
// import java.util.List;
// import java.util.stream.Stream;
// import java.util.zip.GZIPInputStream;

// @RestController
// @RequestMapping("/admin")
// public class AdminDashboardController {

//     private static final Path LOG_DIR = Paths.get("logs");
//     private static final String DEFAULT_LOG_FILE = "application.log";

//     // ---------------------------
//     // DASHBOARD (HTML)
//     // ---------------------------
//     @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminDashboard() {
//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//   <meta charset="UTF-8">
//   <title>Dashboard admin - Loto Tracker</title>
//   <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
//   <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
//   <meta name="viewport" content="width=device-width, initial-scale=1.0">
//   <style>
//     .nav-item{
//       width:100%;display:flex;align-items:center;gap:8px;padding:8px 10px;border-radius:10px;border:none;cursor:pointer;text-align:left;
//       background:transparent;color:#9ca3af;border-left:3px solid transparent;font-size:.85rem;
//       transition:background .18s ease,color .18s ease,border-color .18s ease,transform .10s ease;
//     }
//     .nav-item:hover{background:#1e293b;color:#e5e7eb;border-left-color:#22c55e;transform:translateX(2px);}
//     .nav-item.active{background:#334155;color:#f9fafb;font-weight:600;border-left-color:#22c55e;}
//     .cell-date{font-variant-numeric:tabular-nums;white-space:nowrap;}
//     .pill{padding:5px 10px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;font-size:.8rem;}
//     .pill-green{border:none;background:#22c55e;color:#020617;font-weight:700;}
//     .input{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:4px 10px;font-size:.8rem;}
//     .select{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:3px 8px;}
//     .card{border-radius:12px;border:1px solid #111827;background:#020617;}
//     .muted{color:#9ca3af;}
//     .danger{color:#f97373;}
//     .ok{color:#22c55e;}
//     code{background:#0b1220;border:1px solid #111827;border-radius:8px;padding:2px 6px;}
//   </style>
// </head>

// <body style="background:#020617;color:#e5e7eb;font-family:system-ui,sans-serif;margin:0;">
// <div style="min-height:100vh;display:flex;">

//   <aside style="width:240px;background:#020617;border-right:1px solid #111827;
//                 padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
//                 position:sticky;top:0;align-self:flex-start;height:100vh;">
//     <div>
//       <div style="font-size:.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">Loto Tracker API</div>
//       <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">Admin console</div>
//     </div>

//     <nav style="display:flex;flex-direction:column;gap:6px;font-size:.85rem;">
//       <button class="nav-item active" data-section="swagger"><span>*</span><span>Swagger / API</span></button>
//       <button class="nav-item" data-section="logs"><span>*</span><span>Logs serveur</span></button>
//       <button class="nav-item" data-section="db"><span>*</span><span>Base de données (CRUD)</span></button>
//       <button class="nav-item" data-section="stats"><span>*</span><span>Stats joueurs & tickets</span></button>
//     </nav>

//     <div style="margin-top:auto;font-size:.75rem;color:#6b7280;line-height:1.4;">
//       Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
//       Profil Spring : <span style="color:#22c55e;">dev</span>
//     </div>
//   </aside>

//   <div style="flex:1;display:flex;flex-direction:column;min-width:0;">
//     <header style="padding:14px 20px;border-bottom:1px solid #111827;display:flex;justify-content:space-between;align-items:center;">
//       <div>
//         <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
//         <p id="pageSubtitle" style="margin:2px 0 0;font-size:.8rem;color:#9ca3af;">
//           Vue générale : Swagger, logs serveur, base de données et statistiques.
//         </p>
//       </div>
//       <button id="btnLogout" type="button" class="pill">Déconnexion</button>
//     </header>

//     <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

//       <!-- SWAGGER -->
//       <section id="section-swagger" class="section-panel" style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
//         <p class="muted" style="margin:0 0 4px;font-size:.85rem;">Documentation Swagger de l'API Loto Tracker.</p>
//         <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
//           <button id="btnOpenSwagger" type="button" class="pill pill-green">Ouvrir Swagger dans un nouvel onglet</button>
//           <span style="font-size:.8rem;color:#6b7280;">ou utilisez la vue intégrée ci-dessous.</span>
//         </div>
//         <div style="flex:1;min-height:0;margin-top:8px;">
//           <iframe id="swaggerFrame" src="/swagger-ui/index.html"
//                   style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;background:#020617;"></iframe>
//         </div>
//       </section>

//       <!-- LOGS -->
//       <section id="section-logs" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">
//         <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:.8rem;">
//           <span class="muted">Logs du backend (lecture seule).</span>

//           <label class="muted">
//             Fichier :
//             <select id="logFileSelect" class="select" style="min-width:240px;">
//               <option>Chargement...</option>
//             </select>
//           </label>

//           <button id="btnRefreshLogs" type="button" class="pill pill-green">🔄 Rafraîchir</button>

//           <label class="muted">
//             Lignes :
//             <select id="logLineCount" class="select">
//               <option value="200">200</option>
//               <option value="400" selected>400</option>
//               <option value="800">800</option>
//               <option value="1500">1500</option>
//             </select>
//           </label>

//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoScrollLogs" checked> Auto-scroll
//           </label>
//           <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//             <input type="checkbox" id="autoRefreshLogs" checked> Auto-refresh (3s)
//           </label>

//           <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
//         </div>

//         <div id="logsContainer"
//              style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
//                     background:#020617;padding:10px;font-family:monospace;font-size:11px;white-space:pre-wrap;">
//           Chargement des logs...
//         </div>
//       </section>

//       <!-- DB CRUD -->
//       <section id="section-db" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Vue base de données (CRUD simplifié).</span>

//           <label class="muted">
//             Table :
//             <select id="dbResourceSelect" class="select">
//               <option value="tickets">public.tickets</option>
//               <option value="users">public.users</option>
//               <option value="ticket_gains">public.ticket_gains</option>
//               <option value="ticket_gain">public.ticket_gain (alias)</option>
//               <option value="refresh_tokens">public.refresh_tokens</option>
//             </select>
//           </label>

//           <button id="btnLoadData" type="button" class="pill pill-green">Charger</button>
//           <button id="btnNewRow" type="button" class="pill">➕ Nouvelle ligne</button>

//           <span id="dbInfoText" style="color:#6b7280;font-size:.8rem;">
//             Champs sensibles masqués (RGPD). refresh_tokens = lecture seule.
//           </span>
//         </div>

//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;font-size:.8rem;">
//           <label class="muted">
//             Recherche :
//             <input id="dbSearch" type="text" placeholder="Texte à filtrer..." class="input">
//           </label>

//           <label class="muted">
//             Lignes / page :
//             <select id="dbPageSize" class="select">
//               <option value="20" selected>20</option>
//               <option value="50">50</option>
//               <option value="100">100</option>
//             </select>
//           </label>

//           <span id="dbPagingInfo" style="color:#6b7280;">Page 1 / 1</span>

//           <div style="display:flex;gap:4px;">
//             <button id="dbPrevPage" type="button" class="pill" style="font-size:.75rem;">◀</button>
//             <button id="dbNextPage" type="button" class="pill" style="font-size:.75rem;">▶</button>
//           </div>

//           <span id="dbStatus" style="color:#6b7280;"></span>
//         </div>

//         <div id="dbTableWrapper" class="card" style="flex:1;min-height:0;overflow:auto;">
//           <table id="dbTable" style="border-collapse:collapse;width:100%;font-size:.8rem;">
//             <thead id="dbTableHead"></thead>
//             <tbody id="dbTableBody"></tbody>
//           </table>
//         </div>

//         <!-- MODAL -->
//         <div id="dbModalOverlay"
//              style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.7);
//                     align-items:center;justify-content:center;z-index:40;">
//           <div style="background:#020617;border-radius:16px;border:1px solid #111827;
//                       padding:16px 18px;width:420px;max-width:95%;">
//             <h2 id="dbModalTitle" style="margin:0 0 8px;font-size:1rem;">Nouvelle ligne</h2>
//             <form id="dbModalForm" style="display:flex;flex-direction:column;gap:8px;max-height:60vh;overflow:auto;"></form>
//             <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;">
//               <button id="dbModalCancel" type="button" class="pill">Annuler</button>
//               <button id="dbModalSave" type="button" class="pill pill-green">Enregistrer</button>
//             </div>
//           </div>
//         </div>
//       </section>

//       <!-- STATS -->
//       <section id="section-stats" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
//         <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//           <span class="muted">Analyse des joueurs et de leurs tickets.</span>
//           <button id="btnLoadStats" type="button" class="pill pill-green">Recharger les stats</button>

//           <label class="muted">
//             Tri :
//             <select id="statsSort" class="select">
//               <option value="ticketsDesc">Nb tickets ↓</option>
//               <option value="gainDesc">Gain total ↓</option>
//               <option value="lastTicketDesc">Dernier ticket ↓</option>
//             </select>
//           </label>

//           <label class="muted">
//             Filtrer par email :
//             <input id="statsSearch" type="text" placeholder="user@hbnb.com" class="input">
//           </label>

//           <span id="statsStatus" style="color:#6b7280;"></span>
//         </div>

//         <div id="statsGrid"
//              style="flex:1;min-height:0;overflow:auto;display:grid;
//                     grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
//                     gap:10px;padding-top:4px;"></div>
//       </section>

//     </main>
//   </div>
// </div>

// <script>
//   const DEFAULT_LOG_FILE = "application.log";

//   // ✅ JWT en cookie HttpOnly => on NE PEUT PAS le lire, et on n'envoie PAS de header Authorization
//   function getAuthHeaders() {
//     return {};
//   }

//   // ✅ RGPD/Sécurité : champs à masquer même en admin
//   const SENSITIVE_PARTS = [
//     "password","hashed","hash",
//     "token","refresh","access",
//     "secret","apikey","api_key",
//     "session","cookie","reset"
//   ];

//   function isSensitiveKey(key) {
//     if (!key) return false;
//     const k = String(key).toLowerCase();
//     return SENSITIVE_PARTS.some(p => k.includes(p));
//   }

//   function maskValue(v) {
//     if (v === null || v === undefined || v === "") return "";
//     const s = String(v);
//     if (s.length <= 6) return "••••••";
//     return s.slice(0, 3) + "••••••" + s.slice(-3);
//   }

//   function safeCellValue(key, value) {
//     if (value === null || value === undefined) return "";
//     if (isSensitiveKey(key)) return maskValue(value);

//     if (Array.isArray(value)) {
//       if (String(key).toLowerCase().includes("tickets")) return "[array] (" + value.length + ")";
//       return JSON.stringify(value);
//     }
//     if (typeof value === "object") return JSON.stringify(value);
//     return String(value);
//   }

//   // ---------- NAV ----------
//   const navItems = document.querySelectorAll(".nav-item");
//   const sections = {
//     swagger: document.getElementById("section-swagger"),
//     logs: document.getElementById("section-logs"),
//     db: document.getElementById("section-db"),
//     stats: document.getElementById("section-stats")
//   };
//   const pageTitle = document.getElementById("pageTitle");
//   const pageSubtitle = document.getElementById("pageSubtitle");

//   const subtitles = {
//     swagger: "Documentation Swagger et tests de l’API.",
//     logs: "Suivi en temps réel des logs Spring Boot (fichiers + .gz).",
//     db: "Exploration et modification des tables principales (CRUD).",
//     stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
//   };

//   function showSection(key) {
//     Object.keys(sections).forEach(k => sections[k].style.display = (k === key) ? "flex" : "none");

//     navItems.forEach(btn => {
//       if (btn.getAttribute("data-section") === key) {
//         btn.classList.add("active");
//         btn.style.background = "#111827";
//         btn.style.color = "#e5e7eb";
//       } else {
//         btn.classList.remove("active");
//         btn.style.background = "transparent";
//         btn.style.color = "#9ca3af";
//       }
//     });

//     pageTitle.textContent = "Tableau de bord administrateur";
//     pageSubtitle.textContent = subtitles[key] || "";

//     if (key === "logs") loadLogFiles().then(() => refreshLogs());
//     if (key === "stats") loadStats();
//   }

//   navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.getAttribute("data-section"))));

//   // ---------- SWAGGER ----------
//   document.getElementById("btnOpenSwagger").addEventListener("click", () => window.open("/swagger-ui/index.html", "_blank"));

//   // ---------- LOGS ----------
//   const logsContainer = document.getElementById("logsContainer");
//   const btnRefreshLogs = document.getElementById("btnRefreshLogs");
//   const logLineCountSelect = document.getElementById("logLineCount");
//   const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
//   const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
//   const logsLastRefresh = document.getElementById("logsLastRefresh");
//   const logFileSelect = document.getElementById("logFileSelect");

//   async function loadLogFiles() {
//     try {
//       const res = await fetch("/admin/logs/files", {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur " + res.status + "</option>";
//         return;
//       }

//       const files = await res.json();
//       logFileSelect.innerHTML = "";

//       if (!Array.isArray(files) || files.length === 0) {
//         const o = document.createElement("option");
//         o.value = DEFAULT_LOG_FILE;
//         o.textContent = "Aucun fichier (logs/)";
//         logFileSelect.appendChild(o);
//         return;
//       }

//       let foundDefault = false;
//       files.forEach(f => {
//         const o = document.createElement("option");
//         o.value = f;
//         o.textContent = f;
//         if (f === DEFAULT_LOG_FILE) { o.selected = true; foundDefault = true; }
//         logFileSelect.appendChild(o);
//       });

//       if (!foundDefault) logFileSelect.value = files[0];

//     } catch (e) {
//       console.error(e);
//       logFileSelect.innerHTML = "<option value=\\"" + DEFAULT_LOG_FILE + "\\">Erreur réseau</option>";
//     }
//   }

//   async function refreshLogs() {
//     const lines = logLineCountSelect.value;
//     const file = logFileSelect.value || DEFAULT_LOG_FILE;

//     try {
//       const res = await fetch(`/admin/logs?lines=${encodeURIComponent(lines)}&file=${encodeURIComponent(file)}`, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       const text = await res.text();
//       logsContainer.textContent = text || "(Aucun log pour le moment)";

//       const now = new Date();
//       logsLastRefresh.textContent = "Dernier refresh : " + now.toLocaleTimeString("fr-FR");

//       if (autoScrollLogsCheckbox.checked) logsContainer.scrollTop = logsContainer.scrollHeight;

//     } catch (e) {
//       console.error(e);
//       logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//     }
//   }

//   btnRefreshLogs.addEventListener("click", refreshLogs);
//   logLineCountSelect.addEventListener("change", refreshLogs);
//   logFileSelect.addEventListener("change", refreshLogs);
//   setInterval(() => { if (autoRefreshLogsCheckbox.checked) refreshLogs(); }, 3000);

//   // ---------- DB / CRUD ----------
//   const dbResourceSelect = document.getElementById("dbResourceSelect");
//   const btnLoadData = document.getElementById("btnLoadData");
//   const btnNewRow = document.getElementById("btnNewRow");
//   const dbTableHead = document.getElementById("dbTableHead");
//   const dbTableBody = document.getElementById("dbTableBody");
//   const dbSearch = document.getElementById("dbSearch");
//   const dbPageSize = document.getElementById("dbPageSize");
//   const dbPagingInfo = document.getElementById("dbPagingInfo");
//   const dbPrevPage = document.getElementById("dbPrevPage");
//   const dbNextPage = document.getElementById("dbNextPage");
//   const dbModalOverlay = document.getElementById("dbModalOverlay");
//   const dbModalTitle = document.getElementById("dbModalTitle");
//   const dbModalForm = document.getElementById("dbModalForm");
//   const dbModalCancel = document.getElementById("dbModalCancel");
//   const dbModalSave = document.getElementById("dbModalSave");
//   const dbStatus = document.getElementById("dbStatus");

//   const DB_ENDPOINTS = {
//     users: "/api/admin/users",
//     tickets: "/api/admin/tickets",
//     ticket_gains: "/api/admin/ticket-gains",
//     ticket_gain: "/api/admin/ticket-gains",
//     refresh_tokens: "/api/admin/refresh-tokens"
//   };

//   const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);

//   const COLUMN_ORDERS = {
//     refresh_tokens: ["id","user_id","userId","token_hash","tokenHash","created_at","createdAt","expires_at","expiresAt","revoked","revoked_at","revokedAt"],
//     users: ["id","email","role","admin","firstName","lastName","created_at","createdAt","updated_at","updatedAt"],
//     tickets: ["id","userId","user_id","drawDate","draw_date","created_at","createdAt","updated_at","updatedAt"],
//     ticket_gains: ["id","ticket_id","ticketId","matching_numbers","lucky_number_match","gain_amount","created_at","createdAt"],
//     ticket_gain: ["id","ticket_id","ticketId","matching_numbers","lucky_number_match","gain_amount","created_at","createdAt"]
//   };

//   const SORT_FIELDS = {
//     refresh_tokens: ["created_at","createdAt","expires_at","expiresAt"],
//     users: ["created_at","createdAt","updated_at","updatedAt"],
//     tickets: ["created_at","createdAt","updated_at","updatedAt","drawDate","draw_date"],
//     ticket_gains: ["created_at","createdAt"],
//     ticket_gain: ["created_at","createdAt"]
//   };

//   let dbCurrentResource = "users";
//   let dbRawData = [];
//   let dbFilteredData = [];
//   let dbCurrentPage = 0;
//   let dbEditingRow = null;

//   function isDateKey(key) {
//     if (!key) return false;
//     const k = key.toLowerCase();
//     return k.endsWith("at") || k.endsWith("_at") || k.includes("date");
//   }

//   function formatDateValue(value) {
//     if (value === null || value === undefined || value === "") return "";

//     if (Array.isArray(value) && value.length >= 3) {
//       const y = value[0] || 1970;
//       const m = (value[1] || 1) - 1;
//       const d = value[2] || 1;
//       const hh = value[3] || 0;
//       const mm = value[4] || 0;
//       const ss = value[5] || 0;
//       const dt = new Date(y, m, d, hh, mm, ss);
//       if (!Number.isNaN(dt.getTime())) return dt.toLocaleString("fr-FR");
//     }

//     if (typeof value === "string") {
//       const t = Date.parse(value);
//       if (!Number.isNaN(t)) return new Date(t).toLocaleString("fr-FR");
//       const asNum = Number(value);
//       if (!Number.isNaN(asNum)) value = asNum; else return value;
//     }

//     if (typeof value === "number") {
//       const ms = (value > 1e12) ? value : (value * 1000);
//       const d = new Date(ms);
//       if (!Number.isNaN(d.getTime())) return d.toLocaleString("fr-FR");
//     }

//     return String(value);
//   }

//   function getSortMs(row) {
//     const fields = SORT_FIELDS[dbCurrentResource] || ["created_at","createdAt","updated_at","updatedAt"];
//     for (const f of fields) {
//       const v = row?.[f];
//       if (v === null || v === undefined || v === "") continue;

//       if (Array.isArray(v) && v.length >= 3) {
//         const y = v[0] || 1970;
//         const m = (v[1] || 1) - 1;
//         const d = v[2] || 1;
//         const hh = v[3] || 0;
//         const mm = v[4] || 0;
//         const ss = v[5] || 0;
//         return new Date(y, m, d, hh, mm, ss).getTime();
//       }

//       if (typeof v === "string") {
//         const t = Date.parse(v);
//         if (!Number.isNaN(t)) return t;
//         const asNum = Number(v);
//         if (!Number.isNaN(asNum)) return (asNum > 1e12) ? asNum : asNum * 1000;
//       }

//       if (typeof v === "number") return (v > 1e12) ? v : v * 1000;
//     }
//     return 0;
//   }

//   function getColumns(pageData) {
//     if (!pageData || pageData.length === 0) return [];
//     const existing = new Set();
//     pageData.forEach(r => Object.keys(r || {}).forEach(k => existing.add(k)));

//     const preferred = COLUMN_ORDERS[dbCurrentResource];
//     let cols = [];

//     if (preferred && preferred.length) {
//       cols = preferred.filter(k => existing.has(k));
//       const rest = Array.from(existing).filter(k => !cols.includes(k));
//       cols = cols.concat(rest);
//     } else {
//       cols = Object.keys(pageData[0]);
//     }
//     return cols;
//   }

//   function applyDbFilter() {
//     const q = dbSearch.value.toLowerCase().trim();
//     if (!q) dbFilteredData = dbRawData.slice();
//     else dbFilteredData = dbRawData.filter(row => JSON.stringify(row).toLowerCase().includes(q));

//     dbFilteredData.sort((a, b) => getSortMs(b) - getSortMs(a));
//     dbCurrentPage = 0;
//     renderDbTable();
//   }

//   function setDbStatus(text, isError=false) {
//     dbStatus.textContent = text;
//     dbStatus.className = isError ? "danger" : "muted";
//   }

//   function renderDbTable() {
//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "";

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);

//     btnNewRow.disabled = isReadOnly;
//     btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//     btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//     if (!dbFilteredData || dbFilteredData.length === 0) {
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
//       dbPagingInfo.textContent = "Page 1 / 1";
//       return;
//     }

//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage >= totalPages) dbCurrentPage = totalPages - 1;

//     const start = dbCurrentPage * pageSize;
//     const end = Math.min(start + pageSize, dbFilteredData.length);
//     const pageData = dbFilteredData.slice(start, end);

//     const cols = getColumns(pageData);

//     const headerRow = document.createElement("tr");
//     cols.forEach(k => {
//       const th = document.createElement("th");
//       th.textContent = k;
//       th.style.padding = "6px 8px";
//       th.style.borderBottom = "1px solid #111827";
//       th.style.textAlign = "left";
//       headerRow.appendChild(th);
//     });

//     if (!isReadOnly) {
//       const thActions = document.createElement("th");
//       thActions.textContent = "Actions";
//       thActions.style.padding = "6px 8px";
//       thActions.style.borderBottom = "1px solid #111827";
//       headerRow.appendChild(thActions);
//     }

//     dbTableHead.appendChild(headerRow);

//     pageData.forEach(row => {
//       const tr = document.createElement("tr");
//       tr.style.borderBottom = "1px solid #020617";

//       cols.forEach(k => {
//         const td = document.createElement("td");
//         const value = row[k];

//         if (isDateKey(k)) {
//           td.textContent = formatDateValue(value);
//           td.className = "cell-date";
//         } else {
//           td.textContent = safeCellValue(k, value);
//         }

//         td.style.padding = "6px 8px";
//         td.style.verticalAlign = "top";
//         td.style.maxWidth = "320px";
//         td.style.wordBreak = "break-word";
//         tr.appendChild(td);
//       });

//       if (!isReadOnly) {
//         const tdActions = document.createElement("td");
//         tdActions.style.padding = "6px 8px";
//         tdActions.style.whiteSpace = "nowrap";

//         const id = row.id || row._id;

//         const btnEdit = document.createElement("button");
//         btnEdit.textContent = "Modifier";
//         btnEdit.style.padding = "3px 8px";
//         btnEdit.style.borderRadius = "999px";
//         btnEdit.style.border = "none";
//         btnEdit.style.fontSize = "0.75rem";
//         btnEdit.style.background = "#3b82f6";
//         btnEdit.style.color = "#020617";
//         btnEdit.style.cursor = "pointer";
//         btnEdit.style.marginRight = "4px";
//         btnEdit.addEventListener("click", () => openDbModal("edit", row));

//         const btnDelete = document.createElement("button");
//         btnDelete.textContent = "Supprimer";
//         btnDelete.style.padding = "3px 8px";
//         btnDelete.style.borderRadius = "999px";
//         btnDelete.style.border = "none";
//         btnDelete.style.fontSize = "0.75rem";
//         btnDelete.style.background = "#ef4444";
//         btnDelete.style.color = "#020617";
//         btnDelete.style.cursor = "pointer";

//         btnDelete.addEventListener("click", async () => {
//           if (!id) { alert("ID introuvable."); return; }
//           if (!confirm("Supprimer " + dbCurrentResource + " #" + id + " ?")) return;

//           const url = DB_ENDPOINTS[dbCurrentResource] + "/" + id;
//           try {
//             const delRes = await fetch(url, {
//               method: "DELETE",
//               headers: { ...getAuthHeaders() },
//               credentials: "include"
//             });

//             if (!delRes.ok) {
//               const t = await delRes.text().catch(() => "");
//               alert("Erreur " + delRes.status + " lors de la suppression.\\n" + t);
//               return;
//             }

//             dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
//             applyDbFilter();

//           } catch (e) {
//             console.error(e);
//             alert("Erreur réseau lors de la suppression.");
//           }
//         });

//         tdActions.appendChild(btnEdit);
//         tdActions.appendChild(btnDelete);
//         tr.appendChild(tdActions);
//       }

//       dbTableBody.appendChild(tr);
//     });

//     dbPagingInfo.textContent = "Page " + (dbCurrentPage + 1) + " / " + totalPages;
//   }

//   async function loadDbData() {
//     dbCurrentResource = dbResourceSelect.value;

//     const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
//     btnNewRow.disabled = isReadOnly;
//     btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
//     btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

//     const url = DB_ENDPOINTS[dbCurrentResource];

//     dbTableHead.innerHTML = "";
//     dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";
//     setDbStatus("Chargement...");

//     if (!url) {
//       setDbStatus("Endpoint non configuré", true);
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Endpoint non configuré.</td></tr>";
//       return;
//     }

//     try {
//       const res = await fetch(url, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         const txt = await res.text().catch(() => "");
//         setDbStatus("Erreur " + res.status, true);

//         const hint =
//           (res.status === 401)
//             ? ("<br><span class='muted'>Astuce cookie : si tu t'es loggé via <code>127.0.0.1</code> mais tu ouvres le dashboard en <code>localhost</code>, le cookie <code>jwtToken</code> ne part pas ⇒ 401.<br>Utilise le même host partout (tout <code>localhost</code> OU tout <code>127.0.0.1</code>).</span>")
//             : "";

//         dbTableBody.innerHTML =
//           "<tr><td style='padding:8px;color:#f97373;line-height:1.4;'>" +
//           "Erreur " + res.status + ". " + (txt || "") + hint +
//           "</td></tr>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data) || data.length === 0) {
//         dbRawData = [];
//         setDbStatus("0 ligne");
//         applyDbFilter();
//         return;
//       }

//       dbRawData = data;
//       setDbStatus(data.length + " lignes");
//       applyDbFilter();

//     } catch (e) {
//       console.error(e);
//       setDbStatus("Erreur réseau", true);
//       dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur réseau.</td></tr>";
//     }
//   }

//   btnLoadData.addEventListener("click", loadDbData);
//   dbSearch.addEventListener("input", () => applyDbFilter());
//   dbPageSize.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
//   dbPrevPage.addEventListener("click", () => { if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); } });
//   dbNextPage.addEventListener("click", () => {
//     const pageSize = parseInt(dbPageSize.value, 10) || 20;
//     const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//     if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
//   });

//   function openDbModal(mode, row) {
//     if (READ_ONLY_RESOURCES.has(dbCurrentResource)) {
//       alert("Cette table est en lecture seule (données sensibles).");
//       return;
//     }

//     dbEditingRow = { resource: dbCurrentResource, mode, row: row || null };
//     dbModalForm.innerHTML = "";

//     const baseRow = row || (dbRawData[0] || {});
//     const keys = Object.keys(baseRow);

//     const protectedFields = ["id","_id","created_at","updated_at","createdAt","updatedAt"];

//     keys.forEach(k => {
//       const wrapper = document.createElement("div");
//       wrapper.style.display = "flex";
//       wrapper.style.flexDirection = "column";
//       wrapper.style.gap = "2px";

//       const label = document.createElement("label");
//       label.textContent = k + (isSensitiveKey(k) ? " (masqué)" : "");
//       label.style.fontSize = "0.8rem";
//       label.style.color = "#9ca3af";

//       const input = document.createElement("input");
//       input.name = k;
//       input.value = row ? (row[k] ?? "") : "";
//       input.style.background = "#020617";
//       input.style.color = "#e5e7eb";
//       input.style.borderRadius = "8px";
//       input.style.border = "1px solid #4b5563";
//       input.style.padding = "6px 8px";
//       input.style.fontSize = "0.8rem";

//       if (protectedFields.includes(k) || isSensitiveKey(k)) {
//         input.disabled = true;
//         input.style.opacity = "0.6";
//         if (isSensitiveKey(k)) input.value = maskValue(input.value);
//       }

//       wrapper.appendChild(label);
//       wrapper.appendChild(input);
//       dbModalForm.appendChild(wrapper);
//     });

//     dbModalTitle.textContent = (mode === "edit")
//       ? "Modifier la ligne #" + (row.id || row._id)
//       : "Nouvelle ligne";

//     dbModalOverlay.style.display = "flex";
//   }

//   function closeDbModal() {
//     dbModalOverlay.style.display = "none";
//     dbEditingRow = null;
//   }

//   dbModalCancel.addEventListener("click", closeDbModal);

//   dbModalSave.addEventListener("click", async () => {
//     if (!dbEditingRow) return;

//     const formData = new FormData(dbModalForm);
//     const obj = {};

//     formData.forEach((v, k) => {
//       const input = dbModalForm.querySelector('input[name="' + k + '"]');
//       if (input && input.disabled) return;
//       obj[k] = v === "" ? null : v;
//     });

//     const resource = dbEditingRow.resource;
//     const baseUrl = DB_ENDPOINTS[resource];
//     let method = "POST";
//     let url = baseUrl;

//     if (dbEditingRow.mode === "edit") {
//       const id = dbEditingRow.row.id || dbEditingRow.row._id;
//       method = "PUT";
//       url = baseUrl + "/" + id;
//     }

//     try {
//       const res = await fetch(url, {
//         method,
//         headers: { "Content-Type": "application/json", ...getAuthHeaders() },
//         credentials: "include",
//         body: JSON.stringify(obj)
//       });

//       if (!res.ok) {
//         const t = await res.text().catch(() => "");
//         alert("Erreur " + res.status + " lors de l’enregistrement.\\n" + t);
//         return;
//       }

//       closeDbModal();
//       await loadDbData();

//     } catch (e) {
//       console.error(e);
//       alert("Erreur réseau lors de l’enregistrement.");
//     }
//   });

//   btnNewRow.addEventListener("click", () => openDbModal("create", null));

//   // ---------- STATS ----------
//   const btnLoadStats = document.getElementById("btnLoadStats");
//   const statsGrid = document.getElementById("statsGrid");
//   const statsSort = document.getElementById("statsSort");
//   const statsSearch = document.getElementById("statsSearch");
//   const statsStatus = document.getElementById("statsStatus");
//   const STATS_ENDPOINT = "/api/admin/users-stats";

//   let statsRaw = [];
//   let statsFiltered = [];

//   function setStatsStatus(text, isError=false) {
//     statsStatus.textContent = text;
//     statsStatus.className = isError ? "danger" : "muted";
//   }

//   function applyStatsFilter() {
//     const q = statsSearch.value.toLowerCase().trim();
//     if (!q) statsFiltered = statsRaw.slice();
//     else {
//       statsFiltered = statsRaw.filter(u =>
//         (u.email || "").toLowerCase().includes(q) ||
//         (u.firstName || "").toLowerCase().includes(q) ||
//         (u.lastName || "").toLowerCase().includes(q)
//       );
//     }

//     const sortKey = statsSort.value;
//     statsFiltered.sort((a, b) => {
//       const tA = a.ticketsCount || 0;
//       const tB = b.ticketsCount || 0;
//       const gA = a.totalGain || 0;
//       const gB = b.totalGain || 0;
//       const dA = a.lastTicketDate ? new Date(a.lastTicketDate).getTime() : 0;
//       const dB = b.lastTicketDate ? new Date(b.lastTicketDate).getTime() : 0;

//       if (sortKey === "ticketsDesc") return tB - tA;
//       if (sortKey === "gainDesc") return gB - gA;
//       if (sortKey === "lastTicketDesc") return dB - dA;
//       return 0;
//     });

//     renderStats();
//   }

//   function renderStats() {
//     statsGrid.innerHTML = "";
//     if (!statsFiltered || statsFiltered.length === 0) {
//       statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Aucune statistique disponible.</div>";
//       return;
//     }

//     statsFiltered.forEach(u => {
//       const card = document.createElement("article");
//       card.style.borderRadius = "14px";
//       card.style.border = "1px solid #111827";
//       card.style.background = "linear-gradient(135deg,#020617,#020617,#020617)";
//       card.style.padding = "10px 12px";
//       card.style.display = "flex";
//       card.style.flexDirection = "column";
//       card.style.gap = "6px";

//       const header = document.createElement("div");
//       header.style.display = "flex";
//       header.style.justifyContent = "space-between";
//       header.style.alignItems = "center";

//       const title = document.createElement("div");
//       title.innerHTML =
//         "<div style='font-size:.9rem;font-weight:600;'>" + (u.firstName || "") + " " + (u.lastName || "") + "</div>" +
//         "<div style='font-size:.78rem;color:#9ca3af;'>" + (u.email || "") + "</div>";

//       const badge = document.createElement("span");
//       badge.textContent = u.admin ? "ADMIN" : "USER";
//       badge.style.fontSize = ".7rem";
//       badge.style.padding = "2px 8px";
//       badge.style.borderRadius = "999px";
//       badge.style.border = "1px solid " + (u.admin ? "#22c55e" : "#4b5563");
//       badge.style.color = u.admin ? "#22c55e" : "#9ca3af";

//       header.appendChild(title);
//       header.appendChild(badge);

//       const statsRow = document.createElement("div");
//       statsRow.style.display = "grid";
//       statsRow.style.gridTemplateColumns = "repeat(3,minmax(0,1fr))";
//       statsRow.style.gap = "6px";
//       statsRow.style.marginTop = "4px";

//       const blocks = [
//         { label: "Tickets", value: u.ticketsCount || 0 },
//         { label: "Gain total", value: (u.totalGain || 0) + " €" },
//         { label: "Meilleur gain", value: (u.bestGain || 0) + " €" }
//       ];

//       blocks.forEach(b => {
//         const div = document.createElement("div");
//         div.style.fontSize = ".75rem";
//         div.style.color = "#9ca3af";
//         div.innerHTML =
//           "<div>" + b.label + "</div>" +
//           "<div style='color:#e5e7eb;font-weight:600;font-size:.86rem;'>" + b.value + "</div>";
//         statsRow.appendChild(div);
//       });

//       const footer = document.createElement("div");
//       footer.style.marginTop = "4px";
//       footer.style.fontSize = ".75rem";
//       footer.style.color = "#6b7280";
//       footer.textContent = "Dernier ticket : " + (u.lastTicketDate || "—") + " | ID utilisateur : " + (u.id || u._id || "—");

//       card.appendChild(header);
//       card.appendChild(statsRow);
//       card.appendChild(footer);

//       statsGrid.appendChild(card);
//     });
//   }

//   async function loadStats() {
//     statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Chargement des statistiques...</div>";
//     setStatsStatus("Chargement...");

//     try {
//       const res = await fetch(STATS_ENDPOINT, {
//         method: "GET",
//         headers: { ...getAuthHeaders() },
//         credentials: "include"
//       });

//       if (!res.ok) {
//         const t = await res.text().catch(() => "");
//         setStatsStatus("Erreur " + res.status, true);
//         statsGrid.innerHTML =
//           "<div class='danger' style='font-size:.85rem;line-height:1.4;'>" +
//           "Endpoint stats indisponible : <code>" + STATS_ENDPOINT + "</code><br>" +
//           "Code : " + res.status + "<br>" +
//           (t ? ("<pre class='muted' style='white-space:pre-wrap;'>" + t + "</pre>") : "") +
//           "</div>";
//         return;
//       }

//       const data = await res.json();
//       if (!Array.isArray(data)) {
//         setStatsStatus("Réponse invalide", true);
//         statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Réponse invalide du serveur.</div>";
//         return;
//       }

//       statsRaw = data;
//       setStatsStatus(data.length + " users");
//       applyStatsFilter();

//     } catch (e) {
//       console.error(e);
//       setStatsStatus("Erreur réseau", true);
//       statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Erreur réseau.</div>";
//     }
//   }

//   btnLoadStats.addEventListener("click", loadStats);
//   statsSort.addEventListener("change", applyStatsFilter);
//   statsSearch.addEventListener("input", applyStatsFilter);

//   // ---------- LOGOUT ----------
//   document.getElementById("btnLogout").addEventListener("click", async () => {
//     try { await fetch("/api/auth/logout", { method: "POST", credentials: "include" }); }
//     catch (e) { console.error(e); }
//     window.location.href = "/admin-login.html";
//   });

//   // ---------- INIT ----------
//   showSection("swagger");
//   loadLogFiles().then(() => refreshLogs());
//   loadDbData();
// </script>

// </body>
// </html>
// """;
//     }

//     // ---------------------------
//     // LOGS API
//     // ---------------------------
//     @GetMapping(value = "/logs/files", produces = MediaType.APPLICATION_JSON_VALUE)
//     public List<String> listLogFiles() throws IOException {
//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return List.of();
//         }

//         try (Stream<Path> s = Files.list(LOG_DIR)) {
//             return s.filter(Files::isRegularFile)
//                     .map(p -> p.getFileName().toString())
//                     .filter(this::isAllowedLogName)
//                     .sorted(Comparator.reverseOrder())
//                     .toList();
//         }
//     }

//     @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
//     public ResponseEntity<String> getLogs(
//             @RequestParam(defaultValue = "400") int lines,
//             @RequestParam(defaultValue = DEFAULT_LOG_FILE) String file
//     ) throws IOException {

//         if (!Files.exists(LOG_DIR) || !Files.isDirectory(LOG_DIR)) {
//             return ResponseEntity.ok("Dossier logs introuvable : " + LOG_DIR.toAbsolutePath());
//         }

//         if (!isAllowedLogName(file)) {
//             return ResponseEntity.badRequest().body("Fichier log invalide.");
//         }

//         Path requested = LOG_DIR.resolve(file).normalize();
//         if (!requested.startsWith(LOG_DIR)) {
//             return ResponseEntity.badRequest().body("Fichier invalide.");
//         }

//         if (!Files.exists(requested) || !Files.isRegularFile(requested)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + requested.toAbsolutePath());
//         }

//         int safeLines = Math.max(1, Math.min(lines, 20000));
//         List<String> all = readAllLinesSmart(requested);

//         int fromIndex = Math.max(0, all.size() - safeLines);
//         String text = String.join("\n", all.subList(fromIndex, all.size()));

//         // Convertit les "\\n" en retours à la ligne
//         text = text.replace("\\\\n", "\n");

//         return ResponseEntity.ok(text);
//     }

//     // ---------------------------
//     // Helpers
//     // ---------------------------
//     private boolean isAllowedLogName(String name) {
//         if (name == null) return false;
//         if (name.contains("..") || name.contains("/") || name.contains("\\")) return false;

//         return name.endsWith(".log")
//                 || name.contains(".log.")
//                 || name.endsWith(".log.gz")
//                 || name.endsWith(".gz");
//     }

//     private List<String> readAllLinesSmart(Path file) throws IOException {
//         String fn = file.getFileName().toString().toLowerCase();

//         if (fn.endsWith(".gz")) {
//             try (InputStream in = Files.newInputStream(file);
//                  GZIPInputStream gz = new GZIPInputStream(in);
//                  InputStreamReader isr = new InputStreamReader(gz, StandardCharsets.UTF_8);
//                  BufferedReader br = new BufferedReader(isr)) {
//                 return br.lines().toList();
//             }
//         }

//         return Files.readAllLines(file, StandardCharsets.UTF_8);
//     }
// }



// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.nio.file.*;
// import java.util.List;

// @RestController
// @RequestMapping("/admin")
// public class AdminDashboardController {

//     // ⚠️ adapte ce chemin à ton fichier de log réel
//     private static final Path LOG_PATH = Paths.get("logs/application.log");

//     @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminDashboard() {

//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//     <meta charset="UTF-8">
//     <title>Dashboard admin - Loto Tracker</title>
//     <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
//     <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
//     <meta name="viewport" content="width=device-width, initial-scale=1.0">
//     <style>
//         /* Style de base du menu */
//         .nav-item {
//             width: 100%;
//             display: flex;
//             align-items: center;
//             gap: 8px;
//             padding: 8px 10px;
//             border-radius: 10px;
//             border: none;
//             cursor: pointer;
//             text-align: left;

//             background: transparent;
//             color: #9ca3af;
//             border-left: 3px solid transparent;

//             font-size: 0.85rem;
//             transition:
//                 background 0.18s ease,
//                 color 0.18s ease,
//                 border-color 0.18s ease,
//                 transform 0.10s ease;
//         }

//         /* Hover bien plus intense */
//         .nav-item:hover {
//             background: #1e293b;          /* fond plus visible */
//             color: #e5e7eb;               /* texte plus clair */
//             border-left-color: #22c55e;   /* liseré vert */
//             transform: translateX(2px);   /* petit slide à droite */
//         }

//         /* Onglet sélectionné (actif) */
//         .nav-item.active {
//             background: #334155;          /* encore plus contrasté */
//             color: #f9fafb;
//             font-weight: 600;
//             border-left-color: #22c55e;   /* liseré vert permanent */
//         }
//     </style>

// </head>
// <body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;margin:0;">
// <div style="min-height:100vh;display:flex;">

//     <!-- SIDEBAR GAUCHE -->
//     <aside style="width:240px;background:#020617;border-right:1px solid #111827;
//                   padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
//                   position:sticky;top:0;align-self:flex-start;height:100vh;">
//         <!-- Logo / titre -->
//         <div>
//             <div style="font-size:0.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">
//                 Loto Tracker API
//             </div>
//             <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">
//                 Admin console
//             </div>
//         </div>

//         <!-- Menu -->
//         <nav style="display:flex;flex-direction:column;gap:6px;font-size:0.85rem;">
//             <button class="nav-item active" data-section="swagger">
//                 <span>*</span>
//                 <span>Swagger / API</span>
//             </button>
//             <button class="nav-item" data-section="logs">
//                 <span>*</span>
//                 <span>Logs serveur</span>
//             </button>
//             <button class="nav-item" data-section="db">
//                 <span>*</span>
//                 <span>Base de données (CRUD)</span>
//             </button>
//             <button class="nav-item" data-section="stats">
//                 <span>*</span>
//                 <span>Stats joueurs & tickets</span>
//             </button>
//         </nav>

//         <div style="margin-top:auto;font-size:0.75rem;color:#6b7280;line-height:1.4;">
//             Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
//             Profil Spring : <span style="color:#22c55e;">dev</span>
//         </div>
//     </aside>

//     <!-- COLONNE CONTENU -->
//     <div style="flex:1;display:flex;flex-direction:column;min-width:0;">

//         <!-- HEADER -->
//         <header style="padding:14px 20px;border-bottom:1px solid #111827;
//                        display:flex;justify-content:space-between;align-items:center;">
//             <div>
//                 <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
//                 <p id="pageSubtitle" style="margin:2px 0 0;font-size:0.8rem;color:#9ca3af;">
//                     Vue générale : Swagger, logs serveur, base de données et statistiques.
//                 </p>
//             </div>
//             <button id="btnLogout" type="button"
//                     style="padding:6px 14px;border-radius:999px;border:1px solid #4b5563;
//                            background:#020617;color:#e5e7eb;font-size:0.8rem;cursor:pointer;">
//                 Déconnexion
//             </button>
//         </header>

//         <!-- CONTENU -->
//         <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

//             <!-- SECTION SWAGGER -->
//             <section id="section-swagger" class="section-panel"
//                      style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
//                 <p style="margin:0 0 4px;font-size:0.85rem;color:#9ca3af;">
//                     Documentation Swagger de l'API Loto Tracker.
//                 </p>
//                 <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
//                     <button id="btnOpenSwagger" type="button"
//                             style="padding:6px 12px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;font-size:0.8rem;">
//                         Ouvrir Swagger dans un nouvel onglet
//                     </button>
//                     <span style="font-size:0.8rem;color:#6b7280;">
//                         ou utilisez la vue intégrée ci-dessous.
//                     </span>
//                 </div>
//                 <div style="flex:1;min-height:0;margin-top:8px;">
//                     <iframe id="swaggerFrame"
//                             src="/swagger-ui/index.html"
//                             style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;
//                                    background:#020617;"></iframe>
//                 </div>
//             </section>

//             <!-- SECTION LOGS -->
//             <section id="section-logs" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">
//                 <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:0.8rem;">
//                     <span style="color:#9ca3af;">Logs du backend (lecture seule).</span>
//                     <button id="btnRefreshLogs" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;">
//                         🔄 Rafraîchir
//                     </button>
//                     <label style="color:#9ca3af;">
//                         Lignes :
//                         <select id="logLineCount"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="200">200</option>
//                             <option value="400" selected>400</option>
//                             <option value="800">800</option>
//                         </select>
//                     </label>
//                     <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//                         <input type="checkbox" id="autoScrollLogs" checked>
//                         Auto-scroll
//                     </label>
//                     <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
//                         <input type="checkbox" id="autoRefreshLogs" checked>
//                         Auto-refresh (3s)
//                     </label>
//                     <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
//                 </div>

//                 <div id="logsContainer"
//                      style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
//                             background:#020617;padding:10px;font-family:monospace;font-size:11px;
//                             white-space:pre-wrap;">
//                     Chargement des logs...
//                 </div>
//             </section>

//             <!-- SECTION BASE DE DONNÉES / CRUD -->
//             <section id="section-db" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:0.85rem;">
//                 <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//                     <span style="color:#9ca3af;">Vue base de données (CRUD simplifié).</span>
//                     <label style="color:#9ca3af;">
//                         Table :
//                         <select id="dbResourceSelect"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="tickets">public.tickets</option>
//                             <option value="users">public.users</option>
//                             <option value="ticket_gains">public.ticket_gains</option>
//                             <option value="refresh_tokens">public.refresh_tokens</option>
//                         </select>
//                     </label>
//                     <button id="btnLoadData" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;">
//                         Charger
//                     </button>
//                     <button id="btnNewRow" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:1px solid #4b5563;
//                                    background:#020617;color:#e5e7eb;cursor:pointer;">
//                         ➕ Nouvelle ligne
//                     </button>
//                     <span id="dbInfoText" style="color:#6b7280;font-size:0.8rem;">
//                         Lecture / édition / suppression. L’ID et les dates sont protégés par défaut.
//                     </span>
//                 </div>

//                 <!-- Zone recherche & pagination client -->
//                 <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;font-size:0.8rem;">
//                     <label style="color:#9ca3af;">
//                         Recherche :
//                         <input id="dbSearch" type="text" placeholder="Texte à filtrer..."
//                                style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                       border:1px solid #4b5563;padding:4px 10px;font-size:0.8rem;">
//                     </label>
//                     <label style="color:#9ca3af;">
//                         Lignes / page :
//                         <select id="dbPageSize"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="20" selected>20</option>
//                             <option value="50">50</option>
//                             <option value="100">100</option>
//                         </select>
//                     </label>
//                     <span id="dbPagingInfo" style="color:#6b7280;">Page 1 / 1</span>
//                     <div style="display:flex;gap:4px;">
//                         <button id="dbPrevPage" type="button"
//                                 style="padding:3px 8px;border-radius:999px;border:1px solid #4b5563;
//                                        background:#020617;color:#e5e7eb;font-size:0.75rem;cursor:pointer;">
//                             ◀
//                         </button>
//                         <button id="dbNextPage" type="button"
//                                 style="padding:3px 8px;border-radius:999px;border:1px solid #4b5563;
//                                        background:#020617;color:#e5e7eb;font-size:0.75rem;cursor:pointer;">
//                             ▶
//                         </button>
//                     </div>
//                 </div>

//                 <div id="dbTableWrapper"
//                      style="flex:1;min-height:0;overflow:auto;border-radius:12px;border:1px solid #111827;
//                             background:#020617;">
//                     <table id="dbTable" style="border-collapse:collapse;width:100%;font-size:0.8rem;">
//                         <thead id="dbTableHead"></thead>
//                         <tbody id="dbTableBody"></tbody>
//                     </table>
//                 </div>

//                 <!-- Modal création / édition -->
//                 <div id="dbModalOverlay"
//                      style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.7);
//                             align-items:center;justify-content:center;z-index:40;">
//                     <div style="background:#020617;border-radius:16px;border:1px solid #111827;
//                                 padding:16px 18px;width:420px;max-width:95%;">
//                         <h2 id="dbModalTitle" style="margin:0 0 8px;font-size:1rem;">Nouvelle ligne</h2>
//                         <form id="dbModalForm" style="display:flex;flex-direction:column;gap:8px;max-height:60vh;overflow:auto;">
//                             <!-- champs dynamiques ici -->
//                         </form>
//                         <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;">
//                             <button id="dbModalCancel" type="button"
//                                     style="padding:5px 10px;border-radius:999px;border:1px solid #4b5563;
//                                            background:#020617;color:#e5e7eb;font-size:0.8rem;cursor:pointer;">
//                                 Annuler
//                             </button>
//                             <button id="dbModalSave" type="button"
//                                     style="padding:5px 12px;border-radius:999px;border:none;background:#22c55e;
//                                            color:#020617;font-weight:600;font-size:0.8rem;cursor:pointer;">
//                                 Enregistrer
//                             </button>
//                         </div>
//                     </div>
//                 </div>
//             </section>

//             <!-- SECTION STATS JOUEURS / TICKETS -->
//             <section id="section-stats" class="section-panel"
//                      style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:0.85rem;">
//                 <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
//                     <span style="color:#9ca3af;">Analyse des joueurs et de leurs tickets.</span>
//                     <button id="btnLoadStats" type="button"
//                             style="padding:5px 10px;border-radius:999px;border:none;background:#22c55e;
//                                    color:#020617;font-weight:600;cursor:pointer;">
//                         Recharger les stats
//                     </button>
//                     <label style="color:#9ca3af;">
//                         Tri :
//                         <select id="statsSort"
//                                 style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                        border:1px solid #4b5563;padding:3px 8px;">
//                             <option value="ticketsDesc">Nb tickets ↓</option>
//                             <option value="gainDesc">Gain total ↓</option>
//                             <option value="lastTicketDesc">Dernier ticket ↓</option>
//                         </select>
//                     </label>
//                     <label style="color:#9ca3af;">
//                         Filtrer par email :
//                         <input id="statsSearch" type="text" placeholder="user@hbnb.com"
//                                style="background:#020617;color:#e5e7eb;border-radius:999px;
//                                       border:1px solid #4b5563;padding:4px 10px;font-size:0.8rem;">
//                     </label>
//                 </div>

//                 <div id="statsGrid"
//                      style="flex:1;min-height:0;overflow:auto;display:grid;
//                             grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
//                             gap:10px;padding-top:4px;">
//                     <!-- cards dynamiques -->
//                 </div>
//             </section>

//         </main>
//     </div>
// </div>

// <script>
//     const API_BASE = window.location.origin;

//     // -------- JWT HEADER --------
//     // ⚠️ adapte "jwtToken" au nom réellement utilisé dans ton front
//     function getAuthHeaders() {
//         const token = localStorage.getItem("jwtToken");
//         if (!token) return {};
//         return { "Authorization": "Bearer " + token };
//     }

//     // ---------- NAVIGATION / SECTIONS ----------
//     const navItems = document.querySelectorAll(".nav-item");
//     const sections = {
//         swagger: document.getElementById("section-swagger"),
//         logs: document.getElementById("section-logs"),
//         db: document.getElementById("section-db"),
//         stats: document.getElementById("section-stats")
//     };
//     const pageTitle = document.getElementById("pageTitle");
//     const pageSubtitle = document.getElementById("pageSubtitle");

//     const subtitles = {
//         swagger: "Documentation Swagger et tests de l’API.",
//         logs: "Suivi en temps réel des logs Spring Boot.",
//         db: "Exploration et modification des tables principales (lecture / écriture).",
//         stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
//     };

//     function showSection(key) {
//         Object.keys(sections).forEach(k => {
//             sections[k].style.display = (k === key) ? "flex" : "none";
//         });
//         navItems.forEach(btn => {
//             if (btn.getAttribute("data-section") === key) {
//                 btn.classList.add("active");
//                 btn.style.background = "#111827";
//                 btn.style.color = "#e5e7eb";
//             } else {
//                 btn.classList.remove("active");
//                 btn.style.background = "transparent";
//                 btn.style.color = "#9ca3af";
//             }
//         });
//         pageTitle.textContent = "Tableau de bord administrateur";
//         pageSubtitle.textContent = subtitles[key] || "";
//     }

//     navItems.forEach(btn => {
//         btn.addEventListener("click", () => {
//             const section = btn.getAttribute("data-section");
//             showSection(section);
//         });
//     });

//     // ---------- SWAGGER ----------
//     document.getElementById("btnOpenSwagger").addEventListener("click", () => {
//         window.open("/swagger-ui/index.html", "_blank");
//     });

//     // ---------- LOGS ----------
//     const logsContainer = document.getElementById("logsContainer");
//     const btnRefreshLogs = document.getElementById("btnRefreshLogs");
//     const logLineCountSelect = document.getElementById("logLineCount");
//     const autoScrollLogsCheckbox = document.getElementById("autoScrollLogs");
//     const autoRefreshLogsCheckbox = document.getElementById("autoRefreshLogs");
//     const logsLastRefresh = document.getElementById("logsLastRefresh");

//     async function refreshLogs() {
//         const lines = logLineCountSelect.value;
//         try {
//             const res = await fetch(`/admin/logs?lines=${lines}`, {
//                 method: "GET",
//                 headers: {
//                     ...getAuthHeaders()
//                 },
//                 credentials: "include"
//             });
//             const text = await res.text();
//             logsContainer.textContent = text || "(Aucun log pour le moment)";
//             const now = new Date();
//             logsLastRefresh.textContent = "Dernier refresh : " + now.toLocaleTimeString("fr-FR");
//             if (autoScrollLogsCheckbox.checked) {
//                 logsContainer.scrollTop = logsContainer.scrollHeight;
//             }
//         } catch (e) {
//             console.error(e);
//             logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
//         }
//     }

//     btnRefreshLogs.addEventListener("click", refreshLogs);
//     logLineCountSelect.addEventListener("change", refreshLogs);
//     setInterval(() => {
//         if (autoRefreshLogsCheckbox.checked) {
//             refreshLogs();
//         }
//     }, 3000);

//     // ---------- BASE DE DONNÉES / CRUD ----------
//     const dbResourceSelect = document.getElementById("dbResourceSelect");
//     const btnLoadData = document.getElementById("btnLoadData");
//     const btnNewRow = document.getElementById("btnNewRow");
//     const dbTableHead = document.getElementById("dbTableHead");
//     const dbTableBody = document.getElementById("dbTableBody");
//     const dbSearch = document.getElementById("dbSearch");
//     const dbPageSize = document.getElementById("dbPageSize");
//     const dbPagingInfo = document.getElementById("dbPagingInfo");
//     const dbPrevPage = document.getElementById("dbPrevPage");
//     const dbNextPage = document.getElementById("dbNextPage");
//     const dbModalOverlay = document.getElementById("dbModalOverlay");
//     const dbModalTitle = document.getElementById("dbModalTitle");
//     const dbModalForm = document.getElementById("dbModalForm");
//     const dbModalCancel = document.getElementById("dbModalCancel");
//     const dbModalSave = document.getElementById("dbModalSave");

//     // const DB_ENDPOINTS = {
//     //     users: "/api/users",
//     //     tickets: "/api/admin/tickets",
//     //     ticket_gains: "/api/admin/ticket-gains"
//     // };
//     // ➜ endpoints réellement utilisés par le mini-HeidiSQL
//     const DB_ENDPOINTS = {
//         // on utilise les routes normales /api/users/** (JWT + rôle ADMIN déjà OK)
//         users: "/api/users",

//         // pour tickets & ticket_gains, on reste sur l'admin CRUD
//         tickets: "/api/admin/tickets",
//         ticket_gains: "/api/admin/ticket-gains"
//         // refresh_tokens: "/api/admin/refresh-tokens"
//     };


//     let dbCurrentResource = "users";
//     let dbRawData = [];
//     let dbFilteredData = [];
//     let dbCurrentPage = 0;
//     let dbEditingRow = null; // {resource, id, mode:'create'|'edit'}

//     function applyDbFilter() {
//         const q = dbSearch.value.toLowerCase().trim();
//         if (!q) {
//             dbFilteredData = dbRawData.slice();
//         } else {
//             dbFilteredData = dbRawData.filter(row =>
//                 JSON.stringify(row).toLowerCase().includes(q)
//             );
//         }
//         dbCurrentPage = 0;
//         renderDbTable();
//     }

//     function renderDbTable() {
//         dbTableHead.innerHTML = "";
//         dbTableBody.innerHTML = "";

//         if (!dbFilteredData || dbFilteredData.length === 0) {
//             dbTableBody.innerHTML =
//                 "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
//             dbPagingInfo.textContent = "Page 1 / 1";
//             return;
//         }

//         const pageSize = parseInt(dbPageSize.value, 10) || 20;
//         const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//         if (dbCurrentPage >= totalPages) dbCurrentPage = totalPages - 1;

//         const start = dbCurrentPage * pageSize;
//         const end = Math.min(start + pageSize, dbFilteredData.length);
//         const pageData = dbFilteredData.slice(start, end);

//         const keys = Object.keys(pageData[0]);

//         // head
//         const headerRow = document.createElement("tr");
//         keys.forEach(k => {
//             const th = document.createElement("th");
//             th.textContent = k;
//             th.style.padding = "6px 8px";
//             th.style.borderBottom = "1px solid #111827";
//             th.style.textAlign = "left";
//             headerRow.appendChild(th);
//         });
//         const thActions = document.createElement("th");
//         thActions.textContent = "Actions";
//         thActions.style.padding = "6px 8px";
//         thActions.style.borderBottom = "1px solid #111827";
//         headerRow.appendChild(thActions);
//         dbTableHead.appendChild(headerRow);

//         // body
//         pageData.forEach(row => {
//             const tr = document.createElement("tr");
//             tr.style.borderBottom = "1px solid #020617";

//             keys.forEach(k => {
//                 const td = document.createElement("td");
//                 let value = row[k];
//                 if (value === null || value === undefined) value = "";
//                 if (typeof value === "object") value = JSON.stringify(value);
//                 td.textContent = String(value);
//                 td.style.padding = "6px 8px";
//                 td.style.verticalAlign = "top";
//                 td.style.maxWidth = "260px";
//                 td.style.wordBreak = "break-word";
//                 tr.appendChild(td);
//             });

//             const tdActions = document.createElement("td");
//             tdActions.style.padding = "6px 8px";
//             tdActions.style.whiteSpace = "nowrap";

//             const id = row.id || row._id;

//             const btnEdit = document.createElement("button");
//             btnEdit.textContent = "Modifier";
//             btnEdit.style.padding = "3px 8px";
//             btnEdit.style.borderRadius = "999px";
//             btnEdit.style.border = "none";
//             btnEdit.style.fontSize = "0.75rem";
//             btnEdit.style.background = "#3b82f6";
//             btnEdit.style.color = "#020617";
//             btnEdit.style.cursor = "pointer";
//             btnEdit.style.marginRight = "4px";
//             btnEdit.addEventListener("click", () => openDbModal("edit", row));

//             const btnDelete = document.createElement("button");
//             btnDelete.textContent = "Supprimer";
//             btnDelete.style.padding = "3px 8px";
//             btnDelete.style.borderRadius = "999px";
//             btnDelete.style.border = "none";
//             btnDelete.style.fontSize = "0.75rem";
//             btnDelete.style.background = "#ef4444";
//             btnDelete.style.color = "#020617";
//             btnDelete.style.cursor = "pointer";

//             btnDelete.addEventListener("click", async () => {
//                 if (!id) { alert("ID introuvable."); return; }
//                 if (!confirm("Supprimer " + dbCurrentResource + " #" + id + " ?")) return;
//                 const url = DB_ENDPOINTS[dbCurrentResource] + "/" + id;
//                 try {
//                     const delRes = await fetch(url, {
//                         method: "DELETE",
//                         headers: {
//                             ...getAuthHeaders()
//                         },
//                         credentials: "include"
//                     });
//                     if (!delRes.ok) {
//                         alert("Erreur " + delRes.status + " lors de la suppression.");
//                         return;
//                     }
//                     dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
//                     applyDbFilter();
//                 } catch (e) {
//                     console.error(e);
//                     alert("Erreur réseau lors de la suppression.");
//                 }
//             });

//             tdActions.appendChild(btnEdit);
//             tdActions.appendChild(btnDelete);
//             tr.appendChild(tdActions);
//             dbTableBody.appendChild(tr);
//         });

//         dbPagingInfo.textContent = "Page " + (dbCurrentPage + 1) + " / " + totalPages;
//     }

//     async function loadDbData() {
//         dbCurrentResource = dbResourceSelect.value;
//         const url = DB_ENDPOINTS[dbCurrentResource];

//         dbTableHead.innerHTML = "";
//         dbTableBody.innerHTML =
//             "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";

//         if (!url) {
//             dbTableBody.innerHTML =
//                 "<tr><td style='padding:8px;color:#f97373;'>Endpoint non configuré.</td></tr>";
//             return;
//         }

//         try {
//             const res = await fetch(url, {
//                 method: "GET",
//                 headers: {
//                     ...getAuthHeaders()
//                 },
//                 credentials: "include"
//             });
//             if (!res.ok) {
//                 dbTableBody.innerHTML =
//                     "<tr><td style='padding:8px;color:#f97373;'>Erreur " + res.status + ".</td></tr>";
//                 return;
//             }
//             const data = await res.json();
//             if (!Array.isArray(data) || data.length === 0) {
//                 dbRawData = [];
//                 applyDbFilter();
//                 return;
//             }
//             dbRawData = data;
//             applyDbFilter();
//         } catch (e) {
//             console.error(e);
//             dbTableBody.innerHTML =
//                 "<tr><td style='padding:8px;color:#f97373;'>Erreur réseau.</td></tr>";
//         }
//     }

//     btnLoadData.addEventListener("click", loadDbData);
//     dbSearch.addEventListener("input", () => applyDbFilter());
//     dbPageSize.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
//     dbPrevPage.addEventListener("click", () => {
//         if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); }
//     });
//     dbNextPage.addEventListener("click", () => {
//         const pageSize = parseInt(dbPageSize.value, 10) || 20;
//         const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
//         if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
//     });

//     function openDbModal(mode, row) {
//         dbEditingRow = { resource: dbCurrentResource, mode, row: row || null };
//         dbModalForm.innerHTML = "";

//         const baseRow = row || (dbRawData[0] || {});
//         const keys = Object.keys(baseRow);

//         const protectedFields = ["id", "_id", "created_at", "updated_at", "createdAt", "updatedAt"];

//         keys.forEach(k => {
//             const wrapper = document.createElement("div");
//             wrapper.style.display = "flex";
//             wrapper.style.flexDirection = "column";
//             wrapper.style.gap = "2px";

//             const label = document.createElement("label");
//             label.textContent = k;
//             label.style.fontSize = "0.8rem";
//             label.style.color = "#9ca3af";

//             const input = document.createElement("input");
//             input.name = k;
//             input.value = row ? (row[k] ?? "") : "";
//             input.style.background = "#020617";
//             input.style.color = "#e5e7eb";
//             input.style.borderRadius = "8px";
//             input.style.border = "1px solid #4b5563";
//             input.style.padding = "6px 8px";
//             input.style.fontSize = "0.8rem";

//             if (protectedFields.includes(k)) {
//                 input.disabled = true;
//                 input.style.opacity = "0.6";
//             }

//             wrapper.appendChild(label);
//             wrapper.appendChild(input);
//             dbModalForm.appendChild(wrapper);
//         });

//         dbModalTitle.textContent =
//             mode === "edit" ? "Modifier la ligne #" + (row.id || row._id) : "Nouvelle ligne";
//         dbModalOverlay.style.display = "flex";
//     }

//     function closeDbModal() {
//         dbModalOverlay.style.display = "none";
//         dbEditingRow = null;
//     }

//     dbModalCancel.addEventListener("click", closeDbModal);

//     dbModalSave.addEventListener("click", async () => {
//         if (!dbEditingRow) return;
//         const formData = new FormData(dbModalForm);
//         const obj = {};
//         formData.forEach((v, k) => {
//             const input = dbModalForm.querySelector('input[name="' + k + '"]');
//             if (input && input.disabled) return;
//             obj[k] = v === "" ? null : v;
//         });

//         const resource = dbEditingRow.resource;
//         const baseUrl = DB_ENDPOINTS[resource];
//         let method = "POST";
//         let url = baseUrl;

//         if (dbEditingRow.mode === "edit") {
//             const id = dbEditingRow.row.id || dbEditingRow.row._id;
//             method = "PUT";
//             url = baseUrl + "/" + id;
//         }

//         try {
//             const res = await fetch(url, {
//                 method,
//                 headers: {
//                     "Content-Type": "application/json",
//                     ...getAuthHeaders()
//                 },
//                 credentials: "include",
//                 body: JSON.stringify(obj)
//             });
//             if (!res.ok) {
//                 alert("Erreur " + res.status + " lors de l’enregistrement.");
//                 return;
//             }
//             closeDbModal();
//             await loadDbData();
//         } catch (e) {
//             console.error(e);
//             alert("Erreur réseau lors de l’enregistrement.");
//         }
//     });

//     btnNewRow.addEventListener("click", () => openDbModal("create", null));

//     // ---------- STATS JOUEURS / TICKETS ----------
//     const btnLoadStats = document.getElementById("btnLoadStats");
//     const statsGrid = document.getElementById("statsGrid");
//     const statsSort = document.getElementById("statsSort");
//     const statsSearch = document.getElementById("statsSearch");

//     const STATS_ENDPOINT = "/api/admin/users-stats";

//     let statsRaw = [];
//     let statsFiltered = [];

//     function applyStatsFilter() {
//         const q = statsSearch.value.toLowerCase().trim();
//         if (!q) statsFiltered = statsRaw.slice();
//         else {
//             statsFiltered = statsRaw.filter(u =>
//                 (u.email || "").toLowerCase().includes(q) ||
//                 (u.firstName || "").toLowerCase().includes(q) ||
//                 (u.lastName || "").toLowerCase().includes(q)
//             );
//         }

//         const sortKey = statsSort.value;
//         statsFiltered.sort((a, b) => {
//             const tA = a.ticketsCount || 0;
//             const tB = b.ticketsCount || 0;
//             const gA = a.totalGain || 0;
//             const gB = b.totalGain || 0;
//             const dA = a.lastTicketDate ? new Date(a.lastTicketDate).getTime() : 0;
//             const dB = b.lastTicketDate ? new Date(b.lastTicketDate).getTime() : 0;

//             if (sortKey === "ticketsDesc") return tB - tA;
//             if (sortKey === "gainDesc") return gB - gA;
//             if (sortKey === "lastTicketDesc") return dB - dA;
//             return 0;
//         });

//         renderStats();
//     }

//     function renderStats() {
//         statsGrid.innerHTML = "";
//         if (!statsFiltered || statsFiltered.length === 0) {
//             statsGrid.innerHTML =
//                 "<div style='color:#9ca3af;font-size:0.85rem;'>Aucune statistique disponible.</div>";
//             return;
//         }

//         statsFiltered.forEach(u => {
//             const card = document.createElement("article");
//             card.style.borderRadius = "14px";
//             card.style.border = "1px solid #111827";
//             card.style.background = "linear-gradient(135deg,#020617,#020617,#020617)";
//             card.style.padding = "10px 12px";
//             card.style.display = "flex";
//             card.style.flexDirection = "column";
//             card.style.gap = "6px";

//             const header = document.createElement("div");
//             header.style.display = "flex";
//             header.style.justifyContent = "space-between";
//             header.style.alignItems = "center";

//             const title = document.createElement("div");
//             title.innerHTML =
//                 "<div style='font-size:0.9rem;font-weight:600;'>" +
//                 (u.firstName || "") + " " + (u.lastName || "") + "</div>" +
//                 "<div style='font-size:0.78rem;color:#9ca3af;'>" + (u.email || "") + "</div>";

//             const badge = document.createElement("span");
//             badge.textContent = u.admin ? "ADMIN" : "USER";
//             badge.style.fontSize = "0.7rem";
//             badge.style.padding = "2px 8px";
//             badge.style.borderRadius = "999px";
//             badge.style.border = "1px solid " + (u.admin ? "#22c55e" : "#4b5563");
//             badge.style.color = u.admin ? "#22c55e" : "#9ca3af";

//             header.appendChild(title);
//             header.appendChild(badge);

//             const statsRow = document.createElement("div");
//             statsRow.style.display = "grid";
//             statsRow.style.gridTemplateColumns = "repeat(3,minmax(0,1fr))";
//             statsRow.style.gap = "6px";
//             statsRow.style.marginTop = "4px";

//             const blocks = [
//                 { label: "Tickets", value: u.ticketsCount || 0 },
//                 { label: "Gain total", value: (u.totalGain || 0) + " €" },
//                 { label: "Meilleur gain", value: (u.bestGain || 0) + " €" }
//             ];
//             blocks.forEach(b => {
//                 const div = document.createElement("div");
//                 div.style.fontSize = "0.75rem";
//                 div.style.color = "#9ca3af";
//                 div.innerHTML =
//                     "<div>" + b.label + "</div>" +
//                     "<div style='color:#e5e7eb;font-weight:600;font-size:0.86rem;'>" + b.value + "</div>";
//                 statsRow.appendChild(div);
//             });

//             const footer = document.createElement("div");
//             footer.style.marginTop = "4px";
//             footer.style.fontSize = "0.75rem";
//             footer.style.color = "#6b7280";
//             footer.textContent =
//                 "Dernier ticket : " + (u.lastTicketDate || "—") +
//                 " | ID utilisateur : " + (u.id || u._id || "—");

//             card.appendChild(header);
//             card.appendChild(statsRow);
//             card.appendChild(footer);
//             statsGrid.appendChild(card);
//         });
//     }

//     async function loadStats() {
//         statsGrid.innerHTML =
//             "<div style='color:#9ca3af;font-size:0.85rem;'>Chargement des statistiques...</div>";
//         try {
//             const res = await fetch(STATS_ENDPOINT, {
//                 method: "GET",
//                 headers: {
//                     ...getAuthHeaders()
//                 },
//                 credentials: "include"
//             });
//             if (!res.ok) {
//                 statsGrid.innerHTML =
//                     "<div style='color:#f97373;font-size:0.85rem;'>Erreur " + res.status + ".</div>";
//                 return;
//             }
//             const data = await res.json();
//             if (!Array.isArray(data)) {
//                 statsGrid.innerHTML =
//                     "<div style='color:#f97373;font-size:0.85rem;'>Réponse invalide du serveur.</div>";
//                 return;
//             }
//             statsRaw = data;
//             applyStatsFilter();
//         } catch (e) {
//             console.error(e);
//             statsGrid.innerHTML =
//                 "<div style='color:#f97373;font-size:0.85rem;'>Erreur réseau.</div>";
//         }
//     }

//     btnLoadStats.addEventListener("click", loadStats);
//     statsSort.addEventListener("change", applyStatsFilter);
//     statsSearch.addEventListener("input", applyStatsFilter);

//     // ---------- LOGOUT ----------
//     document.getElementById("btnLogout").addEventListener("click", async () => {
//         try {
//             await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
//         } catch (e) {
//             console.error(e);
//         }
//         window.location.href = "/admin-login.html";
//     });

//     // ---------- INIT ----------
//     showSection("swagger");
//     refreshLogs();      // premier chargement
//     loadDbData();       // charge users par défaut
// </script>
// </body>
// </html>
// """;
//     }

//     // Endpoint pour lire les logs (appelé par JS)
//     @GetMapping(value = "/logs", produces = MediaType.TEXT_PLAIN_VALUE)
//     public ResponseEntity<String> getLogs(@RequestParam(defaultValue = "400") int lines) throws IOException {
//         if (!Files.exists(LOG_PATH)) {
//             return ResponseEntity.ok("Fichier de log introuvable : " + LOG_PATH.toAbsolutePath());
//         }
//         List<String> all = Files.readAllLines(LOG_PATH);
//         int fromIndex = Math.max(0, all.size() - lines);
//         return ResponseEntity.ok(String.join("\n", all.subList(fromIndex, all.size())));
//     }
// }


package com.fdjloto.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminDashboardController {

    private static final Path LOG_PATH = Paths.get("logs/application.log");

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public String adminDashboard() {

        return """
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Dashboard admin - Loto Tracker</title>
  <link rel="icon" href="http://localhost:5500/favicon-admin.ico">
  <link rel="icon" href="https://stephanedinahet.fr/favicon-admin.ico">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    .nav-item{
      width:100%;display:flex;align-items:center;gap:8px;padding:8px 10px;border-radius:10px;border:none;cursor:pointer;text-align:left;
      background:transparent;color:#9ca3af;border-left:3px solid transparent;font-size:.85rem;
      transition:background .18s ease,color .18s ease,border-color .18s ease,transform .10s ease;
    }
    .nav-item:hover{background:#1e293b;color:#e5e7eb;border-left-color:#22c55e;transform:translateX(2px);}
    .nav-item.active{background:#334155;color:#f9fafb;font-weight:600;border-left-color:#22c55e;}

    .pill{padding:6px 14px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;font-size:.8rem;}
    .pill-green{border:none;background:#22c55e;color:#020617;font-weight:700;}
    .input{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:4px 10px;font-size:.8rem;}
    .select{background:#020617;color:#e5e7eb;border-radius:999px;border:1px solid #4b5563;padding:3px 8px;}
    .muted{color:#9ca3af;}
    .danger{color:#f97373;}
    code{background:#0b1220;border:1px solid #111827;border-radius:8px;padding:2px 6px;}
  </style>
</head>

<body style="background:#020617;color:#e5e7eb;font-family:system-ui,sans-serif;margin:0;">
<div style="min-height:100vh;display:flex;">

  <aside style="width:240px;background:#020617;border-right:1px solid #111827;
                padding:18px 14px;box-sizing:border-box;display:flex;flex-direction:column;gap:18px;
                position:sticky;top:0;align-self:flex-start;height:100vh;">
    <div>
      <div style="font-size:.75rem;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;">Loto Tracker API</div>
      <div style="margin-top:4px;font-size:1.1rem;font-weight:600;">Admin console</div>
    </div>

    <nav style="display:flex;flex-direction:column;gap:6px;font-size:.85rem;">
      <button class="nav-item active" data-section="swagger"><span>*</span><span>Swagger / API</span></button>
      <button class="nav-item" data-section="logs"><span>*</span><span>Logs serveur</span></button>
      <button class="nav-item" data-section="db"><span>*</span><span>Base de données (CRUD)</span></button>
      <button class="nav-item" data-section="stats"><span>*</span><span>Stats joueurs & tickets</span></button>
    </nav>

    <div style="margin-top:auto;font-size:.75rem;color:#6b7280;line-height:1.4;">
      Connecté en tant qu'<span style="color:#e5e7eb;">administrateur</span>.<br>
      Profil Spring : <span style="color:#22c55e;">dev</span>
    </div>
  </aside>

  <div style="flex:1;display:flex;flex-direction:column;min-width:0;">
    <header style="padding:14px 20px;border-bottom:1px solid #111827;display:flex;justify-content:space-between;align-items:center;">
      <div>
        <h1 id="pageTitle" style="margin:0;font-size:1.3rem;">Tableau de bord administrateur</h1>
        <p id="pageSubtitle" style="margin:2px 0 0;font-size:.8rem;color:#9ca3af;">
          Vue générale : Swagger, logs serveur, base de données et statistiques.
        </p>
      </div>
      <button id="btnLogout" type="button" class="pill">Déconnexion</button>
    </header>

    <main style="flex:1;padding:16px 20px;display:flex;flex-direction:column;gap:14px;min-height:0;">

      <!-- SWAGGER -->
      <section id="section-swagger" class="section-panel" style="flex:1;display:flex;flex-direction:column;gap:8px;min-height:0;">
        <p class="muted" style="margin:0 0 4px;font-size:.85rem;">Documentation Swagger de l'API Loto Tracker.</p>
        <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
          <button id="btnOpenSwagger" type="button" class="pill pill-green">Ouvrir Swagger dans un nouvel onglet</button>
          <span style="font-size:.8rem;color:#6b7280;">ou utilisez la vue intégrée ci-dessous.</span>
        </div>
        <div style="flex:1;min-height:0;margin-top:8px;">
          <iframe id="swaggerFrame" src="/swagger-ui/index.html"
                  style="width:100%;height:100%;border-radius:12px;border:1px solid #111827;background:#020617;"></iframe>
        </div>
      </section>

      <!-- LOGS -->
      <section id="section-logs" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:8px;min-height:0;">
        <div style="display:flex;flex-wrap:wrap;gap:10px;align-items:center;font-size:.8rem;">
          <span class="muted">Logs du backend (lecture seule).</span>
          <button id="btnRefreshLogs" type="button" class="pill pill-green">🔄 Rafraîchir</button>

          <label class="muted">
            Lignes :
            <select id="logLineCount" class="select">
              <option value="200">200</option>
              <option value="400" selected>400</option>
              <option value="800">800</option>
            </select>
          </label>

          <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
            <input type="checkbox" id="autoScrollLogs" checked> Auto-scroll
          </label>
          <label style="display:flex;align-items:center;gap:4px;color:#9ca3af;">
            <input type="checkbox" id="autoRefreshLogs" checked> Auto-refresh (3s)
          </label>

          <span id="logsLastRefresh" style="color:#6b7280;">Dernier refresh : -</span>
        </div>

        <div id="logsContainer"
             style="flex:1;min-height:0;overflow-y:auto;border-radius:12px;border:1px solid #111827;
                    background:#020617;padding:10px;font-family:monospace;font-size:11px;white-space:pre-wrap;">
          Chargement des logs...
        </div>
      </section>

      <!-- DB CRUD -->
      <section id="section-db" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
        <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
          <span class="muted">Vue base de données (CRUD simplifié).</span>

          <label class="muted">
            Table :
            <select id="dbResourceSelect" class="select">
              <option value="users">public.users</option>
              <option value="tickets">public.tickets</option>

              <!-- ✅ ici: ticket-gains -->
              <option value="ticket_gains">public.ticket-gains</option>

              <option value="refresh_tokens">public.refresh_tokens</option>
            </select>
          </label>

          <button id="btnLoadData" type="button" class="pill pill-green">Charger</button>
          <button id="btnNewRow" type="button" class="pill">➕ Nouvelle ligne</button>

          <span id="dbInfoText" style="color:#6b7280;font-size:.8rem;">
            Champs sensibles masqués. refresh_tokens = lecture seule.
          </span>
          <span id="dbStatus" style="color:#6b7280;font-size:.8rem;"></span>
        </div>

        <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;font-size:.8rem;">
          <label class="muted">
            Recherche :
            <input id="dbSearch" type="text" placeholder="Texte à filtrer..." class="input">
          </label>

          <label class="muted">
            Lignes / page :
            <select id="dbPageSize" class="select">
              <option value="20" selected>20</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
          </label>

          <span id="dbPagingInfo" style="color:#6b7280;">Page 1 / 1</span>

          <div style="display:flex;gap:4px;">
            <button id="dbPrevPage" type="button" class="pill" style="font-size:.75rem;">◀</button>
            <button id="dbNextPage" type="button" class="pill" style="font-size:.75rem;">▶</button>
          </div>
        </div>

        <div id="dbTableWrapper" style="flex:1;min-height:0;overflow:auto;border-radius:12px;border:1px solid #111827;background:#020617;">
          <table id="dbTable" style="border-collapse:collapse;width:100%;font-size:.8rem;">
            <thead id="dbTableHead"></thead>
            <tbody id="dbTableBody"></tbody>
          </table>
        </div>

        <!-- MODAL -->
        <div id="dbModalOverlay"
             style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.7);
                    align-items:center;justify-content:center;z-index:40;">
          <div style="background:#020617;border-radius:16px;border:1px solid #111827;
                      padding:16px 18px;width:420px;max-width:95%;">
            <h2 id="dbModalTitle" style="margin:0 0 8px;font-size:1rem;">Nouvelle ligne</h2>
            <form id="dbModalForm" style="display:flex;flex-direction:column;gap:8px;max-height:60vh;overflow:auto;"></form>
            <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;">
              <button id="dbModalCancel" type="button" class="pill">Annuler</button>
              <button id="dbModalSave" type="button" class="pill pill-green">Enregistrer</button>
            </div>
          </div>
        </div>
      </section>

      <!-- STATS -->
      <section id="section-stats" class="section-panel" style="flex:1;display:none;flex-direction:column;gap:10px;min-height:0;font-size:.85rem;">
        <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
          <span class="muted">Analyse des joueurs et de leurs tickets.</span>
          <button id="btnLoadStats" type="button" class="pill pill-green">Recharger les stats</button>

          <label class="muted">
            Filtrer par email :
            <input id="statsSearch" type="text" placeholder="user@hbnb.com" class="input">
          </label>

          <span id="statsStatus" style="color:#6b7280;"></span>
        </div>

        <div id="statsGrid"
             style="flex:1;min-height:0;overflow:auto;display:grid;
                    grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
                    gap:10px;padding-top:4px;"></div>
      </section>

    </main>
  </div>
</div>

<script>
  // ✅ JWT en cookie => ne PAS lire localStorage, ne PAS ajouter Authorization
  function getAuthHeaders() {
    return {};
  }

  // ✅ IMPORTANT : toujours envoyer le cookie
  const FETCH_OPTS = { credentials: "include" };

  // ---------- NAV ----------
  const navItems = document.querySelectorAll(".nav-item");
  const sections = {
    swagger: document.getElementById("section-swagger"),
    logs: document.getElementById("section-logs"),
    db: document.getElementById("section-db"),
    stats: document.getElementById("section-stats")
  };
  const pageTitle = document.getElementById("pageTitle");
  const pageSubtitle = document.getElementById("pageSubtitle");

  const subtitles = {
    swagger: "Documentation Swagger et tests de l’API.",
    logs: "Suivi en temps réel des logs Spring Boot.",
    db: "Exploration et modification des tables principales (CRUD).",
    stats: "Vue synthétique par joueur : tickets, gains, dernier tirage, etc."
  };

  function showSection(key) {
    Object.keys(sections).forEach(k => sections[k].style.display = (k === key) ? "flex" : "none");

    navItems.forEach(btn => {
      const active = (btn.getAttribute("data-section") === key);
      btn.classList.toggle("active", active);
      btn.style.background = active ? "#111827" : "transparent";
      btn.style.color = active ? "#e5e7eb" : "#9ca3af";
    });

    pageTitle.textContent = "Tableau de bord administrateur";
    pageSubtitle.textContent = subtitles[key] || "";

    if (key === "logs") refreshLogs();
    if (key === "stats") loadStats();
  }

  navItems.forEach(btn => btn.addEventListener("click", () => showSection(btn.getAttribute("data-section"))));

  // ---------- SWAGGER ----------
  document.getElementById("btnOpenSwagger").addEventListener("click", () => window.open("/swagger-ui/index.html", "_blank"));

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
      const res = await fetch(`/admin/logs?lines=${encodeURIComponent(lines)}`, {
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
      logsContainer.textContent += "\\n[ERREUR] Impossible de charger les logs.";
    }
  }

  btnRefreshLogs.addEventListener("click", refreshLogs);
  logLineCountSelect.addEventListener("change", refreshLogs);
  setInterval(() => { if (autoRefreshLogsCheckbox.checked) refreshLogs(); }, 3000);

  // ---------- DB / CRUD ----------
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

  // ✅ IMPORTANT: pour éviter que "ticket-gains" casse alors que "ticket_gain" marche,
  // on mappe les 2 vers LE MEME endpoint backend.
  const DB_ENDPOINTS = {
    users: "/api/admin/users",
    tickets: "/api/admin/tickets",
    ticket_gains: "/api/admin/ticket-gains",
    refresh_tokens: "/api/admin/refresh-tokens"
  };

  // ✅ refresh_tokens en lecture seule (si tu l'exposes)
  const READ_ONLY_RESOURCES = new Set(["refresh_tokens"]);

  // ✅ RGPD : champs à masquer
  const SENSITIVE_PARTS = ["password","hashed","hash","token","refresh","access","secret","apikey","api_key","session","cookie","reset"];
  function isSensitiveKey(key) {
    if (!key) return false;
    const k = String(key).toLowerCase();
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
    if (Array.isArray(value)) return JSON.stringify(value);
    if (typeof value === "object") return JSON.stringify(value);
    return String(value);
  }

  let dbCurrentResource = "users";
  let dbRawData = [];
  let dbFilteredData = [];
  let dbCurrentPage = 0;
  let dbEditingRow = null;

  function setDbStatus(text, isError=false) {
    dbStatus.textContent = text;
    dbStatus.className = isError ? "danger" : "muted";
  }

  function applyDbFilter() {
    const q = dbSearch.value.toLowerCase().trim();
    if (!q) dbFilteredData = dbRawData.slice();
    else dbFilteredData = dbRawData.filter(row => JSON.stringify(row).toLowerCase().includes(q));
    dbCurrentPage = 0;
    renderDbTable();
  }

  function renderDbTable() {
    dbTableHead.innerHTML = "";
    dbTableBody.innerHTML = "";

    const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
    btnNewRow.disabled = isReadOnly;
    btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
    btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

    if (!dbFilteredData || dbFilteredData.length === 0) {
      dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Aucune donnée.</td></tr>";
      dbPagingInfo.textContent = "Page 1 / 1";
      return;
    }

    const pageSize = parseInt(dbPageSize.value, 10) || 20;
    const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
    if (dbCurrentPage >= totalPages) dbCurrentPage = totalPages - 1;

    const start = dbCurrentPage * pageSize;
    const end = Math.min(start + pageSize, dbFilteredData.length);
    const pageData = dbFilteredData.slice(start, end);

    const keys = Object.keys(pageData[0] || {});
    const headerRow = document.createElement("tr");
    keys.forEach(k => {
      const th = document.createElement("th");
      th.textContent = k;
      th.style.padding = "6px 8px";
      th.style.borderBottom = "1px solid #111827";
      th.style.textAlign = "left";
      headerRow.appendChild(th);
    });

    if (!isReadOnly) {
      const thActions = document.createElement("th");
      thActions.textContent = "Actions";
      thActions.style.padding = "6px 8px";
      thActions.style.borderBottom = "1px solid #111827";
      headerRow.appendChild(thActions);
    }

    dbTableHead.appendChild(headerRow);

    pageData.forEach(row => {
      const tr = document.createElement("tr");
      tr.style.borderBottom = "1px solid #020617";

      keys.forEach(k => {
        const td = document.createElement("td");
        td.textContent = safeCellValue(k, row[k]);
        td.style.padding = "6px 8px";
        td.style.verticalAlign = "top";
        td.style.maxWidth = "320px";
        td.style.wordBreak = "break-word";
        tr.appendChild(td);
      });

      if (!isReadOnly) {
        const tdActions = document.createElement("td");
        tdActions.style.padding = "6px 8px";
        tdActions.style.whiteSpace = "nowrap";

        const id = row.id || row._id;

        const btnEdit = document.createElement("button");
        btnEdit.textContent = "Modifier";
        btnEdit.style.padding = "3px 8px";
        btnEdit.style.borderRadius = "999px";
        btnEdit.style.border = "none";
        btnEdit.style.fontSize = "0.75rem";
        btnEdit.style.background = "#3b82f6";
        btnEdit.style.color = "#020617";
        btnEdit.style.cursor = "pointer";
        btnEdit.style.marginRight = "4px";
        btnEdit.addEventListener("click", () => openDbModal("edit", row));

        const btnDelete = document.createElement("button");
        btnDelete.textContent = "Supprimer";
        btnDelete.style.padding = "3px 8px";
        btnDelete.style.borderRadius = "999px";
        btnDelete.style.border = "none";
        btnDelete.style.fontSize = "0.75rem";
        btnDelete.style.background = "#ef4444";
        btnDelete.style.color = "#020617";
        btnDelete.style.cursor = "pointer";

        btnDelete.addEventListener("click", async () => {
          if (!id) { alert("ID introuvable."); return; }
          if (!confirm("Supprimer " + dbCurrentResource + " #" + id + " ?")) return;

          const url = DB_ENDPOINTS[dbCurrentResource] + "/" + id;
          try {
            const delRes = await fetch(url, { method: "DELETE", headers: { ...getAuthHeaders() }, credentials: "include" });
            if (!delRes.ok) {
              const t = await delRes.text().catch(() => "");
              alert("Erreur " + delRes.status + " lors de la suppression.\\n" + t);
              return;
            }
            dbRawData = dbRawData.filter(r => (r.id || r._id) !== id);
            applyDbFilter();
          } catch (e) {
            console.error(e);
            alert("Erreur réseau lors de la suppression.");
          }
        });

        tdActions.appendChild(btnEdit);
        tdActions.appendChild(btnDelete);
        tr.appendChild(tdActions);
      }

      dbTableBody.appendChild(tr);
    });

    dbPagingInfo.textContent = "Page " + (dbCurrentPage + 1) + " / " + totalPages;
  }

  async function loadDbData() {
    dbCurrentResource = dbResourceSelect.value;
    const url = DB_ENDPOINTS[dbCurrentResource];

    const isReadOnly = READ_ONLY_RESOURCES.has(dbCurrentResource);
    btnNewRow.disabled = isReadOnly;
    btnNewRow.style.opacity = isReadOnly ? "0.45" : "1";
    btnNewRow.style.cursor = isReadOnly ? "not-allowed" : "pointer";

    dbTableHead.innerHTML = "";
    dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#9ca3af;'>Chargement...</td></tr>";
    setDbStatus("Chargement...");

    if (!url) {
      setDbStatus("Endpoint non configuré", true);
      dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Endpoint non configuré.</td></tr>";
      return;
    }

    try {
      const res = await fetch(url, { method: "GET", headers: { ...getAuthHeaders() }, credentials: "include" });

      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        setDbStatus("Erreur " + res.status, true);
        dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur " + res.status + ". " + (txt || "") + "</td></tr>";
        return;
      }

      const data = await res.json();
      if (!Array.isArray(data) || data.length === 0) {
        dbRawData = [];
        setDbStatus("0 ligne");
        applyDbFilter();
        return;
      }

      dbRawData = data;
      setDbStatus(data.length + " lignes");
      applyDbFilter();

    } catch (e) {
      console.error(e);
      setDbStatus("Erreur réseau", true);
      dbTableBody.innerHTML = "<tr><td style='padding:8px;color:#f97373;'>Erreur réseau.</td></tr>";
    }
  }

  btnLoadData.addEventListener("click", loadDbData);
  dbSearch.addEventListener("input", applyDbFilter);
  dbPageSize.addEventListener("change", () => { dbCurrentPage = 0; renderDbTable(); });
  dbPrevPage.addEventListener("click", () => { if (dbCurrentPage > 0) { dbCurrentPage--; renderDbTable(); }});
  dbNextPage.addEventListener("click", () => {
    const pageSize = parseInt(dbPageSize.value, 10) || 20;
    const totalPages = Math.max(1, Math.ceil(dbFilteredData.length / pageSize));
    if (dbCurrentPage < totalPages - 1) { dbCurrentPage++; renderDbTable(); }
  });

  function openDbModal(mode, row) {
    if (READ_ONLY_RESOURCES.has(dbCurrentResource)) {
      alert("Cette table est en lecture seule (données sensibles).");
      return;
    }

    dbEditingRow = { resource: dbCurrentResource, mode, row: row || null };
    dbModalForm.innerHTML = "";

    const baseRow = row || (dbRawData[0] || {});
    const keys = Object.keys(baseRow);

    const protectedFields = ["id","_id","created_at","updated_at","createdAt","updatedAt"];

    keys.forEach(k => {
      const wrapper = document.createElement("div");
      wrapper.style.display = "flex";
      wrapper.style.flexDirection = "column";
      wrapper.style.gap = "2px";

      const label = document.createElement("label");
      label.textContent = k + (isSensitiveKey(k) ? " (masqué)" : "");
      label.style.fontSize = "0.8rem";
      label.style.color = "#9ca3af";

      const input = document.createElement("input");
      input.name = k;
      input.value = row ? (row[k] ?? "") : "";
      input.style.background = "#020617";
      input.style.color = "#e5e7eb";
      input.style.borderRadius = "8px";
      input.style.border = "1px solid #4b5563";
      input.style.padding = "6px 8px";
      input.style.fontSize = "0.8rem";

      if (protectedFields.includes(k) || isSensitiveKey(k)) {
        input.disabled = true;
        input.style.opacity = "0.6";
        if (isSensitiveKey(k)) input.value = maskValue(input.value);
      }

      wrapper.appendChild(label);
      wrapper.appendChild(input);
      dbModalForm.appendChild(wrapper);
    });

    dbModalTitle.textContent = (mode === "edit")
      ? "Modifier la ligne #" + (row.id || row._id)
      : "Nouvelle ligne";

    dbModalOverlay.style.display = "flex";
  }

  function closeDbModal() {
    dbModalOverlay.style.display = "none";
    dbEditingRow = null;
  }

  dbModalCancel.addEventListener("click", closeDbModal);

  dbModalSave.addEventListener("click", async () => {
    if (!dbEditingRow) return;

    const formData = new FormData(dbModalForm);
    const obj = {};

    formData.forEach((v, k) => {
      const input = dbModalForm.querySelector('input[name="' + k + '"]');
      if (input && input.disabled) return;
      obj[k] = v === "" ? null : v;
    });

    const resource = dbEditingRow.resource;
    const baseUrl = DB_ENDPOINTS[resource];
    let method = "POST";
    let url = baseUrl;

    if (dbEditingRow.mode === "edit") {
      const id = dbEditingRow.row.id || dbEditingRow.row._id;
      method = "PUT";
      url = baseUrl + "/" + id;
    }

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json", ...getAuthHeaders() },
        credentials: "include",
        body: JSON.stringify(obj)
      });

      if (!res.ok) {
        const t = await res.text().catch(() => "");
        alert("Erreur " + res.status + " lors de l’enregistrement.\\n" + t);
        return;
      }

      closeDbModal();
      await loadDbData();

    } catch (e) {
      console.error(e);
      alert("Erreur réseau lors de l’enregistrement.");
    }
  });

  btnNewRow.addEventListener("click", () => openDbModal("create", null));

  // ---------- STATS ----------
  const btnLoadStats = document.getElementById("btnLoadStats");
  const statsGrid = document.getElementById("statsGrid");
  const statsSearch = document.getElementById("statsSearch");
  const statsStatus = document.getElementById("statsStatus");
  const STATS_ENDPOINT = "/api/admin/users-stats";

  let statsRaw = [];
  let statsFiltered = [];

  function setStatsStatus(text, isError=false) {
    statsStatus.textContent = text;
    statsStatus.className = isError ? "danger" : "muted";
  }

  function applyStatsFilter() {
    const q = statsSearch.value.toLowerCase().trim();
    if (!q) statsFiltered = statsRaw.slice();
    else statsFiltered = statsRaw.filter(u => (u.email || "").toLowerCase().includes(q));
    renderStats();
  }

  function renderStats() {
    statsGrid.innerHTML = "";
    if (!statsFiltered || statsFiltered.length === 0) {
      statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Aucune statistique disponible.</div>";
      return;
    }

    statsFiltered.forEach(u => {
      const card = document.createElement("article");
      card.style.borderRadius = "14px";
      card.style.border = "1px solid #111827";
      card.style.background = "#020617";
      card.style.padding = "10px 12px";
      card.style.display = "flex";
      card.style.flexDirection = "column";
      card.style.gap = "6px";

      const title = document.createElement("div");
      title.innerHTML =
        "<div style='font-size:.9rem;font-weight:600;'>" + (u.firstName || "") + " " + (u.lastName || "") + "</div>" +
        "<div style='font-size:.78rem;color:#9ca3af;'>" + (u.email || "") + "</div>";

      const footer = document.createElement("div");
      footer.style.marginTop = "4px";
      footer.style.fontSize = ".75rem";
      footer.style.color = "#6b7280";
      footer.textContent = "Tickets: " + (u.ticketsCount || 0) + " | Gain total: " + (u.totalGain || 0) + "€";

      card.appendChild(title);
      card.appendChild(footer);
      statsGrid.appendChild(card);
    });
  }

  async function loadStats() {
    statsGrid.innerHTML = "<div class='muted' style='font-size:.85rem;'>Chargement des statistiques...</div>";
    setStatsStatus("Chargement...");

    try {
      const res = await fetch(STATS_ENDPOINT, { method: "GET", headers: { ...getAuthHeaders() }, credentials: "include" });

      if (!res.ok) {
        const t = await res.text().catch(() => "");
        setStatsStatus("Erreur " + res.status, true);
        statsGrid.innerHTML =
          "<div class='danger' style='font-size:.85rem;line-height:1.4;'>" +
          "Endpoint stats indisponible : <code>" + STATS_ENDPOINT + "</code><br>" +
          "Code : " + res.status + "<br>" +
          (t ? ("<pre class='muted' style='white-space:pre-wrap;'>" + t + "</pre>") : "") +
          "</div>";
        return;
      }

      const data = await res.json();
      if (!Array.isArray(data)) {
        setStatsStatus("Réponse invalide", true);
        statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Réponse invalide du serveur.</div>";
        return;
      }

      statsRaw = data;
      setStatsStatus(data.length + " users");
      applyStatsFilter();

    } catch (e) {
      console.error(e);
      setStatsStatus("Erreur réseau", true);
      statsGrid.innerHTML = "<div class='danger' style='font-size:.85rem;'>Erreur réseau.</div>";
    }
  }

  btnLoadStats.addEventListener("click", loadStats);
  statsSearch.addEventListener("input", applyStatsFilter);

  // ---------- LOGOUT ----------
  document.getElementById("btnLogout").addEventListener("click", async () => {
    try { await fetch("/api/auth/logout", { method: "POST", credentials: "include" }); }
    catch (e) { console.error(e); }
    window.location.href = "/admin-login.html";
  });

  // ---------- INIT ----------
  showSection("swagger");
  refreshLogs();
  loadDbData();
</script>

</body>
</html>
""";
    }

    @GetMapping(value = "/logs", produces = "text/plain; charset=utf-8")
    public ResponseEntity<String> getLogs(@RequestParam(defaultValue = "400") int lines) throws IOException {
        if (!Files.exists(LOG_PATH)) {
            return ResponseEntity.ok("Fichier de log introuvable : " + LOG_PATH.toAbsolutePath());
        }
        List<String> all = Files.readAllLines(LOG_PATH, StandardCharsets.UTF_8);
        int safeLines = Math.max(1, Math.min(lines, 20000));
        int fromIndex = Math.max(0, all.size() - safeLines);
        return ResponseEntity.ok(String.join("\n", all.subList(fromIndex, all.size())));
    }
}
