package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.*;
import com.mochu.oa.service.*;
import io.swagger.v3.oas.annotations.Operation;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "报表中心")
public class ReportController {

    private final BizProjectService bizProjectService;
    private final BizReconciliationStatementService bizReconciliationStatementService;
    private final BizExpenseReportService bizExpenseReportService;
    private final BizInvoiceService bizInvoiceService;
    private final BizCostCollectionService bizCostCollectionService;
    private final BizPaymentApplyService bizPaymentApplyService;
    private final BizPurchaseOrderService bizPurchaseOrderService;

    @GetMapping("/overview")
    @Operation(summary = "经营报表概览（按自然月统计）")
    public Result<Map<String, Object>> overview(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        Map<String, Object> slice = monthlyFinancialSlice(ym);
        long projectCount = bizProjectService.count();

        long paymentPendingCount = bizPaymentApplyService.count(
                new LambdaQueryWrapper<BizPaymentApply>().in(BizPaymentApply::getStatus, 2, 3));
        QueryWrapper<BizPaymentApply> pendSum = new QueryWrapper<>();
        pendSum.select("IFNULL(SUM(total_amount),0) AS amt");
        pendSum.in("status", 2, 3);
        List<Map<String, Object>> pendRows = bizPaymentApplyService.listMaps(pendSum);
        BigDecimal paymentPendingAmount = pendRows != null && !pendRows.isEmpty()
                ? toBd(pendRows.get(0).get("amt"))
                : BigDecimal.ZERO;

        Map<String, Object> paymentApply = new HashMap<>();
        paymentApply.put("pendingCount", paymentPendingCount);
        paymentApply.put("pendingAmount", paymentPendingAmount);

        Map<String, Object> project = new HashMap<>();
        project.put("total", projectCount);

        Map<String, Object> data = new HashMap<>();
        data.put("period", ym.toString());
        data.put("project", project);
        data.put("paymentApply", paymentApply);
        data.putAll(slice);
        return Result.success(data);
    }

    @GetMapping("/trend")
    @Operation(summary = "近 N 个月关键指标趋势（用于图表）")
    public Result<Map<String, Object>> trend(
            @RequestParam(defaultValue = "6") int months) {
        if (months < 1) {
            months = 1;
        }
        if (months > 24) {
            months = 24;
        }
        List<Map<String, Object>> points = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            points.add(monthlyTrendPoint(ym));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("months", months);
        data.put("points", points);
        return Result.success(data);
    }

    /** 概览与趋势共用的「按月」财务切片（不含项目总数、全局待审付款） */
    private Map<String, Object> monthlyFinancialSlice(YearMonth ym) {
        Map<String, Object> m = monthlyTrendPoint(ym);
        Map<String, Object> reconciliation = new HashMap<>();
        reconciliation.put("statementCount", m.get("statementCount"));
        reconciliation.put("currentReceiptSum", m.get("receiptSum"));
        reconciliation.put("differenceSum", m.get("differenceSum"));

        Map<String, Object> expense = new HashMap<>();
        expense.put("amount", m.get("expenseAmount"));
        expense.put("count", m.get("expenseCount"));

        Map<String, Object> invoice = new HashMap<>();
        invoice.put("amount", m.get("invoiceAmount"));
        invoice.put("count", m.get("invoiceCount"));

        Map<String, Object> cost = new HashMap<>();
        cost.put("amount", m.get("costAmount"));

        Map<String, Object> purchase = new HashMap<>();
        purchase.put("orderCount", m.get("purchaseOrderCount"));

        Map<String, Object> out = new HashMap<>();
        out.put("reconciliation", reconciliation);
        out.put("expense", expense);
        out.put("invoice", invoice);
        out.put("cost", cost);
        out.put("purchase", purchase);
        return out;
    }

    private Map<String, Object> monthlyTrendPoint(YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);
        String periodStr = ym.toString();

        LambdaQueryWrapper<BizReconciliationStatement> stW = new LambdaQueryWrapper<BizReconciliationStatement>()
                .eq(BizReconciliationStatement::getPeriod, periodStr);
        long statementCount = bizReconciliationStatementService.count(stW);
        QueryWrapper<BizReconciliationStatement> stSum = new QueryWrapper<>();
        stSum.select("IFNULL(SUM(current_receipt),0) AS receiptSum", "IFNULL(SUM(difference_amount),0) AS differenceSum");
        stSum.eq("period", periodStr);
        List<Map<String, Object>> stRows = bizReconciliationStatementService.listMaps(stSum);
        BigDecimal receiptSum = BigDecimal.ZERO;
        BigDecimal diffSum = BigDecimal.ZERO;
        if (stRows != null && !stRows.isEmpty()) {
            receiptSum = toBd(stRows.get(0).get("receiptSum"));
            diffSum = toBd(stRows.get(0).get("differenceSum"));
        }

