#!/bin/bash
set -e

echo ""
echo "=== CHECKS ==="

command -v java >/dev/null && java -version || echo "❌ java missing"
command -v javac >/dev/null && javac -version || echo "❌ javac missing"
command -v mvn >/dev/null && mvn -version || echo "❌ mvn missing"

command -v psql >/dev/null && psql --version || echo "❌ psql missing"
command -v mongosh >/dev/null && mongosh --version || echo "⚠️ mongosh missing (souvent installé avec mongodb-org)"

sudo service postgresql status | head -n 5 || true
sudo systemctl status mongod | head -n 5 || true

echo ""
echo "=== Docker check ==="
docker --version || echo "❌ docker missing"
docker compose version || echo "❌ docker compose missing"
# IMPORTANT : après la première install Docker: FERME ET RÉOUVRE le terminal (ou reboot WSL)

echo "✅ Checks done."
