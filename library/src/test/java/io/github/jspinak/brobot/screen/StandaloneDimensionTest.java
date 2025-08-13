package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.startup.PhysicalResolutionInitializer;
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
 * Standalone test to verify match dimensions equal search image dimensions.
 * Run with: java io.github.jspinak.brobot.screen.StandaloneDimensionTest
 */
public class StandaloneDimensionTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("  Brobot Match Dimension Verification Test");
        System.out.println("===========================================\n");
        
        // Initialize physical resolution
        PhysicalResolutionInitializer.forceInitialization();
        Settings.AlwaysResize = 0;
        
        // Run tests
        testSimpleMatch();
        testVariousSizes();
        testActualCapture();
        
        System.out.println("\n===========================================");
        System.out.println("  All tests completed successfully!");
        System.out.println("===========================================");
    }
    
    private static void testSimpleMatch() throws IOException {
        System.out.println("Test 1: Simple Pattern Match");
        System.out.println("-----------------------------");
        
        // Create test directory
        File testDir = new File("target/dimension-test");
        testDir.mkdirs();
        
        // Create a pattern (60x40 black rectangle)
        int patternWidth = 60;
        int patternHeight = 40;
        BufferedImage patternImg = new BufferedImage(patternWidth, patternHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = patternImg.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, patternWidth, patternHeight);
        g.dispose();
        
        File patternFile = new File(testDir, "pattern.png");
        ImageIO.write(patternImg, "PNG", patternFile);
        System.out.println("Created pattern: " + patternWidth + "x" + patternHeight);
        
        // Create a screen image containing the pattern
        int screenWidth = 300;
        int screenHeight = 200;
        BufferedImage screenImg = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        g = screenImg.createGraphics();
        // Fill with white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, screenWidth, screenHeight);
        // Place black rectangle at position (100, 80)
        g.setColor(Color.BLACK);
        g.fillRect(100, 80, patternWidth, patternHeight);
        g.dispose();
        
        System.out.println("Created screen: " + screenWidth + "x" + screenHeight);
        System.out.println("Pattern placed at: (100, 80)");
        
        // Create ScreenImage
        ScreenImage screenImage = new ScreenImage(
            new Rectangle(0, 0, screenWidth, screenHeight), screenImg);
        
        // Find the pattern
        Pattern pattern = new Pattern(patternFile.getAbsolutePath());
        pattern.similar(0.95);
        
        Finder finder = new Finder(screenImage);
        finder.find(pattern);
        
        if (!finder.hasNext()) {
            throw new RuntimeException("ERROR: Pattern not found!");
        }
        
        Match match = finder.next();
        Rectangle matchRect = match.getRect();
        
        System.out.println("\nMatch found:");
        System.out.println("  Position: (" + matchRect.x + ", " + matchRect.y + ")");
        System.out.println("  Size: " + matchRect.width + "x" + matchRect.height);
        
        // Verify dimensions
        if (matchRect.width != patternWidth) {
            throw new RuntimeException("ERROR: Match width " + matchRect.width + 
                " != pattern width " + patternWidth);
        }
        if (matchRect.height != patternHeight) {
            throw new RuntimeException("ERROR: Match height " + matchRect.height + 
                " != pattern height " + patternHeight);
        }
        
        // Verify position
        if (matchRect.x != 100 || matchRect.y != 80) {
            throw new RuntimeException("ERROR: Match position incorrect");
        }
        
        System.out.println("✓ PASS: Dimensions and position correct!\n");
    }
    
    private static void testVariousSizes() throws IOException {
        System.out.println("Test 2: Various Pattern Sizes");
        System.out.println("------------------------------");
        
        int[][] sizes = {
            {30, 20},
            {50, 50},
            {100, 75},
            {150, 100}
        };
        
        File testDir = new File("target/dimension-test");
        
        for (int[] size : sizes) {
            int w = size[0];
            int h = size[1];
            
            System.out.print("Testing " + w + "x" + h + " pattern... ");
            
            // Create pattern
            BufferedImage patternImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = patternImg.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
            g.dispose();
            
            File patternFile = new File(testDir, "pattern-" + w + "x" + h + ".png");
            ImageIO.write(patternImg, "PNG", patternFile);
            
            // Create screen with pattern
            BufferedImage screenImg = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
            g = screenImg.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 400, 300);
            g.setColor(Color.BLACK);
            g.fillRect(50, 50, w, h);
            g.dispose();
            
            // Find pattern
            ScreenImage screenImage = new ScreenImage(
                new Rectangle(0, 0, 400, 300), screenImg);
            Pattern pattern = new Pattern(patternFile.getAbsolutePath()).similar(0.95);
            
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (!finder.hasNext()) {
                throw new RuntimeException("Pattern not found!");
            }
            
            Match match = finder.next();
            Rectangle rect = match.getRect();
            
            if (rect.width != w || rect.height != h) {
                throw new RuntimeException("Dimension mismatch! Got " + 
                    rect.width + "x" + rect.height);
            }
            
            System.out.println("✓ Match: " + rect.width + "x" + rect.height);
        }
        
        System.out.println("✓ PASS: All sizes matched correctly!\n");
    }
    
    private static void testActualCapture() throws IOException {
        System.out.println("Test 3: Actual Screen Capture");
        System.out.println("------------------------------");
        
        // Ensure physical resolution is active
        PhysicalResolutionInitializer.forceInitialization();
        
        // Test capturing a specific region
        PhysicalResolutionScreen screen = new PhysicalResolutionScreen();
        
        // Capture a 150x100 region
        Rectangle region = new Rectangle(10, 10, 150, 100);
        ScreenImage capture = screen.capture(region);
        BufferedImage img = capture.getImage();
        
        System.out.println("Requested capture: " + region.width + "x" + region.height);
        System.out.println("Actual capture: " + img.getWidth() + "x" + img.getHeight());
        
        if (img.getWidth() != region.width || img.getHeight() != region.height) {
            throw new RuntimeException("ERROR: Capture dimensions don't match request!");
        }
        
        // Save for inspection
        File testDir = new File("target/dimension-test");
        File captureFile = new File(testDir, "capture.png");
        ImageIO.write(img, "PNG", captureFile);
        System.out.println("Saved capture to: " + captureFile.getAbsolutePath());
        
        System.out.println("✓ PASS: Screen capture dimensions correct!\n");
    }
}