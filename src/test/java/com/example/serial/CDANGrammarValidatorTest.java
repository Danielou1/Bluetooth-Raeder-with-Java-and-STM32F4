package com.example.serial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CDANGrammarValidatorTest {

    private final CDANGrammarValidator validator = new CDANGrammarValidator();

    @Test
    void testValidCommands() {
        assertTrue(validator.isValid("LED LD4 ON"), "Valid LED ON command");
        assertTrue(validator.isValid("led ld4 off"), "Valid command (case-insensitive)");
        assertTrue(validator.isValid("STATUS ALL"), "Valid STATUS ALL command");
        assertTrue(validator.isValid("PWM FAN 50"), "Valid PWM command with value");
        assertTrue(validator.isValid("TEMP READ"), "Valid TEMP READ command");
    }

    @Test
    void testInvalidCommands() {
        assertFalse(validator.isValid("LED FAN ON"), "Invalid target for LED");
        assertFalse(validator.isValid("LED LD4 TOGGLE"), "Invalid argument for LED");
        assertFalse(validator.isValid("PWM FAN 101"), "PWM value out of range (too high)");
        assertFalse(validator.isValid("PWM FAN -1"), "PWM value out of range (too low)");
        assertFalse(validator.isValid("PWM FAN ABC"), "PWM value not a number");
        assertFalse(validator.isValid("TEMP ON"), "Invalid target for TEMP");
        assertFalse(validator.isValid("UNKNOWN COMMAND"), "Unknown command");
    }

    @Test
    void testInvalidNumberOfParts() {
        assertFalse(validator.isValid("LED"), "Too few parts");
        assertFalse(validator.isValid("LED LD4"), "Too few parts for command with args");
        assertFalse(validator.isValid("TEMP READ NOW"), "Too many parts for TEMP READ");
        assertFalse(validator.isValid("STATUS ALL EXTRA"), "Too many parts for STATUS ALL");
    }

    @Test
    void testNullOrEmpty() {
        assertFalse(validator.isValid(null), "Null command is invalid");
        assertFalse(validator.isValid(""), "Empty command is invalid");
        assertFalse(validator.isValid("   "), "Whitespace command is invalid");
    }
}
