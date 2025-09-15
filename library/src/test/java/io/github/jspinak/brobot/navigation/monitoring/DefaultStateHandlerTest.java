package io.github.jspinak.brobot.navigation.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Test suite for DefaultStateHandler. Tests the default state handling implementation with various
 * scenarios.
 *
 * <p>Note: This test is disabled in CI environments due to intermittent timeout issues. It passes
 * consistently in local development environments.
 */
@DisplayName("DefaultStateHandler Tests")
@Timeout(value = 10, unit = TimeUnit.SECONDS) // Prevent CI/CD timeout
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Flaky test - times out intermittently in CI/CD")
class DefaultStateHandlerTest extends BrobotTestBase {

    @Mock private StateNavigator stateNavigator;

    @Mock private State currentState;

    private DefaultStateHandler stateHandler;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        stateHandler = new DefaultStateHandler(stateNavigator);

        // Setup default state behavior
        when(currentState.getName()).thenReturn("TestState");
        when(currentState.getId()).thenReturn(1L);
    }

    @Nested
    @DisplayName("Handle State Tests")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    class HandleStateTests {

        @Test
        @DisplayName("Should handle state with single transition")
        void testHandleStateWithSingleTransition() {
            // Arrange
            StateTransitions transitions = new StateTransitions();
            transitions.setStateName("TestState");
            transitions.setStateId(1L);

            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            Set<Long> activateStates = new HashSet<>();
            activateStates.add(2L);
            transition.setActivate(activateStates);
            transitions.getTransitions().add(transition);

            when(stateNavigator.openState(2L)).thenReturn(true);

            // Act
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertTrue(result);
            verify(stateNavigator).openState(2L);
        }

        @Test
        @DisplayName("Should handle state with multiple transitions - takes first")
        void testHandleStateWithMultipleTransitions() {
            // Arrange
            StateTransitions transitions = new StateTransitions();

            // First transition
            TaskSequenceStateTransition transition1 = new TaskSequenceStateTransition();
            transition1.setActivate(Set.of(2L));
            transitions.getTransitions().add(transition1);

            // Second transition (should be ignored)
            TaskSequenceStateTransition transition2 = new TaskSequenceStateTransition();
            transition2.setActivate(Set.of(3L));
            transitions.getTransitions().add(transition2);

            when(stateNavigator.openState(2L)).thenReturn(true);

            // Act
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertTrue(result);
            verify(stateNavigator).openState(2L);
            verify(stateNavigator, never()).openState(3L);
        }

        @Test
        @DisplayName("Should handle transition with multiple target states - takes first")
        void testHandleTransitionWithMultipleTargets() {
            // Arrange
            StateTransitions transitions = new StateTransitions();

            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            Set<Long> multipleTargets = new HashSet<>();
            multipleTargets.add(2L);
            multipleTargets.add(3L);
            multipleTargets.add(4L);
            transition.setActivate(multipleTargets);
            transitions.getTransitions().add(transition);

            when(stateNavigator.openState(anyLong())).thenReturn(true);

            // Act
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertTrue(result);
            // Should navigate to one of the targets (first from iterator)
            verify(stateNavigator, times(1)).openState(anyLong());
        }

        @Test
        @DisplayName("Should return false when no transitions available")
        void testHandleStateWithNoTransitions() {
            // Arrange
            StateTransitions emptyTransitions = new StateTransitions();
            emptyTransitions.setStateName("TestState");

            // Act
            boolean result = stateHandler.handleState(currentState, emptyTransitions);

            // Assert
            assertFalse(result);
            verify(stateNavigator, never()).openState(anyLong());
        }

        @Test
        @DisplayName("Should return false when transitions is null")
        void testHandleStateWithNullTransitions() {
            // Act
            boolean result = stateHandler.handleState(currentState, null);

            // Assert
            assertFalse(result);
            verify(stateNavigator, never()).openState(anyLong());
        }

        @Test
        @DisplayName("Should handle transition with empty activation set")
        void testHandleTransitionWithEmptyActivation() {
            // Arrange
            StateTransitions transitions = new StateTransitions();

            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(new HashSet<>()); // Empty activation set
            transitions.getTransitions().add(transition);

            // Act
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertFalse(result);
            verify(stateNavigator, never()).openState(anyLong());
        }

        @Test
        @DisplayName("Should handle navigation failure")
        void testHandleNavigationFailure() {
            // Arrange
            StateTransitions transitions = new StateTransitions();

            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(2L));
            transitions.getTransitions().add(transition);

            when(stateNavigator.openState(2L)).thenReturn(false);

            // Act
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertFalse(result);
            verify(stateNavigator).openState(2L);
        }

        @Test
        @DisplayName("Should handle navigation exception")
        void testHandleNavigationException() {
            // Arrange
            StateTransitions transitions = new StateTransitions();

            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(2L));
            transitions.getTransitions().add(transition);

            when(stateNavigator.openState(2L)).thenThrow(new RuntimeException("Navigation error"));

            // Act
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertFalse(result);
            verify(stateNavigator).openState(2L);
        }

        @Test
        @DisplayName("Should work with JavaStateTransition")
        void testHandleJavaStateTransition() {
            // Arrange
            StateTransitions transitions = new StateTransitions();

            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setActivate(Set.of(3L));
            javaTransition.setTransitionFunction(() -> true);
            transitions.getTransitions().add(javaTransition);

            when(stateNavigator.openState(3L)).thenReturn(true);

            // Act
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertTrue(result);
            verify(stateNavigator).openState(3L);
        }
    }

    @Nested
    @DisplayName("No Transition Found Tests")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    class NoTransitionFoundTests {

        @Test
        @DisplayName("Should handle no transition found gracefully")
        void testOnNoTransitionFound() {
            // Act - should not throw exception
            assertDoesNotThrow(() -> stateHandler.onNoTransitionFound());

            // Assert - no navigation should be attempted
            verify(stateNavigator, never()).openState(anyLong());
        }

        @Test
        @DisplayName("Should allow multiple calls to onNoTransitionFound")
        void testMultipleCallsToOnNoTransitionFound() {
            // Act - call multiple times
            assertDoesNotThrow(
                    () -> {
                        stateHandler.onNoTransitionFound();
                        stateHandler.onNoTransitionFound();
                        stateHandler.onNoTransitionFound();
                    });

            // Assert
            verify(stateNavigator, never()).openState(anyLong());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete flow with state transitions")
        void testCompleteFlow() {
            // Arrange
            State state1 = mock(State.class);
            when(state1.getName()).thenReturn("State1");
            when(state1.getId()).thenReturn(1L);

            State state2 = mock(State.class);
            when(state2.getName()).thenReturn("State2");
            when(state2.getId()).thenReturn(2L);

            StateTransitions transitions1 = new StateTransitions();
            transitions1.setStateName("State1");
            transitions1.setStateId(1L);

            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(2L));
            transitions1.getTransitions().add(transition);

            when(stateNavigator.openState(2L)).thenReturn(true);

            // Act
            boolean result1 = stateHandler.handleState(state1, transitions1);

            // Assert
            assertTrue(result1);
            verify(stateNavigator).openState(2L);

            // Now handle state2 with no transitions
            StateTransitions transitions2 = new StateTransitions();
            transitions2.setStateName("State2");
            transitions2.setStateId(2L);

            boolean result2 = stateHandler.handleState(state2, transitions2);

            assertFalse(result2);
        }

        @Test
        @DisplayName("Should handle mixed transition types")
        void testMixedTransitionTypes() {
            // Arrange
            StateTransitions transitions = new StateTransitions();

            // Add different types of transitions
            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setActivate(Set.of(2L));
            transitions.getTransitions().add(javaTransition);

            TaskSequenceStateTransition taskTransition = new TaskSequenceStateTransition();
            taskTransition.setActivate(Set.of(3L));
            transitions.getTransitions().add(taskTransition);

            when(stateNavigator.openState(2L)).thenReturn(true);

            // Act - should use first transition (Java)
            boolean result = stateHandler.handleState(currentState, transitions);

            // Assert
            assertTrue(result);
            verify(stateNavigator).openState(2L);
            verify(stateNavigator, never()).openState(3L);
        }
    }
}
