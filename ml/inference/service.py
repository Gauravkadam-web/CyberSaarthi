"""
CyberSaarthi — Classifier Service Singleton
Phase 4
"""

import re
import json
import logging
from pathlib import Path
from typing import Dict, Any, List

import joblib
import numpy as np

from ml.inference.config import settings

logger = logging.getLogger("cybersaarthi.ml")

TAXONOMY_V1_CATEGORIES: List[Dict[str, str]] = [
    {
        "code": "FINANCIAL_UPI_FRAUD",
        "display_name": "Financial and UPI Fraud",
        "description": "Unauthorized financial debits, fake QR codes, payment gateway fraud, and SIM swaps."
    },
    {
        "code": "PHISHING_SOCIAL_ENGINEERING",
        "display_name": "Phishing and Social Engineering",
        "description": "Deceptive links, KYC update warnings, fake CBI/police notices, and courier scams."
    },
    {
        "code": "IDENTITY_THEFT",
        "display_name": "Identity Theft",
        "description": "Misuse of Aadhaar, PAN, or passport for fraudulent loans or SIM activations."
    },
    {
        "code": "SOCIAL_ACCOUNT_COMPROMISE",
        "display_name": "Social Media and Account Compromise",
        "description": "Takeover of Instagram, WhatsApp, Facebook, or email via password or 2FA bypass."
    },
    {
        "code": "CYBERSTALKING_HARASSMENT",
        "display_name": "Cyberstalking and Online Harassment",
        "description": "Digital extortion, sextortion blackmail, abusive conduct, and morphing threats."
    },
    {
        "code": "JOB_INVESTMENT_SCAM",
        "display_name": "Job and Investment Scams",
        "description": "Telegram task schemes, YouTube like scams, fake work-from-home, and crypto trading."
    },
    {
        "code": "MALWARE_RANSOMWARE",
        "display_name": "Malware and Ransomware",
        "description": "Ransomware encryption (.locked), spyware, malicious APKs, and remote access trojans."
    },
    {
        "code": "OTHER_UNCLASSIFIED",
        "display_name": "Other or Unclassified",
        "description": "Ambiguous, multi-vector incidents, or predictions below the 0.60 confidence threshold."
    }
]


class ClassifierService:
    """Singleton service managing in-memory baseline model and TF-IDF vectorizer."""

    def __init__(self):
        self.model = None
        self.vectorizer = None
        self.metadata = {}
        self.algorithm_name = "Unknown"
        self._is_loaded = False

    def load_artifacts(self) -> bool:
        """Loads model and vectorizer into memory from artifacts directory."""
        model_path = settings.MODEL_ARTIFACT_PATH
        vectorizer_path = settings.VECTORIZER_ARTIFACT_PATH
        metadata_path = settings.METADATA_PATH

        if not model_path.exists() or not vectorizer_path.exists():
            logger.error(f"Model artifacts missing! Expected at {model_path} and {vectorizer_path}")
            self._is_loaded = False
            return False

        try:
            logger.info(f"Loading TF-IDF vectorizer from {vectorizer_path}...")
            self.vectorizer = joblib.load(vectorizer_path)

            logger.info(f"Loading ML model from {model_path}...")
            self.model = joblib.load(model_path)

            if metadata_path.exists():
                with open(metadata_path, "r", encoding="utf-8") as f:
                    self.metadata = json.load(f)
                self.algorithm_name = self.metadata.get("winning_model_name", "Logistic Regression")
            else:
                self.algorithm_name = type(self.model).__name__

            self._is_loaded = True
            logger.info(f"Successfully loaded {self.algorithm_name} artifact (Taxonomy v{settings.TAXONOMY_VERSION}).")
            return True

        except Exception as e:
            logger.error(f"Error loading model artifacts: {e}", exc_info=True)
            self._is_loaded = False
            return False

    @property
    def is_ready(self) -> bool:
        return self._is_loaded and self.model is not None and self.vectorizer is not None

    def clean_text(self, text: str) -> str:
        """Applies consistent text normalization matching training preprocessing."""
        text = str(text).strip()
        text = re.sub(r"https?://\S+|www\.\S+", " url_link ", text)
        text = re.sub(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b", " email_address ", text)
        text = re.sub(r"\b[6-9]\d{9}\b", " phone_number ", text)
        text = re.sub(r"\s+", " ", text).strip()
        return text.lower()

    def predict(self, text: str, taxonomy_version: str = "1.0") -> Dict[str, Any]:
        """Classifies narrative text and applies confidence threshold routing."""
        if not self.is_ready:
            raise RuntimeError("Classifier service is not ready. Artifacts are not loaded.")

        if taxonomy_version != settings.TAXONOMY_VERSION:
            raise ValueError(
                f"Unsupported taxonomy version '{taxonomy_version}'. Active version is '{settings.TAXONOMY_VERSION}'."
            )

        cleaned = self.clean_text(text)
        X = self.vectorizer.transform([cleaned])

        probabilities = self.model.predict_proba(X)[0]
        classes = self.model.classes_

        best_idx = int(np.argmax(probabilities))
        raw_category = str(classes[best_idx])
        confidence_score = float(probabilities[best_idx])

        # Enforce 0.60 Confidence Threshold Routing (RQ-A3)
        if confidence_score < settings.CONFIDENCE_THRESHOLD:
            predicted_category = "OTHER_UNCLASSIFIED"
            requires_manual_review = True
            disclaimer = (
                f"Confidence score ({confidence_score:.2f}) is below the {settings.CONFIDENCE_THRESHOLD:.2f} threshold. "
                "Advisory classification assigned to OTHER_UNCLASSIFIED and routed to manual review."
            )
        else:
            predicted_category = raw_category
            requires_manual_review = False
            disclaimer = (
                "Advisory ML category suggestion for guidance only. "
                "Does not constitute a verified legal conclusion or official accusation."
            )

        return {
            "predicted_category": predicted_category,
            "confidence_score": round(confidence_score, 4),
            "requires_manual_review": requires_manual_review,
            "score_type": "predict_proba",
            "model_version": settings.MODEL_VERSION,
            "taxonomy_version": settings.TAXONOMY_VERSION,
            "is_advisory": True,
            "disclaimer": disclaimer,
        }

    def get_taxonomy(self) -> Dict[str, Any]:
        """Returns the official supported Taxonomy v1.0 specifications."""
        return {
            "taxonomy_version": settings.TAXONOMY_VERSION,
            "categories": TAXONOMY_V1_CATEGORIES,
        }


# Singleton service instance
classifier_service = ClassifierService()
