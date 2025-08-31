package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ExecutionMode.
 * Tests execution permissions and mode detection.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExecutionModeTest extends BrobotTestBase {
    
    private ExecutionMode executionMode;
    
    // Store original values to restore after tests
    private boolean originalMock;
    private List<String> originalScreenshots;
    
    @BeforeAll
    void saveOriginalSettings() {
        originalMock = FrameworkSettings.mock;
        originalScreenshots = new ArrayList<>(FrameworkSettings.screenshots);
    }
    
    @AfterAll
    void restoreOriginalSettings() {
        FrameworkSettings.mock = originalMock;
        FrameworkSettings.screenshots = originalScreenshots;
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        executionMode = new ExecutionMode();
        // Clear screenshots for clean test state
        FrameworkSettings.screenshots.clear();
    }
    
    @Nested
    @DisplayName("Basic Mock Mode Tests")
    class BasicMockModeTests {
        
        @Test
        @Order(1)
        @DisplayName("Should detect mock mode when enabled and no screenshots")
        void testMockModeEnabled() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            
            assertTrue(executionMode.isMock());
        }
        
        @Test
        @Order(2)
        @DisplayName("Should not be in mock mode when disabled")
        void testMockModeDisabled() {
            FrameworkSettings.mock = false;
            FrameworkSettings.screenshots.clear();
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @Order(3)
        @DisplayName("Should not be in mock mode when screenshots present")
        void testMockModeWithScreenshots() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("screenshot1.png");
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @Order(4)
        @DisplayName("Should handle empty screenshot list")
        void testEmptyScreenshotList() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots = new ArrayList<>();
            
            assertTrue(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Screenshot Override Tests")
    class ScreenshotOverrideTests {
        
        @Test
        @Order(5)
        @DisplayName("Should override mock mode with single screenshot")
        void testSingleScreenshotOverride() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("test.png");
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @Order(6)
        @DisplayName("Should override mock mode with multiple screenshots")
        void testMultipleScreenshotsOverride() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.addAll(Arrays.asList(
                "screen1.png",
                "screen2.png",
                "screen3.png"
            ));
            
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @Order(7)
        @DisplayName("Should return to mock mode when screenshots cleared")
        void testClearScreenshots() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("test.png");
            
            assertFalse(executionMode.isMock());
            
            FrameworkSettings.screenshots.clear();
            
            assertTrue(executionMode.isMock());
        }
        
        @Test
        @Order(8)
        @DisplayName("Should handle screenshot removal")
        void testScreenshotRemoval() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("test1.png");
            FrameworkSettings.screenshots.add("test2.png");
            
            assertFalse(executionMode.isMock());
            
            FrameworkSettings.screenshots.remove("test1.png");
            // Still has one screenshot
            assertFalse(executionMode.isMock());
            
            FrameworkSettings.screenshots.remove("test2.png");
            // Now empty
            assertTrue(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @Order(9)
        @DisplayName("Should handle null screenshot list gracefully")
        void testNullScreenshotList() {
            FrameworkSettings.mock = true;
            // This would be a programming error, but test defensive behavior
            FrameworkSettings.screenshots = null;
            
            // Should throw or handle gracefully
            assertThrows(NullPointerException.class, () -> {
                executionMode.isMock();
            });
            
            // Restore to valid state
            FrameworkSettings.screenshots = new ArrayList<>();
        }
        
        @Test
        @Order(10)
        @DisplayName("Should handle screenshot list with null entries")
        void testScreenshotListWithNulls() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots = new ArrayList<>();
            FrameworkSettings.screenshots.add(null);
            FrameworkSettings.screenshots.add("valid.png");
            FrameworkSettings.screenshots.add(null);
            
            // Has at least one non-null entry
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @Order(11)
        @DisplayName("Should handle screenshot list with empty strings")
        void testScreenshotListWithEmptyStrings() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots = new ArrayList<>();
            FrameworkSettings.screenshots.add("");
            FrameworkSettings.screenshots.add("  ");
            
            // List is not empty (has entries, even if blank)
            assertFalse(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {
        
        @Test
        @Order(12)
        @DisplayName("Should handle rapid state changes")
        void testRapidStateChanges() {
            for (int i = 0; i < 100; i++) {
                FrameworkSettings.mock = (i % 2 == 0);
                
                if (i % 3 == 0) {
                    FrameworkSettings.screenshots.add("screen" + i + ".png");
                } else {
                    FrameworkSettings.screenshots.clear();
                }
                
                boolean expectedMock = FrameworkSettings.mock && FrameworkSettings.screenshots.isEmpty();
                assertEquals(expectedMock, executionMode.isMock());
            }
        }
        
        @Test
        @Order(13)
        @DisplayName("Should maintain consistent state")
        void testConsistentState() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            
            // Multiple calls should return same result
            boolean first = executionMode.isMock();
            boolean second = executionMode.isMock();
            boolean third = executionMode.isMock();
            
            assertEquals(first, second);
            assertEquals(second, third);
            assertTrue(first);
        }
        
        @Test
        @Order(14)
        @DisplayName("Should reflect immediate changes")
        void testImmediateChanges() {
            FrameworkSettings.mock = false;
            assertFalse(executionMode.isMock());
            
            FrameworkSettings.mock = true;
            assertTrue(executionMode.isMock());
            
            FrameworkSettings.screenshots.add("test.png");
            assertFalse(executionMode.isMock());
            
            FrameworkSettings.screenshots.clear();
            assertTrue(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @Order(15)
        @DisplayName("Should work with typical test setup")
        void testTypicalTestSetup() {
            // Simulate typical test environment
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots = Arrays.asList(
                "screenshots/login-page.png",
                "screenshots/dashboard.png",
                "screenshots/settings.png"
            );
            
            // Screenshots override mock mode
            assertFalse(executionMode.isMock());
        }
        
        @Test
        @Order(16)
        @DisplayName("Should work with development setup")
        void testDevelopmentSetup() {
            // Simulate development environment
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            
            // Pure mock mode for development
            assertTrue(executionMode.isMock());
        }
        
        @Test
        @Order(17)
        @DisplayName("Should work with production setup")
        void testProductionSetup() {
            // Simulate production environment
            FrameworkSettings.mock = false;
            FrameworkSettings.screenshots.clear();
            
            // Real execution in production
            assertFalse(executionMode.isMock());
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @Order(18)
        @DisplayName("Should handle large screenshot lists efficiently")
        void testLargeScreenshotList() {
            FrameworkSettings.mock = true;
            
            // Add many screenshots
            for (int i = 0; i < 1000; i++) {
                FrameworkSettings.screenshots.add("screen" + i + ".png");
            }
            
            long start = System.currentTimeMillis();
            boolean result = executionMode.isMock();
            long duration = System.currentTimeMillis() - start;
            
            assertFalse(result);
            // Should be very fast (< 10ms)
            assertTrue(duration < 10, "Check should be fast, took: " + duration + "ms");
        }
        
        @Test
        @Order(19)
        @DisplayName("Should handle repeated calls efficiently")
        void testRepeatedCalls() {
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                executionMode.isMock();
            }
            long duration = System.currentTimeMillis() - start;
            
            // Should be very fast (< 100ms for 10000 calls)
            assertTrue(duration < 100, "Repeated calls should be fast, took: " + duration + "ms");
        }
    }
    
    @Nested
    @DisplayName("Documentation Tests")
    class DocumentationTests {
        
        @Test
        @Order(20)
        @DisplayName("Should match documented behavior for mock mode")
        void testDocumentedMockBehavior() {
            // As documented: mock = true AND screenshots.isEmpty()
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            assertTrue(executionMode.isMock(), "Should be mock when mock=true and no screenshots");
            
            FrameworkSettings.mock = false;
            FrameworkSettings.screenshots.clear();
            assertFalse(executionMode.isMock(), "Should not be mock when mock=false");
            
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.add("test.png");
            assertFalse(executionMode.isMock(), "Should not be mock when screenshots present");
        }
        
        @Test
        @Order(21)
        @DisplayName("Should follow screenshot precedence rule")
        void testScreenshotPrecedence() {
            // Documentation states: screenshots take precedence over mock mode
            FrameworkSettings.mock = true;
            
            // Without screenshots - mock mode active
            assertTrue(executionMode.isMock());
            
            // With screenshots - real mode (screenshots take precedence)
            FrameworkSettings.screenshots.add("test.png");
            assertFalse(executionMode.isMock());
            
            // Even with mock=true, screenshots force real mode
            assertTrue(FrameworkSettings.mock, "Mock flag should still be true");
            assertFalse(executionMode.isMock(), "But execution should not be mock");
        }
    }
}