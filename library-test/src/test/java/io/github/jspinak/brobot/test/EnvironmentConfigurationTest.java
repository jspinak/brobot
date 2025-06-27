package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates and tests the different BrobotEnvironment configurations.
 * This test class shows how the environment affects various operations.
 */
public class EnvironmentConfigurationTest {
    
    @Nested
    @DisplayName("Unit Test Mode")
    class UnitTestMode extends BaseUnitTest {
        
        @Test
        @DisplayName("Should use mock data for Pattern creation")
        void testPatternWithMockData() {
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            
            // Verify we're in mock mode
            assertTrue(env.isMockMode(), "Should be in mock mode");
            assertFalse(env.useRealFiles(), "Should not use real files");
            
            // Create a pattern - should get dummy image
            Pattern pattern = new Pattern("nonexistent-file.png");
            
            assertNotNull(pattern.getImage(), "Should have a dummy image");
            assertEquals(100, pattern.w(), "Dummy image should be 100px wide");
            assertEquals(100, pattern.h(), "Dummy image should be 100px high");
        }
        
        @Test
        @DisplayName("Should skip screen capture in mock mode")
        void testScreenCaptureInMockMode() {
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            
            // Verify screen capture is disabled
            assertFalse(env.canCaptureScreen(), "Should not be able to capture screen");
            
            // Try to capture screen - should get dummy image
            Region region = new Region(0, 0, 200, 150);
            BufferedImage capture = BufferedImageUtilities.getBufferedImageFromScreen(region);
            
            assertNotNull(capture, "Should return dummy image");
            assertEquals(200, capture.getWidth(), "Dummy should match requested width");
            assertEquals(150, capture.getHeight(), "Dummy should match requested height");
        }
    }
    
    @Nested
    @DisplayName("Integration Test Mode")
    class IntegrationTestMode extends BaseIntegrationTest {
        
        @Test
        @DisplayName("Should load real files in headless environment")
        void testRealFileLoading() {
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            
            // Verify we're NOT in mock mode
            assertFalse(env.isMockMode(), "Should not be in mock mode");
            assertTrue(env.useRealFiles(), "Should use real files");
            
            // This would load a real file if it exists
            // For this demo, we'll just verify the configuration
            System.out.println("Integration test environment: " + env.getEnvironmentInfo());
        }
        
        @Test
        @DisplayName("Should handle screen capture appropriately in headless")
        void testScreenCaptureInHeadless() {
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            
            // In CI/CD, this would be false
            System.out.println("Has display: " + env.hasDisplay());
            System.out.println("Can capture screen: " + env.canCaptureScreen());
            
            // Screen capture would return dummy in headless
            Region region = new Region(0, 0, 100, 100);
            BufferedImage capture = BufferedImageUtilities.getBufferedImageFromScreen(region);
            
            assertNotNull(capture, "Should always return an image (real or dummy)");
        }
    }
    
    @Nested
    @DisplayName("Custom Configuration Tests")
    class CustomConfigurationTests {
        
        @Test
        @DisplayName("Should allow temporary environment switching")
        void testTemporaryEnvironmentSwitch() {
            // Start with current environment
            ExecutionEnvironment original = ExecutionEnvironment.getInstance();
            boolean wasInMockMode = original.isMockMode();
            
            try {
                // Switch to opposite mode
                ExecutionEnvironment newEnv = ExecutionEnvironment.builder()
                    .mockMode(!wasInMockMode)
                    .build();
                ExecutionEnvironment.setInstance(newEnv);
                
                // Verify the switch
                assertEquals(!wasInMockMode, ExecutionEnvironment.getInstance().isMockMode());
                
            } finally {
                // Always restore original
                ExecutionEnvironment.setInstance(original);
            }
            
            // Verify restoration
            assertEquals(wasInMockMode, ExecutionEnvironment.getInstance().isMockMode());
        }
        
        @Test
        @DisplayName("Should read environment variables when configured")
        void testEnvironmentVariableConfiguration() {
            // Save original
            ExecutionEnvironment original = ExecutionEnvironment.getInstance();
            
            try {
                // Create configuration from environment
                ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .fromEnvironment()
                    .build();
                
                // The configuration would be affected by:
                // - BROBOT_MOCK_MODE
                // - BROBOT_FORCE_HEADLESS
                // - BROBOT_ALLOW_SCREEN_CAPTURE
                // - System properties brobot.mock.mode, etc.
                
                System.out.println("Environment from variables: " + env.getEnvironmentInfo());
                
            } finally {
                ExecutionEnvironment.setInstance(original);
            }
        }
    }
    
    @Test
    @DisplayName("Should provide clear environment information")
    void testEnvironmentInfo() {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        String info = env.getEnvironmentInfo();
        
        assertNotNull(info);
        assertTrue(info.contains("mockMode="));
        assertTrue(info.contains("hasDisplay="));
        assertTrue(info.contains("canCaptureScreen="));
        assertTrue(info.contains("useRealFiles="));
        
        System.out.println("Current environment: " + info);
    }
}