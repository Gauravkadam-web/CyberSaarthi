-- V5: Create ML Prediction and Category Inference Tables
-- Phase 8: ML Inference Integration

CREATE TABLE ml_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES incident_reports(id) ON DELETE CASCADE,
    predicted_category_id UUID REFERENCES incident_categories(id) ON DELETE SET NULL,
    predicted_category_code VARCHAR(80) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(80) NOT NULL,
    taxonomy_version VARCHAR(30) NOT NULL,
    confidence_score NUMERIC(5, 4),
    score_type VARCHAR(50) NOT NULL,
    requires_manual_review BOOLEAN NOT NULL DEFAULT FALSE,
    inference_status VARCHAR(40) NOT NULL,
    inferred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message VARCHAR(500)
);

CREATE INDEX idx_ml_predictions_report_id ON ml_predictions(report_id, inferred_at DESC);
CREATE INDEX idx_ml_predictions_status ON ml_predictions(inference_status);
