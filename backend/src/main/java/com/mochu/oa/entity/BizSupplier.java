package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_supplier")
public class BizSupplier extends BaseEntity {
    
    @TableField("supplier_no")
    private String supplierNo;
    
    @TableField("supplier_name")
    private String supplierName;
    
    @TableField("supplier_type")
    private Integer supplierType;
    
    @TableField("category")
    private String category;
    
    @TableField("contact_name")
    private String contactName;
    
    @TableField("contact_phone")
    private String contactPhone;
    
    @TableField("contact_email")
    private String contactEmail;
    
    @TableField("province")
    private String province;
    
    @TableField("city")
    private String city;
    
    @TableField("detailed_address")
    private String detailedAddress;
    
    @TableField("bank_name")
    private String bankName;
    
    @TableField("bank_account")
    private String bankAccount;
    
    @TableField("tax_no")
    private String taxNo;
    
    @TableField("status")
    private Integer status;
    
    @TableField("rating")
    private Integer rating;
    
    @TableField("remark")
    private String remark;
}