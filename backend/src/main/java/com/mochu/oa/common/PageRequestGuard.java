package com.mochu.oa.common;

/** 分页参数校验与兜底工具，统一后端分页行为。 */
public final class PageRequestGuard {

    private PageRequestGuard() {
    }

    public static String validate(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            return "pageNum 必须大于等于 1";
        }
        if (pageSize == null || pageSize < 1) {
            return "pageSize 必须大于等于 1";
        }
        return null;
    }

    public static int normalizePageSize(Integer pageSize, int maxPageSize) {
        if (pageSize == null || pageSize < 1) {
            return 1;
        }
        return Math.min(pageSize, Math.max(1, maxPageSize));
    }
}
