package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizPurchaseOrder;
import com.mochu.oa.entity.BizInquiryRecord;
import com.mochu.oa.service.BizPurchaseOrderService;
import com.mochu.oa.service.BizInquiryRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
@Tag(name = "采购管理")
public class BizPurchaseOrderController {
    
    private final BizPurchaseOrderService bizPurchaseOrderService;
    private final BizInquiryRecordService bizInquiryRecordService;
    
    @GetMapping("/list")
    @Operation(summary = "获取采购订单列表")
    public Result<List<BizPurchaseOrder>> list() {
        List<BizPurchaseOrder> list = bizPurchaseOrderService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询采购订单")
    public Result<Page<BizPurchaseOrder>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BizPurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizPurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizPurchaseOrder::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizPurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizPurchaseOrder::getCreatedAt);
        Page<BizPurchaseOrder> result = bizPurchaseOrderService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取采购订单详情")
    public Result<BizPurchaseOrder> getById(@Parameter(description = "订单ID") @PathVariable Long id) {
        BizPurchaseOrder order = bizPurchaseOrderService.getById(id);
        return Result.success(order);
    }
    
    @PostMapping
    @Operation(summary = "创建采购订单")
    public Result<Void> create(@RequestBody BizPurchaseOrder order) {
        bizPurchaseOrderService.save(order);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新采购订单")
    public Result<Void> update(@RequestBody BizPurchaseOrder order) {
        bizPurchaseOrderService.updateById(order);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除采购订单")
    public Result<Void> delete(@Parameter(description = "订单ID") @PathVariable Long id) {
        bizPurchaseOrderService.removeById(id);
        return Result.success(null);
    }
    
    @GetMapping("/price/base/list")
    @Operation(summary = "获取基准价列表")
    public Result<List<Map<String, Object>>> getBasePriceList(
            @Parameter(description = "物资名称") @RequestParam(required = false) String materialName,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId) {
        return Result.success(List.of());
    }
    
    @GetMapping("/price/standard/list")
    @Operation(summary = "获取标准价列表")
    public Result<List<Map<String, Object>>> getStandardPriceList(
            @Parameter(description = "物资名称") @RequestParam(required = false) String materialName,
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId) {
        return Result.success(List.of());
    }
    
    @GetMapping("/price/search")
    @Operation(summary = "价格搜索")
    public Result<Map<String, Object>> searchPrice(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("basePrice", 100.00);
        result.put("standardPrice", 120.00);
        result.put("marketPrice", 115.00);
        result.put("supplierPrices", List.of());
        return Result.success(result);
    }
    
    @GetMapping("/price/{id}/history")
    @Operation(summary = "价格历史")
    public Result<List<Map<String, Object>>> getPriceHistory(@Parameter(description = "价格ID") @PathVariable Long id) {
        return Result.success(List.of());
    }
    
    @GetMapping("/inquiry/list")
    @Operation(summary = "获取询价单列表")
    public Result<List<Map<String, Object>>> getInquiryList(@RequestParam(required = false) Long projectId) {
        List<BizInquiryRecord> records = bizInquiryRecordService.getByProjectId(projectId);
        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("inquiryNo", r.getInquiryNo());
            map.put("materialName", r.getMaterialName());
            map.put("supplierName", r.getSupplierName());
            map.put("unitPrice", r.getUnitPrice());
            map.put("inquiryDate", r.getInquiryDate());
            map.put("status", r.getStatus());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }
    
    @GetMapping("/inquiry/page")
    @Operation(summary = "分页查询询价单")
    public Result<Page<Map<String, Object>>> pageInquiry(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }
    
    @GetMapping("/inquiry/{id}")
    @Operation(summary = "获取询价单详情")
    public Result<Map<String, Object>> getInquiry(@Parameter(description = "询价单ID") @PathVariable Long id) {
        BizInquiryRecord record = bizInquiryRecordService.getById(id);
        Map<String, Object> inquiry = new HashMap<>();
        if (record != null) {
            inquiry.put("id", record.getId());
            inquiry.put("inquiryNo", record.getInquiryNo());
            inquiry.put("materialName", record.getMaterialName());
            inquiry.put("supplierName", record.getSupplierName());
            inquiry.put("unitPrice", record.getUnitPrice());
            inquiry.put("status", record.getStatus());
        }
        return Result.success(inquiry);
    }
    
    @PostMapping("/inquiry")
    @Operation(summary = "创建询价单")
    public Result<Void> createInquiry(@RequestBody BizInquiryRecord inquiry) {
        inquiry.setDeleted(0);
        inquiry.setStatus(1);
        bizInquiryRecordService.save(inquiry);
        return Result.success(null);
    }
    
    @PostMapping("/inquiry/{id}/submit")
    @Operation(summary = "提交询价单")
    public Result<Void> submitInquiry(@Parameter(description = "询价单ID") @PathVariable Long id) {
        BizInquiryRecord record = new BizInquiryRecord();
        record.setId(id);
        record.setStatus(2);
        bizInquiryRecordService.updateById(record);
        return Result.success(null);
    }
    
    @GetMapping("/price/match")
    @Operation(summary = "价格匹配")
    public Result<Map<String, Object>> matchPrice(
            @Parameter(description = "物资ID") @RequestParam Long materialId,
            @Parameter(description = "项目ID") @RequestParam Long projectId) {
        Map<String, Object> result = bizInquiryRecordService.matchPrice(materialId, projectId);
        return Result.success(result);
    }
    
    @GetMapping("/price/compare")
    @Operation(summary = "价格比价")
    public Result<List<Map<String, Object>>> comparePrice(
            @Parameter(description = "物资ID") @RequestParam Long materialId,
            @Parameter(description = "项目ID") @RequestParam Long projectId) {
        List<Map<String, Object>> result = bizInquiryRecordService.comparePrices(materialId, projectId);
        return Result.success(result);
    }
    
    @GetMapping("/price/auto-compare")
    @Operation(summary = "自动比价")
    public Result<Map<String, Object>> autoCompare(
            @Parameter(description = "物资ID") @RequestParam Long materialId,
            @Parameter(description = "项目ID") @RequestParam Long projectId) {
        Map<String, Object> result = bizInquiryRecordService.autoCompare(materialId, projectId);
        return Result.success(result);
    }
    
    @PostMapping("/compare")
    @Operation(summary = "比价")
    public Result<Map<String, Object>> compare(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("suppliers", List.of());
        result.put("bestPrice", 100.00);
        result.put("recommendation", "Supplier A");
        return Result.success(result);
    }
    
    @GetMapping("/limit-check")
    @Operation(summary = "零星采购限额检查")
    public Result<Map<String, Object>> checkLimit(
            @Parameter(description = "项目ID") @RequestParam Long projectId,
            @Parameter(description = "本次金额") @RequestParam java.math.BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        
        java.math.BigDecimal historicalAmount = new java.math.BigDecimal("0");
        java.math.BigDecimal totalAmount = historicalAmount.add(amount);
        java.math.BigDecimal limitRatio = new java.math.BigDecimal("1.5");
        
        result.put("isOverLimit", false);
        result.put("historicalAmount", historicalAmount);
        result.put("currentAmount", amount);
        result.put("totalAmount", totalAmount);
        result.put("limitRatio", limitRatio);
        result.put("limitAmount", new java.math.BigDecimal("0"));
        result.put("warningLevel", "normal");
        
        if (amount.compareTo(new java.math.BigDecimal("5000")) > 0) {
            result.put("isOverLimit", true);
            result.put("warningLevel", "high");
            result.put("warningMessage", "大额采购需走审批流程");
        }
        
        return Result.success(result);
    }
    
    @PostMapping("/limit-override")
    @Operation(summary = "超限额审批")
    public Result<Void> overrideLimit(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @GetMapping("/match-result/{purchaseId}")
    @Operation(summary = "获取匹配结果")
    public Result<Map<String, Object>> getMatchResult(@PathVariable Long purchaseId) {
        Map<String, Object> result = new HashMap<>();
        result.put("purchaseId", purchaseId);
        result.put("matchSource", "base");
        result.put("basePriceId", 1L);
        result.put("basePrice", new java.math.BigDecimal("5000.00"));
        result.put("matchDetails", List.of());
        return Result.success(result);
    }
    
    @PostMapping("/confirm-price")
    @Operation(summary = "确认价格入库")
    public Result<Void> confirmPrice(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
}