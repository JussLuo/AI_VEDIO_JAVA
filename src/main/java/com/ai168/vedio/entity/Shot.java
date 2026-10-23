package com.ai168.vedio.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 分镜实体，对应 shots 数据表。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shot {
    /** 分镜唯一标识。 */
    private String id;
    /** 所属剧本标识。 */
    private String scriptId;
    /** 剧本内的镜头序号。 */
    private Integer shotNumber;
    /** 画面描述。 */
    private String visualDescription;
    /** 视频生成提示词。 */
    private String videoPrompt;
    /** 镜头时长，单位为秒。 */
    private Integer durationSeconds;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
