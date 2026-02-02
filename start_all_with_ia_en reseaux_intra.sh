# #!/bin/bash
# set -e

# # Ports
# PORT_STATIC=5500
# PORT_SPRING=8082
# PORT_AI=8090

# # Dossiers
# STATIC_DIR="src/main/resources/static"
# AI_FILE="ai.py"              # ai.py à la racine

# ENABLE_OLLAMA=false          # true plus tard

# open_browser() {
#   local URL="$1"
#   # évite xdg-open en root (Chrome no-sandbox)
#   if [ "$(id -u)" -eq 0 ]; then
#     if command -v powershell.exe >/dev/null 2>&1; then
#       powershell.exe start "$URL" >/dev/null 2>&1 || true
#       echo "   🪟 Ouverture demandée côté Windows: $URL"
#     else
#       echo "   ⚠️ root: ouvre manuellement $URL"
#     fi
#     return
#   fi
#   if command -v xdg-open >/dev/null 2>&1; then xdg-open "$URL" >/dev/null 2>&1 || true
#   elif command -v powershell.exe >/dev/null 2>&1; then powershell.exe start "$URL" >/dev/null 2>&1 || true
#   else echo "➡️ Ouvre: $URL"; fi
# }

# start_ollama() {
#   [ "$ENABLE_OLLAMA" = "true" ] || return 0
#   echo "==> Ollama"
#   command -v ollama >/dev/null 2>&1 || { echo "   ⚠️ ollama absent (skip)"; return 0; }
#   curl -s http://localhost:11434 >/dev/null 2>&1 && { echo "   ✅ déjà actif"; return 0; }
#   nohup ollama serve >/tmp/ollama.log 2>&1 & disown
#   sleep 1
#   curl -s http://localhost:11434 >/dev/null 2>&1 && echo "   ✅ lancé" || echo "   ⚠️ voir /tmp/ollama.log"
# }

# start_ai_service() {
#   echo "==> 3bis) AI service (8090) en arrière-plan"

#   [ -f "$AI_FILE" ] || { echo "❌ $AI_FILE introuvable à la racine"; exit 1; }

#   if command -v lsof >/dev/null 2>&1 && lsof -i :"$PORT_AI" >/dev/null 2>&1; then
#     echo "   ⚠️ Port $PORT_AI déjà utilisé (skip)"
#     return 0
#   fi

#   # Lance via python (ton ai.py contient app = FastAPI(...) et les routes /health, /ai/chat)
#   # nohup python3 "$AI_FILE" >/tmp/ai_8090.log 2>&1 & disown
#   nohup python3 -m uvicorn ai:app --host 0.0.0.0 --port $PORT_AI >/tmp/ai_8090.log 2>&1 & disown

#   # Attends /health
#   for _ in {1..20}; do
#     if curl -s "http://localhost:$PORT_AI/health" >/dev/null 2>&1; then
#       echo "   ✅ AI OK: http://localhost:$PORT_AI/health"
#       return 0
#     fi
#     sleep 1
#   done

#   echo "   ⚠️ AI lancé mais /health ne répond pas (logs: /tmp/ai_8090.log)"
# }

# echo "==> 1) MongoDB"
# sudo service mongod start >/dev/null 2>&1 || sudo service mongodb start >/dev/null 2>&1 || true

# echo "==> 2) PostgreSQL"
# sudo service postgresql start >/dev/null 2>&1 || true

# # (optionnel)
# start_ollama

# echo "==> 3) Front static (5500) en arrière-plan"
# [ -d "$STATIC_DIR" ] || { echo "❌ $STATIC_DIR introuvable"; exit 1; }

# if command -v lsof >/dev/null 2>&1 && lsof -i :"$PORT_STATIC" >/dev/null 2>&1; then
#   echo "   ⚠️ Port $PORT_STATIC déjà utilisé (skip)"
# else
#   (cd "$STATIC_DIR" && nohup python3 -m http.server "$PORT_STATIC" --bind 0.0.0.0 >/tmp/static_http.log 2>&1 & disown)
#   echo "   ✅ Front: http://localhost:$PORT_STATIC/  (ai: /ai.html)"
# fi

# # démarre l’AI avant d’ouvrir le front
# start_ai_service

# echo "==> 4) Ouvre le front"
# open_browser "http://localhost:$PORT_STATIC/"
# open_browser "http://localhost:$PORT_STATIC/ai.html"

# echo "==> 5) Build Spring Boot"
# mvn clean install

# echo "==> 6) Spring Boot (au premier plan) + Swagger auto"
# (
#   while ! curl -s "http://localhost:$PORT_SPRING/swagger-ui/index.html" >/dev/null 2>&1; do
#     sleep 2
#   done
#   open_browser "http://localhost:$PORT_SPRING/swagger-ui/index.html"
# ) &

