package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.config.mock.MockModeManager;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test without Spring to verify basic JUnit works.
 */
public class NonSpringTest {
    
    @BeforeEach
    public void setup() {
        System.setProperty("java.awt.headless", "true");
        MockModeManager.setMockMode(true);
    }
    
    @Test
    public void testBasicAssertion() {
        System.out.println("Running basic test without Spring");
        assertTrue(true, "Basic assertion should pass");
    }
    
    @Test
    public void testMockMode() {
        System.out.println("Mock mode enabled: " + MockModeManager.isMockMode());
        assertTrue(MockModeManager.isMockMode(), "Mock mode should be enabled");
    }
}