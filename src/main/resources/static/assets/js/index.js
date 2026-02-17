// <!-- ✅ Sidebar + Auth-only + Burger -->
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

            <a class="nav-item${active("index.html") || (path.endsWith("/") ? " active" : "")}" href="index.html" data-map="show">
              <i class="fa-solid fa-chart-line"></i><span>Résultats</span>
            </a>

            <a class="nav-item auth-only${active("tickets.html")}" href="tickets.html" data-auth="required" data-map="hide" style="display:none;">
              <i class="fa-solid fa-ticket"></i><span>Tickets</span>
            </a>
            <a class="nav-item auth-only${active("statistiques.html")}" href="statistiques.html" data-auth="required" data-map="hide" style="display:none;">
              <i class="fa-solid fa-signal"></i><span>Statistiques</span>
            </a>
            <a class="nav-item auth-only${active("profil.html")}" href="profil.html" data-auth="required" data-map="hide" style="display:none;">
              <i class="fa-solid fa-user-gear"></i><span>Compte</span>
            </a>

            <div class="nav-sep"></div>

            <div class="nav-title">Jouer</div>
            <a class="nav-item" href="index.html" data-map="show">
              <i class="fa-solid fa-clover"></i><span>Loto</span>
            </a>
            <a class="nav-item${active("euromillions.html")}" href="euromillions.html" data-map="hide">
              <i class="fa-solid fa-star"></i><span>Euromillions</span>
            </a>

            <div id="sidebarMapBlock">
              <div class="nav-sep"></div>
              <div class="nav-title">Carte</div>
              <button class="btn-map" id="openMapModalBtn" type="button" data-bs-toggle="modal" data-bs-target="#mapModal">
                <i class="fa-solid fa-map-location-dot"></i> Ouvrir la carte
              </button>
            </div>
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
          const isOverlay = e.target.id === "overlay";
          if (!isOverlay) return;

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

        setTimeout(() => {
          applyAuthOnly();
        }, 80);

        setTimeout(() => {
          applyAuthOnly();
        }, 800);
      });
    })();




