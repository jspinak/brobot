package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import java.io.File;

/**
 * Final test to verify DPI scaling solution.
 */
public class FinalDPITest extends BrobotTestBase {
    
    @Test
    public void testDPIScalingSolution() throws Exception {
        System.out.println("=== FINAL DPI SCALING TEST ===\n");
        
        // Set the ImagePath to find our patterns
        String currentDir = new File(".").getAbsolutePath();
        ImagePath.setBundlePath(currentDir);
        System.out.println("ImagePath set to: " + currentDir);
        
        Screen screen = new Screen();
        String patternPath = "images/prompt/claude-prompt-1.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.out.println("Pattern file not found: " + patternFile.getAbsolutePath());
            return;
        }
        
        System.out.println("Pattern file found: " + patternFile.getAbsolutePath());
        System.out.println("\n1. TESTING WITHOUT FIX (AlwaysResize = 1.0):");
        System.out.println("----------------------------------------------");
        
        Settings.AlwaysResize = 1.0f;
        testPatternMatching(screen, patternPath, "No scaling");
        
        System.out.println("\n2. TESTING WITH FIX (AlwaysResize = 0.8):");
        System.out.println("-------------------------------------------");
        
        Settings.AlwaysResize = 0.8f;
        testPatternMatching(screen, patternPath, "125% scaling fix");
        
        System.out.println("\n=== CONCLUSION ===");
        System.out.println("If the pattern was found with 0.8 scaling but not with 1.0,");
        System.out.println("this confirms the DPI scaling issue and validates the fix.");
        System.out.println("\nTo use this fix in Brobot:");
        System.out.println("1. Add DPIConfigurationuration to your Spring context");
        System.out.println("2. Or manually set: Settings.AlwaysResize = 0.8f");
    }
    
    private void testPatternMatching(Screen screen, String patternPath, String description) {
        System.out.println("Testing: " + description);
        System.out.println("Settings.AlwaysResize = " + Settings.AlwaysResize);
        
        // Try different similarity thresholds
        double[] thresholds = {0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65};
        boolean found = false;
        
        for (double threshold : thresholds) {
            try {
                Pattern pattern = new Pattern(patternPath).similar(threshold);
                Match match = screen.find(pattern);
                System.out.println("✓ FOUND at threshold " + threshold);
                System.out.println("  Actual similarity score: " + String.format("%.3f", match.getScore()));
                System.out.println("  Location: " + match.getTarget());
                found = true;
                break;
            } catch (FindFailed e) {
                // Continue to next threshold
            }
        }
        
        if (!found) {
            System.out.println("✗ NOT FOUND even at 0.65 threshold");
        }
    }
    
    @Test
    public void testOptimalScaling() throws Exception {
        System.out.println("=== FINDING OPTIMAL SCALING FACTOR ===\n");
        
        // Set the ImagePath
        String currentDir = new File(".").getAbsolutePath();
        ImagePath.setBundlePath(currentDir);
        
        Screen screen = new Screen();
        String patternPath = "images/prompt/claude-prompt-1.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.out.println("Pattern file not found");
            return;
        }
        
        float[] factors = {1.0f, 0.95f, 0.9f, 0.85f, 0.8f, 0.75f, 0.7f, 0.667f, 0.6f};
        float bestFactor = 1.0f;
        double bestScore = 0.0;
        
        System.out.println("Testing different scaling factors...\n");
        
        for (float factor : factors) {
            Settings.AlwaysResize = factor;
            
            try {
                Pattern pattern = new Pattern(patternPath).similar(0.5); // Low threshold to find anything
                Match match = screen.find(pattern);
                double score = match.getScore();
                
                System.out.println(String.format("Factor %.3f: Score = %.3f", factor, score));
                
                if (score > bestScore) {
                    bestScore = score;
                    bestFactor = factor;
                }
            } catch (FindFailed e) {
                System.out.println(String.format("Factor %.3f: Not found", factor));
            }
        }
        
        System.out.println("\n=== OPTIMAL RESULT ===");
        System.out.println("Best scaling factor: " + bestFactor);
        System.out.println("Best similarity score: " + String.format("%.3f", bestScore));
        
        if (bestFactor == 0.8f) {
            System.out.println("✓ Confirms 125% Windows display scaling");
        } else if (bestFactor == 0.667f) {
            System.out.println("✓ Indicates 150% Windows display scaling");
        } else if (bestFactor == 1.0f && bestScore > 0.9) {
            System.out.println("✓ No scaling needed - patterns match at native resolution");
        }
    }
}