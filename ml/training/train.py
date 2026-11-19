"""
CyberSaarthi — Model Training, Validation Comparison & Test Evaluation Pipeline
Phase 3 / Task 3.3, 3.4, 3.5 & 3.6
=============================================================================
Trains 3 baseline classical classifiers using 5-fold stratified CV on Train only:
  1. Multinomial Naive Bayes (MNB)
  2. Logistic Regression (LR)
  3. Linear Support Vector Machine (Linear SVM with Platt calibration)

Selects winning model based on Validation Macro-F1 (frozen protocol rule),
evaluates once on the held-out test set, benchmarks inference latency,
and exports production artifacts + metadata.

Usage:
    python ml/training/train.py
"""

import time
import json
import hashlib
from datetime import datetime
from pathlib import Path

import joblib
import pandas as pd
import numpy as np
from sklearn.naive_bayes import MultinomialNB
from sklearn.linear_model import LogisticRegression
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.model_selection import GridSearchCV, StratifiedKFold
from sklearn.metrics import (
    classification_report,
    confusion_matrix,
    f1_score,
    accuracy_score,
    precision_score,
    recall_score,
)

from features import load_vectorizer, PROCESSED_DIR, ARTIFACTS_DIR

MODEL_PATH = ARTIFACTS_DIR / "baseline_model.joblib"
METADATA_PATH = ARTIFACTS_DIR / "model_metadata.json"
RANDOM_SEED = 42

TAXONOMY_V1_CODES = [
    "CYBERSTALKING_HARASSMENT",
    "FINANCIAL_UPI_FRAUD",
    "IDENTITY_THEFT",
    "JOB_INVESTMENT_SCAM",
    "MALWARE_RANSOMWARE",
    "OTHER_UNCLASSIFIED",
    "PHISHING_SOCIAL_ENGINEERING",
    "SOCIAL_ACCOUNT_COMPROMISE",
]


def load_data_and_features():
    """Loads CSV splits and vectorizes them using the fitted TF-IDF vectorizer."""
    vectorizer = load_vectorizer()

    train_df = pd.read_csv(PROCESSED_DIR / "train.csv")
    val_df = pd.read_csv(PROCESSED_DIR / "val.csv")
    test_df = pd.read_csv(PROCESSED_DIR / "test.csv")

    X_train = vectorizer.transform(train_df["text"])
    y_train = train_df["category"]

    X_val = vectorizer.transform(val_df["text"])
    y_val = val_df["category"]

    X_test = vectorizer.transform(test_df["text"])
    y_test = test_df["category"]

    return (X_train, y_train), (X_val, y_val), (X_test, y_test), test_df


def train_and_tune_models(X_train, y_train):
    """Performs 5-fold Stratified CV on training data to tune hyperparameters."""
    print("\n" + "=" * 70)
    print("  Task 3.3: 5-Fold Stratified Cross-Validation on Training Data")
    print("=" * 70)

    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_SEED)
    results = {}

    # 1. Multinomial Naive Bayes
    print("\nTuning 1/3: Multinomial Naive Bayes (MNB)...")
    mnb_params = {"alpha": [0.01, 0.1, 0.5, 1.0]}
    mnb_grid = GridSearchCV(
        MultinomialNB(),
        mnb_params,
        cv=cv,
        scoring="f1_macro",
        n_jobs=-1,
        refit=True
    )
    mnb_grid.fit(X_train, y_train)
    print(f"  Best MNB alpha: {mnb_grid.best_params_['alpha']} (Train CV Macro-F1: {mnb_grid.best_score_:.4f})")
    results["Multinomial Naive Bayes"] = mnb_grid.best_estimator_

    # 2. Logistic Regression
    print("\nTuning 2/3: Logistic Regression (LR)...")
    lr_params = {"C": [0.01, 0.1, 1.0, 10.0]}
    lr_grid = GridSearchCV(
        LogisticRegression(max_iter=1000, class_weight="balanced", random_state=RANDOM_SEED),
        lr_params,
        cv=cv,
        scoring="f1_macro",
        n_jobs=-1,
        refit=True
    )
    lr_grid.fit(X_train, y_train)
    print(f"  Best LR C: {lr_grid.best_params_['C']} (Train CV Macro-F1: {lr_grid.best_score_:.4f})")
    results["Logistic Regression"] = lr_grid.best_estimator_

    # 3. Linear SVM with Platt Calibration
    print("\nTuning 3/3: Linear SVM (LinearSVC) with Platt Calibration...")
    svm_params = {"estimator__C": [0.01, 0.1, 1.0, 10.0]}
    base_svm = LinearSVC(class_weight="balanced", random_state=RANDOM_SEED, max_iter=2000)
    calibrated_svm = CalibratedClassifierCV(estimator=base_svm, method="sigmoid", cv=5)
    svm_grid = GridSearchCV(
        calibrated_svm,
        svm_params,
        cv=cv,
        scoring="f1_macro",
        n_jobs=-1,
        refit=True
    )
    svm_grid.fit(X_train, y_train)
    best_c = svm_grid.best_params_["estimator__C"]
    print(f"  Best Linear SVM C: {best_c} (Train CV Macro-F1: {svm_grid.best_score_:.4f})")
    results["Linear SVM (Platt Calibrated)"] = svm_grid.best_estimator_

    return results


