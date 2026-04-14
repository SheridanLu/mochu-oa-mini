package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizContractPaymentPlan;
import com.mochu.oa.service.BizContractPaymentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-plan")
@RequiredArgsConstructor
@Tag(name = "付款计划管理")
public class BizContractPaymentPlanController {
    
    private final BizContractPaymentPlanService bizContractPaymentPlanService;
    
    @GetMapping("/list")
    @Operation(summary = "获取付款计划列表")
    public Result<?> list(@RequestParam(required = false) Long contractId) {
        LambdaQueryWrapper<BizContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        if (contractId != null) {
            wrapper.eq(BizContractPaymentPlan::getContractId, contractId);
        }
        return Result.success(bizContractPaymentPlanService.list(wrapper));
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询付款计划")
    public Result<Page<BizContractPaymentPlan>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long contractId) {
        Page<BizContractPaymentPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        if (contractId != null) {
            wrapper.eq(BizContractPaymentPlan::getContractId, contractId);
        }
        return Result.success(bizContractPaymentPlanService.page(page, wrapper));
    }
    
    @PostMapping
    @Operation(summary = "创建付款计划")
    public Result<Void> create(@RequestBody BizContractPaymentPlan plan) {
        bizContractPaymentPlanService.save(plan);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新付款计划")
    public Result<Void> update(@RequestBody BizContractPaymentPlan plan) {
        bizContractPaymentPlanService.updateById(plan);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除付款计划")
    public Result<Void> delete(@PathVariable Long id) {
        bizContractPaymentPlanService.removeById(id);
        return Result.success(null);
    }
    
    @GetMapping("/contracts/{contractId}")
    @Operation(summary = "获取合同付款计划")
    public Result<List<BizContractPaymentPlan>> getByContract(@PathVariable Long contractId) {
        LambdaQueryWrapper<BizContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizContractPaymentPlan::getContractId, contractId);
        return Result.success(bizContractPaymentPlanService.list(wrapper));
    }
    
    @GetMapping("/monthly/{month}")
    @Operation(summary = "获取月度付款计划")
    public Result<Map<String, Object>> getMonthlyPlan(@PathVariable String month) {
        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("totalPlan", 500000.00);
        result.put("totalPaid", 300000.00);
        result.put("totalPending", 200000.00);
        result.put("items", List.of());
        return Result.success(result);
    }
    
    @PostMapping("/generate-monthly")
    @Operation(summary = "生成月度付款计划")
    public Result<Void> generateMonthly(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "更新计划状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
}