# Product Requirements Document (PRD)
## CyberSaarthi
**Project title:** Cybercrime Awareness and Reporting Portal  
**Tagline:** Your Guide to a Safer Digital World  
**Document version:** 1.0  
**Status:** Baseline draft for team review  
**Prepared for:** MCA academic group project  
**Team:** Two-member student team

---

## 1. Product Overview

CyberSaarthi is an educational cybercrime-awareness and incident-reporting support portal. It helps users understand common cyber threats, learn safer digital practices, submit structured incident reports, receive ML-assisted incident categorisation, and track the status of reports within the application.

The academic project title remains **Cybercrime Awareness and Reporting Portal**. CyberSaarthi is the product name used in the application.

CyberSaarthi is not a law-enforcement system, an emergency response service, or a government complaint portal. It does not investigate crimes, identify offenders, recover money, or submit complaints to government authorities.

## 2. Vision, Mission and Objectives

### 2.1 Vision
Make practical cyber-safety knowledge and structured incident-reporting support accessible through one understandable digital platform.

### 2.2 Mission
Combine awareness education, guided reporting, transparent report tracking, and carefully evaluated machine-learning assistance in a secure and privacy-conscious application.

### 2.3 Product objectives
1. Present understandable information about common cybercrime patterns and prevention.
2. Help users submit consistent, structured incident reports.
3. Categorise report descriptions using an ML model, with uncertainty handled safely.
4. Allow users to track application-level report status.
5. Give administrators tools to review reports and maintain awareness content.
6. Provide aggregate, privacy-conscious analytics for academic evaluation.
7. Evaluate both classification performance and the educational value of awareness activities.

## 3. Problem Statement

People may not recognise cybercrime patterns, may not know what information to preserve, and may struggle to describe incidents consistently. Awareness resources are often disconnected from the process of documenting an incident. A student-built portal can demonstrate how structured education, guided data capture, and ML-assisted categorisation may support this process.

The project will evaluate this proposition rather than assume that an application automatically improves reporting outcomes or prevents cybercrime.

## 4. Target Users and Personas

### 4.1 General user
A person seeking cyber-safety information or wishing to document an incident in CyberSaarthi. They need clear language, accessible guidance, privacy explanations, and a simple reporting process.

### 4.2 Administrator/reviewer
An authorised project administrator who reviews submitted reports, updates statuses, manages awareness content, and monitors aggregate application activity.

### 4.3 Researcher/evaluator
A project team member or academic evaluator who examines de-identified evaluation data, model metrics, usability feedback, and awareness-assessment results.

## 5. Scope

### 5.1 In scope
- User registration, authentication, and role-based access.
- Cybercrime awareness articles and safety guidance.
- Awareness quizzes and score feedback.
- Structured incident-report submission.
- ML-assisted categorisation of incident descriptions.
- Confidence-aware handling and manual review of uncertain predictions.
- User-facing report history and status tracking.
- Administrator report review and status management.
- Awareness-content management.
- Aggregate dashboards and research evaluation exports with privacy safeguards.
- Audit logging for sensitive administrative actions.
- Security, usability, testing, and research documentation.

### 5.2 Out of scope
- Police investigation or offender identification.
- Direct filing of official complaints with government systems.
- Legal advice or legal determinations.
- Guaranteed prevention, detection, recovery, or resolution of losses.
- Money recovery, bank account access, payment processing, or financial integrations.
- Real-time emergency response.
- Automated accusation of a person or organisation.
- Public display of submitted reports or personal details.
- Unreviewed automated decisions that materially affect a user's rights or access to services.

## 6. Functional Requirements Summary

| ID | Requirement | Priority |
|---|---|---|
| FR-01 | User registration, login, logout, and role-based access | Must |
| FR-02 | Browse and search awareness content | Must |
| FR-03 | Complete awareness quizzes and view feedback | Should |
| FR-04 | Submit a structured incident report | Must |
| FR-05 | Generate ML-assisted incident category suggestions | Must |
| FR-06 | View submitted reports and application status history | Must |
| FR-07 | Allow authorised administrators to review and update reports | Must |
| FR-08 | Manage awareness content and categories | Should |
| FR-09 | Display aggregate operational and research analytics | Should |

