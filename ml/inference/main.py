"""
CyberSaarthi — FastAPI ML Inference Service Application
Phase 4
"""

import time
import logging
from contextlib import asynccontextmanager
from typing import Dict, Any

from fastapi import FastAPI, HTTPException, Request, Response, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse

from ml.inference.config import settings
from ml.inference.schemas import (
    InferenceRequest,
    InferenceResponse,
    HealthResponse,
    TaxonomyResponse,
)
from ml.inference.service import classifier_service

# ---------------------------------------------------------------------------
# Logging Configuration (Strict Zero-PII Policy)
# ---------------------------------------------------------------------------
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO),
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("cybersaarthi.api")

start_time: float = 0.0


# ---------------------------------------------------------------------------
# Lifespan Management (Load Artifacts Once on Boot)
# ---------------------------------------------------------------------------
@asynccontextmanager
async def lifespan(app: FastAPI):
    global start_time
    start_time = time.time()
    logger.info("Initializing CyberSaarthi ML Inference Service...")

    # Load artifacts into memory
    loaded = classifier_service.load_artifacts()
    if not loaded:
        logger.warning(
            "Service started but ML artifacts could not be loaded. "
            "Endpoints will respond with 503 until artifacts are present."
        )
    else:
        logger.info(
            f"Service startup complete. Model '{classifier_service.algorithm_name}' "
            f"ready for inference on port {settings.PORT}."
        )

    yield

    logger.info("Shutting down CyberSaarthi ML Inference Service.")


# ---------------------------------------------------------------------------
# Application Initialization
# ---------------------------------------------------------------------------
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.SERVICE_VERSION,
    description=(
        "Lightweight machine learning inference microservice for CyberSaarthi. "
        "Provides advisory cybercrime text classification across the 8-class project taxonomy."
    ),
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# ---------------------------------------------------------------------------
# Security & CORS Middleware
# Restricted to Spring Boot backend and localhost testing
# ---------------------------------------------------------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)


# ---------------------------------------------------------------------------
# Zero-PII Request Telemetry Middleware
# Logs latency and HTTP codes only — never raw narrative bodies
# ---------------------------------------------------------------------------
@app.middleware("http")
async def log_requests_zero_pii(request: Request, call_next):
    start = time.perf_counter()
    response: Response = await call_next(request)
    duration_ms = (time.perf_counter() - start) * 1000

    # Privacy enforcement: logs contain ONLY method, path, status, and duration
    logger.info(
        f"{request.method} {request.url.path} -> {response.status_code} "
        f"({duration_ms:.2f} ms)"
    )
    return response


# ---------------------------------------------------------------------------
# Route Handlers
# ---------------------------------------------------------------------------
@app.get("/", include_in_schema=False)
async def root():
    """Redirects root URL to interactive Swagger documentation."""
    return RedirectResponse(url="/docs")


@app.get(
    "/health",
    response_model=HealthResponse,
    tags=["System"],
    summary="Health & Readiness Probe",
    description="Returns operational status, model readiness, and version metadata.",
)
async def health_check():
    uptime = time.time() - start_time if start_time > 0 else 0.0
    is_ready = classifier_service.is_ready

    if not is_ready:
        return HealthResponse(
            status="DEGRADED",
            service="cybersaarthi-ml-inference",
            model_loaded=False,
            model_version=settings.MODEL_VERSION,
            taxonomy_version=settings.TAXONOMY_VERSION,
            model_algorithm="None",
            supported_categories_count=0,
            uptime_seconds=round(uptime, 2),
        )

    return HealthResponse(
        status="HEALTHY",
        service="cybersaarthi-ml-inference",
        model_loaded=True,
        model_version=settings.MODEL_VERSION,
        taxonomy_version=settings.TAXONOMY_VERSION,
        model_algorithm=classifier_service.algorithm_name,
        supported_categories_count=len(classifier_service.get_taxonomy()["categories"]),
        uptime_seconds=round(uptime, 2),
    )


@app.get(
    "/v1/taxonomy",
    response_model=TaxonomyResponse,
    tags=["Taxonomy"],
    summary="List Supported Taxonomy Categories",
    description="Returns all 8 category codes, display names, and descriptions for Taxonomy v1.0.",
)
async def get_taxonomy():
    return classifier_service.get_taxonomy()


@app.post(
    "/v1/classify",
    response_model=InferenceResponse,
    tags=["Inference"],
    summary="Advisory Incident Classification",
    description=(
        "Analyzes an incident narrative and returns the suggested category code, "
        "posterior probability, and manual review routing status."
    ),
)
async def classify_incident(payload: InferenceRequest):
    if not classifier_service.is_ready:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="ML inference service is not ready. Model artifacts are uninitialized.",
        )

    try:
        result = classifier_service.predict(
            text=payload.text,
            taxonomy_version=payload.taxonomy_version or settings.TAXONOMY_VERSION,
        )
        return result
    except ValueError as ve:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(ve),
        )
    except Exception as e:
        logger.error(f"Inference error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An internal error occurred during model inference.",
        )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "ml.inference.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,
    )
