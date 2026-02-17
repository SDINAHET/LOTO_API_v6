// config.js
// (function () {
//   const HOST = window.location.hostname;

//   const PROD_DOMAINS = [
//     "stephanedinahet.fr",
//     "loto-tracker.fr"
//   ];

//   const IS_PROD = PROD_DOMAINS.some(domain =>
//     HOST === domain ||
//     HOST === `www.${domain}` ||
//     HOST.endsWith(`.${domain}`)
//   );

//   const API_BASE = IS_PROD
//     ? window.location.origin
//     : `${window.location.protocol}//${HOST}:8082`;

//   // On rend API_BASE global
//   window.API_BASE = API_BASE;

//   console.log("API_BASE:", API_BASE);
// })();


// (function () {
//   const host = window.location.hostname;

//   const isLocal = (host === "localhost" || host === "127.0.0.1");
//   const isLotoTracker = host === "loto-tracker.fr" || host === "www.loto-tracker.fr";

//   const API_BASE = isLocal
//     ? "http://localhost:8082"
//     : (isLotoTracker ? "https://loto-tracker.fr" : "https://stephanedinahet.fr");

//   window.API_BASE = API_BASE;
//   window.getApiBase = () => window.API_BASE;

//   console.log("API_BASE =", window.API_BASE);
// })();



// (function () {
//   const HOST = window.location.hostname;

//   const PROD_DOMAINS = ["stephanedinahet.fr", "loto-tracker.fr"];

//   const IS_PROD = PROD_DOMAINS.some(d =>
//     HOST === d ||
//     HOST === `www.${d}` ||
//     HOST.endsWith(`.${d}`)
//   );

//   // ✅ Local => API sur 8082 du même host (localhost/127.0.0.1)
//   // ✅ Prod => même origin (fonctionne pour stephanedinahet.fr ET loto-tracker.fr)
//   const API_BASE_PRIMARY = (HOST === "localhost" || HOST === "127.0.0.1")
//     ? `http://${HOST}:8082`
//     : (IS_PROD ? window.location.origin : "https://stephanedinahet.fr"); // fallback sécurité

//   window.API_BASE = API_BASE_PRIMARY;
//   window.getApiBase = () => window.API_BASE;

//   console.log("API_BASE =", window.API_BASE);
// })();
