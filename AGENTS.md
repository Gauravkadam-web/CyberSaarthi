# AGENTS.md — CyberSaarthi Project Instructions

## 1. Project Identity

- Academic project title: **Cybercrime Awareness and Reporting Portal**.
- Product name: **CyberSaarthi**.
- Tagline: **Your Guide to a Safer Digital World.**
- Do not rename the approved academic project title.
- This is a two-member MCA academic project with both software and research objectives.

## 2. Product Purpose and Boundaries

CyberSaarthi is a cybercrime-awareness and incident-reporting support portal. It provides educational content, structured incident documentation, ML-assisted categorisation, report tracking, administrator review, and aggregate research analytics.

It is not:
- A government portal or official complaint filing system.
- A police investigation or emergency response system.
- A legal-advice service.
- A money recovery or banking service.
- A tool for identifying or accusing alleged offenders.

The UI must clearly communicate these boundaries. Do not imply that submitting a report to CyberSaarthi submits it to police or a government authority.

For India-specific urgent financial cyber fraud guidance, provide verified links to the official National Cyber Crime Reporting Portal and 1930 helpline. Do not invent or alter official contact details.

## 3. Approved Technology Direction

- Frontend: React.
- Core backend: Java + Spring Boot.
- ML inference backend: Python + FastAPI.
- Database: PostgreSQL.
- APIs: REST/JSON with versioning and OpenAPI documentation.

Do not introduce a new major framework, database, queue, cloud service, or architectural pattern without explaining the need and recording an architecture decision.

## 4. Architecture Rules

- React communicates with Spring Boot for application workflows.
- Spring Boot is the system of record and owns application database writes.
- FastAPI is responsible for model inference only.
- The ML service must not directly mutate application database tables.
- The frontend must not call internal ML endpoints directly.
- Keep the Spring application modular and layered: controller, service, repository, DTO/schema, and domain responsibilities.
- Keep controllers thin and business rules in services.
- Do not introduce microservices beyond the agreed FastAPI ML boundary without approval.
- Report submission must not fail solely because ML inference is unavailable.

## 5. Requirements and Documentation

Before implementing a feature:
1. Read `docs/PRD.md` and `docs/SRS.md`.
2. Read `docs/architecture.md` and `docs/database-design.md` where relevant.
3. Check the development and research roadmaps.
4. Identify the requirement ID and acceptance criteria.
5. Update documentation if the implementation reveals a necessary change.

Do not silently change product scope, taxonomy, status names, API contracts, or database relationships. Record material decisions and explain trade-offs.

## 6. ML and Research Integrity

- Initial taxonomy has eight categories as documented in the PRD.
- Candidate baseline models are Multinomial Naive Bayes, Logistic Regression, and Linear SVM.
- Do not invent dataset provenance, licence, sample counts, metrics, or model performance.
- Do not use the held-out test set for model tuning.
- Fit learned preprocessing only on training data.
- Check duplicate/near-duplicate leakage and group leakage where applicable.
- Report macro-F1, weighted-F1, per-class precision/recall/F1, confusion matrix, and latency.
- Record seed, data version, split, parameters, software versions, taxonomy version, and model version.
- Treat model output as an advisory category suggestion, never a verified fact or legal conclusion.
- Do not call an uncalibrated score a calibrated confidence.
- Low-confidence, unsupported, or failed predictions must be routed to manual review or unclassified handling.
- Preserve negative and inconclusive results. Never fabricate participants, approvals, data, or results.

## 7. Security and Privacy Rules

- Never store plaintext passwords.
- Never commit API keys, passwords, tokens, private keys, database credentials, or production secrets.
- Use environment variables and provide safe `.env.example` files containing placeholders only.
- Enforce backend role checks and record ownership checks on every protected operation.
- Validate input server-side and constrain payload sizes.
- Use parameterised database access.
- Do not log passwords, tokens, OTPs, PINs, full card details, or raw sensitive report narratives.
- Collect the minimum personal information required.
- Avoid evidence uploads in the MVP unless secure storage, access control, retention, and deletion are designed and tested.
- Use synthetic or properly de-identified data for development and demonstrations.
- Keep internal review notes private from ordinary users.
- Do not expose direct identifiers in analytics or research exports by default.
- Define retention and deletion requirements before collecting real incident data.

## 8. API and Data Rules

- Use request/response DTOs; do not expose persistence entities directly.
- Validate all inbound payloads.
- Return consistent error schemas; never expose stack traces.
- Version public API routes.
- Document endpoint permissions and ownership rules.
- Use database migrations for schema changes.
- Do not rely only on frontend validation.
- Keep category codes, display names, and taxonomy version consistent across DB, Spring Boot, FastAPI, frontend, and research reports.
- Use explicit status transitions; do not allow arbitrary status updates.

## 9. Testing Rules

Add tests for:
- Business logic and validation.
- Authentication and role permissions.
- Report ownership and IDOR-style access attempts.
- Status transition rules.
- ML request/response contract.
- ML timeout, unavailable service, and malformed response.
- Taxonomy mismatch and low-confidence routing.
- Quiz scoring.
- Data access and migration behaviour.
- Sensitive data not appearing in API responses or logs where testable.

Do not mark a phase complete based only on a successful local startup. Follow the exit criteria in `docs/development-roadmap.md`.

## 10. Code Quality

- Prefer readable, maintainable code over unnecessary abstraction.
- Keep functions and classes focused.
- Use meaningful names and consistent formatting.
- Avoid duplicated business logic.
- Handle errors explicitly.
- Add comments for non-obvious decisions, not for every line.
- Do not add dependencies without a clear reason.
- Keep generated files, build outputs, local databases, model binaries, and secrets out of Git unless explicitly approved.

## 11. Git Workflow

- Work on focused feature or documentation branches.
- Do not rewrite shared history or force-push without explicit approval.
- Do not commit unrelated changes.
- Inspect `git status` and diffs before committing.
- Use meaningful commit messages.
- Update README and relevant docs when setup or behaviour changes.
- Never delete user-authored files or replace an existing design without checking first.

## 12. Antigravity Execution Behaviour

When asked to implement a phase:
1. Inspect the existing repository before editing.
2. Report current branch, working-tree state, existing files, and relevant code.
3. Identify conflicts with the approved docs.
4. Propose a short implementation plan.
5. Implement only the requested phase and its dependencies.
6. Run relevant tests and report exact commands/results.
7. Review security, privacy, and maintainability.
8. Update documentation and phase evidence.
9. Do not claim tests passed unless they were actually run.
10. Stop at the phase boundary and wait for review before starting the next phase.

If requirements are ambiguous or conflict with these documents, ask for clarification rather than inventing a decision.

## 13. Completion Report Format

At the end of a phase, report:
- Summary of changes.
- Files created/modified.
- Requirements addressed.
- Architecture/database decisions.
- Commands and tests run with actual outcomes.
- Known issues and limitations.
- Security/privacy considerations.
- Git status and commit details, if applicable.
- Recommended next phase (do not begin it automatically).
