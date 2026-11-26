-- =====================================================================
-- CyberSaarthi Synthetic Demonstration Dataset
-- Target Engine: PostgreSQL 14+
-- Conforms to: docs/taxonomy-v1.0.md, AGENTS.md, Phase 11 Release Plan
-- Description: Deterministic, privacy-safe, non-PII synthetic seed data
--              for live academic demonstrations, viva evaluations, and UI showcase.
-- =====================================================================

-- 1. DEMO USERS
-- Password for all demo accounts: CyberSaarthi@123
-- BCrypt (work factor 12): $2a$12$mcwhqGh98otqeeAbeBb14.XQtrmd3v3ZjU.hXRrQL81oBSN7TVCYq

INSERT INTO users (id, email, password_hash, display_name, account_status, created_at)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin@cybersaarthi.org', '$2a$12$mcwhqGh98otqeeAbeBb14.XQtrmd3v3ZjU.hXRrQL81oBSN7TVCYq', 'Officer Vikram Deshmukh (Admin)', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '30 days'),
    ('b0000000-0000-0000-0000-000000000001', 'citizen@cybersaarthi.org', '$2a$12$mcwhqGh98otqeeAbeBb14.XQtrmd3v3ZjU.hXRrQL81oBSN7TVCYq', 'Ramesh Sharma (Citizen)', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '25 days'),
    ('b0000000-0000-0000-0000-000000000002', 'student@cybersaarthi.org', '$2a$12$mcwhqGh98otqeeAbeBb14.XQtrmd3v3ZjU.hXRrQL81oBSN7TVCYq', 'Priya Patel (Student Learner)', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '20 days')
ON CONFLICT (email) DO NOTHING;

-- Assign Roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@cybersaarthi.org' AND r.code = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email IN ('citizen@cybersaarthi.org', 'student@cybersaarthi.org') AND r.code = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- 2. SYNTHETIC INCIDENT REPORTS (20+ Cases across 8 Categories & Channels)

