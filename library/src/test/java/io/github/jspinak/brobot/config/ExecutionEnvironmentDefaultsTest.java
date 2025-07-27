package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that ExecutionEnvironment sets appropriate defaults for GUI automation.
 */
public class ExecutionEnvironmentDefaultsTest {

    @Test
    public void testDefaultHeadlessPropertyIsSetToFalse() {
        // When ExecutionEnvironment class is loaded (happens automatically on first reference)
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // Then java.awt.headless should be set to false by default
        String headlessProperty = System.getProperty("java.awt.headless");
        assertNotNull(headlessProperty, "java.awt.headless property should be set");
        assertEquals("false", headlessProperty, 
            "java.awt.headless should default to false for GUI automation");
        
        // And ExecutionEnvironment should detect non-headless capability
        assertTrue(env.hasDisplay(), 
            "ExecutionEnvironment should detect display availability when headless=false");
        assertTrue(env.canCaptureScreen(), 
            "ExecutionEnvironment should allow screen capture when headless=false");
    }
    
    @Test
    public void testHeadlessPropertyOverrideBehavior() {
        // This test verifies that Brobot overrides headless settings by default
        // (Note: we can't easily test the override in the same JVM since static blocks 
        // run only once, but we can verify the final state)
        
        // Given the current state after ExecutionEnvironment initialization
        String headlessProperty = System.getProperty("java.awt.headless");
        
        // The property should be set to false for GUI automation
        assertNotNull(headlessProperty, "java.awt.headless property should exist");
        assertEquals("false", headlessProperty, 
            "java.awt.headless should be overridden to false for GUI automation");
        
        // If someone needs to test preservation behavior, they would set:
        // System.setProperty("brobot.preserve.headless.setting", "true");
        // before any Brobot classes are loaded
    }
    
    @Test
    public void testExecutionEnvironmentBasicFunctionality() {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // Basic functionality should work
        assertNotNull(env, "ExecutionEnvironment instance should not be null");
        assertFalse(env.isMockMode(), "Mock mode should be false by default");
        assertTrue(env.useRealFiles(), "Should use real files by default");
        assertNotNull(env.getEnvironmentInfo(), "Environment info should be available");
    }
}