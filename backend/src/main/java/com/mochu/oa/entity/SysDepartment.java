package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class SysDepartment extends BaseEntity {
    
    @TableField("dept_no")
    private String deptNo;
    
    @TableField("dept_name")
    private String deptName;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("level")
    private Integer level;
    
    @TableField("leader_id")
    private Long leaderId;
    
    @TableField("leader_name")
    private String leaderName;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("status")
    private Integer status;

    @TableField(exist = false)
    private List<SysDepartment> children;
}