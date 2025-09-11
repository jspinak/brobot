package io.github.jspinak.brobot.util.image.capture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.core.services.SikuliScreenCapture;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive test class for DirectRobotCapture functionality. Tests direct screen capture
 * operations using SikuliX Screen API.
 */
@ExtendWith(MockitoExtension.class)
@DisabledInCI
public class DirectRobotCaptureTest extends BrobotTestBase {

    private DirectRobotCapture directRobotCapture;
    private BufferedImage testImage;

    @Mock private SikuliScreenCapture mockScreenCapture;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create test image
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        if (g != null) {
            g.setColor(Color.RED);
            g.fillRect(0, 0, 100, 100);
            g.setColor(Color.WHITE);
            g.fillRect(25, 25, 50, 50);
            g.dispose();
        }
    }

    @Test
    @DisplayName("Should initialize screen capture successfully")
    void shouldInitializeScreenCaptureSuccessfully() {
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);
        assertNotNull(directRobotCapture);
    }

    @Test
    @DisplayName("Should handle null screen capture gracefully")
    void shouldHandleNullScreenCaptureGracefully() {
        directRobotCapture = new DirectRobotCapture(null);
        assertNotNull(directRobotCapture);

        // Should return null when screen capture is not initialized
        BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);
        assertNull(result);
    }

    @Test
    @DisplayName("Should capture region successfully")
    void shouldCaptureRegionSuccessfully() {
        // Arrange
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(testImage);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureRegion(10, 20, 100, 100);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
        verify(mockScreenCapture).captureRegion(10, 20, 100, 100);
    }

    @Test
    @DisplayName("Should handle capture exception")
    void shouldHandleCaptureException() {
        // Arrange
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Capture failed"));
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should capture full screen")
    void shouldCaptureFullScreen() {
        // Arrange
        BufferedImage fullScreenImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        when(mockScreenCapture.captureScreen()).thenReturn(fullScreenImage);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureFullScreen();

        // Assert
        assertNotNull(result);
        assertEquals(1920, result.getWidth());
        assertEquals(1080, result.getHeight());
        verify(mockScreenCapture).captureScreen();
    }

    @Test
    @DisplayName("Should handle full screen capture with null result")
    void shouldHandleFullScreenCaptureWithNullRobot() {
        // Arrange
        when(mockScreenCapture.captureScreen()).thenReturn(null);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureFullScreen();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle full screen capture exception")
    void shouldHandleFullScreenCaptureException() {
        // Arrange
        when(mockScreenCapture.captureScreen())
                .thenThrow(new RuntimeException("Screen capture failed"));
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureFullScreen();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should compare with Sikuli capture - same size")
    void shouldCompareWithSikuliCaptureSameSize() {
        // Arrange
        BufferedImage sikuliImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(testImage);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act & Assert
        assertDoesNotThrow(
                () -> directRobotCapture.compareWithSikuliCapture(sikuliImage, 0, 0, 100, 100));
    }

    @Test
    @DisplayName("Should compare with Sikuli capture - different size")
    void shouldCompareWithSikuliCaptureDifferentSize() {
        // Arrange
        BufferedImage sikuliImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(testImage);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act & Assert
        assertDoesNotThrow(
                () -> directRobotCapture.compareWithSikuliCapture(sikuliImage, 0, 0, 100, 100));
    }

    @Test
    @DisplayName("Should handle null Sikuli capture in comparison")
    void shouldHandleNullSikuliCaptureInComparison() {
        // Arrange
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(testImage);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act & Assert
        assertDoesNotThrow(() -> directRobotCapture.compareWithSikuliCapture(null, 0, 0, 100, 100));
    }

    @Test
    @DisplayName("Should handle null direct capture in comparison")
    void shouldHandleNullDirectCaptureInComparison() {
        // Arrange
        BufferedImage sikuliImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(null);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act & Assert
        assertDoesNotThrow(
                () -> directRobotCapture.compareWithSikuliCapture(sikuliImage, 0, 0, 100, 100));
    }

    @Test
    @DisplayName("Should get image type string")
    void shouldGetImageTypeString() {
        // Arrange
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act & Assert - Test via captured image (indirectly tests private method)
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(testImage);
        BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);
        assertNotNull(result);
        // The method is called internally and logs the type
    }

    @Test
    @DisplayName("Should handle zero-size region")
    void shouldHandleZeroSizeRegion() {
        // Arrange
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureRegion(0, 0, 0, 100);

        // Assert
        assertNull(result);
        verify(mockScreenCapture, never()).captureRegion(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should handle negative dimensions")
    void shouldHandleNegativeDimensions() {
        // Arrange
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureRegion(0, 0, -10, -10);

        // Assert
        assertNull(result);
        verify(mockScreenCapture, never()).captureRegion(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should handle Robot initialization failure")
    void shouldHandleRobotInitializationFailure() {
        // This test verifies backward compatibility with no-arg constructor
        directRobotCapture = new DirectRobotCapture();
        assertNotNull(directRobotCapture);

        // Should still work with default SikuliScreenCapture
        // In mock mode, this will return null
        BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);
        // Result depends on mock mode settings
    }

    @Test
    @DisplayName("Should log center pixel color")
    void shouldLogCenterPixelColor() {
        // Arrange
        when(mockScreenCapture.captureRegion(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(testImage);
        directRobotCapture = new DirectRobotCapture(mockScreenCapture);

        // Act
        BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);

        // Assert
        assertNotNull(result);
        // The center pixel color is logged internally
        // We verify the image was captured successfully
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }
}
