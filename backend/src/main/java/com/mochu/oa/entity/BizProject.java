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
    
    @TableField("project_alias")
    private String projectAlias;
    
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
    
    @TableField("amount_without_tax")
    private BigDecimal amountWithoutTax;
    
    @TableField("tax_rate")
    private BigDecimal taxRate;
    
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    
    @TableField("contract_type")
    private String contractType;
    
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
    
    @TableField("budget_limit")
    private BigDecimal budgetLimit;
    
    @TableField("bid_date")
    private LocalDate bidDate;
    
    @TableField("winning_notice")
    private String winningNotice;
    
    @TableField("winning_amount")
    private BigDecimal winningAmount;
    
    @TableField("attachment_paths")
    private String attachmentPaths;
    
    @TableField("remark")
    private String remark;
}