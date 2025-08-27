package io.github.jspinak.brobot.runner.json.validation.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.json.validation.schema.ProjectSchemaValidator;

import static org.junit.jupiter.api.Assertions.*;

class ProjectSchemaValidatorTest {

    private ProjectSchemaValidator projectSchemaValidator;

    @BeforeEach
    void setUp() {
        projectSchemaValidator = new ProjectSchemaValidator();
    }

    @Test
    void validate_whenJsonIsValid_shouldReturnSuccessfulResult() {
        String validJson = """
        {
            "id": 1,
            "name": "Valid Project",
            "states": [
                {"id": 1, "name": "State1"},
                {"id": 2, "name": "State2"}
            ],
            "stateTransitions": [
                {
                    "id": 1,
                    "sourceStateId": 1,
                    "actionDefinition": {
                        "steps": [
                            {
                                "actionOptions": {"action": "CLICK"},
                                "objectCollection": {"stateImages": [101]}
                            }
                        ]
                    },
                    "statesToEnter": [2]
                }
            ]
        }
        """;

        ValidationResult result = projectSchemaValidator.validate(validJson);
        if (!result.isValid()) {
            result.getErrors().forEach(System.out::println);
        }

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void validate_whenJsonIsInvalid_shouldReturnCriticalError() {
        String invalidJson = "{ invalid }";

        ValidationResult result = projectSchemaValidator.validate(invalidJson);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("Invalid JSON format", result.getErrors().getFirst().errorCode());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().getFirst().severity());
    }

    @Test
    void validate_whenStateReferencesAreInvalid_shouldReturnErrors() {
        // This JSON is schema-valid in terms of required fields for a transition,
        // but contains invalid state references
        String invalidStateReferencesJson = """
        {
            "id": 1,
            "name": "Invalid Project State Refs",
            "states": [
                {"id": 1, "name": "State1"}
            ],
            "stateTransitions": [
                {
                    "id": 1,
                    "sourceStateId": 2,
                    "actionDefinition": {
                        "steps": [
                            {
                                "actionOptions": {"action": "CLICK"},
                                "objectCollection": {}
                            }
                        ]
                    },
                    "statesToEnter": [3]
                }
            ]
        }
        """;

        ValidationResult result = projectSchemaValidator.validate(invalidStateReferencesJson);

        // For detailed debugging if the test fails:
        if (result.getErrors().stream().noneMatch(e -> "Invalid state reference".equals(e.errorCode()))) {
            System.out.println("Debug: Errors found in validate_whenStateReferencesAreInvalid_shouldReturnErrors:");
            result.getErrors().forEach(error ->
                    System.out.println("  ErrorCode: " + error.errorCode() +
                            ", Message: " + error.message() +
                            ", Severity: " + error.severity())
            );
            // Specifically check if schema validation errors are occurring
            boolean schemaErrorPresent = result.getErrors().stream()
                    .anyMatch(e -> "Schema validation failed".equals(e.errorCode()) ||
                            "Schema validation error".equals(e.errorCode()));
            if (schemaErrorPresent) {
                System.out.println("Debug: Schema validation errors are present, preventing custom validation.");
            }
        }

        assertFalse(result.isValid(), "Result should be invalid due to state reference errors.");

        // Check that at least one "Invalid state reference" error exists
        assertTrue(result.getErrors().stream()
                        .anyMatch(e -> "Invalid state reference".equals(e.errorCode())),
                "Should contain 'Invalid state reference' error code.");

        // The specific errors are tested.
        // In this case, sourceStateId: 2 is invalid, and stateId: 3 in statesToEnter is invalid.
        long invalidRefCount = result.getErrors().stream()
                .filter(e -> "Invalid state reference".equals(e.errorCode()))
                .count();
        assertEquals(2, invalidRefCount, "Should find two 'Invalid state reference' errors.");
    }

    @Test
    void validate_whenTransitionsAreInconsistent_shouldReturnWarnings() {
        String inconsistentTransitionsJson = """
        {
            "id": 1,
            "name": "Inconsistent Project",
            "states": [
                {"id": 1, "name": "State1"}
            ],
            "stateTransitions": [
                {"id": 1, "sourceStateId": 1, "actionDefinition": {"steps": []}}
            ]
        }
        """;

        ValidationResult result = projectSchemaValidator.validate(inconsistentTransitionsJson);

        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(e -> e.errorCode().equals("Empty action steps")));
    }
}