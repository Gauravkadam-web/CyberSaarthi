# Contribution & Development Guidelines
## CyberSaarthi — Cybercrime Awareness and Reporting Portal

Welcome to the CyberSaarthi project. This document outlines the development conventions, Git branching strategy, coding standards, and review processes for our two-member MCA academic team.

---

## 1. Core Principles

1. **Academic and Ethical Integrity:** Never commit fabricated evaluation data, fake metrics, unverified licenses, or real victim narratives.
2. **Security by Design:** Never commit passwords, tokens, API keys, or production database credentials. Use `.env.example` templates with placeholders only.
3. **Traceability:** Every major feature, model experiment, or database change must trace back to a requirement in [SRS.md](docs/SRS.md) and be recorded in [traceability-matrix.md](docs/traceability-matrix.md).
4. **Peer Review:** In a two-member team, every major PR must be reviewed by the other team member before merging into `main` or `develop`.

---

## 2. Git Branch Strategy

* `main` — Production/stable branch. Contains only thoroughly tested, supervisor-demonstrable code and finalized documentation.
* `develop` — Shared integration branch for active development.
* `feature/<short-name>` — Feature-specific branches (e.g., `feature/user-auth`, `feature/report-submission`).
* `ml/<short-name>` — ML training, pipeline, or experiment branches (e.g., `ml/baseline-models`, `ml/fastapi-service`).
* `docs/<short-name>` — Documentation-only changes (e.g., `docs/literature-review`, `docs/api-specs`).
* `hotfix/<short-name>` — Urgent bug fixes applied directly to stable branches.

---

## 3. Commit Message Standards

We follow the Conventional Commits format:

```text
<type>(<scope>): <short description in present tense>

[optional body explaining motivation and changes]

[optional footer referencing issue or requirement ID]
```

### Types:
- `feat`: A new feature (e.g., `feat(auth): implement jwt token generation`)
- `fix`: A bug fix (e.g., `fix(reports): resolve null pointer on optional date`)
- `docs`: Documentation updates (e.g., `docs(prd): update taxonomy definitions`)
- `style`: Formatting, missing semicolons, whitespace (no code change)
- `refactor`: Refactoring production code without changing behavior
- `test`: Adding or updating unit, integration, or contract tests
- `chore`: Build scripts, dependencies, configuration updates

---

## 4. Two-Member Work Allocation & Coordination

| Member | Primary Focus Areas | Secondary / Review Areas |
|---|---|---|
| **Member A** | Spring Boot backend, REST API contracts, PostgreSQL migrations, JWT Auth, Incident reporting workflow | React UI integration, ML contract verification, System testing |
| **Member B** | Dataset audit & preprocessing, ML baselines & evaluation, FastAPI inference service, Research methodology | React UI integration, Spring client review, Literature review |

Both members collaboratively maintain the React web client, write research documentation, review code diffs, and conduct joint demo dry-runs.

---

## 5. Pull Request & Code Review Checklist

Before requesting review and merging any PR, ensure:

- [ ] All code builds cleanly and local automated tests pass.
- [ ] No secrets, credentials, API keys, or raw personal data are present in the diff.
- [ ] New endpoints have server-side input validation and proper error handling.
- [ ] Database changes include versioned migrations with rollback considerations.
- [ ] New/updated APIs are documented in OpenAPI/Swagger annotations.
- [ ] Relevant documentation and `traceability-matrix.md` are updated.
- [ ] Both team members have tested the branch locally.

---

## 6. Development Workflow Steps

1. Create a branch from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```
2. Develop iteratively, writing corresponding unit and integration tests.
3. Test locally using the documented test commands.
4. Push branch and open a Pull Request using the GitHub PR template.
5. Reviewer tests locally and approves the PR.
6. Merge into `develop` using a clean merge commit or squash-and-merge.
