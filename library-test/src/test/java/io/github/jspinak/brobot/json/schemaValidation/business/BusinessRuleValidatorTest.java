package io.github.jspinak.brobot.json.schemaValidation.business;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessRuleValidatorTest {

    @Mock
    private TransitionRuleValidator transitionRuleValidator;

    @Mock
    private FunctionRuleValidator functionRuleValidator;

    private BusinessRuleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BusinessRuleValidator(transitionRuleValidator, functionRuleValidator);
    }

    @Test
    void validateRules_withNoErrors_shouldReturnValidResult() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        ValidationResult transitionResult = new ValidationResult();
        ValidationResult functionResult = new ValidationResult();

        when(transitionRuleValidator.validateTransitionRules(projectModel)).thenReturn(transitionResult);
        when(functionRuleValidator.validateFunctionRules(dslModel)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }

    @Test
    void validateRules_withTransitionErrors_shouldReturnInvalidResult() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        ValidationResult transitionResult = new ValidationResult();
        transitionResult.addError("ERROR_CODE", "Error message", ValidationSeverity.ERROR);

        ValidationResult functionResult = new ValidationResult();

        when(transitionRuleValidator.validateTransitionRules(projectModel)).thenReturn(transitionResult);
        when(functionRuleValidator.validateFunctionRules(dslModel)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("ERROR_CODE", result.getErrors().get(0).errorCode());

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }

    @Test
    void validateRules_withFunctionErrors_shouldReturnInvalidResult() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        ValidationResult transitionResult = new ValidationResult();

        ValidationResult functionResult = new ValidationResult();
        functionResult.addError("FUNCTION_ERROR", "Function error message", ValidationSeverity.ERROR);

        when(transitionRuleValidator.validateTransitionRules(projectModel)).thenReturn(transitionResult);
        when(functionRuleValidator.validateFunctionRules(dslModel)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("FUNCTION_ERROR", result.getErrors().get(0).errorCode());

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }

    @Test
    void validateRules_withBothErrors_shouldMergeAllErrors() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        ValidationResult transitionResult = new ValidationResult();
        transitionResult.addError("TRANSITION_ERROR", "Transition error message", ValidationSeverity.ERROR);

        ValidationResult functionResult = new ValidationResult();
        functionResult.addError("FUNCTION_ERROR", "Function error message", ValidationSeverity.WARNING);

        when(transitionRuleValidator.validateTransitionRules(projectModel)).thenReturn(transitionResult);
        when(functionRuleValidator.validateFunctionRules(dslModel)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().stream().anyMatch(e -> "TRANSITION_ERROR".equals(e.errorCode())));
        assertTrue(result.getErrors().stream().anyMatch(e -> "FUNCTION_ERROR".equals(e.errorCode())));

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }

    @Test
    void validateRules_withCriticalError_shouldHaveCriticalSeverity() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        ValidationResult transitionResult = new ValidationResult();
        transitionResult.addError("CRITICAL_ERROR", "Critical error message", ValidationSeverity.CRITICAL);

        ValidationResult functionResult = new ValidationResult();

        when(transitionRuleValidator.validateTransitionRules(projectModel)).thenReturn(transitionResult);
        when(functionRuleValidator.validateFunctionRules(dslModel)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.hasCriticalErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }

    @Test
    void validateRules_withWarningOnly_shouldBeValid() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        ValidationResult transitionResult = new ValidationResult();
        transitionResult.addError("WARNING", "Warning message", ValidationSeverity.WARNING);

        ValidationResult functionResult = new ValidationResult();

        when(transitionRuleValidator.validateTransitionRules(projectModel)).thenReturn(transitionResult);
        when(functionRuleValidator.validateFunctionRules(dslModel)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertTrue(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertFalse(result.hasSevereErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.WARNING, result.getErrors().get(0).severity());

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }

    @Test
    void validateRules_withNullModels_shouldPassNullToValidators() {
        // Arrange
        ValidationResult transitionResult = new ValidationResult();
        ValidationResult functionResult = new ValidationResult();

        when(transitionRuleValidator.validateTransitionRules(null)).thenReturn(transitionResult);
        when(functionRuleValidator.validateFunctionRules(null)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(null, null);

        // Assert
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());

        verify(transitionRuleValidator).validateTransitionRules(null);
        verify(functionRuleValidator).validateFunctionRules(null);
    }

    @Test
    void validateRules_whenTransitionValidatorThrowsException_shouldHandleGracefully() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        when(transitionRuleValidator.validateTransitionRules(projectModel))
                .thenThrow(new RuntimeException("Validation error"));

        ValidationResult functionResult = new ValidationResult();
        when(functionRuleValidator.validateFunctionRules(dslModel)).thenReturn(functionResult);

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }

    @Test
    void validateRules_whenFunctionValidatorThrowsException_shouldHandleGracefully() {
        // Arrange
        Object projectModel = new Object();
        Object dslModel = new Object();

        ValidationResult transitionResult = new ValidationResult();
        when(transitionRuleValidator.validateTransitionRules(projectModel)).thenReturn(transitionResult);

        when(functionRuleValidator.validateFunctionRules(dslModel))
                .thenThrow(new RuntimeException("Validation error"));

        // Act
        ValidationResult result = validator.validateRules(projectModel, dslModel);

        // Assert
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());

        verify(transitionRuleValidator).validateTransitionRules(projectModel);
        verify(functionRuleValidator).validateFunctionRules(dslModel);
    }
}