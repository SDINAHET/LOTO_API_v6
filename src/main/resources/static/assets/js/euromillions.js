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

            <a class="nav-item auth-only${active("tickets.html")}" href="tickets.html" data-auth="required" style="display:none;">
              <i class="fa-solid fa-ticket"></i><span>Tickets</span>
            </a>
            <a class="nav-item auth-only${active("statistiques.html")}" href="statistiques.html" data-auth="required" style="display:none;">
              <i class="fa-solid fa-signal"></i><span>Statistiques</span>
            </a>
            <a class="nav-item auth-only${active("profil.html")}" href="profil.html" data-auth="required" style="display:none;">
              <i class="fa-solid fa-user-gear"></i><span>Compte</span>
            </a>

            <div class="nav-sep"></div>

            <div class="nav-title">Jouer</div>
            <a class="nav-item" href="index.html">
              <i class="fa-solid fa-clover"></i><span>Loto</span>
            </a>
            <a class="nav-item${active("euromillions.html")}" href="euromillions.html">
              <i class="fa-solid fa-star"></i><span>Euromillions</span>
            </a>
          </aside>
        `;
      }

      function closeSidebarIfMobile() {
        const sidebar = document.getElementById("sidebar");
        const overlay = document.getElementById("overlay");
        if (!sidebar || !overlay) return;
        if (window.matchMedia("(max-width: 991px)").matches) {
          sidebar.classList.remove("open");
          overlay.classList.remove("show");
          document.body.classList.remove("no-scroll");
        }
      }

      function bindBurger() {
        if (document.documentElement.dataset.burgerDelegation === "1") return;
        document.documentElement.dataset.burgerDelegation = "1";

        document.addEventListener("click", (e) => {
          const isBurger = e.target.closest("#burgerBtn");
          if (!isBurger) return;

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

        if (!logged) {
          document.querySelectorAll(".auth-only").forEach((a) => {
            a.addEventListener("click", (e) => {
              e.preventDefault();
              closeSidebarIfMobile();
              window.location.href = "login.html";
            });
          });
        }
      }

      document.addEventListener("layout:ready", () => {
        const mount = document.getElementById("appSidebar");
        if (mount) mount.innerHTML = renderSidebar();

        bindBurger();

        setTimeout(applyAuthOnly, 80);
        setTimeout(applyAuthOnly, 800);
      });
    })();
