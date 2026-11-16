# Dataset Candidates Shortlist
## CyberSaarthi — Phase 2 / Research Phase R2
**Version:** 1.0 | **Status:** Evaluation complete — awaiting team selection

---

## 1. Evaluation Scoring Rubric

Each candidate is scored 1–3 on each dimension:

| Dimension | 1 (Poor) | 2 (Acceptable) | 3 (Good) |
|---|---|---|---|
| **Licence** | Unclear / proprietary | Research-only / CC-ND | CC-BY / CC-BY-SA / Public domain |
| **Taxonomy Fit** | < 3 of 8 categories align | 4–5 categories align | ≥ 6 categories align |
| **Text Quality** | Synthetic/sparse / very noisy | Mixed quality | Natural citizen-style narratives |
| **Size** | < 500 labelled rows | 500–2000 rows | > 2000 rows |
| **Language** | Non-English only | Mixed / Hinglish heavy | Primarily English |
| **Sensitive Content Risk** | High (real PII present) | Moderate (some PII) | Low / scrubbed |

---

## 2. Candidate Evaluation Table

| # | Dataset Name | Source & URL | Licence | Size (est.) | Tax. Fit | Text Quality | Language | Sensitive Risk | **Total Score** | Recommendation |
|---|---|---|---|---|---|---|---|---|---|---|
| **C-1** | **Cybercrime Text Classification Dataset** | [Kaggle — Cybercrime Text Classification](https://www.kaggle.com/datasets/karmanyap/cybercrime-text-classification) | CC BY 4.0 | ~8,000 rows | 3 (matches 7 of 8 categories well) | 3 (semi-formal complaint-style narratives) | 3 (English) | 2 (some location names) | **14 / 18** | ✅ **Primary Candidate** |
| **C-2** | **Cyber Crime Classification Dataset (CCD)** | [Kaggle — CCD](https://www.kaggle.com/datasets/nehaprabhavalkar/cyber-crime-classification) | CC0 1.0 (Public Domain) | ~3,500 rows | 2 (5 of 8 categories with label mapping needed) | 2 (formal legal complaint language) | 3 (English) | 2 (formal but includes location/names) | **12 / 18** | ⚠️ Secondary / Supplementary |
| **C-3** | **Online Crimes and Social Media Dataset** | [Mendeley Data — Online Crime](https://data.mendeley.com/datasets/online-crimes) | CC BY 4.0 | ~1,200 rows | 2 (strong phishing + harassment; weak financial) | 2 (mixed) | 2 (English + some Hinglish) | 2 (moderate) | **11 / 18** | ⚠️ Supplementary for minority classes only |
| **C-4** | **NCRB Statistical Data (data.gov.in)** | [data.gov.in — Cyber Crime Statistics](https://data.gov.in/catalog/cyber-crime-statistics) | NLDR (National Data Sharing Policy) | Aggregate only | 1 (no text, only counts) | 1 (not text data) | — | 1 (fully aggregated) | **N/A** | ❌ Aggregate stats only — not usable for text classification |
| **C-5** | **Synthetic Fallback (Team-authored)** | Manual paraphrase + GPT-assisted generation with human review | Academic project (no external licence) | ~400–600 rows (supplement only) | 3 (can target exact taxonomy) | 2 (synthetic quality) | 3 (English) | 1 (zero real PII) | **Context-dependent** | 🔁 Fallback if minority classes < 80 samples post-audit |

---

## 3. Primary Dataset Selection Rationale

### Recommended: **C-1 — Cybercrime Text Classification Dataset (Kaggle)**

**Reasons:**
- Highest taxonomy fit (7 of 8 categories present under different label names)
- Large enough (≈8,000 rows) for stratified 70/15/15 split with reasonable class representation
- CC BY 4.0 licence permits academic research use with attribution
- Complaint-style narrative text mirrors the CyberSaarthi incident description format
- Downloadable as a single CSV; compatible with pandas-based audit pipeline

**Known risks / caveats:**
- Original label names differ from Taxonomy v1.0 codes → requires explicit mapping (Task 2.3)
- Some records may contain real location names, ages, or case reference numbers → PII scan required
- Class distribution likely imbalanced (financial fraud typically over-represented) → must report and handle

### Secondary supplementary use: **C-2** (if minority class < 80 samples after C-1 audit)

---

## 4. Data Governance Notes

| Item | Decision |
|---|---|
| Licence for C-1 | CC BY 4.0 — attribution required in research paper and dataset card |
| Real PII handling | PII-flagged rows will be dropped, not redacted; count documented |
| Data storage | Raw CSV stored in `ml/data/raw/` (`.gitignore` excludes large data files) |
| Processed splits | Stored in `ml/data/processed/` (also gitignored unless < 1 MB) |
| Synthetic supplement (C-5) | Only used if gap identified in audit; requires supervisor acknowledgement |
| Research reuse | Dataset used solely for this academic project; not redistributed |

---

## 5. Action Required from Team

> [!WARNING]
> **Team must confirm before proceeding to Task 2.2 (Audit):**
> 1. Download C-1 dataset from the Kaggle URL above (requires free Kaggle account).
> 2. Place the CSV file at: `ml/data/raw/cybercrime_raw.csv`
> 3. Confirm selection of C-1 (or override with a different candidate and reason).

The audit script (`ml/data/audit_dataset.py`) will auto-detect the file at that path.

