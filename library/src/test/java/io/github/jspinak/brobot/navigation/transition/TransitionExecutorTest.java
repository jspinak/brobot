package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.statemanagement.StateVisibilityManager;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.MessageFormatter;

@DisplayName("TransitionExecutor Tests")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class TransitionExecutorTest extends BrobotTestBase {

    @Mock private StateTransitionService stateTransitionService;

    @Mock private StateTransitionsJointTable stateTransitionsJointTable;

    @Mock private StateVisibilityManager stateVisibilityManager;

    @Mock private StateMemory stateMemory;

    @Mock private StateService stateService;

    @Mock private TransitionFetcher transitionFetcher;

    @Mock private TransitionConditionPackager transitionConditionPackager;

    @Mock private StateTransition mockTransition;

    @Mock private State mockState;

    @Mock private BooleanSupplier mockCondition;

    @Mock private StateTransitions mockStateTransitions;

    private TransitionExecutor executor;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        executor =
                new TransitionExecutor(
                        stateTransitionService,
                        stateTransitionsJointTable,
                        stateVisibilityManager,
                        stateMemory,
                        stateService,
                        transitionFetcher,
                        transitionConditionPackager);
    }

    @Nested
    @DisplayName("Basic Transition Execution")
    class BasicTransitionExecution {

        @Test
        @DisplayName("Successful transition logs success message")
        public void testGo_Success() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            // Setup successful transition
            setupSuccessfulTransition(fromState, toState);

            try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
                // Execute
                boolean result = executor.go(fromState, toState);

                // Verify
                assertTrue(result);
                consoleMock.verify(
                        () ->
                                ConsoleReporter.format(
                                        MessageFormatter.check
                                                + " Transition %s->%s successful. \n",
                                        fromState,
                                        toState));
            }
        }

        @Test
        @DisplayName("Failed transition logs failure message")
        public void testGo_Failure() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            // Setup failed transition - source state not active
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());

            try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
                // Execute
                boolean result = executor.go(fromState, toState);

                // Verify
                assertFalse(result);
                consoleMock.verify(
                        () ->
                                ConsoleReporter.format(
                                        MessageFormatter.fail
                                                + " Transition %s->%s not successful. \n",
                                        fromState,
                                        toState));
            }
        }
    }

    @Nested
    @DisplayName("Transition Validation")
    class TransitionValidation {

        @Test
        @DisplayName("Transition fails when source state not active")
        public void testGo_SourceNotActive() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertFalse(result);
            verify(stateMemory, atLeastOnce())
                    .getActiveStates(); // Allow multiple calls for debug output
            verify(transitionFetcher, never()).getTransitions(any(), any());
        }

        @Test
        @DisplayName("Transition fails when no transition exists")
        public void testGo_NoTransition() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            when(stateMemory.getActiveStates()).thenReturn(Set.of(fromState));
            when(transitionFetcher.getTransitions(fromState, toState)).thenReturn(Optional.empty());

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertFalse(result);
            verify(transitionFetcher).getTransitions(fromState, toState);
        }

        @Test
        @DisplayName("Transition fails when FromTransition returns false")
        public void testGo_FromTransitionFails() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            TransitionFetcher mockFetcher = mock(TransitionFetcher.class);
            BooleanSupplier fromTransition = mock(BooleanSupplier.class);

            when(stateMemory.getActiveStates()).thenReturn(Set.of(fromState));
            when(transitionFetcher.getTransitions(fromState, toState))
                    .thenReturn(Optional.of(mockFetcher));
            when(mockFetcher.getFromTransitionFunction()).thenReturn(fromTransition);
            when(fromTransition.getAsBoolean()).thenReturn(false);

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertFalse(result);
            verify(fromTransition).getAsBoolean();
        }
    }

    @Nested
    @DisplayName("State Activation")
    class StateActivation {

        @Test
        @DisplayName("Simple transition activates target state")
        public void testSimpleTransition() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            setupSuccessfulTransition(fromState, toState);

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertTrue(result);
            verify(stateMemory, atLeastOnce()).getActiveStates();
        }

        @Test
        @DisplayName("Transition updates state probabilities")
        public void testProbabilityUpdate() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            setupSuccessfulTransition(fromState, toState);

            // Override the toState mock to track the call
            State targetState = mock(State.class);
            when(stateService.getState(toState)).thenReturn(Optional.of(targetState));

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify - The transition happened successfully
            assertTrue(result);
            // Note: setProbabilityToBaseProbability may be called during setupSuccessfulTransition
            // or as part of the transition, depending on implementation details
            // We verify it was called at least once
            verify(targetState, atLeastOnce()).setProbabilityToBaseProbability();
        }
    }

    @Nested
    @DisplayName("Exit State Handling")
    class ExitStateHandling {

        @Test
        @DisplayName("Exit states are deactivated")
        public void testExitStates() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Long exitState = 3L;

            setupSuccessfulTransition(fromState, toState);

            // Add exit state to transition
            TransitionFetcher mockFetcher = mock(TransitionFetcher.class);
            when(transitionFetcher.getTransitions(fromState, toState))
                    .thenReturn(Optional.of(mockFetcher));

            StateTransition fromTransition = mock(StateTransition.class);
            when(fromTransition.getExit()).thenReturn(Set.of(exitState));
            when(mockFetcher.getFromTransition()).thenReturn(fromTransition);

            BooleanSupplier fromFunction = mock(BooleanSupplier.class);
            when(fromFunction.getAsBoolean()).thenReturn(true);
            when(mockFetcher.getFromTransitionFunction()).thenReturn(fromFunction);

            StateTransitions stateTransitions = mock(StateTransitions.class);
            when(mockFetcher.getFromTransitions()).thenReturn(stateTransitions);
            when(stateTransitions.stateStaysVisible(toState)).thenReturn(true);

            when(stateMemory.getActiveStates())
                    .thenReturn(Set.of(fromState))
                    .thenReturn(Set.of(fromState, toState));

            State exitStateObj = mock(State.class);
            when(stateService.getState(exitState)).thenReturn(Optional.of(exitStateObj));

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertTrue(result);
            verify(stateMemory).removeInactiveState(exitState);
        }
    }

    @Nested
    @DisplayName("Hidden State Management")
    class HiddenStateManagement {

        @Test
        @DisplayName("Hidden states are updated after transition")
        public void testHiddenStatesUpdate() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            setupSuccessfulTransition(fromState, toState);

            // Override stateMemory to ensure state is not initially active when doTransitionTo is
            // called
            when(stateMemory.getActiveStates())
                    .thenReturn(Set.of(fromState)) // Initial check
                    .thenReturn(Set.of(fromState)) // doTransitionTo check for toState
                    .thenReturn(Set.of(fromState, toState)) // After activation
                    .thenReturn(Set.of(fromState, toState)); // Final check

            // Set up additional mocks for doTransitionTo to work
            StateTransitions toStateTransitions = mock(StateTransitions.class);
            StateTransition toTransition = mock(StateTransition.class);
            BooleanSupplier toTransitionFunction = mock(BooleanSupplier.class);

            when(stateTransitionService.getTransitions(toState))
                    .thenReturn(Optional.of(toStateTransitions));
            when(toStateTransitions.getTransitionFinish()).thenReturn(toTransition);
            when(toTransition.getActivate()).thenReturn(new HashSet<>());
            when(toTransition.getExit()).thenReturn(new HashSet<>());
            when(transitionConditionPackager.toBooleanSupplier(toTransition))
                    .thenReturn(toTransitionFunction);
            when(toTransitionFunction.getAsBoolean()).thenReturn(true);

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertTrue(result);
            // The visibility manager should be updated with the new active state
            // Using atLeastOnce because it may be called multiple times during state updates
            verify(stateVisibilityManager, atLeastOnce()).set(toState);
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Cascading transitions activate additional states")
        public void testCascadingTransitions() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Long cascadeState = 3L;

            setupSuccessfulTransition(fromState, toState);

            // Add cascade state
            TransitionFetcher mockFetcher = mock(TransitionFetcher.class);
            when(transitionFetcher.getTransitions(fromState, toState))
                    .thenReturn(Optional.of(mockFetcher));

            StateTransition fromTransition = mock(StateTransition.class);
            when(fromTransition.getActivate()).thenReturn(Set.of(cascadeState));
            when(fromTransition.getExit()).thenReturn(new HashSet<>());
            when(mockFetcher.getFromTransition()).thenReturn(fromTransition);

            BooleanSupplier fromFunction = mock(BooleanSupplier.class);
            when(fromFunction.getAsBoolean()).thenReturn(true);
            when(mockFetcher.getFromTransitionFunction()).thenReturn(fromFunction);

            StateTransitions stateTransitions = mock(StateTransitions.class);
            when(mockFetcher.getFromTransitions()).thenReturn(stateTransitions);
            when(stateTransitions.stateStaysVisible(toState)).thenReturn(true);

            when(stateMemory.getActiveStates())
                    .thenReturn(Set.of(fromState))
                    .thenReturn(Set.of(fromState, toState, cascadeState));

            State cascadeStateObj = mock(State.class);
            when(stateService.getState(cascadeState)).thenReturn(Optional.of(cascadeStateObj));

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertTrue(result);
            verify(cascadeStateObj).setProbabilityToBaseProbability();
        }

        @Test
        @DisplayName("Source state exits when target doesn't stay visible")
        public void testSourceStateExit() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            TransitionFetcher mockFetcher = mock(TransitionFetcher.class);
            when(transitionFetcher.getTransitions(fromState, toState))
                    .thenReturn(Optional.of(mockFetcher));

            StateTransition fromTransition = mock(StateTransition.class);
            when(fromTransition.getExit()).thenReturn(new HashSet<>());
            when(fromTransition.getActivate()).thenReturn(new HashSet<>());
            when(mockFetcher.getFromTransition()).thenReturn(fromTransition);

            BooleanSupplier fromFunction = mock(BooleanSupplier.class);
            when(fromFunction.getAsBoolean()).thenReturn(true);
            when(mockFetcher.getFromTransitionFunction()).thenReturn(fromFunction);

            StateTransitions stateTransitions = mock(StateTransitions.class);
            when(mockFetcher.getFromTransitions()).thenReturn(stateTransitions);
            when(stateTransitions.stateStaysVisible(toState))
                    .thenReturn(false); // Source should exit

            when(stateMemory.getActiveStates())
                    .thenReturn(Set.of(fromState))
                    .thenReturn(Set.of(fromState, toState))
                    .thenReturn(Set.of(toState)); // After removal

            State fromStateObj = mock(State.class);
            when(fromStateObj.getHiddenStateIds()).thenReturn(new HashSet<>()); // No hidden states
            when(stateService.getState(fromState)).thenReturn(Optional.of(fromStateObj));
            when(mockFetcher.getFromState()).thenReturn(fromStateObj); // Set up getFromState

            State toStateObj = mock(State.class);
            when(stateService.getState(toState)).thenReturn(Optional.of(toStateObj));

            // Execute
            boolean result = executor.go(fromState, toState);

            // Verify
            assertTrue(result);
            // The state memory should remove the from state when it doesn't stay visible
            // Using atLeastOnce because it may be called during cleanup
            verify(stateMemory, atLeastOnce()).removeInactiveState(fromState);
        }
    }

    // Helper method
    private void setupSuccessfulTransition(Long fromState, Long toState) {
        TransitionFetcher mockFetcher = mock(TransitionFetcher.class);
        BooleanSupplier fromFunction = mock(BooleanSupplier.class);
        StateTransition fromTransition = mock(StateTransition.class);
        StateTransitions stateTransitions = mock(StateTransitions.class);

        when(stateMemory.getActiveStates())
                .thenReturn(Set.of(fromState))
                .thenReturn(Set.of(fromState, toState));

        when(transitionFetcher.getTransitions(fromState, toState))
                .thenReturn(Optional.of(mockFetcher));

        when(mockFetcher.getFromTransitionFunction()).thenReturn(fromFunction);
        when(fromFunction.getAsBoolean()).thenReturn(true);

        when(mockFetcher.getFromTransition()).thenReturn(fromTransition);
        when(fromTransition.getExit()).thenReturn(new HashSet<>());
        when(fromTransition.getActivate()).thenReturn(new HashSet<>());

        when(mockFetcher.getFromTransitions()).thenReturn(stateTransitions);
        when(stateTransitions.stateStaysVisible(toState)).thenReturn(true);

        State toStateObj = mock(State.class);
        when(stateService.getState(toState)).thenReturn(Optional.of(toStateObj));
    }
}
