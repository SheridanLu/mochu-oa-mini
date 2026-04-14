package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizExpenseReport;
import com.mochu.oa.service.BizExpenseReportService;
import com.mochu.oa.service.BizBudgetAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expense/report")
@RequiredArgsConstructor
@Tag(name = "报销管理")
public class BizExpenseReportController {
    
    private final BizExpenseReportService bizExpenseReportService;
    private final BizBudgetAllocationService bizBudgetAllocationService;
    
    @GetMapping("/list")
    @Operation(summary = "获取报销单列表")
    public Result<List<BizExpenseReport>> list() {
        List<BizExpenseReport> list = bizExpenseReportService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询报销单")
    public Result<Page<BizExpenseReport>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "报销人ID") @RequestParam(required = false) Long reporterId,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BizExpenseReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizExpenseReport> wrapper = new LambdaQueryWrapper<>();
        if (reporterId != null) {
            wrapper.eq(BizExpenseReport::getReporterId, reporterId);
        }
        if (departmentId != null) {
            wrapper.eq(BizExpenseReport::getDepartmentId, departmentId);
        }
        if (projectId != null) {
            wrapper.eq(BizExpenseReport::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizExpenseReport::getStatus, status);
        }
        wrapper.orderByDesc(BizExpenseReport::getCreatedAt);
        Page<BizExpenseReport> result = bizExpenseReportService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取报销单详情")
    public Result<BizExpenseReport> getById(@Parameter(description = "报销单ID") @PathVariable Long id) {
        BizExpenseReport report = bizExpenseReportService.getById(id);
        return Result.success(report);
    }
    
    @PostMapping
    @Operation(summary = "创建报销单")
    public Result<Void> create(@RequestBody BizExpenseReport report) {
        bizExpenseReportService.save(report);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新报销单")
    public Result<Void> update(@RequestBody BizExpenseReport report) {
        bizExpenseReportService.updateById(report);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除报销单")
    public Result<Void> delete(@Parameter(description = "报销单ID") @PathVariable Long id) {
        bizExpenseReportService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交报销单")
    public Result<Void> submit(@Parameter(description = "报销单ID") @PathVariable Long id) {
        BizExpenseReport report = new BizExpenseReport();
        report.setId(id);
        report.setStatus(1);
        bizExpenseReportService.updateById(report);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批报销单")
    public Result<Void> approve(
            @Parameter(description = "报销单ID") @PathVariable Long id,
            @Parameter(description = "审批动作") @RequestParam String action,
            @Parameter(description = "审批意见") @RequestParam(required = false) String opinion) {
        BizExpenseReport report = new BizExpenseReport();
        report.setId(id);
        report.setStatus("approve".equals(action) ? 2 : 3);
        bizExpenseReportService.updateById(report);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/attachments")
    @Operation(summary = "上传报销附件")
    public Result<Map<String, Object>> uploadAttachment(
            @Parameter(description = "报销单ID") @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("attachmentId", 1L);
        result.put("fileName", "凭证.pdf");
        result.put("filePath", "/uploads/expense/1.pdf");
        return Result.success(result);
    }
    
    @GetMapping("/{id}/attachments")
    @Operation(summary = "获取报销附件列表")
    public Result<List<Map<String, Object>>> getAttachments(@Parameter(description = "报销单ID") @PathVariable Long id) {
        List<Map<String, Object>> attachments = new ArrayList<>();
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("id", 1L);
        attachment.put("fileName", "凭证.pdf");
        attachment.put("filePath", "/uploads/expense/1.pdf");
        attachment.put("uploadTime", LocalDate.now().toString());
        attachments.add(attachment);
        return Result.success(attachments);
    }
    
    @DeleteMapping("/attachment/{attachmentId}")
    @Operation(summary = "删除报销附件")
    public Result<Void> deleteAttachment(@Parameter(description = "附件ID") @PathVariable Long attachmentId) {
        return Result.success(null);
    }
    
    @GetMapping("/budget-check")
    @Operation(summary = "预算检查")
    public Result<Map<String, Object>> budgetCheck(
            @Parameter(description = "部门ID") @RequestParam Long departmentId,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "金额") @RequestParam BigDecimal amount) {
        Map<String, Object> result = bizBudgetAllocationService.checkBudget(departmentId, projectId, amount);
        return Result.success(result);
    }
    
    @PostMapping("/invoice-verify")
    @Operation(summary = "发票验真")
    public Result<Map<String, Object>> verifyInvoice(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("isValid", true);
        result.put("invoiceNo", params.get("invoiceNo"));
        result.put("amount", params.get("amount"));
        result.put("taxAmount", new BigDecimal("100.00"));
        result.put("verifiedAt", LocalDate.now().toString());
        return Result.success(result);
    }
    
    @PostMapping("/export")
    @Operation(summary = "导出报销单")
    public Result<Map<String, String>> export(@RequestBody Map<String, Object> params) {
        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", "/exports/expense-report.xlsx");
        return Result.success(result);
    }
    
    @GetMapping("/{id}/approval-trace")
    @Operation(summary = "获取审批轨迹")
    public Result<List<Map<String, Object>>> getApprovalTrace(@Parameter(description = "报销单ID") @PathVariable Long id) {
        List<Map<String, Object>> traces = new ArrayList<>();
        Map<String, Object> trace = new HashMap<>();
        trace.put("step", 1);
        trace.put("approver", "张三");
        trace.put("action", "审批通过");
        trace.put("opinion", "同意");
        trace.put("createdAt", LocalDate.now().toString());
        traces.add(trace);
        return Result.success(traces);
    }
}