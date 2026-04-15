package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_income_contract")
public class BizIncomeContract extends BaseEntity {
    
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
    
    @TableField("party_a")
    private String partyA;
    
    @TableField("party_b")
    private String partyB;
    
    @TableField("signed_file_path")
    private String signedFilePath;
    
    @TableField("remark")
    private String remark;

    @TableField(exist = false)
    private Long templateId;

    @TableField(exist = false)
    private Integer templateVersion;

    @TableField(exist = false)
    private String templateContentHash;
}