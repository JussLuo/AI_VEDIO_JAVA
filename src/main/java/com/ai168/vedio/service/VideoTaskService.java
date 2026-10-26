package com.ai168.vedio.service;

import com.ai168.vedio.dto.CreateVideoTaskRequest;
import com.ai168.vedio.entity.VideoTask;
import java.util.List;
import java.util.Optional;

public interface VideoTaskService {
    VideoTask createTask(String shotId, CreateVideoTaskRequest request);

    Optional<VideoTask> findTaskById(String id);

    List<VideoTask> listShotTasks(String shotId);

    byte[] readVideo(String taskId);
}
