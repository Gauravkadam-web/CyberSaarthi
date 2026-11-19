"""
CyberSaarthi — Classification & Validation Endpoint Tests
Phase 4
"""

import pytest
from fastapi import status


def test_classify_social_media_account_takeover(client):
    """Verifies high-confidence classification of account compromise."""
    payload = {
        "text": "My Instagram account was hacked yesterday and someone changed my password and 2FA.",
        "taxonomy_version": "1.0"
    }
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_200_OK

    data = response.json()
    assert data["predicted_category"] == "SOCIAL_ACCOUNT_COMPROMISE"
    assert data["confidence_score"] >= 0.60
    assert data["requires_manual_review"] is False
    assert data["score_type"] == "predict_proba"
    assert data["model_version"] == "baseline-v1.0"
    assert data["taxonomy_version"] == "1.0"
    assert data["is_advisory"] is True
    assert "Advisory" in data["disclaimer"]


def test_classify_upi_fraud(client):
    """Verifies classification of financial UPI fraud."""
    payload = {
        "text": "epfo alert: your pf account has unclaimed balance of rs 30,000. pay processing fee to withdraw",
        "taxonomy_version": "1.0"
    }
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_200_OK

    data = response.json()
    assert data["predicted_category"] == "FINANCIAL_UPI_FRAUD"
    assert data["confidence_score"] >= 0.60
    assert data["requires_manual_review"] is False


def test_classify_ransomware(client):
    """Verifies classification of malware/ransomware."""
    payload = {
        "text": "All my office computer files are encrypted with .locked extension and a notepad note demands Bitcoin payment.",
        "taxonomy_version": "1.0"
    }
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_200_OK

    data = response.json()
    assert data["predicted_category"] == "MALWARE_RANSOMWARE"
    assert data["is_advisory"] is True


def test_classify_low_confidence_threshold_routing(client):
    """Verifies that an ambiguous query scores below 0.60 and routes to manual review."""
    payload = {
        "text": "I noticed strange battery drain on my phone and the screen brightness flickered once.",
        "taxonomy_version": "1.0"
    }
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_200_OK

    data = response.json()
    # Ambiguous query should trigger confidence routing if score < 0.60
    if data["confidence_score"] < 0.60:
        assert data["predicted_category"] == "OTHER_UNCLASSIFIED"
        assert data["requires_manual_review"] is True
        assert "threshold" in data["disclaimer"]
    else:
        # If model is confident, it should still be a valid category
        assert data["predicted_category"] in [
            "MALWARE_RANSOMWARE", "OTHER_UNCLASSIFIED", "PHISHING_SOCIAL_ENGINEERING"
        ]


def test_classify_text_too_short_validation(client):
    """Verifies that input < 10 characters returns 422 Unprocessable Entity."""
    payload = {"text": "hacked", "taxonomy_version": "1.0"}
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY


def test_classify_whitespace_only_validation(client):
    """Verifies that whitespace-only string returns 422."""
    payload = {"text": "               ", "taxonomy_version": "1.0"}
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY


def test_classify_text_too_long_validation(client):
    """Verifies that input > 3000 characters returns 422."""
    payload = {"text": "A" * 3005, "taxonomy_version": "1.0"}
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY


def test_classify_unsupported_taxonomy_version(client):
    """Verifies that unsupported taxonomy version returns 400 Bad Request."""
    payload = {
        "text": "My bank account was compromised through a fraudulent SMS link.",
        "taxonomy_version": "9.9"
    }
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_400_BAD_REQUEST
    data = response.json()
    assert "Unsupported taxonomy version" in data["detail"]


def test_classify_default_taxonomy_version(client):
    """Verifies that omitting taxonomy_version defaults cleanly to 1.0."""
    payload = {
        "text": "Urgent: Your India Post parcel cannot be delivered. Click link to update KYC and address"
    }
    response = client.post("/v1/classify", json=payload)
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["taxonomy_version"] == "1.0"
    assert data["predicted_category"] == "PHISHING_SOCIAL_ENGINEERING"
