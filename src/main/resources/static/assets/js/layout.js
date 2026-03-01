/* layout.js
   Injecte header + footer + CSS du layout
   ✅ Compatible cookie HttpOnly (JWT invisible côté JS)
   ✅ CSRF activé (XSRF-TOKEN cookie + header X-XSRF-TOKEN sur POST/PUT/DELETE)
   ✅ Refresh automatique sur 401 (fetch + axios) sans redirection immédiate
   Requis dans les pages qui contiennent :
   <div id="appHeader"></div>
   <div id="appFooter"></div>
// */
(function () {
  const HOST = window.location.hostname;

  // ✅ En local : on vise le même host que le front (localhost OU 127.0.0.1)
  const API_BASE_PRIMARY =
    (HOST === "localhost" || HOST === "127.0.0.1")
      ? `http://${HOST}:8082`
      // : "https://stephanedinahet.fr";
      : "https://loto-tracker.fr"; // fallback sécurité


  // const HOST = window.location.hostname;

  // const PROD_DOMAINS = ["stephanedinahet.fr", "loto-tracker.fr"];

  // const IS_PROD = PROD_DOMAINS.some((domain) =>
  //   HOST === domain ||
  //   HOST === `www.${domain}` ||
  //   HOST.endsWith(`.${domain}`)
  // );

  // // ✅ PROD : même origin (apache reverse proxy)
  // // ✅ LOCAL : API sur le même host, port 8082
  // const API_BASE = IS_PROD
  //   ? window.location.origin
  //   : `${window.location.protocol}//${HOST}:8082`;

  // window.API_BASE = API_BASE;
  // console.log("API_BASE:", API_BASE);

  // function getActiveBase() {
  //   return window.__API_BASE_ACTIVE__ || window.API_BASE;
  // }
//   (function () {
//     const HOST = window.location.hostname;

//     const PROD_DOMAINS = ["stephanedinahet.fr", "loto-tracker.fr"];

//     const IS_PROD = PROD_DOMAINS.some(d =>
//       HOST === d ||
//       HOST === `www.${d}` ||
//       HOST.endsWith(`.${d}`)
//     );

//     // ✅ Local => API sur 8082 du même host (localhost/127.0.0.1)
//     // ✅ Prod => même origin (fonctionne pour stephanedinahet.fr ET loto-tracker.fr)
//     const API_BASE_PRIMARY = (HOST === "localhost" || HOST === "127.0.0.1")
//       ? `http://${HOST}:8082`
//       : (IS_PROD ? window.location.origin : "https://stephanedinahet.fr"); // fallback sécurité

//     window.API_BASE = API_BASE_PRIMARY;
//     window.getApiBase = () => window.API_BASE;

//     console.log("API_BASE =", window.API_BASE);
// // })();



// (function () {
//   const HOST = window.location.hostname;

//   const PROD_DOMAINS = [
//     "stephanedinahet.fr",
//     "loto-tracker.fr"
//   ];

//   const IS_PROD = PROD_DOMAINS.some(domain =>
//     HOST === domain ||
//     HOST === `www.${domain}` ||
//     HOST.endsWith(`.${domain}`)
//   );

//   const API_BASE = IS_PROD
//     ? window.location.origin
//     : `${window.location.protocol}//${HOST}:8082`;

//   // On rend API_BASE global
//   window.API_BASE = API_BASE;

//   console.log("API_BASE:", API_BASE);


  const API_BASE_FALLBACK = null; // ✅ désactiver en local

  const USERINFO_PATH = "/api/protected/userinfo";
  const LOGOUT_PATH = "/api/auth/logout";
  const CSRF_PATH = "/api/auth/csrf";
  const REFRESH_PATH = "/api/auth/refresh";

  function getActiveBase() {
    return window.__API_BASE_ACTIVE__ || API_BASE_PRIMARY;
  }

  if (window.__LAYOUT_ALREADY_LOADED__) {
    console.warn("[layout] déjà chargé, on stoppe la 2e init");
    return;
  }
  window.__LAYOUT_ALREADY_LOADED__ = true;


  /* =========================================================
     Cookies helpers
  ========================================================= */
  function getCookie(name) {
    const m = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
    return m ? decodeURIComponent(m[2]) : null;
  }

  /* =========================================================
     CSRF helpers
  ========================================================= */
  let csrfCached = null;

  async function ensureCsrfToken(baseUrl) {
    const fromCookie = getCookie("XSRF-TOKEN");
    if (fromCookie) {
      csrfCached = fromCookie;
      return csrfCached;
    }

    try {
      // const res = await (window.__ORIGINAL_FETCH__ || fetch)(`${baseUrl}${CSRF_PATH}`, {
      //   method: "GET",
      //   credentials: "include",
      //   cache: "no-store"
      // });

      const res = await (window.__ORIGINAL_FETCH__ || fetch)(`${baseUrl}${CSRF_PATH}`, {
        method: "POST",
        credentials: "include",
        cache: "no-store",
        headers: { "Accept": "application/json" }
      });

      if (res.ok) {
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

    let token = getCookie("XSRF-TOKEN");
    if (!token) token = await ensureCsrfToken(base);
    csrfCached = token || null;

    const headers = new Headers(options.headers || {});
    if (token) headers.set("X-XSRF-TOKEN", token);

    const hasBody = options.body !== undefined && options.body !== null;
    if (hasBody && !headers.has("Content-Type") && typeof options.body === "string") {
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
      /*.brand-logo{ width: 44px; height: 44px; object-fit: contain; }*/
      .brand-logo{ width: 90px; height: 90px; object-fit: contain; }
      .brand-title{
        font-weight: 900;
        letter-spacing:.2px;
        line-height: 1.1;
        white-space: nowrap;
      }
      .brand-sub{ font-size: .9rem; color: var(--muted); margin-top: 2px; }

      /* Mobile */
      @media (max-width: 600px){
        .brand-logo{
          width: 65px;
          height: 65px;
        }
      }

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

      .user-chip{
        background: transparent !important;
        border: none !important;
        box-shadow: none !important;
        padding: 0 !important;
        border-radius: 0 !important;
        display: inline-flex;
        align-items: center;
        gap: 8px;
      }
      .user-chip span{
        color: var(--text);
        /*font-weight: 800;*/
        font-weight: 500;
        opacity: .95;
      }
      /*.user-chip b{ font-weight: 900; }*/
      .user-chip b{ font-weight: 300; }

      /* Réduire la police du "Bienvenue, ..." */
        .user-chip span{
          font-size: .9rem;   /* essaie .85rem si tu veux plus petit */
        }


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

      @media (max-width: 480px){
        .brand > div { display: none !important; }
        .topbar .user-chip{ gap: 8px; }
        .topbar #logoutBtn{
          padding: 8px 10px;
          height: 36px;
          /*font-size: .85rem;*/
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

  function isLegalPage() {
    const path = (window.location.pathname || "").toLowerCase();
    return (
      path.endsWith("mentions_legales.html") ||
      path.endsWith("conditions_utilisation.html") ||
      path.endsWith("politique_confidentialite.html")
    );
  }


  /* =========================================================
     Refresh helper (utilise originalFetch pour éviter boucle)
  ========================================================= */
  async function refreshAccessToken(baseUrl) {
    const base = baseUrl || getActiveBase();
    const originalFetch = window.__ORIGINAL_FETCH__ || fetch;

    const opts = await withCsrfHeaders(
      { method: "POST", credentials: "include", cache: "no-store" },
      base
    );

    const res = await originalFetch(`${base}${REFRESH_PATH}`, opts);
    return res.ok;
  }

  /* =========================================================
     Fetch global patch : CSRF + refresh-on-401 + queue
  ========================================================= */
  let isRefreshing = false;
  let refreshWaiters = [];

  function waitForRefresh() {
    return new Promise((resolve) => refreshWaiters.push(resolve));
  }
  function resolveRefresh(ok) {
    refreshWaiters.forEach((r) => r(ok));
    refreshWaiters = [];
  }

  function isApiUrl(input) {
    const base = getActiveBase();
    const url = typeof input === "string" ? input : input?.url;
    if (!url) return false;

    if (url.startsWith(base)) return true; // URL absolue backend
    if (url.startsWith("/api/")) return true; // URL relative API
    return false;
  }

  // async function apiFetch(input, init = {}) {
  //   const base = getActiveBase();
  //   const originalFetch = window.__ORIGINAL_FETCH__ || fetch;

  //   init.credentials = init.credentials || "include";
  //   init.cache = init.cache || "no-store";

  //   init = await withCsrfHeaders(init, base);

  //   const res1 = await originalFetch(input, init);
  //   if (res1.status !== 401) return res1;

  //   if (isRefreshing) {
  //     const ok = await waitForRefresh();
  //     if (!ok) return res1;
  //     const init2 = await withCsrfHeaders({ ...init }, base);
  //     return originalFetch(input, init2);
  //   }

  //   isRefreshing = true;
  //   try {
  //     const ok = await refreshAccessToken(base);
  //     resolveRefresh(ok);
  //     if (!ok) return res1;

  //     const init2 = await withCsrfHeaders({ ...init }, base);
  //     return originalFetch(input, init2);
  //   } finally {
  //     isRefreshing = false;
  //   }
  // }

  async function apiFetch(input, init = {}) {
    const base = getActiveBase();
    const originalFetch = window.__ORIGINAL_FETCH__ || fetch;

    init.credentials = init.credentials || "include";
    init.cache = init.cache || "no-store";

    init = await withCsrfHeaders(init, base);

    const res1 = await originalFetch(input, init);
    if (res1.status !== 401) return res1;

    // ✅ IMPORTANT : ne pas tenter de refresh pour /userinfo
    // (Lighthouse arrive en invité => 401 normal)
    try {
      const urlStr = typeof input === "string" ? input : (input?.url || "");
      const path = urlStr.startsWith("http") ? new URL(urlStr).pathname : urlStr;

      // Gère les URLs absolues et relatives
      if (path === USERINFO_PATH || path.endsWith(USERINFO_PATH)) {
        return res1; // 401 => invité, pas de refresh
      }
    } catch {
      // ignore
    }

    if (isRefreshing) {
      const ok = await waitForRefresh();
      if (!ok) return res1;
      const init2 = await withCsrfHeaders({ ...init }, base);
      return originalFetch(input, init2);
    }

    isRefreshing = true;
    try {
      const ok = await refreshAccessToken(base);
      resolveRefresh(ok);
      if (!ok) return res1;

      const init2 = await withCsrfHeaders({ ...init }, base);
      return originalFetch(input, init2);
    } finally {
      isRefreshing = false;
    }
  }


  function setupFetchFastPatch() {
    if (window.__FETCH_PATCHED__) return;
    window.__FETCH_PATCHED__ = true;

    window.__ORIGINAL_FETCH__ = window.fetch;

    window.fetch = function (input, init) {
      if (isApiUrl(input)) return apiFetch(input, init);
      return window.__ORIGINAL_FETCH__(input, init);
    };
  }

  // exposer helpers utiles
  window.ensureCsrfToken = ensureCsrfToken;
  window.getActiveBase = getActiveBase;

  /* =========================================================
     fetchWithRefresh helper (compat legacy pages)
     - garde le même comportement que fetch patché (CSRF + refresh 401)
     - accepte URL relatives (/api/...) ou absolues
  ========================================================= */
  window.fetchWithRefresh = async function fetchWithRefresh(input, init = {}) {
    const base = getActiveBase();

    // Garantit credentials/cache comme le patch fetch()
    init = init || {};
    init.credentials = init.credentials || "include";
    init.cache = init.cache || "no-store";

    // Ajoute CSRF si méthode unsafe
    init = await withCsrfHeaders(init, base);

    // Si on reçoit une URL relative /api/..., on la transforme en URL absolue
    let finalInput = input;
    if (typeof input === "string" && input.startsWith("/api/")) {
      finalInput = `${base}${input}`;
    } else if (input && typeof input === "object" && input.url && input.url.startsWith("/api/")) {
      finalInput = new Request(`${base}${input.url}`, input);
    }

    // IMPORTANT :
    // On appelle le fetch patché (window.fetch) -> il fera refresh sur 401 + retry
    return fetch(finalInput, init);
  };


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
     HTML HEADER / FOOTER
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
          <img src="assets/img/loto_tracker.png" alt="Loto Tracker" class="brand-logo">
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

          <a href="/admin-login.html" class="btn-ghost admin-only" id="adminLink" style="display:none;">
            <svg class="btn-ico" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 2l8 4v6c0 5-3 9-8 10C7 21 4 17 4 12V6l8-4z"></path>
            </svg>
            <span>Admin</span>
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

  function renderFooter() {
    return `
      <footer class="footer" role="contentinfo" aria-label="Pied de page du site">

        <a href="mentions_legales.html"
          aria-label="Consulter les mentions légales">
          Mentions légales
        </a>

        <a href="conditions_utilisation.html"
          aria-label="Consulter les conditions d'utilisation">
          Conditions
        </a>

        <a href="politique_confidentialite.html"
          aria-label="Consulter la politique de confidentialité">
          Confidentialité
        </a>

        <a href="#"
          id="openCookiePrefs"
          role="button"
          aria-label="Ouvrir les préférences des cookies">
          <i class="fa-solid fa-cookie-bite" aria-hidden="true"></i>
          <span>Cookies</span>
        </a>

        <span aria-label="Copyright 2026 Stéphane Dinahet">
          © 2026
        </span>

        <a href="https://github.com/SDINAHET/LOTO_API_v6/tree/fix3_migration"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Voir le projet LOTO API v6 sur GitHub (nouvel onglet)">
          <i class="fa-brands fa-github" aria-hidden="true"></i>
          <span>SDINAHET</span>
        </a>

        <a href="assets/pdf/CV_Stephane_Dinahet.pdf"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Télécharger le CV de Stéphane Dinahet au format PDF (nouvel onglet)">
          <i class="fa-solid fa-file-pdf" aria-hidden="true"></i>
          <span>CV</span>
        </a>

        <span class="api-status"
              role="status"
              aria-live="polite"
              aria-label="État actuel de l'API">
              <span id="apiDot"
                    class="api-dot api-offline"
                    aria-hidden="true"></span>
              <span>API</span>
        </span>

        <span aria-live="polite"
              aria-label="Nombre total de visites du site">
              <span id="visitCount">—</span> visites
        </span>

        ${isLegalPage() ? `
          <a href="https://lototracker.goatcounter.com/"
            target="_blank"
            rel="noopener noreferrer"
            class="footer-stat-site"
            aria-label="Voir les statistiques du site (nouvel onglet)">
            <i class="fa-solid fa-chart-simple" aria-hidden="true"></i>
            <span>Stat du site</span>
          </a>

          <a href="https://status.stephanedinahet.fr/status/loto"
            target="_blank"
            rel="noopener noreferrer"
            class="footer-status-site"
            aria-label="Voir le statut du site en temps réel (nouvel onglet)">
            <i class="fa-solid fa-signal" aria-hidden="true"></i>
            <span>Status site</span>
          </a>
        ` : ""}

      </footer>
    `;
  }

  function isTrackingExcludedPage() {
    const path = (location.pathname || "").toLowerCase();

    // Liste des pages à EXCLURE du tracking
    const excludedPages = [
      "admin-login.html",
      "admin.html",
      "login.html",
      "register.html"
    ];

    return excludedPages.some(page => path.endsWith(page));
  }

  function loadGoatCounter() {
    // ❌ Ne pas tracker certaines pages
    if (isTrackingExcludedPage()) {
      window.goatcounter = { no_onload: true };
      return;
    }

    // ❌ Ne pas tracker hors production
    // if (!location.hostname.endsWith("stephanedinahet.fr")) {
    if (!location.hostname.endsWith("loto-tracker.fr")) {
      window.goatcounter = { no_onload: true };
      return;
    }

    // Évite les doublons
    if (document.querySelector('script[data-goatcounter]')) return;

    // (Optionnel) forcer un chemin canonique
    window.goatcounter = window.goatcounter || {};
    window.goatcounter.path = location.pathname;

    const s = document.createElement("script");
    s.setAttribute("data-goatcounter", "https://lototracker.goatcounter.com/count");
    s.async = true;
    s.src = "//gc.zgo.at/count.js";
    document.body.appendChild(s);
  }




  /* =========================================================
   Admin link (dans le menu burger)
========================================================= */
function renderAdminMenuItem() {
  return `
    <a href="/admin-login.html" class="btn-ghost admin-menu-item" id="adminBurgerLink">
      <svg class="btn-ico" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M12 2l8 4v6c0 5-3 9-8 10C7 21 4 17 4 12V6l8-4z"></path>
      </svg>
      <span>Admin Dashboard</span>
    </a>
  `;
}

function setAdminInBurger(isAdmin) {
  const slot = document.getElementById("adminMenuSlot");
  if (!slot) return;
  slot.innerHTML = isAdmin ? renderAdminMenuItem() : "";
}


  /* =========================================================
     Auth UI (source de vérité = /userinfo)
  ========================================================= */
  // async function fetchUserInfo(baseUrl) {
  //   // ✅ plus besoin de fetchWithRefresh : fetch() est patché -> refresh auto
  //   const res = await fetch(`${baseUrl}${USERINFO_PATH}`, { method: "GET" });
  //   if (!res.ok) throw new Error(`userinfo ${res.status}`);
  //   return await res.json();
  // }
  async function fetchUserInfo(baseUrl) {
    const res = await fetch(`${baseUrl}${USERINFO_PATH}`, { method: "GET" });

    if (res.status === 401) {
      // invité → on ne log pas d’erreur
      return null;
    }

    if (!res.ok) {
      throw new Error(`userinfo ${res.status}`);
    }

    return await res.json();
  }


  // function setAuthUI({ logged, label }) {
  function setAuthUI({ logged, label, isAdmin }) {
    const adminLink = document.getElementById("adminLink");
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

      // 🔥 AJOUT ICI
      if (window.location.pathname.endsWith("/login.html")) {
        window.location.replace("index.html");
        return;
      }

      // ✅ injecte l’item Admin dans le burger menu
      setAdminInBurger(!!isAdmin);

      // (optionnel) tu peux aussi masquer le bouton admin du header :
      if (adminLink) adminLink.style.display = "none";
      // if (adminLink) adminLink.style.display = isAdmin ? "inline-flex" : "none";
      return;
    }

    userChip.style.display = "none";
    setAdminInBurger(false);
    if (adminLink) adminLink.style.display = "none";
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

  // async function checkUserAuthUI() {
  //   try {
  //     // const data = await fetchUserInfo(API_BASE_PRIMARY);
  //     // const shown = data.username || data.email || "Utilisateur";
  //     // setAuthUI({ logged: true, label: shown });
  //     const data = await fetchUserInfo(API_BASE_PRIMARY);
  //     const shown = data.username || data.email || "Utilisateur";

  //     const isAdmin =
  //       data?.role === "ADMIN" ||
  //       data?.role === "ROLE_ADMIN" ||
  //       (Array.isArray(data?.roles) && data.roles.includes("ADMIN")) ||
  //       (Array.isArray(data?.roles) && data.roles.includes("ROLE_ADMIN"));

  //     setAuthUI({ logged: true, label: shown, isAdmin });

  //     window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
  //     return;
  //   } catch {
  //     if (API_BASE_FALLBACK) {
  //       try {
  //         const data2 = await fetchUserInfo(API_BASE_FALLBACK);
  //         const shown2 = data2.username || data2.email || "Utilisateur";
  //         setAuthUI({ logged: true, label: shown2 });
  //         window.__API_BASE_ACTIVE__ = API_BASE_FALLBACK;
  //         return;
  //       } catch {
  //         // setAuthUI({ logged: false });
  //         setAuthUI({ logged: false, isAdmin: false });
  //         window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
  //         return;
  //       }
  //     }
  //     // setAuthUI({ logged: false });
  //     setAuthUI({ logged: false, isAdmin: false });
  //     window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
  //   }
  // }

  async function checkUserAuthUI() {
    try {
      const data = await fetchUserInfo(API_BASE_PRIMARY);

      // ✅ Cas normal : utilisateur non connecté (ex: 401 => fetchUserInfo() retourne null)
      if (!data) {
        setAuthUI({ logged: false, isAdmin: false });
        window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
        return;
      }

      const shown = data.username || data.email || "Utilisateur";

      const isAdmin =
        data?.role === "ADMIN" ||
        data?.role === "ROLE_ADMIN" ||
        (Array.isArray(data?.roles) && data.roles.includes("ADMIN")) ||
        (Array.isArray(data?.roles) && data.roles.includes("ROLE_ADMIN"));

      setAuthUI({ logged: true, label: shown, isAdmin });
      window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
      return;
    } catch {
      if (API_BASE_FALLBACK) {
        try {
          const data2 = await fetchUserInfo(API_BASE_FALLBACK);

          // ✅ Cas normal : invité aussi sur le fallback
          if (!data2) {
            setAuthUI({ logged: false, isAdmin: false });
            window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
            return;
          }

          const shown2 = data2.username || data2.email || "Utilisateur";

          const isAdmin2 =
            data2?.role === "ADMIN" ||
            data2?.role === "ROLE_ADMIN" ||
            (Array.isArray(data2?.roles) && data2.roles.includes("ADMIN")) ||
            (Array.isArray(data2?.roles) && data2.roles.includes("ROLE_ADMIN"));

          setAuthUI({ logged: true, label: shown2, isAdmin: isAdmin2 });
          window.__API_BASE_ACTIVE__ = API_BASE_FALLBACK;
          return;
        } catch {
          setAuthUI({ logged: false, isAdmin: false });
          window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
          return;
        }
      }

      setAuthUI({ logged: false, isAdmin: false });
      window.__API_BASE_ACTIVE__ = API_BASE_PRIMARY;
    }
  }


  /* =========================================================
     Ping API (alive/down) => 200 OU 401 = API vivante
  ========================================================= */
  async function pingApi(baseUrl) {
    const res = await (window.__ORIGINAL_FETCH__ || fetch)(`${baseUrl}${USERINFO_PATH}`, {
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
     Visits counter
  ========================================================= */
  async function loadVisitCount() {
    const base =
      getActiveBase() ||
      ((location.hostname === "localhost" || location.hostname === "127.0.0.1")
        ? `http://${location.hostname}:8082`
        // : "https://stephanedinahet.fr");
        : "https://loto-tracker.fr");
        // : window.location.origin);


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
     Logout
  ========================================================= */
  async function logout() {
    const base = getActiveBase();

    try {
      const opts = await withCsrfHeaders(
        { method: "POST", credentials: "include", cache: "no-store" },
        base
      );
      await fetch(`${base}${LOGOUT_PATH}`, opts);
    } catch {
      // ignore
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
     Analytics
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

  async function trackEvent(eventType, extra = {}) {
    const base =
      getActiveBase() ||
      ((location.hostname === "localhost" || location.hostname === "127.0.0.1")
        ? `http://${location.hostname}:8082`
        // : "https://stephanedinahet.fr");
        : "https://loto-tracker.fr");
        // : window.location.origin);


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

  /* =========================================================
     Axios patch : CSRF + refresh-on-401
  ========================================================= */
  function setupAxiosFastPatch() {
    if (!window.axios) return;
    if (window.__AXIOS_PATCHED__) return;
    window.__AXIOS_PATCHED__ = true;

    axios.defaults.withCredentials = true;

    axios.interceptors.request.use(async (config) => {
      const method = String(config.method || "GET").toUpperCase();
      const unsafe = ["POST", "PUT", "PATCH", "DELETE"].includes(method);
      if (!unsafe) return config;

      const base = getActiveBase();
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

    let axRefreshing = false;
    let axQueue = [];

    function axResolve(ok) {
      axQueue.forEach((r) => r(ok));
      axQueue = [];
    }

    axios.interceptors.response.use(
      (r) => r,
      async (err) => {
        const status = err?.response?.status;
        const original = err?.config;

        if (status !== 401 || !original || original._retry) {
          if (status === 403) console.warn("[axios] 403 Forbidden", err.response?.data || "");
          return Promise.reject(err);
        }

        original._retry = true;

        if (axRefreshing) {
          const ok = await new Promise((resolve) => axQueue.push(resolve));
          return ok ? axios(original) : Promise.reject(err);
        }

        axRefreshing = true;
        try {
          const ok = await refreshAccessToken(getActiveBase());
          axResolve(ok);
          if (!ok) return Promise.reject(err);
          return axios(original);
        } finally {
          axRefreshing = false;
        }
      }
    );
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

    // ✅ GOATCOUNTER : tracking global, avec exclusions
    loadGoatCounter();

    // ✅ Patch fetch/axios très tôt
    setupFetchFastPatch();
    setupAxiosFastPatch();

    // ✅ CSRF : prépare le token au chargement
    ensureCsrfToken(getActiveBase());

    // Heure
    if (!window.matchMedia("(max-width: 480px)").matches) {
      updateParisTime();
      setInterval(updateParisTime, 1000);
    }

    // Auth puis le reste
    checkUserAuthUI().finally(() => {
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


    // function renderAdminMenuItem() {
    //   return `
    //     <a href="/admin-login.html" class="btn-ghost admin-menu-item" id="adminBurgerLink">
    //       <svg class="btn-ico" viewBox="0 0 24 24" aria-hidden="true">
    //         <path d="M12 2l8 4v6c0 5-3 9-8 10C7 21 4 17 4 12V6l8-4z"></path>
    //       </svg>
    //       <span>Admin Dashboard</span>
    //     </a>
    //   `;
    // }

    // function setAdminInBurger(isAdmin) {
    //   const slot = document.getElementById("adminMenuSlot");
    //   if (!slot) return;

    //   if (isAdmin) {
    //     slot.innerHTML = renderAdminMenuItem();
    //   } else {
    //     slot.innerHTML = "";
    //   }
    // }

    function renderAdminMenuItem() {
      return `
        <a href="/admin-login.html" class="nav-item admin-menu-item" id="adminBurgerLink">
          <i class="fa-solid fa-shield-halved"></i><span>Admin</span>
        </a>
      `;
    }

    function setAdminInBurger(isAdmin) {
      const slot = document.getElementById("adminMenuSlot");
      if (!slot) return;
      slot.innerHTML = isAdmin ? renderAdminMenuItem() : "";
    }

    function setBurgerIdentity({ logged, label, isAdmin }) {
      const box = document.getElementById("burgerUserBox");
      const name = document.getElementById("burgerUserName");
      const role = document.getElementById("burgerUserRole");

      if (!box || !name || !role) return;

      if (!logged) {
        box.style.display = "none";
        name.textContent = "—";
        role.textContent = "Invité";
        return;
      }

      box.style.display = "block";
      name.textContent = label || "Utilisateur";
      role.textContent = isAdmin ? "Administrateur" : "Utilisateur";
    }



    document.dispatchEvent(new CustomEvent("layout:ready"));
  }

  document.addEventListener("DOMContentLoaded", injectLayout);
})();





(function initDrawCalendarSidebar() {
  const monthLabel = document.getElementById("monthLabel");
  const daysGrid = document.getElementById("daysGrid");
  const prevBtn = document.getElementById("prevMonth");
  const nextBtn = document.getElementById("nextMonth");

  // (optionnels si tu ajoutes les <select>)
  const monthSelect = document.getElementById("monthSelect");
  const yearSelect = document.getElementById("yearSelect");

  if (!monthLabel || !daysGrid || !prevBtn || !nextBtn) return;

  const pad2 = (n) => String(n).padStart(2, "0");
  const ymd = (d) =>
    `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;

  const MONTHS = [
    "Janvier","Février","Mars","Avril","Mai","Juin",
    "Juillet","Août","Septembre","Octobre","Novembre","Décembre"
  ];

  function isDrawDay(date) {
    const d = date.getDay();
    return d === 1 || d === 3 || d === 6; // Lun/Mer/Sam
  }

  function lastAvailableDrawDateKey() {
    const now = new Date();
    let d = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    // si jour de tirage et avant 21h -> précédent tirage
    if (isDrawDay(d) && now.getHours() < 21) {
      d = new Date(d.getFullYear(), d.getMonth(), d.getDate() - 1);
    }

    for (let i = 0; i < 14; i++) {
      const tmp = new Date(d.getFullYear(), d.getMonth(), d.getDate() - i);
      if (isDrawDay(tmp)) return ymd(tmp);
    }
    return ymd(d);
  }

  // ✅ URL selected : accepte /tirage/YYYY-MM-DD ET /tirage/YYYY-MM-DD/
  const match = location.pathname.match(/\/tirage\/(\d{4}-\d{2}-\d{2})(?:\/)?$/);
  const selectedKeyRaw = match ? match[1] : null;

  // Mois affiché = celui de l’URL sinon mois courant
  let view = new Date();
  view.setDate(1);
  if (selectedKeyRaw) {
    const [yy, mm] = selectedKeyRaw.split("-").map(Number);
    view = new Date(yy, mm - 1, 1);
  }

  function clearSelected() {
    daysGrid.querySelectorAll(".day.selected").forEach((el) =>
      el.classList.remove("selected")
    );
  }

  function syncSelects() {
    if (monthSelect) monthSelect.value = String(view.getMonth());
    if (yearSelect) yearSelect.value = String(view.getFullYear());
  }

  function renderCalendar() {
    daysGrid.innerHTML = "";

    const y = view.getFullYear();
    const m = view.getMonth();
    monthLabel.textContent = `${MONTHS[m]} ${y}`;

    const first = new Date(y, m, 1);
    const firstDow = (first.getDay() + 6) % 7; // lundi=0
    const daysInMonth = new Date(y, m + 1, 0).getDate();

    const todayKey = ymd(new Date());
    const lastKey = lastAvailableDrawDateKey();

    // ✅ clé sélectionnée : même logique que "safe" (pas de future date)
    const safeSelectedKey =
      selectedKeyRaw ? (selectedKeyRaw > lastKey ? lastKey : selectedKeyRaw) : null;

    // padding début de mois
    for (let i = 0; i < firstDow; i++) {
      const cell = document.createElement("div");
      cell.className = "day muted";
      daysGrid.appendChild(cell);
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const d = new Date(y, m, day);
      const key = ymd(d);

      const cell = document.createElement("div");
      cell.className = "day";
      cell.textContent = String(day);

      if (key === todayKey) cell.classList.add("today");

      if (isDrawDay(d)) {
        cell.classList.add("draw");

        // ✅ violet auto si URL correspond AU JOUR DE TIRAGE
        if (safeSelectedKey && key === safeSelectedKey) {
          cell.classList.add("selected");
        }

        const safe = key > lastKey ? lastKey : key;

        cell.title = `Ouvrir ${safe}`;
        cell.addEventListener("click", () => {
          // feedback violet immédiat
          clearSelected();
          cell.classList.add("selected");
          location.href = `/tirage/${safe}`;
        });
      } else {
        cell.classList.add("muted");
      }

      daysGrid.appendChild(cell);
    }

    syncSelects();
  }

  prevBtn.addEventListener("click", () => {
    view = new Date(view.getFullYear(), view.getMonth() - 1, 1);
    renderCalendar();
  });

  nextBtn.addEventListener("click", () => {
    view = new Date(view.getFullYear(), view.getMonth() + 1, 1);
    renderCalendar();
  });

  // ✅ (optionnel) Select mois/année
  function initMonthYearSelects() {
    if (!monthSelect || !yearSelect) return;

    // mois
    monthSelect.innerHTML = MONTHS.map(
      (name, idx) => `<option value="${idx}">${name}</option>`
    ).join("");

    // années (ex : 2019 -> année+1)
    const nowY = new Date().getFullYear();
    const minY = 2019;
    const maxY = nowY + 1;

    yearSelect.innerHTML = Array.from({ length: (maxY - minY + 1) }, (_, i) => {
      const yy = minY + i;
      return `<option value="${yy}">${yy}</option>`;
    }).join("");

    monthSelect.addEventListener("change", () => {
      view = new Date(view.getFullYear(), Number(monthSelect.value), 1);
      renderCalendar();
    });

    yearSelect.addEventListener("change", () => {
      view = new Date(Number(yearSelect.value), view.getMonth(), 1);
      renderCalendar();
    });
  }

  initMonthYearSelects();
  renderCalendar();
})();
