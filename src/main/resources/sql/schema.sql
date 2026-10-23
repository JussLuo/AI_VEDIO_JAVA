CREATE TABLE IF NOT EXISTS projects (
    id VARCHAR(36) PRIMARY KEY COMMENT '项目唯一标识',
    name VARCHAR(255) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    created_at VARCHAR(30) NOT NULL COMMENT '创建时间',
    updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间'
) COMMENT='漫剧项目';

CREATE TABLE IF NOT EXISTS scripts (
    id VARCHAR(36) PRIMARY KEY COMMENT '剧本唯一标识',
    project_id VARCHAR(36) NOT NULL COMMENT '所属项目标识',
    title VARCHAR(255) NOT NULL COMMENT '剧本标题',
    content TEXT NOT NULL COMMENT '剧本文本',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '剧本状态',
    created_at VARCHAR(30) NOT NULL COMMENT '创建时间',
    updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间',
    CONSTRAINT fk_scripts_project FOREIGN KEY (project_id) REFERENCES projects(id)
) COMMENT='项目剧本';

CREATE TABLE IF NOT EXISTS shots (
    id VARCHAR(36) PRIMARY KEY COMMENT '分镜唯一标识',
    script_id VARCHAR(36) NOT NULL COMMENT '所属剧本标识',
    shot_number INT NOT NULL COMMENT '剧本内镜头序号',
    visual_description TEXT NOT NULL COMMENT '画面描述',
    video_prompt TEXT NOT NULL COMMENT '视频生成提示词',
    duration_seconds INT NOT NULL COMMENT '镜头时长，单位秒',
    created_at VARCHAR(30) NOT NULL COMMENT '创建时间',
    updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间',
    CONSTRAINT fk_shots_script FOREIGN KEY (script_id) REFERENCES scripts(id),
    CONSTRAINT uq_shots_script_number UNIQUE (script_id, shot_number)
) COMMENT='剧本分镜';

ALTER TABLE projects COMMENT='漫剧项目';
ALTER TABLE projects MODIFY COLUMN id VARCHAR(36) NOT NULL COMMENT '项目唯一标识';
ALTER TABLE projects MODIFY COLUMN name VARCHAR(255) NOT NULL COMMENT '项目名称';
ALTER TABLE projects MODIFY COLUMN description TEXT COMMENT '项目描述';
ALTER TABLE projects MODIFY COLUMN created_at VARCHAR(30) NOT NULL COMMENT '创建时间';
ALTER TABLE projects MODIFY COLUMN updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间';

ALTER TABLE scripts COMMENT='项目剧本';
ALTER TABLE scripts MODIFY COLUMN id VARCHAR(36) NOT NULL COMMENT '剧本唯一标识';
ALTER TABLE scripts MODIFY COLUMN project_id VARCHAR(36) NOT NULL COMMENT '所属项目标识';
ALTER TABLE scripts MODIFY COLUMN title VARCHAR(255) NOT NULL COMMENT '剧本标题';
ALTER TABLE scripts MODIFY COLUMN content TEXT NOT NULL COMMENT '剧本文本';
ALTER TABLE scripts MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '剧本状态';
ALTER TABLE scripts MODIFY COLUMN created_at VARCHAR(30) NOT NULL COMMENT '创建时间';
ALTER TABLE scripts MODIFY COLUMN updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间';

ALTER TABLE shots COMMENT='剧本分镜';
ALTER TABLE shots MODIFY COLUMN id VARCHAR(36) NOT NULL COMMENT '分镜唯一标识';
ALTER TABLE shots MODIFY COLUMN script_id VARCHAR(36) NOT NULL COMMENT '所属剧本标识';
ALTER TABLE shots MODIFY COLUMN shot_number INT NOT NULL COMMENT '剧本内镜头序号';
ALTER TABLE shots MODIFY COLUMN visual_description TEXT NOT NULL COMMENT '画面描述';
ALTER TABLE shots MODIFY COLUMN video_prompt TEXT NOT NULL COMMENT '视频生成提示词';
ALTER TABLE shots MODIFY COLUMN duration_seconds INT NOT NULL COMMENT '镜头时长，单位秒';
ALTER TABLE shots MODIFY COLUMN created_at VARCHAR(30) NOT NULL COMMENT '创建时间';
ALTER TABLE shots MODIFY COLUMN updated_at VARCHAR(30) NOT NULL COMMENT '最后更新时间';
