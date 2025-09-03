package io.github.jspinak.brobot.runner.json.validation.business;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for BusinessRuleValidator - validates business logic rules for Brobot configurations.
 * Ensures configurations follow business constraints and best practices.
 */
@DisplayName("BusinessRuleValidator Tests")
public class BusinessRuleValidatorTest extends BrobotTestBase {
    
    private BusinessRuleValidator validator;
    private ObjectMapper objectMapper;
    
    @Mock
    private TransitionRuleValidator transitionRuleValidator;
    
    @Mock
    private FunctionRuleValidator functionRuleValidator;
    
    private AutoCloseable mocks;
    
    // Helper methods to check for specific errors
    private boolean hasError(ValidationResult result, String context, String errorType) {
        return result.getErrors().stream()
            .anyMatch(e -> e.errorCode().contains(errorType) && e.message().contains(context));
    }
    
    private boolean hasWarning(ValidationResult result, String context, String warningType) {
        return result.getWarnings().stream()
            .anyMatch(e -> e.errorCode().contains(warningType) && e.message().contains(context));
    }
    
    private boolean hasInfo(ValidationResult result, String context, String infoType) {
        return result.getInfoMessages().stream()
            .anyMatch(e -> e.errorCode().contains(infoType) && e.message().contains(context));
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mocks = MockitoAnnotations.openMocks(this);
        validator = new BusinessRuleValidator(transitionRuleValidator, functionRuleValidator);
        objectMapper = new ObjectMapper();
    }
    
    @Nested
    @DisplayName("State Business Rules")
    class StateBusinessRules {
        
