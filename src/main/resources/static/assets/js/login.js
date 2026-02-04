    const API_BASE =
      (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
        ? "http://localhost:8082"
        : "https://stephanedinahet.fr";

    function getCookie(name) {
      const value = `; ${document.cookie}`;
      const parts = value.split(`; ${name}=`);
      if (parts.length === 2) return parts.pop().split(";").shift();
      return null;
    }

    function checkAuth() {
      const tokenCookie = getCookie("jwtToken");
      const tokenLS = localStorage.getItem("jwtToken");
      /* if (tokenCookie || tokenLS) window.location.href = "index_connexion.html"; */
      if (tokenCookie || tokenLS) window.location.href = "index.html";
    }
    document.addEventListener("DOMContentLoaded", checkAuth);

    // Email UI: icons during typing, border only on submit
    const emailInput = document.getElementById("email");
    const emailField = document.getElementById("emailField");
    const emailHint = document.getElementById("emailHint");
    const emailOkIcon = document.getElementById("emailOkIcon");
    const emailBadIcon = document.getElementById("emailBadIcon");
    let hasSubmitted = false;

    function isValidEmail(email) {
      return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(email).trim());
    }

    emailInput.addEventListener("input", () => {
      const v = emailInput.value.trim();
      emailHint.classList.remove("show");

      if (!hasSubmitted) {
        emailField.classList.remove("valid", "invalid");
      }

      if (!v) {
        emailOkIcon.classList.add("hidden");
        emailBadIcon.classList.add("hidden");
        emailOkIcon.classList.remove("show");
        emailBadIcon.classList.remove("show");
        return;
      }

      if (isValidEmail(v)) {
        emailBadIcon.classList.add("hidden");
        emailBadIcon.classList.remove("show");
        emailOkIcon.classList.remove("hidden");
        emailOkIcon.classList.add("show");
      } else {
        emailOkIcon.classList.add("hidden");
        emailOkIcon.classList.remove("show");
        emailBadIcon.classList.remove("hidden");
        emailBadIcon.classList.add("show");
      }
    });

    function validateEmailOnSubmit() {
      const v = emailInput.value.trim();
      if (!v) {
        emailField.classList.add("invalid");
        emailField.classList.remove("valid");
        emailHint.textContent = "Email requis.";
        emailHint.classList.add("show");
        return false;
      }
      if (isValidEmail(v)) {
        emailField.classList.add("valid");
        emailField.classList.remove("invalid");
        emailHint.classList.remove("show");
        return true;
      }
      emailField.classList.add("invalid");
      emailField.classList.remove("valid");
      emailHint.textContent = "Email invalide (ex: nom@domaine.fr)";
      emailHint.classList.add("show");
      return false;
    }

    // Toggle password
    const passInput = document.getElementById("password");
    const togglePassword = document.getElementById("togglePassword");
    const eyeOpen = document.getElementById("eyeOpen");
    const eyeClosed = document.getElementById("eyeClosed");

    togglePassword.addEventListener("click", () => {
      const hidden = passInput.type === "password";
      passInput.type = hidden ? "text" : "password";
      eyeOpen.classList.toggle("hidden", !hidden);
      eyeClosed.classList.toggle("hidden", hidden);
      passInput.focus();
    });

    // Message no alert
    const loginMsg = document.getElementById("loginMsg");
    const submitBtn = document.getElementById("submitBtn");

    function showMsg(type, text){
      loginMsg.classList.remove("ok","bad","show");
      loginMsg.classList.add("show", type);
      loginMsg.textContent = text;
    }
    function clearMsg(){
      loginMsg.classList.remove("ok","bad","show");
      loginMsg.textContent = "";
    }

    // Loto balls transition
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

    // Submit login
    document.getElementById("loginForm").addEventListener("submit", async (event) => {
      event.preventDefault();
      clearMsg();

      hasSubmitted = true;

      const okEmail = validateEmailOnSubmit();
      const email = emailInput.value.trim();
      const password = passInput.value;

      if (!okEmail) return showMsg("bad", "❌ Email invalide.");
      if (!password || password.length < 4) return showMsg("bad", "❌ Mot de passe invalide.");

      submitBtn.disabled = true;
      submitBtn.textContent = "Connexion...";

      try {
        const response = await fetch(`${API_BASE}/api/auth/login3`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password }),
          credentials: "include"
        });

        if (!response.ok) throw new Error();

        showMsg("ok", "✅ Login OK ! Redirection...");
        startLotoBallsTransition();

        setTimeout(() => {
          /* window.location.href = "index_connexion.html"; */
          window.location.href = "index.html";
        }, 1600);

      } catch (e) {
        showMsg("bad", "❌ Login NOK : identifiants incorrects ou serveur indisponible.");
      } finally {
        setTimeout(() => {
          submitBtn.disabled = false;
          submitBtn.textContent = "Se connecter";
        }, 800);
      }
    });
