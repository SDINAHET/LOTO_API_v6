/* layout-dashboard.js
   - Injecte topbar
   - Burger pilote la sidebar (#sidebar + .is-open + #sidebarOverlay)
   - Logout
   - ✅ apiFetch() : credentials + CSRF header auto
   - ✅ debugAuth() : diagnostique cookies/JWT/401 logs
*/
(function () {
  "use strict";

  // const HOST = window.location.hostname;
  // const IS_LOCAL = (HOST === "localhost" || HOST === "127.0.0.1");

  // // ✅ IMPORTANT : API locale forcée sur localhost pour matcher les cookies
  // const API_BASE = IS_LOCAL
  //   ? `${window.location.protocol}//${window.location.hostname}:8082`
  //   : "https://stephanedinahet.fr";

  const HOST = window.location.hostname;
  const IS_PROD =
    HOST === "stephanedinahet.fr" ||
    HOST === "www.stephanedinahet.fr";

  // si tu es en local (localhost, 127.0.0.1, 192.168.x.x, etc.)
  // l’API est sur le même host mais port 8082
  const API_BASE = IS_PROD
    ? "https://stephanedinahet.fr"
    : `${window.location.protocol}//${HOST}:8082`;



  const USERINFO_PATH = "/api/protected/userinfo";
  const LOGOUT_PATH = "/api/auth/logout";

  function ensureLayoutStyles() {
    if (document.getElementById("layoutDashboardStyles")) return;

    const style = document.createElement("style");
    style.id = "layoutDashboardStyles";
    style.textContent = `
      :root{
        --topbar-h: 72px;
        --stroke: rgba(255,255,255,.12);
        --text: rgba(255,255,255,.92);
        --muted: rgba(255,255,255,.65);
      }

      .topbar{
        position: fixed;
        top: 0; left: 0; right: 0;
        z-index: 1030;
        height: var(--topbar-h);
        display:flex;
        align-items:center;
        justify-content:space-between;
        gap: 14px;
        padding: 12px 16px;
        background: rgba(10,16,28,.82);
        backdrop-filter: blur(12px);
        border-bottom: 1px solid var(--stroke);
      }
      body{ padding-top: var(--topbar-h); }

      .brand{
        display:flex; align-items:center; gap: 12px;
        text-decoration:none; color: var(--text);
        min-width: 0;
      }
      .brand-logo{ width: 44px; height: 44px; object-fit: contain; }
      .brand-title{ font-weight: 900; letter-spacing:.2px; line-height: 1.1; white-space: nowrap; }
      .brand-sub{ font-size: .9rem; color: var(--muted); margin-top: 2px; }

      .topbar-actions{
        display:flex;
        align-items:center;
        gap: 10px;
        margin-left:auto;
      }

      .btn-ghost{
        display:inline-flex;
        align-items:center;
        justify-content:center;
        gap: 8px;
        padding:10px 12px;
        border-radius: 12px;
        border: 1px solid var(--stroke);
        background: rgba(255,255,255,.06);
        color: var(--text);
        text-decoration:none;
        font-weight: 800;
        white-space: nowrap;
        transition: transform .15s ease, background .15s ease;
        cursor: pointer;
      }
      .btn-ghost:hover{ transform: translateY(-1px); background: rgba(255,255,255,.08); }

      .btn-ico{
        width: 18px; height: 18px;
        fill: none;
        stroke: var(--text);
        stroke-width: 2;
        stroke-linecap: round;
        stroke-linejoin: round;
        opacity:.95;
      }

      .btn-burger{ width: 44px; height: 44px; padding: 0; border-radius: 12px; display:none; }
      @media (max-width: 900px){ .btn-burger{ display:inline-flex; } }

      .user-chip{
        background: transparent !important;
        border: none !important;
        box-shadow: none !important;
        padding: 0 !important;
        border-radius: 0 !important;
        display: inline-flex;
        align-items: center;
        gap: 12px;
      }
      .user-chip span{ color: var(--text); font-weight: 800; opacity: .95; }
      .user-chip b{ font-weight: 900; }

      #logoutBtn{
        display:inline-flex;
        align-items:center;
        gap:8px;
        padding:10px 12px;
        border-radius:12px;
        border:1px solid rgba(239,68,68,.25);
        background: rgba(239,68,68,.12);
        color: var(--text);
        font-weight: 800;
        cursor:pointer;
        transition: transform .15s ease, background .15s ease;
      }
      #logoutBtn:hover{ transform: translateY(-1px); background: rgba(239,68,68,.2); }
    `;
    document.head.appendChild(style);
  }

  function renderHeader() {
    return `
      <header class="topbar">
        <button class="btn-ghost btn-burger" id="burgerBtn" type="button" aria-label="Ouvrir le menu">
          <svg class="btn-ico" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 6h16"></path>
            <path d="M4 12h16"></path>
            <path d="M4 18h16"></path>
          </svg>
        </button>

        <a class="brand" href="../index.html" aria-label="Retour au site">
          <img src="/assets/img/loto_tracker.png" alt="Loto Tracker" class="brand-logo">
          <div>
            <div class="brand-title">Admin console</div>
            <div class="brand-sub">Tableau de bord</div>
          </div>
        </a>

        <div class="topbar-actions">
          <div class="user-chip" id="userChip" style="display:none;">
            <span>Bienvenue, <b id="userEmail">—</b></span>
            <button id="logoutBtn" type="button" title="Déconnexion">Déconnexion</button>
          </div>
        </div>
      </header>
    `;
  }

  function getCookie(name) {
    const m = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]+)"));
    return m ? decodeURIComponent(m[2]) : null;
  }

  // ✅ CSRF token (Spring CookieCsrfTokenRepository -> cookie "XSRF-TOKEN")
  function getCsrfToken() {
    return getCookie("XSRF-TOKEN");
  }

  // ✅ Wrapper fetch global: credentials + CSRF header auto
  async function apiFetch(path, options = {}) {
    const url = path.startsWith("http") ? path : `${API_BASE}${path}`;
    const method = (options.method || "GET").toUpperCase();

    const headers = new Headers(options.headers || {});
    if (!headers.has("Accept")) headers.set("Accept", "application/json");

    // Ajoute CSRF sur méthodes mutantes
    if (method !== "GET" && method !== "HEAD" && method !== "OPTIONS") {
      const token = getCsrfToken();
      if (token) headers.set("X-XSRF-TOKEN", token);
    }

    return fetch(url, {
      ...options,
      method,
      headers,
      credentials: "include",
      cache: "no-store",
    });
  }

  // Expose global (admin-dashboard.js l’utilise)
  window.API_BASE = API_BASE;
  window.apiFetch = apiFetch;

  async function fetchUserInfo() {
    const res = await apiFetch(USERINFO_PATH, { method: "GET" });
    if (!res.ok) throw new Error(`userinfo ${res.status}`);
    return res.json();
  }

  function bindBurger() {
    const burger = document.getElementById("burgerBtn");
    const sidebar = document.getElementById("sidebar");
    const overlay = document.getElementById("sidebarOverlay");
    if (!burger || !sidebar || !overlay) return;

    const open = () => {
      sidebar.classList.add("is-open");
      overlay.hidden = false;
      document.body.style.overflow = "hidden";
    };
    const close = () => {
      sidebar.classList.remove("is-open");
      overlay.hidden = true;
      document.body.style.overflow = "";
    };

    burger.addEventListener("click", () => {
      sidebar.classList.contains("is-open") ? close() : open();
    });
    overlay.addEventListener("click", close);
    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape") close();
    });
  }

  async function bindAuthUI() {
    const userChip = document.getElementById("userChip");
    const userEmail = document.getElementById("userEmail");
    const logoutBtn = document.getElementById("logoutBtn");
    if (!userChip || !userEmail || !logoutBtn) return;

    try {
      const data = await fetchUserInfo();
      const label = data.username || data.email || "Administrateur";
      userEmail.textContent = label;
      userChip.style.display = "inline-flex";
    } catch {
      // userChip.style.display = "none";
      // Affiche un état "Session expirée" au lieu de masquer
      userEmail.textContent = "Session expirée";
      userChip.style.display = "inline-flex";
    }

    logoutBtn.addEventListener("click", async () => {
      try {
        await apiFetch(LOGOUT_PATH, { method: "POST" });
      } catch {}
      window.location.href = "/index.html";
    });
  }

  // ✅ DEBUG : lance ça depuis la console si logs 401
  window.debugAuth = async function debugAuth() {
    console.log("=== DEBUG AUTH ===");
    console.log("HOST:", HOST);
    console.log("API_BASE:", API_BASE);
    console.log("document.cookie (visible JS):", document.cookie || "(vide)");

    // ping public
    try {
      const ping = await apiFetch("/admin/ping", { method: "GET", headers: { Accept: "application/json" } });
      console.log("PING /admin/ping:", ping.status, await ping.text());
    } catch (e) {
      console.log("PING error:", e);
    }

    // userinfo (protégé)
    try {
      const u = await apiFetch("/api/protected/userinfo", { method: "GET", headers: { Accept: "application/json" } });
      console.log("GET /api/protected/userinfo:", u.status, await u.text());
    } catch (e) {
      console.log("userinfo error:", e);
    }

    // logs (protégé admin)
    try {
      const l = await apiFetch("/api/admin/logs?lines=50", { method: "GET", headers: { Accept: "text/plain" } });
      console.log("GET /api/admin/logs:", l.status, await l.text());
    } catch (e) {
      console.log("logs error:", e);
    }

    console.log("=== END DEBUG AUTH ===");
  };

  // INIT
  ensureLayoutStyles();
  const headerSlot = document.getElementById("appHeader");
  if (headerSlot) headerSlot.innerHTML = renderHeader();

  bindBurger();
  bindAuthUI();
})();
