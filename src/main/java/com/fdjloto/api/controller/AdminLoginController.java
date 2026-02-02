// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class AdminLoginController {

//     @GetMapping(value = "/admin-login.html", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminLoginPage() {

//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//     <meta charset="UTF-8">
//     <title>Login admin - Swagger</title>
//     <meta name="viewport" content="width=device-width, initial-scale=1.0">
// </head>
// <body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0">
// <div style="background:#0f172a;border-radius:16px;padding:24px 28px;max-width:380px;width:100%;box-shadow:0 20px 60px rgba(15,23,42,0.9)">
//     <h1 style="margin-top:0;margin-bottom:12px;font-size:1.4rem;">Connexion admin</h1>
//     <p style="margin:0 0 18px;font-size:0.9rem;color:#9ca3af;">
//         Accès réservé à l'administrateur pour consulter la documentation Swagger.
//     </p>

//     <form id="adminLoginForm">
//         <div style="margin-bottom:10px;">
//             <label for="email" style="display:block;font-size:0.85rem;margin-bottom:4px;">Email admin</label>
//             <input id="email" type="email" required
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <div style="margin-bottom:14px;">
//             <label for="password" style="display:block;font-size:0.85rem;margin-bottom:4px;">Mot de passe</label>
//             <input id="password" type="password" required
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <button id="submitBtn" type="submit"
//                 style="width:100%;padding:9px 12px;border-radius:999px;border:none;background:#22c55e;color:#020617;font-weight:600;cursor:pointer;">
//             Se connecter
//         </button>
//     </form>

//     <p id="errorMsg" style="margin-top:10px;font-size:0.85rem;color:#f97373;display:none;"></p>

//     <p style="margin-top:14px;font-size:0.8rem;color:#6b7280;">
//         Accès bloqué après 1 tentative.
//     </p>
//     <p style="margin-top:4px;font-size:0.8rem;color:#6b7280;">
//         Après connexion réussie, vous serez redirigé automatiquement vers Swagger UI.
//     </p>
// </div>

// <script>
//     // 🔧 BASE URL dynamique : local vs prod
//     const API_BASE =
//         window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
//             ? "http://localhost:8082"
//             : "https://stephanedinahet.fr";

//     const form = document.getElementById("adminLoginForm");
//     const errorMsg = document.getElementById("errorMsg");
//     const submitBtn = document.getElementById("submitBtn");

//     form.addEventListener("submit", async function (e) {
//         e.preventDefault();
//         const email = document.getElementById("email").value.trim();
//         const password = document.getElementById("password").value.trim();
//         errorMsg.style.display = "none";

//         try {
//             const res = await fetch(`${API_BASE}/api/auth/login-swagger`, {
//                 method: "POST",
//                 headers: { "Content-Type": "application/json" },
//                 credentials: "include",
//                 body: JSON.stringify({ email, password })
//             });

//             // 🔒 Cas 403 : trop de tentatives → on bloque l'UI
//             if (res.status === 403) {
//                 let data = {};
//                 try { data = await res.json(); } catch(e) {}
//                 errorMsg.textContent = data.message || "Accès bloqué après 1 tentative. Contactez l'administrateur.";
//                 errorMsg.style.display = "block";

//                 // Désactivation du formulaire
//                 submitBtn.disabled = true;
//                 submitBtn.style.opacity = "0.5";
//                 submitBtn.style.cursor = "not-allowed";
//                 document.getElementById("email").disabled = true;
//                 document.getElementById("password").disabled = true;

//                 // 👉 si tu préfères rediriger vers ta page 403 :
//                 // window.location.href = API_BASE + "/403";
//                 return;
//             }

//             // ❌ Autres erreurs (401…) → on affiche le message + tentatives restantes
//             if (!res.ok) {
//                 let data = {};
//                 try { data = await res.json(); } catch(e) {}
//                 if (data.tentativesRestantes !== undefined) {
//                     errorMsg.textContent =
//                         (data.message || "Identifiants invalides.") +
//                         " Tentatives restantes : " + data.tentativesRestantes;
//                 } else {
//                     errorMsg.textContent = data.message || "Identifiants invalides ou droits insuffisants.";
//                 }
//                 errorMsg.style.display = "block";
//                 return;
//             }

//             // ✅ Login OK → on renvoie vers Swagger UI
//             window.location.href = API_BASE + "/swagger-ui/index.html";

//         } catch (err) {
//             console.error(err);
//             errorMsg.textContent = "Erreur réseau, réessayez plus tard.";
//             errorMsg.style.display = "block";
//         }
//     });
// </script>
// </body>
// </html>
// """;
//     }
// }

// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class AdminLoginController {

//     @GetMapping(value = "/admin-login.html", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminLoginPage() {

