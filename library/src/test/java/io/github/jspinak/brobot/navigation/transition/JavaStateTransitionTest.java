package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for JavaStateTransition. Tests all aspects of Java-based state
 * transitions including builder, execution, state management, and edge cases.
 */
@DisplayName("JavaStateTransition Tests")
class JavaStateTransitionTest extends BrobotTestBase {

    private JavaStateTransition transition;
    private AtomicBoolean functionCalled;
    private BooleanSupplier successFunction;
    private BooleanSupplier failureFunction;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        transition = new JavaStateTransition();
        functionCalled = new AtomicBoolean(false);
        successFunction =
                () -> {
                    functionCalled.set(true);
                    return true;
                };
        failureFunction =
                () -> {
                    functionCalled.set(true);
                    return false;
                };
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {

        @Test
        @DisplayName("Should have correct type identifier")
        void testTypeIdentifier() {
            assertEquals("java", transition.getType());
        }

        @Test
        @DisplayName("Should return empty TaskSequence optional")
        void testNoTaskSequence() {
            Optional<TaskSequence> result = transition.getTaskSequenceOptional();
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should initialize with empty collections")
        void testInitialState() {
            assertNotNull(transition.getActivateNames());
            assertNotNull(transition.getExitNames());
            assertNotNull(transition.getActivate());
            assertNotNull(transition.getExit());
            assertTrue(transition.getActivateNames().isEmpty());
            assertTrue(transition.getExitNames().isEmpty());
            assertTrue(transition.getActivate().isEmpty());
            assertTrue(transition.getExit().isEmpty());
        }

        @Test
        @DisplayName("Should have zero initial score")
        void testInitialScore() {
            assertEquals(0, transition.getScore());
        }

        @Test
        @DisplayName("Should have zero initial success count")
        void testInitialSuccessCount() {
            assertEquals(0, transition.getTimesSuccessful());
        }

        @Test
        @DisplayName("Should have null visibility by default")
        void testInitialVisibility() {
            assertNull(transition.getStaysVisibleAfterTransition());
        }
    }

    @Nested
    @DisplayName("State Management")
    class StateManagement {

        @Test
        @DisplayName("Should manage activation state names")
        void testActivationStateNames() {
            Set<String> states = new HashSet<>(Arrays.asList("State1", "State2", "State3"));
            transition.setActivateNames(states);

            assertEquals(states, transition.getActivateNames());
            assertEquals(3, transition.getActivateNames().size());
            assertTrue(transition.getActivateNames().contains("State2"));
        }

        @Test
        @DisplayName("Should manage exit state names")
        void testExitStateNames() {
            Set<String> states = new HashSet<>(Arrays.asList("Exit1", "Exit2"));
            transition.setExitNames(states);

            assertEquals(states, transition.getExitNames());
            assertEquals(2, transition.getExitNames().size());
            assertTrue(transition.getExitNames().contains("Exit1"));
        }

        @Test
        @DisplayName("Should manage activation state IDs")
        void testActivationStateIds() {
            Set<Long> ids = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L));
            transition.setActivate(ids);

            assertEquals(ids, transition.getActivate());
            assertEquals(4, transition.getActivate().size());
            assertTrue(transition.getActivate().contains(3L));
        }

        @Test
        @DisplayName("Should manage exit state IDs")
        void testExitStateIds() {
            Set<Long> ids = new HashSet<>(Arrays.asList(10L, 20L));
            transition.setExit(ids);

            assertEquals(ids, transition.getExit());
            assertEquals(2, transition.getExit().size());
            assertTrue(transition.getExit().contains(20L));
        }

        @Test
        @DisplayName("Should handle empty state sets")
        void testEmptyStateSets() {
            transition.setActivateNames(new HashSet<>());
            transition.setExitNames(new HashSet<>());
            transition.setActivate(new HashSet<>());
            transition.setExit(new HashSet<>());

            assertTrue(transition.getActivateNames().isEmpty());
            assertTrue(transition.getExitNames().isEmpty());
            assertTrue(transition.getActivate().isEmpty());
            assertTrue(transition.getExit().isEmpty());
        }

