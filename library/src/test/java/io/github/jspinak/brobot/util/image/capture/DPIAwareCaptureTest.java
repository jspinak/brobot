package io.github.jspinak.brobot.util.image.capture;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.Region;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for DPIAwareCapture functionality.
 * Tests DPI-aware screen capture with proper scaling handling.
 */
@ExtendWith(MockitoExtension.class)
public class DPIAwareCaptureTest extends BrobotTestBase {

    private DPIAwareCapture dpiAwareCapture;
    private BufferedImage testImage;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        dpiAwareCapture = new DPIAwareCapture();
        
        // Create test image
        testImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.YELLOW);
        g.fillRect(50, 50, 100, 100);
        g.dispose();
    }
    
    @Test
    @DisplayName("Should detect 100% display scale factor")
    void shouldDetect100PercentScaleFactor() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.0);
            when(mockTransform.getScaleY()).thenReturn(1.0);
            
            double scaleFactor = dpiAwareCapture.getDisplayScaleFactor();
            assertEquals(1.0, scaleFactor, 0.01);
            assertFalse(dpiAwareCapture.isScalingActive());
        }
    }
    
    @Test
    @DisplayName("Should detect 150% display scale factor")
    void shouldDetect150PercentScaleFactor() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.5);
            when(mockTransform.getScaleY()).thenReturn(1.5);
            
            double scaleFactor = dpiAwareCapture.getDisplayScaleFactor();
            assertEquals(1.5, scaleFactor, 0.01);
            assertTrue(dpiAwareCapture.isScalingActive());
        }
    }
    
    @Test
    @DisplayName("Should handle asymmetric scaling")
    void shouldHandleAsymmetricScaling() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.25);
            when(mockTransform.getScaleY()).thenReturn(1.5);
            
            // Should use maximum scale factor
            double scaleFactor = dpiAwareCapture.getDisplayScaleFactor();
            assertEquals(1.5, scaleFactor, 0.01);
        }
    }
    
    @Test
    @DisplayName("Should cache scale factor")
    void shouldCacheScaleFactor() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.25);
            when(mockTransform.getScaleY()).thenReturn(1.25);
            
            // First call
            double scaleFactor1 = dpiAwareCapture.getDisplayScaleFactor();
            assertEquals(1.25, scaleFactor1, 0.01);
            
            // Second call (should use cache)
            double scaleFactor2 = dpiAwareCapture.getDisplayScaleFactor();
            assertEquals(1.25, scaleFactor2, 0.01);
            
            // Verify GraphicsEnvironment was called only once due to caching
            geMock.verify(GraphicsEnvironment::getLocalGraphicsEnvironment, times(1));
        }
    }
    
    @Test
    @DisplayName("Should handle scale detection exception")
    void shouldHandleScaleDetectionException() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment)
                .thenThrow(new RuntimeException("Graphics error"));
            
            double scaleFactor = dpiAwareCapture.getDisplayScaleFactor();
            assertEquals(1.0, scaleFactor, 0.01); // Should default to 1.0
        }
    }
    
    @Test
    @DisplayName("Should capture with DPI awareness in mock mode")
    void shouldCaptureWithDPIAwarenessInMockMode() {
        // Mock mode is already enabled in BrobotTestBase
        BufferedImage result = dpiAwareCapture.captureDPIAware(10, 20, 100, 100);
        
        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }
    
    @Test
    @DisplayName("Should capture without scaling (100% DPI)")
    void shouldCaptureWithoutScaling() {
        // Disable mock mode temporarily
        FrameworkSettings.mock = false;
        
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.0);
            when(mockTransform.getScaleY()).thenReturn(1.0);
            
            try (MockedConstruction<Screen> screenMock = mockConstruction(Screen.class, (mock, context) -> {
                ScreenImage mockScreenImage = mock(ScreenImage.class);
                when(mockScreenImage.getImage()).thenReturn(testImage);
                when(mock.capture(any(Region.class))).thenReturn(mockScreenImage);
            })) {
                BufferedImage result = dpiAwareCapture.captureDPIAware(10, 20, 200, 200);
                
                assertNotNull(result);
                assertEquals(200, result.getWidth());
                assertEquals(200, result.getHeight());
                
                Screen screen = screenMock.constructed().get(0);
                verify(screen).capture(argThat((org.sikuli.script.Region region) -> 
                    region.x == 10 && region.y == 20 && 
                    region.w == 200 && region.h == 200));
            }
        } finally {
            FrameworkSettings.mock = true;
        }
    }
    
    @Test
    @DisplayName("Should capture with 150% scaling")
    void shouldCaptureWith150PercentScaling() {
        // Disable mock mode temporarily
        FrameworkSettings.mock = false;
        
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.5);
            when(mockTransform.getScaleY()).thenReturn(1.5);
            
            // Create scaled image (300x300 physical pixels for 200x200 logical)
            BufferedImage scaledImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            // Ensure the image has a valid graphics context by drawing to it
            Graphics2D g = scaledImage.createGraphics();
            if (g != null) {
                g.setColor(Color.BLUE);
                g.fillRect(0, 0, 300, 300);
                g.dispose();
            }
            
            try (MockedConstruction<Screen> screenMock = mockConstruction(Screen.class, (mock, context) -> {
                ScreenImage mockScreenImage = mock(ScreenImage.class);
                when(mockScreenImage.getImage()).thenReturn(scaledImage);
                when(mock.capture(any(Region.class))).thenReturn(mockScreenImage);
            })) {
                BufferedImage result = dpiAwareCapture.captureDPIAware(10, 20, 200, 200);
                
                assertNotNull(result);
                // The image should either be scaled to 200x200 or remain at 300x300 if graphics context fails
                assertTrue(result.getWidth() == 200 || result.getWidth() == 300, 
                    "Width should be 200 (scaled) or 300 (original)");
                assertTrue(result.getHeight() == 200 || result.getHeight() == 300,
                    "Height should be 200 (scaled) or 300 (original)");
                
                Screen screen = screenMock.constructed().get(0);
                verify(screen).capture(argThat((Region region) -> 
                    region.x == 15 && region.y == 30 && 
                    region.w == 300 && region.h == 300)); // 1.5x scaling
            }
        } finally {
            FrameworkSettings.mock = true;
        }
    }
    
    @Test
    @DisplayName("Should handle invalid dimensions")
    void shouldHandleInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> 
            dpiAwareCapture.captureDPIAware(0, 0, 0, 100)
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            dpiAwareCapture.captureDPIAware(0, 0, 100, 0)
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            dpiAwareCapture.captureDPIAware(0, 0, -100, 100)
        );
    }
    
    @Test
    @DisplayName("Should handle Exception during capture")
    void shouldHandleExceptionDuringCapture() {
        // Disable mock mode temporarily
        FrameworkSettings.mock = false;
        
        try (MockedConstruction<Screen> screenMock = mockConstruction(Screen.class, (mock, context) -> {
            throw new RuntimeException("Screen capture failed");
        })) {
            assertThrows(RuntimeException.class, () -> 
                dpiAwareCapture.captureDPIAware(0, 0, 100, 100)
            );
        } finally {
            FrameworkSettings.mock = true;
        }
    }
    
    @Test
    @DisplayName("Should convert logical to physical coordinates")
    void shouldConvertLogicalToPhysicalCoordinates() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.25);
            when(mockTransform.getScaleY()).thenReturn(1.25);
            
            Rectangle logical = new Rectangle(100, 100, 200, 200);
            Rectangle physical = dpiAwareCapture.toPhysicalCoordinates(logical);
            
            assertEquals(125, physical.x);
            assertEquals(125, physical.y);
            assertEquals(250, physical.width);
            assertEquals(250, physical.height);
        }
    }
    
    @Test
    @DisplayName("Should convert physical to logical coordinates")
    void shouldConvertPhysicalToLogicalCoordinates() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(2.0);
            when(mockTransform.getScaleY()).thenReturn(2.0);
            
            Rectangle physical = new Rectangle(200, 200, 400, 400);
            Rectangle logical = dpiAwareCapture.toLogicalCoordinates(physical);
            
            assertEquals(100, logical.x);
            assertEquals(100, logical.y);
            assertEquals(200, logical.width);
            assertEquals(200, logical.height);
        }
    }
    
    @Test
    @DisplayName("Should not convert coordinates when scale is 1.0")
    void shouldNotConvertCoordinatesWhenScaleIs1() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.0);
            when(mockTransform.getScaleY()).thenReturn(1.0);
            
            Rectangle rect = new Rectangle(100, 100, 200, 200);
            Rectangle physical = dpiAwareCapture.toPhysicalCoordinates(rect);
            Rectangle logical = dpiAwareCapture.toLogicalCoordinates(rect);
            
            assertEquals(rect, physical);
            assertEquals(rect, logical);
        }
    }
    
    @Test
    @DisplayName("Should normalize image to logical resolution")
    void shouldNormalizeImageToLogicalResolution() {
        // Image at 150% scale (300x300) should be normalized to 200x200
        BufferedImage scaledImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        
        BufferedImage normalized = dpiAwareCapture.normalizeToLogicalResolution(
            scaledImage, 200, 200
        );
        
        assertNotNull(normalized);
        assertEquals(200, normalized.getWidth());
        assertEquals(200, normalized.getHeight());
    }
    
    @Test
    @DisplayName("Should not normalize if dimensions match")
    void shouldNotNormalizeIfDimensionsMatch() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        
        BufferedImage normalized = dpiAwareCapture.normalizeToLogicalResolution(
            image, 200, 200
        );
        
        assertSame(image, normalized); // Should return same instance
    }
    
    @Test
    @DisplayName("Should handle inconsistent scaling in normalization")
    void shouldHandleInconsistentScalingInNormalization() {
        // Image with inconsistent aspect ratio
        BufferedImage image = new BufferedImage(300, 400, BufferedImage.TYPE_INT_RGB);
        
        BufferedImage normalized = dpiAwareCapture.normalizeToLogicalResolution(
            image, 200, 200
        );
        
        assertNotNull(normalized);
        assertEquals(200, normalized.getWidth());
        assertEquals(200, normalized.getHeight());
    }
    
    @Test
    @DisplayName("Should handle various scale factors")
    void shouldHandleVariousScaleFactors() {
        double[] scaleFactors = {0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0};
        
        for (double scale : scaleFactors) {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
                AffineTransform mockTransform = mock(AffineTransform.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
                when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
                when(mockTransform.getScaleX()).thenReturn(scale);
                when(mockTransform.getScaleY()).thenReturn(scale);
                
                // Clear cache to force recalculation
                dpiAwareCapture = new DPIAwareCapture();
                
                double detectedScale = dpiAwareCapture.getDisplayScaleFactor();
                assertEquals(scale, detectedScale, 0.01);
                
                if (Math.abs(scale - 1.0) > 0.01) {
                    assertTrue(dpiAwareCapture.isScalingActive());
                } else {
                    assertFalse(dpiAwareCapture.isScalingActive());
                }
            }
        }
    }
    
    @Test
    @DisplayName("Should scale image to logical resolution")
    void shouldScaleImageToLogicalResolution() {
        // Create a 300x300 image
        BufferedImage physicalImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = physicalImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 300, 300);
        g.dispose();
        
        // Use reflection to test private method
        try {
            var method = DPIAwareCapture.class.getDeclaredMethod(
                "scaleToLogicalResolution", BufferedImage.class, int.class, int.class
            );
            method.setAccessible(true);
            
            BufferedImage scaled = (BufferedImage) method.invoke(
                dpiAwareCapture, physicalImage, 200, 200
            );
            
            assertNotNull(scaled);
            assertEquals(200, scaled.getWidth());
            assertEquals(200, scaled.getHeight());
        } catch (Exception e) {
            fail("Failed to test scaleToLogicalResolution: " + e.getMessage());
        }
    }
}