        QueryWrapper<BizExpenseReport> exQ = new QueryWrapper<>();
        exQ.select("IFNULL(SUM(total_amount),0) AS amt", "COUNT(*) AS cnt");
        exQ.between("created_at", startDt, endDt);
        List<Map<String, Object>> exRows = bizExpenseReportService.listMaps(exQ);
        BigDecimal expenseAmount = BigDecimal.ZERO;
        long expenseCount = 0;
        if (exRows != null && !exRows.isEmpty()) {
            expenseAmount = toBd(exRows.get(0).get("amt"));
            expenseCount = toLong(exRows.get(0).get("cnt"));
        }

        QueryWrapper<BizInvoice> invQ = new QueryWrapper<>();
        invQ.select("IFNULL(SUM(total_amount),0) AS amt", "COUNT(*) AS cnt");
        invQ.between("opened_date", start, end);
        List<Map<String, Object>> invRows = bizInvoiceService.listMaps(invQ);
        BigDecimal invoiceAmount = BigDecimal.ZERO;
        long invoiceCount = 0;
        if (invRows != null && !invRows.isEmpty()) {
            invoiceAmount = toBd(invRows.get(0).get("amt"));
            invoiceCount = toLong(invRows.get(0).get("cnt"));
        }

        QueryWrapper<BizCostCollection> costQ = new QueryWrapper<>();
        costQ.select("IFNULL(SUM(amount),0) AS amt");
        costQ.between("collected_date", start, end);
        List<Map<String, Object>> costRows = bizCostCollectionService.listMaps(costQ);
        BigDecimal costAmount = costRows != null && !costRows.isEmpty()
                ? toBd(costRows.get(0).get("amt"))
                : BigDecimal.ZERO;

        LambdaQueryWrapper<BizPurchaseOrder> poW = new LambdaQueryWrapper<>();
        poW.between(BizPurchaseOrder::getCreatedAt, startDt, endDt);
        long purchaseOrderCount = bizPurchaseOrderService.count(poW);

        Map<String, Object> p = new HashMap<>();
        p.put("period", periodStr);
        p.put("statementCount", statementCount);
        p.put("receiptSum", receiptSum);
        p.put("differenceSum", diffSum);
        p.put("expenseAmount", expenseAmount);
        p.put("expenseCount", expenseCount);
        p.put("invoiceAmount", invoiceAmount);
        p.put("invoiceCount", invoiceCount);
        p.put("costAmount", costAmount);
        p.put("purchaseOrderCount", purchaseOrderCount);
        return p;
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

    private static long toLong(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        return Long.parseLong(v.toString());
    }

    private static final int EXPORT_ROW_LIMIT = 10000;