def evaluate_on_validation_set(models, X_val, y_val):
    """Compares all tuned models on held-out Validation set to select the winner."""
    print("\n" + "=" * 70)
    print("  Task 3.4: Validation Set Evaluation & Model Selection")
    print("=" * 70)

    val_records = []
    for name, model in models.items():
        start_t = time.perf_counter()
        y_pred = model.predict(X_val)
        lat_ms = (time.perf_counter() - start_t) / len(y_val) * 1000

        macro_f1 = f1_score(y_val, y_pred, average="macro", zero_division=0)
        weighted_f1 = f1_score(y_val, y_pred, average="weighted", zero_division=0)
        acc = accuracy_score(y_val, y_pred)

        val_records.append({
            "model_name": name,
            "val_macro_f1": round(macro_f1, 4),
            "val_weighted_f1": round(weighted_f1, 4),
            "val_accuracy": round(acc, 4),
            "latency_per_sample_ms": round(lat_ms, 4),
            "estimator": model
        })

    val_df = pd.DataFrame(val_records)
    print("\nValidation Comparison Table:")
    print(f"{'Model':<32} | {'Val Macro-F1':<13} | {'Val Weighted-F1':<15} | {'Val Accuracy':<12}")
    print("-" * 80)
    for _, r in val_df.iterrows():
        print(f"{r['model_name']:<32} | {r['val_macro_f1']:<13.4f} | {r['val_weighted_f1']:<15.4f} | {r['val_accuracy']:<12.4f}")

    # Predeclared Selection Rule: Highest Macro-F1 on Validation Set
    val_df = val_df.sort_values(by="val_macro_f1", ascending=False).reset_index(drop=True)
    winner_row = val_df.iloc[0]
    print(f"\nWINNING MODEL SELECTED (Highest Validation Macro-F1):")
    print(f"  -> {winner_row['model_name']} with Validation Macro-F1 = {winner_row['val_macro_f1']:.4f}")

    return winner_row["model_name"], winner_row["estimator"], val_df


def evaluate_winning_model_on_test(winner_name, model, X_test, y_test, test_df):
    """Evaluates winning model exactly once on the held-out Test set."""
    print("\n" + "=" * 70)
    print("  Task 3.5: Final Held-Out Test Set Evaluation")
    print("=" * 70)

    # Predictions
    y_pred = model.predict(X_test)
    y_proba = model.predict_proba(X_test)

    # Overall metrics
    macro_f1 = f1_score(y_test, y_pred, average="macro", zero_division=0)
    weighted_f1 = f1_score(y_test, y_pred, average="weighted", zero_division=0)
    acc = accuracy_score(y_test, y_pred)
    macro_prec = precision_score(y_test, y_pred, average="macro", zero_division=0)
    macro_rec = recall_score(y_test, y_pred, average="macro", zero_division=0)

    print(f"Model Evaluated: {winner_name}")
    print(f"  Test Accuracy   : {acc:.4f} ({acc*100:.2f}%)")
    print(f"  Test Macro-F1   : {macro_f1:.4f}")
    print(f"  Test Weighted-F1: {weighted_f1:.4f}")
    print(f"  Test Macro-Prec : {macro_prec:.4f}")
    print(f"  Test Macro-Rec  : {macro_rec:.4f}")

    # Per-Class Classification Report
    labels = sorted(list(set(y_test) | set(y_pred)))
    report_dict = classification_report(y_test, y_pred, labels=labels, output_dict=True, zero_division=0)
    report_text = classification_report(y_test, y_pred, labels=labels, zero_division=0)
    print("\nPer-Class Classification Report on Test Set:")
    print(report_text)

    # Confusion Matrix
    cm = confusion_matrix(y_test, y_pred, labels=labels)
    cm_df = pd.DataFrame(cm, index=labels, columns=labels)
    print("\nConfusion Matrix (Rows: Actual, Cols: Predicted):")
    print(cm_df.to_string())

    # Inference Latency Benchmark (100 individual calls)
    print("\nBenchmarking single-sample inference latency (100 trials)...")
    latencies = []
    for i in range(min(100, X_test.shape[0])):
        sample = X_test[i]
        t0 = time.perf_counter()
        _ = model.predict_proba(sample)
        latencies.append((time.perf_counter() - t0) * 1000)

    p50_ms = round(float(np.percentile(latencies, 50)), 3)
    p95_ms = round(float(np.percentile(latencies, 95)), 3)
    p99_ms = round(float(np.percentile(latencies, 99)), 3)
    print(f"  Latency p50: {p50_ms} ms | p95: {p95_ms} ms | p99: {p99_ms} ms")

    # Error Analysis (Top 5 representative misclassifications)
    misclassified = []
    for idx, (actual, pred) in enumerate(zip(y_test, y_pred)):
        if actual != pred:
            conf = float(np.max(y_proba[idx]))
            misclassified.append({
                "actual": actual,
                "predicted": pred,
                "confidence": round(conf, 4),
                "text_snippet": test_df.iloc[idx]["text"][:140]
            })

    print(f"\nTotal Test Misclassifications: {len(misclassified)} of {len(y_test)} ({len(misclassified)/len(y_test)*100:.2f}%)")
    print("Sample Misclassifications for Error Analysis:")
    for m in misclassified[:5]:
        print(f"  [Actual: {m['actual']}] -> [Pred: {m['predicted']}] (Conf: {m['confidence']})")
        print(f"    Text: \"{m['text_snippet']}...\"\n")

    test_metrics = {
        "accuracy": round(acc, 4),
        "macro_f1": round(macro_f1, 4),
        "weighted_f1": round(weighted_f1, 4),
        "macro_precision": round(macro_prec, 4),
        "macro_recall": round(macro_rec, 4),
        "latency_p50_ms": p50_ms,
        "latency_p95_ms": p95_ms,
        "latency_p99_ms": p99_ms,
        "classification_report": report_dict,
        "confusion_matrix": cm.tolist(),
        "confusion_labels": labels,
        "sample_errors": misclassified[:5]
    }

    return test_metrics, report_text, cm_df


