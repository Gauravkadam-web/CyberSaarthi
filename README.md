# CyberSaarthi — Cybercrime Awareness and Reporting Portal

> **Tagline:** *Your Guide to a Safer Digital World.*  
> **Academic Project:** MCA Two-Member Group Project  
> **Current Status:** Phases 0–7 Completed | Phase 8 (ML Inference Integration) Ready

---

## 1. Project Overview

**CyberSaarthi** is an academic software and research prototype designed to provide structured cybercrime awareness education, guided incident reporting support, ML-assisted category suggestion, and application-level status tracking.

> [!IMPORTANT]
> **Scope and Boundaries Disclaimer:**  
> CyberSaarthi is an academic educational prototype. It is **NOT** an official police reporting portal, an emergency response mechanism, or a legal advice service. It does **NOT** investigate crimes, identify alleged offenders, or recover financial losses.  
> 
> For real-time emergency reporting of cyber financial fraud in India, users must report immediately to the **National Cyber Crime Reporting Portal** ([cybercrime.gov.in](https://cybercrime.gov.in)) or call the national cybercrime helpline **1930**.

---

## 2. System Architecture & Tech Stack

The portal employs a clean, layered architecture separating user experience, business logic, and machine learning:

* **Frontend Web Client:** React (Interactive awareness modules, quizzes, reporting flow, admin queues)
* **Core Application Backend:** Java 21 LTS with Spring Boot 3.4.x (System of record, JWT authentication, RBAC, REST APIs, PostgreSQL transactions)
* **ML Inference Service:** Python 3.10+ with FastAPI & Scikit-learn (Advisory incident text categorisation, model serving)
* **Database:** PostgreSQL 18 (Relational persistence with versioned Flyway migrations)
* **API Documentation:** OpenAPI 3.0 / Swagger UI (FastAPI docs at `:8000/docs`, Spring Boot docs at `:8080/swagger-ui.html`)

---

## 3. Repository Structure

```text
CyberSaarthi/
├── AGENTS.md                  # AI coding-agent execution rules & research integrity gates
├── CONTRIBUTING.md            # Team branch workflow, commit standards, and PR guidelines
├── README.md                  # Project overview, current progress, and setup documentation
├── .gitignore                 # Multi-stack Git ignore rules across tech stack
├── .env.example               # Root environment variable template
├── .github/                   # Issue and Pull Request templates
│   ├── ISSUE_TEMPLATE/
│   │   ├── feature_request.md
│   │   ├── bug_report.md
│   │   └── research_task.md
│   └── pull_request_template.md
├── docs/                      # Core baseline project documentation
│   ├── PRD.md                 # Product Requirements Document
│   ├── SRS.md                 # Software Requirements Specification
│   ├── architecture.md        # System architecture specification
│   ├── database-design.md     # PostgreSQL schema and data governance design
│   ├── development-roadmap.md # 12-phase development implementation plan
│   ├── research-proposal.md   # Academic research proposal (Tracks A & B)
│   ├── research-roadmap.md    # Research phases, methodology, and integrity gates
│   ├── taxonomy-v1.0.md       # Approved 8-category taxonomy specification
│   ├── official-reporting-and-disclaimers.md # Statutory guidance (1930 / cybercrime.gov.in)
│   ├── product-analysis-and-wireframes.md   # UX wireframes and user interaction journeys
│   └── traceability-matrix.md # Requirements Traceability Matrix (RTM)
├── docs-research/             # Literature reviews, experiment logs, and research notes
│   ├── literature-review/
│   │   └── literature-review-matrix.md # 16-paper literature review (Tracks A & B)
│   └── experiments/
│       ├── dataset-audit-report.md     # Sentinel dataset audit (PII removal, quality checks)
│       ├── taxonomy-mapping-v1.0.md   # Mapping 14 raw classes to Taxonomy v1.0
│       └── experiment-protocol.md     # Frozen 70/15/15 stratified protocol (seed 42)
├── frontend/                  # React web application
│   └── .env.example
├── backend/                   # Spring Boot application
│   └── .env.example
├── ml/                        # ML pipeline, experiments, and FastAPI service
│   ├── data/                  # Audited datasets (gitignored large CSVs)
│   ├── training/              # Baseline training & evaluation scripts
│   │   ├── preprocess.py      # Stratified train/val/test splits (8,690 clean samples)
│   │   ├── features.py        # TF-IDF feature extraction (train-fitted only)
│   │   ├── train.py           # Model training & CV comparison (MNB, LR, Linear SVM)
│   │   └── evaluate.py        # Held-out test evaluation & metrics export
│   ├── artifacts/             # Serialized model & metadata (baseline_model.joblib, etc.)
│   ├── inference/             # FastAPI inference service application
│   │   ├── main.py            # FastAPI entrypoint & lifecycle handlers
│   │   ├── schemas.py         # Pydantic v2 request/response validation schemas
│   │   ├── service.py         # Prediction pipeline & low-confidence routing logic
│   │   └── config.py          # Environment settings & thresholds
│   ├── tests/                 # Automated pytest suite (health & classification tests)
│   ├── reports/               # Experiment metrics & baseline evaluation report
│   └── requirements.txt       # Pinned ML dependencies
└── scripts/                   # Helper launch & maintenance scripts
    ├── run_ml_service.bat     # Windows batch launcher for FastAPI service
    └── run_ml_service.ps1     # Windows PowerShell launcher for FastAPI service
```

---

## 4. Current Progress & Completed Phases

| Phase | Title | Status | Key Deliverables |
|:---:|:---|:---:|:---|
| **Phase 0** | **Project Foundation** | ✅ Completed | Repository scaffolding, multi-stack `.gitignore`, `.env.example`, `AGENTS.md`, `CONTRIBUTING.md`, PR/issue templates, RTM baseline (`docs/traceability-matrix.md`). |
| **Phase 1** | **Literature Review & Product Analysis** | ✅ Completed | 16-paper academic literature review matrix, frozen Taxonomy v1.0 (8 categories), statutory legal disclaimers, responsive wireframes. |
| **Phase 2** | **Dataset Audit & ML Protocol** | ✅ Completed | Audited 14,000-sample Sentinel dataset (dropped 778 PII samples), mapped to Taxonomy v1.0, established frozen 70/15/15 stratified protocol (seed 42). |
| **Phase 3** | **ML Baseline Development** | ✅ Completed | Evaluated Naive Bayes, Logistic Regression, Linear SVM (with Platt scaling). Logistic Regression selected as champion (Test Accuracy: **98.39%**, Macro-F1: **0.8820**, Latency p50: **0.459 ms**). Model artifacts and evaluation report published. |
| **Phase 4** | **FastAPI ML Inference Service** | ✅ Completed | FastAPI service under `ml/inference/`, Pydantic v2 schemas, advisory scoring, low-confidence routing (`< 0.55` fallback to `OTHER_UNCLASSIFIED` / `requires_manual_review = true`), 12/12 passing pytest tests, batch/powershell launch scripts. |
| **Phase 5** | **Spring Boot Foundation & Auth** | ✅ Completed | Spring Boot 3.4.2, Java 21, Flyway migrations (`users`, `roles`, `incident_categories`), stateless JWT auth, RBAC, 21 unit/integration tests, Swagger UI at `:8080/swagger-ui.html`. |
| **Phase 6** | **Awareness Topics & Quizzes** | ✅ Completed | Awareness content catalog, deterministic quiz scoring engine, answer-secrecy projection, 33/33 backend tests passing, responsive React UI. |
| **Phase 7** | **Incident Reporting & Tracking** | ✅ Completed | Flyway V4 migrations, Crockford Base32 tracking codes (`CS-YYYY-XXXXX`), data minimisation guards against card numbers/PINs, IDOR prevention, status state machine, immutable audit logs, admin review queue, 43/43 backend tests, Vite React pages. |
| **Phase 8** | **ML Integration** | 🔄 *Next Up* | Spring-to-FastAPI client, resilient timeout/fallback policy (`CLASSIFICATION_PENDING`), prediction audit logs. |
| **Phase 9** | **Admin Dashboard & Analytics** | ⏳ Planned | Operational queues, aggregate reporting metrics, privacy-preserving research data exports. |
| **Phase 10** | **Quality, Security & Usability** | ⏳ Planned | Full security boundary testing, IDOR prevention verification, accessibility (WCAG 2.1 AA) audit. |
| **Phase 11** | **Release, Documentation & Defense** | ⏳ Planned | User guide, synthetic demo seeding, research manuscript, final academic presentation. |

---

## 5. Quickstart: Running the ML Inference Service

The ML service can be launched independently using the virtual environment in `ml/.venv`:

### Windows PowerShell:
```powershell
.\scripts\run_ml_service.ps1
```

### Windows Command Prompt (cmd):
```cmd
scripts\run_ml_service.bat
```

### Interactive API Documentation:
Once running, open your browser at:
* **Interactive Swagger UI:** [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
* **OpenAPI ReDoc:** [http://127.0.0.1:8000/redoc](http://127.0.0.1:8000/redoc)
* **Health Check:** [http://127.0.0.1:8000/api/v1/health](http://127.0.0.1:8000/api/v1/health)

### Running Automated ML Tests:
```powershell
& ".\ml\.venv\Scripts\pytest.exe" -v ml/tests
```

---

## 6. Security and Privacy Guidelines

- **Never commit real secrets:** Always verify `git status` and diffs before committing.
- **Data Minimisation:** Do not collect or store passwords, OTPs, PINs, or financial account credentials.
- **Synthetic Data for Development:** Use generated or synthetic data for testing and demonstrations.
- **Advisory Classification Policy:** Model predictions are strictly advisory suggestions; low-confidence predictions are automatically routed for administrative human review.
