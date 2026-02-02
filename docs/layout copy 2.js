/* layout.js
   Injecte header + footer + CSS du layout
   ✅ Compatible cookie HttpOnly (JWT invisible côté JS)
   ✅ CSRF activé (XSRF-TOKEN cookie + header X-XSRF-TOKEN sur POST/PUT/DELETE)
   Requis dans les pages qui contiennent :
   <div id="appHeader"></div>
   <div id="appFooter"></div>
*/
(function () {
  const HOST = window.location.hostname;

  // ✅ En local : on vise le même host que le front (localhost OU 127.0.0.1)
  const API_BASE_PRIMARY =
    (HOST === "localhost" || HOST === "127.0.0.1")
      ? `http://${HOST}:8082`
      : "https://stephanedinahet.fr";

  // ✅ Fallback en local : on tente aussi l’autre host
  // const API_BASE_FALLBACK =
  //   (HOST === "localhost") ? "http://127.0.0.1:8082"
  //     : (HOST === "127.0.0.1") ? "http://localhost:8082"
  //       : null;

  const API_BASE_FALLBACK = null; // ✅ désactiver en local

  const USERINFO_PATH = "/api/protected/userinfo";
  const LOGOUT_PATH = "/api/auth/logout";
  const CSRF_PATH = "/api/auth/csrf";
  const REFRESH_PATH = "/api/auth/refresh";

  function getActiveBase() {
    return window.__API_BASE_ACTIVE__ || API_BASE_PRIMARY;
  }

  /* =========================================================
     Cookies helpers
  ========================================================= */
  function getCookie(name) {
    const m = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
    return m ? decodeURIComponent(m[2]) : null;
  }

  /* =========================================================
     CSRF helpers
     - Spring Security met un cookie XSRF-TOKEN (lisible côté JS)
     - On envoie le header X-XSRF-TOKEN sur les requêtes mutantes
  ========================================================= */
  let csrfCached = null;

  async function ensureCsrfToken(baseUrl) {
    // 1) si cookie déjà là, on prend
    const fromCookie = getCookie("XSRF-TOKEN");
    if (fromCookie) {
      csrfCached = fromCookie;
      return csrfCached;
    }

    // 2) sinon on force Spring à créer le cookie via l’endpoint /csrf
    try {
      const res = await fetch(`${baseUrl}${CSRF_PATH}`, {
        method: "GET",
        credentials: "include",
        cache: "no-store"
      });

      if (res.ok) {
        // token peut venir du JSON ou du cookie
        let token = null;
        try {
          const data = await res.json();
          token = data?.token || null;
        } catch {
          // ignore
        }

        const cookie2 = getCookie("XSRF-TOKEN");
        csrfCached = token || cookie2 || null;
        return csrfCached;
      }
    } catch {
      // ignore
    }
    return null;
  }

  function isUnsafeMethod(method) {
    const m = String(method || "GET").toUpperCase();
    return m === "POST" || m === "PUT" || m === "PATCH" || m === "DELETE";
  }

  async function withCsrfHeaders(options = {}, baseUrl) {
    const method = (options.method || "GET").toUpperCase();
    if (!isUnsafeMethod(method)) return options;

    const base = baseUrl || getActiveBase();
    // const token = csrfCached || (await ensureCsrfToken(base));
    let token = getCookie("XSRF-TOKEN");
    if (!token) token = await ensureCsrfToken(base);
    // garde le cache synchro si tu veux
    csrfCached = token || null;


    const headers = new Headers(options.headers || {});
    if (token) headers.set("X-XSRF-TOKEN", token);

    // si on envoie du JSON, on pose le content-type si absent
    const hasBody = options.body !== undefined && options.body !== null;
    if (hasBody && !headers.has("Content-Type") && typeof options.body === "string") {
      // si tu envoies déjà du FormData, ne set pas Content-Type (browser le fait)
      headers.set("Content-Type", "application/json");
    }

    return { ...options, headers };
  }

  /* =========================================================
     Styles globaux du layout (header + footer)
  ========================================================= */
  function ensureLayoutStyles() {
    if (document.getElementById("layoutStyles")) return;

    const style = document.createElement("style");
    style.id = "layoutStyles";
    style.textContent = `
      :root{
        --topbar-h: 72px;
        --footer-h: 56px;
        --stroke: rgba(255,255,255,.12);
        --text: rgba(255,255,255,.92);
        --muted: rgba(255,255,255,.65);
      }

      body.has-fixed-footer{ padding-bottom: var(--footer-h); }

      .topbar{
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
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
        display:flex;
        align-items:center;
        gap: 12px;
        text-decoration:none;
        color: var(--text);
        min-width: 0;
      }
      .brand-logo{ width: 44px; height: 44px; object-fit: contain; }
      .brand-title{
        font-weight: 900;
        letter-spacing:.2px;
        line-height: 1.1;
        white-space: nowrap;
      }
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
      }
      .btn-ghost:hover{ transform: translateY(-1px); background: rgba(255,255,255,.08); }

      .btn-ico{
        width: 18px;
        height: 18px;
        fill: none;
        stroke: var(--text);
        stroke-width: 2;
        stroke-linecap: round;
        stroke-linejoin: round;
        opacity:.95;
      }

      .btn-burger{
        display:none;
        align-items:center;
        justify-content:center;
        width: 44px;
        height: 44px;
        padding: 0;
        border-radius: 12px;
      }
      @media (max-width: 991px){ .btn-burger{ display:inline-flex; } }

      .chip{
        display:inline-flex;
        align-items:center;
        gap: 10px;
        padding:10px 12px;
        border-radius: 12px;
        border:1px solid var(--stroke);
        background: rgba(255,255,255,.06);
        color: var(--text);
        font-weight: 800;
      }

      /* ✅ userChip fondu dans la topbar (pas un cadre) */
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
      .user-chip span{
        color: var(--text);
        font-weight: 800;
        opacity: .95;
      }
      .user-chip b{ font-weight: 900; }

      /* ✅ Déconnexion = ghost rouge */
      .topbar #logoutBtn.btn-danger-soft{
        display:inline-flex;
        align-items:center;
        gap:8px;
        padding:10px 12px;
        border-radius:12px;
        border:1px solid rgba(239,68,68,.25);
        background: rgba(239,68,68,.12);
        color: var(--text);
        font-weight: 800;
        white-space: nowrap;
        line-height: 1;
        height: 40px;
        cursor:pointer;
        transition: transform .15s ease, background .15s ease;
      }
      .topbar #logoutBtn.btn-danger-soft:hover{
        transform: translateY(-1px);
        background: rgba(239,68,68,.2);
      }
      .topbar #logoutBtn.btn-danger-soft .btn-ico{
        width:18px;
        height:18px;
        stroke: var(--text);
      }

      /* ✅ MOBILE : compact */
      @media (max-width: 480px){
        .brand > div { display: none !important; }
        .topbar .user-chip{ gap: 8px; }
        .topbar #logoutBtn{
          padding: 8px 10px;
          height: 36px;
          font-size: .85rem;
        }
      }

      .footer{
        position: fixed;
        left: 0;
        right: 0;
        bottom: 0;
        height: auto;
        min-height: var(--footer-h);
        padding: 10px 16px calc(14px + env(safe-area-inset-bottom));
        display:flex;
        align-items:center;
        justify-content:center;
        gap: 12px;
        flex-wrap: wrap;
        background: rgba(10,16,28,.55);
        backdrop-filter: blur(10px);
        border-top: 1px solid var(--stroke);
        color: var(--muted);
        font-weight: 800;
        font-size: .9rem;
        z-index: 40;
      }
      .footer a{ color: var(--muted); text-decoration: none; }
      .footer a:hover{ color: var(--text); }

      @media (max-width: 480px){
        .footer{ font-size: .82rem; gap: 8px; }
        .footer a{ padding: 4px 6px; }
      }

      .api-status{
        display:flex;
        align-items:center;
        gap:8px;
        font-weight:900;
      }
      .api-dot{
        width:10px;
        height:10px;
        border-radius:50%;
        box-shadow: 0 0 6px currentColor;
      }
      .api-online{ background:#22c55e; color:#22c55e; }
      .api-offline{ background:#ef4444; color:#ef4444; }

      @media (min-width: 992px){
        .sidebar{
          position: sticky;
          top: calc(var(--topbar-h) + 16px);
          align-self: start;
        }
      }

      .brand > div { min-width: 0; }
      .brand-title, .brand-sub {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    `;
    document.head.appendChild(style);
  }

  /* =========================================================
     Refresh token helper (fetchWithRefresh)
     ✅ gère automatiquement le refresh token si 401 reçu
     ✅ utilise la bonne API active (PRIMARY ou FALLBACK)
  ========================================================= */
  async function refreshAccessToken(baseUrl) {
    const base = baseUrl || getActiveBase();
    const opts = await withCsrfHeaders(
      {
        method: "POST",
        credentials: "include",
        cache: "no-store"
      },
      base
    );

    const res = await fetch(`${base}${REFRESH_PATH}`, opts);
    return res.ok;
  }

  async function fetchWithRefresh(url, options = {}) {
    const base = getActiveBase();
    const opt1 = await withCsrfHeaders(
      {
        ...options,
        credentials: "include",
        cache: "no-store"
      },
      base
    );

    const res1 = await fetch(url, opt1);
    if (res1.status !== 401) return res1;

    // 401 → on tente refresh
    const ok = await refreshAccessToken(base);
    if (!ok) return res1;

    // refresh OK → retry 1 fois
    const opt2 = await withCsrfHeaders(
      {
        ...options,
        credentials: "include",
        cache: "no-store"
      },
      base
    );
    return await fetch(url, opt2);
  }

  window.fetchWithRefresh = fetchWithRefresh;
  // (optionnel) exposer si tu veux réutiliser ailleurs
  window.ensureCsrfToken = ensureCsrfToken;
  window.getActiveBase = getActiveBase;

  /* =========================================================
     Burger (menu) - delegation globale
  ========================================================= */
  function bindBurgerGlobal() {
    if (document.documentElement.dataset.burgerDelegation === "1") return;
    document.documentElement.dataset.burgerDelegation = "1";

    document.addEventListener("click", (e) => {
      const burger = e.target.closest("#burgerBtn");
      if (!burger) return;

      const sidebar = document.getElementById("sidebar");
      const overlay = document.getElementById("overlay");
      if (!sidebar || !overlay) return;

      const open = sidebar.classList.toggle("open");
      overlay.classList.toggle("show", open);
      document.body.classList.toggle("no-scroll", open);
    });

    document.addEventListener("click", (e) => {
      if (e.target.id !== "overlay") return;

      const sidebar = document.getElementById("sidebar");
      const overlay = document.getElementById("overlay");
      if (!sidebar || !overlay) return;

      sidebar.classList.remove("open");
      overlay.classList.remove("show");
      document.body.classList.remove("no-scroll");
    });

    document.addEventListener("keydown", (e) => {
      if (e.key !== "Escape") return;

      const sidebar = document.getElementById("sidebar");
      const overlay = document.getElementById("overlay");
      if (!sidebar || !overlay) return;

      sidebar.classList.remove("open");
      overlay.classList.remove("show");
      document.body.classList.remove("no-scroll");
    });
  }

  /* =========================================================
     HTML HEADER
  ========================================================= */
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

        <a class="brand" href="index.html" aria-label="Retour à l'accueil">
          <img src="loto_tracker.png" alt="Loto Tracker" class="brand-logo">
          <div>
            <div class="brand-title" id="brandTitle">Tracker du Loto Français</div>
            <div class="brand-sub" id="currentTime">Heure de Paris : --:--:--</div>
          </div>
        </a>

        <div class="topbar-actions">
          <a href="login.html" class="btn-ghost" id="authActionBtn">
            <svg class="btn-ico" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M20 21a8 8 0 0 0-16 0"></path>
              <circle cx="12" cy="8" r="4"></circle>
            </svg>
            <span id="authActionText">Se connecter</span>
          </a>

          <a href="register.html" class="btn-ghost" id="registerBtn">
            <svg class="btn-ico" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="8.5" cy="7" r="4"></circle>
              <path d="M20 8v6"></path>
              <path d="M23 11h-6"></path>
            </svg>
            <span>Créer un compte</span>
          </a>

          <div class="chip user-chip" id="userChip" style="display:none;">
            <span>Bienvenue, <b id="userEmail">—</b></span>
            <button class="btn-danger-soft" id="logoutBtn" type="button" title="Déconnexion">
              <svg class="btn-ico" viewBox="0 0 24 24" aria-hidden="true" style="stroke:white">
                <path d="M10 17l5-5-5-5"></path>
                <path d="M15 12H3"></path>
                <path d="M21 3v18"></path>
              </svg>
              Déconnexion
            </button>
          </div>
        </div>
      </header>
    `;
  }

  /* =========================================================
     HTML FOOTER
  ========================================================= */
  function renderFooter() {
    return `
      <footer class="footer">
        <a href="mentions_legales.html">Mentions légales</a>
        <a href="conditions_utilisation.html">Conditions</a>
        <a href="politique_confidentialite.html">Confidentialité</a>
        <a href="#" id="openCookiePrefs">🍪 Cookies</a>
        <span>© 2026 SDINAHET</span>

        <span class="api-status" title="État de l'API">
          <span id="apiDot" class="api-dot api-offline"></span>
          API
        </span>
        <span><span id="visitCount">—</span> visites</span>
      </footer>
    `;
  }

  /* =========================================================
     Helpers fetch (userinfo) => AUTH UNIQUEMENT
  ========================================================= */
  async function fetchUserInfo(baseUrl) {
    const res = await fetchWithRefresh(`${baseUrl}${USERINFO_PATH}`, { method: "GET" });
    if (!res.ok) throw new Error(`userinfo ${res.status}`);
    return await res.json();
  }

  function setAuthUI({ logged, label }) {
    const authBtn = document.getElementById("authActionBtn");
    const registerBtn = document.getElementById("registerBtn");
    const userChip = document.getElementById("userChip");
    const userEmail = document.getElementById("userEmail");

    if (!authBtn || !userChip || !userEmail) return;

    const path = window.location.pathname.toLowerCase();
    const isLoginPage = path.endsWith("/login.html");
    const isRegisterPage = path.endsWith("/register.html");

    if (logged) {
      authBtn.style.display = "none";
      if (registerBtn) registerBtn.style.display = "none";
      userChip.style.display = "inline-flex";
      userEmail.textContent = label || "Utilisateur";
      return;
    }

    userChip.style.display = "none";
    userEmail.textContent = "—";

    if (isLoginPage) {
      authBtn.style.display = "none";
      if (registerBtn) registerBtn.style.display = "inline-flex";
      return;
    }

    if (isRegisterPage) {
      authBtn.style.display = "inline-flex";
      if (registerBtn) registerBtn.style.display = "none";
      return;
    }

    authBtn.style.display = "inline-flex";
    if (registerBtn) registerBtn.style.display = "none";
  }

  /* =========================================================
     Auth UI (SOURCE DE VÉRITÉ = /userinfo)
     ✅ essaie PRIMARY puis FALLBACK (localhost/127)
  ========================================================= */
  async function checkUserAuthUI() {
    try {
      const data = await fetchUserInfo(API_BASE_PRIMARY);
      const shown = data.username || data.email || "Utilisateur";
      setAuthUI({ logged: true, label: shown });
      window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
      return;
    } catch {
      if (API_BASE_FALLBACK) {
        try {
          const data2 = await fetchUserInfo(API_BASE_FALLBACK);
          const shown2 = data2.username || data2.email || "Utilisateur";
          setAuthUI({ logged: true, label: shown2 });
          window.__API_BASE_ACTIVE__ = API_BASE_FALLBACK;
          return;
        } catch {
          setAuthUI({ logged: false });
          window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
          return;
        }
      }

      setAuthUI({ logged: false });
      window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
    }
  }

  /* =========================================================
     Ping API (alive/down) => 200 OU 401 = API vivante
  ========================================================= */
  async function pingApi(baseUrl) {
    const res = await fetch(`${baseUrl}${USERINFO_PATH}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store"
    });
    return (res.status === 200 || res.status === 401);
  }

  async function checkApiAlive() {
    const dot = document.getElementById("apiDot");
    if (!dot) return;

    const basePrimary = getActiveBase();

    try {
      const okPrimary = await pingApi(basePrimary);
      if (okPrimary) {
        dot.classList.remove("api-offline");
        dot.classList.add("api-online");
        return;
      }

      if (API_BASE_FALLBACK) {
        const okFallback = await pingApi(API_BASE_FALLBACK);
        if (okFallback) {
          window.__API_BASE_ACTIVE__ = API_BASE_FALLBACK;
          dot.classList.remove("api-offline");
          dot.classList.add("api-online");
          return;
        }
      }

      throw new Error("API down");
    } catch {
      dot.classList.remove("api-online");
      dot.classList.add("api-offline");
    }
  }

  /* =========================================================
     Visits counter (footer) - unique (pas de doublon)
  ========================================================= */
  async function loadVisitCount() {
    const base =
      getActiveBase() ||
      ((location.hostname === "localhost" || location.hostname === "127.0.0.1")
        ? `http://${location.hostname}:8082`
        : "https://stephanedinahet.fr");

    const el = document.getElementById("visitCount");
    if (!el) return;

    try {
      const key = "visitCountedThisSession";
      const already = sessionStorage.getItem(key) === "1";
      const url = already ? `${base}/api/visits/total` : `${base}/api/visits`;

      const res = await fetch(url, { cache: "no-store", credentials: "include" });
      if (!res.ok) throw new Error("Visits API error");

      const data = await res.json();
      el.textContent = Number(data.total ?? 0).toLocaleString("fr-FR");

      if (!already) sessionStorage.setItem(key, "1");
    } catch (e) {
      console.warn("Visites indisponibles", e);
      el.textContent = "—";
    }
  }

  /* =========================================================
     Logout (toujours retour index.html)
     ✅ POST => CSRF header automatiquement
  ========================================================= */
  async function logout() {
    const base = getActiveBase();

    try {
      const opts = await withCsrfHeaders(
        {
          method: "POST",
          credentials: "include",
          cache: "no-store"
        },
        base
      );

      await fetch(`${base}${LOGOUT_PATH}`, opts);
    } catch {
      // même si l'API échoue, on force la déconnexion côté front
    } finally {
      localStorage.removeItem("tickets.page");
      localStorage.removeItem("tickets.size");
      localStorage.removeItem("jwtToken");
      window.location.href = "index.html";
    }
  }

  function bindLogout() {
    const btn = document.getElementById("logoutBtn");
    if (!btn) return;
    btn.addEventListener("click", logout);
  }

  /* =========================================================
     Heure de Paris
  ========================================================= */
  function updateParisTime() {
    const el = document.getElementById("currentTime");
    if (!el) return;

    const fmt = new Intl.DateTimeFormat("fr-FR", {
      timeZone: "Europe/Paris",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit"
    });
    el.textContent = "Heure de Paris : " + fmt.format(new Date());
  }

  /* =========================================================
     Analytics navigation tracking (safe: no-op si pas besoin)
  ========================================================= */
  function bindAnalyticsNavigationTracking() {
    if (document.documentElement.dataset.analyticsNavBound === "1") return;
    document.documentElement.dataset.analyticsNavBound = "1";

    document.addEventListener("click", (e) => {
      const a = e.target.closest("a");
      if (!a) return;
      const href = a.getAttribute("href") || "";
      if (!href || href.startsWith("http") || href.startsWith("#") || href.startsWith("mailto:")) return;

      trackEvent("nav_click", { href });
    });
  }

  /* =========================================================
     trackEvent (réutilisable)
     ✅ POST => CSRF header automatiquement
  ========================================================= */
  async function trackEvent(eventType, extra = {}) {
    const base =
      getActiveBase() ||
      ((location.hostname === "localhost" || location.hostname === "127.0.0.1")
        ? `http://${location.hostname}:8082`
        : "https://stephanedinahet.fr");

    const url = `${base}/api/analytics/event`;

    try {
      const opts = await withCsrfHeaders(
        {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            eventType,
            page: window.location.pathname,
            ts: new Date().toISOString(),
            extra: {
              referrer: document.referrer || "-",
              screen: `${screen.width}x${screen.height}`,
              ...extra
            }
          })
        },
        base
      );

      const res = await fetch(url, opts);
      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        console.warn("[analytics] POST failed", res.status, txt);
      }
    } catch (err) {
      console.warn("[analytics] network error", err);
    }
  }

  /* =========================================================
     Analytics – page view automatique
  ========================================================= */
  function trackPageView() {
    trackEvent("page_view");
  }

  /* =========================================================
     Footer height sync
  ========================================================= */
  function syncFooterHeight() {
    const footer = document.querySelector(".footer");
    if (!footer) return;
    document.documentElement.style.setProperty("--footer-h", footer.offsetHeight + "px");
  }

  // ===== AXIOS FAST PATCH (CSRF + cookies) =====
  function setupAxiosFastPatch() {
    if (!window.axios) return;            // axios pas chargé
    if (window.__AXIOS_PATCHED__) return; // évite double patch
    window.__AXIOS_PATCHED__ = true;

    // Toujours envoyer les cookies (jwtToken, refreshToken, XSRF-TOKEN)
    axios.defaults.withCredentials = true;

    // Interceptor: ajoute CSRF sur méthodes mutantes
    axios.interceptors.request.use(async (config) => {
      const method = String(config.method || "GET").toUpperCase();
      const unsafe = ["POST", "PUT", "PATCH", "DELETE"].includes(method);
      if (!unsafe) return config;

      const base = getActiveBase();

      // S'assurer que XSRF-TOKEN existe (cookie créé via /api/auth/csrf si besoin)
      let token = getCookie("XSRF-TOKEN") || csrfCached;
      if (!token) {
        await ensureCsrfToken(base);
        token = getCookie("XSRF-TOKEN") || csrfCached;
      }

      if (token) {
        config.headers = config.headers || {};
        config.headers["X-XSRF-TOKEN"] = token;
      }

      return config;
    });

    // Optionnel: log clair si 403 CSRF
    axios.interceptors.response.use(
      (r) => r,
      (err) => {
        if (err?.response?.status === 403) {
          console.warn("[axios] 403 Forbidden (CSRF/header/role)", err.response?.data || "");
        }
        return Promise.reject(err);
      }
    );
  }

