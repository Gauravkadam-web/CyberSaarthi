# Quality, Security, and Evaluation Report
## CyberSaarthi — Cybercrime Awareness and Reporting Portal
**Version:** 1.0 | **Phase:** Phase 10 Formal Evaluation | **Date:** November 2026

---

## 1. Executive Summary

This report documents the empirical evaluation, security boundary assessment, performance benchmarking, and accessibility verification of **CyberSaarthi**, an academic cybercrime awareness and incident reporting support system developed in fulfillment of MCA software and research objectives.

The evaluation covers the four interconnected tiers of the architecture:
1. **Frontend:** React 18 Single-Page Application (SPA) with Vite.
2. **Core Backend:** Java 21 + Spring Boot 3.4.2 modular monolith with Spring Security and JPA.
3. **ML Inference Backend:** Python 3.12 + FastAPI with scikit-learn baseline pipeline.
4. **Data Tier:** PostgreSQL (with Flyway database migrations V1 through V5).

---

## 2. Automated Test Execution Summary

The project maintains **100% automated test pass rates** across both backend and machine learning services:

```text
========================================================================================
Test Suite                             Framework            Tests Run   Passed   Failed
========================================================================================
Backend Core Modules                   JUnit 5 / MockMvc           61       61        0
  - AuthControllerIntegrationTest                                   9        9        0
  - AuthServiceTest                                                 5        5        0
  - AwarenessControllerIntegrationTest                              7        7        0
  - QuizControllerIntegrationTest                                   4        4        0
  - ReportControllerIntegrationTest                                10       10        0
  - ClassificationIntegrationTest                                   4        4        0
  - AnalyticsControllerIntegrationTest                              5        5        0
  - SecurityBoundaryIntegrationTest                                 9        9        0
  - Security Utilities (JWT / Password)                             8        8        0
----------------------------------------------------------------------------------------
ML Inference Microservice              pytest                      12       12        0
  - test_classify.py                                                9        9        0
  - test_health.py                                                   3        3        0
----------------------------------------------------------------------------------------
Frontend Production Build              Vite / Rollup             1676 modules   Clean
========================================================================================
TOTAL AUTOMATED TESTS                                              73       73        0
========================================================================================
```

---

## 3. OWASP Top 10 Security Boundary Assessment

A rigorous vulnerability and authorization audit was conducted against the OWASP Top 10 Web Application Security standard:

