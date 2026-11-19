"""
CyberSaarthi — End-to-End Master ML Pipeline Runner
Phase 3 / Master Orchestrator
===================================================
Executes the full machine learning training and evaluation pipeline
in a single deterministic run:
  1. Data preprocessing and stratified 70/15/15 splitting (seed=42)
  2. TF-IDF feature extraction (fit strictly on Train only)
  3. 5-fold cross-validation on candidate models (MNB, LR, Linear SVM)
  4. Validation set comparison and predeclared model selection
  5. Single-pass held-out test set evaluation
  6. Artifact serialization and reproducibility metadata export

Usage:
    python ml/training/run_pipeline.py
"""

import sys
import time
from pathlib import Path

# Add script directory to sys.path so modules import seamlessly
CURRENT_DIR = Path(__file__).resolve().parent
if str(CURRENT_DIR) not in sys.path:
    sys.path.insert(0, str(CURRENT_DIR))

from preprocess import run_preprocessing
from features import run_feature_pipeline
from train import run_training_pipeline


def main():
    start_total = time.perf_counter()
    print("=" * 75)
    print("  CYBERSAARTHI — END-TO-END BASELINE MACHINE LEARNING PIPELINE")
    print("=" * 75)

    try:
        # Step 1: Preprocessing & Splitting
        print("\n>>> STEP 1 / 3: Preprocessing & Stratified Splitting")
        run_preprocessing()

        # Step 2: Feature Extraction (TF-IDF fit on Train only)
        print("\n>>> STEP 2 / 3: Feature Extraction Pipeline (Train-only TF-IDF)")
        run_feature_pipeline()

        # Step 3: Model Training, Validation Selection & Test Evaluation
        print("\n>>> STEP 3 / 3: Model Training, Tuning, Selection & Evaluation")
        metadata = run_training_pipeline()

        total_elapsed = time.perf_counter() - start_total
        print("\n" + "=" * 75)
        print(f"  ALL PIPELINE STAGES COMPLETED SUCCESSFULLY IN {total_elapsed:.2f} SECONDS!")
        print(f"  Production Model : {metadata['winning_model_name']}")
        print(f"  Test Accuracy    : {metadata['test_metrics']['accuracy']*100:.2f}%")
        print(f"  Test Macro-F1    : {metadata['test_metrics']['macro_f1']:.4f}")
        print(f"  Inference Latency: {metadata['test_metrics']['latency_p50_ms']} ms (p50)")
        print(f"  Model Artifact   : ml/training/artifacts/{metadata['model_file']}")
        print(f"  Metadata File    : ml/training/artifacts/model_metadata.json")
        print("=" * 75)

    except Exception as e:
        print(f"\n[PIPELINE ERROR] Pipeline execution failed: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
