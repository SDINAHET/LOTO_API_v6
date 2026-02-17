(function () {
  const API_BASE =
    (location.hostname === "localhost" || location.hostname === "127.0.0.1")
      ? "http://localhost:8082"
      : "https://stephanedinahet.fr";

  const ME_URL = `${API_BASE}/api/auth/me`;
  const API_TICKETS = `${API_BASE}/api/tickets`;
  const API_GAINS = `${API_BASE}/api/gains`;

  /* ===== DOM ===== */
  const tableBody = document.getElementById("ticketsTable");
  const searchInput = document.getElementById("searchInput");
  const pageSizeSelect = document.getElementById("pageSize");
  const pagerInfo = document.getElementById("pagerInfo");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");
  const logScaleToggle = document.getElementById("logScale");

  const totalDepenseEl = document.getElementById("totalDepense");
  const totalGagneEl = document.getElementById("totalGagne");

  const errBox = document.getElementById("errBox");
  const errText = document.getElementById("errText");

  let chartInstance = null;

  /* ===== STATE ===== */
  let allTickets = [];
  let filteredTickets = [];
  let currentPage = 1;

  /* ===== UTILS ===== */
  const fmtLong = d =>
    new Date(d).toLocaleDateString("fr-FR", {
      day: "2-digit",
      month: "long",
      year: "numeric"
    });

  const fmtShort = d =>
    new Date(d).toLocaleDateString("fr-FR", {
      day: "2-digit",
      month: "2-digit"
    });

  function showError(msg) {
    errText.textContent = msg;
    errBox.classList.remove("d-none");
  }

  function hideError() {
    errBox.classList.add("d-none");
  }

  function getStatus(drawDate, gain) {
    const today = new Date().toISOString().split("T")[0];
    const d = new Date(drawDate).toISOString().split("T")[0];

    if (d < today) {
      return gain > 0
        ? { label: "Gagné", cls: "badge-win" }
        : { label: "Perdu", cls: "badge-lose" };
    }
    return { label: "En attente", cls: "badge-wait" };
  }

  /* ===== TABLE ===== */
  function renderTable() {
    tableBody.innerHTML = "";

    const size = Number(pageSizeSelect.value);
    const start = (currentPage - 1) * size;
    const pageItems = filteredTickets.slice(start, start + size);

    if (!pageItems.length) {
      tableBody.innerHTML =
        `<tr><td colspan="3" class="muted">Aucun résultat</td></tr>`;
      return;
    }

    for (const t of pageItems) {
      const gain = t.gain;
      const st = getStatus(t.drawDate, gain);

      tableBody.innerHTML += `
        <tr>
          <td data-label="Date">
            <div class="ticket-head">
              <b class="ticket-date">${fmtLong(t.drawDate)}</b>

              <!-- Gain visible uniquement en mobile -->
              <b class="ticket-gain-mobile d-lg-none">${gain.toFixed(2)} €</b>

              <span class="badge-soft ${st.cls} ticket-status">${st.label}</span>
            </div>
          </td>


          <td data-label="Statut" class="cell-hide-mobile">
            <span class="badge-soft ${st.cls}">${st.label}</span>
          </td>

          <td data-label="Gains" class="text-end">
            <b>${gain.toFixed(2)} €</b>
          </td>
        </tr>
      `;


    }

    const maxPage = Math.ceil(filteredTickets.length / size);
    pagerInfo.textContent = `Page ${currentPage} / ${maxPage}`;

    prevBtn.disabled = currentPage === 1;
    nextBtn.disabled = currentPage === maxPage;
  }

  /* ===== CHART ===== */
    function renderChart() {
    const monthly = {};

    filteredTickets.forEach(t => {
        const d = new Date(t.drawDate);
        const key = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}`;

        if (!monthly[key]) {
        monthly[key] = { depense: 0, gain: 0 };
        }
        monthly[key].depense += 2.20;
        monthly[key].gain += t.gain;
    });

    const labels = Object.keys(monthly).sort().map(k => {
        const [y,m] = k.split("-");
        return new Date(y, m-1).toLocaleDateString("fr-FR", { month:"long", year:"numeric" });
    });

    const depenses = Object.keys(monthly).sort().map(k => monthly[k].depense);
    const gains = Object.keys(monthly).sort().map(k => monthly[k].gain);

    const ctx = document.getElementById("statsChart").getContext("2d");
    if (chartInstance) chartInstance.destroy();

    chartInstance = new Chart(ctx, {
        type: "bar",
        data: {
        labels,
        datasets: [
            {
            label: "Dépenses (€)",
            data: depenses,
            backgroundColor: "rgba(59,130,246,0.65)"
            },
            {
            label: "Gains (€)",
            data: gains,
            backgroundColor: "rgba(16,185,129,0.65)"
            }
        ]
        },
        options: {
        responsive: true,
        plugins: {
            legend: {
            labels: { color: "rgba(255,255,255,.85)" }
            }
        },
        scales: {
            x: {
            ticks: { color: "rgba(255,255,255,.7)" },
            grid: { color: "rgba(255,255,255,.06)" }
            },
            y: {
            type: logScaleToggle.checked ? "logarithmic" : "linear",
            beginAtZero: true,
            ticks: { color: "rgba(255,255,255,.7)" },
            grid: { color: "rgba(255,255,255,.06)" }
            }
        }
        }
    });
    }


  /* ===== FILTER ===== */
  function applyFilters() {
    const q = searchInput.value.toLowerCase();
    filteredTickets = allTickets.filter(t =>
      fmtLong(t.drawDate).toLowerCase().includes(q)
    );
    currentPage = 1;
    renderTable();
    renderChart();
  }

  /* ===== LOAD ===== */
  async function loadStats() {
    try {
      await axios.get(ME_URL, { withCredentials: true });

      const [tRes, gRes] = await Promise.all([
        axios.get(API_TICKETS, { withCredentials: true }),
        axios.get(API_GAINS, { withCredentials: true })
      ]);

      const gainsMap = new Map(
        gRes.data.map(g => [g.ticketId, Number(g.gainAmount || 0)])
      );

      allTickets = tRes.data
        .filter(t => t.drawDate)
        .sort((a, b) => new Date(b.drawDate) - new Date(a.drawDate))
        .map(t => ({
          ...t,
          gain: gainsMap.get(t.id) || 0
        }));

      filteredTickets = [...allTickets];

      totalDepenseEl.textContent = (allTickets.length * 2.2).toFixed(2);
      totalGagneEl.textContent = allTickets
        .reduce((s, t) => s + t.gain, 0)
        .toFixed(2);

      renderTable();
      renderChart();

    } catch (e) {
      showError("Impossible de charger les statistiques.");
      console.error(e);
    }
  }

  /* ===== EVENTS ===== */
  searchInput.addEventListener("input", applyFilters);
  pageSizeSelect.addEventListener("change", applyFilters);
  logScaleToggle.addEventListener("change", renderChart);

  prevBtn.addEventListener("click", () => {
    currentPage--;
    renderTable();
  });

  nextBtn.addEventListener("click", () => {
    currentPage++;
    renderTable();
  });

  document.addEventListener("DOMContentLoaded", loadStats);
})();
