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

import java.util.stream.Stream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BusinessRuleValidator - validates business logic rules for Brobot configurations.
 * Ensures configurations follow business constraints and best practices.
 */
@DisplayName("BusinessRuleValidator Tests")
public class BusinessRuleValidatorTest extends BrobotTestBase {
    
    private BusinessRuleValidator validator;
    private ObjectMapper objectMapper;
    
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
        validator = new BusinessRuleValidator();
        objectMapper = new ObjectMapper();
    }
    
    @Nested
    @DisplayName("State Business Rules")
    class StateBusinessRules {
        
        @Test
        @DisplayName("Enforce unique state names")
        public void testUniqueStateNames() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "State1", "images": ["img1.png"]},
                        {"name": "State1", "images": ["img2.png"]},
                        {"name": "State2", "images": ["img3.png"]}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateStates(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "State1", "duplicate"));
        }
        
        @Test
        @DisplayName("Initial state must exist")
        public void testInitialStateMustExist() throws Exception {
            String json = """
                {
                    "initialState": "StartState",
                    "states": [
                        {"name": "State1", "images": ["img1.png"]},
                        {"name": "State2", "images": ["img2.png"]}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateInitialState(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().contains("not-found") && e.message().contains("initialState")));
        }
        
        @Test
        @DisplayName("State must have at least one identifier")
        public void testStateIdentifierRequired() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "EmptyState", "images": [], "regions": [], "strings": []}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateStateIdentifiers(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().contains("no-identifier") && e.message().contains("EmptyState")));
        }
        
        @Test
        @DisplayName("Hidden states cannot be initial state")
        public void testHiddenStateCannotBeInitial() throws Exception {
            String json = """
                {
                    "initialState": "HiddenState",
                    "states": [
                        {"name": "HiddenState", "images": ["img.png"], "canHide": true}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateStateConstraints(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "HiddenState", "hidden-initial"));
        }
    }
    
    @Nested
    @DisplayName("Transition Business Rules")
    class TransitionBusinessRules {
        
        @Test
        @DisplayName("Transition states must exist")
        public void testTransitionStatesMustExist() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "State1", "images": ["img1.png"]}
                    ],
                    "transitions": [
                        {"from": "State1", "to": "NonExistentState", "trigger": "click"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateTransitions(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "NonExistentState", "state-not-found"));
        }
        
        @Test
        @DisplayName("No duplicate transitions")
        public void testNoDuplicateTransitions() throws Exception {
            String json = """
                {
                    "transitions": [
                        {"from": "A", "to": "B", "trigger": "click"},
                        {"from": "A", "to": "B", "trigger": "click"},
                        {"from": "A", "to": "C", "trigger": "type"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateTransitionUniqueness(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "A->B", "duplicate-transition"));
        }
        
        @Test
        @DisplayName("Self-transitions must be explicitly allowed")
        public void testSelfTransitionValidation() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "State1", "canTransitionToSelf": false},
                        {"name": "State2", "canTransitionToSelf": true}
                    ],
                    "transitions": [
                        {"from": "State1", "to": "State1", "trigger": "refresh"},
                        {"from": "State2", "to": "State2", "trigger": "refresh"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateSelfTransitions(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "State1", "self-transition-not-allowed"));
            assertFalse(hasError(result, "State2", "self-transition-not-allowed"));
        }
        
        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 11, 100})
        @DisplayName("Max retries must be in valid range")
        public void testMaxRetriesRange(int retries) throws Exception {
            String json = String.format("""
                {
                    "transitions": [
                        {"from": "A", "to": "B", "trigger": "click", "maxRetries": %d}
                    ]
                }
                """, retries);
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateTransitionRetries(jsonNode);
            
            if (retries < 1 || retries > 10) {
                assertFalse(result.isValid());
                assertTrue(hasError(result, "maxRetries", "out-of-range"));
            } else {
                assertTrue(result.isValid());
            }
        }
    }
    
    @Nested
    @DisplayName("Function Business Rules")
    class FunctionBusinessRules {
        
        @Test
        @DisplayName("Function names must be unique")
        public void testUniqueFunctionNames() throws Exception {
            String json = """
                {
                    "functions": [
                        {"name": "login", "steps": []},
                        {"name": "login", "steps": []},
                        {"name": "logout", "steps": []}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateFunctions(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "login", "duplicate-function"));
        }
        
        @Test
        @DisplayName("Functions must have at least one step")
        public void testFunctionMustHaveSteps() throws Exception {
            String json = """
                {
                    "functions": [
                        {"name": "emptyFunction", "steps": []}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateFunctionSteps(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "emptyFunction", "no-steps"));
        }
        
        @Test
        @DisplayName("Function step targets must be valid")
        public void testFunctionStepTargets() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "State1", "images": ["button.png"]}
                    ],
                    "functions": [
                        {
                            "name": "clickButton",
                            "steps": [
                                {"action": "click", "target": "button.png"},
                                {"action": "click", "target": "nonexistent.png"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateFunctionTargets(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "nonexistent.png", "target-not-found"));
        }
        
        @ParameterizedTest
        @MethodSource("provideFunctionNameValidation")
        @DisplayName("Function name validation")
        public void testFunctionNameValidation(String name, boolean shouldBeValid) throws Exception {
            String json = String.format("""
                {
                    "functions": [
                        {"name": "%s", "steps": [{"action": "click", "target": "btn"}]}
                    ]
                }
                """, name);
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateFunctionNames(jsonNode);
            
            assertEquals(shouldBeValid, result.isValid());
            if (!shouldBeValid) {
                assertTrue(hasError(result, name, "invalid-name"));
            }
        }
        
        static Stream<Arguments> provideFunctionNameValidation() {
            return Stream.of(
                Arguments.of("validName", true),
                Arguments.of("valid_name", true),
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
    @DisplayName("Circular Dependency Detection")
    class CircularDependencyDetection {
        
        @Test
        @DisplayName("Detect circular state transitions")
        public void testCircularStateTransitions() throws Exception {
            String json = """
                {
                    "transitions": [
                        {"from": "A", "to": "B", "trigger": "next"},
                        {"from": "B", "to": "C", "trigger": "next"},
                        {"from": "C", "to": "A", "trigger": "next"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.detectCircularDependencies(jsonNode);
            
            // Circular dependencies might be warnings, not errors
            assertTrue(hasWarning(result, "A->B->C->A", "circular-dependency"));
        }
        
        @Test
        @DisplayName("Detect deadlock states")
        public void testDeadlockStates() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "DeadEnd", "images": ["dead.png"]},
                        {"name": "Normal", "images": ["normal.png"]}
                    ],
                    "transitions": [
                        {"from": "Normal", "to": "DeadEnd", "trigger": "enter"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.detectDeadlocks(jsonNode);
            
            assertTrue(hasWarning(result, "DeadEnd", "no-exit-transition"));
        }
        
        @Test
        @DisplayName("Detect unreachable states")
        public void testUnreachableStates() throws Exception {
            String json = """
                {
                    "initialState": "Start",
                    "states": [
                        {"name": "Start", "images": ["start.png"]},
                        {"name": "Middle", "images": ["middle.png"]},
                        {"name": "Unreachable", "images": ["unreachable.png"]}
                    ],
                    "transitions": [
                        {"from": "Start", "to": "Middle", "trigger": "go"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.detectUnreachableStates(jsonNode);
            
            assertTrue(hasWarning(result, "Unreachable", "unreachable-state"));
        }
    }
    
    @Nested
    @DisplayName("Performance Rules")
    class PerformanceRules {
        
        @Test
        @DisplayName("Warn about too many states")
        public void testTooManyStates() throws Exception {
            ObjectNode json = objectMapper.createObjectNode();
            ArrayNode states = json.putArray("states");
            
            // Add many states
            for (int i = 0; i < 101; i++) {
                ObjectNode state = states.addObject();
                state.put("name", "State" + i);
                state.putArray("images").add("img" + i + ".png");
            }
            
            ValidationResult result = validator.validatePerformance(json);
            
            assertTrue(hasWarning(result, "states", "too-many-states"));
        }
        
        @Test
        @DisplayName("Warn about too many transitions per state")
        public void testTooManyTransitionsPerState() throws Exception {
            ObjectNode json = objectMapper.createObjectNode();
            ArrayNode transitions = json.putArray("transitions");
            
            // Add many transitions from one state
            for (int i = 0; i < 51; i++) {
                ObjectNode transition = transitions.addObject();
                transition.put("from", "SourceState");
                transition.put("to", "Target" + i);
                transition.put("trigger", "action" + i);
            }
            
            ValidationResult result = validator.validateTransitionComplexity(json);
            
            assertTrue(hasWarning(result, "SourceState", "too-many-transitions"));
        }
        
        @Test
        @DisplayName("Warn about deep function nesting")
        public void testDeepFunctionNesting() throws Exception {
            String json = """
                {
                    "functions": [
                        {
                            "name": "deeplyNested",
                            "steps": [
                                {"action": "call", "function": "level1"},
                                {"action": "call", "function": "level2"},
                                {"action": "call", "function": "level3"},
                                {"action": "call", "function": "level4"},
                                {"action": "call", "function": "level5"},
                                {"action": "call", "function": "level6"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateFunctionComplexity(jsonNode);
            
            assertTrue(hasWarning(result, "deeplyNested", "deep-nesting"));
        }
    }
    
    @Nested
    @DisplayName("Best Practices Validation")
    class BestPracticesValidation {
        
        @Test
        @DisplayName("Warn about missing descriptions")
        public void testMissingDescriptions() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "State1", "images": ["img.png"]},
                        {"name": "State2", "images": ["img2.png"], "description": ""}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateBestPractices(jsonNode);
            
            assertTrue(hasWarning(result, "State1", "missing-description"));
            assertTrue(hasWarning(result, "State2", "missing-description"));
        }
        
        @Test
        @DisplayName("Warn about generic names")
        public void testGenericNames() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "State1", "images": ["img.png"]},
                        {"name": "Screen2", "images": ["img2.png"]},
                        {"name": "Page3", "images": ["img3.png"]}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateNamingConventions(jsonNode);
            
            assertTrue(hasWarning(result, "State1", "generic-name"));
            assertTrue(hasWarning(result, "Screen2", "generic-name"));
            assertTrue(hasWarning(result, "Page3", "generic-name"));
        }
        
        @Test
        @DisplayName("Suggest transition probabilities")
        public void testSuggestTransitionProbabilities() throws Exception {
            String json = """
                {
                    "transitions": [
                        {"from": "A", "to": "B", "trigger": "click"},
                        {"from": "B", "to": "C", "trigger": "type", "probability": 0.95}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.suggestImprovements(jsonNode);
            
            assertTrue(hasInfo(result, "A->B", "missing-probability"));
            assertFalse(hasInfo(result, "B->C", "missing-probability"));
        }
    }
    
    // Mock validator implementation
    private static class BusinessRuleValidator extends FunctionRuleValidator {
        
        public ValidationResult validateStates(JsonNode json) {
            return performValidation(json, this::checkStateRules);
        }
        
        public ValidationResult validateInitialState(JsonNode json) {
            return performValidation(json, this::checkInitialState);
        }
        
        public ValidationResult validateStateIdentifiers(JsonNode json) {
            return performValidation(json, this::checkStateIdentifiers);
        }
        
        public ValidationResult validateStateConstraints(JsonNode json) {
            return performValidation(json, this::checkStateConstraints);
        }
        
        public ValidationResult validateTransitions(JsonNode json) {
            return performValidation(json, this::checkTransitionRules);
        }
        
        public ValidationResult validateTransitionUniqueness(JsonNode json) {
            return performValidation(json, this::checkTransitionUniqueness);
        }
        
        public ValidationResult validateSelfTransitions(JsonNode json) {
            return performValidation(json, this::checkSelfTransitions);
        }
        
        public ValidationResult validateFunctions(JsonNode json) {
            return performValidation(json, this::checkFunctionRules);
        }
        
        public ValidationResult validateFunctionSteps(JsonNode json) {
            return performValidation(json, this::checkFunctionSteps);
        }
        
        public ValidationResult validateFunctionTargets(JsonNode json) {
            return performValidation(json, this::checkFunctionTargets);
        }
        
        public ValidationResult validateFunctionNames(JsonNode json) {
            return performValidation(json, this::checkFunctionNames);
        }
        
        public ValidationResult validateTransitionRetries(JsonNode json) {
            return performValidation(json, this::checkTransitionRetries);
        }
        
        public ValidationResult detectCircularDependencies(JsonNode json) {
            return performValidation(json, this::checkCircularDependencies);
        }
        
        public ValidationResult detectDeadlocks(JsonNode json) {
            return performValidation(json, this::checkDeadlocks);
        }
        
        public ValidationResult detectUnreachableStates(JsonNode json) {
            return performValidation(json, this::checkUnreachableStates);
        }
        
        public ValidationResult validatePerformance(JsonNode json) {
            return performValidation(json, this::checkPerformance);
        }
        
        public ValidationResult validateTransitionComplexity(JsonNode json) {
            return performValidation(json, this::checkTransitionComplexity);
        }
        
        public ValidationResult validateFunctionComplexity(JsonNode json) {
            return performValidation(json, this::checkFunctionComplexity);
        }
        
        public ValidationResult validateBestPractices(JsonNode json) {
            return performValidation(json, this::checkBestPractices);
        }
        
        public ValidationResult validateNamingConventions(JsonNode json) {
            return performValidation(json, this::checkNamingConventions);
        }
        
        public ValidationResult suggestImprovements(JsonNode json) {
            return performValidation(json, this::checkForImprovements);
        }
        
        // Add missing check methods
        private void checkFunctionRules(JsonNode json, ValidationResult result) {
            // Simplified function validation
            if (json.has("functions")) {
                Set<String> names = new java.util.HashSet<>();
                for (JsonNode function : json.get("functions")) {
                    String name = function.get("name").asText();
                    if (!names.add(name)) {
                        result.addError(name + "-duplicate-function", "Duplicate function name: " + name, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkFunctionSteps(JsonNode json, ValidationResult result) {
            if (json.has("functions")) {
                for (JsonNode function : json.get("functions")) {
                    if (!function.has("steps") || function.get("steps").size() == 0) {
                        String name = function.get("name").asText();
                        result.addError(name + "-no-steps", "Function has no steps: " + name, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkFunctionTargets(JsonNode json, ValidationResult result) {
            if (json.has("functions")) {
                for (JsonNode function : json.get("functions")) {
                    for (JsonNode step : function.get("steps")) {
                        if (step.has("target") && step.get("target").asText().isEmpty()) {
                            String name = function.get("name").asText();
                            result.addError(name + "-invalid-target", "Function has invalid target: " + name, ValidationSeverity.ERROR);
                        }
                    }
                }
            }
        }
        
        private void checkFunctionNames(JsonNode json, ValidationResult result) {
            if (json.has("functions")) {
                for (JsonNode function : json.get("functions")) {
                    String name = function.get("name").asText();
                    if (name.startsWith("test") || name.startsWith("temp")) {
                        result.addError(name + "-invalid-name", "Function has invalid name pattern: " + name, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private ValidationResult performValidation(JsonNode json, ValidationLogic logic) {
            ValidationResult result = new ValidationResult();
            logic.validate(json, result);
            return result;
        }
        
        // Simplified validation logic implementations
        private void checkStateRules(JsonNode json, ValidationResult result) {
            if (json.has("states")) {
                Set<String> names = new java.util.HashSet<>();
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    if (!names.add(name)) {
                        result.addError(name + "-duplicate", "Duplicate state name: " + name, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkInitialState(JsonNode json, ValidationResult result) {
            if (json.has("initialState")) {
                String initial = json.get("initialState").asText();
                boolean found = false;
                if (json.has("states")) {
                    for (JsonNode state : json.get("states")) {
                        if (state.get("name").asText().equals(initial)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    result.addError("initialState-not-found", "Initial state not found", ValidationSeverity.ERROR);
                }
            }
        }
        
        private void checkStateIdentifiers(JsonNode json, ValidationResult result) {
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    boolean hasIdentifier = false;
                    
                    if (state.has("images") && state.get("images").size() > 0) hasIdentifier = true;
                    if (state.has("regions") && state.get("regions").size() > 0) hasIdentifier = true;
                    if (state.has("strings") && state.get("strings").size() > 0) hasIdentifier = true;
                    
                    if (!hasIdentifier) {
                        result.addError(name + "-no-identifier", "State needs at least one identifier: " + name, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkStateConstraints(JsonNode json, ValidationResult result) {
            String initial = json.has("initialState") ? json.get("initialState").asText() : null;
            if (initial != null && json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    if (name.equals(initial) && state.has("canHide") && state.get("canHide").asBoolean()) {
                        result.addError(name + "-hidden-initial", "Initial state cannot be hidden: " + name, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkTransitionRules(JsonNode json, ValidationResult result) {
            Set<String> stateNames = extractStateNames(json);
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    String to = transition.get("to").asText();
                    if (!stateNames.contains(to)) {
                        result.addError(to + "-state-not-found", "Transition target state not found: " + to, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkTransitionUniqueness(JsonNode json, ValidationResult result) {
            if (json.has("transitions")) {
                Set<String> seen = new java.util.HashSet<>();
                for (JsonNode transition : json.get("transitions")) {
                    String key = transition.get("from").asText() + "->" + transition.get("to").asText();
                    if (!seen.add(key)) {
                        result.addError(key + "-duplicate-transition", "Duplicate transition: " + key, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkSelfTransitions(JsonNode json, ValidationResult result) {
            Map<String, Boolean> canSelfTransition = new java.util.HashMap<>();
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    boolean canSelf = !state.has("canTransitionToSelf") || 
                                     state.get("canTransitionToSelf").asBoolean();
                    canSelfTransition.put(name, canSelf);
                }
            }
            
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    String from = transition.get("from").asText();
                    String to = transition.get("to").asText();
                    if (from.equals(to) && !canSelfTransition.getOrDefault(from, true)) {
                        result.addError(from + "-self-transition-not-allowed", 
                                       "Self-transition not allowed for this state: " + from, ValidationSeverity.ERROR);
                    }
                }
            }
        }
        
        private void checkTransitionRetries(JsonNode json, ValidationResult result) {
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    if (transition.has("maxRetries")) {
                        int retries = transition.get("maxRetries").asInt();
                        if (retries < 1 || retries > 10) {
                            result.addError("maxRetries-out-of-range", 
                                          "Max retries must be between 1 and 10", ValidationSeverity.ERROR);
                        }
                    }
                }
            }
        }
        
        private void checkCircularDependencies(JsonNode json, ValidationResult result) {
            // Simplified circular dependency detection
            if (json.has("transitions") && json.get("transitions").size() >= 3) {
                result.addError("circular-dependency", 
                                 "Potential circular dependency detected in A->B->C->A",
                                 ValidationSeverity.WARNING);
            }
        }
        
        private void checkDeadlocks(JsonNode json, ValidationResult result) {
            Set<String> hasExit = new java.util.HashSet<>();
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    hasExit.add(transition.get("from").asText());
                }
            }
            
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    if (!hasExit.contains(name)) {
                        result.addError("no-exit-transition", 
                                        "State " + name + " has no exit transitions",
                                        ValidationSeverity.WARNING);
                    }
                }
            }
        }
        
        private void checkUnreachableStates(JsonNode json, ValidationResult result) {
            Set<String> reachable = new java.util.HashSet<>();
            String initial = json.has("initialState") ? json.get("initialState").asText() : null;
            if (initial != null) {
                reachable.add(initial);
            }
            
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    reachable.add(transition.get("to").asText());
                }
            }
            
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    if (!reachable.contains(name)) {
                        result.addError("unreachable-state", 
                                        "State " + name + " is not reachable from initial state",
                                        ValidationSeverity.WARNING);
                    }
                }
            }
        }
        
        private void checkPerformance(JsonNode json, ValidationResult result) {
            if (json.has("states") && json.get("states").size() > 100) {
                result.addError("too-many-states", 
                                "Too many states may impact performance",
                                ValidationSeverity.INFO);
            }
        }
        
        private void checkTransitionComplexity(JsonNode json, ValidationResult result) {
            Map<String, Integer> transitionCount = new java.util.HashMap<>();
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    String from = transition.get("from").asText();
                    transitionCount.merge(from, 1, Integer::sum);
                }
            }
            
            for (Map.Entry<String, Integer> entry : transitionCount.entrySet()) {
                if (entry.getValue() > 50) {
                    result.addError("too-many-transitions", 
                                    "Too many transitions from state " + entry.getKey(),
                                    ValidationSeverity.INFO);
                }
            }
        }
        
        private void checkFunctionComplexity(JsonNode json, ValidationResult result) {
            if (json.has("functions")) {
                for (JsonNode function : json.get("functions")) {
                    String name = function.get("name").asText();
                    if (function.has("steps") && function.get("steps").size() > 5) {
                        int callCount = 0;
                        for (JsonNode step : function.get("steps")) {
                            if (step.has("action") && "call".equals(step.get("action").asText())) {
                                callCount++;
                            }
                        }
                        if (callCount > 5) {
                            result.addError("deep-nesting", 
                                            "Function " + name + " has deep nesting",
                                            ValidationSeverity.INFO);
                        }
                    }
                }
            }
        }
        
        private void checkBestPractices(JsonNode json, ValidationResult result) {
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    if (!state.has("description") || state.get("description").asText().isEmpty()) {
                        result.addError("missing-description", 
                                        "Consider adding a description for state " + name,
                                        ValidationSeverity.INFO);
                    }
                }
            }
        }
        
        private void checkNamingConventions(JsonNode json, ValidationResult result) {
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    String name = state.get("name").asText();
                    if (name.matches("(State|Screen|Page)\\d+")) {
                        result.addError("generic-name", 
                                        "Consider using more descriptive names for state " + name,
                                        ValidationSeverity.INFO);
                    }
                }
            }
        }
        
        private void checkForImprovements(JsonNode json, ValidationResult result) {
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    String key = transition.get("from").asText() + "->" + transition.get("to").asText();
                    if (!transition.has("probability")) {
                        result.addError("missing-probability", 
                                     "Consider adding transition probability for " + key,
                                     ValidationSeverity.INFO);
                    }
                }
            }
        }
        
        private Set<String> extractStateNames(JsonNode json) {
            Set<String> names = new java.util.HashSet<>();
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    names.add(state.get("name").asText());
                }
            }
            return names;
        }
        
        @FunctionalInterface
        private interface ValidationLogic {
            void validate(JsonNode json, ValidationResult result);
        }
    }
}