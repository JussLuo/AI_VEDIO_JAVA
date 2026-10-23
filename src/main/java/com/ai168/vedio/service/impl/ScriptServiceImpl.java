package com.ai168.vedio.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ai168.vedio.dto.CreateScriptRequest;
import com.ai168.vedio.dto.UpdateScriptRequest;
import com.ai168.vedio.entity.Script;
import com.ai168.vedio.mapper.ProjectMapper;
import com.ai168.vedio.mapper.ScriptMapper;
import com.ai168.vedio.service.ScriptService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ScriptServiceImpl implements ScriptService {
    private final ScriptMapper scriptMapper;
    private final ProjectMapper projectMapper;

    public ScriptServiceImpl(ScriptMapper scriptMapper, ProjectMapper projectMapper) {
        this.scriptMapper = scriptMapper;
        this.projectMapper = projectMapper;
    }
    @Override
    public Script createScript(String projectId, CreateScriptRequest request) {
        if (projectMapper.selectById(projectId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在");
        }
        Instant now = Instant.now();
        Script script = new Script(
                IdUtil.fastSimpleUUID(),
                projectId,
                request.title().trim(),
                request.content(),
                "DRAFT",
                now,
                now);
        scriptMapper.insert(script);
        return script;
    }
    @Override
    public List<Script> listProjectScripts(String projectId) {
        return scriptMapper.selectByProjectId(projectId);
    }

    @Override
    public Optional<Script> findScriptById(String id) {
        return scriptMapper.selectById(id);
    }

    @Override
    public Optional<Script> updateScript(String id, UpdateScriptRequest request) {
        return scriptMapper.selectById(id).map(existing -> {
            existing.setTitle(request.title().trim());
            existing.setContent(request.content());
            existing.setStatus(request.status());
            existing.setUpdatedAt(Instant.now());
            scriptMapper.update(existing);
            return existing;
        });
    }

    @Override
    public boolean deleteScript(String id) {
        return scriptMapper.deleteById(id) > 0;
    }
}
