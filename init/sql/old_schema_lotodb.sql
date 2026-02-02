-- =========================================================
-- Schema LotoDB
-- PostgreSQL
-- Compatible Spring Boot / Hibernate (validate)
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
    created_at TEXT,
    updated_at TEXT
);

-- =========================
-- TABLE: tickets
-- =========================
CREATE TABLE IF NOT EXISTS public.tickets (
    id TEXT PRIMARY KEY,
    numbers TEXT NOT NULL,
    lucky_number INTEGER NOT NULL,
    draw_date TEXT NOT NULL,
    draw_day TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT,
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
    gain_amount REAL NOT NULL
);

-- =========================
-- FOREIGN KEYS
-- =========================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'tickets_user_id_fkey'
    ) THEN
        ALTER TABLE public.tickets
        ADD CONSTRAINT tickets_user_id_fkey
        FOREIGN KEY (user_id)
        REFERENCES public.users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ticket_gains_ticket_id_fkey'
    ) THEN
        ALTER TABLE public.ticket_gains
        ADD CONSTRAINT ticket_gains_ticket_id_fkey
        FOREIGN KEY (ticket_id)
        REFERENCES public.tickets(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;
    END IF;
END
$$;

COMMIT;
