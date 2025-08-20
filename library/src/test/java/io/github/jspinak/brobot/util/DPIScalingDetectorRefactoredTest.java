package io.github.jspinak.brobot.util;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Refactored test suite for DPIScalingDetector using the testable method.
 * This avoids complex static mocking of Screen class.
 */
@DisplayName("DPIScalingDetector Refactored Tests")
public class DPIScalingDetectorRefactoredTest extends BrobotTestBase {
    
    private DisplayMode mockDisplayMode;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockDisplayMode = mock(DisplayMode.class);
        
        // Clear cache before each test
        DPIScalingDetector.clearCache();
    }
    
    @Nested
    @DisplayName("Scaling Factor Detection")
    class ScalingFactorDetection {
        
        @Test
        @DisplayName("Detect no scaling (100%)")
        public void testNoScaling() {
            Rectangle bounds = new Rectangle(0, 0, 1920, 1080);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(1.0f, factor, 0.01f);
        }
        
        @Test
        @DisplayName("Detect 125% scaling")
        public void test125PercentScaling() {
            // At 125% scaling, 1920x1080 physical becomes 1536x864 logical
            Rectangle bounds = new Rectangle(0, 0, 1536, 864);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(0.8f, factor, 0.01f);
        }
        
        @Test
        @DisplayName("Detect 150% scaling")
        public void test150PercentScaling() {
            // At 150% scaling, 1920x1080 physical becomes 1280x720 logical
            Rectangle bounds = new Rectangle(0, 0, 1280, 720);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(0.67f, factor, 0.02f);
        }
        
        @Test
        @DisplayName("Detect 175% scaling")
        public void test175PercentScaling() {
            // At 175% scaling, 1920x1080 physical becomes ~1097x617 logical
            Rectangle bounds = new Rectangle(0, 0, 1097, 617);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(0.57f, factor, 0.02f);
        }
        
        @Test
        @DisplayName("Detect custom scaling")
        public void testCustomScaling() {
            // Custom scaling scenario
            Rectangle bounds = new Rectangle(0, 0, 1600, 900);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(0.833f, factor, 0.01f);
        }
    }
    
    @Nested
    @DisplayName("Various Resolution Tests")
    class VariousResolutions {
        
        @ParameterizedTest
        @CsvSource({
            "1920, 1080, 1920, 1080, 1.0",  // 100% scaling
            "2560, 1440, 2560, 1440, 1.0",  // 1440p at 100%
            "3840, 2160, 3840, 2160, 1.0",  // 4K at 100%
            "1366, 768, 1366, 768, 1.0",    // Common laptop resolution
            "2048, 1152, 2560, 1440, 0.8",  // 125% scaling on 1440p
            "2560, 1440, 3840, 2160, 0.67"  // 150% scaling on 4K
        })
        @DisplayName("Different resolutions and scaling")
        public void testVariousResolutions(int logicalWidth, int logicalHeight,
                                          int physicalWidth, int physicalHeight, 
                                          float expectedFactor) {
            Rectangle bounds = new Rectangle(0, 0, logicalWidth, logicalHeight);
            when(mockDisplayMode.getWidth()).thenReturn(physicalWidth);
            when(mockDisplayMode.getHeight()).thenReturn(physicalHeight);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(expectedFactor, factor, 0.02f);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Handle null bounds")
        public void testNullBounds() {
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(null, mockDisplayMode);
            
            assertEquals(1.0f, factor);
        }
        
        @Test
        @DisplayName("Handle null DisplayMode")
        public void testNullDisplayMode() {
            Rectangle bounds = new Rectangle(0, 0, 1920, 1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, null);
            
            assertEquals(1.0f, factor);
        }
        
        @Test
        @DisplayName("Handle both null")
        public void testBothNull() {
            float factor = DPIScalingDetector.detectScalingFactorForBounds(null, null);
            
            assertEquals(1.0f, factor);
        }
    }
    
    @Nested
    @DisplayName("Scaling Descriptions")
    class ScalingDescriptions {
        
        @ParameterizedTest
        @CsvSource({
            "1920, 1080, 1920, 1080, 'No scaling detected (100%)'",
            "1536, 864, 1920, 1080, '125% Windows scaling detected - patterns will be scaled to 80%'",
            "1280, 720, 1920, 1080, '150% Windows scaling detected - patterns will be scaled to 67%'",
            "1097, 617, 1920, 1080, '175% Windows scaling detected - patterns will be scaled to 57%'"
        })
        @DisplayName("Standard scaling descriptions")
        public void testScalingDescriptions(int logicalWidth, int logicalHeight,
                                           int physicalWidth, int physicalHeight, 
                                           String expectedDescription) {
            Rectangle bounds = new Rectangle(0, 0, logicalWidth, logicalHeight);
            when(mockDisplayMode.getWidth()).thenReturn(physicalWidth);
            when(mockDisplayMode.getHeight()).thenReturn(physicalHeight);
            
            // First detect the scaling factor to cache it
            DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            // Then get the description
            String description = DPIScalingDetector.getScalingDescription();
            
            assertEquals(expectedDescription, description);
        }
        
        @Test
        @DisplayName("Custom scaling description")
        public void testCustomScalingDescription() {
            Rectangle bounds = new Rectangle(0, 0, 1600, 900);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            String description = DPIScalingDetector.getScalingDescription();
            
            assertTrue(description.contains("120%") || description.contains("83%"));
        }
    }
    
    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {
        
        @Test
        @DisplayName("High DPI 4K monitor")
        public void test4KMonitor() {
            // 4K monitor at 150% scaling
            Rectangle bounds = new Rectangle(0, 0, 2560, 1440);
            when(mockDisplayMode.getWidth()).thenReturn(3840);
            when(mockDisplayMode.getHeight()).thenReturn(2160);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(0.67f, factor, 0.02f);
        }
        
        @Test
        @DisplayName("Windows laptop with 125% scaling")
        public void testWindowsLaptop() {
            // Common Windows laptop scenario
            Rectangle bounds = new Rectangle(0, 0, 1536, 864);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(0.8f, factor, 0.01f);
        }
        
        @Test
        @DisplayName("Surface device with high scaling")
        public void testSurfaceDevice() {
            // Surface Pro with 200% scaling
            Rectangle bounds = new Rectangle(0, 0, 1368, 912);
            when(mockDisplayMode.getWidth()).thenReturn(2736);
            when(mockDisplayMode.getHeight()).thenReturn(1824);
            
            float factor = DPIScalingDetector.detectScalingFactorForBounds(bounds, mockDisplayMode);
            
            assertEquals(0.5f, factor, 0.01f);
        }
    }
}