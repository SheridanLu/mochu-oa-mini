-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：财务管理 - 付款计划管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 10. 支出合同付款计划表
-- ----------------------------
DROP TABLE IF EXISTS biz_contract_payment_plan;
CREATE TABLE biz_contract_payment_plan (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id             BIGINT NOT NULL COMMENT '支出合同ID',
    payment_term_id         BIGINT COMMENT '付款条款ID',
    payment_term_name       VARCHAR(100) COMMENT '付款条款名称',
    planned_payment_date    DATE COMMENT '计划付款日期',
    planned_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '计划付款金额',
    payment_condition       VARCHAR(500) COMMENT '付款条件说明',
    sort_order              INT DEFAULT 0 COMMENT '排序号',
    status                  TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_by              BIGINT COMMENT '创建人',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by              BIGINT COMMENT '更新人',
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_contract_id (contract_id),
    INDEX idx_payment_term_id (payment_term_id),
    INDEX idx_planned_payment_date (planned_payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支出合同付款计划表';

-- ----------------------------
-- 11. 付款申请主表
-- ----------------------------
DROP TABLE IF EXISTS biz_payment_apply;
CREATE TABLE biz_payment_apply (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    apply_no            VARCHAR(50) NOT NULL COMMENT '付款申请编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    contract_id         BIGINT COMMENT '支出合同ID',
    category            TINYINT NOT NULL COMMENT '付款类别: 1-材料款 2-设备款 3-人工费 4-其他',
    supplier_id         BIGINT COMMENT '供应商ID',
    supplier_name       VARCHAR(100) COMMENT '供应商名称',
    amount              DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '申请付款金额',
    tax_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '含税金额',
    payment_method      TINYINT COMMENT '付款方式: 1-银行转账 2-现金 3-承兑汇票',
    bank_name           VARCHAR(100) COMMENT '收款银行',
    bank_account        VARCHAR(50) COMMENT '收款账号',
    purpose             VARCHAR(200) COMMENT '付款用途',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已通过 5-已驳回 6-已支付',
    submitted_by        BIGINT COMMENT '提交人',
    submitted_at        DATETIME COMMENT '提交时间',
    paid_by             BIGINT COMMENT '付款人',
    paid_at             DATETIME COMMENT '付款时间',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_apply_no (apply_no),
    INDEX idx_project_contract (project_id, contract_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付款申请主表';

-- ----------------------------
-- 12. 付款申请明细表
-- ----------------------------
DROP TABLE IF EXISTS biz_payment_apply_detail;
CREATE TABLE biz_payment_apply_detail (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    apply_id        BIGINT NOT NULL COMMENT '付款申请ID',
    contract_id     BIGINT COMMENT '关联合同ID',
    invoice_no      VARCHAR(50) COMMENT '发票号',
    invoice_amount  DECIMAL(18,2) DEFAULT 0 COMMENT '发票金额',
    apply_amount    DECIMAL(18,2) DEFAULT 0 COMMENT '申请金额',
    remark          VARCHAR(200) COMMENT '备注',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_apply_id (apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付款申请明细表';