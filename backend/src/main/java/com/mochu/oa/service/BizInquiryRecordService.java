package com.mochu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mochu.oa.entity.BizInquiryRecord;

import java.util.List;
import java.util.Map;

public interface BizInquiryRecordService extends IService<BizInquiryRecord> {
    
    List<BizInquiryRecord> getByProjectId(Long projectId);
    
    Map<String, Object> matchPrice(Long materialId, Long projectId);
    
    List<Map<String, Object>> comparePrices(Long materialId, Long projectId);
    
    Map<String, Object> autoCompare(Long materialId, Long projectId);
}