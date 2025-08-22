package io.github.jspinak.brobot.json.schemaValidation.model;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void testAddError() {
        ValidationResult result = new ValidationResult();
        ValidationError error = new ValidationError("Code1", "Test message", ValidationSeverity.ERROR);

        result.addError(error);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(error, result.getErrors().get(0));
    }

    @Test
    void testMergeValidationResults() {
        ValidationResult result1 = new ValidationResult();
        result1.addError(new ValidationError("Code1", "Error 1", ValidationSeverity.ERROR));

        ValidationResult result2 = new ValidationResult();
        result2.addError(new ValidationError("Code2", "Error 2", ValidationSeverity.WARNING));

        result1.merge(result2);

        assertEquals(2, result1.getErrors().size());
    }

    @Test
    void testHasCriticalErrors() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("Code1", "Critical error", ValidationSeverity.CRITICAL));

        assertTrue(result.hasCriticalErrors());
    }

    @Test
    void testGetFormattedErrors() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("Code1", "Critical error", ValidationSeverity.CRITICAL));
        result.addError(new ValidationError("Code2", "Regular error", ValidationSeverity.ERROR));
        result.addError(new ValidationError("Code3", "Warning", ValidationSeverity.WARNING));

        String formattedErrors = result.getFormattedErrors();

        assertTrue(formattedErrors.contains("CRITICAL ERRORS:"));
        assertTrue(formattedErrors.contains("ERRORS:"));
        assertTrue(formattedErrors.contains("WARNINGS:"));
    }

    @Test
    void testIsValid() {
        ValidationResult result = new ValidationResult();

        assertTrue(result.isValid());

        result.addError(new ValidationError("Code1", "Error", ValidationSeverity.ERROR));

        assertFalse(result.isValid());
    }
}