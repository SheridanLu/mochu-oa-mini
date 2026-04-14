package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_material_category")
public class BizMaterialCategory extends BaseEntity {
    
    @TableField("category_name")
    private String categoryName;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("level")
    private Integer level;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("status")
    private Integer status;
}