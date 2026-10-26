package com.ai168.vedio.controller;

import com.ai168.vedio.dto.CreateRenderRequest;
import com.ai168.vedio.entity.RenderTask;
import com.ai168.vedio.service.RenderTaskService;
import jakarta.validation.Valid;
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
public class RenderTaskController {
    private final RenderTaskService renderTaskService;

    public RenderTaskController(RenderTaskService renderTaskService) {
        this.renderTaskService = renderTaskService;
    }

    @PostMapping("/api/scripts/{scriptId}/renders")
    public ResponseEntity<RenderTask> createRender(@PathVariable String scriptId, @Valid @RequestBody CreateRenderRequest request) {
        RenderTask task = renderTaskService.createRender(scriptId, request);
        return ResponseEntity.accepted()
                .location(URI.create("/api/renders/" + task.getId()))
                .body(task);
    }

    @GetMapping("/api/scripts/{scriptId}/renders")
    public List<RenderTask> listScriptRenders(@PathVariable String scriptId) {
        return renderTaskService.listScriptRenders(scriptId);
    }

    @GetMapping("/api/renders/{id}")
    public ResponseEntity<RenderTask> getRender(@PathVariable String id) {
        return renderTaskService.findRenderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/renders/{id}/content")
    public ResponseEntity<byte[]> getContent(@PathVariable String id) {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(renderTaskService.readRender(id));
    }
}
