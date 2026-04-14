package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizIncomeContractSplit;
import com.mochu.oa.entity.BizReconciliationStatement;
import com.mochu.oa.service.BizIncomeContractSplitService;
import com.mochu.oa.service.BizReconciliationStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statement")
@RequiredArgsConstructor
@Tag(name = "收入对账管理")
public class BizReconciliationStatementController {
    
    private final BizReconciliationStatementService bizReconciliationStatementService;
    private final BizIncomeContractSplitService bizIncomeContractSplitService;
    
    @GetMapping("/list")
    @Operation(summary = "获取对账单列表")
    public Result<List<BizReconciliationStatement>> list() {
        List<BizReconciliationStatement> list = bizReconciliationStatementService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询对账单")
    public Result<Page<BizReconciliationStatement>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "周期") @RequestParam(required = false) String period) {
        Page<BizReconciliationStatement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizReconciliationStatement> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizReconciliationStatement::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizReconciliationStatement::getStatus, status);
        }
        if (period != null) {
            wrapper.eq(BizReconciliationStatement::getPeriod, period);
        }
        wrapper.orderByDesc(BizReconciliationStatement::getCreatedAt);
        Page<BizReconciliationStatement> result = bizReconciliationStatementService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取对账单详情")
    public Result<BizReconciliationStatement> getById(@Parameter(description = "对账单ID") @PathVariable Long id) {
        BizReconciliationStatement statement = bizReconciliationStatementService.getById(id);
        return Result.success(statement);
    }
    
    @PostMapping
    @Operation(summary = "创建对账单")
    public Result<Void> create(@RequestBody BizReconciliationStatement statement) {
        bizReconciliationStatementService.save(statement);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新对账单")
    public Result<Void> update(@RequestBody BizReconciliationStatement statement) {
        bizReconciliationStatementService.updateById(statement);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除对账单")
    public Result<Void> delete(@Parameter(description = "对账单ID") @PathVariable Long id) {
        bizReconciliationStatementService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交对账单")
    public Result<Void> submit(@Parameter(description = "对账单ID") @PathVariable Long id) {
        BizReconciliationStatement statement = new BizReconciliationStatement();
        statement.setId(id);
        statement.setStatus(1);
        bizReconciliationStatementService.updateById(statement);
        return Result.success(null);
    }
    
    @GetMapping("/config/{projectId}")
    @Operation(summary = "获取对账定时配置")
    public Result<Map<String, Object>> getConfig(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        Map<String, Object> config = new HashMap<>();
        config.put("cycleType", "monthly");
        config.put("cycleDay", 25);
        config.put("isEnabled", true);
        return Result.success(config);
    }
    
    @PutMapping("/config/{projectId}")
    @Operation(summary = "更新对账定时配置")
    public Result<Void> updateConfig(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody Map<String, Object> config) {
        return Result.success(null);
    }
    
    @PostMapping("/generate-manual")
    @Operation(summary = "手动生成对账单")
    public Result<Void> generateManual(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @GetMapping("/difference/periods/{projectId}")
    @Operation(summary = "获取差异分析期次列表")
    public Result<List<String>> getDifferencePeriods(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        return Result.success(List.of("2026-01", "2026-02", "2026-03"));
    }
    
    @GetMapping("/difference/analysis")
    @Operation(summary = "差异分析")
    public Result<Map<String, Object>> getDifferenceAnalysis(
            @Parameter(description = "对账单ID") @RequestParam Long statementId,
            @Parameter(description = "对比期次") @RequestParam String comparePeriod) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("currentPeriod", "2026-03");
        analysis.put("comparePeriod", comparePeriod);
        analysis.put("currentProduction", 100000.00);
        analysis.put("compareProduction", 80000.00);
        analysis.put("differenceAmount", 20000.00);
        analysis.put("differenceRatio", 25.0);
        return Result.success(analysis);
    }
    
    @PostMapping("/difference/remark")
    @Operation(summary = "保存差异备注")
    public Result<Void> saveDifferenceRemark(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PostMapping("/difference/export")
    @Operation(summary = "导出差异分析")
    public Result<Map<String, String>> exportDifference(@RequestBody Map<String, Object> params) {
        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", "/exports/difference-2026-03.xlsx");
        return Result.success(result);
    }
    
    @PostMapping("/split")
    @Operation(summary = "收入拆分")
    public Result<Void> createSplit(@RequestBody Map<String, Object> params) {
        Long contractId = Long.valueOf(params.get("contractId").toString());
        List<Map<String, Object>> splitData = (List<Map<String, Object>>) params.get("splits");
        
        List<BizIncomeContractSplit> splits = splitData.stream().map(m -> {
            BizIncomeContractSplit split = new BizIncomeContractSplit();
            split.setGanttTaskId(m.get("ganttTaskId") != null ? Long.valueOf(m.get("ganttTaskId").toString()) : null);
            split.setTaskName(m.get("taskName") != null ? m.get("taskName").toString() : null);
            split.setAmount(new java.math.BigDecimal(m.get("amount").toString()));
            split.setProgressRatio(m.get("progressRatio") != null ? new java.math.BigDecimal(m.get("progressRatio").toString()) : null);
            return split;
        }).collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> validation = bizIncomeContractSplitService.validateSplit(contractId, splits);
        if (!(Boolean) validation.get("valid")) {
            return Result.error(validation.get("errors").toString());
        }
        
        bizIncomeContractSplitService.saveSplit(contractId, splits, 1L);
        return Result.success(null);
    }
    
    @GetMapping("/split/list")
    @Operation(summary = "获取收入拆分列表")
    public Result<List<BizIncomeContractSplit>> getSplitList(@Parameter(description = "合同ID") @RequestParam Long contractId) {
        List<BizIncomeContractSplit> list = bizIncomeContractSplitService.getByContractId(contractId);
        return Result.success(list);
    }
    
    @GetMapping("/split/validate")
    @Operation(summary = "校验拆分金额")
    public Result<Map<String, Object>> validateSplitAmount(@Parameter(description = "合同ID") @RequestParam Long contractId) {
        Map<String, Object> summary = bizIncomeContractSplitService.getSplitSummary(contractId);
        return Result.success(summary);
    }
    
    @GetMapping("/split/tasks")
    @Operation(summary = "获取甘特任务列表用于拆分")
    public Result<List<Map<String, Object>>> getGanttTasksForSplit(@Parameter(description = "项目ID") @RequestParam Long projectId) {
        return Result.success(List.of());
    }
}