-- 可选：成本归集示例数据（需先有项目 id=1 或改为实际项目 ID）
INSERT INTO biz_cost_collection (
    project_id, project_name, cost_category, cost_category_name, amount,
    source_type, source_id, source_no, collected_date, period, remark,
    created_at, deleted
) VALUES
(1, '示例项目', 1, '材料费', 125000.00, 2, NULL, 'CG202604001', '2026-04-10', '2026-04', '钢材采购', NOW(), 0),
(1, '示例项目', 2, '人工费', 45000.00, 1, NULL, 'BX202604002', '2026-04-08', '2026-04', '施工人员报销', NOW(), 0);
