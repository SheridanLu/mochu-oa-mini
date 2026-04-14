-- =============================================
-- MOCHU-OA V3.0 数据库表结构
-- 模块：财务管理 - 回款督办管理
-- 创建时间：2026-04-13
-- =============================================

-- ----------------------------
-- 6. 回款督办计划表
-- ----------------------------
DROP TABLE IF EXISTS biz_payment_supervision_plan;
CREATE TABLE biz_payment_supervision_plan (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    plan_no             VARCHAR(50) NOT NULL COMMENT '督办计划编号',
    project_id          BIGINT NOT NULL COMMENT '项目ID',
    contract_id         BIGINT COMMENT '收入合同ID',
    gantt_node          VARCHAR(200) COMMENT '甘特节点名称',
   应付金额             DECIMAL(18,2) DEFAULT 0 COMMENT '应付金额',
    paid_amount         DECIMAL(18,2) DEFAULT 0 COMMENT '已回款金额',
    gap_amount          DECIMAL(18,2) DEFAULT 0 COMMENT '回款缺口',
    completion_rate     DECIMAL(10,4) DEFAULT 0 COMMENT '完成率',
    priority            TINYINT DEFAULT 2 COMMENT '优先级: 1-高 2-中 3-低',
    overdue_days        INT DEFAULT 0 COMMENT '超期天数',
    supervision_status  TINYINT DEFAULT 1 COMMENT '督办状态: 1-待接收 2-督办中 3-已完成 4-已终止',
    approval_status     TINYINT DEFAULT 1 COMMENT '审批状态: 1-草稿 2-待审批 3-审批中 4-已通过 5-已驳回',
    responsible_id      BIGINT COMMENT '责任人ID',
    responsible_name    VARCHAR(50) COMMENT '责任人姓名',
    deadline            DATE COMMENT '建议完成期限',
    remark              TEXT COMMENT '备注',
    created_by          BIGINT COMMENT '创建人',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          BIGINT COMMENT '更新人',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE INDEX uk_plan_no (plan_no),
    INDEX idx_project_contract (project_id, contract_id),
    INDEX idx_supervision_status (supervision_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='回款督办计划表';

-- ----------------------------
-- 7. 督办审批记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_payment_supervision_approval;
CREATE TABLE biz_payment_supervision_approval (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    plan_id         BIGINT NOT NULL COMMENT '督办计划ID',
    approver_id     BIGINT NOT NULL COMMENT '审批人ID',
    approver_name   VARCHAR(50) COMMENT '审批人姓名',
    action          TINYINT NOT NULL COMMENT '审批动作: 1-同意 2-驳回 3-加签',
    opinion         TEXT COMMENT '审批意见',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_plan_id (plan_id),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='督办审批记录表';

-- ----------------------------
-- 8. 执行反馈表
-- ----------------------------
DROP TABLE IF EXISTS biz_payment_supervision_feedback;
CREATE TABLE biz_payment_supervision_feedback (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    plan_id                 BIGINT NOT NULL COMMENT '督办计划ID',
    progress_desc           TEXT COMMENT '进度描述',
    expected_receipt_date   DATE COMMENT '预计回款日期',
    issue_desc              TEXT COMMENT '问题说明',
    submitted_by            BIGINT COMMENT '提交人ID',
    submitted_by_name        VARCHAR(50) COMMENT '提交人姓名',
    submitted_at            DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    INDEX idx_plan_id (plan_id),
    INDEX idx_submitted_at (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='执行反馈表';

-- ----------------------------
-- 9. 反馈附件表
-- ----------------------------
DROP TABLE IF EXISTS biz_payment_supervision_feedback_attachment;
CREATE TABLE biz_payment_supervision_feedback_attachment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    feedback_id     BIGINT NOT NULL COMMENT '反馈记录ID',
    file_name       VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path       VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size       BIGINT COMMENT '文件大小(字节)',
    uploaded_by     BIGINT COMMENT '上传人ID',
    uploaded_by_name VARCHAR(50) COMMENT '上传人姓名',
    uploaded_at     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_feedback_id (feedback_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='反馈附件表';