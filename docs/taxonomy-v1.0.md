# CyberSaarthi Incident Taxonomy v1.0
## Versioned Taxonomy Specification for Text Classification & Incident Documentation
**Document Version:** 1.0 | **Taxonomy Version:** `1.0`  
**Status:** Frozen Baseline for Dataset Mapping & Model Experiments

---

## 1. Principles of Taxonomy Design

1. **Academic Alignment:** Aligned with contemporary Indian and international cyber threat categories while remaining sufficiently granular for a 2-member MCA project.
2. **Mutual Exclusivity:** Clear inclusion and exclusion boundaries to minimize cross-class labeling ambiguity.
3. **Immutability:** Taxonomy version `1.0` is immutable. If categories are merged or added later, they must be released as `2.0` with versioned migration scripts.
4. **Advisory Semantics:** Model classifications mapped to these categories are suggestions, with `OTHER_UNCLASSIFIED` reserved for low-confidence or ambiguous instances.

---

## 2. Category Specifications

### Category 1: Financial and UPI Fraud
* **Code:** `FINANCIAL_UPI_FRAUD`
* **Display Name:** Financial and UPI Fraud
* **Scope & Definition:** Unauthorized financial transactions, deceptive payment requests, fake UPI QR codes, fraudulent payment gateway links, SIM swap leading to bank account drain, debit/credit card skimming, or lottery fraud involving upfront monetary transfers.
* **Typical Keywords / Tokens:** UPI, GPay, PhonePe, Paytm, QR code, bank debit, OTP shared, credit card charge, unauthorized transfer, refund scam, electricity bill scam.
* **Exclusions:** Investment or fake job schemes where money is paid willingly as a "task deposit" (categorized under `JOB_INVESTMENT_SCAM`).
* **Synthetic Example:** *"I received a call claiming my electricity connection would be cut off unless I paid Rs. 15. The caller sent a QR code. When I scanned it on PhonePe and entered my PIN, Rs. 25,000 was debited from my bank account."*

---

### Category 2: Phishing and Social Engineering
* **Code:** `PHISHING_SOCIAL_ENGINEERING`
* **Display Name:** Phishing and Social Engineering
* **Scope & Definition:** Deceptive communications (emails, SMS/smishing, phone calls/vishing, WhatsApp messages) impersonating legitimate organizations (banks, government agencies, courier services, Netflix, tech support) designed to harvest credentials, confidential personal data, or banking details before an actual financial theft is executed.
* **Typical Keywords / Tokens:** Phishing email, fake link, verify account, courier delivery failed, update KYC, fake website, impersonation, login alert, urgent action required.
* **Exclusions:** Scams where an immediate unauthorized UPI debit was completed (categorized under `FINANCIAL_UPI_FRAUD`).
* **Synthetic Example:** *"I got an SMS saying my SpeedPost parcel could not be delivered due to an incorrect address. It contained a link to a fake India Post website asking me to input my credit card and Aadhaar details to pay a re-delivery fee."*

---

### Category 3: Identity Theft
* **Code:** `IDENTITY_THEFT`
* **Display Name:** Identity Theft
* **Scope & Definition:** Unauthorized misuse or impersonation of an individual's personal identity, government documents (Aadhaar, PAN card, passport, driver's license), or biometric records to open fraudulent bank accounts, secure unauthorized loans, purchase forged SIM cards, or register illegal services in the victim's name.
* **Typical Keywords / Tokens:** PAN misused, fake loan taken, Aadhaar forged, identity impersonation, multiple SIMs registered on ID, CIBIL score dropped, forged documents.
* **Exclusions:** Impersonation restricted solely to social media profiles (categorized under `SOCIAL_ACCOUNT_COMPROMISE`).
* **Synthetic Example:** *"When I checked my CIBIL credit report, I discovered an active personal loan of Rs. 1,50,000 taken from an NBFC that I never applied for. Someone forged my PAN card and Aadhaar to take this loan."*

---

### Category 4: Social Media and Account Compromise
* **Code:** `SOCIAL_ACCOUNT_COMPROMISE`
* **Display Name:** Social Media and Account Compromise
* **Scope & Definition:** Unauthorized access, takeover, hacking, or duplicate cloning of user accounts across social networking platforms, email providers, or messaging services (Instagram, Facebook, WhatsApp, Gmail, Twitter/X, Telegram). Often accompanied by fraudulent requests for money sent to the victim's contact list.
* **Typical Keywords / Tokens:** Instagram hacked, password changed, 2FA bypass, locked out of Facebook, profile clone, WhatsApp account hijacked, scam messages sent to friends.
* **Exclusions:** Systematic targeted online harassment, non-consensual image sharing, or stalking (categorized under `CYBERSTALKING_HARASSMENT`).
* **Synthetic Example:** *"My Instagram account was hacked yesterday. The attacker changed my registered email and two-factor authentication, and is now sending direct messages to my followers asking them to send money via UPI urgently."*

---

