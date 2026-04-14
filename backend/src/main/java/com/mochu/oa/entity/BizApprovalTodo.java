package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_approval_todo")
public class BizApprovalTodo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("instance_id")
    private Long instanceId;
    
    @TableField("node_order")
    private Integer nodeOrder;
    
    @TableField("node_name")
    private String nodeName;
    
    @TableField("handler_id")
    private Long handlerId;
    
    @TableField("handler_name")
    private String handlerName;
    
    @TableField("category")
    private String category;
    
    @TableField("priority")
    private Integer priority;
    
    @TableField("status")
    private Integer status;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}