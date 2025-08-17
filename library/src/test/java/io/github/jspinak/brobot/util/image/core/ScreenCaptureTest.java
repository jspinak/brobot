package io.github.jspinak.brobot.util.image.core;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Unit test for screen capture functionality.
 * Tests the core screen capture mechanism to identify why illustrations are black.
 */
public class ScreenCaptureTest {

    @BeforeAll
    public static void setupEnvironment() {
        // Force non-headless mode for this test
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    public void setUp() {
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
    public void testGraphicsEnvironmentAccess() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        // When java.awt.headless=false is set, GraphicsEnvironment should not be headless
        // However, on some systems it may still report headless - that's when ExecutionEnvironment override helps
        boolean isHeadless = ge.isHeadless();
        
        if (isHeadless) {
            // If GraphicsEnvironment reports headless, ExecutionEnvironment should still work
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            assertTrue(env.hasDisplay(), 
                "ExecutionEnvironment must override headless detection when java.awt.headless=false");
            assertTrue(env.canCaptureScreen(), 
                "ExecutionEnvironment must allow screen capture when java.awt.headless=false");
        } else {
            // If not headless, verify screen devices are accessible
            var devices = ge.getScreenDevices();
            assertTrue(devices.length > 0, "Must have at least one screen device");
            
            // Verify at least one device has valid display mode
            boolean hasValidDisplay = false;
            for (var device : devices) {
                var mode = device.getDisplayMode();
                if (mode.getWidth() > 0 && mode.getHeight() > 0) {
                    hasValidDisplay = true;
                    break;
                }
            }
            assertTrue(hasValidDisplay, "At least one screen device must have valid display mode");
        }
    }

    @Test
    public void testExecutionEnvironmentConfiguration() {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // Verify all required conditions for screen capture
        assertFalse(env.isMockMode(), "Must not be in mock mode for screen capture");
        assertTrue(env.hasDisplay(), "Must have display available");
        assertTrue(env.canCaptureScreen(), "Must be able to capture screen");
        assertTrue(env.useRealFiles(), "Must use real files for image operations");
        assertFalse(env.shouldSkipSikuliX(), "Must not skip SikuliX operations");
    }

    @Test
    public void testBufferedImageUtilitiesScreenCapture() {
        // Skip test if running in environments where screen capture might not work
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping test in headless environment");
        assumeFalse(System.getenv("WSL_DISTRO_NAME") != null, "Skipping test in WSL environment");
        assumeFalse(System.getenv("CI") != null, "Skipping test in CI environment");
        
        // Verify ExecutionEnvironment allows screen capture
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        assertTrue(env.canCaptureScreen(), 
            "ExecutionEnvironment.canCaptureScreen() must be true for screen capture to work");
        
        // Create a region for full screen capture
        Region fullScreen = new Region(); // Default constructor creates full screen region
        
        // Test static method screen capture
        BufferedImage screenshot1 = BufferedImageUtilities.getBufferedImageFromScreen(fullScreen);
        assertNotNull(screenshot1, "Static method must return non-null image");
        assertTrue(screenshot1.getWidth() > 0, "Screenshot width must be positive");
        assertTrue(screenshot1.getHeight() > 0, "Screenshot height must be positive");
        
        // Test instance method screen capture
        BufferedImageUtilities utils = new BufferedImageUtilities();
        BufferedImage screenshot2 = utils.getBuffImgFromScreen(fullScreen);
        assertNotNull(screenshot2, "Instance method must return non-null image");
        assertTrue(screenshot2.getWidth() > 0, "Screenshot width must be positive");
        assertTrue(screenshot2.getHeight() > 0, "Screenshot height must be positive");
        
        // Verify screenshots are not all black pixels
        assertTrue(checkForNonBlackPixels(screenshot1), 
            "Screenshot must contain non-black pixels - black image indicates failed screen capture");
        assertTrue(checkForNonBlackPixels(screenshot2), 
            "Screenshot must contain non-black pixels - black image indicates failed screen capture");
        
        // Verify both methods produce similar results
        assertEquals(screenshot1.getWidth(), screenshot2.getWidth(), 
            "Static and instance methods should produce same width screenshots");
        assertEquals(screenshot1.getHeight(), screenshot2.getHeight(), 
            "Static and instance methods should produce same height screenshots");
    }

    @Test
    public void testSystemPropertyOverride() {
        // Verify system property is set correctly
        String headlessProperty = System.getProperty("java.awt.headless");
        assertEquals("false", headlessProperty, "java.awt.headless must be set to false");
        
        // Verify ExecutionEnvironment respects the property
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        assertTrue(env.hasDisplay(), "ExecutionEnvironment must detect display when java.awt.headless=false");
    }

    private boolean checkForNonBlackPixels(BufferedImage image) {
        // Check first 100x100 pixels (or smaller if image is smaller)
        int maxX = Math.min(100, image.getWidth());
        int maxY = Math.min(100, image.getHeight());
        
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                int rgb = image.getRGB(x, y);
                // Check if pixel is not pure black (0xFF000000 is opaque black)
                if (rgb != 0xFF000000) {
                    return true;
                }
            }
        }
        return false;
    }
}