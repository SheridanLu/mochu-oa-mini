package com.mochu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mochu.oa.entity.SysPermission;

import java.util.List;

public interface SysPermissionService extends IService<SysPermission> {
    
    List<SysPermission> getMenusByUserId(Long userId);
    
    List<SysPermission> buildMenuTree(List<SysPermission> permissions);
}