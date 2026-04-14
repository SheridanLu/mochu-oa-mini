package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_warehouse_entry")
public class BizWarehouseEntry extends BaseEntity {
    
    @TableField("entry_no")
    private String entryNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("supplier_id")
    private Long supplierId;
    
    @TableField("supplier_name")
    private String supplierName;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("entry_type")
    private Integer entryType;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("total_quantity")
    private BigDecimal totalQuantity;
    
    @TableField("warehouse_id")
    private Long warehouseId;
    
    @TableField("warehouse_name")
    private String warehouseName;
    
    @TableField("entry_date")
    private LocalDate entryDate;
    
    @TableField("status")
    private Integer status;
    
    @TableField("remark")
    private String remark;
}