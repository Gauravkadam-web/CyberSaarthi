# Software Requirements Specification (SRS)
## CyberSaarthi — Cybercrime Awareness and Reporting Portal
**Version:** 1.0 | **Status:** Baseline draft

## 1. Introduction

### 1.1 Purpose
This SRS defines functional and non-functional requirements for CyberSaarthi. It is intended for the student development team, academic supervisor, testers, and evaluators.

### 1.2 Product scope
CyberSaarthi provides cybercrime awareness, structured incident documentation, ML-assisted categorisation, application-level report tracking, administrator review, and aggregate research analytics. It is not an official complaint filing or law-enforcement platform.

### 1.3 Definitions
- **Incident report:** User-submitted description of a suspected cyber incident.
- **Suggested category:** ML-generated category recommendation.
- **Reviewer:** Authorised administrator who checks reports and may correct classifications.
- **Taxonomy:** Versioned set of incident categories.
- **Macro-F1:** Arithmetic mean of per-class F1 scores.
- **PII:** Personally identifiable information.

## 2. Overall Description

### 2.1 User classes
1. Guest: reads public awareness content and official guidance.
2. Registered user: submits reports, completes quizzes, and tracks own reports.
3. Administrator: reviews reports, manages content, and accesses permitted analytics.
4. Research evaluator: accesses approved, de-identified research outputs only.

### 2.2 Operating environment
- Web client: React.
- Application API: Java with Spring Boot.
- ML inference API: Python with FastAPI.
- Database: PostgreSQL.
- Browser support: current versions of major desktop and mobile browsers.
- Deployment configuration must be environment-driven.

### 2.3 Constraints
- Two-member student team.
- Academic prototype, not production-grade public infrastructure.
- No direct integration with police or government complaint systems.
- Dataset and participant research require rights, provenance, and ethics checks.

### 2.4 Assumptions
Users have internet access and a modern browser. Administrators are provisioned through a controlled process. ML classification may be unavailable independently of the core reporting service.

## 3. Functional Requirements

### FR-01: Authentication and authorisation
**Description:** The system shall support registration and authentication for registered users and administrators.

**Requirements**
- FR-01.1: The system shall validate registration fields and reject duplicate account identifiers.
- FR-01.2: Passwords shall be stored using a suitable adaptive password-hashing algorithm; plaintext passwords shall never be stored.
- FR-01.3: Users shall be able to log in and log out.
- FR-01.4: Protected endpoints shall enforce server-side authorisation.
- FR-01.5: A user shall only access their own reports and profile data.
- FR-01.6: Administrator privileges shall not be self-assigned through public registration.
- FR-01.7: Authentication failures shall not reveal whether a particular account exists.

**Acceptance criteria:** A registered user can authenticate; unauthorised users cannot access protected resources; a user cannot retrieve another user's report by changing an identifier.

### FR-02: Awareness content
- FR-02.1: Guests and registered users shall be able to browse published awareness topics.
- FR-02.2: Each topic shall support a title, summary, body, category/tags, and publication state.
- FR-02.3: Users shall be able to search or filter topics using available metadata.
- FR-02.4: Draft or unpublished content shall not be visible to ordinary users.
- FR-02.5: Content shall distinguish warning signs, preventive steps, and recommended response actions.

**Acceptance criteria:** Published content is visible; draft content is restricted; topic details render on mobile and desktop.

### FR-03: Awareness assessments
- FR-03.1: The system shall present a quiz associated with an awareness topic or module.
- FR-03.2: The system shall validate submitted answers.
- FR-03.3: The system shall calculate and display a score and explanatory feedback.
- FR-03.4: The system may retain a user's attempt history subject to the documented privacy policy.
- FR-03.5: Research pre-test and post-test records shall be linked using a pseudonymous study identifier where approved.

**Acceptance criteria:** A completed quiz produces a reproducible score; invalid or incomplete submissions are handled with clear messages.

### FR-04: Incident report submission
- FR-04.1: Authenticated users shall be able to submit an incident report.
- FR-04.2: The form shall collect only fields justified by the reporting workflow, such as incident description, approximate date, incident channel/type, and optional non-sensitive context.
- FR-04.3: The form shall warn users not to submit passwords, OTPs, PINs, full payment-card details, or unnecessary identity documents.
- FR-04.4: The server shall validate all submitted fields.
- FR-04.5: A successful submission shall receive a unique reference identifier.
- FR-04.6: The report shall be assigned an initial status such as `SUBMITTED`.
- FR-04.7: The user shall receive a confirmation that the report is recorded within CyberSaarthi, not officially filed with an authority.
- FR-04.8: Report submission shall remain possible if the ML service is unavailable.

**Acceptance criteria:** Valid submissions persist and return a reference; invalid submissions return field-level errors; ML downtime does not discard a valid report.

### FR-05: ML-assisted categorisation
- FR-05.1: The application shall send the minimum necessary incident text and request metadata to the ML inference service.
- FR-05.2: The ML service shall return a predicted category, model version, taxonomy version, and an appropriate score or uncertainty indicator.
- FR-05.3: The application shall treat the prediction as advisory.
- FR-05.4: Low-confidence, unsupported, or failed predictions shall be marked for manual review or `UNCLASSIFIED`.
- FR-05.5: An administrator shall be able to correct the suggested category.
- FR-05.6: The system shall retain both the original suggestion and the reviewed category where required for audit and evaluation.
- FR-05.7: The system shall not infer legal guilt, identity, or authenticity of a claim.

**Acceptance criteria:** A valid text request returns a taxonomy-consistent result; invalid input is rejected; low-confidence output follows the configured review policy.

