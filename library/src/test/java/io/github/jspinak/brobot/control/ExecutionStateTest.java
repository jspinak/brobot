package io.github.jspinak.brobot.control;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStateTest {

    @Test
    void testEnumValues() {
        ExecutionState[] states = ExecutionState.values();
        assertEquals(5, states.length);
        
        // Verify all expected states exist
        assertNotNull(ExecutionState.valueOf("IDLE"));
        assertNotNull(ExecutionState.valueOf("RUNNING"));
        assertNotNull(ExecutionState.valueOf("PAUSED"));
        assertNotNull(ExecutionState.valueOf("STOPPING"));
        assertNotNull(ExecutionState.valueOf("STOPPED"));
    }

    @Test
    void testStateDescriptions() {
        assertEquals("Idle - ready to start", ExecutionState.IDLE.getDescription());
        assertEquals("Running", ExecutionState.RUNNING.getDescription());
        assertEquals("Paused - can be resumed", ExecutionState.PAUSED.getDescription());
        assertEquals("Stopping - cleanup in progress", ExecutionState.STOPPING.getDescription());
        assertEquals("Stopped", ExecutionState.STOPPED.getDescription());
    }

    @ParameterizedTest
    @CsvSource({
        "RUNNING, true",
        "PAUSED, true",
        "IDLE, false",
        "STOPPING, false",
        "STOPPED, false"
    })
    void testIsActive(ExecutionState state, boolean expectedActive) {
        assertEquals(expectedActive, state.isActive());
    }

    @ParameterizedTest
    @CsvSource({
        "STOPPING, true",
        "STOPPED, true",
        "IDLE, false",
        "RUNNING, false",
        "PAUSED, false"
    })
    void testIsTerminated(ExecutionState state, boolean expectedTerminated) {
        assertEquals(expectedTerminated, state.isTerminated());
    }

    @ParameterizedTest
    @CsvSource({
        "IDLE, true",
        "STOPPED, true",
        "RUNNING, false",
        "PAUSED, false",
        "STOPPING, false"
    })
    void testCanStart(ExecutionState state, boolean expectedCanStart) {
        assertEquals(expectedCanStart, state.canStart());
    }

    @ParameterizedTest
    @CsvSource({
        "RUNNING, true",
        "IDLE, false",
        "PAUSED, false",
        "STOPPING, false",
        "STOPPED, false"
    })
    void testCanPause(ExecutionState state, boolean expectedCanPause) {
        assertEquals(expectedCanPause, state.canPause());
    }

    @ParameterizedTest
    @CsvSource({
        "PAUSED, true",
        "IDLE, false",
        "RUNNING, false",
        "STOPPING, false",
        "STOPPED, false"
    })
    void testCanResume(ExecutionState state, boolean expectedCanResume) {
        assertEquals(expectedCanResume, state.canResume());
    }

    @ParameterizedTest
    @CsvSource({
        "RUNNING, true",
        "PAUSED, true",
        "IDLE, false",
        "STOPPING, false",
        "STOPPED, false"
    })
    void testCanStop(ExecutionState state, boolean expectedCanStop) {
        assertEquals(expectedCanStop, state.canStop());
    }

    @ParameterizedTest
    @EnumSource(ExecutionState.class)
    void testStateTransitions(ExecutionState state) {
        // Verify that state transition logic is consistent
        if (state.canStart()) {
            assertTrue(state == ExecutionState.IDLE || state == ExecutionState.STOPPED,
                    "Only IDLE and STOPPED states should allow starting");
        }
        
        if (state.canPause()) {
            assertTrue(state.isActive() && state == ExecutionState.RUNNING,
                    "Only RUNNING state should allow pausing");
        }
        
        if (state.canResume()) {
            assertTrue(state.isActive() && state == ExecutionState.PAUSED,
                    "Only PAUSED state should allow resuming");
        }
        
        if (state.canStop()) {
            assertTrue(state.isActive(),
                    "Only active states should allow stopping");
        }
    }

    @Test
    void testMutuallyExclusiveProperties() {
        for (ExecutionState state : ExecutionState.values()) {
            // A state cannot be both active and terminated
            if (state.isActive()) {
                assertFalse(state.isTerminated(),
                        state + " cannot be both active and terminated");
            }
            
            // A state cannot allow both pause and resume
            if (state.canPause()) {
                assertFalse(state.canResume(),
                        state + " cannot allow both pause and resume");
            }
        }
    }
}