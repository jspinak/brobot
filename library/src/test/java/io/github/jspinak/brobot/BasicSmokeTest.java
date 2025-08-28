package io.github.jspinak.brobot;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic smoke test to verify test infrastructure works.
 */
@DisplayName("Basic Smoke Test")
public class BasicSmokeTest extends BrobotTestBase {
    
    @Test
    @DisplayName("Test infrastructure should work")
    void testInfrastructureWorks() {
        assertTrue(true, "Basic assertion should work");
    }
    
    @Test
    @DisplayName("Mock mode should be enabled")
    void testMockModeEnabled() {
        assertTrue(isMockMode(), "Mock mode should be enabled");
    }
    
    @Test
    @DisplayName("Simple math should work")
    void testSimpleMath() {
        assertEquals(4, 2 + 2, "2 + 2 should equal 4");
    }
}