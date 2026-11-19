# Requirements Traceability Matrix (RTM)
## CyberSaarthi — Cybercrime Awareness and Reporting Portal
**Version:** 1.2 | **Status:** Phase 2 Updated

This matrix maps each requirement specified in [SRS.md](SRS.md) and [PRD.md](PRD.md) to its architectural component, database entities, target implementation phase, test strategy, and current status.

---

## 1. Functional Requirements Traceability

| Req ID | Requirement Description | Component / Layer | Primary DB Tables | Target Phase | Verification / Test Strategy | Status |
|---|---|---|---|---|---|---|
| **FR-01** | User authentication, registration, login/logout, RBAC | Spring Boot (`auth`, `users`), React | `users`, `roles`, `user_roles` | Phase 5 | Unit tests for password hashing; integration tests for JWT auth, duplicate checks, and IDOR prevention | Planned |
| **FR-02** | Browse, filter, and search published awareness content | Spring Boot (`awareness`), React | `awareness_topics` | Phase 6 | Unit tests for topic visibility (DRAFT vs PUBLISHED); UI component tests | Planned |
| **FR-03** | Awareness assessments (quizzes, score calculation, feedback) | Spring Boot (`assessment`), React | `quiz_questions`, `quiz_attempts` | Phase 6 | Deterministic scoring tests; answer integrity checks (correct answers hidden from unsubmitted payloads) | Planned |
| **FR-04** | Structured incident report submission & validation | Spring Boot (`reports`), React | `incident_reports`, `report_status_history` | Phase 7 | Backend validation tests (payload size, no PII/passwords); reference code uniqueness tests; offline ML degradation tests | Planned |
| **FR-05** | ML-assisted categorisation & advisory inference | FastAPI (`inference`), Spring Boot (`classification`) | `ml_predictions`, `incident_categories` | Phase 3, 4, 8 | Contract tests for FastAPI schema; timeout/retry tests; low-confidence fallback & review flag verification | **In Progress** |
| **FR-06** | User report history & application status tracking | Spring Boot (`reports`), React | `incident_reports`, `report_status_history` | Phase 7 | Access control & record ownership tests; chronological status history tests | Planned |
| **FR-07** | Administrator review queue & status management | Spring Boot (`admin`), React | `incident_reports`, `review_notes`, `audit_events` | Phase 7 | RBAC tests (non-admin forbidden); state machine transition validation; audit logging tests; internal notes privacy checks | Planned |
| **FR-08** | Awareness content & taxonomy administration | Spring Boot (`admin`, `awareness`), React | `awareness_topics`, `incident_categories` | Phase 6, 7 | Admin-only mutation tests; taxonomy version immutability verification | Planned |
| **FR-09** | Aggregate operational & research analytics | Spring Boot (`analytics`), React | `incident_reports`, `quiz_attempts`, `audit_events` | Phase 9 | Aggregation accuracy reconciliation; PII exclusion & small-cell suppression tests | Planned |

---

## 2. Non-Functional Requirements Traceability

| Req ID | Area | Requirement Description | Implementation Approach | Target Phase | Verification Strategy | Status |
|---|---|---|---|---|---|---|
| **NFR-01** | Security | Enforce authentication, RBAC, input validation, secure secrets | Spring Security, parameterised JPA queries, `.env` management | Phase 5, 7, 10 | Security audit, authorization boundary testing, automated security scans | Planned |
| **NFR-02** | Privacy | Data minimisation, retention rules, research de-identification | Restrict narrative fields, omit sensitive evidence in MVP, mask exports | Phase 0, 7, 9 | Code review against privacy guidelines, export schema verification | In Progress |
| **NFR-03** | Performance | Sub-second API responses, tracked ML inference latency | Spring connection pools, lightweight scikit-learn models | Phase 4, 8, 10 | API response benchmarking, inference latency percentile measurement | Planned |
| **NFR-04** | Availability & Resilience | Report submission succeeds even when ML service is offline | Asynchronous or graceful fallback to `CLASSIFICATION_PENDING` | Phase 8 | Fault injection tests (stopping FastAPI and submitting reports) | Planned |
| **NFR-05** | Usability | Intuitive UI, responsive design, helpful validation messages | React responsive layouts, clear error banners, accessible controls | Phase 6, 7, 10 | Usability testing against representative user personas | Planned |
| **NFR-06** | Accessibility | Keyboard navigation, high contrast, ARIA labels, semantic HTML | Semantic HTML5, accessible color palette, visible focus states | Phase 6, 7, 10 | Automated accessibility audits (Lighthouse / axe-core) | Planned |
| **NFR-07** | Maintainability | Layered architecture (controller-service-repository), clean code | Spring Boot modular monolith, thin controllers, clean separation | All phases | Code review against `AGENTS.md` guidelines | In Progress |
| **NFR-08** | Portability | Environment-driven configuration across dev and demo | `.env.example`, configurable properties, docker-ready | Phase 0, 5, 11 | Multi-environment validation tests | In Progress |
| **NFR-09** | Auditability | Immutable audit trail for all sensitive administrative actions | `audit_events` table populated on status and category updates | Phase 7 | Audit persistence and tamper-resistance verification tests | Planned |
| **NFR-10** | Reproducibility | Fixed random seeds, logged splits, versioned taxonomy & model artifacts | Experiment logging scripts, pinned dependency versions, model cards | Phase 2, 3, 4 | Independent re-execution of ML training and evaluation pipeline | **In Progress** |

