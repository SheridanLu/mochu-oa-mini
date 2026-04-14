package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_expense_report")
public class BizExpenseReport extends BaseEntity {
    
    @TableField("report_no")
    private String reportNo;
    
    @TableField("reporter_id")
    private Long reporterId;
    
    @TableField("reporter_name")
    private String reporterName;
    
    @TableField("department_id")
    private Long departmentId;
    
    @TableField("department_name")
    private String departmentName;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("contract_no")
    private String contractNo;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    
    @TableField("net_amount")
    private BigDecimal netAmount;
    
    @TableField("expense_category")
    private Integer expenseCategory;
    
    @TableField("expense_date")
    private LocalDate expenseDate;
    
    @TableField("purpose")
    private String purpose;
    
    @TableField("status")
    private Integer status;
    
    @TableField("submitted_by")
    private Long submittedBy;
    
    @TableField("submitted_at")
    private java.time.LocalDateTime submittedAt;
    
    @TableField("paid_by")
    private Long paidBy;
    
    @TableField("paid_at")
    private java.time.LocalDateTime paidAt;
}