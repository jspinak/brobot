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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
// Removed old logging import: // Removed old logging import: import io.github.jspinak.brobot.tools.logging.MessageFormatter;

@DisplayName("TransitionExecutor Tests")
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

            // Setup successful transition scenario
            setupSuccessfulTransition(fromState, toState);

                // Execute
                boolean result = executor.go(fromState, toState);

                // Verify
                assertTrue(result);
                consoleMock.verify(
                        () ->
            }
        }

        @Test
        @DisplayName("Failed transition logs failure message")
        public void testGo_Failure() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            // Setup failed transition scenario - source state not active
            when(stateMemory.isActive(fromState)).thenReturn(false);
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());

                // Execute
                boolean result = executor.go(fromState, toState);

                // Verify
                assertFalse(result);
                consoleMock.verify(
                        () ->
            }
        }
    }

    @Nested
    @DisplayName("Transition Validation")
    class TransitionValidation {

        @Test
        @DisplayName("Transition fails when source state not active")
        public void testDoTransitions_SourceNotActive() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            when(stateMemory.isActive(fromState)).thenReturn(false);

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertFalse(result);
            verify(stateMemory).isActive(fromState);
            verify(transitionFetcher, never()).getTransitionToActivate(any(), any());
        }

        @Test
        @DisplayName("Transition fails when no transition exists")
        public void testDoTransitions_NoTransition() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            when(stateMemory.isActive(fromState)).thenReturn(true);
            when(transitionFetcher.getTransitionToActivate(fromState, toState))
                    .thenReturn(Optional.empty());

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertFalse(result);
            verify(transitionFetcher).getTransitionToActivate(fromState, toState);
        }
    }

    @Nested
    @DisplayName("State Activation and Cascading")
    class StateActivationAndCascading {

        @Test
        @DisplayName("Simple transition activates target state")
        public void testSimpleTransition() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            setupBasicTransition(fromState, toState);
            when(mockTransition.getCascadeAction()).thenReturn(StateTransition.CascadeAction.NONE);
            when(mockCondition.getAsBoolean()).thenReturn(true);

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertTrue(result);
            verify(stateMemory).isActive(fromState);
            verify(mockCondition).getAsBoolean();
        }

        @Test
        @DisplayName("Cascading transition activates additional states")
        public void testCascadingTransition() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Long cascadeState = 3L;

            setupBasicTransition(fromState, toState);
            when(mockTransition.getCascadeAction())
                    .thenReturn(StateTransition.CascadeAction.ACTIVATE);
            when(mockTransition.getActivate()).thenReturn(Set.of(cascadeState));
            when(mockCondition.getAsBoolean()).thenReturn(true);

            // Stub cascade transition
            StateTransition cascadeTransition = mock(StateTransition.class);
            when(transitionFetcher.getTransitionToActivate(toState, cascadeState))
                    .thenReturn(Optional.of(cascadeTransition));
            BooleanSupplier cascadeCondition = mock(BooleanSupplier.class);
            when(cascadeCondition.getAsBoolean()).thenReturn(true);
            when(transitionConditionPackager.packageTransition(
                            cascadeTransition, toState, cascadeState))
                    .thenReturn(cascadeCondition);

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertTrue(result);
            verify(transitionFetcher).getTransitionToActivate(toState, cascadeState);
        }

        @Test
        @DisplayName("Already active states are not re-activated")
        public void testAlreadyActiveStateNotReactivated() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            setupBasicTransition(fromState, toState);
            when(mockTransition.getCascadeAction())
                    .thenReturn(StateTransition.CascadeAction.ACTIVATE);
            when(mockTransition.getActivate())
                    .thenReturn(Set.of(toState)); // Try to activate already active state
            when(stateMemory.isActive(toState)).thenReturn(true);
            when(mockCondition.getAsBoolean()).thenReturn(true);

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify - Should not attempt to re-activate
            assertTrue(result);
            verify(transitionFetcher, times(1)).getTransitionToActivate(fromState, toState);
            // Should not fetch transition for already active state
        }
    }

    @Nested
    @DisplayName("Exit State Handling")
    class ExitStateHandling {

        @Test
        @DisplayName("Exit states are deactivated after transition")
        public void testExitStatesDeactivated() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Long exitState = 3L;

            setupBasicTransition(fromState, toState);
            when(mockTransition.getExit()).thenReturn(Set.of(exitState));
            when(mockCondition.getAsBoolean()).thenReturn(true);
            when(stateService.get(exitState)).thenReturn(Optional.of(mockState));

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertTrue(result);
            verify(stateMemory).setActiveState(exitState, false);
        }

        @Test
        @DisplayName("Non-existent exit states are handled gracefully")
        public void testNonExistentExitState() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Long exitState = 99L;

            setupBasicTransition(fromState, toState);
            when(mockTransition.getExit()).thenReturn(Set.of(exitState));
            when(mockCondition.getAsBoolean()).thenReturn(true);
            when(stateService.get(exitState)).thenReturn(Optional.empty());

            // Execute - Should not throw
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertTrue(result);
            verify(stateService).get(exitState);
            verify(stateMemory, never()).setActiveState(eq(exitState), anyBoolean());
        }
    }

    @Nested
    @DisplayName("Hidden State Management")
    class HiddenStateManagement {

        @Test
        @DisplayName("Hidden states are updated after transition")
        public void testHiddenStatesUpdated() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            setupBasicTransition(fromState, toState);
            when(mockCondition.getAsBoolean()).thenReturn(true);

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertTrue(result);
            verify(stateVisibilityManager).setHiddenStates(toState);
        }
    }

    @Nested
    @DisplayName("Probability Updates")
    class ProbabilityUpdates {

        @Test
        @DisplayName("State probabilities updated after successful transition")
        public void testProbabilityUpdates() {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;

            setupBasicTransition(fromState, toState);
            when(mockCondition.getAsBoolean()).thenReturn(true);
            when(stateService.get(toState)).thenReturn(Optional.of(mockState));

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertTrue(result);
            verify(mockState).setProbabilityExists(100);
        }
    }

    @Nested
    @DisplayName("Complex Transition Scenarios")
    class ComplexTransitionScenarios {

        @Test
        @DisplayName("Multi-level cascading transitions")
        public void testMultiLevelCascading() {
            // Setup - A->B triggers B->C
            Long stateA = 1L;
            Long stateB = 2L;
            Long stateC = 3L;

            setupBasicTransition(stateA, stateB);
            when(mockTransition.getCascadeAction())
                    .thenReturn(StateTransition.CascadeAction.ACTIVATE);
            when(mockTransition.getActivate()).thenReturn(Set.of(stateC));
            when(mockCondition.getAsBoolean()).thenReturn(true);

            // Setup cascade
            StateTransition cascadeTransition = mock(StateTransition.class);
            when(transitionFetcher.getTransitionToActivate(stateB, stateC))
                    .thenReturn(Optional.of(cascadeTransition));
            when(cascadeTransition.getCascadeAction())
                    .thenReturn(StateTransition.CascadeAction.NONE);

            BooleanSupplier cascadeCondition = mock(BooleanSupplier.class);
            when(cascadeCondition.getAsBoolean()).thenReturn(true);
            when(transitionConditionPackager.packageTransition(cascadeTransition, stateB, stateC))
                    .thenReturn(cascadeCondition);

            // Execute
            boolean result = executor.doTransitions(stateA, stateB);

            // Verify
            assertTrue(result);
            verify(transitionFetcher).getTransitionToActivate(stateB, stateC);
            verify(cascadeCondition).getAsBoolean();
        }

        @ParameterizedTest
        @CsvSource({
            "true, true, true", // Both succeed
            "true, false, true", // Core succeeds, cascade fails
            "false, true, false", // Core fails
            "false, false, false" // Both fail
        })
        @DisplayName("Cascade failure doesn't affect core transition")
        public void testCascadeFailureHandling(
                boolean coreSuccess, boolean cascadeSuccess, boolean expectedResult) {
            // Setup
            Long fromState = 1L;
            Long toState = 2L;
            Long cascadeState = 3L;

            setupBasicTransition(fromState, toState);
            when(mockTransition.getCascadeAction())
                    .thenReturn(StateTransition.CascadeAction.ACTIVATE);
            when(mockTransition.getActivate()).thenReturn(Set.of(cascadeState));
            when(mockCondition.getAsBoolean()).thenReturn(coreSuccess);

            if (coreSuccess) {
                StateTransition cascadeTransition = mock(StateTransition.class);
                when(transitionFetcher.getTransitionToActivate(toState, cascadeState))
                        .thenReturn(Optional.of(cascadeTransition));

                BooleanSupplier cascadeCondition = mock(BooleanSupplier.class);
                when(cascadeCondition.getAsBoolean()).thenReturn(cascadeSuccess);
                when(transitionConditionPackager.packageTransition(
                                cascadeTransition, toState, cascadeState))
                        .thenReturn(cascadeCondition);
            }

            // Execute
            boolean result = executor.doTransitions(fromState, toState);

            // Verify
            assertEquals(expectedResult, result);
        }
    }

    // Helper method
    private void setupBasicTransition(Long fromState, Long toState) {
        when(stateMemory.isActive(fromState)).thenReturn(true);
        when(transitionFetcher.getTransitionToActivate(fromState, toState))
                .thenReturn(Optional.of(mockTransition));
        when(transitionConditionPackager.packageTransition(mockTransition, fromState, toState))
                .thenReturn(mockCondition);
        when(mockTransition.getExit()).thenReturn(new HashSet<>());
        when(mockTransition.getActivate()).thenReturn(new HashSet<>());
    }
}
