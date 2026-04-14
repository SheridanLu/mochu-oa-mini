package com.mochu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.SysRolePermission;
import com.mochu.oa.mapper.SysRolePermissionMapper;
import com.mochu.oa.service.SysRolePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {
    
    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        List<SysRolePermission> list = list(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        return list.stream().map(SysRolePermission::getPermissionId).toList();
    }
    
    @Override
    @Transactional
    public void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        remove(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        for (Long permissionId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            save(rp);
        }
    }
}