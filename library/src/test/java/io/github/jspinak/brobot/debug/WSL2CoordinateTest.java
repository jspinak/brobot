package io.github.jspinak.brobot.debug;

import java.awt.*;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Screen;

import io.github.jspinak.brobot.test.DisabledInCI;

/** Test to identify if WSL2/X11 is causing coordinate transformation issues. */
@DisabledInCI
public class WSL2CoordinateTest extends DebugTestBase {

    @Test
    public void testCoordinateSystemDiscrepancy() {
        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("WSL2/X11 COORDINATE SYSTEM TEST");
        System.out.println(
                "================================================================================\n");

        try {
            // Test 1: Check GraphicsEnvironment
            System.out.println("--- Test 1: Graphics Environment ---");
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            System.out.printf("Number of graphics devices: %d%n", screens.length);

            for (int i = 0; i < screens.length; i++) {
                GraphicsDevice gd = screens[i];
                GraphicsConfiguration gc = gd.getDefaultConfiguration();
                Rectangle bounds = gc.getBounds();
                System.out.printf("Graphics Device %d: %s%n", i, gd.getIDstring());
                System.out.printf(
                        "  Bounds: x=%d, y=%d, width=%d, height=%d%n",
                        bounds.x, bounds.y, bounds.width, bounds.height);

                // Check transforms
                System.out.printf("  Default transform: %s%n", gc.getDefaultTransform());
                System.out.printf("  Normalization transform: %s%n", gc.getNormalizingTransform());
            }

            // Test 2: Check Toolkit screen size
            System.out.println("\n--- Test 2: AWT Toolkit Screen Size ---");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            System.out.printf("Toolkit screen size: %dx%d%n", screenSize.width, screenSize.height);

            // Test 3: SikuliX Screen initialization
            System.out.println("\n--- Test 3: SikuliX Screen Initialization ---");
            Screen sikuliScreen = new Screen();
            System.out.printf(
                    "SikuliX primary screen: %dx%d at (%d,%d)%n",
                    sikuliScreen.w, sikuliScreen.h, sikuliScreen.x, sikuliScreen.y);

            // Test 4: Create test region and verify transformations
            System.out.println("\n--- Test 4: Region Transformation Test ---");

            // Lower left quarter in different ways
            int screenWidth = sikuliScreen.w;
            int screenHeight = sikuliScreen.h;

            // Method 1: Direct coordinates
            org.sikuli.script.Region directRegion =
                    new org.sikuli.script.Region(
                            0, screenHeight / 2, screenWidth / 2, screenHeight / 2);
            System.out.printf("Direct construction: %s%n", directRegion);

            // Method 2: Using Screen.newRegion
            org.sikuli.script.Region screenRegion =
                    sikuliScreen.newRegion(0, screenHeight / 2, screenWidth / 2, screenHeight / 2);
            System.out.printf("Screen.newRegion: %s%n", screenRegion);

            // Method 3: Using setROI (Region of Interest)
            sikuliScreen.setRect(0, screenHeight / 2, screenWidth / 2, screenHeight / 2);
            Rectangle roiRect = sikuliScreen.getRect();
            System.out.printf(
                    "Screen ROI: x=%d, y=%d, w=%d, h=%d%n",
                    roiRect.x, roiRect.y, roiRect.width, roiRect.height);
            sikuliScreen.setRect(sikuliScreen.getBounds()); // Reset to full screen

            // Test 5: Virtual coordinate check
            System.out.println("\n--- Test 5: Virtual vs Physical Coordinates ---");

            // Try to detect if coordinates are being scaled
            org.sikuli.script.Region testRegion = new org.sikuli.script.Region(100, 100, 100, 100);
            Rectangle rect = testRegion.getRect();

            boolean coordinatesMatch =
                    (rect.x == 100 && rect.y == 100 && rect.width == 100 && rect.height == 100);
            System.out.printf("Coordinates preserved: %s%n", coordinatesMatch);
            if (!coordinatesMatch) {
                System.out.printf(
                        "Expected: (100,100,100,100), Got: (%d,%d,%d,%d)%n",
                        rect.x, rect.y, rect.width, rect.height);
            }

            // Test 6: Check for display scaling
            System.out.println("\n--- Test 6: Display Scaling Detection ---");
            GraphicsConfiguration gc =
                    GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice()
                            .getDefaultConfiguration();

            double scaleX = gc.getDefaultTransform().getScaleX();
            double scaleY = gc.getDefaultTransform().getScaleY();
            System.out.printf("Display scaling: X=%.2f, Y=%.2f%n", scaleX, scaleY);

            if (scaleX != 1.0 || scaleY != 1.0) {
                System.out.println(
                        "WARNING: Display scaling detected! This could cause coordinate"
                                + " mismatches.");
            }

            // Test 7: Environment variables
            System.out.println("\n--- Test 7: Environment Variables ---");
            System.out.printf("DISPLAY: %s%n", System.getenv("DISPLAY"));
            System.out.printf("WAYLAND_DISPLAY: %s%n", System.getenv("WAYLAND_DISPLAY"));
            System.out.printf("GDK_SCALE: %s%n", System.getenv("GDK_SCALE"));
            System.out.printf("QT_SCALE_FACTOR: %s%n", System.getenv("QT_SCALE_FACTOR"));

            // Test 8: Actual highlight test with coordinate logging
            System.out.println("\n--- Test 8: Highlight Coordinate Logging ---");
            if (!GraphicsEnvironment.isHeadless()) {
                org.sikuli.script.Region lowerLeft = new org.sikuli.script.Region(0, 540, 960, 540);
                System.out.printf("About to highlight region: %s%n", lowerLeft);
                System.out.printf("  getX()=%d, getY()=%d%n", lowerLeft.getX(), lowerLeft.getY());
                System.out.printf("  getCenter(): %s%n", lowerLeft.getCenter());
                System.out.printf("  getScreen(): %s%n", lowerLeft.getScreen());
                System.out.printf("  getRect(): %s%n", lowerLeft.getRect());

                // Log what SikuliX thinks it's highlighting
                System.out.println("Calling highlight(1) on region...");
                lowerLeft.highlight(1);
                System.out.println("Highlight call completed.");
            }

        } catch (Exception e) {
            System.err.println("Error during coordinate test: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("TEST COMPLETE - Check for scaling or transformation issues above");
        System.out.println(
                "================================================================================\n");
    }
}
