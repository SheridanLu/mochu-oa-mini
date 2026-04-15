package com.mochu.oa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.common.PageRequestGuard;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.entity.SysUserRole;
import com.mochu.oa.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {
    
    private final SysUserRoleService sysUserRoleService;

    public IPage<SysUser> pageUsers(String username, String realName, Integer status, int page, int size) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            w.like(SysUser::getUsername, username.trim());
        }
        if (StringUtils.hasText(realName)) {
            w.like(SysUser::getRealName, realName.trim());
        }
        if (status != null) {
            w.eq(SysUser::getStatus, status);
        }
        w.orderByDesc(SysUser::getId);
        int pageNum = Math.max(1, page);
        int pageSize = PageRequestGuard.normalizePageSize(size, 100);
        return page(new Page<>(pageNum, pageSize), w);
    }

    /** 部门负责人等下拉：不分页 */
    public List<SysUser> listForSelect() {
        return list(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1)
                .select(SysUser::getId, SysUser::getRealName, SysUser::getUsername)
                .orderByAsc(SysUser::getRealName));
    }
    
    public SysUser findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }
    
    public SysUser login(String username, String password, PasswordEncoder passwordEncoder) {
        SysUser user = findByUsername(username);
        if (user == null) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        if (user.getStatus() != 1) {
            return null;
        }
        return user;
    }
    
    public void updateLoginInfo(Long userId, String ip) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        updateById(user);
    }
    
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = roleIds.stream().map(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            }).collect(java.util.stream.Collectors.toList());
            sysUserRoleService.saveBatch(userRoles);
        }
    }
}