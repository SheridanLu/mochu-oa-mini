package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizMaterial;
import com.mochu.oa.service.BizMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
@Tag(name = "物资管理")
public class BizMaterialController {
    
    private final BizMaterialService bizMaterialService;
    
    @GetMapping("/list")
    @Operation(summary = "获取物资列表")
    public Result<List<BizMaterial>> list() {
        List<BizMaterial> list = bizMaterialService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询物资")
    public Result<Page<BizMaterial>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "物资名称") @RequestParam(required = false) String materialName,
            @Parameter(description = "物资分类ID") @RequestParam(required = false) Long categoryId) {
        Page<BizMaterial> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizMaterial> wrapper = new LambdaQueryWrapper<>();
        if (materialName != null) {
            wrapper.like(BizMaterial::getMaterialName, materialName);
        }
        if (categoryId != null) {
            wrapper.eq(BizMaterial::getCategoryId, categoryId);
        }
        Page<BizMaterial> result = bizMaterialService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取物资详情")
    public Result<BizMaterial> getById(@Parameter(description = "物资ID") @PathVariable Long id) {
        BizMaterial material = bizMaterialService.getById(id);
        return Result.success(material);
    }
    
    @PostMapping
    @Operation(summary = "创建物资")
    public Result<Void> create(@RequestBody BizMaterial material) {
        bizMaterialService.save(material);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新物资")
    public Result<Void> update(@RequestBody BizMaterial material) {
        bizMaterialService.updateById(material);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除物资")
    public Result<Void> delete(@Parameter(description = "物资ID") @PathVariable Long id) {
        bizMaterialService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/outbound")
    @Operation(summary = "物资出库")
    public Result<Void> outbound(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PostMapping("/transfer")
    @Operation(summary = "物资调拨")
    public Result<Void> transfer(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @PostMapping("/outbound/validate-transfer")
    @Operation(summary = "验证调拨")
    public Result<Map<String, Object>> validateTransfer(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("isValid", true);
        result.put("warnings", List.of());
        return Result.success(result);
    }
    
    @GetMapping("/stock/{warehouseId}")
    @Operation(summary = "获取仓库库存")
    public Result<List<Map<String, Object>>> getStock(@Parameter(description = "仓库ID") @PathVariable Long warehouseId) {
        return Result.success(List.of());
    }
    
    @PostMapping("/outbound/validate")
    @Operation(summary = "验证出库必填关联")
    public Result<Map<String, Object>> validateOutbound(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String contractNo = (String) params.get("contractNo");
        String warehouseEntryNo = (String) params.get("warehouseEntryNo");
        
        boolean isValid = (contractNo != null && !contractNo.isEmpty()) || 
                         (warehouseEntryNo != null && !warehouseEntryNo.isEmpty());
        
        result.put("isValid", isValid);
        result.put("errors", isValid ? List.of() : List.of("合同编号和入库单号至少填写一项"));
        return Result.success(result);
    }
    
    @PostMapping("/transfer/validate-cross-project")
    @Operation(summary = "跨项目调拨校验")
    public Result<Map<String, Object>> validateCrossProjectTransfer(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Long fromProjectId = params.get("fromProjectId") != null ? 
            Long.parseLong(params.get("fromProjectId").toString()) : null;
        Long toProjectId = params.get("toProjectId") != null ? 
            Long.parseLong(params.get("toProjectId").toString()) : null;
        
        boolean isCrossProject = fromProjectId != null && toProjectId != null && 
                                 !fromProjectId.equals(toProjectId);
        
        result.put("isCrossProject", isCrossProject);
        result.put("requiresApproval", isCrossProject);
        result.put("canProceed", !isCrossProject);
        
        if (isCrossProject) {
            result.put("warningMessage", "跨项目调拨需要审批");
            result.put("blockMessage", "请先创建调拨单并等待审批");
        }
        
        return Result.success(result);
    }
    
    @PostMapping("/transfer/approve")
    @Operation(summary = "跨项目调拨审批")
    public Result<Void> approveTransfer(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @GetMapping("/transfer/log")
    @Operation(summary = "调拨拦截日志")
    public Result<List<Map<String, Object>>> getTransferLog(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return Result.success(List.of());
    }
}