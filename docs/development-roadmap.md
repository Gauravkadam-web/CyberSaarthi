# Development Roadmap
## CyberSaarthi — Phase-Based Implementation Plan
**Version:** 1.0 | **Status:** Proposed baseline

## 1. Development Principles

- Build from approved requirements and architecture.
- Keep each phase reviewable and demonstrable.
- Do not begin a later phase while critical design questions remain unresolved.
- Keep documentation, code, tests, and decisions in version control.
- Use synthetic data until privacy and data-governance requirements are ready.
- Treat ML as an advisory subsystem; reporting must remain resilient without it.

## 2. Technology Baseline

- Frontend: React.
- Core backend: Java + Spring Boot.
- ML inference: Python + FastAPI.
- Database: PostgreSQL.
- API style: versioned REST/JSON.
- API documentation: OpenAPI/Swagger.
- Testing: frontend test framework, JUnit/Spring testing, pytest.
- Source control: Git and GitHub.

Exact framework versions, build tools, migration tooling, and deployment provider are implementation decisions to record before coding.

## 3. Phases

### Phase 0 — Project foundation
**Work**
- Create repository and agreed folder structure.
- Add PRD, SRS, architecture, database design, research proposal, and roadmaps.
- Add `AGENTS.md` and contribution rules.
- Configure branch strategy, issue templates, and environment examples.
- Create requirements traceability matrix.

**Exit criteria**
- Both members can run the documented development environment.
- Documentation is reviewed and project scope is frozen for MVP.

### Phase 1 — Literature review and product analysis
**Work**
- Complete literature review and research-gap matrix.
- Finalise user roles, workflows, taxonomy draft, and UI wireframes.
- Confirm official reporting links and disclaimer wording.

**Exit criteria**
- Supervisor review of proposal, taxonomy direction, and scope.

### Phase 2 — Dataset audit and ML protocol
**Work**
- Verify candidate dataset provenance and licence.
- Audit quality, class distribution, duplicates, and taxonomy mapping.
- Define split, metrics, baselines, and reproducibility protocol.

**Exit criteria**
- Dataset and experiment protocol approved before model comparison.

### Phase 3 — ML baseline development
**Work**
- Build preprocessing pipeline.
- Train candidate classical models.
- Tune only on training/validation data.
- Evaluate on held-out test data.
- Save model artifact and metadata.

**Exit criteria**
- Reproducible metrics, error analysis, and model selection report.

### Phase 4 — FastAPI inference service
**Work**
- Implement health and prediction endpoints.
- Load model once at startup.
- Validate payload and taxonomy.
- Return category and model metadata.
- Add unit/API tests and service documentation.

**Exit criteria**
- Tests pass; schema matches the agreed contract; invalid inputs and model-unavailable states are handled.

### Phase 5 — Spring Boot foundation
**Work**
- Create application structure and configuration.
- Configure PostgreSQL and migrations.
- Implement common error handling and validation.
- Add authentication and role-based access.
- Publish OpenAPI documentation.

**Exit criteria**
- Authentication and protected endpoint tests pass; migrations work on a clean database.

### Phase 6 — Awareness and assessment
**Work**
- Implement published awareness topics.
- Add admin content management.
- Implement quiz retrieval, scoring, and attempt storage.
- Add responsive frontend screens.

**Exit criteria**
- Public visibility rules and quiz scoring are tested.

### Phase 7 — Incident reporting and tracking
**Work**
- Implement report submission and validation.
- Generate reference identifiers.
- Implement user report list/detail and status history.
- Implement admin review queue and controlled status transitions.
- Add audit events.

**Exit criteria**
- Ownership, role checks, status transitions, and audit tests pass.

### Phase 8 — ML integration
**Work**
- Implement Spring-to-FastAPI client.
- Persist prediction metadata.
- Add timeouts, retry/idempotency policy, and pending state.
- Add low-confidence/manual-review flow.
- Display advisory category in UI.

**Exit criteria**
- Report submission remains successful when ML is offline; prediction version and taxonomy are traceable.

### Phase 9 — Admin dashboard and analytics
**Work**
- Add aggregate counts and filters.
- Add de-identified research export if approved.
- Apply access controls and small-cell privacy safeguards.
- Verify aggregate reconciliation.

**Exit criteria**
- Dashboard figures are traceable and do not expose unnecessary personal data.

### Phase 10 — Quality, security, and evaluation
**Work**
- Run functional, integration, contract, and UI tests.
- Test authorisation boundaries and common web vulnerabilities.
- Test failure modes, performance, and accessibility.
- Conduct usability evaluation under an appropriate protocol.
- Fix critical and high-severity defects.

**Exit criteria**
- Test report signed off; limitations and unresolved risks documented.

### Phase 11 — Release, documentation, and publication
**Work**
- Prepare demo environment and synthetic demo data.
- Complete user guide, setup guide, API docs, architecture, and final report.
- Prepare research manuscript and presentation.
- Tag a release and archive reproducibility artifacts.

**Exit criteria**
- Clean installation/demo is reproducible; both members can explain architecture, ML, limitations, and research results.

## 4. Suggested Repository Structure

```text
cybersaarthi/
├── AGENTS.md
├── README.md
├── docs/
│   ├── PRD.md
│   ├── SRS.md
│   ├── architecture.md
│   ├── database-design.md
│   ├── research-proposal.md
│   ├── research-roadmap.md
│   └── development-roadmap.md
├── frontend/
├── backend/
├── ml/
│   ├── data/
│   ├── training/
│   ├── inference/
│   ├── tests/
│   └── reports/
├── docs-research/
│   ├── literature-review/
│   └── experiments/
└── scripts/
```

This is a suggested structure. The team may adjust it before Phase 0 is frozen, but must keep responsibilities clear and avoid duplicate documentation.

## 5. Branch and Review Workflow

Suggested branches:
- `main`: stable, reviewed work.
- `develop`: integration branch if the team needs it.
- `feature/<short-name>`: focused feature branches.
- `docs/<short-name>`: documentation-only changes.

For each change:
1. Create an issue with acceptance criteria.
2. Create a focused branch.
3. Implement and test.
4. Review the diff and security implications.
5. Merge only after checks pass.
6. Update docs and traceability evidence.

For a two-member team, each member should review the other's major changes.

## 6. Definition of Done

- Requirement and acceptance criteria satisfied.
- Code formatted and reviewed.
- Relevant automated tests pass.
- Authorisation and privacy considered.
- Error and empty states handled.
- API/schema documentation updated.
- No secrets or real personal data committed.
- Demonstration evidence captured.
- Known limitations recorded.

## 7. Release Strategy

Use milestone releases rather than deploying every incomplete feature publicly:
- Foundation release: environment, docs, authentication.
- Functional MVP: awareness, quiz, reporting, tracking, admin review.
- ML release: inference integration and review handling.
- Evaluation release: analytics, testing, and research outputs.

Do not advertise the prototype as an official cybercrime reporting service.
