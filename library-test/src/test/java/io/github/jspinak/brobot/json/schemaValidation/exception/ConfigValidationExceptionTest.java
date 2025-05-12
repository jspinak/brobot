package io.github.jspinak.brobot.json.schemaValidation.exception;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidationExceptionTest {

    @Test
    void constructor_withMessageOnly_shouldSetMessage() {
        String message = "Validation error";
        ConfigValidationException exception = new ConfigValidationException(message);

        assertEquals(message, exception.getMessage());
        assertNotNull(exception.getValidationResult());
        assertFalse(exception.getValidationResult().hasErrors());
    }

    @Test
    void constructor_withValidationResultOnly_shouldCreateMessageFromResult() {
        ValidationResult result = new ValidationResult();
        result.addError("ERR-001", "Some error", ValidationSeverity.ERROR);

        ConfigValidationException exception = new ConfigValidationException(result);

        assertTrue(exception.getMessage().contains("ERR-001"));
        assertTrue(exception.getMessage().contains("Some error"));
        assertEquals(result, exception.getValidationResult());
    }

    @Test
    void constructor_withMessageAndValidationResult_shouldCombineMessages() {
        String message = "Validation error";
        ValidationResult result = new ValidationResult();
        result.addError("ERR-001", "Error occurred", ValidationSeverity.ERROR);

        ConfigValidationException exception = new ConfigValidationException(message, result);

        assertTrue(exception.getMessage().startsWith(message));
        assertTrue(exception.getMessage().contains("ERR-001"));
        assertTrue(exception.getMessage().contains("Error occurred"));
        assertEquals(result, exception.getValidationResult());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetCause() {
        String message = "Validation error";
        Throwable cause = new RuntimeException("Original error");

        ConfigValidationException exception = new ConfigValidationException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getValidationResult());
        assertFalse(exception.getValidationResult().hasErrors());
    }

    @Test
    void constructor_withMessageCauseAndValidationResult_shouldSetAll() {
        String message = "Validation error";
        Throwable cause = new RuntimeException("Original error");
        ValidationResult result = new ValidationResult();
        result.addError("ERR-001", "Error occurred", ValidationSeverity.ERROR);

        ConfigValidationException exception = new ConfigValidationException(message, cause, result);

        assertTrue(exception.getMessage().startsWith(message));
        assertTrue(exception.getMessage().contains("ERR-001"));
        assertEquals(cause, exception.getCause());
        assertEquals(result, exception.getValidationResult());
    }

    @Test
    void createMessageFromResult_withNullResult_shouldHandleGracefully() {
        ConfigValidationException exception = new ConfigValidationException((ValidationResult)null);

        assertEquals("No validation errors", exception.getMessage());
    }

    @Test
    void createMessageFromResult_withEmptyResult_shouldShowNoErrors() {
        ValidationResult result = new ValidationResult();

        ConfigValidationException exception = new ConfigValidationException(result);

        assertEquals("No validation errors", exception.getMessage());
    }

    @Test
    void createMessageFromResult_withMultipleSeverities_shouldFormatCorrectly() {
        ValidationResult result = new ValidationResult();
        result.addError("CRITICAL-001", "Critical Error", ValidationSeverity.CRITICAL);
        result.addError("ERR-001", "Normal Error", ValidationSeverity.ERROR);
        result.addError("WARN-001", "Warning", ValidationSeverity.WARNING);

        ConfigValidationException exception = new ConfigValidationException(result);

        String message = exception.getMessage();
        assertTrue(message.contains("CRITICAL ERRORS"));
        assertTrue(message.contains("Critical Error"));
        assertTrue(message.contains("ERRORS"));
        assertTrue(message.contains("Normal Error"));
        assertTrue(message.contains("WARNINGS"));
        assertTrue(message.contains("Warning"));

        // Verify the order: Critical > Error > Warning
        int criticalPos = message.indexOf("CRITICAL ERRORS");
        int errorPos = message.indexOf("ERRORS");
        int warningPos = message.indexOf("WARNINGS");

        assertTrue(criticalPos < errorPos);
        assertTrue(errorPos < warningPos);
    }

    @Test
    void getValidationResult_shouldReturnCorrectInstance() {
        ValidationResult result = new ValidationResult();
        result.addError("ERR-001", "Testing error", ValidationSeverity.ERROR);

        ConfigValidationException exception = new ConfigValidationException(result);

        ValidationResult returnedResult = exception.getValidationResult();
        assertSame(result, returnedResult);
        assertEquals(1, returnedResult.getErrors().size());
        assertEquals("ERR-001", returnedResult.getErrors().getFirst().errorCode());
    }
}