// ===== FETCH FAST PATCH (CSRF + cookies) =====
function setupFetchFastPatch() {
  if (window.__FETCH_PATCHED__) return;
  window.__FETCH_PATCHED__ = true;

  const originalFetch = window.fetch;

  window.fetch = async function (input, init = {}) {
    // Toujours inclure les cookies (refreshToken + XSRF-TOKEN)
    init.credentials = init.credentials || "include";

    // Ajoute automatiquement X-XSRF-TOKEN sur POST/PUT/PATCH/DELETE
    const base = getActiveBase();
    init = await withCsrfHeaders(init, base);

    return originalFetch(input, init);
  };
}


  /* =========================================================
     Init layout
  ========================================================= */
  function injectLayout() {
    ensureLayoutStyles();
    bindBurgerGlobal();

    document.body.classList.add("has-fixed-footer");

    const headerMount = document.getElementById("appHeader");
    const footerMount = document.getElementById("appFooter");

    if (headerMount) headerMount.innerHTML = renderHeader();
    if (footerMount) footerMount.innerHTML = renderFooter();

    // ✅ CSRF : on prépare le token au chargement (utile pour analytics/logout/etc.)
    ensureCsrfToken(getActiveBase());
    setupAxiosFastPatch();
    setupFetchFastPatch();

    // Heure (inutile sur mobile car caché)
    if (!window.matchMedia("(max-width: 480px)").matches) {
      updateParisTime();
      setInterval(updateParisTime, 1000);
    }

    // Auth puis le reste
    checkUserAuthUI().finally(() => {
      // après auth, on re-prépare CSRF sur la base active (important si fallback)
      ensureCsrfToken(getActiveBase());

      bindLogout();

      checkApiAlive();
      setInterval(checkApiAlive, 30000);

      loadVisitCount();

      trackPageView();
      bindAnalyticsNavigationTracking();

      syncFooterHeight();
      window.addEventListener("resize", syncFooterHeight);
    });

    document.dispatchEvent(new CustomEvent("layout:ready"));
  }

  document.addEventListener("DOMContentLoaded", injectLayout);
})();
