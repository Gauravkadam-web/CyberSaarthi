"""
CyberSaarthi — Dataset Audit Script
Phase 2 / Research Phase R2
====================================
Run this script AFTER placing the raw dataset CSV at:
    ml/data/raw/cybercrime_raw.csv

Usage:
    python ml/data/audit_dataset.py

Outputs:
    - Console summary of all audit checks
    - ml/data/audit_output/class_distribution.csv
    - ml/data/audit_output/duplicate_pairs.csv
    - ml/data/audit_output/pii_flagged_rows.csv
    - ml/data/audit_output/audit_summary.txt

Requirements:
    See ml/training/requirements.txt
    Install: pip install -r ml/training/requirements.txt
"""

import os
import re
import sys
import hashlib
import textwrap
from datetime import datetime
from pathlib import Path

import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# ---------------------------------------------------------------------------
# Configuration — align with Taxonomy v1.0 and experiment protocol
# ---------------------------------------------------------------------------
RAW_CSV_PATH = Path("ml/data/raw/cybercrime_raw.csv")
OUTPUT_DIR = Path("ml/data/audit_output")

# Column names — defaults with automatic detection fallback
TEXT_COLUMN = "text"              # Column containing incident description text
LABEL_COLUMN = "category_name"    # Column containing original category label

TEXT_COLUMN_CANDIDATES = ["text", "description", "incident_description", "narrative", "complaint"]
LABEL_COLUMN_CANDIDATES = ["category_name", "category", "label", "class", "target"]

# Near-duplicate detection threshold
NEAR_DUPLICATE_THRESHOLD = 0.90   # Cosine similarity >= this → flagged as near-duplicate

# Minimum text length to be considered a usable record
MIN_TEXT_LENGTH = 20              # characters

# PII detection patterns (conservative — flag for human review)
PII_PATTERNS = {
    "phone_number":    r"\b[6-9]\d{9}\b",
    "aadhaar_number":  r"\b\d{4}\s?\d{4}\s?\d{4}\b",
    "pan_card":        r"\b[A-Z]{5}[0-9]{4}[A-Z]\b",
    "email_address":   r"[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}",
    "credit_card":     r"\b(?:\d[ -]?){13,16}\b",
    "account_number":  r"\b\d{9,18}\b",
}

TAXONOMY_V1_CODES = [
    "FINANCIAL_UPI_FRAUD",
    "PHISHING_SOCIAL_ENGINEERING",
    "IDENTITY_THEFT",
    "SOCIAL_ACCOUNT_COMPROMISE",
    "CYBERSTALKING_HARASSMENT",
    "JOB_INVESTMENT_SCAM",
    "MALWARE_RANSOMWARE",
    "OTHER_UNCLASSIFIED",
]


# ---------------------------------------------------------------------------
# Utility helpers
# ---------------------------------------------------------------------------

def separator(title: str) -> None:
    print(f"\n{'=' * 70}")
    print(f"  {title}")
    print(f"{'=' * 70}")


def write_summary_line(f, line: str) -> None:
    print(line)
    f.write(line + "\n")


# ---------------------------------------------------------------------------
# Main audit logic
# ---------------------------------------------------------------------------

