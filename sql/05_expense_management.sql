-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：财务管理 - 日常报销管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 13. 报销单主表
-- ----------------------------
DROP TABLE IF EXISTS biz_expense_report;
CREATE TABLE biz_expense_report (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    report_no           VARCHAR(50) NOT NULL COMMENT '报销单编号',
    reporter_id         BIGINT NOT NULL COMMENT '报销人ID',
    reporter_name       VARCHAR(50) COMMENT '报销人姓名',
    department_id       BIGINT COMMENT '部门ID',
    department_name     VARCHAR(100) COMMENT '部门名称',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '合同ID',
    contract_no         VARCHAR(50) COMMENT '合同编号',
    total_amount        DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '报销总金额',
    tax_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
    net_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '不含税金额',
    expense_category    TINYINT COMMENT '费用类别: 1-差旅费 2-招待费 3-办公费 4-交通费 5-其他',
    expense_date        DATE COMMENT '费用发生日期',
    purpose             VARCHAR(500) COMMENT '费用用途',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已通过 5-已驳回 6-已支付',
    submitted_by        BIGINT COMMENT '提交人',
    submitted_at        DATETIME COMMENT '提交时间',
    paid_by             BIGINT COMMENT '付款人',
    paid_at             DATETIME COMMENT '付款时间',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_report_no (report_no),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_department_id (department_id),
    INDEX idx_project_id (project_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报销单主表';

-- ----------------------------
-- 14. 报销明细表
-- ----------------------------
DROP TABLE IF EXISTS biz_expense_report_detail;
CREATE TABLE biz_expense_report_detail (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    report_id           BIGINT NOT NULL COMMENT '报销单ID',
    expense_category    TINYINT NOT NULL COMMENT '费用类别: 1-差旅费 2-招待费 3-办公费 4-交通费 5-其他',
    item_name           VARCHAR(200) COMMENT '费用项目名称',
    amount              DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '金额',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    tax_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
    invoice_no          VARCHAR(50) COMMENT '发票号',
    invoice_date        DATE COMMENT '发票日期',
    description         VARCHAR(500) COMMENT '说明',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报销明细表';

-- ----------------------------
-- 15. 报销附件表
-- ----------------------------
DROP TABLE IF EXISTS biz_expense_report_attachment;
CREATE TABLE biz_expense_report_attachment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    report_id       BIGINT NOT NULL COMMENT '报销单ID',
    file_name       VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path       VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size       BIGINT COMMENT '文件大小(字节)',
    file_type       VARCHAR(50) COMMENT '文件类型',
    uploader_id     BIGINT COMMENT '上传人ID',
    uploader_name   VARCHAR(50) COMMENT '上传人姓名',
    upload_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报销附件表';

-- ----------------------------
-- 16. 报销审批记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_expense_report_approval;
CREATE TABLE biz_expense_report_approval (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    report_id       BIGINT NOT NULL COMMENT '报销单ID',
    approver_id     BIGINT NOT NULL COMMENT '审批人ID',
    approver_name   VARCHAR(50) COMMENT '审批人姓名',
    approver_role   VARCHAR(50) COMMENT '审批角色',
    action          TINYINT NOT NULL COMMENT '审批动作: 1-同意 2-驳回 3-加签',
    opinion         TEXT COMMENT '审批意见',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_report_id (report_id),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报销审批记录表';