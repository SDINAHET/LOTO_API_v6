#!/bin/bash
set -e

SCRIPTS=(
  "00-env.sh"
  "01-apt-base.sh"
  # "00-free-port-5432.sh"
  "02-java-maven.sh"
  "06-docker.sh"
  "03-postgres.sh"
  "04-mongodb.sh"
  "05-db-init-sql.sh"
  "07-ollama.sh"
  "99-check.sh"
  "test_connections.sh"
)

cd "$(dirname "$0")"

echo "=== INIT SETUP (from zero) ==="

for s in "${SCRIPTS[@]}"; do
  echo ""
  echo ">>> Running: $s"

    if [ ! -f "$s" ]; then
    echo "❌ Script manquant : $s"
    exit 1
  fi

  bash "./$s"
done

echo ""
echo "✅ Setup terminé avec succès."
