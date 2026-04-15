package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.PageRequestGuard;
import com.mochu.oa.common.ParamGuard;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizContractPaymentPlan;
import com.mochu.oa.entity.BizExpenseContract;
import com.mochu.oa.service.BizContractPaymentPlanService;
import com.mochu.oa.service.BizExpenseContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment-plan")
@RequiredArgsConstructor
@Tag(name = "付款计划管理")
public class BizContractPaymentPlanController {
    
    private final BizContractPaymentPlanService bizContractPaymentPlanService;
    private final BizExpenseContractService bizExpenseContractService;
    
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
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "支出合同ID") @RequestParam(required = false) Long contractId,
            @Parameter(description = "支出合同ID列表（与 contractId 二选一或组合）") @RequestParam(required = false) List<Long> contractIds,
            @Parameter(description = "项目ID（按支出合同归属过滤）") @RequestParam(required = false) Long projectId,
            @Parameter(description = "计划月份 YYYY-MM") @RequestParam(required = false) String planMonth,
            @Parameter(description = "状态 1启用 0停用") @RequestParam(required = false) Integer status) {
        String pageErr = PageRequestGuard.validate(pageNum, pageSize);
        if (pageErr != null) {
            return Result.badRequest(pageErr);
        }
        pageSize = PageRequestGuard.normalizePageSize(pageSize, 500);
        Page<BizContractPaymentPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        if (contractId != null) {
            wrapper.eq(BizContractPaymentPlan::getContractId, contractId);
        } else if (contractIds != null && !contractIds.isEmpty()) {
            wrapper.in(BizContractPaymentPlan::getContractId, contractIds);
        }
        if (projectId != null) {
            List<Long> projectContractIds = bizExpenseContractService.list(
                            new LambdaQueryWrapper<BizExpenseContract>().eq(BizExpenseContract::getProjectId, projectId))
                    .stream().map(BizExpenseContract::getId).collect(Collectors.toList());
            if (projectContractIds.isEmpty()) {
                return Result.success(new Page<>(pageNum, pageSize));
            }
            wrapper.in(BizContractPaymentPlan::getContractId, projectContractIds);
        }
        if (planMonth != null && !planMonth.isBlank()) {
            YearMonth ym;
            try {
                ym = ParamGuard.parseYearMonth(planMonth, "planMonth");
            } catch (IllegalArgumentException ex) {
                return Result.badRequest(ex.getMessage());
            }
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            wrapper.ge(BizContractPaymentPlan::getPlannedPaymentDate, start)
                    .le(BizContractPaymentPlan::getPlannedPaymentDate, end);
        }
        if (status != null) {
            String statusErr = ParamGuard.validateOneOf(status, "status", 0, 1);
            if (statusErr != null) {
                return Result.badRequest(statusErr);
            }
            wrapper.eq(BizContractPaymentPlan::getStatus, status);
        }
        wrapper.orderByAsc(BizContractPaymentPlan::getPlannedPaymentDate)
                .orderByAsc(BizContractPaymentPlan::getSortOrder);
        return Result.success(bizContractPaymentPlanService.page(page, wrapper));
    }

    @GetMapping("/summary")
    @Operation(summary = "付款计划列表统计卡片")
    public Result<Map<String, Object>> summary() {
        List<BizContractPaymentPlan> all = bizContractPaymentPlanService.list();
        YearMonth ym = YearMonth.now();
        LocalDate ms = ym.atDay(1);
        LocalDate me = ym.atEndOfMonth();
        LocalDate today = LocalDate.now();
        LocalDate weekEnd = today.plusDays(7);

        long enabled = all.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count();
        BigDecimal monthAmount = all.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .filter(p -> p.getPlannedPaymentDate() != null
                        && !p.getPlannedPaymentDate().isBefore(ms)
                        && !p.getPlannedPaymentDate().isAfter(me))
                .map(p -> p.getPlannedAmount() != null ? p.getPlannedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long dueSoon = all.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .filter(p -> p.getPlannedPaymentDate() != null
                        && !p.getPlannedPaymentDate().isBefore(today)
                        && !p.getPlannedPaymentDate().isAfter(weekEnd))
                .count();

        Map<String, Object> m = new HashMap<>();
        m.put("enabledCount", enabled);
        m.put("monthPlanAmount", monthAmount);
        m.put("dueWithin7Days", dueSoon);
        return Result.success(m);
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

    @GetMapping("/{id}")
    @Operation(summary = "付款计划详情（含关联支出合同）")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        BizContractPaymentPlan plan = bizContractPaymentPlanService.getById(id);
        if (plan == null) {
            return Result.notFound("付款计划不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("plan", plan);
        if (plan.getContractId() != null) {
            BizExpenseContract c = bizExpenseContractService.getById(plan.getContractId());
            data.put("contract", c);
        }
        return Result.success(data);
    }
    
    @GetMapping("/contracts/{contractId}")
    @Operation(summary = "获取合同付款计划")
    public Result<List<BizContractPaymentPlan>> getByContract(@PathVariable Long contractId) {
        LambdaQueryWrapper<BizContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizContractPaymentPlan::getContractId, contractId);
        return Result.success(bizContractPaymentPlanService.list(wrapper));
    }
    
    @GetMapping("/monthly/{month}")
    @Operation(summary = "获取月度付款计划汇总（按计划付款日落在该自然月的启用计划汇总；已付金额暂无台账对接时为 0）")
    public Result<Map<String, Object>> getMonthlyPlan(@PathVariable String month) {
        YearMonth ym;
        try {
            ym = ParamGuard.parseYearMonth(month, "month");
        } catch (IllegalArgumentException ex) {
            return Result.badRequest(ex.getMessage());
        }
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        LambdaQueryWrapper<BizContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizContractPaymentPlan::getStatus, 1)
                .ge(BizContractPaymentPlan::getPlannedPaymentDate, start)
                .le(BizContractPaymentPlan::getPlannedPaymentDate, end)
                .orderByAsc(BizContractPaymentPlan::getPlannedPaymentDate)
                .orderByAsc(BizContractPaymentPlan::getSortOrder);
        List<BizContractPaymentPlan> plans = bizContractPaymentPlanService.list(wrapper);
        BigDecimal totalPlan = plans.stream()
                .map(p -> p.getPlannedAmount() != null ? p.getPlannedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("totalPlan", totalPlan);
        result.put("totalPaid", BigDecimal.ZERO);
        result.put("totalPending", totalPlan);
        result.put("items", plans);
        return Result.success(result);
    }
    
    @PostMapping("/generate-monthly")
    @Operation(summary = "生成月度付款计划（尚未实现：请在支出合同中维护付款条款与计划）")
    public Result<Void> generateMonthly(@RequestBody Map<String, Object> params) {
        return Result.error(501, "月度批量生成尚未实现，请在支出合同中维护付款条款与计划");
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "更新计划状态（body: {\"status\":1|0}）")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        BizContractPaymentPlan plan = bizContractPaymentPlanService.getById(id);
        if (plan == null) {
            return Result.notFound("付款计划不存在");
        }
        Object s = params != null ? params.get("status") : null;
        if (s == null) {
            return Result.badRequest("缺少 status");
        }
        int status;
        if (s instanceof Number) {
            status = ((Number) s).intValue();
        } else {
            status = Integer.parseInt(s.toString().trim());
        }
        String statusErr = ParamGuard.validateOneOf(status, "status", 0, 1);
        if (statusErr != null) {
            return Result.badRequest(statusErr);
        }
        plan.setStatus(status);
        bizContractPaymentPlanService.updateById(plan);
        return Result.success(null);
    }

    private static final int EXPORT_ROW_LIMIT = 10000;

    @GetMapping("/export/csv")
    @Operation(summary = "导出付款计划 CSV（与分页筛选条件一致，最多 " + EXPORT_ROW_LIMIT + " 行）")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) List<Long> contractIds,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String planMonth,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BizContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        if (contractId != null) {
            wrapper.eq(BizContractPaymentPlan::getContractId, contractId);
        } else if (contractIds != null && !contractIds.isEmpty()) {
            wrapper.in(BizContractPaymentPlan::getContractId, contractIds);
        }
        if (projectId != null) {
            List<Long> projectContractIds = bizExpenseContractService.list(
                            new LambdaQueryWrapper<BizExpenseContract>().eq(BizExpenseContract::getProjectId, projectId))
                    .stream().map(BizExpenseContract::getId).collect(Collectors.toList());
            if (projectContractIds.isEmpty()) {
                byte[] empty = ("\uFEFF" + paymentPlanCsvHeader()).getBytes(StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payment-plan.csv\"")
                        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                        .body(empty);
            }
            wrapper.in(BizContractPaymentPlan::getContractId, projectContractIds);
        }
        if (planMonth != null && !planMonth.isBlank()) {
            YearMonth ym;
            try {
                ym = ParamGuard.parseYearMonth(planMonth, "planMonth");
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            wrapper.ge(BizContractPaymentPlan::getPlannedPaymentDate, start)
                    .le(BizContractPaymentPlan::getPlannedPaymentDate, end);
        }
        if (status != null) {
            String statusErr = ParamGuard.validateOneOf(status, "status", 0, 1);
            if (statusErr != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, statusErr);
            }
            wrapper.eq(BizContractPaymentPlan::getStatus, status);
        }
        wrapper.orderByAsc(BizContractPaymentPlan::getPlannedPaymentDate)
                .orderByAsc(BizContractPaymentPlan::getSortOrder)
                .last("LIMIT " + EXPORT_ROW_LIMIT);
        List<BizContractPaymentPlan> list = bizContractPaymentPlanService.list(wrapper);
        Map<Long, BizExpenseContract> contractMap = new HashMap<>();
        for (Long cid : list.stream().map(BizContractPaymentPlan::getContractId).filter(Objects::nonNull).distinct()
                .collect(Collectors.toList())) {
            BizExpenseContract c = bizExpenseContractService.getById(cid);
            if (c != null) {
                contractMap.put(cid, c);
            }
        }
        StringBuilder sb = new StringBuilder();
        appendCsvRow(sb, "id", "contract_no", "contract_name", "project_name", "payment_term_name", "planned_amount",
                "planned_payment_date", "payment_condition", "status", "sort_order");
        for (BizContractPaymentPlan p : list) {
            BizExpenseContract c = p.getContractId() != null ? contractMap.get(p.getContractId()) : null;
            appendCsvRow(sb, p.getId(), c != null ? c.getContractNo() : "", c != null ? c.getContractName() : "",
                    c != null ? c.getProjectName() : "", p.getPaymentTermName(), p.getPlannedAmount(),
                    p.getPlannedPaymentDate(), p.getPaymentCondition(), p.getStatus(), p.getSortOrder());
        }
        byte[] body = ("\uFEFF" + sb).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payment-plan.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    private static String paymentPlanCsvHeader() {
        return "id,contract_no,contract_name,project_name,payment_term_name,planned_amount,planned_payment_date,payment_condition,status,sort_order\n";
    }

    private static void appendCsvRow(StringBuilder sb, Object... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(csvEscape(cells[i]));
        }
        sb.append('\n');
    }

    private static String csvEscape(Object o) {
        if (o == null) {
            return "";
        }
        String s = o.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\r") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

}
