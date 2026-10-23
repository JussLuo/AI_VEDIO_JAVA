package com.ai168.vedio.mapper;

import com.ai168.vedio.entity.Project;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper {
    void insert(Project project);
    List<Project> selectAll();
    Optional<Project> selectById(@Param("id") String id);
}
