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

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify match dimensions equal search image dimensions
 * using static screenshots instead of live screen capture.
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
    public void testWithScreenshotImages() throws IOException {
        System.out.println("\n=== Screenshot Image Match Test ===");
        
        // Use screenshots from library folder
        String screenshotDir = "screenshots/";
        String imageDir = "images/";
        
        Path screenshotPath = Paths.get(screenshotDir, "floranext1.png");
        Path[] imagePaths = {
            Paths.get(imageDir, "topLeft.png"),
            Paths.get(imageDir, "bottomRight.png")
        };
        
        File screenshotFile = screenshotPath.toFile();
        if (!screenshotFile.exists()) {
            System.out.println("Screenshot not found: " + screenshotFile.getAbsolutePath());
            System.out.println("Skipping test");
            return;
        }
        
        // Load screenshot
        BufferedImage screenshotImg = ImageIO.read(screenshotFile);
        ScreenImage screenImage = new ScreenImage(
            new Rectangle(0, 0, screenshotImg.getWidth(), screenshotImg.getHeight()),
            screenshotImg);
        
        System.out.println("Screenshot loaded: " + screenshotImg.getWidth() + "x" + screenshotImg.getHeight());
        
        // Test matching each pattern
        for (Path imagePath : imagePaths) {
            File imageFile = imagePath.toFile();
            if (!imageFile.exists()) {
                System.out.println("Pattern not found: " + imageFile.getAbsolutePath());
                continue;
            }
            
            BufferedImage patternImg = ImageIO.read(imageFile);
            Pattern pattern = new Pattern(imageFile.getAbsolutePath()).similar(0.7);
            
            System.out.println("\nTesting pattern: " + imageFile.getName());
            System.out.println("Pattern size: " + patternImg.getWidth() + "x" + patternImg.getHeight());
            
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                Rectangle rect = match.getRect();
                
                System.out.println("Match found at: " + rect.x + ", " + rect.y);
                System.out.println("Match size: " + rect.width + "x" + rect.height);
                
                // CRITICAL ASSERTION: Match dimensions must equal pattern dimensions
                assertEquals(patternImg.getWidth(), rect.width,
                    "Match width MUST equal pattern width for " + imageFile.getName());
                assertEquals(patternImg.getHeight(), rect.height,
                    "Match height MUST equal pattern height for " + imageFile.getName());
                
                System.out.println("✓ Dimensions match correctly!");
            } else {
                System.out.println("Pattern not found in screenshot");
            }
        }
        
        System.out.println("\n===================================\n");
    }
}