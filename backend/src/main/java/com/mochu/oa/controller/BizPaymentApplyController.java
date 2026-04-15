package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizPaymentApply;
import com.mochu.oa.service.BizPaymentApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/payment-apply")
@RequiredArgsConstructor
@Tag(name = "付款申请管理")
public class BizPaymentApplyController {

    private final BizPaymentApplyService bizPaymentApplyService;

    @GetMapping("/list")
    @Operation(summary = "获取付款申请列表")
    public Result<List<BizPaymentApply>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String applyNo,
            @RequestParam(required = false) String createdBegin,
            @RequestParam(required = false) String createdEnd) {
        LambdaQueryWrapper<BizPaymentApply> w = buildScopeWrapper(
                projectId, contractId, category, supplierId, status, applyNo, createdBegin, createdEnd);
        w.orderByDesc(BizPaymentApply::getCreatedAt);
        return Result.success(bizPaymentApplyService.list(w));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询付款申请")
    public Result<Page<BizPaymentApply>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String applyNo,
            @RequestParam(required = false) String createdBegin,
            @RequestParam(required = false) String createdEnd) {
        Page<BizPaymentApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizPaymentApply> w = buildScopeWrapper(
                projectId, contractId, category, supplierId, status, applyNo, createdBegin, createdEnd);
        w.orderByDesc(BizPaymentApply::getCreatedAt);
        return Result.success(bizPaymentApplyService.page(page, w));
    }

    @GetMapping("/summary")
    @Operation(summary = "付款申请统计（与列表筛选一致，不含状态筛选）")
    public Result<Map<String, Object>> summary(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String applyNo,
            @RequestParam(required = false) String createdBegin,
            @RequestParam(required = false) String createdEnd) {
        LambdaQueryWrapper<BizPaymentApply> pendingW = buildScopeWrapper(
                projectId, contractId, category, supplierId, null, applyNo, createdBegin, createdEnd);
        pendingW.in(BizPaymentApply::getStatus, 2, 3);
        long pending = bizPaymentApplyService.count(pendingW);

        LambdaQueryWrapper<BizPaymentApply> paidW = buildScopeWrapper(
                projectId, contractId, category, supplierId, null, applyNo, createdBegin, createdEnd);
        paidW.eq(BizPaymentApply::getStatus, 6);
        long paid = bizPaymentApplyService.count(paidW);

        QueryWrapper<BizPaymentApply> sumQ = new QueryWrapper<>();
        sumQ.select("IFNULL(SUM(total_amount),0) AS totalAmount");
        applyScopeToQuery(sumQ, projectId, contractId, category, supplierId, applyNo, createdBegin, createdEnd);
        List<Map<String, Object>> rows = bizPaymentApplyService.listMaps(sumQ);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (rows != null && !rows.isEmpty()) {
            Object v = rows.get(0).get("totalAmount");
            if (v != null) {
                totalAmount = new BigDecimal(v.toString());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("pendingCount", pending);
        data.put("paidCount", paid);
        data.put("totalAmount", totalAmount);
        return Result.success(data);
    }

    @GetMapping("/by-contract/{contractId}")
    @Operation(summary = "按合同汇总付款申请")
    public Result<Map<String, Object>> getByContract(@PathVariable Long contractId) {
        LambdaQueryWrapper<BizPaymentApply> w = new LambdaQueryWrapper<BizPaymentApply>()
                .eq(BizPaymentApply::getContractId, contractId);
        List<BizPaymentApply> list = bizPaymentApplyService.list(w);
        BigDecimal totalApplied = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        for (BizPaymentApply a : list) {
            BigDecimal t = a.getTotalAmount() != null ? a.getTotalAmount()
                    : (a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO);
            totalApplied = totalApplied.add(t);
            Integer s = a.getStatus();
            if (s != null && s == 6) {
                totalPaid = totalPaid.add(t);
            } else if (s != null && s >= 2 && s <= 4) {
                pendingAmount = pendingAmount.add(t);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contractId);
        result.put("totalApplied", totalApplied);
        result.put("totalPaid", totalPaid);
        result.put("pendingAmount", pendingAmount);
        result.put("recordCount", list.size());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取付款申请详情")
    public Result<BizPaymentApply> getById(@PathVariable Long id) {
        return Result.success(bizPaymentApplyService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建付款申请")
    public Result<Void> create(@RequestBody BizPaymentApply apply) {
        if (apply.getApplyNo() == null || apply.getApplyNo().isBlank()) {
            apply.setApplyNo(nextApplyNo());
        }
        if (apply.getTotalAmount() == null && apply.getAmount() != null) {
            apply.setTotalAmount(apply.getAmount());
        }
        if (apply.getStatus() == null) {
            apply.setStatus(1);
        }
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

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交付款申请")
    public Result<Void> submit(@PathVariable Long id) {
        BizPaymentApply row = new BizPaymentApply();
        row.setId(id);
        row.setStatus(2);
        row.setSubmittedAt(LocalDateTime.now());
        bizPaymentApplyService.updateById(row);
        return Result.success(null);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批付款申请")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String opinion) {
        BizPaymentApply row = new BizPaymentApply();
        row.setId(id);
        row.setStatus("approve".equalsIgnoreCase(action) ? 4 : 5);
        bizPaymentApplyService.updateById(row);
        return Result.success(null);
    }

    @PostMapping("/{id}/associate-invoice")
    @Operation(summary = "关联发票")
    public Result<Void> associateInvoice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }

    private static String nextApplyNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int r = ThreadLocalRandom.current().nextInt(100, 1000);
        return "PA" + ts + r;
    }

    private LambdaQueryWrapper<BizPaymentApply> buildScopeWrapper(
            Long projectId,
            Long contractId,
            Integer category,
            Long supplierId,
            Integer status,
            String applyNo,
            String createdBegin,
            String createdEnd) {
        LambdaQueryWrapper<BizPaymentApply> w = new LambdaQueryWrapper<>();
        if (projectId != null) {
            w.eq(BizPaymentApply::getProjectId, projectId);
        }
        if (contractId != null) {
            w.eq(BizPaymentApply::getContractId, contractId);
        }
        if (category != null) {
            w.eq(BizPaymentApply::getCategory, category);
        }
        if (supplierId != null) {
            w.eq(BizPaymentApply::getSupplierId, supplierId);
        }
        if (status != null) {
            w.eq(BizPaymentApply::getStatus, status);
        }
        if (applyNo != null && !applyNo.isBlank()) {
            w.like(BizPaymentApply::getApplyNo, applyNo.trim());
        }
        if (createdBegin != null && !createdBegin.isBlank()) {
            w.ge(BizPaymentApply::getCreatedAt,
                    java.time.LocalDate.parse(createdBegin.trim()).atStartOfDay());
        }
        if (createdEnd != null && !createdEnd.isBlank()) {
            w.le(BizPaymentApply::getCreatedAt,
                    java.time.LocalDate.parse(createdEnd.trim()).atTime(23, 59, 59));
        }
        return w;
    }

    private void applyScopeToQuery(
            QueryWrapper<BizPaymentApply> q,
            Long projectId,
            Long contractId,
            Integer category,
            Long supplierId,
            String applyNo,
            String createdBegin,
            String createdEnd) {
        if (projectId != null) {
            q.eq("project_id", projectId);
        }
        if (contractId != null) {
            q.eq("contract_id", contractId);
        }
        if (category != null) {
            q.eq("category", category);
        }
        if (supplierId != null) {
            q.eq("supplier_id", supplierId);
        }
        if (applyNo != null && !applyNo.isBlank()) {
            q.like("apply_no", applyNo.trim());
        }
        if (createdBegin != null && !createdBegin.isBlank()) {
            q.ge("created_at", java.time.LocalDate.parse(createdBegin.trim()).atStartOfDay());
        }
        if (createdEnd != null && !createdEnd.isBlank()) {
            q.le("created_at", java.time.LocalDate.parse(createdEnd.trim()).atTime(23, 59, 59));
        }
    }
}
