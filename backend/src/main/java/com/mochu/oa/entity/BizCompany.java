package com.mochu.oa.entity;

import com.mochu.oa.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BizCompany extends BaseEntity {
    private String companyName;
    private String creditCode;
    private String address;
    private String contactName;
    private String contactPhone;
    private String bankName;
    private String bankAccount;
    private String taxNo;
    private String email;
    private Integer status;
}