package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gantt")
public class BizGantt extends BaseEntity {
    
    @TableField("gantt_no")
    private String ganttNo;
    
    @TableField("gantt_name")
    private String ganttName;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("status")
    private Integer status;
    
    @TableField("start_date")
    private LocalDate startDate;
    
    @TableField("end_date")
    private LocalDate endDate;
    
    @TableField("actual_start_date")
    private LocalDate actualStartDate;
    
    @TableField("actual_end_date")
    private LocalDate actualEndDate;
    
    @TableField("total_days")
    private Integer totalDays;
    
    @TableField("progress")
    private BigDecimal progress;
    
    @TableField("submitter_id")
    private Long submitterId;
    
    @TableField("submitter_name")
    private String submitterName;
}