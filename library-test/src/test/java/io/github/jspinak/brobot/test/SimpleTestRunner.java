package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.test.config.MinimalTestConfig;
import io.github.jspinak.brobot.test.config.MockScreenCaptureConfig;
// Removed unused import
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test runner to verify the test configuration works without hanging.
 * This test uses a minimal configuration to bypass components that might block.
 */
@SpringBootTest(classes = {MinimalTestConfig.class, MockScreenCaptureConfig.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(initializers = io.github.jspinak.brobot.test.config.TestConfigurationManager.class)
public class SimpleTestRunner extends BrobotTestBase {
    
    @Test
    public void testContextLoads() {
        System.out.println("✓ Spring context loaded successfully");
        assertTrue(true, "Context should load without hanging");
    }
    
    @Test 
    public void testMockModeEnabled() {
        System.out.println("✓ Mock mode is enabled: " + isMockMode());
        assertTrue(isMockMode(), "Mock mode should be enabled in tests");
    }
    
    @Test
    public void testHeadlessEnvironment() {
        String headless = System.getProperty("java.awt.headless");
        System.out.println("✓ Headless mode: " + headless);
        assertTrue("true".equals(headless), "Should be running in headless mode");
    }
}