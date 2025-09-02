package io.github.jspinak.brobot.util.image.capture;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for UIScaleNormalizer functionality.
 * Tests UI scale detection and normalization for pattern matching.
 */
@ExtendWith(MockitoExtension.class)
public class UIScaleNormalizerTest extends BrobotTestBase {

    private UIScaleNormalizer uiScaleNormalizer;
    private BufferedImage testImage;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        uiScaleNormalizer = new UIScaleNormalizer();
        
        // Create test image with UI elements
        testImage = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 400, 300);
        g.setColor(Color.WHITE);
        g.fillRect(50, 50, 100, 80); // UI element
        g.dispose();
    }
    
    @Test
    @DisplayName("Should detect UI scale from image")
    void shouldDetectUIScaleFromImage() {
        UIScaleNormalizer.UIScale scale = uiScaleNormalizer.detectUIScale(testImage);
        
        assertNotNull(scale);
        assertTrue(scale.getScaleFactor() > 0);
        assertTrue(scale.getUiElementWidth() >= 0);
        assertTrue(scale.getUiElementHeight() >= 0);
    }
    
    @Test
    @DisplayName("Should handle null image")
    void shouldHandleNullImage() {
        UIScaleNormalizer.UIScale scale = uiScaleNormalizer.detectUIScale(null);
        
        assertNotNull(scale);
        assertEquals(1.0, scale.getScaleFactor());
        assertEquals(0, scale.getUiElementWidth());
        assertEquals(0, scale.getUiElementHeight());
    }
    
    @Test
    @DisplayName("Should detect bright UI elements")
    void shouldDetectBrightUIElements() {
        // Create image with bright UI element
        BufferedImage brightImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = brightImage.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, 200, 200);
        g.setColor(new Color(250, 250, 250)); // Very bright
        g.fillRect(25, 25, 150, 150);
        g.dispose();
        
        UIScaleNormalizer.UIScale scale = uiScaleNormalizer.detectUIScale(brightImage);
        
        assertNotNull(scale);
        assertTrue(scale.getUiElementWidth() > 0);
        assertTrue(scale.getUiElementHeight() > 0);
    }
    
    @Test
    @DisplayName("Should handle dark image without UI elements")
    void shouldHandleDarkImageWithoutUIElements() {
        // Create completely dark image
        BufferedImage darkImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = darkImage.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 200, 200);
        g.dispose();
        
        UIScaleNormalizer.UIScale scale = uiScaleNormalizer.detectUIScale(darkImage);
        
        assertNotNull(scale);
        assertEquals(1.0, scale.getScaleFactor());
    }
    
    @Test
    @DisplayName("Should normalize images with same scale")
    void shouldNormalizeImagesWithSameScale() {
        BufferedImage pattern = createTestImage(100, 100, 20, 20, 60, 60);
        BufferedImage scene = createTestImage(400, 300, 100, 100, 60, 60);
        
        BufferedImage normalized = uiScaleNormalizer.normalizeScales(pattern, scene);
        
        assertNotNull(normalized);
        // When scales are similar, pattern should be returned unchanged
        assertEquals(pattern.getWidth(), normalized.getWidth());
        assertEquals(pattern.getHeight(), normalized.getHeight());
    }
    
    @Test
    @DisplayName("Should normalize images with different scales")
    void shouldNormalizeImagesWithDifferentScales() {
        // Pattern with small UI element
        BufferedImage pattern = createTestImage(100, 100, 20, 20, 30, 30);
        // Scene with large UI element (simulating zoom)
        BufferedImage scene = createTestImage(400, 300, 100, 100, 120, 120);
        
        BufferedImage normalized = uiScaleNormalizer.normalizeScales(pattern, scene);
        
        assertNotNull(normalized);
        // Should scale pattern to match scene's UI scale
        assertTrue(normalized.getWidth() != pattern.getWidth() || 
                  normalized.getHeight() != pattern.getHeight());
    }
    
    @Test
    @DisplayName("Should handle null pattern in normalization")
    void shouldHandleNullPatternInNormalization() {
        BufferedImage scene = createTestImage(400, 300, 100, 100, 60, 60);
        
        BufferedImage normalized = uiScaleNormalizer.normalizeScales(null, scene);
        
        assertNull(normalized);
    }
    
    @Test
    @DisplayName("Should handle null scene in normalization")
    void shouldHandleNullSceneInNormalization() {
        BufferedImage pattern = createTestImage(100, 100, 20, 20, 60, 60);
        
        BufferedImage normalized = uiScaleNormalizer.normalizeScales(pattern, null);
        
        assertNotNull(normalized);
        assertEquals(pattern, normalized);
    }
    
    @Test
    @DisplayName("Should handle both null images in normalization")
    void shouldHandleBothNullImagesInNormalization() {
        BufferedImage normalized = uiScaleNormalizer.normalizeScales(null, null);
        
        assertNull(normalized);
    }
    
    @Test
    @DisplayName("Should scale image correctly")
    void shouldScaleImageCorrectly() {
        BufferedImage original = createTestImage(100, 100, 10, 10, 80, 80);
        double scaleFactor = 2.0;
        
        BufferedImage scaled = uiScaleNormalizer.scaleImage(original, scaleFactor);
        
        assertNotNull(scaled);
        assertEquals((int)(original.getWidth() * scaleFactor), scaled.getWidth());
        assertEquals((int)(original.getHeight() * scaleFactor), scaled.getHeight());
    }
    
    @Test
    @DisplayName("Should handle scale factor of 1.0")
    void shouldHandleScaleFactorOfOne() {
        BufferedImage original = createTestImage(100, 100, 10, 10, 80, 80);
        
        BufferedImage scaled = uiScaleNormalizer.scaleImage(original, 1.0);
        
        assertNotNull(scaled);
        assertEquals(original, scaled); // Should return same instance
    }
    
    @Test
    @DisplayName("Should handle very small scale factor")
    void shouldHandleVerySmallScaleFactor() {
        BufferedImage original = createTestImage(100, 100, 10, 10, 80, 80);
        
        BufferedImage scaled = uiScaleNormalizer.scaleImage(original, 0.1);
        
        assertNotNull(scaled);
        assertEquals(10, scaled.getWidth());
        assertEquals(10, scaled.getHeight());
    }
    
    @Test
    @DisplayName("Should handle very large scale factor")
    void shouldHandleVeryLargeScaleFactor() {
        BufferedImage original = createTestImage(10, 10, 2, 2, 6, 6);
        
        BufferedImage scaled = uiScaleNormalizer.scaleImage(original, 10.0);
        
        assertNotNull(scaled);
        assertEquals(100, scaled.getWidth());
        assertEquals(100, scaled.getHeight());
    }
    
    @Test
    @DisplayName("Should handle null image in scaling")
    void shouldHandleNullImageInScaling() {
        BufferedImage scaled = uiScaleNormalizer.scaleImage(null, 2.0);
        
        assertNull(scaled);
    }
    
    @Test
    @DisplayName("Should detect scale from complex UI pattern")
    void shouldDetectScaleFromComplexUIPattern() {
        // Create image with multiple UI elements
        BufferedImage complexImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = complexImage.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, 800, 600);
        
        // Multiple bright UI elements
        g.setColor(Color.WHITE);
        g.fillRect(50, 50, 100, 30);   // Top bar
        g.fillRect(50, 100, 200, 400); // Side panel
        g.fillRect(300, 100, 450, 400); // Main content
        g.dispose();
        
        UIScaleNormalizer.UIScale scale = uiScaleNormalizer.detectUIScale(complexImage);
        
        assertNotNull(scale);
        assertTrue(scale.getUiElementWidth() > 0);
        assertTrue(scale.getUiElementHeight() > 0);
    }
    
    @Test
    @DisplayName("Should handle images with different color depths")
    void shouldHandleImagesWithDifferentColorDepths() {
        BufferedImage[] images = {
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY),
            new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR)
        };
        
        for (BufferedImage img : images) {
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(10, 10, 80, 80);
            g.dispose();
            
            UIScaleNormalizer.UIScale scale = uiScaleNormalizer.detectUIScale(img);
            assertNotNull(scale);
        }
    }
    
    @Test
    @DisplayName("Should validate UIScale class")
    void shouldValidateUIScaleClass() {
        UIScaleNormalizer.UIScale scale = new UIScaleNormalizer.UIScale(1.5, 100, 80);
        
        assertEquals(1.5, scale.getScaleFactor());
        assertEquals(100, scale.getUiElementWidth());
        assertEquals(80, scale.getUiElementHeight());
        
        // Test with zero values
        UIScaleNormalizer.UIScale zeroScale = new UIScaleNormalizer.UIScale(0, 0, 0);
        assertEquals(0, zeroScale.getScaleFactor());
        assertEquals(0, zeroScale.getUiElementWidth());
        assertEquals(0, zeroScale.getUiElementHeight());
        
        // Test with negative values (edge case)
        UIScaleNormalizer.UIScale negativeScale = new UIScaleNormalizer.UIScale(-1, -10, -20);
        assertEquals(-1, negativeScale.getScaleFactor());
        assertEquals(-10, negativeScale.getUiElementWidth());
        assertEquals(-20, negativeScale.getUiElementHeight());
    }
    
    // Helper method to create test images with UI elements
    private BufferedImage createTestImage(int width, int height, int uiX, int uiY, int uiWidth, int uiHeight) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.fillRect(uiX, uiY, uiWidth, uiHeight);
        g.dispose();
        return image;
    }
}