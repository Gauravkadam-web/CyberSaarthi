# Literature Review Matrix & Research Gap Analysis
## CyberSaarthi — Cybercrime Awareness and Reporting Portal
**Document Version:** 1.0 | **Phase:** Phase 1 / Phase R1  
**Target:** Academic MCA Project & Research Foundations

---

## 1. Overview and Review Methodology

This literature review investigates existing scholarly work across three intersecting domains:
1. **Incident Text Classification & NLP:** Application of supervised text classification (Multinomial Naive Bayes, Support Vector Machines, Logistic Regression, neural methods) on cybercrime, fraud, and abuse narratives.
2. **Cybercrime Awareness Interventions:** Educational frameworks, simulated training, pre-test/post-test assessment methodologies, and knowledge retention.
3. **Human-in-the-Loop Decision Support in Incident Reporting:** Systems that balance automated machine learning assistance with human review to maintain legal, ethical, and operational safety.

---

## 2. Literature Review Matrix

| # | Citation & Source | Focus Area | Dataset / Source | Methods / Models | Key Findings | Stated Limitations | Relevance to CyberSaarthi |
|---|---|---|---|---|---|---|---|
| **1** | Al-Garadi et al. (2016), *IEEE Access* | Cyberbullying & Online Harassment Detection | Twitter / Social media textual dataset | Naive Bayes, SVM, Random Forest, KNN with TF-IDF and n-grams | Linear SVM and Naive Bayes achieved competitive baseline performance (~85-88% F1) for binary harassment identification. | High class imbalance led to significant false positives on minority abusive classes; no advisory human review layer. | Confirms Linear SVM and MNB as strong, lightweight baselines; emphasizes the necessity of macro-F1 over simple accuracy. |
| **2** | Senthilkumar et al. (2021), *J. Information Security & Applications* | Phishing & Social Engineering Classification | Public email and SMS scam corpora | Multinomial Naive Bayes, Logistic Regression, BiLSTM | Classical ML (Logistic Regression, MNB) reached 92-94% accuracy with sub-10ms inference latency; deep learning offered marginal gains at high computational cost. | Tested exclusively on clean, curated benchmark sets with uniform text length; performance degrades on informal victim narratives. | Justifies selecting classical models (Logistic Regression, MNB, Linear SVM) for real-time web inference in FastAPI. |
| **3** | Wash (2010), *ACM Transactions on Computer-Human Interaction (TOCHI)* | Folk Models of Cyber Threats & User Decision-Making | Qualitative interviews with non-expert computer users | Semi-structured thematic analysis | Users categorize threats into flawed "folk models" (e.g., conflating viruses with phishing), preventing them from taking correct response steps. | Qualitative only; small sample size (33 participants); did not evaluate an educational software intervention. | Direct justification for CyberSaarthi's 8-category structured taxonomy and educational guidance modules. |
| **4** | Jensen et al. (2017), *Computers & Security* | Effectiveness of Cyber Awareness Training | Controlled experiment in corporate setting | Pre-test / Post-test experimental design | Structured interactive awareness training produced a measurable knowledge gain (+22% immediate retention), but knowledge decayed over 90 days without reinforcement. | Focused on enterprise phishing; lacked integration with incident reporting tools; high attrition between tests. | Direct precedent for Track B (Pre-test vs Post-test awareness evaluation paired with quiz scoring). |
| **5** | Edwards et al. (2017), *ACM Workshop on Security Information Workers (WSIW)* | Incident Reporting Usability & Victim Burden | Analysis of consumer cybercrime reporting platforms | Cognitive walkthrough and usability analysis | Complex legal jargon, ambiguous categories, and lack of status visibility discourage victims from completing reports or reporting early. | Did not test an automated classification prototype. | Informs CyberSaarthi's UI: plain language, immediate reference tracking, and clear boundary disclaimers. |
| **6** | Amjad et al. (2020), *Neural Computing and Applications* | Multi-class Cybercrime Text Categorization | Scraped forum and consumer complaint data | Multinomial Naive Bayes, SVM, Decision Trees, BERT | Multi-class text categorization suffers severely when minority classes have fewer samples; macro-F1 drops by >15% without careful tuning. | Ignored confidence calibration; treated model outputs as authoritative classifications rather than suggestions. | Proves why CyberSaarthi must enforce human-in-the-loop review for low-confidence outputs and report macro-F1. |
| **7** | National Crime Records Bureau (NCRB) & I4C Reports (2022–2023) | Indian Cybercrime Incidence & Reporting Patterns | Official Indian cybercrime portal (1930 / cybercrime.gov.in) statistics | Statistical trend analysis | Financial fraud (UPI scams, phishing, OTP fraud) comprises over 65% of reported cyber incidents in India. | Aggregate statistics only; detailed victim narratives are confidential. | Informs our taxonomy weighting: Financial and UPI Fraud is our primary priority category. |