Detailed requirements and acceptance criteria are in `SRS.md`.

## 7. Initial Incident Taxonomy

The initial category set is a project taxonomy and must be validated against the selected dataset and literature before model training.

1. Financial and UPI Fraud
2. Phishing and Social Engineering
3. Identity Theft
4. Social Media and Account Compromise
5. Cyberstalking and Online Harassment
6. Job and Investment Scams
7. Malware and Ransomware
8. Other or Unclassified

Categories may be revised only through a documented taxonomy change. Training labels, UI labels, evaluation reports, and API contracts must use a consistent versioned taxonomy.

## 8. Machine-Learning Product Requirements

The first model task is text classification of an incident description into one of the approved categories.

Candidate baseline models:
- Multinomial Naive Bayes
- Logistic Regression
- Linear Support Vector Machine

Evaluation must include accuracy, precision, recall, F1-score, macro-F1, weighted-F1, confusion matrix, per-class metrics, and inference latency. Class imbalance must be reported and addressed transparently.

The system must:
- Treat model output as a suggestion, not a finding of fact.
- Display uncertainty appropriately.
- Route low-confidence or unsupported cases to manual review.
- Permit authorised reviewers to correct the suggested category.
- Record model version and taxonomy version for reproducibility.
- Avoid claiming a model is accurate without measured, held-out evaluation.
- Never use the prediction to accuse a person or determine legal responsibility.

A probability-like score from a model must not automatically be described as a calibrated confidence unless calibration has been evaluated.

## 9. Research Tracks

### Track A: Incident description classification
Investigate whether classical text-classification methods can categorise short cyber-incident descriptions across the chosen taxonomy. Compare candidate models using a reproducible experimental protocol and class-wise metrics.

### Track B: Awareness intervention evaluation
Investigate whether a structured awareness activity is associated with a measurable change in participants' knowledge scores. Use an appropriate pre-test/post-test design, voluntary participation, informed consent, and aggregate reporting. Do not claim causal impact unless the design supports it.

The team must confirm institutional ethics and consent requirements before collecting participant data.

## 10. Key User Journeys

### 10.1 Awareness journey
1. User opens the awareness section.
2. User selects a topic.
3. Portal presents concise explanation, warning signs, and preventive actions.
4. User optionally completes a quiz.
5. Portal displays score and learning feedback.

### 10.2 Incident reporting journey
1. User signs in and opens the reporting form.
2. Portal explains what information is requested and how it will be used.
3. User enters incident details and submits.
4. Backend validates and stores the report.
5. ML service suggests a category and returns model metadata.
6. The application applies the configured review policy.
7. User sees a reference number and current application status.
8. An authorised administrator reviews and updates the report.
9. User can view status history and relevant guidance.

### 10.3 Administrator journey
1. Administrator signs in.
2. Administrator opens the review queue.
3. Administrator views a report with only the information needed for review.
4. Administrator confirms or corrects the category and updates status.
5. The system records actor, timestamp, and change in an audit trail.
6. Aggregate dashboard figures update without exposing personal data.

## 11. Non-Functional Requirements

- **Security:** secure password hashing, access control, input validation, secure secrets, transport security in deployment, and protection against common web vulnerabilities.
- **Privacy:** data minimisation, clear purpose, consent where applicable, retention rules, restricted access, and de-identification for research.
- **Usability:** responsive interface, clear language, validation feedback, and accessible form controls.
- **Performance:** define and measure response-time targets during implementation; ML inference should be measured separately.
- **Reliability:** predictable validation errors, safe failure when ML service is unavailable, and recoverable application errors.
- **Maintainability:** layered backend, documented APIs, environment-based configuration, automated tests, and versioned migrations.
- **Scalability:** modular services and stateless API design where practical.
- **Auditability:** sensitive administrative actions must be traceable.
- **Reproducibility:** preserve dataset provenance, split strategy, random seeds, model parameters, and evaluation outputs.

