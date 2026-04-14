-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：施工管理（甘特图、进度）
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 47. 甘特图主表
-- ----------------------------
DROP TABLE IF EXISTS biz_gantt;
CREATE TABLE biz_gantt (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    gantt_no            VARCHAR(50) NOT NULL COMMENT '甘特图编号',
    gantt_name          VARCHAR(200) NOT NULL COMMENT '甘特图名称',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已审批 5-进行中 6-已完成 7-已驳回',
    start_date          DATE COMMENT '计划开始日期',
    end_date            DATE COMMENT '计划结束日期',
    actual_start_date   DATE COMMENT '实际开始日期',
    actual_end_date     DATE COMMENT '实际结束日期',
    total_days          INT COMMENT '计划总天数',
    progress            DECIMAL(10,4) DEFAULT 0 COMMENT '总体进度(%)',
    submitter_id        BIGINT COMMENT '提交人ID',
    submitter_name      VARCHAR(50) COMMENT '提交人姓名',
    submitted_at        DATETIME COMMENT '提交时间',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_gantt_no (gantt_no),
    INDEX idx_project_id (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='甘特图主表';

-- ----------------------------
-- 48. 甘特任务节点表
-- ----------------------------
DROP TABLE IF EXISTS biz_gantt_task;
CREATE TABLE biz_gantt_task (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    gantt_id            BIGINT NOT NULL COMMENT '甘特图ID',
    parent_id           BIGINT DEFAULT 0 COMMENT '父任务ID',
    task_name           VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type           TINYINT NOT NULL COMMENT '任务类型: 1-里程碑 2-阶段 3-任务',
    start_date          DATE COMMENT '计划开始日期',
    end_date            DATE COMMENT '计划结束日期',
    actual_start_date   DATE COMMENT '实际开始日期',
    actual_end_date     DATE COMMENT '实际结束日期',
    duration            INT COMMENT '工期(天)',
    progress            DECIMAL(10,4) DEFAULT 0 COMMENT '进度(%)',
    weight              DECIMAL(10,4) DEFAULT 0 COMMENT '权重',
    dependency          VARCHAR(200) COMMENT '前置任务依赖',
    responsible_id       BIGINT COMMENT '责任人ID',
    responsible_name    VARCHAR(50) COMMENT '责任人姓名',
    hidden_work         TINYINT COMMENT '是否隐蔽工程: 0-否 1-是',
    hidden_accept_status TINYINT COMMENT '隐蔽工程验收状态: 1-待验收 2-已验收 3-不通过',
    sort_order          INT DEFAULT 0 COMMENT '排序号',
    is_locked           TINYINT DEFAULT 0 COMMENT '是否锁定: 0-否 1-是',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_gantt_id (gantt_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='甘特任务节点表';

-- ----------------------------
-- 49. 进度填报记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_progress_report;
CREATE TABLE biz_progress_report (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    report_no           VARCHAR(50) NOT NULL COMMENT '进度报告编号',
    gantt_id            BIGINT NOT NULL COMMENT '甘特图ID',
    gantt_name          VARCHAR(200) COMMENT '甘特图名称',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    report_date         DATE NOT NULL COMMENT '填报日期',
    overall_progress    DECIMAL(10,4) DEFAULT 0 COMMENT '整体进度(%)',
    work_content        TEXT COMMENT '施工内容',
    problem_desc        TEXT COMMENT '存在问题',
    solution            TEXT COMMENT '解决方案',
    next_plan           TEXT COMMENT '下一步计划',
    reporter_id         BIGINT COMMENT '填报人ID',
    reporter_name       VARCHAR(50) COMMENT '填报人姓名',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-已通过 4-已驳回',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_report_no (report_no),
    INDEX idx_gantt_id (gantt_id),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='进度填报记录表';

-- ----------------------------
-- 50. 进度填报明细表
-- ----------------------------
DROP TABLE IF EXISTS biz_progress_report_item;
CREATE TABLE biz_progress_report_item (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    report_id           BIGINT NOT NULL COMMENT '进度报告ID',
    task_id             BIGINT NOT NULL COMMENT '任务节点ID',
    task_name           VARCHAR(200) COMMENT '任务名称',
    planned_progress    DECIMAL(10,4) DEFAULT 0 COMMENT '计划进度(%)',
    actual_progress     DECIMAL(10,4) DEFAULT 0 COMMENT '实际进度(%)',
    progress_diff       DECIMAL(10,4) DEFAULT 0 COMMENT '进度偏差',
    work_quantity       DECIMAL(18,2) COMMENT '完成工程量',
    unit                VARCHAR(20) COMMENT '单位',
    remark              VARCHAR(500) COMMENT '备注',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='进度填报明细表';

-- ----------------------------
-- 51. 现场签证单
-- ----------------------------
DROP TABLE IF EXISTS biz_site_visa;
CREATE TABLE biz_site_visa (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    visa_no             VARCHAR(50) NOT NULL COMMENT '签证单编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '合同ID',
    visa_type           TINYINT NOT NULL COMMENT '签证类型: 1-费用签证 2-工期签证 3-其他',
    content             TEXT NOT NULL COMMENT '签证内容',
    quantity            DECIMAL(18,2) COMMENT '工程量',
    unit                VARCHAR(20) COMMENT '单位',
    unit_price          DECIMAL(18,2) COMMENT '单价',
    amount              DECIMAL(18,2) DEFAULT 0 COMMENT '金额',
    visa_date           DATE COMMENT '签证日期',
    applicant_id        BIGINT COMMENT '申请人ID',
    applicant_name      VARCHAR(50) COMMENT '申请人姓名',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已通过 5-已驳回',
    file_path           VARCHAR(500) COMMENT '附件路径',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_visa_no (visa_no),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='现场签证单';

-- ----------------------------
-- 52. 甲方变更单
-- ----------------------------
DROP TABLE IF EXISTS biz_change_order;
CREATE TABLE biz_change_order (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    change_no           VARCHAR(50) NOT NULL COMMENT '变更单编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '合同ID',
    change_type         TINYINT NOT NULL COMMENT '变更类型: 1-设计变更 2-现场变更 3-甲方要求 4-其他',
    change_reason       VARCHAR(500) COMMENT '变更原因',
    change_content      TEXT NOT NULL COMMENT '变更内容',
    original_amount    DECIMAL(18,2) DEFAULT 0 COMMENT '原金额',
    change_amount       DECIMAL(18,2) DEFAULT 0 COMMENT '变更金额',
    final_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '最终金额',
    days_change         INT DEFAULT 0 COMMENT '工期变化(天)',
    change_date         DATE COMMENT '变更日期',
    apply_date          DATE COMMENT '申请日期',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已通过 5-已驳回',
    approval_file_path  VARCHAR(500) COMMENT '审批文件路径',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_change_no (change_no),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='甲方变更单';

-- ----------------------------
-- 53. 劳务签证单
-- ----------------------------
DROP TABLE IF EXISTS biz_labor_visa;
CREATE TABLE biz_labor_visa (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    visa_no             VARCHAR(50) NOT NULL COMMENT '签证单编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '劳务合同ID',
    team_name           VARCHAR(100) COMMENT '班组名称',
    visa_type           TINYINT NOT NULL COMMENT '签证类型: 1-点工 2-包工 3-其他',
    work_days           DECIMAL(10,2) COMMENT '工日数',
    daily_rate          DECIMAL(18,2) COMMENT '日工资',
    amount              DECIMAL(18,2) DEFAULT 0 COMMENT '金额',
    work_content        TEXT NOT NULL COMMENT '工作内容',
    visa_date           DATE COMMENT '签证日期',
    applicant_id        BIGINT COMMENT '申请人ID',
    applicant_name      VARCHAR(50) COMMENT '申请人姓名',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-已通过 4-已驳回',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_visa_no (visa_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='劳务签证单';

-- ----------------------------
-- 54. 隐蔽工程记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_hidden_work;
CREATE TABLE biz_hidden_work (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    record_no           VARCHAR(50) NOT NULL COMMENT '隐蔽工程记录编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    gantt_task_id       BIGINT COMMENT '甘特任务节点ID',
    task_name           VARCHAR(200) COMMENT '任务名称',
    work_type           VARCHAR(100) COMMENT '施工类型',
    work_content        TEXT NOT NULL COMMENT '施工内容',
    accept_standard     TEXT COMMENT '验收标准',
    accept_result       TINYINT COMMENT '验收结果: 1-合格 2-不合格 3-待定',
    accept_date         DATE COMMENT '验收日期',
    accept_by           BIGINT COMMENT '验收人ID',
    accept_by_name      VARCHAR(50) COMMENT '验收人姓名',
    file_paths          TEXT COMMENT '附件路径(JSON)',
    remark              VARCHAR(500) COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_record_no (record_no),
    INDEX idx_project_id (project_id),
    INDEX idx_gantt_task_id (gantt_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='隐蔽工程记录表';

-- ----------------------------
-- 55. 竣工验收记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_completion_acceptance;
CREATE TABLE biz_completion_acceptance (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    acceptance_no       VARCHAR(50) NOT NULL COMMENT '验收单编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    acceptance_type     TINYINT NOT NULL COMMENT '验收类型: 1-分项验收 2-阶段验收 3-竣工验收',
    accept_content      TEXT NOT NULL COMMENT '验收内容',
    accept_date         DATE COMMENT '验收日期',
    accept_by           VARCHAR(200) COMMENT '验收参与方',
    accept_result       TINYINT COMMENT '验收结果: 1-通过 2-整改后通过 3-不通过',
    issue_desc          TEXT COMMENT '问题描述',
    rectification_deadline DATE COMMENT '整改期限',
    rectification_result TEXT COMMENT '整改结果',
    file_paths          TEXT COMMENT '附件路径(JSON)',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-待验收 2-验收中 3-已完成',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_acceptance_no (acceptance_no),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='竣工验收记录表';

-- ----------------------------
-- 56. 劳务结算表
-- ----------------------------
DROP TABLE IF EXISTS biz_labor_settlement;
CREATE TABLE biz_labor_settlement (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    settlement_no       VARCHAR(50) NOT NULL COMMENT '结算单编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '劳务合同ID',
    contract_no         VARCHAR(50) COMMENT '合同编号',
    team_name           VARCHAR(100) COMMENT '班组名称',
    total_amount        DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '结算总金额',
    paid_amount         DECIMAL(18,2) DEFAULT 0 COMMENT '已付金额',
    pending_amount      DECIMAL(18,2) DEFAULT 0 COMMENT '待付金额',
    settlement_date     DATE COMMENT '结算日期',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-已通过 4-已驳回',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_settlement_no (settlement_no),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='劳务结算表';