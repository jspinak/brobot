package io.github.jspinak.brobot.runner.json.validation.exception;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidationExceptionTest {

    @Test
    void constructor_withMessageOnly_shouldSetMessage() {
        String message = "Configuration validation failed";
        
        ConfigValidationException exception = new ConfigValidationException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertNotNull(exception.getValidationResult()); // Empty result is created
    }

    @Test
    void constructor_withMessageAndResult_shouldSetBoth() {
        String message = "Validation errors found";
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("TestError", "Test error message", ValidationSeverity.ERROR));
        
        ConfigValidationException exception = new ConfigValidationException(message, result);
        
        assertTrue(exception.getMessage().startsWith(message));
        assertTrue(exception.getMessage().contains("Test error message"));
        assertSame(result, exception.getValidationResult());
        assertNull(exception.getCause());
        assertTrue(exception.getValidationResult().hasErrors());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        String message = "Validation failed due to exception";
        Exception cause = new IllegalArgumentException("Invalid argument");
        
        ConfigValidationException exception = new ConfigValidationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertSame(cause, exception.getCause());
        assertNotNull(exception.getValidationResult()); // Empty result is created
        assertFalse(exception.getValidationResult().hasErrors());
    }

    @Test
    void constructor_withAllParameters_shouldSetAll() {
        String message = "Complete validation failure";
        Exception cause = new RuntimeException("Runtime error");
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("CriticalError", "Critical failure", ValidationSeverity.CRITICAL));
        
        ConfigValidationException exception = new ConfigValidationException(message, cause, result);
        
        assertTrue(exception.getMessage().startsWith(message));
        assertTrue(exception.getMessage().contains("Critical failure"));
        assertSame(cause, exception.getCause());
        assertSame(result, exception.getValidationResult());
        assertTrue(exception.getValidationResult().hasCriticalErrors());
    }

    @Test
    void getValidationResult_whenNotSet_shouldReturnEmptyResult() {
        ConfigValidationException exception = new ConfigValidationException("Error");
        
        assertNotNull(exception.getValidationResult());
        assertFalse(exception.getValidationResult().hasErrors());
    }

    @Test
    void getValidationResult_withMultipleErrors_shouldReturnAllErrors() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("Error1", "First error", ValidationSeverity.ERROR));
        result.addError(new ValidationError("Error2", "Second error", ValidationSeverity.WARNING));
        result.addError(new ValidationError("Error3", "Third error", ValidationSeverity.INFO));
        
        ConfigValidationException exception = new ConfigValidationException("Multiple errors", result);
        
        ValidationResult actualResult = exception.getValidationResult();
        assertNotNull(actualResult);
        assertEquals(3, actualResult.getErrors().size()); // getErrors() returns all errors including warnings and info
        assertEquals(1, actualResult.getWarnings().size());
        assertEquals(1, actualResult.getInfoMessages().size());
    }

    @Test
    void toString_shouldIncludeMessageAndCause() {
        Exception cause = new IllegalStateException("Invalid state");
        ConfigValidationException exception = new ConfigValidationException("Test error", cause);
        
        String toString = exception.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ConfigValidationException"));
        assertTrue(toString.contains("Test error"));
    }

    @Test
    void validationResult_shouldBeImmutableReference() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("InitialError", "Initial", ValidationSeverity.ERROR));
        
        ConfigValidationException exception = new ConfigValidationException("Error", result);
        
        // Modify the original result after creating exception
        result.addError(new ValidationError("AddedError", "Added later", ValidationSeverity.ERROR));
        
        // The exception should reflect the changes (not a defensive copy)
        assertEquals(2, exception.getValidationResult().getErrors().size());
    }
    
    @Test
    void constructor_withEmptyValidationResult_shouldShowNoErrors() {
        ValidationResult emptyResult = new ValidationResult();
        
        ConfigValidationException exception = new ConfigValidationException(emptyResult);
        
        assertEquals("No validation errors", exception.getMessage());
        assertSame(emptyResult, exception.getValidationResult());
    }

    @Test
    void constructor_withNullMessage_shouldHandleGracefully() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("Error", "Test error", ValidationSeverity.ERROR));
        
        ConfigValidationException exception = new ConfigValidationException(null, result);
        
        // Message will be "null\n" followed by formatted errors
        assertTrue(exception.getMessage().contains("Test error"));
        assertSame(result, exception.getValidationResult());
    }

    @Test
    void constructor_withValidationResultOnly_shouldCreateMessageFromResult() {
        ValidationResult result = new ValidationResult();
        result.addError(new ValidationError("Error1", "First error", ValidationSeverity.CRITICAL));
        result.addError(new ValidationError("Error2", "Second error", ValidationSeverity.ERROR));
        
        ConfigValidationException exception = new ConfigValidationException(result);
        
        String message = exception.getMessage();
        assertTrue(message.contains("CRITICAL ERRORS"));
        assertTrue(message.contains("First error"));
        assertTrue(message.contains("ERRORS"));
        assertTrue(message.contains("Second error"));
        assertSame(result, exception.getValidationResult());
    }

    @Test
    void exception_shouldBeThrowable() {
        ConfigValidationException exception = new ConfigValidationException("Test");
        
        assertThrows(ConfigValidationException.class, () -> {
            throw exception;
        });
    }

    @Test
    void getCause_whenChained_shouldReturnRootCause() {
        IllegalArgumentException rootCause = new IllegalArgumentException("Root");
        RuntimeException middleCause = new RuntimeException("Middle", rootCause);
        ConfigValidationException exception = new ConfigValidationException("Top", middleCause);
        
        assertEquals(middleCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}