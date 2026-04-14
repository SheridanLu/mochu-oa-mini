package com.mochu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.BizGanttTask;
import com.mochu.oa.entity.BizIncomeContract;
import com.mochu.oa.entity.BizIncomeContractSplit;
import com.mochu.oa.mapper.BizIncomeContractSplitMapper;
import com.mochu.oa.service.BizGanttTaskService;
import com.mochu.oa.service.BizIncomeContractService;
import com.mochu.oa.service.BizIncomeContractSplitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizIncomeContractSplitServiceImpl extends ServiceImpl<BizIncomeContractSplitMapper, BizIncomeContractSplit> implements BizIncomeContractSplitService {
    
    private final BizIncomeContractService bizIncomeContractService;
    private final BizGanttTaskService bizGanttTaskService;
    
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");
    
    @Override
    public List<BizIncomeContractSplit> getByContractId(Long contractId) {
        return list(new LambdaQueryWrapper<BizIncomeContractSplit>()
                .eq(BizIncomeContractSplit::getContractId, contractId)
                .eq(BizIncomeContractSplit::getDeleted, 0)
                .orderByAsc(BizIncomeContractSplit::getSortOrder));
    }
    
    @Override
    public Map<String, Object> validateSplit(Long contractId, List<BizIncomeContractSplit> splits) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("errors", new java.util.ArrayList<String>());
        
        BizIncomeContract contract = bizIncomeContractService.getById(contractId);
        if (contract == null) {
            result.put("valid", false);
            ((List<String>) result.get("errors")).add("合同不存在");
            return result;
        }
        
        BigDecimal contractAmount = contract.getTotalAmount();
        BigDecimal splitTotal = splits.stream()
                .map(BizIncomeContractSplit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal difference = contractAmount.subtract(splitTotal).abs();
        if (difference.compareTo(TOLERANCE) > 0) {
            result.put("valid", false);
            ((List<String>) result.get("errors")).add(
                    "拆分金额合计(" + splitTotal + ")与合同金额(" + contractAmount + ")不符，差额：" + difference);
        }
        
        for (int i = 0; i < splits.size(); i++) {
            BizIncomeContractSplit split = splits.get(i);
            if (split.getAmount() == null || split.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                ((List<String>) result.get("errors")).add("第" + (i + 1) + "行：金额必须大于0");
                result.put("valid", false);
            }
            if (split.getGanttTaskId() != null) {
                BizGanttTask task = bizGanttTaskService.getById(split.getGanttTaskId());
                if (task != null && split.getProgressRatio() == null) {
                    split.setProgressRatio(task.getProgress() != null ? task.getProgress() : BigDecimal.ZERO);
                }
            }
            if (split.getProgressRatio() != null && contractAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal expectedProduction = contractAmount.multiply(split.getProgressRatio())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                split.setProductionValue(expectedProduction);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSplit(Long contractId, List<BizIncomeContractSplit> splits, Long userId) {
        Map<String, Object> validation = validateSplit(contractId, splits);
        if (!(Boolean) validation.get("valid")) {
            throw new RuntimeException("拆分校验失败：" + validation.get("errors"));
        }
        
        LambdaQueryWrapper<BizIncomeContractSplit> wrapper = new LambdaQueryWrapper<BizIncomeContractSplit>()
                .eq(BizIncomeContractSplit::getContractId, contractId);
        remove(wrapper);
        
        for (int i = 0; i < splits.size(); i++) {
            BizIncomeContractSplit split = splits.get(i);
            split.setContractId(contractId);
            split.setSortOrder(i + 1);
            split.setCreatedBy(userId);
            split.setStatus(1);
            split.setDeleted(0);
        }
        
        return saveBatch(splits);
    }
    
    @Override
    public BigDecimal calculateTotalAmount(Long contractId) {
        List<BizIncomeContractSplit> splits = getByContractId(contractId);
        return splits.stream()
                .map(BizIncomeContractSplit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public Map<String, Object> getSplitSummary(Long contractId) {
        Map<String, Object> summary = new HashMap<>();
        
        BizIncomeContract contract = bizIncomeContractService.getById(contractId);
        if (contract == null) {
            return summary;
        }
        
        List<BizIncomeContractSplit> splits = getByContractId(contractId);
        BigDecimal totalSplit = splits.stream()
                .map(BizIncomeContractSplit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.put("contractAmount", contract.getTotalAmount());
        summary.put("totalSplitAmount", totalSplit);
        summary.put("difference", contract.getTotalAmount().subtract(totalSplit).abs());
        summary.put("isValid", contract.getTotalAmount().subtract(totalSplit).abs().compareTo(TOLERANCE) <= 0);
        summary.put("splitCount", splits.size());
        
        BigDecimal totalProgress = splits.stream()
                .map(BizIncomeContractSplit::getProgressRatio)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalProgress", totalProgress);
        
        return summary;
    }
}