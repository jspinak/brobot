package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.startup.BrobotStartup;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.Pattern;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.basics.Settings;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that verify physical resolution handling with screenshots.
 * This ensures Brobot processes images at the correct resolution.
 */
public class PhysicalResolutionTest {
    
    @Test
    public void testPhysicalResolutionWithScreenshots() throws IOException {
        // Initialize Brobot with physical resolution
        new BrobotStartup();
        
        System.out.println("\n=== Physical Resolution Screenshot Test ===");
        
        // Load screenshots from library folder
        String screenshotDir = "screenshots/";
        Path screenshotPath = Paths.get(screenshotDir, "floranext4.png");
        File screenshotFile = screenshotPath.toFile();
        
        if (!screenshotFile.exists()) {
            System.out.println("Screenshot not found: " + screenshotFile.getAbsolutePath());
            System.out.println("Skipping test");
            return;
        }
        
        // Load and analyze screenshot
        BufferedImage image = ImageIO.read(screenshotFile);
        
        System.out.println("Screenshot dimensions: " + image.getWidth() + "x" + image.getHeight());
        System.out.println("Image type: " + image.getType());
        System.out.println("Color model: " + image.getColorModel().getClass().getSimpleName());
        
        // Create ScreenImage for pattern matching
        ScreenImage screenImage = new ScreenImage(
            new Rectangle(0, 0, image.getWidth(), image.getHeight()),
            image);
        
        // Test pattern matching at physical resolution
        String imageDir = "images/";
        Path patternPath = Paths.get(imageDir, "bottomRight3.png");
        File patternFile = patternPath.toFile();
        
        if (patternFile.exists()) {
            BufferedImage patternImg = ImageIO.read(patternFile);
            Pattern pattern = new Pattern(patternFile.getAbsolutePath()).similar(0.7);
            
            System.out.println("\nPattern dimensions: " + patternImg.getWidth() + "x" + patternImg.getHeight());
            
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                Rectangle rect = match.getRect();
                
                System.out.println("Match found at: " + rect.x + ", " + rect.y);
                System.out.println("Match dimensions: " + rect.width + "x" + rect.height);
                
                // Verify dimensions are preserved
                assertEquals(patternImg.getWidth(), rect.width,
                    "Pattern width should be preserved at physical resolution");
                assertEquals(patternImg.getHeight(), rect.height,
                    "Pattern height should be preserved at physical resolution");
                
                System.out.println("✓ SUCCESS: Physical resolution dimensions preserved");
            } else {
                System.out.println("Pattern not found in screenshot");
            }
        }
        
        // Save analysis for verification if needed
        File outputDir = new File("target/test-screenshots");
        outputDir.mkdirs();
        File outputFile = new File(outputDir, "physical-resolution-test.png");
        ImageIO.write(image, "PNG", outputFile);
        System.out.println("\nAnalysis saved to: " + outputFile.getAbsolutePath());
        
        System.out.println("=======================================\n");
    }
    
    @Test
    public void testDPIScalingWithScreenshots() throws IOException {
        System.out.println("\n=== DPI Scaling Test with Screenshots ===");
        
        // Test with different screenshot sizes to verify scaling handling
        String screenshotDir = "screenshots/";
        String[] screenshots = {"floranext0.png", "floranext1.png", "floranext2.png"};
        
        for (String screenshotName : screenshots) {
            Path screenshotPath = Paths.get(screenshotDir, screenshotName);
            File screenshotFile = screenshotPath.toFile();
            
            if (!screenshotFile.exists()) {
                continue;
            }
            
            BufferedImage img = ImageIO.read(screenshotFile);
            System.out.println("\n" + screenshotName + ": " + img.getWidth() + "x" + img.getHeight());
            
            // Create a sub-region to test scaling
            int regionWidth = 200;
            int regionHeight = 200;
            
            if (img.getWidth() >= regionWidth && img.getHeight() >= regionHeight) {
                BufferedImage subImage = img.getSubimage(100, 100, regionWidth, regionHeight);
                
                // Verify sub-image dimensions
                assertEquals(regionWidth, subImage.getWidth(),
                    "Sub-image width should match requested size");
                assertEquals(regionHeight, subImage.getHeight(),
                    "Sub-image height should match requested size");
                
                System.out.println("  ✓ Sub-region extraction works correctly");
                
                // Test pattern matching in sub-region
                ScreenImage screenImage = new ScreenImage(
                    new Rectangle(0, 0, regionWidth, regionHeight),
                    subImage);
                
                // Try to find a small pattern
                String imageDir = "images/";
                Path patternPath = Paths.get(imageDir, "topLeft.png");
                File patternFile = patternPath.toFile();
                
                if (patternFile.exists()) {
                    BufferedImage patternImg = ImageIO.read(patternFile);
                    Pattern pattern = new Pattern(patternFile.getAbsolutePath()).similar(0.6);
                    
                    Finder finder = new Finder(screenImage);
                    finder.find(pattern);
                    
                    if (finder.hasNext()) {
                        Match match = finder.next();
                        Rectangle rect = match.getRect();
                        
                        // Verify dimensions are preserved in sub-region
                        assertEquals(patternImg.getWidth(), rect.width,
                            "Pattern width preserved in sub-region");
                        assertEquals(patternImg.getHeight(), rect.height,
                            "Pattern height preserved in sub-region");
                        
                        System.out.println("  ✓ Pattern matching works in sub-region");
                    }
                }
            }
        }
        
        System.out.println("\n=====================================\n");
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