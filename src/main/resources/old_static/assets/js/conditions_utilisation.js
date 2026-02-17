  (function () {
  //   const API_BASE =
  //     (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
  //       ? "http://localhost:8082"
  //       : "https://stephanedinahet.fr";

  const HOST = window.location.hostname;

  // Liste des domaines de production
  const PROD_DOMAINS = [
    "stephanedinahet.fr",
    "loto-tracker.fr"
  ];

  // Vérifie si le host correspond à un domaine ou sous-domaine
  const IS_PROD = PROD_DOMAINS.some(domain =>
    HOST === domain ||
    HOST === `www.${domain}` ||
    HOST.endsWith(`.${domain}`)
  );

  // En prod → reverse proxy HTTPS
  // En local → port 8082
  const API_BASE = IS_PROD
    ? window.location.origin
    : `${window.location.protocol}//${HOST}:8082`;

  console.log("HOST:", HOST);
  console.log("IS_PROD:", IS_PROD);
  console.log("API_BASE:", API_BASE);


    function renderSidebar() {
      const path = (window.location.pathname || "").toLowerCase();
      const active = (name) => path.endsWith(name) ? " active" : "";

      return `
        <aside class="sidebar" id="sidebar">

          <div class="burger-user" id="burgerUserBox" style="display:none;">
            <div class="burger-user-row">
              <i class="fa-solid fa-user-shield"></i>
              <div class="burger-user-text">
                <div class="burger-user-name" id="burgerUserName">—</div>
                <div class="burger-user-role" id="burgerUserRole">Utilisateur</div>
              </div>
            </div>
          </div>

          <div id="adminMenuSlot"></div>
          <!-- <div class="nav-sep"></div> -->

          <div class="nav-title">Dashboard</div>

          <a class="nav-item${active("index.html") || (path.endsWith("/") ? " active" : "")}" href="index.html">
            <i class="fa-solid fa-chart-line"></i><span>Résultats</span>
          </a>

          <a class="nav-item auth-only${active("tickets.html")}" href="tickets.html" style="display:none;">
            <i class="fa-solid fa-ticket"></i><span>Tickets</span>
          </a>
          <a class="nav-item auth-only${active("statistiques.html")}" href="statistiques.html" style="display:none;">
            <i class="fa-solid fa-signal"></i><span>Statistiques</span>
          </a>
          <a class="nav-item auth-only${active("profil.html")}" href="profil.html" style="display:none;">
            <i class="fa-solid fa-user-gear"></i><span>Compte</span>
          </a>

          <div class="nav-sep"></div>

          <div class="nav-title">Pages</div>
          <a class="nav-item${active("mentions_legales.html")}" href="mentions_legales.html">
            <i class="fa-solid fa-file-signature"></i><span>Mentions légales</span>
          </a>
          <a class="nav-item${active("conditions_utilisation.html")}" href="conditions_utilisation.html">
            <i class="fa-solid fa-scale-balanced"></i><span>Conditions</span>
          </a>
          <a class="nav-item${active("politique_confidentialite.html")}" href="politique_confidentialite.html">
            <i class="fa-solid fa-user-shield"></i><span>Confidentialité</span>
          </a>
        </aside>
      `;
    }

    async function fetchUserInfo() {
      const res = await fetch(`${API_BASE}/api/protected/userinfo`, {
        method: "GET",
        credentials: "include",
        cache: "no-store"
      });
      if (!res.ok) throw new Error("Not authenticated");
      return await res.json();
    }

    async function applyAuthOnly() {
      let logged = false;
      try { await fetchUserInfo(); logged = true; } catch { logged = false; }
      document.querySelectorAll(".auth-only").forEach((el) => {
        el.style.display = logged ? "" : "none";
      });
    }

    document.addEventListener("layout:ready", () => {
      const mount = document.getElementById("appSidebar");
      if (mount) mount.innerHTML = renderSidebar();

      // le burger est déjà bind dans layout.js (bindBurgerGlobal) :contentReference[oaicite:3]{index=3}
      // donc pas besoin de re-binder ici

      setTimeout(applyAuthOnly, 80);
      setTimeout(applyAuthOnly, 800);
    });
  })();