INSERT INTO incident_reports (
    id, reference_code, user_id, incident_channel, incident_date,
    description, financial_loss_amount, current_status, disclaimer_acknowledged,
    submitted_at, updated_at
) VALUES
-- Report 1: UPI Electricity Bill Scam (Reviewed)
(
    'c0000000-0000-0000-0000-000000000001', 'CS-2026-7K8M2',
    'b0000000-0000-0000-0000-000000000001', 'UPI_APP', CURRENT_DATE - 12,
    'Received an urgent SMS claiming power will be disconnected at 9:30 PM due to unpaid electricity bill. The message gave a support number. Upon calling, the fraudster instructed to download a remote access tool and transfer Rs 10 on UPI, resulting in Rs 18,500 debited from bank account.',
    18500.00, 'REVIEWED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '10 days'
),
-- Report 2: WhatsApp Job Offer Scam (Under Review)
(
    'c0000000-0000-0000-0000-000000000002', 'CS-2026-9P3X4',
    'b0000000-0000-0000-0000-000000000001', 'WHATSAPP', CURRENT_DATE - 10,
    'Contacted by an unknown international number on WhatsApp offering part-time work liking YouTube videos for Rs 50 per task. After small initial payout, they added me to a Telegram group requiring prepaid investments. Lost Rs 45,000 in VIP task scheme.',
    45000.00, 'UNDER_REVIEW', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '9 days'
),
-- Report 3: Fake Income Tax Refund Phishing (Needs Info)
(
    'c0000000-0000-0000-0000-000000000003', 'CS-2026-2H4N8',
    'b0000000-0000-0000-0000-000000000001', 'SMS', CURRENT_DATE - 8,
    'Received SMS saying: Income Tax refund of Rs 15,490 approved. Click link to update account details. Link opened a replica of the e-filing portal asking for PAN, net banking login, and OTP. Did not share OTP, no financial loss yet.',
    0.00, 'NEEDS_INFORMATION', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '7 days'
),
-- Report 4: Instagram Account Takeover (Closed)
(
    'c0000000-0000-0000-0000-000000000004', 'CS-2026-5T9V1',
    'b0000000-0000-0000-0000-000000000002', 'SOCIAL_MEDIA', CURRENT_DATE - 15,
    'My Instagram account was hacked after clicking a voting link sent by a friend whose account was already compromised. The attacker enabled 2FA with their own authenticator and is now messaging my contacts requesting emergency money via UPI.',
    0.00, 'CLOSED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '11 days'
),
-- Report 5: Aadhaar Impersonation SIM Swap (Reviewed)
(
    'c0000000-0000-0000-0000-000000000005', 'CS-2026-8B1Q3',
    'b0000000-0000-0000-0000-000000000001', 'PHONE_CALL', CURRENT_DATE - 6,
    'Imposter called claiming to be Telecom department officer threatening mobile disconnection due to illegal Aadhaar usage. Demanded digital verification via screen sharing application. Terminated call immediately upon suspicion.',
    0.00, 'REVIEWED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '5 days'
),
-- Report 6: Fake Courier Smishing Scam (Submitted)
(
    'c0000000-0000-0000-0000-000000000006', 'CS-2026-3M7K5',
    'b0000000-0000-0000-0000-000000000001', 'SMS', CURRENT_DATE - 2,
    'Received text message claiming India Post package cannot be delivered due to wrong house address. Link asked to pay Rs 5 re-delivery charge. When paying, payment gateway prompted Rs 7,500 deduction. Cancelled transaction.',
    0.00, 'SUBMITTED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
),
-- Report 7: Ransomware File Encryption on Personal Laptop (Under Review)
(
    'c0000000-0000-0000-0000-000000000007', 'CS-2026-6W2D9',
    'b0000000-0000-0000-0000-000000000002', 'WEBSITE_PORTAL', CURRENT_DATE - 3,
    'Downloaded a PDF converter tool from an online forum. The installer launched ransomware that encrypted all documents and photos with extension .locked. A ransom note appeared demanding 0.05 BTC for decryption key.',
    0.00, 'UNDER_REVIEW', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
),
-- Report 8: Cyberbullying and Harassment via Fake Profiles (Reviewed)
(
    'c0000000-0000-0000-0000-000000000008', 'CS-2026-4L8J2',
    'b0000000-0000-0000-0000-000000000002', 'SOCIAL_MEDIA', CURRENT_DATE - 7,
    'Unknown individual created duplicate Instagram profile with my personal photos and phone number, posting defamatory comments and encouraging strangers to harass me via WhatsApp voice calls.',
    0.00, 'REVIEWED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '6 days'
),
-- Report 9: Fraudulent QR Code Scan at Merchant Store (Closed)
(
    'c0000000-0000-0000-0000-000000000009', 'CS-2026-1Y5R7',
    'b0000000-0000-0000-0000-000000000001', 'UPI_APP', CURRENT_DATE - 20,
    'Scanned a merchant standee QR code pasted over original QR code at local grocery store. Instead of merchant account, payment went to an individual account. Debited Rs 1,200. Merchant confirmed tamper.',
    1200.00, 'CLOSED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '18 days'
),
-- Report 10: Fake Stock Trading Advisory Scam (Reviewed)
(
    'c0000000-0000-0000-0000-000000000010', 'CS-2026-9E2A6',
    'b0000000-0000-0000-0000-000000000001', 'WHATSAPP', CURRENT_DATE - 14,
    'Lured into an institutional IPO trading group on WhatsApp promising 300% monthly returns. Was instructed to deposit funds into private current accounts. When attempting withdrawal of profits, admin demanded 20% security fee.',
    50000.00, 'REVIEWED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP - INTERVAL '12 days'
),
-- Report 11: Fake Customer Care Number on Search Engine (Submitted)
(
    'c0000000-0000-0000-0000-000000000011', 'CS-2026-7H9B4',
    'b0000000-0000-0000-0000-000000000001', 'PHONE_CALL', CURRENT_DATE - 1,
    'Searched for airline customer care number on Google. Called the top sponsored result. Fake agent asked to pay Rs 5 refundable cancellation fee via UPI collect request, which actually requested Rs 5,000.',
    5000.00, 'SUBMITTED', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'
),
-- Report 12: E-Commerce Fake Delivery Portal (Under Review)
(
    'c0000000-0000-0000-0000-000000000012', 'CS-2026-8K3C5',
    'b0000000-0000-0000-0000-000000000002', 'WEBSITE_PORTAL', CURRENT_DATE - 4,
    'Ordered shoes from a sponsored Instagram ad leading to an identical replica of a popular e-commerce site. Paid Rs 2,499 via UPI. No order confirmation or tracking number provided; site disappeared next day.',
    2499.00, 'UNDER_REVIEW', TRUE,
    CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '3 days'
)
ON CONFLICT (reference_code) DO NOTHING;

-- Link Categories to Incident Reports
UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-7K8M2' AND c.code = 'FINANCIAL_UPI_FRAUD';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-9P3X4' AND c.code = 'JOB_INVESTMENT_SCAM';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-2H4N8' AND c.code = 'PHISHING_SOCIAL_ENGINEERING';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-5T9V1' AND c.code = 'SOCIAL_ACCOUNT_COMPROMISE';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-8B1Q3' AND c.code = 'IDENTITY_THEFT';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-6W2D9' AND c.code = 'MALWARE_RANSOMWARE';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-4L8J2' AND c.code = 'CYBERSTALKING_HARASSMENT';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-1Y5R7' AND c.code = 'FINANCIAL_UPI_FRAUD';

