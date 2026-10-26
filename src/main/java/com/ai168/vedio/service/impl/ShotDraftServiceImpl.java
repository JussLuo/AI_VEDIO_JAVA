package com.ai168.vedio.service.impl;

import com.ai168.vedio.dto.ShotDraft;
import com.ai168.vedio.entity.Script;
import com.ai168.vedio.mapper.ScriptMapper;
import com.ai168.vedio.service.ShotDraftService;
import com.ai168.vedio.service.TextModelClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShotDraftServiceImpl implements ShotDraftService {
    private final ScriptMapper scriptMapper;
    private final TextModelClient textModelClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShotDraftServiceImpl(ScriptMapper scriptMapper, TextModelClient textModelClient) {
        this.scriptMapper = scriptMapper;
        this.textModelClient = textModelClient;
    }

    @Override
    public List<ShotDraft> generateDrafts(String scriptId) {
        Script script = scriptMapper.selectById(scriptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "剧本不存在"));
        if (script.getContent() == null || script.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "剧本内容为空");
        }
        try {
            JsonNode root = objectMapper.readTree(textModelClient.generateShotDrafts(script.getContent()));
            if (!root.isArray() || root.isEmpty() || root.size() > 30) {
                throw invalidResponse();
            }
            List<ShotDraft> drafts = new ArrayList<>();
            for (JsonNode item : root) {
                int number = item.path("shotNumber").asInt(-1);
                int duration = item.path("durationSeconds").asInt(-1);
                String description = item.path("visualDescription").asText("").trim();
                String prompt = item.path("videoPrompt").asText("").trim();
                if (number != drafts.size() + 1 || duration < 1 || duration > 60
                        || description.isEmpty() || prompt.isEmpty()) {
                    throw invalidResponse();
                }
                drafts.add(new ShotDraft(number, description, prompt, duration));
            }
            return drafts;
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw invalidResponse();
        }
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "文本模型返回的分镜格式无效");
    }
}
