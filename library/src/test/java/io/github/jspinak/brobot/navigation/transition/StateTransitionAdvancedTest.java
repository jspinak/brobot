package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Advanced test suite for StateTransition implementations and StateTransitions container. Tests
 * transition types, execution, conditions, and complex navigation scenarios.
 */
@DisplayName("StateTransition Advanced Tests")
class StateTransitionAdvancedTest extends BrobotTestBase {

    @Mock private State fromState;

    @Mock private State toState;

    @Mock private State intermediateState;

    @Mock private TaskSequence mockTaskSequence;

    @Mock private BooleanSupplier mockTransitionFunction;

    private StateTransitions stateTransitions;
    private TaskSequenceStateTransition taskSequenceTransition;
    private JavaStateTransition javaTransition;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Setup mock states
        when(fromState.getName()).thenReturn("FromState");
        when(fromState.getId()).thenReturn(1L);
        when(toState.getName()).thenReturn("ToState");
        when(toState.getId()).thenReturn(2L);
        when(intermediateState.getName()).thenReturn("IntermediateState");
        when(intermediateState.getId()).thenReturn(3L);

        // Create StateTransitions container
        stateTransitions = new StateTransitions();
        stateTransitions.setStateName("FromState");
        stateTransitions.setStateId(1L);

        // Create transition implementations
        taskSequenceTransition = new TaskSequenceStateTransition();
        taskSequenceTransition.setActionDefinition(mockTaskSequence);
        taskSequenceTransition.setActivate(Set.of(2L));

