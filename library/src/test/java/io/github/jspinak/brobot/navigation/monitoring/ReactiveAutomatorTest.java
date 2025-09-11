package io.github.jspinak.brobot.navigation.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionState;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for ReactiveAutomator class. Tests continuous state monitoring, transition
 * execution, and error handling.
 */
public class ReactiveAutomatorTest extends BrobotTestBase {

    @Mock private StateDetector mockStateDetector;

    @Mock private StateTransitionStore mockStateTransitionStore;

    @Mock private StateService mockStateService;

    @Mock private StateHandler mockStateHandler;

    @Mock private MonitoringService mockMonitoringService;

    @Mock private ExecutionController mockExecutionController;

    private ReactiveAutomator reactiveAutomator;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        reactiveAutomator =
                new ReactiveAutomator(
                        mockStateDetector,
                        mockStateTransitionStore,
                        mockStateService,
                        mockStateHandler,
                        mockMonitoringService);
    }

    @AfterEach
    public void tearDown() {
        // Ensure clean shutdown
        reactiveAutomator.stop();
    }

    @Nested
    @DisplayName("Start and Stop Tests")
    class StartStopTests {

        @Test
        @DisplayName("Should start automation correctly")
        public void testStartAutomation() {
            when(mockExecutionController.isRunning()).thenReturn(false, true);

            // Create automator with mock execution controller
            ReactiveAutomator automator =
                    new ReactiveAutomator(
                            mockStateDetector,
                            mockStateTransitionStore,
                            mockStateService,
                            mockStateHandler,
                            mockMonitoringService);

            automator.start();

            // Verify monitoring service was started with correct parameters
            ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
            ArgumentCaptor<BooleanSupplier> conditionCaptor =
                    ArgumentCaptor.forClass(BooleanSupplier.class);
            ArgumentCaptor<Long> delayCaptor = ArgumentCaptor.forClass(Long.class);

            verify(mockMonitoringService)
                    .startContinuousTask(
                            taskCaptor.capture(), conditionCaptor.capture(), delayCaptor.capture());

            assertNotNull(taskCaptor.getValue());
            assertNotNull(conditionCaptor.getValue());
            assertEquals(1L, delayCaptor.getValue());
        }

        @Test
        @DisplayName("Should not start when already running")
        public void testStartWhenAlreadyRunning() {
            // Setup automator to appear as running
            ReactiveAutomator automator = spy(reactiveAutomator);
            doReturn(true).when(automator).isRunning();

            automator.start();

            // Should not start monitoring service
            verify(mockMonitoringService, never()).startContinuousTask(any(), any(), anyLong());
        }

        @Test
        @DisplayName("Should stop automation correctly")
        public void testStopAutomation() {
            reactiveAutomator.start();
            reactiveAutomator.stop();

            verify(mockMonitoringService).stop();
            assertFalse(reactiveAutomator.isRunning());
        }
    }

    @Nested
    @DisplayName("Automation Loop Tests")
    class AutomationLoopTests {

        private Runnable capturedTask;
        private BooleanSupplier capturedCondition;

        @BeforeEach
        public void setupCapture() {
            // Capture the task that gets passed to monitoring service
            doAnswer(
                            invocation -> {
                                capturedTask = invocation.getArgument(0);
                                capturedCondition = invocation.getArgument(1);
                                return null;
                            })
                    .when(mockMonitoringService)
                    .startContinuousTask(any(), any(), anyLong());
        }

        @Test
        @DisplayName("Should process active states with transitions")
        public void testProcessActiveStatesWithTransitions() {
            // Setup test data
            Long stateId = 1L;
            State state = new State();
            state.setName("TestState");
            StateTransitions transitions = mock(StateTransitions.class);

            Set<Long> activeStates = new HashSet<>(Collections.singletonList(stateId));
            when(mockStateDetector.refreshActiveStates()).thenReturn(activeStates);
            when(mockStateTransitionStore.get(stateId)).thenReturn(Optional.of(transitions));
            when(mockStateService.getState(stateId)).thenReturn(Optional.of(state));
            when(mockStateHandler.handleState(state, transitions)).thenReturn(true);

            // Start the automator to capture the task
            reactiveAutomator.start();

            // Execute the captured task
            capturedTask.run();

            // Verify state was handled
            verify(mockStateHandler).handleState(state, transitions);
            verify(mockStateHandler, never()).onNoTransitionFound();
        }

        @Test
        @DisplayName("Should handle states without transitions")
        public void testProcessStatesWithoutTransitions() {
            Long stateId = 2L;
            Set<Long> activeStates = new HashSet<>(Collections.singletonList(stateId));

            when(mockStateDetector.refreshActiveStates()).thenReturn(activeStates);
            when(mockStateTransitionStore.get(stateId)).thenReturn(Optional.empty());

            reactiveAutomator.start();
            capturedTask.run();

            verify(mockStateHandler).onNoTransitionFound();
            verify(mockStateHandler, never()).handleState(any(), any());
        }

        @Test
        @DisplayName("Should process multiple active states")
        public void testProcessMultipleActiveStates() {
            // Setup multiple states
            Long stateId1 = 1L;
            Long stateId2 = 2L;
            Long stateId3 = 3L;

            State state1 = createState("State1");
            State state2 = createState("State2");
            StateTransitions transitions1 = mock(StateTransitions.class);
            StateTransitions transitions2 = mock(StateTransitions.class);

            Set<Long> activeStates = new HashSet<>(Arrays.asList(stateId1, stateId2, stateId3));

            when(mockStateDetector.refreshActiveStates()).thenReturn(activeStates);

            // State 1: has transitions
            when(mockStateTransitionStore.get(stateId1)).thenReturn(Optional.of(transitions1));
            when(mockStateService.getState(stateId1)).thenReturn(Optional.of(state1));

            // State 2: has transitions
            when(mockStateTransitionStore.get(stateId2)).thenReturn(Optional.of(transitions2));
            when(mockStateService.getState(stateId2)).thenReturn(Optional.of(state2));

            // State 3: no transitions
            when(mockStateTransitionStore.get(stateId3)).thenReturn(Optional.empty());

            when(mockStateHandler.handleState(any(), any())).thenReturn(true);

            reactiveAutomator.start();
            capturedTask.run();

            // Verify both states with transitions were handled
            verify(mockStateHandler).handleState(state1, transitions1);
            verify(mockStateHandler).handleState(state2, transitions2);
            verify(mockStateHandler, times(1)).onNoTransitionFound(); // For state3
        }

        @Test
        @DisplayName("Should handle state handler returning false")
        public void testStateHandlerReturnsFalse() {
            Long stateId = 1L;
            State state = createState("TestState");
            StateTransitions transitions = mock(StateTransitions.class);

            Set<Long> activeStates = Collections.singleton(stateId);

            when(mockStateDetector.refreshActiveStates()).thenReturn(activeStates);
            when(mockStateTransitionStore.get(stateId)).thenReturn(Optional.of(transitions));
            when(mockStateService.getState(stateId)).thenReturn(Optional.of(state));
            when(mockStateHandler.handleState(state, transitions)).thenReturn(false);

            reactiveAutomator.start();

            // Should not throw exception when handler returns false
            assertDoesNotThrow(() -> capturedTask.run());

            verify(mockStateHandler).handleState(state, transitions);
        }

        @Test
        @DisplayName("Should skip when state not found in service")
        public void testStateNotFoundInService() {
            Long stateId = 1L;
            StateTransitions transitions = mock(StateTransitions.class);

            Set<Long> activeStates = Collections.singleton(stateId);

            when(mockStateDetector.refreshActiveStates()).thenReturn(activeStates);
            when(mockStateTransitionStore.get(stateId)).thenReturn(Optional.of(transitions));
            when(mockStateService.getState(stateId)).thenReturn(Optional.empty());

            reactiveAutomator.start();
            capturedTask.run();

            // Should not call handler if state not found
            verify(mockStateHandler, never()).handleState(any(), any());
            verify(mockStateHandler, never()).onNoTransitionFound();
        }

        @Test
        @DisplayName("Should not execute when not running")
        public void testNoExecutionWhenNotRunning() {
            // Don't start the automator

            // Manually create and execute task
            ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);

            reactiveAutomator.start();
            verify(mockMonitoringService)
                    .startContinuousTask(taskCaptor.capture(), any(), anyLong());

            // Stop the automator
            reactiveAutomator.stop();

            // Execute captured task
            taskCaptor.getValue().run();

            // Should not call any methods when not running
            verify(mockStateDetector, never()).refreshActiveStates();
            verify(mockStateHandler, never()).handleState(any(), any());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        private Runnable capturedTask;

        @BeforeEach
        public void setupCapture() {
            doAnswer(
                            invocation -> {
                                capturedTask = invocation.getArgument(0);
                                return null;
                            })
                    .when(mockMonitoringService)
                    .startContinuousTask(any(), any(), anyLong());
        }

        @Test
        @DisplayName("Should stop on exception in automation loop")
        public void testStopOnException() {
            when(mockStateDetector.refreshActiveStates())
                    .thenThrow(new RuntimeException("Test exception"));

            reactiveAutomator.start();

            assertTrue(reactiveAutomator.isRunning());

            // Execute task which should throw exception
            capturedTask.run();

            // Should stop after exception
            verify(mockMonitoringService).stop();
        }

        @Test
        @DisplayName("Should handle exception in state handler")
        public void testExceptionInStateHandler() {
            Long stateId = 1L;
            State state = createState("TestState");
            StateTransitions transitions = mock(StateTransitions.class);

            Set<Long> activeStates = Collections.singleton(stateId);

            when(mockStateDetector.refreshActiveStates()).thenReturn(activeStates);
            when(mockStateTransitionStore.get(stateId)).thenReturn(Optional.of(transitions));
            when(mockStateService.getState(stateId)).thenReturn(Optional.of(state));
            when(mockStateHandler.handleState(state, transitions))
                    .thenThrow(new RuntimeException("Handler exception"));

            reactiveAutomator.start();

            // Execute task
            capturedTask.run();

            // Should stop on handler exception
            verify(mockMonitoringService).stop();
        }

        @Test
        @DisplayName("Should handle null active states gracefully")
        public void testNullActiveStates() {
            when(mockStateDetector.refreshActiveStates()).thenReturn(null);

            reactiveAutomator.start();

            // Should handle null without exception
            assertDoesNotThrow(() -> capturedTask.run());

            // But should stop due to NPE
            verify(mockMonitoringService).stop();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should integrate with monitoring service correctly")
        public void testMonitoringServiceIntegration() {
            AtomicInteger executionCount = new AtomicInteger(0);
            AtomicBoolean shouldContinue = new AtomicBoolean(true);

            // Setup real monitoring service behavior
            doAnswer(
                            invocation -> {
                                Runnable task = invocation.getArgument(0);
                                BooleanSupplier condition = invocation.getArgument(1);

                                // Simulate periodic execution
                                new Thread(
                                                () -> {
                                                    while (condition.getAsBoolean()
                                                            && executionCount.get() < 3) {
                                                        task.run();
                                                        executionCount.incrementAndGet();
                                                        try {
                                                            Thread.sleep(100);
                                                        } catch (InterruptedException e) {
                                                            break;
                                                        }
                                                    }
                                                })
                                        .start();

                                return null;
                            })
                    .when(mockMonitoringService)
                    .startContinuousTask(any(), any(), anyLong());

            when(mockStateDetector.refreshActiveStates()).thenReturn(Collections.emptySet());

            reactiveAutomator.start();

            // Wait for executions
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                fail("Test interrupted");
            }

            assertTrue(executionCount.get() >= 2);

            reactiveAutomator.stop();
            shouldContinue.set(false);
        }

        @Test
        @DisplayName("Should pass correct search interval to monitoring service")
        public void testSearchIntervalConfiguration() {
            reactiveAutomator.start();

            ArgumentCaptor<Long> intervalCaptor = ArgumentCaptor.forClass(Long.class);
            verify(mockMonitoringService)
                    .startContinuousTask(any(), any(), intervalCaptor.capture());

            assertEquals(1L, intervalCaptor.getValue()); // Default interval is 1 second
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should maintain proper lifecycle states")
        public void testLifecycleStates() {
            assertFalse(reactiveAutomator.isRunning());

            reactiveAutomator.start();
            assertTrue(reactiveAutomator.isRunning());

            reactiveAutomator.stop();
            assertFalse(reactiveAutomator.isRunning());
        }

        @Test
        @DisplayName("Should handle multiple start-stop cycles")
        public void testMultipleStartStopCycles() {
            // Test that we can start and stop the automator multiple times
            // This verifies the state management is working correctly

            // First cycle
            assertFalse(reactiveAutomator.isRunning());
            reactiveAutomator.start();
            assertTrue(reactiveAutomator.isRunning());

            reactiveAutomator.stop();
            assertFalse(reactiveAutomator.isRunning());

            // Second cycle - reset the automator state if needed
            reactiveAutomator.reset(); // Reset to IDLE state
            assertFalse(reactiveAutomator.isRunning());
            reactiveAutomator.start();
            assertTrue(reactiveAutomator.isRunning());

            reactiveAutomator.stop();
            assertFalse(reactiveAutomator.isRunning());

            // Third cycle
            reactiveAutomator.reset();
            assertFalse(reactiveAutomator.isRunning());
            reactiveAutomator.start();
            assertTrue(reactiveAutomator.isRunning());

            reactiveAutomator.stop();
            assertFalse(reactiveAutomator.isRunning());

            // Verify the monitoring service was called appropriately
            verify(mockMonitoringService, atLeast(3)).startContinuousTask(any(), any(), anyLong());
            verify(mockMonitoringService, atLeast(3)).stop();
        }

        @Test
        @DisplayName("Should support pause and resume")
        public void testPauseResume() {
            reactiveAutomator.start();
            assertTrue(reactiveAutomator.isRunning());

            reactiveAutomator.pause();
            assertTrue(reactiveAutomator.isPaused());

            reactiveAutomator.resume();
            assertFalse(reactiveAutomator.isPaused());
            assertTrue(reactiveAutomator.isRunning());
        }

        @Test
        @DisplayName("Should reset properly")
        public void testReset() {
            reactiveAutomator.start();
            reactiveAutomator.stop();

            reactiveAutomator.reset();

            assertEquals(ExecutionState.IDLE, reactiveAutomator.getState());
        }
    }

    // Helper methods

    private State createState(String name) {
        State state = new State();
        state.setName(name);
        return state;
    }
}
