"""
CyberSaarthi — Request & Response Pydantic Schemas
Phase 4
"""

from typing import List, Optional
from pydantic import BaseModel, Field, field_validator


class InferenceRequest(BaseModel):
    """Payload submitted to /v1/classify."""
    text: str = Field(
        ...,
        min_length=10,
        max_length=3000,
        description="Incident narrative text describing the suspected cyber threat or crime",
        examples=["My Instagram account was hacked yesterday and someone changed my password."]
    )
    taxonomy_version: Optional[str] = Field(
        default="1.0",
        description="Target taxonomy version",
        examples=["1.0"]
    )

    @field_validator("text")
    @classmethod
    def validate_non_empty(cls, v: str) -> str:
        stripped = v.strip()
        if len(stripped) < 10:
            raise ValueError("Incident narrative must contain at least 10 non-whitespace characters.")
        return stripped


class InferenceResponse(BaseModel):
    """Response returned by /v1/classify."""
    model_config = {"protected_namespaces": ()}

    predicted_category: str = Field(
        ...,
        description="Suggested incident category code from Taxonomy v1.0",
        examples=["SOCIAL_ACCOUNT_COMPROMISE"]
    )
    confidence_score: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Model posterior probability / confidence score",
        examples=[0.9993]
    )
    requires_manual_review: bool = Field(
        ...,
        description="Flag indicating if the prediction fell below threshold (0.60) or is ambiguous",
        examples=[False]
    )
    score_type: str = Field(
        default="predict_proba",
        description="Mathematical interpretation of the confidence score",
        examples=["predict_proba"]
    )
    model_version: str = Field(
        default="baseline-v1.0",
        description="Version identifier of the active model artifact",
        examples=["baseline-v1.0"]
    )
    taxonomy_version: str = Field(
        default="1.0",
        description="Version identifier of the incident taxonomy",
        examples=["1.0"]
    )
    is_advisory: bool = Field(
        default=True,
        description="Enforces that prediction is an advisory suggestion, not a verified legal conclusion"
    )
    disclaimer: str = Field(
        ...,
        description="Mandatory legal/operational boundary notice for client applications"
    )


class HealthResponse(BaseModel):
    """Response returned by /health."""
    model_config = {"protected_namespaces": ()}

    status: str = Field(..., examples=["HEALTHY", "DEGRADED"])
    service: str = Field(default="cybersaarthi-ml-inference")
    model_loaded: bool
    model_version: str
    taxonomy_version: str
    model_algorithm: str
    supported_categories_count: int
    uptime_seconds: float


class CategoryItem(BaseModel):
    code: str
    display_name: str
    description: Optional[str] = None


class TaxonomyResponse(BaseModel):
    """Response returned by /v1/taxonomy."""
    taxonomy_version: str = "1.0"
    categories: List[CategoryItem]
