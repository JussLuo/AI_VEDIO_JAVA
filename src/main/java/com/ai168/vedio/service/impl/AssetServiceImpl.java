package com.ai168.vedio.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ai168.vedio.dto.SaveAssetRequest;
import com.ai168.vedio.entity.Asset;
import com.ai168.vedio.entity.Shot;
import com.ai168.vedio.mapper.AssetMapper;
import com.ai168.vedio.mapper.ProjectMapper;
import com.ai168.vedio.mapper.ScriptMapper;
import com.ai168.vedio.mapper.ShotMapper;
import com.ai168.vedio.service.AssetService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetServiceImpl implements AssetService {
    private final AssetMapper assetMapper;
    private final ProjectMapper projectMapper;
    private final ShotMapper shotMapper;
    private final ScriptMapper scriptMapper;

    public AssetServiceImpl(
            AssetMapper assetMapper,
            ProjectMapper projectMapper,
            ShotMapper shotMapper,
            ScriptMapper scriptMapper) {
        this.assetMapper = assetMapper;
        this.projectMapper = projectMapper;
        this.shotMapper = shotMapper;
        this.scriptMapper = scriptMapper;
    }

    @Override
    public Asset createAsset(String projectId, SaveAssetRequest request) {
        requireProject(projectId);
        String shotId = normalizeShotId(request.shotId());
        validateShot(projectId, shotId);
        Instant now = Instant.now();
        Asset asset = new Asset(
                IdUtil.fastSimpleUUID(),
                projectId,
                shotId,
                request.assetType().trim(),
                request.name().trim(),
                request.filePath().trim(),
                request.mimeType().trim(),
                request.fileSize(),
                now,
                now);
        assetMapper.insert(asset);
        return asset;
    }

    @Override
    public List<Asset> listProjectAssets(String projectId) {
        requireProject(projectId);
        return assetMapper.selectByProjectId(projectId);
    }

    @Override
    public Optional<Asset> findAssetById(String id) {
        return assetMapper.selectById(id);
    }

    @Override
    public Optional<Asset> updateAsset(String id, SaveAssetRequest request) {
        Optional<Asset> existing = assetMapper.selectById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        Asset asset = existing.get();
        String shotId = normalizeShotId(request.shotId());
        validateShot(asset.getProjectId(), shotId);
        asset.setShotId(shotId);
        asset.setAssetType(request.assetType().trim());
        asset.setName(request.name().trim());
        asset.setFilePath(request.filePath().trim());
        asset.setMimeType(request.mimeType().trim());
        asset.setFileSize(request.fileSize());
        asset.setUpdatedAt(Instant.now());
        assetMapper.update(asset);
        return Optional.of(asset);
    }

    @Override
    public boolean deleteAsset(String id) {
        return assetMapper.deleteById(id) > 0;
    }

    private void requireProject(String projectId) {
        if (projectMapper.selectById(projectId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在");
        }
    }

    private String normalizeShotId(String shotId) {
        return shotId == null || shotId.isBlank() ? null : shotId.trim();
    }

    private void validateShot(String projectId, String shotId) {
        if (shotId == null) {
            return;
        }
        Shot shot = shotMapper.selectById(shotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分镜不存在"));
        String shotProjectId = scriptMapper.selectById(shot.getScriptId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "剧本不存在"))
                .getProjectId();
        if (!projectId.equals(shotProjectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分镜不属于该项目");
        }
    }
}
