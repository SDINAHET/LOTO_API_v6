#!/usr/bin/env bash
set -euo pipefail

#APP_DIR="/root/Loto_API_prod"
APP_DIR="/root/Loto_API_prod/src/main/resources/static"
PORT="5500"
LOG_DIR="/var/log/lototracker-front"
LOG_FILE="$LOG_DIR/http-server.log"
PID_FILE="/run/lototracker-front.pid"

mkdir -p "$LOG_DIR"

# Stop propre si déjà lancé
if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  kill "$(cat "$PID_FILE")" || true
  sleep 1
fi

# Si le port est encore pris, on tue
if lsof -ti tcp:"$PORT" >/dev/null 2>&1; then
  lsof -ti tcp:"$PORT" | xargs -r kill -9
  sleep 1
fi

cd "$APP_DIR"

# Lance http-server en background + log
nohup npx http-server ./ -a 127.0.0.1 -p "$PORT" -c 3600 \
  >> "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"
echo "Started http-server PID=$(cat "$PID_FILE") on 127.0.0.1:$PORT"
