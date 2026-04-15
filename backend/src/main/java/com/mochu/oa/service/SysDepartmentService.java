package com.mochu.oa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.SysDepartment;
import com.mochu.oa.mapper.SysDepartmentMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDepartmentService extends ServiceImpl<SysDepartmentMapper, SysDepartment> {

    public List<SysDepartment> buildDeptTree() {
        List<SysDepartment> all = list(new LambdaQueryWrapper<SysDepartment>()
                .orderByAsc(SysDepartment::getSortOrder));
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<SysDepartment>> byParent = all.stream()
                .collect(Collectors.groupingBy(d -> {
                    Long p = d.getParentId();
                    return (p == null || p == 0L) ? 0L : p;
                }));
        return buildLevel(0L, byParent);
    }

    private List<SysDepartment> buildLevel(Long parentId, Map<Long, List<SysDepartment>> byParent) {
        List<SysDepartment> nodes = new ArrayList<>(byParent.getOrDefault(parentId, Collections.emptyList()));
        for (SysDepartment d : nodes) {
            d.setChildren(buildLevel(d.getId(), byParent));
        }
        return nodes;
    }
}