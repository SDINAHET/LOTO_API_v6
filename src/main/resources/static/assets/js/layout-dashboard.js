/* layout-dashboard.js
   - Topbar glass moderne
   - Burger mobile (<900px) pilote la sidebar admin (#sidebar + .is-open + #sidebarOverlay)
   - Affiche "Bienvenue, user" depuis /api/protected/userinfo
   - Logout -> /admin-login.html
*/
(function () {
  const HOST = window.location.hostname;
  const API_BASE =
    (HOST === "localhost" || HOST === "127.0.0.1")
      ? `http://${HOST}:8082`
      : "https://stephanedinahet.fr";

  const USERINFO_PATH = "/api/protected/userinfo";
  const LOGOUT_PATH = "/api/auth/logout";

  function ensureStyles() {
    if (document.getElementById("layoutDashboardStyles")) return;

    const style = document.createElement("style");
    style.id = "layoutDashboardStyles";
    style.textContent = `
      :root{
        --topbar-h: 72px;
        --stroke: rgba(255,255,255,.10);
        --text: rgba(255,255,255,.92);
        --muted: rgba(255,255,255,.62);
        --glass: rgba(10,16,28,.72);
        --shadow: 0 18px 55px rgba(0,0,0,.45);
      }

      body{ padding-top: var(--topbar-h); }

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
        background: var(--glass);
        backdrop-filter: blur(14px);
        border-bottom: 1px solid var(--stroke);
      }

      .brand{
        display:flex;
        align-items:center;
        gap: 12px;
        text-decoration:none;
        color: var(--text);
        min-width: 0;
      }
      .brand-logo{
        width: 42px; height: 42px;
        object-fit: contain;
        filter: drop-shadow(0 6px 14px rgba(0,0,0,.35));
      }
      .brand-title{
        font-weight: 900;
        letter-spacing:.2px;
        line-height: 1.1;
        white-space: nowrap;
      }
      .brand-sub{
        font-size: .9rem;
        color: var(--muted);
        margin-top: 2px;
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
        justify-content:center;
        gap: 8px;
        padding:10px 12px;
        border-radius: 14px;
        border: 1px solid var(--stroke);
        background: rgba(255,255,255,.06);
        color: var(--text);
        text-decoration:none;
        font-weight: 800;
        white-space: nowrap;
        transition: transform .15s ease, background .15s ease, border-color .15s ease;
      }
      .btn-ghost:hover{
        transform: translateY(-1px);
        background: rgba(255,255,255,.08);
        border-color: rgba(255,255,255,.16);
      }

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

      /* Burger visible seulement en mobile */
      .btn-burger{ display:none; width:44px; height:44px; padding:0; border-radius:14px; }
      @media (max-width: 900px){ .btn-burger{ display:inline-flex; } }

      .user-chip{
        display:inline-flex;
        align-items:center;
        gap: 12px;
      }
      .user-chip span{
        color: var(--text);
        font-weight: 800;
        opacity: .95;
      }
      .user-chip b{ font-weight: 900; }

      #logoutBtn{
        display:inline-flex;
        align-items:center;
        gap:8px;
        padding:10px 12px;
        border-radius:14px;
        border:1px solid rgba(239,68,68,.22);
        background: rgba(239,68,68,.12);
        color: var(--text);
        font-weight: 900;
        cursor:pointer;
        transition: transform .15s ease, background .15s ease;
      }
      #logoutBtn:hover{ transform: translateY(-1px); background: rgba(239,68,68,.18); }

      @media (max-width: 520px){
        .brand > div{ display:none; }
        #logoutBtn{ padding:8px 10px; }
      }
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

  async function fetchUserInfo() {
    const res = await fetch(`${API_BASE}${USERINFO_PATH}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store"
    });
    if (!res.ok) throw new Error(`userinfo ${res.status}`);
    return await res.json();
  }

  function bindBurger() {
    const burger = document.getElementById("burgerBtn");
    const sidebar = document.getElementById("sidebar");
    const overlay = document.getElementById("sidebarOverlay");

    if (!burger || !sidebar || !overlay) return;

    function open() {
      sidebar.classList.add("is-open");
      overlay.hidden = false;
      document.body.style.overflow = "hidden";
    }
    function close() {
      sidebar.classList.remove("is-open");
      overlay.hidden = true;
      document.body.style.overflow = "";
    }

    burger.addEventListener("click", () => {
      const isOpen = sidebar.classList.contains("is-open");
      isOpen ? close() : open();
    });

    overlay.addEventListener("click", close);
    document.addEventListener("keydown", (e) => { if (e.key === "Escape") close(); });
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
      userChip.style.display = "none";
    }

    logoutBtn.addEventListener("click", async () => {
      try {
        await fetch(`${API_BASE}${LOGOUT_PATH}`, {
          method: "POST",
          credentials: "include",
          cache: "no-store"
        });
      } catch {}
      window.location.href = "/admin-login.html";
    });
  }

  // INIT
  ensureStyles();
  const headerSlot = document.getElementById("appHeader");
  if (headerSlot) headerSlot.innerHTML = renderHeader();

  bindBurger();
  bindAuthUI();
})();
