package com.mochu.oa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {
    
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
}