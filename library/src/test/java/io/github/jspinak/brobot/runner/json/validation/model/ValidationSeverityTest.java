package io.github.jspinak.brobot.runner.json.validation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationSeverity enum without Spring dependencies.
 * Migrated from library-test module.
 */
class ValidationSeverityTest {

    @Test
    void testValidationSeverityValues() {
        assertEquals(4, ValidationSeverity.values().length);
        assertEquals(ValidationSeverity.WARNING, ValidationSeverity.valueOf("WARNING"));
        assertEquals(ValidationSeverity.ERROR, ValidationSeverity.valueOf("ERROR"));
        assertEquals(ValidationSeverity.CRITICAL, ValidationSeverity.valueOf("CRITICAL"));
        assertEquals(ValidationSeverity.INFO, ValidationSeverity.valueOf("INFO"));
    }

    @Test
    void testValidationSeverityOrdinal() {
        // Test that severity levels are ordered correctly
        assertTrue(ValidationSeverity.INFO.ordinal() < ValidationSeverity.WARNING.ordinal());
        assertTrue(ValidationSeverity.WARNING.ordinal() < ValidationSeverity.ERROR.ordinal());
        assertTrue(ValidationSeverity.ERROR.ordinal() < ValidationSeverity.CRITICAL.ordinal());
    }

    @Test
    void testValidationSeverityName() {
        assertEquals("INFO", ValidationSeverity.INFO.name());
        assertEquals("WARNING", ValidationSeverity.WARNING.name());
        assertEquals("ERROR", ValidationSeverity.ERROR.name());
        assertEquals("CRITICAL", ValidationSeverity.CRITICAL.name());
    }

    @Test
    void testValidationSeverityValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> 
            ValidationSeverity.valueOf("INVALID")
        );
    }

    @Test
    void testValidationSeverityComparison() {
        // Test that we can compare severities
        ValidationSeverity info = ValidationSeverity.INFO;
        ValidationSeverity warning = ValidationSeverity.WARNING;
        ValidationSeverity error = ValidationSeverity.ERROR;
        ValidationSeverity critical = ValidationSeverity.CRITICAL;

        // Using ordinal for comparison
        assertTrue(info.ordinal() < warning.ordinal());
        assertTrue(warning.ordinal() < error.ordinal());
        assertTrue(error.ordinal() < critical.ordinal());
    }

    @Test
    void testAllSeveritiesPresent() {
        ValidationSeverity[] severities = ValidationSeverity.values();
        
        boolean hasInfo = false;
        boolean hasWarning = false;
        boolean hasError = false;
        boolean hasCritical = false;
        
        for (ValidationSeverity severity : severities) {
            switch (severity) {
                case INFO -> hasInfo = true;
                case WARNING -> hasWarning = true;
                case ERROR -> hasError = true;
                case CRITICAL -> hasCritical = true;
            }
        }
        
        assertTrue(hasInfo, "INFO severity should be present");
        assertTrue(hasWarning, "WARNING severity should be present");
        assertTrue(hasError, "ERROR severity should be present");
        assertTrue(hasCritical, "CRITICAL severity should be present");
    }
}