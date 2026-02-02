#!/bin/bash
set -euo pipefail

echo "=== Test des connexions aux bases de données ==="

# Charger .env (supporte exécution depuis init/ ou racine)
ENV_FILE=""
if [ -f ".env" ]; then
  ENV_FILE=".env"
elif [ -f "../.env" ]; then
  ENV_FILE="../.env"
fi

if [ -n "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  set -a
  source "$ENV_FILE"
  set +a
  echo "✅ .env chargé: $ENV_FILE"
else
  echo "⚠️ Aucun .env trouvé (./.env ou ../.env). On continue avec l'environnement courant."
fi

# Helpers
GLOBAL_STATUS=0
print_result() {
  local label="$1"
  if [ $? -eq 0 ]; then
    echo "✅ $label - Succès"
  else
    echo "❌ $label - Échec"
    GLOBAL_STATUS=1
  fi
}

need_cmd() {
  local c="$1"
  if ! command -v "$c" >/dev/null 2>&1; then
    echo "❌ Commande manquante: $c"
    GLOBAL_STATUS=1
    return 1
  fi
  return 0
}

# Vérifier outils
need_cmd psql || true
need_cmd curl || true
# mongosh optionnel (si Mongo en Docker sans mongosh local, on peut skipper)
HAS_MONGOSH=0
if command -v mongosh >/dev/null 2>&1; then
  HAS_MONGOSH=1
else
  echo "⚠️ mongosh absent -> tests Mongo ignorés"
fi

echo -e "\n1) Test PostgreSQL"
echo "------------------"

# Variables attendues (avec defaults raisonnables)
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_DB="${POSTGRES_DB:-lotodb}"
POSTGRES_USER="${POSTGRES_USER:-postgres}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-postgres}"

# Connexion + liste tables
PGPASSWORD="$POSTGRES_PASSWORD" psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "\dt" >/dev/null 2>&1
print_result "Connexion à PostgreSQL ($POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DB)"

# Tables (si elles n'existent pas, ça doit échouer -> utile)
PGPASSWORD="$POSTGRES_PASSWORD" psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT count(*) FROM users;" >/dev/null 2>&1
print_result "Table 'users'"

PGPASSWORD="$POSTGRES_PASSWORD" psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT count(*) FROM tickets;" >/dev/null 2>&1
print_result "Table 'tickets'"

echo -e "\n2) Test MongoDB"
echo "--------------"

MONGODB_HOST="${MONGODB_HOST:-localhost}"
MONGODB_PORT="${MONGODB_PORT:-27017}"
MONGODB_DB="${MONGODB_DB:-loto}"
MONGODB_USER="${MONGODB_USER:-}"
MONGODB_PASSWORD="${MONGODB_PASSWORD:-}"
MONGODB_AUTH_DB="${MONGODB_AUTH_DB:-admin}"

if [ "$HAS_MONGOSH" -eq 1 ]; then
  # Si user/pass fournis -> auth, sinon no-auth
  if [ -n "$MONGODB_USER" ] && [ -n "$MONGODB_PASSWORD" ]; then
    mongosh --quiet \
      --host "$MONGODB_HOST" --port "$MONGODB_PORT" \
      -u "$MONGODB_USER" -p "$MONGODB_PASSWORD" --authenticationDatabase "$MONGODB_AUTH_DB" \
      --eval "db.getSiblingDB('$MONGODB_DB').runCommand({ ping: 1 })" >/dev/null 2>&1
    print_result "Connexion à MongoDB (auth) ($MONGODB_HOST:$MONGODB_PORT/$MONGODB_DB)"

    mongosh --quiet \
      --host "$MONGODB_HOST" --port "$MONGODB_PORT" \
      -u "$MONGODB_USER" -p "$MONGODB_PASSWORD" --authenticationDatabase "$MONGODB_AUTH_DB" \
      --eval "db.getSiblingDB('$MONGODB_DB').getCollectionNames()" >/dev/null 2>&1
    print_result "Accès aux collections MongoDB"
  else
    mongosh --quiet \
      --host "$MONGODB_HOST" --port "$MONGODB_PORT" \
      --eval "db.getSiblingDB('$MONGODB_DB').runCommand({ ping: 1 })" >/dev/null 2>&1
    print_result "Connexion à MongoDB (no-auth) ($MONGODB_HOST:$MONGODB_PORT/$MONGODB_DB)"

    mongosh --quiet \
      --host "$MONGODB_HOST" --port "$MONGODB_PORT" \
      --eval "db.getSiblingDB('$MONGODB_DB').getCollectionNames()" >/dev/null 2>&1
    print_result "Accès aux collections MongoDB"
  fi
fi

echo -e "\n3) Test API"
echo "----------"

API_BASE_URL="${API_BASE_URL:-http://localhost:8082}"

curl -sSf "$API_BASE_URL/actuator/health" >/dev/null 2>&1
print_result "Health Check de l'API ($API_BASE_URL/actuator/health)"

echo -e "\n=== Résultat final ==="
if [ $GLOBAL_STATUS -eq 0 ]; then
  echo "✅ Tous les tests ont réussi"
else
  echo "❌ Certains tests ont échoué"
fi

exit $GLOBAL_STATUS
