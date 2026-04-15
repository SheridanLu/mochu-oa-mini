package com.mochu.oa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.common.PageRequestGuard;
import com.mochu.oa.entity.SysRole;
import com.mochu.oa.mapper.SysRoleMapper;
import com.mochu.oa.mapper.SysUserRoleMapper;
import com.mochu.oa.vo.RoleUserCountRow;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    private void fillUserCounts(List<SysRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        List<Long> roleIds = roles.stream()
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return;
        }
        List<RoleUserCountRow> rows = sysUserRoleMapper.countDistinctUsersByRoleIds(roleIds);
        Map<Long, Integer> countByRole = rows.stream()
                .collect(Collectors.toMap(
                        RoleUserCountRow::getRoleId,
                        r -> r.getUserCount() == null ? 0 : r.getUserCount().intValue(),
                        (a, b) -> a));
        for (SysRole r : roles) {
            r.setUserCount(countByRole.getOrDefault(r.getId(), 0));
        }
    }

    public IPage<SysRole> pageRoles(String roleName, Integer status, int page, int size) {
        LambdaQueryWrapper<SysRole> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            w.like(SysRole::getRoleName, roleName.trim());
        }
        if (status != null) {
            w.eq(SysRole::getStatus, status);
        }
        w.orderByAsc(SysRole::getRoleCode);
        int pageNum = Math.max(1, page);
        int pageSize = PageRequestGuard.normalizePageSize(size, 100);
        IPage<SysRole> result = page(new Page<>(pageNum, pageSize), w);
        fillUserCounts(result.getRecords());
        return result;
    }

    /** 下拉/分配角色：不分页，仅启用角色 */
    public List<SysRole> listForSelect() {
        return list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getRoleName));
    }
}