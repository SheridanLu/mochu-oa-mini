package com.mochu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mochu.oa.entity.SysUserRole;
import com.mochu.oa.vo.RoleUserCountRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("<script>"
            + "SELECT role_id AS roleId, COUNT(DISTINCT user_id) AS userCount "
            + "FROM sys_user_role WHERE role_id IN "
            + "<foreach collection='roleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "GROUP BY role_id"
            + "</script>")
    List<RoleUserCountRow> countDistinctUsersByRoleIds(@Param("roleIds") List<Long> roleIds);
}