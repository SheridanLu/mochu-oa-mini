package com.mochu.oa.common;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParamGuardTest {

    @Test
    void validateRangeShouldWork() {
        assertNull(ParamGuard.validateRange(null, 1, 7, "status"));
        assertNull(ParamGuard.validateRange(3, 1, 7, "status"));
        assertEquals("status 只能为 1-7", ParamGuard.validateRange(9, 1, 7, "status"));
    }

    @Test
    void validateOneOfShouldWork() {
        assertNull(ParamGuard.validateOneOf(null, "status", 0, 1));
        assertNull(ParamGuard.validateOneOf(1, "status", 0, 1));
        assertEquals("status 只能为 0 或 1", ParamGuard.validateOneOf(3, "status", 0, 1));
    }

    @Test
    void parseYearMonthShouldParseOrThrowReadableError() {
        YearMonth ym = ParamGuard.parseYearMonth("2026-04", "planMonth");
        assertEquals(2026, ym.getYear());
        assertEquals(4, ym.getMonthValue());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ParamGuard.parseYearMonth("2026/04", "planMonth"));
        assertEquals("planMonth 格式错误，应为 YYYY-MM", ex.getMessage());
    }
}
