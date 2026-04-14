-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：系统管理（用户、角色、权限）
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 57. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username            VARCHAR(50) NOT NULL COMMENT '用户名',
    password            VARCHAR(255) NOT NULL COMMENT '密码',
    real_name           VARCHAR(50) COMMENT '真实姓名',
    phone               VARCHAR(20) COMMENT '手机号',
    email               VARCHAR(100) COMMENT '邮箱',
    avatar              VARCHAR(500) COMMENT '头像路径',
    department_id       BIGINT COMMENT '部门ID',
    department_name     VARCHAR(100) COMMENT '部门名称',
    position            VARCHAR(50) COMMENT '职位',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-正常 2-禁用 3-锁定',
    lock_until          DATETIME COMMENT '锁定截止时间',
    login_attempts      INT DEFAULT 0 COMMENT '登录失败次数',
    last_login_time     DATETIME COMMENT '最后登录时间',
    last_login_ip      VARCHAR(50) COMMENT '最后登录IP',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_username (username),
    INDEX idx_department_id (department_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ----------------------------
-- 58. 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dept_no             VARCHAR(50) NOT NULL COMMENT '部门编号',
    dept_name           VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id           BIGINT DEFAULT 0 COMMENT '父部门ID',
    level               INT DEFAULT 1 COMMENT '层级',
    leader_id           BIGINT COMMENT '部门负责人ID',
    leader_name         VARCHAR(50) COMMENT '部门负责人姓名',
    sort_order          INT DEFAULT 0 COMMENT '排序号',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_dept_no (dept_no),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

-- ----------------------------
-- 59. 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_code           VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name           VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_type           TINYINT COMMENT '角色类型: 1-系统角色 2-业务角色',
    data_scope          TINYINT DEFAULT 1 COMMENT '数据范围: 1-全部 2-本部门 3-本部门及子部门 4-仅本人 5-指定范围',
    dept_ids            VARCHAR(500) COMMENT '指定部门范围(多个逗号分隔)',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    remark              VARCHAR(500) COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ----------------------------
-- 60. 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- ----------------------------
-- 61. 权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    permission_code    VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_name    VARCHAR(100) NOT NULL COMMENT '权限名称',
    parent_id          BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_type    TINYINT NOT NULL COMMENT '权限类型: 1-目录 2-菜单 3-按钮 4-接口',
    path               VARCHAR(200) COMMENT '路由路径',
    component          VARCHAR(200) COMMENT '组件路径',
    icon               VARCHAR(50) COMMENT '图标',
    sort_order          INT DEFAULT 0 COMMENT '排序号',
    visible            TINYINT DEFAULT 1 COMMENT '是否可见: 0-否 1-是',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_permission_code (permission_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- ----------------------------
-- 62. 角色权限关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    permission_id   BIGINT NOT NULL COMMENT '权限ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- ----------------------------
-- 63. 系统公告表
-- ----------------------------
DROP TABLE IF EXISTS sys_announcement;
CREATE TABLE sys_announcement (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title               VARCHAR(200) NOT NULL COMMENT '公告标题',
    content             LONGTEXT COMMENT '公告内容',
    cover_image_id      BIGINT COMMENT '封面图片ID',
    cover_image_url     VARCHAR(500) COMMENT '封面图片URL',
    content_images      LONGTEXT COMMENT '内容图片ID列表(JSON)',
    announcement_type   TINYINT COMMENT '公告类型: 1-系统公告 2-业务公告 3-其他',
    priority            TINYINT DEFAULT 2 COMMENT '优先级: 1-高 2-中 3-低',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已通过 5-已驳回 6-已发布 7-已下线',
    approval_instance_id BIGINT COMMENT '审批流程实例ID',
    publish_time        DATETIME COMMENT '发布时间',
    expire_time         DATETIME COMMENT '过期时间',
    is_top              TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    view_count          INT DEFAULT 0 COMMENT '浏览次数',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    INDEX idx_status_publish (status, publish_time),
    INDEX idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统公告表';

-- ----------------------------
-- 64. 待办事项表
-- ----------------------------
DROP TABLE IF EXISTS biz_todo;
CREATE TABLE biz_todo (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    todo_no             VARCHAR(50) NOT NULL COMMENT '待办编号',
    biz_type            VARCHAR(50) NOT NULL COMMENT '业务类型',
    biz_id              BIGINT NOT NULL COMMENT '业务数据ID',
    title               VARCHAR(200) NOT NULL COMMENT '待办标题',
    content             TEXT COMMENT '待办内容',
    todo_category       TINYINT NOT NULL COMMENT '分类: 1-待办 2-已办 3-已阅',
    source_type         TINYINT NOT NULL COMMENT '来源: 1-审批 2-通知 3-系统 4-任务',
    handler_id          BIGINT NOT NULL COMMENT '处理人ID',
    handler_name        VARCHAR(50) COMMENT '处理人姓名',
    applicant_id        BIGINT COMMENT '申请人ID',
    applicant_name      VARCHAR(50) COMMENT '申请人姓名',
    current_node        VARCHAR(100) COMMENT '当前审批节点',
    priority            INT DEFAULT 0 COMMENT '紧急程度',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-待处理 2-处理中 3-已完成 4-已取消',
    action_time         DATETIME COMMENT '处理时间',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_todo_no (todo_no),
    INDEX idx_handler_category_time (handler_id, todo_category, action_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='待办事项表';

-- ----------------------------
-- 65. 待办操作日志表
-- ----------------------------
DROP TABLE IF EXISTS biz_todo_action_log;
CREATE TABLE biz_todo_action_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    todo_id         BIGINT NOT NULL COMMENT '待办ID',
    user_id         BIGINT NOT NULL COMMENT '操作用户ID',
    user_name       VARCHAR(50) COMMENT '操作用户姓名',
    action_type     VARCHAR(50) NOT NULL COMMENT '操作类型: approve/reject/read等',
    action_result  VARCHAR(50) COMMENT '操作结果',
    opinion         TEXT COMMENT '审批意见',
    operate_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_todo_id (todo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='待办操作日志表';

-- ----------------------------
-- 66. 附件表
-- ----------------------------
DROP TABLE IF EXISTS biz_attachment;
CREATE TABLE biz_attachment (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_name           VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path           VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size           BIGINT COMMENT '文件大小(字节)',
    file_type           VARCHAR(50) COMMENT '文件类型',
    extension           VARCHAR(20) COMMENT '文件扩展名',
    bucket_name         VARCHAR(100) COMMENT '存储桶名称',
    biz_type            VARCHAR(50) COMMENT '业务类型',
    biz_id              BIGINT COMMENT '业务ID',
    uploader_id         BIGINT COMMENT '上传人ID',
    uploader_name       VARCHAR(50) COMMENT '上传人姓名',
    upload_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_biz_type_id (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='附件表';

-- ----------------------------
-- 67. 系统配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key          VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value        TEXT COMMENT '配置值',
    config_type         VARCHAR(50) COMMENT '配置类型',
    description         VARCHAR(200) COMMENT '说明',
    is_system           TINYINT DEFAULT 0 COMMENT '是否系统配置: 0-否 1-是',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

-- ----------------------------
-- 68. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id             BIGINT COMMENT '操作用户ID',
    username            VARCHAR(50) COMMENT '用户名',
    real_name           VARCHAR(50) COMMENT '真实姓名',
    module             VARCHAR(50) COMMENT '模块',
    operation           VARCHAR(50) COMMENT '操作',
    method              VARCHAR(200) COMMENT '请求方法',
    request_url         VARCHAR(500) COMMENT '请求URL',
    request_params      TEXT COMMENT '请求参数',
    response_result     TEXT COMMENT '响应结果',
    ip_address         VARCHAR(50) COMMENT 'IP地址',
    execution_time      INT COMMENT '执行时长(毫秒)',
    status              TINYINT COMMENT '状态: 1-成功 0-失败',
    error_msg           TEXT COMMENT '错误信息',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ----------------------------
-- 69. 数据字典表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_type           VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_label          VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value          VARCHAR(100) NOT NULL COMMENT '字典值',
    sort_order          INT DEFAULT 0 COMMENT '排序号',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    remark              VARCHAR(200) COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据字典表';

-- =============================================
-- 汇总：全部数据库表（共69张）
-- =============================================
-- 01_income_management.sql (3表) - 收入管理
-- 02_statement_difference.sql (2表) - 对账差异
-- 03_payment_supervision.sql (4表) - 回款督办
-- 04_payment_plan.sql (3表) - 付款计划
-- 05_expense_management.sql (4表) - 日常报销
-- 06_invoice_budget.sql (3表) - 发票与预算
-- 07_project_contract.sql (5表) - 项目与合同
-- 08_supplier_management.sql (5表) - 供应商管理
-- 09_purchase_management.sql (7表) - 采购管理
-- 10_material_management.sql (10表) - 物资管理
-- 11_construction_management.sql (10表) - 施工管理
-- 12_system_management.sql (13表) - 系统管理
-- =============================================