package com.mochu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.BizInquiryRecord;
import com.mochu.oa.entity.BizMaterial;
import com.mochu.oa.mapper.BizInquiryRecordMapper;
import com.mochu.oa.service.BizInquiryRecordService;
import com.mochu.oa.service.BizMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizInquiryRecordServiceImpl extends ServiceImpl<BizInquiryRecordMapper, BizInquiryRecord> implements BizInquiryRecordService {
    
    private final BizMaterialService bizMaterialService;
    
    @Override
    public List<BizInquiryRecord> getByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<BizInquiryRecord>()
                .eq(BizInquiryRecord::getProjectId, projectId)
                .eq(BizInquiryRecord::getDeleted, 0)
                .orderByDesc(BizInquiryRecord::getInquiryDate));
    }
    
    @Override
    public Map<String, Object> matchPrice(Long materialId, Long projectId) {
        Map<String, Object> result = new HashMap<>();
        result.put("matched", false);
        result.put("source", null);
        result.put("price", null);
        result.put("supplierName", null);
        
        BizMaterial material = bizMaterialService.getById(materialId);
        if (material == null) {
            result.put("reason", "物资不存在");
            return result;
        }
        
        LambdaQueryWrapper<BizInquiryRecord> wrapper = new LambdaQueryWrapper<BizInquiryRecord>()
                .eq(BizInquiryRecord::getMaterialId, materialId)
                .eq(BizInquiryRecord::getDeleted, 0)
                .ge(BizInquiryRecord::getInquiryDate, LocalDateTime.now().minusMonths(3))
                .orderByDesc(BizInquiryRecord::getInquiryDate);
        List<BizInquiryRecord> records = list(wrapper);
        
        if (!records.isEmpty()) {
            BizInquiryRecord latest = records.get(0);
            result.put("matched", true);
            result.put("source", "base");
            result.put("price", latest.getUnitPrice());
            result.put("supplierName", latest.getSupplierName());
            result.put("inquiryDate", latest.getInquiryDate());
            result.put("inquiryId", latest.getId());
            return result;
        }
        
        LambdaQueryWrapper<BizInquiryRecord> baseWrapper = new LambdaQueryWrapper<BizInquiryRecord>()
                .eq(BizInquiryRecord::getMaterialId, materialId)
                .eq(BizInquiryRecord::getDeleted, 0)
                .orderByDesc(BizInquiryRecord::getInquiryDate);
        List<BizInquiryRecord> baseRecords = list(baseWrapper);
        
        if (!baseRecords.isEmpty()) {
            BizInquiryRecord latest = baseRecords.get(0);
            result.put("matched", true);
            result.put("source", "standard");
            result.put("price", latest.getUnitPrice());
            result.put("supplierName", latest.getSupplierName());
            result.put("inquiryDate", latest.getInquiryDate());
            result.put("inquiryId", latest.getId());
            return result;
        }
        
        result.put("reason", "价格待询价");
        return result;
    }
    
    @Override
    public List<Map<String, Object>> comparePrices(Long materialId, Long projectId) {
        List<Map<String, Object>> comparisons = new ArrayList<>();
        
        LambdaQueryWrapper<BizInquiryRecord> wrapper = new LambdaQueryWrapper<BizInquiryRecord>()
                .eq(BizInquiryRecord::getMaterialId, materialId)
                .eq(BizInquiryRecord::getDeleted, 0)
                .orderByAsc(BizInquiryRecord::getUnitPrice);
        List<BizInquiryRecord> records = list(wrapper);
        
        for (BizInquiryRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("supplierId", record.getSupplierId());
            item.put("supplierName", record.getSupplierName());
            item.put("unitPrice", record.getUnitPrice());
            item.put("taxRate", record.getTaxRate());
            item.put("totalPrice", record.getTotalPrice());
            item.put("deliveryCycle", record.getDeliveryCycle());
            item.put("inquiryDate", record.getInquiryDate());
            
            BigDecimal priceWithoutTax = record.getUnitPrice();
            if (record.getTaxRate() != null) {
                priceWithoutTax = record.getUnitPrice().divide(
                        BigDecimal.ONE.add(record.getTaxRate().divide(new BigDecimal("100"))), 
                        2, java.math.RoundingMode.HALF_UP);
            }
            item.put("priceWithoutTax", priceWithoutTax);
            
            comparisons.add(item);
        }
        
        if (comparisons.size() > 1) {
            BigDecimal minPrice = (BigDecimal) comparisons.get(0).get("unitPrice");
            BigDecimal maxPrice = (BigDecimal) comparisons.get(comparisons.size() - 1).get("unitPrice");
            BigDecimal diff = maxPrice.subtract(minPrice);
            BigDecimal ratio = minPrice.compareTo(BigDecimal.ZERO) > 0 
                    ? diff.multiply(new BigDecimal("100")).divide(minPrice, 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("minPrice", minPrice);
            summary.put("maxPrice", maxPrice);
            summary.put("diff", diff);
            summary.put("diffRatio", ratio);
            summary.put("supplierCount", comparisons.size());
            
            Map<String, Object> resultWrapper = new HashMap<>();
            resultWrapper.put("items", comparisons);
            resultWrapper.put("summary", summary);
            return List.of(resultWrapper);
        }
        
        return comparisons;
    }
    
    @Override
    public Map<String, Object> autoCompare(Long materialId, Long projectId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> matchResult = matchPrice(materialId, projectId);
        result.put("match", matchResult);
        
        List<Map<String, Object>> comparisons = comparePrices(materialId, projectId);
        result.put("comparison", comparisons);
        
        if (!comparisons.isEmpty()) {
            Map<String, Object> best = comparisons.get(0);
            result.put("recommended", best);
            
            String source = (String) matchResult.get("source");
            if (source != null) {
                BigDecimal price = (BigDecimal) matchResult.get("price");
                BigDecimal comparePrice = (BigDecimal) best.get("unitPrice");
                
                if (price != null && comparePrice != null && price.compareTo(comparePrice) != 0) {
                    result.put("warning", "基准价与最低报价不一致，建议重新询价");
                }
            }
        }
        
        return result;
    }
}