def run_audit() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    summary_path = OUTPUT_DIR / "audit_summary.txt"

    with open(summary_path, "w", encoding="utf-8") as summary_file:
        ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        summary_file.write(f"CyberSaarthi Dataset Audit Report\nGenerated: {ts}\n\n")

        # ------------------------------------------------------------------ #
        # 1. Load dataset                                                     #
        # ------------------------------------------------------------------ #
        separator("1. LOADING DATASET")
        if not RAW_CSV_PATH.exists():
            msg = (
                f"ERROR: Raw dataset not found at '{RAW_CSV_PATH}'.\n"
                f"Please download the dataset and place it at that path.\n"
                f"See docs-research/experiments/dataset-candidates.md for instructions."
            )
            print(msg)
            sys.exit(1)

        df = pd.read_csv(RAW_CSV_PATH, low_memory=False)
        total_rows = len(df)
        write_summary_line(summary_file, f"Raw CSV loaded: {RAW_CSV_PATH}")
        write_summary_line(summary_file, f"Total rows     : {total_rows:,}")
        write_summary_line(summary_file, f"Columns        : {list(df.columns)}")

        # Resolve text and label columns
        global TEXT_COLUMN, LABEL_COLUMN
        if TEXT_COLUMN not in df.columns:
            for cand in TEXT_COLUMN_CANDIDATES:
                if cand in df.columns:
                    TEXT_COLUMN = cand
                    break
        if LABEL_COLUMN not in df.columns:
            for cand in LABEL_COLUMN_CANDIDATES:
                if cand in df.columns:
                    LABEL_COLUMN = cand
                    break

        write_summary_line(summary_file, f"Active text column : '{TEXT_COLUMN}'")
        write_summary_line(summary_file, f"Active label column: '{LABEL_COLUMN}'")

        # Validate required columns
        for col in [TEXT_COLUMN, LABEL_COLUMN]:
            if col not in df.columns:
                print(f"\nERROR: Expected column '{col}' not found in CSV.")
                print(f"Found columns: {list(df.columns)}")
                print("Update TEXT_COLUMN / LABEL_COLUMN in this script to match your CSV header.")
                sys.exit(1)

        # ------------------------------------------------------------------ #
        # 2. Missing values                                                   #
        # ------------------------------------------------------------------ #
        separator("2. MISSING VALUES")
        missing = df[[TEXT_COLUMN, LABEL_COLUMN]].isnull().sum()
        write_summary_line(summary_file, f"Missing in '{TEXT_COLUMN}' : {missing[TEXT_COLUMN]}")
        write_summary_line(summary_file, f"Missing in '{LABEL_COLUMN}': {missing[LABEL_COLUMN]}")

        df_clean = df.dropna(subset=[TEXT_COLUMN, LABEL_COLUMN]).copy()
        write_summary_line(summary_file, f"Rows after dropping NaN   : {len(df_clean):,}")

        # ------------------------------------------------------------------ #
        # 3. Text length distribution                                         #
        # ------------------------------------------------------------------ #
        separator("3. TEXT LENGTH ANALYSIS")
        df_clean["text_length"] = df_clean[TEXT_COLUMN].astype(str).str.len()
        too_short = (df_clean["text_length"] < MIN_TEXT_LENGTH).sum()
        write_summary_line(summary_file, f"Min text length  : {df_clean['text_length'].min()}")
        write_summary_line(summary_file, f"Max text length  : {df_clean['text_length'].max()}")
        write_summary_line(summary_file, f"Median length    : {df_clean['text_length'].median():.0f} chars")
        write_summary_line(summary_file, f"Rows < {MIN_TEXT_LENGTH} chars (too short): {too_short}")

        df_clean = df_clean[df_clean["text_length"] >= MIN_TEXT_LENGTH].copy()
        write_summary_line(summary_file, f"Rows after short-text filter: {len(df_clean):,}")

        # ------------------------------------------------------------------ #
        # 4. Label inventory & class distribution                             #
        # ------------------------------------------------------------------ #
        separator("4. CLASS DISTRIBUTION (ORIGINAL LABELS)")
        label_counts = (
            df_clean[LABEL_COLUMN]
            .value_counts()
            .reset_index()
            .rename(columns={"index": "original_label", LABEL_COLUMN: "count"})
        )
        label_counts.columns = ["original_label", "count"]
        label_counts["percentage"] = (label_counts["count"] / len(df_clean) * 100).round(2)
        label_counts["flag"] = label_counts["count"].apply(
            lambda c: "⚠️  MINORITY (<80)" if c < 80 else ("⚠️  IMBALANCED (<200)" if c < 200 else "OK")
        )

        for _, row in label_counts.iterrows():
            line = f"  {str(row['original_label']):<45} {row['count']:>5} rows  ({row['percentage']:>5.1f}%)  {row['flag']}"
            write_summary_line(summary_file, line)

        label_counts.to_csv(OUTPUT_DIR / "class_distribution.csv", index=False)
        write_summary_line(summary_file, f"\nSaved: {OUTPUT_DIR / 'class_distribution.csv'}")

        # ------------------------------------------------------------------ #
        # 5. Exact duplicate detection                                        #
        # ------------------------------------------------------------------ #
        separator("5. EXACT DUPLICATE DETECTION")
        df_clean["text_hash"] = df_clean[TEXT_COLUMN].astype(str).apply(
            lambda t: hashlib.md5(t.strip().lower().encode()).hexdigest()
        )
        exact_dupes = df_clean["text_hash"].duplicated(keep="first").sum()
        write_summary_line(summary_file, f"Exact duplicate rows: {exact_dupes}")
        df_clean = df_clean[~df_clean["text_hash"].duplicated(keep="first")].copy()
        write_summary_line(summary_file, f"Rows after exact dedup: {len(df_clean):,}")

        # ------------------------------------------------------------------ #
        # 6. Near-duplicate detection (sample-based for large datasets)       #
        # ------------------------------------------------------------------ #
        separator("6. NEAR-DUPLICATE DETECTION (Cosine Similarity)")
        SAMPLE_SIZE = min(2000, len(df_clean))
        df_sample = df_clean.sample(n=SAMPLE_SIZE, random_state=42)

        tfidf = TfidfVectorizer(max_features=5000, ngram_range=(1, 2))
        tfidf_matrix = tfidf.fit_transform(df_sample[TEXT_COLUMN].astype(str))

        near_dup_pairs = []
        # Only check consecutive chunks to keep runtime manageable
        CHUNK = 500
        for i in range(0, SAMPLE_SIZE, CHUNK):
            chunk_matrix = tfidf_matrix[i : i + CHUNK]
            sims = cosine_similarity(chunk_matrix, tfidf_matrix[i:])
            rows, cols = np.where(sims >= NEAR_DUPLICATE_THRESHOLD)
            for r, c in zip(rows, cols):
                abs_r, abs_c = i + r, i + c
                if abs_r < abs_c:
                    near_dup_pairs.append(
                        {
                            "index_a": df_sample.index[abs_r],
                            "index_b": df_sample.index[abs_c],
                            "similarity": round(float(sims[r, c]), 4),
                            "text_a": df_sample[TEXT_COLUMN].iloc[abs_r][:120],
                            "text_b": df_sample[TEXT_COLUMN].iloc[abs_c][:120],
                        }
                    )

        near_dup_df = pd.DataFrame(near_dup_pairs)
        write_summary_line(
            summary_file,
            f"Near-duplicate pairs found (sample of {SAMPLE_SIZE}): {len(near_dup_df)}"
        )
        if not near_dup_df.empty:
            near_dup_df.to_csv(OUTPUT_DIR / "duplicate_pairs.csv", index=False)
            write_summary_line(summary_file, f"Saved: {OUTPUT_DIR / 'duplicate_pairs.csv'}")
        else:
            write_summary_line(summary_file, "No near-duplicate pairs detected above threshold.")

        # ------------------------------------------------------------------ #
        # 7. Language audit (basic heuristic: non-ASCII character ratio)      #
        # ------------------------------------------------------------------ #
        separator("7. LANGUAGE AUDIT")
        def non_ascii_ratio(text: str) -> float:
            text = str(text)
            if not text:
                return 0.0
            non_ascii = sum(1 for c in text if ord(c) > 127)
            return non_ascii / len(text)

        df_clean["non_ascii_ratio"] = df_clean[TEXT_COLUMN].apply(non_ascii_ratio)
        hinglish_suspect = (df_clean["non_ascii_ratio"] > 0.05).sum()
        write_summary_line(summary_file, f"Rows with >5% non-ASCII (possible Hinglish/Hindi): {hinglish_suspect}")
        write_summary_line(summary_file, "Action: These rows will be flagged and EXCLUDED from English-only training set.")

        df_english = df_clean[df_clean["non_ascii_ratio"] <= 0.05].copy()
        write_summary_line(summary_file, f"Rows after language filter (English-only): {len(df_english):,}")

        # ------------------------------------------------------------------ #
        # 8. PII scan                                                         #
        # ------------------------------------------------------------------ #
        separator("8. PII / SENSITIVE CONTENT SCAN")
        pii_flags = []
        for idx, text in df_english[TEXT_COLUMN].astype(str).items():
            found = []
            for pii_type, pattern in PII_PATTERNS.items():
                if re.search(pattern, text):
                    found.append(pii_type)
            if found:
                pii_flags.append({"row_index": idx, "pii_types_found": ", ".join(found), "text_snippet": text[:200]})

        pii_df = pd.DataFrame(pii_flags)
        write_summary_line(summary_file, f"Rows with potential PII detected: {len(pii_df)}")
        if not pii_df.empty:
            pii_df.to_csv(OUTPUT_DIR / "pii_flagged_rows.csv", index=False)
            write_summary_line(summary_file, f"Saved: {OUTPUT_DIR / 'pii_flagged_rows.csv'}")
            write_summary_line(summary_file, "Action: PII-flagged rows will be DROPPED (not redacted) before training.")
        else:
            write_summary_line(summary_file, "No PII patterns detected. Proceeding with full set.")

        df_final = df_english[~df_english.index.isin(pii_df["row_index"] if not pii_df.empty else [])].copy()
        write_summary_line(summary_file, f"\nFINAL USABLE ROWS after all filters: {len(df_final):,}")

        # ------------------------------------------------------------------ #
        # 9. Summary verdict                                                  #
        # ------------------------------------------------------------------ #
        separator("9. AUDIT SUMMARY VERDICT")
        verdict_lines = [
            f"  Total rows in raw CSV          : {total_rows:,}",
            f"  Rows after NaN drop            : {len(df_clean):,}",
            f"  Rows after short-text filter   : (included above)",
            f"  Rows after exact deduplication : (included above)",
            f"  Rows after language filter     : {len(df_english):,}",
            f"  PII rows dropped               : {len(pii_df)}",
            f"  FINAL USABLE ROWS              : {len(df_final):,}",
            "",
            "  Next step: Fill in taxonomy-mapping-v1.0.md using class_distribution.csv",
            "  then run the train/val/test split in Phase 3.",
        ]
        for line in verdict_lines:
            write_summary_line(summary_file, line)

        print(f"\nAudit complete. Full summary saved to: {summary_path}")


if __name__ == "__main__":
    run_audit()

