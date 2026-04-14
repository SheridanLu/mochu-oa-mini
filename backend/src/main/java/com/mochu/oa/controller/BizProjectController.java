package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizProject;
import com.mochu.oa.service.BizProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Tag(name = "项目管理")
public class BizProjectController {
    
    private final BizProjectService bizProjectService;
    
    @GetMapping("/list")
    @Operation(summary = "获取项目列表")
    public Result<List<BizProject>> list() {
        List<BizProject> list = bizProjectService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询项目")
    public Result<Page<BizProject>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目名称") @RequestParam(required = false) String projectName,
            @Parameter(description = "项目状态") @RequestParam(required = false) Integer status) {
        Page<BizProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizProject> wrapper = new LambdaQueryWrapper<>();
        if (projectName != null) {
            wrapper.like(BizProject::getProjectName, projectName);
        }
        if (status != null) {
            wrapper.eq(BizProject::getStatus, status);
        }
        Page<BizProject> result = bizProjectService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情")
    public Result<BizProject> getById(@Parameter(description = "项目ID") @PathVariable Long id) {
        BizProject project = bizProjectService.getById(id);
        return Result.success(project);
    }
    
    @PostMapping
    @Operation(summary = "创建项目")
    public Result<Void> create(@RequestBody BizProject project) {
        bizProjectService.save(project);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新项目")
    public Result<Void> update(@RequestBody BizProject project) {
        bizProjectService.updateById(project);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目")
    public Result<Void> delete(@Parameter(description = "项目ID") @PathVariable Long id) {
        bizProjectService.removeById(id);
        return Result.success(null);
    }
}