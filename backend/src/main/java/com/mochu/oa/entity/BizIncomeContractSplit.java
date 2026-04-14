package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("biz_income_contract_split")
public class BizIncomeContractSplit {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("contract_id")
    private Long contractId;
    
    @TableField("gantt_task_id")
    private Long ganttTaskId;
    
    @TableField("task_name")
    private String taskName;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("progress_ratio")
    private BigDecimal progressRatio;
    
    @TableField("production_value")
    private BigDecimal productionValue;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("status")
    private Integer status;
    
    @TableField("created_by")
    private Long createdBy;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_by")
    private Long updatedBy;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("deleted")
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Long getGanttTaskId() { return ganttTaskId; }
    public void setGanttTaskId(Long ganttTaskId) { this.ganttTaskId = ganttTaskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getProgressRatio() { return progressRatio; }
    public void setProgressRatio(BigDecimal progressRatio) { this.progressRatio = progressRatio; }
    public BigDecimal getProductionValue() { return productionValue; }
    public void setProductionValue(BigDecimal productionValue) { this.productionValue = productionValue; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}