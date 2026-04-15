package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.SysRole;
import com.mochu.oa.service.SysRoleService;
import com.mochu.oa.service.SysRolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class SysRoleController {
    
    private final SysRoleService sysRoleService;
    private final SysRolePermissionService sysRolePermissionService;
    
    @GetMapping("/list")
    @Operation(summary = "分页查询角色")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<SysRole> pageResult = sysRoleService.pageRoles(roleName, status, page, size);
        return Result.success(Map.of(
                "list", pageResult.getRecords(),
                "total", pageResult.getTotal(),
                "pages", pageResult.getPages(),
                "current", pageResult.getCurrent()
        ));
    }

    @GetMapping("/select-list")
    @Operation(summary = "角色下拉（分配角色等，不分页）")
    public Result<List<SysRole>> selectList() {
        return Result.success(sysRoleService.listForSelect());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "创建角色")
    public Result<Long> create(@RequestBody SysRole role) {
        if (!StringUtils.hasText(role.getRoleCode())) {
            return Result.badRequest("角色编码不能为空");
        }
        if (!StringUtils.hasText(role.getRoleName())) {
            return Result.badRequest("角色名称不能为空");
        }
        if (role.getRoleType() == null) {
            role.setRoleType(1);
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getDataScope() == null) {
            role.setDataScope(3);
        }
        sysRoleService.save(role);
        return Result.success(role.getId());
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