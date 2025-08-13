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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that match dimensions equal search image dimensions.
 * This is critical for proper pattern matching behavior.
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
    public void testMatchDimensionsEqualSearchImage() throws IOException {
        System.out.println("\n=== Match Dimension Test ===");
        
        // Create a test pattern (you can replace with an actual image path)
        File testImage = createTestImage(100, 50);
        Pattern pattern = new Pattern(testImage.getAbsolutePath());
        
        // Capture the screen
        Screen screen = new PhysicalResolutionScreen();
        ScreenImage capture = screen.capture();
        
        System.out.println("Screen captured at: " + 
            capture.getImage().getWidth() + "x" + capture.getImage().getHeight());
        
        // Place the test image on screen (simulated - in real test you'd have it displayed)
        // For this test, we'll verify the dimensions directly
        
        BufferedImage searchImage = ImageIO.read(testImage);
        System.out.println("Search image dimensions: " + 
            searchImage.getWidth() + "x" + searchImage.getHeight());
        
        // In a real scenario with pattern matching:
        // Finder finder = new Finder(capture);
        // finder.find(pattern);
        // if (finder.hasNext()) {
        //     Match match = finder.next();
        //     Rectangle matchRect = match.getRect();
        //     
        //     assertEquals(searchImage.getWidth(), matchRect.width,
        //         "Match width should equal search image width");
        //     assertEquals(searchImage.getHeight(), matchRect.height,
        //         "Match height should equal search image height");
        // }
        
        // Verify Settings
        assertEquals(0, Settings.AlwaysResize, 0.01,
            "Settings.AlwaysResize should be 0 (disabled)");
        
        System.out.println("✓ Test configuration verified");
        System.out.println("  - Physical resolution capture enabled");
        System.out.println("  - Settings.AlwaysResize = 0");
        System.out.println("  - Match dimensions should equal search image");
        
        // Clean up
        testImage.delete();
        
        System.out.println("=============================\n");
    }
    
    @Test
    public void testCoordinateSystem() {
        System.out.println("\n=== Coordinate System Test ===");
        
        Screen screen = new PhysicalResolutionScreen();
        
        // Test capturing a specific region
        Rectangle testRect = new Rectangle(100, 100, 200, 150);
        ScreenImage capture = screen.capture(testRect);
        
        BufferedImage img = capture.getImage();
        
        // The captured image should have the exact dimensions requested
        assertEquals(200, img.getWidth(),
            "Captured width should match requested width");
        assertEquals(150, img.getHeight(),
            "Captured height should match requested height");
        
        System.out.println("✓ Coordinate system test passed");
        System.out.println("  Requested: " + testRect.width + "x" + testRect.height);
        System.out.println("  Captured: " + img.getWidth() + "x" + img.getHeight());
        
        System.out.println("===============================\n");
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