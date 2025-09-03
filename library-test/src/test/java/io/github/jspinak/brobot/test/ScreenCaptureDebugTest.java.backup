package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified test to check screen capture functionality
 */
public class ScreenCaptureDebugTest {

    @BeforeAll
    public static void setupEnvironment() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    public void testBasicScreenCapture() {
        // Check GraphicsEnvironment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        System.out.println("GraphicsEnvironment.isHeadless: " + ge.isHeadless());
        System.out.println("java.awt.headless property: " + System.getProperty("java.awt.headless"));
        
        // Check ExecutionEnvironment
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        System.out.println("ExecutionEnvironment.hasDisplay: " + env.hasDisplay());
        System.out.println("ExecutionEnvironment.canCaptureScreen: " + env.canCaptureScreen());
        System.out.println("ExecutionEnvironment.isMockMode: " + env.isMockMode());
        
        assertTrue(env.canCaptureScreen(), "Must be able to capture screen");
        
        // Try screen capture with BufferedImageUtilities instance
        BufferedImageUtilities utils = new BufferedImageUtilities();
        Region fullScreen = new Region(); // Default is full screen
        BufferedImage screenshot = utils.getBuffImgFromScreen(fullScreen);
        
        assertNotNull(screenshot, "Screenshot must not be null");
        assertTrue(screenshot.getWidth() > 0, "Screenshot width must be positive");
        assertTrue(screenshot.getHeight() > 0, "Screenshot height must be positive");
        
        // Check if the image is not all black
        boolean hasNonBlackPixels = false;
        int maxCheck = Math.min(100, screenshot.getWidth());
        int maxY = Math.min(100, screenshot.getHeight());
        
        for (int x = 0; x < maxCheck && !hasNonBlackPixels; x++) {
            for (int y = 0; y < maxY; y++) {
                int rgb = screenshot.getRGB(x, y);
                if (rgb != 0xFF000000) { // Not pure black
                    hasNonBlackPixels = true;
                    break;
                }
            }
        }
        
        System.out.println("Screenshot dimensions: " + screenshot.getWidth() + "x" + screenshot.getHeight());
        System.out.println("Has non-black pixels: " + hasNonBlackPixels);
        
        assertTrue(hasNonBlackPixels, "Screenshot must contain non-black pixels");
    }
    
    @Test 
    public void testStaticScreenCapture() {
        // Try static method
        Region fullScreen = new Region();
        BufferedImage screenshot = BufferedImageUtilities.getBufferedImageFromScreen(fullScreen);
        
        assertNotNull(screenshot, "Static screenshot must not be null");
        assertTrue(screenshot.getWidth() > 0, "Screenshot width must be positive");
        assertTrue(screenshot.getHeight() > 0, "Screenshot height must be positive");
    }
}