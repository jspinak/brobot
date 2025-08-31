package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ExecutionMode.
 * Tests the execution permission logic for determining mock mode.
 */
@DisplayName("ExecutionMode Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag(TestCategories.CONFIG)
@Tag(TestCategories.CI_SAFE)
public class ExecutionModeTest extends BrobotTestBase {
    
    private ExecutionMode executionMode;
    private boolean originalMock;
    private ArrayList<String> originalScreenshots;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        executionMode = new ExecutionMode();
        
        // Save original values
        originalMock = FrameworkSettings.mock;
        originalScreenshots = new ArrayList<>(FrameworkSettings.screenshots);
        
        // Clear settings for test
        FrameworkSettings.mock = false;
        FrameworkSettings.screenshots.clear();
    }
    
    @AfterEach
    public void restoreSettings() {
        // Restore original values
        FrameworkSettings.mock = originalMock;
        FrameworkSettings.screenshots = originalScreenshots;
    }
    
    @Nested
    @DisplayName("Mock Mode Detection")
    class MockModeDetection {
        
        @Test
        @DisplayName("Should return false when mock is disabled")
        void shouldReturnFalseWhenMockDisabled() {
            FrameworkSettings.mock = false;
            FrameworkSettings.screenshots.clear();
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @DisplayName("Should return true when mock is enabled and no screenshots")
        void shouldReturnTrueWhenMockEnabledAndNoScreenshots() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            
            assertTrue(executionMode.isMock());
        }
        
        @Test
        @DisplayName("Should return false when mock enabled but screenshots present")
        void shouldReturnFalseWhenMockEnabledButScreenshotsPresent() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("test1.png");
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @DisplayName("Should return false when mock disabled but screenshots present")
        void shouldReturnFalseWhenMockDisabledButScreenshotsPresent() {
            FrameworkSettings.mock = false;
            FrameworkSettings.screenshots.add("test1.png");
            FrameworkSettings.screenshots.add("test2.png");
            
            assertFalse(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Screenshot Override Behavior")
    class ScreenshotOverrideBehavior {
        
        @Test
        @DisplayName("Single screenshot should override mock mode")
        void singleScreenshotShouldOverrideMockMode() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("single.png");
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @DisplayName("Multiple screenshots should override mock mode")
        void multipleScreenshotsShouldOverrideMockMode() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("screen1.png");
            FrameworkSettings.screenshots.add("screen2.png");
            FrameworkSettings.screenshots.add("screen3.png");
            
            assertFalse(executionMode.isMock());
            assertEquals(3, FrameworkSettings.screenshots.size());
        }
        
        @Test
        @DisplayName("Clearing screenshots should restore mock mode")
        void clearingScreenshotsShouldRestoreMockMode() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("temp.png");
            
            assertFalse(executionMode.isMock());
            
            FrameworkSettings.screenshots.clear();
            
            assertTrue(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle rapid state changes")
        void shouldHandleRapidStateChanges() {
            for (int i = 0; i < 100; i++) {
                FrameworkSettings.mock = (i % 2 == 0);
                
                if (i % 3 == 0) {
                    FrameworkSettings.screenshots.add("test" + i + ".png");
                } else if (i % 5 == 0) {
                    FrameworkSettings.screenshots.clear();
                }
                
                // Should always return consistent result based on current state
                boolean expected = FrameworkSettings.mock && FrameworkSettings.screenshots.isEmpty();
                assertEquals(expected, executionMode.isMock());
            }
        }
        
        @Test
        @DisplayName("Should handle empty string in screenshots")
        void shouldHandleEmptyStringInScreenshots() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("");
            
            // Even empty string counts as a screenshot
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @DisplayName("Should handle null in screenshots list")
        void shouldHandleNullInScreenshotsList() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add(null);
            
            // Null entry still makes list non-empty
            assertFalse(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Configuration Scenarios")
    class ConfigurationScenarios {
        
        @ParameterizedTest
        @DisplayName("Should handle various configuration combinations")
        @CsvSource({
            "false, 0, false",  // No mock, no screenshots -> false
            "false, 1, false",  // No mock, with screenshots -> false
            "true, 0, true",    // Mock, no screenshots -> true
            "true, 1, false",   // Mock, with screenshots -> false
            "true, 5, false",   // Mock, many screenshots -> false
        })
        void shouldHandleVariousConfigurationCombinations(boolean mock, int screenshotCount, boolean expectedResult) {
            FrameworkSettings.mock = mock;
            FrameworkSettings.screenshots.clear();
            
            for (int i = 0; i < screenshotCount; i++) {
                FrameworkSettings.screenshots.add("screen" + i + ".png");
            }
            
            assertEquals(expectedResult, executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Usage Patterns")
    class UsagePatterns {
        
        @Test
        @DisplayName("Should support unit test mode")
        void shouldSupportUnitTestMode() {
            // Unit tests typically use mock mode
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            
            assertTrue(executionMode.isMock());
        }
        
        @Test
        @DisplayName("Should support integration test mode")
        void shouldSupportIntegrationTestMode() {
            // Integration tests might use screenshots
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("integration_test.png");
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @DisplayName("Should support production mode")
        void shouldSupportProductionMode() {
            // Production typically has mock disabled
            FrameworkSettings.mock = false;
            FrameworkSettings.screenshots.clear();
            
            assertFalse(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Component Integration")
    class ComponentIntegration {
        
        @Test
        @DisplayName("Should be consistent across multiple instances")
        void shouldBeConsistentAcrossMultipleInstances() {
            ExecutionMode mode1 = new ExecutionMode();
            ExecutionMode mode2 = new ExecutionMode();
            
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            
            assertEquals(mode1.isMock(), mode2.isMock());
            
            FrameworkSettings.screenshots.add("test.png");
            
            assertEquals(mode1.isMock(), mode2.isMock());
        }
        
        @Test
        @DisplayName("Should reflect immediate setting changes")
        void shouldReflectImmediateSettingChanges() {
            FrameworkSettings.mock = false;
            assertFalse(executionMode.isMock());
            
            FrameworkSettings.mock = true;
            assertTrue(executionMode.isMock());
            
            FrameworkSettings.screenshots.add("immediate.png");
            assertFalse(executionMode.isMock());
        }
    }
}