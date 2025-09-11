package io.github.jspinak.brobot.runner.json.validation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValidationSeverityTest {

    @Test
    void testValidationSeverityValues() {
        assertEquals(4, ValidationSeverity.values().length);
        assertEquals(ValidationSeverity.WARNING, ValidationSeverity.valueOf("WARNING"));
        assertEquals(ValidationSeverity.ERROR, ValidationSeverity.valueOf("ERROR"));
        assertEquals(ValidationSeverity.CRITICAL, ValidationSeverity.valueOf("CRITICAL"));
        assertEquals(ValidationSeverity.INFO, ValidationSeverity.valueOf("INFO"));
    }
}
