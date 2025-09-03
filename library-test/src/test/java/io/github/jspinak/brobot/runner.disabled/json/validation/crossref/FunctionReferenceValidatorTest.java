package io.github.jspinak.brobot.runner.json.validation.crossref;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.json.validation.crossref.FunctionReferenceValidator;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FunctionReferenceValidatorTest {

    private FunctionReferenceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FunctionReferenceValidator();
    }

    @Test
    void validateInternalReferences_withNullModel_shouldReturnCriticalError() {
        // Act
        ValidationResult result = validator.validateInternalReferences(null);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());
        assertEquals("Invalid DSL model", result.getErrors().get(0).errorCode());
    }

    @Test
    void validateInternalReferences_withValidEmptyModel_shouldReturnNoErrors() {
        // Arrange
        Map<String, Object> dslModel = new HashMap<>();

        // Act
        ValidationResult result = validator.validateInternalReferences(dslModel);

        // Assert
        assertFalse(result.hasErrors());
        assertTrue(result.isValid());
    }

    @Test
    void validateInternalReferences_withInvalidModelType_shouldReturnCriticalError() {
        // Arrange
        String invalidModel = "Not a Map";

        // Act
        ValidationResult result = validator.validateInternalReferences(invalidModel);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());
        assertEquals("Invalid DSL model type", result.getErrors().get(0).errorCode());
    }

    @Test
    void validateInternalReferences_withDuplicateFunctionNames_shouldReturnError() {
        // Arrange
        Map<String, Object> dslModel = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        // Two functions with the same name
        Map<String, Object> function1 = new HashMap<>();
        function1.put("name", "duplicatedFunc");
        functions.add(function1);

        Map<String, Object> function2 = new HashMap<>();
        function2.put("name", "duplicatedFunc");
        functions.add(function2);

        dslModel.put("automationFunctions", functions);

        // Act
        ValidationResult result = validator.validateInternalReferences(dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("duplicatedFunc") &&
                        e.errorCode().equals("Duplicate function name")));
    }

    @Test
    void validateInternalReferences_withInvalidFunctionCall_shouldReturnError() {
        // Arrange
        Map<String, Object> dslModel = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        // A function that calls a non-existent function
        Map<String, Object> function = new HashMap<>();
        function.put("name", "callerFunc");

        List<Map<String, Object>> statements = new ArrayList<>();
        Map<String, Object> methodCall = new HashMap<>();
        methodCall.put("statementType", "methodCall");
        methodCall.put("object", null);
        methodCall.put("method", "nonExistentFunc");
        statements.add(methodCall);

        function.put("statements", statements);
        functions.add(function);
        dslModel.put("automationFunctions", functions);

        // Act
        ValidationResult result = validator.validateInternalReferences(dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("nonExistentFunc") &&
                        e.errorCode().equals("Invalid function call")));
    }

    @Test
    void validateButtonFunctionReferences_bothModelsNull_shouldReturnCriticalErrors() {
        // Act
        ValidationResult result = validator.validateButtonFunctionReferences(null, null);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Invalid project model")));
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Invalid DSL model")));
    }

    @Test
    void validateButtonFunctionReferences_withValidReferences_shouldReturnNoErrors() {
        // Arrange
        Map<String, Object> projectModel = new HashMap<>();
        Map<String, Object> dslModel = new HashMap<>();

        // DSL model with one function
        List<Map<String, Object>> functions = new ArrayList<>();
        Map<String, Object> function = new HashMap<>();
        function.put("name", "validFunction");
        functions.add(function);
        dslModel.put("automationFunctions", functions);

        // Project model with one button that calls the function
        Map<String, Object> automation = new HashMap<>();
        List<Map<String, Object>> buttons = new ArrayList<>();
        Map<String, Object> button = new HashMap<>();
        button.put("id", "button1");
        button.put("functionName", "validFunction");
        buttons.add(button);
        automation.put("buttons", buttons);
        projectModel.put("automation", automation);

        // Act
        ValidationResult result = validator.validateButtonFunctionReferences(projectModel, dslModel);

        // Assert
        assertFalse(result.hasErrors());
        assertTrue(result.isValid());
    }

    @Test
    void validateButtonFunctionReferences_withInvalidFunctionReference_shouldReturnError() {
        // Arrange
        Map<String, Object> projectModel = new HashMap<>();
        Map<String, Object> dslModel = new HashMap<>();

        // DSL model without functions
        dslModel.put("automationFunctions", new ArrayList<>());

        // Project model one button that calls a non-existent function
        Map<String, Object> automation = new HashMap<>();
        List<Map<String, Object>> buttons = new ArrayList<>();
        Map<String, Object> button = new HashMap<>();
        button.put("id", "button1");
        button.put("functionName", "nonExistentFunction");
        buttons.add(button);
        automation.put("buttons", buttons);
        projectModel.put("automation", automation);

        // Act
        ValidationResult result = validator.validateButtonFunctionReferences(projectModel, dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("nonExistentFunction") &&
                        e.errorCode().equals("Invalid function reference")));
    }

    @Test
    void validateButtonFunctionReferences_withIncompatibleParameters_shouldReturnWarning() {
        // Arrange
        Map<String, Object> projectModel = new HashMap<>();
        Map<String, Object> dslModel = new HashMap<>();

        // DSL model with one function that expects parameters
        List<Map<String, Object>> functions = new ArrayList<>();
        Map<String, Object> function = new HashMap<>();
        function.put("name", "paramFunction");

        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("name", "requiredParam");
        parameters.add(parameter);
        function.put("parameters", parameters);

        functions.add(function);
        dslModel.put("automationFunctions", functions);

        // Project model with one button that calls the function but with the wrong parameters.
        Map<String, Object> automation = new HashMap<>();
        List<Map<String, Object>> buttons = new ArrayList<>();
        Map<String, Object> button = new HashMap<>();
        button.put("id", "button1");
        button.put("functionName", "paramFunction");
        Map<String, Object> buttonParams = new HashMap<>();
        buttonParams.put("wrongParam", "value");
        button.put("parameters", buttonParams);
        buttons.add(button);
        automation.put("buttons", buttons);
        projectModel.put("automation", automation);

        // Act
        ValidationResult result = validator.validateButtonFunctionReferences(projectModel, dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("requiredParam") &&
                        e.errorCode().equals("Missing parameter")));
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("wrongParam") &&
                        e.errorCode().equals("Unknown parameter")));
    }

    @Test
    void validateInternalReferences_withInvalidVariableReferences_shouldReturnError() {
        // Arrange
        Map<String, Object> dslModel = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        // Function with a variable that references an invalid variable.
        Map<String, Object> function = new HashMap<>();
        function.put("name", "funcWithInvalidVar");

        List<Map<String, Object>> statements = new ArrayList<>();
        Map<String, Object> varRef = new HashMap<>();
        varRef.put("statementType", "assignment");
        varRef.put("variable", "declaredVar");

        Map<String, Object> valueExpr = new HashMap<>();
        valueExpr.put("expressionType", "variable");
        valueExpr.put("name", "undeclaredVar");
        varRef.put("value", valueExpr);

        statements.add(varRef);
        function.put("statements", statements);
        functions.add(function);
        dslModel.put("automationFunctions", functions);

        // Act
        ValidationResult result = validator.validateInternalReferences(dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("undeclaredVar") &&
                        e.errorCode().equals("Undefined variable")));
    }

    @Test
    void validateInternalReferences_withInvalidMethodCall_shouldReturnError() {
        // Arrange
        Map<String, Object> dslModel = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        // function with an invalid method call
        Map<String, Object> function = new HashMap<>();
        function.put("name", "funcWithInvalidMethod");

        List<Map<String, Object>> statements = new ArrayList<>();
        Map<String, Object> methodCall = new HashMap<>();
        methodCall.put("statementType", "methodCall");
        methodCall.put("object", "action");
        methodCall.put("method", "invalidMethod"); // action does not have "invalidMethod"
        statements.add(methodCall);

        function.put("statements", statements);
        functions.add(function);
        dslModel.put("automationFunctions", functions);

        // Act
        ValidationResult result = validator.validateInternalReferences(dslModel);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("invalidMethod") &&
                        e.errorCode().equals("Invalid method call")));
    }
}