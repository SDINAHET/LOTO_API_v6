#!/bin/bash
set -e

# ========================
# CONFIG
# ========================
PORT_STATIC=5500
PORT_SPRING=8082
PORT_AI=8091

STATIC_DIR="src/main/resources/static"
AI_MODULE="ai:app"     # uvicorn ai:app
AI_ENABLED=true        # 🔥 active / désactive l’IA ici

# ========================
# HELPERS
# ========================
open_browser() {
  local URL="$1"
  if command -v powershell.exe >/dev/null 2>&1; then
    powershell.exe start "$URL" >/dev/null 2>&1 || true
    echo "   🪟 Ouverture demandée côté Windows: $URL"
  else
    echo "➡️ Ouvre: $URL"
  fi
}

port_used() {
  lsof -i :"$1" >/dev/null 2>&1
}

# ========================
# AI SERVICE
# ========================
start_ai_service() {
  echo "==> AI service (port $PORT_AI)"

  [ "$AI_ENABLED" = "true" ] || {
    echo "   ⏭️ AI désactivée (AI_ENABLED=false)"
    return 0
  }

  port_used "$PORT_AI" && {
    echo "   ⚠️ Port $PORT_AI déjà utilisé (skip)"
    return 0
  }

  # Active le venv si présent
  [ -d ".venv" ] && source .venv/bin/activate

  nohup python3 -m uvicorn "$AI_MODULE" \
    --host 0.0.0.0 --port "$PORT_AI" \
    >/tmp/ai_${PORT_AI}.log 2>&1 & disown

  for _ in {1..20}; do
    if curl -s "http://localhost:$PORT_AI/health" >/dev/null 2>&1; then
      echo "   ✅ AI UP : http://localhost:$PORT_AI/health"
      return 0
    fi
    sleep 1
  done

  echo "   ⚠️ AI lancée mais /health ne répond pas"
  echo "   📄 Logs : /tmp/ai_${PORT_AI}.log"
}

# ========================
# SERVICES SYSTEM
# ========================
echo "==> MongoDB"
sudo service mongod start >/dev/null 2>&1 || true

echo "==> PostgreSQL"
sudo service postgresql start >/dev/null 2>&1 || true

# ========================
# FRONT STATIC
# ========================
echo "==> Front static (5500)"
if ! port_used "$PORT_STATIC"; then
  (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" \
    >/tmp/static_http.log 2>&1 & disown)
fi

# ========================
# SPRING BOOT
# ========================
echo "==> Build Spring Boot"
mvn clean install

echo "==> Démarrage Spring Boot (8082)"
nohup mvn spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=$PORT_SPRING" \
  >/tmp/spring_${PORT_SPRING}.log 2>&1 & disown

echo "==> Attente Spring Boot..."
for _ in {1..60}; do
  if curl -s "http://localhost:$PORT_SPRING/actuator/health" >/dev/null 2>&1; then
    echo "   ✅ Spring UP"
    break
  fi
  sleep 2
done

# ========================
# AI (EN PLUS)
# ========================
start_ai_service

# ========================
# OPEN BROWSER
# ========================
open_browser "http://localhost:$PORT_STATIC/index.html"
# open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"

echo ""
echo "==> 🚀 Stack complète prête"
echo "   Front  : http://localhost:5500"
echo "   API    : http://localhost:8082"
echo "   Swagger: http://localhost:8082/swagger-ui/index.html"
echo "   AI     : http://localhost:8091/health"
