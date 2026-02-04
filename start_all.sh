#!/bin/bash
set -e

# ========================
# CONFIG
# ========================
PORT_STATIC=5500
PORT_SPRING=8082
PORT_AI=8091

STATIC_DIR="src/main/resources/static"

# ✅ Mets true seulement quand tu veux lancer l'AI
AI_ENABLED=false

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

# ========================
# AI SERVICE
# ========================
start_ai_service() {
  echo "==> AI service (8091)"

  if [ "$AI_ENABLED" != "true" ]; then
    echo "   ⏭️ AI désactivée (AI_ENABLED=false)"
    return 0
  fi

  if lsof -i :"$PORT_AI" >/dev/null 2>&1; then
    echo "   ⚠️ Port $PORT_AI déjà utilisé (skip)"
    return 0
  fi

  nohup python3 -m uvicorn ai:app --host 0.0.0.0 --port "$PORT_AI" \
    >/tmp/ai_8091.log 2>&1 & disown

  for _ in {1..20}; do
    if curl -s "http://localhost:$PORT_AI/health" >/dev/null 2>&1; then
      echo "   ✅ AI OK"
      return 0
    fi
    sleep 1
  done

  echo "   ⚠️ AI lancé mais /health ne répond pas"
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
if ! lsof -i :"$PORT_STATIC" >/dev/null 2>&1; then
  (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" \
    >/tmp/static_http.log 2>&1 & disown)
  sleep 1
fi



# ========================
# SPRING BOOT
# ========================
echo "==> Build Spring Boot"
mvn clean install

echo "==> Démarrage Spring Boot"
nohup mvn spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=$PORT_SPRING" \
  >/tmp/spring_8082.log 2>&1 & disown

echo "==> Attente Spring Boot..."
for _ in {1..60}; do
  if curl -s "http://localhost:$PORT_SPRING/actuator/health" >/dev/null 2>&1; then
    echo "   ✅ Spring UP"
    break
  fi
  sleep 2
done

# open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"
open_browser "http://localhost:$PORT_STATIC/admin-login.html"
open_browser "http://localhost:$PORT_STATIC/index.html"

# ========================
# AI (optionnelle)
# ========================
start_ai_service

echo "==> 🚀 Stack complète prête"
echo "   Front  : http://localhost:5500/admin-login.html"
echo "   Admin  : http://localhost:5500/admin/dashboard.html"
echo "   API    : http://localhost:8082"
echo "   Swagger: http://localhost:8082/swagger-ui/index.html"
