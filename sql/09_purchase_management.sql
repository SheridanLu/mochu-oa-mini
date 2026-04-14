-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：采购管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 30. 采购清单主表
-- ----------------------------
DROP TABLE IF EXISTS biz_purchase_order;
CREATE TABLE biz_purchase_order (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no            VARCHAR(50) NOT NULL COMMENT '采购清单编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '关联合同ID',
    contract_no         VARCHAR(50) COMMENT '关联合同编号',
    source_type         TINYINT COMMENT '来源类型: 1-手工创建 2-合同附件导入',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已审批 5-进行中 6-已完成 7-已驳回 8-已取消',
    total_budget        DECIMAL(18,2) DEFAULT 0 COMMENT '预算总额',
    total_quantity      DECIMAL(18,2) DEFAULT 0 COMMENT '计划总量',
    abnormal_count      INT DEFAULT 0 COMMENT '异常行数',
    submitter_id        BIGINT COMMENT '提交人ID',
    submitter_name      VARCHAR(50) COMMENT '提交人姓名',
    submitted_at        DATETIME COMMENT '提交时间',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_project_id (project_id),
    INDEX idx_contract_id (contract_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购清单主表';

-- ----------------------------
-- 31. 采购清单明细表
-- ----------------------------
DROP TABLE IF EXISTS biz_purchase_order_item;
CREATE TABLE biz_purchase_order_item (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id            BIGINT NOT NULL COMMENT '采购清单ID',
    material_name       VARCHAR(200) NOT NULL COMMENT '物资名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    unit                VARCHAR(20) COMMENT '单位',
    plan_quantity       DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '计划数量',
    budget_price        DECIMAL(18,2) DEFAULT 0 COMMENT '预算单价',
    budget_amount       DECIMAL(18,2) DEFAULT 0 COMMENT '预算合价',
    match_source        TINYINT COMMENT '匹配来源: 1-基准价 2-基础价 3-待询价',
    supplier_id         BIGINT COMMENT '匹配供应商ID',
    supplier_name       VARCHAR(200) COMMENT '匹配供应商名称',
    remark              VARCHAR(500) COMMENT '备注',
    row_status          TINYINT DEFAULT 1 COMMENT '行状态: 1-正常 2-异常',
    row_remark          VARCHAR(200) COMMENT '行异常说明',
    sort_order          INT DEFAULT 0 COMMENT '排序号',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购清单明细表';

-- ----------------------------
-- 32. 采购申请单
-- ----------------------------
DROP TABLE IF EXISTS biz_purchase_apply;
CREATE TABLE biz_purchase_apply (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    apply_no            VARCHAR(50) NOT NULL COMMENT '采购申请编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    purchase_type       TINYINT NOT NULL COMMENT '采购类型: 1-批量采购 2-零星采购',
    supplier_id         BIGINT COMMENT '供应商ID',
    supplier_name       VARCHAR(200) COMMENT '供应商名称',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '采购总金额',
    delivery_date       DATE COMMENT '期望交货日期',
    delivery_place      VARCHAR(200) COMMENT '交货地点',
    purpose             VARCHAR(500) COMMENT '采购用途',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已通过 5-已驳回 6-已下单 7-已完成 8-已取消',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_apply_no (apply_no),
    INDEX idx_project_id (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购申请单';

-- ----------------------------
-- 33. 零星采购累计记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_spot_purchase_record;
CREATE TABLE biz_spot_purchase_record (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '关联合同ID',
    contract_no         VARCHAR(50) COMMENT '关联合同编号',
    contract_total_amount DECIMAL(18,2) DEFAULT 0 COMMENT '合同总额',
    historical_amount   DECIMAL(18,2) DEFAULT 0 COMMENT '历史累计金额',
    current_amount      DECIMAL(18,2) DEFAULT 0 COMMENT '本次申请金额',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '累计总金额',
    limit_ratio         DECIMAL(10,4) DEFAULT 0.015 COMMENT '限额比例(1.5%)',
    limit_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '限额金额',
    is_exceed           TINYINT DEFAULT 0 COMMENT '是否超限: 0-否 1-是',
    exceed_approval_type TINYINT COMMENT '超限审批类型: 1-默认流程 2-超限流程',
    apply_id            BIGINT COMMENT '采购申请ID',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_id (project_id),
    INDEX idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='零星采购累计记录表';

-- ----------------------------
-- 34. 采购价格历史表
-- ----------------------------
DROP TABLE IF EXISTS biz_material_price_history;
CREATE TABLE biz_material_price_history (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    material_name       VARCHAR(200) NOT NULL COMMENT '材料名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    unit                VARCHAR(20) COMMENT '单位',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name       VARCHAR(200) COMMENT '供应商名称',
    price               DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '单价',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    price_with_tax      DECIMAL(18,2) DEFAULT 0 COMMENT '含税单价',
    source_type         TINYINT NOT NULL COMMENT '来源: 1-询价 2-合同 3-基准价确认',
    source_id           BIGINT COMMENT '来源ID',
    source_no           VARCHAR(50) COMMENT '来源单号',
    purchase_time       DATE NOT NULL COMMENT '采购时间',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_material_name (material_name),
    INDEX idx_purchase_time (purchase_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购价格历史表';

-- ----------------------------
-- 35. 询价比价记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_inquiry_comparison;
CREATE TABLE biz_inquiry_comparison (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    comparison_no       VARCHAR(50) NOT NULL COMMENT '比价单编号',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    material_name       VARCHAR(200) NOT NULL COMMENT '材料名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    quantity            DECIMAL(18,2) COMMENT '数量',
    inquiry_count       INT DEFAULT 0 COMMENT '询价供应商数量',
    best_supplier_id    BIGINT COMMENT '最优供应商ID',
    best_supplier_name  VARCHAR(200) COMMENT '最优供应商名称',
    best_price          DECIMAL(18,2) DEFAULT 0 COMMENT '最优单价',
    best_delivery_cycle INT COMMENT '最优交货周期',
    best_score          DECIMAL(5,2) COMMENT '综合评分',
    comparison_result   TINYINT COMMENT '比价结果: 1-已选择 2-待选择 3-已取消',
    report_file_path    VARCHAR(500) COMMENT '比价报告文件路径',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_comparison_no (comparison_no),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='询价比价记录表';

-- ----------------------------
-- 36. 比价明细表
-- ----------------------------
DROP TABLE IF EXISTS biz_inquiry_comparison_item;
CREATE TABLE biz_inquiry_comparison_item (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    comparison_id       BIGINT NOT NULL COMMENT '比价记录ID',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name       VARCHAR(200) COMMENT '供应商名称',
    unit_price          DECIMAL(18,2) DEFAULT 0 COMMENT '单价',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    total_price         DECIMAL(18,2) DEFAULT 0 COMMENT '总价',
    delivery_cycle      INT COMMENT '交货周期(天)',
    quality_score      DECIMAL(5,2) COMMENT '质量评分',
    delivery_score     DECIMAL(5,2) COMMENT '交货评分',
    service_score      DECIMAL(5,2) COMMENT '服务评分',
    price_score        DECIMAL(5,2) COMMENT '价格评分',
    total_score         DECIMAL(5,2) COMMENT '综合评分',
    rank                INT COMMENT '排名',
    is_selected         TINYINT DEFAULT 0 COMMENT '是否选中: 0-否 1-是',
    remark              VARCHAR(500) COMMENT '备注',
    inquiry_date        DATE COMMENT '询价日期',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_comparison_id (comparison_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='比价明细表';