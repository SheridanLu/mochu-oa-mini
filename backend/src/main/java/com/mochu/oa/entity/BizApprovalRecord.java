package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_approval_record")
public class BizApprovalRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("instance_id")
    private Long instanceId;
    
    @TableField("node_order")
    private Integer nodeOrder;
    
    @TableField("node_name")
    private String nodeName;
    
    @TableField("approver_id")
    private Long approverId;
    
    @TableField("approver_name")
    private String approverName;
    
    @TableField("action")
    private String action;
    
    @TableField("opinion")
    private String opinion;
    
    @TableField("delegate_from_id")
    private Long delegateFromId;
    
    @TableField("operate_time")
    private LocalDateTime operateTime;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}