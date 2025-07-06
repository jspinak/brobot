package io.github.jspinak.brobot.runner.execution;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExecutionStateTest {

    @Test
    void testStateDescriptions() {
        assertEquals("Idle", ExecutionState.IDLE.getDescription());
        assertEquals("Starting execution", ExecutionState.STARTING.getDescription());
        assertEquals("Running", ExecutionState.RUNNING.getDescription());
        assertEquals("Paused", ExecutionState.PAUSED.getDescription());
        assertEquals("Stopping", ExecutionState.STOPPING.getDescription());
        assertEquals("Completed successfully", ExecutionState.COMPLETED.getDescription());
        assertEquals("Failed with error", ExecutionState.ERROR.getDescription());
        assertEquals("Timed out", ExecutionState.TIMEOUT.getDescription());
        assertEquals("Stopped", ExecutionState.STOPPED.getDescription());
    }

    @Test
    void testIsActive() {
        assertTrue(ExecutionState.STARTING.isActive());
        assertTrue(ExecutionState.RUNNING.isActive());
        assertTrue(ExecutionState.PAUSED.isActive());
        assertTrue(ExecutionState.STOPPING.isActive());

        assertFalse(ExecutionState.IDLE.isActive());
        assertFalse(ExecutionState.COMPLETED.isActive());
        assertFalse(ExecutionState.ERROR.isActive());
        assertFalse(ExecutionState.TIMEOUT.isActive());
        assertFalse(ExecutionState.STOPPED.isActive());
    }

    @Test
    void testIsSuccessful() {
        assertTrue(ExecutionState.COMPLETED.isSuccessful());

        assertFalse(ExecutionState.IDLE.isSuccessful());
        assertFalse(ExecutionState.STARTING.isSuccessful());
        assertFalse(ExecutionState.RUNNING.isSuccessful());
        assertFalse(ExecutionState.PAUSED.isSuccessful());
        assertFalse(ExecutionState.STOPPING.isSuccessful());
        assertFalse(ExecutionState.ERROR.isSuccessful());
        assertFalse(ExecutionState.TIMEOUT.isSuccessful());
        assertFalse(ExecutionState.STOPPED.isSuccessful());
    }

    @Test
    void testIsFailed() {
        assertTrue(ExecutionState.ERROR.isFailed());
        assertTrue(ExecutionState.TIMEOUT.isFailed());

        assertFalse(ExecutionState.IDLE.isFailed());
        assertFalse(ExecutionState.STARTING.isFailed());
        assertFalse(ExecutionState.RUNNING.isFailed());
        assertFalse(ExecutionState.PAUSED.isFailed());
        assertFalse(ExecutionState.STOPPING.isFailed());
        assertFalse(ExecutionState.COMPLETED.isFailed());
        assertFalse(ExecutionState.STOPPED.isFailed());
    }

    @Test
    void testIsTerminated() {
        assertTrue(ExecutionState.STOPPED.isTerminated());

        assertFalse(ExecutionState.IDLE.isTerminated());
        assertFalse(ExecutionState.STARTING.isTerminated());
        assertFalse(ExecutionState.RUNNING.isTerminated());
        assertFalse(ExecutionState.PAUSED.isTerminated());
        assertFalse(ExecutionState.STOPPING.isTerminated());
        assertFalse(ExecutionState.COMPLETED.isTerminated());
        assertFalse(ExecutionState.ERROR.isTerminated());
        assertFalse(ExecutionState.TIMEOUT.isTerminated());
    }
}