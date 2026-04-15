package com.mochu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract_template")
public class BizContractTemplate extends BaseEntity {

    @TableField("template_name")
    private String templateName;

    @TableField("contract_type")
    private Integer contractType;

    @TableField("template_content")
    private String templateContent;

    @TableField("template_version")
    private Integer templateVersion;

    @TableField("content_hash")
    private String contentHash;

    @TableField("field_keys")
    private String fieldKeys;

    @TableField("source_file_name")
    private String sourceFileName;

    @TableField("source_file_path")
    private String sourceFilePath;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;
}
