package io.github.jspinak.brobot.runner.ui.automation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;

@ExtendWith(MockitoExtension.class)
class RunnerStatusServiceTest {

    @Mock private AutomationOrchestrator automationOrchestrator;

    private RunnerStatusService service;

    @BeforeEach
    void setUp() {
        service = new RunnerStatusService(automationOrchestrator);
    }

    @Test
    void testGetCurrentStatus() {
        // Given
        ExecutionStatus expectedStatus = createStatus(ExecutionState.RUNNING, 0.5, "Processing");
        when(automationOrchestrator.getExecutionStatus()).thenReturn(expectedStatus);

        // When
        ExecutionStatus actualStatus = service.getCurrentStatus();

        // Then
        assertEquals(expectedStatus, actualStatus);
    }

    private ExecutionStatus createStatus(ExecutionState state, double progress, String operation) {
        ExecutionStatus status = new ExecutionStatus();
        status.setState(state);
        status.setProgress(progress);
        status.setCurrentOperation(operation);
        return status;
    }

    @Test
    void testGetCurrentState() {
        // Given
        ExecutionStatus status = createStatus(ExecutionState.PAUSED, 0.3, "Paused");
        when(automationOrchestrator.getExecutionStatus()).thenReturn(status);

        // When
        ExecutionState state = service.getCurrentState();

        // Then
        assertEquals(ExecutionState.PAUSED, state);
    }

    @Test
    void testGetCurrentStateWhenStatusNull() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(null);

        // When
        ExecutionState state = service.getCurrentState();

