package com.mochu.oa.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PageRequestGuardTest {

    @Test
    void validateShouldRejectInvalidPageNumAndPageSize() {
        assertEquals("pageNum 必须大于等于 1", PageRequestGuard.validate(0, 10));
        assertEquals("pageSize 必须大于等于 1", PageRequestGuard.validate(1, 0));
    }

    @Test
    void validateShouldPassWhenParamsValid() {
        assertNull(PageRequestGuard.validate(1, 10));
    }

    @Test
    void normalizePageSizeShouldClampToBounds() {
        assertEquals(1, PageRequestGuard.normalizePageSize(null, 500));
        assertEquals(1, PageRequestGuard.normalizePageSize(0, 500));
        assertEquals(500, PageRequestGuard.normalizePageSize(999, 500));
        assertEquals(20, PageRequestGuard.normalizePageSize(20, 500));
    }
}
