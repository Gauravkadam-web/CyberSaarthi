# CyberSaarthi — Cybercrime Awareness and Reporting Portal

> **Tagline:** *Your Guide to a Safer Digital World.*  
> **Academic Project:** MCA Two-Member Group Project  
> **Status:** Phase 0 (Project Foundation) Completed

---

## 1. Project Overview

**CyberSaarthi** is an academic prototype designed to provide structured cybercrime awareness education, guided incident reporting support, ML-assisted category suggestion, and application-level status tracking.

> [!IMPORTANT]
> **Scope and Boundaries Disclaimer:**  
> CyberSaarthi is an academic educational prototype. It is **NOT** an official police reporting portal, an emergency response mechanism, or a legal advice service. It does **NOT** investigate crimes, identify alleged offenders, or recover financial losses.  
> 
> For real-time emergency reporting of cyber financial fraud in India, users must report immediately to the **National Cyber Crime Reporting Portal** ([cybercrime.gov.in](https://cybercrime.gov.in)) or call the national cybercrime helpline **1930**.

---

## 2. System Architecture & Tech Stack

The portal employs a clean, layered architecture separating user experience, business logic, and machine learning:

* **Frontend Web Client:** React (Interactive awareness modules, quizzes, reporting flow, admin queues)
* **Core Application Backend:** Java 17+ with Spring Boot (System of record, JWT authentication, RBAC, REST APIs, PostgreSQL transactions)
* **ML Inference Service:** Python 3.10+ with FastAPI (Advisory incident text categorisation, model serving)
* **Database:** PostgreSQL (Relational persistence with versioned Flyway/Liquibase migrations)

---

## 3. Repository Structure

```text
CyberSaarthi/
├── AGENTS.md                  # AI coding-agent rules and execution boundaries
├── CONTRIBUTING.md            # Team branch workflow, commit standards, and PR guidelines
├── README.md                  # Project overview and setup documentation
├── .gitignore                 # Comprehensive Git ignore rules across tech stack
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
│   └── traceability-matrix.md # Requirements Traceability Matrix (RTM)
├── docs-research/             # Literature reviews, experiment logs, and research notes
│   ├── literature-review/
│   └── experiments/
├── frontend/                  # React web application
│   └── .env.example
├── backend/                   # Spring Boot application
│   └── .env.example
├── ml/                        # ML pipeline, experiments, and FastAPI service
│   ├── data/                  # Audited datasets (gitignored large files)
│   ├── training/              # Baseline training & evaluation scripts
│   ├── inference/             # FastAPI inference service application
│   ├── tests/                 # Unit & contract tests for ML service
│   ├── reports/               # Experiment metrics & confusion matrices
│   └── .env.example
└── scripts/                   # Setup, deployment, and seed scripts
```

---

## 4. Local Development Prerequisites

Ensure the following runtimes and tools are installed on your workstation:

1. **Git:** `git --version` ($\ge$ 2.30)
2. **Node.js & npm:** Node 18+ LTS (`node -v`, `npm -v`)
3. **Java Development Kit (JDK):** JDK 17 or JDK 21 (`java -version`)
4. **Python:** Python 3.10+ (`python --version`)
5. **PostgreSQL:** Version 14+ running locally or in Docker

---

## 5. Development Phases

The project proceeds strictly through defined phases with exit criteria:

* **Phase 0:** Project Foundation (Repository setup, docs, RTM, branch strategy, environments) — *Complete*
* **Phase 1:** Literature Review & Product Analysis
* **Phase 2:** Dataset Audit & ML Protocol
* **Phase 3:** ML Baseline Development (Naive Bayes, Logistic Regression, Linear SVM)
* **Phase 4:** FastAPI Inference Service
* **Phase 5:** Spring Boot Backend Foundation & Auth
* **Phase 6:** Awareness Topics & Quizzes
* **Phase 7:** Incident Reporting, State Workflow & Tracking
* **Phase 8:** ML Service Integration & Advisory Inference
* **Phase 9:** Admin Dashboard & Aggregate Analytics
* **Phase 10:** Testing, Security Audits & Usability Evaluation
* **Phase 11:** Final Demonstration, Release & Research Publication

---

## 6. Security and Privacy Guidelines

- **Never commit real secrets:** Always verify `git status` and diffs before committing.
- **Data Minimisation:** Do not collect or store passwords, OTPs, PINs, or financial account credentials.
- **Synthetic Data for Development:** Use generated or synthetic data for testing and demonstrations.
- **Review Policy:** Predictions are advisory suggestions; low-confidence predictions are automatically routed for administrative human review.
