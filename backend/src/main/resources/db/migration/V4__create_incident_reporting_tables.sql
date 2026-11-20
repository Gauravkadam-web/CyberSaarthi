-- =====================================================================
-- CyberSaarthi Database Migration V4: Incident Reporting, Status & Auditing
-- Target Engine: PostgreSQL 14+
-- Conforms to: docs/database-design.md & Phase 7 Plan
-- =====================================================================

-- 1. Incident Reports Table
CREATE TABLE IF NOT EXISTS incident_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code VARCHAR(32) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    incident_channel VARCHAR(80) NOT NULL,
    incident_date DATE NOT NULL,
    description TEXT NOT NULL,
    financial_loss_amount NUMERIC(12, 2),
    user_selected_category_id UUID REFERENCES incident_categories(id) ON DELETE SET NULL,
    reviewed_category_id UUID REFERENCES incident_categories(id) ON DELETE SET NULL,
    current_status VARCHAR(40) NOT NULL DEFAULT 'SUBMITTED',
    disclaimer_acknowledged BOOLEAN NOT NULL DEFAULT TRUE,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_reports_ref_code ON incident_reports (reference_code);
CREATE INDEX IF NOT EXISTS idx_reports_user_sub ON incident_reports (user_id, submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_status ON incident_reports (current_status, submitted_at DESC);

-- 2. Report Status History Table
CREATE TABLE IF NOT EXISTS report_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES incident_reports(id) ON DELETE CASCADE,
    from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL,
    changed_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    change_reason TEXT,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_report_status_history ON report_status_history (report_id, changed_at ASC);

-- 3. Review Notes Table
CREATE TABLE IF NOT EXISTS review_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES incident_reports(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note_text TEXT NOT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'INTERNAL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_review_notes_report ON review_notes (report_id, created_at ASC);

-- 4. Audit Events Table
CREATE TABLE IF NOT EXISTS audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    event_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    metadata TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_events_entity ON audit_events (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_created ON audit_events (created_at DESC);