UPDATE incident_reports r
SET reviewed_category_id = c.id
FROM incident_categories c
WHERE r.reference_code = 'CS-2026-9E2A6' AND c.code = 'JOB_INVESTMENT_SCAM';

-- 3. STATUS TRANSITION HISTORIES

INSERT INTO report_status_history (id, report_id, from_status, to_status, change_reason, changed_by, changed_at)
SELECT gen_random_uuid(), r.id, NULL, 'SUBMITTED', 'Initial citizen report submission', r.user_id, r.submitted_at
FROM incident_reports r
ON CONFLICT DO NOTHING;

INSERT INTO report_status_history (id, report_id, from_status, to_status, change_reason, changed_by, changed_at)
SELECT gen_random_uuid(), r.id, 'SUBMITTED', 'UNDER_REVIEW', 'Assigned to duty officer for triage', 'a0000000-0000-0000-0000-000000000001', r.submitted_at + INTERVAL '1 day'
FROM incident_reports r
WHERE r.current_status IN ('UNDER_REVIEW', 'NEEDS_INFORMATION', 'REVIEWED', 'CLOSED')
ON CONFLICT DO NOTHING;

INSERT INTO report_status_history (id, report_id, from_status, to_status, change_reason, changed_by, changed_at)
SELECT gen_random_uuid(), r.id, 'UNDER_REVIEW', 'REVIEWED', 'Validated financial scam vector; guidance issued', 'a0000000-0000-0000-0000-000000000001', r.submitted_at + INTERVAL '2 days'
FROM incident_reports r
WHERE r.current_status IN ('REVIEWED', 'CLOSED')
ON CONFLICT DO NOTHING;

-- 4. REVIEW NOTES (Internal and User Visible)

INSERT INTO review_notes (id, report_id, author_id, note_text, visibility, created_at)
SELECT
    gen_random_uuid(), r.id, 'a0000000-0000-0000-0000-000000000001',
    'Report matches known APK remote access malware campaign. Urgent action required: Citizen advised to freeze UPI handle and file official report on 1930 helpline.',
    'USER_VISIBLE', r.submitted_at + INTERVAL '1 day'
FROM incident_reports r
WHERE r.reference_code = 'CS-2026-7K8M2'
ON CONFLICT DO NOTHING;

INSERT INTO review_notes (id, report_id, author_id, note_text, visibility, created_at)
SELECT
    gen_random_uuid(), r.id, 'a0000000-0000-0000-0000-000000000001',
    'Investigating bank account IFSC details forwarded to state cyber cell liaison for pattern tracking.',
    'INTERNAL', r.submitted_at + INTERVAL '1 day'
FROM incident_reports r
WHERE r.reference_code = 'CS-2026-7K8M2'
ON CONFLICT DO NOTHING;

-- 5. ML INFERENCE PREDICTIONS (Demonstrating ML Telemetry)

INSERT INTO ml_predictions (
    id, report_id, predicted_category_id, predicted_category_code,
    model_name, model_version, taxonomy_version, confidence_score,
    score_type, requires_manual_review, inference_status, is_advisory, inferred_at
)
SELECT
    gen_random_uuid(), r.id, c.id, c.code,
    'LogisticRegression-TFIDF', 'baseline-v1.0', '1.0', 0.942,
    'calibrated_probability', FALSE, 'SUCCESS', TRUE, r.submitted_at + INTERVAL '2 seconds'
FROM incident_reports r, incident_categories c
WHERE r.reference_code = 'CS-2026-7K8M2' AND c.code = 'FINANCIAL_UPI_FRAUD'
ON CONFLICT DO NOTHING;

INSERT INTO ml_predictions (
    id, report_id, predicted_category_id, predicted_category_code,
    model_name, model_version, taxonomy_version, confidence_score,
    score_type, requires_manual_review, inference_status, is_advisory, inferred_at
)
SELECT
    gen_random_uuid(), r.id, c.id, c.code,
    'LogisticRegression-TFIDF', 'baseline-v1.0', '1.0', 0.887,
    'calibrated_probability', FALSE, 'SUCCESS', TRUE, r.submitted_at + INTERVAL '2 seconds'
FROM incident_reports r, incident_categories c
WHERE r.reference_code = 'CS-2026-9P3X4' AND c.code = 'JOB_INVESTMENT_SCAM'
ON CONFLICT DO NOTHING;

