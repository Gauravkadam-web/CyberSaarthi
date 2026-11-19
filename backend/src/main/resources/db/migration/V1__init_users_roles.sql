-- =====================================================================
-- CyberSaarthi Database Migration V1: Initial Users, Roles & RBAC Tables
-- Target Engine: PostgreSQL 14+
-- Conforms to: docs/database-design.md
-- =====================================================================

-- Ensure pgcrypto or native gen_random_uuid() is available
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for case-insensitive email lookup
CREATE INDEX IF NOT EXISTS idx_users_email ON users (LOWER(email));

-- 3. User Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- 4. Seed Foundational Roles
INSERT INTO roles (code, description) VALUES
    ('ROLE_USER', 'Standard registered public user with reporting and quiz access'),
    ('ROLE_ADMIN', 'Authorised portal administrator with review and moderation access')
ON CONFLICT (code) DO NOTHING;