//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//     <meta charset="UTF-8">
//     <title>Login admin - Swagger</title>
//     <meta name="viewport" content="width=device-width, initial-scale=1.0">
// </head>
// <body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0">
// <div style="background:#0f172a;border-radius:16px;padding:24px 28px;max-width:380px;width:100%;box-shadow:0 20px 60px rgba(15,23,42,0.9)">
//     <h1 style="margin-top:0;margin-bottom:12px;font-size:1.4rem;">Connexion admin</h1>
//     <p style="margin:0 0 18px;font-size:0.9rem;color:#9ca3af;">
//         Accès réservé à l'administrateur pour consulter la documentation Swagger.
//     </p>

//     <form id="adminLoginForm">
//         <div style="margin-bottom:10px;">
//             <label for="email" style="display:block;font-size:0.85rem;margin-bottom:4px;">Email admin</label>
//             <input id="email" type="email" required
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <div style="margin-bottom:14px;">
//             <label for="password" style="display:block;font-size:0.85rem;margin-bottom:4px;">Mot de passe</label>
//             <input id="password" type="password" required
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <!-- 🔐 CAPTCHA avancé -->
//         <div style="margin-bottom:14px;">
//             <label style="display:block;font-size:0.85rem;margin-bottom:6px;">
//                 Vérification anti-robot
//             </label>

//             <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px;">
//                 <canvas id="captchaCanvas" width="140" height="45"
//                         style="border-radius:8px;border:1px solid #4b5563;background:#020617;cursor:not-allowed;"></canvas>

//                 <button type="button" id="refreshCaptcha"
//                         style="padding:6px 10px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;">
//                     ↻
//                 </button>
//             </div>

//             <input id="captchaAnswer" type="text" required
//                    placeholder="Recopiez le code"
//                    autocomplete="off" spellcheck="false"
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <button id="submitBtn" type="submit"
//                 style="width:100%;padding:9px 12px;border-radius:999px;border:none;background:#22c55e;color:#020617;font-weight:600;cursor:pointer;">
//             Se connecter
//         </button>
//     </form>

//     <p id="errorMsg" style="margin-top:10px;font-size:0.85rem;color:#f97373;display:none;"></p>

//     <p style="margin-top:14px;font-size:0.8rem;color:#6b7280;">
//         Accès bloqué après 3 tentative.
//     </p>
//     <p style="margin-top:4px;font-size:0.8rem;color:#6b7280;">
//         Après connexion réussie, vous serez redirigé automatiquement vers Swagger UI.
//     </p>
// </div>

// <script>
//     // 🔧 BASE URL dynamique : local vs prod
//     const API_BASE =
//         window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
//             ? "http://localhost:8082"
//             : "https://stephanedinahet.fr";

//     const form = document.getElementById("adminLoginForm");
//     const errorMsg = document.getElementById("errorMsg");
//     const submitBtn = document.getElementById("submitBtn");

//     // 🔐 CAPTCHA avancé (canvas + code alphanumérique)
//     const captchaCanvas = document.getElementById("captchaCanvas");
//     const captchaCtx = captchaCanvas.getContext("2d");
//     const captchaInput = document.getElementById("captchaAnswer");
//     const refreshCaptchaBtn = document.getElementById("refreshCaptcha");

//     let captchaCode = "";

//     function randomCaptchaChar() {
//         const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // pas de 0,1,O,I
//         return chars.charAt(Math.floor(Math.random() * chars.length));
//     }

//     function generateCaptcha() {
//         captchaCode = "";
//         captchaCtx.clearRect(0, 0, captchaCanvas.width, captchaCanvas.height);
//         captchaCtx.fillStyle = "#020617";
//         captchaCtx.fillRect(0, 0, captchaCanvas.width, captchaCanvas.height);

//         // Bruit de fond (points)
//         for (let i = 0; i < 40; i++) {
//             captchaCtx.fillStyle = `rgba(255,255,255,${Math.random() * 0.2})`;
//             captchaCtx.beginPath();
//             captchaCtx.arc(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height, 1.2, 0, Math.PI * 2);
//             captchaCtx.fill();
//         }

//         // Lignes parasites
//         for (let i = 0; i < 5; i++) {
//             captchaCtx.strokeStyle = `rgba(148,163,184,${0.3 + Math.random() * 0.3})`;
//             captchaCtx.beginPath();
//             captchaCtx.moveTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
//             captchaCtx.lineTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
//             captchaCtx.stroke();
//         }

//         // Caractères déformés
//         for (let i = 0; i < 6; i++) {
//             const char = randomCaptchaChar();
//             captchaCode += char;

//             const x = 15 + i * 20;
//             const y = 30 + (Math.random() * 6 - 3); // léger décalage vertical
//             const angle = (Math.random() - 0.5) * 0.6; // rotation

//             captchaCtx.save();
//             captchaCtx.translate(x, y);
//             captchaCtx.rotate(angle);
//             captchaCtx.font = "bold 22px Arial";
//             captchaCtx.fillStyle = "#e5e7eb";
//             captchaCtx.fillText(char, 0, 0);
//             captchaCtx.restore();
//         }

