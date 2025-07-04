package io.github.jspinak.brobot.runner.json.validation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationResult without Spring dependencies.
 * Migrated from library-test module.
 */
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

    @Test
    void testIsValidWithWarnings() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("Code1", "Warning", ValidationSeverity.WARNING));

        // Should still be valid with only warnings
        assertTrue(result.isValid());
    }

    @Test
    void testHasErrorsOfSeverity() {
        ValidationResult result = new ValidationResult();
        
        assertFalse(result.hasErrorsOfSeverity(ValidationSeverity.ERROR));
        assertFalse(result.hasErrorsOfSeverity(ValidationSeverity.WARNING));
        assertFalse(result.hasErrorsOfSeverity(ValidationSeverity.CRITICAL));

        result.addError(new ValidationError("Code1", "Warning", ValidationSeverity.WARNING));
        
        assertFalse(result.hasErrorsOfSeverity(ValidationSeverity.ERROR));
        assertTrue(result.hasErrorsOfSeverity(ValidationSeverity.WARNING));
        assertFalse(result.hasErrorsOfSeverity(ValidationSeverity.CRITICAL));
    }

    @Test
    void testClear() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("Code1", "Error 1", ValidationSeverity.ERROR));
        result.addError(new ValidationError("Code2", "Error 2", ValidationSeverity.WARNING));

        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());

        result.clear();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void testGetErrorsBySeverity() {
        ValidationResult result = new ValidationResult();
        ValidationError error1 = new ValidationError("Code1", "Error 1", ValidationSeverity.ERROR);
        ValidationError error2 = new ValidationError("Code2", "Warning 1", ValidationSeverity.WARNING);
        ValidationError error3 = new ValidationError("Code3", "Error 2", ValidationSeverity.ERROR);
        
        result.addError(error1);
        result.addError(error2);
        result.addError(error3);

        var errors = result.getErrorsBySeverity(ValidationSeverity.ERROR);
        assertEquals(2, errors.size());
        assertTrue(errors.contains(error1));
        assertTrue(errors.contains(error3));

        var warnings = result.getErrorsBySeverity(ValidationSeverity.WARNING);
        assertEquals(1, warnings.size());
        assertTrue(warnings.contains(error2));
    }
}