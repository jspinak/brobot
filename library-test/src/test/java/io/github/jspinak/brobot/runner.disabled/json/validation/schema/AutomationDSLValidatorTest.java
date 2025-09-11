package io.github.jspinak.brobot.runner.json.validation.schema;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

class AutomationDSLValidatorTest {

    private AutomationDSLValidator automationDSLValidator;

    @BeforeEach
    void setUp() {
        automationDSLValidator = new AutomationDSLValidator();
    }

    @Test
    void validate_whenJsonIsValid_shouldReturnSuccessfulResult() {
        String validJson =
                """
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
        String invalidSchemaJson =
                """
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
        assertTrue(
                result.getErrors().stream()
                        .anyMatch(e -> e.errorCode().equals("Schema validation failed")));
    }

    @Test
    void validate_whenFunctionNamesAreNotUnique_shouldReturnError() {
        String duplicateFunctionNamesJson =
                """
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
        assertTrue(
                result.getErrors().stream()
                        .anyMatch(e -> e.errorCode().equals("Duplicate function name")));
    }

    @Test
    void validate_whenNonVoidFunctionMissingReturn_shouldReturnError() {
        // Using "int" which is valid in the schema enum
        String functionMissingReturnJson =
                """
        {
            "automationFunctions": [
                {
                    "name": "getNumber",
                    "returnType": "int",
                    "statements": []
                }
            ]
        }
        """;

        ValidationResult result = automationDSLValidator.validate(functionMissingReturnJson);

        assertFalse(result.isValid());
        assertTrue(
                result.getErrors().stream()
                        .anyMatch(e -> e.errorCode().equals("Missing return statement")));
    }

    @Test
    void validate_whenFunctionHasNoName_shouldReturnError() {
        // Schema requires name, so this will fail with critical error
        String missingNameJson =
                """
        {
            "automationFunctions": [
                {
                    "returnType": "void",
                    "statements": []
                }
            ]
        }
        """;

        ValidationResult result = automationDSLValidator.validate(missingNameJson);

        assertFalse(result.isValid());
        assertTrue(result.hasCriticalErrors());
        assertTrue(
                result.getErrors().stream()
                        .anyMatch(e -> e.errorCode().equals("Schema validation failed")));
    }
}
