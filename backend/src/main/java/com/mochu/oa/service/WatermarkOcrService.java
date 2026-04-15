package com.mochu.oa.service;

import com.mochu.oa.common.WatermarkOcrResult;

import java.util.Map;

public interface WatermarkOcrService {
    WatermarkOcrResult parsePhoto(String photoUrl, String taskName);

    Map<String, Object> metricsSnapshot();
}
