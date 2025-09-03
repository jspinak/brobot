package io.github.jspinak.brobot.runner.json.validation.model;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import static org.junit.jupiter.api.Assertions.*;

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
    void testValidationErrorToString() {
        ValidationError error = new ValidationError("Code1", "Test message", ValidationSeverity.ERROR);

        String expected = "[ERROR] Code1: Test message";
        assertEquals(expected, error.toString());
    }
}