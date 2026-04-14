package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_budget_allocation")
public class BizBudgetAllocation extends BaseEntity {
    
    @TableField("department_id")
    private Long departmentId;
    
    @TableField("department_name")
    private String departmentName;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("year")
    private Integer year;
    
    @TableField("month")
    private Integer month;
    
    @TableField("budget_type")
    private Integer budgetType;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("used_amount")
    private BigDecimal usedAmount;
    
    @TableField("available_amount")
    private BigDecimal availableAmount;
    
    @TableField("usage_rate")
    private BigDecimal usageRate;
    
    @TableField("status")
    private Integer status;
}