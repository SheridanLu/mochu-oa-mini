package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_invoice")
public class BizInvoice extends BaseEntity {
    
    @TableField("invoice_no")
    private String invoiceNo;
    
    @TableField("invoice_type")
    private Integer invoiceType;
    
    @TableField("invoice_code")
    private String invoiceCode;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("tax_rate")
    private BigDecimal taxRate;
    
    @TableField("opened_date")
    private LocalDate openedDate;
    
    @TableField("seller_name")
    private String sellerName;
    
    @TableField("seller_tax_no")
    private String sellerTaxNo;
    
    @TableField("buyer_name")
    private String buyerName;
    
    @TableField("buyer_tax_no")
    private String buyerTaxNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("file_path")
    private String filePath;
    
    @TableField("is_verified")
    private Integer isVerified;
    
    @TableField("verified_date")
    private LocalDate verifiedDate;
    
    @TableField("remark")
    private String remark;
}