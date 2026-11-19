"""
CyberSaarthi — Standalone Model Evaluation & Inference Test Script
Phase 3 / Task 3.4 & 3.5
==================================================================
Loads serialized baseline model and TF-IDF vectorizer artifacts from
`ml/training/artifacts/` and evaluates on any target dataset or single inputs.

Usage:
    python ml/training/evaluate.py --split test
    python ml/training/evaluate.py --text "My UPI PIN was asked on a fake PhonePe call"
"""

import sys
import argparse
from pathlib import Path
import joblib
import pandas as pd
import numpy as np
from sklearn.metrics import classification_report, confusion_matrix, f1_score, accuracy_score

ARTIFACTS_DIR = Path("ml/training/artifacts")
PROCESSED_DIR = Path("ml/data/processed")
MODEL_PATH = ARTIFACTS_DIR / "baseline_model.joblib"
VECTORIZER_PATH = ARTIFACTS_DIR / "tfidf_vectorizer.joblib"
METADATA_PATH = ARTIFACTS_DIR / "model_metadata.json"

CONFIDENCE_THRESHOLD = 0.60


def load_artifacts():
    if not MODEL_PATH.exists():
        raise FileNotFoundError(f"Model not found at {MODEL_PATH}. Run train.py first.")
    if not VECTORIZER_PATH.exists():
        raise FileNotFoundError(f"Vectorizer not found at {VECTORIZER_PATH}. Run features.py first.")
    model = joblib.load(MODEL_PATH)
    vectorizer = joblib.load(VECTORIZER_PATH)
    return model, vectorizer


def predict_single(text: str):
    """Predicts category and confidence for an individual input string."""
    model, vectorizer = load_artifacts()
    X = vectorizer.transform([text.lower().strip()])
    probs = model.predict_proba(X)[0]
    classes = model.classes_

    best_idx = int(np.argmax(probs))
    best_cat = str(classes[best_idx])
    score = float(probs[best_idx])

    requires_review = bool(score < CONFIDENCE_THRESHOLD)
    advisory_category = "OTHER_UNCLASSIFIED" if requires_review else best_cat

    return {
        "input_text": text,
        "predicted_category": best_cat,
        "advisory_category": advisory_category,
        "confidence_score": round(score, 4),
        "requires_manual_review": requires_review,
        "score_type": "predict_proba",
        "model_version": "baseline-v1.0",
        "taxonomy_version": "1.0"
    }


def evaluate_split(split_name: str = "test"):
    """Evaluates the serialized model on a designated split (train/val/test)."""
    split_file = PROCESSED_DIR / f"{split_name}.csv"
    if not split_file.exists():
        raise FileNotFoundError(f"Split file {split_file} not found.")

    model, vectorizer = load_artifacts()
    df = pd.read_csv(split_file)
    print("=" * 70)
    print(f"  CyberSaarthi — Evaluating {MODEL_PATH.name} on '{split_name}.csv'")
    print(f"  Total samples: {len(df):,}")
    print("=" * 70)

    X = vectorizer.transform(df["text"])
    y_true = df["category"]
    y_pred = model.predict(X)

    acc = accuracy_score(y_true, y_pred)
    macro_f1 = f1_score(y_true, y_pred, average="macro", zero_division=0)
    weighted_f1 = f1_score(y_true, y_pred, average="weighted", zero_division=0)

    print(f"\nAccuracy   : {acc:.4f} ({acc*100:.2f}%)")
    print(f"Macro-F1   : {macro_f1:.4f}")
    print(f"Weighted-F1: {weighted_f1:.4f}\n")

    print(classification_report(y_true, y_pred, zero_division=0))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Evaluate CyberSaarthi Baseline Model")
    parser.add_argument("--split", type=str, default="test", choices=["train", "val", "test"], help="Dataset split to evaluate")
    parser.add_argument("--text", type=str, default=None, help="Single incident text to classify")

    args = parser.parse_args()

    if args.text:
        res = predict_single(args.text)
        print("\nSingle Input Prediction:")
        for k, v in res.items():
            print(f"  {k:<24}: {v}")
    else:
        evaluate_split(args.split)
