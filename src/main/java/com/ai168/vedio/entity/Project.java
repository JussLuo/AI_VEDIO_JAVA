package com.ai168.vedio.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 项目实体，对应 projects 数据表。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    /** 项目唯一标识。 */
    private String id;
    /** 项目名称。 */
    private String name;
    /** 项目描述。 */
    private String description;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
