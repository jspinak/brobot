package io.github.jspinak.brobot.runner.execution;

import lombok.Data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Data
class ExecutionStatusManagerTest {

    private ExecutionStatus status;
    private ExecutionStatusManager statusManager;

    @BeforeEach
    void setUp() {
        status = new ExecutionStatus();
        statusManager = new ExecutionStatusManager(status);
    }

    @Test
    void testUpdateState() {
        // Update state
        statusManager.updateState(ExecutionState.RUNNING);

        // Verify status was updated
        assertEquals(ExecutionState.RUNNING, status.getState());
    }

    @Test
    void testUpdateProgress() {
        // Update progress
        statusManager.updateProgress(0.75);

        // Verify status was updated
        assertEquals(0.75, status.getProgress());
    }

    @Test
    void testSetCurrentOperation() {
        // Update operation
        String operation = "Test Operation";
        statusManager.setCurrentOperation(operation);

        // Verify status was updated
        assertEquals(operation, status.getCurrentOperation());
    }

    @Test
    void testUpdateStartTime() {
        // Update start time
        Instant now = Instant.now();
        statusManager.updateStartTime(now);

        // Verify status was updated
        assertEquals(now, status.getStartTime());
    }

    @Test
    void testUpdateEndTime() {
        // Update end time
        Instant now = Instant.now();
        statusManager.updateEndTime(now);

        // Verify status was updated
        assertEquals(now, status.getEndTime());
    }

    @Test
    void testSetError() {
        // Update error
        Exception error = new RuntimeException("Test Error");
        statusManager.setError(error);

        // Verify status was updated
        assertEquals(error, status.getError());
    }

    @Test
    void testReset() {
        // Setup status with values
        statusManager.updateState(ExecutionState.RUNNING);
        statusManager.updateStartTime(Instant.now());
        statusManager.updateEndTime(Instant.now());
        statusManager.updateProgress(0.7);
        statusManager.setCurrentOperation("Test Operation");
        statusManager.setError(new RuntimeException("Test Error"));

        // Reset
        statusManager.reset();

        // Verify reset to initial state
        assertEquals(ExecutionState.IDLE, status.getState());
        assertNull(status.getStartTime());
        assertNull(status.getEndTime());
        assertEquals(0.0, status.getProgress());
        assertNull(status.getCurrentOperation());
        assertNull(status.getError());
    }

    @Test
    void testStatusConsumerNotification() {
        // Create an atomic reference to track the received status copy
        AtomicReference<ExecutionStatus> receivedStatus = new AtomicReference<>();

        // Set status consumer
        statusManager.setStatusConsumer(receivedStatus::set);

        // Update state
        statusManager.updateState(ExecutionState.RUNNING);

        // Verify consumer was notified with a copy of the status
        assertNotNull(receivedStatus.get());
        assertEquals(ExecutionState.RUNNING, receivedStatus.get().getState());

        // Verify the copy is independent from the original
        receivedStatus.get().setState(ExecutionState.PAUSED);
        assertEquals(ExecutionState.RUNNING, status.getState());
    }

    @Test
    void testMultipleStatusUpdates() {
        // Set up counter to track the number of notifications
        final int[] notificationCount = {0};

        // Set status consumer
        statusManager.setStatusConsumer(status -> notificationCount[0]++);

        // Make several updates
        statusManager.updateState(ExecutionState.STARTING);
        statusManager.updateProgress(0.1);
        statusManager.setCurrentOperation("Operation 1");
        statusManager.updateState(ExecutionState.RUNNING);
        statusManager.updateProgress(0.5);
        statusManager.setCurrentOperation("Operation 2");

        // Verify the consumer was notified for each update
        assertEquals(6, notificationCount[0]);
    }

    @Test
    void testNullConsumer() {
        // Don't set a status consumer

        // Make updates - should not throw exceptions
        statusManager.updateState(ExecutionState.RUNNING);
        statusManager.updateProgress(0.5);

        // Verify status was still updated
        assertEquals(ExecutionState.RUNNING, status.getState());
        assertEquals(0.5, status.getProgress());
    }
}