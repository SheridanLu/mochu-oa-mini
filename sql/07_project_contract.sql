-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：项目与合同管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 20. 项目主表
-- ----------------------------
DROP TABLE IF EXISTS biz_project;
CREATE TABLE biz_project (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    project_no          VARCHAR(50) NOT NULL COMMENT '项目编号',
    project_name        VARCHAR(200) NOT NULL COMMENT '项目名称',
    project_type        TINYINT NOT NULL COMMENT '项目类型: 1-虚拟项目 2-实体项目',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-筹备 2-进行中 3-暂停 4-中止 5-完工 6-结算',
    province            VARCHAR(50) COMMENT '省份',
    city                VARCHAR(50) COMMENT '城市',
    detailed_address    VARCHAR(500) COMMENT '详细地址',
    owner_name          VARCHAR(100) COMMENT '甲方/业主名称',
    owner_contact       VARCHAR(50) COMMENT '甲方联系人',
    owner_phone         VARCHAR(20) COMMENT '甲方联系电话',
    contract_amount     DECIMAL(18,2) DEFAULT 0 COMMENT '合同金额',
    start_date          DATE COMMENT '计划开工日期',
    end_date            COMMENT '计划完工日期',
    actual_start_date   DATE COMMENT '实际开工日期',
    actual_end_date     DATE COMMENT '实际完工日期',
    project_manager_id  BIGINT COMMENT '项目经理ID',
    project_manager_name VARCHAR(50) COMMENT '项目经理姓名',
    department_id       BIGINT COMMENT '所属部门ID',
    department_name     VARCHAR(100) COMMENT '所属部门名称',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_project_no (project_no),
    INDEX idx_status (status),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目主表';

-- ----------------------------
-- 21. 收入合同表
-- ----------------------------
DROP TABLE IF EXISTS biz_income_contract;
CREATE TABLE biz_income_contract (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_no         VARCHAR(50) NOT NULL COMMENT '合同编号',
    contract_name       VARCHAR(200) NOT NULL COMMENT '合同名称',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_type       TINYINT NOT NULL COMMENT '合同类型: 1-施工合同 2-设计合同 3-咨询合同 4-其他',
    sign_date           DATE COMMENT '签订日期',
    start_date          DATE COMMENT '合同开始日期',
    end_date            DATE COMMENT '合同结束日期',
    total_amount        DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '合同总金额(不含税)',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    tax_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
    total_amount_with_tax DECIMAL(18,2) DEFAULT 0 COMMENT '合同含税金额',
    payment_type        TINYINT COMMENT '付款方式: 1-按进度 2-按节点 3-一次性 4-其他',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已签订 5-进行中 6-已变更 7-已终止 8-已结算',
    party_a             VARCHAR(200) COMMENT '甲方(委托方)',
    party_b             VARCHAR(200) COMMENT '乙方(承包方)',
    signed_file_path    VARCHAR(500) COMMENT '合同扫描件路径',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_contract_no (contract_no),
    INDEX idx_project_id (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收入合同表';

-- ----------------------------
-- 22. 支出合同表
-- ----------------------------
DROP TABLE IF EXISTS biz_expense_contract;
CREATE TABLE biz_expense_contract (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_no         VARCHAR(50) NOT NULL COMMENT '合同编号',
    contract_name       VARCHAR(200) NOT NULL COMMENT '合同名称',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_type       TINYINT NOT NULL COMMENT '合同类型: 1-材料采购 2-设备租赁 3-劳务分包 4-专业分包 5-其他',
    supplier_id         BIGINT COMMENT '供应商ID',
    supplier_name       VARCHAR(200) COMMENT '供应商名称',
    sign_date           DATE COMMENT '签订日期',
    start_date          DATE COMMENT '合同开始日期',
    end_date            DATE COMMENT '合同结束日期',
    total_amount        DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '合同总金额(不含税)',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    tax_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
    total_amount_with_tax DECIMAL(18,2) DEFAULT 0 COMMENT '合同含税金额',
    payment_type        TINYINT COMMENT '付款方式: 1-按进度 2-按节点 3-一次性 4-其他',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已签订 5-进行中 6-已变更 7-已终止 8-已结算',
    signed_file_path    VARCHAR(500) COMMENT '合同扫描件路径',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_contract_no (contract_no),
    INDEX idx_project_id (project_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支出合同表';

-- ----------------------------
-- 23. 合同付款条款表
-- ----------------------------
DROP TABLE IF EXISTS biz_contract_payment_term;
CREATE TABLE biz_contract_payment_term (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id         BIGINT NOT NULL COMMENT '合同ID',
    term_name           VARCHAR(100) NOT NULL COMMENT '付款条款名称',
    payment_condition   VARCHAR(500) COMMENT '付款条件描述',
    payment_ratio       DECIMAL(10,4) DEFAULT 0 COMMENT '付款比例(%)',
    planned_amount      DECIMAL(18,2) DEFAULT 0 COMMENT '计划付款金额',
    trigger_condition   VARCHAR(500) COMMENT '触发条件',
    sort_order          INT DEFAULT 0 COMMENT '排序号',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同付款条款表';

-- ----------------------------
-- 24. 合同附件表
-- ----------------------------
DROP TABLE IF EXISTS biz_contract_attachment;
CREATE TABLE biz_contract_attachment (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id         BIGINT NOT NULL COMMENT '合同ID',
    file_name           VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path           VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size           BIGINT COMMENT '文件大小(字节)',
    file_type           VARCHAR(50) COMMENT '文件类型',
    attachment_type    TINYINT COMMENT '附件类型: 1-合同扫描件 2-清单 3-附件',
    uploader_id         BIGINT COMMENT '上传人ID',
    uploader_name       VARCHAR(50) COMMENT '上传人姓名',
    upload_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同附件表';