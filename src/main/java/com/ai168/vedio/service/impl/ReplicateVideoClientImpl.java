package com.ai168.vedio.service.impl;

import com.ai168.vedio.entity.Shot;
import com.ai168.vedio.service.ReplicateVideoClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ReplicateVideoClientImpl implements ReplicateVideoClient {
    private final String baseUrl;
    private final String model;
    private final String apiToken;
    private final RestClient restClient;

    public ReplicateVideoClientImpl(
            @Value("${vedio.video-model.base-url:https://api.replicate.com/v1}") String baseUrl,
            @Value("${vedio.video-model.model:bytedance/seedance-2.0}") String model,
            @Value("${vedio.video-model.api-token:}") String apiToken) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.model = model;
        this.apiToken = apiToken;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public String submit(Shot shot, byte[] image, String mimeType) {
        requireConfiguration();
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", shot.getVideoPrompt());
        input.put("duration", shot.getDurationSeconds());
        input.put("resolution", "480p");
        if (image != null) {
            input.put("image", "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(image));
        }
        try {
            JsonNode response = restClient.post()
                    .uri(baseUrl + "/models/" + model + "/predictions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("input", input))
                    .retrieve()
                    .body(JsonNode.class);
            String id = response == null ? "" : response.path("id").asText("");
            if (!id.matches("[a-zA-Z0-9]+")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Replicate 未返回有效任务标识");
            }
            return id;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Replicate 创建任务失败");
        }
    }

    @Override
    public Prediction getPrediction(String predictionId) {
        requireConfiguration();
        if (!predictionId.matches("[a-zA-Z0-9]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "无效任务标识");
        }
        try {
            JsonNode response = restClient.get()
                    .uri(baseUrl + "/predictions/" + predictionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Replicate 响应为空");
            }
            String status = response.path("status").asText("");
            String outputUrl = response.path("output").asText(null);
            return new Prediction(status, outputUrl);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "查询 Replicate 任务失败");
        }
    }

    private void requireConfiguration() {
        if (baseUrl.isBlank() || model.isBlank() || apiToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Replicate 未配置");
        }
    }
}
