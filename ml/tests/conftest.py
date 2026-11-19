"""
CyberSaarthi — Pytest Fixtures & TestClient Setup
Phase 4
"""

import sys
from pathlib import Path
import pytest
from fastapi.testclient import TestClient

# Ensure root directory is on sys.path
REPO_ROOT = Path(__file__).resolve().parent.parent.parent
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from ml.inference.main import app
from ml.inference.service import classifier_service


@pytest.fixture(scope="session", autouse=True)
def initialize_service():
    """Initializes and loads ML model artifacts once for all test sessions."""
    loaded = classifier_service.load_artifacts()
    if not loaded:
        raise RuntimeError("Failed to load model artifacts for test suite. Ensure train.py ran successfully.")
    yield


@pytest.fixture(scope="module")
def client():
    """Provides a thread-safe HTTP TestClient for FastAPI endpoints."""
    with TestClient(app) as test_client:
        yield test_client
