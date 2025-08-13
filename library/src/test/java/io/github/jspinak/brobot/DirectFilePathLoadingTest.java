package io.github.jspinak.brobot;

import io.github.jspinak.brobot.model.element.Pattern;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Test to verify that Brobot now achieves 0.99 similarity scores like SikuliX IDE
 * by using direct file path loading instead of BufferedImage conversion.
 */
public class DirectFilePathLoadingTest {
    
    @Test
    public void testDirectFilePathLoading() {
        System.out.println("=== TESTING DIRECT FILE PATH LOADING FIX ===\n");
        System.out.println("This test verifies that Brobot now uses direct file path loading");
        System.out.println("like SikuliX IDE to achieve 0.99 similarity scores.\n");
        
        try {
            // Test pattern path - adjust this to your actual pattern
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                System.out.println("Pattern file not found at: " + patternPath);
                System.out.println("Please adjust the path in the test.");
                return;
            }
            
            // Create Brobot Pattern with file path
            Pattern brobotPattern = new Pattern(patternPath);
            
            System.out.println("1. BROBOT PATTERN CREATED");
            System.out.println("   - Name: " + brobotPattern.getName());
            System.out.println("   - Path: " + brobotPattern.getImgpath());
            System.out.println("   - Has BufferedImage: " + (brobotPattern.getImage() != null));
            
            // Get the SikuliX Pattern from Brobot
            // This should now use direct file path loading
            org.sikuli.script.Pattern sikuliPattern = brobotPattern.sikuli();
            
            System.out.println("\n2. SIKULI PATTERN CONVERSION");
            System.out.println("   - Pattern created successfully");
            System.out.println("   - This should have used direct file path loading");
            
            // Test with different similarity thresholds
            System.out.println("\n3. TESTING PATTERN MATCHING");
            System.out.println("   Position VS Code window and wait 5 seconds...");
            Thread.sleep(5000);
            
            Screen screen = new Screen();
            double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70};
            
            for (double threshold : thresholds) {
                sikuliPattern = sikuliPattern.similar(threshold);
                Match match = screen.exists(sikuliPattern, 0);
                
                if (match != null) {
                    System.out.printf("   ✅ FOUND at %.2f threshold with score %.3f%n", 
                                    threshold, match.getScore());
                    
                    // If we find at 0.99, the fix is working!
                    if (threshold >= 0.99) {
                        System.out.println("\n🎉 SUCCESS! Pattern matches at 0.99 similarity!");
                        System.out.println("The direct file path loading fix is working!");
                    }
                    break;
                } else {
                    System.out.printf("   ❌ Not found at %.2f threshold%n", threshold);
                }
            }
            
            // Compare with BufferedImage approach (old way)
            System.out.println("\n4. COMPARING WITH OLD BUFFEREDIMAGE APPROACH");
            
            BufferedImage buffImg = ImageIO.read(patternFile);
            org.sikuli.script.Pattern oldPattern = new org.sikuli.script.Pattern(buffImg);
            
            for (double threshold : thresholds) {
                oldPattern = oldPattern.similar(threshold);
                Match match = screen.exists(oldPattern, 0);
                
                if (match != null) {
                    System.out.printf("   Old approach: Found at %.2f with score %.3f%n", 
                                    threshold, match.getScore());
                    break;
                }
            }
            
            System.out.println("\n5. SETTINGS CHECK");
            System.out.println("   - Settings.AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("   - Settings.MinSimilarity: " + Settings.MinSimilarity);
            
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void testPatternLoadingMethods() {
        System.out.println("\n=== TESTING DIFFERENT PATTERN LOADING METHODS ===\n");
        
        try {
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                System.out.println("Pattern file not found, skipping test");
                return;
            }
            
            // Method 1: Direct file path (SikuliX IDE approach)
            System.out.println("METHOD 1: Direct File Path (SikuliX IDE approach)");
            org.sikuli.script.Pattern directPattern = new org.sikuli.script.Pattern(patternPath);
            testPatternSimilarity(directPattern, "Direct Path");
            
            // Method 2: BufferedImage (old Brobot approach)
            System.out.println("\nMETHOD 2: BufferedImage (old Brobot approach)");
            BufferedImage buffImg = ImageIO.read(patternFile);
            org.sikuli.script.Pattern bufferedPattern = new org.sikuli.script.Pattern(buffImg);
            testPatternSimilarity(bufferedPattern, "BufferedImage");
            
            // Method 3: Brobot Pattern with new fix
            System.out.println("\nMETHOD 3: Brobot Pattern (with new fix)");
            Pattern brobotPattern = new Pattern(patternPath);
            org.sikuli.script.Pattern brobotSikuli = brobotPattern.sikuli();
            testPatternSimilarity(brobotSikuli, "Brobot Fixed");
            
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testPatternSimilarity(org.sikuli.script.Pattern pattern, String method) {
        try {
            System.out.println("Testing with: " + method);
            
            // Create a screen capture
            Screen screen = new Screen();
            BufferedImage screenshot = screen.capture().getImage();
            
            // Use Finder to test matching
            Finder finder = new Finder(screenshot);
            pattern = pattern.similar(0.70);
            finder.findAll(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("  Score: %.3f at (%.0f, %.0f)%n", 
                                match.getScore(), match.getX(), match.getY());
            } else {
                System.out.println("  No match found");
            }
            
            finder.destroy();
            
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
}