package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class SysUserController {
    
    private final SysUserService sysUserService;
    
    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    public Result<List<SysUser>> list() {
        List<SysUser> list = sysUserService.list();
        return Result.success(list);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.success(user);
    }
    
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<Void> create(@RequestBody SysUser user) {
        sysUserService.save(user);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新用户")
    public Result<Void> update(@RequestBody SysUser user) {
        sysUserService.updateById(user);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success(null);
    }
}