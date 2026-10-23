package com.ai168.vedio.service;

import com.ai168.vedio.dto.SaveShotRequest;
import com.ai168.vedio.entity.Shot;
import java.util.List;
import java.util.Optional;

public interface ShotService {
    Shot createShot(String scriptId, SaveShotRequest request);

    List<Shot> listScriptShots(String scriptId);

    Optional<Shot> findShotById(String id);

    Optional<Shot> updateShot(String id, SaveShotRequest request);

    boolean deleteShot(String id);
}
