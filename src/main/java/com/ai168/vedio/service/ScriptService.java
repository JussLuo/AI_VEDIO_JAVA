package com.ai168.vedio.service;

import com.ai168.vedio.dto.CreateScriptRequest;
import com.ai168.vedio.dto.UpdateScriptRequest;
import com.ai168.vedio.entity.Script;
import java.util.List;
import java.util.Optional;

public interface ScriptService {
    Script createScript(String projectId, CreateScriptRequest request);
    List<Script> listProjectScripts(String projectId);
    Optional<Script> findScriptById(String id);
    Optional<Script> updateScript(String id, UpdateScriptRequest request);
    boolean deleteScript(String id);
}
