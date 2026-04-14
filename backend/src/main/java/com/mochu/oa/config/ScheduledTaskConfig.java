package com.mochu.oa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
public class ScheduledTaskConfig {
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateReconciliationStatements() {
        System.out.println("执行定时任务：生成对账单 " + LocalDateTime.now());
    }
    
    @Scheduled(cron = "0 0 3 * * ?")
    public void recalculateSupplierRatings() {
        System.out.println("执行定时任务：重算供应商评级 " + LocalDateTime.now());
    }
    
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendMorningNotifications() {
        System.out.println("执行定时任务：发送待审批提醒 " + LocalDateTime.now());
    }
    
    @Scheduled(cron = "0 0 17 * * ?")
    public void sendAfternoonNotifications() {
        System.out.println("执行定时任务：发送待接收任务提醒 " + LocalDateTime.now());
    }
    
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyOverdueReport() {
        System.out.println("执行定时任务：发送逾期督办周报 " + LocalDateTime.now());
    }
}