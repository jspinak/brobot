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
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.Iterator;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test to see if patterns can be found anywhere on screen,
 * even if not at expected locations.
 */
@DisabledInCI
public class FullScreenSearchTest extends DebugTestBase {
    
    @Test
    public void findPatternsAnywhere() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("FULL SCREEN SEARCH TEST");
        System.out.println("Trying to find patterns anywhere on screen");
        System.out.println("================================================================================\n");
        
        Screen screen = new Screen();
        Robot robot = new Robot();
        
        // Test 1: Capture a very distinctive pattern and search everywhere
        System.out.println("--- Test 1: Create and Search for Distinctive Pattern ---");
        
        // Capture from center of screen (should be visible)
        int captureX = screen.w / 2 - 50;
        int captureY = screen.h / 2 - 50;
        
        Rectangle captureRect = new Rectangle(captureX, captureY, 100, 100);
        BufferedImage capturedImage = robot.createScreenCapture(captureRect);
        
        File patternFile = new File("distinctive_pattern.png");
        ImageIO.write(capturedImage, "png", patternFile);
        
        System.out.printf("Captured pattern from (%d,%d)%n", captureX, captureY);
        System.out.println("Saved as: " + patternFile.getAbsolutePath());
        
        // Lower similarity threshold to increase chances of finding
        Pattern pattern = new Pattern(patternFile.getAbsolutePath()).similar(0.7);
        
        // Search entire screen
        System.out.println("\nSearching entire screen for the pattern...");
        
        try {
            // Find all occurrences
            Iterator<Match> matches = screen.findAll(pattern);
            
            int count = 0;
            while (matches.hasNext()) {
                Match match = matches.next();
                count++;
                
                System.out.printf("FOUND #%d: %s%n", count, match);
                System.out.printf("  Location: (%d,%d)%n", match.x, match.y);
                System.out.printf("  Score: %.3f%n", match.getScore());
                
                // Calculate offset from expected location
                int offsetX = match.x - captureX;
                int offsetY = match.y - captureY;
                System.out.printf("  Offset from expected: (%d,%d)%n", offsetX, offsetY);
                
                // Highlight to see where it thinks it found it
                match.highlight(1);
                Thread.sleep(1100);
            }
            
            if (count == 0) {
                System.out.println("NO MATCHES FOUND!");
            } else {
                System.out.printf("\nTotal matches found: %d%n", count);
            }
            
        } catch (FindFailed e) {
            System.out.println("FindFailed exception: Pattern not found anywhere!");
        }
        
        // Test 2: Try with exists() which is more lenient
        System.out.println("\n--- Test 2: Using exists() Method ---");
        
        Match existsMatch = screen.exists(pattern, 5.0); // Wait up to 5 seconds
        
        if (existsMatch != null) {
            System.out.println("EXISTS found the pattern!");
            System.out.printf("  Location: %s%n", existsMatch);
            System.out.printf("  Offset from expected: (%d,%d)%n", 
                    existsMatch.x - captureX, existsMatch.y - captureY);
        } else {
            System.out.println("EXISTS could not find the pattern!");
        }
        
        // Test 3: Try capturing and searching at different scales
        System.out.println("\n--- Test 3: Testing Different Sizes ---");
        
        int[] sizes = {20, 50, 100, 200};
        
        for (int size : sizes) {
            if (captureX + size > screen.w || captureY + size > screen.h) continue;
            
            Rectangle rect = new Rectangle(captureX, captureY, size, size);
            BufferedImage img = robot.createScreenCapture(rect);
            
            File file = new File(String.format("size_%d.png", size));
            ImageIO.write(img, "png", file);
            
            Pattern p = new Pattern(file.getAbsolutePath()).similar(0.8);
            
            System.out.printf("Size %dx%d: ", size, size);
            
            try {
                Match m = screen.find(p);
                System.out.printf("FOUND at (%d,%d), offset=(%d,%d)%n",
                        m.x, m.y, m.x - captureX, m.y - captureY);
            } catch (FindFailed e) {
                System.out.println("NOT FOUND");
            }
            
            file.delete();
        }
        
        // Test 4: Check if screen capture itself is working
        System.out.println("\n--- Test 4: Screen Capture Verification ---");
        
        ScreenImage fullScreen = screen.capture();
        BufferedImage fullImage = fullScreen.getImage();
        
        System.out.printf("Full screen capture size: %dx%d%n",
                fullImage.getWidth(), fullImage.getHeight());
        
        // Save it to verify content
        File fullScreenFile = new File("full_screen_debug.png");
        ImageIO.write(fullImage, "png", fullScreenFile);
        System.out.println("Saved full screen to: " + fullScreenFile.getAbsolutePath());
        System.out.println("Check this file to see if screen capture is working correctly.");
        
        // Test 5: Try Robot vs SikuliX capture comparison
        System.out.println("\n--- Test 5: Robot vs SikuliX Capture Comparison ---");
        
        // Robot capture
        Rectangle robotRect = new Rectangle(100, 100, 100, 100);
        BufferedImage robotImage = robot.createScreenCapture(robotRect);
        File robotFile = new File("robot_capture.png");
        ImageIO.write(robotImage, "png", robotFile);
        
        // SikuliX capture
        Region sikuliRegion = new Region(100, 100, 100, 100);
        ScreenImage sikuliCapture = screen.capture(sikuliRegion);
        BufferedImage sikuliImage = sikuliCapture.getImage();
        File sikuliFile = new File("sikuli_capture.png");
        ImageIO.write(sikuliImage, "png", sikuliFile);
        
        System.out.println("Saved robot_capture.png and sikuli_capture.png");
        System.out.println("Compare these files to see if they show the same content.");
        
        // Clean up
        patternFile.delete();
        
        System.out.println("\n================================================================================");
        System.out.println("TEST COMPLETE");
        System.out.println("Check the generated PNG files to understand what's happening.");
        System.out.println("================================================================================\n");
    }
}