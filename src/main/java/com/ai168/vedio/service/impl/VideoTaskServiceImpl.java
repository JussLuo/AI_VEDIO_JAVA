package com.ai168.vedio.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ai168.vedio.dto.CreateVideoTaskRequest;
import com.ai168.vedio.entity.Asset;
import com.ai168.vedio.entity.Shot;
import com.ai168.vedio.entity.VideoTask;
import com.ai168.vedio.mapper.AssetMapper;
import com.ai168.vedio.mapper.ShotMapper;
import com.ai168.vedio.mapper.VideoTaskMapper;
import com.ai168.vedio.service.AssetFileStorage;
import com.ai168.vedio.service.ReplicateVideoClient;
import com.ai168.vedio.service.VideoFileStorage;
import com.ai168.vedio.service.VideoTaskService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VideoTaskServiceImpl implements VideoTaskService {
    private final ShotMapper shotMapper;
    private final AssetMapper assetMapper;
    private final VideoTaskMapper taskMapper;
    private final ReplicateVideoClient client;
    private final VideoFileStorage storage;
    private final AssetFileStorage assetStorage;

    public VideoTaskServiceImpl(
            ShotMapper shotMapper,
            AssetMapper assetMapper,
            VideoTaskMapper taskMapper,
            ReplicateVideoClient client,
            VideoFileStorage storage,
            AssetFileStorage assetStorage) {
        this.shotMapper = shotMapper;
        this.assetMapper = assetMapper;
        this.taskMapper = taskMapper;
        this.client = client;
        this.storage = storage;
        this.assetStorage = assetStorage;
    }

    @Override
    public synchronized VideoTask createTask(String shotId, CreateVideoTaskRequest request) {
        Shot shot = shotMapper.selectById(shotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分镜不存在"));
        if (shot.getDurationSeconds() < 1 || shot.getDurationSeconds() > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Replicate 支持 1 到 15 秒的镜头");
        }
        if (taskMapper.countActiveByShotId(shotId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该镜头已有进行中的任务");
        }
        String assetId = request == null ? null : request.assetId();
        if (assetId != null) {
            requireImage(shotId, assetId);
        }
        Instant now = Instant.now();
        VideoTask task = new VideoTask(
                IdUtil.fastSimpleUUID(), shotId, assetId, "QUEUED", null, null, null, now, now);
        taskMapper.insert(task);
        return task;
    }

    @Override
    public Optional<VideoTask> findTaskById(String id) {
        return taskMapper.selectById(id);
    }

    @Override
    public List<VideoTask> listShotTasks(String shotId) {
        if (shotMapper.selectById(shotId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分镜不存在");
        }
        return taskMapper.selectByShotId(shotId);
    }

    @Override
    public byte[] readVideo(String taskId) {
        VideoTask task = taskMapper.selectById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "任务不存在"));
        if (!"SUCCEEDED".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "视频尚未生成完成");
        }
        return storage.read(task.getOutputPath());
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void processPending() {
        for (VideoTask task : taskMapper.selectPending()) {
            if ("QUEUED".equals(task.getStatus())) {
                submit(task);
            } else if ("PROCESSING".equals(task.getStatus())) {
                poll(task);
            }
        }
    }

    private void submit(VideoTask task) {
        task.setStatus("SUBMITTING");
        update(task);
        try {
            Shot shot = shotMapper.selectById(task.getShotId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分镜不存在"));
            byte[] image = null;
            String mimeType = null;
            if (task.getAssetId() != null) {
                Asset asset = requireImage(task.getShotId(), task.getAssetId());
                image = assetStorage.read(asset.getProjectId(), asset.getFilePath());
                mimeType = asset.getMimeType();
            }
            task.setPredictionId(client.submit(shot, image, mimeType));
        } catch (RuntimeException exception) {
            fail(task, "提交视频生成任务失败");
            return;
        }
        task.setStatus("PROCESSING");
        update(task);
    }

    private void poll(VideoTask task) {
        try {
            ReplicateVideoClient.Prediction prediction = client.getPrediction(task.getPredictionId());
            switch (prediction.status()) {
                case "succeeded" -> {
                    if (prediction.outputUrl() == null) {
                        fail(task, "生成结果缺少视频地址");
                        return;
                    }
                    task.setOutputPath(storage.save(task.getId(), prediction.outputUrl()));
                    task.setStatus("SUCCEEDED");
                    update(task);
                }
                case "failed", "canceled" -> fail(task, "视频生成失败或已取消");
                case "starting", "processing" -> {
                }
                default -> fail(task, "未知视频任务状态");
            }
        } catch (RuntimeException exception) {
        }
    }

    private Asset requireImage(String shotId, String assetId) {
        Asset asset = assetMapper.selectById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "素材不存在"));
        if (!shotId.equals(asset.getShotId())
                || !List.of("image/png", "image/jpeg", "image/webp").contains(asset.getMimeType())
                || asset.getFileSize() > 1024 * 1024
                || !assetStorage.isManaged(asset.getProjectId(), asset.getFilePath())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "首帧素材须为该镜头上传的不超过 1 MiB 的图片");
        }
        return asset;
    }

    private void fail(VideoTask task, String message) {
        task.setStatus("FAILED");
        task.setErrorMessage(message);
        update(task);
    }

    private void update(VideoTask task) {
        task.setUpdatedAt(Instant.now());
        taskMapper.update(task);
    }
}
