package com.ai168.vedio.service;

import com.ai168.vedio.dto.SaveAssetRequest;
import com.ai168.vedio.entity.Asset;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface AssetService {
    Asset createAsset(String projectId, SaveAssetRequest request);

    List<Asset> listProjectAssets(String projectId);

    Optional<Asset> findAssetById(String id);

    Optional<Asset> updateAsset(String id, SaveAssetRequest request);

    boolean deleteAsset(String id);

    Asset uploadAsset(String projectId, String shotId, String assetType, String name, MultipartFile file);

    byte[] readAssetContent(String id);
}
