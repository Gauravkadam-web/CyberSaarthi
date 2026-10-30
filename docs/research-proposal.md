# Research Proposal
## CyberSaarthi: Cybercrime Awareness and Reporting Portal
**Academic project title:** Cybercrime Awareness and Reporting Portal  
**Product name:** CyberSaarthi  
**Version:** 1.0 | **Status:** Proposal draft for supervisor review

## 1. Proposed Research Theme

This project investigates how a web-based cybercrime-awareness and structured incident-reporting support portal can combine educational guidance with ML-assisted categorisation of user-described cyber incidents.

The research has two related but independently evaluable tracks:
1. Comparative evaluation of text-classification methods for categorising cyber-incident descriptions.
2. Evaluation of changes in cyber-safety knowledge following a structured awareness intervention.

The study must not assume that classification accuracy or quiz-score improvement automatically reduces cybercrime.

## 2. Background and Rationale

Cybercrime incidents may be described inconsistently by affected individuals. Users may also be uncertain about warning signs, safe immediate actions, and what information to preserve. A digital platform can organise educational content and guide users through a structured description process. Machine learning may assist with initial categorisation, but model output can be wrong, biased, or uncertain and therefore requires evaluation and human oversight.

## 3. Problem Statement

There is a practical need for accessible cyber-safety education and consistent incident documentation. It is worth investigating whether a structured portal can support these activities and whether conventional supervised text-classification models can provide useful category suggestions for incident descriptions under a defined taxonomy.

## 4. Aim

To design, implement, and evaluate an academic cybercrime-awareness and reporting-support portal with an ML-assisted incident categorisation component.

## 5. Objectives

1. Review relevant literature on cybercrime awareness, reporting support, and text classification.
2. Identify and document a research gap supported by the literature.
3. Define a versioned incident taxonomy suitable for the available dataset.
4. Build a secure web application for awareness content and structured incident reporting.
5. Train and compare selected baseline text-classification models.
6. Integrate the selected model as an advisory inference service.
7. Evaluate model performance using held-out data and class-wise metrics.
8. Design an awareness assessment and evaluate pre-test/post-test results if approved.
9. Evaluate usability, reliability, and limitations of the prototype.
10. Prepare reproducible research documentation and publication outputs.

## 6. Research Questions

### Track A — ML classification
- RQ-A1: How do Multinomial Naive Bayes, Logistic Regression, and Linear SVM compare when classifying incident descriptions into the project's cybercrime taxonomy?
- RQ-A2: Which categories have the weakest precision and recall, and what types of errors are most common?
- RQ-A3: How do class imbalance, text length, and ambiguous descriptions affect model performance?
- RQ-A4: What is the inference latency of the evaluated models under the test environment?

### Track B — awareness intervention
- RQ-B1: Do participants' cyber-safety knowledge scores differ between pre-test and post-test after completing the awareness module?
- RQ-B2: Which topics show the largest observed score changes?
- RQ-B3: How do participants rate the clarity and usefulness of the awareness content?

These questions should be refined after literature review and supervisor approval.

## 7. Proposed Hypotheses

For Track A, the study may compare model performance without assuming in advance which model will win. If formal hypotheses are required, define them before inspecting final test results.

For Track B, a tentative hypothesis is that post-test scores will be higher than pre-test scores among participants who complete the intervention. This is an association under the proposed design; causal claims require a stronger design and appropriate controls.

## 8. Proposed Methodology

### 8.1 System development
Use an iterative software-engineering process:
- Requirements analysis.
- Architecture and database design.
- UI and API implementation.
- ML pipeline development.
- Integration.
- Functional, security, and usability testing.

### 8.2 ML experiment
1. Identify a dataset with documented source, licence, collection context, and permitted use.
2. Audit labels, duplicates, missing values, class distribution, and possible leakage.
3. Map source labels to the approved taxonomy and document exclusions.
4. Split data into training, validation, and held-out test sets before fitting preprocessing.
5. Fit text vectorisation and any learned transformations on training data only.
6. Train candidate baselines: Multinomial Naive Bayes, Logistic Regression, and Linear SVM.
7. Tune parameters using training-only cross-validation or a separate validation set.
8. Select a model using a predeclared criterion, with macro-F1 and class-wise recall considered.
9. Evaluate once on the held-out test set.
10. Report accuracy, precision, recall, F1, macro-F1, weighted-F1, confusion matrix, per-class metrics, and latency.
11. Record random seed, software versions, preprocessing, parameters, and model artifact hash.
12. Analyse failure cases and limitations.

Avoid leakage from duplicate or near-duplicate records across splits. If data is grouped by source, user, or incident, use group-aware splitting where appropriate.

