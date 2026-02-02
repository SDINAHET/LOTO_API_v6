#!/bin/bash
set -euo pipefail

DB_NAME="${POSTGRES_DB:-lotodb}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_PASS="${POSTGRES_PASSWORD:-postgres}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SCHEMA_FILE="${SCRIPT_DIR}/sql/schema_lotodb.sql"
DATA_DIR="${SCRIPT_DIR}/data"

USERS_CSV="${DATA_DIR}/users_export.csv"
TICKETS_CSV="${DATA_DIR}/tickets_export.csv"
GAINS_CSV="${DATA_DIR}/ticket_gains_export.csv"

echo "=== PostgreSQL init: schema + data ==="
sudo service postgresql start >/dev/null

# Vérifs fichiers
for f in "$USERS_CSV" "$TICKETS_CSV" "$GAINS_CSV"; do
  if [ ! -f "$f" ]; then
    echo "❌ Fichier manquant: $f"
    exit 1
  fi
done

# Nettoyage CSV (CRLF -> LF) + suppression d'un éventuel ';' final
sed -i 's/\r$//' "$USERS_CSV" "$TICKETS_CSV" "$GAINS_CSV"
sed -i 's/;$//'  "$USERS_CSV" "$TICKETS_CSV" "$GAINS_CSV"

# Vérifie que USERS a 8 champs par ligne (séparateur ;)
BAD_LINES="$(awk -F';' 'NF!=8{print NR":"NF":"$0}' "$USERS_CSV" | head -n 5 || true)"
if [ -n "${BAD_LINES:-}" ]; then
  echo "❌ CSV users invalide (attendu 8 colonnes). Exemples :"
  echo "$BAD_LINES"
  exit 1
fi

# Créer/maj role (idempotent)
sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USER}') THEN
    CREATE ROLE ${DB_USER} LOGIN PASSWORD '${DB_PASS}';
  ELSE
    ALTER ROLE ${DB_USER} WITH LOGIN PASSWORD '${DB_PASS}';
  END IF;
END
\$\$;
SQL

# Créer DB (CREATE DATABASE interdit dans DO)
if ! sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" | grep -q 1; then
  echo "➕ Creating database ${DB_NAME}..."
  sudo -u postgres createdb -O "${DB_USER}" "${DB_NAME}"
else
  echo "✅ Database ${DB_NAME} already exists"
fi

echo "✅ DB=$DB_NAME USER=$DB_USER"

# Appliquer schéma si présent
if [ -f "$SCHEMA_FILE" ]; then
  echo "🚀 Application du schéma: $SCHEMA_FILE"
  sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -f "$SCHEMA_FILE"
fi

# Garantit les tables minimales
echo "🧱 Ensure required tables exist..."
sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;

CREATE TABLE IF NOT EXISTS public.users (
  id text PRIMARY KEY,
  first_name text NOT NULL,
  last_name text NOT NULL,
  email text NOT NULL UNIQUE,
  password text NOT NULL,
  is_admin boolean DEFAULT false NOT NULL,
  created_at text,
  updated_at text
);

CREATE TABLE IF NOT EXISTS public.tickets (
  id text PRIMARY KEY,
  numbers text NOT NULL,
  lucky_number integer NOT NULL,
  draw_date text NOT NULL,
  draw_day text,
  created_at text NOT NULL,
  updated_at text,
  user_id text NOT NULL
);

CREATE TABLE IF NOT EXISTS public.ticket_gains (
  id text PRIMARY KEY,
  ticket_id text NOT NULL,
  matching_numbers integer NOT NULL,
  lucky_number_match boolean NOT NULL,
  gain_amount real NOT NULL
);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ticket_gains_ticket_id_fkey') THEN
    ALTER TABLE public.ticket_gains
      ADD CONSTRAINT ticket_gains_ticket_id_fkey
      FOREIGN KEY (ticket_id) REFERENCES public.tickets(id)
      ON UPDATE CASCADE ON DELETE CASCADE;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='tickets_user_id_fkey') THEN
    ALTER TABLE public.tickets
      ADD CONSTRAINT tickets_user_id_fkey
      FOREIGN KEY (user_id) REFERENCES public.users(id)
      ON UPDATE CASCADE ON DELETE CASCADE;
  END IF;
END $$;

COMMIT;
SQL

# Reset tables
echo "🧹 TRUNCATE tables (rejouable)..."
sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"TRUNCATE TABLE public.ticket_gains, public.tickets, public.users RESTART IDENTITY CASCADE;"

# --- IMPORT USERS (staging + conversion 0/1 -> boolean) ---
echo "📥 Import users..."

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;
DROP TABLE IF EXISTS public.users_staging;
CREATE TABLE public.users_staging (
  id text,
  first_name text,
  last_name text,
  email text,
  password text,
  is_admin_raw text,
  created_at text,
  updated_at text
);
COMMIT;
SQL

# IMPORTANT : \copy via -c (évite le parse error du heredoc)
sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"\copy public.users_staging(id, first_name, last_name, email, password, is_admin_raw, created_at, updated_at) FROM '${USERS_CSV}' WITH (FORMAT csv, DELIMITER ';', QUOTE '\"', ESCAPE '\"');"

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;
INSERT INTO public.users(id, first_name, last_name, email, password, is_admin, created_at, updated_at)
SELECT
  id, first_name, last_name, email, password,
  CASE
    WHEN lower(is_admin_raw) IN ('1','t','true','yes','y') THEN true
    ELSE false
  END,
  created_at, updated_at
FROM public.users_staging;

DROP TABLE public.users_staging;
COMMIT;
SQL

# --- IMPORT TICKETS ---
echo "📥 Import tickets..."
sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"\copy public.tickets(id, numbers, lucky_number, draw_date, draw_day, created_at, updated_at, user_id) FROM '${TICKETS_CSV}' WITH (FORMAT csv, DELIMITER ';', QUOTE '\"', ESCAPE '\"');"

# --- IMPORT GAINS ---
echo "📥 Import ticket_gains..."
sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"\copy public.ticket_gains(id, ticket_id, matching_numbers, lucky_number_match, gain_amount) FROM '${GAINS_CSV}' WITH (FORMAT csv, DELIMITER ';', QUOTE '\"', ESCAPE '\"');"

echo "✅ Vérification:"
sudo -u postgres psql -d "$DB_NAME" -c \
"SELECT 'users' t, count(*) FROM public.users
 UNION ALL SELECT 'tickets', count(*) FROM public.tickets
 UNION ALL SELECT 'ticket_gains', count(*) FROM public.ticket_gains;"

echo "✅ Init PostgreSQL terminé."
