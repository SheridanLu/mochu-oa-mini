-- 若库中已存在旧版 biz_cost_collection（无 deleted 列），执行本脚本以兼容 MyBatis-Plus 逻辑删除字段。
ALTER TABLE biz_cost_collection
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER updated_at;
