package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizIncomeContract;
import com.mochu.oa.service.BizIncomeContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contract/income")
@RequiredArgsConstructor
@Tag(name = "收入合同管理")
public class BizIncomeContractController {
    
    private final BizIncomeContractService bizIncomeContractService;
    
    @GetMapping("/list")
    @Operation(summary = "获取收入合同列表")
    public Result<List<BizIncomeContract>> list() {
        List<BizIncomeContract> list = bizIncomeContractService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询收入合同")
    public Result<Page<BizIncomeContract>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "合同名称") @RequestParam(required = false) String contractName,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId) {
        Page<BizIncomeContract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizIncomeContract> wrapper = new LambdaQueryWrapper<>();
        if (contractName != null) {
            wrapper.like(BizIncomeContract::getContractName, contractName);
        }
        if (projectId != null) {
            wrapper.eq(BizIncomeContract::getProjectId, projectId);
        }
        Page<BizIncomeContract> result = bizIncomeContractService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取收入合同详情")
    public Result<BizIncomeContract> getById(@Parameter(description = "合同ID") @PathVariable Long id) {
        BizIncomeContract contract = bizIncomeContractService.getById(id);
        return Result.success(contract);
    }
    
    @PostMapping
    @Operation(summary = "创建收入合同")
    public Result<Void> create(@RequestBody BizIncomeContract contract) {
        bizIncomeContractService.save(contract);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新收入合同")
    public Result<Void> update(@RequestBody BizIncomeContract contract) {
        bizIncomeContractService.updateById(contract);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收入合同")
    public Result<Void> delete(@Parameter(description = "合同ID") @PathVariable Long id) {
        bizIncomeContractService.removeById(id);
        return Result.success(null);
    }
}