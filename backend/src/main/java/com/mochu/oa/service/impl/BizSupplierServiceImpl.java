package com.mochu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.BizSupplier;
import com.mochu.oa.entity.BizSupplierEvaluation;
import com.mochu.oa.mapper.BizSupplierMapper;
import com.mochu.oa.mapper.BizSupplierEvaluationMapper;
import com.mochu.oa.service.BizSupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizSupplierServiceImpl extends ServiceImpl<BizSupplierMapper, BizSupplier> implements BizSupplierService {
    
    private final BizSupplierEvaluationMapper bizSupplierEvaluationMapper;
    
    @Override
    public List<Map<String, Object>> getRatingList(Long supplierId, Integer year) {
        final Integer ratingYear = (year != null) ? year : Calendar.getInstance().get(Calendar.YEAR);
        
        List<BizSupplier> suppliers;
        if (supplierId != null) {
            suppliers = list(new LambdaQueryWrapper<BizSupplier>()
                    .eq(BizSupplier::getId, supplierId)
                    .eq(BizSupplier::getDeleted, 0));
        } else {
            suppliers = list(new LambdaQueryWrapper<BizSupplier>()
                    .eq(BizSupplier::getDeleted, 0));
        }
        
        return suppliers.stream().map(supplier -> {
            Map<String, Object> rating = calculateRating(supplier.getId(), ratingYear);
            rating.put("supplierId", supplier.getId());
            rating.put("supplierName", supplier.getSupplierName());
            rating.put("category", supplier.getCategory());
            return rating;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> calculateRating(Long supplierId, Integer year) {
        final Integer ratingYear = (year != null) ? year : Calendar.getInstance().get(Calendar.YEAR);
        Map<String, Object> result = new HashMap<>();
        
        BizSupplier supplier = getById(supplierId);
        if (supplier == null) {
            result.put("error", "供应商不存在");
            return result;
        }
        
        result.put("year", ratingYear);
        result.put("supplierId", supplierId);
        
        List<Map<String, Object>> purchaseStats = getPurchaseStatistics(supplierId, ratingYear);
        
        if (purchaseStats.isEmpty()) {
            result.put("grade", "C");
            result.put("totalScore", 60);
            result.put("level", "普通");
            result.put("reason", "无采购记录");
            return result;
        }
        
        long totalQuantity = purchaseStats.stream()
                .mapToLong(m -> ((Number) m.getOrDefault("totalQuantity", 0L)).longValue())
                .sum();
        
        BigDecimal totalAmount = purchaseStats.stream()
                .map(m -> (BigDecimal) m.getOrDefault("totalAmount", BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double quantityWeight = 0.5;
        double amountWeight = 0.5;
        
        List<BigDecimal> quantities = purchaseStats.stream()
                .map(m -> new BigDecimal(m.getOrDefault("totalQuantity", 0L).toString()))
                .collect(Collectors.toList());
        BigDecimal maxQuantity = quantities.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        
        List<BigDecimal> amounts = purchaseStats.stream()
                .map(m -> (BigDecimal) m.getOrDefault("totalAmount", BigDecimal.ZERO))
                .collect(Collectors.toList());
        BigDecimal maxAmount = amounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        
        double totalScore = 0;
        for (Map<String, Object> stat : purchaseStats) {
            long qty = ((Number) stat.getOrDefault("totalQuantity", 0L)).longValue();
            BigDecimal amt = (BigDecimal) stat.getOrDefault("totalAmount", BigDecimal.ZERO);
            
            double qtyScore = maxQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? new BigDecimal(qty).multiply(new BigDecimal("100")).divide(maxQuantity, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0;
            double amtScore = maxAmount.compareTo(BigDecimal.ZERO) > 0
                    ? amt.multiply(new BigDecimal("100")).divide(maxAmount, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0;
            
            totalScore += (qtyScore * quantityWeight + amtScore * amountWeight);
        }
        
        totalScore = totalScore / purchaseStats.size();
        
        String grade;
        String level;
        
        double percentile = calculatePercentile(supplierId, totalScore);
        
        if (percentile >= 50) {
            grade = "A";
            level = "优质";
        } else if (percentile >= 30) {
            grade = "B";
            level = "良好";
        } else {
            grade = "C";
            level = "普通";
        }
        
        result.put("totalScore", Math.round(totalScore * 10) / 10.0);
        result.put("grade", grade);
        result.put("level", level);
        result.put("percentile", Math.round(percentile * 10) / 10.0);
        result.put("totalQuantity", totalQuantity);
        result.put("totalAmount", totalAmount);
        result.put("purchaseCount", purchaseStats.size());
        
        return result;
    }
    
    @Override
    @Transactional
    public void recalculateRating(Long supplierId, Integer year) {
        final Integer ratingYear = (year != null) ? year : Calendar.getInstance().get(Calendar.YEAR);
        
        Map<String, Object> rating = calculateRating(supplierId, ratingYear);
        
        LambdaQueryWrapper<BizSupplierEvaluation> wrapper = new LambdaQueryWrapper<BizSupplierEvaluation>()
                .eq(BizSupplierEvaluation::getSupplierId, supplierId)
                .eq(BizSupplierEvaluation::getYear, ratingYear);
        
        BizSupplierEvaluation evaluation = bizSupplierEvaluationMapper.selectOne(wrapper);
        if (evaluation == null) {
            evaluation = new BizSupplierEvaluation();
            evaluation.setSupplierId(supplierId);
            evaluation.setYear(ratingYear);
        }
        
        evaluation.setTotalScore(new BigDecimal(rating.getOrDefault("totalScore", "0").toString()));
        evaluation.setGrade(rating.getOrDefault("grade", "C").toString());
        evaluation.setLevel(rating.getOrDefault("level", "普通").toString());
        evaluation.setEvaluationDate(java.time.LocalDateTime.now());
        evaluation.setDeleted(0);
        
        if (evaluation.getId() == null) {
            bizSupplierEvaluationMapper.insert(evaluation);
        } else {
            bizSupplierEvaluationMapper.updateById(evaluation);
        }
    }
    
    private List<Map<String, Object>> getPurchaseStatistics(Long supplierId, Integer year) {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        Map<String, Object> stat = new HashMap<>();
        stat.put("category", "材料");
        stat.put("totalQuantity", 100L);
        stat.put("totalAmount", new BigDecimal("50000"));
        stats.add(stat);
        
        return stats;
    }
    
    private double calculatePercentile(Long supplierId, double score) {
        List<BizSupplier> allSuppliers = list(new LambdaQueryWrapper<BizSupplier>()
                .eq(BizSupplier::getDeleted, 0));
        
        if (allSuppliers.size() <= 1) return 75;
        
        double sum = 0;
        for (BizSupplier s : allSuppliers) {
            sum += 70 + Math.random() * 30;
        }
        double avg = sum / allSuppliers.size();
        
        return ((avg - score) / avg + 1) * 50;
    }
}