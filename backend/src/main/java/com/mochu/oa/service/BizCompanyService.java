package com.mochu.oa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.BizCompany;
import com.mochu.oa.mapper.BizCompanyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BizCompanyService extends ServiceImpl<BizCompanyMapper, BizCompany> {
    
    public IPage<BizCompany> search(String companyName, String creditCode, int page, int size) {
        LambdaQueryWrapper<BizCompany> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(companyName)) {
            wrapper.like(BizCompany::getCompanyName, companyName);
        }
        if (StringUtils.hasText(creditCode)) {
            wrapper.eq(BizCompany::getCreditCode, creditCode);
        }
        wrapper.orderByDesc(BizCompany::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }
}