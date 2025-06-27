package io.github.jspinak.brobot.json.schemaValidation.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.json.validation.business.FunctionRuleValidator;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FunctionRuleValidatorTest {

    private FunctionRuleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FunctionRuleValidator();
    }

    @Test
    void validateFunctionRules_withNullModel_shouldReturnCriticalError() {
        // Act
        ValidationResult result = validator.validateFunctionRules(null);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());
        assertEquals("Invalid DSL model", result.getErrors().get(0).errorCode());
    }

    @Test
    void validateFunctionRules_withInvalidModelType_shouldReturnCriticalError() {
        // Arrange
        String invalidModel = "Not a Map";

        // Act
        ValidationResult result = validator.validateFunctionRules(invalidModel);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());
        assertEquals("Invalid DSL model type", result.getErrors().get(0).errorCode());
    }

    @Test
    void validateFunctionRules_withEmptyModel_shouldNotReturnErrors() {
        // Arrange
        Map<String, Object> emptyModel = new HashMap<>();

        // Act
        ValidationResult result = validator.validateFunctionRules(emptyModel);

        // Assert
        assertFalse(result.hasErrors());
        assertTrue(result.isValid());
    }

    @Test
    void validateFunctionRules_withTooComplexFunction_shouldReturnError() {
        // Arrange
        Map<String, Object> model = createModelWithComplexFunction();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Function too complex")));
    }

    @Test
    void validateFunctionRules_withDeepNesting_shouldReturnError() {
        // Arrange
        Map<String, Object> model = createModelWithDeepNesting();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Excessive nesting")));
    }

    @Test
    void validateFunctionRules_withTooManyActionCalls_shouldReturnError() {
        // Arrange
        Map<String, Object> model = createModelWithManyActionCalls();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Too many action calls")));
    }

    @Test
    void validateFunctionRules_withNoErrorHandling_shouldReturnWarning() {
        // Arrange
        Map<String, Object> model = createModelWithNoErrorHandling();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Insufficient error handling") &&
                        e.severity() == ValidationSeverity.WARNING));
    }

    @Test
    void validateFunctionRules_withStateModificationIssues_shouldReturnWarning() {
        // Arrange
        Map<String, Object> model = createModelWithStateModificationIssues();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("State management concern") &&
                        e.severity() == ValidationSeverity.WARNING));
    }

    @Test
    void validateFunctionRules_withRepeatedImageSearches_shouldReturnWarning() {
        // Arrange
        Map<String, Object> model = createModelWithRepeatedImageSearches();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Inefficient search pattern") &&
                        e.severity() == ValidationSeverity.WARNING));
    }

    @Test
    void validateFunctionRules_withHeavyOperations_shouldReturnWarning() {
        // Arrange
        Map<String, Object> model = createModelWithHeavyOperations();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Performance concern") &&
                        e.severity() == ValidationSeverity.WARNING));
    }

    @Test
    void validateFunctionRules_withUnnecessaryWaits_shouldReturnWarning() {
        // Arrange
        Map<String, Object> model = createModelWithUnnecessaryWaits();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Inefficient wait pattern") &&
                        e.severity() == ValidationSeverity.WARNING));
    }

    @Test
    void validateFunctionRules_withMultipleIssues_shouldReturnAllErrors() {
        // Arrange
        Map<String, Object> model = createModelWithMultipleIssues();

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().size() > 1);
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Function too complex")));
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Insufficient error handling")));
    }

    @Test
    void validateFunctionRules_withException_shouldHandleGracefully() {
        // Arrange
        Map<String, Object> model = new HashMap<>();
        model.put("automationFunctions", "not a list");

        // Act
        ValidationResult result = validator.validateFunctionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Validation error") &&
                        e.severity() == ValidationSeverity.ERROR));
    }

    // Helper methods for creating test models

    private Map<String, Object> createModelWithComplexFunction() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "complexFunction");

        List<Map<String, Object>> statements = new ArrayList<>();
        // Create many simple statements to exceed MAX_STATEMENTS_PER_FUNCTION
        for (int i = 0; i < 51; i++) {
            Map<String, Object> statement = new HashMap<>();
            statement.put("statementType", "assignment");
            statement.put("variable", "var" + i);
            statement.put("value", createLiteralExpression("value" + i));
            statements.add(statement);
        }

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithDeepNesting() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "deeplyNestedFunction");

        // Create deeply nested if statements (depth > MAX_DEPTH)
        List<Map<String, Object>> statements = new ArrayList<>();
        Map<String, Object> currentIf = createIfStatement("level1");
        statements.add(currentIf);

        List<Map<String, Object>> currentStatements = (List<Map<String, Object>>) currentIf.get("thenStatements");

        for (int i = 2; i <= 6; i++) {
            Map<String, Object> nestedIf = createIfStatement("level" + i);
            currentStatements.add(nestedIf);
            currentStatements = (List<Map<String, Object>>) nestedIf.get("thenStatements");
        }

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithManyActionCalls() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "manyActionCallsFunction");

        List<Map<String, Object>> statements = new ArrayList<>();
        // Create many action calls to exceed MAX_ACTION_CALLS
        for (int i = 0; i < 21; i++) {
            statements.add(createActionMethodCall("find"));
        }

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithNoErrorHandling() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "noErrorHandlingFunction");

        List<Map<String, Object>> statements = new ArrayList<>();
        // Add action calls without checking results
        statements.add(createActionMethodCall("find"));
        statements.add(createActionMethodCall("click"));

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithStateModificationIssues() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "stateModificationWithoutChecks");

        List<Map<String, Object>> statements = new ArrayList<>();
        // Modify state without checking state
        Map<String, Object> stateModification = new HashMap<>();
        stateModification.put("statementType", "methodCall");
        stateModification.put("object", "stateTransitionsManagement");
        stateModification.put("method", "openState");
        statements.add(stateModification);

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithRepeatedImageSearches() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "repeatedImageSearches");

        List<Map<String, Object>> statements = new ArrayList<>();

        // Create action calls with the same target (simulated by same arguments)
        Map<String, Object> searchCall1 = createActionMethodCall("find");
        List<Map<String, Object>> args1 = new ArrayList<>();
        args1.add(createLiteralExpression("same-image"));
        searchCall1.put("arguments", args1);

        Map<String, Object> searchCall2 = createActionMethodCall("find");
        List<Map<String, Object>> args2 = new ArrayList<>();
        args2.add(createLiteralExpression("same-image"));
        searchCall2.put("arguments", args2);

        statements.add(searchCall1);
        statements.add(searchCall2);

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithHeavyOperations() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "heavyOperationsFunction");

        List<Map<String, Object>> statements = new ArrayList<>();

        // Add heavy operations
        Map<String, Object> heavyOp = createActionMethodCall("findAll");
        statements.add(heavyOp);

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithUnnecessaryWaits() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "unnecessaryWaitsFunction");

        List<Map<String, Object>> statements = new ArrayList<>();

        // Add fixed wait
        Map<String, Object> wait = new HashMap<>();
        wait.put("statementType", "methodCall");
        wait.put("object", "Thread");
        wait.put("method", "sleep");
        statements.add(wait);

        // Without conditional wait
        Map<String, Object> unrelatedStatement = new HashMap<>();
        unrelatedStatement.put("statementType", "assignment");
        unrelatedStatement.put("variable", "x");
        unrelatedStatement.put("value", createLiteralExpression("value"));
        statements.add(unrelatedStatement);

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    private Map<String, Object> createModelWithMultipleIssues() {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();

        Map<String, Object> function = new HashMap<>();
        function.put("name", "multipleIssuesFunction");

        List<Map<String, Object>> statements = new ArrayList<>();

        // Too many statements
        for (int i = 0; i < 51; i++) {
            Map<String, Object> statement = new HashMap<>();
            statement.put("statementType", "assignment");
            statement.put("variable", "var" + i);
            statement.put("value", createLiteralExpression("value" + i));
            statements.add(statement);
        }

        // Add action call without error checking
        statements.add(createActionMethodCall("find"));

        function.put("statements", statements);
        functions.add(function);
        model.put("automationFunctions", functions);

        return model;
    }

    // Helper methods for creating statement objects

    private Map<String, Object> createIfStatement(String conditionName) {
        Map<String, Object> ifStatement = new HashMap<>();
        ifStatement.put("statementType", "if");

        Map<String, Object> condition = new HashMap<>();
        condition.put("expressionType", "variable");
        condition.put("name", conditionName);
        ifStatement.put("condition", condition);

        ifStatement.put("thenStatements", new ArrayList<>());
        ifStatement.put("elseStatements", new ArrayList<>());

        return ifStatement;
    }

    private Map<String, Object> createActionMethodCall(String methodName) {
        Map<String, Object> methodCall = new HashMap<>();
        methodCall.put("statementType", "methodCall");
        methodCall.put("object", "action");
        methodCall.put("method", methodName);
        return methodCall;
    }

    private Map<String, Object> createLiteralExpression(String value) {
        Map<String, Object> expression = new HashMap<>();
        expression.put("expressionType", "literal");
        expression.put("value", value);
        return expression;
    }
}