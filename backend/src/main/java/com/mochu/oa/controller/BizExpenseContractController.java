package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.PageRequestGuard;
import com.mochu.oa.common.ParamGuard;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizExpenseContract;
import com.mochu.oa.service.BizExpenseContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contract/expense")
@RequiredArgsConstructor
@Tag(name = "支出合同管理")
public class BizExpenseContractController {
    
    private final BizExpenseContractService bizExpenseContractService;
    
    @GetMapping("/list")
    @Operation(summary = "获取支出合同列表")
    public Result<List<BizExpenseContract>> list() {
        List<BizExpenseContract> list = bizExpenseContractService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询支出合同")
    public Result<Page<BizExpenseContract>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "合同名称") @RequestParam(required = false) String contractName,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态（1-7）") @RequestParam(required = false) Integer status) {
        String pageErr = PageRequestGuard.validate(pageNum, pageSize);
        if (pageErr != null) {
            return Result.badRequest(pageErr);
        }
        pageSize = PageRequestGuard.normalizePageSize(pageSize, 500);
        String statusErr = ParamGuard.validateRange(status, 1, 7, "status");
        if (statusErr != null) {
            return Result.badRequest(statusErr);
        }
        Page<BizExpenseContract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizExpenseContract> wrapper = new LambdaQueryWrapper<>();
        if (contractName != null) {
            wrapper.like(BizExpenseContract::getContractName, contractName);
        }
        if (projectId != null) {
            wrapper.eq(BizExpenseContract::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizExpenseContract::getStatus, status);
        }
        Page<BizExpenseContract> result = bizExpenseContractService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取支出合同详情")
    public Result<BizExpenseContract> getById(@Parameter(description = "合同ID") @PathVariable Long id) {
        BizExpenseContract contract = bizExpenseContractService.getById(id);
        return Result.success(contract);
    }
    
    @PostMapping
    @Operation(summary = "创建支出合同")
    public Result<Void> create(@RequestBody BizExpenseContract contract) {
        bizExpenseContractService.save(contract);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新支出合同")
    public Result<Void> update(@RequestBody BizExpenseContract contract) {
        bizExpenseContractService.updateById(contract);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除支出合同")
    public Result<Void> delete(@Parameter(description = "合同ID") @PathVariable Long id) {
        bizExpenseContractService.removeById(id);
        return Result.success(null);
    }
}