package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    
    @TableField("role_code")
    private String roleCode;
    
    @TableField("role_name")
    private String roleName;
    
    @TableField("role_type")
    private Integer roleType;
    
    @TableField("data_scope")
    private Integer dataScope;
    
    @TableField("dept_ids")
    private String deptIds;
    
    @TableField("status")
    private Integer status;
    
    @TableField("remark")
    private String remark;

    /** 拥有该角色的用户数（非表字段） */
    @TableField(exist = false)
    private Integer userCount;
}