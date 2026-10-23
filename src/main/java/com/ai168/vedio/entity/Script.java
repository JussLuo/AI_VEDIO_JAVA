package com.ai168.vedio.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 剧本实体，对应 scripts 数据表。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Script {
    /** 剧本唯一标识。 */
    private String id;
    /** 所属项目标识。 */
    private String projectId;
    /** 剧本标题。 */
    private String title;
    /** 剧本文本。 */
    private String content;
    /** 剧本状态。 */
    private String status;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