### FR-06: Report tracking
- FR-06.1: A user shall view a list of their own reports.
- FR-06.2: A user shall view report details and current status.
- FR-06.3: The system shall display a chronological status history where available.
- FR-06.4: Status changes shall be made only by authorised roles.
- FR-06.5: The interface shall explain that statuses represent internal portal workflow, not police or court progress.

**Acceptance criteria:** Users see only their reports; status history is ordered; direct API access is subject to the same authorisation rules.

### FR-07: Administrator review
- FR-07.1: Administrators shall view a paginated review queue.
- FR-07.2: Administrators shall open report details according to least-privilege access.
- FR-07.3: Administrators shall confirm or correct the suggested category.
- FR-07.4: Administrators shall update status using an approved state transition.
- FR-07.5: Administrators shall add an internal review note that is not exposed to the reporting user unless explicitly designated as user-visible.
- FR-07.6: Sensitive actions shall be recorded in an audit log.

**Acceptance criteria:** Non-admin users cannot access the queue or update reports; each permitted change records actor and timestamp.

### FR-08: Content and taxonomy administration
- FR-08.1: Administrators shall create, edit, publish, unpublish, and archive awareness content.
- FR-08.2: Content changes shall record author and timestamps.
- FR-08.3: Taxonomy changes shall be versioned and shall not silently relabel historical model results.
- FR-08.4: Deletion of referenced categories shall be prevented or handled through a documented migration.

**Acceptance criteria:** Only authorised administrators can change content; published state controls public visibility; taxonomy version is traceable.

### FR-09: Analytics and research exports
- FR-09.1: Administrators shall view aggregate report counts by permitted dimensions.
- FR-09.2: Analytics shall avoid exposing direct identifiers or free-text report content by default.
- FR-09.3: Research exports shall use de-identified or pseudonymised records and be access-controlled.
- FR-09.4: The system shall document export filters, date ranges, and data dictionary.
- FR-09.5: Small-cell suppression or aggregation shall be considered where re-identification risk exists.

**Acceptance criteria:** Dashboard totals reconcile with permitted source records; export contains no direct identifiers unless explicitly approved and justified.

## 4. Report Status Model

Suggested initial states:
- `SUBMITTED`
- `CLASSIFICATION_PENDING`
- `UNDER_REVIEW`
- `NEEDS_INFORMATION`
- `REVIEWED`
- `CLOSED`

Allowed transitions must be implemented as an explicit state machine. For example, `SUBMITTED` may move to `CLASSIFICATION_PENDING` or `UNDER_REVIEW`; terminal or closed states must not be reopened without an authorised, audited action. The team must finalise transitions before implementation.

## 5. Data Requirements

Core entities are expected to include users, roles, awareness topics, quiz questions, quiz attempts, incident reports, report status history, incident categories/taxonomy versions, ML prediction records, review notes, and audit events.

Data fields, relationships, indexes, retention, and constraints are specified in `database-design.md`.

## 6. External Interface Requirements

### 6.1 User interface
Responsive web UI with clear navigation, form validation, accessible labels, loading/error/empty states, and persistent official-reporting disclaimer.

### 6.2 Spring Boot API
RESTful JSON API. Endpoints must be versioned, documented, validated, and protected according to role and ownership.

### 6.3 FastAPI inference interface
Internal service endpoint for text classification. It shall validate payload size, reject empty or excessively long input, return a stable schema, and avoid logging raw report text by default.

### 6.4 Database interface
Spring Boot is the system of record and owns transactional writes. The ML service should not directly mutate application tables.

## 7. Non-Functional Requirements

| ID | Area | Requirement |
|---|---|---|
| NFR-01 | Security | Enforce authentication, authorisation, validation, secure password storage, and safe secrets management |
| NFR-02 | Privacy | Minimise collection, document purpose/retention, restrict access, and de-identify research outputs |
| NFR-03 | Performance | Establish measurable targets and benchmark API and inference separately |
| NFR-04 | Availability | Core report submission shall degrade gracefully if ML inference is unavailable |
| NFR-05 | Usability | Forms and guidance shall be understandable and responsive |
| NFR-06 | Accessibility | Use semantic controls, keyboard navigation, visible focus, and sufficient contrast |
| NFR-07 | Maintainability | Separate presentation, application, persistence, and inference responsibilities |
| NFR-08 | Portability | Configure environments through variables and document setup |
| NFR-09 | Auditability | Record security-relevant administrative changes |
| NFR-10 | Reproducibility | Record dataset, split, seed, parameters, taxonomy, and model versions |

## 8. Security and Privacy Requirements

- Apply least privilege and deny access by default.
- Validate input on the server; encode output appropriately.
- Use parameterised persistence operations.
- Protect against CSRF where cookie-based authentication is used; apply secure token handling if bearer tokens are used.
- Rate-limit authentication and report submission where appropriate.
- Do not place secrets in source control.
- Avoid logging credentials, tokens, raw sensitive narratives, or uploaded evidence.
- Define retention and deletion procedures before collecting real personal data.
- Use synthetic or properly de-identified data for demonstrations and model experiments whenever possible.
- Conduct dependency, authorisation, and input-validation testing.

## 9. Error Handling

The API shall return consistent error responses with a machine-readable code, human-readable message, and optional field details. Internal stack traces and secrets must not be returned to clients. ML service timeouts or failures shall create a retryable/pending classification state without losing the report.

## 10. Acceptance and Verification

Each requirement must map to at least one test or demonstrable verification. Maintain a requirements traceability matrix linking requirement ID, implementation component, test case, and evidence. Final acceptance requires supervisor/team review, functional tests, security checks, and documented limitations.
