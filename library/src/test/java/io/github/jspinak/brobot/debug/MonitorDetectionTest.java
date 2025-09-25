package io.github.jspinak.brobot.debug;

import java.awt.*;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Env;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import io.github.jspinak.brobot.test.annotations.DisabledInHeadlessEnvironment;

/**
 * Test to check if SikuliX is detecting monitors incorrectly, which could cause coordinate offset
 * issues.
 */
@DisabledInHeadlessEnvironment("Debug test requires real images and display")
public class MonitorDetectionTest extends DebugTestBase {

    @Test
    public void detectMonitorConfiguration() {
        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("MONITOR DETECTION AND CONFIGURATION TEST");
        System.out.println(
                "================================================================================\n");

        // Test 1: SikuliX Environment Info
        System.out.println("--- SikuliX Environment ---");
        System.out.printf("SikuliX Version: %s%n", Env.getSikuliVersion());
        System.out.printf("OS: %s%n", Env.getOS());
        System.out.printf("Is Linux: %s%n", Env.isLinux());
        System.out.printf("Is Headless: %s%n", GraphicsEnvironment.isHeadless());

        // Test 2: SikuliX Screen Detection
        System.out.println("\n--- SikuliX Screen Detection ---");
        int numScreens = Screen.getNumberScreens();
        System.out.printf("Number of screens detected by SikuliX: %d%n", numScreens);

        for (int i = 0; i < numScreens; i++) {
            Screen s = new Screen(i);
            System.out.printf("\nScreen %d:%n", i);
            System.out.printf("  toString: %s%n", s);
            System.out.printf("  Bounds: %s%n", s.getBounds());
            System.out.printf("  x=%d, y=%d, w=%d, h=%d%n", s.x, s.y, s.w, s.h);
            System.out.printf("  ID: %d%n", s.getID());

            // Get the current Region of Interest (ROI)
            Rectangle rect = s.getRect();
            System.out.printf(
                    "  Current ROI: x=%d, y=%d, w=%d, h=%d%n",
                    rect.x, rect.y, rect.width, rect.height);
        }

        // Test 3: Java AWT Screen Detection
        System.out.println("\n--- Java AWT Screen Detection ---");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        System.out.printf("Number of graphics devices: %d%n", devices.length);

        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice device = devices[i];
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            Rectangle bounds = gc.getBounds();

            System.out.printf("\nDevice %d: %s%n", i, device.getIDstring());
            System.out.printf("  Type: %s%n", getDeviceType(device));
            System.out.printf(
                    "  Bounds: x=%d, y=%d, w=%d, h=%d%n",
                    bounds.x, bounds.y, bounds.width, bounds.height);

            // Check for virtual screen offset
            if (bounds.x != 0 || bounds.y != 0) {
                System.out.printf("  WARNING: Screen has offset! x=%d, y=%d%n", bounds.x, bounds.y);
                System.out.println("  This could cause coordinate mismatch!");
            }

            // Display mode info
            DisplayMode mode = device.getDisplayMode();
            System.out.printf(
                    "  Display Mode: %dx%d, %d-bit, %dHz%n",
                    mode.getWidth(), mode.getHeight(), mode.getBitDepth(), mode.getRefreshRate());
        }

        // Test 4: Check for virtual desktop
        System.out.println("\n--- Virtual Desktop Check ---");
        Rectangle virtualBounds = new Rectangle();
        for (GraphicsDevice gd : devices) {
            virtualBounds = virtualBounds.union(gd.getDefaultConfiguration().getBounds());
        }
        System.out.printf(
                "Virtual desktop bounds: x=%d, y=%d, w=%d, h=%d%n",
                virtualBounds.x, virtualBounds.y, virtualBounds.width, virtualBounds.height);

        if (virtualBounds.x < 0 || virtualBounds.y < 0) {
            System.out.println("WARNING: Virtual desktop extends into negative coordinates!");
            System.out.println("This is likely causing the coordinate mismatch!");
        }

        // Test 5: Primary Screen Check
        System.out.println("\n--- Primary Screen Identification ---");
        Screen primary = new Screen(0);
        System.out.printf("Primary screen (Screen 0): %s%n", primary);

        // Test if (0,0) is on the primary screen
        Region origin = new Region(0, 0, 1, 1);
        int screenId = origin.getScreen().getID();
        System.out.printf("Region at (0,0) is on screen: %d%n", screenId);

        // Test 6: Coordinate Translation Test
        System.out.println("\n--- Coordinate Translation Test ---");

        // Create regions at same coordinates but different ways
        Region r1 = new Region(100, 100, 100, 100);
        Region r2 = primary.newRegion(100, 100, 100, 100);
        Region r3 = new Region(100 + primary.x, 100 + primary.y, 100, 100);

        System.out.printf("Direct Region: %s on Screen %d%n", r1, r1.getScreen().getID());
        System.out.printf("Screen.newRegion: %s on Screen %d%n", r2, r2.getScreen().getID());
        System.out.printf("Global coord Region: %s on Screen %d%n", r3, r3.getScreen().getID());

        // Check if they're equal
        boolean allEqual = r1.equals(r2) && r2.equals(r3);
        System.out.printf("All regions equal: %s%n", allEqual);

        if (!allEqual) {
            System.out.println(
                    "WARNING: Different construction methods produce different regions!");
            System.out.println("This indicates a coordinate system issue!");
        }

        // Test 7: Highlight on each detected screen
        System.out.println("\n--- Multi-Screen Highlight Test ---");
        if (numScreens > 1) {
            System.out.println("Multiple screens detected. Testing highlights on each...");

            for (int i = 0; i < numScreens; i++) {
                Screen s = new Screen(i);
                Region testRegion = s.newRegion(50, 50, 100, 100);
                System.out.printf("Highlighting on Screen %d: %s%n", i, testRegion);
                testRegion.highlight(2, "red");

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                }
            }
        } else {
            System.out.println("Only one screen detected.");
        }

        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("MONITOR DETECTION COMPLETE");
        System.out.println("Check for WARNING messages above - they indicate potential issues.");
        System.out.println(
                "================================================================================\n");
    }

    private String getDeviceType(GraphicsDevice device) {
        int type = device.getType();
        switch (type) {
            case GraphicsDevice.TYPE_RASTER_SCREEN:
                return "Raster Screen";
            case GraphicsDevice.TYPE_PRINTER:
                return "Printer";
            case GraphicsDevice.TYPE_IMAGE_BUFFER:
                return "Image Buffer";
            default:
                return "Unknown (" + type + ")";
        }
    }
}
