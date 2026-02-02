// ai-loader.js
(async () => {
  try {
    // URL de ai.html dans le même dossier que ai-loader.js
    const base = new URL(".", document.currentScript.src);
    const aiUrl = new URL("ai.html", base);

    const res = await fetch(aiUrl, { cache: "no-store" });
    if (!res.ok) throw new Error("ai.html introuvable: " + res.status);

    const html = await res.text();

    const host = document.createElement("div");
    host.id = "lt-ai-host";
    host.innerHTML = html;
    document.body.appendChild(host);

    // exécute les <script> contenus dans ai.html
    host.querySelectorAll("script").forEach((old) => {
      const s = document.createElement("script");
      if (old.src) s.src = old.src;
      s.textContent = old.textContent;
      document.body.appendChild(s);
      old.remove();
    });
  } catch (e) {
    console.warn("Chatbot non chargé :", e);
  }
})();
