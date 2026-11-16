# ML Experiment Protocol — Frozen Specification
## CyberSaarthi — Phase 2 / Research Phase R2 + R3
**Document Version:** 1.0  
**Taxonomy Version:** 1.0  
**Status:** Frozen — do not modify after Phase 3 training begins

> [!CAUTION]
> This document is the single source of truth for all ML experiment decisions.
> Any change to the protocol after the first training run invalidates the reproducibility guarantee.
> If a change is necessary, increment the protocol version to 1.1 and document the reason.

---

## 1. Research Questions Addressed

| ID | Question |
|---|---|
| **RQ-A1** | How do Multinomial Naive Bayes, Logistic Regression, and Linear SVM compare in Macro-F1, per-class Recall, and inference latency for CyberSaarthi's 8-class taxonomy? |
| **RQ-A2** | Which incident categories show the highest cross-class confusion and are most affected by class imbalance? |
| **RQ-A3** | Does a confidence-threshold routing policy effectively isolate low-confidence predictions for manual review? |

---

## 2. Frozen Hyperparameters & Seeds

```yaml
# --- Frozen Protocol Record ---
# Modify only by creating protocol v1.1 with documented justification.

protocol_version: "1.0"
taxonomy_version: "1.0"
random_seed: 42

dataset:
  primary: "C-1 — Cybercrime Text Classification Dataset (Kaggle)"
  licence: "CC BY 4.0"
  raw_path: "ml/data/raw/cybercrime_raw.csv"
  processed_path: "ml/data/processed/"

split:
  strategy: stratified_by_label
  train_ratio: 0.70
  validation_ratio: 0.15
  test_ratio: 0.15
  dedup_before_split: true       # Exact dedup applied before splitting
  leakage_check: true            # Near-duplicate scan on cross-split pairs

preprocessing:
  lowercase: true
  remove_urls: true
  remove_emails: true
  remove_special_chars: true
  tokenizer: whitespace
  stopword_removal: true
  stopword_corpus: nltk_english
  vectorizer: TF-IDF
  tfidf_max_features: 10000
  tfidf_ngram_range: [1, 2]
  tfidf_min_df: 2
  fit_on_training_set_only: true  # CRITICAL — never fit on val or test

models:
  multinomial_naive_bayes:
    class: sklearn.naive_bayes.MultinomialNB
    tuning_param: alpha
    search_values: [0.01, 0.1, 0.5, 1.0]
    tuning_method: 5_fold_cross_validation_on_train_only

  logistic_regression:
    class: sklearn.linear_model.LogisticRegression
    tuning_param: C
    search_values: [0.01, 0.1, 1.0, 10.0]
    solver: lbfgs
    max_iter: 1000
    multi_class: multinomial
    tuning_method: 5_fold_cross_validation_on_train_only

  linear_svm:
    class: sklearn.svm.LinearSVC
    tuning_param: C
    search_values: [0.01, 0.1, 1.0, 10.0]
    max_iter: 2000
    tuning_method: 5_fold_cross_validation_on_train_only

evaluation:
  primary_metric: macro_f1
  secondary_metrics:
    - weighted_f1
    - per_class_precision
    - per_class_recall
    - per_class_f1
    - confusion_matrix
    - accuracy
    - inference_latency_p50_ms
    - inference_latency_p95_ms
  test_set_evaluation: once_on_winning_model_only

model_selection_rule: >
  The model with the highest Macro-F1 on the held-out validation set is selected.
  In case of a tie within ±0.5% Macro-F1, the model with lower per-class F1 variance
  (more consistent across minority classes) is preferred.

confidence_policy:
  threshold: 0.60
  below_threshold_action: "classify as OTHER_UNCLASSIFIED and set requires_manual_review=true"
  ml_service_offline_action: "set status CLASSIFICATION_PENDING"

artifact:
  model_save_format: joblib
  model_path: "ml/training/artifacts/baseline_model.joblib"
  preprocessing_path: "ml/training/artifacts/tfidf_vectorizer.joblib"
  metadata_path: "ml/training/artifacts/model_metadata.json"
```

---

## 3. Data Pipeline — Step-by-Step (Enforced Order)

```
Step 1: Run audit_dataset.py                     → ml/data/audit_output/
Step 2: Complete taxonomy-mapping-v1.0.md        → manual review
Step 3: Apply mapping → produce labeled_clean.csv → ml/data/processed/labeled_clean.csv
Step 4: Stratified split (seed=42)               → train.csv, val.csv, test.csv
Step 5: Fit TF-IDF on train ONLY                 → tfidf_vectorizer.joblib
Step 6: Transform train, val, test separately    → X_train, X_val, X_test
Step 7: Tune each model on train via CV          → best params per model
Step 8: Evaluate each tuned model on val         → val metrics for model selection
Step 9: Select winning model (Macro-F1 on val)
Step 10: Evaluate winning model ONCE on test     → final test metrics
Step 11: Save model, vectorizer, metadata        → ml/training/artifacts/
Step 12: Write evaluation report                 → ml/reports/baseline-evaluation-report.md
```