//         captchaInput.value = "";
//     }

//     refreshCaptchaBtn.addEventListener("click", function () {
//         generateCaptcha();
//         errorMsg.style.display = "none";
//     });

//     // Génère un premier captcha au chargement
//     generateCaptcha();

//     form.addEventListener("submit", async function (e) {
//         e.preventDefault();
//         const email = document.getElementById("email").value.trim();
//         const password = document.getElementById("password").value.trim();
//         const captchaValue = captchaInput.value.trim().toUpperCase();
//         errorMsg.style.display = "none";

//         // 🛑 Vérification CAPTCHA avant l'appel API
//         if (!captchaValue) {
//             errorMsg.textContent = "Veuillez recopier le code de sécurité.";
//             errorMsg.style.display = "block";
//             return;
//         }

//         if (captchaValue !== captchaCode) {
//             errorMsg.textContent = "Captcha invalide. Veuillez réessayer.";
//             errorMsg.style.display = "block";
//             generateCaptcha();
//             return;
//         }

//         try {
//             const res = await fetch(`${API_BASE}/api/auth/login-swagger`, {
//                 method: "POST",
//                 headers: { "Content-Type": "application/json" },
//                 credentials: "include",
//                 body: JSON.stringify({ email, password })
//             });

//             // 🔒 Cas 403 : trop de tentatives → on bloque l'UI
//             if (res.status === 403) {
//                 let data = {};
//                 try { data = await res.json(); } catch(e) {}
//                 errorMsg.textContent = data.message || "Accès bloqué après 3 tentative. Contactez l'administrateur.";
//                 errorMsg.style.display = "block";

//                 // Désactivation du formulaire + captcha
//                 submitBtn.disabled = true;
//                 submitBtn.style.opacity = "0.5";
//                 submitBtn.style.cursor = "not-allowed";
//                 document.getElementById("email").disabled = true;
//                 document.getElementById("password").disabled = true;
//                 captchaInput.disabled = true;
//                 refreshCaptchaBtn.disabled = true;
//                 refreshCaptchaBtn.style.opacity = "0.5";
//                 refreshCaptchaBtn.style.cursor = "not-allowed";

//                 // 👉 si tu préfères rediriger vers ta page 403 :
//                 // window.location.href = API_BASE + "/errors/403.html";
//                 return;
//             }

//             // ❌ Autres erreurs (401…) → on affiche le message + tentatives restantes
//             if (!res.ok) {
//                 let data = {};
//                 try { data = await res.json(); } catch(e) {}
//                 if (data.tentativesRestantes !== undefined) {
//                     errorMsg.textContent =
//                         (data.message || "Identifiants invalides.") +
//                         " Tentatives restantes : " + data.tentativesRestantes;
//                 } else {
//                     errorMsg.textContent = data.message || "Identifiants invalides ou droits insuffisants.";
//                 }
//                 errorMsg.style.display = "block";
//                 generateCaptcha();
//                 return;
//             }

//             // ✅ Login OK → on renvoie vers Swagger UI
//             window.location.href = API_BASE + "/swagger-ui/index.html";

//         } catch (err) {
//             console.error(err);
//             errorMsg.textContent = "Erreur réseau, réessayez plus tard.";
//             errorMsg.style.display = "block";
//         }
//     });
// </script>
// </body>
// </html>
// """;
//     }
// }

// package com.fdjloto.api.controller;

// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class AdminLoginController {

//     @GetMapping(value = "/admin-login.html", produces = MediaType.TEXT_HTML_VALUE)
//     public String adminLoginPage() {

//         return """
// <!DOCTYPE html>
// <html lang="fr">
// <head>
//     <meta charset="UTF-8">
//     <title>Login admin - Swagger</title>
//     <meta name="viewport" content="width=device-width, initial-scale=1.0">
// </head>
// <body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0">
// <div style="background:#0f172a;border-radius:16px;padding:24px 28px;max-width:380px;width:100%;box-shadow:0 20px 60px rgba(15,23,42,0.9)">
//     <h1 style="margin-top:0;margin-bottom:12px;font-size:1.4rem;">Connexion admin</h1>
//     <p style="margin:0 0 18px;font-size:0.9rem;color:#9ca3af;">
//         Accès réservé à l'administrateur pour consulter la documentation Swagger.
//     </p>

//     <form id="adminLoginForm">
//         <div style="margin-bottom:10px;">
//             <label for="email" style="display:block;font-size:0.85rem;margin-bottom:4px;">Email admin</label>
//             <input id="email" type="email" required
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <div style="margin-bottom:14px;">
//             <label for="password" style="display:block;font-size:0.85rem;margin-bottom:4px;">Mot de passe</label>
//             <input id="password" type="password" required
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <!-- 🔐 CAPTCHA avancé -->
//         <div style="margin-bottom:14px;">
//             <label style="display:block;font-size:0.85rem;margin-bottom:6px;">
//                 Vérification anti-robot
//             </label>