---

## 3. Thematic Synthesis

### Theme 1: Classical Text Classification vs Complex Deep Learning
Prior literature consistently shows that for short, domain-specific text classification tasks:
* **Multinomial Naive Bayes (MNB), Logistic Regression, and Linear SVM** provide robust, mathematically explainable, and ultra-fast inference baselines ($< 15$ ms).
* While deep neural networks (Transformers, BERT) occasionally deliver modest F1 score increments (1–3%), their resource footprint, dependency overhead, and latency are poorly suited for lightweight student architectures unless justified by rigorous baseline benchmarking.
* Prior studies frequently committed methodological errors: reporting accuracy on imbalanced classes and leaking preprocessing parameters into test splits.

### Theme 2: The Disconnect Between Awareness and Action
* Existing academic tools either build **pure awareness websites** (passive reading) or **mock reporting portals** (data collection forms).
* Very few systems integrate **contextual awareness education** directly alongside an **advisory incident documentation system**. Users facing an active scam need both immediate remedial guidance (e.g., "call 1930 immediately", "freeze your UPI account") and structured documentation to clarify their thoughts.

### Theme 3: Ethical Boundaries and Human Oversight
* Machine learning text classifiers can be overconfident or produce hallucinations on ambiguous or novel scam patterns.
* Making automated determinations in a cybercrime context is hazardous. Scholarly consensus mandates a **Human-in-the-Loop (HITL)** architecture: the AI proposes an advisory suggestion, but human reviewers (administrators) remain the ultimate authority.

---

## 4. Research Gap Matrix

| # | Identified Scholarly Gap | How Prior Work Failed / Stopped Short | How CyberSaarthi Specifically Bridges This Gap |
|---|---|---|---|
| **G-1** | **Absence of open, class-wise baseline benchmarks for citizen incident descriptions** | Most NLP papers report only macro-accuracy or benchmark exclusively on formal English news/email corpora, ignoring short, informal, semi-structured incident narratives. | **CyberSaarthi Track A:** Provides an open, reproducible comparative evaluation of MNB, Logistic Regression, and Linear SVM, reporting per-class Precision, Recall, Macro-F1, confusion matrices, and latency on citizen-style incident descriptions. |
| **G-2** | **Lack of integrated educational intervention with structured reporting support** | Awareness platforms do not assist in incident capture; reporting portals do not provide contextualized preventive education or quiz-based knowledge reinforcement. | **CyberSaarthi Architecture:** Combines topic-specific educational modules, knowledge assessment quizzes (Track B pre/post test), and structured incident documentation in a unified portal. |
| **G-3** | **Unsafe automated classification without calibrated uncertainty handling** | Previous machine-learning prototypes treated model outputs as verified facts without routing uncertain, out-of-distribution, or low-confidence samples to manual review. | **CyberSaarthi Advisory Engine:** Enforces explicit confidence thresholds, displays uncertainty transparently, routes low-confidence items to `UNDER_REVIEW`, and allows administrative override. |
| **G-4** | **Unclear jurisdictional & institutional boundaries in student prototypes** | Many academic prototypes simulate "police response" or promise "fraud resolution", creating false user expectations and legal ambiguities. | **CyberSaarthi Boundary Enforcement:** Clear academic prototype branding, explicit persistent disclaimers, and direct official redirection to India's **1930 helpline** and **cybercrime.gov.in**. |

---

## 5. Formal Research Questions (Refined)

* **RQ-A1:** How do Multinomial Naive Bayes, Logistic Regression, and Linear Support Vector Machines compare in terms of Macro-F1, per-class Recall, and inference latency when classifying short cyber incident descriptions across CyberSaarthi's 8-class taxonomy?
* **RQ-A2:** Which incident categories exhibit the highest cross-class confusion, and does class imbalance disproportionately degrade minority classes (such as Identity Theft or Malware)?
* **RQ-A3:** Does a confidence-threshold routing policy successfully isolate low-confidence and ambiguous reports for administrative review without overwhelming reviewer capacity?
* **RQ-B1:** Is there a statistically measurable change in participant cyber-safety knowledge scores between pre-test and post-test assessments following engagement with CyberSaarthi's awareness modules?

