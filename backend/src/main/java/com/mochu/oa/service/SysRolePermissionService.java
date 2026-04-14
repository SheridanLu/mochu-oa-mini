package com.mochu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mochu.oa.entity.SysRolePermission;

import java.util.List;

public interface SysRolePermissionService extends IService<SysRolePermission> {
    
    List<Long> getPermissionIdsByRoleId(Long roleId);
    
    void saveRolePermissions(Long roleId, List<Long> permissionIds);
}