# # mvn spring-boot:run
# mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT_SPRING"


# #!/bin/bash
# set -e

# # Ports
# PORT_STATIC=5500
# PORT_SPRING=8082
# PORT_AI=8091

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

#!/usr/bin/env bash
set -euo pipefail

# =============================
# Ports
# =============================
PORT_STATIC=5500
PORT_SPRING=8082
PORT_AI=8091

STATIC_DIR="src/main/resources/static"
AI_MODULE="ai:app"         # uvicorn ai:app
AI_ENABLED=true            # ✅ AI activée

# Auto-install Python deps if missing
AUTO_PIP_INSTALL=true

# Venv (recommandé)
VENV_DIR=".venv"

# =============================
# Helpers
# =============================
log() { echo -e "$*"; }

have_cmd() { command -v "$1" >/dev/null 2>&1; }

open_browser() {
  local URL="$1"
  if have_cmd powershell.exe; then
    powershell.exe start "$URL" >/dev/null 2>&1 || true
    log "   🪟 Ouverture demandée côté Windows: $URL"
  else
    log "➡️ Ouvre: $URL"
  fi
}

# Retourne 1ère IP WSL (souvent 172.x)
get_wsl_ip() {
  hostname -I 2>/dev/null | awk '{print $1}'
}

# Essaie de trouver l’IP LAN Windows (192.168.x.x / 10.x.x.x)
# Fonctionne si powershell.exe est dispo (WSL)
get_windows_lan_ip() {
  if ! have_cmd powershell.exe; then
    return 0
  fi

  # On prend une IPv4 "up" et on privilégie 192.168.* ou 10.* (évite 172.18.* WSL/Hyper-V)
  powershell.exe -NoProfile -Command \
    "(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { \$_.IPAddress -match '^(192\.168|10\.)' -and \$_.PrefixOrigin -ne 'WellKnown' } | Select-Object -First 1 -ExpandProperty IPAddress)" \
    2>/dev/null | tr -d '\r' || true
}

# Affiche toutes les URLs utiles (localhost, WSL 172, Windows 192/10)
print_urls() {
  local WSL_IP WIN_IP
  WSL_IP="$(get_wsl_ip || true)"
  WIN_IP="$(get_windows_lan_ip || true)"

  log ""
  log "================= URLS ================="
  log "Localhost:"
  log "  Front : http://localhost:${PORT_STATIC}/"
  log "  API   : http://localhost:${PORT_SPRING}/swagger-ui/index.html"
  log "  AI    : http://localhost:${PORT_AI}/health"
  log ""

  if [[ -n "${WSL_IP}" ]]; then
    log "WSL (souvent 172.x.x.x) :"
    log "  Front : http://${WSL_IP}:${PORT_STATIC}/"
    log "  API   : http://${WSL_IP}:${PORT_SPRING}/swagger-ui/index.html"
    log "  AI    : http://${WSL_IP}:${PORT_AI}/health"
    log ""
  fi

  if [[ -n "${WIN_IP}" ]]; then
    log "Windows LAN (souvent 192.168.x.x ou 10.x.x.x) :"
    log "  Front : http://${WIN_IP}:${PORT_STATIC}/"
    log "  API   : http://${WIN_IP}:${PORT_SPRING}/swagger-ui/index.html"
    log "  AI    : http://${WIN_IP}:${PORT_AI}/health"
    log ""
    log "👉 Depuis ton téléphone (même Wi-Fi) : utilise EN PRIORITÉ ces URLs Windows."
  else
    log "⚠️ IP Windows LAN introuvable depuis WSL."
    log "   Sur Windows, prends l’IP Wi-Fi (ipconfig) ex: 192.168.1.251"
  fi

  log "========================================"
  log ""
}

