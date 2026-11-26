# CyberSaarthi — User Guide & Administrator Handbook
**Product Name:** CyberSaarthi  
**Tagline:** Your Guide to a Safer Digital World  
**Document Version:** 1.0 | **Release:** v1.0.0 (MCA Project)  

---

## 1. Important Notice & Boundaries

> [!IMPORTANT]
> **CyberSaarthi is an academic cybercrime awareness, structured incident documentation, and research portal.**  
> It is **NOT** a government portal, police FIR filing system, or emergency response service. Submitting an incident report to CyberSaarthi does **not** register a police complaint.
>
> If you have been a victim of **financial cyber fraud**, immediately:
> - Call the **National Cyber Crime Helpline:** **1930**
> - File an official complaint on the government portal: **[https://cybercrime.gov.in](https://cybercrime.gov.in)**
> - Report suspected mobile numbers on Sanchar Saathi: **[https://sancharsaathi.gov.in](https://sancharsaathi.gov.in)**

---

## 2. Citizen User Guide

### 2.1 Account Registration & Login
1. Navigate to the portal homepage: `http://localhost:3000`.
2. Click **Register** on the top navigation bar.
3. Provide your Full Name, Email, and a password (minimum 8 characters, containing uppercase, lowercase, numbers, and special characters).
4. Once registered, log in using your credentials. A secure JWT token will be issued for your session.

### 2.2 Exploring the Awareness Hub (Track B)
1. Click **Awareness Hub** from the top navigation bar (`/awareness`).
2. Filter topics by category (Financial Fraud, Phishing, Social Media Safety, Malware, etc.) or use the search bar.
3. Click **Read Full Guide** to access comprehensive prevention checklists, warning signs, and official response steps.

### 2.3 Taking Educational Quizzes
1. At the bottom of each awareness guide, click **Take Knowledge Check** (`/quizzes/{topicId}`).
2. Answer the multiple-choice questions (MCQs).
3. Click **Submit Assessment**.
4. View your instant score percentage, passing status ($\ge 70\%$ to pass), and detailed explanations for each question.
5. Review your assessment history under **My Assessments** (`/my-attempts`).

### 2.4 Submitting an Incident Report (Track A)
1. Click the red **Report Incident** button (`/reports/new`).
2. Read and acknowledge the mandatory non-police academic disclaimer.
3. Complete the structured incident details:
   - **Delivery Channel:** Select the primary digital vector (UPI App, WhatsApp, Phone Call, SMS, Email, Fake Website, etc.).
   - **Incident Date:** Specify the date the event occurred.
   - **Financial Loss:** Enter the amount lost in Indian Rupees (INR) or enter `0` if no money was lost.
   - **Incident Description:** Describe what transpired.
     > [!WARNING]
     > **Data Minimisation Guard:** Do **NOT** disclose 16-digit payment card numbers, CVVs, ATM PINs, or banking passwords. CyberSaarthi will reject submissions disclosing sensitive financial credentials.
4. Click **Submit Report**.
5. You will receive a unique Crockford Base32 tracking code (e.g., `CS-2026-7K8M2`). Save this code for future inquiries.

### 2.5 Tracking Incident Reports
1. Navigate to **My Reports** (`/reports`) to view all reports submitted by your account.
2. Click **View Report Details** (`/reports/{id}`) to inspect:
   - Current review status (`SUBMITTED`, `UNDER_REVIEW`, `NEEDS_INFORMATION`, `REVIEWED`, `CLOSED`).
   - Chronological status transition history.
   - Official guidance notes left by portal review officers.
   - Advisory machine learning category suggestions.

---

## 3. Administrator & Reviewer Handbook

### 3.1 Administrator Login
1. Log in with an administrator account (Demo: `admin@cybersaarthi.org` / `CyberSaarthi@123`).
2. Administrators receive privileged navigation links:
   - **Review Queue** (`/admin/reports`)
   - **Analytics Dashboard** (`/admin/dashboard`)

### 3.2 Operating the Incident Review Queue
1. Open **Review Queue** (`/admin/reports`).
2. Filter reports by status (`SUBMITTED`, `UNDER_REVIEW`, `NEEDS_INFORMATION`, `REVIEWED`, `CLOSED`) or enter search terms to locate specific incidents.
3. Click **Review** on any report to open the comprehensive triage modal:
   - **Incident Summary & Narrative:** Review the citizen's submission and timeline.
   - **ML Advisory Intelligence:**
     - Displays the predicted category and calibrated confidence percentage.
     - Green badge indicates High Confidence ($\ge 80\%$).
     - Amber badge indicates Moderate Confidence ($60\text{--}79\%$).
     - Red badge indicates Low Confidence ($< 60\%$), requiring mandatory human discretion.
     - Click **Apply ML Suggestion** to auto-assign the verified category.
     - Click **Re-run ML Inference** to query the model on-demand if the report was ingested during offline downtime.
4. **Transitioning Report Status:**
   - Adhere to the state transition matrix:
     - `SUBMITTED` $\to$ `UNDER_REVIEW`
     - `UNDER_REVIEW` $\to$ `NEEDS_INFORMATION` | `REVIEWED` | `CLOSED`
     - `NEEDS_INFORMATION` $\to$ `UNDER_REVIEW` | `CLOSED`
     - `REVIEWED` $\to$ `CLOSED`
   - Provide a mandatory transition reason (minimum 5 characters) for the immutable audit trail.
5. **Attaching Review Notes:**
   - **USER_VISIBLE:** Notes shown to the citizen on their report tracking page (e.g. emergency helpline advice).
   - **INTERNAL:** Confidential officer notes hidden from the citizen.

---

## 4. Operational Intelligence & Analytics Dashboard

Access the administrator analytics suite via `/admin/dashboard`:

### 4.1 Executive KPI Cards
- **Total Reports:** Overall volume of incidents submitted.
- **Documented Loss:** Cumulative financial loss reported in INR.
- **Quiz Assessments:** Total quizzes attempted and overall learner pass rate.
- **ML Diagnostic Rate:** Percentage of reports successfully analyzed by the inference engine.
- **High-Confidence Predictions:** Volume of predictions meeting $\ge 80\%$ confidence.

### 4.2 Analytical Tabs
1. **Operational & Incident Analytics:**
   - Category distribution across all 8 Taxonomy v1.0 classes with $k=5$ small-cell count masking.
   - Digital delivery channel vector breakdowns.
2. **Awareness & Quiz Assessments:**
   - Topic-by-topic mastery progress bars, pass rates, and learner volume.
3. **ML Telemetry & Diagnostics:**
   - Active champion model specification (`LogisticRegression-TFIDF`, baseline-v1.0).
   - Live vs Fallback inference ratios.
   - Administrator reclassification counts and confidence distribution bands.
4. **Research Data Export Center:**
   - One-click CSV downloads for de-identified incident datasets and quiz assessment records.
   - Strict privacy safeguards: Zero direct PII, coarse timestamps (`YYYY-MM`), financial loss brackets, and OWASP formula injection escaping (`=,+,-,@`).
   - Every export action is immutably logged in the audit ledger.

---

## 5. Demonstration Accounts

For viva examinations, evaluations, and local demonstrations:

| Role | Email Address | Password | Permissions |
|---|---|---|---|
| **Administrator** | `admin@cybersaarthi.org` | `CyberSaarthi@123` | Full access to review queue, status transitions, review notes, analytics, and CSV exports. |
| **Citizen User** | `citizen@cybersaarthi.org` | `CyberSaarthi@123` | Submit reports, view own report history, take quizzes, view awareness content. |
| **Student Learner** | `student@cybersaarthi.org` | `CyberSaarthi@123` | Educational quiz learner profile. |
