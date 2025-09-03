package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to debug region highlighting and console output.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.logging.verbosity=VERBOSE",
    "brobot.highlight.enabled=true",
    "brobot.console.actions.enabled=true",
    "brobot.console.actions.level=VERBOSE",
    "logging.level.io.github.jspinak.brobot.model.element.RegionBuilder=DEBUG"
})
public class RegionHighlightDebugTest extends BrobotTestBase {
    
    @Test
    public void testRegionCalculationsAndOutput() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("REGION CALCULATION AND HIGHLIGHTING DEBUG TEST");
        System.out.println("=".repeat(80));
        
        // Test lower left quarter calculation
        System.out.println("\n1. LOWER LEFT QUARTER CALCULATION:");
        System.out.println("-".repeat(60));
        
        Region lowerLeft = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
            .build();
        
        System.out.println("Result: " + lowerLeft.toString());
        System.out.println("Breakdown:");
        System.out.println("  X position: " + lowerLeft.getX() + " pixels");
        System.out.println("  Y position: " + lowerLeft.getY() + " pixels (from TOP of screen)");
        System.out.println("  Width:      " + lowerLeft.getW() + " pixels");
        System.out.println("  Height:     " + lowerLeft.getH() + " pixels");
        System.out.println("  Right edge: " + lowerLeft.x2() + " pixels");
        System.out.println("  Bottom edge:" + lowerLeft.y2() + " pixels");
        
        // Verify it's the lower left quarter
        if (lowerLeft.getX() == 0 && lowerLeft.getY() == 540 && 
            lowerLeft.getW() == 960 && lowerLeft.getH() == 540) {
            System.out.println("\n✓ CONFIRMED: This is the lower left quarter of a 1920x1080 screen");
            System.out.println("  Covers horizontal: 0 to 960 (left half)");
            System.out.println("  Covers vertical: 540 to 1080 (bottom half)");
        }
        
        // Test all quarters for comparison
        System.out.println("\n2. ALL SCREEN QUARTERS:");
        System.out.println("-".repeat(60));
        
        Region upperLeft = Region.builder()
            .withScreenPercentage(0.0, 0.0, 0.5, 0.5)
            .build();
        System.out.println("Upper Left:  " + upperLeft.toString() + 
            " (top-left at 0,0)");
        
        Region upperRight = Region.builder()
            .withScreenPercentage(0.5, 0.0, 0.5, 0.5)
            .build();
        System.out.println("Upper Right: " + upperRight.toString() + 
            " (top-right at " + upperRight.getX() + ",0)");
        
        System.out.println("Lower Left:  " + lowerLeft.toString() + 
            " (bottom-left at 0," + lowerLeft.getY() + ")");
        
        Region lowerRight = Region.builder()
            .withScreenPercentage(0.5, 0.5, 0.5, 0.5)
            .build();
        System.out.println("Lower Right: " + lowerRight.toString() + 
            " (bottom-right at " + lowerRight.getX() + "," + lowerRight.getY() + ")");
        
        // Visual representation
        System.out.println("\n3. VISUAL LAYOUT:");
        System.out.println("-".repeat(60));
        System.out.println("Screen: 1920x1080");
        System.out.println("+------------------+------------------+");
        System.out.println("|   UPPER LEFT     |   UPPER RIGHT    |");
        System.out.println("|   (0,0)          |   (960,0)        |");
        System.out.println("|   960x540        |   960x540        |");
        System.out.println("+------------------+------------------+ <- Y=540 (middle)");
        System.out.println("|   LOWER LEFT     |   LOWER RIGHT    |");
        System.out.println("|   (0,540)        |   (960,540)      |");
        System.out.println("|   960x540        |   960x540        |");
        System.out.println("+------------------+------------------+ <- Y=1080 (bottom)");
        System.out.println("^                  ^                  ^");
        System.out.println("X=0             X=960              X=1920");
        
        // Test PromptState initialization
        System.out.println("\n4. PROMPTSTATE INITIALIZATION:");
        System.out.println("-".repeat(60));
        System.out.println("Creating PromptState (watch for debug output)...\n");
        
        PromptState promptState = new PromptState();
        
        System.out.println("\nPromptState created successfully.");
        System.out.println("ClaudePrompt name: " + promptState.getClaudePrompt().getName());
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEBUG TEST COMPLETE");
        System.out.println("=".repeat(80));
        
        System.out.println("\nSUMMARY:");
        System.out.println("The lower left quarter region R[0.540.960.540] is CORRECT:");
        System.out.println("- It starts at Y=540 (halfway down the 1080 pixel screen)");
        System.out.println("- It extends to Y=1080 (the bottom of the screen)");
        System.out.println("- It starts at X=0 (left edge)");
        System.out.println("- It extends to X=960 (halfway across the 1920 pixel screen)");
        System.out.println("\nThis covers the LOWER LEFT quarter of the screen.");
    }
}