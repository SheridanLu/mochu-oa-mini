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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            @RequestParam(required = false) Integer invoiceType,
            @RequestParam(required = false) Integer isVerified) {
        Page<BizInvoice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizInvoice> wrapper = buildScopeWrapper(projectId, invoiceType, isVerified);
        wrapper.orderByDesc(BizInvoice::getCreatedAt);
        return Result.success(bizInvoiceService.page(page, wrapper));
    }

    @GetMapping("/summary")
    @Operation(summary = "发票统计（不含认证状态筛选）")
    public Result<Map<String, Object>> summary(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer invoiceType) {
        LambdaQueryWrapper<BizInvoice> base = buildScopeWrapper(projectId, invoiceType, null);
        long total = bizInvoiceService.count(base);

        LambdaQueryWrapper<BizInvoice> verifiedW = buildScopeWrapper(projectId, invoiceType, null);
        verifiedW.eq(BizInvoice::getIsVerified, 1);
        long verified = bizInvoiceService.count(verifiedW);

        LambdaQueryWrapper<BizInvoice> unverifiedW = buildScopeWrapper(projectId, invoiceType, null);
        unverifiedW.eq(BizInvoice::getIsVerified, 0);
        long unverified = bizInvoiceService.count(unverifiedW);

        YearMonth ym = YearMonth.now();
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        LambdaQueryWrapper<BizInvoice> monthW = buildScopeWrapper(projectId, invoiceType, null);
        monthW.between(BizInvoice::getOpenedDate, monthStart, monthEnd);
        List<BizInvoice> monthList = bizInvoiceService.list(monthW);
        BigDecimal monthAmount = monthList.stream()
                .map(BizInvoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("verified", verified);
        data.put("unverified", unverified);
        data.put("monthAmount", monthAmount);
        return Result.success(data);
    }

    private LambdaQueryWrapper<BizInvoice> buildScopeWrapper(
            Long projectId, Integer invoiceType, Integer isVerified) {
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
        return wrapper;
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
    @Operation(summary = "认证（标记为已认证）")
    public Result<Map<String, Object>> verify(@PathVariable Long id) {
        BizInvoice inv = bizInvoiceService.getById(id);
        if (inv == null) {
            return Result.notFound("发票不存在");
        }
        inv.setIsVerified(1);
        inv.setVerifiedDate(LocalDate.now());
        bizInvoiceService.updateById(inv);
        Map<String, Object> result = new HashMap<>();
        result.put("isValid", true);
        result.put("verifiedAt", inv.getVerifiedDate() != null ? inv.getVerifiedDate().toString() : LocalDate.now().toString());
        return Result.success(result);
    }
}