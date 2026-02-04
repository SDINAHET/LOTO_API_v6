# # #!/bin/bash
# # set -e

# # # Ports
# # PORT_STATIC=5500
# # PORT_SPRING=8082
# # PORT_AI=8090

# # # Dossiers
# # STATIC_DIR="src/main/resources/static"
# # AI_FILE="ai.py"              # ai.py à la racine

# # ENABLE_OLLAMA=false          # true plus tard

# # open_browser() {
# #   local URL="$1"
# #   # évite xdg-open en root (Chrome no-sandbox)
# #   if [ "$(id -u)" -eq 0 ]; then
# #     if command -v powershell.exe >/dev/null 2>&1; then
# #       powershell.exe start "$URL" >/dev/null 2>&1 || true
# #       echo "   🪟 Ouverture demandée côté Windows: $URL"
# #     else
# #       echo "   ⚠️ root: ouvre manuellement $URL"
# #     fi
# #     return
# #   fi
# #   if command -v xdg-open >/dev/null 2>&1; then xdg-open "$URL" >/dev/null 2>&1 || true
# #   elif command -v powershell.exe >/dev/null 2>&1; then powershell.exe start "$URL" >/dev/null 2>&1 || true
# #   else echo "➡️ Ouvre: $URL"; fi
# # }

# # start_ollama() {
# #   [ "$ENABLE_OLLAMA" = "true" ] || return 0
# #   echo "==> Ollama"
# #   command -v ollama >/dev/null 2>&1 || { echo "   ⚠️ ollama absent (skip)"; return 0; }
# #   curl -s http://localhost:11434 >/dev/null 2>&1 && { echo "   ✅ déjà actif"; return 0; }
# #   nohup ollama serve >/tmp/ollama.log 2>&1 & disown
# #   sleep 1
# #   curl -s http://localhost:11434 >/dev/null 2>&1 && echo "   ✅ lancé" || echo "   ⚠️ voir /tmp/ollama.log"
# # }

# # start_ai_service() {
# #   echo "==> 3bis) AI service (8090) en arrière-plan"

# #   [ -f "$AI_FILE" ] || { echo "❌ $AI_FILE introuvable à la racine"; exit 1; }

# #   if command -v lsof >/dev/null 2>&1 && lsof -i :"$PORT_AI" >/dev/null 2>&1; then
# #     echo "   ⚠️ Port $PORT_AI déjà utilisé (skip)"
# #     return 0
# #   fi

# #   # Lance via python (ton ai.py contient app = FastAPI(...) et les routes /health, /ai/chat)
# #   # nohup python3 "$AI_FILE" >/tmp/ai_8090.log 2>&1 & disown
# #   nohup python3 -m uvicorn ai:app --host 0.0.0.0 --port $PORT_AI >/tmp/ai_8090.log 2>&1 & disown

# #   # Attends /health
# #   for _ in {1..20}; do
# #     if curl -s "http://localhost:$PORT_AI/health" >/dev/null 2>&1; then
# #       echo "   ✅ AI OK: http://localhost:$PORT_AI/health"
# #       return 0
# #     fi
# #     sleep 1
# #   done

# #   echo "   ⚠️ AI lancé mais /health ne répond pas (logs: /tmp/ai_8090.log)"
# # }

# # echo "==> 1) MongoDB"
# # sudo service mongod start >/dev/null 2>&1 || sudo service mongodb start >/dev/null 2>&1 || true

# # echo "==> 2) PostgreSQL"
# # sudo service postgresql start >/dev/null 2>&1 || true

# # # (optionnel)
# # start_ollama

# # echo "==> 3) Front static (5500) en arrière-plan"
# # [ -d "$STATIC_DIR" ] || { echo "❌ $STATIC_DIR introuvable"; exit 1; }

# # if command -v lsof >/dev/null 2>&1 && lsof -i :"$PORT_STATIC" >/dev/null 2>&1; then
# #   echo "   ⚠️ Port $PORT_STATIC déjà utilisé (skip)"
# # else
# #   (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" --bind 0.0.0.0 >/tmp/static_http.log 2>&1 & disown)
# #   echo "   ✅ Front: http://localhost:$PORT_STATIC/  (ai: /ai.html)"
# # fi

