    const API_BASE =
      (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
        ? "http://localhost:8082"
        : "https://stephanedinahet.fr";

    const REGISTER_URL = `${API_BASE}/api/auth/register`;
    const LOGIN_URL    = `${API_BASE}/api/auth/login3`;

    const form = document.getElementById("registerForm");
    const btn  = document.getElementById("submitBtn");
    const fb   = document.getElementById("feedback");

    const firstNameField = document.getElementById("firstNameField");
    const lastNameField  = document.getElementById("lastNameField");
    const emailField     = document.getElementById("emailField");
    const passwordField  = document.getElementById("passwordField");
    const confirmField   = document.getElementById("confirmField");

    const firstNameInput = document.getElementById("firstName");
    const lastNameInput  = document.getElementById("lastName");
    const emailInput     = document.getElementById("email");
    const passInput      = document.getElementById("password");
    const confirmInput   = document.getElementById("confirmPassword");

    const emailOkIcon    = document.getElementById("emailOkIcon");
    const emailBadIcon   = document.getElementById("emailBadIcon");
    const confirmOkIcon  = document.getElementById("confirmOkIcon");
    const confirmBadIcon = document.getElementById("confirmBadIcon");

    const pwBar  = document.getElementById("pwBar");
    const pwText = document.getElementById("pwText");

    let hasSubmitted = false;

    function showMsg(type, text){
      fb.className = "msg show " + (type === "ok" ? "ok" : "bad");
      fb.textContent = text;
    }
    function clearMsg(){
      fb.className = "msg";
      fb.textContent = "";
    }
    function setFieldState(fieldEl, ok){
      fieldEl.classList.toggle("valid", !!ok);
      fieldEl.classList.toggle("invalid", !ok);
    }
    function isValidEmail(v){
      return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(v).trim());
    }
    function showStatus(okEl, badEl, state){
      [okEl,badEl].forEach(el => { el.classList.add("hidden"); el.classList.remove("show"); });
      if (state === "ok"){ okEl.classList.remove("hidden"); okEl.classList.add("show"); }
      if (state === "bad"){ badEl.classList.remove("hidden"); badEl.classList.add("show"); }
    }

    emailInput.addEventListener("input", () => {
      const v = emailInput.value.trim();
      if (!hasSubmitted) emailField.classList.remove("valid","invalid");
      if (!v) return showStatus(emailOkIcon, emailBadIcon, "none");
      return showStatus(emailOkIcon, emailBadIcon, isValidEmail(v) ? "ok" : "bad");
    });

    function updateConfirmLive(){
      const pw = passInput.value;
      const c  = confirmInput.value;
      if (!c) return showStatus(confirmOkIcon, confirmBadIcon, "none");
      if (!pw) return showStatus(confirmOkIcon, confirmBadIcon, "bad");
      return showStatus(confirmOkIcon, confirmBadIcon, (c === pw) ? "ok" : "bad");
    }
    passInput.addEventListener("input", updateConfirmLive);
    confirmInput.addEventListener("input", updateConfirmLive);

    function bindEye(btnId, inputId, openId, closedId){
      const b = document.getElementById(btnId);
      const i = document.getElementById(inputId);
      const o = document.getElementById(openId);
      const c = document.getElementById(closedId);
      b.addEventListener("click", () => {
        const hidden = i.type === "password";
        i.type = hidden ? "text" : "password";
        o.classList.toggle("hidden", !hidden);
        c.classList.toggle("hidden", hidden);
        i.focus();
      });
    }
    bindEye("togglePassword", "password", "eyeOpen", "eyeClosed");
    bindEye("toggleConfirmPassword", "confirmPassword", "eyeOpenConfirm", "eyeClosedConfirm");

    function scorePassword(pw){
      let score = 0;
      if (!pw) return 0;
      if (pw.length >= 8) score += 1;
      if (pw.length >= 12) score += 1;
      if (/[a-z]/.test(pw)) score += 1;
      if (/[A-Z]/.test(pw)) score += 1;
      if (/\d/.test(pw)) score += 1;
      if (/[^a-zA-Z0-9]/.test(pw)) score += 1;
      return Math.min(score, 6);
    }
    function updateStrength(){
      const pw = passInput.value;
      const s = scorePassword(pw);
      const pct = Math.round((s / 6) * 100);

      pwBar.style.width = pw ? (pct + "%") : "0%";
      if (!pw){ pwText.textContent = "—"; pwBar.style.background = "var(--red)"; return; }

      if (s <= 1){ pwBar.style.background = "var(--red)"; pwText.textContent = "Faible"; }
      else if (s <= 3){ pwBar.style.background = "var(--warn)"; pwText.textContent = "Moyen"; }
      else { pwBar.style.background = "var(--green)"; pwText.textContent = "Fort"; }
    }
    passInput.addEventListener("input", updateStrength);
    updateStrength();

    const ballsLayer = document.getElementById("ballsLayer");
    function rand(min, max){ return Math.random() * (max - min) + min; }
    function startLotoBallsTransition(){
      ballsLayer.innerHTML = "";
      ballsLayer.classList.add("show");

      const count = 24;
      for(let i=0;i<count;i++){
        const ball = document.createElement("div");
        ball.className = "ball";
        ball.style.setProperty("--left", `${rand(0, 100)}vw`);
        ball.style.setProperty("--size", `${rand(38, 62)}px`);
        ball.style.setProperty("--dur", `${rand(1.6, 2.6)}s`);
        ball.style.setProperty("--spin", `${rand(0.7, 1.4)}s`);
        ball.style.setProperty("--delay", `${rand(0, 0.7)}s`);
        ball.textContent = String(Math.floor(rand(1, 50)));
        ballsLayer.appendChild(ball);
      }

      setTimeout(() => {
        ballsLayer.classList.remove("show");
        ballsLayer.innerHTML = "";
      }, 3200);
    }

    async function autoLogin(email, password){
      const resp = await fetch(LOGIN_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
        credentials: "include"
      });
      return resp.ok;
    }

    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      clearMsg();
      hasSubmitted = true;

      [firstNameField,lastNameField,emailField,passwordField,confirmField]
        .forEach(f => f.classList.remove("valid","invalid"));

      const firstName = firstNameInput.value.trim();
      const lastName  = lastNameInput.value.trim();
      const email     = emailInput.value.trim();
      const password  = passInput.value;
      const confirm   = confirmInput.value;

      let ok = true;

      if (!firstName){ setFieldState(firstNameField,false); ok=false; } else setFieldState(firstNameField,true);
      if (!lastName){  setFieldState(lastNameField,false);  ok=false; } else setFieldState(lastNameField,true);

      if (!email || !isValidEmail(email)){ setFieldState(emailField,false); ok=false; } else setFieldState(emailField,true);

      const pwScore = scorePassword(password);
      if (!password || password.length < 6 || pwScore < 2){ setFieldState(passwordField,false); ok=false; }
      else { setFieldState(passwordField,true); }

      if (!confirm || confirm !== password){ setFieldState(confirmField,false); ok=false; }
      else { setFieldState(confirmField,true); }

      if (!ok){
        showMsg("bad","❌ Vérifie les champs en rouge (email + mot de passe + confirmation).");
        return;
      }

      btn.disabled = true;
      btn.textContent = "Création...";

      try{
        const registerResp = await fetch(REGISTER_URL, {
          method: "POST",
          headers: { "Content-Type":"application/json", "Accept":"application/json" },
          body: JSON.stringify({ firstName, lastName, email, password, admin:false })
        });

        const text = await registerResp.text().catch(() => "");
        if (!registerResp.ok){
          if (registerResp.status === 409) showMsg("bad","❌ Un compte existe déjà avec cet email.");
          else showMsg("bad","❌ Inscription NOK : " + (text || `HTTP ${registerResp.status}`));
          return;
        }

        showMsg("ok","✅ Compte créé ! Connexion automatique...");
        const logged = await autoLogin(email, password);

        if (!logged){
          showMsg("bad","⚠️ Compte créé, mais auto-login impossible. Redirection vers connexion...");
          setTimeout(() => window.location.href = "login.html", 900);
          return;
        }

        showMsg("ok","✅ Connecté ! Redirection...");
        startLotoBallsTransition();
        setTimeout(() => window.location.href = "index.html", 1400);

      }catch(err){
        console.error(err);
        showMsg("bad","❌ Impossible de contacter le serveur.");
      }finally{
        setTimeout(() => {
          btn.disabled = false;
          btn.textContent = "Créer un compte";
        }, 800);
      }
    });
