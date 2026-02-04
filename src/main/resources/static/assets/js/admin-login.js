// 🔧 BASE URL dynamique : même origin que la page
// const API_BASE = window.location.origin;

const API_BASE =
  (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
    ? "http://localhost:8082"
    : window.location.origin;

const form = document.getElementById("adminLoginForm");
const errorMsg = document.getElementById("errorMsg");
const submitBtn = document.getElementById("submitBtn");

// CAPTCHA
const captchaCanvas = document.getElementById("captchaCanvas");
const captchaCtx = captchaCanvas.getContext("2d");
const captchaInput = document.getElementById("captchaAnswer");
const refreshCaptchaBtn = document.getElementById("refreshCaptcha");

let captchaCode = "";
const CAPTCHA_LENGTH = 7;

function randomCaptchaChar() {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  return chars.charAt(Math.floor(Math.random() * chars.length));
}

function randomTextColor() {
  const colors = ["#f97316", "#22c55e", "#38bdf8", "#a855f7", "#eab308", "#f97373", "#34d399"];
  return colors[Math.floor(Math.random() * colors.length)];
}

// ✅ Validation email live
const emailInput = document.getElementById("email");
const emailStatus = document.getElementById("emailStatus");

const EMAIL_REGEX = new RegExp(
  "^(?=.{6,254}$)(?=.{1,64}@)[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
);

const ICON_OK = `
<svg width="18" height="18" viewBox="0 0 24 24" fill="none"
stroke="#22c55e" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"
style="filter:drop-shadow(0 0 8px rgba(34,197,94,1));">
  <path d="M20 6 9 17l-5-5"></path>
</svg>`;

const ICON_KO = `
<svg width="18" height="18" viewBox="0 0 24 24" fill="none"
stroke="#ef4444" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"
style="filter:drop-shadow(0 0 8px rgba(239,68,68,1));">
  <path d="M18 6 6 18"></path>
  <path d="M6 6 18 18"></path>
</svg>`;

function setEmailNeutral() {
  emailInput.style.border = "1px solid #4b5563";
  emailInput.style.boxShadow = "none";
  emailStatus.style.opacity = "0";
  emailStatus.innerHTML = "";
}

function setEmailValid() {
  emailInput.style.border = "1px solid #22c55e";
  emailInput.style.boxShadow = "0 0 0 2px rgba(34,197,94,0.18), 0 0 18px rgba(34,197,94,0.18)";
  emailStatus.style.opacity = "1";
  emailStatus.innerHTML = ICON_OK;
}

function setEmailInvalid() {
  emailInput.style.border = "1px solid #ef4444";
  emailInput.style.boxShadow = "0 0 0 2px rgba(239,68,68,0.18), 0 0 18px rgba(239,68,68,0.18)";
  emailStatus.style.opacity = "1";
  emailStatus.innerHTML = ICON_KO;
}

function validateEmailLive() {
  const v = emailInput.value.trim();
  if (!v) return setEmailNeutral();
  if (EMAIL_REGEX.test(v)) setEmailValid();
  else setEmailInvalid();
}

emailInput.addEventListener("input", validateEmailLive);
emailInput.addEventListener("blur", validateEmailLive);
validateEmailLive();

// 👁️ Toggle password
const togglePasswordBtn = document.getElementById("togglePassword");
const passwordInput = document.getElementById("password");
const eyeSlash = document.getElementById("eyeSlash");
const eyeIcon = document.getElementById("eyeIcon");

function syncEyeState() {
  const hidden = passwordInput.type === "password";
  eyeSlash.style.opacity = hidden ? "1" : "0";
  eyeIcon.style.filter = hidden
    ? "drop-shadow(0 0 6px rgba(34,197,94,0.7))"
    : "drop-shadow(0 0 10px rgba(34,197,94,1))";
}

togglePasswordBtn.addEventListener("click", () => {
  passwordInput.type = passwordInput.type === "password" ? "text" : "password";
  syncEyeState();
});
syncEyeState();

// CAPTCHA generator
function generateCaptcha() {
  captchaCode = "";

  captchaCtx.setTransform(1, 0, 0, 1, 0, 0);
  captchaCtx.clearRect(0, 0, captchaCanvas.width, captchaCanvas.height);

  // Fond
  const gradient = captchaCtx.createLinearGradient(0, 0, captchaCanvas.width, captchaCanvas.height);
  gradient.addColorStop(0, "#020617");
  gradient.addColorStop(1, "#020617");
  captchaCtx.fillStyle = gradient;
  captchaCtx.fillRect(0, 0, captchaCanvas.width, captchaCanvas.height);

  // Bruit
  for (let i = 0; i < 180; i++) {
    captchaCtx.fillStyle = `rgba(148,163,184,${Math.random() * 0.35})`;
    captchaCtx.beginPath();
    captchaCtx.arc(
      Math.random() * captchaCanvas.width,
      Math.random() * captchaCanvas.height,
      0.8 + Math.random() * 1.7,
      0,
      Math.PI * 2
    );
    captchaCtx.fill();
  }

  // Rectangles parasites
  for (let i = 0; i < 60; i++) {
    captchaCtx.fillStyle = `rgba(31,41,55,${0.3 + Math.random() * 0.5})`;
    const w = 4 + Math.random() * 12;
    const h = 2 + Math.random() * 6;
    captchaCtx.save();
    captchaCtx.translate(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
    captchaCtx.rotate((Math.random() - 0.5) * 1.0);
    captchaCtx.fillRect(-w / 2, -h / 2, w, h);
    captchaCtx.restore();
  }

  // Lignes droites
  for (let i = 0; i < 25; i++) {
    captchaCtx.strokeStyle = `rgba(148,163,184,${0.3 + Math.random() * 0.5})`;
    captchaCtx.lineWidth = 0.7 + Math.random() * 1.3;
    captchaCtx.beginPath();
    captchaCtx.moveTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
    captchaCtx.lineTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
    captchaCtx.stroke();
  }

  // Courbes
  for (let i = 0; i < 12; i++) {
    captchaCtx.strokeStyle = `rgba(56,189,248,${0.15 + Math.random() * 0.4})`;
    captchaCtx.lineWidth = 0.8;
    captchaCtx.beginPath();
    const startX = Math.random() * captchaCanvas.width;
    const startY = Math.random() * captchaCanvas.height;
    const cp1X = Math.random() * captchaCanvas.width;
    const cp1Y = Math.random() * captchaCanvas.height;
    const cp2X = Math.random() * captchaCanvas.width;
    const cp2Y = Math.random() * captchaCanvas.height;
    const endX = Math.random() * captchaCanvas.width;
    const endY = Math.random() * captchaCanvas.height;
    captchaCtx.moveTo(startX, startY);
    captchaCtx.bezierCurveTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY);
    captchaCtx.stroke();
  }

  // Caractères
  for (let i = 0; i < CAPTCHA_LENGTH; i++) {
    const char = randomCaptchaChar();
    captchaCode += char;

    const spacing = captchaCanvas.width / (CAPTCHA_LENGTH + 1);
    const baseX = spacing * (i + 1);
    const baseY = captchaCanvas.height / 2 + (Math.random() * 12 - 6);

    const angle = (Math.random() - 0.5) * 1.2;
    const scaleX = 0.6 + Math.random() * 1.3;
    const scaleY = 0.6 + Math.random() * 1.3;
    const shearX = (Math.random() - 0.5) * 0.7;
    const shearY = (Math.random() - 0.5) * 0.4;
    const fontSize = 26 + Math.floor(Math.random() * 14);

    captchaCtx.save();
    captchaCtx.translate(baseX, baseY);

    const cos = Math.cos(angle);
    const sin = Math.sin(angle);

    const a = cos * scaleX + shearY * sin * scaleX;
    const b = sin * scaleX;
    const c = shearX * cos * scaleY + -sin * scaleY;
    const d = shearX * sin * scaleY + cos * scaleY;

    captchaCtx.transform(a, b, c, d, 0, 0);

    captchaCtx.font = "bold " + fontSize + "px Arial";
    captchaCtx.fillStyle = randomTextColor();
    captchaCtx.shadowColor = "rgba(0,0,0,0.8)";
    captchaCtx.shadowBlur = 3;

    captchaCtx.fillText(char, -8, 8);
    captchaCtx.restore();
  }

  captchaInput.value = "";
}

refreshCaptchaBtn.addEventListener("click", () => {
  generateCaptcha();
  errorMsg.style.display = "none";
});

generateCaptcha();

// Submit
form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();
  const captchaValue = captchaInput.value.trim().toUpperCase();

  errorMsg.style.display = "none";

  if (!captchaValue) {
    errorMsg.textContent = "Veuillez recopier le code de sécurité.";
    errorMsg.style.display = "block";
    return;
  }

  if (captchaValue !== captchaCode) {
    errorMsg.textContent = "Captcha invalide. Veuillez réessayer.";
    errorMsg.style.display = "block";
    generateCaptcha();
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/api/auth/login-swagger`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ email, password })
    });

    if (res.status === 403) {
      let data = {};
      try { data = await res.json(); } catch (e2) {}

      errorMsg.textContent = data.message || "Accès bloqué après 3 tentatives. Contactez l'administrateur.";
      errorMsg.style.display = "block";

      submitBtn.disabled = true;
      submitBtn.style.opacity = "0.5";
      submitBtn.style.cursor = "not-allowed";

      document.getElementById("email").disabled = true;
      document.getElementById("password").disabled = true;
      captchaInput.disabled = true;

      refreshCaptchaBtn.disabled = true;
      refreshCaptchaBtn.style.opacity = "0.5";
      refreshCaptchaBtn.style.cursor = "not-allowed";
      return;
    }

    if (!res.ok) {
      let data = {};
      try { data = await res.json(); } catch (e2) {}

      if (data.tentativesRestantes !== undefined) {
        errorMsg.textContent = (data.message || "Identifiants invalides.")
          + " Tentatives restantes : " + data.tentativesRestantes;
      } else {
        errorMsg.textContent = data.message || "Identifiants invalides ou droits insuffisants.";
      }

      errorMsg.style.display = "block";
      generateCaptcha();
      return;
    }

    // ✅ Login OK
    window.location.href = "/admin/dashboard.html";

  } catch (err) {
    console.error(err);
    errorMsg.textContent = "Erreur réseau, réessayez plus tard.";
    errorMsg.style.display = "block";
  }
});
