package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_approval_def")
public class BizApprovalDef {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("biz_type")
    private String bizType;
    
    @TableField("biz_name")
    private String bizName;
    
    @TableField("flow_json")
    private String flowJson;
    
    @TableField("status")
    private Integer status;
    
    @TableField("version")
    private Integer version;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}