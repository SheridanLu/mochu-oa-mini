package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_purchase_order_item")
public class BizPurchaseOrderItem extends BaseEntity {
    
    @TableField("order_id")
    private Long orderId;
    
    @TableField("material_name")
    private String materialName;
    
    @TableField("spec_model")
    private String specModel;
    
    @TableField("brand")
    private String brand;
    
    @TableField("unit")
    private String unit;
    
    @TableField("plan_quantity")
    private BigDecimal planQuantity;
    
    @TableField("budget_price")
    private BigDecimal budgetPrice;
    
    @TableField("budget_amount")
    private BigDecimal budgetAmount;
    
    @TableField("match_source")
    private Integer matchSource;
    
    @TableField("supplier_id")
    private Long supplierId;
    
    @TableField("supplier_name")
    private String supplierName;
    
    @TableField("remark")
    private String remark;
    
    @TableField("row_status")
    private Integer rowStatus;
    
    @TableField("row_remark")
    private String rowRemark;
    
    @TableField("sort_order")
    private Integer sortOrder;
}