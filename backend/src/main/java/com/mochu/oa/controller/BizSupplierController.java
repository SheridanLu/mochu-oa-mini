package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizSupplier;
import com.mochu.oa.service.BizSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
@Tag(name = "供应商管理")
public class BizSupplierController {
    
    private final BizSupplierService bizSupplierService;
    
    @GetMapping("/list")
    @Operation(summary = "获取供应商列表")
    public Result<List<BizSupplier>> list() {
        List<BizSupplier> list = bizSupplierService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询供应商")
    public Result<Page<BizSupplier>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "供应商名称") @RequestParam(required = false) String supplierName,
            @Parameter(description = "供应商类型") @RequestParam(required = false) Integer supplierType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BizSupplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizSupplier> wrapper = new LambdaQueryWrapper<>();
        if (supplierName != null) {
            wrapper.like(BizSupplier::getSupplierName, supplierName);
        }
        if (supplierType != null) {
            wrapper.eq(BizSupplier::getSupplierType, supplierType);
        }
        if (status != null) {
            wrapper.eq(BizSupplier::getStatus, status);
        }
        Page<BizSupplier> result = bizSupplierService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取供应商详情")
    public Result<BizSupplier> getById(@Parameter(description = "供应商ID") @PathVariable Long id) {
        BizSupplier supplier = bizSupplierService.getById(id);
        return Result.success(supplier);
    }
    
    @PostMapping
    @Operation(summary = "创建供应商")
    public Result<Void> create(@RequestBody BizSupplier supplier) {
        bizSupplierService.save(supplier);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新供应商")
    public Result<Void> update(@RequestBody BizSupplier supplier) {
        bizSupplierService.updateById(supplier);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    public Result<Void> delete(@Parameter(description = "供应商ID") @PathVariable Long id) {
        bizSupplierService.removeById(id);
        return Result.success(null);
    }
    
    @GetMapping("/rating/list")
    @Operation(summary = "获取供应商评级列表")
    public Result<List<Map<String, Object>>> getRatingList(
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "评级年份") @RequestParam(required = false) Integer year) {
        List<Map<String, Object>> list = bizSupplierService.getRatingList(supplierId, year);
        return Result.success(list);
    }
    
    @GetMapping("/rating/{id}")
    @Operation(summary = "获取供应商评级详情")
    public Result<Map<String, Object>> getRating(@Parameter(description = "供应商ID") @PathVariable Long id,
            @Parameter(description = "评级年份") @RequestParam(required = false) Integer year) {
        if (year == null) year = Calendar.getInstance().get(Calendar.YEAR);
        Map<String, Object> rating = bizSupplierService.calculateRating(id, year);
        return Result.success(rating);
    }
    
    @PostMapping("/rating/{id}/recalc")
    @Operation(summary = "重新计算评级")
    public Result<Void> recalcRating(@Parameter(description = "供应商ID") @PathVariable Long id,
            @Parameter(description = "评级年份") @RequestParam(required = false) Integer year) {
        if (year == null) year = Calendar.getInstance().get(Calendar.YEAR);
        bizSupplierService.recalculateRating(id, year);
        return Result.success(null);
    }
    
    @GetMapping("/rating/export")
    @Operation(summary = "导出供应商评级")
    public Result<Map<String, String>> exportRating(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "类型") @RequestParam(required = false) String type) {
        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", "/exports/supplier-rating-" + year + ".xlsx");
        return Result.success(result);
    }
}