//             <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px;">
//                 <canvas id="captchaCanvas" width="220" height="70"
//                         style="border-radius:8px;border:1px solid #4b5563;background:#020617;cursor:not-allowed;"></canvas>

//                 <button type="button" id="refreshCaptcha"
//                         style="padding:6px 10px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;">
//                     ↻
//                 </button>
//             </div>

//             <input id="captchaAnswer" type="text" required
//                    placeholder="Recopiez le code"
//                    autocomplete="off" spellcheck="false"
//                    style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
//         </div>

//         <button id="submitBtn" type="submit"
//                 style="width:100%;padding:9px 12px;border-radius:999px;border:none;background:#22c55e;color:#020617;font-weight:600;cursor:pointer;">
//             Se connecter
//         </button>
//     </form>

//     <p id="errorMsg" style="margin-top:10px;font-size:0.85rem;color:#f97373;display:none;"></p>

//     <p style="margin-top:14px;font-size:0.8rem;color:#6b7280;">
//         Accès bloqué après 3 tentative.
//     </p>
//     <p style="margin-top:4px;font-size:0.8rem;color:#6b7280;">
//         Après connexion réussie, vous serez redirigé automatiquement vers Swagger UI.
//     </p>
// </div>

// <script>
//     // 🔧 BASE URL dynamique : local vs prod
//     const API_BASE =
//         (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
//             ? "http://localhost:8082"
//             : "https://stephanedinahet.fr";

//     const form = document.getElementById("adminLoginForm");
//     const errorMsg = document.getElementById("errorMsg");
//     const submitBtn = document.getElementById("submitBtn");

//     // 🔐 CAPTCHA très difficile (canvas + code alphanumérique lourdement déformé)
//     const captchaCanvas = document.getElementById("captchaCanvas");
//     const captchaCtx = captchaCanvas.getContext("2d");
//     const captchaInput = document.getElementById("captchaAnswer");
//     const refreshCaptchaBtn = document.getElementById("refreshCaptcha");

//     let captchaCode = "";
//     const CAPTCHA_LENGTH = 7; // nombre de caractères

//     function randomCaptchaChar() {
//         const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // pas de 0,1,O,I
//         return chars.charAt(Math.floor(Math.random() * chars.length));
//     }

//     function randomTextColor() {
//         const colors = ["#f97316", "#22c55e", "#38bdf8", "#a855f7", "#eab308", "#f97373", "#34d399"];
//         return colors[Math.floor(Math.random() * colors.length)];
//     }

//     function generateCaptcha() {
//         captchaCode = "";

//         // Reset transform au cas où
//         captchaCtx.setTransform(1, 0, 0, 1, 0, 0);
//         captchaCtx.clearRect(0, 0, captchaCanvas.width, captchaCanvas.height);

//         // Fond dégradé
//         const gradient = captchaCtx.createLinearGradient(0, 0, captchaCanvas.width, captchaCanvas.height);
//         gradient.addColorStop(0, "#020617");
//         gradient.addColorStop(0.5, "#020617");
//         gradient.addColorStop(1, "#020617");
//         captchaCtx.fillStyle = gradient;
//         captchaCtx.fillRect(0, 0, captchaCanvas.width, captchaCanvas.height);

//         // Bruit de fond (points)
//         for (let i = 0; i < 120; i++) {
//             captchaCtx.fillStyle = `rgba(148,163,184,${Math.random() * 0.35})`;
//             captchaCtx.beginPath();
//             captchaCtx.arc(
//                 Math.random() * captchaCanvas.width,
//                 Math.random() * captchaCanvas.height,
//                 0.8 + Math.random() * 1.7,
//                 0,
//                 Math.PI * 2
//             );
//             captchaCtx.fill();
//         }

//         // Petits rectangles parasites
//         for (let i = 0; i < 40; i++) {
//             captchaCtx.fillStyle = `rgba(31,41,55,${0.3 + Math.random() * 0.5})`;
//             const w = 5 + Math.random() * 10;
//             const h = 2 + Math.random() * 6;
//             captchaCtx.fillRect(
//                 Math.random() * captchaCanvas.width,
//                 Math.random() * captchaCanvas.height,
//                 w,
//                 h
//             );
//         }

//         // Lignes droites parasites
//         for (let i = 0; i < 10; i++) {
//             captchaCtx.strokeStyle = `rgba(148,163,184,${0.3 + Math.random() * 0.5})`;
//             captchaCtx.lineWidth = 0.7 + Math.random() * 1.3;
//             captchaCtx.beginPath();
//             captchaCtx.moveTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
//             captchaCtx.lineTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
//             captchaCtx.stroke();
//         }

