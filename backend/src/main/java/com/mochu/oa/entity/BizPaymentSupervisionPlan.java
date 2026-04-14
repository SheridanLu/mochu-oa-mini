package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_payment_supervision_plan")
public class BizPaymentSupervisionPlan extends BaseEntity {
    
    @TableField("plan_no")
    private String planNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("gantt_node")
    private String ganttNode;
    
    @TableField("应付金额")
    private BigDecimal payableAmount;
    
    @TableField("paid_amount")
    private BigDecimal paidAmount;
    
    @TableField("gap_amount")
    private BigDecimal gapAmount;
    
    @TableField("completion_rate")
    private BigDecimal completionRate;
    
    @TableField("priority")
    private Integer priority;
    
    @TableField("overdue_days")
    private Integer overdueDays;
    
    @TableField("supervision_status")
    private Integer supervisionStatus;
    
    @TableField("approval_status")
    private Integer approvalStatus;
    
    @TableField("responsible_id")
    private Long responsibleId;
    
    @TableField("responsible_name")
    private String responsibleName;
    
    @TableField("deadline")
    private LocalDate deadline;
    
    @TableField("remark")
    private String remark;
}