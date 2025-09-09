package io.github.jspinak.brobot.dpi;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.diagnostics.DPIScalingDiagnostic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import io.github.jspinak.brobot.test.DisabledInCI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify DPI scaling theory without Spring dependencies.
 * Tests that screen capture and pattern matching use compatible pixel dimensions.
 */
@DisabledInCI
public class SimpleDPIVerificationTest extends BrobotTestBase {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private Screen screen;
    private double displayScale;
    
    @BeforeEach
    @Override  
    public void setupTest() {
        super.setupTest();
        screen = new Screen();
        displayScale = DPIScalingStrategy.detectDisplayScaling();
        
        System.out.println("\n=== SIMPLE DPI VERIFICATION TEST ===");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("================================\n");
    }
    
    @Test
    public void verifyCriticalAssumption_ScreenCapturePixelDimensions() {
        System.out.println("CRITICAL TEST: Verifying Screen Capture Pixel Dimensions");
        System.out.println("=" + "=".repeat(70));
        
        // Get display information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        
        System.out.println("\n1. DISPLAY CONFIGURATION:");
        System.out.println("   Display Mode (logical): " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("   Display Scaling: " + (int)(displayScale * 100) + "%");
        
        // Calculate expected physical dimensions
        int expectedPhysical_width = (int)(dm.getWidth() * displayScale);
        int expectedPhysical_height = (int)(dm.getHeight() * displayScale);
        System.out.println("   Expected Physical: " + expectedPhysical_width + "x" + expectedPhysical_height);
        
        // Capture screen using SikuliX
        System.out.println("\n2. SIKULIX SCREEN CAPTURE:");
        ScreenImage screenImage = screen.capture();
        BufferedImage capture = screenImage.getImage();
        
        System.out.println("   Actual Capture: " + capture.getWidth() + "x" + capture.getHeight());
        
        // Determine what pixels SikuliX is using
        boolean isLogical = Math.abs(capture.getWidth() - dm.getWidth()) < 10;
        boolean isPhysical = Math.abs(capture.getWidth() - expectedPhysical_width) < 10;
        
        System.out.println("\n3. CRITICAL FINDING:");
        if (isLogical) {
            System.out.println("   ⚠️ CRITICAL: SikuliX captures in LOGICAL pixels!");
            System.out.println("   This means patterns and captures use THE SAME coordinate system.");
            System.out.println("   DPI scaling may NOT be needed for matching!");
        } else if (isPhysical) {
            System.out.println("   ✓ SikuliX captures in PHYSICAL pixels");
            System.out.println("   Patterns in physical pixels should match directly.");
            System.out.println("   DPI scaling IS needed for logical pixel patterns.");
        } else {
            System.out.println("   ❓ UNEXPECTED: Capture dimensions don't match logical OR physical!");
            System.out.println("   There may be additional scaling factors at play.");
        }
        
        // Save capture for analysis
        saveCapture(capture, "screen_capture_analysis");
        
        System.out.println("\n4. IMPLICATIONS FOR PATTERN MATCHING:");
        if (isLogical) {
            System.out.println("   - Patterns captured with SikuliX IDE should match WITHOUT scaling");
            System.out.println("   - Settings.AlwaysResize should be 1.0");
            System.out.println("   - The 30% similarity loss is from OTHER factors, not DPI");
        } else {
            System.out.println("   - Pattern scaling depends on capture method");
            System.out.println("   - Settings.AlwaysResize = " + (1.0 / displayScale) + " for physical patterns");
        }
        
        System.out.println("\n" + "=".repeat(70));
    }
    
    @Test
    public void testClaudeAutomatorPatternsWithActualDimensions() {
        System.out.println("\nTEST: Claude Automator Patterns Analysis");
        System.out.println("=" + "=".repeat(70));
        
        String claudePath = "/home/jspinak/brobot_parent/claude-automator/images/prompt/";
        
        // Test each pattern
        testPatternDimensions(claudePath + "claude-prompt-3.png", "SikuliX Original");
        testPatternDimensions(claudePath + "claude-prompt-3-80.png", "80% Scaled");  
        testPatternDimensions(claudePath + "claude-prompt-win.png", "Windows Capture");
    }
    
    private void testPatternDimensions(String path, String description) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("\n" + description + ": File not found");
            return;
        }
        
