package com.mochu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mochu.oa.entity.BizIncomeContractSplit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BizIncomeContractSplitService extends IService<BizIncomeContractSplit> {
    
    List<BizIncomeContractSplit> getByContractId(Long contractId);
    
    Map<String, Object> validateSplit(Long contractId, List<BizIncomeContractSplit> splits);
    
    boolean saveSplit(Long contractId, List<BizIncomeContractSplit> splits, Long userId);
    
    BigDecimal calculateTotalAmount(Long contractId);
    
    Map<String, Object> getSplitSummary(Long contractId);
}