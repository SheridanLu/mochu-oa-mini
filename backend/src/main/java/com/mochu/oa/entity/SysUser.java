package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    
    @TableField("username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("real_name")
    private String realName;
    
    @TableField("phone")
    private String phone;
    
    @TableField("email")
    private String email;
    
    @TableField("avatar")
    private String avatar;
    
    @TableField("department_id")
    private Long departmentId;
    
    @TableField("department_name")
    private String departmentName;
    
    @TableField("position")
    private String position;
    
    @TableField("status")
    private Integer status;
    
    @TableField("lock_until")
    private LocalDateTime lockUntil;
    
    @TableField("login_attempts")
    private Integer loginAttempts;
    
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    
    @TableField("last_login_ip")
    private String lastLoginIp;
}