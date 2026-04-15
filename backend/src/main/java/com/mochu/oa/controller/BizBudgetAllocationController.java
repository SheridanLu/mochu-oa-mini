package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizBudgetAllocation;
import com.mochu.oa.service.BizBudgetAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@Tag(name = "预算管理")
public class BizBudgetAllocationController {

    private final BizBudgetAllocationService bizBudgetAllocationService;

    @GetMapping("/list")
    @Operation(summary = "获取预算列表")
    public Result<List<BizBudgetAllocation>> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer budgetType,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BizBudgetAllocation> w = buildScopeWrapper(
                departmentId, projectId, year, month, budgetType, status);
        w.orderByDesc(BizBudgetAllocation::getYear)
                .orderByDesc(BizBudgetAllocation::getMonth)
                .orderByDesc(BizBudgetAllocation::getId);
        return Result.success(bizBudgetAllocationService.list(w));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询预算")
    public Result<Page<BizBudgetAllocation>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer budgetType,
            @RequestParam(required = false) Integer status) {
        Page<BizBudgetAllocation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizBudgetAllocation> w = buildScopeWrapper(
                departmentId, projectId, year, month, budgetType, status);
        w.orderByDesc(BizBudgetAllocation::getYear)
                .orderByDesc(BizBudgetAllocation::getMonth)
                .orderByDesc(BizBudgetAllocation::getId);
        return Result.success(bizBudgetAllocationService.page(page, w));
    }

    /**
     * 必须在 /{id} 之前注册，避免部分环境将 "summary" 误匹配为路径变量。
     */
    @GetMapping("/summary")
    @Operation(summary = "预算汇总（可选部门/项目/年度筛选）")
    public Result<Map<String, Object>> summary(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer year) {
        LambdaQueryWrapper<BizBudgetAllocation> scope = buildScopeWrapper(
                departmentId, projectId, year, null, null, null);
        long recordCount = bizBudgetAllocationService.count(scope);

        QueryWrapper<BizBudgetAllocation> sumQ = new QueryWrapper<>();
        sumQ.select(
                "IFNULL(SUM(amount),0) AS totalBudget",
                "IFNULL(SUM(used_amount),0) AS totalUsed",
                "IFNULL(SUM(available_amount),0) AS totalAvailable");
        applyScopeToQueryWrapper(sumQ, departmentId, projectId, year, null, null, null);
        List<Map<String, Object>> sumRows = bizBudgetAllocationService.listMaps(sumQ);
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalUsed = BigDecimal.ZERO;
        BigDecimal totalAvailable = BigDecimal.ZERO;
        if (sumRows != null && !sumRows.isEmpty()) {
            Map<String, Object> row = sumRows.get(0);
            totalBudget = toBd(row.get("totalBudget"));
            totalUsed = toBd(row.get("totalUsed"));
            totalAvailable = toBd(row.get("totalAvailable"));
        }

        LambdaQueryWrapper<BizBudgetAllocation> warnW = buildScopeWrapper(
                departmentId, projectId, year, null, null, null);
        warnW.and(q -> q.ge(BizBudgetAllocation::getUsageRate, new BigDecimal("80"))
                .or(w -> w.apply("amount > 0 AND used_amount / amount >= 0.8")));
        long warningCount = bizBudgetAllocationService.count(warnW);

        BigDecimal usageRatio = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            usageRatio = totalUsed.multiply(new BigDecimal("100"))
                    .divide(totalBudget, 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recordCount", recordCount);
        result.put("totalBudget", totalBudget);
        result.put("usedAmount", totalUsed);
        result.put("remainingAmount", totalAvailable);
        result.put("usageRatio", usageRatio.doubleValue());
        result.put("warningCount", warningCount);
        return Result.success(result);
    }

    @GetMapping("/warning")
    @Operation(summary = "预算预警列表（占用≥80%或已超支）")
    public Result<Map<String, Object>> warnings(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer year) {
        LambdaQueryWrapper<BizBudgetAllocation> w = buildScopeWrapper(
                departmentId, projectId, year, null, null, null);
        w.and(q -> q.ge(BizBudgetAllocation::getUsageRate, new BigDecimal("80"))
                .or(x -> x.apply("amount > 0 AND used_amount / amount >= 0.8")));
        w.orderByDesc(BizBudgetAllocation::getUsageRate);
        List<BizBudgetAllocation> list = bizBudgetAllocationService.list(w);
        Map<String, Object> result = new HashMap<>();
        result.put("warnings", list);
        result.put("total", list.size());
        return Result.success(result);
    }

    @GetMapping("/usage/{id}")
    @Operation(summary = "预算使用明细")
    public Result<Map<String, Object>> usage(@PathVariable Long id) {
        BizBudgetAllocation budget = bizBudgetAllocationService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (budget == null) {
            return Result.notFound("预算不存在");
        }
        result.put("budgetId", id);
        result.put("totalAmount", budget.getAmount());
        result.put("usedAmount", budget.getUsedAmount());
        result.put("availableAmount", budget.getAvailableAmount());
        result.put("usageRate", budget.getUsageRate());
        result.put("details", new Object[] {});
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取预算详情")
    public Result<BizBudgetAllocation> getById(@PathVariable Long id) {
        return Result.success(bizBudgetAllocationService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建预算")
    public Result<Void> create(@RequestBody BizBudgetAllocation budget) {
        fillDerived(budget);
        bizBudgetAllocationService.save(budget);
        return Result.success(null);
    }

    @PutMapping
    @Operation(summary = "更新预算")
    public Result<Void> update(@RequestBody BizBudgetAllocation budget) {
        fillDerived(budget);
        bizBudgetAllocationService.updateById(budget);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预算")
    public Result<Void> delete(@PathVariable Long id) {
        bizBudgetAllocationService.removeById(id);
        return Result.success(null);
    }

    @PostMapping("/adjust")
    @Operation(summary = "预算调整")
    public Result<Void> adjust(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }

    @PostMapping("/transfer")
    @Operation(summary = "预算调拨")
    public Result<Void> transfer(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }

    private void fillDerived(BizBudgetAllocation b) {
        BigDecimal amt = b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO;
        BigDecimal used = b.getUsedAmount() != null ? b.getUsedAmount() : BigDecimal.ZERO;
        BigDecimal avail = amt.subtract(used);
        b.setAvailableAmount(avail.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : avail);
        if (amt.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = used.multiply(new BigDecimal("100")).divide(amt, 4, RoundingMode.HALF_UP);
            b.setUsageRate(rate);
        } else {
            b.setUsageRate(BigDecimal.ZERO);
        }
    }

    private LambdaQueryWrapper<BizBudgetAllocation> buildScopeWrapper(
            Long departmentId,
            Long projectId,
            Integer year,
            Integer month,
            Integer budgetType,
            Integer status) {
        LambdaQueryWrapper<BizBudgetAllocation> w = new LambdaQueryWrapper<>();
        if (departmentId != null) {
            w.eq(BizBudgetAllocation::getDepartmentId, departmentId);
        }
        if (projectId != null) {
            w.eq(BizBudgetAllocation::getProjectId, projectId);
        }
        if (year != null) {
            w.eq(BizBudgetAllocation::getYear, year);
        }
        if (month != null) {
            w.eq(BizBudgetAllocation::getMonth, month);
        }
        if (budgetType != null) {
            w.eq(BizBudgetAllocation::getBudgetType, budgetType);
        }
        if (status != null) {
            w.eq(BizBudgetAllocation::getStatus, status);
        }
        return w;
    }

    private void applyScopeToQueryWrapper(
            QueryWrapper<BizBudgetAllocation> q,
            Long departmentId,
            Long projectId,
            Integer year,
            Integer month,
            Integer budgetType,
            Integer status) {
        if (departmentId != null) {
            q.eq("department_id", departmentId);
        }
        if (projectId != null) {
            q.eq("project_id", projectId);
        }
        if (year != null) {
            q.eq("year", year);
        }
        if (month != null) {
            q.eq("month", month);
        }
        if (budgetType != null) {
            q.eq("budget_type", budgetType);
        }
        if (status != null) {
            q.eq("status", status);
        }
    }

    private static BigDecimal toBd(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        return new BigDecimal(v.toString());
    }
}
