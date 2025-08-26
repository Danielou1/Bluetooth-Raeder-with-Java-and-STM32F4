package com.example.serial;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThresholdCheckerTest {

    private ThresholdChecker checker;

    @BeforeEach
    void setUp() {
        checker = new ThresholdChecker();
    }

    @Test
    void testDefaultTemperatureThreshold() {
        assertFalse(checker.isValueOutOfThreshold("temperature", 25.0f), "25°C should be within default range");
        assertTrue(checker.isValueOutOfThreshold("temperature", 60.0f), "60°C should be out of default range");
        assertTrue(checker.isValueOutOfThreshold("temperature", -10.0f), "-10°C should be out of default range");
    }

    @Test
    void testSetAndGetNewThreshold() {
        checker.setThreshold("temperature", 10.0f, 30.0f);
        ThresholdChecker.Threshold newThreshold = checker.getThreshold("temperature");
        assertNotNull(newThreshold);
        assertEquals(10.0f, newThreshold.getMin());
        assertEquals(30.0f, newThreshold.getMax());
    }

    @Test
    void testCustomThreshold() {
        checker.setThreshold("custom_sensor", 100, 200);
        assertFalse(checker.isValueOutOfThreshold("custom_sensor", 150), "150 should be within custom range");
        assertTrue(checker.isValueOutOfThreshold("custom_sensor", 99), "99 should be out of custom range");
        assertTrue(checker.isValueOutOfThreshold("custom_sensor", 201), "201 should be out of custom range");
    }

    @Test
    void testUnknownSensorType() {
        assertFalse(checker.isValueOutOfThreshold("unknown_sensor", 50), "Unknown sensor should not trigger out of threshold");
    }

    @Test
    void testBoundaryValues() {
        ThresholdChecker.Threshold threshold = new ThresholdChecker.Threshold(10, 20);
        assertFalse(threshold.isOutOfRange(10.0f), "Min boundary should be within range");
        assertFalse(threshold.isOutOfRange(20.0f), "Max boundary should be within range");
        assertTrue(threshold.isOutOfRange(9.99f), "Value just below min should be out of range");
        assertTrue(threshold.isOutOfRange(20.01f), "Value just above max should be out of range");
    }
}
