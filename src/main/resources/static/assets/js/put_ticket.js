    (function(){

      function getCookie(name){
        const m = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
        return m ? decodeURIComponent(m[2]) : null;
      }

      axios.defaults.withCredentials = true;

      async function ensureCsrf(base){
        if (window.ensureCsrfToken) {
          await window.ensureCsrfToken(base); // force la création du cookie XSRF-TOKEN
        }
      }

      // ===== API
      const API_BASE =
        window.__API_BASE_ACTIVE__ ||
        ((location.hostname === "localhost" || location.hostname === "127.0.0.1")
          ? `http://${location.hostname}:8082`
          : "https://stephanedinahet.fr");

      //const API_BASE =
      //  (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
      //    ? "http://localhost:8082"
      //    : "https://stephanedinahet.fr";

      const ME_URL      = `${API_BASE}/api/auth/me`;
      const API_TICKETS = `${API_BASE}/api/tickets`;

      // ===== DOM
      const okBox  = document.getElementById("okBox");
      const okText = document.getElementById("okText");
      const errBox = document.getElementById("errBox");
      const errText= document.getElementById("errText");

      const ticketIdEl = document.getElementById("ticketId");
      const ticketDate = document.getElementById("ticketDate");
      const numberSelection = document.getElementById("numberSelection");
      const chanceSelection = document.getElementById("chanceSelection");
      const pickedCount = document.getElementById("pickedCount");
      const pickedChance = document.getElementById("pickedChance");
      const submitBtn = document.getElementById("submitBtn");
      const form = document.getElementById("ticketForm");

      const dirtyWrap = document.getElementById("dirtyWrap");
      const diffBox = document.getElementById("diffBox");
      const beforeText = document.getElementById("beforeText");
      const afterText = document.getElementById("afterText");

      // ===== State
      let selectedNumbers = new Set(); // 5 numbers
      let selectedChance = null;       // 1..10

      // Dirty/original
      let original = { drawDate: "", numbers: "", chanceNumber: null };
      let isDirty = false;
      let pendingPayload = null;


      function esc(s){
        return String(s).replace(/[&<>"']/g, c => ({
          "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"
        }[c]));
      }

      function normNums(str){
        return (str || "")
          .split("-")
          .map(x => parseInt(String(x).trim(), 10))
          .filter(n => !Number.isNaN(n))
          .sort((a,b)=>a-b);
      }

      // ✅ Génère les lignes "Avant/Après" avec couleurs
      function diffLine(before, after){
        // dates en FR (comme ton affichage)
        const bDateFR = esc(isoToFR(before.drawDate || ""));
        const aDateFR = esc(isoToFR(after.drawDate || ""));

        const bChance = String(before.chanceNumber ?? "");
        const aChance = String(after.chanceNumber ?? "");

        const bNums = normNums(before.numbers).map(String);
        const aNums = normNums(after.numbers).map(String);

        // date
        const dateHtmlBefore = (bDateFR === aDateFR)
          ? bDateFR
          : `<span class="diff-old diff-pill">${bDateFR}</span>`;

        const dateHtmlAfter = (bDateFR === aDateFR)
          ? aDateFR
          : `<span class="diff-new diff-pill">${aDateFR}</span>`;

        // numéros : on colore UNIQUEMENT ceux qui changent
        const bSet = new Set(bNums);
        const aSet = new Set(aNums);

        const numsHtmlBefore = bNums.map(n =>
          aSet.has(n) ? n : `<span class="diff-old diff-pill">${esc(n)}</span>`
        ).join("-");

        const numsHtmlAfter = aNums.map(n =>
          bSet.has(n) ? n : `<span class="diff-new diff-pill">${esc(n)}</span>`
        ).join("-");

        // chance
        const chanceHtmlBefore = (bChance === aChance)
          ? `Chance ${esc(bChance)}`
          : `Chance <span class="diff-old diff-pill">${esc(bChance)}</span>`;

        const chanceHtmlAfter = (bChance === aChance)
          ? `Chance ${esc(aChance)}`
          : `Chance <span class="diff-new diff-pill">${esc(aChance)}</span>`;

        return {
          before: `${dateHtmlBefore} &nbsp;|&nbsp; ${numsHtmlBefore} &nbsp;|&nbsp; ${chanceHtmlBefore}`,
          after:  `${dateHtmlAfter} &nbsp;|&nbsp; ${numsHtmlAfter} &nbsp;|&nbsp; ${chanceHtmlAfter}`,
        };
      }



      // ===== Date helpers (ISO input / FR display)
      function toISODate(value){
        if (!value) return "";
        const s = String(value).trim();

        if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s;
        if (/^\d{4}-\d{2}-\d{2}T/.test(s)) return s.slice(0, 10);

        const m = s.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
        if (m) return `${m[3]}-${m[2]}-${m[1]}`;

        const d = new Date(s);
        if (!Number.isNaN(d.getTime())) return d.toISOString().slice(0, 10);

        return "";
      }

      function isoToFR(iso){
        const s = toISODate(iso);
        if (!s) return "—";
        const [y, m, d] = s.split("-");
        return `${d}/${m}/${y}`;
      }

      // ===== UI helpers
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

      function setLoading(isLoading){
        submitBtn.disabled = isLoading || !isDirty;
        submitBtn.innerHTML = isLoading
          ? `<span class="spinner-border spinner-border-sm me-2"></span>Enregistrement…`
          : `<i class="fa-solid fa-floppy-disk me-2"></i>Mettre à jour`;
      }

      function updateHint(){
        pickedCount.textContent = String(selectedNumbers.size);
        pickedChance.textContent = selectedChance != null ? String(selectedChance) : "—";
      }

      function normalizeNumbersString(){
        return [...selectedNumbers].sort((a,b)=>a-b).join("-");
      }

      // IMPORTANT: affichage FR dans diff + modal
      function formatTicketText(obj){
        const dt = isoToFR(obj.drawDate);
        const nums = obj.numbers || "—";
        const ch = (obj.chanceNumber != null ? obj.chanceNumber : "—");
        return `${dt} | ${nums} | Chance ${ch}`;
      }

      function computeCurrent(){
        return {
          drawDate: ticketDate.value || "",
          numbers: normalizeNumbersString(),
          chanceNumber: selectedChance
        };
      }

      function updateDiffUI(){
        const cur = computeCurrent();

        //beforeText.textContent = formatTicketText(original);
        //afterText.textContent = formatTicketText(cur);

        const d = diffLine(original, cur);
          beforeText.innerHTML = d.before;
          afterText.innerHTML  = d.after;


        const changed =
          (cur.drawDate || "") !== (original.drawDate || "") ||
          (cur.numbers || "") !== (original.numbers || "") ||
          cur.chanceNumber !== original.chanceNumber;

        isDirty = changed;

        dirtyWrap.classList.toggle("d-none", !isDirty);
        diffBox.classList.toggle("d-none", !isDirty);
        submitBtn.disabled = !isDirty;
      }

      function markDirty(){
        updateDiffUI();
      }

      // ===== Auth
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

      // ===== Build pickers
      function buildNumberButtons(){
        numberSelection.innerHTML = "";
        for (let i = 1; i <= 49; i++){
          const el = document.createElement("span");
          el.className = "pick-ball";
          el.textContent = i;

          if (selectedNumbers.has(i)) el.classList.add("selected");

          el.addEventListener("click", () => {
            if (selectedNumbers.has(i)){
              selectedNumbers.delete(i);
              el.classList.remove("selected");
              updateHint();
              markDirty();
              return;
            }
            if (selectedNumbers.size >= 5){
              return;
            }
            selectedNumbers.add(i);
            el.classList.add("selected");
            updateHint();
            markDirty();
          });

          numberSelection.appendChild(el);
        }
      }

      function buildChanceButtons(){
        chanceSelection.innerHTML = "";
        for (let i = 1; i <= 10; i++){
          const el = document.createElement("span");
          el.className = "pick-ball chance";
          el.textContent = i;

          if (selectedChance === i) el.classList.add("selected");

          el.addEventListener("click", () => setChance(i));

          chanceSelection.appendChild(el);
        }
      }

      function setChance(i){
        selectedChance = i;

        chanceSelection.querySelectorAll(".pick-ball.chance")
          .forEach(x => x.classList.remove("selected"));

        const match = [...chanceSelection.querySelectorAll(".pick-ball.chance")]
          .find(x => Number(x.textContent) === i);

        if (match) match.classList.add("selected");
        updateHint();
        markDirty();
      }

      // ===== Load ticket
      async function loadTicketDetails(ticketId){
        try{
          const res = await axios.get(`${API_TICKETS}/${ticketId}`, { withCredentials: true });
          const ticket = res.data || {};

          // IMPORTANT: input date = ISO
          ticketDate.value = toISODate(ticket.drawDate);

          selectedNumbers = new Set(
            (ticket.numbers ? String(ticket.numbers).split("-") : [])
              .map(x => parseInt(x,10))
              .filter(n => !Number.isNaN(n))
          );

          selectedChance = ticket.chanceNumber != null ? parseInt(ticket.chanceNumber,10) : null;
          if (Number.isNaN(selectedChance)) selectedChance = null;

          buildNumberButtons();
          buildChanceButtons();
          updateHint();

          // capture état original (référence) en ISO
          original = {
            drawDate: ticketDate.value || "",
            numbers: ticket.numbers ? String(ticket.numbers) : "",
            chanceNumber: selectedChance
          };

          updateDiffUI();
        }catch(e){
          console.error(e);
          showErr("Impossible de charger ce ticket. Retour à la liste…");
          setTimeout(() => window.location.href = "tickets.html", 1200);
        }
      }

      // ===== Confirm modal (Bootstrap)
      function ensureConfirmModal(){
        if (document.getElementById("confirmModal")) return;

        const modalHtml = `
          <div class="modal fade" id="confirmModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
              <div class="modal-content" style="background: rgba(15,23,42,.95); border: 1px solid rgba(255,255,255,.12); border-radius: 16px;">
                <div class="modal-header" style="border-color: rgba(255,255,255,.10);">
                  <h5 class="modal-title fw-bold">Confirmer la mise à jour</h5>
                  <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Fermer"></button>
                </div>
                <div class="modal-body">
                  <div class="muted mb-2">Vérifie les changements avant de valider :</div>
                  <div class="diff-box">
                    <div class="diff-line">
                      <span class="diff-label">Avant</span>
                      <span class="mono" id="modalBefore">—</span>
                    </div>
                    <hr style="border-color: rgba(255,255,255,.10); margin: 10px 0;">
                    <div class="diff-line">
                      <span class="diff-label">Après</span>
                      <span class="mono" id="modalAfter">—</span>
                    </div>
                  </div>
                </div>
                <div class="modal-footer" style="border-color: rgba(255,255,255,.10);">
                  <button type="button" class="btn btn-soft" data-bs-dismiss="modal">Annuler</button>
                  <button type="button" class="btn btn-primary" id="confirmBtn">
                    <i class="fa-solid fa-floppy-disk me-2"></i>Valider
                  </button>
                </div>
              </div>
            </div>
          </div>
        `;
        document.body.insertAdjacentHTML("beforeend", modalHtml);
      }

      // ===== Init
      document.addEventListener("DOMContentLoaded", async () => {
        ensureConfirmModal();

        const auth = await ensureAuth();
        if (!auth) return;

        const params = new URLSearchParams(window.location.search);
        const id = params.get("id");

        if (!id){
          showErr("Aucun ticket sélectionné. Retour à la liste…");
          setTimeout(() => window.location.href = "tickets.html", 900);
          return;
        }

        ticketIdEl.value = id;

        // UI de base (avant chargement)
        buildNumberButtons();
        buildChanceButtons();
        updateHint();
        updateDiffUI();

        await loadTicketDetails(id);

        // changement date => dirty
        ticketDate.addEventListener("change", markDirty);
      });

      // ===== Submit (PUT) -> confirmation modal
      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        errBox.classList.add("d-none");
        okBox.classList.add("d-none");

        const ticketId = ticketIdEl.value;
        const dateVal = ticketDate.value;

        if (!dateVal){
          showErr("Veuillez choisir une date de tirage.");
          return;
        }
        if (selectedNumbers.size !== 5 || selectedChance == null){
          showErr("Vous devez choisir 5 numéros et 1 numéro chance.");
          return;
        }

        const payload = {
          drawDate: dateVal,                 // ISO
          numbers: normalizeNumbersString(),
          chanceNumber: selectedChance
        };

        pendingPayload = { ticketId, payload };

        // modal
        const modalEl = document.getElementById("confirmModal");
        const modalBefore = document.getElementById("modalBefore");
        const modalAfter = document.getElementById("modalAfter");
        const confirmBtn = document.getElementById("confirmBtn");

        // IMPORTANT: affichage FR via formatTicketText
        //modalBefore.textContent = formatTicketText(original);
        //modalAfter.textContent = formatTicketText(payload);

        const d = diffLine(original, payload);
          modalBefore.innerHTML = d.before;
          modalAfter.innerHTML  = d.after;


        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();

        // éviter multi-bind : reset puis bind
        confirmBtn.onclick = async () => {
          if (!pendingPayload) return;

          try{
            setLoading(true);

            //await axios.put(`${API_TICKETS}/${pendingPayload.ticketId}`, pendingPayload.payload, {
            //  withCredentials: true,
            //  headers: {
            //    "Content-Type": "application/json",
            //    "Accept": "application/json"
            //  }
            //});

            await ensureCsrf(API_BASE);
            const xsrf = getCookie("XSRF-TOKEN");

            await axios.put(`${API_TICKETS}/${pendingPayload.ticketId}`, pendingPayload.payload, {
              withCredentials: true,
              headers: {
                "Content-Type": "application/json",
                "Accept": "application/json",
                ...(xsrf ? { "X-XSRF-TOKEN": xsrf } : {})
              }
            });

            // ✅ redirection avec burst
            //window.location.href = `tickets.html?refresh=1&ts=${Date.now()}`;
            window.location.href = `tickets.html?refresh=1&id=${encodeURIComponent(ticketId)}&ts=${Date.now()}`;

            return;

            localStorage.setItem("tickets.refresh", "1");
            window.location.href = `tickets.html?ts=${Date.now()}`;



            modal.hide();




            // reset original => plus dirty
            original = { ...pendingPayload.payload };
            pendingPayload = null;
            updateDiffUI();

            //showOk("Ticket mis à jour avec succès ✔ Redirection…");
            //    showOk("Ticket mis à jour avec succès    Redirection…");
            //setTimeout(() => window.location.href = "tickets.html", 1000);
            //setTimeout(() => window.location.href = "tickets.html?burst=1", 1000);
            //window.location.href = `tickets.html?burst=1&ts=${Date.now()}`;
            showOk("Ticket mis à jour avec succès — Redirection…");
            setTimeout(() => {
              //window.location.href = `tickets.html?burst=1&ts=${Date.now()}`;
              window.location.href = `tickets.html?refresh=1&ts=${Date.now()}`;
            }, 800);


          }catch(err){
            console.error(err);
            const msg =
              err?.response?.data?.message ||
              err?.response?.data?.error ||
              err?.message ||
              "Erreur lors de la mise à jour du ticket.";
            showErr(msg);
          }finally{
            setLoading(false);
          }
        };
      });
    })();
