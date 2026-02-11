#!/usr/bin/env bash
set -euo pipefail

# =====================================================
# Vendor pull script - Loto Tracker
# Télécharge les librairies frontend en local
# Cible : src/main/resources/static/assets/vendors/
# =====================================================

BASE_DIR="src/main/resources/static/assets/vendors"

# --- Check dependencies
command -v curl >/dev/null 2>&1 || { echo "❌ curl est requis"; exit 1; }

echo "📦 Installation des librairies frontend..."
echo "➡️  Dossier cible : $BASE_DIR"
echo ""

# Crée le dossier de base + sous-dossiers
mkdir -p \
  "$BASE_DIR/axios" \
  "$BASE_DIR/bootstrap" \
  "$BASE_DIR/chartjs" \
  "$BASE_DIR/moment" \
  "$BASE_DIR/leaflet" \
  "$BASE_DIR/fontawesome/css" \
  "$BASE_DIR/fontawesome/webfonts" \
  "$BASE_DIR/canvas-confetti"

# Helper download (fail on HTTP errors + retries)
dl() {
  local url="$1"
  local out="$2"
  echo "   -> $out"
  curl -fL --retry 3 --retry-delay 1 "$url" -o "$out"
}

# -------------------------
# Axios
# -------------------------
echo "⬇️  Axios"
dl "https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js" \
   "$BASE_DIR/axios/axios.min.js"

# -------------------------
# Bootstrap 5.3.0
# -------------------------
echo "⬇️  Bootstrap"
dl "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" \
   "$BASE_DIR/bootstrap/bootstrap.min.css"

dl "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" \
   "$BASE_DIR/bootstrap/bootstrap.bundle.min.js"

# -------------------------
# Chart.js
# -------------------------
echo "⬇️  Chart.js"
dl "https://cdn.jsdelivr.net/npm/chart.js" \
   "$BASE_DIR/chartjs/chart.min.js"

# -------------------------
# Moment.js + Timezone
# -------------------------
echo "⬇️  Moment.js"
dl "https://cdn.jsdelivr.net/npm/moment/min/moment.min.js" \
   "$BASE_DIR/moment/moment.min.js"

dl "https://cdn.jsdelivr.net/npm/moment-timezone/builds/moment-timezone-with-data.min.js" \
   "$BASE_DIR/moment/moment-timezone-with-data.min.js"

# -------------------------
# Leaflet 1.9.4
# -------------------------
echo "⬇️  Leaflet"
dl "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" \
   "$BASE_DIR/leaflet/leaflet.js"

dl "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" \
   "$BASE_DIR/leaflet/leaflet.css"

# -------------------------
# Font Awesome Free 6.5.2 (CSS + webfonts)
# (cdnjs = Free pack)
# -------------------------
echo "⬇️  Font Awesome (Free 6.5.2)"
dl "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" \
   "$BASE_DIR/fontawesome/css/all.min.css"

dl "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/webfonts/fa-solid-900.woff2" \
   "$BASE_DIR/fontawesome/webfonts/fa-solid-900.woff2"

dl "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/webfonts/fa-regular-400.woff2" \
   "$BASE_DIR/fontawesome/webfonts/fa-regular-400.woff2"

dl "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/webfonts/fa-brands-400.woff2" \
   "$BASE_DIR/fontawesome/webfonts/fa-brands-400.woff2"

# -------------------------
# Canvas Confetti (MIT) 1.9.2
# -------------------------
echo "⬇️  Canvas Confetti"
dl "https://cdn.jsdelivr.net/npm/canvas-confetti@1.9.2/dist/confetti.browser.min.js" \
   "$BASE_DIR/canvas-confetti/confetti.browser.min.js"

echo ""
echo "✅ Téléchargement terminé."
echo ""
echo "📊 Tailles finales :"
find "$BASE_DIR" -type f -maxdepth 4 -print0 | xargs -0 ls -lh
