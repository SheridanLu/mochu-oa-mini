package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract_payment_plan")
public class BizContractPaymentPlan extends BaseEntity {
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("payment_term_id")
    private Long paymentTermId;
    
    @TableField("payment_term_name")
    private String paymentTermName;
    
    @TableField("planned_payment_date")
    private LocalDate plannedPaymentDate;
    
    @TableField("planned_amount")
    private BigDecimal plannedAmount;
    
    @TableField("payment_condition")
    private String paymentCondition;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("status")
    private Integer status;
}