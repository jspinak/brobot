package io.github.jspinak.brobot.runner.json.validation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationError without Spring dependencies.
 * Migrated from library-test module.
 */
class ValidationErrorTest {

    @Test
    void testValidationErrorCreation() {
        ValidationError error = new ValidationError("Code1", "Test message", ValidationSeverity.ERROR);

        assertEquals("Code1", error.errorCode());
        assertEquals("Test message", error.message());
        assertEquals(ValidationSeverity.ERROR, error.severity());
    }

    @Test
    void testValidationErrorEquality() {
        ValidationError error1 = new ValidationError("Code1", "Test message", ValidationSeverity.ERROR);
        ValidationError error2 = new ValidationError("Code1", "Test message", ValidationSeverity.ERROR);

        assertEquals(error1, error2);
        assertEquals(error1.hashCode(), error2.hashCode());
    }

    @Test
    void testValidationErrorInequality() {
        ValidationError error1 = new ValidationError("Code1", "Test message", ValidationSeverity.ERROR);
        ValidationError error2 = new ValidationError("Code2", "Test message", ValidationSeverity.ERROR);
        ValidationError error3 = new ValidationError("Code1", "Different message", ValidationSeverity.ERROR);
        ValidationError error4 = new ValidationError("Code1", "Test message", ValidationSeverity.WARNING);

        assertNotEquals(error1, error2);
        assertNotEquals(error1, error3);
        assertNotEquals(error1, error4);
    }

    @Test
    void testValidationErrorToString() {
        ValidationError error = new ValidationError("Code1", "Test message", ValidationSeverity.ERROR);

        String expected = "[ERROR] Code1: Test message";
        assertEquals(expected, error.toString());
    }

    @Test
    void testValidationErrorToStringWithDifferentSeverities() {
        ValidationError warning = new ValidationError("WARN001", "Warning message", ValidationSeverity.WARNING);
        ValidationError critical = new ValidationError("CRIT001", "Critical message", ValidationSeverity.CRITICAL);

        assertEquals("[WARNING] WARN001: Warning message", warning.toString());
        assertEquals("[CRITICAL] CRIT001: Critical message", critical.toString());
    }

    @Test
    void testValidationErrorWithNullValues() {
        // Test that null values are accepted (records don't enforce null checks by default)
        ValidationError error1 = new ValidationError(null, "Message", ValidationSeverity.ERROR);
        assertNull(error1.errorCode());
        assertEquals("Message", error1.message());
        assertEquals(ValidationSeverity.ERROR, error1.severity());
        
        ValidationError error2 = new ValidationError("Code", null, ValidationSeverity.ERROR);
        assertEquals("Code", error2.errorCode());
        assertNull(error2.message());
        assertEquals(ValidationSeverity.ERROR, error2.severity());
        
        ValidationError error3 = new ValidationError("Code", "Message", null);
        assertEquals("Code", error3.errorCode());
        assertEquals("Message", error3.message());
        assertNull(error3.severity());
    }

    @Test
    void testValidationErrorWithEmptyStrings() {
        // Empty strings should be allowed
        ValidationError error1 = new ValidationError("", "Message", ValidationSeverity.ERROR);
        ValidationError error2 = new ValidationError("Code", "", ValidationSeverity.ERROR);

        assertEquals("", error1.errorCode());
        assertEquals("", error2.message());
    }
}