### Category 5: Cyberstalking and Online Harassment
* **Code:** `CYBERSTALKING_HARASSMENT`
* **Display Name:** Cyberstalking and Online Harassment
* **Scope & Definition:** Persistent, unwanted, threatening, intimidating, or abusive digital conduct targeted at an individual. Includes online stalking, cyberbullying, doxxing (publishing private personal data), non-consensual sharing of private or manipulated images (deepfakes, revenge porn), blackmail, and continuous extortion threats.
* **Typical Keywords / Tokens:** Stalking, harassment, abusive messages, blackmail, morphing photos, threatening calls, doxxing, non-consensual image, online defamation.
* **Exclusions:** Financial extortion where malware locks system data (categorized under `MALWARE_RANSOMWARE`).
* **Synthetic Example:** *"An anonymous account on Telegram has been sending me abusive and threatening messages continuously. They morphed my personal photos and are threatening to share them with my family unless I pay them money."*

---

### Category 6: Job and Investment Scams
* **Code:** `JOB_INVESTMENT_SCAM`
* **Display Name:** Job and Investment Scams
* **Scope & Definition:** Fraudulent employment offers, fake work-from-home schemes, "YouTube like/Telegram task" scams, fraudulent crypto-trading platforms, guaranteed high-return ponzi schemes, or fake overseas visa offers where victims willingly transfer funds as "registration deposits", "task commissions", or "initial investments".
* **Typical Keywords / Tokens:** Part-time job, Telegram task, YouTube like scam, guaranteed returns, crypto trading platform, work from home deposit, fake interview, investment scheme.
* **Exclusions:** Unsolicited phishing emails where no investment/task engagement took place (categorized under `PHISHING_SOCIAL_ENGINEERING`).
* **Synthetic Example:** *"I was contacted on WhatsApp for a part-time job involving rating hotels and liking videos. Initially, they credited Rs. 500 to my account, but then asked me to deposit Rs. 50,000 for 'premium tasks'. After I transferred the money, they blocked me."*

---

### Category 7: Malware and Ransomware
* **Code:** `MALWARE_RANSOMWARE`
* **Display Name:** Malware and Ransomware
* **Scope & Definition:** Malicious software infections affecting personal computers, smartphones, or network infrastructure. Includes ransomware (files encrypted with a ransom demand), spyware/keyloggers, trojans, remote access tool (RAT) exploitation (e.g., AnyDesk/TeamViewer abuse), and botnet infections.
* **Typical Keywords / Tokens:** Ransomware, files encrypted, .locked extension, virus, AnyDesk access, keylogger, phone hacked, popups, remote control app.
* **Exclusions:** Standard phishing emails without executable payload downloads (categorized under `PHISHING_SOCIAL_ENGINEERING`).
* **Synthetic Example:** *"My laptop screen turned black and all my documents and project files now have an extension called '.locked'. A notepad file demands 0.1 Bitcoin to provide the decryption key."*

---

### Category 8: Other or Unclassified
* **Code:** `OTHER_UNCLASSIFIED`
* **Display Name:** Other or Unclassified
* **Scope & Definition:** Incidents that do not fit into the seven primary categories, multi-vector incidents that span multiple categories ambiguously, or descriptions with insufficient or uninterpretable information. Also serves as the system default fallback when ML model confidence is below the defined threshold ($< 0.60$).
* **Typical Keywords / Tokens:** Unspecified suspicious activity, general internet query, novel threat pattern, ambiguous text.
* **Exclusions:** Specific cyber threats clearly covered by Categories 1–7.
* **Synthetic Example:** *"Something suspicious happened with my router settings and internet speed is very slow. I am not sure if someone is tapping my connection."*

---

## 3. Database Seed Mapping Format

```json
[
  {"taxonomy_version": "1.0", "code": "FINANCIAL_UPI_FRAUD", "display_name": "Financial and UPI Fraud", "is_active": true},
  {"taxonomy_version": "1.0", "code": "PHISHING_SOCIAL_ENGINEERING", "display_name": "Phishing and Social Engineering", "is_active": true},
  {"taxonomy_version": "1.0", "code": "IDENTITY_THEFT", "display_name": "Identity Theft", "is_active": true},
  {"taxonomy_version": "1.0", "code": "SOCIAL_ACCOUNT_COMPROMISE", "display_name": "Social Media and Account Compromise", "is_active": true},
  {"taxonomy_version": "1.0", "code": "CYBERSTALKING_HARASSMENT", "display_name": "Cyberstalking and Online Harassment", "is_active": true},
  {"taxonomy_version": "1.0", "code": "JOB_INVESTMENT_SCAM", "display_name": "Job and Investment Scams", "is_active": true},
  {"taxonomy_version": "1.0", "code": "MALWARE_RANSOMWARE", "display_name": "Malware and Ransomware", "is_active": true},
  {"taxonomy_version": "1.0", "code": "OTHER_UNCLASSIFIED", "display_name": "Other or Unclassified", "is_active": true}
]
```

