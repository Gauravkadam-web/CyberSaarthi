package org.cybersaarthi.classification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MlClassifyRequest(
    String text,
    @JsonProperty("taxonomy_version") String taxonomyVersion
) {}