> [!WARNING]
> **Leakage prevention rules (non-negotiable):**
> - TF-IDF `fit()` is called **only once**, on the training set.
> - `transform()` is called on val and test using the **already-fitted** vectorizer.
> - Hyperparameter tuning uses only training-set cross-validation, never the val/test split.
> - The test set is loaded only in Step 10, after the winning model is selected.

---

## 4. Mandatory Evaluation Metrics & Reporting Format

Every model evaluation report must include all of the following:

### 4.1 Per-model validation table

| Model | Macro-F1 | Weighted-F1 | Accuracy | Avg Latency (p50 ms) |
|---|---|---|---|---|
| Multinomial Naive Bayes | | | | |
| Logistic Regression | | | | |
| Linear SVM | | | | |

### 4.2 Per-class metrics (winning model on test set)

| Taxonomy Category | Precision | Recall | F1 | Support (count) |
|---|---|---|---|---|
| FINANCIAL_UPI_FRAUD | | | | |
| PHISHING_SOCIAL_ENGINEERING | | | | |
| IDENTITY_THEFT | | | | |
| SOCIAL_ACCOUNT_COMPROMISE | | | | |
| CYBERSTALKING_HARASSMENT | | | | |
| JOB_INVESTMENT_SCAM | | | | |
| MALWARE_RANSOMWARE | | | | |
| OTHER_UNCLASSIFIED | | | | |
| **Macro avg** | | | | |
| **Weighted avg** | | | | |

### 4.3 Required artifacts

- Confusion matrix heatmap (image saved to `ml/reports/figures/`)
- Inference latency benchmark (100 predictions, record p50/p95 in ms)
- Error analysis: 5 representative misclassified examples per confused class pair

---

## 5. Confidence Routing Policy — Implementation Contract

The FastAPI service (Phase 4) must comply with this policy exactly:

| Situation | ML Service Response | Spring Boot Action |
|---|---|---|
| Score ≥ 0.60 | `predicted_category`, `score`, `requires_manual_review: false` | Store prediction, update report status |
| Score < 0.60 | `predicted_category: OTHER_UNCLASSIFIED`, `score`, `requires_manual_review: true` | Route to manual review queue |
| ML service offline / timeout | HTTP 503 or no response | Set status `CLASSIFICATION_PENDING`; do not fail the report |
| Malformed response from ML | Unexpected response schema | Treat as offline; set `CLASSIFICATION_PENDING` |

> [!NOTE]
> **Score semantics:** LinearSVC does not produce probability scores natively. The `score` field for LinearSVC will be populated using `decision_function` values normalized to [0,1] using a sigmoid transform (Platt scaling via `CalibratedClassifierCV`). This must be clearly labelled `score_type: "calibrated_platt_svm"` in the response. For MNB and LR, `score_type: "predict_proba"`.

---

## 6. Reproducibility Record (to be updated after training)

> This section must be filled in during Phase 3 and committed alongside the model artifact.

```json
{
  "protocol_version": "1.0",
  "taxonomy_version": "1.0",
  "dataset_name": "C-1 Cybercrime Text Classification Dataset",
  "dataset_md5_hash": "TBD — compute after download",
  "total_rows_raw": "TBD",
  "total_rows_after_cleaning": "TBD",
  "train_count": "TBD",
  "val_count": "TBD",
  "test_count": "TBD",
  "random_seed": 42,
  "python_version": "TBD",
  "sklearn_version": "TBD",
  "numpy_version": "TBD",
  "pandas_version": "TBD",
  "winning_model": "TBD",
  "winning_model_params": "TBD",
  "val_macro_f1": "TBD",
  "test_macro_f1": "TBD",
  "model_artifact_md5": "TBD",
  "vectorizer_artifact_md5": "TBD",
  "trained_at": "TBD"
}
```

---

## 7. Experiment Integrity Checklist

> To be ticked by the team at end of Phase 3 before claiming Phase 2 exit criteria are satisfied:

- [ ] Dataset provenance and licence are documented in `dataset-candidates.md`
- [ ] All original labels have a mapping disposition in `taxonomy-mapping-v1.0.md`
- [ ] Exact and near-duplicate check completed before splitting (audit output retained)
- [ ] TF-IDF fitted exclusively on training set
- [ ] Test set not examined or used for tuning at any point
- [ ] All three baseline models trained and compared
- [ ] Metrics include Macro-F1, per-class Precision/Recall/F1, confusion matrix, and latency
- [ ] Negative or weak results retained and reported (not hidden)
- [ ] Model artifact, vectorizer, and metadata committed or archived
- [ ] Reproducibility record JSON filled and committed
- [ ] Protocol version matches what is recorded in model metadata JSON

