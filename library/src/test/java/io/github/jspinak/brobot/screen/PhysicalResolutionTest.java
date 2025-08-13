package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.startup.BrobotStartup;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ScreenImage;
import org.sikuli.basics.Settings;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that verify physical resolution capture is working correctly.
 * This ensures Brobot captures at the same resolution as SikuliX IDE.
 */
public class PhysicalResolutionTest {
    
    @Test
    public void testPhysicalScreenCapture() throws IOException {
        // Initialize Brobot with physical resolution
        new BrobotStartup();
        
        System.out.println("\n=== Physical Resolution Capture Test ===");
        
        // Create PhysicalScreen
        PhysicalScreen screen = new PhysicalScreen();
        
        // Get resolution info
        Dimension physical = screen.getPhysicalResolution();
        System.out.println("Physical Resolution: " + physical.width + "x" + physical.height);
        
        // Capture the full screen
        ScreenImage capture = screen.capture();
        BufferedImage image = capture.getImage();
        
        System.out.println("Captured Image Size: " + image.getWidth() + "x" + image.getHeight());
        
        // On Windows with 125% DPI scaling:
        // - Physical resolution should be 1920x1080
        // - Without our fix, Java would capture at 1536x864
        // - With our fix, we capture at 1920x1080 like the IDE
        
        if (isWindows() && physical.width == 1920 && physical.height == 1080) {
            // Verify we're capturing at physical resolution
            assertEquals(1920, image.getWidth(), 
                "Should capture at physical width (1920) not logical width (1536)");
            assertEquals(1080, image.getHeight(),
                "Should capture at physical height (1080) not logical height (864)");
            System.out.println("✓ SUCCESS: Capturing at PHYSICAL resolution like SikuliX IDE");
        } else {
            // On other systems or resolutions, just verify capture matches physical
            assertEquals(physical.width, image.getWidth(),
                "Capture width should match physical resolution");
            assertEquals(physical.height, image.getHeight(),
                "Capture height should match physical resolution");
            System.out.println("✓ Capture matches physical resolution");
        }
        
        // Save screenshot for manual verification if needed
        File outputDir = new File("target/test-screenshots");
        outputDir.mkdirs();
        File outputFile = new File(outputDir, "physical-capture-test.png");
        ImageIO.write(image, "PNG", outputFile);
        System.out.println("Screenshot saved to: " + outputFile.getAbsolutePath());
        
        System.out.println("=======================================\n");
    }
    
    @Test
    public void testDPIScalingCompensation() {
        System.out.println("\n=== DPI Scaling Compensation Test ===");
        
        PhysicalScreen screen = new PhysicalScreen();
        
        if (screen.isScalingCompensated()) {
            System.out.println("DPI Scaling detected and compensated");
            System.out.println("Scale Factor: " + screen.getScaleFactor());
            
            // Test coordinate scaling
            Rectangle logicalRect = new Rectangle(100, 100, 200, 200);
            ScreenImage capture = screen.capture(logicalRect);
            
            // The capture should be scaled appropriately
            BufferedImage image = capture.getImage();
            int expectedWidth = (int)(200 * screen.getScaleFactor());
            int expectedHeight = (int)(200 * screen.getScaleFactor());
            
            assertEquals(expectedWidth, image.getWidth(), 5, // Allow small rounding difference
                "Width should be scaled by DPI factor");
            assertEquals(expectedHeight, image.getHeight(), 5,
                "Height should be scaled by DPI factor");
            
            System.out.println("✓ Coordinate scaling working correctly");
        } else {
            System.out.println("No DPI scaling detected (or running on system without scaling)");
            
            // Without scaling, logical = physical
            Rectangle rect = new Rectangle(100, 100, 200, 200);
            ScreenImage capture = screen.capture(rect);
            BufferedImage image = capture.getImage();
            
            assertEquals(200, image.getWidth(), "Width should match requested size");
            assertEquals(200, image.getHeight(), "Height should match requested size");
            
            System.out.println("✓ Capture working correctly without scaling");
        }
        
        System.out.println("=====================================\n");
    }
    
    @Test
    public void testSettingsAlwaysResizeNotNeeded() {
        System.out.println("\n=== Settings.AlwaysResize Test ===");
        
        // With physical resolution capture, we shouldn't need Settings.AlwaysResize
        float alwaysResize = (float) Settings.AlwaysResize;
        
        System.out.println("Current Settings.AlwaysResize: " + alwaysResize);
        
        if (Math.abs(alwaysResize - 0) < 0.01) {
            System.out.println("✓ Settings.AlwaysResize is 0 (disabled) as expected");
            System.out.println("  Physical resolution capture makes resize unnecessary");
        } else {
            System.out.println("⚠ Settings.AlwaysResize is " + alwaysResize);
            System.out.println("  With physical resolution capture, this should be 0");
            System.out.println("  Consider setting it to 0 in application.properties");
        }
        
        System.out.println("===================================\n");
    }
    
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}