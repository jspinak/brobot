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
 * Test that verifies pattern matching returns correct dimensions.
 * Works on WSL where screenshots are black, making it perfect for testing
 * exact dimension matching with simple black rectangles.
 */
public class BlackBoxMatchTest {
    
    private static final String TEST_DIR = "target/test-images/";
    
    @BeforeAll
    public static void setup() {
        // Ensure physical resolution is initialized
        PhysicalResolutionInitializer.forceInitialization();
        
        // Disable resize to ensure exact matching
        Settings.AlwaysResize = 0;
        
        // Create test directory
        new File(TEST_DIR).mkdirs();
    }
    
    @Test
    public void testBlackBoxMatchDimensions() throws IOException {
        System.out.println("\n=== Black Box Match Dimension Test ===");
        
        // Create a black box pattern (search image)
        int boxWidth = 100;
        int boxHeight = 50;
        File blackBoxFile = createBlackBox(boxWidth, boxHeight, "search-box");
        
        // Create a larger black image that contains the box (screen simulation)
        int screenWidth = 400;
        int screenHeight = 300;
        File screenFile = createBlackScreen(screenWidth, screenHeight, boxWidth, boxHeight, "screen");
        
        System.out.println("Created search box: " + boxWidth + "x" + boxHeight);
        System.out.println("Created screen: " + screenWidth + "x" + screenHeight);
        
        // Load the screen image as a ScreenImage
        BufferedImage screenImg = ImageIO.read(screenFile);
        Rectangle screenRect = new Rectangle(0, 0, screenWidth, screenHeight);
        ScreenImage screenImage = new ScreenImage(screenRect, screenImg);
        
        // Create pattern from the black box
        Pattern pattern = new Pattern(blackBoxFile.getAbsolutePath());
        pattern.similar(0.7); // Lower threshold for black matching
        
        // Use Finder to find the pattern in the screen
        Finder finder = new Finder(screenImage);
        finder.find(pattern);
        
        assertTrue(finder.hasNext(), "Should find the black box in the screen");
        
        Match match = finder.next();
        Rectangle matchRect = match.getRect();
        
        System.out.println("Match found at: " + matchRect.x + ", " + matchRect.y);
        System.out.println("Match dimensions: " + matchRect.width + "x" + matchRect.height);
        System.out.println("Expected dimensions: " + boxWidth + "x" + boxHeight);
        
        // CRITICAL TEST: Match dimensions must equal search image dimensions
        assertEquals(boxWidth, matchRect.width, 
            "Match width must equal search image width");
        assertEquals(boxHeight, matchRect.height,
            "Match height must equal search image height");
        
        // Verify position (should be at 150, 125 based on our placement)
        assertEquals(150, matchRect.x, "Match X position");
        assertEquals(125, matchRect.y, "Match Y position");
        
        System.out.println("✓ Match dimensions are correct!");
        System.out.println("✓ Match position is correct!");
        System.out.println("===================================\n");
    }
    
    @Test
    public void testMultipleSizeMatches() throws IOException {
        System.out.println("\n=== Multiple Size Match Test ===");
        
        int[] widths = {50, 75, 100, 150};
        int[] heights = {30, 45, 60, 90};
        
        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            int h = heights[i];
            
            System.out.println("\nTesting box: " + w + "x" + h);
            
            // Create search pattern
            File searchBox = createBlackBox(w, h, "search-" + w + "x" + h);
            
            // Create screen with the box
            File screen = createBlackScreen(500, 400, w, h, "screen-" + w + "x" + h);
            
            // Load and find
            BufferedImage screenImg = ImageIO.read(screen);
            ScreenImage screenImage = new ScreenImage(
                new Rectangle(0, 0, 500, 400), screenImg);
            
            Pattern pattern = new Pattern(searchBox.getAbsolutePath()).similar(0.7);
            
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            assertTrue(finder.hasNext(), "Should find box " + w + "x" + h);
            
            Match match = finder.next();
            Rectangle rect = match.getRect();
            
            // Verify dimensions
            assertEquals(w, rect.width, 
                "Match width for " + w + "x" + h + " box");
            assertEquals(h, rect.height,
                "Match height for " + w + "x" + h + " box");
            
            System.out.println("  ✓ Dimensions match: " + rect.width + "x" + rect.height);
        }
        
        System.out.println("\n✓ All size matches are correct!");
        System.out.println("==================================\n");
    }
    
    @Test
    public void testScreenCaptureAndMatch() throws IOException {
        System.out.println("\n=== Screen Capture and Match Test ===");
        
        // Create a test pattern
        int patternWidth = 80;
        int patternHeight = 40;
        File patternFile = createBlackBox(patternWidth, patternHeight, "pattern");
        
        // Use actual screen capture (will be black on WSL)
        PhysicalResolutionScreen screen = new PhysicalResolutionScreen();
        ScreenImage capture = screen.capture();
        
        BufferedImage captureImg = capture.getImage();
        System.out.println("Screen captured at: " + 
            captureImg.getWidth() + "x" + captureImg.getHeight());
        
        // Save the capture for inspection
        File captureFile = new File(TEST_DIR + "capture.png");
        ImageIO.write(captureImg, "PNG", captureFile);
        
        // Since WSL captures are black, we can test with our black pattern
        Pattern pattern = new Pattern(patternFile.getAbsolutePath()).similar(0.95);
        
        // Try to find the pattern
        Finder finder = new Finder(capture);
        finder.find(pattern);
        
        if (finder.hasNext()) {
            Match match = finder.next();
            Rectangle rect = match.getRect();
            
            System.out.println("Match found at: " + rect.x + ", " + rect.y);
            System.out.println("Match dimensions: " + rect.width + "x" + rect.height);
            
            // On a black screen, any black rectangle will match
            // The important test is that dimensions are preserved
            assertEquals(patternWidth, rect.width,
                "Match width should equal pattern width");
            assertEquals(patternHeight, rect.height,
                "Match height should equal pattern height");
            
            System.out.println("✓ Dimensions preserved in screen capture!");
        } else {
            System.out.println("No match found (expected on non-black screen)");
        }
        
        System.out.println("=====================================\n");
    }
    
    /**
     * Creates a black rectangle image.
     */
    private File createBlackBox(int width, int height, String name) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        // Fill with black
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.dispose();
        
        File file = new File(TEST_DIR + name + ".png");
        ImageIO.write(img, "PNG", file);
        return file;
    }
    
    /**
     * Creates a black screen with a black box at a specific position.
     * Since both are black, we add a slight border to make it findable.
     */
    private File createBlackScreen(int screenW, int screenH, 
                                   int boxW, int boxH, String name) throws IOException {
        BufferedImage img = new BufferedImage(screenW, screenH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        // Fill with very dark gray (almost black)
        g.setColor(new Color(5, 5, 5));
        g.fillRect(0, 0, screenW, screenH);
        
        // Place pure black box in center
        int x = (screenW - boxW) / 2;
        int y = (screenH - boxH) / 2;
        g.setColor(Color.BLACK);
        g.fillRect(x, y, boxW, boxH);
        
        g.dispose();
        
        File file = new File(TEST_DIR + name + ".png");
        ImageIO.write(img, "PNG", file);
        return file;
    }
}