# Research Roadmap
## CyberSaarthi — Research Execution Plan
**Version:** 1.0 | **Status:** Proposed

## 1. Purpose

This roadmap separates research work from application feature delivery. It defines evidence gates so that the team does not make unsupported claims or train a model on an unverified dataset.

## 2. Research Workstreams

- **R1 — Literature and gap analysis**
- **R2 — Taxonomy and dataset validation**
- **R3 — ML experiment design and model evaluation**
- **R4 — Awareness intervention study**
- **R5 — Integrated system and usability evaluation**
- **R6 — Publication and reproducibility**

## 3. Phase Plan

### Phase R0: Research framing
**Activities**
- Confirm project title, product scope, research tracks, and supervisor expectations.
- Refine research questions.
- Identify what constitutes a contribution beyond implementation.

**Deliverables**
- Approved research proposal.
- Research question and objective matrix.

**Exit gate**
- Supervisor accepts the research direction and feasible scope.

### Phase R1: Literature review
**Activities**
- Search scholarly databases and credible institutional publications.
- Review cybercrime awareness interventions, reporting-support systems, incident text classification, dataset limitations, and human-in-the-loop approaches.
- Record methods, datasets, metrics, findings, and limitations.

**Deliverables**
- Literature review matrix.
- Thematic synthesis.
- Research-gap statement with supporting citations.

**Exit gate**
- Each claimed gap is supported by reviewed sources; novelty is not asserted solely because the team has not found an identical application.

### Phase R2: Taxonomy and dataset audit
**Activities**
- Find candidate datasets.
- Verify provenance, licence, collection context, and permitted use.
- Compare label definitions with the proposed eight categories.
- Inspect missing values, duplicates, near duplicates, class imbalance, language, and sensitive content.
- Document mapping, exclusions, and unresolved ambiguity.

**Deliverables**
- Dataset card.
- Data quality report.
- Taxonomy mapping specification.
- Data governance notes.

**Exit gate**
- Dataset is legally/ethically usable and sufficiently aligned, or the team revises the research scope.

### Phase R3: Experimental protocol
**Activities**
- Define target variable and inclusion/exclusion rules.
- Define train/validation/test strategy and random seed.
- Prevent leakage, including duplicate and group leakage.
- Define vectorisation, baselines, tuning procedure, metrics, and model selection rule.
- Decide how uncertainty and low-confidence predictions will be handled.

**Deliverables**
- Experiment protocol.
- Reproducibility checklist.
- Baseline implementation plan.

**Exit gate**
- Evaluation choices are documented before final test-set evaluation.

### Phase R4: Baseline model experiments
**Activities**
- Train Multinomial Naive Bayes, Logistic Regression, and Linear SVM.
- Use training-only cross-validation or a validation set for tuning.
- Compare macro-F1, weighted-F1, per-class recall, and latency.
- Analyse confusion matrix and representative errors.
- Select the model using the predeclared criterion.

**Deliverables**
- Training scripts.
- Model comparison report.
- Error analysis.
- Versioned model and metadata.

**Exit gate**
- All reported metrics can be reproduced from documented code and data splits.

### Phase R5: Awareness study preparation and execution
**Activities**
- Draft participant information sheet, consent language, and assessment instruments.
- Obtain institutional/supervisor approval as required.
- Pilot the questionnaire for clarity.
- Conduct pre-test, awareness intervention, post-test, and optional feedback.
- De-identify data and analyse paired results.

**Deliverables**
- Approved protocol (where required).
- Questionnaire and scoring guide.
- De-identified dataset.
- Analysis report.

**Exit gate**
- No participant data is collected before required approval; conclusions match the study design.

### Phase R6: System evaluation
**Activities**
- Define representative user tasks.
- Test report submission, status tracking, awareness navigation, and admin review.
- Evaluate API security and ML failure handling.
- Collect usability feedback only under an appropriate consent process.

**Deliverables**
- Test plan and results.
- Usability findings.
- Threat/risk review.
- Limitations register.

**Exit gate**
- Critical defects are resolved or explicitly documented; evaluation methods are transparent.

### Phase R7: Publication
**Activities**
- Select a conference venue after verifying scope and policies.
- Prepare paper with method, data, results, limitations, and reproducibility details.
- Disclose AI/tool assistance according to venue policy.
- For journal extension, add substantial new contribution and disclose the earlier paper.

**Deliverables**
- Conference manuscript.
- Presentation.
- Journal extension plan.
- Reproducibility package where permitted.

**Exit gate**
- All authors approve the manuscript; citations, data rights, and venue policies are checked.

## 4. Suggested Literature Review Matrix

| Field | What to record |
|---|---|
| Citation | Full reference and DOI/URL |
| Research problem | What issue is studied |
| Dataset | Source, size, language, labels, licence |
| Method | Model/intervention/design |
| Evaluation | Metrics and protocol |
| Findings | Results as reported by authors |
| Limitations | Authors' stated limitations |
| Relevance | How it informs CyberSaarthi |
| Gap opportunity | Specific unresolved question |

## 5. Research Integrity Checklist

- [ ] Dataset provenance and licence are documented.
- [ ] Label mapping is explicit and versioned.
- [ ] Duplicates and leakage risks are assessed.
- [ ] Test set is not used for tuning.
- [ ] Metrics include macro and per-class results.
- [ ] Negative results are retained.
- [ ] Model score semantics are accurate.
- [ ] Human-participant approval is obtained where required.
- [ ] Consent and data minimisation are implemented.
- [ ] No personal incident narratives are published.
- [ ] Reproducibility information is preserved.
- [ ] Conference/journal overlap is disclosed.

## 6. Two-Member Work Allocation

Suggested division, to be confirmed by the team:
- **Member A:** Spring Boot application, API contracts, database integration, authentication and reporting workflows.
- **Member B:** Dataset audit, ML experiments, FastAPI inference service, evaluation scripts.
- **Shared:** React UI, requirements, testing, literature review, awareness study, security review, paper writing, and demonstrations.

Both members should understand the complete system and review each other's work. Avoid a division where only one member can explain the ML or backend contribution.

## 7. Risk Register

| Risk | Response |
|---|---|
| No suitable labelled dataset | Narrow the research question or create a carefully governed annotation plan |
| Taxonomy mismatch | Revise categories and mapping before training |
| Weak minority-class performance | Report transparently; explore data quality and class-aware methods |
| Small awareness sample | Treat findings as exploratory and report uncertainty |
| Ethics approval delay | Continue non-human-subject work; do not collect participant data |
| Venue unsuitable or predatory | Verify publisher, peer review, indexing, fees, and policies |
| Paper overlap | Plan distinct contributions and disclose prior publication |

## 8. Milestone Tracking

Use a project board with fields: phase, task, owner, due date, dependency, status, evidence link, and approval. A phase is not complete because code exists; it is complete when its exit evidence is reviewed and accepted.
