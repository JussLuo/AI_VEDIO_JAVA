package com.ai168.vedio.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ai168.vedio.dto.CreateRenderRequest;
import com.ai168.vedio.entity.RenderTask;
import com.ai168.vedio.entity.Shot;
import com.ai168.vedio.entity.VideoTask;
import com.ai168.vedio.mapper.RenderTaskMapper;
import com.ai168.vedio.mapper.ScriptMapper;
import com.ai168.vedio.mapper.ShotMapper;
import com.ai168.vedio.mapper.VideoTaskMapper;
import com.ai168.vedio.service.FfmpegRenderer;
import com.ai168.vedio.service.RenderFileStorage;
import com.ai168.vedio.service.RenderTaskService;
import com.ai168.vedio.service.VideoFileStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RenderTaskServiceImpl implements RenderTaskService {
    private final ScriptMapper scriptMapper;
    private final ShotMapper shotMapper;
    private final VideoTaskMapper videoTaskMapper;
    private final RenderTaskMapper renderTaskMapper;
    private final VideoFileStorage videoStorage;
    private final RenderFileStorage renderStorage;
    private final FfmpegRenderer renderer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RenderTaskServiceImpl(
            ScriptMapper scriptMapper,
            ShotMapper shotMapper,
            VideoTaskMapper videoTaskMapper,
            RenderTaskMapper renderTaskMapper,
            VideoFileStorage videoStorage,
            RenderFileStorage renderStorage,
            FfmpegRenderer renderer) {
        this.scriptMapper = scriptMapper;
        this.shotMapper = shotMapper;
        this.videoTaskMapper = videoTaskMapper;
        this.renderTaskMapper = renderTaskMapper;
        this.videoStorage = videoStorage;
        this.renderStorage = renderStorage;
        this.renderer = renderer;
    }

    @Override
    public RenderTask createRender(String scriptId, CreateRenderRequest request) {
        requireScript(scriptId);
        List<Shot> shots = shotMapper.selectByScriptId(scriptId);
        if (request == null || request.videoTaskIds() == null
                || shots.isEmpty() || shots.size() > 30
                || request.videoTaskIds().size() != shots.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "须为每个分镜选择一个已完成片段");
        }
        for (int index = 0; index < shots.size(); index++) {
            String videoTaskId = request.videoTaskIds().get(index);
            VideoTask videoTask = videoTaskMapper.selectById(videoTaskId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "视频任务不存在"));
            if (!shots.get(index).getId().equals(videoTask.getShotId())
                    || !"SUCCEEDED".equals(videoTask.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "视频任务未完成或镜头顺序不匹配");
            }
            videoStorage.pathFor(videoTask.getOutputPath());
        }
        Instant now = Instant.now();
        RenderTask task = new RenderTask(
                IdUtil.fastSimpleUUID(), scriptId, "QUEUED", encode(request.videoTaskIds()),
                null, null, now, now);
        renderTaskMapper.insert(task);
        return task;
    }

    @Override
    public Optional<RenderTask> findRenderById(String id) {
        return renderTaskMapper.selectById(id);
    }

    @Override
    public List<RenderTask> listScriptRenders(String scriptId) {
        requireScript(scriptId);
        return renderTaskMapper.selectByScriptId(scriptId);
    }

    @Override
    public byte[] readRender(String id) {
        RenderTask task = renderTaskMapper.selectById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "合成任务不存在"));
        if (!"SUCCEEDED".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "成片尚未完成");
        }
        return renderStorage.read(task.getOutputPath());
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 15000)
    public void processPending() {
        for (RenderTask task : renderTaskMapper.selectPending()) {
            render(task);
        }
    }

    private void render(RenderTask task) {
        task.setStatus("RENDERING");
        update(task);
        try {
            List<String> ids = objectMapper.readValue(task.getVideoTaskIds(), new TypeReference<>() {
            });
            List<Path> clips = new ArrayList<>();
            for (String id : ids) {
                VideoTask videoTask = videoTaskMapper.selectById(id)
                        .orElseThrow(() -> new IllegalStateException("视频任务不存在"));
                clips.add(videoStorage.pathFor(videoTask.getOutputPath()));
            }
            Path output = renderStorage.target(task.getId());
            renderer.render(clips, output);
            task.setOutputPath(output.getFileName().toString());
            task.setStatus("SUCCEEDED");
            update(task);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            fail(task);
        } catch (Exception exception) {
            fail(task);
        }
    }

    private String encode(List<String> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "视频任务列表无效");
        }
    }

    private void requireScript(String scriptId) {
        if (scriptMapper.selectById(scriptId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "剧本不存在");
        }
    }

    private void fail(RenderTask task) {
        task.setStatus("FAILED");
        task.setErrorMessage("视频合成失败，请检查片段和 FFmpeg 配置");
        update(task);
    }

    private void update(RenderTask task) {
        task.setUpdatedAt(Instant.now());
        renderTaskMapper.update(task);
    }
}
