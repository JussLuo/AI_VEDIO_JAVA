CREATE TABLE IF NOT EXISTS video_tasks (
    id VARCHAR(36) PRIMARY KEY COMMENT '任务唯一标识',
    shot_id VARCHAR(36) NOT NULL COMMENT '关联分镜标识',
    asset_id VARCHAR(36) NULL COMMENT '可选首帧素材标识',
    status VARCHAR(32) NOT NULL COMMENT '任务状态',
    prediction_id VARCHAR(100) NULL COMMENT 'Replicate 任务标识',
    output_path VARCHAR(1000) NULL COMMENT '本地视频路径',
    error_message VARCHAR(255) NULL COMMENT '安全的失败原因',
    created_at VARCHAR(30) NOT NULL COMMENT '创建时间',
    updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间',
    CONSTRAINT fk_video_tasks_shot FOREIGN KEY (shot_id) REFERENCES shots(id),
    CONSTRAINT fk_video_tasks_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
    INDEX idx_video_tasks_status (status)
) COMMENT='逐镜头视频生成任务';
