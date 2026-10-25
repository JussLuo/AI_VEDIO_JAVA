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
import com.ai168.vedio.service.AssetFileStorage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssetServiceImpl implements AssetService {
    private final AssetMapper assetMapper;
    private final ProjectMapper projectMapper;
    private final ShotMapper shotMapper;
    private final ScriptMapper scriptMapper;
    private final AssetFileStorage fileStorage;

    public AssetServiceImpl(
            AssetMapper assetMapper,
            ProjectMapper projectMapper,
            ShotMapper shotMapper,
            ScriptMapper scriptMapper,
            AssetFileStorage fileStorage) {
        this.assetMapper = assetMapper;
        this.projectMapper = projectMapper;
        this.shotMapper = shotMapper;
        this.scriptMapper = scriptMapper;
        this.fileStorage = fileStorage;
    }

    @Override
    public Asset createAsset(String projectId, SaveAssetRequest request) {
        requireProject(projectId);
        validateMetadataPath(request.filePath(), null);
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
        validateMetadataPath(request.filePath(), asset);
        if (fileStorage.isManaged(asset.getProjectId(), asset.getFilePath())
                && (!request.mimeType().equals(asset.getMimeType())
                || !request.fileSize().equals(asset.getFileSize()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已上传文件的类型和大小不可修改");
        }
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
        Optional<Asset> existing = assetMapper.selectById(id);
        if (existing.isEmpty()) {
            return false;
        }
        Asset asset = existing.get();
        if (assetMapper.deleteById(id) == 0) {
            return false;
        }
        if (fileStorage.isManaged(asset.getProjectId(), asset.getFilePath())) {
            fileStorage.delete(asset.getProjectId(), asset.getFilePath());
        }
        return true;
    }

    @Override
    public Asset uploadAsset(String projectId, String shotId, String assetType, String name, MultipartFile file) {
        requireProject(projectId);
        String normalizedShotId = normalizeShotId(shotId);
        validateShot(projectId, normalizedShotId);
        if (assetType == null || assetType.isBlank() || name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材类型和名称不能为空");
        }
        AssetFileStorage.StoredFile storedFile = fileStorage.save(projectId, file);
        Instant now = Instant.now();
        Asset asset = new Asset(
                IdUtil.fastSimpleUUID(),
                projectId,
                normalizedShotId,
                assetType.trim(),
                name.trim(),
                storedFile.relativePath(),
                storedFile.mimeType(),
                storedFile.size(),
                now,
                now);
        try {
            assetMapper.insert(asset);
        } catch (RuntimeException exception) {
            fileStorage.delete(projectId, storedFile.relativePath());
            throw exception;
        }
        return asset;
    }

    @Override
    public byte[] readAssetContent(String id) {
        Asset asset = assetMapper.selectById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "素材不存在"));
        return fileStorage.read(asset.getProjectId(), asset.getFilePath());
    }

    private void requireProject(String projectId) {
        if (projectMapper.selectById(projectId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在");
        }
    }

    private String normalizeShotId(String shotId) {
        return shotId == null || shotId.isBlank() ? null : shotId.trim();
    }

    private void validateMetadataPath(String filePath, Asset existing) {
        String normalizedPath = filePath.trim().replace('\\', '/');
        if (existing != null
                && fileStorage.isManaged(existing.getProjectId(), existing.getFilePath())
                && normalizedPath.equals(existing.getFilePath())) {
            return;
        }
        if (existing != null && fileStorage.isManaged(existing.getProjectId(), existing.getFilePath())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已上传文件的路径不可修改");
        }
        if (normalizedPath.startsWith("projects/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "上传目录路径由服务端管理");
        }
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
