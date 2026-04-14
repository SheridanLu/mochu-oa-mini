package com.mochu.oa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mochu.oa.entity.BizApprovalDef;
import com.mochu.oa.entity.BizApprovalInstance;
import com.mochu.oa.entity.BizApprovalTodo;

import java.util.List;
import java.util.Map;

public interface BizApprovalService extends IService<BizApprovalInstance> {
    
    Long startApproval(String bizType, Long bizId, String title, Long applicantId, String applicantName);
    
    void approve(Long instanceId, Long approverId, String approverName, String action, String opinion);
    
    void delegate(Long instanceId, Long fromUserId, Long toUserId, String opinion);
    
    void withdraw(Long instanceId, Long userId);
    
    void cancel(Long instanceId);
    
    List<Map<String, Object>> getTodoList(Long userId, String category, String bizType, String keyword, int page, int size);
    
    List<Map<String, Object>> getDoneList(Long userId, String bizType, String keyword, int page, int size);
    
    List<Map<String, Object>> getHistory(Long instanceId);
    
    IPage<BizApprovalTodo> getTodoPage(Long userId, String category, String bizType, int page, int size);
    
    long getTodoCount(Long userId);
    
    long getDoneCount(Long userId);
    
    long getReadCount(Long userId);
    
    void initFlowDefinitions();
    
    BizApprovalDef getFlowDef(String bizType);
}