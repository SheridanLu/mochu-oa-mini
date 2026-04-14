package com.mochu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mochu.oa.entity.BizSupplier;

import java.util.List;
import java.util.Map;

public interface BizSupplierService extends IService<BizSupplier> {
    
    List<Map<String, Object>> getRatingList(Long supplierId, Integer year);
    
    Map<String, Object> calculateRating(Long supplierId, Integer year);
    
    void recalculateRating(Long supplierId, Integer year);
}