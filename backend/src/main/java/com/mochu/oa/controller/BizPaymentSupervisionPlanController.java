package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizPaymentSupervisionPlan;
import com.mochu.oa.service.BizPaymentSupervisionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-supervision")
@RequiredArgsConstructor
@Tag(name = "回款督办管理")
public class BizPaymentSupervisionPlanController {
    
    private final BizPaymentSupervisionPlanService bizPaymentSupervisionPlanService;
    
    @GetMapping("/list")
    @Operation(summary = "获取回款督办计划列表")
    public Result<List<BizPaymentSupervisionPlan>> list() {
        List<BizPaymentSupervisionPlan> list = bizPaymentSupervisionPlanService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询回款督办计划")
    public Result<Page<BizPaymentSupervisionPlan>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "合同ID") @RequestParam(required = false) Long contractId,
            @Parameter(description = "优先级 1高2中3低") @RequestParam(required = false) Integer priority,
            @Parameter(description = "督办状态") @RequestParam(required = false) Integer supervisionStatus,
            @Parameter(description = "审批状态") @RequestParam(required = false) Integer approvalStatus,
            @Parameter(description = "超期等级: warning>0 serious>7") @RequestParam(required = false) String overdueLevel) {
        Page<BizPaymentSupervisionPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizPaymentSupervisionPlan> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizPaymentSupervisionPlan::getProjectId, projectId);
        }
        if (contractId != null) {
            wrapper.eq(BizPaymentSupervisionPlan::getContractId, contractId);
        }
        if (priority != null) {
            wrapper.eq(BizPaymentSupervisionPlan::getPriority, priority);
        }
        if (supervisionStatus != null) {
            wrapper.eq(BizPaymentSupervisionPlan::getSupervisionStatus, supervisionStatus);
        }
        if (approvalStatus != null) {
            wrapper.eq(BizPaymentSupervisionPlan::getApprovalStatus, approvalStatus);
        }
        if ("warning".equalsIgnoreCase(overdueLevel)) {
            wrapper.gt(BizPaymentSupervisionPlan::getOverdueDays, 0);
        } else if ("serious".equalsIgnoreCase(overdueLevel)) {
            wrapper.gt(BizPaymentSupervisionPlan::getOverdueDays, 7);
        }
        wrapper.orderByDesc(BizPaymentSupervisionPlan::getCreatedAt);
        Page<BizPaymentSupervisionPlan> result = bizPaymentSupervisionPlanService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/summary")
    @Operation(summary = "督办列表统计卡片")
    public Result<Map<String, Object>> summary() {
        List<BizPaymentSupervisionPlan> all = bizPaymentSupervisionPlanService.list();
        long inProgress = all.stream().filter(p -> p.getSupervisionStatus() != null && p.getSupervisionStatus() == 2).count();
        long overdue = all.stream().filter(p -> p.getOverdueDays() != null && p.getOverdueDays() > 0).count();
        BigDecimal gapSum = all.stream()
                .map(p -> p.getGapAmount() != null ? p.getGapAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> m = new HashMap<>();
        m.put("inProgress", inProgress);
        m.put("overdue", overdue);
        m.put("gapTotal", gapSum);
        return Result.success(m);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取回款督办计划详情")
    public Result<BizPaymentSupervisionPlan> getById(@Parameter(description = "计划ID") @PathVariable Long id) {
        BizPaymentSupervisionPlan plan = bizPaymentSupervisionPlanService.getById(id);
        return Result.success(plan);
    }
    
    @PostMapping
    @Operation(summary = "创建回款督办计划")
    public Result<Void> create(@RequestBody BizPaymentSupervisionPlan plan) {
        bizPaymentSupervisionPlanService.save(plan);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新回款督办计划")
    public Result<Void> update(@RequestBody BizPaymentSupervisionPlan plan) {
        bizPaymentSupervisionPlanService.updateById(plan);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除回款督办计划")
    public Result<Void> delete(@Parameter(description = "计划ID") @PathVariable Long id) {
        bizPaymentSupervisionPlanService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交审批")
    public Result<Void> submit(@Parameter(description = "计划ID") @PathVariable Long id) {
        BizPaymentSupervisionPlan plan = new BizPaymentSupervisionPlan();
        plan.setId(id);
        plan.setSupervisionStatus(1);
        bizPaymentSupervisionPlanService.updateById(plan);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批")
    public Result<Void> approve(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @Parameter(description = "审批意见") @RequestParam String action,
            @Parameter(description = "意见") @RequestParam(required = false) String opinion) {
        BizPaymentSupervisionPlan plan = new BizPaymentSupervisionPlan();
        plan.setId(id);
        plan.setSupervisionStatus("approve".equals(action) ? 2 : 3);
        bizPaymentSupervisionPlanService.updateById(plan);
        return Result.success(null);
    }
    
    @GetMapping("/precheck/{projectId}")
    @Operation(summary = "数据预检")
    public Result<Map<String, Object>> precheck(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasContract", true);
        result.put("hasGantt", true);
        result.put("hasCompletionData", true);
        result.put("hasPaymentData", true);
        result.put("warnings", List.of());
        return Result.success(result);
    }
    
    @PostMapping("/generate")
    @Operation(summary = "生成督办计划")
    public Result<Void> generate(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PostMapping("/{id}/assign")
    @Operation(summary = "转办")
    public Result<Void> assign(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PostMapping("/{id}/accept")
    @Operation(summary = "接收任务")
    public Result<Void> accept(@Parameter(description = "计划ID") @PathVariable Long id) {
        return Result.success(null);
    }
    
    @PostMapping("/{id}/feedback")
    @Operation(summary = "提交执行反馈")
    public Result<Void> feedback(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PutMapping("/feedback/{feedbackId}")
    @Operation(summary = "更新执行反馈")
    public Result<Void> updateFeedback(
            @Parameter(description = "反馈ID") @PathVariable Long feedbackId,
            @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PostMapping("/export")
    @Operation(summary = "导出督办计划")
    public Result<Map<String, String>> export(@RequestBody Map<String, Object> params) {
        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", "/exports/payment-supervision.xlsx");
        return Result.success(result);
    }
}