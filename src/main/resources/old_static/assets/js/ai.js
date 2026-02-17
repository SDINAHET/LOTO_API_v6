  // (() => {
  //   // ✅ API_BASE : si la page est en file:// => localhost. Sinon, même host que la page (pratique en réseau).
  //   const guessBase = () => {
  //     try {
  //       if (location.protocol === "file:") return "http://127.0.0.1:8091";
  //       return location.origin.replace(/:\d+$/, "") + ":8091";
  //     } catch {
  //       return "http://127.0.0.1:8091";
  //     }
  //   };
  //   const API_BASE = localStorage.getItem("LT_API_BASE") || guessBase();
  //   const AI_SERVICE_URL = API_BASE + "/ai/chat";
  //   const HEALTH_URL = API_BASE + "/health";


    (() => {
    const HOST = location.hostname;

    const PROD_DOMAINS = ["stephanedinahet.fr", "loto-tracker.fr"];

    const IS_PROD = PROD_DOMAINS.some(d =>
      HOST === d || HOST === `www.${d}` || HOST.endsWith(`.${d}`)
    );

    const guessBase = () => {
      try {
        // file:// -> backend local
        if (location.protocol === "file:") return "http://127.0.0.1:8091";

        // prod -> reverse proxy sur le même domaine (pas de port)
        if (IS_PROD) return location.origin;

        // local / LAN -> même host + port 8091
        return `${location.protocol}//${HOST}:8091`;
      } catch {
        return "http://127.0.0.1:8091";
      }
    };

    const API_BASE = localStorage.getItem("LT_API_BASE") || guessBase();

    const AI_SERVICE_URL = `${API_BASE}/ai/chat`;
    const HEALTH_URL = `${API_BASE}/health`;

    console.log("HOST:", HOST);
    console.log("IS_PROD:", IS_PROD);
    console.log("API_BASE:", API_BASE);
  // })();


    const launcher = document.getElementById("lt-chat-launcher");
    const panel = document.getElementById("lt-chat-panel");
    const closeBtn = document.getElementById("lt-chat-close");
    const maxBtn = document.getElementById("lt-chat-max");

    const tabBtns = document.querySelectorAll(".lt-tab-btn");
    const home = document.getElementById("lt-home");
    const messagesTab = document.getElementById("lt-messages-tab");

    const actions = document.querySelectorAll(".lt-action");
    const messagesEl = document.getElementById("lt-messages");
    const input = document.getElementById("lt-input");
    const sendBtn = document.getElementById("lt-send");
    const statusEl = document.getElementById("lt-status");

    const latestEl = document.getElementById("lt-latest-draw");
    const presetsEl = document.getElementById("lt-home-presets");

    let latestDraw = null;

    let currentTicket = {
      nums: [1, 2, 3, 4, 5],
      chance: 1,
      // date: null
      date: new Date().toLocaleDateString().slice(0, 10) // "DD-MM-YYYY"
    };

    // Stop bubbling click
    panel.addEventListener("click", (e) => e.stopPropagation());

    launcher.addEventListener("click", (e) => {
      e.stopPropagation();
      panel.classList.toggle("lt-hidden");

      launcher.classList.toggle("lt-open", !panel.classList.contains("lt-hidden"));
      launcher.classList.remove("lt-invite-show");

      if (!panel.classList.contains("lt-hidden")) {
        setTab("messages");
        setTimeout(() => input.focus(), 50);
      }
    });

    closeBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      panel.classList.add("lt-hidden");
    });

    maxBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      panel.classList.toggle("lt-max");
    });

    document.addEventListener("click", () => panel.classList.add("lt-hidden"));

    function launchFireworks() {
      const duration = 5000;
      const end = Date.now() + duration;

      (function frame() {
        confetti({ particleCount: 8, angle: 60, spread: 70, origin: { x: 0 } });
        confetti({ particleCount: 8, angle: 120, spread: 70, origin: { x: 1 } });

        if (Date.now() < end) requestAnimationFrame(frame);
      })();
    }


    function setTab(tab){
      tabBtns.forEach(b => b.classList.toggle("lt-active", b.dataset.tab === tab));
      home.classList.toggle("lt-show", tab === "home");
      messagesTab.classList.toggle("lt-show", tab === "messages");
      if (tab === "messages") setTimeout(() => input.focus(), 50);
    }

    function bubbleText(text, who){
      const div = document.createElement("div");
      div.className = "lt-bubble " + (who === "user" ? "lt-user" : "lt-bot");
      div.textContent = text;
      messagesEl.appendChild(div);
      messagesEl.scrollTop = messagesEl.scrollHeight;
      return div;
    }

    function bubbleCard(title, buildBodyFn){
      const div = document.createElement("div");
      div.className = "lt-bubble lt-bot";
      const card = document.createElement("div");
      card.className = "lt-card";

      const h = document.createElement("h4");
      h.textContent = title;
      card.appendChild(h);

      buildBodyFn(card);

      div.appendChild(card);
      messagesEl.appendChild(div);
      messagesEl.scrollTop = messagesEl.scrollHeight;
      return div;
    }

    function renderBalls(nums, chance){
      const wrap = document.createElement("div");
      wrap.className = "balls";

      (nums || []).forEach((n) => {
        const b = document.createElement("div");
        b.className = "ball blue";
        b.textContent = String(n);
        wrap.appendChild(b);
      });

      const plus = document.createElement("div");
      plus.className = "lt-mini";
      plus.textContent = "+";
      plus.style.margin = "0 6px";
      wrap.appendChild(plus);

      const c = document.createElement("div");
      c.className = "ball red";
      c.textContent = String(chance);
      wrap.appendChild(c);

      return wrap;
    }

    // Inline editable ball (no prompt)
    function mkInlineEditableBall(color, value, min, max, onSet){
      const b = document.createElement("button");
      b.type = "button";
      b.className = "ball " + color + " editable";

      const span = document.createElement("span");
      span.textContent = String(value);
      b.appendChild(span);

      function toEdit(){
        const inp = document.createElement("input");
        inp.type = "number";
        inp.min = String(min);
        inp.max = String(max);
        inp.value = String(value);
        b.innerHTML = "";
        b.appendChild(inp);
        inp.focus();
        inp.select();

        const commit = () => {
          const n = parseInt(inp.value, 10);
          if (!Number.isFinite(n) || n < min || n > max) {
            b.innerHTML = "";
            b.appendChild(span);
            return;
          }
          onSet(n);
        };

        inp.addEventListener("keydown", (e) => {
          if (e.key === "Enter") commit();
          if (e.key === "Escape") {
            b.innerHTML = "";
            b.appendChild(span);
          }
        });
        inp.addEventListener("blur", commit);
      }

      b.addEventListener("click", (e) => {
        e.preventDefault();
        toEdit();
      });

      return b;
    }

    function normalizeTicket(nums, chance){
      const five = (nums || []).map(x => parseInt(x, 10)).filter(Number.isFinite);

      if (five.length !== 5) return "Il faut exactement 5 numéros.";
      if (five.some(n => n < 1 || n > 49)) return "Numéros entre 1 et 49.";
      if (new Set(five).size !== 5) return "Numéros en double interdits (5 numéros tous différents).";
      if (!Number.isFinite(chance) || chance < 1 || chance > 10) return "Chance entre 1 et 10.";
      return null;
    }


    function buildTicketQuestion(t){
      const nums = (t.nums || []).join(" ");
      const datePart = t.date ? (" le " + t.date) : "";
      return "Je joue " + nums + " + " + t.chance + datePart + ", est-ce gagnant ?";
    }

    function renderTicketWidget(ticket){
      let draft = JSON.parse(JSON.stringify(ticket));
      if (draft.includeSecond == null) draft.includeSecond = false;

      bubbleCard("🎟️ Ticket (modifiable)", (card) => {
        const top = document.createElement("div");
        top.className = "lt-mini";
        top.textContent = "Modifie avec ta combinaison les numéros ci-dessous pour savoir si tu as gagné !";
        card.appendChild(top);

        const warn = document.createElement("div");
        warn.className = "lt-mini";
        warn.style.marginTop = "6px";
        warn.style.color = "rgba(255,180,180,.95)";
        card.appendChild(warn);

        const ballsZone = document.createElement("div");
        card.appendChild(ballsZone);

        const rebuildBalls = () => {
          ballsZone.innerHTML = "";
          warn.textContent = "";

          const wrap = document.createElement("div");
          wrap.className = "balls";

          const used = new Set(draft.nums);

          draft.nums.forEach((n, idx) => {
            const b = mkInlineEditableBall("blue", n, 1, 49, (newVal) => {
              // Anti doublon direct
              const others = draft.nums.filter((_, i) => i !== idx);
              if (others.includes(newVal)) {
                warn.textContent = "⚠️ Doublon interdit : choisis un numéro différent.";
                return;
              }
              draft.nums[idx] = newVal;
              rebuildBalls();
            });
            wrap.appendChild(b);
          });

          const plus = document.createElement("div");
          plus.className = "lt-mini";
          plus.textContent = "+";
          plus.style.margin = "0 6px";
          wrap.appendChild(plus);

          const cBall = mkInlineEditableBall("red", draft.chance, 1, 10, (newVal) => {
            draft.chance = newVal;
            rebuildBalls();
          });
          wrap.appendChild(cBall);

          ballsZone.appendChild(wrap);

          // Validation live
          const err = normalizeTicket(draft.nums, draft.chance);
          if (err) warn.textContent = "⚠️ " + err;
        };

        rebuildBalls();

        const form = document.createElement("div");
        form.className = "lt-form";

        const dInput = document.createElement("input");
        dInput.placeholder = "JJ/MM/AAAA";
        dInput.value = draft.date || "";
        dInput.addEventListener("input", () => draft.date = dInput.value.trim() || null);
        form.appendChild(dInput);

        // ✅ Checkbox second tirage
        const chkWrap = document.createElement("label");
        chkWrap.style.display = "flex";
        chkWrap.style.alignItems = "center";
        chkWrap.style.gap = "10px";
        chkWrap.style.marginTop = "8px";
        chkWrap.style.color = "var(--muted)";
        chkWrap.style.fontSize = "13px";

        const chk = document.createElement("input");
        chk.type = "checkbox";
        chk.checked = !!draft.includeSecond;
        chk.addEventListener("change", () => draft.includeSecond = chk.checked);

        const chkText = document.createElement("span");
        chkText.textContent = "option second tirage";

        chkWrap.appendChild(chk);
        chkWrap.appendChild(chkText);

        card.appendChild(form);
        card.appendChild(chkWrap);

        const row = document.createElement("div");
        row.className = "lt-row";

        const btnApply = document.createElement("button");
        btnApply.className = "lt-btn";
        btnApply.textContent = "Confirmer";
        btnApply.onclick = () => {
          const err = normalizeTicket(draft.nums, draft.chance);
          if (err) return alert(err);

          currentTicket = {
            nums: [...draft.nums],
            chance: draft.chance,
            date: draft.date || null,
            includeSecond: !!draft.includeSecond
          };

          bubbleText("✅ Ticket confirmé.", "bot");
          renderTicketWidget(currentTicket); // republie nouvelle card
        };

        const btnUse = document.createElement("button");
        btnUse.className = "lt-btn primary";
        btnUse.textContent = "Utiliser ce ticket";
        btnUse.onclick = () => {
          const base = buildTicketQuestion(draft);
          const extra = draft.includeSecond ? " (inclure second tirage)" : "";
          input.value = base + extra;
          setTab("messages");
          setTimeout(() => input.focus(), 50);
        };

        row.appendChild(btnApply);
        row.appendChild(btnUse);
        card.appendChild(row);
      });
    }


    function renderCombinations(combos){
      bubbleCard("🎲 Combinaisons proposées", (card) => {
        const mini = document.createElement("div");
        mini.className = "lt-mini";
        mini.textContent = "Clique “Utiliser” pour coller le ticket dans la question.";
        card.appendChild(mini);

        (combos || []).forEach((c, idx) => {
          const box = document.createElement("div");
          box.className = "lt-card";
          box.style.marginTop = "10px";

          const lab = document.createElement("div");
          lab.className = "lt-mini";
          lab.textContent = "Proposition #" + (idx + 1);
          box.appendChild(lab);

          box.appendChild(renderBalls(c.nums, c.chance));

          const row = document.createElement("div");
          row.className = "lt-row";

          const btn = document.createElement("button");
          btn.className = "lt-btn primary";
          btn.textContent = "Utiliser";
          btn.onclick = () => {
            currentTicket = { nums: c.nums, chance: c.chance, date: currentTicket.date || null };
            input.value = buildTicketQuestion(currentTicket);
            setTab("messages");
            setTimeout(() => input.focus(), 50);
          };

          row.appendChild(btn);
          box.appendChild(row);
          card.appendChild(box);
        });
      });
    }

    function renderStats(stats){
      bubbleCard("📊 Stats (fréquences)", (card) => {
        const n = stats?.top_numbers || [];
        const ch = stats?.top_chances || [];

        const p1 = document.createElement("div");
        p1.className = "lt-mini";
        p1.textContent = "Top numéros : " + n.map(x => (x.n + "(" + x.count + ")")).join("  ");
        card.appendChild(p1);

        const p2 = document.createElement("div");
        p2.className = "lt-mini";
        p2.style.marginTop = "8px";
        p2.textContent = "Top Chance : " + ch.map(x => (x.n + "(" + x.count + ")")).join("  ");
        card.appendChild(p2);

        const row = document.createElement("div");
        row.className = "lt-row";

        const btn = document.createElement("button");
        btn.className = "lt-btn primary";
        btn.textContent = "Proposer 5 combinaisons";
        btn.onclick = () => send("Propose 5 combinaisons probables");

        row.appendChild(btn);
        card.appendChild(row);
      });
    }

    async function callAiService(message){
      const res = await fetch(AI_SERVICE_URL, {
        method: "POST",
        headers: {"Content-Type":"application/json"},
        body: JSON.stringify({ message, locale: "fr" }),
      });

      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(res.status + " " + t);
      }
      return res.json();
    }

    async function send(text){
      const msg = (text ?? input.value).trim();
      if(!msg) return;

      setTab("messages");
      bubbleText(msg, "user");
      input.value = "";

      // const loader = bubbleText("…", "bot");
      const loader = bubbleLoader();

      function bubbleLoader(){
        const div = document.createElement("div");
        div.className = "lt-bubble lt-bot";

        const rail = document.createElement("div");
        rail.className = "ai-rail";

        for (let i = 0; i < 6; i++){
          const dot = document.createElement("span");
          dot.className = "ai-dot";
          rail.appendChild(dot);
        }

        div.appendChild(rail);
        messagesEl.appendChild(div);
        messagesEl.scrollTop = messagesEl.scrollHeight;
        return div;
      }


      //function bubbleLoader(){
        //const div = document.createElement("div");
        //div.className = "lt-bubble lt-bot";

        //const loader = document.createElement("div");
        //loader.className = "ai-loader";

        //for (let i = 0; i < 4; i++){
          //const dot = document.createElement("span");
          //loader.appendChild(dot);
        //}

        //div.appendChild(loader);
        //messagesEl.appendChild(div);
        //messagesEl.scrollTop = messagesEl.scrollHeight;
        //return div; // <= on renvoie la bulle complète
//      }


      try{
        const data = await callAiService(msg);
        loader.textContent = data.answer || "Réponse vide.";

        // 🎆 Déclenche feux d’artifice si ticket gagnant (fallback depuis data.answer)
        let totalGains = Number(data.totalGains ?? data.total_gains ?? NaN);

        if (Number.isNaN(totalGains)) {
          const answer = String(data.answer || "");

          // Cherche: "Total gains ... : 2.20 €" ou "2,20 €" (avec espaces possibles)
          const m = answer.match(/Total\s+gains[\s\S]*?:\s*([0-9][0-9\s.,]*)\s*€/i);

          if (m) {
            totalGains = Number(m[1].replace(/\s/g, "").replace(",", "."));
          }
        }

        console.log("totalGains =", totalGains);

        if (!Number.isNaN(totalGains) && totalGains > 0) {
          setTimeout(launchFireworks, 400);
        }


        if (data.ticketSuggestion?.nums && data.ticketSuggestion?.chance) {
          currentTicket = {
            nums: data.ticketSuggestion.nums,
            chance: data.ticketSuggestion.chance,
            date: data.ticketSuggestion.date || currentTicket.date || null
          };
          renderTicketWidget(currentTicket);
        }

        if (Array.isArray(data.combinations) && data.combinations.length) {
          renderCombinations(data.combinations);
        }

        if (data.stats) {
          renderStats(data.stats);
        }

      }catch(e){
        loader.textContent =
          "❌ Impossible de contacter ai-service.\n" +
          "Vérifie :\n" +
          "1) uvicorn tourne sur 8091\n" +
          "2) API_BASE = " + API_BASE + "\n" +
          "3) CORS backend autorise l’origine du front\n" +
          "Détail: " + (e?.message || e);
      }
    }

    // Home presets based on latest draw
    async function fetchLatestDraw(){
      const r = await fetch(API_BASE + "/ai/latest-draw");
      if(!r.ok) throw new Error("latest-draw failed");
      const data = await r.json();
      return data.draw;
    }

    function buildLatestPresets(draw){
      const d = draw.date_fr;
      const main = draw.nums.join(" ") + " + " + draw.chance;
      const second = draw.second_tirage?.nums ? draw.second_tirage.nums.join(" ") : null;

      const base = [
        //"Donne le tirage du " + d + " (MongoDB)",
        //"Résume le tirage du " + d + " (principal + codes + rangs)",
        //"Affiche la combinaison gagnante du " + d,
        //"Donne tous les rangs (gagnants + rapports) du " + d,
        //"Combien de gagnants rang 1 le " + d + " ?",
        //"Quel est le rapport rang 2 le " + d + " ?",
        //"Quel est le total des gagnants (tous rangs) le " + d + " ?",
        //"Donne les codes gagnants du " + d,
        //"Combien de codes gagnants le " + d + " ?",
        //"Donne le Joker+ du " + d,
      ];

      const ticketOps = [
        "Je joue " + main + " le " + d + ", est-ce gagnant ?",
        "Calcule mon rang et mon gain pour " + main + " au " + d,
        "Je joue 1 2 3 4 5 + 1 le " + d + ", est-ce gagnant ?",
        "Compare mon ticket 1 2 3 4 5 + 1 au tirage du " + d,
        "Je joue 1 2 3 4 5 + 1 le " + d + ", quel rang ?",
      ];

      const secondOps = [
        "Second tirage du " + d + " : donne les numéros et les gagnants",
        "Je veux inclure le second tirage pour " + d,
        second ? ("Analyse le second tirage du " + d + " : " + second) : ("Le " + d + ", y a-t-il un second tirage en base ?"),
        "Je joue 1 2 3 4 5 + 1 le " + d + " (inclure second tirage), quel résultat ?",
      ];

      const statsOps = [
        "Stats fréquences MongoDB : top 10 numéros",
        "Donnes moi 3 combinaisons",
        "Donnes moi 10 combinaisons",
        //"Donnes moi 3 combinaisons probables avec fréquence et hasard",
        //"Donnes moi 10 combinaisons probables avec fréquence et hasard",
        //"Donnes moi 3 combinaisons probables avec numéros chaud et froid",
        //"Donnes moi 10 combinaisons probables avec numéros chaud et froid",
        //"Stats fréquences MongoDB : top 10 chances",
        "Numéros chauds sur 50 derniers tirages",
        "Numéros froids sur 50 derniers tirages",
        //"Donne une analyse PMU-like : chaud/froid + stratégie",
        //"Analyse PMU-like : gestion du risque + bankroll",
        //"Compare 2 stratégies : aléatoire vs fréquences",
        "Quels numéros sortent le plus le mercredi ?",
        "Quels numéros sortent le plus le samedi ?",
        "Quelle numéro chance sort le plus souvent ?",
      ];

      const combosOps = [
        //"Propose 5 combinaisons probables (MongoDB)",
        //"Propose 10 combinaisons probables (MongoDB)",
        //"Propose 5 combinaisons en évitant les numéros du dernier tirage",
        //"Propose 5 combinaisons avec 2 numéros chauds et 2 froids",
        //"Donne 3 combinaisons 'safe' et 2 'agressives'",
      ];

      const rulesOps = [
        "Comment on joue au Loto ?",
        "Explique le rôle du numéro Chance",
        //"Explique les probabilités rang 1 vs rang 3",
        //"Pourquoi aucune combinaison n’est plus probable ?",
        "Comment fonctionne le second tirage ?",
        "Quelles erreurs éviter en jouant au Loto ?",
        //"Donne une explication courte type 'PMU' pour débutant",
      ];

      return [...base, ...ticketOps, ...secondOps, ...statsOps, ...combosOps, ...rulesOps];
    }

    function renderHomePresets(draw){
      latestDraw = draw;
      latestEl.textContent = draw.date_fr + " (" + (draw.jour || "") + ") — " + draw.nums.join(" ") + " + Chance " + draw.chance;

      presetsEl.innerHTML = "";
      const presets = buildLatestPresets(draw);

      presets.forEach((q) => {
        const btn = document.createElement("button");
        btn.className = "lt-action";
        btn.textContent = q;
        btn.addEventListener("click", () => send(q));
        presetsEl.appendChild(btn);
      });
    }

    tabBtns.forEach(b => b.addEventListener("click", () => setTab(b.dataset.tab)));
    actions.forEach(a => a.addEventListener("click", () => send(a.dataset.preset)));

    sendBtn.addEventListener("click", () => send());
    input.addEventListener("keydown", (e) => { if(e.key === "Enter") send(); });

    // welcome
    bubbleText("Salut 👋 Clique sur Home pour les questions “dernier tirage”, ou écris une question.", "bot");
    renderTicketWidget(currentTicket);

    // Health check
    (async () => {
      try{
        const r = await fetch(HEALTH_URL);
        if(!r.ok) throw new Error();
        statusEl.innerHTML = "✅ Statut ai-service : <b>OK</b> — " + API_BASE;
      //}catch{
      //  statusEl.innerHTML = "❌ Statut ai-service : <b>OFF</b><br/>Lance <code>uvicorn ai:app --host 0.0.0.0 --port 8091</code>";
      //}
      }catch(err){
        console.error("HEALTH CHECK ERROR:", err);
        statusEl.innerHTML =
          "❌ Statut ai-service : <b>OFF</b><br/>" +
          "Front=" + location.href + "<br/>" +
          "Origin=" + location.origin + "<br/>" +
          "API_BASE=" + API_BASE + "<br/>" +
          "Détail: " + (err?.message || err);
      }

    })();

    // Latest draw presets
    (async () => {
      try{
        const d = await fetchLatestDraw();
        renderHomePresets(d);
      }catch{
        latestEl.textContent = "Impossible de charger le dernier tirage.";
      }
    })();
  })();

  // Invite auto (1 fois)
  const launcher2 = document.getElementById("lt-chat-launcher");

  function showInviteOnce(){
    if (localStorage.getItem("LT_INVITE_SEEN") === "1") return;

    launcher2.classList.add("lt-invite-show");
    setTimeout(() => launcher2.classList.remove("lt-invite-show"), 4200);
    localStorage.setItem("LT_INVITE_SEEN", "1");
  }

  setTimeout(showInviteOnce, 900);
