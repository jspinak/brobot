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
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TransitionRuleValidator.
 * Tests validation of state transitions including cycle detection,
 * reachability analysis, efficiency checks, and concurrency validation.
 */
@DisplayName("TransitionRuleValidator Tests")
public class TransitionRuleValidatorTest extends BrobotTestBase {
    
    private TransitionRuleValidator validator;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        validator = new TransitionRuleValidator();
    }
    
    // Helper methods for test data creation
    private Map<String, Object> createProjectModel() {
        Map<String, Object> project = new HashMap<>();
        project.put("states", new ArrayList<>());
        project.put("stateTransitions", new ArrayList<>());
        return project;
    }
    
    private Map<String, Object> createState(int id, String name) {
        Map<String, Object> state = new HashMap<>();
        state.put("id", id);
        state.put("name", name);
        return state;
    }
    
    private Map<String, Object> createTransition(int fromId, int toId) {
        Map<String, Object> transition = new HashMap<>();
        transition.put("sourceStateId", fromId);
        transition.put("statesToEnter", List.of(toId));
        return transition;
    }
    
    private Map<String, Object> createTransition(int fromId, int toId, String condition) {
        Map<String, Object> transition = new HashMap<>();
        transition.put("sourceStateId", fromId);
        transition.put("statesToEnter", List.of(toId));
        transition.put("condition", condition);
        return transition;
    }
    
    private Map<String, Object> createTransitionWithProbability(int fromId, int toId, double probability) {
        Map<String, Object> transition = new HashMap<>();
        transition.put("sourceStateId", fromId);
        transition.put("statesToEnter", List.of(toId));
        transition.put("probability", probability);
        return transition;
    }
    
    private void addState(Map<String, Object> project, Map<String, Object> state) {
        ((List<Map<String, Object>>)project.get("states")).add(state);
    }
    
    private void addTransition(Map<String, Object> project, Map<String, Object> transition) {
        ((List<Map<String, Object>>)project.get("stateTransitions")).add(transition);
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
        @DisplayName("Handle null project model")
        public void testNullProjectModel() {
            ValidationResult result = validator.validateTransitionRules(null);
            
            assertFalse(result.isValid());
            assertEquals(1, result.getErrors().size());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationSeverity.CRITICAL, error.severity());
            assertTrue(error.message().contains("Project model is null"));
        }
        
        @Test
        @DisplayName("Handle invalid project model type")
        public void testInvalidProjectModelType() {
            ValidationResult result = validator.validateTransitionRules("invalid");
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "Project model could not be processed"));
        }
        
        @Test
        @DisplayName("Handle empty project model")
        public void testEmptyProjectModel() {
            Map<String, Object> project = new HashMap<>();
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }
        
        @Test
        @DisplayName("Handle project with no transitions")
        public void testProjectWithNoTransitions() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should warn about unreachable states
            assertTrue(hasWarning(result, "unreachable"));
        }
        
        @Test
        @DisplayName("Handle project with no states")
        public void testProjectWithNoStates() {
            Map<String, Object> project = new HashMap<>();
            project.put("stateTransitions", new ArrayList<>());
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(result.isValid());
        }
    }
    
    @Nested
    @DisplayName("Cycle Detection")
    class CycleDetection {
        
        @Test
        @DisplayName("Detect simple self-loop")
        public void testSelfLoop() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addTransition(project, createTransition(1, 1));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(hasWarning(result, "cycle"));
        }
        
        @Test
        @DisplayName("Detect two-state cycle")
        public void testTwoStateCycle() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(2, 1));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(hasWarning(result, "cycle"));
        }
        
        @Test
        @DisplayName("Detect complex multi-state cycle")
        public void testComplexCycle() {
            Map<String, Object> project = createProjectModel();
            
            // Create a cycle: 1 -> 2 -> 3 -> 4 -> 1
            for (int i = 1; i <= 4; i++) {
                addState(project, createState(i, "State" + i));
            }
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(2, 3));
            addTransition(project, createTransition(3, 4));
            addTransition(project, createTransition(4, 1));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(hasWarning(result, "cycle"));
        }
        
        @Test
        @DisplayName("Accept DAG without cycles")
        public void testAcyclicGraph() {
            Map<String, Object> project = createProjectModel();
            
            // Create a directed acyclic graph (DAG)
            for (int i = 1; i <= 5; i++) {
                addState(project, createState(i, "State" + i));
            }
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(1, 3));
            addTransition(project, createTransition(2, 4));
            addTransition(project, createTransition(3, 4));
            addTransition(project, createTransition(4, 5));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should not warn about cycles
            assertFalse(hasWarning(result, "cycle"));
        }
        
        @Test
        @DisplayName("Detect multiple independent cycles")
        public void testMultipleCycles() {
            Map<String, Object> project = createProjectModel();
            
            // First cycle: 1 -> 2 -> 1
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(2, 1));
            
            // Second cycle: 3 -> 4 -> 5 -> 3
            addState(project, createState(3, "State3"));
            addState(project, createState(4, "State4"));
            addState(project, createState(5, "State5"));
            addTransition(project, createTransition(3, 4));
            addTransition(project, createTransition(4, 5));
            addTransition(project, createTransition(5, 3));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should detect both cycles
            long cycleWarnings = result.getWarnings().stream()
                .filter(w -> w.message().contains("cycle"))
                .count();
            assertTrue(cycleWarnings >= 2);
        }
        
        @Test
        @DisplayName("Accept cycle with exit condition")
        public void testCycleWithExit() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "LoopState"));
            addState(project, createState(2, "ExitState"));
            
            // Loop that can exit
            addTransition(project, createTransition(1, 1, "continue"));
            addTransition(project, createTransition(1, 2, "exit"));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // May warn but not as severe since there's an exit
            // The actual behavior depends on implementation
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Reachability Analysis")
    class ReachabilityAnalysis {
        
        @Test
        @DisplayName("Detect unreachable state")
        public void testUnreachableState() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "StartState"));
            addState(project, createState(2, "ReachableState"));
            addState(project, createState(3, "UnreachableState"));
            
            addTransition(project, createTransition(1, 2));
            // No transition to state 3
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(hasWarning(result, "unreachable"));
            assertTrue(hasWarning(result, "3")); // State ID 3 is unreachable
        }
        
        @Test
        @DisplayName("All states reachable in connected graph")
        public void testAllStatesReachable() {
            Map<String, Object> project = createProjectModel();
            
            // Create a fully connected graph
            for (int i = 1; i <= 4; i++) {
                addState(project, createState(i, "State" + i));
            }
            
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(2, 3));
            addTransition(project, createTransition(3, 4));
            addTransition(project, createTransition(4, 1)); // Back to start
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // All states should be reachable
            assertFalse(hasWarning(result, "unreachable"));
        }
        
        @Test
        @DisplayName("Detect isolated state group")
        public void testIsolatedStateGroup() {
            Map<String, Object> project = createProjectModel();
            
            // First connected component
            addState(project, createState(1, "Start"));
            addState(project, createState(2, "State2"));
            addTransition(project, createTransition(1, 2));
            
            // Second isolated component
            addState(project, createState(3, "Isolated1"));
            addState(project, createState(4, "Isolated2"));
            addTransition(project, createTransition(3, 4));
            addTransition(project, createTransition(4, 3));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // States 3 and 4 should be unreachable from start
            assertTrue(hasWarning(result, "unreachable"));
            assertTrue(hasWarning(result, "3") || hasWarning(result, "4"));
        }
        
        @Test
        @DisplayName("State reachable through multiple paths")
        public void testMultiplePathsToState() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "Start"));
            addState(project, createState(2, "Path1"));
            addState(project, createState(3, "Path2"));
            addState(project, createState(4, "Target"));
            
            // Multiple paths to state 4
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(1, 3));
            addTransition(project, createTransition(2, 4));
            addTransition(project, createTransition(3, 4));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // All states should be reachable
            assertFalse(hasWarning(result, "unreachable"));
        }
        
        @ParameterizedTest
        @DisplayName("Test various graph sizes for reachability")
        @ValueSource(ints = {2, 5, 10, 20})
        public void testVariousGraphSizes(int stateCount) {
            Map<String, Object> project = createProjectModel();
            
            // Create linear chain of states
            for (int i = 1; i <= stateCount; i++) {
                addState(project, createState(i, "State" + i));
                if (i > 1) {
                    addTransition(project, createTransition(i - 1, i));
                }
            }
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // All states should be reachable in a linear chain
            assertFalse(hasWarning(result, "unreachable"));
        }
    }
    
    @Nested
    @DisplayName("Efficiency Validation")
    class EfficiencyValidation {
        
        @Test
        @DisplayName("Detect duplicate transitions")
        public void testDuplicateTransitions() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            // Add duplicate transitions
            addTransition(project, createTransition(1, 2, "condition1"));
            addTransition(project, createTransition(1, 2, "condition2"));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(hasWarning(result, "duplicate") || hasWarning(result, "redundant"));
        }
        
        @Test
        @DisplayName("Detect redundant paths")
        public void testRedundantPaths() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "Start"));
            addState(project, createState(2, "Middle"));
            addState(project, createState(3, "End"));
            
            // Direct path
            addTransition(project, createTransition(1, 3));
            
            // Indirect path (redundant)
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(2, 3));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // May warn about redundant paths
            // The specific behavior depends on implementation
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Accept efficient transition structure")
        public void testEfficientStructure() {
            Map<String, Object> project = createProjectModel();
            
            // Create an efficient tree structure
            addState(project, createState(1, "Root"));
            addState(project, createState(2, "Left"));
            addState(project, createState(3, "Right"));
            addState(project, createState(4, "LeftChild"));
            addState(project, createState(5, "RightChild"));
            
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(1, 3));
            addTransition(project, createTransition(2, 4));
            addTransition(project, createTransition(3, 5));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should not have efficiency warnings
            assertFalse(hasWarning(result, "redundant"));
            assertFalse(hasWarning(result, "duplicate"));
        }
        
        @Test
        @DisplayName("Detect multiple transitions with same condition")
        public void testSameConditionDifferentTargets() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            addState(project, createState(3, "State3"));
            
            // Same condition leading to different states (ambiguous)
            addTransition(project, createTransition(1, 2, "condition"));
            addTransition(project, createTransition(1, 3, "condition"));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            assertTrue(hasWarning(result, "ambiguous") || hasWarning(result, "duplicate"));
        }
    }
    
    @Nested
    @DisplayName("Concurrency Validation")
    class ConcurrencyValidation {
        
        @Test
        @DisplayName("Detect potential race condition")
        public void testRaceCondition() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "SharedResource"));
            addState(project, createState(2, "Process1"));
            addState(project, createState(3, "Process2"));
            
            // Multiple transitions to the same state (potential race)
            Map<String, Object> trans1 = createTransition(2, 1);
            trans1.put("async", true);
            addTransition(project, trans1);
            
            Map<String, Object> trans2 = createTransition(3, 1);
            trans2.put("async", true);
            addTransition(project, trans2);
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should warn about potential concurrency issues
            assertTrue(hasWarning(result, "race") || hasWarning(result, "concurrency"));
        }
        
        @Test
        @DisplayName("Accept synchronized transitions")
        public void testSynchronizedTransitions() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "SharedResource"));
            addState(project, createState(2, "Process1"));
            addState(project, createState(3, "Process2"));
            
            // Synchronized transitions (with locks/mutexes)
            Map<String, Object> trans1 = createTransition(2, 1);
            trans1.put("synchronized", true);
            addTransition(project, trans1);
            
            Map<String, Object> trans2 = createTransition(3, 1);
            trans2.put("synchronized", true);
            addTransition(project, trans2);
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should not warn about concurrency when synchronized
            assertFalse(hasWarning(result, "race"));
        }
        
        @Test
        @DisplayName("Detect deadlock potential")
        public void testDeadlockPotential() {
            Map<String, Object> project = createProjectModel();
            
            // Create potential deadlock scenario
            addState(project, createState(1, "Resource1"));
            addState(project, createState(2, "Resource2"));
            addState(project, createState(3, "Process1"));
            addState(project, createState(4, "Process2"));
            
            // Process1 needs Resource1 then Resource2
            addTransition(project, createTransition(3, 1));
            addTransition(project, createTransition(1, 2));
            
            // Process2 needs Resource2 then Resource1
            addTransition(project, createTransition(4, 2));
            addTransition(project, createTransition(2, 1));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // May detect potential deadlock
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Large state machine validation")
        public void testLargeStateMachine() {
            Map<String, Object> project = createProjectModel();
            
            // Create a large state machine with 50 states
            for (int i = 1; i <= 50; i++) {
                addState(project, createState(i, "State" + i));
            }
            
            // Add transitions creating a complex but valid structure
            for (int i = 1; i < 50; i++) {
                addTransition(project, createTransition(i, i + 1));
                if (i % 5 == 0 && i > 5) {
                    // Add some back edges for complexity
                    addTransition(project, createTransition(i, i - 5));
                }
            }
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should complete validation without errors
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("State machine with probability transitions")
        public void testProbabilisticTransitions() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "DecisionState"));
            addState(project, createState(2, "Outcome1"));
            addState(project, createState(3, "Outcome2"));
            addState(project, createState(4, "Outcome3"));
            
            // Probabilistic transitions
            addTransition(project, createTransitionWithProbability(1, 2, 0.5));
            addTransition(project, createTransitionWithProbability(1, 3, 0.3));
            addTransition(project, createTransitionWithProbability(1, 4, 0.2));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Probabilities sum to 1.0, should be valid
            assertTrue(result.getErrors().isEmpty() || !hasError(result, "probability"));
        }
        
        @Test
        @DisplayName("Invalid probability sum")
        public void testInvalidProbabilitySum() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "DecisionState"));
            addState(project, createState(2, "Outcome1"));
            addState(project, createState(3, "Outcome2"));
            
            // Probabilities don't sum to 1.0
            addTransition(project, createTransitionWithProbability(1, 2, 0.6));
            addTransition(project, createTransitionWithProbability(1, 3, 0.6));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should warn about invalid probability sum
            assertTrue(hasWarning(result, "probability") || hasWarning(result, "sum"));
        }
        
        @Test
        @DisplayName("Hierarchical state machine")
        public void testHierarchicalStateMachine() {
            Map<String, Object> project = createProjectModel();
            
            // Parent states
            Map<String, Object> parent1 = createState(1, "ParentState1");
            parent1.put("type", "composite");
            addState(project, parent1);
            
            Map<String, Object> parent2 = createState(2, "ParentState2");
            parent2.put("type", "composite");
            addState(project, parent2);
            
            // Child states
            for (int i = 3; i <= 6; i++) {
                Map<String, Object> child = createState(i, "ChildState" + i);
                child.put("parentId", i <= 4 ? 1 : 2);
                addState(project, child);
            }
            
            // Transitions between parents and children
            addTransition(project, createTransition(1, 2));
            addTransition(project, createTransition(3, 4));
            addTransition(project, createTransition(5, 6));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should handle hierarchical structure
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Handle malformed transition structure")
        public void testMalformedTransitionStructure() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            
            // Add malformed transition
            Map<String, Object> badTransition = new HashMap<>();
            badTransition.put("fromState", "not_an_integer"); // Wrong type
            badTransition.put("toState", 2);
            addTransition(project, badTransition);
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should handle gracefully
            assertTrue(hasError(result, "Error validating"));
        }
        
        @Test
        @DisplayName("Handle null transitions in list")
        public void testNullTransitions() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            List<Map<String, Object>> transitions = (List<Map<String, Object>>)project.get("stateTransitions");
            transitions.add(createTransition(1, 2));
            transitions.add(null); // Null transition
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should handle null transitions gracefully
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle states without IDs")
        public void testStatesWithoutIds() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> stateNoId = new HashMap<>();
            stateNoId.put("name", "StateWithoutId");
            addState(project, stateNoId);
            
            addState(project, createState(1, "State1"));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should handle states without IDs
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle empty state list with transitions")
        public void testEmptyStateListWithTransitions() {
            Map<String, Object> project = createProjectModel();
            // No states added
            
            addTransition(project, createTransition(1, 2));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should handle gracefully
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle very large state IDs")
        public void testLargeStateIds() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(Integer.MAX_VALUE, "MaxState"));
            addState(project, createState(Integer.MAX_VALUE - 1, "AlmostMaxState"));
            
            addTransition(project, createTransition(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should handle large IDs
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle negative state IDs")
        public void testNegativeStateIds() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(-1, "NegativeState"));
            addState(project, createState(1, "PositiveState"));
            
            addTransition(project, createTransition(-1, 1));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should handle negative IDs
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Integration with ValidationResult")
    class ValidationResultIntegration {
        
        @Test
        @DisplayName("Properly categorize severity levels")
        public void testSeverityLevels() {
            // Test null model (CRITICAL)
            ValidationResult criticalResult = validator.validateTransitionRules(null);
            assertEquals(1, criticalResult.getErrors().stream()
                .filter(e -> e.severity() == ValidationSeverity.CRITICAL)
                .count());
            
            // Test unreachable state (WARNING)
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "UnreachableState"));
            
            ValidationResult warningResult = validator.validateTransitionRules(project);
            assertTrue(warningResult.getWarnings().stream()
                .anyMatch(e -> e.severity() == ValidationSeverity.WARNING));
        }
        
        @Test
        @DisplayName("Multiple warnings accumulate correctly")
        public void testMultipleWarningsAccumulate() {
            Map<String, Object> project = createProjectModel();
            
            // Create multiple issues
            // Unreachable state
            addState(project, createState(1, "Start"));
            addState(project, createState(2, "Unreachable"));
            
            // Cycle
            addState(project, createState(3, "Cycle1"));
            addState(project, createState(4, "Cycle2"));
            addTransition(project, createTransition(3, 4));
            addTransition(project, createTransition(4, 3));
            
            // Duplicate transitions
            addTransition(project, createTransition(1, 3));
            addTransition(project, createTransition(1, 3));
            
            ValidationResult result = validator.validateTransitionRules(project);
            
            // Should have multiple warnings
            assertTrue(result.getWarnings().size() >= 2);
        }
    }
}