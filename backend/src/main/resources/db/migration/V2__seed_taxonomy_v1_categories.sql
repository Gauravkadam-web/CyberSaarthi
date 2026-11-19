-- =====================================================================
-- CyberSaarthi Database Migration V2: Incident Categories & Taxonomy v1.0
-- Target Engine: PostgreSQL 14+
-- Conforms to: docs/taxonomy-v1.0.md & docs/database-design.md
-- =====================================================================

CREATE TABLE IF NOT EXISTS incident_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    taxonomy_version VARCHAR(30) NOT NULL,
    code VARCHAR(80) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_taxonomy_category UNIQUE (taxonomy_version, code)
);

-- Seed Taxonomy v1.0 (8 Approved Categories)
INSERT INTO incident_categories (taxonomy_version, code, display_name, description, is_active) VALUES
    ('v1.0', 'FINANCIAL_UPI_FRAUD', 'Financial & UPI Fraud', 'Fraudulent UPI transactions, fake QR codes, banking OTP scams, and unauthorized fund transfers.', TRUE),
    ('v1.0', 'PHISHING_SOCIAL_ENGINEERING', 'Phishing & Social Engineering', 'Deceptive emails, fake delivery notifications, lottery messages, and impersonation attempts.', TRUE),
    ('v1.0', 'IDENTITY_THEFT', 'Identity Theft & Impersonation', 'Unauthorized use of Aadhaar, PAN, forged KYC documents, and synthetic identity creation.', TRUE),
    ('v1.0', 'SOCIAL_ACCOUNT_COMPROMISE', 'Social Media Account Compromise', 'Account takeovers, profile cloning, session hijacking, and unauthorized access to social accounts.', TRUE),
    ('v1.0', 'CYBERSTALKING_HARASSMENT', 'Cyberstalking & Online Harassment', 'Persistent harassment, non-consensual image sharing, online abuse, and digital stalking.', TRUE),
    ('v1.0', 'JOB_INVESTMENT_SCAM', 'Job & Investment Scams', 'Work-from-home tasks, Ponzi schemes, fake part-time job offers, and crypto investment fraud.', TRUE),
    ('v1.0', 'MALWARE_RANSOMWARE', 'Malware & Ransomware', 'Malicious APK files, ransomware extortion, keyloggers, and device lockouts.', TRUE),
    ('v1.0', 'OTHER_UNCLASSIFIED', 'Other / Unclassified Incident', 'Uncertain, multi-category, or emerging cyber incidents pending administrative review.', TRUE)
ON CONFLICT (taxonomy_version, code) DO NOTHING;
