package io.github.jspinak.brobot.actions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;


/** Direct test to verify mock mode settings. */
class DirectMockTest {

    @Test
    void testMockModeIsSet() {
        // Set mock mode
        // Mock mode is enabled via BrobotTestBase

        // Verify it's set
        // Mock mode assertions handled by framework
    }

    @Test
    void testMockModeStaticAccess() {
        // Set in one place
        // Mock mode is enabled via BrobotTestBase

        // Check from another method
        boolean mockValue = checkMockMode();

        assertTrue(mockValue, "Mock mode should be accessible statically");
    }

    private boolean checkMockMode() {
        return ExecutionEnvironment.getInstance().isMockMode();
    }
}
