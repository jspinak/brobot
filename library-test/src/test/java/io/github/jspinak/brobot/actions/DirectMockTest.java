package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.config.FrameworkSettings;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Direct test to verify mock mode settings.
 */
class DirectMockTest {
    
    @Test
    void testMockModeIsSet() {
        // Set mock mode
        FrameworkSettings.mock = true;
        
        // Verify it's set
        assertTrue(FrameworkSettings.mock, "Mock mode should be true");
    }
    
    @Test
    void testMockModeStaticAccess() {
        // Set in one place
        FrameworkSettings.mock = true;
        
        // Check from another method
        boolean mockValue = checkMockMode();
        
        assertTrue(mockValue, "Mock mode should be accessible statically");
    }
    
    private boolean checkMockMode() {
        return FrameworkSettings.mock;
    }
}