ensure_apt_tools() {
  # On ne force pas sudo/apt ici, on affiche juste si manque
  local missing=()
  have_cmd curl || missing+=("curl")
  have_cmd lsof || missing+=("lsof")
  have_cmd python3 || missing+=("python3")
  have_cmd mvn || missing+=("maven")

  if (( ${#missing[@]} > 0 )); then
    log "⚠️ Outils manquants: ${missing[*]}"
    log "   Sur Debian/Ubuntu/WSL:"
    log "   sudo apt update && sudo apt install -y ${missing[*]} python3-venv"
    log ""
  fi
}

ensure_venv() {
  if [[ ! -d "${VENV_DIR}" ]]; then
    log "==> Création venv ${VENV_DIR}"
    python3 -m venv "${VENV_DIR}"
  fi
  # shellcheck disable=SC1090
  source "${VENV_DIR}/bin/activate"
  python -m pip install -U pip >/dev/null
}

python_has_imports() {
  python - <<'PY'
import sys
mods = ["fastapi", "uvicorn", "httpx", "pymongo", "dotenv"]
missing = []
for m in mods:
    try:
        __import__(m)
    except Exception:
        missing.append(m)
if missing:
    print("MISSING:", ",".join(missing))
    sys.exit(1)
print("OK")
PY
}

ensure_python_deps() {
  if [[ "${AUTO_PIP_INSTALL}" != "true" ]]; then
    return 0
  fi

  if python_has_imports >/dev/null 2>&1; then
    log "==> Python deps: OK"
    return 0
  fi

  log "==> Installation deps Python (fastapi/uvicorn/httpx/pymongo/python-dotenv)"
  python -m pip install -U "fastapi[standard]" uvicorn httpx pymongo python-dotenv
}

port_in_use() {
  local port="$1"
  lsof -i :"${port}" >/dev/null 2>&1
}

start_static() {
  log "==> Front static (${PORT_STATIC})"
  if port_in_use "${PORT_STATIC}"; then
    log "   ⚠️ Port ${PORT_STATIC} déjà utilisé (skip)"
    return 0
  fi

  if [[ ! -d "${STATIC_DIR}" ]]; then
    log "❌ STATIC_DIR introuvable: ${STATIC_DIR}"
    exit 1
  fi

  (cd "${STATIC_DIR}" && nohup python3 -m http.server "${PORT_STATIC}" --bind 0.0.0.0 \
    >/tmp/static_http_${PORT_STATIC}.log 2>&1 & disown)

  log "   ✅ Static OK (log: /tmp/static_http_${PORT_STATIC}.log)"
}

start_spring() {
  log "==> Build Spring Boot"
  mvn -q clean install

  log "==> Démarrage Spring Boot (${PORT_SPRING})"
  if port_in_use "${PORT_SPRING}"; then
    log "   ⚠️ Port ${PORT_SPRING} déjà utilisé (skip start)"
    return 0
  fi

  nohup mvn -q spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT_SPRING} --server.address=0.0.0.0" \
    >/tmp/spring_${PORT_SPRING}.log 2>&1 & disown

  log "==> Attente Spring Boot..."
  for _ in {1..60}; do
    if curl -fsS "http://localhost:${PORT_SPRING}/swagger-ui/index.html" >/dev/null 2>&1; then
      log "   ✅ Spring UP"
      return 0
    fi
    sleep 2
  done

  log "   ⚠️ Spring lancé mais swagger ne répond pas (log: /tmp/spring_${PORT_SPRING}.log)"
}

start_ai() {
  log "==> AI service (${PORT_AI})"

  if [[ "${AI_ENABLED}" != "true" ]]; then
    log "   ⏭️ AI désactivée (AI_ENABLED=false)"
    return 0
  fi

  if port_in_use "${PORT_AI}"; then
    log "   ⚠️ Port ${PORT_AI} déjà utilisé (skip)"
    return 0
  fi

  # venv + deps
  ensure_venv
  ensure_python_deps

  nohup python -m uvicorn "${AI_MODULE}" --host 0.0.0.0 --port "${PORT_AI}" \
    >/tmp/ai_${PORT_AI}.log 2>&1 & disown

  for _ in {1..30}; do
    if curl -fsS "http://localhost:${PORT_AI}/health" >/dev/null 2>&1; then
      log "   ✅ AI OK (log: /tmp/ai_${PORT_AI}.log)"
      return 0
    fi
    sleep 1
  done

  log "   ⚠️ AI lancé mais /health ne répond pas (log: /tmp/ai_${PORT_AI}.log)"
}

# =============================
# Main
# =============================
ensure_apt_tools

log "==> MongoDB"
sudo service mongod start >/dev/null 2>&1 || true

log "==> PostgreSQL"
sudo service postgresql start >/dev/null 2>&1 || true

start_static
start_spring
start_ai

print_urls

# Optionnel: ouvrir en local
open_browser "http://localhost:${PORT_STATIC}/"
open_browser "http://localhost:${PORT_SPRING}/swagger-ui/index.html"
open_browser "http://localhost:${PORT_AI}/health"

log "==> Services lancés. (CTRL+C ne stoppera pas les nohup)."
log "    Logs:"
log "    - /tmp/static_http_${PORT_STATIC}.log"
log "    - /tmp/spring_${PORT_SPRING}.log"
log "    - /tmp/ai_${PORT_AI}.log"

# Keep script alive if you want (optional)
wait
