# CyberSaarthi — ML Inference Service
## FastAPI Microservice for Advisory Cybercrime Text Categorization
**Version:** 1.0.0 | **Port:** 8000 | **Host:** `127.0.0.1`

---

## 1. Overview

The CyberSaarthi ML Inference Service is a high-performance Python FastAPI microservice that encapsulates the Phase 3 trained baseline classifier (`baseline_model.joblib`) and fitted TF-IDF feature extractor (`tfidf_vectorizer.joblib`). 

### Core Capabilities:
* **Lifespan In-Memory Model Serving:** Loads model artifacts once into memory during boot, delivering median inference latency under **0.5 ms**.
* **Advisory Confidence Routing:** Automatically routes predictions with probability $< 0.60$ to `OTHER_UNCLASSIFIED` with `requires_manual_review = true`.
* **Zero-PII Logging:** Server telemetry records solely HTTP methods, response codes, and execution latencies—**never** the incident narrative text or user identifiers.
* **CORS Restraint:** Restricted strictly to localhost and the future Spring Boot core backend (`http://localhost:8080`).

---

## 2. Quick Start & Execution

### Starting the Service:
```bash
# Option 1: Via batch script (Windows)
scripts\run_ml_service.bat

# Option 2: Via PowerShell
.\scripts\run_ml_service.ps1

# Option 3: Direct command
ml\.venv\Scripts\python.exe -m uvicorn ml.inference.main:app --host 127.0.0.1 --port 8000 --reload
```

Once started:
* **Interactive Swagger UI:** [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
* **ReDoc Documentation:** [http://127.0.0.1:8000/redoc](http://127.0.0.1:8000/redoc)

---

## 3. API Reference

### 3.1 `GET /health` — Readiness Probe
Returns service health, model algorithm, taxonomy version, and uptime.

**Example cURL:**
```bash
curl -X GET "http://127.0.0.1:8000/health"
```

**Response (`200 OK`):**
```json
{
  "status": "HEALTHY",
  "service": "cybersaarthi-ml-inference",
  "model_loaded": true,
  "model_version": "baseline-v1.0",
  "taxonomy_version": "1.0",
  "model_algorithm": "Logistic Regression",
  "supported_categories_count": 8,
  "uptime_seconds": 12.45
}
```

---

### 3.2 `POST /v1/classify` — Advisory Classification
Categorizes citizen incident narratives across the 8 project categories.

**Example cURL:**
```bash
curl -X POST "http://127.0.0.1:8000/v1/classify" \
     -H "Content-Type: application/json" \
     -d '{
       "text": "My Instagram account was hacked yesterday and someone changed my password.",
       "taxonomy_version": "1.0"
     }'
```

**Response (`200 OK`):**
```json
{
  "predicted_category": "SOCIAL_ACCOUNT_COMPROMISE",
  "confidence_score": 0.9993,
  "requires_manual_review": false,
  "score_type": "predict_proba",
  "model_version": "baseline-v1.0",
  "taxonomy_version": "1.0",
  "is_advisory": true,
  "disclaimer": "Advisory ML category suggestion for guidance only. Does not constitute a verified legal conclusion or official accusation."
}
```

---

### 3.3 `GET /v1/taxonomy` — Supported Categories
Returns all 8 category codes, display names, and definitions.

**Example cURL:**
```bash
curl -X GET "http://127.0.0.1:8000/v1/taxonomy"
```

---

## 4. Automated Testing

Run the automated test suite with coverage:
```bash
ml\.venv\Scripts\python.exe -m pytest ml/tests -v
```
All 12 test cases validate input constraints, edge cases, error codes, and confidence threshold routing.
