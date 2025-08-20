package io.github.jspinak.brobot.util;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DPIScalingDetector using Brobot's mock mode.
 * This demonstrates how to properly test SikuliX-dependent code using Brobot's 
 * built-in mock capabilities instead of trying to mock SikuliX directly.
 */
@DisplayName("DPIScalingDetector - Brobot Mock Mode Tests")
public class DPIScalingDetectorBrobotMockTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // This enables mock mode
        DPIScalingDetector.clearCache();
    }
    
    @Nested
    @DisplayName("Mock Mode Behavior")
    class MockModeBehavior {
        
        @Test
        @DisplayName("Returns 1.0 in mock mode")
        public void testReturnsNoScalingInMockMode() {
            // Given: We're in mock mode (set by BrobotTestBase)
            assertTrue(ExecutionEnvironment.getInstance().isMockMode());
            
            // When: Detecting scaling factor
            float factor = DPIScalingDetector.detectScalingFactor();
            
            // Then: Should return 1.0 (no scaling)
            assertEquals(1.0f, factor, 0.01f);
        }
        
        @Test
        @DisplayName("Caches result in mock mode")
        public void testCachingInMockMode() {
            // Given: Clear cache
            DPIScalingDetector.clearCache();
            
            // When: Call multiple times
            float factor1 = DPIScalingDetector.detectScalingFactor();
            float factor2 = DPIScalingDetector.detectScalingFactor();
            float factor3 = DPIScalingDetector.detectScalingFactor();
            
            // Then: All should return same value
            assertEquals(1.0f, factor1, 0.01f);
            assertEquals(factor1, factor2, 0.01f);
            assertEquals(factor2, factor3, 0.01f);
        }
        
        @Test
        @DisplayName("HasScaling returns false in mock mode")
        public void testHasScalingInMockMode() {
            // When: Checking for scaling in mock mode
            boolean hasScaling = DPIScalingDetector.hasScaling();
            
            // Then: Should return false (no scaling)
            assertFalse(hasScaling);
        }
        
        @Test
        @DisplayName("GetScalingDescription in mock mode")
        public void testScalingDescriptionInMockMode() {
            // When: Getting description in mock mode
            String description = DPIScalingDetector.getScalingDescription();
            
            // Then: Should indicate no scaling
            assertEquals("No scaling detected (100%)", description);
        }
        
        @Test
        @DisplayName("ApplyScalingIfNeeded does nothing in mock mode")
        public void testApplyScalingInMockMode() {
            // When/Then: Should not throw and should complete quickly
            assertDoesNotThrow(() -> DPIScalingDetector.applyScalingIfNeeded());
        }
        
        @Test
        @DisplayName("DebugScaling works in mock mode")
        public void testDebugScalingInMockMode() {
            // When/Then: Should not throw
            assertDoesNotThrow(() -> DPIScalingDetector.debugScaling());
        }
    }
    
    @Nested
    @DisplayName("Cache Management in Mock Mode")
    class CacheManagement {
        
        @Test
        @DisplayName("Clear cache works in mock mode")
        public void testClearCacheInMockMode() {
            // Given: Get initial value
            float initialFactor = DPIScalingDetector.detectScalingFactor();
            assertEquals(1.0f, initialFactor, 0.01f);
            
            // When: Clear cache
            DPIScalingDetector.clearCache();
            
            // Then: Should still return same value (mock always returns 1.0)
            float afterClearFactor = DPIScalingDetector.detectScalingFactor();
            assertEquals(1.0f, afterClearFactor, 0.01f);
        }
        
        @Test
        @DisplayName("Cache prevents multiple mock mode checks")
        public void testCacheEfficiency() {
            // Given: Clear cache
            DPIScalingDetector.clearCache();
            
            // When: First call
            float factor1 = DPIScalingDetector.detectScalingFactor();
            
            // Then: Subsequent calls should use cache
            for (int i = 0; i < 100; i++) {
                float factor = DPIScalingDetector.detectScalingFactor();
                assertEquals(factor1, factor, 0.01f);
            }
        }
    }
    
    @Nested
    @DisplayName("Integration with Brobot's Mock System")
    class BrobotIntegration {
        
        @Test
        @DisplayName("Mock mode is properly detected")
        public void testMockModeDetection() {
            // Given: BrobotTestBase has set up mock mode
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            
            // Then: Environment should be in mock mode
            assertTrue(env.isMockMode());
            assertFalse(env.canCaptureScreen());
            
            // And: DPIScalingDetector should recognize this
            float factor = DPIScalingDetector.detectScalingFactor();
            assertEquals(1.0f, factor, 0.01f);
        }
        
        @Test
        @DisplayName("Works without real Screen instance")
        public void testNoRealScreenNeeded() {
            // Given: We're in mock mode where Screen creation would normally fail
            assertTrue(ExecutionEnvironment.getInstance().isMockMode());
            
            // When: Detecting scaling (which normally creates a Screen)
            float factor = DPIScalingDetector.detectScalingFactor();
            
            // Then: Should work without creating real Screen
            assertEquals(1.0f, factor, 0.01f);
        }
    }
}