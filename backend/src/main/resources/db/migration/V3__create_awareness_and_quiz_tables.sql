-- =====================================================================
-- CyberSaarthi Database Migration V3: Awareness Topics & Educational Assessments
-- Target Engine: PostgreSQL 14+
-- Conforms to: docs/database-design.md & Phase 6 Plan
-- =====================================================================

-- 1. Awareness Topics Table
CREATE TABLE IF NOT EXISTS awareness_topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(160) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    body_markdown TEXT NOT NULL,
    topic_category VARCHAR(80) NOT NULL,
    publication_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_awareness_slug ON awareness_topics (slug);
CREATE INDEX IF NOT EXISTS idx_awareness_pub_status ON awareness_topics (publication_status, topic_category);

-- 2. Quiz Questions Table
CREATE TABLE IF NOT EXISTS quiz_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id UUID NOT NULL REFERENCES awareness_topics(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    correct_option_key VARCHAR(10) NOT NULL,
    explanation TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quiz_questions_topic ON quiz_questions (topic_id, display_order);

-- 3. Normalized Quiz Options Table
CREATE TABLE IF NOT EXISTS quiz_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    option_key VARCHAR(10) NOT NULL,
    option_text TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uq_question_option UNIQUE (question_id, option_key)
);

CREATE INDEX IF NOT EXISTS idx_quiz_options_question ON quiz_options (question_id, display_order);

-- 4. Quiz Attempts Table
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id UUID NOT NULL REFERENCES awareness_topics(id) ON DELETE CASCADE,
    score INTEGER NOT NULL,
    max_score INTEGER NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempts_user ON quiz_attempts (user_id, submitted_at DESC);

-- 5. Quiz Attempt Answers Table (Audit breakdown)
CREATE TABLE IF NOT EXISTS quiz_attempt_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    selected_option_key VARCHAR(10) NOT NULL,
    is_correct BOOLEAN NOT NULL
);

-- =====================================================================
-- Seed Foundational Published Awareness Topics & Knowledge Assessments
-- =====================================================================

