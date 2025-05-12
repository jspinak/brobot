package io.github.jspinak.brobot.json.schemaValidation.business;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransitionRuleValidatorTest {

    private TransitionRuleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransitionRuleValidator();
    }

    @Test
    void validateTransitionRules_withNullModel_shouldReturnCriticalError() {
        // Act
        ValidationResult result = validator.validateTransitionRules(null);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());
        assertEquals("Invalid project model", result.getErrors().get(0).errorCode());
    }

    @Test
    void validateTransitionRules_withInvalidModelType_shouldReturnCriticalError() {
        // Arrange
        String invalidModel = "Not a Map";

        // Act
        ValidationResult result = validator.validateTransitionRules(invalidModel);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());
        assertEquals("Invalid project model type", result.getErrors().get(0).errorCode());
    }

    @Test
    void validateTransitionRules_withEmptyModel_shouldNotReturnErrors() {
        // Arrange
        Map<String, Object> emptyModel = new HashMap<>();

        // Act
        ValidationResult result = validator.validateTransitionRules(emptyModel);

        // Assert
        assertFalse(result.hasErrors());
        assertTrue(result.isValid());
    }

    @Test
    void validateTransitionRules_withCycles_shouldDetectProblematicCycles() {
        // Arrange
        Map<String, Object> model = createModelWithCycle();

        // Act
        ValidationResult result = validator.validateTransitionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Potentially problematic transition cycle")));
    }

    @Test
    void validateTransitionRules_withUnreachableStates_shouldDetectUnreachableStates() {
        // Arrange
        Map<String, Object> model = createModelWithUnreachableState();

        // Act
        ValidationResult result = validator.validateTransitionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Unreachable state")));
    }

    @Test
    void validateTransitionRules_withRedundantTransitions_shouldDetectRedundancy() {
        // Arrange
        Map<String, Object> model = createModelWithRedundantTransitions();

        // Act
        ValidationResult result = validator.validateTransitionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Redundant transitions")));
    }

    @Test
    void validateTransitionRules_withManyConcurrentTransitions_shouldDetectConcurrencyIssues() {
        // Arrange
        Map<String, Object> model = createModelWithConcurrencyIssues();

        // Act
        ValidationResult result = validator.validateTransitionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Potential concurrency issue")));
    }

    @Test
    void validateTransitionRules_withMultipleIssues_shouldDetectAllIssues() {
        // Arrange
        Map<String, Object> model = createModelWithMultipleIssues();

        // Act
        ValidationResult result = validator.validateTransitionRules(model);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Potentially problematic transition cycle")));
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Unreachable state")));
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Redundant transitions")));
    }

    @Test
    void validateTransitionRules_withException_shouldHandleGracefully() {
        // Arrange
        Map<String, Object> model = new HashMap<>();
        model.put("states", "not a list"); // Malformed "states"
        model.put("stateTransitions", new ArrayList<>()); // Add "stateTransitions" so the methods don't return early

        // Act
        ValidationResult result = validator.validateTransitionRules(model);

        // Debugging output
        if (result.isValid()) { // Assuming isValid() means no errors
            System.out.println("Test validateTransitionRules_withException_shouldHandleGracefully: No errors found. Result is valid.");
            System.out.println("Errors list: " + result.getErrors());
        } else {
            System.out.println("Test validateTransitionRules_withException_shouldHandleGracefully: Errors found:");
            result.getErrors().forEach(error -> System.out.println("  - " + error));
        }

        // Assert
        assertFalse(result.isValid(), "Result should be invalid as an error should have been caught."); // If hasErrors() is not on ValidationResult
        // assertTrue(result.hasErrors()); // If hasErrors() is the correct method on ValidationResult

        assertTrue(result.getErrors().stream()
                        .anyMatch(e -> "Validation error".equals(e.errorCode()) &&
                                e.severity() == ValidationSeverity.ERROR),
                "Should contain a 'Validation error' with ERROR severity.");
    }

    // Helper methods

    private Map<String, Object> createModelWithCycle() {
        Map<String, Object> model = new HashMap<>();

        // States
        List<Map<String, Object>> states = new ArrayList<>();
        states.add(createState(1, "State 1"));
        states.add(createState(2, "State 2"));
        model.put("states", states);

        // Transitions that form a two-state cycle
        List<Map<String, Object>> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 1, List.of(2)));
        transitions.add(createTransition(2, 2, List.of(1)));
        model.put("stateTransitions", transitions);

        return model;
    }

    private Map<String, Object> createModelWithUnreachableState() {
        Map<String, Object> model = new HashMap<>();

        // States
        List<Map<String, Object>> states = new ArrayList<>();
        states.add(createState(1, "State 1"));
        states.add(createState(2, "State 2"));
        states.add(createState(3, "Unreachable State"));
        model.put("states", states);

        // Transitions - no transition to state 3
        List<Map<String, Object>> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 1, List.of(2)));
        transitions.add(createTransition(2, 2, List.of(1)));
        model.put("stateTransitions", transitions);

        return model;
    }

    private Map<String, Object> createModelWithRedundantTransitions() {
        Map<String, Object> model = new HashMap<>();

        // States
        List<Map<String, Object>> states = new ArrayList<>();
        states.add(createState(1, "State 1"));
        states.add(createState(2, "State 2"));
        model.put("states", states);

        // Multiple transitions between same states
        List<Map<String, Object>> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 1, List.of(2)));
        transitions.add(createTransition(2, 2, List.of(1)));
        transitions.add(createTransition(3, 1, List.of(2))); // Redundant
        model.put("stateTransitions", transitions);

        return model;
    }

    private Map<String, Object> createModelWithConcurrencyIssues() {
        Map<String, Object> model = new HashMap<>();

        // States
        List<Map<String, Object>> states = new ArrayList<>();
        states.add(createState(1, "Central State"));
        states.add(createState(2, "State 2"));
        states.add(createState(3, "State 3"));
        states.add(createState(4, "State 4"));
        states.add(createState(5, "State 5"));
        model.put("states", states);

        // Many transitions targeting the same state
        List<Map<String, Object>> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 2, List.of(1)));
        transitions.add(createTransition(2, 3, List.of(1)));
        transitions.add(createTransition(3, 4, List.of(1)));
        transitions.add(createTransition(4, 5, List.of(1)));
        model.put("stateTransitions", transitions);

        return model;
    }

    private Map<String, Object> createModelWithMultipleIssues() {
        Map<String, Object> model = new HashMap<>();

        // States
        List<Map<String, Object>> states = new ArrayList<>();
        states.add(createState(1, "State 1"));
        states.add(createState(2, "State 2"));
        states.add(createState(3, "Unreachable State"));
        model.put("states", states);

        // Transitions with multiple issues
        List<Map<String, Object>> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 1, List.of(2))); // Part of cycle
        transitions.add(createTransition(2, 2, List.of(1))); // Part of cycle
        transitions.add(createTransition(3, 1, List.of(2))); // Redundant
        model.put("stateTransitions", transitions);

        return model;
    }

    private Map<String, Object> createState(int id, String name) {
        Map<String, Object> state = new HashMap<>();
        state.put("id", id);
        state.put("name", name);
        return state;
    }

    private Map<String, Object> createTransition(int id, int sourceId, List<Integer> targetIds) {
        Map<String, Object> transition = new HashMap<>();
        transition.put("id", id);
        transition.put("sourceStateId", sourceId);
        transition.put("statesToEnter", targetIds);
        return transition;
    }
}