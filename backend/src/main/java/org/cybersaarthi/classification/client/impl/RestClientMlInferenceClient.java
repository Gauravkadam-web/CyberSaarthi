package org.cybersaarthi.classification.client.impl;

import org.cybersaarthi.classification.client.MlInferenceClient;
import org.cybersaarthi.classification.dto.MlClassifyRequest;
import org.cybersaarthi.classification.dto.MlClassifyResponse;
import org.cybersaarthi.classification.dto.MlInferenceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;

@Component
public class RestClientMlInferenceClient implements MlInferenceClient {

    private static final Logger log = LoggerFactory.getLogger(RestClientMlInferenceClient.class);

    private final RestClient mlRestClient;
    private final boolean mlEnabled;
    private final double threshold;

    public RestClientMlInferenceClient(
            RestClient mlRestClient,
            @Value("${cybersaarthi.ml.enabled:true}") boolean mlEnabled,
            @Value("${cybersaarthi.ml.threshold:0.60}") double threshold) {
        this.mlRestClient = mlRestClient;
        this.mlEnabled = mlEnabled;
        this.threshold = threshold;
    }

    @Override
    public MlInferenceResult classify(String text, String taxonomyVersion) {
        if (!mlEnabled) {
            log.info("ML inference client is disabled via configuration; routing to fallback pending.");
            return MlInferenceResult.fallbackPending("ML service is disabled in configuration");
        }

        try {
            MlClassifyRequest requestPayload = new MlClassifyRequest(text, taxonomyVersion);

            MlClassifyResponse response = mlRestClient.post()
                .uri("/v1/classify")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .body(MlClassifyResponse.class);

            if (response == null || response.predictedCategory() == null) {
                log.warn("FastAPI ML service returned an empty prediction response.");
                return MlInferenceResult.failed("Empty prediction payload received from ML service");
            }

            double confidence = response.confidenceScore() != null ? response.confidenceScore() : 0.0;
            boolean manualReviewRequired = Boolean.TRUE.equals(response.requiresManualReview()) || confidence < threshold;

            if (manualReviewRequired) {
                log.info("Prediction for category '{}' flagged for manual review (score: {})",
                    response.predictedCategory(), confidence);
                return MlInferenceResult.lowConfidence(response);
            }

            return MlInferenceResult.success(response);

        } catch (ResourceAccessException rae) {
            if (rae.getCause() instanceof SocketTimeoutException) {
                log.warn("ML inference service request timed out: {}", rae.getMessage());
                return MlInferenceResult.timeout("Inference request timed out: " + rae.getMessage());
            }
            log.warn("ML inference service unreachable (connection refused/offline): {}", rae.getMessage());
            return MlInferenceResult.serviceUnavailable("ML inference service is unreachable: " + rae.getMessage());

        } catch (RestClientResponseException rcre) {
            int statusCode = rcre.getStatusCode().value();
            log.warn("ML inference service returned HTTP error status {}: {}", statusCode, rcre.getResponseBodyAsString());
            if (statusCode == 503) {
                return MlInferenceResult.serviceUnavailable("ML inference service returned 503 Unavailable: " + rcre.getStatusText());
            }
            return MlInferenceResult.failed("ML inference service error [" + statusCode + "]: " + rcre.getStatusText());

        } catch (Exception ex) {
            log.error("Unexpected error during ML inference communication: {}", ex.getMessage(), ex);
            return MlInferenceResult.failed("Unexpected error: " + ex.getMessage());
        }
    }
}