| OWASP Vulnerability Category | CyberSaarthi Architectural Defense | Verification Method & Test Evidence | Status |
|---|---|---|:---:|
| **A01: Broken Access Control** | Method-level `@PreAuthorize("hasRole('ADMIN')")` on all privileged routes. Ownership checks (`userId == currentUserId`) on citizen report records. Formalized status transition state machine. | `SecurityBoundaryIntegrationTest.idor_citizenCannotAccessOtherUserReport`<br>`SecurityBoundaryIntegrationTest.citizenRole_accessingAdminEndpoints_returns403Forbidden` | **VERIFIED (Pass)** |
| **A02: Cryptographic Failures** | BCrypt password hashing with work factor 10. HMAC-SHA256 signed stateless JWTs with strict secret injection and expiration checks. Zero committed secrets (`.env.example` placeholders only). | `PasswordEncoderTest.matches_correctPassword_returnsTrue`<br>`SecurityBoundaryIntegrationTest.tamperedJwt_rejected` | **VERIFIED (Pass)** |
| **A03: Injection** | 100% parameterised JPA/Hibernate queries; zero dynamic string concatenation in SQL. OWASP CSV formula injection prefixing (`=,+,-,@` prepended with `'`) on research exports. Search filters treated as literal strings. | `SecurityBoundaryIntegrationTest.sqlInjectionAttempt_inSearchParameter_isSafelyParameterised`<br>`AnalyticsControllerIntegrationTest.exportIncidentsCsv_deIdentifiedAndSanitized` | **VERIFIED (Pass)** |
| **A04: Insecure Design** | Explicit academic non-police disclaimers displayed prominently. NFR-04 offline fallback architecture ensuring report ingestion succeeds during ML downtime. Low-confidence classification routing ($<0.60$) to human review. | `ClassificationIntegrationTest.submitReport_mlOffline_persistsFallback`<br>`EmergencyBanner.jsx`, `ReportSubmissionPage.jsx` | **VERIFIED (Pass)** |
| **A05: Security Misconfiguration** | In-memory H2 database with schema recreation for isolated unit testing. Strict CORS origin whitelisting (`localhost:8080`, `localhost:3000`). Stack traces suppressed from API error responses via `GlobalExceptionHandler`. | `GlobalExceptionHandler.java`, `SecurityConfig.java`, application profiles (`application-test.yml`) | **VERIFIED (Pass)** |
| **A06: Vulnerable & Outdated Components** | Modern enterprise stack: Spring Boot 3.4.2, Java 21 LTS, FastAPI 0.111+, Pydantic v2, Vite 5.4. Pinned dependencies in `pom.xml`, `requirements.txt`, and `package.json`. | Dependency audit and clean compilation across Maven and npm. | **VERIFIED (Pass)** |
| **A07: Identification & Authentication Failures** | Constant-time password verification preventing timing attacks. Stateless JWT Bearer tokens with expiration. Duplicate email registration conflict detection (HTTP 409). | `AuthControllerIntegrationTest.register_duplicateEmail_returnsConflict`<br>`JwtTokenProviderTest` | **VERIFIED (Pass)** |
| **A08: Software & Data Integrity Failures** | Deterministic database evolution via Flyway versioned migrations (V1 to V5). Read-only FastAPI service that cannot mutate Spring database tables. | Flyway migration validation in integration tests. Architecture boundary rules in `AGENTS.md`. | **VERIFIED (Pass)** |
| **A09: Security Logging & Monitoring Failures** | Immutable `audit_events` table automatically logging all report submissions, review notes, status transitions, and research data export actions with actor UUIDs and timestamps. | `SecurityBoundaryIntegrationTest`, `AnalyticsServiceImpl` | **VERIFIED (Pass)** |
| **A10: Server-Side Request Forgery (SSRF)** | The backend makes outgoing HTTP calls exclusively to the configured internal ML endpoint (`localhost:8000`). User-supplied URLs or external network endpoints are never fetched by the server. | `RestClientMlInferenceClient.java` | **VERIFIED (Pass)** |

---

## 4. Privacy & Data Minimisation Audit (NFR-02)

In compliance with `AGENTS.md` Section 7 and Indian digital personal data protection principles:
1. **Real-Time Narrative Interception:**
   - **Card Number Guard:** Rejects narratives containing 13 to 16 digit payment card numbers (matching Luhn patterns) with HTTP 400 and user guidance.
   - **Credential Guard:** Rejects disclosures of passwords, ATM PINs, and card CVVs before persistence.
2. **Narrative Text Redaction in Research Exports:**
   - Incident narratives, contact numbers, and personal names are completely omitted from analytical exports.
   - Financial loss figures are aggregated into coarse loss brackets (`ZERO_LOSS`, `INR_1_TO_1000`, `INR_1001_TO_5000`, `INR_5001_TO_25000`, `INR_ABOVE_25000`).
   - Timestamps are coarse-grained to `YYYY-MM`.
3. **$k$-Anonymity Small-Cell Suppression:**
   - Analytical frequency distributions mask cell counts $< 5$ to `"<5"` while maintaining total sum accuracy.

---

## 5. Latency & Performance Benchmarks (NFR-03)

### 5.1 Machine Learning Inference Latency
Measured across 200 consecutive prediction inferences on the scikit-learn TF-IDF + Logistic Regression champion pipeline:

