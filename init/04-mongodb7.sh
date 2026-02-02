# #!/bin/bash
# set -e

# # ⚠️ MongoDB a son repo officiel
# sudo curl -fsSL https://pgp.mongodb.com/server-7.0.asc \
#   | sudo gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg

# echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu $(lsb_release -cs)/mongodb-org/7.0 multiverse" \
#   | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list >/dev/null

# sudo apt update -y
# sudo apt install -y mongodb-org

# sudo systemctl enable mongod
# sudo systemctl start mongod

#!/bin/bash
set -euo pipefail

echo "=== MongoDB 7 setup ==="

CODENAME="$(lsb_release -cs 2>/dev/null || echo unknown)"
IS_WSL=0
grep -qiE "(microsoft|wsl)" /proc/version && IS_WSL=1 || true

echo "Distro codename: $CODENAME"
echo "WSL detected: $IS_WSL"

# Helper: start service if systemd exists
start_service_if_possible() {
  local svc="$1"
  if command -v systemctl >/dev/null 2>&1 && [ "$IS_WSL" -eq 0 ]; then
    sudo systemctl enable "$svc" || true
    sudo systemctl start "$svc" || true
    sudo systemctl --no-pager status "$svc" || true
  else
    echo "ℹ️ systemctl unavailable (WSL or no systemd). Skipping service enable/start for $svc."
  fi
}

# 1) Try official MongoDB repo only if it exists for this codename
# MongoDB often lags new Ubuntu releases; noble may not exist.
REPO_URL="https://repo.mongodb.org/apt/ubuntu"
DIST_LINE="$REPO_URL ${CODENAME}/mongodb-org/7.0 multiverse"

echo "Checking MongoDB repo availability for '$CODENAME'..."
if curl -fsI "$REPO_URL/dists/$CODENAME/mongodb-org/7.0/Release" >/dev/null 2>&1; then
  echo "✅ MongoDB repo exists for $CODENAME. Installing via APT..."

  sudo curl -fsSL https://pgp.mongodb.com/server-7.0.asc \
    | sudo gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg

  echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] $DIST_LINE" \
    | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list >/dev/null

  sudo apt update -y
  sudo apt install -y mongodb-org

  start_service_if_possible mongod

  echo "✅ MongoDB installed (APT)."
  exit 0
fi

echo "⚠️ No official MongoDB repo for $CODENAME. Using Docker fallback..."

# 2) Docker fallback
if ! command -v docker >/dev/null 2>&1; then
  echo "❌ Docker not found. Install Docker first (your 06-docker.sh)."
  exit 1
fi

# Ensure docker daemon is running (on WSL systemd may exist; otherwise docker desktop handles it)
if ! docker info >/dev/null 2>&1; then
  echo "❌ Docker daemon not running."
  echo "   - If you're on WSL with Docker Desktop: start Docker Desktop on Windows."
  echo "   - If you're on Linux: sudo systemctl start docker"
  exit 1
fi

# Run container if not already running
if docker ps -a --format '{{.Names}}' | grep -qx mongo7; then
  echo "ℹ️ Container mongo7 already exists. Starting it..."
  docker start mongo7 >/dev/null
else
  echo "Starting MongoDB 7 container..."
  docker run -d --name mongo7 \
    -p 27017:27017 \
    -v mongo7_data:/data/db \
    --restart unless-stopped \
    mongo:7 >/dev/null
fi

echo "✅ MongoDB 7 is running via Docker on mongodb://localhost:27017"

# Quick check
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' | grep mongo7 || true