/* =========================
      API BASE (local / prod)
    ========================== */
    const API_BASE =
      (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
        ? "http://localhost:8082"
        : "https://stephanedinahet.fr";

    /* =========================
      Countdown next draw
    ========================== */
    function startCountdown(){
      const tirageDays = [1,3,6]; // Lundi, Mercredi, Samedi
      const joursFr = ["Dimanche","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"];
      const countdownEl = document.getElementById("countdown");
      const infoEl = document.getElementById("nextDrawInfo");

      function getNextDrawDate(base = moment().tz("Europe/Paris")){
        let next = base.clone().set({ hour:20, minute:0, second:0, millisecond:0 });
        while(!tirageDays.includes(next.day()) || next.isBefore(base)){
          next.add(1,"day");
        }
        return next;
      }

      setInterval(() => {
        const now = moment().tz("Europe/Paris");
        const next = getNextDrawDate(now);
        const diff = moment.duration(next.diff(now));

        infoEl.textContent = `Prochain tirage : ${joursFr[next.day()]} à 20h`;

        const txt = `${diff.days()}j ${diff.hours()}h ${diff.minutes()}m ${diff.seconds()}s`;
        countdownEl.textContent = txt;

        const h = diff.asHours();
        if(h <= 3){
          countdownEl.style.background = "rgba(239,68,68,.25)";
          countdownEl.style.borderColor = "rgba(239,68,68,.35)";
        }else if(h <= 7){
          countdownEl.style.background = "rgba(245,158,11,.20)";
          countdownEl.style.borderColor = "rgba(245,158,11,.35)";
        }else{
          countdownEl.style.background = "linear-gradient(135deg, rgba(59,130,246,.25), rgba(96,165,250,.18))";
          countdownEl.style.borderColor = "rgba(59,130,246,.25)";
        }
      }, 1000);
    }
    startCountdown();

    /* =========================
      Helpers
    ========================== */
    // function getDayName(dateString){
    //  const [day, month, year] = dateString.split("/").map(Number);
    //  const d = new Date(year, month-1, day);
    //  const jours = ["Dimanche","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"];
    //  return jours[d.getDay()];
    //}

    //function getDayName(dateString) {
    //  const m = moment.tz(dateString, "MM/DD/YYYY", "Europe/Paris");
    //  if (!m.isValid()) return "";

    //  const jours = ["Dimanche","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"];
    //  return jours[m.day()];
    //}

    function getDayName(dateString){
      const m = moment(dateString, "DD/MM/YYYY", true);
      if (!m.isValid()) return "";
      const jours = ["Dimanche","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"];
      return jours[m.day()];
    }




    function formatDateForAPI(dateString){
      if(!dateString.includes("/")) return dateString;
      const [dd, mm, yyyy] = dateString.split("/");
      return `${yyyy}-${mm}-${dd}`;
    }

    function safeStr(v){ return (v === null || v === undefined) ? "" : String(v); }

    //function formatDateForDisplay(v){
    //  if(!v) return "-";
      // si ISO (Mongo) -> convert en DD/MM/YYYY
    //  const m = moment(v);
    //  if(m.isValid()) return m.tz("Europe/Paris").format("DD/MM/YYYY");
    //  return safeStr(v);
    //}

    function formatDateForDisplay(v){
      if(!v) return "-";

      // ✅ si c'est "DD/MM/YYYY" (avec slash), on parse explicitement en strict
      if (typeof v === "string" && v.includes("/")) {
        const m = moment(v, "DD/MM/YYYY", true);
        if (m.isValid()) return m.format("DD/MM/YYYY");
        return v;
      }

      // ✅ ISO / Mongo / timestamp
      const mIso = moment(v);
      if (mIso.isValid()) return mIso.tz("Europe/Paris").format("DD/MM/YYYY");

      return safeStr(v);
    }


    function formatMoney(value){
      const n = Number(value);
      if (!isFinite(n) || n <= 0) return "-";
      return n.toLocaleString("fr-FR", { minimumFractionDigits: 0, maximumFractionDigits: 2 }) + " €";
    }

    function pickField(obj, names){
      for (const n of names){
        if (obj && obj[n] !== undefined && obj[n] !== null && obj[n] !== "") return obj[n];
      }
      return null;
    }

    function renderComboDots(rang){
      const rules = {
        1: { blue: 5, red: 1 },
        2: { blue: 5, red: 0 },
        3: { blue: 4, red: 1 },
        4: { blue: 4, red: 0 },
        5: { blue: 3, red: 1 },
        6: { blue: 3, red: 0 },
        7: { blue: 2, red: 1 },
        8: { blue: 2, red: 0 },
        9: { blue: 0, red: 1 },
      };
      const r = rules[rang] || { blue:0, red:0 };
      const dots = [];
      for(let i=0;i<r.blue;i++) dots.push(`<span class="combo-dot" title="Boule"></span>`);
      for(let i=0;i<r.red;i++)  dots.push(`<span class="combo-dot chance" title="Chance"></span>`);
      return dots.join("");
    }

    function buildReportsTablePrincipal(data){
      const rows = [];
      for(let rang=1; rang<=9; rang++){
        const gagnants = pickField(data, [
          `nombreDeGagnantAuRang${rang}`,
          `nombreDeGagnantDuRang${rang}`,
          `nbGagnantDuRang${rang}`,
          `nombreGagnantRang${rang}`,
          `gagnantsRang${rang}`
        ]);

        const gain = pickField(data, [
          `rapportDuRang${rang}`,
          `gainDuRang${rang}`,
          `gainRang${rang}`,
          `rapportRang${rang}`
        ]);

        rows.push(`
          <tr>
            <td data-label="Rang">${rang}</td>
            <td data-label="Combinaisons">${renderComboDots(rang)}</td>
            <td data-label="Gagnants">${(gagnants === null) ? "-" : Number(gagnants).toLocaleString("fr-FR")}</td>
            <td data-label="Gain">${formatMoney(gain)}</td>
          </tr>
        `);
      }

      return `
        <h5 class="text-center mt-3" style="font-weight:950;">Rapports du tirage principal</h5>
        <div class="mt-3">
          <table class="report-table">
            <thead>
              <tr>
                <th style="width:70px;">Rang</th>
                <th>Combinaisons</th>
                <th style="width:130px;">Gagnants</th>
                <th style="width:140px;">Gain (€)</th>
              </tr>
            </thead>
            <tbody>
              ${rows.join("")}
            </tbody>
          </table>
        </div>
      `;
    }

    function buildReportsTableSecond(data){
      // D'après ton modèle Mongo : rang 1..4 pour second tirage
      const rows = [];
      for(let rang=1; rang<=4; rang++){
        const gagnants = pickField(data, [
          `nombreDeGagnantAuRang${rang}SecondTirage`,
          `nombreDeGagnantDuRang${rang}SecondTirage`,
          `nbGagnantDuRang${rang}SecondTirage`
        ]);

        const gain = pickField(data, [
          `rapportDuRang${rang}SecondTirage`,
          `gainDuRang${rang}SecondTirage`
        ]);

        // combinaisons second tirage : c'est généralement 5 boules, sans chance.
        // on met juste des pastilles bleues (5,4,3,2) selon rang
        const blueByRang = {1:5, 2:4, 3:3, 4:2};
        const dots = Array.from({length: blueByRang[rang] || 0}).map(() => `<span class="combo-dot"></span>`).join("");

        rows.push(`
          <tr>
            <td data-label="Rang">${rang}</td>
            <td data-label="Combinaisons">${dots}</td>
            <td data-label="Gagnants">${(gagnants === null) ? "-" : Number(gagnants).toLocaleString("fr-FR")}</td>
            <td data-label="Gain">${formatMoney(gain)}</td>
          </tr>
        `);
      }

      return `
        <h5 class="text-center mt-4" style="font-weight:950;">Rapports du second tirage</h5>
        <div class="mt-3">
          <table class="report-table">
            <thead>
              <tr>
                <th style="width:70px;">Rang</th>
                <th>Combinaisons</th>
                <th style="width:130px;">Gagnants</th>
                <th style="width:140px;">Gain (€)</th>
              </tr>
            </thead>
            <tbody>
              ${rows.join("")}
            </tbody>
          </table>
        </div>
      `;
    }

    function parseCodesGagnants(data){
      const raw = pickField(data, ["codesGagnants", "codes", "winningCodes"]);
      if(!raw) return [];

      if (Array.isArray(raw)) {
        return raw.map(x => safeStr(x).trim()).filter(Boolean);
      }

      // string type: "W 8843 9882, G 7132 6516, ..."
      return safeStr(raw)
        .split(",")
        .map(s => s.trim())
        .filter(Boolean);
    }

    function buildCodesGagnantsBlock(data){
      const codes = parseCodesGagnants(data);
      if(!codes.length) return "";

      const nbCodes = pickField(data, ["nombreDeCodesGagnants", "nbCodesGagnants"]) ?? codes.length;
      const rapportCodes = pickField(data, ["rapportCodesGagnants", "gainCodesGagnants"]);
      const sorted = [...codes].sort((a,b) => a.localeCompare(b, "fr"));

      return `
        <hr style="border-color: rgba(255,255,255,.12); margin: 18px 0;">
        <div class="text-center" style="font-weight:950;">
          Codes gagnants :
          <span style="color: rgba(96,165,250,.95);">${Number(nbCodes).toLocaleString("fr-FR")}</span>
          ${rapportCodes !== null ? `<span class="muted" style="margin-left:8px;">• Gain code : ${formatMoney(rapportCodes)}</span>` : ""}
        </div>
        <div class="codes-wrap">
          ${sorted.slice(0, 50).map(c => `<div class="code-pill">${c}</div>`).join("")}
        </div>
      `;
    }

    /* =========================
      Last20
    ========================== */
    async function loadLast20(){
      try{
        const res = await axios.get(`${API_BASE}/api/historique/last20`);
        const data = res.data || [];
        const container = document.getElementById("last20");
        container.innerHTML = "";

        data.forEach(draw => {
          const dayName = getDayName(draw.dateDeTirage);
          const card = document.createElement("article");
          card.className = "result-card";
          card.innerHTML = `
            <h3>${dayName} ${draw.dateDeTirage}</h3>
            <div class="balls" aria-label="Résultat">
              <span class="ball">${draw.boule1}</span>
              <span class="ball">${draw.boule2}</span>
              <span class="ball">${draw.boule3}</span>
              <span class="ball">${draw.boule4}</span>
              <span class="ball">${draw.boule5}</span>
              <span class="ball chance">${draw.numeroChance}</span>
            </div>
            <button class="btn-mini" onclick="viewDetails('${draw.dateDeTirage}')">
              <i class="fa-solid fa-circle-info"></i> Détail du tirage
            </button>
          `;
          container.appendChild(card);
        });
      }catch(err){
        console.error(err);
      }
    }
    loadLast20();

    /* =========================
      DÉTAIL TIRAGE (MODAL COMPLET)
    ========================== */
    async function viewDetails(date){
      try{
        const formattedDate = formatDateForAPI(date);
        const apiUrl = `${API_BASE}/api/historique/last20/Detail/tirage/${formattedDate}`;

        const res = await axios.get(apiUrl);
        const data = res.data;

        if(!data){
          document.getElementById("modalBody").innerHTML = "<p class='text-warning'>Aucun détail trouvé.</p>";
          new bootstrap.Modal(document.getElementById("detailModal")).show();
          return;
        }

        //const jour = pickField(data, ["jourDeTirage", "jour"]);
        //const dateIsoOrStr = pickField(data, ["dateDeTirage"]) || date;
        //const dateAff = formatDateForDisplay(dateIsoOrStr);

        const jour = getDayName(date); // ✅ recalculé depuis la date cliquée
        const dateAff = date;          // ✅ affiche EXACTEMENT la date de la carte


        const anneeNumero = pickField(data, ["anneeNumeroDeTirage", "numeroDeTirage", "numTirage"]);
        const forclusion = pickField(data, ["dateDeForclusion", "forclusion"]);
        const combinaison = pickField(data, ["combinaisonGagnante", "combinaison", "combinaisonGagnantePrincipale"]);
        const combSecond = pickField(data, ["combinaisonGagnanteSecondTirage", "combinaisonSecondTirage"]);

        const jackpot = pickField(data, ["rapportDuRang1", "jackpotAnnonce", "jackpot", "montantDuJackpot"]);
        const joker = pickField(data, ["numeroJokerplus", "jokerPlus", "numeroJokerPlus"]);
        const devise = pickField(data, ["devise"]) || "€";

        const tirage = [data.boule1, data.boule2, data.boule3, data.boule4, data.boule5].filter(v => v != null);
        const chance = pickField(data, ["numeroChance", "chance", "bouleChance"]);

        const second = [
          data.boule1SecondTirage, data.boule2SecondTirage, data.boule3SecondTirage,
          data.boule4SecondTirage, data.boule5SecondTirage
        ].filter(v => v != null);

        document.getElementById("detailModalLabel").textContent =
          `Détail du tirage — ${(jour ? (jour + " ") : "")}${dateAff}`;

        document.getElementById("modalBody").innerHTML = `
          <!-- <div class="text-center">
            <h4 style="font-weight:1000; margin:0;">
              Résultat Loto du ${(jour ? (jour + " ") : "")}${dateAff}
            </h4>

          </div> -->

          <div class="row g-4 mt-3">
            <div class="col-md-6">
              <h5 class="text-center" style="font-weight:950;">Le tirage</h5>
              <div class="balls justify-content-center" style="margin-top:10px;">
                ${tirage.map(n => `<span class="ball">${n}</span>`).join("")}
                ${chance != null ? `<span class="ball chance">${chance}</span>` : ""}
              </div>
            </div>

            <div class="col-md-6">
              <h5 class="text-center" style="font-weight:950;">Second tirage</h5>
              <div class="balls justify-content-center" style="margin-top:10px;">
                ${second.length ? second.map(n => `<span class="ball">${n}</span>`).join("") : `<span class="muted">—</span>`}
              </div>
            </div>
          </div>

          <div class="text-center mt-3" style="font-weight:950;">
            <span style="color: rgba(96,165,250,.95);">Jackpot annoncé :</span>
            <span>${jackpot ? formatMoney(jackpot).replace(" €","") : "-" } €</span>
          </div>

          <hr style="border-color: rgba(255,255,255,.12); margin: 18px 0;">

          ${buildReportsTablePrincipal(data)}

          ${buildReportsTableSecond(data)}

          <hr style="border-color: rgba(255,255,255,.12); margin: 18px 0;">

          <div class="text-center" style="font-weight:950;">
            Joker+ :
            <span style="color: rgba(96,165,250,.95);">${joker || "-"}</span>
          </div>

          ${buildCodesGagnantsBlock(data)}
        `;

        new bootstrap.Modal(document.getElementById("detailModal")).show();
      }catch(err){
        console.error(err);
        document.getElementById("modalBody").innerHTML = "<p class='text-danger'>Erreur lors du chargement des détails.</p>";
        new bootstrap.Modal(document.getElementById("detailModal")).show();
      }
    }

    /* =========================
      Prediction modal (Chart.js)
    ========================== */
    async function showPrediction(){
      try{
        const res = await axios.get(`${API_BASE}/api/predictions/latest`);
        const data = res.data;

        if(!data || !Array.isArray(data.probableNumbers) || typeof data.sortieRates !== "object"){
          alert("Données de prédiction invalides.");
          return;
        }

        document.getElementById("predictedNumbers").innerHTML = `
          <div class="balls">
            ${data.probableNumbers.map(n => `<span class="ball">${n}</span>`).join("")}
          </div>
        `;
        document.getElementById("predictedChance").innerHTML =
          `<div class="balls"><span class="ball chance">${data.probableChance}</span></div>`;

        const canvas = document.getElementById("sortieRatesChart");
        const ctx = canvas.getContext("2d");
        if(window.__ratesChart) window.__ratesChart.destroy();

        const labels = Object.keys(data.sortieRates);
        const values = Object.values(data.sortieRates);

        window.__ratesChart = new Chart(ctx,{
          type:"bar",
          data:{ labels, datasets:[{ label:"Taux de sortie (%)", data: values, borderWidth: 1 }] },
          options:{
            responsive:true,
            plugins:{ title:{ display:true, text:"📈 Fréquence d'apparition des numéros", font:{ size:16 } } },
            scales:{ y:{ beginAtZero:true } }
          }
        });

        new bootstrap.Modal(document.getElementById("predictionModal")).show();
      }catch (err) {
        console.warn("API prédictions indisponible", err);

        const container = document.getElementById("predictedNumbers");
        const chance = document.getElementById("predictedChance");

        container.innerHTML = `
          <div class="muted text-warning text-center">
            ⏳ Les statistiques ne sont pas encore disponibles.<br>
            Réessaie dans quelques instants.
          </div>
        `;
        chance.innerHTML = "";

        // optionnel : ouvrir quand même le modal
        new bootstrap.Modal(document.getElementById("predictionModal")).show();
      }

    }

    /* =========================
      Search + pagination
    ========================== */
    let searchAllResults = [];
    let currentPage = 1;
    const pageSize = 10;

    function clampToOneMonth(startISO, endISO){
      if(!startISO) return {startISO, endISO, wasClamped:false};
      const s = moment(startISO);
      if(!endISO) return {startISO, endISO, wasClamped:false};

      const e = moment(endISO);
      const maxEnd = s.clone().add(31, "days");
      if(e.isAfter(maxEnd)){
        return { startISO, endISO: maxEnd.format("YYYY-MM-DD"), wasClamped:true };
      }
      return { startISO, endISO, wasClamped:false };
    }

    function renderSearchPage(){
      const container = document.getElementById("searchResults");
      const pagination = document.getElementById("pagination");

      container.innerHTML = "";
      pagination.innerHTML = "";

      if(searchAllResults.length === 0){
        container.innerHTML = `<div class="muted">Aucun résultat.</div>`;
        return;
      }

      const totalPages = Math.ceil(searchAllResults.length / pageSize);
      currentPage = Math.max(1, Math.min(currentPage, totalPages));

      const start = (currentPage - 1) * pageSize;
      const pageItems = searchAllResults.slice(start, start + pageSize);

      pageItems.forEach(draw => {
        const dayName = getDayName(draw.dateDeTirage);
        const card = document.createElement("article");
        card.className = "result-card";
        card.innerHTML = `
          <h3>${dayName} ${draw.dateDeTirage}</h3>
          <div class="balls">
            <span class="ball">${draw.boule1}</span>
            <span class="ball">${draw.boule2}</span>
            <span class="ball">${draw.boule3}</span>
            <span class="ball">${draw.boule4}</span>
            <span class="ball">${draw.boule5}</span>
            <span class="ball chance">${draw.numeroChance}</span>
          </div>
          <button class="btn-mini" onclick="viewDetails('${draw.dateDeTirage}')">
            <i class="fa-solid fa-circle-info"></i> Détail du tirage
          </button>
        `;
        container.appendChild(card);
      });

      const prev = document.createElement("button");
      prev.className = "page-btn";
      prev.textContent = "‹";
      prev.disabled = currentPage === 1;
      prev.onclick = () => { currentPage--; renderSearchPage(); };
      pagination.appendChild(prev);

      const totalPagesSafe = Math.max(1, totalPages);
      const maxButtons = 7;
      let startPage = Math.max(1, currentPage - Math.floor(maxButtons/2));
      let endPage = Math.min(totalPagesSafe, startPage + maxButtons - 1);
      startPage = Math.max(1, endPage - maxButtons + 1);

      for(let p = startPage; p <= endPage; p++){
        const b = document.createElement("button");
        b.className = "page-btn" + (p === currentPage ? " active" : "");
        b.textContent = p;
        b.onclick = () => { currentPage = p; renderSearchPage(); };
        pagination.appendChild(b);
      }

      const next = document.createElement("button");
      next.className = "page-btn";
      next.textContent = "›";
      next.disabled = currentPage === totalPagesSafe;
      next.onclick = () => { currentPage++; renderSearchPage(); };
      pagination.appendChild(next);
    }

    async function searchTirages(){
      let startDate = document.getElementById("startDate").value;
      let endDate = document.getElementById("endDate").value;

      if(!startDate){
        alert("Veuillez sélectionner une date de début.");
        return;
      }

      const clamped = clampToOneMonth(startDate, endDate);
      if(clamped.wasClamped){
        document.getElementById("endDate").value = clamped.endISO;
        endDate = clamped.endISO;
        document.getElementById("searchHint").textContent = "Plage limitée automatiquement à 1 mois maximum.";
      }else{
        document.getElementById("searchHint").textContent = "Astuce : si tu ne mets pas “Au”, on cherche uniquement sur “Du”.";
      }

      let apiUrl;
      if(!endDate){
        apiUrl = `${API_BASE}/api/historique/last20/Detail/tirages?startDate=${startDate}`;
      }else{
        apiUrl = `${API_BASE}/api/historique/last20/Detail/tirages?startDate=${startDate}&endDate=${endDate}`;
      }

      const container = document.getElementById("searchResults");
      container.innerHTML = `<div class="muted">Chargement...</div>`;
      document.getElementById("pagination").innerHTML = "";

      try{
        const res = await axios.get(apiUrl);
        const raw = res.data;
        searchAllResults = Array.isArray(raw) ? raw : (raw ? [raw] : []);
        currentPage = 1;
        renderSearchPage();
      }catch(err){
        console.error(err);
        container.innerHTML = `<div class="text-danger">Erreur lors de la recherche.</div>`;
      }
    }

    /* =========================
      MAP (Leaflet + Overpass)
    ========================== */
    let mapDesktop, mapMobile, userMarkerDesktop, userMarkerMobile;
    let poiLayerDesktop = L.layerGroup();
    let poiLayerMobile = L.layerGroup();

    let userLat = 48.1173; // Rennes
    let userLon = -1.6778;


    // ✅ Icône rouge pour la position utilisateur
    const userRedIcon = L.icon({
      iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",
      shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    // ✅ Icône GPS animé (DivIcon) — à placer AVANT initMapDesktop/initMapMobile
    const gpsDivIcon = L.divIcon({
      className: "",
      html: `
        <div class="gps-wrap">
          <div class="gps-pulse"></div>
          <div class="gps-dot"></div>
        </div>
      `,
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    });


    function initMapDesktop(){
      mapDesktop = L.map('map', { zoomControl: true }).setView([userLat, userLon], 11);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap'
      }).addTo(mapDesktop);

      poiLayerDesktop.addTo(mapDesktop);

      //userMarkerDesktop = L.marker([userLat, userLon]).addTo(mapDesktop)
      userMarkerDesktop = L.marker([userLat, userLon], { icon: userRedIcon }).addTo(mapDesktop)
      //userMarkerDesktop = L.marker([userLat, userLon], { icon: gpsDivIcon, interactive: true }).addTo(mapDesktop)


        .bindPopup("📍 Votre position (approx.)");

      fetchPOIs(userLat, userLon, 30000, mapDesktop, poiLayerDesktop, "mapStatus");
    }

    function initMapMobile(){
      mapMobile = L.map('mapMobile', { zoomControl: true }).setView([userLat, userLon], 11);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap'
      }).addTo(mapMobile);

      poiLayerMobile.addTo(mapMobile);

      //userMarkerMobile = L.marker([userLat, userLon]).addTo(mapMobile)
      userMarkerMobile = L.marker([userLat, userLon], { icon: userRedIcon }).addTo(mapMobile)
      //userMarkerMobile = L.marker([userLat, userLon], { icon: gpsDivIcon, interactive: true }).addTo(mapMobile)


        .bindPopup("📍 Votre position (approx.)");

      fetchPOIs(userLat, userLon, 30000, mapMobile, poiLayerMobile, "mapStatusMobile");
    }

    async function fetchPOIs(lat, lon, radiusMeters, map, layerGroup, statusElId){
      const statusEl = document.getElementById(statusElId);
      if (!statusEl) return;

      statusEl.textContent = `Localisation : ${lat.toFixed(4)}, ${lon.toFixed(4)} — recherche 30 km…`;
      layerGroup.clearLayers();

      const query = `
        [out:json][timeout:25];
        (
          node(around:${radiusMeters},${lat},${lon})["shop"="tobacco"];
          node(around:${radiusMeters},${lat},${lon})["shop"="kiosk"];
          node(around:${radiusMeters},${lat},${lon})["shop"="convenience"];
          node(around:${radiusMeters},${lat},${lon})["amenity"="newsagent"];
        );
        out center tags;
      `.trim();

      try{
        const res = await fetch("https://overpass-api.de/api/interpreter", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
          body: "data=" + encodeURIComponent(query)
        });

        if(!res.ok) throw new Error("Overpass error");
        const json = await res.json();

        const elements = (json.elements || []).slice(0, 120);

        if(elements.length === 0){
          statusEl.textContent = "Aucun point trouvé dans 30 km (OSM/Overpass).";
          return;
        }

        elements.forEach(el => {
          const name = (el.tags && el.tags.name) ? el.tags.name : "Point proche";
          const type = (el.tags && (el.tags.shop || el.tags.amenity)) ? (el.tags.shop || el.tags.amenity) : "commerce";
          const addr = [
            el.tags?.["addr:housenumber"],
            el.tags?.["addr:street"],
            el.tags?.["addr:postcode"],
            el.tags?.["addr:city"]
          ].filter(Boolean).join(" ");

          const m = L.circleMarker([el.lat, el.lon], { radius: 7, weight: 1 })
            .bindPopup(`<b>${name}</b><br><span>${type}</span><br><small>${addr || ""}</small>`);

          layerGroup.addLayer(m);
        });

        map.setView([lat, lon], 11);
        statusEl.textContent = `Localisation : ${lat.toFixed(4)}, ${lon.toFixed(4)} — ${elements.length} points trouvés.`;
      }catch(e){
        console.error(e);
        statusEl.textContent = "Erreur Overpass. Réessaie dans quelques secondes.";
      }
    }

    function applyUserPosition(lat, lon){
      userLat = lat; userLon = lon;
      if(mapDesktop){
        mapDesktop.setView([lat, lon], 12);
        userMarkerDesktop.setLatLng([lat, lon]).bindPopup("📍 Votre position");
        fetchPOIs(lat, lon, 30000, mapDesktop, poiLayerDesktop, "mapStatus");
      }
      if(mapMobile){
        mapMobile.setView([lat, lon], 12);
        userMarkerMobile.setLatLng([lat, lon]).bindPopup("📍 Votre position");
        fetchPOIs(lat, lon, 30000, mapMobile, poiLayerMobile, "mapStatusMobile");
      }
    }

    async function locateUser(){
      return new Promise((resolve, reject) => {
        if(!navigator.geolocation) return reject(new Error("Geolocation unsupported"));
        navigator.geolocation.getCurrentPosition(
          pos => resolve(pos.coords),
          err => reject(err),
          { enableHighAccuracy:true, timeout:8000, maximumAge:60000 }
        );
      });
    }

    document.getElementById("locateBtn")?.addEventListener("click", async () => {
      try{
        const coords = await locateUser();
        applyUserPosition(coords.latitude, coords.longitude);
      }catch(e){
        alert("Impossible de récupérer la position. Autorise la localisation dans le navigateur.");
      }
    });
    document.getElementById("refreshPoisBtn")?.addEventListener("click", () => {
      if(mapDesktop) fetchPOIs(userLat, userLon, 30000, mapDesktop, poiLayerDesktop, "mapStatus");
    });

    document.getElementById("locateBtnMobile")?.addEventListener("click", async () => {
      try{
        const coords = await locateUser();
        applyUserPosition(coords.latitude, coords.longitude);
      }catch(e){
        alert("Impossible de récupérer la position.");
      }
    });
    document.getElementById("refreshPoisBtnMobile")?.addEventListener("click", () => {
      if(mapMobile) fetchPOIs(userLat, userLon, 30000, mapMobile, poiLayerMobile, "mapStatusMobile");
    });

    initMapDesktop();

    const mapModalEl = document.getElementById("mapModal");
    mapModalEl?.addEventListener("shown.bs.modal", () => {
      if(!mapMobile) initMapMobile();
      else mapMobile.invalidateSize();
    });

  // =========================
  // Cookies popup (simple + UX)
  // =========================
  const popup = document.getElementById("cookie-popup");
  const acceptBtn = document.getElementById("accept-cookies");
  const rejectBtn = document.getElementById("reject-cookies");

  function safeGetConsent() {
    try { return localStorage.getItem("cookieConsent"); }
    catch (e) { return null; }
  }

  function safeSetConsent(value) {
    try { localStorage.setItem("cookieConsent", value); }
    catch (e) { /* localStorage indisponible */ }
  }

  function openCookies() {
    if (!popup) return;
    if (!popup.hidden) return; // évite réouverture en boucle
    popup.hidden = false;
    document.body.classList.add("popup-open");
  }

  function closeCookies(choice) {
    if (!popup) return;
    safeSetConsent(choice);
    popup.hidden = true;
    document.body.classList.remove("popup-open");
  }

  // Init
  (function initCookies() {
    if (!popup) return;

    const consent = safeGetConsent();
    if (!consent) openCookies();
    else closeCookies(consent); // ferme proprement + enlève popup-open
  })();

  // Boutons
  acceptBtn?.addEventListener("click", () => closeCookies("accepted"));
  rejectBtn?.addEventListener("click", () => closeCookies("rejected"));

  // ESC ferme
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && popup && !popup.hidden) closeCookies("rejected");
  });

  // Clic sur le fond sombre = ferme (option UX)
  popup?.addEventListener("click", (e) => {
    if (e.target === popup) closeCookies("rejected");
  });

  // Lien footer "🍪 Cookies" => ouvre (sans boucle)
  document.addEventListener("click", (e) => {
    const link = e.target.closest("#openCookiePrefs");
    if (!link) return;
    e.preventDefault();
    openCookies();
  });


  /* =========================
      Visits counter
    ========================= */
    /*async function updateVisitCount() {
      const el = document.getElementById("visitCount");
      if (!el) return;

      try {
        // ✅ Option A: incrémente UNE fois par session (évite refresh = +1)
        const key = "visitCountedThisSession";
        const already = sessionStorage.getItem(key) === "1";

        const url = already
          ? `${API_BASE}/api/visits/total`   // juste lire
          : `${API_BASE}/api/visits`;        // incrémenter

        const res = await fetch(url, { cache: "no-store" });
        if (!res.ok) throw new Error("Visits API error");

        const data = await res.json();
        const total = data.total ?? data.value ?? data.visits ?? null;

        if (total !== null) {
          el.textContent = Number(total).toLocaleString("fr-FR");
          if (!already) sessionStorage.setItem(key, "1");
        } else {
          el.textContent = "—";
        }
      } catch (e) {
        console.warn("Visites indisponibles", e);
        el.textContent = "—";
      }
    }

    // Lance au chargement
    updateVisitCount();*/












