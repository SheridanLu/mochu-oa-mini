package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import com.mochu.oa.entity.SysDepartment;
import com.mochu.oa.service.SysDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/department")
@RequiredArgsConstructor
@Tag(name = "部门管理")
public class SysDepartmentController {
    
    private final SysDepartmentService sysDepartmentService;
    
    @GetMapping("/list")
    @Operation(summary = "获取部门列表")
    public Result<List<SysDepartment>> list() {
        List<SysDepartment> list = sysDepartmentService.list();
        return Result.success(list);
    }
    
    @GetMapping("/tree")
    @Operation(summary = "获取部门树")
    public Result<List<SysDepartment>> tree() {
        return Result.success(sysDepartmentService.buildDeptTree());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情")
    public Result<SysDepartment> getById(@PathVariable Long id) {
        SysDepartment dept = sysDepartmentService.getById(id);
        return Result.success(dept);
    }
    
    @PostMapping
    @Operation(summary = "创建部门")
    public Result<Void> create(@RequestBody SysDepartment dept) {
        sysDepartmentService.save(dept);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新部门")
    public Result<Void> update(@RequestBody SysDepartment dept) {
        sysDepartmentService.updateById(dept);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    public Result<Void> delete(@PathVariable Long id) {
        sysDepartmentService.removeById(id);
        return Result.success(null);
    }
}