### 8.3 Awareness evaluation
Subject to supervisor and institutional approval:
1. Recruit a suitable voluntary participant sample.
2. Provide an information sheet and obtain informed consent.
3. Administer a baseline knowledge assessment.
4. Present the awareness module.
5. Administer a post-intervention assessment using equivalent or appropriately designed questions.
6. Collect optional usability feedback.
7. Analyse paired observations where the same participants complete both tests.
8. Report sample size, missing responses, score distributions, effect size, and uncertainty.
9. Avoid collecting unnecessary personal or sensitive information.

If ethics approval or suitable participants are unavailable, present the awareness module as a product feature and do not claim an empirical learning effect.

## 9. Dataset and Data Governance

The dataset must be evaluated for:
- Provenance and licence.
- Collection date and context.
- Label definitions and consistency.
- Representativeness of the intended taxonomy.
- Class balance.
- Language coverage.
- Duplicate and near-duplicate records.
- Sensitive or identifying content.
- Potential train/test contamination.

Do not scrape private reports or collect real victim narratives without appropriate permissions and safeguards. Public availability does not automatically mean unrestricted research reuse.

## 10. Evaluation Plan

### ML metrics
- Accuracy.
- Per-class precision, recall, and F1.
- Macro-F1 and weighted-F1.
- Confusion matrix.
- Inference latency (report measurement environment and percentile/statistic).
- Optional calibration metrics if probabilities are used.
- Error analysis by class and text characteristics.

### Software evaluation
- Functional test pass rate.
- API validation and authorisation tests.
- Report submission completion.
- ML service failure/degradation behaviour.
- Usability task completion and participant feedback.
- Basic performance and security testing.

### Awareness evaluation
- Pre-test and post-test descriptive statistics.
- Paired statistical analysis where assumptions and sample size support it.
- Effect size and confidence interval where appropriate.
- Topic-level results and limitations.

## 11. Ethical, Privacy, and Safety Considerations

- Obtain supervisor/institutional approval before participant recruitment or human-subject data collection.
- Participation must be voluntary and informed.
- Collect minimum necessary data.
- Use pseudonymous study IDs and restrict linkage files.
- Define retention and deletion before collection.
- Do not publish raw incident narratives or identifiable participant records.
- Clearly communicate that the portal is not an official reporting channel.
- Display verified official reporting links and emergency guidance.
- Do not present model predictions as verified facts or legal conclusions.
- Do not provide unsafe advice that delays official reporting.

## 12. Expected Contributions

Potential contributions, subject to actual results:
1. A documented cybercrime-awareness and structured reporting-support prototype.
2. A versioned taxonomy and reproducible mapping process for the selected dataset.
3. A comparative baseline evaluation of classical text-classification models.
4. An analysis of category-level errors and uncertainty handling.
5. A documented awareness assessment design and, if approved and completed, observed pre/post results.
6. An integrated architecture separating core application workflows from ML inference.

The team must distinguish implementation contribution from empirical research contribution.

## 13. Limitations

- Dataset labels may not represent real-world incident prevalence.
- Public datasets may be synthetic, curated, imbalanced, or language-limited.
- Model results may not generalise to new regions, languages, or evolving scam patterns.
- Self-reported quiz results may be affected by practice effects and selection bias.
- A small participant sample limits statistical power and generalisability.
- The prototype does not investigate or officially process crimes.

## 14. Publication Plan

### Conference paper
Select one focused contribution with a clear method and reproducible results. Possible focus: comparative baseline models for cyber-incident text categorisation, including class-wise error analysis and human-review design.

### Journal extension
A journal extension must provide substantial new work beyond the conference paper, such as expanded datasets, stronger baselines, robustness testing, explainability, external validation, or a larger awareness study. Disclose the conference version and follow venue policies on overlap and prior publication.

### Publication integrity
Do not fabricate data, participants, metrics, approvals, or results. Report negative and inconclusive findings. Cite dataset sources and related work. Confirm target venue scope, indexing claims, fees, and publication policies directly before submission.

## 15. Indicative Deliverables

- Literature review and research-gap matrix.
- Approved research protocol and ethics documents if required.
- Dataset card and data audit report.
- Reproducible ML experiment scripts and evaluation report.
- Working CyberSaarthi prototype.
- Test report and security checklist.
- Research paper draft and presentation.
- Final dissertation/project report.

## 16. Approval Gate

Before research data collection or model training, obtain supervisor agreement on:
- Research questions and contribution.
- Dataset and licence.
- Taxonomy.
- Experimental split and evaluation criteria.
- Human-participant protocol and ethics requirements.
- Publication target and authorship expectations.
