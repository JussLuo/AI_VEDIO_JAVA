package com.ai168.vedio.controller;

import com.ai168.vedio.dto.SaveAssetRequest;
import com.ai168.vedio.entity.Asset;
import com.ai168.vedio.service.AssetService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping("/api/projects/{projectId}/assets")
    public ResponseEntity<Asset> createAsset(@PathVariable String projectId, @Valid @RequestBody SaveAssetRequest request) {
        Asset asset = assetService.createAsset(projectId, request);
        return ResponseEntity.created(URI.create("/api/assets/" + asset.getId())).body(asset);
    }

    @GetMapping("/api/projects/{projectId}/assets")
    public List<Asset> listProjectAssets(@PathVariable String projectId) {
        return assetService.listProjectAssets(projectId);
    }

    @GetMapping("/api/assets/{id}")
    public ResponseEntity<Asset> getAsset(@PathVariable String id) {
        return assetService.findAssetById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/api/assets/{id}")
    public ResponseEntity<Asset> updateAsset(@PathVariable String id, @Valid @RequestBody SaveAssetRequest request) {
        return assetService.updateAsset(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/assets/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable String id) {
        if (assetService.deleteAsset(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/api/projects/{projectId}/assets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Asset> uploadAsset(
            @PathVariable String projectId,
            @RequestParam(required = false) String shotId,
            @RequestParam String assetType,
            @RequestParam String name,
            @RequestParam MultipartFile file) {
        Asset asset = assetService.uploadAsset(projectId, shotId, assetType, name, file);
        return ResponseEntity.created(URI.create("/api/assets/" + asset.getId())).body(asset);
    }

    @GetMapping("/api/assets/{id}/content")
    public ResponseEntity<byte[]> getAssetContent(@PathVariable String id) {
        Asset asset = assetService.findAssetById(id).orElse(null);
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] content = assetService.readAssetContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getMimeType()))
                .body(content);
    }
}
