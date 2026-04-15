package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizApprovalDef;
import com.mochu.oa.entity.BizApprovalInstance;
import com.mochu.oa.entity.BizApprovalTodo;
import com.mochu.oa.service.BizApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
@Tag(name = "审批流程")
public class BizApprovalController {
    
    private final BizApprovalService bizApprovalService;
    
    @PostMapping("/start")
    @Operation(summary = "启动审批流程")
    public Result<Long> startApproval(
            @Parameter(description = "业务类型") @RequestParam String bizType,
            @Parameter(description = "业务ID") @RequestParam Long bizId,
            @Parameter(description = "审批标题") @RequestParam String title,
            @Parameter(description = "申请人ID") @RequestParam Long applicantId,
            @Parameter(description = "申请人姓名") @RequestParam String applicantName) {
        Long instanceId = bizApprovalService.startApproval(bizType, bizId, title, applicantId, applicantName);
        return Result.success(instanceId);
    }
    
    @PostMapping("/{instanceId}/approve")
    @Operation(summary = "审批操作")
    public Result<Void> approve(
            @Parameter(description = "审批实例ID") @PathVariable Long instanceId,
            @Parameter(description = "审批人ID") @RequestParam Long approverId,
            @Parameter(description = "审批人姓名") @RequestParam String approverName,
            @Parameter(description = "审批动作") @RequestParam String action,
            @Parameter(description = "审批意见") @RequestParam(required = false) String opinion) {
        bizApprovalService.approve(instanceId, approverId, approverName, action, opinion);
        return Result.success(null);
    }
    
    @PostMapping("/{instanceId}/delegate")
    @Operation(summary = "转办")
    public Result<Void> delegate(
            @PathVariable Long instanceId,
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId,
            @RequestParam(required = false) String opinion) {
        bizApprovalService.delegate(instanceId, fromUserId, toUserId, opinion);
        return Result.success(null);
    }
    
    @PostMapping("/{instanceId}/withdraw")
    @Operation(summary = "撤回")
    public Result<Void> withdraw(
            @PathVariable Long instanceId,
            @RequestParam Long userId) {
        bizApprovalService.withdraw(instanceId, userId);
        return Result.success(null);
    }
    
    @PostMapping("/{instanceId}/cancel")
    @Operation(summary = "取消审批")
    public Result<Void> cancel(@PathVariable Long instanceId) {
        bizApprovalService.cancel(instanceId);
        return Result.success(null);
    }
    
    @GetMapping("/todo")
    @Operation(summary = "获取待办列表")
    public Result<Map<String, Object>> getTodoList(
            @RequestParam Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> pageData = bizApprovalService.getTodoList(userId, category, bizType, keyword, page, size);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) pageData.getOrDefault("list", List.of());
        long total = pageData.get("total") instanceof Number n ? n.longValue() : 0L;
        long todoCount = bizApprovalService.getTodoCount(userId);
        long doneCount = bizApprovalService.getDoneCount(userId);
        long readCount = bizApprovalService.getReadCount(userId);
        Map<String, Object> body = new HashMap<>();
        body.put("list", list);
        body.put("total", total);
        body.put("todoCount", todoCount);
        body.put("doneCount", doneCount);
        body.put("readCount", readCount);
        return Result.success(body);
    }
    
    @GetMapping("/todo/count")
    @Operation(summary = "获取待办数量")
    public Result<Map<String, Long>> getTodoCount(@RequestParam Long userId) {
        return Result.success(Map.of(
                "todoCount", bizApprovalService.getTodoCount(userId),
                "doneCount", bizApprovalService.getDoneCount(userId),
                "readCount", bizApprovalService.getReadCount(userId)
        ));
    }
    
    @GetMapping("/{instanceId}/history")
    @Operation(summary = "获取审批历史")
    public Result<List<Map<String, Object>>> getHistory(@PathVariable Long instanceId) {
        return Result.success(bizApprovalService.getHistory(instanceId));
    }
    
    @GetMapping("/def/{bizType}")
    @Operation(summary = "获取流程定义")
    public Result<BizApprovalDef> getFlowDef(@PathVariable String bizType) {
        return Result.success(bizApprovalService.getFlowDef(bizType));
    }

    @GetMapping("/def/list")
    @Operation(summary = "获取流程定义列表")
    public Result<List<BizApprovalDef>> listFlowDefs() {
        return Result.success(bizApprovalService.listFlowDefs());
    }

    @PostMapping("/def/save")
    @Operation(summary = "保存流程定义")
    public Result<Void> saveFlowDef(@RequestBody BizApprovalDef def) {
        bizApprovalService.saveFlowDef(def);
        return Result.success(null);
    }
    
    @PostMapping("/def/init")
    @Operation(summary = "初始化流程定义")
    public Result<Void> initFlowDefinitions() {
        bizApprovalService.initFlowDefinitions();
        return Result.success(null);
    }
}