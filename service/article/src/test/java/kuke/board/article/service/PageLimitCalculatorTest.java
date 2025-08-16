package kuke.board.article.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageLimitCalculatorTest {
    @Test
    void calculatePageLimit() {
        calculatePageLimit(1L, 30L, 10L, 301L);
        calculatePageLimit(7L, 30L, 10L, 301L);
        calculatePageLimit(10L, 30L, 10L, 301L);
        calculatePageLimit(11L, 30L, 10L, 601L);
        calculatePageLimit(12L, 30L, 10L, 601L);
    }

    void calculatePageLimit(Long page, Long pageSize, Long movablePageCount, Long expected) {
        Long actual = PageLimitCalculator.calculatePageLimit(page, pageSize, movablePageCount);
        assertEquals(expected, actual);
    }
}