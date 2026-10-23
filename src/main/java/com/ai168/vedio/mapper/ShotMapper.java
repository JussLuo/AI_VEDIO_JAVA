package com.ai168.vedio.mapper;

import com.ai168.vedio.entity.Shot;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShotMapper {
    void insert(Shot shot);

    List<Shot> selectByScriptId(@Param("scriptId") String scriptId);

    Optional<Shot> selectById(@Param("id") String id);

    int countByScriptAndNumber(@Param("scriptId") String scriptId,
                               @Param("shotNumber") Integer shotNumber,
                               @Param("excludedId") String excludedId);

    int update(Shot shot);

    int deleteById(@Param("id") String id);
}
