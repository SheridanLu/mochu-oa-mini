package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {
    
    @TableField("permission_code")
    private String permissionCode;
    
    @TableField("permission_name")
    private String permissionName;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("permission_type")
    private Integer permissionType;
    
    @TableField("path")
    private String path;
    
    @TableField("component")
    private String component;
    
    @TableField("icon")
    private String icon;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("visible")
    private Integer visible;
    
    @TableField("status")
    private Integer status;
    
    @TableField(exist = false)
    private List<SysPermission> children;
}