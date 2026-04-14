-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：供应商管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 25. 供应商主表
-- ----------------------------
DROP TABLE IF EXISTS biz_supplier;
CREATE TABLE biz_supplier (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    supplier_no         VARCHAR(50) NOT NULL COMMENT '供应商编号',
    supplier_name       VARCHAR(200) NOT NULL COMMENT '供应商名称',
    supplier_type       TINYINT NOT NULL COMMENT '供应商类型: 1-材料供应商 2-设备供应商 3-劳务供应商 4-专业分包商 5-其他',
    category            VARCHAR(100) COMMENT '主营品类',
    contact_name        VARCHAR(50) COMMENT '联系人',
    contact_phone       VARCHAR(20) COMMENT '联系电话',
    contact_email       VARCHAR(100) COMMENT '邮箱',
    province            VARCHAR(50) COMMENT '省份',
    city                VARCHAR(50) COMMENT '城市',
    detailed_address    VARCHAR(500) COMMENT '详细地址',
    bank_name           VARCHAR(100) COMMENT '开户银行',
    bank_account        VARCHAR(50) COMMENT '银行账号',
    tax_no              VARCHAR(50) COMMENT '税号',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-正常 2-禁用 3-黑名单',
    rating              TINYINT COMMENT '评级: 1-优秀 2-良好 3-一般 4-较差',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_supplier_no (supplier_no),
    INDEX idx_supplier_type (supplier_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商主表';

-- ----------------------------
-- 26. 供应商资质表
-- ----------------------------
DROP TABLE IF EXISTS biz_supplier_qualification;
CREATE TABLE biz_supplier_qualification (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    qualification_type  TINYINT NOT NULL COMMENT '资质类型: 1-营业执照 2-税务登记证 3-安全生产许可证 4-资质证书 5-其他',
    qualification_name  VARCHAR(200) COMMENT '资质名称',
    qualification_no    VARCHAR(100) COMMENT '资质编号',
    issue_date          DATE COMMENT '发证日期',
    expire_date         DATE COMMENT '到期日期',
    file_path           VARCHAR(500) COMMENT '资质文件路径',
    is_valid            TINYINT DEFAULT 1 COMMENT '是否有效: 0-无效 1-有效',
    remark              VARCHAR(500) COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商资质表';

-- ----------------------------
-- 27. 供应商评价记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_supplier_evaluation;
CREATE TABLE biz_supplier_evaluation (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    project_id          BIGINT COMMENT '项目ID',
    order_id            BIGINT COMMENT '订单/合同ID',
    quality_score       DECIMAL(5,2) COMMENT '质量评分(1-100)',
    delivery_score      DECIMAL(5,2) COMMENT '交货评分(1-100)',
    service_score       DECIMAL(5,2) COMMENT '服务评分(1-100)',
    price_score         DECIMAL(5,2) COMMENT '价格评分(1-100)',
    total_score         DECIMAL(5,2) COMMENT '综合评分',
    evaluation_content  TEXT COMMENT '评价内容',
    evaluator_id        BIGINT COMMENT '评价人ID',
    evaluator_name      VARCHAR(50) COMMENT '评价人姓名',
    evaluation_date     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商评价记录表';

-- ----------------------------
-- 28. 供应商价格等级表（基准价/基础库）
-- ----------------------------
DROP TABLE IF EXISTS biz_supplier_price_level;
CREATE TABLE biz_supplier_price_level (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name       VARCHAR(200) COMMENT '供应商名称',
    material_name       VARCHAR(200) NOT NULL COMMENT '材料/产品名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    unit                VARCHAR(20) COMMENT '单位',
    price               DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '单价',
    price_type          TINYINT NOT NULL COMMENT '价格类型: 1-基准价 2-基础价',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    price_with_tax      DECIMAL(18,2) DEFAULT 0 COMMENT '含税单价',
    valid_from          DATE COMMENT '生效开始日期',
    valid_to            DATE COMMENT '生效结束日期',
    source_type         TINYINT COMMENT '来源: 1-询价 2-合同 3-历史交易',
    source_id           BIGINT COMMENT '来源ID',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-有效 0-无效',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_price_type (price_type),
    INDEX idx_material_name (material_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商价格等级表';

-- ----------------------------
-- 29. 询价记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_inquiry_record;
CREATE TABLE biz_inquiry_record (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    inquiry_no          VARCHAR(50) NOT NULL COMMENT '询价单编号',
    material_name       VARCHAR(200) NOT NULL COMMENT '材料名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    unit                VARCHAR(20) COMMENT '单位',
    quantity            DECIMAL(18,2) COMMENT '询价数量',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name       VARCHAR(200) COMMENT '供应商名称',
    unit_price          DECIMAL(18,2) DEFAULT 0 COMMENT '单价',
    tax_rate            DECIMAL(10,4) DEFAULT 0 COMMENT '税率',
    total_price         DECIMAL(18,2) DEFAULT 0 COMMENT '总价',
    delivery_cycle      INT COMMENT '交货周期(天)',
    inquiry_date        DATE COMMENT '询价日期',
    expected_arrival    DATE COMMENT '预计到货日期',
    remark              VARCHAR(500) COMMENT '备注',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-询价中 2-已报价 3-已确认 4-已取消',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_inquiry_no (inquiry_no),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='询价记录表';