---

## 3. Maintenance and Updates
This RTM is a living document. At the conclusion of each development phase, update:
1. The **Status** column (`Planned` → `In Progress` → `Verified`).
2. The specific test case names and commit hashes validating the requirement.

---

## 4. Phase 1 Deliverables & Evidence Log

| Deliverable | Document Path | Verification Notes & Artifacts | Status |
|---|---|---|---|
| **Literature Review & Gap Matrix** | `docs-research/literature-review/literature-review-matrix.md` | Benchmarked 7 major scholarly publications, identified 4 distinct research gaps (G-1 to G-4), refined RQ-A1 to RQ-B1. | **Verified** |
| **Official Reporting & Disclaimers** | `docs/official-reporting-and-disclaimers.md` | Frozen official Indian helpline numbers (1930), portal links (cybercrime.gov.in, sancharsaathi.gov.in), and standardized UI disclaimer copy. | **Verified** |
| **Incident Taxonomy v1.0** | `docs/taxonomy-v1.0.md` | Formalized 8 mutually exclusive incident categories, keywords, boundaries, and synthetic example narratives. | **Verified** |
| **Product Analysis & UI Wireframes** | `docs/product-analysis-and-wireframes.md` | Documented 4 user roles, detailed 3 core workflows (Mermaid), and produced layout wireframes for all major screens. | **Verified** |

---

## 5. Phase 2 Deliverables & Evidence Log

| Deliverable | Document Path | Notes | Status |
|---|---|---|---|
| **Dataset Candidates Shortlist** | `docs-research/experiments/dataset-candidates.md` | Evaluated 5 candidates; downloaded Sentinel Indian Cybercrime Dataset (MD5: `b8f2d274a27b2384ac84700c4e7fc29e`). | **Verified** |
| **Dataset Audit Script** | `ml/data/audit_dataset.py` | Executed programmatically; audited 14,000 raw rows, zero missing, zero language violations, detected 778 PII rows. | **Verified** |
| **Dataset Audit Report** | `docs-research/experiments/dataset-audit-report.md` | Fully populated with audit metrics; 778 PII rows dropped, 13,222 usable rows (8,400 cyber incidents), dataset approved. | **Verified** |
| **Taxonomy Mapping Specification** | `docs-research/experiments/taxonomy-mapping-v1.0.md` | Mapped and frozen across 4 incident classes + legit exclusion; 8,400 clean samples ready for 70/15/15 split. | **Verified** |
| **Frozen Experiment Protocol** | `docs-research/experiments/experiment-protocol.md` | Frozen: seed=42, stratified split, TF-IDF (1-2 gram, 10k features), 3 models (MNB, LR, Linear SVM), threshold=0.60. | **Verified** |
| **ML Requirements & Virtual Environment** | `ml/training/requirements.txt`, `ml/.venv` | Pinned packages and verified virtual environment (scikit-learn 1.4.2, pandas 2.2.2, numpy 1.26.4, nltk 3.8.1). | **Verified** |
| **Data directory gitignore rules** | `ml/data/raw/.gitignore`, `ml/data/processed/.gitignore`, `ml/data/audit_output/.gitignore`, `ml/training/artifacts/.gitignore` | Raw data, processed splits, and model artifacts excluded from Git. | **Verified** |
