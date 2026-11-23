package org.cybersaarthi.classification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class MlClientConfig {

    @Value("${cybersaarthi.ml.base-url:http://localhost:8000}")
    private String baseUrl;

    @Value("${cybersaarthi.ml.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${cybersaarthi.ml.read-timeout-ms:3000}")
    private int readTimeoutMs;

    @Bean
    public RestClient mlRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build();
    }
}
