-- 公告管理表
CREATE TABLE IF NOT EXISTS sys_announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    cover_image_id BIGINT COMMENT '封面图片ID',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    content_images TEXT COMMENT '内容图片JSON数组',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
    publish_time DATETIME COMMENT '发布时间',
    expire_time DATETIME COMMENT '过期时间',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态 draft-草稿 pending-待审批 approving-审批中 approved-已通过 published-已发布 offline-已下线 rejected-已驳回',
    approval_instance_id BIGINT COMMENT '审批实例ID',
    creator_name VARCHAR(100) COMMENT '创建人名称',
    created_by BIGINT DEFAULT 0 COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT DEFAULT 0 COMMENT '更新人ID',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time),
    INDEX idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告管理表';