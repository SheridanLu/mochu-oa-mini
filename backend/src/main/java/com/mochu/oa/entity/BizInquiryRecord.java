package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("biz_inquiry_record")
public class BizInquiryRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("inquiry_no")
    private String inquiryNo;
    
    @TableField("project_id")
    private Long projectId;
    
    @TableField("project_name")
    private String projectName;
    
    @TableField("material_id")
    private Long materialId;
    
    @TableField("material_name")
    private String materialName;
    
    @TableField("specification")
    private String specification;
    
    @TableField("unit")
    private String unit;
    
    @TableField("quantity")
    private BigDecimal quantity;
    
    @TableField("inquiry_date")
    private LocalDateTime inquiryDate;
    
    @TableField("expected_arrival_date")
    private LocalDateTime expectedArrivalDate;
    
    @TableField("supplier_id")
    private Long supplierId;
    
    @TableField("supplier_name")
    private String supplierName;
    
    @TableField("unit_price")
    private BigDecimal unitPrice;
    
    @TableField("tax_rate")
    private BigDecimal taxRate;
    
    @TableField("total_price")
    private BigDecimal totalPrice;
    
    @TableField("delivery_cycle")
    private Integer deliveryCycle;
    
    @TableField("status")
    private Integer status;
    
    @TableField("remark")
    private String remark;
    
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
    public String getInquiryNo() { return inquiryNo; }
    public void setInquiryNo(String inquiryNo) { this.inquiryNo = inquiryNo; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public LocalDateTime getInquiryDate() { return inquiryDate; }
    public void setInquiryDate(LocalDateTime inquiryDate) { this.inquiryDate = inquiryDate; }
    public LocalDateTime getExpectedArrivalDate() { return expectedArrivalDate; }
    public void setExpectedArrivalDate(LocalDateTime expectedArrivalDate) { this.expectedArrivalDate = expectedArrivalDate; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Integer getDeliveryCycle() { return deliveryCycle; }
    public void setDeliveryCycle(Integer deliveryCycle) { this.deliveryCycle = deliveryCycle; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
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