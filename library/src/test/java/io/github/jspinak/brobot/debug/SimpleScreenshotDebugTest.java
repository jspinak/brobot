package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.sikuli.interfaces.FrameworkSettings;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to debug screenshot capture issues.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.screenshot.save-history=true",
    "brobot.screenshot.history-path=test-history/",
    "brobot.highlight.enabled=true",
    "brobot.aspects.visual-feedback.enabled=true",
    "brobot.framework.mock=false"
})
public class SimpleScreenshotDebugTest {

    @Autowired
    private BufferedImageUtilities bufferedImageUtilities;

    @Test
    void debugScreenshotCapture() throws Exception {
        System.out.println("\n=== SIMPLE SCREENSHOT DEBUG TEST ===");
        
        // 1. Check environment
        System.out.println("java.awt.headless: " + System.getProperty("java.awt.headless"));
        System.out.println("GraphicsEnvironment.isHeadless: " + GraphicsEnvironment.isHeadless());
        System.out.println("ExecutionEnvironment.hasDisplay: " + ExecutionEnvironment.getInstance().hasDisplay());
        System.out.println("ExecutionEnvironment.canCaptureScreen: " + ExecutionEnvironment.getInstance().canCaptureScreen());
        
        // 2. Check FrameworkSettings
        System.out.println("\n=== FRAMEWORK SETTINGS ===");
        System.out.println("FrameworkSettings.saveHistory: " + FrameworkSettings.saveHistory);
        System.out.println("FrameworkSettings.historyPath: " + FrameworkSettings.historyPath);
        
        // Force enable
        FrameworkSettings.saveHistory = true;
        FrameworkSettings.historyPath = "test-history/";
        
        // Create directory
        Path historyPath = Paths.get("test-history");
        Files.createDirectories(historyPath);
        
        // 3. Test different capture methods
        System.out.println("\n=== TESTING CAPTURE METHODS ===");
        
        // Method 1: Direct Robot
        System.out.println("\nMethod 1: Direct Java Robot");
        try {
            Robot robot = new Robot();
            BufferedImage capture1 = robot.createScreenCapture(new Rectangle(0, 0, 200, 200));
            File file1 = new File(historyPath.toFile(), "method1-robot.png");
            ImageIO.write(capture1, "png", file1);
            analyzeImage(file1, capture1);
        } catch (Exception e) {
            System.err.println("Robot capture failed: " + e.getMessage());
        }
        
        // Method 2: BufferedImageUtilities
        System.out.println("\nMethod 2: BufferedImageUtilities");
        try {
            io.github.jspinak.brobot.model.region.Region region = 
                new io.github.jspinak.brobot.model.region.Region(0, 0, 200, 200);
            BufferedImage capture2 = bufferedImageUtilities.captureRegion(region);
            if (capture2 != null) {
                File file2 = new File(historyPath.toFile(), "method2-utils.png");
                ImageIO.write(capture2, "png", file2);
                analyzeImage(file2, capture2);
            } else {
                System.err.println("BufferedImageUtilities returned null!");
            }
        } catch (Exception e) {
            System.err.println("BufferedImageUtilities capture failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Method 3: Check display configuration
        System.out.println("\n=== DISPLAY CONFIGURATION ===");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        System.out.println("Number of screens: " + devices.length);
        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice device = devices[i];
            System.out.println("Screen " + i + ": " + device.getIDstring());
            System.out.println("  Type: " + device.getType());
            System.out.println("  Available: " + device.isDisplayChangeSupported());
        }
    }
    
    private void analyzeImage(File file, BufferedImage img) {
        System.out.println("File: " + file.getName());
        System.out.println("  Size: " + file.length() + " bytes");
        System.out.println("  Dimensions: " + img.getWidth() + "x" + img.getHeight());
        
        // Check for black pixels
        int blackCount = 0;
        int totalSamples = 100;
        for (int i = 0; i < totalSamples; i++) {
            int x = (int)(Math.random() * img.getWidth());
            int y = (int)(Math.random() * img.getHeight());
            if (img.getRGB(x, y) == 0xFF000000) {
                blackCount++;
            }
        }
        
        double blackPercentage = (blackCount * 100.0) / totalSamples;
        System.out.println("  Black pixels: " + blackPercentage + "%");
        
        if (blackPercentage > 90) {
            System.out.println("  ⚠️ WARNING: Image appears to be BLACK!");
            
            // Check specific pixels
            System.out.println("  Sample pixels:");
            for (int i = 0; i < 5; i++) {
                int x = i * img.getWidth() / 5;
                int y = i * img.getHeight() / 5;
                System.out.printf("    (%d,%d): 0x%08X%n", x, y, img.getRGB(x, y));
            }
        } else {
            System.out.println("  ✓ Image has content");
        }
    }
}