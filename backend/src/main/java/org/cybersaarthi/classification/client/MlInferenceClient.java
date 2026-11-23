package org.cybersaarthi.classification.client;

import org.cybersaarthi.classification.dto.MlInferenceResult;

public interface MlInferenceClient {

    MlInferenceResult classify(String text, String taxonomyVersion);
}