# # # démarre l’AI avant d’ouvrir le front
# # start_ai_service

# # echo "==> 4) Ouvre le front"
# # open_browser "http://localhost:$PORT_STATIC/"
# # open_browser "http://localhost:$PORT_STATIC/ai.html"

# # echo "==> 5) Build Spring Boot"
# # mvn clean install

# # echo "==> 6) Spring Boot (au premier plan) + Swagger auto"
# # (
# #   while ! curl -s "http://localhost:$PORT_SPRING/swagger-ui/index.html" >/dev/null 2>&1; do
# #     sleep 2
# #   done
# #   open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"
# # ) &

# # # mvn spring-boot:run
# # mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT_SPRING"


# #!/bin/bash
# set -e

# # Ports
# PORT_STATIC=5500
# PORT_SPRING=8082
# PORT_AI=8090

# STATIC_DIR="src/main/resources/static"
# AI_FILE="ai.py"

# open_browser() {
#   local URL="$1"
#   if command -v powershell.exe >/dev/null 2>&1; then
#     powershell.exe start "$URL" >/dev/null 2>&1 || true
#     echo "   🪟 Ouverture demandée côté Windows: $URL"
#   else
#     echo "➡️ Ouvre: $URL"
#   fi
# }

# start_ai_service() {
#   echo "==> AI service (8090)"

#   if lsof -i :"$PORT_AI" >/dev/null 2>&1; then
#     echo "   ⚠️ Port $PORT_AI déjà utilisé (skip)"
#     return
#   fi

#   nohup python3 -m uvicorn ai:app --host 0.0.0.0 --port "$PORT_AI" \
#     >/tmp/ai_8090.log 2>&1 & disown

#   for _ in {1..20}; do
#     if curl -s "http://localhost:$PORT_AI/health" >/dev/null 2>&1; then
#       echo "   ✅ AI OK"
#       return
#     fi
#     sleep 1
#   done

#   echo "   ⚠️ AI lancé mais /health ne répond pas"
# }

# echo "==> MongoDB"
# sudo service mongod start >/dev/null 2>&1 || true

# echo "==> PostgreSQL"
# sudo service postgresql start >/dev/null 2>&1 || true

# echo "==> Front static (5500)"
# if ! lsof -i :"$PORT_STATIC" >/dev/null 2>&1; then
#   (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" \
#     >/tmp/static_http.log 2>&1 & disown)
# fi

# open_browser "http://localhost:$PORT_STATIC/"

# echo "==> Build Spring Boot"
# mvn clean install

# echo "==> Démarrage Spring Boot (premier plan)"
# mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT_SPRING" &

# echo "==> Attente Spring Boot..."
# for _ in {1..60}; do
#   if curl -s "http://localhost:$PORT_SPRING/swagger-ui/index.html" >/dev/null 2>&1; then
#     echo "   ✅ Spring UP"
#     break
#   fi
#   sleep 2
# done

# open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"

# start_ai_service

# # Spring reste vivant tant que le script tourne
# wait

#!/bin/bash
set -e

# Ports
PORT_STATIC=5500
PORT_SPRING=8082
PORT_AI=8091

STATIC_DIR="src/main/resources/static"
AI_FILE="ai.py"

# ✅ Mets true seulement quand tu veux lancer l'AI
AI_ENABLED=false

open_browser() {
  local URL="$1"
  if command -v powershell.exe >/dev/null 2>&1; then
    powershell.exe start "$URL" >/dev/null 2>&1 || true
    echo "   🪟 Ouverture demandée côté Windows: $URL"
  else
    echo "➡️ Ouvre: $URL"
  fi
}

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
  return 0
}

echo "==> MongoDB"
sudo service mongod start >/dev/null 2>&1 || true

echo "==> PostgreSQL"
sudo service postgresql start >/dev/null 2>&1 || true

