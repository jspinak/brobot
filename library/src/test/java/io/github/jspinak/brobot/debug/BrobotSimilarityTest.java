package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Image;
import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test to verify what similarity threshold Brobot is actually using
 * and compare with direct SikuliX matching
 */
@DisabledInCI
public class BrobotSimilarityTest extends BrobotTestBase {
    
    @Test
    public void testBrobotPatternMatching() {
        System.out.println("=== BROBOT PATTERN MATCHING TEST ===\n");
        
        try {
            // Give user time to switch to the target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 5 seconds to make the target screen visible...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Starting test...\n");
            
            // Load pattern image the way Brobot does
            String patternPath = "images/prompt/claude-prompt-1.png";
            File file = new File(patternPath);
            BufferedImage buffImg = ImageIO.read(file);
            
            System.out.println("1. PATTERN IMAGE:");
            System.out.println("   Path: " + patternPath);
            System.out.println("   Loaded size: " + buffImg.getWidth() + "x" + buffImg.getHeight());
            System.out.println("   Type: " + getImageTypeName(buffImg.getType()));
            
            // Create Brobot Pattern
            Pattern brobotPattern = new Pattern(buffImg);
            brobotPattern.setName("claude-prompt-1");
            
            // Get the SikuliX pattern from Brobot
            org.sikuli.script.Pattern sikuliPattern = brobotPattern.sikuli();
            System.out.println("\n2. BROBOT PATTERN CONVERSION:");
            System.out.println("   SikuliX Pattern created");
            System.out.println("   Pattern similarity setting: " + sikuliPattern.getSimilar());
            
            // Check global settings
            System.out.println("\n3. GLOBAL SETTINGS:");
            System.out.println("   Settings.MinSimilarity: " + org.sikuli.basics.Settings.MinSimilarity);
            System.out.println("   Settings.AlwaysResize: " + org.sikuli.basics.Settings.AlwaysResize);
            System.out.println("   Settings.CheckLastSeen: " + org.sikuli.basics.Settings.CheckLastSeen);
            
            // Capture screen
            Screen screen = new Screen();
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImg = screenCapture.getImage();
            System.out.println("\n4. SCREEN CAPTURE:");
            System.out.println("   Screen size: " + screenImg.getWidth() + "x" + screenImg.getHeight());
            System.out.println("   Type: " + getImageTypeName(screenImg.getType()));
            
            // Test matching with different thresholds
            System.out.println("\n5. TESTING DIFFERENT SIMILARITY THRESHOLDS:");
            double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60, 0.55, 0.50};
            
            for (double threshold : thresholds) {
                // Set the threshold
                org.sikuli.basics.Settings.MinSimilarity = threshold;
                sikuliPattern = sikuliPattern.similar(threshold);
                
                // Create new Finder for each test
                Finder finder = new Finder(screenImg);
                finder.findAll(sikuliPattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("   Threshold " + String.format("%.2f", threshold) + 
                        ": FOUND with score " + String.format("%.3f", match.getScore()) +
                        " at (" + match.x + ", " + match.y + ")");
                    
                    // Count total matches
                    int count = 1;
                    while (finder.hasNext()) {
                        finder.next();
                        count++;
                    }
                    if (count > 1) {
                        System.out.println("                  (Total " + count + " matches found)");
                    }
                    
                    finder.destroy();
                    break; // Stop at first successful threshold
                } else {
                    System.out.println("   Threshold " + String.format("%.2f", threshold) + ": No match");
                }
                finder.destroy();
            }
            
            // Now test with Brobot's actual similarity setting
            System.out.println("\n6. TESTING WITH BROBOT'S ACTUAL SETTINGS:");
            
            // Reset to default
            org.sikuli.basics.Settings.MinSimilarity = 0.7;
            
            // Get fresh SikuliX pattern from Brobot (simulating what happens in ScenePatternMatcher)
            sikuliPattern = brobotPattern.sikuli();
            double patternSimilarity = sikuliPattern.getSimilar();
            System.out.println("   Pattern similarity from sikuli(): " + patternSimilarity);
            
            // Update to global similarity if different
            double globalSimilarity = org.sikuli.basics.Settings.MinSimilarity;
            if (Math.abs(sikuliPattern.getSimilar() - globalSimilarity) > 0.01) {
                sikuliPattern = sikuliPattern.similar(globalSimilarity);
                System.out.println("   Updated pattern similarity to: " + sikuliPattern.getSimilar());
            }
            
            // Test with this setting
            Finder finder = new Finder(screenImg);
            finder.findAll(sikuliPattern);
            
            int matchCount = 0;
            double bestScore = 0;
            while (finder.hasNext()) {
                Match match = finder.next();
                matchCount++;
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                }
                if (matchCount <= 3) {
                    System.out.println("   Match #" + matchCount + ": score=" + 
                        String.format("%.3f", match.getScore()) + " at (" + match.x + ", " + match.y + ")");
                }
            }
            
            if (matchCount > 3) {
                System.out.println("   ... and " + (matchCount - 3) + " more matches");
            }
            
            System.out.println("\n   RESULT: " + matchCount + " total matches, best score: " + 
                String.format("%.3f", bestScore));
            
            finder.destroy();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB (Type 1)";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB (Type 2)";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "ARGB_PRE (Type 3)";
            case BufferedImage.TYPE_INT_BGR: return "BGR (Type 4)";
            case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR (Type 5)";
            case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR (Type 6)";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE (Type 7)";
            default: return "Type " + type;
        }
    }
}