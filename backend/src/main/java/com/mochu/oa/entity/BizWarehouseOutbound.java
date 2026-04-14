package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_warehouse_outbound")
public class BizWarehouseOutbound extends BaseEntity {
    
    @TableField("outbound_no")
    private String outboundNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("outbound_type")
    private Integer outboundType;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("total_quantity")
    private BigDecimal totalQuantity;
    
    @TableField("warehouse_id")
    private Long warehouseId;
    
    @TableField("warehouse_name")
    private String warehouseName;
    
    @TableField("outbound_date")
    private LocalDate outboundDate;
    
    @TableField("status")
    private Integer status;
    
    @TableField("remark")
    private String remark;
}