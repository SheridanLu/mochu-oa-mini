-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：财务管理 - 发票与预算管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 17. 发票表
-- ----------------------------
DROP TABLE IF EXISTS biz_invoice;
CREATE TABLE biz_invoice (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    invoice_no          VARCHAR(50) NOT NULL COMMENT '发票号',
    invoice_type        TINYINT NOT NULL COMMENT '发票类型: 1-增值税专用发票 2-增值税普通发票 3-电子发票 4-其他',
    invoice_code        VARCHAR(20) COMMENT '发票代码',
    amount              DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '发票金额',
    tax_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '价税合计',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    opened_date         DATE COMMENT '开票日期',
    seller_name         VARCHAR(200) COMMENT '销售方名称',
    seller_tax_no       VARCHAR(50) COMMENT '销售方税号',
    buyer_name          VARCHAR(200) COMMENT '购买方名称',
    buyer_tax_no        VARCHAR(50) COMMENT '购买方税号',
    project_id          BIGINT COMMENT '项目ID',
    contract_id         BIGINT COMMENT '合同ID',
    file_path           VARCHAR(500) COMMENT '发票文件路径',
    is_verified         TINYINT DEFAULT 0 COMMENT '是否认证: 0-未认证 1-已认证',
    verified_date       DATE COMMENT '认证日期',
    remark              VARCHAR(500) COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_invoice_no (invoice_no),
    INDEX idx_invoice_type (invoice_type),
    INDEX idx_project_id (project_id),
    INDEX idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='发票表';

-- ----------------------------
-- 18. 部门预算表
-- ----------------------------
DROP TABLE IF EXISTS biz_budget_allocation;
CREATE TABLE biz_budget_allocation (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    department_id   BIGINT NOT NULL COMMENT '部门ID',
    department_name VARCHAR(100) COMMENT '部门名称',
    project_id      BIGINT COMMENT '项目ID',
    project_name    VARCHAR(200) COMMENT '项目名称',
    year            INT NOT NULL COMMENT '预算年度',
    month           INT COMMENT '预算月份',
    budget_type     TINYINT NOT NULL COMMENT '预算类型: 1-年度预算 2-月度预算',
    amount          DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '预算金额',
    used_amount     DECIMAL(18,2) DEFAULT 0 COMMENT '已使用金额',
    available_amount DECIMAL(18,2) DEFAULT 0 COMMENT '可用金额',
    usage_rate      DECIMAL(10,4) DEFAULT 0 COMMENT '使用率',
    status          TINYINT DEFAULT 1 COMMENT '状态: 1-生效 2-调整中 3-已冻结',
    created_by      BIGINT COMMENT '创建人',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    INDEX idx_department_project (department_id, project_id, year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门预算表';

-- ----------------------------
-- 19. 成本归集表
-- ----------------------------
DROP TABLE IF EXISTS biz_cost_collection;
CREATE TABLE biz_cost_collection (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    cost_category       TINYINT NOT NULL COMMENT '成本类别: 1-材料费 2-人工费 3-机械费 4-管理费 5-其他',
    cost_category_name  VARCHAR(50) COMMENT '成本类别名称',
    amount              DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '金额',
    source_type         TINYINT NOT NULL COMMENT '来源类型: 1-报销 2-采购 3-付款 4-工资 5-其他',
    source_id           BIGINT COMMENT '来源ID',
    source_no           VARCHAR(50) COMMENT '来源单据号',
    collected_date      DATE NOT NULL COMMENT '归集日期',
    period              VARCHAR(7) COMMENT '归属期间(YYYY-MM)',
    remark              VARCHAR(500) COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    INDEX idx_project_id (project_id),
    INDEX idx_cost_category (cost_category),
    INDEX idx_collected_date (collected_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='成本归集表';

-- =============================================
-- 汇总：财务管理模块共19张表
-- =============================================
-- 01_income_management.sql (3表) - 收入管理
-- 02_statement_difference.sql (2表) - 对账差异
-- 03_payment_supervision.sql (4表) - 回款督办
-- 04_payment_plan.sql (3表) - 付款计划
-- 05_expense_management.sql (4表) - 日常报销
-- 06_invoice_budget.sql (3表) - 发票与预算
-- =============================================