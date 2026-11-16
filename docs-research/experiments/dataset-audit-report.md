# Dataset Audit Report
## CyberSaarthi — Phase 2 / Research Phase R2
**Status:** Template — populate after running `python ml/data/audit_dataset.py`

> [!NOTE]
> This document is a structured report template. Run the audit script and paste the output from
> `ml/data/audit_output/audit_summary.txt` into the sections below. 
> The report must be completed and reviewed by both team members before proceeding to Phase 3.

---

## 1. Dataset Under Audit

| Field | Value |
|---|---|
| **Dataset Name** | *(e.g., Cybercrime Text Classification Dataset)* |
| **Source URL** | *(paste Kaggle/Mendeley URL)* |
| **Downloaded By** | *(team member name)* |
| **Download Date** | *(YYYY-MM-DD)* |
| **Raw File Name** | `cybercrime_raw.csv` |
| **Raw File MD5 Hash** | *(run `certutil -hashfile ml/data/raw/cybercrime_raw.csv MD5` on Windows)* |
| **Licence** | *(e.g., CC BY 4.0)* |
| **Raw Row Count** | *(paste from audit output)* |

---

## 2. Audit Script Output

### 2.1 Missing Values
*(paste from audit_summary.txt)*

### 2.2 Text Length Analysis
*(paste from audit_summary.txt)*

### 2.3 Class Distribution (Original Labels)
*(paste from audit_summary.txt or embed class_distribution.csv content here)*

### 2.4 Exact Duplicates
*(paste from audit_summary.txt)*

### 2.5 Near-Duplicate Pairs
*(paste count from audit_summary.txt; review duplicate_pairs.csv for samples)*

### 2.6 Language Audit
*(paste from audit_summary.txt — count of non-English/Hinglish rows)*

### 2.7 PII Scan
*(paste from audit_summary.txt — count and types of PII found)*

---

## 3. Final Usable Row Count

| Stage | Row Count |
|---|---|
| Raw CSV | |
| After NaN drop | |
| After short-text filter | |
| After exact deduplication | |
| After language filter (English-only) | |
| After PII row drop | |
| **Final usable rows** | |

---

## 4. Data Quality Verdict

| Check | Result | Action Required |
|---|---|---|
| Licence permits academic research use | ☐ Yes / ☐ No | |
| Total usable rows sufficient (≥ 500) | ☐ Yes / ☐ No | |
| Taxonomy coverage (≥ 6 of 8 categories mappable) | ☐ Yes / ☐ No | |
| No CSAM or severe sensitive content | ☐ Yes / ☐ No | |
| PII rows dropped or resolved | ☐ Yes / ☐ No | |
| Near-duplicate risk acceptable | ☐ Yes / ☐ No | |
| **Overall: Dataset approved for training** | ☐ Approved / ☐ Rejected | |

---

## 5. Team Sign-Off

| Member | Reviewed | Date |
|---|---|---|
| Member A | ☐ | |
| Member B | ☐ | |

