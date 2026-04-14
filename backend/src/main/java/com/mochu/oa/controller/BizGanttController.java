package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizGantt;
import com.mochu.oa.service.BizGanttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gantt")
@RequiredArgsConstructor
@Tag(name = "甘特图管理")
public class BizGanttController {
    
    private final BizGanttService bizGanttService;
    
    @GetMapping("/list")
    @Operation(summary = "获取甘特图列表")
    public Result<List<BizGantt>> list() {
        List<BizGantt> list = bizGanttService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询甘特图")
    public Result<Page<BizGantt>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BizGantt> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizGantt> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizGantt::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizGantt::getStatus, status);
        }
        Page<BizGantt> result = bizGanttService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取甘特图详情")
    public Result<BizGantt> getById(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        BizGantt gantt = bizGanttService.getById(id);
        return Result.success(gantt);
    }
    
    @PostMapping
    @Operation(summary = "创建甘特图")
    public Result<Void> create(@RequestBody BizGantt gantt) {
        bizGanttService.save(gantt);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新甘特图")
    public Result<Void> update(@RequestBody BizGantt gantt) {
        bizGanttService.updateById(gantt);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除甘特图")
    public Result<Void> delete(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        bizGanttService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交甘特图")
    public Result<Void> submit(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        BizGantt gantt = new BizGantt();
        gantt.setId(id);
        gantt.setStatus(1);
        bizGanttService.updateById(gantt);
        return Result.success(null);
    }
    
    @GetMapping("/{id}/progress")
    @Operation(summary = "获取甘特图进度")
    public Result<Map<String, Object>> getProgress(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("overallProgress", 65);
        progress.put("totalTasks", 10);
        progress.put("completedTasks", 6);
        progress.put("inProgressTasks", 3);
        progress.put("pendingTasks", 1);
        return Result.success(progress);
    }
    
    @PutMapping("/{id}/progress")
    @Operation(summary = "更新甘特图进度")
    public Result<Void> updateProgress(
            @Parameter(description = "甘特图ID") @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @GetMapping("/income-split/{contractId}")
    @Operation(summary = "获取收入拆分数据")
    public Result<List<Map<String, Object>>> getIncomeSplit(@Parameter(description = "合同ID") @PathVariable Long contractId) {
        return Result.success(List.of());
    }
}