//         // Lignes courbes parasites (Bezier / courbes)
//         for (let i = 0; i < 6; i++) {
//             captchaCtx.strokeStyle = `rgba(56,189,248,${0.2 + Math.random() * 0.4})`;
//             captchaCtx.lineWidth = 1;
//             captchaCtx.beginPath();
//             const startX = Math.random() * captchaCanvas.width;
//             const startY = Math.random() * captchaCanvas.height;
//             const cp1X = Math.random() * captchaCanvas.width;
//             const cp1Y = Math.random() * captchaCanvas.height;
//             const cp2X = Math.random() * captchaCanvas.width;
//             const cp2Y = Math.random() * captchaCanvas.height;
//             const endX = Math.random() * captchaCanvas.width;
//             const endY = Math.random() * captchaCanvas.height;
//             captchaCtx.moveTo(startX, startY);
//             captchaCtx.bezierCurveTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY);
//             captchaCtx.stroke();
//         }

//         // Caractères très déformés + couleurs
//         for (let i = 0; i < CAPTCHA_LENGTH; i++) {
//             const char = randomCaptchaChar();
//             captchaCode += char;

//             const spacing = captchaCanvas.width / (CAPTCHA_LENGTH + 1);

//             // Position de base du caractère
//             const baseX = spacing * (i + 1);
//             const baseY = captchaCanvas.height / 2 + (Math.random() * 12 - 6);

//             // Déformations individuelles
//             const angle = (Math.random() - 0.5) * 1.0;          // rotation forte
//             const scaleX = 0.7 + Math.random() * 1.1;           // étirement horizontal
//             const scaleY = 0.7 + Math.random() * 1.1;           // étirement vertical
//             const shearX = (Math.random() - 0.5) * 0.6;         // inclinaison horizontale
//             const shearY = (Math.random() - 0.5) * 0.3;         // inclinaison verticale
//             const fontSize = 26 + Math.floor(Math.random() * 12);

//             captchaCtx.save();

//             // On translate au point de base
//             captchaCtx.translate(baseX, baseY);

//             // Combinaison rotation + shear + scale via transform
//             const cos = Math.cos(angle);
//             const sin = Math.sin(angle);

//             // Matrice approximative pour combiner rotation, scale et shear
//             const a = cos * scaleX + shearY * sin * scaleX;
//             const b = sin * scaleX;
//             const c = shearX * cos * scaleY + -sin * scaleY;
//             const d = shearX * sin * scaleY + cos * scaleY;

//             captchaCtx.transform(a, b, c, d, 0, 0);

//             captchaCtx.font = "bold " + fontSize + "px Arial";
//             captchaCtx.fillStyle = randomTextColor();
//             captchaCtx.shadowColor = "rgba(0,0,0,0.7)";
//             captchaCtx.shadowBlur = 3;

//             // Décalage léger pour centrer à peu près autour de (0,0)
//             captchaCtx.fillText(char, -8, 8);

//             captchaCtx.restore();
//         }

//         captchaInput.value = "";
//     }

//     refreshCaptchaBtn.addEventListener("click", function () {
//         generateCaptcha();
//         errorMsg.style.display = "none";
//     });

//     // Génère un premier captcha au chargement
//     generateCaptcha();

//     form.addEventListener("submit", async function (e) {
//         e.preventDefault();
//         const email = document.getElementById("email").value.trim();
//         const password = document.getElementById("password").value.trim();
//         const captchaValue = captchaInput.value.trim().toUpperCase();
//         errorMsg.style.display = "none";

//         // 🛑 Vérification CAPTCHA avant l'appel API
//         if (!captchaValue) {
//             errorMsg.textContent = "Veuillez recopier le code de sécurité.";
//             errorMsg.style.display = "block";
//             return;
//         }

//         if (captchaValue !== captchaCode) {
//             errorMsg.textContent = "Captcha invalide. Veuillez réessayer.";
//             errorMsg.style.display = "block";
//             generateCaptcha();
//             return;
//         }

//         try {
//             const res = await fetch(`${API_BASE}/api/auth/login-swagger`, {
//                 method: "POST",
//                 headers: { "Content-Type": "application/json" },
//                 credentials: "include",
//                 body: JSON.stringify({ email, password })
//             });

//             // 🔒 Cas 403 : trop de tentatives → on bloque l'UI
//             if (res.status === 403) {
//                 let data = {};
//                 try { data = await res.json(); } catch(e) {}
//                 errorMsg.textContent = data.message || "Accès bloqué après 3 tentative. Contactez l'administrateur.";
//                 errorMsg.style.display = "block";

//                 // Désactivation du formulaire + captcha
//                 submitBtn.disabled = true;
//                 submitBtn.style.opacity = "0.5";
//                 submitBtn.style.cursor = "not-allowed";
//                 document.getElementById("email").disabled = true;
//                 document.getElementById("password").disabled = true;
//                 captchaInput.disabled = true;
//                 refreshCaptchaBtn.disabled = true;
//                 refreshCaptchaBtn.style.opacity = "0.5";
//                 refreshCaptchaBtn.style.cursor = "not-allowed";

//                 return;
//             }

