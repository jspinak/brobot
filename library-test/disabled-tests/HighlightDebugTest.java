package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Manual test for highlight debugging.
 * This test is disabled by default since it requires a display.
 * Run manually to debug highlighting issues.
 */
public class HighlightDebugTest extends BrobotTestBase {
    
    @Test
    @Disabled("Manual test - requires display")
    public void testHighlightDebug() {
        HighlightDebugger debugger = new HighlightDebugger();
        
        System.out.println("Starting highlight debug test...");
        System.out.println("This will show different highlighting methods.");
        System.out.println("Watch your screen for visual highlights.");
        
        // First, draw the grid to show quadrants
        System.out.println("\n1. Drawing grid overlay (5 seconds)...");
        debugger.drawDebugGrid();
        
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then compare highlighting methods
        System.out.println("\n2. Comparing highlight methods...");
        debugger.compareHighlightMethods();
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\nTest complete!");
        System.out.println("If the highlights didn't match the expected lower-left quarter,");
        System.out.println("there may be an issue with SikuliX coordinate transformation.");
    }
}