package com.ai168.vedio.controller;

import com.ai168.vedio.dto.SaveShotRequest;
import com.ai168.vedio.dto.ShotDraft;
import com.ai168.vedio.entity.Shot;
import com.ai168.vedio.service.ShotDraftService;
import com.ai168.vedio.service.ShotService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShotController {
    private final ShotService shotService;
    private final ShotDraftService shotDraftService;

    public ShotController(ShotService shotService, ShotDraftService shotDraftService) {
        this.shotService = shotService;
        this.shotDraftService = shotDraftService;
    }

    @PostMapping("/api/scripts/{scriptId}/shot-drafts")
    public List<ShotDraft> generateShotDrafts(@PathVariable String scriptId) {
        return shotDraftService.generateDrafts(scriptId);
    }

    @PostMapping("/api/scripts/{scriptId}/shots")
    public ResponseEntity<Shot> createShot(@PathVariable String scriptId, @Valid @RequestBody SaveShotRequest request) {
        Shot shot = shotService.createShot(scriptId, request);
        return ResponseEntity.created(URI.create("/api/shots/" + shot.getId())).body(shot);
    }

    @GetMapping("/api/scripts/{scriptId}/shots")
    public List<Shot> listScriptShots(@PathVariable String scriptId) {
        return shotService.listScriptShots(scriptId);
    }

    @GetMapping("/api/shots/{id}")
    public ResponseEntity<Shot> getShot(@PathVariable String id) {
        return shotService.findShotById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/api/shots/{id}")
    public ResponseEntity<Shot> updateShot(@PathVariable String id, @Valid @RequestBody SaveShotRequest request) {
        return shotService.updateShot(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/shots/{id}")
    public ResponseEntity<Void> deleteShot(@PathVariable String id) {
        if (shotService.deleteShot(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
