"""
CyberSaarthi — Health & Taxonomy Endpoint Tests
Phase 4
"""

import pytest
from fastapi import status


def test_health_check_healthy(client):
    """Verifies that /health returns 200 OK and model is marked loaded."""
    response = client.get("/health")
    assert response.status_code == status.HTTP_200_OK

    data = response.json()
    assert data["status"] == "HEALTHY"
    assert data["service"] == "cybersaarthi-ml-inference"
    assert data["model_loaded"] is True
    assert data["model_version"] == "baseline-v1.0"
    assert data["taxonomy_version"] == "1.0"
    assert data["model_algorithm"] == "Logistic Regression"
    assert data["supported_categories_count"] == 8
    assert data["uptime_seconds"] >= 0.0


def test_taxonomy_endpoint(client):
    """Verifies that /v1/taxonomy returns all 8 Taxonomy v1.0 categories."""
    response = client.get("/v1/taxonomy")
    assert response.status_code == status.HTTP_200_OK

    data = response.json()
    assert data["taxonomy_version"] == "1.0"
    categories = data["categories"]
    assert len(categories) == 8

    category_codes = [c["code"] for c in categories]
    expected_codes = [
        "FINANCIAL_UPI_FRAUD",
        "PHISHING_SOCIAL_ENGINEERING",
        "IDENTITY_THEFT",
        "SOCIAL_ACCOUNT_COMPROMISE",
        "CYBERSTALKING_HARASSMENT",
        "JOB_INVESTMENT_SCAM",
        "MALWARE_RANSOMWARE",
        "OTHER_UNCLASSIFIED",
    ]
    for code in expected_codes:
        assert code in category_codes

    # Verify each category has code and display_name
    for cat in categories:
        assert len(cat["code"]) > 0
        assert len(cat["display_name"]) > 0
        assert len(cat["description"]) > 0


def test_root_redirect_to_docs(client):
    """Verifies root URL redirects to Swagger docs."""
    response = client.get("/", follow_redirects=False)
    assert response.status_code in [status.HTTP_307_TEMPORARY_REDIRECT, status.HTTP_302_FOUND]
    assert response.headers["location"] == "/docs"
