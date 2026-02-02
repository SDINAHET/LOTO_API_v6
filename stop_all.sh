#!/bin/bash

set -e

PORTS=(5500 8082 8091)

echo "=== 🔥 Nettoyage des ports : ${PORTS[*]} ==="
echo ""

for PORT in "${PORTS[@]}"; do
  echo "👉 Vérification du port $PORT"

  if sudo ss -lntp 2>/dev/null | grep -q ":$PORT"; then
    echo "⚠️ Port $PORT utilisé"

    sudo ss -lntp | grep ":$PORT" || true

    PIDS=$(sudo lsof -t -i:$PORT 2>/dev/null || true)

    if [ -n "$PIDS" ]; then
      echo "PID(s) détecté(s) : $PIDS"
      echo "Arrêt propre (SIGTERM)..."
      sudo kill $PIDS || true
      sleep 2

      if sudo ss -lntp 2>/dev/null | grep -q ":$PORT"; then
        echo "Arrêt forcé (SIGKILL)..."
        sudo kill -9 $PIDS || true
        sleep 1
      fi
    else
      echo "⚠️ Impossible de déterminer le PID (lsof manquant ?)"
    fi
  else
    echo "✅ Port $PORT déjà libre"
  fi

  # Check final
  if sudo ss -lntp 2>/dev/null | grep -q ":$PORT"; then
    echo "❌ Échec : port $PORT toujours occupé"
  else
    echo "✅ Port $PORT libéré"
  fi

  echo "--------------------------------------------"
done

echo "🎉 Tous les ports ont été traités"
