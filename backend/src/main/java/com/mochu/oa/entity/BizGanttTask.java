package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gantt_task")
public class BizGanttTask extends BaseEntity {
    
    @TableField("gantt_id")
    private Long ganttId;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("task_name")
    private String taskName;
    
    @TableField("task_type")
    private Integer taskType;
    
    @TableField("start_date")
    private LocalDate startDate;
    
    @TableField("end_date")
    private LocalDate endDate;
    
    @TableField("actual_start_date")
    private LocalDate actualStartDate;
    
    @TableField("actual_end_date")
    private LocalDate actualEndDate;
    
    @TableField("duration")
    private Integer duration;
    
    @TableField("progress")
    private BigDecimal progress;
    
    @TableField("weight")
    private BigDecimal weight;
    
    @TableField("dependency")
    private String dependency;
    
    @TableField("responsible_id")
    private Long responsibleId;
    
    @TableField("responsible_name")
    private String responsibleName;
    
    @TableField("hidden_work")
    private Integer hiddenWork;
    
    @TableField("hidden_accept_status")
    private Integer hiddenAcceptStatus;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("is_locked")
    private Integer isLocked;
}