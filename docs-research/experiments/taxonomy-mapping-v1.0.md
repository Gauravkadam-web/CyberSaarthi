# Taxonomy Mapping Specification v1.0
## CyberSaarthi — Phase 2 / Research Phase R2
**Taxonomy Version:** 1.0  
**Status:** Mapped & Frozen for Experiment Pipeline  
**Dataset Reference:** Sentinel Multi-Class Scam & Cybercrime Text Dataset (MD5: `b8f2d274a27b2384ac84700c4e7fc29e`)

---

## 1. Mapping Overview

Following the completion of the dataset audit (`audit_dataset.py`), the original dataset labels have been audited and mapped to CyberSaarthi's Taxonomy v1.0 specifications as defined in `docs/taxonomy-v1.0.md`.

---

## 2. Mapping Table

| Original Dataset Label | Sample Count (post-PII) | Maps To (Taxonomy v1.0 Code) | Mapping Confidence | Mapping Rationale / Notes |
|---|---|---|---|---|
| `financial_fraud` | 3,080 | `FINANCIAL_UPI_FRAUD` | HIGH | Covers fraudulent transfers, lottery scams, crypto investment schemes, and task fraud. Directly aligns with Category 1. |
| `government_impersonation` | 2,905 | `PHISHING_SOCIAL_ENGINEERING` | HIGH | Fake notices from CBI, Police, Supreme Court, TRAI, and tax authorities designed to extract credentials and funds via social engineering. Category 2. |
| `bank_phishing` | 1,875 | `PHISHING_SOCIAL_ENGINEERING` | HIGH | Deceptive communications impersonating commercial banks (SBI, HDFC, ICICI), KYC expiry alerts, and phishing links. Category 2. |
| `romance_parcel_sextortion` | 540 | `CYBERSTALKING_HARASSMENT` | HIGH | Digital extortion, sextortion blackmail, and abusive pressure campaigns. Category 5. Sample count (540) well exceeds the $\ge 80$ sample threshold. |
| `legit` | 4,822 | `EXCLUDE` | — | Genuine commercial bank SMS, utility recharges, and legitimate notifications. Excluded from multi-class cybercrime model training to ensure focus on incident descriptions. |

---

## 3. Exclusion Register

| Original Label | Rows Excluded | Reason for Exclusion |
|---|---|---|
| `legit` | 4,822 | Non-crime benign transactional messages. The CyberSaarthi model is an incident categorization engine for submitted victim reports, not a binary spam filter. |
| PII-flagged records | 778 | Dropped in compliance with data minimization rules (phone numbers/account IDs). |

* **Total raw rows:** 14,000
* **Total excluded rows:** 5,600 (4,822 legit + 778 PII)
* **Total retained cyber incident rows:** **8,400**

---

## 4. Final Label Distribution for Model Training

| Taxonomy v1.0 Code | Display Name | Mapped Row Count | % of Incident Dataset | Balance Flag |
|---|---|---|---|---|
| `FINANCIAL_UPI_FRAUD` | Financial and UPI Fraud | 3,080 | 36.7% | Robust majority |
| `PHISHING_SOCIAL_ENGINEERING` | Phishing and Social Engineering | 4,780 | 56.9% | Combined (Govt + Bank) |
| `CYBERSTALKING_HARASSMENT` | Cyberstalking and Online Harassment | 540 | 6.4% | Satisfies $>80$ threshold |
| **TOTAL INCIDENT TRAINING SET** | | **8,400** | **100.0%** | Ready for 70/15/15 Split |

> [!NOTE]
> **Minority Class Handling in Protocol:**  
> `CYBERSTALKING_HARASSMENT` comprises 540 rows (6.4%). Under our stratified 70/15/15 split:
> - Training: $\approx 378$ samples
> - Validation: $\approx 81$ samples
> - Test: $\approx 81$ samples
> This provides adequate statistical support without requiring synthetic generation for baseline models.
> Categories with zero representation in this specific SMS dataset (`MALWARE_RANSOMWARE`, `IDENTITY_THEFT`, `SOCIAL_ACCOUNT_COMPROMISE`) will be augmented in Phase 3 experiments using synthetic incident templates or benchmark supplementary corpora as specified in `dataset-candidates.md` (C-5).

---

## 5. Mapping Sign-Off & Freeze

| Verification Item | Status |
|---|---|
| All original labels accounted for | ✅ Complete |
| Exclusions justified and documented | ✅ Complete |
| PII removed | ✅ Complete (778 dropped) |
| Mapping approved by team | ✅ Frozen for Phase 3 |