-- Topic 1: UPI & Digital Payment Fraud Prevention
INSERT INTO awareness_topics (id, slug, title, summary, body_markdown, topic_category, publication_status, published_at)
VALUES (
    'a1b2c3d4-0001-4000-8000-000000000001',
    'upi-digital-payment-fraud-prevention',
    'UPI & Digital Payment Fraud Prevention',
    'Learn how fraudulent QR codes, fake refund links, and PIN requests are used to drain bank accounts, and how to stay protected.',
    '# UPI & Digital Payment Fraud Prevention

## Overview
Unified Payments Interface (UPI) has transformed digital transactions in India. However, cyber fraudsters exploit misunderstandings about how UPI works to siphon funds.

### The Golden Rule of UPI
> **Remember:** You ONLY enter your UPI PIN to **SEND** or **DEDUCT** money from your account. You **NEVER** need to enter a PIN, password, or scan a QR code to **RECEIVE** money.

---

## Common Modus Operandi

### 1. The Marketplace / OLX Scam
- Fraudsters pose as buyers of second-hand furniture, vehicles, or electronics.
- They claim to pay an advance and send a QR code or "collect request".
- They tell you: *"Scan this QR code and enter your PIN to accept Rs. 5,000."*
- The moment you enter your PIN, Rs. 5,000 is debited from your account!

### 2. Fake Customer Care Numbers
- Scammers create fake Google business listings or social profiles posing as bank or wallet customer care.
- When contacted for refund assistance, they ask you to install remote-access apps (e.g., AnyDesk, TeamViewer) or click approval links.

---

## Red Flags to Watch For
- Anyone asking you to enter your UPI PIN to receive a prize, cash back, or refund.
- Requests to install remote support apps on your smartphone.
- Urgency tactics claiming your bank account or KYC will be blocked within 24 hours.

---

## Immediate Emergency Action
If you have fallen victim to a UPI financial scam:
1. **Immediately dial 1930** (Citizen Financial Cyber Fraud Reporting System) within the "golden hour" to freeze transaction flows.
2. File an incident complaint on **[cybercrime.gov.in](https://cybercrime.gov.in)**.
3. Block your debit card and UPI profile immediately via your bank mobile app.',
    'FINANCIAL_UPI_FRAUD',
    'PUBLISHED',
    CURRENT_TIMESTAMP
) ON CONFLICT (slug) DO NOTHING;

-- Questions for Topic 1
INSERT INTO quiz_questions (id, topic_id, question_text, correct_option_key, explanation, display_order)
VALUES (
    'b1c2d3e4-0001-4000-8000-000000000001',
    'a1b2c3d4-0001-4000-8000-000000000001',
    'A buyer on an online marketplace sends you a QR code claiming it will transfer an advance payment to your bank account. What is the safest action?',
    'B',
    'Receiving money via UPI NEVER requires entering your PIN or scanning a QR code. QR codes are only scanned to pay out money.',
    1
) ON CONFLICT (id) DO NOTHING;

INSERT INTO quiz_options (question_id, option_key, option_text, display_order) VALUES
('b1c2d3e4-0001-4000-8000-000000000001', 'A', 'Scan the QR code and enter your UPI PIN to approve receipt.', 1),
('b1c2d3e4-0001-4000-8000-000000000001', 'B', 'Do not scan it. Receiving money on UPI never requires scanning a QR code or entering a PIN.', 2),
('b1c2d3e4-0001-4000-8000-000000000001', 'C', 'Enter a wrong PIN first to verify if the sender is genuine.', 3),
('b1c2d3e4-0001-4000-8000-000000000001', 'D', 'Forward the QR code to your friend to receive on your behalf.', 4)
ON CONFLICT (question_id, option_key) DO NOTHING;

INSERT INTO quiz_questions (id, topic_id, question_text, correct_option_key, explanation, display_order)
VALUES (
    'b1c2d3e4-0001-4000-8000-000000000002',
    'a1b2c3d4-0001-4000-8000-000000000001',
    'If you realize that money has been fraudulently debited from your bank account via UPI, what is the official emergency helpline number in India?',
    'C',
    '1930 is the official National Cyber Financial Fraud Helpline operated by the Ministry of Home Affairs (MHA) to freeze stolen funds.',
    2
) ON CONFLICT (id) DO NOTHING;

INSERT INTO quiz_options (question_id, option_key, option_text, display_order) VALUES
('b1c2d3e4-0001-4000-8000-000000000002', 'A', '100', 1),
('b1c2d3e4-0001-4000-8000-000000000002', 'B', '1091', 2),
('b1c2d3e4-0001-4000-8000-000000000002', 'C', '1930', 3),
('b1c2d3e4-0001-4000-8000-000000000002', 'D', '1800', 4)
ON CONFLICT (question_id, option_key) DO NOTHING;

-- Topic 2: Phishing Links & Social Engineering Defense
INSERT INTO awareness_topics (id, slug, title, summary, body_markdown, topic_category, publication_status, published_at)
VALUES (
    'a1b2c3d4-0001-4000-8000-000000000002',
    'phishing-links-social-engineering-defense',
    'Phishing Links & Social Engineering Defense',
    'Identify deceptive SMS, WhatsApp messages, and spoofed bank websites designed to steal credentials and OTPs.',
    '# Phishing & Social Engineering Defense

## What is Phishing?
Phishing is the fraudulent practice of sending messages disguised as trustworthy organizations (such as banks, courier companies, or utility boards) to trick victims into revealing sensitive personal credentials, passwords, or one-time passwords (OTPs).

---

## Anatomy of a Phishing Message
1. **False Sense of Urgency:** *"Your electricity will be disconnected tonight at 9:30 PM due to unpaid bills."* or *"Your courier parcel is detained at Mumbai terminal. Click here to update address."*
2. **Deceptive URLs:** Look-alike domains such as `sbi-kyc-update.xyz`, `indiapost-delivery-track.top`, or `electricity-bill-pay.site`.
3. **Credential Harvesting Page:** A webpage mirroring official portals asking for card numbers, netbanking credentials, and OTPs.

---

## Defensive Best Practices
- Never click on unverified short links (`bit.ly`, `tinyurl.com`, or unfamiliar `.top`/`.xyz` domains) received via SMS.
- Check the official domain ending: Indian government websites end strictly in `.gov.in` or `.nic.in`.
- Official banks never ask for your password, PIN, or OTP over SMS or phone call.',
    'PHISHING_SOCIAL_ENGINEERING',
    'PUBLISHED',
    CURRENT_TIMESTAMP
) ON CONFLICT (slug) DO NOTHING;

-- Questions for Topic 2
INSERT INTO quiz_questions (id, topic_id, question_text, correct_option_key, explanation, display_order)
VALUES (
    'b1c2d3e4-0001-4000-8000-000000000003',
    'a1b2c3d4-0001-4000-8000-000000000002',
    'You receive an SMS: "Dear Customer, your electricity power will be disconnected at 9:30 PM tonight. Immediately call 98765xxxxx to pay pending bill." How should you handle this?',
    'C',
    'Disconnection threats from personal mobile numbers with urgent deadlines are classic phishing lures. Utility boards never send personal 10-digit mobile numbers for bill payments.',
    1
) ON CONFLICT (id) DO NOTHING;

INSERT INTO quiz_options (question_id, option_key, option_text, display_order) VALUES
('b1c2d3e4-0001-4000-8000-000000000003', 'A', 'Call the number immediately to avoid power disconnection.', 1),
('b1c2d3e4-0001-4000-8000-000000000003', 'B', 'Send your previous bill copy to the number on WhatsApp.', 2),
('b1c2d3e4-0001-4000-8000-000000000003', 'C', 'Ignore the SMS. Check your official electricity board portal or app directly for genuine billing status.', 3),
('b1c2d3e4-0001-4000-8000-000000000003', 'D', 'Forward the message to your family members so they can call.', 4)
ON CONFLICT (question_id, option_key) DO NOTHING;

-- Topic 3: Social Media Account Security & Takeover Defense
INSERT INTO awareness_topics (id, slug, title, summary, body_markdown, topic_category, publication_status, published_at)
VALUES (
    'a1b2c3d4-0001-4000-8000-000000000003',
    'social-media-account-security',
    'Social Media Account Security & Takeover Defense',
    'Essential safeguards against Instagram and WhatsApp cloning, session hijacking, and extortion tactics.',
    '# Social Media Account Security

## Common Social Engineering Traps
- **Fake Copyright Infringement Notices:** DMs claiming your account will be deleted in 24 hours unless you verify identity via an external form.
- **Vote/Friend Help Requests:** A friend asks for a screenshot of an SMS code sent to your phone *"to help them recover their account"*. That SMS code is actually your own login OTP!

---

## Defensive Steps
1. **Enable Two-Factor Authentication (2FA):** Always use an authenticator app (e.g., Google Authenticator) rather than SMS where possible.
2. **Never Share Verification Codes:** No legitimate friend or platform support will ever ask you for your 2FA code.
3. **Regularly Audit Active Sessions:** Check *Settings -> Security -> Logged in devices* and terminate unfamiliar sessions.',
    'SOCIAL_ACCOUNT_COMPROMISE',
    'PUBLISHED',
    CURRENT_TIMESTAMP
) ON CONFLICT (slug) DO NOTHING;

-- Questions for Topic 3
INSERT INTO quiz_questions (id, topic_id, question_text, correct_option_key, explanation, display_order)
VALUES (
    'b1c2d3e4-0001-4000-8000-000000000004',
    'a1b2c3d4-0001-4000-8000-000000000003',
    'A friend on Instagram sends you a DM asking: "Hey, can you send me the screenshot of the 6-digit code sent to your phone? I need it to unlock my account." What is actually happening?',
    'A',
    'The attacker is attempting to reset YOUR account password or WhatsApp and is tricking you into providing your own authorization code.',
    1
) ON CONFLICT (id) DO NOTHING;

INSERT INTO quiz_options (question_id, option_key, option_text, display_order) VALUES
('b1c2d3e4-0001-4000-8000-000000000004', 'A', 'The friend account is compromised; the attacker is trying to take over YOUR account.', 1),
('b1c2d3e4-0001-4000-8000-000000000004', 'B', 'The friend is genuinely locked out and Instagram uses friends as backup contacts.', 2),
('b1c2d3e4-0001-4000-8000-000000000004', 'C', 'It is a routine system check from Meta support.', 3),
('b1c2d3e4-0001-4000-8000-000000000004', 'D', 'Sharing the code is safe as long as it is an image screenshot.', 4)
ON CONFLICT (question_id, option_key) DO NOTHING;
