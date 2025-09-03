package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.config.dpi.DPIAutoDetector;
import io.github.jspinak.brobot.config.dpi.DPIConfiguration;
import io.github.jspinak.brobot.model.element.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Tests DPI scaling detection and automatic compensation.
 */
public class DPIScalingTest extends BrobotTestBase {
    
    @Test
    public void testDPIScalingDetection() {
        System.out.println("=== DPI SCALING DETECTION TEST ===\n");
        
        // Detect system scaling
        DPIAutoDetector detector = new DPIAutoDetector();
        float scaleFactor = detector.detectScalingFactor();
        System.out.println("\nDetected scaling factor: " + scaleFactor);
        System.out.println("Description: " + detector.getScalingDescription());
        System.out.println("Has scaling: " + (Math.abs(scaleFactor - 1.0f) > 0.01f));
        
        // Test with and without scaling compensation
        testPatternMatching();
    }
    
    @Test
    public void testDPIConfiguration() {
        System.out.println("\n=== BROBOT DPI CONFIG TEST ===\n");
        
        // Create and initialize the config with detector
        DPIAutoDetector detector = new DPIAutoDetector();
        DPIConfiguration dpiConfig = new DPIConfiguration(detector);
        
        // Test current configuration
        System.out.println("1. Current configuration:");
        System.out.println("   Current resize factor: " + dpiConfig.getCurrentResizeFactor());
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        // Test manual override
        System.out.println("\n2. Testing manual override:");
        dpiConfig.setResizeFactor(0.9f);
        System.out.println("   Settings.AlwaysResize after manual set: " + Settings.AlwaysResize);
        
        // Redetect and apply
        System.out.println("\n3. Testing redetection:");
        dpiConfig.redetectAndApply();
        System.out.println("   Settings.AlwaysResize after redetection: " + Settings.AlwaysResize);
    }
    
    private void testPatternMatching() {
        try {
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                System.out.println("Pattern file not found: " + patternPath);
                return;
            }
            
            System.out.println("\n=== PATTERN MATCHING WITH DPI COMPENSATION ===");
            System.out.println("Position VS Code window and wait 3 seconds...");
            Thread.sleep(3000);
            
            // Capture screen
            Screen screen = new Screen();
            BufferedImage screenshot = screen.capture().getImage();
            
            // Load pattern
            BufferedImage patternImage = ImageIO.read(patternFile);
            
            // Test WITHOUT DPI compensation
            System.out.println("\n1. WITHOUT DPI Compensation:");
            Settings.AlwaysResize = 1.0f;
            testWithSettings(patternImage, screenshot);
            
            // Test WITH auto-detected DPI compensation
            System.out.println("\n2. WITH Auto-Detected DPI Compensation:");
            float detectedScale = new DPIAutoDetector().detectScalingFactor();
            Settings.AlwaysResize = detectedScale;
            System.out.println("   Applied scaling: " + detectedScale);
            testWithSettings(patternImage, screenshot);
            
            // Test with Brobot Pattern (should use BufferedImage)
            System.out.println("\n3. WITH Brobot Pattern:");
            Pattern brobotPattern = new Pattern(patternPath);
            org.sikuli.script.Pattern sikuliPattern = brobotPattern.sikuli();
            testSikuliPattern(sikuliPattern, screenshot);
            
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testWithSettings(BufferedImage pattern, BufferedImage screenshot) {
        org.sikuli.script.Pattern p = new org.sikuli.script.Pattern(pattern);
        
        double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70};
        for (double threshold : thresholds) {
            p = p.similar(threshold);
            Finder finder = new Finder(screenshot);
            finder.find(p);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("   ✓ Found at %.2f threshold with score %.3f%n", 
                                threshold, match.getScore());
                finder.destroy();
                return;
            }
            finder.destroy();
        }
        System.out.println("   ✗ Not found at any threshold");
    }
    
    private void testSikuliPattern(org.sikuli.script.Pattern pattern, BufferedImage screenshot) {
        double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70};
        for (double threshold : thresholds) {
            pattern = pattern.similar(threshold);
            Finder finder = new Finder(screenshot);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("   ✓ Found at %.2f threshold with score %.3f%n", 
                                threshold, match.getScore());
                finder.destroy();
                return;
            }
            finder.destroy();
        }
        System.out.println("   ✗ Not found at any threshold");
    }
}