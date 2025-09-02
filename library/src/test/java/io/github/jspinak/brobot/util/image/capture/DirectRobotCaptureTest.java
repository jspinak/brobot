package io.github.jspinak.brobot.util.image.capture;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for DirectRobotCapture functionality.
 * Tests direct screen capture operations with proper mocking for headless environments.
 */
@ExtendWith(MockitoExtension.class)
public class DirectRobotCaptureTest extends BrobotTestBase {

    private DirectRobotCapture directRobotCapture;
    private BufferedImage testImage;
    
    @Mock
    private Robot mockRobot;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Create test image
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.setColor(Color.WHITE);
        g.fillRect(25, 25, 50, 50);
        g.dispose();
    }
    
    @Test
    @DisplayName("Should initialize Robot successfully")
    void shouldInitializeRobotSuccessfully() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class)) {
            directRobotCapture = new DirectRobotCapture();
            assertNotNull(directRobotCapture);
            assertEquals(1, robotMock.constructed().size());
        }
    }
    
    @Test
    @DisplayName("Should handle Robot initialization failure")
    void shouldHandleRobotInitializationFailure() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            throw new AWTException("Headless environment");
        })) {
            directRobotCapture = new DirectRobotCapture();
            assertNotNull(directRobotCapture);
            
            // Should return null when Robot is not initialized
            BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);
            assertNull(result);
        }
    }
    
    @Test
    @DisplayName("Should capture region successfully")
    void shouldCaptureRegionSuccessfully() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(testImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            BufferedImage result = directRobotCapture.captureRegion(10, 20, 100, 100);
            
            assertNotNull(result);
            assertEquals(100, result.getWidth());
            assertEquals(100, result.getHeight());
            
            Robot robot = robotMock.constructed().get(0);
            verify(robot).createScreenCapture(new Rectangle(10, 20, 100, 100));
        }
    }
    
    @Test
    @DisplayName("Should handle capture exception")
    void shouldHandleCaptureException() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class)))
                .thenThrow(new RuntimeException("Capture failed"));
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);
            assertNull(result);
        }
    }
    
    @Test
    @DisplayName("Should capture full screen")
    void shouldCaptureFullScreen() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(testImage);
        })) {
            try (MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                Toolkit mockToolkit = mock(Toolkit.class);
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
                
                directRobotCapture = new DirectRobotCapture();
                BufferedImage result = directRobotCapture.captureFullScreen();
                
                assertNotNull(result);
                assertEquals(100, result.getWidth()); // Returns testImage
                assertEquals(100, result.getHeight());
                
                Robot robot = robotMock.constructed().get(0);
                verify(robot).createScreenCapture(new Rectangle(0, 0, 1920, 1080));
            }
        }
    }
    
    @Test
    @DisplayName("Should handle full screen capture with null robot")
    void shouldHandleFullScreenCaptureWithNullRobot() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            throw new AWTException("No robot");
        })) {
            directRobotCapture = new DirectRobotCapture();
            BufferedImage result = directRobotCapture.captureFullScreen();
            assertNull(result);
        }
    }
    
    @Test
    @DisplayName("Should compare with Sikuli capture - same size")
    void shouldCompareWithSikuliCaptureSameSize() {
        BufferedImage sikuliImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(testImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            assertDoesNotThrow(() -> 
                directRobotCapture.compareWithSikuliCapture(sikuliImage, 0, 0, 100, 100)
            );
        }
    }
    
    @Test
    @DisplayName("Should compare with Sikuli capture - different size")
    void shouldCompareWithSikuliCaptureDifferentSize() {
        BufferedImage sikuliImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(testImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            assertDoesNotThrow(() -> 
                directRobotCapture.compareWithSikuliCapture(sikuliImage, 0, 0, 100, 100)
            );
        }
    }
    
    @Test
    @DisplayName("Should handle null Sikuli capture in comparison")
    void shouldHandleNullSikuliCaptureInComparison() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(testImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            assertDoesNotThrow(() -> 
                directRobotCapture.compareWithSikuliCapture(null, 0, 0, 100, 100)
            );
        }
    }
    
    @Test
    @DisplayName("Should handle null direct capture in comparison")
    void shouldHandleNullDirectCaptureInComparison() {
        BufferedImage sikuliImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(null);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            assertDoesNotThrow(() -> 
                directRobotCapture.compareWithSikuliCapture(sikuliImage, 0, 0, 100, 100)
            );
        }
    }
    
    @Test
    @DisplayName("Should detect pixel differences in comparison")
    void shouldDetectPixelDifferencesInComparison() {
        // Create different colored images
        BufferedImage sikuliImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sikuliImage.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(testImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            assertDoesNotThrow(() -> 
                directRobotCapture.compareWithSikuliCapture(sikuliImage, 0, 0, 100, 100)
            );
        }
    }
    
    @Test
    @DisplayName("Should handle various image types")
    void shouldHandleVariousImageTypes() {
        BufferedImage[] testImages = {
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR),
            new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY)
        };
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class)) {
            directRobotCapture = new DirectRobotCapture();
            
            for (BufferedImage img : testImages) {
                Robot robot = robotMock.constructed().get(0);
                when(robot.createScreenCapture(any(Rectangle.class))).thenReturn(img);
                
                BufferedImage result = directRobotCapture.captureRegion(0, 0, 100, 100);
                assertNotNull(result);
                assertEquals(100, result.getWidth());
                assertEquals(100, result.getHeight());
            }
        }
    }
    
    @Test
    @DisplayName("Should handle zero-size region")
    void shouldHandleZeroSizeRegion() {
        BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(emptyImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            BufferedImage result = directRobotCapture.captureRegion(0, 0, 0, 0);
            assertNotNull(result);
        }
    }
    
    @Test
    @DisplayName("Should handle negative coordinates")
    void shouldHandleNegativeCoordinates() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(testImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            BufferedImage result = directRobotCapture.captureRegion(-10, -20, 100, 100);
            assertNotNull(result);
            
            Robot robot = robotMock.constructed().get(0);
            verify(robot).createScreenCapture(new Rectangle(-10, -20, 100, 100));
        }
    }
    
    @Test
    @DisplayName("Should handle large capture regions")
    void shouldHandleLargeCaptureRegions() {
        BufferedImage largeImage = new BufferedImage(4096, 2160, BufferedImage.TYPE_INT_RGB);
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(largeImage);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            BufferedImage result = directRobotCapture.captureRegion(0, 0, 4096, 2160);
            assertNotNull(result);
            assertEquals(4096, result.getWidth());
            assertEquals(2160, result.getHeight());
        }
    }
    
    @Test
    @DisplayName("Should verify center pixel logging")
    void shouldVerifyCenterPixelLogging() {
        // Create image with specific center pixel color
        BufferedImage imageWithCenter = new BufferedImage(101, 101, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imageWithCenter.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 101, 101);
        g.setColor(Color.MAGENTA);
        g.fillRect(50, 50, 1, 1); // Center pixel
        g.dispose();
        
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(imageWithCenter);
        })) {
            directRobotCapture = new DirectRobotCapture();
            
            BufferedImage result = directRobotCapture.captureRegion(0, 0, 101, 101);
            assertNotNull(result);
            
            // Verify center pixel color
            int centerX = result.getWidth() / 2;
            int centerY = result.getHeight() / 2;
            int rgb = result.getRGB(centerX, centerY);
            assertEquals(Color.MAGENTA.getRGB(), rgb);
        }
    }
    
    @Test
    @DisplayName("Should handle full screen capture exception")
    void shouldHandleFullScreenCaptureException() {
        try (MockedConstruction<Robot> robotMock = mockConstruction(Robot.class, (mock, context) -> {
            when(mock.createScreenCapture(any(Rectangle.class)))
                .thenThrow(new RuntimeException("Screen capture failed"));
        })) {
            try (MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                Toolkit mockToolkit = mock(Toolkit.class);
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
                
                directRobotCapture = new DirectRobotCapture();
                BufferedImage result = directRobotCapture.captureFullScreen();
                assertNull(result);
            }
        }
    }
}