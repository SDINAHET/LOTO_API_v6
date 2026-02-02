#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

echo "=== Ollama setup ==="

# Lire .env si présent
if [ -f "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  set -a
  source "$ENV_FILE"
  set +a
fi

OLLAMA_BASE="${OLLAMA_BASE:-http://127.0.0.1:11434}"
OLLAMA_MODEL="${OLLAMA_MODEL:-llama3.1:8b}"

echo "OLLAMA_BASE=$OLLAMA_BASE"
echo "OLLAMA_MODEL=$OLLAMA_MODEL"

# Dépendances système minimales
echo "📦 Installing deps..."
sudo apt-get update -y
sudo apt-get install -y curl ca-certificates

# Installer Ollama si absent
if ! command -v ollama >/dev/null 2>&1; then
  echo "⬇️ Installing Ollama..."
  curl -fsSL https://ollama.com/install.sh | sh
else
  echo "✅ Ollama déjà installé"
fi

# Démarrage : systemd si dispo, sinon background
echo "🚀 Starting Ollama..."

if command -v systemctl >/dev/null 2>&1 && systemctl list-unit-files 2>/dev/null | grep -q '^ollama\.service'; then
  # Cas systemd OK
  sudo systemctl enable ollama >/dev/null 2>&1 || true
  sudo systemctl restart ollama
else
  # Cas WSL sans systemd : lancer ollama serve en background si pas déjà là
  if pgrep -x "ollama" >/dev/null 2>&1; then
    echo "✅ Ollama tourne déjà (process)"
  else
    echo "⚠️ systemd non dispo -> lancement 'ollama serve' en background"
    nohup ollama serve >/tmp/ollama.log 2>&1 &
    sleep 2
  fi
fi

# Vérifier dispo API
echo "🔎 Checking Ollama API..."
# Ollama expose /api/tags quand il répond
if ! curl -fsS "$OLLAMA_BASE/api/tags" >/dev/null 2>&1; then
  echo "❌ Ollama ne répond pas sur $OLLAMA_BASE"
  echo "👉 Log: /tmp/ollama.log (si mode background)"
  exit 1
fi
echo "✅ Ollama API OK"

# Pull modèle (idempotent)
echo "📥 Pull model: $OLLAMA_MODEL"
ollama pull "$OLLAMA_MODEL"

echo "✅ Ollama prêt (model: $OLLAMA_MODEL)"
