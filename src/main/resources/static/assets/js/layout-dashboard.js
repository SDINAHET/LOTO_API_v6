/* layout-dashboard.js (ADMIN CRUD FIX)
   - Injecte topbar
   - Burger pilote la sidebar (#sidebar + .is-open + #sidebarOverlay)
   - Logout
   - ✅ apiFetch() : credentials + CSRF auto + merge headers
   - ✅ prewarmCsrf() : force Spring à poser XSRF-TOKEN (si CookieCsrfTokenRepository)
   - ✅ debugAuth() + debugPutUser() : diagnostics 401/403 CRUD
*/
(function () {
  "use strict";

  // ----------------------------
  // API_BASE (robuste)
  // ----------------------------
  const HOST = window.location.hostname;

  // Prod si domaine principal OU sous-domaine
  const IS_PROD =
    HOST === "stephanedinahet.fr" ||
    HOST === "www.stephanedinahet.fr" ||
    HOST.endsWith(".stephanedinahet.fr");

  // ✅ En prod: API via reverse-proxy sur le domaine principal
  // ✅ En local/réseau: API sur même host mais port 8082
  const API_BASE = IS_PROD
    ? "https://stephanedinahet.fr"
    : `${window.location.protocol}//${HOST}:8082`;

  const USERINFO_PATH = "/api/protected/userinfo";
  const LOGOUT_PATH = "/api/auth/logout";

  // ----------------------------
  // Styles topbar
  // ----------------------------
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

      .session-expired { opacity: .75; }
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

  // ----------------------------
  // Cookies + CSRF helpers
  // ----------------------------
  function getCookie(name) {
    const m = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]+)"));
    return m ? decodeURIComponent(m[2]) : null;
  }

  // Spring CookieCsrfTokenRepository => cookie "XSRF-TOKEN" (NON HttpOnly)
  function getCsrfToken() {
    return getCookie("XSRF-TOKEN");
  }

  function isMutating(method) {
    const m = String(method || "GET").toUpperCase();
    return m !== "GET" && m !== "HEAD" && m !== "OPTIONS";
  }

  // ----------------------------
  // ✅ apiFetch robuste (cookies + CSRF + merge headers)
  // ----------------------------
  async function apiFetch(path, options = {}) {
    const url = path.startsWith("http") ? path : `${API_BASE}${path}`;
    const method = String(options.method || "GET").toUpperCase();

    // base headers
    const baseHeaders = new Headers();
    baseHeaders.set("X-Requested-With", "XMLHttpRequest"); // utile Spring / proxies

    // merge headers utilisateur
    const userHeaders = new Headers(options.headers || {});
    userHeaders.forEach((v, k) => baseHeaders.set(k, v));

    // Accept par défaut si pas défini par l'appelant
    if (!baseHeaders.has("Accept")) baseHeaders.set("Accept", "application/json");

    // CSRF sur méthodes mutantes
    if (isMutating(method)) {
      const token = getCsrfToken();
      if (token) {
        baseHeaders.set("X-XSRF-TOKEN", token); // standard XSRF cookie
        baseHeaders.set("X-CSRF-TOKEN", token); // fallback selon configs
      }
    }

    const res = await fetch(url, {
      ...options,
      method,
      headers: baseHeaders,
      credentials: "include",
      cache: "no-store",
      mode: "cors",
    });

    // log utile si 401/403 sur PUT/POST
    if (res.status === 401 || res.status === 403) {
      console.warn("[apiFetch] AUTH ERROR", {
        status: res.status,
        url,
        method,
        hasXsrfCookieVisibleToJs: !!getCsrfToken(),
        host: HOST,
        apiBase: API_BASE,
      });
    }

    return res;
  }

  // Expose global (admin-dashboard.js l’utilise)
  window.API_BASE = API_BASE;
  window.apiFetch = apiFetch;

  // ----------------------------
  // Pré-chauffage CSRF
  // (Important pour que Spring pose le cookie XSRF-TOKEN)
  // ----------------------------
  async function prewarmCsrf() {
    try {
      await apiFetch(USERINFO_PATH, { method: "GET" });
    } catch {}
  }

  async function fetchUserInfo() {
    const res = await apiFetch(USERINFO_PATH, { method: "GET" });
    if (!res.ok) throw new Error(`userinfo ${res.status}`);
    return res.json();
  }

  // ----------------------------
  // Burger
  // ----------------------------
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

  // ----------------------------
  // Auth UI + Logout
  // ----------------------------
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
      userEmail.textContent = "Session expirée";
      userChip.style.display = "inline-flex";
      userChip.classList.add("session-expired");
    }

    logoutBtn.addEventListener("click", async () => {
      try {
        await apiFetch(LOGOUT_PATH, { method: "POST" });
      } catch {}
      window.location.href = "/index.html";
    });
  }

  // ----------------------------
  // Debug helpers
  // ----------------------------
  window.debugAuth = async function debugAuth() {
    console.log("=== DEBUG AUTH ===");
    console.log("HOST:", HOST);
    console.log("API_BASE:", API_BASE);
    console.log("document.cookie (visible JS):", document.cookie || "(vide)");
    console.log("XSRF-TOKEN visible JS:", !!getCsrfToken());

    // ping
    try {
      const ping = await apiFetch("/api/admin/ping", {
        method: "GET",
        headers: { Accept: "text/plain" },
      });
      console.log("GET /admin/ping:", ping.status, await ping.text());
    } catch (e) {
      console.log("PING error:", e);
    }

    // userinfo
    try {
      const u = await apiFetch("/api/protected/userinfo", {
        method: "GET",
        headers: { Accept: "application/json" },
      });
      console.log("GET /api/protected/userinfo:", u.status, await u.text());
    } catch (e) {
      console.log("userinfo error:", e);
    }

    // admin users
    try {
      const r = await apiFetch("/api/admin/users", {
        method: "GET",
        headers: { Accept: "application/json" },
      });
      console.log("GET /api/admin/users:", r.status, (await r.text()).slice(0, 500));
    } catch (e) {
      console.log("admin users error:", e);
    }

    console.log("=== END DEBUG AUTH ===");
  };

  // Test PUT (remplace id + payload si besoin)
  window.debugPutUser = async function debugPutUser(id, payload) {
    console.log("=== DEBUG PUT USER ===");
    console.log("XSRF-TOKEN visible JS:", !!getCsrfToken(), "value:", getCsrfToken());

    const body = payload || { firstName: "Test", lastName: "Admin", email: "test@exemple.com", role: "ROLE_USER", admin: false };

    try {
      const res = await apiFetch(`/api/admin/users/${encodeURIComponent(id)}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify(body),
      });
      const txt = await res.text().catch(() => "");
      console.log("PUT status:", res.status);
      console.log("PUT body:", txt);
      console.log("=== END DEBUG PUT USER ===");
      return { status: res.status, body: txt };
    } catch (e) {
      console.error("PUT error:", e);
      console.log("=== END DEBUG PUT USER ===");
      return { error: String(e) };
    }
  };

  // ----------------------------
  // INIT
  // ----------------------------
  ensureLayoutStyles();

  const headerSlot = document.getElementById("appHeader");
  if (headerSlot) headerSlot.innerHTML = renderHeader();

  bindBurger();

  // ✅ Important: pré-chauffe CSRF AVANT bindAuthUI + avant tes PUT/POST/DELETE
  prewarmCsrf().finally(() => {
    bindAuthUI();
  });
})();
