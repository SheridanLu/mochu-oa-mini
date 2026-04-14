-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：物资管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 37. 物资主表
-- ----------------------------
DROP TABLE IF EXISTS biz_material;
CREATE TABLE biz_material (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    material_no         VARCHAR(50) NOT NULL COMMENT '物资编号',
    material_name       VARCHAR(200) NOT NULL COMMENT '物资名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    category_id         BIGINT COMMENT '物资分类ID',
    category_name       VARCHAR(100) COMMENT '物资分类名称',
    unit                VARCHAR(20) COMMENT '计量单位',
    weight              DECIMAL(10,2) COMMENT '重量(kg)',
    length              DECIMAL(10,2) COMMENT '长度(m)',
    width               DECIMAL(10,2) COMMENT '宽度(m)',
    height              DECIMAL(10,2) COMMENT '高度(m)',
    standard            VARCHAR(200) COMMENT '执行标准',
    safe_stock          DECIMAL(18,2) DEFAULT 0 COMMENT '安全库存',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_material_no (material_no),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物资主表';

-- ----------------------------
-- 38. 物资分类表
-- ----------------------------
DROP TABLE IF EXISTS biz_material_category;
CREATE TABLE biz_material_category (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_name       VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id          BIGINT DEFAULT 0 COMMENT '父分类ID',
    level               INT DEFAULT 1 COMMENT '层级',
    sort_order          INT DEFAULT 0 COMMENT '排序号',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物资分类表';

-- ----------------------------
-- 39. 入库单主表
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse_entry;
CREATE TABLE biz_warehouse_entry (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entry_no            VARCHAR(50) NOT NULL COMMENT '入库单编号',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    supplier_id         BIGINT COMMENT '供应商ID',
    supplier_name       VARCHAR(200) COMMENT '供应商名称',
    contract_id         BIGINT COMMENT '合同ID',
    entry_type          TINYINT NOT NULL COMMENT '入库类型: 1-采购入库 2-退货入库 3-调拨入库 4-其他',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '入库总金额',
    total_quantity      DECIMAL(18,2) DEFAULT 0 COMMENT '入库总数量',
    warehouse_id        BIGINT COMMENT '仓库ID',
    warehouse_name      VARCHAR(100) COMMENT '仓库名称',
    entry_date          DATE COMMENT '入库日期',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已完成 5-已驳回',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_entry_no (entry_no),
    INDEX idx_project_id (project_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入库单主表';

-- ----------------------------
-- 40. 入库单明细表
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse_entry_item;
CREATE TABLE biz_warehouse_entry_item (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entry_id            BIGINT NOT NULL COMMENT '入库单ID',
    material_id         BIGINT NOT NULL COMMENT '物资ID',
    material_no         VARCHAR(50) COMMENT '物资编号',
    material_name       VARCHAR(200) COMMENT '物资名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    unit                VARCHAR(20) COMMENT '单位',
    quantity            DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '入库数量',
    unit_price          DECIMAL(18,2) DEFAULT 0 COMMENT '单价',
    total_price         DECIMAL(18,2) DEFAULT 0 COMMENT '总价',
    batch_no            VARCHAR(50) COMMENT '批次号',
    production_date     DATE COMMENT '生产日期',
    expiry_date         DATE COMMENT '有效期至',
    remark              VARCHAR(500) COMMENT '备注',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_entry_id (entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入库单明细表';

-- ----------------------------
-- 41. 出库单主表
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse_out;
CREATE TABLE biz_warehouse_out (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    out_no              VARCHAR(50) NOT NULL COMMENT '出库单编号',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    contract_id         BIGINT COMMENT '合同ID',
    contract_no         VARCHAR(50) COMMENT '合同编号',
    out_type            TINYINT NOT NULL COMMENT '出库类型: 1-施工领用 2-跨项目调拨 3-退货 4-其他',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '出库总金额',
    total_quantity      DECIMAL(18,2) DEFAULT 0 COMMENT '出库总数量',
    warehouse_id        BIGINT COMMENT '仓库ID',
    warehouse_name      VARCHAR(100) COMMENT '仓库名称',
    out_date            DATE COMMENT '出库日期',
    receiver_id         BIGINT COMMENT '领料人ID',
    receiver_name       VARCHAR(50) COMMENT '领料人姓名',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已完成 5-已驳回',
    is_cross_project    TINYINT DEFAULT 0 COMMENT '是否跨项目: 0-否 1-是',
    cross_approval_status TINYINT COMMENT '跨项目审批状态: 1-待审批 2-已通过 3-已驳回',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_out_no (out_no),
    INDEX idx_project_id (project_id),
    INDEX idx_contract_id (contract_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出库单主表';

-- ----------------------------
-- 42. 出库单明细表
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse_out_item;
CREATE TABLE biz_warehouse_out_item (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    out_id              BIGINT NOT NULL COMMENT '出库单ID',
    material_id         BIGINT NOT NULL COMMENT '物资ID',
    material_no         VARCHAR(50) COMMENT '物资编号',
    material_name       VARCHAR(200) COMMENT '物资名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    unit                VARCHAR(20) COMMENT '单位',
    quantity            DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '出库数量',
    unit_price          DECIMAL(18,2) DEFAULT 0 COMMENT '单价',
    total_price         DECIMAL(18,2) DEFAULT 0 COMMENT '总价',
    batch_no            VARCHAR(50) COMMENT '批次号',
    remark              VARCHAR(500) COMMENT '备注',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_out_id (out_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出库单明细表';

-- ----------------------------
-- 43. 仓库库存表
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse_stock;
CREATE TABLE biz_warehouse_stock (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    warehouse_id        BIGINT NOT NULL COMMENT '仓库ID',
    warehouse_name      VARCHAR(100) COMMENT '仓库名称',
    material_id         BIGINT NOT NULL COMMENT '物资ID',
    material_no         VARCHAR(50) COMMENT '物资编号',
    material_name       VARCHAR(200) COMMENT '物资名称',
    spec_model          VARCHAR(100) COMMENT '规格型号',
    brand               VARCHAR(100) COMMENT '品牌',
    unit                VARCHAR(20) COMMENT '单位',
    quantity            DECIMAL(18,2) DEFAULT 0 COMMENT '库存数量',
    available_quantity  DECIMAL(18,2) DEFAULT 0 COMMENT '可用数量',
    locked_quantity     DECIMAL(18,2) DEFAULT 0 COMMENT '锁定数量',
    unit_price          DECIMAL(18,2) DEFAULT 0 COMMENT '单价',
    total_price         DECIMAL(18,2) DEFAULT 0 COMMENT '总价',
    batch_no            VARCHAR(50) COMMENT '批次号',
    last_entry_date     DATE COMMENT '最后入库日期',
    last_out_date       DATE COMMENT '最后出库日期',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_warehouse_material (warehouse_id, material_id, batch_no),
    INDEX idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仓库库存表';

-- ----------------------------
-- 44. 仓库表
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse;
CREATE TABLE biz_warehouse (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    warehouse_no        VARCHAR(50) NOT NULL COMMENT '仓库编号',
    warehouse_name      VARCHAR(100) NOT NULL COMMENT '仓库名称',
    warehouse_type      TINYINT COMMENT '仓库类型: 1-材料仓库 2-设备仓库 3-虚拟仓库',
    province            VARCHAR(50) COMMENT '省份',
    city                VARCHAR(50) COMMENT '城市',
    detailed_address    VARCHAR(500) COMMENT '详细地址',
    manager_id          BIGINT COMMENT '仓库管理员ID',
    manager_name        VARCHAR(50) COMMENT '仓库管理员姓名',
    capacity            DECIMAL(18,2) COMMENT '容量(立方米)',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_warehouse_no (warehouse_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='仓库表';

-- ----------------------------
-- 45. 退库单
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse_return;
CREATE TABLE biz_warehouse_return (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    return_no           VARCHAR(50) NOT NULL COMMENT '退库单编号',
    out_id              BIGINT COMMENT '原出库单ID',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    return_type         TINYINT NOT NULL COMMENT '退库类型: 1-未使用退库 2-损坏退库 3-其他',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '退库总金额',
    total_quantity      DECIMAL(18,2) DEFAULT 0 COMMENT '退库总数量',
    warehouse_id        BIGINT COMMENT '目标仓库ID',
    warehouse_name      VARCHAR(100) COMMENT '目标仓库名称',
    return_date         DATE COMMENT '退库日期',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-已完成 4-已驳回',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_return_no (return_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退库单';

-- ----------------------------
-- 46. 调拨单
-- ----------------------------
DROP TABLE IF EXISTS biz_warehouse_transfer;
CREATE TABLE biz_warehouse_transfer (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    transfer_no         VARCHAR(50) NOT NULL COMMENT '调拨单编号',
    from_warehouse_id   BIGINT NOT NULL COMMENT '调出仓库ID',
    from_warehouse_name VARCHAR(100) COMMENT '调出仓库名称',
    to_warehouse_id    BIGINT NOT NULL COMMENT '调入仓库ID',
    to_warehouse_name  VARCHAR(100) COMMENT '调入仓库名称',
    project_id          BIGINT COMMENT '项目ID',
    project_name        VARCHAR(200) COMMENT '项目名称',
    total_amount        DECIMAL(18,2) DEFAULT 0 COMMENT '调拨总金额',
    total_quantity      DECIMAL(18,2) DEFAULT 0 COMMENT '调拨总数量',
    transfer_date       DATE COMMENT '调拨日期',
    status              TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待审批 3-审批中 4-已完成 5-已驳回',
    approval_status     TINYINT COMMENT '审批状态',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_transfer_no (transfer_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='调拨单';