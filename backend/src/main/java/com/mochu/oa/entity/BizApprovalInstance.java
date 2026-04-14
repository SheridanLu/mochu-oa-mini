package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_approval_instance")
public class BizApprovalInstance {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("biz_type")
    private String bizType;
    
    @TableField("biz_id")
    private Long bizId;
    
    @TableField("applicant_id")
    private Long applicantId;
    
    @TableField("applicant_name")
    private String applicantName;
    
    @TableField("title")
    private String title;
    
    @TableField("status")
    private String status;
    
    @TableField("current_node_order")
    private Integer currentNodeOrder;
    
    @TableField("current_node_name")
    private String currentNodeName;
    
    @TableField("start_time")
    private LocalDateTime startTime;
    
    @TableField("end_time")
    private LocalDateTime endTime;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}