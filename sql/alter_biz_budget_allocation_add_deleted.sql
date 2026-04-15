-- 若库中已存在旧版 biz_budget_allocation（无 deleted 列），执行本脚本。
ALTER TABLE biz_budget_allocation
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER updated_at;
