// cookies.js (global) - injecte le popup si absent + gère consentement
(function () {
  const CONSENT_KEY = "cookieConsent";

  function safeGetConsent() {
    try { return localStorage.getItem(CONSENT_KEY); } catch { return null; }
  }
  function safeSetConsent(value) {
    try { localStorage.setItem(CONSENT_KEY, value); } catch {}
  }

  function ensurePopupExists() {
    let popup = document.getElementById("cookie-popup");
    if (popup) return popup;

    // Injecte le HTML du popup (même structure que ton index.html)
    const wrapper = document.createElement("div");
    wrapper.innerHTML = `
      <div id="cookie-popup" class="cookie-popup" hidden>
        <div class="cookie-content">
          <div class="cookie-head">
            <div class="cookie-icon" aria-hidden="true">🍪</div>
            <div>
              <h2 id="cookie-title">Cookies & confidentialité</h2>
              <p id="cookie-desc">
                On utilise des cookies pour faire fonctionner le site et, si vous l’acceptez,
                mesurer l’audience pour améliorer Loto Tracker.
              </p>
            </div>
          </div>

          <div class="cookie-cards">
            <div class="cookie-card">
              <div class="cookie-card-title">
                <span class="dot dot-blue"></span>
                <strong>Essentiels</strong>
                <span class="pill-pill">Toujours actifs</span>
              </div>
              <div class="cookie-card-text">Connexion, sécurité, navigation.</div>
            </div>

            <div class="cookie-card">
              <div class="cookie-card-title">
                <span class="dot dot-cyan"></span>
                <strong>Mesure d’audience</strong>
                <span class="pill-pill pill-muted">Optionnel</span>
              </div>
              <div class="cookie-card-text">Activé uniquement avec votre accord.</div>
            </div>
          </div>

          <div class="cookie-footer">
            <a class="cookie-link" href="politique_confidentialite.html#cookies-traceurs">
              En savoir plus
            </a>

            <div class="cookie-actions">
              <button id="reject-cookies" type="button">Continuer sans accepter</button>
              <button id="accept-cookies" type="button">Accepter</button>
            </div>
          </div>
        </div>
      </div>
    `.trim();

    document.body.appendChild(wrapper.firstElementChild);
    return document.getElementById("cookie-popup");
  }

  function openCookies(popup) {
    if (!popup) return;
    popup.hidden = false;
    document.body.classList.add("popup-open");
  }

  function closeCookies(popup, choice) {
    if (!popup) return;
    safeSetConsent(choice);
    popup.hidden = true;
    document.body.classList.remove("popup-open");
  }

  function init() {
    const popup = ensurePopupExists();
    const acceptBtn = document.getElementById("accept-cookies");
    const rejectBtn = document.getElementById("reject-cookies");

    const consent = safeGetConsent();
    if (!consent) openCookies(popup);
    else closeCookies(popup, consent);

    acceptBtn?.addEventListener("click", () => closeCookies(popup, "accepted"));
    rejectBtn?.addEventListener("click", () => closeCookies(popup, "rejected"));

    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && popup && !popup.hidden) closeCookies(popup, "rejected");
    });

    popup?.addEventListener("click", (e) => {
      if (e.target === popup) closeCookies(popup, "rejected");
    });

    // lien footer : <a id="openCookiePrefs" ...>
    document.addEventListener("click", (e) => {
      const link = e.target.closest("#openCookiePrefs");
      if (!link) return;
      e.preventDefault();
      openCookies(popup);
    });
  }

  // DOM ready
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
