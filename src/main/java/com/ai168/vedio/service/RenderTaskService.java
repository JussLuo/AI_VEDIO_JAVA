package com.ai168.vedio.service;

import com.ai168.vedio.dto.CreateRenderRequest;
import com.ai168.vedio.entity.RenderTask;
import java.util.List;
import java.util.Optional;

public interface RenderTaskService {
    RenderTask createRender(String scriptId, CreateRenderRequest request);

    Optional<RenderTask> findRenderById(String id);

    List<RenderTask> listScriptRenders(String scriptId);

    byte[] readRender(String id);
}
