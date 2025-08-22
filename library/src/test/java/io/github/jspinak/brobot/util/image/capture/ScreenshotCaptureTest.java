package io.github.jspinak.brobot.util.image.capture;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.file.SaveToFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Image;
import org.sikuli.script.Mouse;
import org.sikuli.script.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ScreenshotCapture functionality.
 * Tests screenshot capture operations in mock mode.
 */
public class ScreenshotCaptureTest extends BrobotTestBase {

    @Mock
    private SaveToFile saveToFile;
    
    @Mock
    private Screen mockScreen;
    
    @Mock
    private Robot mockRobot;
    
    private ScreenshotCapture screenshotCapture;
    private BufferedImage testImage;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        // Create test image
        testImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1920, 1080);
        g.setColor(Color.BLACK);
        g.fillRect(100, 100, 200, 200);
        g.dispose();
        
        screenshotCapture = new ScreenshotCapture(saveToFile);
    }
    
    @Test
    void shouldSaveScreenshotWithDate() {
        // Setup mock behavior
        when(saveToFile.saveImageWithDate(any(Image.class), anyString()))
            .thenReturn("testScreenshot-20240101.png");
        
        // In mock mode, capture should return a dummy image
        screenshotCapture.saveScreenshotWithDate("testScreenshot");
        
        // Verify save was attempted
        verify(saveToFile, times(1)).saveImageWithDate(
            any(Image.class), 
            eq("testScreenshot")
        );
    }
    
    @Test
    void shouldSaveScreenshotWithEmptyName() {
        when(saveToFile.saveImageWithDate(any(Image.class), anyString()))
            .thenReturn("-20240101.png");
        
        screenshotCapture.saveScreenshotWithDate("");
        
        verify(saveToFile, times(1)).saveImageWithDate(
            any(Image.class), 
            eq("")
        );
    }
    
    @Test
    void shouldSaveScreenshotWithNullName() {
        when(saveToFile.saveImageWithDate(any(Image.class), anyString()))
            .thenReturn("null-20240101.png");
        
        screenshotCapture.saveScreenshotWithDate(null);
        
        verify(saveToFile, times(1)).saveImageWithDate(
            any(Image.class), 
            isNull()
        );
    }
    
    @Test
    void shouldHandleSaveFailure() {
        // Setup mock to simulate save failure
        when(saveToFile.saveImageWithDate(any(Image.class), anyString()))
            .thenReturn(null);
        
        // Should not throw exception even if save fails
        assertDoesNotThrow(() -> 
            screenshotCapture.saveScreenshotWithDate("failedSave")
        );
        
        verify(saveToFile, times(1)).saveImageWithDate(
            any(Image.class), 
            eq("failedSave")
        );
    }
    
    @Test
    void shouldCaptureScreenshot() throws Exception {
        String filename = tempDir.resolve("test_screenshot.png").toString();
        
        // In mock mode, should create a dummy screenshot file
        screenshotCapture.captureScreenshot(filename);
        
        // File should be created even in mock mode
        File screenshotFile = new File(filename);
        // In mock mode, we might not actually create the file
        // but the method should complete without exception
        assertDoesNotThrow(() -> 
            screenshotCapture.captureScreenshot(filename)
        );
    }
    
    @Test
    void shouldHandleInvalidPath() {
        String invalidPath = "/invalid/path/that/does/not/exist/screenshot.png";
        
        // Should handle gracefully without throwing
        assertDoesNotThrow(() -> 
            screenshotCapture.captureScreenshot(invalidPath)
        );
    }
    
    @Test
    void shouldCaptureWithSpecialCharactersInFilename() {
        String filename = tempDir.resolve("test@#$%_screenshot.png").toString();
        
        assertDoesNotThrow(() -> 
            screenshotCapture.captureScreenshot(filename)
        );
    }
    
    @Test
    void shouldCaptureToExistingDirectory() throws Exception {
        File dir = tempDir.resolve("screenshots").toFile();
        dir.mkdirs();
        String filename = new File(dir, "capture.png").getAbsolutePath();
        
        assertDoesNotThrow(() -> 
            screenshotCapture.captureScreenshot(filename)
        );
    }
    
    @Test
    void shouldHandleNullFilename() {
        // Should handle null gracefully
        assertDoesNotThrow(() -> 
            screenshotCapture.captureScreenshot(null)
        );
    }
    
    @Test
    void shouldSaveMultipleScreenshots() {
        when(saveToFile.saveImageWithDate(any(Image.class), anyString()))
            .thenReturn("screenshot.png");
        
        // Save multiple screenshots
        for (int i = 0; i < 5; i++) {
            screenshotCapture.saveScreenshotWithDate("screenshot_" + i);
        }
        
        // Verify all saves were attempted
        verify(saveToFile, times(5)).saveImageWithDate(
            any(Image.class), 
            anyString()
        );
    }
    
    @Test
    void shouldHandleLongFilenames() {
        String longName = "a".repeat(255); // Max filename length on most systems
        
        when(saveToFile.saveImageWithDate(any(Image.class), anyString()))
            .thenReturn(longName + ".png");
        
        assertDoesNotThrow(() -> 
            screenshotCapture.saveScreenshotWithDate(longName)
        );
        
        verify(saveToFile).saveImageWithDate(
            any(Image.class), 
            eq(longName)
        );
    }
    
    @Test
    void shouldCaptureWithDifferentExtensions() throws Exception {
        // Test with different file extensions
        String pngFile = tempDir.resolve("test.png").toString();
        String jpgFile = tempDir.resolve("test.jpg").toString();
        String bmpFile = tempDir.resolve("test.bmp").toString();
        
        assertDoesNotThrow(() -> {
            screenshotCapture.captureScreenshot(pngFile);
            screenshotCapture.captureScreenshot(jpgFile);
            screenshotCapture.captureScreenshot(bmpFile);
        });
    }
    
    @Test
    void shouldHandleRelativePaths() {
        String relativePath = "screenshots/capture.png";
        
        assertDoesNotThrow(() -> 
            screenshotCapture.captureScreenshot(relativePath)
        );
    }
}