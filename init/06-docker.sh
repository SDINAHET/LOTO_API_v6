#!/bin/bash
set -euo pipefail

echo "=== Installation Docker ==="

# Supprimer anciennes versions si présentes (ok si déjà clean)
sudo apt-get remove -y docker docker-engine docker.io containerd runc >/dev/null 2>&1 || true

# Dépendances
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# Keyrings dir
sudo install -m 0755 -d /etc/apt/keyrings

# Clé GPG Docker (NE PAS réécrire si déjà présente -> évite le prompt)
if [ ! -f /etc/apt/keyrings/docker.gpg ]; then
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg
  echo "✅ Docker GPG key installed"
else
  echo "✅ Docker GPG key already exists"
fi

# Dépôt Docker (écrasé proprement, pas interactif)
CODENAME="$(lsb_release -cs)"
ARCH="$(dpkg --print-architecture)"

sudo tee /etc/apt/sources.list.d/docker.list >/dev/null <<EOF
deb [arch=${ARCH} signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu ${CODENAME} stable
EOF

# Installation Docker
sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Démarrage Docker (systemd si dispo)
if command -v systemctl >/dev/null 2>&1; then
  sudo systemctl enable docker >/dev/null 2>&1 || true
  sudo systemctl restart docker || sudo systemctl start docker
  echo "✅ Docker service started (systemd)"
else
  echo "⚠️ systemd non dispo (WSL) -> Docker daemon géré par Docker Desktop (Windows)"
fi

# Ajouter l’utilisateur courant au groupe docker (si besoin)
if ! groups "$USER" | grep -q '\bdocker\b'; then
  sudo usermod -aG docker "$USER"
  echo "ℹ️ Utilisateur ajouté au groupe docker (déconnexion/reconnexion requise)"
fi

echo "✅ Docker installé"
