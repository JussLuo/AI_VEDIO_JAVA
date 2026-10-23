package com.ai168.vedio.mapper;

import com.ai168.vedio.entity.Script;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScriptMapper {
    void insert(Script script);

    List<Script> selectByProjectId(@Param("projectId") String projectId);

    Optional<Script> selectById(@Param("id") String id);

    int update(Script script);

    int deleteById(@Param("id") String id);
}
