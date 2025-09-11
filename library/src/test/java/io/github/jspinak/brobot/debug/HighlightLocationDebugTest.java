package io.github.jspinak.brobot.debug;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Screen;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test to diagnose the fundamental highlighting location issue. The regions have correct size but
 * appear in wrong locations.
 */
@DisabledInCI
public class HighlightLocationDebugTest extends BrobotTestBase {

    @Test
    public void debugHighlightLocation() {
        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("HIGHLIGHT LOCATION DEBUG TEST");
        System.out.println(
                "================================================================================\n");

        // Get actual screen dimensions
        try {
            Screen screen = new Screen();
            System.out.printf("SikuliX Screen dimensions: %dx%d%n", screen.w, screen.h);
            System.out.printf("Screen bounds: %s%n", screen.getBounds());
            System.out.printf("Screen ID: %d%n", screen.getID());

            // Test 1: Create Brobot Region for lower left quarter
            System.out.println("\n--- Test 1: Brobot Region (Lower Left Quarter) ---");
            Region brobotRegion =
                    Region.builder()
                            .withScreenPercentage(0.0, 0.5, 0.5, 0.5) // x=0%, y=50%, w=50%, h=50%
                            .build();
            System.out.printf("Brobot Region: %s%n", brobotRegion);
            System.out.printf(
                    "  X=%d, Y=%d, W=%d, H=%d%n",
                    brobotRegion.getX(),
                    brobotRegion.getY(),
                    brobotRegion.getW(),
                    brobotRegion.getH());

            // Test 2: Create equivalent SikuliX Region directly
            System.out.println("\n--- Test 2: Direct SikuliX Region (Same Coordinates) ---");
            org.sikuli.script.Region sikuliRegion =
                    new org.sikuli.script.Region(
                            brobotRegion.getX(),
                            brobotRegion.getY(),
                            brobotRegion.getW(),
                            brobotRegion.getH());
            System.out.printf("SikuliX Region: %s%n", sikuliRegion);
            System.out.printf(
                    "  X=%d, Y=%d, W=%d, H=%d%n",
                    sikuliRegion.x, sikuliRegion.y, sikuliRegion.w, sikuliRegion.h);

            // Test 3: Check if coordinates match
            System.out.println("\n--- Test 3: Coordinate Comparison ---");
            boolean xMatch = brobotRegion.getX() == sikuliRegion.x;
            boolean yMatch = brobotRegion.getY() == sikuliRegion.y;
            boolean wMatch = brobotRegion.getW() == sikuliRegion.w;
            boolean hMatch = brobotRegion.getH() == sikuliRegion.h;

            System.out.printf(
                    "X coordinates match: %s (Brobot=%d, Sikuli=%d)%n",
                    xMatch, brobotRegion.getX(), sikuliRegion.x);
            System.out.printf(
                    "Y coordinates match: %s (Brobot=%d, Sikuli=%d)%n",
                    yMatch, brobotRegion.getY(), sikuliRegion.y);
            System.out.printf(
                    "Width matches: %s (Brobot=%d, Sikuli=%d)%n",
                    wMatch, brobotRegion.getW(), sikuliRegion.w);
            System.out.printf(
                    "Height matches: %s (Brobot=%d, Sikuli=%d)%n",
                    hMatch, brobotRegion.getH(), sikuliRegion.h);

            // Test 4: Create regions at specific screen locations
            System.out.println("\n--- Test 4: Test Regions at Known Locations ---");
            testSpecificRegion("Top Left (100,100)", 100, 100, 200, 200);
            testSpecificRegion("Center (860,440)", 860, 440, 200, 200);
            testSpecificRegion("Lower Left (0,540)", 0, 540, 960, 540);

            // Test 5: Check for coordinate transformation issues
            System.out.println("\n--- Test 5: Coordinate System Analysis ---");
            System.out.printf(
                    "Java AWT headless mode: %s%n", java.awt.GraphicsEnvironment.isHeadless());
            System.out.printf("Operating System: %s%n", System.getProperty("os.name"));
            System.out.printf("Display environment variable: %s%n", System.getenv("DISPLAY"));

            // Test 6: Multiple monitor check
            System.out.println("\n--- Test 6: Monitor Configuration ---");
            int numScreens = Screen.getNumberScreens();
            System.out.printf("Number of screens detected: %d%n", numScreens);
            for (int i = 0; i < numScreens; i++) {
                Screen s = new Screen(i);
                System.out.printf("Screen %d: %dx%d at (%d,%d)%n", i, s.w, s.h, s.x, s.y);
            }

            // Test 7: Highlight with explicit coordinates
            System.out.println("\n--- Test 7: Attempting Highlight (if not headless) ---");
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                System.out.println("Creating region at (0,540) with size 960x540...");
                org.sikuli.script.Region testRegion =
                        new org.sikuli.script.Region(0, 540, 960, 540);
                System.out.printf("About to highlight: %s%n", testRegion);

                // Log the actual screen-relative bounds
                System.out.printf("Screen-relative bounds: %s%n", testRegion.getRect());
                System.out.printf("Screen containing region: %d%n", testRegion.getScreen().getID());

                // Try highlighting
                testRegion.highlight(2);
                System.out.println("Highlight command sent. Check if it appears at (0,540)");
            } else {
                System.out.println("Headless environment - skipping actual highlight");
            }

        } catch (Exception e) {
            System.err.println("Error during highlight debug: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("DEBUG COMPLETE");
        System.out.println(
                "================================================================================\n");
    }

    private void testSpecificRegion(String label, int x, int y, int w, int h) {
        System.out.printf("\n%s:%n", label);

        // Create Brobot region with explicit coordinates
        Region brobotRegion = new Region(x, y, w, h);
        System.out.printf("  Brobot Region: %s%n", brobotRegion);

        // Create SikuliX region with same coordinates
        org.sikuli.script.Region sikuliRegion = new org.sikuli.script.Region(x, y, w, h);
        System.out.printf("  SikuliX Region: %s%n", sikuliRegion);

        // Check if they match
        boolean match =
                (sikuliRegion.x == x
                        && sikuliRegion.y == y
                        && sikuliRegion.w == w
                        && sikuliRegion.h == h);
        System.out.printf("  Coordinates preserved correctly: %s%n", match);

        if (!match) {
            System.out.printf(
                    "  ERROR: Expected (%d,%d,%d,%d) but got (%d,%d,%d,%d)%n",
                    x, y, w, h, sikuliRegion.x, sikuliRegion.y, sikuliRegion.w, sikuliRegion.h);
        }
    }
}
