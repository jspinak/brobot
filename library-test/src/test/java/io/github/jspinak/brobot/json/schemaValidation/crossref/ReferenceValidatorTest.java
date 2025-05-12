package io.github.jspinak.brobot.json.schemaValidation.crossref;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceValidatorTest {

    @Mock
    private StateReferenceValidator stateReferenceValidator;

    @Mock
    private FunctionReferenceValidator functionReferenceValidator;

    private ReferenceValidator validator;
    private Map<String, Object> projectModel;
    private Map<String, Object> dslModel;

    @BeforeEach
    void setUp() {
        validator = new ReferenceValidator(stateReferenceValidator, functionReferenceValidator);
        projectModel = new HashMap<>();
        dslModel = new HashMap<>();
    }

    @Test
    void validateReferences_withNoErrors_shouldReturnValidResult() {
        // Arrange
        ValidationResult emptyResult = new ValidationResult();
        when(stateReferenceValidator.validateInternalReferences(any())).thenReturn(emptyResult);
        when(functionReferenceValidator.validateInternalReferences(any())).thenReturn(emptyResult);
        when(stateReferenceValidator.validateStateReferencesInFunctions(any(), any())).thenReturn(emptyResult);
        when(functionReferenceValidator.validateButtonFunctionReferences(any(), any())).thenReturn(emptyResult);

        // Act
        ValidationResult result = validator.validateReferences(projectModel, dslModel);

        // Assert
        assertFalse(result.hasErrors());
        assertTrue(result.isValid());
        verify(stateReferenceValidator).validateInternalReferences(projectModel);
        verify(functionReferenceValidator).validateInternalReferences(dslModel);
        verify(stateReferenceValidator).validateStateReferencesInFunctions(projectModel, dslModel);
        verify(functionReferenceValidator).validateButtonFunctionReferences(projectModel, dslModel);
    }

    @Test
    void validateReferences_withInternalErrors_shouldMergeResults() {
        // Arrange
        ValidationResult stateResult = new ValidationResult();
        stateResult.addError("state-error", "State internal error", ValidationSeverity.ERROR);

        ValidationResult functionResult = new ValidationResult();
        functionResult.addError("function-error", "Function internal error", ValidationSeverity.WARNING);

        when(stateReferenceValidator.validateInternalReferences(any())).thenReturn(stateResult);
        when(functionReferenceValidator.validateInternalReferences(any())).thenReturn(functionResult);
        when(stateReferenceValidator.validateStateReferencesInFunctions(any(), any())).thenReturn(new ValidationResult());
        when(functionReferenceValidator.validateButtonFunctionReferences(any(), any())).thenReturn(new ValidationResult());

        // Act
        ValidationResult result = validator.validateReferences(projectModel, dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasSevereErrors());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.errorCode().equals("state-error")));
        assertTrue(result.getErrors().stream().anyMatch(e -> e.errorCode().equals("function-error")));
    }

    @Test
    void validateReferences_withCrossReferenceErrors_shouldMergeResults() {
        // Arrange
        ValidationResult stateInFuncsResult = new ValidationResult();
        stateInFuncsResult.addError("state-func-error", "Invalid state in function", ValidationSeverity.ERROR);

        ValidationResult buttonFuncResult = new ValidationResult();
        buttonFuncResult.addError("button-func-error", "Invalid button function", ValidationSeverity.ERROR);

        when(stateReferenceValidator.validateInternalReferences(any())).thenReturn(new ValidationResult());
        when(functionReferenceValidator.validateInternalReferences(any())).thenReturn(new ValidationResult());
        when(stateReferenceValidator.validateStateReferencesInFunctions(any(), any())).thenReturn(stateInFuncsResult);
        when(functionReferenceValidator.validateButtonFunctionReferences(any(), any())).thenReturn(buttonFuncResult);

        // Act
        ValidationResult result = validator.validateReferences(projectModel, dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.errorCode().equals("state-func-error")));
        assertTrue(result.getErrors().stream().anyMatch(e -> e.errorCode().equals("button-func-error")));
    }

    @Test
    void validateReferences_withException_shouldHandleGracefully() {
        // Arrange
        when(stateReferenceValidator.validateInternalReferences(any()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        ValidationResult result = validator.validateReferences(projectModel, dslModel);

        // Assert
        // The code does not have explicit error messages for unexpected behavior.
        // Therefore, only logging is performed.
        // The implementation does not provide a generic error message.
        assertFalse(result.hasErrors());
    }

    @Test
    void validateReferences_withNullInputs_shouldHandleGracefully() {
        // Act & Assert - should not return exceptions
        ValidationResult result = validator.validateReferences(null, null);

        // The implementation should pass Null values to the validator
        verify(stateReferenceValidator).validateInternalReferences(null);
        verify(functionReferenceValidator).validateInternalReferences(null);
    }
}