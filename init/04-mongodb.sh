#!/bin/bash
set -euo pipefail

echo "=== MongoDB 7.0 install (Ubuntu 22.04) ==="

CODENAME="$(lsb_release -cs)"
if [[ "$CODENAME" != "jammy" ]]; then
  echo "⚠️ Ce script est prévu pour Ubuntu 22.04 (jammy). Codename détecté: $CODENAME"
  echo "   (Tu peux l'adapter, mais MongoDB repo n'est pas garanti sur les autres versions.)"
fi

IS_WSL=0
grep -qiE "(microsoft|wsl)" /proc/version && IS_WSL=1 || true

echo "Distro codename: $CODENAME"
echo "WSL detected: $IS_WSL"

# 1) Clé GPG MongoDB
sudo mkdir -p /usr/share/keyrings
curl -fsSL https://pgp.mongodb.com/server-7.0.asc \
  | sudo gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg

# 2) Repo MongoDB pour Ubuntu 22.04
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" \
  | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list >/dev/null

# 3) Install
sudo apt update -y
sudo apt install -y mongodb-org

# 4) Service (si systemd dispo)
if command -v systemctl >/dev/null 2>&1 && [ "$IS_WSL" -eq 0 ]; then
  sudo systemctl enable mongod
  sudo systemctl start mongod
  sudo systemctl --no-pager status mongod || true
else
  echo "ℹ️ Pas de systemd (souvent WSL). MongoDB est installé, mais le service ne peut pas être géré via systemctl."
  echo "   Options :"
  echo "   - Lancer MongoDB manuellement: sudo mongod --config /etc/mongod.conf"
  echo "   - Ou utiliser Docker (recommandé sur WSL si tu veux un service 'toujours on')."
fi

echo "✅ MongoDB 7.0 installé."
echo "Test: mongosh --eval 'db.runCommand({ ping: 1 })'"


# Note : le méta-paquet mongodb-org installe automatiquement :
# mongodb-org-server → mongod
# mongodb-org-mongos
# mongodb-org-tools
# mongodb-mongosh
