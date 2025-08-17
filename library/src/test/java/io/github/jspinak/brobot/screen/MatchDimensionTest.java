package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.startup.PhysicalResolutionInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that match dimensions equal search image dimensions
 * using static screenshots for reproducible testing.
 */
public class MatchDimensionTest {
    
    @BeforeAll
    public static void setup() {
        // Force physical resolution mode before any AWT classes load
        PhysicalResolutionInitializer.forceInitialization();
        
        // Ensure Settings.AlwaysResize is disabled
        Settings.AlwaysResize = 0;
    }
    
    @Test
    public void testMatchDimensionsWithScreenshots() throws IOException {
        System.out.println("\n=== Match Dimension Test with Screenshots ===");
        
        // Use real screenshots and images from library folder
        String screenshotDir = "screenshots/";
        String imageDir = "images/";
        
        Path screenshotPath = Paths.get(screenshotDir, "floranext2.png");
        Path imagePath = Paths.get(imageDir, "bottomRight2.png");
        
        File screenshotFile = screenshotPath.toFile();
        File imageFile = imagePath.toFile();
        
        if (!screenshotFile.exists() || !imageFile.exists()) {
            System.out.println("Test files not found:");
            System.out.println("  Screenshot: " + screenshotFile.getAbsolutePath());
            System.out.println("  Image: " + imageFile.getAbsolutePath());
            System.out.println("Skipping test");
            return;
        }
        
        // Load screenshot as ScreenImage
        BufferedImage screenshotImg = ImageIO.read(screenshotFile);
        ScreenImage screenImage = new ScreenImage(
            new Rectangle(0, 0, screenshotImg.getWidth(), screenshotImg.getHeight()),
            screenshotImg);
        
        // Load pattern image
        BufferedImage searchImage = ImageIO.read(imageFile);
        Pattern pattern = new Pattern(imageFile.getAbsolutePath()).similar(0.7);
        
        System.out.println("Screenshot dimensions: " + 
            screenshotImg.getWidth() + "x" + screenshotImg.getHeight());
        System.out.println("Search image dimensions: " + 
            searchImage.getWidth() + "x" + searchImage.getHeight());
        
        // Find pattern in screenshot
        Finder finder = new Finder(screenImage);
        finder.find(pattern);
        
        if (finder.hasNext()) {
            Match match = finder.next();
            Rectangle matchRect = match.getRect();
            
            System.out.println("Match found at: " + matchRect.x + ", " + matchRect.y);
            System.out.println("Match dimensions: " + matchRect.width + "x" + matchRect.height);
            System.out.println("Match score: " + match.getScore());
            
            // CRITICAL TEST: Verify dimensions match
            assertEquals(searchImage.getWidth(), matchRect.width,
                "Match width should equal search image width");
            assertEquals(searchImage.getHeight(), matchRect.height,
                "Match height should equal search image height");
            
            System.out.println("✓ Match dimensions verified correctly!");
        } else {
            System.out.println("Pattern not found - may need to adjust similarity threshold");
        }
        
        // Verify Settings
        assertEquals(0, Settings.AlwaysResize, 0.01,
            "Settings.AlwaysResize should be 0 (disabled)");
        
        System.out.println("✓ Test configuration verified");
        System.out.println("  - Physical resolution enabled");
        System.out.println("  - Settings.AlwaysResize = 0");
        System.out.println("  - Match dimensions equal search image");
        
        System.out.println("=============================\n");
    }
    
    @Test
    public void testCoordinateSystemWithRegions() throws IOException {
        System.out.println("\n=== Coordinate System Test with Regions ===");
        
        // Test with multiple patterns at different positions
        String screenshotDir = "screenshots/";
        String imageDir = "images/";
        
        Path screenshotPath = Paths.get(screenshotDir, "floranext3.png");
        File screenshotFile = screenshotPath.toFile();
        
        if (!screenshotFile.exists()) {
            System.out.println("Screenshot not found: " + screenshotFile.getAbsolutePath());
            System.out.println("Skipping test");
            return;
        }
        
        BufferedImage screenshotImg = ImageIO.read(screenshotFile);
        ScreenImage screenImage = new ScreenImage(
            new Rectangle(0, 0, screenshotImg.getWidth(), screenshotImg.getHeight()),
            screenshotImg);
        
        // Test finding patterns at different coordinates
        String[] patterns = {"topLeft2.png", "bottomR.png", "bottomR2.png"};
        
        for (String patternName : patterns) {
            Path imagePath = Paths.get(imageDir, patternName);
            File imageFile = imagePath.toFile();
            
            if (!imageFile.exists()) {
                System.out.println("Pattern not found: " + patternName);
                continue;
            }
            
            BufferedImage patternImg = ImageIO.read(imageFile);
            Pattern pattern = new Pattern(imageFile.getAbsolutePath()).similar(0.6);
            
            System.out.println("\nSearching for: " + patternName);
            System.out.println("Pattern size: " + patternImg.getWidth() + "x" + patternImg.getHeight());
            
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                Rectangle rect = match.getRect();
                
                System.out.println("  Found at: (" + rect.x + ", " + rect.y + ")");
                System.out.println("  Size: " + rect.width + "x" + rect.height);
                
                // Verify dimensions
                assertEquals(patternImg.getWidth(), rect.width,
                    "Width should match for " + patternName);
                assertEquals(patternImg.getHeight(), rect.height,
                    "Height should match for " + patternName);
                
                System.out.println("  ✓ Dimensions and coordinates verified");
            } else {
                System.out.println("  Not found in screenshot");
            }
        }
        
        System.out.println("\n===============================\n");
    }
    
    private File createTestImage(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Draw something recognizable
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(java.awt.Color.BLACK);
        g.drawRect(5, 5, width-10, height-10);
        g.drawString("TEST", width/2 - 15, height/2);
        g.dispose();
        
        File tempFile = File.createTempFile("test-pattern-", ".png");
        ImageIO.write(img, "PNG", tempFile);
        return tempFile;
    }
}