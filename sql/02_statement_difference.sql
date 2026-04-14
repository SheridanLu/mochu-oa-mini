-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：财务管理 - 对账差异管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 4. 对账附件表
-- ----------------------------
DROP TABLE IF EXISTS biz_statement_attachment;
CREATE TABLE biz_statement_attachment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    statement_id    BIGINT NOT NULL COMMENT '对账单ID',
    file_name       VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path       VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size       BIGINT COMMENT '文件大小(字节)',
    file_type       VARCHAR(50) COMMENT '文件类型',
    uploader_id     BIGINT COMMENT '上传人ID',
    uploader_name   VARCHAR(50) COMMENT '上传人姓名',
    upload_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_statement_id (statement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对账附件表';

-- ----------------------------
-- 5. 差异记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_statement_difference;
CREATE TABLE biz_statement_difference (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    statement_id            BIGINT NOT NULL COMMENT '本期对账单ID',
    compare_statement_id    BIGINT COMMENT '对比对账单ID(上期)',
    difference_type         TINYINT COMMENT '差异类型: 1-金额差异 2-数量差异 3-综合差异',
    difference_amount       DECIMAL(18,2) DEFAULT 0 COMMENT '差异金额',
    difference_ratio        DECIMAL(10,4) DEFAULT 0 COMMENT '差异比例',
    project_diff_amount     DECIMAL(18,2) DEFAULT 0 COMMENT '项目总额差异',
    receivable_diff_amount  DECIMAL(18,2) DEFAULT 0 COMMENT '应收余额差异',
    receipt_diff_amount     DECIMAL(18,2) DEFAULT 0 COMMENT '回款差异',
    remark                  TEXT COMMENT '差异说明',
    created_by              BIGINT COMMENT '创建人',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by              BIGINT COMMENT '更新人',
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_statement_id (statement_id),
    INDEX idx_compare_statement_id (compare_statement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='差异记录表';