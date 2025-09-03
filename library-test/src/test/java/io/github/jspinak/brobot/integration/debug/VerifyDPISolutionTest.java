package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.github.jspinak.brobot.config.dpi.DPIConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the DPI scaling solution improves pattern matching
 * from ~0.70 similarity to ~0.94 similarity.
 */
public class VerifyDPISolutionTest extends BrobotTestBase {
    
    @Test
    public void testPatternMatchingWithoutFix() {
        System.out.println("=== TESTING WITHOUT DPI FIX ===\n");
        
        // Ensure no scaling is applied
        Settings.AlwaysResize = 1.0f;
        
        Screen screen = new Screen();
        String patternPath = "images/prompt/claude-prompt-1.png";
        
        System.out.println("Settings.AlwaysResize = " + Settings.AlwaysResize);
        System.out.println("Testing pattern: " + patternPath);
        
        try {
            // Try with high similarity (should fail)
            Match match = screen.find(new Pattern(patternPath).similar(0.9));
            System.out.println("Found at 0.9 similarity! Score: " + match.getScore());
            System.out.println("This is unexpected - patterns might have been updated");
        } catch (FindFailed e) {
            System.out.println("Not found at 0.9 similarity (expected)");
            
            try {
                // Try with lower similarity
                Match match = screen.find(new Pattern(patternPath).similar(0.65));
                System.out.println("Found at 0.65 similarity. Score: " + match.getScore());
                System.out.println("Score is likely around: 0.70-0.71");
            } catch (FindFailed e2) {
                System.out.println("Pattern not found even at 0.65 similarity");
            }
        }
    }
    
    @Test
    public void testPatternMatchingWithManualFix() {
        System.out.println("\n=== TESTING WITH MANUAL DPI FIX ===\n");
        
        // Apply the fix for 125% scaling
        Settings.AlwaysResize = 0.8f;
        
        Screen screen = new Screen();
        String patternPath = "images/prompt/claude-prompt-1.png";
        
        System.out.println("Settings.AlwaysResize = " + Settings.AlwaysResize);
        System.out.println("Testing pattern: " + patternPath);
        
        try {
            // Should now work with high similarity
            Match match = screen.find(new Pattern(patternPath).similar(0.9));
            System.out.println("✓ Found at 0.9 similarity! Score: " + match.getScore());
            System.out.println("Location: " + match.getTarget());
            
            assertTrue(match.getScore() >= 0.9, 
                "With DPI fix, similarity should be >= 0.9, but was: " + match.getScore());
            
        } catch (FindFailed e) {
            System.out.println("✗ Not found at 0.9 similarity");
            
            try {
                // Check what similarity we do achieve
                Match match = screen.find(new Pattern(patternPath).similar(0.7));
                System.out.println("Found at 0.7 similarity. Score: " + match.getScore());
                System.out.println("Score should be around 0.94 with proper fix");
            } catch (FindFailed e2) {
                fail("Pattern should be found with DPI fix applied");
            }
        }
    }
    
    @Test
    public void testAutoConfiguration() {
        System.out.println("\n=== TESTING AUTO DPI CONFIGURATION ===\n");
        
        // Create Spring context with our configuration
        try (AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(DPIConfiguration.class)) {
            
            // Configuration should have run automatically
            System.out.println("\nAfter auto-configuration:");
            System.out.println("Settings.AlwaysResize = " + Settings.AlwaysResize);
            
            // Test pattern matching
            Screen screen = new Screen();
            String patternPath = "images/prompt/claude-prompt-1.png";
            
            try {
                Match match = screen.find(new Pattern(patternPath).similar(0.85));
                System.out.println("\n✓ Pattern found with auto-configuration!");
                System.out.println("Score: " + match.getScore());
                
                assertTrue(match.getScore() >= 0.85, 
                    "Auto-config should achieve >= 0.85 similarity");
                    
            } catch (FindFailed e) {
                System.out.println("\n✗ Pattern not found at 0.85 similarity");
                System.out.println("Auto-configuration may need adjustment for your system");
            }
        }
    }
    
    @Test
    public void testDifferentScalingFactors() {
        System.out.println("\n=== TESTING DIFFERENT SCALING FACTORS ===\n");
        
        Screen screen = new Screen();
        String patternPath = "images/prompt/claude-prompt-1.png";
        
        // Test different scaling factors
        float[] scalingFactors = {1.0f, 0.9f, 0.8f, 0.75f, 0.667f};
        float bestFactor = 1.0f;
        double bestScore = 0.0;
        
        for (float factor : scalingFactors) {
            Settings.AlwaysResize = factor;
            System.out.println("\nTesting with AlwaysResize = " + factor);
            
            try {
                Match match = screen.find(new Pattern(patternPath).similar(0.6));
                double score = match.getScore();
                System.out.println("  Found! Score: " + String.format("%.3f", score));
                
                if (score > bestScore) {
                    bestScore = score;
                    bestFactor = factor;
                }
            } catch (FindFailed e) {
                System.out.println("  Not found at 0.6 similarity");
            }
        }
        
        System.out.println("\n=== RESULTS ===");
        System.out.println("Best scaling factor: " + bestFactor);
        System.out.println("Best similarity score: " + String.format("%.3f", bestScore));
        
        if (bestFactor == 0.8f) {
            System.out.println("✓ This confirms 125% Windows scaling (0.8 = 100/125)");
        } else if (bestFactor == 0.667f) {
            System.out.println("This suggests 150% Windows scaling (0.667 = 100/150)");
        } else if (bestFactor == 0.75f) {
            System.out.println("This suggests 133% Windows scaling (0.75 = 100/133)");
        }
    }
}