package com.mochu.oa.entity;

import com.mochu.oa.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysAnnouncement extends BaseEntity {
    private String title;
    private String content;
    private Long coverImageId;
    private String coverImage;
    private String contentImages;
    private Boolean isTop;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private String status;
    private Long approvalInstanceId;
    private String creatorName;
}