package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizCostCollection;
import com.mochu.oa.service.BizCostCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
@Tag(name = "成本管理")
public class BizCostController {

    private final BizCostCollectionService bizCostCollectionService;

    @GetMapping("/list")
    @Operation(summary = "获取成本列表")
    public Result<List<BizCostCollection>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer costCategory,
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd,
            @RequestParam(required = false) String collectedBegin,
            @RequestParam(required = false) String collectedEnd) {
        LambdaQueryWrapper<BizCostCollection> w = buildScopeWrapper(
                projectId, costCategory, periodStart, periodEnd, collectedBegin, collectedEnd);
        w.orderByDesc(BizCostCollection::getCollectedDate).orderByDesc(BizCostCollection::getId);
        return Result.success(bizCostCollectionService.list(w));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询成本")
    public Result<Page<BizCostCollection>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer costCategory,
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd,
            @RequestParam(required = false) String collectedBegin,
            @RequestParam(required = false) String collectedEnd) {
        Page<BizCostCollection> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizCostCollection> w = buildScopeWrapper(
                projectId, costCategory, periodStart, periodEnd, collectedBegin, collectedEnd);
        w.orderByDesc(BizCostCollection::getCollectedDate).orderByDesc(BizCostCollection::getId);
        return Result.success(bizCostCollectionService.page(page, w));
    }

    @GetMapping("/summary")
    @Operation(summary = "成本归集统计（与列表筛选一致，不含成本类别时便于看总额）")
    public Result<Map<String, Object>> summary(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer costCategory,
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd,
            @RequestParam(required = false) String collectedBegin,
            @RequestParam(required = false) String collectedEnd) {
        LambdaQueryWrapper<BizCostCollection> scope = buildScopeWrapper(
                projectId, costCategory, periodStart, periodEnd, collectedBegin, collectedEnd);
        long totalCount = bizCostCollectionService.count(scope);

        QueryWrapper<BizCostCollection> sumQ = new QueryWrapper<>();
        sumQ.select("IFNULL(SUM(amount),0) AS totalAmount");
        applyScopeToQueryWrapper(sumQ, projectId, costCategory, periodStart, periodEnd, collectedBegin, collectedEnd);
        List<Map<String, Object>> sumRows = bizCostCollectionService.listMaps(sumQ);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (sumRows != null && !sumRows.isEmpty()) {
            Object v = sumRows.get(0).get("totalAmount");
            if (v != null) {
                totalAmount = new BigDecimal(v.toString());
            }
        }

        LocalDate m0 = YearMonth.now().atDay(1);
        LocalDate m1 = YearMonth.now().atEndOfMonth();
        QueryWrapper<BizCostCollection> monthQ = new QueryWrapper<>();
        monthQ.select("IFNULL(SUM(amount),0) AS monthAmount");
        applyScopeToQueryWrapper(monthQ, projectId, costCategory, periodStart, periodEnd, collectedBegin, collectedEnd);
        monthQ.between("collected_date", m0, m1);
        List<Map<String, Object>> monthRows = bizCostCollectionService.listMaps(monthQ);
        BigDecimal monthAmount = BigDecimal.ZERO;
        if (monthRows != null && !monthRows.isEmpty()) {
            Object v = monthRows.get(0).get("monthAmount");
            if (v != null) {
                monthAmount = new BigDecimal(v.toString());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", totalCount);
        data.put("totalAmount", totalAmount);
        data.put("monthAmount", monthAmount);
        return Result.success(data);
    }

    private void applyScopeToQueryWrapper(
            QueryWrapper<BizCostCollection> q,
            Long projectId,
            Integer costCategory,
            String periodStart,
            String periodEnd,
            String collectedBegin,
            String collectedEnd) {
        if (projectId != null) {
            q.eq("project_id", projectId);
        }
        if (costCategory != null) {
            q.eq("cost_category", costCategory);
        }
        if (periodStart != null && !periodStart.isBlank()) {
            q.ge("period", periodStart.trim());
        }
        if (periodEnd != null && !periodEnd.isBlank()) {
            q.le("period", periodEnd.trim());
        }
        if (collectedBegin != null && !collectedBegin.isBlank()) {
            q.ge("collected_date", LocalDate.parse(collectedBegin.trim()));
        }
        if (collectedEnd != null && !collectedEnd.isBlank()) {
            q.le("collected_date", LocalDate.parse(collectedEnd.trim()));
        }
    }

    private LambdaQueryWrapper<BizCostCollection> buildScopeWrapper(
            Long projectId,
            Integer costCategory,
            String periodStart,
            String periodEnd,
            String collectedBegin,
            String collectedEnd) {
        LambdaQueryWrapper<BizCostCollection> w = new LambdaQueryWrapper<>();
        if (projectId != null) {
            w.eq(BizCostCollection::getProjectId, projectId);
        }
        if (costCategory != null) {
            w.eq(BizCostCollection::getCostCategory, costCategory);
        }
        if (periodStart != null && !periodStart.isBlank()) {
            w.ge(BizCostCollection::getPeriod, periodStart.trim());
        }
        if (periodEnd != null && !periodEnd.isBlank()) {
            w.le(BizCostCollection::getPeriod, periodEnd.trim());
        }
        if (collectedBegin != null && !collectedBegin.isBlank()) {
            w.ge(BizCostCollection::getCollectedDate, LocalDate.parse(collectedBegin.trim()));
        }
        if (collectedEnd != null && !collectedEnd.isBlank()) {
            w.le(BizCostCollection::getCollectedDate, LocalDate.parse(collectedEnd.trim()));
        }
        return w;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取成本详情")
    public Result<BizCostCollection> getById(@PathVariable Long id) {
        return Result.success(bizCostCollectionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建成本记录")
    public Result<Void> create(@RequestBody BizCostCollection row) {
        fillCategoryName(row);
        if (row.getCollectedDate() == null) {
            row.setCollectedDate(LocalDate.now());
        }
        if (row.getPeriod() == null || row.getPeriod().isBlank()) {
            YearMonth ym = YearMonth.from(row.getCollectedDate());
            row.setPeriod(ym.toString());
        }
        bizCostCollectionService.save(row);
        return Result.success(null);
    }

    @PutMapping
    @Operation(summary = "更新成本记录")
    public Result<Void> update(@RequestBody BizCostCollection row) {
        fillCategoryName(row);
        bizCostCollectionService.updateById(row);
        return Result.success(null);
    }

    private void fillCategoryName(BizCostCollection row) {
        if (row.getCostCategory() == null) {
            return;
        }
        if (row.getCostCategoryName() != null && !row.getCostCategoryName().isBlank()) {
            return;
        }
        row.setCostCategoryName(categoryName(row.getCostCategory()));
    }

    private static String categoryName(int c) {
        switch (c) {
            case 1:
                return "材料费";
            case 2:
                return "人工费";
            case 3:
                return "机械费";
            case 4:
                return "管理费";
            case 5:
                return "其他";
            default:
                return "其他";
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "成本统计（按项目）")
    public Result<Map<String, Object>> statistics(
            @RequestParam Long projectId,
            @RequestParam(required = false) String period) {
        LambdaQueryWrapper<BizCostCollection> w = new LambdaQueryWrapper<BizCostCollection>()
                .eq(BizCostCollection::getProjectId, projectId);
        if (period != null && !period.isBlank()) {
            w.eq(BizCostCollection::getPeriod, period.trim());
        }
        List<BizCostCollection> list = bizCostCollectionService.list(w);
        BigDecimal material = BigDecimal.ZERO;
        BigDecimal equipment = BigDecimal.ZERO;
        BigDecimal labor = BigDecimal.ZERO;
        BigDecimal manage = BigDecimal.ZERO;
        BigDecimal other = BigDecimal.ZERO;
        for (BizCostCollection c : list) {
            BigDecimal a = c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO;
            Integer cat = c.getCostCategory();
            if (cat == null) {
                other = other.add(a);
                continue;
            }
            switch (cat) {
                case 1:
                    material = material.add(a);
                    break;
                case 2:
                    labor = labor.add(a);
                    break;
                case 3:
                    equipment = equipment.add(a);
                    break;
                case 4:
                    manage = manage.add(a);
                    break;
                default:
                    other = other.add(a);
            }
        }
        BigDecimal total = material.add(equipment).add(labor).add(manage).add(other);
        Map<String, Object> result = new HashMap<>();
        result.put("materialCost", material);
        result.put("equipmentCost", equipment);
        result.put("laborCost", labor);
        result.put("manageCost", manage);
        result.put("otherCost", other);
        result.put("totalCost", total);
        return Result.success(result);
    }

    @GetMapping("/daily")
    @Operation(summary = "日账明细")
    public Result<List<Map<String, Object>>> dailyCost(
            @RequestParam Long projectId,
            @RequestParam LocalDate date) {
        LambdaQueryWrapper<BizCostCollection> w = new LambdaQueryWrapper<BizCostCollection>()
                .eq(BizCostCollection::getProjectId, projectId)
                .eq(BizCostCollection::getCollectedDate, date);
        List<BizCostCollection> list = bizCostCollectionService.list(w);
        List<Map<String, Object>> out = new ArrayList<>();
        for (BizCostCollection c : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("amount", c.getAmount());
            m.put("costCategory", c.getCostCategory());
            m.put("sourceType", c.getSourceType());
            m.put("sourceNo", c.getSourceNo());
            out.add(m);
        }
        return Result.success(out);
    }

    @GetMapping("/monthly")
    @Operation(summary = "月账汇总")
    public Result<Map<String, Object>> monthlyCost(
            @RequestParam Long projectId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        String period = String.format("%04d-%02d", year, month);
        LambdaQueryWrapper<BizCostCollection> w = new LambdaQueryWrapper<BizCostCollection>()
                .eq(BizCostCollection::getProjectId, projectId)
                .eq(BizCostCollection::getPeriod, period);
        List<BizCostCollection> list = bizCostCollectionService.list(w);
        BigDecimal total = list.stream()
                .map(BizCostCollection::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("totalCost", total);
        result.put("dailyCosts", list);
        return Result.success(result);
    }

    @PostMapping("/auto-collect")
    @Operation(summary = "自动归集成本")
    public Result<Map<String, Object>> autoCollect(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("collectedCount", 0);
        result.put("totalAmount", BigDecimal.ZERO);
        result.put("message", "自动归集规则待与采购/报销模块对接");
        return Result.success(result);
    }
}
