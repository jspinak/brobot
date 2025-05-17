package io.github.jspinak.brobot.runner.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SafetyManagerTest {

    private SafetyManager safetyManager;

    @BeforeEach
    void setUp() {
        safetyManager = new SafetyManager();
    }

    @Test
    void testInitialState() {
        // Initially, no emergency stop is active
        assertFalse(safetyManager.isEmergencyStopActive());
    }

    @Test
    void testEmergencyStop() {
        // Request emergency stop
        safetyManager.requestEmergencyStop("Test reason");

        // Verify emergency stop is active
        assertTrue(safetyManager.isEmergencyStopActive());
    }

    @Test
    void testResetEmergencyStop() {
        // Request emergency stop
        safetyManager.requestEmergencyStop("Test reason");
        assertTrue(safetyManager.isEmergencyStopActive());

        // Reset emergency stop
        safetyManager.resetEmergencyStop();

        // Verify emergency stop is no longer active
        assertFalse(safetyManager.isEmergencyStopActive());
    }

    @Test
    void testPerformSafetyCheck_Normal() {
        // No emergency stop requested, should not throw exception
        assertDoesNotThrow(() -> safetyManager.performSafetyCheck());
    }

    @Test
    void testPerformSafetyCheck_EmergencyStop() {
        // Request emergency stop
        safetyManager.requestEmergencyStop("Test reason");

        // Safety check should throw exception
        Exception exception = assertThrows(RuntimeException.class, () -> safetyManager.performSafetyCheck());
        assertTrue(exception.getMessage().contains("Emergency stop requested"));
    }

    @Test
    void testRegisterSafetyHooks() {
        // Should not throw any exceptions
        assertDoesNotThrow(() -> safetyManager.registerSafetyHooks());
    }

    @Test
    void testMultipleEmergencyStopRequests() {
        // Request emergency stop multiple times
        safetyManager.requestEmergencyStop("Reason 1");
        safetyManager.requestEmergencyStop("Reason 2");

        // Should still be active
        assertTrue(safetyManager.isEmergencyStopActive());

        // Reset should clear all requests
        safetyManager.resetEmergencyStop();
        assertFalse(safetyManager.isEmergencyStopActive());
    }

    @Test
    void testResetWhenNoEmergencyStop() {
        // Reset when no emergency stop is active
        assertFalse(safetyManager.isEmergencyStopActive());
        safetyManager.resetEmergencyStop();

        // Should still be inactive
        assertFalse(safetyManager.isEmergencyStopActive());
    }
}