echo "==> Front static (5500)"
if ! lsof -i :"$PORT_STATIC" >/dev/null 2>&1; then
  (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" \
    >/tmp/static_http.log 2>&1 & disown)
fi
# open_browser "http://localhost:$PORT_STATIC/"

echo "==> Build Spring Boot"
mvn clean install

echo "==> Démarrage Spring Boot"
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT_SPRING" &

echo "==> Attente Spring Boot..."
for _ in {1..60}; do
  if curl -s "http://localhost:$PORT_SPRING/swagger-ui/index.html" >/dev/null 2>&1; then
    echo "   ✅ Spring UP"
    break
  fi
  sleep 2
done
open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"

# ✅ AI: ne démarre que si AI_ENABLED=true
start_ai_service

echo "==> Refresh du front quand Spring est prêt"
open_browser "http://localhost:$PORT_STATIC/"

wait

# #!/bin/bash
# set -e

# # Ports
# PORT_STATIC=5500
# PORT_SPRING=8082
# PORT_AI=8091

# STATIC_DIR="src/main/resources/static"
# AI_ENABLED=false

# # Récupère une IP LAN (utile pour afficher l'URL aux autres)
# LAN_IP=$(hostname -I 2>/dev/null | awk '{print $1}')

# open_browser() {
#   local URL="$1"
#   if command -v powershell.exe >/dev/null 2>&1; then
#     powershell.exe start "$URL" >/dev/null 2>&1 || true
#     echo "   🪟 Ouverture demandée côté Windows: $URL"
#   else
#     echo "➡️ Ouvre: $URL"
#   fi
# }

# start_ai_service() {
#   echo "==> AI service ($PORT_AI)"

#   if [ "$AI_ENABLED" != "true" ]; then
#     echo "   ⏭️ AI désactivée (AI_ENABLED=false)"
#     return 0
#   fi

#   if lsof -i :"$PORT_AI" >/dev/null 2>&1; then
#     echo "   ⚠️ Port $PORT_AI déjà utilisé (skip)"
#     return 0
#   fi

#   nohup python3 -m uvicorn ai:app --host 0.0.0.0 --port "$PORT_AI" \
#     >/tmp/ai_${PORT_AI}.log 2>&1 & disown

#   for _ in {1..20}; do
#     if curl -s "http://localhost:$PORT_AI/health" >/dev/null 2>&1; then
#       echo "   ✅ AI OK"
#       return 0
#     fi
#     sleep 1
#   done

#   echo "   ⚠️ AI lancé mais /health ne répond pas"
#   return 0
# }

# echo "==> MongoDB"
# sudo service mongod start >/dev/null 2>&1 || true

# echo "==> PostgreSQL"
# sudo service postgresql start >/dev/null 2>&1 || true

# echo "==> Front static ($PORT_STATIC)"
# if ! lsof -i :"$PORT_STATIC" >/dev/null 2>&1; then
#   (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" --bind 0.0.0.0 \
#     >/tmp/static_http.log 2>&1 & disown)
# fi

# echo "==> Build Spring Boot"
# mvn clean install

# echo "==> Démarrage Spring Boot ($PORT_SPRING)"
# mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT_SPRING --server.address=0.0.0.0" &

# echo "==> Attente Spring Boot..."
# for _ in {1..60}; do
#   if curl -s "http://localhost:$PORT_SPRING/swagger-ui/index.html" >/dev/null 2>&1; then
#     echo "   ✅ Spring UP"
#     break
#   fi
#   sleep 2
# done

# # Affichage URLs intranet
# echo ""
# echo "=== URLS INTRANET ==="
# echo "Front :  http://${LAN_IP:-<ton-ip>}:$PORT_STATIC/"
# echo "API   :  http://${LAN_IP:-<ton-ip>}:$PORT_SPRING/swagger-ui/index.html"
# if [ "$AI_ENABLED" = "true" ]; then
#   echo "AI    :  http://${LAN_IP:-<ton-ip>}:$PORT_AI/health"
# fi
# echo "====================="
# echo ""

# # Optionnel : ouvrir côté machine locale
# open_browser "http://localhost:$PORT_STATIC/"
# open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"

# start_ai_service
# wait

# #!/bin/bash
# set -e

# # =====================
# # Ports
# # =====================
# PORT_STATIC=5500
# PORT_SPRING=8082
# PORT_AI=8091

# STATIC_DIR="src/main/resources/static"
# AI_ENABLED=false

# # =====================
# # Détection IPs
# # =====================

# # IP WSL (172.x.x.x)
# WSL_IP=$(hostname -I 2>/dev/null | awk '{print $1}')

# # IP LAN Windows (192.168.x.x) via PowerShell
# LAN_IP=$(powershell.exe -NoProfile -Command \
#   "(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { \$_.IPAddress -like '192.168.*' } | Select-Object -First 1 -ExpandProperty IPAddress)" \
#   2>/dev/null | tr -d '\r')

# # Fallback si PowerShell échoue
# LAN_IP=${LAN_IP:-"<ip-lan>"}

# # =====================
# # Fonctions
# # =====================

# open_browser() {
#   local URL="$1"
#   if command -v powershell.exe >/dev/null 2>&1; then
#     powershell.exe start "$URL" >/dev/null 2>&1 || true
#   else
#     echo "➡️ Ouvre: $URL"
#   fi
# }

# start_ai_service() {
#   echo "==> AI service ($PORT_AI)"

#   if [ "$AI_ENABLED" != "true" ]; then
#     echo "   ⏭️ AI désactivée (AI_ENABLED=false)"
#     return 0
#   fi

#   if lsof -i :"$PORT_AI" >/dev/null 2>&1; then
#     echo "   ⚠️ Port $PORT_AI déjà utilisé"
#     return 0
#   fi

#   nohup python3 -m uvicorn ai:app --host 0.0.0.0 --port "$PORT_AI" \
#     >/tmp/ai_${PORT_AI}.log 2>&1 & disown

#   for _ in {1..20}; do
#     if curl -s "http://localhost:$PORT_AI/health" >/dev/null 2>&1; then
#       echo "   ✅ AI OK"
#       return 0
#     fi
#     sleep 1
#   done

#   echo "   ⚠️ AI lancé mais /health ne répond pas"
# }

# # =====================
# # Services
# # =====================

# echo "==> MongoDB"
# sudo service mongod start >/dev/null 2>&1 || true

# echo "==> PostgreSQL"
# sudo service postgresql start >/dev/null 2>&1 || true

# # =====================
# # Front static
# # =====================

# echo "==> Front static ($PORT_STATIC)"
# if ! lsof -i :"$PORT_STATIC" >/dev/null 2>&1; then
#   (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" --bind 0.0.0.0 \
#     >/tmp/static_http.log 2>&1 & disown)
# fi

# # =====================
# # Spring Boot
# # =====================

# echo "==> Build Spring Boot"
# mvn clean install

# echo "==> Démarrage Spring Boot ($PORT_SPRING)"
# mvn spring-boot:run \
#   -Dspring-boot.run.arguments="--server.port=$PORT_SPRING --server.address=0.0.0.0" &

# echo "==> Attente Spring Boot..."
# for _ in {1..60}; do
#   if curl -s "http://localhost:$PORT_SPRING/swagger-ui/index.html" >/dev/null 2>&1; then
#     echo "   ✅ Spring UP"
#     break
#   fi
#   sleep 2
# done

# # =====================
# # AFFICHAGE DES URLS
# # =====================

# echo ""
# echo "================= ACCÈS DISPONIBLES ================="
# echo ""
# echo "🖥️  LOCAL (PC uniquement)"
# echo "   Front : http://localhost:$PORT_STATIC/"
# echo "   API   : http://localhost:$PORT_SPRING/swagger-ui/index.html"
# if [ "$AI_ENABLED" = "true" ]; then
#   echo "   AI    : http://localhost:$PORT_AI/health"
# fi

# echo ""
# echo "🐧 WSL (réseau interne)"
# echo "   Front : http://$WSL_IP:$PORT_STATIC/"
# echo "   API   : http://$WSL_IP:$PORT_SPRING/swagger-ui/index.html"
# if [ "$AI_ENABLED" = "true" ]; then
#   echo "   AI    : http://$WSL_IP:$PORT_AI/health"
# fi

# echo ""
# echo "📱 INTRANET (Téléphone / autres PC)"
# echo "   Front : http://$LAN_IP:$PORT_STATIC/"
# echo "   API   : http://$LAN_IP:$PORT_SPRING/swagger-ui/index.html"
# if [ "$AI_ENABLED" = "true" ]; then
#   echo "   AI    : http://$LAN_IP:$PORT_AI/health"
# fi

# echo "====================================================="
# echo ""

# # =====================
# # Ouvrir localement
# # =====================

# open_browser "http://localhost:$PORT_STATIC/"
# open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"

# start_ai_service
# wait
