package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.SysAnnouncement;
import com.mochu.oa.service.SysAnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/announcement")
@RequiredArgsConstructor
@Tag(name = "公告管理")
public class SysAnnouncementController {
    
    private final SysAnnouncementService announcementService;
    
    @GetMapping("/list")
    @Operation(summary = "获取公告列表")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<SysAnnouncement> pageResult = announcementService.list(title, status, startTime, endTime, page, size);
        return Result.success(Map.of(
            "list", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "pages", pageResult.getPages(),
            "current", pageResult.getCurrent()
        ));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情")
    public Result<SysAnnouncement> get(@PathVariable Long id) {
        SysAnnouncement announcement = announcementService.getById(id);
        return Result.success(announcement);
    }
    
    @PostMapping
    @Operation(summary = "创建公告")
    public Result<Long> create(@RequestBody SysAnnouncement announcement) {
        announcement.setStatus("draft");
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setCreatorName("管理员");
        announcementService.save(announcement);
        return Result.success(announcement.getId());
    }
    
    @PutMapping
    @Operation(summary = "更新公告")
    public Result<Void> update(@RequestBody SysAnnouncement announcement) {
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementService.updateById(announcement);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交审批")
    public Result<Void> submit(@PathVariable Long id) {
        announcementService.submit(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布公告")
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publish(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/offline")
    @Operation(summary = "下线公告")
    public Result<Void> offline(@PathVariable Long id) {
        announcementService.offline(id);
        return Result.success(null);
    }
    
    @GetMapping("/carousel")
    @Operation(summary = "获取轮播公告")
    public Result<List<SysAnnouncement>> carousel() {
        List<SysAnnouncement> list = announcementService.lambdaQuery()
            .eq(SysAnnouncement::getStatus, "published")
            .isNotNull(SysAnnouncement::getCoverImage)
            .orderByDesc(SysAnnouncement::getIsTop)
            .orderByDesc(SysAnnouncement::getPublishTime)
            .last("LIMIT 5")
            .list();
        return Result.success(list);
    }
}