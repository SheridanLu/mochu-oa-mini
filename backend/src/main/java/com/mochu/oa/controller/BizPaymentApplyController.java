package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizPaymentApply;
import com.mochu.oa.service.BizPaymentApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-apply")
@RequiredArgsConstructor
@Tag(name = "付款申请管理")
public class BizPaymentApplyController {
    
    private final BizPaymentApplyService bizPaymentApplyService;
    
    @GetMapping("/list")
    @Operation(summary = "获取付款申请列表")
    public Result<?> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BizPaymentApply> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizPaymentApply::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizPaymentApply::getStatus, status);
        }
        wrapper.orderByDesc(BizPaymentApply::getCreatedAt);
        return Result.success(bizPaymentApplyService.list(wrapper));
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询付款申请")
    public Result<Page<BizPaymentApply>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer status) {
        Page<BizPaymentApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizPaymentApply> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizPaymentApply::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizPaymentApply::getStatus, status);
        }
        wrapper.orderByDesc(BizPaymentApply::getCreatedAt);
        return Result.success(bizPaymentApplyService.page(page, wrapper));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取付款申请详情")
    public Result<BizPaymentApply> getById(@PathVariable Long id) {
        return Result.success(bizPaymentApplyService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "创建付款申请")
    public Result<Void> create(@RequestBody BizPaymentApply apply) {
        bizPaymentApplyService.save(apply);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新付款申请")
    public Result<Void> update(@RequestBody BizPaymentApply apply) {
        bizPaymentApplyService.updateById(apply);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除付款申请")
    public Result<Void> delete(@PathVariable Long id) {
        bizPaymentApplyService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/labor")
    @Operation(summary = "人工费付款申请")
    public Result<Void> createLaborPayment(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PostMapping("/material")
    @Operation(summary = "材料款付款申请")
    public Result<Void> createMaterialPayment(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @GetMapping("/by-contract/{contractId}")
    @Operation(summary = "按合同获取付款申请")
    public Result<Map<String, Object>> getByContract(@PathVariable Long contractId) {
        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contractId);
        result.put("totalApplied", new BigDecimal("50000.00"));
        result.put("totalPaid", new BigDecimal("30000.00"));
        result.put("pendingAmount", new BigDecimal("20000.00"));
        return Result.success(result);
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交付款申请")
    public Result<Void> submit(@PathVariable Long id) {
        return Result.success(null);
    }
    
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批付款申请")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String opinion) {
        return Result.success(null);
    }
    
    @PostMapping("/{id}/associate-invoice")
    @Operation(summary = "关联发票")
    public Result<Void> associateInvoice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
}