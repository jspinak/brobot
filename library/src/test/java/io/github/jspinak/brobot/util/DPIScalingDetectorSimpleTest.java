package io.github.jspinak.brobot.util;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified test suite for DPIScalingDetector that works with Brobot's mock mode.
 * Extends BrobotTestBase for proper mock configuration.
 * 
 * NOTE: Disabled due to complex mocking issues with SikuliX Screen class.
 * See DPIScalingDetectorRefactoredTest for working tests.
 */
@DisplayName("DPIScalingDetector Simple Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
@org.junit.jupiter.api.Disabled("Complex mocking issues with SikuliX Screen - see DPIScalingDetectorRefactoredTest")
public class DPIScalingDetectorSimpleTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Enables mock mode via BrobotTestBase
        // Clear cache before each test
        DPIScalingDetector.clearCache();
    }
    
    @Nested
    @DisplayName("Basic Functionality")
    class BasicFunctionality {
        
        @Test
        @DisplayName("Detect scaling factor returns valid value")
        public void testDetectScalingFactor() {
            float factor = DPIScalingDetector.detectScalingFactor();
            
            // Should return a positive value
            assertTrue(factor > 0, "Factor should be positive");
            assertTrue(factor <= 2.0, "Factor should be reasonable");
        }
        
        @Test
        @DisplayName("Has scaling returns boolean")
        public void testHasScaling() {
            boolean hasScaling = DPIScalingDetector.hasScaling();
            
            // Should return without exception
            assertNotNull(hasScaling);
        }
        
        @Test
        @DisplayName("Get scaling description returns string")
        public void testGetScalingDescription() {
            String description = DPIScalingDetector.getScalingDescription();
            
            assertNotNull(description, "Description should not be null");
            assertFalse(description.isEmpty(), "Description should not be empty");
        }
    }
    
    @Nested
    @DisplayName("Caching")
    class Caching {
        
        @Test
        @DisplayName("Cached value is consistent")
        public void testCaching() {
            // Clear cache
            DPIScalingDetector.clearCache();
            
            // First call
            float factor1 = DPIScalingDetector.detectScalingFactor();
            
            // Second call should return cached value
            float factor2 = DPIScalingDetector.detectScalingFactor();
            
            assertEquals(factor1, factor2, "Cached value should be the same");
        }
        
        @Test
        @DisplayName("Clear cache allows re-detection")
        public void testClearCache() {
            // Get initial value
            float factor1 = DPIScalingDetector.detectScalingFactor();
            
            // Clear cache
            DPIScalingDetector.clearCache();
            
            // This should work without throwing
            float factor2 = DPIScalingDetector.detectScalingFactor();
            
            // Both should be valid values
            assertTrue(factor1 > 0);
            assertTrue(factor2 > 0);
        }
    }
    
    @Nested
    @DisplayName("Integration Methods")
    class IntegrationMethods {
        
        @Test
        @DisplayName("Apply scaling if needed completes")
        public void testApplyScaling() {
            // This should not throw
            assertDoesNotThrow(() -> DPIScalingDetector.applyScalingIfNeeded());
        }
        
        @Test
        @DisplayName("Debug scaling outputs information")
        public void testDebugScaling() {
            // Should complete without error
            assertDoesNotThrow(() -> DPIScalingDetector.debugScaling());
        }
    }
    
    @Nested
    @DisplayName("Error Resilience")
    class ErrorResilience {
        
        @Test
        @DisplayName("Methods handle errors gracefully")
        public void testErrorHandling() {
            // Even if there are issues, should return default values
            float factor = DPIScalingDetector.detectScalingFactor();
            
            // Should return positive value even on error
            assertTrue(factor > 0, "Should return positive value even on error");
        }
        
        @Test
        @DisplayName("Multiple rapid calls work correctly")
        public void testRapidCalls() {
            // Clear cache first
            DPIScalingDetector.clearCache();
            
            // Make multiple rapid calls
            for (int i = 0; i < 10; i++) {
                float factor = DPIScalingDetector.detectScalingFactor();
                assertTrue(factor > 0);
            }
            
            // Should complete without issues
            assertTrue(true);
        }
    }
    
    @Nested
    @DisplayName("Description Messages")
    class DescriptionMessages {
        
        @Test
        @DisplayName("Description contains meaningful text")
        public void testDescriptionContent() {
            String description = DPIScalingDetector.getScalingDescription();
            
            // Should contain some key words
            assertTrue(
                description.contains("scaling") || 
                description.contains("%") ||
                description.contains("No scaling"),
                "Description should contain scaling information"
            );
        }
        
        @Test
        @DisplayName("Description is consistent with factor")
        public void testDescriptionConsistency() {
            float factor = DPIScalingDetector.detectScalingFactor();
            String description = DPIScalingDetector.getScalingDescription();
            
            if (Math.abs(factor - 1.0f) < 0.01) {
                assertTrue(description.contains("No scaling") || description.contains("100%"));
            } else {
                assertTrue(description.contains("scaling") || description.contains("%"));
            }
        }
    }
}