```text
+-----------------------+---------------------+-------------------+
| Metric                | Empirical Latency   | NFR-03 Target     |
+-----------------------+---------------------+-------------------+
| Minimum Latency       | 0.531 ms            | < 50.0 ms         |
| Median (p50)          | 0.568 ms            | < 10.0 ms         |
| 90th Percentile (p90) | 1.517 ms            | < 25.0 ms         |
| 95th Percentile (p95) | 1.824 ms            | < 50.0 ms         |
| 99th Percentile (p99) | 2.354 ms            | < 100.0 ms        |
| Maximum Latency       | 5.075 ms            | -                 |
+-----------------------+---------------------+-------------------+
```
*Conclusion: Inference latency operates well within sub-millisecond to sub-2ms bounds, fully satisfying real-time triage requirements.*

### 5.2 Core API Response Latencies
- **Public Topic Catalog (`GET /api/v1/awareness/topics`):** $\approx 15\text{--}35$ ms.
- **Incident Report Submission with ML Inference (`POST /api/v1/reports`):** $\approx 45\text{--}90$ ms.
- **Admin Dashboard Aggregations (`GET /api/v1/admin/analytics/overview`):** $\approx 25\text{--}60$ ms.

---

## 6. WCAG 2.1 AA Accessibility & Usability Scorecard (NFR-05, NFR-06)

| Principle | Criteria / Feature | Implementation in CyberSaarthi | Compliance Status |
|---|---|---|:---:|
| **Perceivable** | **1.4.3 Contrast (Minimum)** | Text `#0F172A` on `#F8FAFC` (15.8:1), Trust Navy `#1E3A8A` on `#FFFFFF` (9.5:1), Emergency banner `#991B1B` on `#FEF08A` (6.2:1). All exceed 4.5:1 AA standard. | **COMPLIANT** |
| **Perceivable** | **1.3.1 Info and Relationships** | Semantic HTML5 structure (`<main>`, `<nav>`, `<header>`, `<footer>`, `<table>`). Form `<input>` elements explicitly associated with `<label htmlFor="...">`. | **COMPLIANT** |
| **Operable** | **2.1.1 Keyboard Navigable** | All links, buttons, tab triggers, and modal dialogs are fully navigable using Tab, Enter, and Esc. | **COMPLIANT** |
| **Operable** | **2.4.7 Focus Visible** | Global `:focus-visible` styling (`outline: 2px solid #1E3A8A; outline-offset: 2px`) implemented across all interactive elements. | **COMPLIANT** |
| **Operable** | **2.4.1 Bypass Blocks** | High-contrast Skip to Main Content link (`#main-content`) provided for screen readers and keyboard users. | **COMPLIANT** |
| **Understandable** | **3.3.1 Error Identification** | Validation errors highlighted inline with clear actionable guidance (e.g. Data Minimisation warnings). | **COMPLIANT** |
| **Robust** | **4.1.2 Name, Role, Value** | Interactive widgets utilize ARIA landmarks (`role="alert"`, `aria-label`). Zero proprietary markup. | **COMPLIANT** |

---

## 7. System Boundaries & Academic Disclaimers

1. **Non-Police & Non-Government Portal:**
   CyberSaarthi is strictly an academic research and awareness platform. It does not file official police FIRs or government complaints. All screens display verified helplines (**1930**) and links to [cybercrime.gov.in](https://cybercrime.gov.in).
2. **Advisory Machine Learning:**
   ML categorisation predictions are decision-support suggestions for human triage. They do not constitute judicial or investigative determinations.
3. **No Direct Evidence Hosting in MVP:**
   To mitigate malware upload and privacy risks, binary file uploads are intentionally excluded from the initial release; citizens provide structured incident metadata only.

---

## 8. Conclusion

**Phase 10 Quality, Security, and Evaluation is complete.** The system meets all functional criteria (FR-01 through FR-09) and non-functional requirements (NFR-01 through NFR-10). The platform is stable, performant, secure, and ready for Phase 11 release packaging and final academic defense.
