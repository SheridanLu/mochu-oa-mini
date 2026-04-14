package com.mochu.oa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {
    
    public void sendWeComNotification(String userId, String title, String content) {
        log.info("发送企业微信通知 - 用户: {}, 标题: {}", userId, title);
        Map<String, Object> params = new HashMap<>();
        params.put("touser", userId);
        params.put("msgtype", "text");
        params.put("text", Map.of("content", title + "\n" + content));
    }
    
    public void sendSmsNotification(String phone, String templateCode, Map<String, String> params) {
        log.info("发送短信通知 - 手机号: {}, 模板: {}", phone, templateCode);
    }
    
    public void sendEmailNotification(String email, String subject, String content) {
        log.info("发送邮件通知 - 邮箱: {}, 主题: {}", email, subject);
    }
    
    public void sendPushNotification(Long userId, String title, String content, String type) {
        log.info("站内信通知 - 用户ID: {}, 标题: {}, 类型: {}", userId, title, type);
    }
    
    public void batchSendMorningApprovalReminder() {
        log.info("批量发送待审批提醒");
    }
    
    public void batchSendAfternoonTaskReminder() {
        log.info("批量发送待接收任务提醒");
    }
    
    public void sendOverdueWarning(Long projectId, Integer overdueDays) {
        log.info("逾期回款预警 - 项目ID: {}, 逾期天数: {}", projectId, overdueDays);
    }
    
    public void sendBudgetWarning(Long departmentId, Long projectId, BigDecimal usageRatio) {
        log.info("预算占用预警 - 部门ID: {}, 项目ID: {}, 使用率: {}", departmentId, projectId, usageRatio);
    }
    
    public void sendInvoiceExpiringWarning(Long invoiceId, Integer remainingDays) {
        log.info("发票过期预警 - 发票ID: {}, 剩余天数: {}", invoiceId, remainingDays);
    }
}