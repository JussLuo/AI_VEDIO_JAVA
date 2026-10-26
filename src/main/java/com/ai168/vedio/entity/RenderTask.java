package com.ai168.vedio.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 剧本视频合成任务实体，对应 render_tasks 数据表。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenderTask {
    /** 任务唯一标识。 */
    private String id;
    /** 所属剧本标识。 */
    private String scriptId;
    /** 任务状态。 */
    private String status;
    /** 按镜头顺序排列的视频任务标识 JSON。 */
    private String videoTaskIds;
    /** 本地成片文件名。 */
    private String outputPath;
    /** 安全的失败原因。 */
    private String errorMessage;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
