package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_expense_contract")
public class BizExpenseContract extends BaseEntity {
    
    @TableField("contract_no")
    private String contractNo;
    
    @TableField("contract_name")
    private String contractName;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("contract_type")
    private Integer contractType;
    
    @TableField("supplier_id")
    private Long supplierId;
    
    @TableField("supplier_name")
    private String supplierName;
    
    @TableField("sign_date")
    private LocalDate signDate;
    
    @TableField("start_date")
    private LocalDate startDate;
    
    @TableField("end_date")
    private LocalDate endDate;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("tax_rate")
    private BigDecimal taxRate;
    
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    
    @TableField("total_amount_with_tax")
    private BigDecimal totalAmountWithTax;
    
    @TableField("payment_type")
    private Integer paymentType;
    
    @TableField("status")
    private Integer status;
    
    @TableField("signed_file_path")
    private String signedFilePath;
    
    @TableField("remark")
    private String remark;
}