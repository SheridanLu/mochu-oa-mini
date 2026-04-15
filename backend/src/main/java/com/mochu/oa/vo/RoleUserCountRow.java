package com.mochu.oa.vo;

import lombok.Data;

/** 角色关联用户数（聚合查询一行） */
@Data
public class RoleUserCountRow {
    private Long roleId;
    private Long userCount;
}
