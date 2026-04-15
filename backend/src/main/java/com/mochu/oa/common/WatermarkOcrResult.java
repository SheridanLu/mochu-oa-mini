package com.mochu.oa.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkOcrResult {
    private boolean success;
    private boolean validWatermark;
    private String rawText;
    private String message;
}
