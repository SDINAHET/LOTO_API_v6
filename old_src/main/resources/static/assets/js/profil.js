    let userInfo = {};

    const API_BASE =
      window.__API_BASE_ACTIVE__ ||
      ((location.hostname === "localhost" || location.hostname === "127.0.0.1")
        ? `http://${location.hostname}:8082`
        : "https://stephanedinahet.fr");

    //const API_BASE =
    //  (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
    //    ? "http://localhost:8082"
    //    : "https://stephanedinahet.fr";

    const ME_URL = `${API_BASE}/api/auth/me`;
    const TOKEN_URL = `${API_BASE}/api/auth/token`; // optionnel
    const USERS_URL = `${API_BASE}/api/users`;

    // RGPD delete account
    const DELETE_ME_URL = `${API_BASE}/api/users/me`;
    const LOGOUT_URL = `${API_BASE}/api/auth/logout`;

    const emailEl = document.getElementById("email");
    const firstEl = document.getElementById("firstName");
    const lastEl  = document.getElementById("lastName");
    const pwdEl   = document.getElementById("currentPassword");
    //const confirmPwdEl = document.getElementById("confirmPassword");
    //const newPwdEl = document.getElementById("newPassword");
    const formEl  = document.getElementById("profileForm");
    const btnEl   = document.getElementById("saveChanges");

    const okBox  = document.getElementById("okBox");
    const okText = document.getElementById("okText");
    const errBox = document.getElementById("errBox");
    const errText= document.getElementById("errText");

    // --- CSRF (comme POST/PUT tickets) ---
    axios.defaults.withCredentials = true;
    // (optionnel mais propre)
    axios.defaults.xsrfCookieName = "XSRF-TOKEN";
    axios.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

    function getCookie(name) {
      const m = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
      return m ? decodeURIComponent(m[2]) : null;
    }

    async function ensureCsrf(baseUrl) {
      try {
        // force Spring à générer / renvoyer le cookie XSRF-TOKEN
        await axios.get(`${baseUrl}/api/auth/csrf`, { withCredentials: true });
      } catch (e) {
        // si ça échoue, on laisse le delete gérer l'erreur
        console.warn("CSRF init failed:", e?.message || e);
      }
    }

    function showOk(msg){
      errBox.classList.add("d-none");
      okText.textContent = msg || "Modifications enregistrées.";
      okBox.classList.remove("d-none");
    }

    function showErr(msg){
      okBox.classList.add("d-none");
      errText.textContent = msg || "Erreur lors de la mise à jour.";
      errBox.classList.remove("d-none");
    }

    function setLoading(on){
      btnEl.disabled = on;
      btnEl.innerHTML = on
        ? `<span class="spinner-border spinner-border-sm me-2"></span>Enregistrement...`
        : `Enregistrer les modifications`;
    }

    // optionnel (si ton backend l’exige)
    async function getJwtToken(){
      try{
        const res = await axios.get(TOKEN_URL, { withCredentials: true });
        return res.data?.jwtToken || null;
      }catch(e){
        return null;
      }
    }

    async function loadUserProfile(){
      try{
        const res = await axios.get(ME_URL, { withCredentials: true });
        userInfo = res.data || {};

        emailEl.value = userInfo.email || "";
        firstEl.value = userInfo.first_name || userInfo.firstName || "";
        lastEl.value  = userInfo.last_name  || userInfo.lastName  || "";

        if (userInfo.id) formEl.setAttribute("data-user-id", userInfo.id);
      }catch(e){
        console.error("Erreur /api/auth/me:", e);
        showErr("Session expirée. Merci de te reconnecter.");
        setTimeout(() => window.location.href = "login.html", 900);
      }
    }

    formEl.addEventListener("submit", async (e) => {
      e.preventDefault();

      okBox.classList.add("d-none");
      errBox.classList.add("d-none");

      const userId = formEl.getAttribute("data-user-id");
      if (!userId) return showErr("ID utilisateur introuvable.");

      const password = pwdEl.value?.trim();
      if (!password) return showErr("Merci de saisir ton mot de passe actuel.");

    //const payload = {
    //  id: userId,
    //  email: userInfo.email,                 // ⚠️ obligatoire sinon setEmail(null)
    //  firstName: firstEl.value.trim(),
    //  lastName:  lastEl.value.trim(),


      // ⚠️ TON controller encode user.password -> donc il faut l’envoyer
    //  password: password
    //};
    const payload = {
      firstName: firstEl.value.trim(),
      lastName: lastEl.value.trim(),
      currentPassword: password
    };
    //const payload = {
    //  id: userId,
    //  email: userInfo.email,        // ⚠️ souvent obligatoire côté JPA
    //  firstName: firstEl.value.trim(),
    //  lastName: lastEl.value.trim(),
    //  password: password            // ✅ NOM ATTENDU
    //};

      try {
        setLoading(true);

        const jwt = await getJwtToken();
        const headers = { "Content-Type": "application/json" };
        if (jwt) headers["Authorization"] = `Bearer ${jwt}`;

        //await axios.put(`${USERS_URL}/${userId}`, payload, {
        //  withCredentials: true,
        //  headers
        //});

        const res = await fetchWithRefresh(`${USERS_URL}/${userId}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });

        if (!res.ok) {
          const txt = await res.text().catch(() => "");
          throw { response: { status: res.status, data: { message: txt } } };
        }


        showOk("Profil mis à jour avec succès. Redirection…");
        pwdEl.value = "";

        setTimeout(() => {
          window.location.href = "index.html";
        }, 1000);

      } catch (err) {
        console.error("Erreur PUT:", err?.response?.data || err.message);

        if (err?.response?.status === 401) {
          showErr("Session expirée. Merci de te reconnecter.");
        } else if (err?.response?.status === 400) {
          showErr("Mot de passe actuel incorrect.");
        } else {
          showErr(err?.response?.data?.message || "Erreur lors de l’enregistrement.");
        }
      } finally {
        setLoading(false);
      }
    });


    document.addEventListener("DOMContentLoaded", loadUserProfile);

    // toggle password
    (function(){
      const input = document.getElementById("currentPassword");
      const btn = document.getElementById("togglePassword");
      if (!input || !btn) return;

      btn.addEventListener("click", () => {
        const show = input.type === "password";
        input.type = show ? "text" : "password";
        btn.classList.toggle("is-on", show);
      });
    })();

    // ✅ Fix overlay + modal (évite impression "figé" si overlay reste actif)
    (function fixOverlayWithModal(){
      const overlay = document.getElementById("overlay");
      const modalEl = document.getElementById("deleteModal");
      if (!overlay || !modalEl) return;

      modalEl.addEventListener("show.bs.modal", () => {
        overlay.classList.remove("show");
      });
    })();

    // --- Suppression de compte (RGPD) ---
    (function setupDeleteAccount(){
      const btnOpen = document.getElementById("btnDeleteAccount");
      const modalEl = document.getElementById("deleteModal");
      const pwd = document.getElementById("deletePassword");
      const btnConfirm = document.getElementById("confirmDeleteBtn");

      const delErrBox = document.getElementById("delErrBox");
      const delErrText = document.getElementById("delErrText");

      if (!btnOpen || !modalEl || !pwd || !btnConfirm) return;

      const modal = new bootstrap.Modal(modalEl);

      function showDelErr(msg){
        delErrText.textContent = msg || "Erreur lors de la suppression.";
        delErrBox.classList.remove("d-none");
      }
      function clearDelErr(){
        delErrBox.classList.add("d-none");
        delErrText.textContent = "";
      }

      btnOpen.addEventListener("click", () => {
        clearDelErr();
        pwd.value = "";
        modal.show();
        setTimeout(() => pwd.focus(), 250);
      });

      btnConfirm.addEventListener("click", async () => {
        clearDelErr();

        const currentPassword = pwd.value?.trim();
        if (!currentPassword) return showDelErr("Merci de saisir ton mot de passe.");

        btnConfirm.disabled = true;
        btnConfirm.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Suppression...`;

        try {
          // ✅ Utilise fetch() patché par layout.js (CSRF + credentials + refresh)
          const res = await fetch(DELETE_ME_URL, {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ currentPassword })
          });

          if (!res.ok) {
            const txt = await res.text().catch(() => "");
            const msg = txt || `Erreur HTTP ${res.status}`;

            if (res.status === 401) throw { status: 401, msg };
            if (res.status === 400) throw { status: 400, msg };
            if (res.status === 403) throw { status: 403, msg };

            throw { status: res.status, msg };
          }

          // Tentative de logout (optionnel)
          try { await fetch(LOGOUT_URL, { method: "POST" }); } catch(e){}

          // Nettoyage front
          try { localStorage.removeItem("token"); } catch(e){}
          try { sessionStorage.clear(); } catch(e){}

          modal.hide();
          showOk("Compte supprimé. Déconnexion…");

          setTimeout(() => {
            window.location.href = "login.html";
          }, 900);

        } catch (err) {
          console.error("Erreur DELETE /me:", err);

          if (err?.status === 401) {
            showDelErr("Session expirée. Merci de te reconnecter.");
          } else if (err?.status === 400) {
            showDelErr("Mot de passe incorrect.");
          } else if (err?.status === 403) {
            showDelErr("CSRF refusé (403). Vérifie que /api/auth/csrf répond bien et que le cookie XSRF-TOKEN est présent.");
          } else {
            showDelErr("Impossible de supprimer le compte. Réessaie.");
          }
        } finally {
          btnConfirm.disabled = false;
          btnConfirm.innerHTML = `Oui, supprimer`;
        }
      });
    })();


    document.addEventListener("DOMContentLoaded", () => {
      const note = document.getElementById("emailNote");
      if (!note) return;

      setTimeout(() => {
        note.classList.add("fade-out");

        // optionnel : retirer complètement du DOM après l’animation
        setTimeout(() => {
          note.remove();
        }, 600);
      }, 3000); // ⏱️ 3 secondes
    });
