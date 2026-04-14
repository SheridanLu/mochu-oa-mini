package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("biz_supplier_evaluation")
public class BizSupplierEvaluation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("supplier_id")
    private Long supplierId;
    
    @TableField("year")
    private Integer year;
    
    @TableField("quarter")
    private String quarter;
    
    @TableField("total_score")
    private BigDecimal totalScore;
    
    @TableField("grade")
    private String grade;
    
    @TableField("level")
    private String level;
    
    @TableField("quantity_rank")
    private Integer quantityRank;
    
    @TableField("amount_rank")
    private Integer amountRank;
    
    @TableField("evaluation_date")
    private LocalDateTime evaluationDate;
    
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
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getQuarter() { return quarter; }
    public void setQuarter(String quarter) { this.quarter = quarter; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getQuantityRank() { return quantityRank; }
    public void setQuantityRank(Integer quantityRank) { this.quantityRank = quantityRank; }
    public Integer getAmountRank() { return amountRank; }
    public void setAmountRank(Integer amountRank) { this.amountRank = amountRank; }
    public LocalDateTime getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDateTime evaluationDate) { this.evaluationDate = evaluationDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}