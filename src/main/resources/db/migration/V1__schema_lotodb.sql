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
