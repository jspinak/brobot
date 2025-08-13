package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.config.WindowsAutoScaleConfig;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.ScreenImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Test to verify pattern matching works correctly on Windows with DPI scaling.
 * Run this on Windows to verify the fix works.
 */
public class WindowsDPITest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("===============================================");
        System.out.println("  Windows DPI Pattern Matching Test");
        System.out.println("===============================================\n");
        
        // Initialize Windows scaling configuration
        WindowsAutoScaleConfig config = new WindowsAutoScaleConfig();
        config.configureWindowsScaling();
        
        System.out.println("Current Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println();
        
        // Run the critical test
        boolean success = testPatternMatching();
        
        if (success) {
            System.out.println("\n===============================================");
            System.out.println("  ✓ SUCCESS: Pattern matching works correctly!");
            System.out.println("  Match dimensions equal search image dimensions");
            System.out.println("===============================================");
        } else {
            System.out.println("\n===============================================");
            System.out.println("  ✗ FAILURE: Pattern matching has issues");
            System.out.println("  Match dimensions DO NOT equal search image");
            System.out.println("===============================================");
            System.exit(1);
        }
    }
    
    private static boolean testPatternMatching() throws IOException {
        System.out.println("Testing Pattern Matching");
        System.out.println("------------------------");
        
        // Create test directory
        File testDir = new File("target/windows-dpi-test");
        testDir.mkdirs();
        
        // Test multiple sizes to be thorough
        int[][] testSizes = {
            {86, 59},   // The exact size from the user's report
            {100, 50},
            {75, 75},
            {120, 80}
        };
        
        boolean allPassed = true;
        
        for (int[] size : testSizes) {
            int patternWidth = size[0];
            int patternHeight = size[1];
            
            System.out.print("\nTesting " + patternWidth + "x" + patternHeight + " pattern... ");
            
            // Create pattern image
            BufferedImage patternImg = createTestPattern(patternWidth, patternHeight);
            File patternFile = new File(testDir, "pattern-" + patternWidth + "x" + patternHeight + ".png");
            ImageIO.write(patternImg, "PNG", patternFile);
            
            // Create screen image containing the pattern
            BufferedImage screenImg = createScreenWithPattern(
                500, 400, patternWidth, patternHeight, 100, 100);
            
            // Create ScreenImage
            ScreenImage screenImage = new ScreenImage(
                new Rectangle(0, 0, 500, 400), screenImg);
            
            // Create pattern and find it
            Pattern pattern = new Pattern(patternFile.getAbsolutePath());
            pattern.similar(0.90); // Lower threshold for test patterns
            
            // Find the pattern
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (!finder.hasNext()) {
                System.out.println("✗ Pattern not found!");
                allPassed = false;
                continue;
            }
            
            Match match = finder.next();
            Rectangle matchRect = match.getRect();
            
            System.out.println("\n  Found at: (" + matchRect.x + ", " + matchRect.y + ")");
            System.out.println("  Match size: " + matchRect.width + "x" + matchRect.height);
            System.out.println("  Expected: " + patternWidth + "x" + patternHeight);
            
            // CRITICAL CHECK: Match dimensions must equal pattern dimensions
            if (matchRect.width == patternWidth && matchRect.height == patternHeight) {
                System.out.println("  ✓ PASS: Dimensions match!");
            } else {
                System.out.println("  ✗ FAIL: Dimension mismatch!");
                System.out.println("    Match is " + 
                    ((float)matchRect.width/patternWidth) + "x smaller in width");
                System.out.println("    Match is " + 
                    ((float)matchRect.height/patternHeight) + "x smaller in height");
                allPassed = false;
            }
            
            // Also check position (should be at 100, 100)
            if (matchRect.x == 100 && matchRect.y == 100) {
                System.out.println("  ✓ Position correct");
            } else {
                System.out.println("  ✗ Position incorrect (expected 100,100)");
                // Position error is less critical than dimension error
            }
        }
        
        return allPassed;
    }
    
    private static BufferedImage createTestPattern(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        // Create a distinctive pattern
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.BLACK);
        g.fillRect(5, 5, width-10, height-10);
        
        g.setColor(Color.WHITE);
        g.drawString("TEST", width/2 - 15, height/2);
        
        g.dispose();
        return img;
    }
    
    private static BufferedImage createScreenWithPattern(
            int screenW, int screenH, 
            int patternW, int patternH,
            int patternX, int patternY) {
        
        BufferedImage img = new BufferedImage(screenW, screenH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        // Gray background
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, screenW, screenH);
        
        // Place the pattern
        BufferedImage pattern = createTestPattern(patternW, patternH);
        g.drawImage(pattern, patternX, patternY, null);
        
        g.dispose();
        return img;
    }
}