    @GetMapping("/export/csv")
    @Operation(summary = "导出 CSV（报销/发票/成本/付款申请）")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam String type,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) Long projectId) {
        LocalDate d0 = LocalDate.parse(from.trim());
        LocalDate d1 = LocalDate.parse(to.trim());
        if (d1.isBefore(d0)) {
            LocalDate tmp = d0;
            d0 = d1;
            d1 = tmp;
        }
        String csv;
        String filename;
        switch (type) {
            case "expense":
                csv = buildExpenseCsv(d0, d1, projectId);
                filename = "expense-report.csv";
                break;
            case "invoice":
                csv = buildInvoiceCsv(d0, d1, projectId);
                filename = "invoice-report.csv";
                break;
            case "cost":
                csv = buildCostCsv(d0, d1, projectId);
                filename = "cost-report.csv";
                break;
            case "payment_apply":
                csv = buildPaymentApplyCsv(d0, d1, projectId);
                filename = "payment-apply-report.csv";
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type 需为 expense|invoice|cost|payment_apply");
        }
        byte[] body = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    private String buildExpenseCsv(LocalDate from, LocalDate to, Long projectId) {
        LambdaQueryWrapper<BizExpenseReport> w = new LambdaQueryWrapper<>();
        w.ge(BizExpenseReport::getCreatedAt, from.atStartOfDay());
        w.le(BizExpenseReport::getCreatedAt, to.atTime(LocalTime.MAX));
        if (projectId != null) {
            w.eq(BizExpenseReport::getProjectId, projectId);
        }
        w.orderByDesc(BizExpenseReport::getCreatedAt);
        w.last("LIMIT " + EXPORT_ROW_LIMIT);
        List<BizExpenseReport> list = bizExpenseReportService.list(w);
        StringBuilder sb = new StringBuilder();
        appendRow(sb, "id", "report_no", "reporter_name", "department_name", "project_name", "total_amount", "status",
                "created_at");
        for (BizExpenseReport r : list) {
            appendRow(sb, r.getId(), r.getReportNo(), r.getReporterName(), r.getDepartmentName(), r.getProjectName(),
                    r.getTotalAmount(), r.getStatus(), r.getCreatedAt());
        }
        return sb.toString();
    }

    private String buildInvoiceCsv(LocalDate from, LocalDate to, Long projectId) {
        LambdaQueryWrapper<BizInvoice> w = new LambdaQueryWrapper<>();
        w.between(BizInvoice::getOpenedDate, from, to);
        if (projectId != null) {
            w.eq(BizInvoice::getProjectId, projectId);
        }
        w.orderByDesc(BizInvoice::getOpenedDate);
        w.last("LIMIT " + EXPORT_ROW_LIMIT);
        List<BizInvoice> list = bizInvoiceService.list(w);
        StringBuilder sb = new StringBuilder();
        appendRow(sb, "id", "invoice_no", "invoice_type", "total_amount", "opened_date", "project_id", "is_verified",
                "created_at");
        for (BizInvoice r : list) {
            appendRow(sb, r.getId(), r.getInvoiceNo(), r.getInvoiceType(), r.getTotalAmount(), r.getOpenedDate(),
                    r.getProjectId(), r.getIsVerified(), r.getCreatedAt());
        }
        return sb.toString();
    }

    private String buildCostCsv(LocalDate from, LocalDate to, Long projectId) {
        LambdaQueryWrapper<BizCostCollection> w = new LambdaQueryWrapper<>();
        w.between(BizCostCollection::getCollectedDate, from, to);
        if (projectId != null) {
            w.eq(BizCostCollection::getProjectId, projectId);
        }
        w.orderByDesc(BizCostCollection::getCollectedDate);
        w.last("LIMIT " + EXPORT_ROW_LIMIT);
        List<BizCostCollection> list = bizCostCollectionService.list(w);
        StringBuilder sb = new StringBuilder();
        appendRow(sb, "id", "project_name", "cost_category", "cost_category_name", "amount", "source_type",
                "source_no", "collected_date", "period");
        for (BizCostCollection r : list) {
            appendRow(sb, r.getId(), r.getProjectName(), r.getCostCategory(), r.getCostCategoryName(), r.getAmount(),
                    r.getSourceType(), r.getSourceNo(), r.getCollectedDate(), r.getPeriod());
        }
        return sb.toString();
    }

    private String buildPaymentApplyCsv(LocalDate from, LocalDate to, Long projectId) {
        LambdaQueryWrapper<BizPaymentApply> w = new LambdaQueryWrapper<>();
        w.ge(BizPaymentApply::getCreatedAt, from.atStartOfDay());
        w.le(BizPaymentApply::getCreatedAt, to.atTime(LocalTime.MAX));
        if (projectId != null) {
            w.eq(BizPaymentApply::getProjectId, projectId);
        }
        w.orderByDesc(BizPaymentApply::getCreatedAt);
        w.last("LIMIT " + EXPORT_ROW_LIMIT);
        List<BizPaymentApply> list = bizPaymentApplyService.list(w);
        StringBuilder sb = new StringBuilder();
        appendRow(sb, "id", "apply_no", "category", "supplier_name", "total_amount", "status", "created_at");
        for (BizPaymentApply r : list) {
            appendRow(sb, r.getId(), r.getApplyNo(), r.getCategory(), r.getSupplierName(), r.getTotalAmount(),
                    r.getStatus(), r.getCreatedAt());
        }
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, Object... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(csvField(cells[i]));
        }
        sb.append('\n');
    }

    private static String csvField(Object o) {
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
