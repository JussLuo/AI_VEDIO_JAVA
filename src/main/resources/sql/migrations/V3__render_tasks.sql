CREATE TABLE IF NOT EXISTS render_tasks (
    id VARCHAR(36) PRIMARY KEY COMMENT '合成任务唯一标识',
    script_id VARCHAR(36) NOT NULL COMMENT '所属剧本标识',
    status VARCHAR(32) NOT NULL COMMENT '合成任务状态',
    video_task_ids TEXT NOT NULL COMMENT '按镜头顺序排列的视频任务标识 JSON',
    output_path VARCHAR(1000) NULL COMMENT '本地成片文件名',
    error_message VARCHAR(255) NULL COMMENT '安全的失败原因',
    created_at VARCHAR(30) NOT NULL COMMENT '创建时间',
    updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间',
    CONSTRAINT fk_render_tasks_script FOREIGN KEY (script_id) REFERENCES scripts(id),
    INDEX idx_render_tasks_status (status)
) COMMENT='剧本视频合成任务';
