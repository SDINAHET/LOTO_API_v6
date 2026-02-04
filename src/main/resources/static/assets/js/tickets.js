    (function(){
      moment.locale("fr");

      function getCookie(name){
        const m = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
        return m ? decodeURIComponent(m[2]) : null;
      }

      axios.defaults.withCredentials = true;

      async function ensureCsrf(base){
        if (window.ensureCsrfToken) {
          await window.ensureCsrfToken(base);
        }
      }

      const API_BASE = (window.getActiveBase && window.getActiveBase()) || (
        (location.hostname === "localhost" || location.hostname === "127.0.0.1")
          ? `http://${location.hostname}:8082`
          : "https://stephanedinahet.fr"
      );



      const ME_URL      = `${API_BASE}/api/auth/me`;
      const API_TICKETS = `${API_BASE}/api/tickets`;
      const API_GAINS   = `${API_BASE}/api/gains`;
      const API_TIRAGE  = `${API_BASE}/api/historique/last20/Detail/tirage/`;

      const okBox  = document.getElementById("okBox");
      const okText = document.getElementById("okText");
      const errBox = document.getElementById("errBox");
      const errText= document.getElementById("errText");

      const tableBody = document.getElementById("ticketsTable");
      const pagination = document.getElementById("pagination");
      const pageInfo = document.getElementById("pageInfo");
      const pageSizeSelect = document.getElementById("pageSizeSelect");
      const addTicketBtn = document.getElementById("addTicketBtn");

      let allTickets = [];
      let gainsMap = new Map();   // String(ticketId) -> gainAmount
      let currentPage = 1;
      let pageSize = 10;

      const tirageCache = {};     // YYYY-MM-DD -> tirage|null

      function showOk(msg){
        errBox.classList.add("d-none");
        okText.textContent = msg || "OK.";
        okBox.classList.remove("d-none");
        setTimeout(() => okBox.classList.add("d-none"), 2500);
      }
      function showErr(msg){
        okBox.classList.add("d-none");
        errText.textContent = msg || "Erreur.";
        errBox.classList.remove("d-none");
      }

      async function ensureAuth(){
        try{
          await axios.get(ME_URL, { withCredentials: true });
          return true;
        }catch(e){
          showErr("Session expirée. Redirection vers la connexion…");
          setTimeout(() => window.location.href = "login.html", 900);
          return false;
        }
      }

      function ymd(dateStr){
        return moment.tz(dateStr, "Europe/Paris").format("YYYY-MM-DD");
      }

      function formatDate(dateStr){
        return moment.tz(dateStr, "Europe/Paris").format("DD MMMM YYYY");
      }

      function formatGain(gain){
        if (gain === null || gain === undefined) return "-";
        const n = Number(gain);
        if (Number.isNaN(n)) return "-";
        return `${n.toFixed(2)} €`;
      }

      // ✅ sécurisé : jamais NaN
      function formatNumbers(numbersStr){
        if (!numbersStr) return "N/A";

        const nums = numbersStr
          .split("-")
          .map(s => parseInt(String(s).trim(), 10))
          .filter(n => !Number.isNaN(n));

        if (nums.length !== 5) return "N/A";

        const balls = nums.map(n => {
          const v = String(n);
          return `<span class="num-main ticket-main-number" data-value="${v}">${v}</span>`;
        }).join("");

        return `<div class="ticket-numbers-grid">${balls}</div>`;
      }

      function formatChance(num){
        if (num === null || num === undefined || num === "") return "N/A";
        const v = String(parseInt(num,10));
        if (v === "NaN") return "N/A";
        return `<span class="num-chance ticket-chance-number" data-value="${v}">${v}</span>`;
      }

      // ✅ statut: "En attente" jusqu'à 20h, puis "Calcul en cours" si pas de gain
      // + prise en compte tirage manquant
      function getStatus(drawDateStr, gain, tirage){
        const drawDay = moment.tz(drawDateStr, "Europe/Paris").startOf("day");
        const now = moment().tz("Europe/Paris");

        const dow = drawDay.day();
        if (![1,3,6].includes(dow)) {
          return { label: "Erreur date", cls: "bg-danger text-white", eyeEnabled: false };
        }

        if (tirage && tirage.__missing) {
          return { label: "Tirage non disponible", cls: "bg-secondary text-white", eyeEnabled: true };
        }

        const drawAt20 = drawDay.clone().hour(20).minute(0).second(0);

        if (now.isBefore(drawAt20)) {
          return { label: "En attente", cls: "bg-secondary text-white", eyeEnabled: false };
        }

        if (gain === undefined || gain === null || Number.isNaN(Number(gain))) {
          return { label: "Calcul en cours", cls: "bg-secondary text-white", eyeEnabled: true };
        }

        if (Number(gain) > 0) return { label: "Gagné", cls: "bg-success text-white", eyeEnabled: true };
        return { label: "Perdu", cls: "bg-warning text-dark", eyeEnabled: true };
      }

      // ✅ cache tirage + expiration du __missing (10 min)
      async function getTirageByYMD(key){
        if (tirageCache[key] !== undefined) {
          const cached = tirageCache[key];
          if (cached && cached.__missing && cached.__ts && (Date.now() - cached.__ts) > 10*60*1000) {
            delete tirageCache[key];
          } else {
            return cached;
          }
        }

        try{
          const resp = await axios.get(`${API_TIRAGE}${key}`, { withCredentials: true });
          tirageCache[key] = resp.data || null;
          return tirageCache[key];
        }catch(e){
          const status = e?.response?.status;
          if (status === 404){
            tirageCache[key] = { __missing: true, __ts: Date.now() };
            return tirageCache[key];
          }
          tirageCache[key] = null;
          return null;
        }
      }

      function highlightTicketNumbers(ticketId, winningNumbers, winningChance){
        const row = document.getElementById(`ticket-${ticketId}`);
        if (!row) return;

        row.querySelectorAll(".ticket-main-number").forEach(span => {
          const val = span.dataset.value;
          if (winningNumbers.includes(val)) span.classList.add("num-main-win");
        });

        const chanceSpan = row.querySelector(".ticket-chance-number");
        if (chanceSpan && winningChance){
          if (chanceSpan.dataset.value === winningChance) chanceSpan.classList.add("num-chance-win");
        }
      }

      function parseMoneyLike(v){
        if (v === null || v === undefined || v === "") return 0;
        if (typeof v === "number") return v;
        const s = String(v).replace(/\s/g,"").replace(",",".");
        const n = parseFloat(s);
        return Number.isNaN(n) ? 0 : n;
      }

      function computeSecondDrawGain(ticket, tirage){
        if (!tirage) return 0;

        const secondDrawNumbers = [
          tirage.boule1SecondTirage,
          tirage.boule2SecondTirage,
          tirage.boule3SecondTirage,
          tirage.boule4SecondTirage,
          tirage.boule5SecondTirage
        ].filter(v => v !== null && v !== undefined && v !== "")
         .map(v => parseInt(v, 10))
         .filter(v => !Number.isNaN(v));

        if (secondDrawNumbers.length !== 5) return 0;

        const ticketNumbers = (ticket.numbers || "")
          .split("-")
          .map(n => parseInt(n,10))
          .filter(n => !Number.isNaN(n));

        if (ticketNumbers.length !== 5) return 0;

        let matches = 0;
        for (const n of ticketNumbers) if (secondDrawNumbers.includes(n)) matches++;

        let field = null;
        if (matches === 5) field = "rapportDuRang1SecondTirage";
        else if (matches === 4) field = "rapportDuRang2SecondTirage";
        else if (matches === 3) field = "rapportDuRang3SecondTirage";
        else if (matches === 2) field = "rapportDuRang4SecondTirage";
        else return 0;

        return parseMoneyLike(tirage[field]);
      }

      function computeMainDrawGain(ticket, tirage){
        if (!tirage) return 0;

        const winningNumbers = [
          tirage.boule1, tirage.boule2, tirage.boule3, tirage.boule4, tirage.boule5
        ].filter(v => v !== null && v !== undefined && v !== "")
        .map(v => parseInt(v, 10))
        .filter(v => !Number.isNaN(v));

        const winningChance = (tirage.numeroChance !== null && tirage.numeroChance !== undefined && tirage.numeroChance !== "")
          ? parseInt(tirage.numeroChance, 10)
          : null;

        if (winningNumbers.length !== 5 || winningChance === null || Number.isNaN(winningChance)) return 0;

        const ticketNumbers = (ticket.numbers || "")
          .split("-")
          .map(n => parseInt(n,10))
          .filter(n => !Number.isNaN(n));

        const ticketChance = parseInt(ticket.chanceNumber, 10);
        if (ticketNumbers.length !== 5 || Number.isNaN(ticketChance)) return 0;

        let matches = 0;
        for (const n of ticketNumbers) if (winningNumbers.includes(n)) matches++;

        const chanceMatch = (ticketChance === winningChance);

        let rang = null;
        if (matches === 5 && chanceMatch) rang = 1;
        else if (matches === 5) rang = 2;
        else if (matches === 4 && chanceMatch) rang = 3;
        else if (matches === 4) rang = 4;
        else if (matches === 3 && chanceMatch) rang = 5;
        else if (matches === 3) rang = 6;
        else if (matches === 2 && chanceMatch) rang = 7;
        else if (matches === 2) rang = 8;
        else if (matches === 0 && chanceMatch) rang = 9;
        else return 0;

        const field = `rapportDuRang${rang}`;
        return parseMoneyLike(tirage[field]);
      }

      function computeProvisionalGain(ticket, tirage){
        const main = computeMainDrawGain(ticket, tirage);
        const second = computeSecondDrawGain(ticket, tirage);
        return {
          total: (main || 0) + (second || 0),
          main: main || 0,
          second: second || 0
        };
      }

      function renderGainHtml({backendGain, provisional}){
        if (backendGain !== null && backendGain !== undefined && !Number.isNaN(Number(backendGain))){
          return formatGain(backendGain);
        }

        if (!provisional || provisional.total <= 0) return "-";

        const total = provisional.total;
        const main = provisional.main;
        const second = provisional.second;

        const lines = [];

        if (main > 0 && second > 0){
          lines.push(`<div>~ <b>${formatGain(main)}</b> <span class="muted">(principal)</span></div>`);
          lines.push(`<div>+ <b>${formatGain(second)}</b> <span class="muted">(2nd tirage)</span></div>`);
          lines.push(`<div class="muted" style="font-size:.85rem;">Total provisoire ~ <b>${formatGain(total)}</b></div>`);
        } else {
          lines.push(`<div>~ <b>${formatGain(total)}</b> <span class="muted">(provisoire)</span></div>`);
        }

        return lines.join("");
      }

      function setBadge(el, label, cls){
        if (!el) return;
        el.textContent = label;
        const keepInline = el.classList.contains("badge-inline-mobile");
        el.className = `badge ${cls}` + (keepInline ? " badge-inline-mobile" : "");
      }

      function totalPages(){
        return Math.max(1, Math.ceil(allTickets.length / pageSize));
      }

      function renderPagination(){
        const tp = totalPages();
        if (currentPage > tp) currentPage = tp;

        pageInfo.textContent =
          window.innerWidth <= 575
            ? `${currentPage}/${tp}`
            : `Page ${currentPage} / ${tp}`;

        pagination.innerHTML = "";

        const mkBtn = (label, page, disabled=false, active=false) => {
          const li = document.createElement("li");
          li.className = `page-item ${disabled ? "disabled" : ""} ${active ? "active" : ""}`;
          const btn = document.createElement("button");
          btn.className = "page-link";
          btn.type = "button";
          btn.innerHTML = label;
          btn.onclick = () => changePage(page);
          li.appendChild(btn);
          return li;
        };

        pagination.appendChild(mkBtn("&laquo;", currentPage-1, currentPage===1));

        const max = window.innerWidth <= 575 ? 3 : 7;

        let start = currentPage - Math.floor(max / 2);
        let end   = start + max - 1;

        if (start < 1) { start = 1; end = Math.min(tp, start + max - 1); }
        if (end > tp)  { end = tp; start = Math.max(1, end - max + 1); }

        for (let p = start; p <= end; p++){
          pagination.appendChild(mkBtn(String(p), p, false, p===currentPage));
        }

        pagination.appendChild(mkBtn("&raquo;", currentPage+1, currentPage===tp));
      }

      function changePage(p){
        const tp = totalPages();
        if (p < 1 || p > tp) return;
        currentPage = p;

        localStorage.setItem("tickets.page", String(currentPage));
        localStorage.setItem("tickets.size", String(pageSize));

        renderTicketsPage();
      }

      async function renderTicketsPage(){
        const start = (currentPage - 1) * pageSize;
        const end   = start + pageSize;
        const pageTickets = allTickets.slice(start, end);

        tableBody.innerHTML = "";

        if (!pageTickets.length){
          tableBody.innerHTML = `
            <tr>
              <td colspan="6" class="text-center muted py-4">
                Aucun ticket pour le moment.
              </td>
            </tr>`;
          renderPagination();
          return;
        }

        // on prépare le cache des tirages pour la page (utile pour status + highlight + provisoire)
        const uniqueDates = [...new Set(pageTickets.map(t => ymd(t.drawDate)))];
        const tirages = await Promise.all(uniqueDates.map(d => getTirageByYMD(d)));
        const tirageByDate = new Map(uniqueDates.map((d,i) => [d, tirages[i]]));

        for (const ticket of pageTickets){
          const tid = String(ticket.id);
          let gain = gainsMap.has(tid) ? Number(gainsMap.get(tid)) : undefined;

          const tYMD = ymd(ticket.drawDate);
          const tirage = tirageByDate.get(tYMD);

          const st = getStatus(ticket.drawDate, gain, tirage);
          const eyeDisabled = st.eyeEnabled ? "" : "disabled";

          const rowHtml = `
            <tr id="ticket-${tid}">
              <td data-label="Date"><b>${formatDate(ticket.drawDate)}</b></td>

              <td data-label="Numéros">${formatNumbers(ticket.numbers)}</td>

              <td data-label="Chance">
                <div class="ticket-chance-wrap">
                  ${formatChance(ticket.chanceNumber)}
                </div>
              </td>

              <td data-label="Statut" class="td-status">
                <span id="status-${tid}" class="badge ${st.cls}">${st.label}</span>
              </td>

              <td data-label="Gains" class="text-end td-gain">
                <div class="gain-status-line">
                  <b id="gain-cell-${tid}">${gain !== undefined ? formatGain(gain) : "-"}</b>
                  <span class="badge ${st.cls} badge-inline-mobile" id="status-inline-${tid}">
                    ${st.label}
                  </span>
                </div>
              </td>

              <td data-label="Actions" class="text-end">
                <button class="btn-action view ${st.eyeEnabled ? '' : 'opacity-50'}"
                        ${eyeDisabled}
                        onclick="window.__showTirageDetails && window.__showTirageDetails('${ticket.drawDate}')">
                  <i class="fa-solid fa-eye"></i>
                </button>

                <a class="btn-action edit ms-1" href="put_ticket.html?id=${tid}">
                  <i class="fa-solid fa-pen"></i>
                </a>

                <button class="btn-action delete ms-1"
                        onclick="window.__deleteTicket && window.__deleteTicket('${tid}')">
                  <i class="fa-solid fa-trash"></i>
                </button>
              </td>
            </tr>
          `;
          tableBody.insertAdjacentHTML("beforeend", rowHtml);

          // highlight + provisoire si tirage OK
          if (tirage && !tirage.__missing){
            const winningNumbers = [
              tirage.boule1, tirage.boule2, tirage.boule3, tirage.boule4, tirage.boule5
            ].filter(v => v !== null && v !== undefined && v !== "")
             .map(v => String(parseInt(v,10)))
             .filter(v => v !== "NaN");

            const winningChance = (tirage.numeroChance !== null && tirage.numeroChance !== undefined && tirage.numeroChance !== "")
              ? String(parseInt(tirage.numeroChance,10))
              : null;

            highlightTicketNumbers(tid, winningNumbers, winningChance);

            // ✅ provisoire UNIQUEMENT pour le ticket ciblé, dans la fenêtre active
            const isTarget =
              window.__burstTicketId && String(tid) === String(window.__burstTicketId);

            const isInBurst =
              isTarget && window.__provisionalUntil && Date.now() < window.__provisionalUntil;

            if (isInBurst){
              const backendGain = gainsMap.has(tid) ? Number(gainsMap.get(tid)) : null;

              if (backendGain === null || backendGain === undefined || Number.isNaN(backendGain)){
                const prov = computeProvisionalGain(ticket, tirage);
                const gainCell = document.getElementById(`gain-cell-${tid}`);

                // ✅ provisoire gagnant
                if (prov && prov.total > 0){
                  if (gainCell) gainCell.innerHTML = renderGainHtml({ backendGain: null, provisional: prov });
                  setBadge(document.getElementById(`status-${tid}`), "Gagné (provisoire)", "bg-success text-white");
                  setBadge(document.getElementById(`status-inline-${tid}`), "Gagné (provisoire)", "bg-success text-white");
                }
                // ✅ provisoire perdant (total = 0) => AU LIEU de laisser "Calcul en cours"
                else if (prov && prov.total === 0){
                  if (gainCell){
                    gainCell.innerHTML = `<div>~ <b>${formatGain(0)}</b> <span class="muted">(provisoire)</span></div>`;
                  }
                  setBadge(document.getElementById(`status-${tid}`), "Perdu (provisoire)", "bg-warning text-dark");
                  setBadge(document.getElementById(`status-inline-${tid}`), "Perdu (provisoire)", "bg-warning text-dark");
                }
              }
            }


            // ✅ affichage second tirage si backend prêt (sinon prov gère déjà main+second)
            const secondGain = computeSecondDrawGain(ticket, tirage);
            if (secondGain > 0){
              const baseGain = gainsMap.has(tid) ? Number(gainsMap.get(tid)) : null;
              const backendReady = (baseGain !== null && baseGain !== undefined && !Number.isNaN(baseGain));

              if (backendReady){
                const gainCell = document.getElementById(`gain-cell-${tid}`);
                if (gainCell){
                  gainCell.innerHTML = `
                    ${formatGain(baseGain)}
                    <div class="muted" style="font-size:.85rem;">
                      + ${formatGain(secondGain)} <span class="muted">(2nd tirage)</span>
                    </div>
                  `;
                }
                setBadge(document.getElementById(`status-${tid}`), "Gagné", "bg-success text-white");
                setBadge(document.getElementById(`status-inline-${tid}`), "Gagné", "bg-success text-white");
              }
            }
          }
        }

        renderPagination();
      }

      // Modal détails tirage : identique à ta version (inchangé)
      async function showTirageDetails(dateStr){
        const modalEl   = document.getElementById("tirageModal");
        const modalBody = document.getElementById("modalTirageBody");
        const modalLbl  = document.getElementById("tirageModalLabel");

        modalBody.innerHTML = `
          <p class="text-center muted mb-0">
            <span class="spinner-border spinner-border-sm me-2"></span>Chargement…
          </p>
        `;

        const key = ymd(dateStr);

        const pickField = (obj, keys) => {
          for (const k of keys){
            if (obj && obj[k] !== undefined && obj[k] !== null && obj[k] !== "") return obj[k];
          }
          return null;
        };

        const formatDateForDisplay = (v) => {
          const s = (v ? String(v).trim() : key);

          if (/^\d{2}\/\d{2}\/\d{4}$/.test(s)){
            const m = moment.tz(s, "DD/MM/YYYY", true, "Europe/Paris");
            return m.isValid() ? m.format("DD/MM/YYYY") : s;
          }
          if (/^\d{4}-\d{2}-\d{2}/.test(s)){
            const m = moment.tz(s, "Europe/Paris");
            return m.isValid() ? m.format("DD/MM/YYYY") : s;
          }

          const m = moment.tz(s, "Europe/Paris");
          return m.isValid() ? m.format("DD/MM/YYYY") : s;
        };

        const moneyLike = (v) => {
          if (v === null || v === undefined || v === "") return null;
          const n = (typeof v === "number") ? v : parseFloat(String(v).replace(/\s/g,"").replace(",","."));
          return Number.isNaN(n) ? null : n;
        };

        const formatMoney = (v) => {
          const n = moneyLike(v);
          if (n === null) return "-";
          return `${n.toLocaleString("fr-FR")} €`;
        };

        const esc = (s) => String(s).replace(/[&<>"']/g, c => ({
          "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"
        }[c]));

        const buildCodesGagnantsBlock = (data) => {
          const raw = pickField(data, ["codesGagnants", "codes_gagnants", "codes", "codeGagnant", "codesGagnantsJokerPlus"]);
          if (!raw) return "";

          let items = [];
          if (Array.isArray(raw)) items = raw.map(String);
          else if (typeof raw === "object") items = Object.values(raw).map(String);
          else {
            const s = String(raw).trim();
            items = s.includes(",") ? s.split(",").map(v => v.trim()) : [s];
          }

          items = items.filter(Boolean);
          if (!items.length) return "";

          return `
            <hr class="my-4" style="border-color: rgba(255,255,255,.10);">
            <h5 class="fw-bold mb-2">Codes Loto</h5>
            <div class="d-flex flex-wrap gap-2">
              ${items.map(code => `
                <span style="
                  padding:.45rem .75rem;
                  border-radius:10px;
                  font-weight:900;
                  letter-spacing:.5px;
                  background: rgba(245,158,11,.18);
                  border: 2px solid rgba(245,158,11,.85);
                  color: #fde68a;
                  box-shadow: 0 0 0 1px rgba(0,0,0,.25) inset;
                ">
                  ${esc(code)}
                </span>
              `).join("")}
            </div>
          `;
        };

        const renderComboDotsMain = (rang) => {
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
          const r = rules[rang] || {blue:0, red:0};
          return [
            ...Array.from({length:r.blue}, () => `<span class="combo-dot" title="Boule"></span>`),
            ...Array.from({length:r.red},  () => `<span class="combo-dot chance" title="Chance"></span>`)
          ].join("");
        };

        const renderComboDotsSecond = (rang) => {
          const blueByRang = {1:5, 2:4, 3:3, 4:2};
          const n = blueByRang[rang] || 0;
          return Array.from({length:n}, () => `<span class="combo-dot" title="Boule"></span>`).join("");
        };

        try{
          const data = await getTirageByYMD(key);

          if (!data || Object.keys(data).length === 0 || data.__missing){
            modalLbl.textContent = "Détails du tirage";
            modalBody.innerHTML = `<p class="text-center muted mb-0">Aucun détail trouvé pour ce tirage.</p>`;
            new bootstrap.Modal(modalEl).show();
            return;
          }

          const jour  = pickField(data, ["jourDeTirage", "jour"]) || "";
          const dtAff = formatDateForDisplay(pickField(data, ["dateDeTirage"]) || key);

          const numeros = [data.boule1, data.boule2, data.boule3, data.boule4, data.boule5]
            .filter(v => v !== null && v !== undefined && v !== "");

          const chance = pickField(data, ["numeroChance", "chance", "bouleChance"]);

          const secondTirage = [
            data.boule1SecondTirage, data.boule2SecondTirage, data.boule3SecondTirage,
            data.boule4SecondTirage, data.boule5SecondTirage
          ].filter(v => v !== null && v !== undefined && v !== "");

          const jackpotRaw = pickField(data, ["rapportDuRang1", "jackpotAnnonce", "jackpot", "montantDuJackpot"]);
          const jackpot = formatMoney(jackpotRaw);

          const joker = pickField(data, ["numeroJokerplus", "jokerPlus", "numeroJokerPlus"]);

          const mainRows = [1,2,3,4,5,6,7,8,9].map(rang => {
            const winners = data[`nombreDeGagnantAuRang${rang}`];
            const payout  = data[`rapportDuRang${rang}`];
            return `
              <tr>
                <td data-label="Rang">${rang}</td>
                <td data-label="Combinaisons">${renderComboDotsMain(rang)}</td>
                <td data-label="Gagnants"><b>${winners ?? "-"}</b></td>
                <td data-label="Gain"><b>${payout != null ? formatMoney(payout) : "-"}</b></td>
              </tr>
            `;
          }).join("");

          const hasSecond = [1,2,3,4].some(r =>
            data[`nombreDeGagnantAuRang${r}SecondTirage`] != null ||
            data[`rapportDuRang${r}SecondTirage`] != null
          );

          let secondBlock = "";
          if (hasSecond){
            const secondRows = [1,2,3,4].map(rang => {
              const winners = data[`nombreDeGagnantAuRang${rang}SecondTirage`];
              const payout  = data[`rapportDuRang${rang}SecondTirage`];
              return `
                <tr>
                  <td data-label="Rang">${rang}</td>
                  <td data-label="Combinaisons">${renderComboDotsSecond(rang)}</td>
                  <td data-label="Gagnants"><b>${winners ?? "-"}</b></td>
                  <td data-label="Gain"><b>${payout != null ? formatMoney(payout) : "-"}</b></td>
                </tr>
              `;
            }).join("");

            secondBlock = `
              <hr class="my-4" style="border-color: rgba(255,255,255,.10);">
              <h5 class="fw-bold mb-2">Rapports du second tirage</h5>
              <div class="table-responsive modal-table">
                <table class="table table-hover mb-0">
                  <thead>
                    <tr><th>Rang</th><th>Combinaisons</th><th>Gagnants</th><th>Gain</th></tr>
                  </thead>
                  <tbody>${secondRows}</tbody>
                </table>
              </div>
            `;
          } else {
            secondBlock = `
              <hr class="my-4" style="border-color: rgba(255,255,255,.10);">
              <p class="muted mb-0">Aucun rapport pour le second tirage sur ce tirage.</p>
            `;
          }

          modalLbl.textContent = `Détails du tirage — ${jour ? (jour + " ") : ""}${dtAff}`;

          modalBody.innerHTML = `
            <div>
              <div class="row g-3 mt-1">
                <div class="col-md-6">
                  <div class="muted fw-bold mb-2">Tirage principal</div>
                  <div>
                    ${numeros.map(n => `<span class="lottery-ball">${esc(n)}</span>`).join("")}
                    <span class="lottery-ball lucky-number">${chance != null ? esc(chance) : "—"}</span>
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="muted fw-bold mb-2">Second tirage</div>
                  <div>
                    ${secondTirage.length
                      ? secondTirage.map(n => `<span class="lottery-ball">${esc(n)}</span>`).join("")
                      : `<span class="muted">Non disponible</span>`
                    }
                  </div>
                </div>
              </div>

              <div class="mt-3" style="font-weight:950;">
                <span class="muted">Jackpot annoncé :</span>
                <span class="ms-2">${esc(jackpot)}</span>
              </div>

              <hr class="my-4" style="border-color: rgba(255,255,255,.10);">

              <h5 class="fw-bold mb-2">Rapports du tirage principal</h5>
              <div class="table-responsive modal-table">
                <table class="table table-hover mb-0">
                  <thead><tr><th>Rang</th><th>Combinaisons</th><th>Gagnants</th><th>Gain</th></tr></thead>
                  <tbody>${mainRows}</tbody>
                </table>
              </div>

              ${secondBlock}

              <hr class="my-4" style="border-color: rgba(255,255,255,.10);">
              <div class="fw-bold">
                Joker+ : <span class="ms-2">${joker ? esc(joker) : "—"}</span>
              </div>

              ${buildCodesGagnantsBlock(data)}
            </div>
          `;

          new bootstrap.Modal(modalEl).show();
        }catch(e){
          console.error(e);
          modalLbl.textContent = "Détails du tirage";
          modalBody.innerHTML = `<p class="text-center text-danger mb-0">Erreur lors du chargement du tirage.</p>`;
          new bootstrap.Modal(modalEl).show();
        }
      }

      // ===== Delete ticket
      let pendingDeleteTicketId = null;
      let confirmModalInstance = null;

      function openDeleteConfirm(ticketId){
        pendingDeleteTicketId = String(ticketId);
        const modalEl = document.getElementById("confirmDeleteModal");
        confirmModalInstance = bootstrap.Modal.getOrCreateInstance(modalEl);
        confirmModalInstance.show();
      }

      async function performDeleteTicket(){
        if (!pendingDeleteTicketId) return;

        const ticketId = pendingDeleteTicketId;
        pendingDeleteTicketId = null;

        const btn = document.getElementById("confirmDeleteBtn");
        const oldHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Suppression...`;

        try{
          await ensureCsrf(API_BASE);
          const xsrf = getCookie("XSRF-TOKEN");

          await axios.delete(`${API_TICKETS}/${ticketId}`, {
            withCredentials: true,
            headers: {
              "Accept": "application/json",
              ...(xsrf ? { "X-XSRF-TOKEN": xsrf } : {})
            }
          });

          allTickets = allTickets.filter(t => String(t.id) !== String(ticketId));
          gainsMap.delete(String(ticketId));

          showOk("Ticket supprimé.");
          renderTicketsPage();

          if (confirmModalInstance) confirmModalInstance.hide();
        }catch(e){
          console.error(e);
          showErr(
            e?.response?.data?.message ||
            e?.response?.data?.error ||
            "Impossible de supprimer ce ticket."
          );
        }finally{
          btn.disabled = false;
          btn.innerHTML = oldHtml;
        }
      }

      // ✅ refresh gains + provisoire sur ticket ciblé + second tirage backend
      async function refreshGainsOnly(){
        try{
          const gainsRes = await axios.get(API_GAINS, {
            withCredentials: true,
            params: { ts: Date.now() }
          });

          const gains = Array.isArray(gainsRes.data) ? gainsRes.data : [];
          const newMap = new Map(gains.map(g => [String(g.ticketId), g.gainAmount]));

          // ✅ optimisation: lookup O(1)
          const ticketsById = new Map(allTickets.map(t => [String(t.id), t]));

          const rows = document.querySelectorAll('tbody#ticketsTable tr[id^="ticket-"]');

          const visibleTickets = [];
          rows.forEach(row => {
            const ticketId = row.id.replace("ticket-","");
            const ticket = ticketsById.get(String(ticketId));
            if (ticket) visibleTickets.push(ticket);
          });

          const uniqueDates = [...new Set(visibleTickets.map(t => ymd(t.drawDate)))];
          const tirages = await Promise.all(uniqueDates.map(d => getTirageByYMD(d)));
          const tirageByDate = new Map(uniqueDates.map((d,i) => [d, tirages[i]]));

          rows.forEach(row => {
            const ticketId = row.id.replace("ticket-","");
            const ticket = ticketsById.get(String(ticketId));
            if (!ticket) return;

            const backendValRaw = newMap.has(String(ticketId)) ? newMap.get(String(ticketId)) : null;
            const backendVal = (backendValRaw === null || backendValRaw === undefined) ? null : Number(backendValRaw);

            const isTarget =
              window.__burstTicketId && String(ticketId) === String(window.__burstTicketId);

            const isInBurst =
              isTarget && window.__provisionalUntil && Date.now() < window.__provisionalUntil;

            const backendReady =
              !isInBurst && (backendVal !== null && !Number.isNaN(backendVal));

            const gainCell = document.getElementById(`gain-cell-${ticketId}`);
            const tirage = tirageByDate.get(ymd(ticket.drawDate));

            // 1) gain affichage
            if (backendReady){
              if (gainCell) gainCell.innerHTML = formatGain(backendVal);
            } else {
              let usedProvisional = false;

            if (isInBurst && tirage && !tirage.__missing){
              const prov = computeProvisionalGain(ticket, tirage);

              // ✅ gagnant provisoire
              if (prov && prov.total > 0){
                usedProvisional = true;
                if (gainCell) gainCell.innerHTML = renderGainHtml({ backendGain: null, provisional: prov });
                setBadge(document.getElementById(`status-${ticketId}`), "Gagné (provisoire)", "bg-success text-white");
                setBadge(document.getElementById(`status-inline-${ticketId}`), "Gagné (provisoire)", "bg-success text-white");
              }
              // ✅ perdant provisoire (0)
              else if (prov && prov.total === 0){
                usedProvisional = true;
                if (gainCell){
                  gainCell.innerHTML = `<div>~ <b>${formatGain(0)}</b> <span class="muted">(provisoire)</span></div>`;
                }
                setBadge(document.getElementById(`status-${ticketId}`), "Perdu (provisoire)", "bg-warning text-dark");
                setBadge(document.getElementById(`status-inline-${ticketId}`), "Perdu (provisoire)", "bg-warning text-dark");
              }
            }


              if (!usedProvisional){
                if (gainCell) gainCell.innerHTML = "-";
              }
            }

            // 2) statut (centralisé)
            let stObj = getStatus(ticket.drawDate, backendReady ? backendVal : null, tirage);

            // si provisoire gagnant affiché
            if (isInBurst && !backendReady && tirage && !tirage.__missing){
              const prov = computeProvisionalGain(ticket, tirage);
              if (prov && prov.total > 0){
                stObj = { label: "Gagné (provisoire)", cls: "bg-success text-white", eyeEnabled: true };
              }
            }


            setBadge(document.getElementById(`status-${ticketId}`), stObj.label, stObj.cls);
            setBadge(document.getElementById(`status-inline-${ticketId}`), stObj.label, stObj.cls);

            // 3) second tirage: UNIQUEMENT si backendReady (sinon prov gère déjà total)
            if (tirage && !tirage.__missing && gainCell){
              const secondGain = computeSecondDrawGain(ticket, tirage);
              if (secondGain > 0 && backendReady){
                const baseGain = backendVal;

                gainCell.innerHTML = `
                  ${formatGain(baseGain)}
                  <div class="muted" style="font-size:.85rem;">
                    + ${formatGain(secondGain)} <span class="muted">(2nd tirage)</span>
                  </div>
                `;

                setBadge(document.getElementById(`status-${ticketId}`), "Gagné", "bg-success text-white");
                setBadge(document.getElementById(`status-inline-${ticketId}`), "Gagné", "bg-success text-white");
              }
            }
          });

          gainsMap = newMap;

        }catch(e){
          console.error("refreshGainsOnly error:", e);
        }
      }

      // ===== Polling
      let gainsIntervalId = null;
      let isRefreshing = false;

      function stopGainsPolling(){
        if (!gainsIntervalId) return;
        clearInterval(gainsIntervalId);
        gainsIntervalId = null;
      }

      function startGainsPolling(intervalMs = 30000){
        stopGainsPolling();
        refreshGainsOnly();

        gainsIntervalId = setInterval(async () => {
          if (isRefreshing) return;
          isRefreshing = true;
          try{
            await refreshGainsOnly();
          } finally {
            isRefreshing = false;
          }
        }, intervalMs);
      }

      let burstStopTimeoutId = null;

      function startBurstPolling(durationMs = 60000, intervalMs = 5000){
        if (burstStopTimeoutId) clearTimeout(burstStopTimeoutId);

        startGainsPolling(intervalMs);

        burstStopTimeoutId = setTimeout(() => {
          burstStopTimeoutId = null;

          // ✅ fin du burst
          window.__burstTicketId = null;
          window.__provisionalUntil = null;

          // ✅ on repasse au polling normal
          startGainsPolling(30000);
        }, durationMs);
      }



      async function loadTickets(){
        const auth = await ensureAuth();
        if (!auth) return;

        try{
          const ts = Date.now();
          const [ticketsRes, gainsRes] = await Promise.all([
            axios.get(API_TICKETS, { withCredentials: true, params: { ts } }),
            axios.get(API_GAINS,   { withCredentials: true, params: { ts } })
          ]);

          const tickets = Array.isArray(ticketsRes.data) ? ticketsRes.data : [];
          const gains   = Array.isArray(gainsRes.data) ? gainsRes.data : [];

          gainsMap = new Map(gains.map(g => [String(g.ticketId), g.gainAmount]));

          allTickets = tickets
            .filter(t => t && t.drawDate)
            .sort((a,b) =>
              moment.tz(b.drawDate, "Europe/Paris").valueOf() - moment.tz(a.drawDate, "Europe/Paris").valueOf()
            );

          await renderTicketsPage();
        }catch(e){
          console.error(e);
          showErr("Impossible de charger les tickets (tickets/gains).");
        }
      }

      addTicketBtn.addEventListener("click", () => window.location.href = "post_ticket.html");

      pageSizeSelect.value = String(pageSize);
      pageSizeSelect.addEventListener("change", () => {
        pageSize = parseInt(pageSizeSelect.value, 10);
        currentPage = 1;

        localStorage.setItem("tickets.page", String(currentPage));
        localStorage.setItem("tickets.size", String(pageSize));

        renderTicketsPage();
      });

      const savedPage = parseInt(localStorage.getItem("tickets.page"), 10);
      if (!Number.isNaN(savedPage) && savedPage > 0) currentPage = savedPage;

      const savedSize = parseInt(localStorage.getItem("tickets.size"), 10);
      if (!Number.isNaN(savedSize) && [10,25,50].includes(savedSize)) {
        pageSize = savedSize;
        pageSizeSelect.value = String(pageSize);
      }

      document.getElementById("confirmDeleteBtn").addEventListener("click", performDeleteTicket);

      window.__showTirageDetails = showTirageDetails;
      window.__deleteTicket = openDeleteConfirm;
      window.__refreshGainsOnly = refreshGainsOnly;

      document.addEventListener("visibilitychange", () => {
        if (document.hidden) {
          stopGainsPolling();
          return;
        }
        // si burst actif (timer en cours), ne pas relancer un polling normal
        if (burstStopTimeoutId || window.__burstTicketId) return;

        startGainsPolling(30000);
      });


      document.addEventListener("DOMContentLoaded", async () => {
        const params = new URLSearchParams(window.location.search);
        const wantsBurst = params.get("burst") === "1" || params.get("refresh") === "1";
        const burstTicketId = params.get("id");

        if (wantsBurst && burstTicketId) {
          window.__provisionalUntil = Date.now() + 30000; // 30s
          window.__burstTicketId = String(burstTicketId);
        } else {
          window.__provisionalUntil = null;
          window.__burstTicketId = null;
        }

        await loadTickets();

        if (wantsBurst && burstTicketId) {
          startBurstPolling(60000, 5000); // polling 5s puis STOP à 1 min
        } else {
          startGainsPolling(30000); // polling normal seulement hors PUT
        }

        // nettoyage URL
        params.delete("burst");
        params.delete("refresh");
        params.delete("ts");
        params.delete("id");
        const newQs = params.toString();
        const newUrl = window.location.pathname + (newQs ? ("?" + newQs) : "");
        window.history.replaceState({}, "", newUrl);
      });


      window.addEventListener("pageshow", async (e) => {
        if (!e.persisted) return;

        await loadTickets();

        // ✅ si burst actif -> burst
        if (window.__burstTicketId) {
          window.__provisionalUntil = Date.now() + 30000;
          startBurstPolling(60000, 5000);
        } else {
          startGainsPolling(30000);
        }
      });


    })();
