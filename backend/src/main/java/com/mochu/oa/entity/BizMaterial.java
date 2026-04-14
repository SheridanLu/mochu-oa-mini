package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_material")
public class BizMaterial extends BaseEntity {
    
    @TableField("material_no")
    private String materialNo;
    
    @TableField("material_name")
    private String materialName;
    
    @TableField("spec_model")
    private String specModel;
    
    @TableField("brand")
    private String brand;
    
    @TableField("category_id")
    private Long categoryId;
    
    @TableField("category_name")
    private String categoryName;
    
    @TableField("unit")
    private String unit;
    
    @TableField("weight")
    private BigDecimal weight;
    
    @TableField("length")
    private BigDecimal length;
    
    @TableField("width")
    private BigDecimal width;
    
    @TableField("height")
    private BigDecimal height;
    
    @TableField("standard")
    private String standard;
    
    @TableField("safe_stock")
    private BigDecimal safeStock;
    
    @TableField("status")
    private Integer status;
    
    @TableField("remark")
    private String remark;
}