def save_artifacts_and_metadata(winner_name, model, val_df, test_metrics):
    """Saves serialized model binary and comprehensive reproducibility metadata."""
    ARTIFACTS_DIR.mkdir(parents=True, exist_ok=True)

    # 1. Save winning model
    joblib.dump(model, MODEL_PATH)
    print(f"\nSaved production model to {MODEL_PATH}")

    # Compute SHA256 of model binary
    with open(MODEL_PATH, "rb") as f:
        model_hash = hashlib.sha256(f.read()).hexdigest()

    vectorizer_path = ARTIFACTS_DIR / "tfidf_vectorizer.joblib"
    with open(vectorizer_path, "rb") as f:
        vec_hash = hashlib.sha256(f.read()).hexdigest()

    metadata = {
        "protocol_version": "1.0",
        "taxonomy_version": "1.0",
        "trained_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "winning_model_name": winner_name,
        "model_file": str(MODEL_PATH.name),
        "model_sha256": model_hash,
        "vectorizer_file": str(vectorizer_path.name),
        "vectorizer_sha256": vec_hash,
        "random_seed": RANDOM_SEED,
        "taxonomy_categories": TAXONOMY_V1_CODES,
        "score_semantics": "calibrated_platt_svm" if "SVM" in winner_name else "predict_proba",
        "confidence_threshold": 0.60,
        "validation_comparison": val_df.drop(columns=["estimator"]).to_dict(orient="records"),
        "test_metrics": test_metrics,
    }

    with open(METADATA_PATH, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)

    print(f"Saved reproducibility metadata to {METADATA_PATH}")
    return metadata


def run_training_pipeline():
    print("=" * 70)
    print("  CyberSaarthi — Baseline Model Training & Evaluation")
    print("=" * 70)

    # 1. Load data
    (X_train, y_train), (X_val, y_val), (X_test, y_test), test_df = load_data_and_features()
    print(f"Data Loaded: Train={X_train.shape[0]:,}, Val={X_val.shape[0]:,}, Test={X_test.shape[0]:,}")

    # 2. Train & tune models
    tuned_models = train_and_tune_models(X_train, y_train)

    # 3. Validate and select winner
    winner_name, winning_model, val_df = evaluate_on_validation_set(tuned_models, X_val, y_val)

    # 4. Evaluate winner on held-out test set
    test_metrics, report_text, cm_df = evaluate_winning_model_on_test(
        winner_name, winning_model, X_test, y_test, test_df
    )

    # 5. Save artifacts and metadata
    metadata = save_artifacts_and_metadata(winner_name, winning_model, val_df, test_metrics)

    print("\n" + "=" * 70)
    print("  Training, Selection, and Evaluation Completed Successfully!")
    print(f"  Selected Model: {winner_name}")
    print(f"  Test Macro-F1 : {test_metrics['macro_f1']:.4f}")
    print(f"  Artifacts saved in {ARTIFACTS_DIR}")
    print("=" * 70)

    return metadata


if __name__ == "__main__":
    run_training_pipeline()
