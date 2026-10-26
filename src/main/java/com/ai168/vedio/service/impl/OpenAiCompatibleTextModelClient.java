package com.ai168.vedio.service.impl;

import com.ai168.vedio.service.TextModelClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OpenAiCompatibleTextModelClient implements TextModelClient {
    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final RestClient restClient;

    public OpenAiCompatibleTextModelClient(
            @Value("${vedio.text-model.base-url:}") String baseUrl,
            @Value("${vedio.text-model.name:}") String model,
            @Value("${vedio.text-model.api-key:}") String apiKey) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public String generateShotDrafts(String scriptContent) {
        if (baseUrl.isBlank() || model.isBlank() || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "文本模型未配置");
        }
        String instruction = "将剧本拆分为不超过30个分镜，只返回JSON数组。每项必须包含shotNumber（从1连续递增）、"
                + "visualDescription（画面描述）、videoPrompt（视频生成提示词）、durationSeconds（1到60的整数）。";
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", instruction),
                        Map.of("role", "user", "content", scriptContent)));
        try {
            JsonNode response = restClient.post()
                    .uri(baseUrl.replaceAll("/+$", "") + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode content = response == null ? null : response.path("choices").path(0).path("message").path("content");
            if (content == null || !content.isTextual()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "文本模型响应缺少内容");
            }
            return content.asText();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "文本模型请求失败");
        }
    }
}