//             // ❌ Autres erreurs (401…) → on affiche le message + tentatives restantes
//             if (!res.ok) {
//                 let data = {};
//                 try { data = await res.json(); } catch(e) {}
//                 if (data.tentativesRestantes !== undefined) {
//                     errorMsg.textContent =
//                         (data.message || "Identifiants invalides.") +
//                         " Tentatives restantes : " + data.tentativesRestantes;
//                 } else {
//                     errorMsg.textContent = data.message || "Identifiants invalides ou droits insuffisants.";
//                 }
//                 errorMsg.style.display = "block";
//                 generateCaptcha();
//                 return;
//             }

//             // ✅ Login OK → on renvoie vers Swagger UI
//             window.location.href = API_BASE + "/swagger-ui/index.html";

//         } catch (err) {
//             console.error(err);
//             errorMsg.textContent = "Erreur réseau, réessayez plus tard.";
//             errorMsg.style.display = "block";
//         }
//     });
// </script>
// </body>
// </html>
// """;
//     }
// }

package com.fdjloto.api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminLoginController {

    @GetMapping(value = "/admin-login.html", produces = MediaType.TEXT_HTML_VALUE)
    public String adminLoginPage() {

        return """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Login admin - Swagger</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="/favicon-admin.ico">


<style>
  /* Base inputs (au cas où le navigateur force un style) */
  input {
    background-color: #020617 !important;
    color: #e5e7eb !important;
  }

  /* Focus */
  input:focus {
    background-color: #020617 !important;
    color: #e5e7eb !important;
    outline: none;
  }

  /* ✅ Chrome/Safari: autofill (cause du fond blanc) */
  input:-webkit-autofill,
  input:-webkit-autofill:hover,
  input:-webkit-autofill:focus,
  textarea:-webkit-autofill,
  textarea:-webkit-autofill:hover,
  textarea:-webkit-autofill:focus,
  select:-webkit-autofill,
  select:-webkit-autofill:hover,
  select:-webkit-autofill:focus {
    -webkit-text-fill-color: #e5e7eb !important;
    caret-color: #e5e7eb !important;

    /* Peint le fond en sombre malgré l'autofill */
    -webkit-box-shadow: 0 0 0px 1000px #020617 inset !important;
            box-shadow: 0 0 0px 1000px #020617 inset !important;

    border: 1px solid #4b5563 !important;
    transition: background-color 9999s ease-out 0s;
  }

    /* Standard-ish */
    input:autofill {
    background-color: #020617 !important;
    color: #e5e7eb !important;
    }

    /* Chrome/Safari (déjà OK chez toi) */
    input:-webkit-autofill,
    input:-webkit-autofill:hover,
    input:-webkit-autofill:focus {
    -webkit-text-fill-color: #e5e7eb !important;
    caret-color: #e5e7eb !important;
    -webkit-box-shadow: 0 0 0 1000px #020617 inset !important;
            box-shadow: 0 0 0 1000px #020617 inset !important;
    border: 1px solid #4b5563 !important;
    }


</style>
</head>
<body style="background:#020617;color:#e5e7eb;font-family:system-ui, sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0">
<div style="background:#0f172a;border-radius:16px;padding:24px 28px;max-width:380px;width:100%;box-shadow:0 20px 60px rgba(15,23,42,0.9)">
    <h1 style="margin-top:0;margin-bottom:12px;font-size:1.4rem;">Connexion admin</h1>
    <p style="margin:0 0 18px;font-size:0.9rem;color:#9ca3af;">
        Accès réservé à l'administrateur pour consulter la documentation Swagger.
    </p>

    <form id="adminLoginForm">
        <div style="margin-bottom:10px;">
        <label for="email" style="display:block;font-size:0.85rem;margin-bottom:4px;">Email admin</label>

        <div style="position:relative;width:100%;">
            <input id="email" type="email" required
            style="width:100%;
                    box-sizing:border-box;
                    padding:8px 46px 8px 10px;
                    border-radius:8px;
                    border:1px solid #4b5563;
                    background:#020617;
                    color:#e5e7eb;">

            <!-- ✅ / ❌ indicateur -->
            <div id="emailStatus"
            style="position:absolute;right:10px;top:50%;transform:translateY(-50%);
                    width:26px;height:26px;
                    display:flex;align-items:center;justify-content:center;
                    pointer-events:none;opacity:0;transition:opacity .15s ease;">
            <!-- SVG (sera injecté en JS) -->
            </div>
        </div>
        </div>


        <div style="margin-bottom:14px;">
        <label for="password" style="display:block;font-size:0.85rem;margin-bottom:4px;">Mot de passe</label>

        <div style="position:relative;width:100%;">
            <input id="password" type="password" required
            style="width:100%;
                    box-sizing:border-box;
                    padding:8px 46px 8px 10px;
                    border-radius:8px;
                    border:1px solid #4b5563;
                    background:#020617;
                    color:#e5e7eb;">

            <button type="button" id="togglePassword"
            aria-label="Afficher / masquer le mot de passe"
            style="position:absolute;right:10px;top:50%;transform:translateY(-50%);
                    width:26px;height:26px;padding:0;
                    background:none;border:none;cursor:pointer;
                    display:flex;align-items:center;justify-content:center;">
            <!-- 👁️ SVG néon -->
            <svg id="eyeIcon" width="22" height="22" viewBox="0 0 24 24" fill="none"
                stroke="#22c55e" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                style="filter:drop-shadow(0 0 6px rgba(34,197,94,0.8));transition:all 0.25s ease;">
                <path id="eyeOpen" d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"/>
                <circle id="eyePupil" cx="12" cy="12" r="3"/>
                <line id="eyeSlash" x1="3" y1="21" x2="21" y2="3" style="opacity:0;transition:opacity 0.2s ease;"/>
            </svg>
            </button>
        </div>
        </div>



        <!-- 🔐 CAPTCHA très avancé -->
        <div style="margin-bottom:14px;">
            <label style="display:block;font-size:0.85rem;margin-bottom:6px;">
                Vérification anti-robot
            </label>

            <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px;">
                <canvas id="captchaCanvas" width="220" height="70"
                        style="border-radius:8px;border:1px solid #4b5563;background:#020617;cursor:not-allowed;"></canvas>

                <button type="button" id="refreshCaptcha"
                        style="padding:6px 10px;border-radius:999px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;cursor:pointer;">
                    ↻
                </button>
            </div>

            <input id="captchaAnswer" type="text" required
                   placeholder="Recopiez le code"
                   autocomplete="off" spellcheck="false"
                   style="width:100%;padding:8px 10px;border-radius:8px;border:1px solid #4b5563;background:#020617;color:#e5e7eb;">
        </div>

        <button id="submitBtn" type="submit"
                style="width:100%;padding:9px 12px;border-radius:999px;border:none;background:#22c55e;color:#020617;font-weight:600;cursor:pointer;">
            Se connecter
        </button>
    </form>

    <p id="errorMsg" style="margin-top:10px;font-size:0.85rem;color:#f97373;display:none;"></p>

    <p style="margin-top:14px;font-size:0.8rem;color:#6b7280;">
        Accès bloqué après 3 tentatives.
    </p>
    <p style="margin-top:4px;font-size:0.8rem;color:#6b7280;">
        Après connexion réussie, vous serez redirigé automatiquement vers Swagger UI.
    </p>
</div>

<script>
    // 🔧 BASE URL dynamique : local vs prod
    const API_BASE = window.location.origin;


    const form = document.getElementById("adminLoginForm");
    const errorMsg = document.getElementById("errorMsg");
    const submitBtn = document.getElementById("submitBtn");

    // 🔐 CAPTCHA très difficile (canvas + code alphanumérique lourdement déformé)
    const captchaCanvas = document.getElementById("captchaCanvas");
    const captchaCtx = captchaCanvas.getContext("2d");
    const captchaInput = document.getElementById("captchaAnswer");
    const refreshCaptchaBtn = document.getElementById("refreshCaptcha");

    let captchaCode = "";
    const CAPTCHA_LENGTH = 7; // nombre de caractères

    function randomCaptchaChar() {
        const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // pas de 0,1,O,I
        return chars.charAt(Math.floor(Math.random() * chars.length));
    }

    function randomTextColor() {
        const colors = ["#f97316", "#22c55e", "#38bdf8", "#a855f7", "#eab308", "#f97373", "#34d399"];
        return colors[Math.floor(Math.random() * colors.length)];
    }

    // ✅ Validation email (regex) + icône + bordure néon
    const emailInput = document.getElementById("email");
    const emailStatus = document.getElementById("emailStatus");

    // IMPORTANT: dans un text block Java, il faut doubler les backslashes
    // const EMAIL_REGEX = /^(?=.{6,254}$)(?=.{1,64}@)[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$/;
    const EMAIL_REGEX = new RegExp("^(?=.{6,254}$)(?=.{1,64}@)[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");

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

    // vide -> neutre
    if (!v) {
        setEmailNeutral();
        return;
    }

    // valid / invalid
    if (EMAIL_REGEX.test(v)) setEmailValid();
    else setEmailInvalid();
    }

    emailInput.addEventListener("input", validateEmailLive);
    emailInput.addEventListener("blur", validateEmailLive);
    validateEmailLive();


    // 👁️ Toggle password – œil néon
    const togglePasswordBtn = document.getElementById("togglePassword");
    const passwordInput = document.getElementById("password");
    const eyeSlash = document.getElementById("eyeSlash");
    const eyeIcon = document.getElementById("eyeIcon");

    function syncEyeState() {
    const hidden = passwordInput.type === "password"; // caché = password
    eyeSlash.style.opacity = hidden ? "1" : "0";      // barré quand c’est caché
    eyeIcon.style.filter = hidden
        ? "drop-shadow(0 0 6px rgba(34,197,94,0.7))"
        : "drop-shadow(0 0 10px rgba(34,197,94,1))";
    }

    togglePasswordBtn.addEventListener("click", () => {
    passwordInput.type = passwordInput.type === "password" ? "text" : "password";
    syncEyeState();
    });

    // état initial (important)
    syncEyeState();



    function generateCaptcha() {
        captchaCode = "";

        // Reset transform au cas où
        captchaCtx.setTransform(1, 0, 0, 1, 0, 0);
        captchaCtx.clearRect(0, 0, captchaCanvas.width, captchaCanvas.height);

        // Fond
        const gradient = captchaCtx.createLinearGradient(0, 0, captchaCanvas.width, captchaCanvas.height);
        gradient.addColorStop(0, "#020617");
        gradient.addColorStop(0.5, "#020617");
        gradient.addColorStop(1, "#020617");
        captchaCtx.fillStyle = gradient;
        captchaCtx.fillRect(0, 0, captchaCanvas.width, captchaCanvas.height);

        // Bruit de fond (points)
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

        // Petits rectangles parasites
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

        // Lignes droites parasites (beaucoup)
        for (let i = 0; i < 25; i++) {
            captchaCtx.strokeStyle = `rgba(148,163,184,${0.3 + Math.random() * 0.5})`;
            captchaCtx.lineWidth = 0.7 + Math.random() * 1.3;
            captchaCtx.beginPath();
            captchaCtx.moveTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
            captchaCtx.lineTo(Math.random() * captchaCanvas.width, Math.random() * captchaCanvas.height);
            captchaCtx.stroke();
        }

        // Lignes courbes parasites (Bezier)
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

        // Zigzags / polylignes parasites
        for (let i = 0; i < 10; i++) {
            captchaCtx.strokeStyle = `rgba(99,102,241,${0.2 + Math.random() * 0.5})`;
            captchaCtx.lineWidth = 0.8 + Math.random();
            captchaCtx.beginPath();
            let x = Math.random() * captchaCanvas.width;
            let y = Math.random() * captchaCanvas.height;
            captchaCtx.moveTo(x, y);
            const segments = 4 + Math.floor(Math.random() * 4);
            for (let s = 0; s < segments; s++) {
                x += (Math.random() - 0.5) * 40;
                y += (Math.random() - 0.5) * 24;
                captchaCtx.lineTo(
                    Math.max(0, Math.min(captchaCanvas.width, x)),
                    Math.max(0, Math.min(captchaCanvas.height, y))
                );
            }
            captchaCtx.stroke();
        }

        // Caractères très déformés + couleurs
        for (let i = 0; i < CAPTCHA_LENGTH; i++) {
            const char = randomCaptchaChar();
            captchaCode += char;

            const spacing = captchaCanvas.width / (CAPTCHA_LENGTH + 1);

            // Position de base du caractère
            const baseX = spacing * (i + 1);
            const baseY = captchaCanvas.height / 2 + (Math.random() * 12 - 6);

            // Déformations individuelles
            const angle = (Math.random() - 0.5) * 1.2;          // rotation encore plus forte
            const scaleX = 0.6 + Math.random() * 1.3;           // étirement horizontal
            const scaleY = 0.6 + Math.random() * 1.3;           // étirement vertical
            const shearX = (Math.random() - 0.5) * 0.7;         // inclinaison horizontale
            const shearY = (Math.random() - 0.5) * 0.4;         // inclinaison verticale
            const fontSize = 26 + Math.floor(Math.random() * 14);

            captchaCtx.save();

            // On translate au point de base
            captchaCtx.translate(baseX, baseY);

            // Combinaison rotation + shear + scale via transform
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

    refreshCaptchaBtn.addEventListener("click", function () {
        generateCaptcha();
        errorMsg.style.display = "none";
    });

    // Génère un premier captcha au chargement
    generateCaptcha();

    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();
        const captchaValue = captchaInput.value.trim().toUpperCase();
        errorMsg.style.display = "none";

        // 🛑 Vérification CAPTCHA avant l'appel API
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
                try { data = await res.json(); } catch(e) {}
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
                try { data = await res.json(); } catch(e) {}
                if (data.tentativesRestantes !== undefined) {
                    errorMsg.textContent =
                        (data.message || "Identifiants invalides.") +
                        " Tentatives restantes : " + data.tentativesRestantes;
                } else {
                    errorMsg.textContent = data.message || "Identifiants invalides ou droits insuffisants.";
                }
                errorMsg.style.display = "block";
                generateCaptcha();
                return;
            }

            // window.location.href = API_BASE + "/swagger-ui/index.html";
            //window.location.href = API_BASE + "/admin/dashboard";
            window.location.href = "/admin/dashboard";


        } catch (err) {
            console.error(err);
            errorMsg.textContent = "Erreur réseau, réessayez plus tard.";
            errorMsg.style.display = "block";
        }
    });
</script>
</body>
</html>
""";
    }
}
