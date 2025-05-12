package io.github.jspinak.brobot.json.schemaValidation.schema;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutomationDSLValidatorTest {

    private AutomationDSLValidator automationDSLValidator;

    @BeforeEach
    void setUp() {
        automationDSLValidator = new AutomationDSLValidator();
    }

    @Test
    void validate_whenJsonIsValid_shouldReturnSuccessfulResult() {
        String validJson = """
        {
            "automationFunctions": [
                {
                    "name": "function1",
                    "returnType": "void",
                    "statements": []
                }
            ]
        }
        """;

        ValidationResult result = automationDSLValidator.validate(validJson);

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void validate_whenJsonIsInvalid_shouldReturnCriticalError() {
        String invalidJson = "{ invalid }";

        ValidationResult result = automationDSLValidator.validate(invalidJson);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("Invalid JSON format", result.getErrors().getFirst().errorCode());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().getFirst().severity());
    }

    @Test
    void validate_whenSchemaValidationFails_shouldReturnErrors() {
        String invalidSchemaJson = """
        {
            "automationFunctions": [
                {
                    "name": null,
                    "returnType": "void"
                }
            ]
        }
        """;

        ValidationResult result = automationDSLValidator.validate(invalidSchemaJson);

        assertFalse(result.isValid());
        assertTrue(result.hasSevereErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Schema validation failed")));
    }

    @Test
    void validate_whenFunctionNamesAreNotUnique_shouldReturnError() {
        String duplicateFunctionNamesJson = """
        {
            "automationFunctions": [
                {
                    "name": "function1",
                    "returnType": "void",
                    "statements": []
                },
                {
                    "name": "function1",
                    "returnType": "void",
                    "statements": []
                }
            ]
        }
        """;

        ValidationResult result = automationDSLValidator.validate(duplicateFunctionNamesJson);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Duplicate function name")));
    }
}