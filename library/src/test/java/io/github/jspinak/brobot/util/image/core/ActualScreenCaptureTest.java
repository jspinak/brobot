package io.github.jspinak.brobot.util.image.core;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Test that actually captures a screenshot and verifies it has content.
 * This test should FAIL if screenshots are coming back black.
 */
public class ActualScreenCaptureTest {

    private static final String TEST_SCREENSHOT_PATH = "test-actual-screenshot.png";
    
    static boolean isHeadless() {
        // Check if we're actually headless (not just having DISPLAY set)
        try {
            return GraphicsEnvironment.isHeadless() || 
                   "true".equals(System.getProperty("java.awt.headless"));
        } catch (Exception e) {
            // If we can't determine, assume headless
            return true;
        }
    }

    @BeforeAll
    public static void setupEnvironment() {
        // Force non-headless mode for this test
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    public void setUp() {
        // Clean up any existing test screenshot
        File testFile = new File(TEST_SCREENSHOT_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
        
        // Create fresh ExecutionEnvironment with explicit non-headless configuration
        ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(false)
                .forceHeadless(false)
                .allowScreenCapture(true)
                .fromEnvironment()
                .build();
        
        ExecutionEnvironment.setInstance(env);
    }

    @Test
    public void testScreenCaptureHasNonBlackPixels() throws IOException {
        // Skip test if running in headless mode
        assumeFalse(isHeadless(), "Skipping test in headless environment");
        
        // Also skip if in WSL or CI environment where screen capture might not work
        assumeFalse(System.getenv("WSL_DISTRO_NAME") != null, "Skipping test in WSL environment");
        assumeFalse(System.getenv("CI") != null, "Skipping test in CI environment");
        assumeFalse(System.getenv("GITHUB_ACTIONS") != null, "Skipping test in GitHub Actions");
        
        // First verify the environment is correctly configured
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        System.out.println("=== Environment Check ===");
        System.out.println("GraphicsEnvironment.isHeadless: " + ge.isHeadless());
        System.out.println("java.awt.headless property: " + System.getProperty("java.awt.headless"));
        System.out.println("ExecutionEnvironment.hasDisplay: " + env.hasDisplay());
        System.out.println("ExecutionEnvironment.canCaptureScreen: " + env.canCaptureScreen());
        System.out.println("ExecutionEnvironment.isMockMode: " + env.isMockMode());
        
        assertTrue(env.canCaptureScreen(), 
            "ExecutionEnvironment must allow screen capture - if this fails, screen capture is disabled");
        
        // Capture screenshot using BufferedImageUtilities instance method
        Region fullScreen = new Region(); // Default constructor creates full screen region
        BufferedImageUtilities utils = new BufferedImageUtilities();
        BufferedImage screenshot = utils.getBuffImgFromScreen(fullScreen);
        
        // Basic validation
        assertNotNull(screenshot, "Screenshot must not be null");
        assertTrue(screenshot.getWidth() > 0, "Screenshot width must be positive");
        assertTrue(screenshot.getHeight() > 0, "Screenshot height must be positive");
        
        System.out.println("Screenshot dimensions: " + screenshot.getWidth() + "x" + screenshot.getHeight());
        
        // Save the screenshot to disk for manual inspection
        File screenshotFile = new File(TEST_SCREENSHOT_PATH);
        ImageIO.write(screenshot, "png", screenshotFile);
        System.out.println("Screenshot saved to: " + screenshotFile.getAbsolutePath());
        assertTrue(screenshotFile.exists(), "Screenshot file must be created");
        assertTrue(screenshotFile.length() > 0, "Screenshot file must not be empty");
        
        // Check for non-black pixels - this is the critical test
        boolean hasNonBlackPixels = checkForNonBlackPixels(screenshot);
        int[] pixelStats = getPixelStatistics(screenshot);
        
        System.out.println("=== Pixel Analysis ===");
        System.out.println("Total pixels checked: " + pixelStats[0]);
        System.out.println("Black pixels (0xFF000000): " + pixelStats[1]);
        System.out.println("Non-black pixels: " + pixelStats[2]);
        System.out.println("Percentage non-black: " + (100.0 * pixelStats[2] / pixelStats[0]) + "%");
        System.out.println("Has non-black pixels: " + hasNonBlackPixels);
        
        // Show some sample pixel values for debugging
        System.out.println("=== Sample Pixel Values ===");
        for (int y = 0; y < Math.min(5, screenshot.getHeight()); y++) {
            for (int x = 0; x < Math.min(10, screenshot.getWidth()); x++) {
                int rgb = screenshot.getRGB(x, y);
                System.out.printf("(%d,%d): 0x%08X ", x, y, rgb);
            }
            System.out.println();
        }
        
        // THIS IS THE KEY ASSERTION - if screenshots are black, this will fail
        assertTrue(hasNonBlackPixels, 
            "Screenshot must contain non-black pixels! " +
            "All black pixels indicates screen capture is not working. " +
            "Check the saved screenshot at: " + screenshotFile.getAbsolutePath());
        
        // Additional validation - at least 1% of pixels should be non-black for a real screenshot
        double nonBlackPercentage = 100.0 * pixelStats[2] / pixelStats[0];
        assertTrue(nonBlackPercentage >= 1.0, 
            "At least 1% of pixels should be non-black for a real screenshot, got: " + nonBlackPercentage + "%");
    }
    
    @Test
    public void testStaticScreenCaptureMethod() throws IOException {
        // Skip test if running in headless mode
        assumeFalse(isHeadless(), "Skipping test in headless environment");
        
        // Also skip if in WSL or CI environment where screen capture might not work
        assumeFalse(System.getenv("WSL_DISTRO_NAME") != null, "Skipping test in WSL environment");
        assumeFalse(System.getenv("CI") != null, "Skipping test in CI environment");
        assumeFalse(System.getenv("GITHUB_ACTIONS") != null, "Skipping test in GitHub Actions");
        // Test the static method as well
        Region fullScreen = new Region();
        BufferedImage screenshot = BufferedImageUtilities.getBufferedImageFromScreen(fullScreen);
        
        assertNotNull(screenshot, "Static method screenshot must not be null");
        assertTrue(screenshot.getWidth() > 0, "Screenshot width must be positive");
        assertTrue(screenshot.getHeight() > 0, "Screenshot height must be positive");
        
        // Save this screenshot too
        File screenshotFile = new File("test-static-screenshot.png");
        ImageIO.write(screenshot, "png", screenshotFile);
        System.out.println("Static screenshot saved to: " + screenshotFile.getAbsolutePath());
        
        boolean hasNonBlackPixels = checkForNonBlackPixels(screenshot);
        assertTrue(hasNonBlackPixels, 
            "Static method screenshot must also contain non-black pixels!");
    }

    private boolean checkForNonBlackPixels(BufferedImage image) {
        // Check a reasonable sample of pixels (up to 10000 pixels)
        int maxX = Math.min(100, image.getWidth());
        int maxY = Math.min(100, image.getHeight());
        
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                int rgb = image.getRGB(x, y);
                if (rgb != 0xFF000000) { // Not pure black (alpha=255, rgb=0)
                    return true;
                }
            }
        }
        return false;
    }
    
    private int[] getPixelStatistics(BufferedImage image) {
        int totalPixels = 0;
        int blackPixels = 0;
        int nonBlackPixels = 0;
        
        // Sample up to 10000 pixels for statistics
        int maxX = Math.min(100, image.getWidth());
        int maxY = Math.min(100, image.getHeight());
        
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                totalPixels++;
                int rgb = image.getRGB(x, y);
                if (rgb == 0xFF000000) { // Pure black
                    blackPixels++;
                } else {
                    nonBlackPixels++;
                }
            }
        }
        
        return new int[]{totalPixels, blackPixels, nonBlackPixels};
    }
}