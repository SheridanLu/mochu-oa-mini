-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：财务管理 - 收入管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 1. 收入合同拆分表
-- ----------------------------
DROP TABLE IF EXISTS biz_income_contract_split;
CREATE TABLE biz_income_contract_split (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id     BIGINT NOT NULL COMMENT '收入合同ID',
    gantt_task_id   BIGINT COMMENT '甘特任务节点ID',
    task_name       VARCHAR(200) COMMENT '任务/里程碑名称',
    amount          DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '拆分金额',
    progress_ratio  DECIMAL(10,4) DEFAULT 0 COMMENT '进度比例',
    production_value DECIMAL(18,2) DEFAULT 0 COMMENT '产值',
    sort_order      INT DEFAULT 0 COMMENT '排序号',
    status          TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_by      BIGINT COMMENT '创建人',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    INDEX idx_contract_id (contract_id),
    INDEX idx_gantt_task_id (gantt_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收入合同拆分表';

-- ----------------------------
-- 2. 对账定时配置表
-- ----------------------------
DROP TABLE IF EXISTS biz_reconciliation_setting;
CREATE TABLE biz_reconciliation_setting (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    project_id      BIGINT NOT NULL COMMENT '项目ID',
    cycle_type      TINYINT NOT NULL COMMENT '周期类型: 1-月度 2-季度 3-自定义',
    cycle_day       INT COMMENT '周期日/季度月',
    is_enabled      TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    last_generate_time DATETIME COMMENT '最后生成时间',
    created_by      BIGINT COMMENT '创建人',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对账定时配置表';

-- ----------------------------
-- 3. 对账单主表
-- ----------------------------
DROP TABLE IF EXISTS biz_reconciliation_statement;
CREATE TABLE biz_reconciliation_statement (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    statement_no        VARCHAR(50) NOT NULL COMMENT '对账单编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    contract_id         BIGINT COMMENT '收入合同ID',
    period              VARCHAR(20) NOT NULL COMMENT '对账期间(YYYY-MM)',
    period_start        DATE COMMENT '期间开始日期',
    period_end          DATE COMMENT '期间结束日期',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已完成 5-已驳回',
    contract_amount     DECIMAL(18,2) DEFAULT 0 COMMENT '合同含税金额',
    current_production  DECIMAL(18,2) DEFAULT 0 COMMENT '当期产值',
    accumulated_production DECIMAL(18,2) DEFAULT 0 COMMENT '累计产值',
    current_receipt     DECIMAL(18,2) DEFAULT 0 COMMENT '当期回款',
    accumulated_receipt DECIMAL(18,2) DEFAULT 0 COMMENT '累计回款',
    receivable_balance  DECIMAL(18,2) DEFAULT 0 COMMENT '应收余额',
    difference_amount  DECIMAL(18,2) DEFAULT 0 COMMENT '差异金额',
    difference_ratio    DECIMAL(10,4) DEFAULT 0 COMMENT '差异比例',
    remark              TEXT COMMENT '备注',
    submitted_by        BIGINT COMMENT '提交人',
    submitted_at        DATETIME COMMENT '提交时间',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_statement_no (statement_no),
    INDEX idx_project_id (project_id),
    INDEX idx_contract_id (contract_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对账单主表';