        // Then
        assertEquals(ExecutionState.IDLE, state);
    }

    @Test
    void testIsRunning() {
        // Test RUNNING state
        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.RUNNING, 0.5, "Running"));
        assertTrue(service.isRunning());

        // Test STARTING state
        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.STARTING, 0.0, "Starting"));
        assertTrue(service.isRunning());

        // Test PAUSED state
        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.PAUSED, 0.5, "Paused"));
        assertFalse(service.isRunning());
    }

    @Test
    void testIsPaused() {
        // Test PAUSED state
        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.PAUSED, 0.5, "Paused"));
        assertTrue(service.isPaused());

        // Test RUNNING state
        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.RUNNING, 0.5, "Running"));
        assertFalse(service.isPaused());
    }

    @Test
    void testIsActive() {
        // Test active states
        ExecutionState[] activeStates = {
            ExecutionState.RUNNING,
            ExecutionState.PAUSED,
            ExecutionState.STARTING,
            ExecutionState.STOPPING
        };

        for (ExecutionState state : activeStates) {
            when(automationOrchestrator.getExecutionStatus())
                    .thenReturn(createStatus(state, 0.5, "Test"));
            assertTrue(service.isActive(), "State " + state + " should be active");
        }

        // Test inactive states
        ExecutionState[] inactiveStates = {
            ExecutionState.IDLE,
            ExecutionState.COMPLETED,
            ExecutionState.FAILED,
            ExecutionState.ERROR
        };

        for (ExecutionState state : inactiveStates) {
            when(automationOrchestrator.getExecutionStatus())
                    .thenReturn(createStatus(state, 0.5, "Test"));
            assertFalse(service.isActive(), "State " + state + " should not be active");
        }
    }

    @Test
    void testStatusMonitoring() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger updateCount = new AtomicInteger(0);
        AtomicReference<ExecutionStatus> lastStatus = new AtomicReference<>();

        service.addStatusListener(
                status -> {
                    updateCount.incrementAndGet();
                    lastStatus.set(status);
                    latch.countDown();
                });

        // Configure to update frequently
        service.setConfiguration(
                RunnerStatusService.StatusConfiguration.builder()
                        .updateIntervalMs(50)
                        .notifyOnlyOnChange(false)
                        .build());

        ExecutionStatus status1 = createStatus(ExecutionState.RUNNING, 0.3, "Step 1");
        ExecutionStatus status2 = createStatus(ExecutionState.RUNNING, 0.6, "Step 2");

        when(automationOrchestrator.getExecutionStatus()).thenReturn(status1).thenReturn(status2);

        // When
        service.startMonitoring();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        assertTrue(updateCount.get() >= 2);
        assertNotNull(lastStatus.get());
    }

    @Test
    void testNotifyOnlyOnChange() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger updateCount = new AtomicInteger(0);

        service.addStatusListener(
                status -> {
                    updateCount.incrementAndGet();
                    latch.countDown();
                });

        // Configure to notify only on change
        service.setConfiguration(
                RunnerStatusService.StatusConfiguration.builder()
                        .updateIntervalMs(50)
                        .notifyOnlyOnChange(true)
                        .build());

        // Return same status multiple times
        ExecutionStatus unchangedStatus = createStatus(ExecutionState.RUNNING, 0.5, "Same");
        when(automationOrchestrator.getExecutionStatus()).thenReturn(unchangedStatus);

        // When
        service.startMonitoring();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        Thread.sleep(200); // Allow time for multiple checks
        service.stopMonitoring();

        // Should only notify once since status didn't change
        assertEquals(1, updateCount.get());
    }

    @Test
    void testStateChangeNotification() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ExecutionState> oldStateRef = new AtomicReference<>();
        AtomicReference<ExecutionState> newStateRef = new AtomicReference<>();

        service.addStateChangeListener(
                (oldState, newState) -> {
                    oldStateRef.set(oldState);
                    newStateRef.set(newState);
                    latch.countDown();
                });

        // Configure
        service.setConfiguration(
                RunnerStatusService.StatusConfiguration.builder()
                        .updateIntervalMs(50)
                        .notifyOnlyOnChange(true)
                        .build());

        ExecutionStatus status1 = createStatus(ExecutionState.RUNNING, 0.3, "Running");
        ExecutionStatus status2 = createStatus(ExecutionState.PAUSED, 0.3, "Paused");

        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(status1)
                .thenReturn(status1) // Same status
                .thenReturn(status2); // Changed state

        // When
        service.startMonitoring();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        assertEquals(ExecutionState.RUNNING, oldStateRef.get());
        assertEquals(ExecutionState.PAUSED, newStateRef.get());
    }

    @Test
    void testForceUpdate() {
        // Given
        AtomicReference<ExecutionStatus> receivedStatus = new AtomicReference<>();
        service.addStatusListener(receivedStatus::set);

        ExecutionStatus expectedStatus = createStatus(ExecutionState.RUNNING, 0.7, "Forced");
        when(automationOrchestrator.getExecutionStatus()).thenReturn(expectedStatus);

        // When
        service.forceUpdate();

        // Then
        assertEquals(expectedStatus, receivedStatus.get());
    }

    @Test
    void testRemoveListeners() {
        // Given
        AtomicInteger statusUpdateCount = new AtomicInteger(0);
        AtomicInteger stateChangeCount = new AtomicInteger(0);

        RunnerStatusService.StatusUpdateListener statusListener =
                status -> statusUpdateCount.incrementAndGet();
        RunnerStatusService.StateChangeListener stateListener =
                (old, newer) -> stateChangeCount.incrementAndGet();

        service.addStatusListener(statusListener);
        service.addStateChangeListener(stateListener);

        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.RUNNING, 0.5, "Test"));

        // Verify listeners work
        service.forceUpdate();
        assertEquals(1, statusUpdateCount.get());

        // When - remove listeners
        service.removeStatusListener(statusListener);
        service.removeStateChangeListener(stateListener);

        // Then - listeners should not be called
        service.forceUpdate();
        assertEquals(1, statusUpdateCount.get()); // Should still be 1
    }

    @Test
    void testGetStatusSummary() {
        // Test with status
        ExecutionStatus status = createStatus(ExecutionState.RUNNING, 0.75, "Processing item 5");
        when(automationOrchestrator.getExecutionStatus()).thenReturn(status);

        String summary = service.getStatusSummary();
        assertTrue(summary.contains("Running"));
        assertTrue(summary.contains("75.0%"));
        assertTrue(summary.contains("Processing item 5"));

        // Test with null status
        when(automationOrchestrator.getExecutionStatus()).thenReturn(null);
        summary = service.getStatusSummary();
        assertEquals("No status available", summary);

        // Test with null operation
        status = createStatus(ExecutionState.IDLE, 0.0, null);
        when(automationOrchestrator.getExecutionStatus()).thenReturn(status);
        summary = service.getStatusSummary();
        assertTrue(summary.contains("None"));
    }

    @Test
    void testStartStopMonitoring() throws InterruptedException {
        // Given
        CountDownLatch startLatch = new CountDownLatch(1);
        service.addStatusListener(status -> startLatch.countDown());

        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.IDLE, 0.0, "Idle"));

        // Start monitoring
        service.startMonitoring();
        assertTrue(startLatch.await(1, TimeUnit.SECONDS));

        // Try to start again (should not create another thread)
        service.startMonitoring();

        // Stop monitoring
        service.stopMonitoring();
        Thread.sleep(100);

        // Verify no more updates after stopping
        AtomicInteger updateCount = new AtomicInteger(0);
        service.addStatusListener(status -> updateCount.incrementAndGet());
        Thread.sleep(200);
        assertEquals(0, updateCount.get());
    }

    @Test
    void testMonitoringErrorHandling() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger updateCount = new AtomicInteger(0);

        service.addStatusListener(
                status -> {
                    updateCount.incrementAndGet();
                    latch.countDown();
                    if (updateCount.get() == 1) {
                        throw new RuntimeException("Test exception");
                    }
                });

        service.setConfiguration(
                RunnerStatusService.StatusConfiguration.builder().updateIntervalMs(50).build());

        when(automationOrchestrator.getExecutionStatus())
                .thenReturn(createStatus(ExecutionState.RUNNING, 0.5, "Test"));

        // When
        service.startMonitoring();

        // Then - monitoring should continue despite exception
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        assertTrue(updateCount.get() >= 2);
    }
}