        @Test
        @DisplayName("Should handle duplicate state names")
        void testDuplicateStateNames() {
            Set<String> states = new HashSet<>(Arrays.asList("State1", "State1", "State2"));
            transition.setActivateNames(states);

            // Set removes duplicates
            assertEquals(2, transition.getActivateNames().size());
        }
    }

    @Nested
    @DisplayName("Transition Function")
    class TransitionFunction {

        @Test
        @DisplayName("Should execute success function")
        void testSuccessFunction() {
            transition.setTransitionFunction(successFunction);

            BooleanSupplier function = transition.getTransitionFunction();
            assertNotNull(function);
            assertTrue(function.getAsBoolean());
            assertTrue(functionCalled.get());
        }

        @Test
        @DisplayName("Should execute failure function")
        void testFailureFunction() {
            transition.setTransitionFunction(failureFunction);

            BooleanSupplier function = transition.getTransitionFunction();
            assertNotNull(function);
            assertFalse(function.getAsBoolean());
            assertTrue(functionCalled.get());
        }

        @Test
        @DisplayName("Should handle null function")
        void testNullFunction() {
            transition.setTransitionFunction(null);
            assertNull(transition.getTransitionFunction());
        }

        @Test
        @DisplayName("Should handle complex function logic")
        void testComplexFunction() {
            AtomicInteger counter = new AtomicInteger(0);
            BooleanSupplier complexFunction =
                    () -> {
                        int count = counter.incrementAndGet();
                        return count % 2 == 0; // Success on even counts
                    };

            transition.setTransitionFunction(complexFunction);

            assertFalse(transition.getTransitionFunction().getAsBoolean()); // 1st call: false
            assertTrue(transition.getTransitionFunction().getAsBoolean()); // 2nd call: true
            assertFalse(transition.getTransitionFunction().getAsBoolean()); // 3rd call: false
        }

        @Test
        @DisplayName("Should handle exception in function")
        void testFunctionWithException() {
            BooleanSupplier exceptionFunction =
                    () -> {
                        throw new RuntimeException("Test exception");
                    };

            transition.setTransitionFunction(exceptionFunction);

            assertThrows(
                    RuntimeException.class,
                    () -> transition.getTransitionFunction().getAsBoolean());
        }
    }

    @Nested
    @DisplayName("Visibility Management")
    class VisibilityManagement {

        @Test
        @DisplayName("Should set visibility to TRUE")
        void testVisibilityTrue() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);
            assertEquals(
                    StateTransition.StaysVisible.TRUE, transition.getStaysVisibleAfterTransition());
        }

        @Test
        @DisplayName("Should set visibility to FALSE")
        void testVisibilityFalse() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.FALSE);
            assertEquals(
                    StateTransition.StaysVisible.FALSE,
                    transition.getStaysVisibleAfterTransition());
        }

        @Test
        @DisplayName("Should set visibility to NONE")
        void testVisibilityNone() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.NONE);
            assertEquals(
                    StateTransition.StaysVisible.NONE, transition.getStaysVisibleAfterTransition());
        }

        @Test
        @DisplayName("Should handle visibility changes")
        void testVisibilityChanges() {
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);
            assertEquals(
                    StateTransition.StaysVisible.TRUE, transition.getStaysVisibleAfterTransition());

            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.FALSE);
            assertEquals(
                    StateTransition.StaysVisible.FALSE,
                    transition.getStaysVisibleAfterTransition());
        }
    }

    @Nested
    @DisplayName("Score and Metrics")
    class ScoreAndMetrics {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10, 50, 100, 1000, Integer.MAX_VALUE})
        @DisplayName("Should handle various score values")
        void testScoreValues(int score) {
            transition.setScore(score);
            assertEquals(score, transition.getScore());
        }

        @Test
        @DisplayName("Should handle negative scores")
        void testNegativeScore() {
            transition.setScore(-100);
            assertEquals(-100, transition.getScore());
        }

        @Test
        @DisplayName("Should track success count")
        void testSuccessCount() {
            assertEquals(0, transition.getTimesSuccessful());

            transition.setTimesSuccessful(5);
            assertEquals(5, transition.getTimesSuccessful());

            transition.setTimesSuccessful(transition.getTimesSuccessful() + 1);
            assertEquals(6, transition.getTimesSuccessful());
        }

        @Test
        @DisplayName("Should handle large success counts")
        void testLargeSuccessCount() {
            transition.setTimesSuccessful(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, transition.getTimesSuccessful());
        }
    }

    @Nested
    @DisplayName("ToString Method")
    class ToStringMethod {

        @Test
        @DisplayName("Should format empty transition")
        void testToStringEmpty() {
            String result = transition.toString();
            assertNotNull(result);
            assertTrue(result.contains("activate=[]"));
            assertTrue(result.contains("exit=[]"));
        }

        @Test
        @DisplayName("Should format transition with states")
        void testToStringWithStates() {
            transition.setActivateNames(new HashSet<>(Arrays.asList("State1", "State2")));
            transition.setExitNames(new HashSet<>(Arrays.asList("Exit1")));

            String result = transition.toString();
            assertNotNull(result);
            assertTrue(result.contains("activate="));
            assertTrue(result.contains("exit="));
            assertTrue(result.contains("State1") || result.contains("State2"));
            assertTrue(result.contains("Exit1"));
        }

        @Test
        @DisplayName("Should handle special characters in state names")
        void testToStringSpecialCharacters() {
            transition.setActivateNames(
                    new HashSet<>(Arrays.asList("State-1", "State_2", "State.3", "State 4")));

            String result = transition.toString();
            assertNotNull(result);
            assertTrue(result.contains("State-1") || result.contains("State_2"));
        }
    }

    @Nested
    @DisplayName("Builder Pattern Extended")
    class BuilderPatternExtended {

        @Test
        @DisplayName("Should build with all configurations")
        void testCompleteBuilder() {
            JavaStateTransition built =
                    new JavaStateTransition.Builder()
                            .setFunction(successFunction)
                            .addToActivate("A1", "A2", "A3")
                            .addToExit("E1", "E2")
                            .setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE)
                            .setScore(50)
                            .build();

            assertNotNull(built);
            assertNotNull(built.getTransitionFunction());
            assertEquals(3, built.getActivateNames().size());
            assertEquals(2, built.getExitNames().size());
            assertEquals(StateTransition.StaysVisible.TRUE, built.getStaysVisibleAfterTransition());
            assertEquals(50, built.getScore());
        }

        @Test
        @DisplayName("Should handle boolean visibility setter")
        void testBooleanVisibilitySetter() {
            JavaStateTransition built1 =
                    new JavaStateTransition.Builder().setStaysVisibleAfterTransition(true).build();
            assertEquals(
                    StateTransition.StaysVisible.TRUE, built1.getStaysVisibleAfterTransition());

            JavaStateTransition built2 =
                    new JavaStateTransition.Builder().setStaysVisibleAfterTransition(false).build();
            assertEquals(
                    StateTransition.StaysVisible.FALSE, built2.getStaysVisibleAfterTransition());
        }

        @Test
        @DisplayName("Should accumulate states with multiple calls")
        void testAccumulateStates() {
            JavaStateTransition built =
                    new JavaStateTransition.Builder()
                            .addToActivate("S1")
                            .addToActivate("S2", "S3")
                            .addToActivate("S4")
                            .addToExit("E1")
                            .addToExit("E2", "E3", "E4")
                            .build();

            assertEquals(4, built.getActivateNames().size());
            assertEquals(4, built.getExitNames().size());
            assertTrue(built.getActivateNames().containsAll(Arrays.asList("S1", "S2", "S3", "S4")));
            assertTrue(built.getExitNames().containsAll(Arrays.asList("E1", "E2", "E3", "E4")));
        }

        @Test
        @DisplayName("Should handle empty varargs")
        void testEmptyVarargs() {
            JavaStateTransition built =
                    new JavaStateTransition.Builder().addToActivate().addToExit().build();

            assertTrue(built.getActivateNames().isEmpty());
            assertTrue(built.getExitNames().isEmpty());
        }

        @Test
        @DisplayName("Should handle duplicate states in builder")
        void testBuilderDuplicates() {
            JavaStateTransition built =
                    new JavaStateTransition.Builder()
                            .addToActivate("State1", "State1", "State2")
                            .addToActivate("State2", "State3")
                            .build();

            // Set removes duplicates
            assertEquals(3, built.getActivateNames().size());
            assertTrue(
                    built.getActivateNames()
                            .containsAll(Arrays.asList("State1", "State2", "State3")));
        }

        @Test
        @DisplayName("Should reuse builder for multiple transitions")
        void testBuilderReuse() {
            JavaStateTransition.Builder builder = new JavaStateTransition.Builder().setScore(10);

            JavaStateTransition t1 = builder.addToActivate("S1").build();

            JavaStateTransition t2 = builder.addToActivate("S2").build();

            // Both should have accumulated states due to builder reuse
            assertTrue(t1.getActivateNames().contains("S1"));
            assertTrue(t2.getActivateNames().contains("S1"));
            assertTrue(t2.getActivateNames().contains("S2"));
        }

        @ParameterizedTest
        @CsvSource({"-100, NONE", "0, NONE", "50, TRUE", "100, FALSE", "1000, NONE"})
        @DisplayName("Should build with various score and visibility combinations")
        void testScoreVisibilityCombinations(int score, String visibility) {
            StateTransition.StaysVisible vis = StateTransition.StaysVisible.valueOf(visibility);

            JavaStateTransition built =
                    new JavaStateTransition.Builder()
                            .setScore(score)
                            .setStaysVisibleAfterTransition(vis)
                            .build();

            assertEquals(score, built.getScore());
            assertEquals(vis, built.getStaysVisibleAfterTransition());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundaries")
    class EdgeCasesAndBoundaries {

        @Test
        @DisplayName("Should handle null state names gracefully")
        void testNullStateNames() {
            transition.setActivateNames(null);
            transition.setExitNames(null);

            assertNull(transition.getActivateNames());
            assertNull(transition.getExitNames());

            // toString should handle nulls
            String result = transition.toString();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle very long state names")
        void testLongStateNames() {
            String longName = "State" + "X".repeat(1000);
            transition.setActivateNames(new HashSet<>(Arrays.asList(longName)));

            assertEquals(1, transition.getActivateNames().size());
            assertTrue(transition.getActivateNames().contains(longName));
        }

        @Test
        @DisplayName("Should handle large number of states")
        void testManyStates() {
            Set<String> manyStates = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                manyStates.add("State" + i);
            }

            transition.setActivateNames(manyStates);
            assertEquals(1000, transition.getActivateNames().size());
        }

        @Test
        @DisplayName("Should maintain state after multiple operations")
        void testStateConsistency() {
            transition.setScore(100);
            transition.setTimesSuccessful(5);
            transition.setTransitionFunction(successFunction);
            transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);
            transition.setActivateNames(new HashSet<>(Arrays.asList("S1", "S2")));
            transition.setExitNames(new HashSet<>(Arrays.asList("E1")));
            transition.setActivate(new HashSet<>(Arrays.asList(1L, 2L)));
            transition.setExit(new HashSet<>(Arrays.asList(10L)));

            // Verify all properties remain set
            assertEquals(100, transition.getScore());
            assertEquals(5, transition.getTimesSuccessful());
            assertNotNull(transition.getTransitionFunction());
            assertEquals(
                    StateTransition.StaysVisible.TRUE, transition.getStaysVisibleAfterTransition());
            assertEquals(2, transition.getActivateNames().size());
            assertEquals(1, transition.getExitNames().size());
            assertEquals(2, transition.getActivate().size());
            assertEquals(1, transition.getExit().size());
        }
    }
}
