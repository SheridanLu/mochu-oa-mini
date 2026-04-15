package com.mochu.oa.common;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** 通用参数校验工具。 */
public final class ParamGuard {

    private ParamGuard() {
    }

    public static String validateRange(Integer value, int min, int max, String name) {
        if (value == null) {
            return null;
        }
        if (value < min || value > max) {
            return name + " 只能为 " + min + "-" + max;
        }
        return null;
    }

    public static String validateOneOf(Integer value, String name, int... allowed) {
        if (value == null) {
            return null;
        }
        for (int a : allowed) {
            if (value == a) {
                return null;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allowed.length; i++) {
            if (i > 0) sb.append(" 或 ");
            sb.append(allowed[i]);
        }
        return name + " 只能为 " + sb;
    }

    public static YearMonth parseYearMonth(String value, String name) {
        try {
            return YearMonth.parse(value == null ? "" : value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(name + " 格式错误，应为 YYYY-MM");
        }
    }
}
