package com.ai168.vedio.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 素材实体，对应 assets 数据表。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    /** 素材唯一标识。 */
    private String id;
    /** 所属项目标识。 */
    private String projectId;
    /** 关联分镜标识。 */
    private String shotId;
    /** 素材类型。 */
    private String assetType;
    /** 素材名称。 */
    private String name;
    /** 本地文件路径。 */
    private String filePath;
    /** 文件媒体类型。 */
    private String mimeType;
    /** 文件大小，单位字节。 */
    private Long fileSize;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
