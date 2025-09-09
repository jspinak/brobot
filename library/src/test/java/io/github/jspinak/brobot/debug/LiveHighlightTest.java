package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.sikuli.script.Screen;
import org.sikuli.script.Region;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Live test to highlight specific regions and see where they actually appear.
 * Run this test and observe where the highlights show up on your screen.
 */
@DisabledInCI
public class LiveHighlightTest extends BrobotTestBase {
    
    @Test
    public void performLiveHighlights() throws InterruptedException {
        System.out.println("\n================================================================================");
        System.out.println("LIVE HIGHLIGHT TEST - WATCH YOUR SCREEN!");
        System.out.println("================================================================================\n");
        
        try {
            Screen screen = new Screen();
            System.out.printf("Screen: %dx%d at (%d,%d)%n", 
                screen.w, screen.h, screen.x, screen.y);
            
            // Test 1: Highlight each corner
            System.out.println("\n--- Test 1: Highlighting Screen Corners (200x200 each) ---");
            
            // Top-left corner
            Region topLeft = new Region(0, 0, 200, 200);
            System.out.printf("Highlighting TOP-LEFT: %s%n", topLeft);
            topLeft.highlight(2, "red");
            Thread.sleep(2500);
            
            // Top-right corner
            Region topRight = new Region(screen.w - 200, 0, 200, 200);
            System.out.printf("Highlighting TOP-RIGHT: %s%n", topRight);
            topRight.highlight(2, "green");
            Thread.sleep(2500);
            
            // Bottom-left corner (THIS IS WHERE YOUR REGION SHOULD BE)
            Region bottomLeft = new Region(0, screen.h - 200, 200, 200);
            System.out.printf("Highlighting BOTTOM-LEFT: %s%n", bottomLeft);
            bottomLeft.highlight(2, "blue");
            Thread.sleep(2500);
            
            // Bottom-right corner
            Region bottomRight = new Region(screen.w - 200, screen.h - 200, 200, 200);
            System.out.printf("Highlighting BOTTOM-RIGHT: %s%n", bottomRight);
            bottomRight.highlight(2, "yellow");
            Thread.sleep(2500);
            
            // Test 2: Highlight the exact region you're trying to use (lower left quarter)
            System.out.println("\n--- Test 2: Highlighting Lower Left Quarter ---");
            Region lowerLeftQuarter = new Region(0, 540, 960, 540);
            System.out.printf("Highlighting LOWER LEFT QUARTER: %s%n", lowerLeftQuarter);
            System.out.println("This should cover the bottom-left quarter of your screen.");
            System.out.println("Coordinates: x=0, y=540, width=960, height=540");
            lowerLeftQuarter.highlight(3, "magenta");
            Thread.sleep(3500);
            
            // Test 3: Highlight with offset to see if there's a pattern
            System.out.println("\n--- Test 3: Testing Different Y Coordinates ---");
            
            // Try Y=0 (top)
            Region topStrip = new Region(0, 0, 960, 100);
            System.out.printf("Highlighting at Y=0 (TOP): %s%n", topStrip);
            topStrip.highlight(2, "cyan");
            Thread.sleep(2500);
            
            // Try Y=490 (just above middle)
            Region aboveMiddle = new Region(0, 490, 960, 100);
            System.out.printf("Highlighting at Y=490 (ABOVE MIDDLE): %s%n", aboveMiddle);
            aboveMiddle.highlight(2, "orange");
            Thread.sleep(2500);
            
            // Try Y=540 (your target)
            Region atTarget = new Region(0, 540, 960, 100);
            System.out.printf("Highlighting at Y=540 (YOUR TARGET): %s%n", atTarget);
            atTarget.highlight(2, "white");
            Thread.sleep(2500);
            
            // Try Y=980 (near bottom)
            Region nearBottom = new Region(0, 980, 960, 100);
            System.out.printf("Highlighting at Y=980 (NEAR BOTTOM): %s%n", nearBottom);
            nearBottom.highlight(2, "gray");
            Thread.sleep(2500);
            
            // Test 4: Multiple simultaneous highlights
            System.out.println("\n--- Test 4: Multiple Simultaneous Highlights ---");
            System.out.println("Highlighting 3 regions at once to see if they interfere:");
            
            Region r1 = new Region(100, 100, 200, 200);
            Region r2 = new Region(400, 400, 200, 200);
            Region r3 = new Region(700, 700, 200, 200);
            
            System.out.printf("Region 1: %s%n", r1);
            System.out.printf("Region 2: %s%n", r2);
            System.out.printf("Region 3: %s%n", r3);
            
            r1.highlight(3, "red");
            r2.highlight(3, "green");
            r3.highlight(3, "blue");
            
            Thread.sleep(3500);
            
            System.out.println("\n================================================================================");
            System.out.println("TEST COMPLETE");
            System.out.println("Did the highlights appear where expected?");
            System.out.println("- Corners should be at screen edges");
            System.out.println("- Lower left quarter should be at bottom-left");
            System.out.println("- Y=540 should be at middle of screen");
            System.out.println("================================================================================\n");
            
        } catch (Exception e) {
            System.err.println("Error during live highlight test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}