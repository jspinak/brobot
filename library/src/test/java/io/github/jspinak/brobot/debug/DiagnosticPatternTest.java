package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Diagnostic test to understand why SikuliX IDE finds patterns at 0.99 
 * but our code doesn't find them at all.
 */
@DisabledInCI
public class DiagnosticPatternTest extends DebugTestBase {
    
    @Test
    public void diagnosePatternMatching() throws Exception {
        System.out.println("=== DIAGNOSTIC PATTERN MATCHING TEST ===\n");
        
        // Pattern file
        String patternPath = "images/prompt/claude-prompt-1.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.out.println("Pattern file not found: " + patternPath);
            return;
        }
        
        System.out.println("Pattern file: " + patternFile.getAbsolutePath());
        System.out.println("Pattern exists: " + patternFile.exists());
        System.out.println("Pattern size: " + patternFile.length() + " bytes\n");
        
        // Load pattern image to check its properties
        BufferedImage patternImage = ImageIO.read(patternFile);
        System.out.println("Pattern dimensions: " + patternImage.getWidth() + "x" + patternImage.getHeight());
        System.out.println("Pattern type: " + getImageType(patternImage.getType()) + "\n");
        
        // Check current settings
        System.out.println("CURRENT SETTINGS:");
        System.out.println("  Settings.MinSimilarity: " + Settings.MinSimilarity);
        System.out.println("  Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("  Settings.CheckLastSeen: " + Settings.CheckLastSeen);
        System.out.println("  Settings.AutoWaitTimeout: " + Settings.AutoWaitTimeout);
        System.out.println("  Settings.WaitScanRate: " + Settings.WaitScanRate + "\n");
        
        System.out.println("Position VS Code window and press Enter...");
        System.in.read();
        
        // Capture screen
        System.out.println("\n1. CAPTURING SCREEN");
        Screen screen = new Screen();
        ScreenImage screenCapture = screen.capture();
        BufferedImage screenImage = screenCapture.getImage();
        System.out.println("  Screen captured: " + screenImage.getWidth() + "x" + screenImage.getHeight());
        System.out.println("  Screen type: " + getImageType(screenImage.getType()));
        
        // Save screenshot for inspection
        File screenshotFile = new File("debug-screenshot.png");
        ImageIO.write(screenImage, "png", screenshotFile);
        System.out.println("  Screenshot saved to: " + screenshotFile.getAbsolutePath() + "\n");
        
        // Test 1: Direct Pattern with file path
        System.out.println("2. TEST WITH FILE PATH PATTERN");
        try {
            Pattern p1 = new Pattern(patternFile.getAbsolutePath());
            testWithPattern(p1, screenImage, "File path pattern");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        // Test 2: Pattern with BufferedImage
        System.out.println("\n3. TEST WITH BUFFEREDIMAGE PATTERN");
        Pattern p2 = new Pattern(patternImage);
        testWithPattern(p2, screenImage, "BufferedImage pattern");
        
        // Test 3: Pattern with Image.create()
        System.out.println("\n4. TEST WITH IMAGE.CREATE() PATTERN");
        try {
            Image img = Image.create(patternFile.getAbsolutePath());
            if (img != null && img.isValid()) {
                Pattern p3 = new Pattern(img);
                testWithPattern(p3, screenImage, "Image.create() pattern");
            } else {
                System.out.println("  Image.create() returned invalid image");
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        // Test 4: Test with Settings.AlwaysResize = 0.8
        System.out.println("\n5. TEST WITH ALWAYSRESIZE = 0.8");
        Settings.AlwaysResize = 0.8f;
        Pattern p4 = new Pattern(patternImage);
        testWithPattern(p4, screenImage, "Pattern with resize 0.8");
        Settings.AlwaysResize = 1.0f;
        
        // Test 5: Screen.exists() instead of Finder
        System.out.println("\n6. TEST WITH SCREEN.EXISTS()");
        Pattern p5 = new Pattern(patternImage).similar(0.70);
        Match screenMatch = screen.exists(p5, 0);
        if (screenMatch != null) {
            System.out.println("  ✓ Found with Screen.exists()! Score: " + screenMatch.getScore());
        } else {
            System.out.println("  ✗ Not found with Screen.exists()");
        }
        
        // Test 6: Check if pattern is actually visible
        System.out.println("\n7. CHECKING PATTERN VISIBILITY");
        System.out.println("  Is VS Code window active and visible?");
        System.out.println("  Is the Claude prompt visible on screen?");
        System.out.println("  Pattern we're looking for:");
        System.out.println("    - Size: " + patternImage.getWidth() + "x" + patternImage.getHeight());
        System.out.println("    - Should contain text 'Write a prompt for Claude' or similar");
        
        // Save pattern for comparison
        File patternCopy = new File("debug-pattern.png");
        ImageIO.write(patternImage, "png", patternCopy);
        System.out.println("\n  Pattern saved to: " + patternCopy.getAbsolutePath());
        System.out.println("  Compare debug-screenshot.png with debug-pattern.png");
        System.out.println("  Check if the pattern is actually visible in the screenshot");
        
        System.out.println("\n=== END DIAGNOSTIC TEST ===");
    }
    
    private void testWithPattern(Pattern pattern, BufferedImage screenImage, String description) {
        System.out.println("  Testing: " + description);
        
        // Test at different similarity levels
        double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60, 0.50};
        
        for (double threshold : thresholds) {
            pattern = pattern.similar(threshold);
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("    ✓ Found at %.2f threshold with score %.3f at (%d, %d)%n", 
                                threshold, match.getScore(), match.x, match.y);
                finder.destroy();
                return;
            }
            finder.destroy();
        }
        
        System.out.println("    ✗ Not found at any threshold");
    }
    
    private String getImageType(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
            default: return "Type " + type;
        }
    }
}