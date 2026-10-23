package com.ai168.vedio.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ai168.vedio.dto.SaveShotRequest;
import com.ai168.vedio.entity.Shot;
import com.ai168.vedio.mapper.ScriptMapper;
import com.ai168.vedio.mapper.ShotMapper;
import com.ai168.vedio.service.ShotService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShotServiceImpl implements ShotService {
    private final ShotMapper shotMapper;
    private final ScriptMapper scriptMapper;

    public ShotServiceImpl(ShotMapper shotMapper, ScriptMapper scriptMapper) {
        this.shotMapper = shotMapper;
        this.scriptMapper = scriptMapper;
    }

    @Override
    public Shot createShot(String scriptId, SaveShotRequest request) {
        requireScript(scriptId);
        ensureNumberAvailable(scriptId, request.shotNumber(), null);
        Instant now = Instant.now();
        Shot shot = new Shot(
                IdUtil.fastSimpleUUID(),
                scriptId,
                request.shotNumber(),
                request.visualDescription().trim(),
                request.videoPrompt().trim(),
                request.durationSeconds(),
                now,
                now);
        try {
            shotMapper.insert(shot);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "镜头序号已存在", exception);
        }
        return shot;
    }

    @Override
    public List<Shot> listScriptShots(String scriptId) {
        requireScript(scriptId);
        return shotMapper.selectByScriptId(scriptId);
    }

    @Override
    public Optional<Shot> findShotById(String id) {
        return shotMapper.selectById(id);
    }

    @Override
    public Optional<Shot> updateShot(String id, SaveShotRequest request) {
        Optional<Shot> existing = shotMapper.selectById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        Shot shot = existing.get();
        ensureNumberAvailable(shot.getScriptId(), request.shotNumber(), id);
        shot.setShotNumber(request.shotNumber());
        shot.setVisualDescription(request.visualDescription().trim());
        shot.setVideoPrompt(request.videoPrompt().trim());
        shot.setDurationSeconds(request.durationSeconds());
        shot.setUpdatedAt(Instant.now());
        try {
            shotMapper.update(shot);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "镜头序号已存在", exception);
        }
        return Optional.of(shot);
    }

    @Override
    public boolean deleteShot(String id) {
        return shotMapper.deleteById(id) > 0;
    }

    private void requireScript(String scriptId) {
        if (scriptMapper.selectById(scriptId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "剧本不存在");
        }
    }

    private void ensureNumberAvailable(String scriptId, Integer shotNumber, String excludedId) {
        if (shotMapper.countByScriptAndNumber(scriptId, shotNumber, excludedId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "镜头序号已存在");
        }
    }
}
