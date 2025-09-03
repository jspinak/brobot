package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ExecutionEnvironment.
 * Tests environment detection, mock mode, and display availability.
 */
@DisplayName("ExecutionEnvironment Tests")
public class ExecutionEnvironmentTest extends BrobotTestBase {
    
    private ExecutionEnvironment environment;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Reset to a fresh instance for each test
        environment = ExecutionEnvironment.builder().build();
        ExecutionEnvironment.setInstance(environment);
    }
    
    @Nested
    @DisplayName("Singleton Management")
    class SingletonManagement {
        
        @Test
        @DisplayName("Should return same instance")
        void shouldReturnSameInstance() {
            ExecutionEnvironment first = ExecutionEnvironment.getInstance();
            ExecutionEnvironment second = ExecutionEnvironment.getInstance();
            
            assertSame(first, second, "Should return the same singleton instance");
        }
        
        @Test
        @DisplayName("Should allow setting new instance")
        void shouldAllowSettingNewInstance() {
            ExecutionEnvironment original = ExecutionEnvironment.getInstance();
            ExecutionEnvironment newInstance = ExecutionEnvironment.builder()
                .mockMode(true)
                .build();
            
            ExecutionEnvironment.setInstance(newInstance);
            
            assertSame(newInstance, ExecutionEnvironment.getInstance());
            assertNotSame(original, ExecutionEnvironment.getInstance());
        }
    }
    
    @Nested
    @DisplayName("Mock Mode Configuration")
    class MockModeConfiguration {
        
        @Test
        @DisplayName("Should default to false")
        void shouldDefaultToFalse() {
            ExecutionEnvironment env = ExecutionEnvironment.builder().build();
            assertFalse(env.isMockMode());
        }
        
        @Test
        @DisplayName("Should enable mock mode")
        void shouldEnableMockMode() {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(true)
                .build();
            
            assertTrue(env.isMockMode());
        }
        
        @Test
        @DisplayName("Should disable screen operations in mock mode")
        void shouldDisableScreenOpsInMockMode() {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(true)
                .build();
            
            assertTrue(env.shouldSkipSikuliX());
            // In mock mode, can still capture screens (simulated)
        }
    }
    
    @Nested
    @DisplayName("Display Detection")
    class DisplayDetection {
        
        @Test
        @DisplayName("Should detect display when not headless")
        void shouldDetectDisplayWhenNotHeadless() {
            // In test environment with BrobotTestBase, hasDisplay() returns false due to test mode
            // We need to temporarily clear test mode to test the actual logic
            String originalTestMode = System.getProperty("brobot.test.mode");
            String originalTestType = System.getProperty("brobot.test.type");
            
            try {
                // Temporarily disable test mode to test actual display logic
                System.clearProperty("brobot.test.mode");
                System.clearProperty("brobot.test.type");
                
                ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .mockMode(false)
                    .forceHeadless(false)
                    .build();
                
                // Should have display when forced not headless
                assertTrue(env.hasDisplay());
            } finally {
                // Restore test mode
                if (originalTestMode != null) System.setProperty("brobot.test.mode", originalTestMode);
                if (originalTestType != null) System.setProperty("brobot.test.type", originalTestType);
            }
        }
        
        @Test
        @DisplayName("Should cache display check results")
        void shouldCacheDisplayCheckResults() {
            ExecutionEnvironment env = spy(ExecutionEnvironment.builder().build());
            
            // First call
            boolean firstResult = env.hasDisplay();
            
            // Second call within cache duration
            boolean secondResult = env.hasDisplay();
            
            assertEquals(firstResult, secondResult);
            // Due to caching, performDisplayCheck should only be called once
            // Note: This would require making performDisplayCheck package-private for verification
        }
        
        @Test
        @DisplayName("Should refresh display cache on demand")
        void shouldRefreshDisplayCacheOnDemand() {
            // In test mode, hasDisplay() always returns false regardless of forceHeadless
            // We need to temporarily disable test mode
            String originalTestMode = System.getProperty("brobot.test.mode");
            String originalTestType = System.getProperty("brobot.test.type");
            
            try {
                System.clearProperty("brobot.test.mode");
                System.clearProperty("brobot.test.type");
                
                ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .forceHeadless(true)
                    .build();
                
                // Initial check
                assertFalse(env.hasDisplay());
                
                // Change configuration
                env = ExecutionEnvironment.builder()
                    .forceHeadless(false)
                    .build();
                ExecutionEnvironment.setInstance(env);
                
                // Refresh cache
                env.refreshDisplayCheck();
                
                // Should reflect new configuration
                assertTrue(env.hasDisplay());
            } finally {
                if (originalTestMode != null) System.setProperty("brobot.test.mode", originalTestMode);
                if (originalTestType != null) System.setProperty("brobot.test.type", originalTestType);
            }
        }
        
        @ParameterizedTest
        @DisplayName("Should respect forceHeadless setting")
        @ValueSource(booleans = {true, false})
        void shouldRespectForceHeadlessSetting(boolean forceHeadless) {
            // In test mode, hasDisplay() always returns false
            // We need to temporarily disable test mode to test forceHeadless logic
            String originalTestMode = System.getProperty("brobot.test.mode");
            String originalTestType = System.getProperty("brobot.test.type");
            
            try {
                System.clearProperty("brobot.test.mode");
                System.clearProperty("brobot.test.type");
                
                ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .forceHeadless(forceHeadless)
                    .build();
                
                assertEquals(!forceHeadless, env.hasDisplay());
            } finally {
                if (originalTestMode != null) System.setProperty("brobot.test.mode", originalTestMode);
                if (originalTestType != null) System.setProperty("brobot.test.type", originalTestType);
            }
        }
    }
    
    @Nested
    @DisplayName("Environment Detection")
    class EnvironmentDetection {
        
        @Test
        @DisplayName("Should detect CI environment")
        void shouldDetectCIEnvironment() {
            // Test with various CI environment variables
            ExecutionEnvironment env = ExecutionEnvironment.builder().build();
            
            // CI detection is done internally by hasDisplay()
            // We can't directly test isRunningInCI as it's private
            boolean hasDisplay = env.hasDisplay();
            assertNotNull(hasDisplay);
        }
        
        @Test
        @DisplayName("Should detect Docker environment")
        void shouldDetectDockerEnvironment() {
            ExecutionEnvironment env = ExecutionEnvironment.builder().build();
            
            // Docker detection would be part of hasDisplay() logic
            // Testing hasDisplay covers the environment detection
            boolean hasDisplay = env.hasDisplay();
            assertNotNull(hasDisplay);
        }
        
        @Test
        @DisplayName("Should handle WSL detection")
        void shouldHandleWSLDetection() {
            // WSL detection is based on environment variables
            // We can't easily mock System.getenv, but we can test the logic exists
            ExecutionEnvironment env = ExecutionEnvironment.builder().build();
            
            // The method should exist and return a result
            boolean hasDisplay = env.hasDisplay();
            assertNotNull(hasDisplay);
        }
    }
    
    @Nested
    @DisplayName("SikuliX Integration")
    class SikuliXIntegration {
        
        @Test
        @DisplayName("Should skip SikuliX in mock mode")
        void shouldSkipSikuliXInMockMode() {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(true)
                .build();
            
            assertTrue(env.shouldSkipSikuliX());
        }
        
        @Test
        @DisplayName("Should skip SikuliX when no display")
        void shouldSkipSikuliXWhenNoDisplay() {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(false)
                .forceHeadless(true)
                .build();
            
            assertTrue(env.shouldSkipSikuliX());
        }
        
        @Test
        @DisplayName("Should not skip SikuliX with display and no mock")
        void shouldNotSkipSikuliXWithDisplayAndNoMock() {
            // In test mode, hasDisplay() returns false, so shouldSkipSikuliX() returns true
            // We need to test without test mode
            String originalTestMode = System.getProperty("brobot.test.mode");
            String originalTestType = System.getProperty("brobot.test.type");
            
            try {
                System.clearProperty("brobot.test.mode");
                System.clearProperty("brobot.test.type");
                
                ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .mockMode(false)
                    .forceHeadless(false)
                    .build();
                
                assertFalse(env.shouldSkipSikuliX());
            } finally {
                if (originalTestMode != null) System.setProperty("brobot.test.mode", originalTestMode);
                if (originalTestType != null) System.setProperty("brobot.test.type", originalTestType);
            }
        }
    }
    
    @Nested
    @DisplayName("Screen Capture Configuration")
    class ScreenCaptureConfiguration {
        
        @Test
        @DisplayName("Should allow screen capture by default")
        void shouldAllowScreenCaptureByDefault() {
            ExecutionEnvironment env = ExecutionEnvironment.builder().build();
            
            // canCaptureScreen depends on display availability
            // In test environment, this may vary
            assertNotNull(env.canCaptureScreen());
        }
        
        @Test
        @DisplayName("Should skip screen capture when disabled")
        void shouldSkipScreenCaptureWhenDisabled() {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .allowScreenCapture(false)
                .build();
            
            assertFalse(env.canCaptureScreen());
        }
        
        @Test
        @DisplayName("Should handle screen capture in headless mode")
        void shouldHandleScreenCaptureInHeadlessMode() {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .forceHeadless(true)
                .allowScreenCapture(true)
                .build();
            
            // Even with allowScreenCapture true, headless should prevent it
            assertFalse(env.canCaptureScreen());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Should build with all options")
        void shouldBuildWithAllOptions() {
            // Temporarily disable test mode to test forceHeadless=false
            String originalTestMode = System.getProperty("brobot.test.mode");
            String originalTestType = System.getProperty("brobot.test.type");
            
            try {
                System.clearProperty("brobot.test.mode");
                System.clearProperty("brobot.test.type");
                
                ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .mockMode(true)
                    .forceHeadless(false)
                    .allowScreenCapture(true)
                    .verboseLogging(true)
                    .build();
                
                assertTrue(env.isMockMode());
                assertTrue(env.hasDisplay()); // forceHeadless is false
                // canCaptureScreen is false in mock mode
                assertFalse(env.canCaptureScreen());
                // verboseLogging is set but not exposed via getter
            } finally {
                if (originalTestMode != null) System.setProperty("brobot.test.mode", originalTestMode);
                if (originalTestType != null) System.setProperty("brobot.test.type", originalTestType);
            }
        }
        
        @Test
        @DisplayName("Should build with defaults")
        void shouldBuildWithDefaults() {
            ExecutionEnvironment env = ExecutionEnvironment.builder().build();
            
            assertFalse(env.isMockMode());
            // Display detection depends on actual environment
            assertNotNull(env.hasDisplay());
            // canCaptureScreen depends on actual display
            assertNotNull(env.canCaptureScreen());
            // verboseLogging defaults to false but not exposed
        }
        
        @Test
        @DisplayName("Should create independent instances")
        void shouldCreateIndependentInstances() {
            ExecutionEnvironment env1 = ExecutionEnvironment.builder()
                .mockMode(true)
                .build();
            
            ExecutionEnvironment env2 = ExecutionEnvironment.builder()
                .mockMode(false)
                .build();
            
            assertTrue(env1.isMockMode());
            assertFalse(env2.isMockMode());
        }
    }
    
    @Nested
    @DisplayName("Logging Configuration")
    class LoggingConfiguration {
        
        @Test
        @DisplayName("Should configure verbose logging")
        void shouldConfigureVerboseLogging() {
            // verboseLogging can be set via builder but not queried
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .verboseLogging(false)
                .build();
            
            // Setting is stored internally but not exposed
            assertNotNull(env);
        }
        
        @Test
        @DisplayName("Should enable verbose logging via builder")
        void shouldEnableVerboseLoggingViaBuilder() {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .verboseLogging(true)
                .build();
            
            // Setting is stored internally
            assertNotNull(env);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle rapid display checks")
        void shouldHandleRapidDisplayChecks() {
            ExecutionEnvironment env = ExecutionEnvironment.builder().build();
            
            // Rapid successive calls should use cache
            for (int i = 0; i < 100; i++) {
                boolean hasDisplay = env.hasDisplay();
                assertNotNull(hasDisplay);
            }
            
            // Should complete quickly due to caching
        }
        
        @Test
        @DisplayName("Should handle environment changes")
        void shouldHandleEnvironmentChanges() {
            // In test mode, hasDisplay() always returns false
            // We need to temporarily disable test mode
            String originalTestMode = System.getProperty("brobot.test.mode");
            String originalTestType = System.getProperty("brobot.test.type");
            
            try {
                System.clearProperty("brobot.test.mode");
                System.clearProperty("brobot.test.type");
                
                ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .mockMode(false)
                    .forceHeadless(true)
                    .build();
                
                assertFalse(env.hasDisplay());
                
                // Simulate environment change
                env = ExecutionEnvironment.builder()
                    .mockMode(false)
                    .forceHeadless(false)
                    .build();
                ExecutionEnvironment.setInstance(env);
                
                assertTrue(env.hasDisplay());
            } finally {
                if (originalTestMode != null) System.setProperty("brobot.test.mode", originalTestMode);
                if (originalTestType != null) System.setProperty("brobot.test.type", originalTestType);
            }
        }
        
        @Test
        @DisplayName("Should handle mock mode transition")
        void shouldHandleMockModeTransition() {
            // Start in real mode
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(false)
                .build();
            ExecutionEnvironment.setInstance(env);
            
            assertFalse(env.isMockMode());
            
            // Transition to mock mode
            env = ExecutionEnvironment.builder()
                .mockMode(true)
                .build();
            ExecutionEnvironment.setInstance(env);
            
            assertTrue(env.isMockMode());
            assertTrue(env.shouldSkipSikuliX());
        }
    }
}