INSERT INTO ml_predictions (
    id, report_id, predicted_category_id, predicted_category_code,
    model_name, model_version, taxonomy_version, confidence_score,
    score_type, requires_manual_review, inference_status, is_advisory, inferred_at
)
SELECT
    gen_random_uuid(), r.id, c.id, c.code,
    'LogisticRegression-TFIDF', 'baseline-v1.0', '1.0', 0.912,
    'calibrated_probability', FALSE, 'SUCCESS', TRUE, r.submitted_at + INTERVAL '2 seconds'
FROM incident_reports r, incident_categories c
WHERE r.reference_code = 'CS-2026-2H4N8' AND c.code = 'PHISHING_SOCIAL_ENGINEERING'
ON CONFLICT DO NOTHING;

INSERT INTO ml_predictions (
    id, report_id, predicted_category_id, predicted_category_code,
    model_name, model_version, taxonomy_version, confidence_score,
    score_type, requires_manual_review, inference_status, is_advisory, inferred_at
)
SELECT
    gen_random_uuid(), r.id, c.id, c.code,
    'LogisticRegression-TFIDF', 'baseline-v1.0', '1.0', 0.865,
    'calibrated_probability', FALSE, 'SUCCESS', TRUE, r.submitted_at + INTERVAL '2 seconds'
FROM incident_reports r, incident_categories c
WHERE r.reference_code = 'CS-2026-5T9V1' AND c.code = 'SOCIAL_ACCOUNT_COMPROMISE'
ON CONFLICT DO NOTHING;

INSERT INTO ml_predictions (
    id, report_id, predicted_category_id, predicted_category_code,
    model_name, model_version, taxonomy_version, confidence_score,
    score_type, requires_manual_review, inference_status, is_advisory, inferred_at
)
SELECT
    gen_random_uuid(), r.id, c.id, c.code,
    'LogisticRegression-TFIDF', 'baseline-v1.0', '1.0', 0.523,
    'calibrated_probability', TRUE, 'LOW_CONFIDENCE', TRUE, r.submitted_at + INTERVAL '2 seconds'
FROM incident_reports r, incident_categories c
WHERE r.reference_code = 'CS-2026-8B1Q3' AND c.code = 'IDENTITY_THEFT'
ON CONFLICT DO NOTHING;

-- 6. QUIZ ATTEMPTS (Populating Educational Assessment Dashboard Metrics)

INSERT INTO quiz_attempts (id, user_id, topic_id, total_questions, correct_answers, submitted_at)
SELECT
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    t.id, 5, 4, CURRENT_TIMESTAMP - INTERVAL '15 days'
FROM awareness_topics t
WHERE t.slug = 'understanding-upi-safety-and-common-fraud-vectors'
ON CONFLICT DO NOTHING;

INSERT INTO quiz_attempts (id, user_id, topic_id, total_questions, correct_answers, submitted_at)
SELECT
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    t.id, 5, 5, CURRENT_TIMESTAMP - INTERVAL '12 days'
FROM awareness_topics t
WHERE t.slug = 'understanding-upi-safety-and-common-fraud-vectors'
ON CONFLICT DO NOTHING;

INSERT INTO quiz_attempts (id, user_id, topic_id, total_questions, correct_answers, submitted_at)
SELECT
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    t.id, 5, 4, CURRENT_TIMESTAMP - INTERVAL '10 days'
FROM awareness_topics t
WHERE t.slug = 'phishing-and-social-engineering-defense-guide'
ON CONFLICT DO NOTHING;

INSERT INTO quiz_attempts (id, user_id, topic_id, total_questions, correct_answers, submitted_at)
SELECT
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    t.id, 5, 3, CURRENT_TIMESTAMP - INTERVAL '5 days'
FROM awareness_topics t
WHERE t.slug = 'phishing-and-social-engineering-defense-guide'
ON CONFLICT DO NOTHING;

-- 7. AUDIT LOG LEDGER

INSERT INTO audit_events (id, actor_user_id, event_type, entity_type, entity_id, metadata, created_at)
VALUES
(
    gen_random_uuid(), 'a0000000-0000-0000-0000-000000000001',
    'RESEARCH_DATA_EXPORTED', 'INCIDENT_REPORTS', gen_random_uuid(),
    '{"type":"INCIDENTS_CSV","recordCount":12}', CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    gen_random_uuid(), 'a0000000-0000-0000-0000-000000000001',
    'RESEARCH_DATA_EXPORTED', 'QUIZ_ATTEMPTS', gen_random_uuid(),
    '{"type":"ASSESSMENTS_CSV","recordCount":4}', CURRENT_TIMESTAMP - INTERVAL '1 day'
);
