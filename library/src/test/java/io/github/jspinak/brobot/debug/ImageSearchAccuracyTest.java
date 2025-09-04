package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.Rectangle;
import java.awt.Robot;

/**
 * Test to verify if SikuliX image searches work correctly despite highlighting issues.
 * This is critical - if searches are also broken, the entire automation won't work.
 */
public class ImageSearchAccuracyTest extends DebugTestBase {
    
    @Test
    public void testImageSearchAccuracy() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("IMAGE SEARCH ACCURACY TEST");
        System.out.println("Testing if SikuliX can find images at correct coordinates");
        System.out.println("================================================================================\n");
        
        Screen screen = new Screen();
        Robot robot = new Robot();
        
        // Test 1: Create and find a known pattern
        System.out.println("--- Test 1: Screenshot and Search Test ---");
        
        // Take a screenshot of a small region at a known location
        int testX = 100;
        int testY = 100;
        int testW = 200;
        int testH = 200;
        
        Rectangle captureRect = new Rectangle(testX, testY, testW, testH);
        BufferedImage capturedImage = robot.createScreenCapture(captureRect);
        
        // Save the captured image
        File patternFile = new File("test_pattern.png");
        ImageIO.write(capturedImage, "png", patternFile);
        System.out.printf("Captured region at (%d,%d,%d,%d) and saved to %s%n", 
                testX, testY, testW, testH, patternFile.getName());
        
        // Now try to find this pattern on the screen
        Pattern pattern = new Pattern(patternFile.getAbsolutePath());
        pattern.similar(0.95); // High similarity threshold
        
        System.out.println("Searching for the pattern on screen...");
        
        try {
            Match match = screen.find(pattern);
            
            System.out.println("PATTERN FOUND!");
            System.out.printf("  Match location: x=%d, y=%d%n", match.x, match.y);
            System.out.printf("  Match region: %s%n", match);
            System.out.printf("  Match score: %.3f%n", match.getScore());
            
            // Check if the match is at the expected location
            boolean correctLocation = (Math.abs(match.x - testX) < 5) && 
                                    (Math.abs(match.y - testY) < 5);
            
            if (correctLocation) {
                System.out.println("✓ MATCH IS AT THE CORRECT LOCATION!");
            } else {
                System.out.printf("✗ MATCH IS AT WRONG LOCATION!%n");
                System.out.printf("  Expected: (%d,%d)%n", testX, testY);
                System.out.printf("  Found at: (%d,%d)%n", match.x, match.y);
                System.out.printf("  Offset: x=%d, y=%d%n", 
                        match.x - testX, match.y - testY);
            }
            
            // Highlight the match to see where it appears
            System.out.println("Highlighting the match location...");
            match.highlight(2);
            
        } catch (FindFailed e) {
            System.out.println("✗ PATTERN NOT FOUND!");
            System.out.println("This suggests image searching is broken.");
        }
        
        // Test 2: Search in specific regions
        System.out.println("\n--- Test 2: Region-Constrained Search ---");
        
        // Define different search regions
        Region topLeft = new Region(0, 0, 500, 500);
        Region bottomRight = new Region(screen.w - 500, screen.h - 500, 500, 500);
        Region center = new Region(screen.w/2 - 250, screen.h/2 - 250, 500, 500);
        
        // Take a screenshot from center region
        Rectangle centerRect = new Rectangle(center.x, center.y, 100, 100);
        BufferedImage centerImage = robot.createScreenCapture(centerRect);
        File centerFile = new File("center_pattern.png");
        ImageIO.write(centerImage, "png", centerFile);
        
        Pattern centerPattern = new Pattern(centerFile.getAbsolutePath());
        
        System.out.println("Testing region-constrained searches:");
        
        // Search in correct region (should find)
        System.out.printf("Searching in CENTER region %s...%n", center);
        try {
            Match centerMatch = center.find(centerPattern);
            System.out.printf("  ✓ Found in center: %s%n", centerMatch);
        } catch (FindFailed e) {
            System.out.println("  ✗ Not found in center (UNEXPECTED!)");
        }
        
