    (function(){

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

      const API_BASE =
        window.__API_BASE_ACTIVE__ ||
        ((location.hostname === "localhost" || location.hostname === "127.0.0.1")
          ? `http://${location.hostname}:8082`
          : "https://stephanedinahet.fr");

      const ME_URL      = `${API_BASE}/api/auth/me`;
      const API_TICKETS = `${API_BASE}/api/tickets`;

      const okBox  = document.getElementById("okBox");
      const okText = document.getElementById("okText");
      const errBox = document.getElementById("errBox");
      const errText= document.getElementById("errText");

      const drawDate = document.getElementById("drawDate");
      const numberSelection = document.getElementById("numberSelection");
      const chanceSelection = document.getElementById("chanceSelection");
      const pickedCount = document.getElementById("pickedCount");
      const pickedChance = document.getElementById("pickedChance");
      const submitBtn = document.getElementById("submitBtn");
      const form = document.getElementById("ticketForm");

      let selectedNumbers = new Set();
      let selectedChance = null;
      let userId = null;

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
        submitBtn.disabled = isLoading;
        submitBtn.innerHTML = isLoading
          ? `<span class="spinner-border spinner-border-sm me-2"></span>Enregistrement…`
          : `<i class="fa-solid fa-plus me-2"></i>Enregistrer`;
      }

      function updateHint(){
        pickedCount.textContent = String(selectedNumbers.size);
        pickedChance.textContent = selectedChance != null ? String(selectedChance) : "—";
      }

      async function ensureAuth(){
        try{
          const res = await axios.get(ME_URL, { withCredentials: true });
          userId = res?.data?.id || null;
          if (!userId) throw new Error("Missing user id");
          return true;
        }catch(e){
          showErr("Session expirée. Redirection vers la connexion…");
          setTimeout(() => window.location.href = "login.html", 900);
          return false;
        }
      }

      function updateDisabledNumbers(){
        const maxReached = selectedNumbers.size >= 5;

        numberSelection.querySelectorAll(".pick-ball").forEach(el => {
          const n = Number(el.textContent);
          const isSelected = selectedNumbers.has(n);

          if (maxReached && !isSelected) el.classList.add("disabled");
          else el.classList.remove("disabled");
        });
      }

      function buildNumberButtons(){
        numberSelection.innerHTML = "";
        for (let i = 1; i <= 49; i++){
          const el = document.createElement("span");
          el.className = "pick-ball";
          el.textContent = i;

          if (selectedNumbers.has(i)) el.classList.add("selected");

          el.addEventListener("click", () => {
            if (el.classList.contains("disabled")) return;

            if (selectedNumbers.has(i)){
              selectedNumbers.delete(i);
              el.classList.remove("selected");
              updateHint();
              updateDisabledNumbers();
              return;
            }

            if (selectedNumbers.size >= 5){
              return;
            }

            selectedNumbers.add(i);
            el.classList.add("selected");
            updateHint();
            updateDisabledNumbers();
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

          el.addEventListener("click", () => {
            setChance(i);
          });

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
      }

      function resetSelections(){
        selectedNumbers = new Set();
        selectedChance = null;
        buildNumberButtons();
        buildChanceButtons();
        updateHint();
        updateDisabledNumbers();
      }

      // ✅ extraction ID ticket créé (supporte plusieurs formats backend)
      function extractCreatedTicketId(postResponse){
        try{
          const data = postResponse?.data;

          const candidates = [
            data?.id,
            data?.ticketId,
            data?.ticket?.id,
            data?.data?.id,
            data?.data?.ticketId
          ];

          for (const c of candidates){
            if (c !== null && c !== undefined && String(c).trim() !== "") return String(c);
          }

          // header Location: /api/tickets/{id}
          const loc = postResponse?.headers?.location || postResponse?.headers?.Location;
          if (loc && typeof loc === "string"){
            const m = loc.match(/\/(\d+)\s*$/);
            if (m && m[1]) return String(m[1]);
          }

          return null;
        } catch {
          return null;
        }
      }

      document.addEventListener("DOMContentLoaded", async () => {
        const auth = await ensureAuth();
        if (!auth) return;

        buildNumberButtons();
        buildChanceButtons();
        updateHint();
        updateDisabledNumbers();
      });

      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        errBox.classList.add("d-none");
        okBox.classList.add("d-none");

        const dateVal = drawDate.value;

        if (!dateVal){
          showErr("Veuillez choisir une date de tirage.");
          return;
        }
        if (selectedNumbers.size !== 5 || selectedChance == null){
          showErr("Vous devez choisir 5 numéros et 1 numéro chance.");
          return;
        }

        const numbersSorted = [...selectedNumbers].sort((a,b) => a-b);

        const payload = {
          userId,
          drawDate: dateVal,
          numbers: numbersSorted.join("-"),
          chanceNumber: selectedChance
        };

        try{
          setLoading(true);

          await ensureCsrf(API_BASE);
          const xsrf = getCookie("XSRF-TOKEN");

          const resp = await axios.post(API_TICKETS, payload, {
            withCredentials: true,
            headers: {
              "Content-Type": "application/json",
              "Accept": "application/json",
              ...(xsrf ? { "X-XSRF-TOKEN": xsrf } : {})
            }
          });

          const createdId = extractCreatedTicketId(resp);

          showOk("Ticket ajouté avec succès — Redirection…");

          setTimeout(() => {
            const ts = Date.now();
            if (createdId){
              window.location.href = `tickets.html?burst=1&id=${encodeURIComponent(createdId)}&ts=${ts}`;
            } else {
              // fallback : comportement actuel (sans id)
              window.location.href = `tickets.html?burst=1&ts=${ts}`;
            }
          }, 800);

          resetSelections();
        }catch(err){
          console.error(err);
          const msg =
            err?.response?.data?.message ||
            err?.response?.data?.error ||
            err?.message ||
            "Erreur lors de l'ajout du ticket.";
          showErr(msg);
        }finally{
          setLoading(false);
        }
      });
    })();
