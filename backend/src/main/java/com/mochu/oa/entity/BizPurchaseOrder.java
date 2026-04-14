package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_purchase_order")
public class BizPurchaseOrder extends BaseEntity {
    
    @TableField("order_no")
    private String orderNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("contract_no")
    private String contractNo;
    
    @TableField("source_type")
    private Integer sourceType;
    
    @TableField("status")
    private Integer status;
    
    @TableField("total_budget")
    private BigDecimal totalBudget;
    
    @TableField("total_quantity")
    private BigDecimal totalQuantity;
    
    @TableField("abnormal_count")
    private Integer abnormalCount;
    
    @TableField("submitter_id")
    private Long submitterId;
    
    @TableField("submitter_name")
    private String submitterName;
    
    @TableField("submitted_at")
    private java.time.LocalDateTime submittedAt;
}