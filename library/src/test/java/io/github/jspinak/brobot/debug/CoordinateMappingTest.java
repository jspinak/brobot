package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Systematic test to determine how SikuliX is mapping coordinates. This test will help identify the
 * transformation between requested and actual positions.
 */
@DisabledInCI
public class CoordinateMappingTest extends DebugTestBase {

    @Test
    public void mapCoordinateSystem() throws Exception {
        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("COORDINATE MAPPING TEST");
        System.out.println("This test will highlight regions and ask you where they appear.");
        System.out.println(
                "================================================================================\n");

        Screen screen = new Screen();
        Scanner scanner = new Scanner(System.in);

        System.out.printf(
                "Screen reports: %dx%d at (%d,%d)%n", screen.w, screen.h, screen.x, screen.y);

        // Test 1: Find where (0,0) actually appears
        System.out.println("\n--- Test 1: Finding Origin (0,0) ---");
        Region origin = new Region(0, 0, 100, 100);
        System.out.printf("Highlighting region at (0,0) with size 100x100: %s%n", origin);
        origin.highlight(3, "red");

        System.out.println("\nWhere did the RED highlight appear?");
        System.out.println("1. Top-left corner");
        System.out.println("2. Top-right corner");
        System.out.println("3. Bottom-left corner");
        System.out.println("4. Bottom-right corner");
        System.out.println("5. Center");
        System.out.println("6. Somewhere else");
        System.out.println("7. Didn't appear");
        System.out.print("Enter choice (1-7): ");

        Thread.sleep(3500); // Give time to observe

        // Test 2: Test center of screen
        System.out.println("\n--- Test 2: Center of Screen ---");
        int centerX = screen.w / 2 - 50;
        int centerY = screen.h / 2 - 50;
        Region center = new Region(centerX, centerY, 100, 100);
        System.out.printf("Highlighting region at center (%d,%d): %s%n", centerX, centerY, center);
        center.highlight(3, "green");

        System.out.println("\nWhere did the GREEN highlight appear?");
        System.out.println("(Describe location or press Enter to continue)");

        Thread.sleep(3500);

        // Test 3: Grid test - divide screen into 9 sections
        System.out.println("\n--- Test 3: Grid Test (9 positions) ---");
        System.out.println("Will highlight 9 positions in sequence. Note where each appears.");

        String[] colors = {
            "red", "green", "blue", "yellow", "cyan", "magenta", "white", "orange", "gray"
        };
        int gridSize = 3;
        int boxSize = 80;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int x = col * (screen.w / gridSize) + 50;
                int y = row * (screen.h / gridSize) + 50;

                Region gridRegion = new Region(x, y, boxSize, boxSize);
                String color = colors[row * gridSize + col];

                System.out.printf(
                        "Position %d,%d: Highlighting at (%d,%d) in %s%n",
                        row, col, x, y, color.toUpperCase());
                gridRegion.highlight(2, color);

                Thread.sleep(2200);
            }
        }

        // Test 4: Try mouse click coordinates
        System.out.println("\n--- Test 4: Mouse Location Test ---");
        System.out.println("Click somewhere on the screen and I'll highlight that position.");
        System.out.println("Press Enter when ready...");

        try {
            Location mousePos = new Location(MouseInfo.getPointerInfo().getLocation());
            System.out.printf("Mouse at: %s%n", mousePos);

            Region mouseRegion = new Region(mousePos.x - 50, mousePos.y - 50, 100, 100);
            System.out.printf("Highlighting around mouse position: %s%n", mouseRegion);
            mouseRegion.highlight(3, "purple");

            System.out.println("Did the highlight appear around your mouse position? (y/n)");

        } catch (Exception e) {
            System.out.println("Could not get mouse position: " + e.getMessage());
        }

        // Test 5: Offset detection
        System.out.println("\n--- Test 5: Systematic Offset Detection ---");
        System.out.println("Testing if there's a consistent offset...");

        // Highlight at specific pixel positions
        int[] testX = {0, 100, 200, 300, 400, 500};
        int[] testY = {0, 100, 200, 300, 400, 500};

        for (int i = 0; i < testX.length; i++) {
            Region testRegion = new Region(testX[i], testY[i], 50, 50);
            System.out.printf("Flashing at (%d,%d)...%n", testX[i], testY[i]);
            testRegion.highlight(0.5);
            Thread.sleep(600);
        }

        // Test 6: Full screen boundaries
        System.out.println("\n--- Test 6: Screen Boundary Test ---");

        // Top edge
        Region topEdge = new Region(0, 0, screen.w, 20);
        System.out.println("Highlighting TOP edge (full width, 20px height)");
        topEdge.highlight(2, "red");
        Thread.sleep(2200);

        // Bottom edge
        Region bottomEdge = new Region(0, screen.h - 20, screen.w, 20);
        System.out.println("Highlighting BOTTOM edge");
        bottomEdge.highlight(2, "blue");
        Thread.sleep(2200);

        // Left edge
        Region leftEdge = new Region(0, 0, 20, screen.h);
        System.out.println("Highlighting LEFT edge");
        leftEdge.highlight(2, "green");
        Thread.sleep(2200);

        // Right edge
        Region rightEdge = new Region(screen.w - 20, 0, 20, screen.h);
        System.out.println("Highlighting RIGHT edge");
        rightEdge.highlight(2, "yellow");
        Thread.sleep(2200);

        // Test 7: Check Screen vs Region coordinate systems
        System.out.println("\n--- Test 7: Screen Method Comparison ---");

        // Method 1: Direct Region
        Region direct = new Region(100, 100, 100, 100);
        System.out.printf("Direct Region: %s%n", direct);
        direct.highlight(2, "red");
        Thread.sleep(2200);

        // Method 2: Screen.newRegion
        Region fromScreen = screen.newRegion(100, 100, 100, 100);
        System.out.printf("Screen.newRegion: %s%n", fromScreen);
        fromScreen.highlight(2, "green");
        Thread.sleep(2200);

        // Method 3: Location-based
        Location loc = new Location(100, 100);
        Region fromLocation = new Region(loc.x, loc.y, 100, 100);
        System.out.printf("From Location: %s%n", fromLocation);
        fromLocation.highlight(2, "blue");

        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("ANALYSIS COMPLETE");
        System.out.println("Based on where highlights appeared vs. where they were requested,");
        System.out.println("we can determine the coordinate transformation.");
        System.out.println(
                "================================================================================\n");
    }

    @Test
    public void testVirtualDisplay() {
        System.out.println("\n--- Virtual Display Check ---");

        try {
            // Check all graphics devices
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();

            System.out.printf("Number of graphics devices: %d%n", devices.length);

            for (GraphicsDevice device : devices) {
                System.out.printf("Device: %s%n", device.getIDstring());

                DisplayMode mode = device.getDisplayMode();
                System.out.printf(
                        "  Display Mode: %dx%d @ %dHz, %d-bit%n",
                        mode.getWidth(),
                        mode.getHeight(),
                        mode.getRefreshRate(),
                        mode.getBitDepth());

                GraphicsConfiguration gc = device.getDefaultConfiguration();
                Rectangle bounds = gc.getBounds();
                System.out.printf("  Bounds: %s%n", bounds);

                // Check if this is a virtual display
                System.out.printf("  Full screen supported: %s%n", device.isFullScreenSupported());
                System.out.printf(
                        "  Display change supported: %s%n", device.isDisplayChangeSupported());
            }

            // Check SikuliX's view
            System.out.println("\nSikuliX Screen Detection:");
            int numScreens = Screen.getNumberScreens();
            System.out.printf("Number of screens: %d%n", numScreens);

            for (int i = 0; i < numScreens; i++) {
                Screen s = new Screen(i);
                System.out.printf("Screen %d: %s%n", i, s);
                System.out.printf("  Bounds: %s%n", s.getBounds());
                System.out.printf("  Robot: %s%n", s.getRobot());
            }

        } catch (Exception e) {
            System.err.println("Error checking displays: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
