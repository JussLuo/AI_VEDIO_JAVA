package com.ai168.vedio.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 视频生成任务实体，对应 video_tasks 数据表。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoTask {
    /** 任务唯一标识。 */
    private String id;
    /** 关联分镜标识。 */
    private String shotId;
    /** 可选首帧素材标识。 */
    private String assetId;
    /** 任务状态。 */
    private String status;
    /** Replicate 任务标识。 */
    private String predictionId;
    /** 本地视频路径。 */
    private String outputPath;
    /** 不包含上游敏感内容的失败原因。 */
    private String errorMessage;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