## 12. Success Measures and KPIs

- Successful completion rate for report submission.
- Percentage of reports receiving a category suggestion.
- Percentage of low-confidence cases routed for review.
- Reviewer correction rate by category.
- Report status visibility and successful status-history retrieval.
- Awareness quiz participation and completion.
- Pre-test/post-test score distributions and effect-size estimates where appropriate.
- Macro-F1 and per-class recall on a held-out test set.
- Inference latency and service failure rate.
- Usability feedback and observed task completion.

Targets must be set after baseline testing; do not invent performance claims.

## 13. Privacy, Safety and Official Reporting

CyberSaarthi must clearly state that it is an academic support portal and is not affiliated with or a replacement for official reporting channels.

For urgent financial cyber fraud in India, users should be directed to the official National Cyber Crime Reporting Portal and the national cybercrime helpline **1930**. The application must display official links and advise users not to delay official reporting while using CyberSaarthi.

The project must not request passwords, OTPs, PINs, full card details, or unnecessary identity documents. Sensitive evidence uploads should be avoided in the initial release unless secure storage, access control, retention, and deletion procedures are implemented and tested.

## 14. Assumptions and Constraints

- The project is developed by a two-member MCA team.
- The primary implementation stack is React, Spring Boot, Python FastAPI for ML inference, and PostgreSQL.
- Dataset availability, permitted use, licensing, and representativeness require verification.
- The project is an academic prototype and will not claim official institutional endorsement.
- The initial release will use a manageable, documented taxonomy and baseline ML models.

## 15. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Dataset labels do not match taxonomy | Conduct dataset audit and taxonomy mapping before training |
| Class imbalance | Report class distribution; use appropriate training-only strategies |
| Model overconfidence | Evaluate calibration; use conservative review thresholds |
| Sensitive personal data exposure | Minimise data, restrict access, define retention, and test authorisation |
| ML service outage | Keep report submission available; mark classification pending |
| Scope exceeds team capacity | Deliver in phases; prioritise must-have requirements |
| Research claims exceed evidence | Predefine methods and report limitations |
| Users mistake portal for official authority | Persistent disclaimer and verified official links |

## 16. Development Roadmap

1. Product and research foundation.
2. Literature review and research-gap validation.
3. Product analysis and UX planning.
4. Dataset research and preparation.
5. ML baseline development and evaluation.
6. Application foundation and authentication.
7. Awareness content and assessments.
8. Incident reporting and tracking.
9. ML service integration.
10. Administration, analytics, security, and evaluation.
11. Final documentation, demonstration, and publication preparation.

See `development-roadmap.md` and `research-roadmap.md`.

## 17. Publication Strategy

Prepare a conference paper around a focused, reproducible contribution, such as comparative baseline evaluation for cyber-incident text classification or a carefully designed awareness assessment.

A later journal submission must disclose any related conference publication and provide substantial additional contribution, such as expanded data evaluation, stronger baselines, robustness analysis, explainability, or a larger validated awareness study. Follow the target venue's duplicate-publication and extension policies. Acceptance is not guaranteed.

## 18. Future Scope

Potential future enhancements include multilingual support (Marathi, Hindi, and Hinglish), explainable AI, phishing URL analysis, suspicious-message analysis, transformer models, retrieval-based guidance, advanced analytics, gamification, threat-intelligence integration, mobile applications, and institutional deployment. These are not commitments for the initial release.

## 19. Definition of Done

A feature is done when its requirements and acceptance criteria are met, code is reviewed, relevant tests pass, security and privacy implications are considered, documentation is updated, and the feature is demonstrated. Research features additionally require reproducible methods, documented limitations, and traceable results.
