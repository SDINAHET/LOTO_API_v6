  (function () {
    const API_BASE =
      (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
        ? "http://localhost:8082"
        : "https://stephanedinahet.fr";

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

// document.addEventListener("layout:ready", () => {
//   // uniquement sur la page mentions légales
//   if (!document.body.classList.contains("page-mentions-legales")) return;

//   const footer = document.getElementById("appFooter");
//   if (!footer) return;

//   // évite les doublons
//   if (footer.querySelector(".footer-statistiques")) return;

//   const a = document.createElement("a");
//   a.href = "https://lototracker.goatcounter.com/";
//   a.target = "_blank"; // optionnel : ouvre dans un nouvel onglet
//   a.rel = "noopener noreferrer";
//   a.className = "footer-statistiques";
//   a.textContent = "Statistiques";

//   // tu peux choisir où l'insérer :
//   // - à la fin :
//   footer.appendChild(a);

//   // ou au début :
//   // footer.prepend(a);
// });
