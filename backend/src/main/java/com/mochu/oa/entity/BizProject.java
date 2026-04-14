package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project")
public class BizProject extends BaseEntity {
    
    @TableField("project_no")
    private String projectNo;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("project_type")
    private Integer projectType;
    
    @TableField("status")
    private Integer status;
    
    @TableField("province")
    private String province;
    
    @TableField("city")
    private String city;
    
    @TableField("detailed_address")
    private String detailedAddress;
    
    @TableField("owner_name")
    private String ownerName;
    
    @TableField("owner_contact")
    private String ownerContact;
    
    @TableField("owner_phone")
    private String ownerPhone;
    
    @TableField("contract_amount")
    private BigDecimal contractAmount;
    
    @TableField("start_date")
    private LocalDate startDate;
    
    @TableField("end_date")
    private LocalDate endDate;
    
    @TableField("actual_start_date")
    private LocalDate actualStartDate;
    
    @TableField("actual_end_date")
    private LocalDate actualEndDate;
    
    @TableField("project_manager_id")
    private Long projectManagerId;
    
    @TableField("project_manager_name")
    private String projectManagerName;
    
    @TableField("department_id")
    private Long departmentId;
    
    @TableField("department_name")
    private String departmentName;
    
    @TableField("remark")
    private String remark;
}