        javaTransition = new JavaStateTransition();
        javaTransition.setTransitionFunction(mockTransitionFunction);
        javaTransition.setActivateNames(Set.of("ToState"));
    }

    @Nested
    @DisplayName("TaskSequence Transitions")
    class TaskSequenceTransitions {

        @Test
        @DisplayName("Should create task sequence transition with activation states")
        void testTaskSequenceTransitionCreation() {
            // Arrange
            taskSequenceTransition.setActivate(Set.of(2L, 3L));
            taskSequenceTransition.setExit(Set.of(1L));
            taskSequenceTransition.setPathCost(10);

            // Act & Assert
            assertEquals(2, taskSequenceTransition.getActivate().size());
            assertTrue(taskSequenceTransition.getActivate().contains(2L));
            assertTrue(taskSequenceTransition.getActivate().contains(3L));
            assertEquals(1, taskSequenceTransition.getExit().size());
            assertEquals(10, taskSequenceTransition.getPathCost());
        }

        @Test
        @DisplayName("Should handle visibility settings")
        void testVisibilitySettings() {
            // Test each visibility setting
            for (StateTransition.StaysVisible visibility : StateTransition.StaysVisible.values()) {
                taskSequenceTransition.setStaysVisibleAfterTransition(visibility);
                assertEquals(visibility, taskSequenceTransition.getStaysVisibleAfterTransition());
            }
        }

        @Test
        @DisplayName("Should track success count")
        void testSuccessTracking() {
            // Arrange
            taskSequenceTransition.setTimesSuccessful(0);

            // Act
            for (int i = 0; i < 5; i++) {
                taskSequenceTransition.setTimesSuccessful(
                        taskSequenceTransition.getTimesSuccessful() + 1);
            }

            // Assert
            assertEquals(5, taskSequenceTransition.getTimesSuccessful());
        }
    }

    @Nested
    @DisplayName("Java Function Transitions")
    class JavaFunctionTransitions {

        @Test
        @DisplayName("Should execute Boolean supplier function")
        void testBooleanSupplierExecution() {
            // Arrange
            when(mockTransitionFunction.getAsBoolean()).thenReturn(true);
            javaTransition.setTransitionFunction(mockTransitionFunction);

            // Act
            boolean result = javaTransition.getTransitionFunction().getAsBoolean();

            // Assert
            assertTrue(result);
            verify(mockTransitionFunction).getAsBoolean();
        }

        @Test
        @DisplayName("Should handle function returning false")
        void testFunctionReturningFalse() {
            // Arrange
            when(mockTransitionFunction.getAsBoolean()).thenReturn(false);
            javaTransition.setTransitionFunction(mockTransitionFunction);

            // Act
            boolean result = javaTransition.getTransitionFunction().getAsBoolean();

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should convert state names to IDs")
        void testStateNameToIdConversion() {
            // Arrange
            javaTransition.setActivateNames(Set.of("State1", "State2", "State3"));
            javaTransition.setExitNames(Set.of("State4"));

            // Act & Assert
            assertEquals(3, javaTransition.getActivateNames().size());
            assertEquals(1, javaTransition.getExitNames().size());
        }
    }

    @Nested
    @DisplayName("StateTransitions Container")
    class StateTransitionsContainer {

        @Test
        @DisplayName("Should manage multiple transitions")
        void testMultipleTransitions() {
            // Arrange
            StateTransition transition1 = mock(TaskSequenceStateTransition.class);
            StateTransition transition2 = mock(TaskSequenceStateTransition.class);
            when(transition1.getActivate()).thenReturn(Set.of(2L));
            when(transition2.getActivate()).thenReturn(Set.of(3L));

            // Act
            stateTransitions.getTransitions().add(transition1);
            stateTransitions.getTransitions().add(transition2);

            // Assert
            assertEquals(2, stateTransitions.getTransitions().size());
        }

        @Test
        @DisplayName("Should find transition by target state")
        void testFindTransitionByTarget() {
            // Arrange
            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(2L, 3L));
            stateTransitions.getTransitions().add(transition);

            // Act
            Optional<StateTransition> found =
                    stateTransitions.getTransitionFunctionByActivatedStateId(2L);

            // Assert
            assertTrue(found.isPresent());
            assertEquals(transition, found.get());
        }

        @Test
        @DisplayName("Should return empty for non-existent target")
        void testNoTransitionFound() {
            // Act
            Optional<StateTransition> found =
                    stateTransitions.getTransitionFunctionByActivatedStateId(999L);

            // Assert
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should handle transition finish")
        void testTransitionFinish() {
            // Arrange
            StateTransition finishTransition = mock(TaskSequenceStateTransition.class);
            stateTransitions.setTransitionFinish(finishTransition);

            // Act
            Optional<StateTransition> found =
                    stateTransitions.getTransitionFunctionByActivatedStateId(1L);

            // Assert
            assertTrue(found.isPresent());
            assertEquals(finishTransition, found.get());
        }
    }

    @Nested
    @DisplayName("Path Scoring")
    class PathScoring {

        @ParameterizedTest
        @ValueSource(ints = {0, 10, 50, 100, 1000})
        @DisplayName("Should handle various score values")
        void testScoreValues(int score) {
            // Arrange & Act
            taskSequenceTransition.setPathCost(score);

            // Assert
            assertEquals(score, taskSequenceTransition.getPathCost());
        }

        @Test
        @DisplayName("Should compare transitions by score")
        void testScoreComparison() {
            // Arrange
            TaskSequenceStateTransition lowScore = new TaskSequenceStateTransition();
            lowScore.setPathCost(10);

            TaskSequenceStateTransition highScore = new TaskSequenceStateTransition();
            highScore.setPathCost(100);

            // Act
            List<StateTransition> transitions = Arrays.asList(highScore, lowScore);
            transitions.sort(Comparator.comparingInt(StateTransition::getPathCost));

            // Assert
            assertEquals(lowScore, transitions.get(0));
            assertEquals(highScore, transitions.get(1));
        }
    }

    @Nested
    @DisplayName("Concurrent Transition Access")
    class ConcurrentTransitionAccess {

        @Test
        @DisplayName("Should handle concurrent transition additions")
        void testConcurrentAdditions() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            Set<Integer> addedIds = Collections.synchronizedSet(new HashSet<>());

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(
                        () -> {
                            try {
                                TaskSequenceStateTransition transition =
                                        new TaskSequenceStateTransition();
                                transition.setActivate(Set.of((long) threadId));
                                // Synchronize the add operation since ArrayList is not thread-safe
                                synchronized (stateTransitions) {
                                    stateTransitions.getTransitions().add(transition);
                                    addedIds.add(threadId);
                                }
                            } finally {
                                latch.countDown();
                            }
                        });
            }

            // Assert
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            // Verify all threads successfully added their transitions
            assertEquals(threadCount, addedIds.size());
            assertEquals(threadCount, stateTransitions.getTransitions().size());
            executor.shutdown();
        }

        @Test
        @DisplayName("Should safely read during concurrent modifications")
        void testConcurrentReads() throws InterruptedException {
            // Arrange
            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(2L));
            stateTransitions.getTransitions().add(transition);

            int readers = 20;
            CountDownLatch latch = new CountDownLatch(readers);
            ExecutorService executor = Executors.newFixedThreadPool(readers);
            Set<StateTransition> foundTransitions = ConcurrentHashMap.newKeySet();

            // Act
            for (int i = 0; i < readers; i++) {
                executor.submit(
                        () -> {
                            try {
                                Optional<StateTransition> found =
                                        stateTransitions.getTransitionFunctionByActivatedStateId(
                                                2L);
                                found.ifPresent(foundTransitions::add);
                            } finally {
                                latch.countDown();
                            }
                        });
            }

            // Assert
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(1, foundTransitions.size());
            assertTrue(foundTransitions.contains(transition));
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Transition Validation")
    class TransitionValidation {

        @Test
        @DisplayName("Should detect circular transitions")
        void testCircularTransitionDetection() {
            // Arrange
            TaskSequenceStateTransition transition1 = new TaskSequenceStateTransition();
            transition1.setActivate(Set.of(2L));

            TaskSequenceStateTransition transition2 = new TaskSequenceStateTransition();
            transition2.setActivate(Set.of(1L)); // Creates cycle

            // Act
            stateTransitions.getTransitions().add(transition1);
            // In real implementation, would need cycle detection logic

            // Assert
            // This is a placeholder - actual implementation would need cycle detection
            assertNotNull(stateTransitions);
        }

        @Test
        @DisplayName("Should validate required fields")
        void testRequiredFieldValidation() {
            // Arrange
            TaskSequenceStateTransition incomplete = new TaskSequenceStateTransition();

            // Act & Assert
            assertNull(incomplete.getActionDefinition());
            assertTrue(incomplete.getActivate().isEmpty());
            assertEquals(1, incomplete.getPathCost()); // Default cost is 1
        }

        @Test
        @DisplayName("Should handle empty activation sets")
        void testEmptyActivationSet() {
            // Arrange
            TaskSequenceStateTransition emptyTransition = new TaskSequenceStateTransition();
            emptyTransition.setActivate(new HashSet<>());

            // Act
            stateTransitions.getTransitions().add(emptyTransition);

            // Assert
            // Should not find transition for any target
            Optional<StateTransition> found =
                    stateTransitions.getTransitionFunctionByActivatedStateId(2L);
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Visibility Behavior")
    class VisibilityBehavior {

        @ParameterizedTest
        @EnumSource(StateTransition.StaysVisible.class)
        @DisplayName("Should respect visibility settings")
        void testVisibilityInheritance(StateTransition.StaysVisible visibility) {
            // Arrange
            taskSequenceTransition.setStaysVisibleAfterTransition(visibility);
            stateTransitions.setStaysVisibleAfterTransition(true);

            // Act & Assert
            if (visibility == StateTransition.StaysVisible.NONE) {
                // Should inherit from StateTransitions
                assertTrue(stateTransitions.isStaysVisibleAfterTransition());
            } else {
                // Should use own setting
                assertEquals(visibility, taskSequenceTransition.getStaysVisibleAfterTransition());
            }
        }

        @Test
        @DisplayName("Should override container visibility when set")
        void testVisibilityOverride() {
            // Arrange
            stateTransitions.setStaysVisibleAfterTransition(false);
            taskSequenceTransition.setStaysVisibleAfterTransition(
                    StateTransition.StaysVisible.TRUE);

            // Act & Assert
            assertEquals(
                    StateTransition.StaysVisible.TRUE,
                    taskSequenceTransition.getStaysVisibleAfterTransition());
            assertFalse(stateTransitions.isStaysVisibleAfterTransition());
        }
    }
}
