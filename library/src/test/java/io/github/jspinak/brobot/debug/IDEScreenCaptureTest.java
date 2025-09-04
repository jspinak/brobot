package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Test that mimics exactly how SikuliX IDE captures screenshots.
 * This will help us understand why IDE patterns are sharp.
 */
public class IDEScreenCaptureTest extends DebugTestBase {
    
    @Test
    public void testIDEStyleCapture() throws Exception {
        System.out.println("=== SIKULIX IDE STYLE CAPTURE TEST ===\n");
        
        // 1. Note: ScreenUnion is not available in the API, only in IDE
        System.out.println("1. SCREEN UNION NOTE:");
        System.out.println("   ScreenUnion is IDE-internal and not available in API");
        System.out.println("   The IDE uses: (new ScreenUnion()).getScreen().capture()");
        System.out.println("   We'll test with Screen.getPrimaryScreen() instead");
        
        // 2. Test regular Screen capture (for comparison)
        System.out.println("\n2. REGULAR SCREEN CAPTURE:");
        Screen screen = new Screen();
        ScreenImage screenImg = screen.capture();
        BufferedImage regularCapture = screenImg.getImage();
        
        System.out.println("   Screen captured: " + regularCapture.getWidth() + "x" + regularCapture.getHeight());
        System.out.println("   Image type: " + getImageType(regularCapture.getType()));
        saveImage(regularCapture, "test-regular-capture.png");
        
        // 3. Test Screen.all() capture
        System.out.println("\n3. SCREEN.ALL() CAPTURE:");
        Region all = Screen.all();
        System.out.println("   Screen.all bounds: " + all.getRect());
        ScreenImage allImg = all.getScreen().capture();
        BufferedImage allCapture = allImg.getImage();
        
        System.out.println("   All captured: " + allCapture.getWidth() + "x" + allCapture.getHeight());
        System.out.println("   Image type: " + getImageType(allCapture.getType()));
        saveImage(allCapture, "test-all-capture.png");
        
        // 4. Test Primary Screen capture
        System.out.println("\n4. PRIMARY SCREEN CAPTURE:");
        Screen primary = Screen.getPrimaryScreen();
        System.out.println("   Primary screen ID: " + primary.getID());
        System.out.println("   Primary bounds: " + primary.getBounds());
        ScreenImage primaryImg = primary.capture();
        BufferedImage primaryCapture = primaryImg.getImage();
        
        System.out.println("   Primary captured: " + primaryCapture.getWidth() + "x" + primaryCapture.getHeight());
        System.out.println("   Image type: " + getImageType(primaryCapture.getType()));
        saveImage(primaryCapture, "test-primary-capture.png");
        
        // 5. Test with Finder (how IDE does pattern matching)
        System.out.println("\n5. PATTERN MATCHING TEST (IDE style):");
        testPatternMatching(screenImg);
        
        System.out.println("\n=== COMPARISON ===");
        System.out.println("Check these files for sharpness:");
        System.out.println("  - test-regular-capture.png");
        System.out.println("  - test-all-capture.png");
        System.out.println("  - test-primary-capture.png");
        System.out.println("\nNote: ScreenUnion is IDE-internal, not available in API");
        
        System.out.println("\n=== TEST COMPLETE ===");
    }
    
    private void testPatternMatching(ScreenImage searchImage) {
        try {
            // Load a pattern (use your claude-prompt pattern)
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                System.out.println("   Pattern not found: " + patternPath);
                return;
            }
            
            // This mimics what IDE does in PatternPaneTargetOffset.findTarget()
            System.out.println("   Using pattern: " + patternPath);
            
            // Create a Finder with the captured screen
            Region screenUnion = Region.create(0, 0, 1, 1);
            Finder finder = new Finder(searchImage, screenUnion);
            
            // Find the pattern (IDE uses file path directly!)
            finder.find(patternPath);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("   ✓ Pattern found!");
                System.out.println("     Score: " + match.getScore());
                System.out.println("     Location: " + match.getTarget());
            } else {
                System.out.println("   ✗ Pattern not found");
                
                // Try with different similarity
                finder = new Finder(searchImage, screenUnion);
                Pattern pattern = new Pattern(patternPath).similar(0.7);
                finder.find(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("   ✓ Found at 0.7 similarity");
                    System.out.println("     Score: " + match.getScore());
                }
            }
            
            finder.destroy();
            
        } catch (Exception e) {
            System.out.println("   Error in pattern matching: " + e.getMessage());
        }
    }
    
    private void saveImage(BufferedImage image, String filename) {
        try {
            File file = new File(filename);
            ImageIO.write(image, "png", file);
            System.out.println("   Saved: " + file.getName() + " (" + image.getWidth() + "x" + image.getHeight() + ")");
            
            // Check file size for quality indication
            long fileSize = file.length();
            System.out.println("   File size: " + (fileSize / 1024) + " KB");
            
        } catch (IOException e) {
            System.out.println("   Failed to save: " + e.getMessage());
        }
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
    
    @Test
    public void testMultiScreenCapture() {
        System.out.println("=== MULTI-SCREEN CAPTURE TEST ===\n");
        
        try {
            // Test if there are multiple screens
            int numScreens = Screen.getNumberScreens();
            System.out.println("Number of screens: " + numScreens);
            
            for (int i = 0; i < numScreens; i++) {
                Screen s = Screen.getScreen(i);
                System.out.println("\nScreen " + i + ":");
                System.out.println("  ID: " + s.getID());
                System.out.println("  Bounds: " + s.getBounds());
                
                // Capture each screen
                ScreenImage img = s.capture();
                BufferedImage capture = img.getImage();
                System.out.println("  Captured: " + capture.getWidth() + "x" + capture.getHeight());
                saveImage(capture, "test-screen-" + i + "-capture.png");
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}