"""
CyberSaarthi — TF-IDF Feature Extraction Pipeline
Phase 3 / Task 3.2
==================================================
Fits TF-IDF vectorizer exclusively on the training partition
and transforms validation and test sets without data leakage.

Usage:
    python ml/training/features.py
"""

from pathlib import Path
import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
PROCESSED_DIR = Path("ml/data/processed")
ARTIFACTS_DIR = Path("ml/training/artifacts")
VECTORIZER_PATH = ARTIFACTS_DIR / "tfidf_vectorizer.joblib"

NGRAM_RANGE = (1, 2)
MAX_FEATURES = 10000
MIN_DF = 2
SUBLINEAR_TF = True


def build_vectorizer() -> TfidfVectorizer:
    """Instantiates a TF-IDF vectorizer with frozen hyperparameters."""
    return TfidfVectorizer(
        ngram_range=NGRAM_RANGE,
        max_features=MAX_FEATURES,
        min_df=MIN_DF,
        sublinear_tf=SUBLINEAR_TF,
        stop_words="english",
        strip_accents="unicode",
        lowercase=True,
    )


def fit_and_save_vectorizer(train_texts: pd.Series) -> TfidfVectorizer:
    """Fits vectorizer strictly on training texts and saves artifact."""
    ARTIFACTS_DIR.mkdir(parents=True, exist_ok=True)
    vectorizer = build_vectorizer()
    print("Fitting TF-IDF Vectorizer on training data...")
    vectorizer.fit(train_texts)
    joblib.dump(vectorizer, VECTORIZER_PATH)
    print(f"Fitted vectorizer saved to {VECTORIZER_PATH}")
    print(f"Vocabulary size: {len(vectorizer.vocabulary_):,} features")
    return vectorizer


def load_vectorizer() -> TfidfVectorizer:
    """Loads the serialized TF-IDF vectorizer artifact."""
    if not VECTORIZER_PATH.exists():
        raise FileNotFoundError(f"Vectorizer not found at {VECTORIZER_PATH}. Run features.py first.")
    return joblib.load(VECTORIZER_PATH)


def run_feature_pipeline():
    print("=" * 70)
    print("  CyberSaarthi — TF-IDF Feature Extraction Pipeline")
    print("=" * 70)

    train_path = PROCESSED_DIR / "train.csv"
    val_path = PROCESSED_DIR / "val.csv"
    test_path = PROCESSED_DIR / "test.csv"

    if not train_path.exists():
        raise FileNotFoundError(f"Train data not found at {train_path}. Run preprocess.py first.")

    train_df = pd.read_csv(train_path)
    val_df = pd.read_csv(val_path)
    test_df = pd.read_csv(test_path)

    # Strict boundary: fit on train ONLY
    vectorizer = fit_and_save_vectorizer(train_df["text"])

    # Transform all three partitions
    X_train = vectorizer.transform(train_df["text"])
    X_val = vectorizer.transform(val_df["text"])
    X_test = vectorizer.transform(test_df["text"])

    print(f"\nFeature Matrices Extracted:")
    print(f"  X_train: {X_train.shape[0]:,} rows x {X_train.shape[1]:,} features")
    print(f"  X_val  : {X_val.shape[0]:,} rows x {X_val.shape[1]:,} features")
    print(f"  X_test : {X_test.shape[0]:,} rows x {X_test.shape[1]:,} features")

    # Sample top features
    feature_names = vectorizer.get_feature_names_out()
    print(f"\nSample top n-gram features: {list(feature_names[:15])}")
    print("\nFeature extraction completed successfully.")
    return X_train, X_val, X_test


if __name__ == "__main__":
    run_feature_pipeline()
