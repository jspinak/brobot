package io.github.jspinak.brobot.runner.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStatusTest {

    private ExecutionStatus status;

    @BeforeEach
    void setUp() {
        status = new ExecutionStatus();
    }

    @Test
    void testInitialState() {
        assertEquals(ExecutionState.IDLE, status.getState());
        assertEquals(0.0, status.getProgress());
        assertNull(status.getStartTime());
        assertNull(status.getEndTime());
        assertNull(status.getCurrentOperation());
        assertNull(status.getError());
    }

    @Test
    void testStateUpdates() {
        status.setState(ExecutionState.RUNNING);
        assertEquals(ExecutionState.RUNNING, status.getState());

        status.setStartTime(Instant.now());
        assertNotNull(status.getStartTime());

        status.setEndTime(Instant.now());
        assertNotNull(status.getEndTime());

        status.setProgress(0.5);
        assertEquals(0.5, status.getProgress());

        String operation = "Test Operation";
        status.setCurrentOperation(operation);
        assertEquals(operation, status.getCurrentOperation());

        Exception error = new RuntimeException("Test Error");
        status.setError(error);
        assertEquals(error, status.getError());
    }

    @Test
    void testGetDuration() {
        // When no start time is set
        assertNull(status.getDuration());

        // When start time is set but no end time
        Instant start = Instant.now().minus(Duration.ofSeconds(10));
        status.setStartTime(start);
        Duration duration = status.getDuration();
        assertNotNull(duration);
        assertTrue(duration.getSeconds() >= 10);

        // When both start and end times are set
        Instant end = start.plus(Duration.ofSeconds(5));
        status.setEndTime(end);
        assertEquals(Duration.ofSeconds(5), status.getDuration());
    }

    @Test
    void testIsFinished() {
        // Initially not finished
        assertFalse(status.isFinished());

        // Test each terminal state
        status.setState(ExecutionState.COMPLETED);
        assertTrue(status.isFinished());

        status.setState(ExecutionState.ERROR);
        assertTrue(status.isFinished());

        status.setState(ExecutionState.TIMEOUT);
        assertTrue(status.isFinished());

        status.setState(ExecutionState.STOPPED);
        assertTrue(status.isFinished());

        // Non-terminal states
        status.setState(ExecutionState.IDLE);
        assertFalse(status.isFinished());

        status.setState(ExecutionState.STARTING);
        assertFalse(status.isFinished());

        status.setState(ExecutionState.RUNNING);
        assertFalse(status.isFinished());

        status.setState(ExecutionState.PAUSED);
        assertFalse(status.isFinished());

        status.setState(ExecutionState.STOPPING);
        assertFalse(status.isFinished());
    }

    @Test
    void testGetStatusMessage() {
        // Basic state message
        status.setState(ExecutionState.IDLE);
        assertEquals("Idle", status.getStatusMessage());

        // With current operation
        status.setState(ExecutionState.RUNNING);
        status.setCurrentOperation("Test Operation");
        assertTrue(status.getStatusMessage().contains("Running"));
        assertTrue(status.getStatusMessage().contains("Test Operation"));

        // With error
        status.setState(ExecutionState.ERROR);
        Exception error = new RuntimeException("Test Error");
        status.setError(error);
        assertTrue(status.getStatusMessage().contains("Failed with error"));
        assertTrue(status.getStatusMessage().contains("Test Error"));

        // With running time
        Instant now = Instant.now();
        status.setState(ExecutionState.RUNNING);
        status.setStartTime(now.minus(Duration.ofMinutes(5)));
        String message = status.getStatusMessage();
        assertTrue(message.contains("Running for"));
        assertTrue(message.contains("5 min"));
    }

    @Test
    void testCopy() {
        // Setup original status
        status.setState(ExecutionState.RUNNING);
        Instant start = Instant.now();
        status.setStartTime(start);
        status.setProgress(0.7);
        status.setCurrentOperation("Test Operation");

        // Create copy
        ExecutionStatus copy = status.copy();

        // Verify copy has same values
        assertEquals(ExecutionState.RUNNING, copy.getState());
        assertEquals(start, copy.getStartTime());
        assertEquals(0.7, copy.getProgress());
        assertEquals("Test Operation", copy.getCurrentOperation());

        // Verify copy is independent
        status.setState(ExecutionState.PAUSED);
        assertNotEquals(status.getState(), copy.getState());
    }

    @Test
    void testReset() {
        // Setup status with values
        status.setState(ExecutionState.RUNNING);
        status.setStartTime(Instant.now());
        status.setEndTime(Instant.now());
        status.setProgress(0.7);
        status.setCurrentOperation("Test Operation");
        status.setError(new RuntimeException("Test Error"));

        // Reset
        status.reset();

        // Verify reset to initial state
        assertEquals(ExecutionState.IDLE, status.getState());
        assertNull(status.getStartTime());
        assertNull(status.getEndTime());
        assertEquals(0.0, status.getProgress());
        assertNull(status.getCurrentOperation());
        assertNull(status.getError());
    }

    @Test
    void testToString() {
        status.setState(ExecutionState.RUNNING);
        status.setProgress(0.5);
        status.setCurrentOperation("Test Operation");

        String result = status.toString();
        assertTrue(result.contains("RUNNING"));
        assertTrue(result.contains("50,0%"));
        assertTrue(result.contains("Test Operation"));
    }
}