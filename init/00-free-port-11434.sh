#!/bin/bash
set -e

PORT=11434

echo "=== Vérification du port $PORT ==="

# Liste des processus qui écoutent sur le port
if sudo ss -lntp 2>/dev/null | grep -q ":$PORT"; then
  echo "⚠️ Le port $PORT est utilisé :"
  sudo ss -lntp | grep ":$PORT" || true

  # Récupérer les PID
  PIDS=$(sudo lsof -t -i:$PORT 2>/dev/null || true)

  if [ -n "$PIDS" ]; then
    echo ""
    echo "PID(s) détecté(s) sur le port $PORT : $PIDS"
    echo "Tentative d'arrêt propre (SIGTERM)..."
    sudo kill $PIDS || true
    sleep 2

    # Si encore occupé → kill -9
    if sudo ss -lntp 2>/dev/null | grep -q ":$PORT"; then
      echo "⚠️ Le port est encore occupé, arrêt forcé (SIGKILL)..."
      sudo kill -9 $PIDS || true
      sleep 1
    fi
  else
    echo "⚠️ Impossible de déterminer le PID automatiquement (lsof manquant ?)"
    echo "👉 Installe lsof : sudo apt install -y lsof"
  fi
else
  echo "✅ Port $PORT libre"
fi

# Vérification finale
if sudo ss -lntp 2>/dev/null | grep -q ":$PORT"; then
  echo "❌ Port $PORT toujours occupé. Détails :"
  sudo lsof -i:$PORT 2>/dev/null || true
  exit 1
fi

echo "✅ Port $PORT libéré avec succès"
