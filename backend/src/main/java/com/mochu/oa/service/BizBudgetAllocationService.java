package com.mochu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mochu.oa.entity.BizBudgetAllocation;

import java.math.BigDecimal;
import java.util.Map;

public interface BizBudgetAllocationService extends IService<BizBudgetAllocation> {
    
    Map<String, Object> checkBudget(Long departmentId, Long projectId, BigDecimal amount);
    
    Map<String, Object> getBudgetStatus(Long departmentId, Long projectId, Integer year, Integer month);
    
    boolean isOverBudget(Long departmentId, Long projectId, BigDecimal amount);
    
    void updateUsedAmount(Long budgetId, BigDecimal amount);
}