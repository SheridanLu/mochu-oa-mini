package com.mochu.oa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochu.oa.entity.SysAnnouncement;
import com.mochu.oa.mapper.SysAnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysAnnouncementService extends ServiceImpl<SysAnnouncementMapper, SysAnnouncement> {
    
    public IPage<SysAnnouncement> list(String title, String status, String startTime, String endTime, int page, int size) {
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(SysAnnouncement::getTitle, title);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysAnnouncement::getStatus, status);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(SysAnnouncement::getCreateTime, LocalDateTime.parse(startTime + " 00:00:00"));
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(SysAnnouncement::getCreateTime, LocalDateTime.parse(endTime + " 23:59:59"));
        }
        wrapper.orderByDesc(SysAnnouncement::getIsTop).orderByDesc(SysAnnouncement::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
    
    public boolean submit(Long id) {
        SysAnnouncement announcement = getById(id);
        if (announcement != null) {
            announcement.setStatus("pending");
            return updateById(announcement);
        }
        return false;
    }
    
    public boolean publish(Long id) {
        SysAnnouncement announcement = getById(id);
        if (announcement != null) {
            announcement.setStatus("published");
            announcement.setPublishTime(LocalDateTime.now());
            return updateById(announcement);
        }
        return false;
    }
    
    public boolean offline(Long id) {
        SysAnnouncement announcement = getById(id);
        if (announcement != null) {
            announcement.setStatus("offline");
            return updateById(announcement);
        }
        return false;
    }
}