package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_payment_apply")
public class BizPaymentApply extends BaseEntity {
    
    @TableField("apply_no")
    private String applyNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("category")
    private Integer category;
    
    @TableField("supplier_id")
    private Long supplierId;
    
    @TableField("supplier_name")
    private String supplierName;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("payment_method")
    private Integer paymentMethod;
    
    @TableField("bank_name")
    private String bankName;
    
    @TableField("bank_account")
    private String bankAccount;
    
    @TableField("purpose")
    private String purpose;
    
    @TableField("status")
    private Integer status;
    
    @TableField("submitted_by")
    private Long submittedBy;
    
    @TableField("submitted_at")
    private LocalDateTime submittedAt;
    
    @TableField("paid_by")
    private Long paidBy;
    
    @TableField("paid_at")
    private LocalDateTime paidAt;
    
    @TableField("remark")
    private String remark;
}