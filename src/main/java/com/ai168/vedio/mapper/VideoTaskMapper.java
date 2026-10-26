package com.ai168.vedio.mapper;

import com.ai168.vedio.entity.VideoTask;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoTaskMapper {
    void insert(VideoTask task);

    int update(VideoTask task);

    Optional<VideoTask> selectById(@Param("id") String id);

    List<VideoTask> selectByShotId(@Param("shotId") String shotId);

    List<VideoTask> selectPending();

    int countActiveByShotId(@Param("shotId") String shotId);
}
