"""
CyberSaarthi — Inference Service Configuration
Phase 4
"""

import os
from pathlib import Path
from dataclasses import dataclass, field

# Base repository and artifact directories
BASE_DIR = Path(__file__).resolve().parent.parent
ARTIFACTS_DIR = BASE_DIR / "training" / "artifacts"


@dataclass
class Settings:
    # Service Information
    APP_NAME: str = "CyberSaarthi ML Inference Service"
    SERVICE_VERSION: str = "1.0.0"
    ENVIRONMENT: str = os.getenv("ENVIRONMENT", "development")
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")

    # Networking
    HOST: str = os.getenv("HOST", "127.0.0.1")
    PORT: int = int(os.getenv("PORT", "8000"))

    # Security & CORS (Restricted to Spring Boot and local dev)
    CORS_ALLOWED_ORIGINS: list[str] = field(
        default_factory=lambda: [
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://localhost:3000",
        ]
    )

    # Model & Taxonomy Configuration
    TAXONOMY_VERSION: str = "1.0"
    MODEL_VERSION: str = "baseline-v1.0"
    CONFIDENCE_THRESHOLD: float = float(os.getenv("CONFIDENCE_THRESHOLD", "0.60"))

    # Artifact File Paths
    MODEL_ARTIFACT_PATH: Path = ARTIFACTS_DIR / "baseline_model.joblib"
    VECTORIZER_ARTIFACT_PATH: Path = ARTIFACTS_DIR / "tfidf_vectorizer.joblib"
    METADATA_PATH: Path = ARTIFACTS_DIR / "model_metadata.json"

    # Input Payload Validation Limits
    MIN_TEXT_LENGTH: int = 10
    MAX_TEXT_LENGTH: int = 3000


settings = Settings()
