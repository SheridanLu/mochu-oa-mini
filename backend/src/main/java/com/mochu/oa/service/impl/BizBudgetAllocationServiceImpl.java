package com.mochu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.BizBudgetAllocation;
import com.mochu.oa.mapper.BizBudgetAllocationMapper;
import com.mochu.oa.service.BizBudgetAllocationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class BizBudgetAllocationServiceImpl extends ServiceImpl<BizBudgetAllocationMapper, BizBudgetAllocation> implements BizBudgetAllocationService {
    
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("80");
    private static final BigDecimal BLOCK_THRESHOLD = new BigDecimal("100");
    
    @Override
    public Map<String, Object> checkBudget(Long departmentId, Long projectId, BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate now = LocalDate.now();
        Integer year = now.getYear();
        Integer month = now.getMonthValue();
        
        LambdaQueryWrapper<BizBudgetAllocation> wrapper = new LambdaQueryWrapper<BizBudgetAllocation>()
                .eq(BizBudgetAllocation::getDepartmentId, departmentId)
                .eq(BizBudgetAllocation::getYear, year)
                .eq(BizBudgetAllocation::getMonth, month)
                .eq(BizBudgetAllocation::getDeleted, 0);
        
        if (projectId != null) {
            wrapper.eq(BizBudgetAllocation::getProjectId, projectId);
        }
        
        BizBudgetAllocation budget = getOne(wrapper);
        
        if (budget == null) {
            result.put("status", "no_budget");
            result.put("canSubmit", false);
            result.put("message", "未找到预算配置");
            return result;
        }
        
        BigDecimal totalBudget = budget.getAmount() != null ? budget.getAmount() : BigDecimal.ZERO;
        BigDecimal usedAmount = budget.getUsedAmount() != null ? budget.getUsedAmount() : BigDecimal.ZERO;
        BigDecimal availableAmount = totalBudget.subtract(usedAmount);
        BigDecimal usageRatio = totalBudget.compareTo(BigDecimal.ZERO) > 0 
                ? usedAmount.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        BigDecimal afterUseAmount = usedAmount.add(amount);
        BigDecimal afterUseRatio = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? afterUseAmount.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        result.put("budgetId", budget.getId());
        result.put("totalBudget", totalBudget);
        result.put("usedAmount", usedAmount);
        result.put("availableAmount", availableAmount);
        result.put("usageRatio", usageRatio);
        result.put("afterUseAmount", afterUseAmount);
        result.put("afterUseRatio", afterUseRatio);
        
        String status;
        boolean canSubmit = true;
        String warning = null;
        
        if (afterUseRatio.compareTo(BLOCK_THRESHOLD) >= 0) {
            status = "blocked";
            canSubmit = false;
            warning = "预算已超支，无法提交报销";
        } else if (afterUseRatio.compareTo(WARNING_THRESHOLD) >= 0) {
            status = "warning";
            warning = "预算占用已超过80%，请注意";
        } else {
            status = "normal";
        }
        
        result.put("status", status);
        result.put("canSubmit", canSubmit);
        result.put("warning", warning);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getBudgetStatus(Long departmentId, Long projectId, Integer year, Integer month) {
        Map<String, Object> result = new HashMap<>();
        
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        LambdaQueryWrapper<BizBudgetAllocation> wrapper = new LambdaQueryWrapper<BizBudgetAllocation>()
                .eq(BizBudgetAllocation::getDepartmentId, departmentId)
                .eq(BizBudgetAllocation::getYear, year)
                .eq(BizBudgetAllocation::getMonth, month)
                .eq(BizBudgetAllocation::getDeleted, 0);
        
        if (projectId != null) {
            wrapper.eq(BizBudgetAllocation::getProjectId, projectId);
        }
        
        BizBudgetAllocation budget = getOne(wrapper);
        
        if (budget == null) {
            result.put("hasBudget", false);
            return result;
        }
        
        result.put("hasBudget", true);
        result.put("totalBudget", budget.getAmount());
        result.put("usedAmount", budget.getUsedAmount());
        result.put("availableAmount", budget.getAvailableAmount());
        result.put("usageRate", budget.getUsageRate());
        result.put("status", budget.getStatus());
        
        return result;
    }
    
    @Override
    public boolean isOverBudget(Long departmentId, Long projectId, BigDecimal amount) {
        Map<String, Object> check = checkBudget(departmentId, projectId, amount);
        return !(Boolean) check.get("canSubmit");
    }
    
    @Override
    public void updateUsedAmount(Long budgetId, BigDecimal amount) {
        BizBudgetAllocation budget = getById(budgetId);
        if (budget != null) {
            BigDecimal usedAmount = budget.getUsedAmount() != null ? budget.getUsedAmount() : BigDecimal.ZERO;
            budget.setUsedAmount(usedAmount.add(amount));
            
            BigDecimal availableAmount = budget.getAmount().subtract(budget.getUsedAmount());
            budget.setAvailableAmount(availableAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : availableAmount);
            
            BigDecimal usageRate = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? budget.getUsedAmount().multiply(new BigDecimal("100")).divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            budget.setUsageRate(usageRate);
            
            updateById(budget);
        }
    }
}