        try {
            BufferedImage img = ImageIO.read(file);
            System.out.println("\n" + description + ":");
            System.out.println("  File: " + file.getName());
            System.out.println("  Dimensions: " + img.getWidth() + "x" + img.getHeight());
            
            // Capture a screen region of same size to compare
            System.out.println("  Testing pattern matching...");
            
            // Test with different DPI settings
            testWithDPI(path, 1.0f, "No scaling");
            testWithDPI(path, 0.8f, "DPI 0.8");
            testWithDPI(path, 0.67f, "DPI 0.67");
            
        } catch (IOException e) {
            System.err.println("  Error: " + e.getMessage());
        }
    }
    
    private void testWithDPI(String patternPath, float dpi, String description) {
        Settings.AlwaysResize = dpi;
        try {
            Pattern pattern = new Pattern(patternPath).similar(0.3);
            Match match = screen.exists(pattern, 0.1);
            
            if (match != null) {
                System.out.printf("    %s: %.1f%%\n", description, match.getScore() * 100);
            } else {
                System.out.printf("    %s: No match\n", description);
            }
        } catch (Exception e) {
            System.out.printf("    %s: Error\n", description);
        }
    }
    
    @Test
    public void verifyAutoDetectionLogic() {
        System.out.println("\nTEST: Auto-Detection Logic Verification");
        System.out.println("=" + "=".repeat(70));
        
        // Test DPIScalingStrategy
        double detected = DPIScalingStrategy.detectDisplayScaling();
        float calculated = DPIScalingStrategy.calculatePatternScaleFactor(detected);
        
        System.out.println("\nDPIScalingStrategy Results:");
        System.out.println("  Detected scaling: " + (int)(detected * 100) + "%");
        System.out.println("  Calculated pattern factor: " + calculated);
        System.out.println("  Formula: 1.0 / " + detected + " = " + calculated);
        
        // Test with different pattern sources
        System.out.println("\nPattern Source Analysis:");
        for (DPIScalingStrategy.PatternSource source : DPIScalingStrategy.PatternSource.values()) {
            float factor = DPIScalingStrategy.getOptimalResizeFactor(source);
            System.out.println("  " + source + ": " + factor);
        }
        
        // Print full diagnostics
        System.out.println("\nFull Diagnostics:");
        DPIScalingStrategy.printDiagnostics();
    }
    
    @Test
    public void captureMatchedRegionAndCompare() {
        System.out.println("\nTEST: Capture Matched Region and Compare");
        System.out.println("=" + "=".repeat(70));
        
        String testPattern = "/home/jspinak/brobot_parent/claude-automator/images/prompt/claude-prompt-3.png";
        File patternFile = new File(testPattern);
        
        if (!patternFile.exists()) {
            System.out.println("Pattern not found, skipping test");
            return;
        }
        
        try {
            // Try to find pattern with different DPI settings
            Match bestMatch = null;
            float bestDPI = 1.0f;
            double bestScore = 0;
            
            float[] testDPIs = {1.0f, 0.8f, 0.67f};
            
            for (float dpi : testDPIs) {
                Settings.AlwaysResize = dpi;
                Pattern pattern = new Pattern(testPattern).similar(0.3);
                Match match = screen.exists(pattern, 0.5);
                
                if (match != null && match.getScore() > bestScore) {
                    bestMatch = match;
                    bestDPI = dpi;
                    bestScore = match.getScore();
                }
            }
            
            if (bestMatch == null) {
                System.out.println("Pattern not found on screen");
                return;
            }
            
            System.out.println("Found pattern with DPI " + bestDPI + ", score: " + 
                             String.format("%.1f%%", bestScore * 100));
            
            // Capture the matched region
            Region matchRegion = new Region(bestMatch);
            BufferedImage captured = screen.capture(matchRegion).getImage();
            
            // Load original pattern
            BufferedImage original = ImageIO.read(patternFile);
            
            System.out.println("\nDimension Comparison:");
            System.out.println("  Original pattern: " + original.getWidth() + "x" + original.getHeight());
            System.out.println("  Captured region: " + captured.getWidth() + "x" + captured.getHeight());
            
            double widthRatio = (double)captured.getWidth() / original.getWidth();
            double heightRatio = (double)captured.getHeight() / original.getHeight();
            
            System.out.println("  Width ratio: " + String.format("%.3f", widthRatio));
            System.out.println("  Height ratio: " + String.format("%.3f", heightRatio));
            
            if (Math.abs(widthRatio - 0.8) < 0.05) {
                System.out.println("  ✓ Captured is 80% of original (matches DPI scaling)");
            } else if (Math.abs(widthRatio - 1.0) < 0.05) {
                System.out.println("  ✓ Captured matches original size");
            } else {
                System.out.println("  ⚠ Unexpected size ratio");
            }
            
            // Save captured region
            File captureFile = saveCapture(captured, "matched_region");
            
            // Try reverse matching
            System.out.println("\nReverse Matching Test:");
            Settings.AlwaysResize = 1.0f; // Reset to no scaling
            Pattern capturedPattern = new Pattern(captureFile.getAbsolutePath()).similar(0.3);
            Match reverseMatch = screen.exists(capturedPattern, 0.5);
            
            if (reverseMatch != null) {
                System.out.println("  Captured region found with similarity: " + 
                                 String.format("%.1f%%", reverseMatch.getScore() * 100));
                
                if (reverseMatch.getScore() > 0.95) {
                    System.out.println("  ✅ PERFECT: Capture and search are fully compatible");
                } else if (reverseMatch.getScore() > 0.80) {
                    System.out.println("  ✓ GOOD: Capture and search are mostly compatible");
                } else {
                    System.out.println("  ⚠ WARNING: Potential compatibility issues");
                }
            } else {
                System.out.println("  ❌ Captured region NOT found - incompatible methods!");
            }
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private File saveCapture(BufferedImage image, String prefix) {
        try {
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(debugDir, prefix + "_" + timestamp + ".png");
            ImageIO.write(image, "png", file);
            System.out.println("  Saved to: " + file.getName());
            return file;
        } catch (IOException e) {
            System.err.println("  Failed to save: " + e.getMessage());
            return null;
        }
    }
}