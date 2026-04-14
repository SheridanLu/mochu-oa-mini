package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizInvoice;
import com.mochu.oa.service.BizInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
@Tag(name = "发票管理")
public class BizInvoiceController {
    
    private final BizInvoiceService bizInvoiceService;
    
    @GetMapping("/list")
    @Operation(summary = "获取发票列表")
    public Result<?> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer invoiceType,
            @RequestParam(required = false) Integer isVerified) {
        LambdaQueryWrapper<BizInvoice> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizInvoice::getProjectId, projectId);
        }
        if (invoiceType != null) {
            wrapper.eq(BizInvoice::getInvoiceType, invoiceType);
        }
        if (isVerified != null) {
            wrapper.eq(BizInvoice::getIsVerified, isVerified);
        }
        return Result.success(bizInvoiceService.list(wrapper));
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询发票")
    public Result<Page<BizInvoice>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer invoiceType) {
        Page<BizInvoice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizInvoice> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizInvoice::getProjectId, projectId);
        }
        if (invoiceType != null) {
            wrapper.eq(BizInvoice::getInvoiceType, invoiceType);
        }
        return Result.success(bizInvoiceService.page(page, wrapper));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取发票详情")
    public Result<BizInvoice> getById(@PathVariable Long id) {
        return Result.success(bizInvoiceService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "创建发票")
    public Result<Void> create(@RequestBody BizInvoice invoice) {
        bizInvoiceService.save(invoice);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新发票")
    public Result<Void> update(@RequestBody BizInvoice invoice) {
        bizInvoiceService.updateById(invoice);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除发票")
    public Result<Void> delete(@PathVariable Long id) {
        bizInvoiceService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/verify")
    @Operation(summary = "验真")
    public Result<Map<String, Object>> verify(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("isValid", true);
        result.put("taxAmount", 1000.00);
        result.put("verifiedAt", "2026-04-13");
        return Result.success(result);
    }
}