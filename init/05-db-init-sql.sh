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

# Vérifie que USERS a 8 champs par ligne (séparateur ;).
# (id;first_name;last_name;email;password;is_admin;created_at;updated_at)
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

# Garantit les tables minimales (alignées avec ton schema final)
echo "🧱 Ensure required tables exist..."
sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;

-- USERS
CREATE TABLE IF NOT EXISTS public.users (
  id TEXT PRIMARY KEY,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  is_admin BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ
);

-- TICKETS
CREATE TABLE IF NOT EXISTS public.tickets (
  id TEXT PRIMARY KEY,
  numbers TEXT NOT NULL,
  lucky_number INTEGER NOT NULL,
  draw_date DATE NOT NULL,
  draw_day TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ,
  user_id TEXT NOT NULL
);

-- TICKET_GAINS
CREATE TABLE IF NOT EXISTS public.ticket_gains (
  id TEXT PRIMARY KEY,
  ticket_id TEXT NOT NULL,
  matching_numbers INTEGER NOT NULL,
  lucky_number_match BOOLEAN NOT NULL,
  gain_amount NUMERIC(10,2) NOT NULL
);

-- REFRESH_TOKENS
CREATE TABLE IF NOT EXISTS public.refresh_tokens (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  token_hash TEXT NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ
);

-- Foreign keys (idempotent)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='tickets_user_id_fkey') THEN
    ALTER TABLE public.tickets
      ADD CONSTRAINT tickets_user_id_fkey
      FOREIGN KEY (user_id) REFERENCES public.users(id)
      ON UPDATE CASCADE ON DELETE CASCADE;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ticket_gains_ticket_id_fkey') THEN
    ALTER TABLE public.ticket_gains
      ADD CONSTRAINT ticket_gains_ticket_id_fkey
      FOREIGN KEY (ticket_id) REFERENCES public.tickets(id)
      ON UPDATE CASCADE ON DELETE CASCADE;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='refresh_tokens_user_id_fkey') THEN
    ALTER TABLE public.refresh_tokens
      ADD CONSTRAINT refresh_tokens_user_id_fkey
      FOREIGN KEY (user_id) REFERENCES public.users(id)
      ON UPDATE CASCADE ON DELETE CASCADE;
  END IF;
END $$;

COMMIT;
SQL

# Reset tables (rejouable)
echo "🧹 TRUNCATE tables (rejouable)..."
sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"TRUNCATE TABLE public.refresh_tokens, public.ticket_gains, public.tickets, public.users RESTART IDENTITY CASCADE;"

# --- IMPORT USERS (staging + conversion is_admin 0/1 -> boolean + dates -> timestamptz) ---
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
  created_at_raw text,
  updated_at_raw text
);
COMMIT;
SQL

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"\copy public.users_staging(id, first_name, last_name, email, password, is_admin_raw, created_at_raw, updated_at_raw) FROM '${USERS_CSV}' WITH (FORMAT csv, DELIMITER ';', QUOTE '\"', ESCAPE '\"');"

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;

INSERT INTO public.users(id, first_name, last_name, email, password, is_admin, created_at, updated_at)
SELECT
  id,
  first_name,
  last_name,
  email,
  password,
  CASE
    WHEN lower(is_admin_raw) IN ('1','t','true','yes','y') THEN true
    ELSE false
  END AS is_admin,
  NULLIF(created_at_raw,'')::timestamptz,
  NULLIF(updated_at_raw,'')::timestamptz
FROM public.users_staging;

DROP TABLE public.users_staging;

COMMIT;
SQL

# --- IMPORT TICKETS (staging pour caster draw_date + timestamps) ---
echo "📥 Import tickets..."

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;
DROP TABLE IF EXISTS public.tickets_staging;
CREATE TABLE public.tickets_staging (
  id text,
  numbers text,
  lucky_number text,
  draw_date_raw text,
  draw_day text,
  created_at_raw text,
  updated_at_raw text,
  user_id text
);
COMMIT;
SQL

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"\copy public.tickets_staging(id, numbers, lucky_number, draw_date_raw, draw_day, created_at_raw, updated_at_raw, user_id) FROM '${TICKETS_CSV}' WITH (FORMAT csv, DELIMITER ';', QUOTE '\"', ESCAPE '\"');"

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;

INSERT INTO public.tickets(id, numbers, lucky_number, draw_date, draw_day, created_at, updated_at, user_id)
SELECT
  id,
  numbers,
  lucky_number::int,
  NULLIF(draw_date_raw,'')::date,
  draw_day,
  NULLIF(created_at_raw,'')::timestamptz,
  NULLIF(updated_at_raw,'')::timestamptz,
  user_id
FROM public.tickets_staging;

DROP TABLE public.tickets_staging;

COMMIT;
SQL

# --- IMPORT GAINS (staging pour caster boolean/numeric) ---
echo "📥 Import ticket_gains..."

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;
DROP TABLE IF EXISTS public.ticket_gains_staging;
CREATE TABLE public.ticket_gains_staging (
  id text,
  ticket_id text,
  matching_numbers text,
  lucky_number_match_raw text,
  gain_amount_raw text
);
COMMIT;
SQL

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -c \
"\copy public.ticket_gains_staging(id, ticket_id, matching_numbers, lucky_number_match_raw, gain_amount_raw) FROM '${GAINS_CSV}' WITH (FORMAT csv, DELIMITER ';', QUOTE '\"', ESCAPE '\"');"

sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;

INSERT INTO public.ticket_gains(id, ticket_id, matching_numbers, lucky_number_match, gain_amount)
SELECT
  id,
  ticket_id,
  matching_numbers::int,
  CASE
    WHEN lower(lucky_number_match_raw) IN ('1','t','true','yes','y') THEN true
    ELSE false
  END AS lucky_number_match,
  REPLACE(NULLIF(gain_amount_raw,''), ',', '.')::numeric(10,2)
FROM public.ticket_gains_staging;

DROP TABLE public.ticket_gains_staging;

COMMIT;
SQL

echo "✅ Vérification:"
sudo -u postgres psql -d "$DB_NAME" -c \
"SELECT 'users' t, count(*) FROM public.users
 UNION ALL SELECT 'tickets', count(*) FROM public.tickets
 UNION ALL SELECT 'ticket_gains', count(*) FROM public.ticket_gains
 UNION ALL SELECT 'refresh_tokens', count(*) FROM public.refresh_tokens;"

echo "✅ Init PostgreSQL terminé."
