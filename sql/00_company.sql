-- 公司信息表
CREATE TABLE IF NOT EXISTS biz_company (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    company_name VARCHAR(200) NOT NULL COMMENT '公司名称',
    credit_code VARCHAR(50) COMMENT '统一社会信用代码',
    address VARCHAR(500) COMMENT '公司地址',
    contact_name VARCHAR(100) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    bank_name VARCHAR(100) COMMENT '开户银行',
    bank_account VARCHAR(50) COMMENT '银行账号',
    tax_no VARCHAR(50) COMMENT '纳税人识别号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_by BIGINT DEFAULT 0 COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT DEFAULT 0 COMMENT '更新人ID',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_company_name (company_name),
    INDEX idx_credit_code (credit_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司信息表';