package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import com.mochu.oa.entity.SysRole;
import com.mochu.oa.entity.SysRolePermission;
import com.mochu.oa.service.SysRoleService;
import com.mochu.oa.service.SysRolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class SysRoleController {
    
    private final SysRoleService sysRoleService;
    private final SysRolePermissionService sysRolePermissionService;
    
    @GetMapping("/list")
    @Operation(summary = "获取角色列表")
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleService.list());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "创建角色")
    public Result<Void> create(@RequestBody SysRole role) {
        sysRoleService.save(role);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新角色")
    public Result<Void> update(@RequestBody SysRole role) {
        sysRoleService.updateById(role);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return Result.success(null);
    }
    
    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色权限")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        List<Long> permissionIds = sysRolePermissionService.getPermissionIdsByRoleId(id);
        return Result.success(permissionIds);
    }
    
    @PostMapping("/{id}/permissions")
    @Operation(summary = "保存角色权限")
    public Result<Void> savePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        sysRolePermissionService.saveRolePermissions(id, permissionIds);
        return Result.success(null);
    }
}