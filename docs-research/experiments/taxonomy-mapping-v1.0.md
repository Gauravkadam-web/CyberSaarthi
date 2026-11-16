# Taxonomy Mapping Specification v1.0
## CyberSaarthi — Phase 2 / Research Phase R2
**Taxonomy Version:** 1.0  
**Status:** Template — to be filled after dataset audit (audit_dataset.py output: `class_distribution.csv`)

> [!IMPORTANT]
> This mapping must be completed and committed **before any model training begins**.
> Every original dataset label must have a disposition: mapped to a Taxonomy v1.0 code OR marked `EXCLUDE`.
> This document is version-controlled. Any change invalidates previous trained models.

---

## 1. Mapping Instructions

For each row in `ml/data/audit_output/class_distribution.csv`:

1. Read the original label name and sample count.
2. Refer to `docs/taxonomy-v1.0.md` for each category's exact scope, inclusion rules, and exclusion rules.
3. Assign a **Taxonomy v1.0 code** (from Section 2 below) or mark `EXCLUDE`.
4. Set a **Mapping Confidence** level:
   - `HIGH` — Direct semantic match, minimal ambiguity
   - `MEDIUM` — Partial overlap; reasonable but debatable
   - `LOW` — Speculative or multi-category ambiguity; flag for supervisor review
5. Write clear **Mapping Notes** for every non-obvious decision.

---

## 2. Mapping Table

> **Status:** Template — populate after running `python ml/data/audit_dataset.py`

| Original Dataset Label | Sample Count (post-audit) | Maps To (Taxonomy v1.0 Code) | Mapping Confidence | Notes / Ambiguity |
|---|---|---|---|---|
| *(fill from class_distribution.csv)* | | | | |

### Template rows for common expected labels (update/delete as needed after audit):

| Original Dataset Label | Sample Count (post-audit) | Maps To (Taxonomy v1.0 Code) | Mapping Confidence | Notes / Ambiguity |
|---|---|---|---|---|
| `UPI Fraud` / `Online Banking Fraud` | — | `FINANCIAL_UPI_FRAUD` | HIGH | Direct match to Taxonomy Category 1 |
| `Phishing` / `Vishing` / `Smishing` | — | `PHISHING_SOCIAL_ENGINEERING` | HIGH | Direct match to Taxonomy Category 2 |
| `Identity Theft` / `Document Fraud` | — | `IDENTITY_THEFT` | HIGH | Direct match to Taxonomy Category 3 |
| `Social Media Hacking` / `Account Takeover` | — | `SOCIAL_ACCOUNT_COMPROMISE` | HIGH | Direct match to Taxonomy Category 4 |
| `Cyberstalking` / `Online Harassment` / `Blackmail` | — | `CYBERSTALKING_HARASSMENT` | HIGH | Direct match to Taxonomy Category 5 |
| `Job Scam` / `Fake Investment` / `Ponzi` | — | `JOB_INVESTMENT_SCAM` | HIGH | Direct match to Taxonomy Category 6 |
| `Malware` / `Ransomware` / `Hacking` / `RAT` | — | `MALWARE_RANSOMWARE` | HIGH | Direct match to Taxonomy Category 7 |
| `Other` / `Miscellaneous` / `Unclassified` | — | `OTHER_UNCLASSIFIED` | HIGH | Direct match to Taxonomy Category 8 |
| `Hate Speech` / `Defamation` | — | `CYBERSTALKING_HARASSMENT` | MEDIUM | Overlap with harassment; flag for review |
| `Spam` / `Unsolicited Marketing` | — | `EXCLUDE` | — | Out of scope for cybercrime taxonomy |
| `Child Pornography` / `CSAM` | — | `EXCLUDE` | — | Out of scope + ethical and legal risks; must not be retained |
| `Data Breach` (corporate/enterprise) | — | `EXCLUDE` | — | Beyond citizen reporting scope for MVP |

---

## 3. Exclusion Register

Document every label marked `EXCLUDE` with an explicit reason:

| Original Label | Rows Excluded | Reason |
|---|---|---|
| *(fill after audit)* | | |

**Total excluded rows:** *(fill)*  
**Total retained for training:** *(fill)*

---

## 4. Ambiguous & Medium-Confidence Decisions

> Any mapping marked MEDIUM or LOW **must** be reviewed by both team members before the mapping is frozen.

| Original Label | Mapping Decision | Ambiguity Description | Resolution |
|---|---|---|---|
| *(fill after audit)* | | | |

---

## 5. Final Label Distribution (Post-Mapping)

> Fill this table after applying the mapping above to the cleaned dataset.

| Taxonomy v1.0 Code | Display Name | Mapped Row Count | % of Total | Flag |
|---|---|---|---|---|
| `FINANCIAL_UPI_FRAUD` | Financial and UPI Fraud | — | — | — |
| `PHISHING_SOCIAL_ENGINEERING` | Phishing and Social Engineering | — | — | — |
| `IDENTITY_THEFT` | Identity Theft | — | — | — |
| `SOCIAL_ACCOUNT_COMPROMISE` | Social Media and Account Compromise | — | — | — |
| `CYBERSTALKING_HARASSMENT` | Cyberstalking and Online Harassment | — | — | — |
| `JOB_INVESTMENT_SCAM` | Job and Investment Scams | — | — | — |
| `MALWARE_RANSOMWARE` | Malware and Ransomware | — | — | — |
| `OTHER_UNCLASSIFIED` | Other or Unclassified | — | — | — |
| **TOTAL** | | — | 100% | — |

> [!NOTE]
> Any class with fewer than 80 samples after mapping must be flagged.
> Options: (a) accept as-is and report class imbalance, (b) supplement with carefully governed synthetic examples (requires supervisor approval), (c) merge with the most semantically similar category (requires taxonomy change — new version 1.1 needed).

---

## 6. Mapping Frozen Confirmation

Before Phase 3 begins, both team members sign off:

| Item | Status |
|---|---|
| All original labels have a disposition (mapped or excluded) | ☐ Pending |
| All MEDIUM/LOW confidence decisions reviewed | ☐ Pending |
| Exclusion register complete with counts | ☐ Pending |
| Final distribution table filled | ☐ Pending |
| Document committed to `main` branch | ☐ Pending |
| **Mapping Frozen** | ☐ Not yet frozen |

