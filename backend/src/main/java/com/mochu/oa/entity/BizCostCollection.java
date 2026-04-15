package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cost_collection")
public class BizCostCollection extends BaseEntity {

    @TableField("project_id")
    private Long projectId;

    @TableField("project_name")
    private String projectName;

    @TableField("cost_category")
    private Integer costCategory;

    @TableField("cost_category_name")
    private String costCategoryName;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("source_type")
    private Integer sourceType;

    @TableField("source_id")
    private Long sourceId;

    @TableField("source_no")
    private String sourceNo;

    @TableField("collected_date")
    private LocalDate collectedDate;

    @TableField("period")
    private String period;

    @TableField("remark")
    private String remark;
}
