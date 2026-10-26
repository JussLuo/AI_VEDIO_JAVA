package com.ai168.vedio.mapper;

import com.ai168.vedio.entity.RenderTask;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RenderTaskMapper {
    void insert(RenderTask task);

    int update(RenderTask task);

    Optional<RenderTask> selectById(@Param("id") String id);

    List<RenderTask> selectByScriptId(@Param("scriptId") String scriptId);

    List<RenderTask> selectPending();
}
