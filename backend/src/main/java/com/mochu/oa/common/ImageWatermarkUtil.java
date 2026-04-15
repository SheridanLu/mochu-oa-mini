package com.mochu.oa.common;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ImageWatermarkUtil {

    private static final Pattern DATE_PATTERN = Pattern.compile("(19|20)\\d{2}[-_]?([01]\\d)[-_]?([0-3]\\d)");
    private static final Pattern IMAGE_EXT_PATTERN = Pattern.compile("(?i)\\.(jpg|jpeg|png|webp|gif)$");

    private ImageWatermarkUtil() {
    }

    /**
     * 轻量级水印命名规则校验（MVP）
     * 约定：图片文件名建议包含 任务名关键字 + 日期（yyyyMMdd 或 yyyy-MM-dd）
     * 示例：主体结构_20260415_现场1.jpg
     */
    public static boolean hasValidWatermarkHint(String photoUrl, String taskName) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return false;
        }
        String decoded = URLDecoder.decode(photoUrl, StandardCharsets.UTF_8);
        String lower = decoded.toLowerCase(Locale.ROOT);
        if (!IMAGE_EXT_PATTERN.matcher(lower).find()) {
            return false;
        }
        if (!DATE_PATTERN.matcher(lower).find()) {
            return false;
        }
        if (taskName == null || taskName.isBlank()) {
            return true;
        }
        String normalizedTask = taskName.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return !normalizedTask.isEmpty() && lower.contains(normalizedTask);
    }
}
