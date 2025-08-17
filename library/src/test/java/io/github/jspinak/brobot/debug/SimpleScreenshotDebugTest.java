package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test to verify screenshot handling and analysis.
 */
public class SimpleScreenshotDebugTest {

    // @Autowired
    private BufferedImageUtilities bufferedImageUtilities;

    @Test
    void debugScreenshotAnalysis() throws Exception {
        
        System.out.println("\n=== SCREENSHOT ANALYSIS TEST ===");
        
        // 1. Check environment
        System.out.println("java.awt.headless: " + System.getProperty("java.awt.headless"));
        System.out.println("GraphicsEnvironment.isHeadless: " + GraphicsEnvironment.isHeadless());
        
        // 2. Load and analyze screenshots from library folder
        System.out.println("\n=== ANALYZING SCREENSHOTS ===");
        
        String screenshotDir = "screenshots/";
        String imageDir = "images/";
        
        // Create test output directory
        Path historyPath = Paths.get("test-history");
        Files.createDirectories(historyPath);
        
        // Method 1: Analyze existing screenshots
        System.out.println("\nMethod 1: Screenshot Analysis");
        Path screenshotPath = Paths.get(screenshotDir, "floranext0.png");
        File screenshotFile = screenshotPath.toFile();
        
        if (screenshotFile.exists()) {
            BufferedImage screenshot = ImageIO.read(screenshotFile);
            File outputFile = new File(historyPath.toFile(), "screenshot-analysis.png");
            ImageIO.write(screenshot, "png", outputFile);
            analyzeImage(outputFile, screenshot);
        } else {
            System.err.println("Screenshot not found: " + screenshotFile.getAbsolutePath());
        }
        
        // Method 2: Test pattern matching
        System.out.println("\nMethod 2: Pattern Matching Test");
        Path imagePath = Paths.get(imageDir, "topLeft.png");
        File imageFile = imagePath.toFile();
        
        if (screenshotFile.exists() && imageFile.exists()) {
            BufferedImage screenshot = ImageIO.read(screenshotFile);
            BufferedImage pattern = ImageIO.read(imageFile);
            
            System.out.println("Screenshot: " + screenshot.getWidth() + "x" + screenshot.getHeight());
            System.out.println("Pattern: " + pattern.getWidth() + "x" + pattern.getHeight());
            
            // Test if pattern could be found in screenshot
            io.github.jspinak.brobot.model.element.Region searchRegion = 
                new io.github.jspinak.brobot.model.element.Region(0, 0, 
                    screenshot.getWidth(), screenshot.getHeight());
            
            System.out.println("Search region defined: " + searchRegion);
            analyzeImage(imageFile, pattern);
        } else {
            System.err.println("Pattern or screenshot not found");
        }
        
        // Method 3: Test multiple screenshots
        System.out.println("\n=== MULTIPLE SCREENSHOT TEST ===");
        String[] screenshots = {"floranext1.png", "floranext2.png", "floranext3.png"};
        
        for (String screenshotName : screenshots) {
            Path path = Paths.get("screenshots/", screenshotName);
            File file = path.toFile();
            
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                System.out.println("\n" + screenshotName + ": " + img.getWidth() + "x" + img.getHeight());
                
                // Sample analysis
                int sampleSize = Math.min(100, img.getWidth() * img.getHeight() / 100);
                analyzeImageSamples(img, sampleSize);
            }
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
        
        // Analyze color distribution
        System.out.println("  Sample pixels:");
        for (int i = 0; i < Math.min(5, totalSamples); i++) {
            int x = (int)(Math.random() * img.getWidth());
            int y = (int)(Math.random() * img.getHeight());
            int rgb = img.getRGB(x, y);
            System.out.printf("    (%d,%d): 0x%08X%n", x, y, rgb);
        }
        
        if (blackPercentage > 90) {
            System.out.println("  ⚠️ Image is mostly black");
        } else if (blackPercentage < 10) {
            System.out.println("  ✓ Image has rich content");
        } else {
            System.out.println("  ✓ Image has mixed content");
        }
    }
    
    private void analyzeImageSamples(BufferedImage img, int sampleSize) {
        int blackCount = 0;
        int whiteCount = 0;
        
        for (int i = 0; i < sampleSize; i++) {
            int x = (int)(Math.random() * img.getWidth());
            int y = (int)(Math.random() * img.getHeight());
            int rgb = img.getRGB(x, y);
            
            if (rgb == 0xFF000000) blackCount++;
            if (rgb == 0xFFFFFFFF) whiteCount++;
        }
        
        System.out.println("  Samples: " + sampleSize);
        System.out.println("  Black: " + (blackCount * 100.0 / sampleSize) + "%");
        System.out.println("  White: " + (whiteCount * 100.0 / sampleSize) + "%");
        System.out.println("  Other: " + ((sampleSize - blackCount - whiteCount) * 100.0 / sampleSize) + "%");
    }
}