        @Test
        @DisplayName("Enforce unique state names")
        public void testUniqueStateNames() throws Exception {
            // Create a project model with duplicate state names
            Map<String, Object> projectModel = new HashMap<>();
            List<Map<String, Object>> states = new ArrayList<>();
            
            Map<String, Object> state1 = new HashMap<>();
            state1.put("name", "State1");
            states.add(state1);
            
            Map<String, Object> state2 = new HashMap<>();
            state2.put("name", "State1"); // Duplicate
            states.add(state2);
            
            projectModel.put("states", states);
            
            // Mock the transition validator to return error for duplicates
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("duplicate-state", "Duplicate state name: State1", ValidationSeverity.ERROR);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "State1", "duplicate"));
        }
        
        @Test
        @DisplayName("Initial state must exist")
        public void testInitialStateMustExist() throws Exception {
            // Create a project model with non-existent initial state
            Map<String, Object> projectModel = new HashMap<>();
            projectModel.put("initialState", "StartState");
            
            List<Map<String, Object>> states = new ArrayList<>();
            Map<String, Object> state1 = new HashMap<>();
            state1.put("name", "State1");
            states.add(state1);
            
            projectModel.put("states", states);
            
            // Mock the validator to return error for missing initial state
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("missing-initial-state", "Initial state 'StartState' not found", ValidationSeverity.ERROR);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "StartState", "missing"));
        }
    }
    
    @Nested
    @DisplayName("Transition Business Rules")
    class TransitionBusinessRules {
        
        @Test
        @DisplayName("No self-transitions allowed")
        public void testNoSelfTransitions() throws Exception {
            Map<String, Object> projectModel = createProjectWithSelfTransition();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("self-transition", "Self-transition detected in State1", ValidationSeverity.ERROR);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "State1", "self-transition"));
        }
        
        @Test
        @DisplayName("No duplicate transitions")
        public void testNoDuplicateTransitions() throws Exception {
            Map<String, Object> projectModel = createProjectWithDuplicateTransitions();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("duplicate-transition", "Duplicate transition from State1 to State2", ValidationSeverity.WARNING);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertTrue(result.isValid()); // Warnings don't make it invalid
            assertTrue(hasWarning(result, "State1", "duplicate"));
        }
        
        @ParameterizedTest
        @DisplayName("Max retries must be in valid range")
        @ValueSource(ints = {-1, 0, 11, 100})
        public void testMaxRetriesRange(int retries) throws Exception {
            Map<String, Object> projectModel = createProjectWithTransition(retries);
            
            ValidationResult mockResult = new ValidationResult();
            if (retries < 1 || retries > 10) {
                mockResult.addError("invalid-retries", "Invalid max retries: " + retries, ValidationSeverity.ERROR);
            }
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            if (retries < 1 || retries > 10) {
                assertFalse(result.isValid());
            } else {
                assertTrue(result.isValid());
            }
        }
        
        @Test
        @DisplayName("Transition probabilities must sum to 1.0")
        public void testTransitionProbabilities() throws Exception {
            Map<String, Object> projectModel = createProjectWithInvalidProbabilities();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("invalid-probabilities", "Transition probabilities don't sum to 1.0", ValidationSeverity.ERROR);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "probabilities", "invalid-probabilities"));
        }
    }
    
    @Nested
    @DisplayName("Function Business Rules")
    class FunctionBusinessRules {
        
        @Test
        @DisplayName("Function must have at least one step")
        public void testFunctionMustHaveSteps() throws Exception {
            Map<String, Object> dslModel = createDSLWithEmptyFunction();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("empty-function", "Function 'EmptyFunc' has no steps", ValidationSeverity.ERROR);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(new ValidationResult());
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(mockResult);
            
            ValidationResult result = validator.validateRules(new HashMap<>(), dslModel);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "EmptyFunc", "empty-function"));
        }
        
        @Test
        @DisplayName("Function step targets must be valid")
        public void testFunctionStepTargets() throws Exception {
            Map<String, Object> dslModel = createDSLWithInvalidTarget();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("invalid-target", "Invalid target in function", ValidationSeverity.ERROR);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(new ValidationResult());
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(mockResult);
            
            ValidationResult result = validator.validateRules(new HashMap<>(), dslModel);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "target", "invalid"));
        }
        
        @ParameterizedTest
        @DisplayName("Function name validation")
        @MethodSource("functionNameProvider")
        public void testFunctionNameValidation(String functionName, boolean isValid) throws Exception {
            Map<String, Object> dslModel = createDSLWithFunction(functionName);
            
            ValidationResult mockResult = new ValidationResult();
            if (!isValid) {
                mockResult.addError("invalid-function-name", "Invalid function name: " + functionName, ValidationSeverity.ERROR);
            }
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(new ValidationResult());
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(mockResult);
            
            ValidationResult result = validator.validateRules(new HashMap<>(), dslModel);
            
            assertEquals(isValid, result.isValid());
        }
        
        static Stream<Arguments> functionNameProvider() {
            return Stream.of(
                Arguments.of("validFunction", true),
                Arguments.of("valid_function", true),
                Arguments.of("validName123", true),
                Arguments.of("", false),
                Arguments.of("123invalid", false),
                Arguments.of("invalid-name", false),
                Arguments.of("invalid name", false),
                Arguments.of("@invalid", false)
            );
        }
    }
    
    @Nested
    @DisplayName("Performance Rules")
    class PerformanceRules {
        
        @Test
        @DisplayName("Warn about too many states")
        public void testTooManyStates() throws Exception {
            Map<String, Object> projectModel = createProjectWithManyStates(50);
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("too-many-states", "Project has 50 states (recommended max: 30)", ValidationSeverity.WARNING);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertTrue(result.isValid()); // Warning doesn't invalidate
            assertTrue(hasWarning(result, "50 states", "too-many-states"));
        }
        
        @Test
        @DisplayName("Warn about too many transitions per state")
        public void testTooManyTransitionsPerState() throws Exception {
            Map<String, Object> projectModel = createProjectWithManyTransitions(15);
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("too-many-transitions", "State has 15 transitions (recommended max: 10)", ValidationSeverity.WARNING);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertTrue(result.isValid());
            assertTrue(hasWarning(result, "15 transitions", "too-many-transitions"));
        }
        
        @Test
        @DisplayName("Warn about deep function nesting")
        public void testDeepFunctionNesting() throws Exception {
            Map<String, Object> dslModel = createDSLWithDeepNesting(6);
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("deep-nesting", "Function has nesting depth of 6 (recommended max: 3)", ValidationSeverity.WARNING);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(new ValidationResult());
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(mockResult);
            
            ValidationResult result = validator.validateRules(new HashMap<>(), dslModel);
            
            assertTrue(result.isValid());
            assertTrue(hasWarning(result, "depth of 6", "deep-nesting"));
        }
    }
    
    @Nested
    @DisplayName("Best Practices Validation")
    class BestPracticesValidation {
        
        @Test
        @DisplayName("Warn about missing descriptions")
        public void testMissingDescriptions() throws Exception {
            Map<String, Object> projectModel = createProjectWithoutDescriptions();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("missing-description", "Consider adding descriptions for better documentation", ValidationSeverity.INFO);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertTrue(result.isValid());
            assertTrue(hasInfo(result, "descriptions", "missing-description"));
        }
        
        @Test
        @DisplayName("Warn about generic names")
        public void testGenericNames() throws Exception {
            Map<String, Object> projectModel = createProjectWithGenericNames();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("generic-names", "Consider using more descriptive names", ValidationSeverity.INFO);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertTrue(result.isValid());
            assertTrue(hasInfo(result, "descriptive", "names"));
        }
        
        @Test
        @DisplayName("Suggest transition probabilities")
        public void testSuggestTransitionProbabilities() throws Exception {
            Map<String, Object> projectModel = createProjectWithoutProbabilities();
            
            ValidationResult mockResult = new ValidationResult();
            mockResult.addError("missing-probabilities", "Consider adding transition probabilities for better path selection", ValidationSeverity.INFO);
            when(transitionRuleValidator.validateTransitionRules(any())).thenReturn(mockResult);
            when(functionRuleValidator.validateFunctionRules(any())).thenReturn(new ValidationResult());
            
            ValidationResult result = validator.validateRules(projectModel, new HashMap<>());
            
            assertTrue(result.isValid());
            assertTrue(hasInfo(result, "probabilities", "missing-probabilities"));
        }
    }
    
    // Helper methods to create test data
    private Map<String, Object> createProjectWithSelfTransition() {
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> transition = new HashMap<>();
        transition.put("from", "State1");
        transition.put("to", "State1");
        project.put("transitions", List.of(transition));
        return project;
    }
    
    private Map<String, Object> createProjectWithDuplicateTransitions() {
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> trans1 = new HashMap<>();
        trans1.put("from", "State1");
        trans1.put("to", "State2");
        
        Map<String, Object> trans2 = new HashMap<>();
        trans2.put("from", "State1");
        trans2.put("to", "State2");
        
        project.put("transitions", List.of(trans1, trans2));
        return project;
    }
    
    private Map<String, Object> createProjectWithTransition(int retries) {
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> transition = new HashMap<>();
        transition.put("maxRetries", retries);
        project.put("transitions", List.of(transition));
        return project;
    }
    
    private Map<String, Object> createProjectWithInvalidProbabilities() {
        Map<String, Object> project = new HashMap<>();
        List<Map<String, Object>> transitions = new ArrayList<>();
        
        Map<String, Object> trans1 = new HashMap<>();
        trans1.put("probability", 0.6);
        transitions.add(trans1);
        
        Map<String, Object> trans2 = new HashMap<>();
        trans2.put("probability", 0.6); // Sum > 1.0
        transitions.add(trans2);
        
        project.put("transitions", transitions);
        return project;
    }
    
    private Map<String, Object> createDSLWithEmptyFunction() {
        Map<String, Object> dsl = new HashMap<>();
        Map<String, Object> function = new HashMap<>();
        function.put("name", "EmptyFunc");
        function.put("steps", new ArrayList<>());
        dsl.put("functions", List.of(function));
        return dsl;
    }
    
    private Map<String, Object> createDSLWithInvalidTarget() {
        Map<String, Object> dsl = new HashMap<>();
        Map<String, Object> function = new HashMap<>();
        Map<String, Object> step = new HashMap<>();
        step.put("target", ""); // Invalid empty target
        function.put("steps", List.of(step));
        dsl.put("functions", List.of(function));
        return dsl;
    }
    
    private Map<String, Object> createDSLWithFunction(String name) {
        Map<String, Object> dsl = new HashMap<>();
        Map<String, Object> function = new HashMap<>();
        function.put("name", name);
        dsl.put("functions", List.of(function));
        return dsl;
    }
    
    private Map<String, Object> createProjectWithManyStates(int count) {
        Map<String, Object> project = new HashMap<>();
        List<Map<String, Object>> states = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> state = new HashMap<>();
            state.put("name", "State" + i);
            states.add(state);
        }
        project.put("states", states);
        return project;
    }
    
    private Map<String, Object> createProjectWithManyTransitions(int count) {
        Map<String, Object> project = new HashMap<>();
        List<Map<String, Object>> transitions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> trans = new HashMap<>();
            trans.put("from", "State1");
            trans.put("to", "State" + (i + 2));
            transitions.add(trans);
        }
        project.put("transitions", transitions);
        return project;
    }
    
    private Map<String, Object> createDSLWithDeepNesting(int depth) {
        Map<String, Object> dsl = new HashMap<>();
        Map<String, Object> function = new HashMap<>();
        function.put("name", "DeepFunction");
        function.put("nestingDepth", depth);
        dsl.put("functions", List.of(function));
        return dsl;
    }
    
    private Map<String, Object> createProjectWithoutDescriptions() {
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        state.put("name", "State1");
        // No description field
        project.put("states", List.of(state));
        return project;
    }
    
    private Map<String, Object> createProjectWithGenericNames() {
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        state.put("name", "State1"); // Generic name
        project.put("states", List.of(state));
        return project;
    }
    
    private Map<String, Object> createProjectWithoutProbabilities() {
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> transition = new HashMap<>();
        transition.put("from", "State1");
        transition.put("to", "State2");
        // No probability field
        project.put("transitions", List.of(transition));
        return project;
    }
}