package io.github.jspinak.brobot.runner.json.validation.business;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for FunctionRuleValidator.
 * Tests validation of automation functions for complexity, error handling,
 * state management, and performance patterns.
 */
@DisplayName("FunctionRuleValidator Tests")
public class FunctionRuleValidatorTest extends BrobotTestBase {
    
    private FunctionRuleValidator validator;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        validator = new FunctionRuleValidator();
    }
    
    // Helper methods for test data creation
    private Map<String, Object> createDSLModel() {
        Map<String, Object> dsl = new HashMap<>();
        dsl.put("automationFunctions", new ArrayList<>());
        return dsl;
    }
    
    private Map<String, Object> createFunction(String name) {
        Map<String, Object> function = new HashMap<>();
        function.put("name", name);
        function.put("statements", new ArrayList<>());
        return function;
    }
    
    private Map<String, Object> createStatement(String type) {
        Map<String, Object> statement = new HashMap<>();
        statement.put("statementType", type);
        return statement;
    }
    
    private Map<String, Object> createActionCall(String action, String target) {
        Map<String, Object> actionCall = new HashMap<>();
        actionCall.put("statementType", "methodCall");
        actionCall.put("object", "action");
        actionCall.put("method", action);
        actionCall.put("target", target);
        return actionCall;
    }
    
    private Map<String, Object> createIfStatement(List<Map<String, Object>> thenBlock, List<Map<String, Object>> elseBlock) {
        Map<String, Object> ifStatement = new HashMap<>();
        ifStatement.put("statementType", "if");
        ifStatement.put("condition", "someCondition");
        ifStatement.put("thenStatements", thenBlock);
        if (elseBlock != null) {
            ifStatement.put("elseStatements", elseBlock);
        }
        return ifStatement;
    }
    
    private Map<String, Object> createWhileLoop(List<Map<String, Object>> body) {
        Map<String, Object> whileLoop = new HashMap<>();
        whileLoop.put("statementType", "forEach");
        whileLoop.put("collection", "someCollection");
        whileLoop.put("statements", body);
        return whileLoop;
    }
    
    private boolean hasError(ValidationResult result, String context) {
        return result.getErrors().stream()
            .anyMatch(e -> e.message().contains(context));
    }
    
    private boolean hasWarning(ValidationResult result, String context) {
        return result.getWarnings().stream()
            .anyMatch(e -> e.message().contains(context));
    }
    
    @Nested
    @DisplayName("Basic Validation")
    class BasicValidation {
        
        @Test
        @DisplayName("Handle null DSL model")
        public void testNullDSLModel() {
            ValidationResult result = validator.validateFunctionRules(null);
            
            assertFalse(result.isValid());
            assertEquals(1, result.getErrors().size());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationSeverity.CRITICAL, error.severity());
            assertTrue(error.message().contains("DSL model is null"));
        }
        
        @Test
        @DisplayName("Handle invalid DSL model type")
        public void testInvalidDSLModelType() {
            ValidationResult result = validator.validateFunctionRules("invalid");
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "DSL model could not be processed"));
        }
        
        @Test
        @DisplayName("Handle empty DSL model")
        public void testEmptyDSLModel() {
            Map<String, Object> dsl = new HashMap<>();
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }
        
        @Test
        @DisplayName("Handle DSL with no functions")
        public void testDSLWithNoFunctions() {
            Map<String, Object> dsl = createDSLModel();
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }
        
        @Test
        @DisplayName("Handle function without name")
        public void testFunctionWithoutName() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = new HashMap<>();
            function.put("statements", List.of(createStatement("action")));
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should handle gracefully with "unknown" as name
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle function without statements")
        public void testFunctionWithoutStatements() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = new HashMap<>();
            function.put("name", "EmptyFunc");
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Complexity Validation")
    class ComplexityValidation {
        
        @Test
        @DisplayName("Detect function with too many statements")
        public void testTooManyStatements() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("ComplexFunc");
            
            // Add 51 statements (exceeds max of 50)
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 51; i++) {
                statements.add(createStatement("action"));
            }
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "ComplexFunc"));
            assertTrue(hasWarning(result, "51 statements"));
            assertTrue(hasWarning(result, "exceeds the recommended maximum of 50"));
        }
        
        @Test
        @DisplayName("Accept function within statement limit")
        public void testWithinStatementLimit() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("SimpleFunc");
            
            // Add 50 statements (at the limit)
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                statements.add(createStatement("action"));
            }
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertFalse(hasWarning(result, "exceeds the recommended maximum"));
        }
        
        @Test
        @DisplayName("Detect excessive nesting depth")
        public void testExcessiveNestingDepth() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("DeepNestingFunc");
            
            // Create deeply nested structure (6 levels deep, exceeds max of 5)
            List<Map<String, Object>> level6 = List.of(createStatement("action"));
            List<Map<String, Object>> level5 = List.of(createIfStatement(level6, null));
            List<Map<String, Object>> level4 = List.of(createWhileLoop(level5));
            List<Map<String, Object>> level3 = List.of(createIfStatement(level4, null));
            List<Map<String, Object>> level2 = List.of(createWhileLoop(level3));
            List<Map<String, Object>> level1 = List.of(createIfStatement(level2, null));
            
            function.put("statements", level1);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "DeepNestingFunc"));
            assertTrue(hasWarning(result, "nesting depth"));
            assertTrue(hasWarning(result, "exceeds the recommended maximum"));
        }
        
        @Test
        @DisplayName("Accept function within nesting depth limit")
        public void testWithinNestingDepthLimit() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("ModerateNestingFunc");
            
            // Create nested structure (5 levels deep, at the limit)
            List<Map<String, Object>> level5 = List.of(createStatement("action"));
            List<Map<String, Object>> level4 = List.of(createIfStatement(level5, null));
            List<Map<String, Object>> level3 = List.of(createWhileLoop(level4));
            List<Map<String, Object>> level2 = List.of(createIfStatement(level3, null));
            List<Map<String, Object>> level1 = List.of(createWhileLoop(level2));
            
            function.put("statements", level1);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertFalse(hasWarning(result, "nesting depth"));
        }
        
        @Test
        @DisplayName("Detect too many action calls")
        public void testTooManyActionCalls() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("ActionHeavyFunc");
            
            // Add 21 action calls (exceeds max of 20)
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 21; i++) {
                statements.add(createActionCall("click", "button" + i));
            }
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "ActionHeavyFunc"));
            assertTrue(hasWarning(result, "21 action calls"));
            assertTrue(hasWarning(result, "exceeds the recommended maximum of 20"));
        }
        
        @ParameterizedTest
        @DisplayName("Test various statement counts")
        @ValueSource(ints = {0, 1, 10, 25, 49, 50, 51, 75, 100})
        public void testVariousStatementCounts(int statementCount) {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("TestFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < statementCount; i++) {
                statements.add(createStatement("action"));
            }
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            if (statementCount > 50) {
                assertTrue(hasWarning(result, "statements"));
                assertTrue(hasWarning(result, "exceeds"));
            } else {
                assertFalse(hasWarning(result, "statements") && hasWarning(result, "exceeds"));
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling Validation")
    class ErrorHandlingValidation {
        
        @Test
        @DisplayName("Detect missing error handling for action calls")
        public void testMissingErrorHandling() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("NoErrorHandlingFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            statements.add(createActionCall("click", "button"));
            statements.add(createActionCall("type", "textField"));
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "NoErrorHandlingFunc"));
            assertTrue(hasWarning(result, "error handling"));
        }
        
        @Test
        @DisplayName("Accept function with error handling")
        public void testWithErrorHandling() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("SafeFunc");
            
            // Create action with error handling
            List<Map<String, Object>> statements = new ArrayList<>();
            Map<String, Object> actionCall = createActionCall("click", "button");
            statements.add(actionCall);
            
            // Add error handling (try-catch or if-result-check)
            Map<String, Object> errorCheck = new HashMap<>();
            errorCheck.put("statementType", "if");
            Map<String, Object> condition = new HashMap<>();
            condition.put("expressionType", "methodCall");
            condition.put("method", "isSuccess");
            errorCheck.put("condition", condition);
            errorCheck.put("thenStatements", List.of(createStatement("handleError")));
            statements.add(errorCheck);
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should not warn about missing error handling
            assertFalse(hasWarning(result, "SafeFunc") && hasWarning(result, "error handling"));
        }
        
        @Test
        @DisplayName("Accept function with no action calls")
        public void testNoActionCallsNoWarning() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("UtilityFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            statements.add(createStatement("log"));
            statements.add(createStatement("calculate"));
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertFalse(hasWarning(result, "error handling"));
        }
        
        @Test
        @DisplayName("Detect multiple action calls without error handling")
        public void testMultipleActionsNoErrorHandling() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("RiskyFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                statements.add(createActionCall("click", "button" + i));
            }
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "RiskyFunc"));
            assertTrue(hasWarning(result, "error handling"));
        }
    }
    
    @Nested
    @DisplayName("State Management Validation")
    class StateManagementValidation {
        
        @Test
        @DisplayName("Detect state modification without checks")
        public void testStateModificationWithoutChecks() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("UnsafeStateFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            Map<String, Object> stateModification = new HashMap<>();
            stateModification.put("statementType", "methodCall");
            stateModification.put("object", "stateTransitionsManagement");
            stateModification.put("method", "openState");
            stateModification.put("state", "newState");
            statements.add(stateModification);
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "UnsafeStateFunc"));
            assertTrue(hasWarning(result, "state"));
        }
        
        @Test
        @DisplayName("Accept state modification with checks")
        public void testStateModificationWithChecks() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("SafeStateFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            
            // Check state before modification
            Map<String, Object> stateCheck = new HashMap<>();
            stateCheck.put("statementType", "methodCall");
            stateCheck.put("object", "stateTransitionsManagement");
            stateCheck.put("method", "getCurrentState");
            stateCheck.put("state", "currentState");
            statements.add(stateCheck);
            
            // Modify state
            Map<String, Object> stateModification = new HashMap<>();
            stateModification.put("statementType", "methodCall");
            stateModification.put("object", "stateTransitionsManagement");
            stateModification.put("method", "openState");
            stateModification.put("state", "newState");
            statements.add(stateModification);
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should not warn about state management when checks are present
            boolean hasStateWarning = result.getWarnings().stream()
                .anyMatch(w -> w.message().contains("SafeStateFunc") && w.message().contains("state"));
            assertFalse(hasStateWarning, "Should not warn about state management when checks are present");
        }
        
        @Test
        @DisplayName("Accept function without state modifications")
        public void testNoStateModifications() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("StatelessFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            statements.add(createActionCall("click", "button"));
            statements.add(createStatement("log"));
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertFalse(hasWarning(result, "state"));
        }
        
        @Test
        @DisplayName("Detect multiple state changes without verification")
        public void testMultipleStateChangesNoVerification() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("ChaoticStateFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Map<String, Object> stateModification = new HashMap<>();
                stateModification.put("statementType", "methodCall");
                stateModification.put("object", "stateTransitionsManagement");
                stateModification.put("method", "openState");
                stateModification.put("state", "state" + i);
                statements.add(stateModification);
            }
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "ChaoticStateFunc"));
            assertTrue(hasWarning(result, "state"));
        }
    }
    
    @Nested
    @DisplayName("Performance Validation")
    class PerformanceValidation {
        
        @Test
        @DisplayName("Detect repeated image searches")
        public void testRepeatedImageSearches() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("InefficientSearchFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            // Search for the same image multiple times
            for (int i = 0; i < 3; i++) {
                Map<String, Object> search = new HashMap<>();
                search.put("statementType", "methodCall");
                search.put("object", "action");
                search.put("method", "find");
                List<Map<String, Object>> args = new ArrayList<>();
                Map<String, Object> arg = new HashMap<>();
                arg.put("value", "sameImage.png");
                args.add(arg);
                search.put("arguments", args);
                statements.add(search);
            }
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "InefficientSearchFunc"));
            assertTrue(hasWarning(result, "repeated") || hasWarning(result, "performance"));
        }
        
        @Test
        @DisplayName("Detect heavy operations")
        public void testHeavyOperations() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("HeavyOperationFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            
            // Add heavy operations
            Map<String, Object> findAll = new HashMap<>();
            findAll.put("statementType", "methodCall");
            findAll.put("object", "action");
            findAll.put("method", "findAll");
            statements.add(findAll);
            
            Map<String, Object> findColor = new HashMap<>();
            findColor.put("statementType", "methodCall");
            findColor.put("object", "action");
            findColor.put("method", "findColor");
            statements.add(findColor);
            
            Map<String, Object> ocrSearch = new HashMap<>();
            ocrSearch.put("statementType", "methodCall");
            ocrSearch.put("object", "action");
            ocrSearch.put("method", "findText");
            statements.add(ocrSearch);
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "HeavyOperationFunc"));
            assertTrue(hasWarning(result, "expensive") || hasWarning(result, "computationally"));
        }
        
        @Test
        @DisplayName("Detect unnecessary waits")
        public void testUnnecessaryWaits() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("WaitHeavyFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            
            // Add fixed waits
            for (int i = 0; i < 3; i++) {
                Map<String, Object> wait = new HashMap<>();
                wait.put("statementType", "wait");
                wait.put("duration", 5000);
                statements.add(wait);
            }
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(hasWarning(result, "WaitHeavyFunc"));
            assertTrue(hasWarning(result, "wait") || hasWarning(result, "performance"));
        }
        
        @Test
        @DisplayName("Accept optimized function")
        public void testOptimizedFunction() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("OptimizedFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            
            // Search once and cache result
            statements.add(createActionCall("find", "button"));
            
            // Use conditional wait instead of fixed
            Map<String, Object> conditionalWait = new HashMap<>();
            conditionalWait.put("type", "waitFor");
            conditionalWait.put("target", "element");
            statements.add(conditionalWait);
            
            // Targeted action
            statements.add(createActionCall("click", "cachedResult"));
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should not have performance warnings
            assertFalse(hasWarning(result, "performance"));
            assertFalse(hasWarning(result, "repeated"));
            assertFalse(hasWarning(result, "heavy"));
        }
        
        @Test
        @DisplayName("Detect multiple performance issues")
        public void testMultiplePerformanceIssues() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("SlowFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            
            // Repeated searches
            statements.add(createActionCall("find", "image1.png"));
            statements.add(createActionCall("find", "image1.png"));
            
            // Heavy operation
            statements.add(createActionCall("findAll", "pattern"));
            
            // Fixed wait
            Map<String, Object> wait = new HashMap<>();
            wait.put("type", "wait");
            wait.put("duration", 10000);
            statements.add(wait);
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should have multiple performance warnings
            assertTrue(hasWarning(result, "SlowFunc"));
            int warningCount = result.getWarnings().size();
            assertTrue(warningCount >= 2, "Expected multiple performance warnings");
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Handle malformed function structure")
        public void testMalformedFunctionStructure() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = new HashMap<>();
            function.put("name", "BadFunc");
            function.put("statements", "not a list"); // Wrong type
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should handle gracefully
            assertTrue(hasError(result, "Error validating"));
        }
        
        @Test
        @DisplayName("Handle null statements in list")
        public void testNullStatements() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("NullStmtFunc");
            
            List<Map<String, Object>> statements = new ArrayList<>();
            statements.add(createStatement("action"));
            statements.add(null); // Null statement
            statements.add(createStatement("log"));
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should handle null statements gracefully
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle deeply nested complex structure")
        public void testDeeplyNestedComplexStructure() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("ComplexFunc");
            
            // Create a very complex nested structure
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                List<Map<String, Object>> innerBlock = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    innerBlock.add(createActionCall("action" + j, "target" + j));
                }
                statements.add(createIfStatement(innerBlock, innerBlock));
            }
            
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should complete validation without errors
            assertNotNull(result);
            // Should detect complexity issues
            assertTrue(hasWarning(result, "ComplexFunc"));
        }
        
        @Test
        @DisplayName("Handle empty function list")
        public void testEmptyFunctionList() {
            Map<String, Object> dsl = new HashMap<>();
            dsl.put("automationFunctions", new ArrayList<>());
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
            assertTrue(result.getWarnings().isEmpty());
        }
        
        @Test
        @DisplayName("Handle function with empty statement list")
        public void testFunctionWithEmptyStatementList() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("EmptyFunc");
            function.put("statements", new ArrayList<>());
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(result.isValid());
        }
    }
    
    @Nested
    @DisplayName("Multiple Functions Validation")
    class MultipleFunctionsValidation {
        
        @Test
        @DisplayName("Validate multiple functions independently")
        public void testMultipleFunctions() {
            Map<String, Object> dsl = createDSLModel();
            
            // Add a compliant function
            Map<String, Object> goodFunc = createFunction("GoodFunc");
            goodFunc.put("statements", List.of(createStatement("log")));
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(goodFunc);
            
            // Add a function with issues
            Map<String, Object> badFunc = createFunction("BadFunc");
            List<Map<String, Object>> badStatements = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                badStatements.add(createStatement("action"));
            }
            badFunc.put("statements", badStatements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(badFunc);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should have warning for BadFunc only
            assertTrue(hasWarning(result, "BadFunc"));
            assertFalse(hasWarning(result, "GoodFunc"));
        }
        
        @Test
        @DisplayName("Accumulate issues from all functions")
        public void testAccumulateIssues() {
            Map<String, Object> dsl = createDSLModel();
            
            // Function with complexity issue
            Map<String, Object> complexFunc = createFunction("ComplexFunc");
            List<Map<String, Object>> complexStatements = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                complexStatements.add(createStatement("action"));
            }
            complexFunc.put("statements", complexStatements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(complexFunc);
            
            // Function with error handling issue
            Map<String, Object> noErrorFunc = createFunction("NoErrorFunc");
            noErrorFunc.put("statements", List.of(createActionCall("click", "button")));
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(noErrorFunc);
            
            // Function with performance issue
            Map<String, Object> slowFunc = createFunction("SlowFunc");
            List<Map<String, Object>> slowStatements = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                slowStatements.add(createActionCall("find", "sameImage"));
            }
            slowFunc.put("statements", slowStatements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(slowFunc);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            // Should have warnings for all three functions
            assertTrue(hasWarning(result, "ComplexFunc"));
            assertTrue(hasWarning(result, "NoErrorFunc"));
            assertTrue(hasWarning(result, "SlowFunc"));
            assertTrue(result.getWarnings().size() >= 3);
        }
    }
    
    @Nested
    @DisplayName("Integration with ValidationResult")
    class ValidationResultIntegration {
        
        @Test
        @DisplayName("Properly categorize severity levels")
        public void testSeverityLevels() {
            // Test null model (CRITICAL)
            ValidationResult criticalResult = validator.validateFunctionRules(null);
            assertEquals(1, criticalResult.getErrors().stream()
                .filter(e -> e.severity() == ValidationSeverity.CRITICAL)
                .count());
            
            // Test complexity warning (WARNING)
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("ComplexFunc");
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                statements.add(createStatement("action"));
            }
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult warningResult = validator.validateFunctionRules(dsl);
            assertTrue(warningResult.getWarnings().stream()
                .anyMatch(e -> e.severity() == ValidationSeverity.WARNING));
        }
        
        @Test
        @DisplayName("Validation result affects isValid()")
        public void testIsValidFlag() {
            // Valid DSL should return valid
            Map<String, Object> validDsl = createDSLModel();
            Map<String, Object> validFunc = createFunction("ValidFunc");
            validFunc.put("statements", List.of(createStatement("log")));
            ((List<Map<String, Object>>)validDsl.get("automationFunctions")).add(validFunc);
            
            ValidationResult validResult = validator.validateFunctionRules(validDsl);
            assertTrue(validResult.isValid());
            
            // Invalid DSL should return invalid
            ValidationResult invalidResult = validator.validateFunctionRules(null);
            assertFalse(invalidResult.isValid());
        }
        
        @Test
        @DisplayName("Warning doesn't invalidate result")
        public void testWarningsDoNotInvalidate() {
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("WarningFunc");
            
            // Add something that triggers a warning but not an error
            List<Map<String, Object>> statements = new ArrayList<>();
            for (int i = 0; i < 51; i++) {
                statements.add(createStatement("action"));
            }
            function.put("statements", statements);
            ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
            
            ValidationResult result = validator.validateFunctionRules(dsl);
            
            assertTrue(result.isValid()); // Still valid despite warnings
            assertFalse(result.getWarnings().isEmpty()); // Has warnings
            assertTrue(result.getErrorsAndCritical().isEmpty()); // No errors or critical errors
        }
    }
}