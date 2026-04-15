package com.mochu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.SysPermission;
import com.mochu.oa.entity.SysRolePermission;
import com.mochu.oa.entity.SysUserRole;
import com.mochu.oa.mapper.SysPermissionMapper;
import com.mochu.oa.service.SysPermissionService;
import com.mochu.oa.service.SysRolePermissionService;
import com.mochu.oa.service.SysUserRoleService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {
    
    private final SysUserRoleService sysUserRoleService;
    private final SysRolePermissionService sysRolePermissionService;
    
    public SysPermissionServiceImpl(
            SysUserRoleService sysUserRoleService,
            SysRolePermissionService sysRolePermissionService
    ) {
        this.sysUserRoleService = sysUserRoleService;
        this.sysRolePermissionService = sysRolePermissionService;
    }
    
    @Override
    public List<SysPermission> getMenusByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> permissionIds = sysRolePermissionService.list(new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<SysPermission> permissions = list(new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getId, permissionIds)
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSortOrder));
        
        return buildMenuTree(permissions);
    }
    
    @Override
    public List<SysPermission> buildMenuTree(List<SysPermission> permissions) {
        Map<Long, List<SysPermission>> parentMap = permissions.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));
        
        return buildTree(0L, parentMap);
    }
    
    private List<SysPermission> buildTree(Long parentId, Map<Long, List<SysPermission>> parentMap) {
        List<SysPermission> children = parentMap.getOrDefault(parentId, Collections.emptyList());
        children.forEach(p -> p.setChildren(buildTree(p.getId(), parentMap)));
        return children;
    }
}