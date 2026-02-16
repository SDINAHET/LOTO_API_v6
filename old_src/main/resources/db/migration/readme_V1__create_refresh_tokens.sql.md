V1__create_refresh_tokens.sql
-- =========================
-- TABLE: refresh_tokens
-- =========================
CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL
);

-- FK refresh_tokens -> users
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'refresh_tokens_user_id_fkey'
    ) THEN
        ALTER TABLE public.refresh_tokens
        ADD CONSTRAINT refresh_tokens_user_id_fkey
        FOREIGN KEY (user_id)
        REFERENCES public.users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;
    END IF;
END
$$;




BEGIN;

CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

-- FK refresh_tokens -> users
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'refresh_tokens_user_id_fkey'
    ) THEN
        ALTER TABLE public.refresh_tokens
        ADD CONSTRAINT refresh_tokens_user_id_fkey
        FOREIGN KEY (user_id)
        REFERENCES public.users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;
    END IF;
END
$$;

COMMIT;



root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v4# psql -U postgres -d lotodb
psql (14.20 (Ubuntu 14.20-0ubuntu0.22.04.1))
Type "help" for help.

lotodb=# BEGIN;

CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

-- FK refresh_tokens -> users
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'refresh_tokens_user_id_fkey'
    ) THEN
        ALTER TABLE public.refresh_tokens
        ADD CONSTRAINT refresh_tokens_user_id_fkey
        FOREIGN KEY (user_id)
        REFERENCES public.users(id)
COMMIT; IF;DELETE CASCADE;
BEGIN
CREATE TABLE
DO
COMMIT
lotodb=# \dt public.refresh_tokens
             List of relations
 Schema |      Name      | Type  |  Owner
--------+----------------+-------+----------
 public | refresh_tokens | table | postgres
(1 row)

lotodb=# \d public.refresh_tokens
                     Table "public.refresh_tokens"
   Column   |           Type           | Collation | Nullable | Default
------------+--------------------------+-----------+----------+---------
 id         | text                     |           | not null |
 user_id    | text                     |           | not null |
 token_hash | text                     |           | not null |
 created_at | timestamp with time zone |           | not null | now()
 expires_at | timestamp with time zone |           | not null |
 revoked_at | timestamp with time zone |           |          |
Indexes:
    "refresh_tokens_pkey" PRIMARY KEY, btree (id)
    "refresh_tokens_token_hash_key" UNIQUE CONSTRAINT, btree (token_hash)
Foreign-key constraints:
    "refresh_tokens_user_id_fkey" FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE

lotodb=#












db complete
V1__schema_lotodb.sql
-- =========================================================
-- Schema LotoDB
-- PostgreSQL
-- Compatible Spring Boot / Hibernate (ddl-auto=validate)
-- =========================================================

BEGIN;

-- =========================
-- TABLE: users
-- =========================
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

-- =========================
-- TABLE: tickets
-- =========================
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

-- =========================
-- TABLE: ticket_gains
-- =========================
CREATE TABLE IF NOT EXISTS public.ticket_gains (
    id TEXT PRIMARY KEY,
    ticket_id TEXT NOT NULL,
    matching_numbers INTEGER NOT NULL,
    lucky_number_match BOOLEAN NOT NULL,
    gain_amount NUMERIC(10,2) NOT NULL
);

-- =========================
-- TABLE: refresh_tokens
-- =========================
CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

-- =========================
-- FOREIGN KEYS
-- =========================

-- tickets -> users
ALTER TABLE public.tickets
ADD CONSTRAINT IF NOT EXISTS tickets_user_id_fkey
FOREIGN KEY (user_id)
REFERENCES public.users(id)
ON UPDATE CASCADE
ON DELETE CASCADE;

-- ticket_gains -> tickets
ALTER TABLE public.ticket_gains
ADD CONSTRAINT IF NOT EXISTS ticket_gains_ticket_id_fkey
FOREIGN KEY (ticket_id)
REFERENCES public.tickets(id)
ON UPDATE CASCADE
ON DELETE CASCADE;

-- refresh_tokens -> users
ALTER TABLE public.refresh_tokens
ADD CONSTRAINT IF NOT EXISTS refresh_tokens_user_id_fkey
FOREIGN KEY (user_id)
REFERENCES public.users(id)
ON UPDATE CASCADE
ON DELETE CASCADE;

COMMIT;





ALTER TABLE public.tickets
ADD CONSTRAINT tickets_user_id_fkey
FOREIGN KEY (user_id)
REFERENCES public.users(id)
ON UPDATE CASCADE
ON DELETE CASCADE;


ALTER TABLE public.tickets
ADD CONSTRAINT tickets_user_id_fkey
FOREIGN KEY (user_id)
REFERENCES public.users(id)
ON UPDATE CASCADE
ON DELETE CASCADE;



lotodb=# ALTER TABLE public.tickets
ADD CONSTRAINT tickets_user_id_fkey
FOREIGN KEY (user_id)
REFERENCES public.users(id)
ON UPDATE CASCADE
ON DELETE CASCADE;
ALTER TABLE
lotodb=# \d public.tickets
                 Table "public.tickets"
    Column    |  Type   | Collation | Nullable | Default
--------------+---------+-----------+----------+---------
 id           | text    |           | not null |
 numbers      | text    |           | not null |
 lucky_number | integer |           | not null |
 draw_date    | text    |           | not null |
 draw_day     | text    |           |          |
 created_at   | text    |           | not null |
 updated_at   | text    |           |          |
 user_id      | text    |           | not null |
Indexes:
    "tickets_pkey" PRIMARY KEY, btree (id)
Foreign-key constraints:
    "tickets_user_id_fkey" FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE
Referenced by:
    TABLE "ticket_gains" CONSTRAINT "ticket_gains_ticket_id_fkey" FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON UPDATE CASCADE ON DELETE CASCADE

lotodb=#
