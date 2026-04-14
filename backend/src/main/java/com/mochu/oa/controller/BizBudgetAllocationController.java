package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizBudgetAllocation;
import com.mochu.oa.service.BizBudgetAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@Tag(name = "预算管理")
public class BizBudgetAllocationController {
    
    private final BizBudgetAllocationService bizBudgetAllocationService;
    
    @GetMapping("/list")
    @Operation(summary = "获取预算列表")
    public Result<?> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LambdaQueryWrapper<BizBudgetAllocation> wrapper = new LambdaQueryWrapper<>();
        if (departmentId != null) {
            wrapper.eq(BizBudgetAllocation::getDepartmentId, departmentId);
        }
        if (year != null) {
            wrapper.eq(BizBudgetAllocation::getYear, year);
        }
        if (month != null) {
            wrapper.eq(BizBudgetAllocation::getMonth, month);
        }
        return Result.success(bizBudgetAllocationService.list(wrapper));
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询预算")
    public Result<Page<BizBudgetAllocation>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer year) {
        Page<BizBudgetAllocation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizBudgetAllocation> wrapper = new LambdaQueryWrapper<>();
        if (departmentId != null) {
            wrapper.eq(BizBudgetAllocation::getDepartmentId, departmentId);
        }
        if (year != null) {
            wrapper.eq(BizBudgetAllocation::getYear, year);
        }
        return Result.success(bizBudgetAllocationService.page(page, wrapper));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取预算详情")
    public Result<BizBudgetAllocation> getById(@PathVariable Long id) {
        return Result.success(bizBudgetAllocationService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "创建预算")
    public Result<Void> create(@RequestBody BizBudgetAllocation budget) {
        bizBudgetAllocationService.save(budget);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新预算")
    public Result<Void> update(@RequestBody BizBudgetAllocation budget) {
        bizBudgetAllocationService.updateById(budget);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除预算")
    public Result<Void> delete(@PathVariable Long id) {
        bizBudgetAllocationService.removeById(id);
        return Result.success(null);
    }
    
    @GetMapping("/summary")
    @Operation(summary = "预算汇总")
    public Result<Map<String, Object>> summary(
            @RequestParam Long departmentId,
            @RequestParam Integer year) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalBudget", new BigDecimal("500000.00"));
        result.put("usedAmount", new BigDecimal("350000.00"));
        result.put("remainingAmount", new BigDecimal("150000.00"));
        result.put("usageRatio", 70.0);
        return Result.success(result);
    }
    
    @GetMapping("/usage/{id}")
    @Operation(summary = "预算使用明细")
    public Result<Map<String, Object>> usage(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("budgetId", id);
        result.put("totalAmount", new BigDecimal("100000.00"));
        result.put("usedAmount", new BigDecimal("65000.00"));
        result.put("details", new Object[]{});
        return Result.success(result);
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
    
    @GetMapping("/warning")
    @Operation(summary = "预算预警列表")
    public Result<Map<String, Object>> warnings(
            @RequestParam Long departmentId,
            @RequestParam(required = false) Integer year) {
        Map<String, Object> result = new HashMap<>();
        result.put("warnings", new Object[]{});
        return Result.success(result);
    }
}