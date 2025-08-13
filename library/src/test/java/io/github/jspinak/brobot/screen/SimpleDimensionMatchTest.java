package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.startup.PhysicalResolutionInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify match dimensions equal search image dimensions.
 */
public class SimpleDimensionMatchTest {
    
    @BeforeAll
    public static void setup() {
        // Force physical resolution
        PhysicalResolutionInitializer.forceInitialization();
        Settings.AlwaysResize = 0;
    }
    
    @Test
    public void testMatchDimensionsSimple() throws IOException {
        System.out.println("\n=== Simple Dimension Match Test ===");
        
        // Create test directory
        File testDir = new File("target/dimension-test");
        testDir.mkdirs();
        
        // Create a simple test pattern (50x30 black rectangle)
        int patternWidth = 50;
        int patternHeight = 30;
        BufferedImage patternImg = new BufferedImage(patternWidth, patternHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = patternImg.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, patternWidth, patternHeight);
        g.dispose();
        
        File patternFile = new File(testDir, "pattern.png");
        ImageIO.write(patternImg, "PNG", patternFile);
        
        // Create a screen image containing the pattern (200x150 with pattern at 75,60)
        int screenWidth = 200;
        int screenHeight = 150;
        BufferedImage screenImg = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        g = screenImg.createGraphics();
        // Fill with gray
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, screenWidth, screenHeight);
        // Place black rectangle at specific position
        g.setColor(Color.BLACK);
        g.fillRect(75, 60, patternWidth, patternHeight);
        g.dispose();
        
        // Create ScreenImage from the BufferedImage
        ScreenImage screenImage = new ScreenImage(
            new Rectangle(0, 0, screenWidth, screenHeight), screenImg);
        
        // Create pattern and find it
        Pattern pattern = new Pattern(patternFile.getAbsolutePath());
        pattern.similar(0.95);
        
        Finder finder = new Finder(screenImage);
        finder.find(pattern);
        
        assertTrue(finder.hasNext(), "Should find the pattern");
        
        Match match = finder.next();
        Rectangle matchRect = match.getRect();
        
        System.out.println("Pattern size: " + patternWidth + "x" + patternHeight);
        System.out.println("Match found at: " + matchRect.x + ", " + matchRect.y);
        System.out.println("Match size: " + matchRect.width + "x" + matchRect.height);
        
        // CRITICAL ASSERTION: Match dimensions must equal pattern dimensions
        assertEquals(patternWidth, matchRect.width, 
            "Match width MUST equal pattern width");
        assertEquals(patternHeight, matchRect.height,
            "Match height MUST equal pattern height");
        
        // Also verify position
        assertEquals(75, matchRect.x, "Match X position");
        assertEquals(60, matchRect.y, "Match Y position");
        
        System.out.println("✓ SUCCESS: Match dimensions are correct!");
        System.out.println("====================================\n");
    }
    
    @Test
    public void testWithActualScreenCapture() throws IOException {
        System.out.println("\n=== Actual Screen Capture Test ===");
        
        // Force initialization again to be sure
        PhysicalResolutionInitializer.forceInitialization();
        
        // Capture actual screen
        PhysicalResolutionScreen screen = new PhysicalResolutionScreen();
        ScreenImage capture = screen.capture(new Rectangle(0, 0, 100, 100));
        
        BufferedImage img = capture.getImage();
        System.out.println("Captured region: 100x100");
        System.out.println("Actual capture size: " + img.getWidth() + "x" + img.getHeight());
        
        // The captured image should be exactly 100x100
        assertEquals(100, img.getWidth(), "Capture width should be 100");
        assertEquals(100, img.getHeight(), "Capture height should be 100");
        
        System.out.println("✓ Capture dimensions are correct!");
        System.out.println("===================================\n");
    }
}