package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_reconciliation_statement")
public class BizReconciliationStatement extends BaseEntity {
    
    @TableField("statement_no")
    private String statementNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("period")
    private String period;
    
    @TableField("period_start")
    private LocalDate periodStart;
    
    @TableField("period_end")
    private LocalDate periodEnd;
    
    @TableField("status")
    private Integer status;
    
    @TableField("contract_amount")
    private BigDecimal contractAmount;
    
    @TableField("current_production")
    private BigDecimal currentProduction;
    
    @TableField("accumulated_production")
    private BigDecimal accumulatedProduction;
    
    @TableField("current_receipt")
    private BigDecimal currentReceipt;
    
    @TableField("accumulated_receipt")
    private BigDecimal accumulatedReceipt;
    
    @TableField("receivable_balance")
    private BigDecimal receivableBalance;
    
    @TableField("difference_amount")
    private BigDecimal differenceAmount;
    
    @TableField("difference_ratio")
    private BigDecimal differenceRatio;
    
    @TableField("remark")
    private String remark;
    
    @TableField("submitted_by")
    private Long submittedBy;
    
    @TableField("submitted_at")
    private java.time.LocalDateTime submittedAt;
}