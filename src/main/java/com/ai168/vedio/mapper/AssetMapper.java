package com.ai168.vedio.mapper;

import com.ai168.vedio.entity.Asset;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetMapper {
    void insert(Asset asset);

    List<Asset> selectByProjectId(@Param("projectId") String projectId);

    Optional<Asset> selectById(@Param("id") String id);

    int update(Asset asset);

    int deleteById(@Param("id") String id);
}
