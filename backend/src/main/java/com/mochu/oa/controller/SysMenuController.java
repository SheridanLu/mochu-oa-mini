package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import com.mochu.oa.entity.SysPermission;
import com.mochu.oa.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理")
public class SysMenuController {
    
    private final SysPermissionService sysPermissionService;
    
    @GetMapping("/list")
    @Operation(summary = "获取菜单列表")
    public Result<List<SysPermission>> list() {
        return Result.success(sysPermissionService.list());
    }
    
    @GetMapping("/tree")
    @Operation(summary = "获取菜单树")
    public Result<List<SysPermission>> getMenuTree() {
        List<SysPermission> menus = sysPermissionService.list();
        return Result.success(sysPermissionService.buildMenuTree(menus));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户菜单")
    public Result<List<SysPermission>> getUserMenus(@PathVariable Long userId) {
        return Result.success(sysPermissionService.getMenusByUserId(userId));
    }
    
    @PostMapping
    @Operation(summary = "创建菜单")
    public Result<Void> create(@RequestBody SysPermission permission) {
        sysPermissionService.save(permission);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新菜单")
    public Result<Void> update(@RequestBody SysPermission permission) {
        sysPermissionService.updateById(permission);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    public Result<Void> delete(@PathVariable Long id) {
        sysPermissionService.removeById(id);
        return Result.success(null);
    }
}