package com.ai168.vedio.controller;

import com.ai168.vedio.dto.CreateVideoTaskRequest;
import com.ai168.vedio.entity.VideoTask;
import com.ai168.vedio.service.VideoTaskService;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoTaskController {
    private final VideoTaskService taskService;

    public VideoTaskController(VideoTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/api/shots/{shotId}/video-tasks")
    public ResponseEntity<VideoTask> createTask(
            @PathVariable String shotId, @RequestBody(required = false) CreateVideoTaskRequest request) {
        VideoTask task = taskService.createTask(shotId, request);
        return ResponseEntity.accepted()
                .location(URI.create("/api/video-tasks/" + task.getId()))
                .body(task);
    }

    @GetMapping("/api/shots/{shotId}/video-tasks")
    public List<VideoTask> listShotTasks(@PathVariable String shotId) {
        return taskService.listShotTasks(shotId);
    }

    @GetMapping("/api/video-tasks/{id}")
    public ResponseEntity<VideoTask> getTask(@PathVariable String id) {
        return taskService.findTaskById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/video-tasks/{id}/content")
    public ResponseEntity<byte[]> getVideo(@PathVariable String id) {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(taskService.readVideo(id));
    }
}