        // Search in wrong region (should not find)
        System.out.printf("Searching in TOP-LEFT region %s...%n", topLeft);
        try {
            Match wrongMatch = topLeft.find(centerPattern);
            System.out.printf("  ✗ Found in top-left (UNEXPECTED!): %s%n", wrongMatch);
        } catch (FindFailed e) {
            System.out.println("  ✓ Not found in top-left (expected)");
        }
        
        // Test 3: Mouse click accuracy
        System.out.println("\n--- Test 3: Click Accuracy Test ---");
        
        // Move mouse to a known position
        int clickX = 300;
        int clickY = 300;
        
        System.out.printf("Moving mouse to (%d,%d)...%n", clickX, clickY);
        screen.mouseMove(new Location(clickX, clickY));
        Thread.sleep(500);
        
        // Get actual mouse position
        Location mousePos = Env.getMouseLocation();
        System.out.printf("Mouse position: %s%n", mousePos);
        
        boolean mouseCorrect = (Math.abs(mousePos.x - clickX) < 5) && 
                              (Math.abs(mousePos.y - clickY) < 5);
        
        if (mouseCorrect) {
            System.out.println("✓ Mouse moved to correct position!");
        } else {
            System.out.printf("✗ Mouse at wrong position!%n");
            System.out.printf("  Expected: (%d,%d)%n", clickX, clickY);
            System.out.printf("  Actual: (%d,%d)%n", mousePos.x, mousePos.y);
        }
        
        // Test 4: Coordinate system consistency
        System.out.println("\n--- Test 4: Coordinate System Consistency ---");
        
        // Capture at different locations and search for them
        int[] testXCoords = {0, 200, 400, 600, 800};
        int[] testYCoords = {0, 200, 400, 600};
        
        int successCount = 0;
        int totalTests = 0;
        
        for (int x : testXCoords) {
            for (int y : testYCoords) {
                if (x + 100 > screen.w || y + 100 > screen.h) continue;
                
                totalTests++;
                
                // Capture a small region
                Rectangle rect = new Rectangle(x, y, 100, 100);
                BufferedImage img = robot.createScreenCapture(rect);
                File file = new File(String.format("test_%d_%d.png", x, y));
                ImageIO.write(img, "png", file);
                
                // Search for it
                Pattern p = new Pattern(file.getAbsolutePath()).similar(0.95);
                
                try {
                    Match m = screen.find(p);
                    
                    // Check if found at correct location
                    if (Math.abs(m.x - x) < 5 && Math.abs(m.y - y) < 5) {
                        successCount++;
                        System.out.printf("  ✓ (%d,%d) found correctly%n", x, y);
                    } else {
                        System.out.printf("  ✗ (%d,%d) found at wrong location (%d,%d)%n", 
                                x, y, m.x, m.y);
                    }
                    
                    file.delete(); // Clean up
                    
                } catch (FindFailed e) {
                    System.out.printf("  ✗ (%d,%d) not found%n", x, y);
                    file.delete();
                }
            }
        }
        
        System.out.printf("\nCoordinate consistency: %d/%d successful (%.1f%%)%n",
                successCount, totalTests, (100.0 * successCount / totalTests));
        
        if (successCount == totalTests) {
            System.out.println("✓ IMAGE SEARCHING WORKS CORRECTLY!");
            System.out.println("  Despite highlighting issues, pattern matching is accurate.");
        } else if (successCount > totalTests / 2) {
            System.out.println("⚠ IMAGE SEARCHING PARTIALLY WORKS");
            System.out.println("  Some searches are accurate but not all.");
        } else {
            System.out.println("✗ IMAGE SEARCHING IS BROKEN");
            System.out.println("  Pattern matching has the same coordinate issues as highlighting.");
        }
        
        // Clean up test files
        patternFile.delete();
        centerFile.delete();
        
        System.out.println("\n================================================================================");
        System.out.println("TEST COMPLETE");
        System.out.println("================================================================================\n");
    }
}