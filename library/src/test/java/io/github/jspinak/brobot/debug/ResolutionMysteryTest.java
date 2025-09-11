package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;

import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test to understand why Brobot sometimes captures at 1536x864 and sometimes at 1920x1080. This is
 * the KEY to understanding the pattern matching discrepancy!
 */
@DisabledInCI
public class ResolutionMysteryTest extends DebugTestBase {

    @Test
    public void investigateResolutionChanges() throws Exception {
        System.out.println("=== RESOLUTION MYSTERY INVESTIGATION ===\n");

        // 1. Check environment
        System.out.println("1. ENVIRONMENT:");
        System.out.println("   Java version: " + System.getProperty("java.version"));
        System.out.println("   OS: " + System.getProperty("os.name"));
        System.out.println("   OS version: " + System.getProperty("os.version"));
        System.out.println("   User dir: " + System.getProperty("user.dir"));

        // Check if running in WSL
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        if (wslDistro != null) {
            System.out.println("   Running in WSL: " + wslDistro);
        } else {
            System.out.println("   NOT running in WSL");
        }

        // 2. Check display configuration
        System.out.println("\n2. DISPLAY CONFIGURATION:");
        try {
            GraphicsDevice device =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

            DisplayMode mode = device.getDisplayMode();
            System.out.println("   Display Mode: " + mode.getWidth() + "x" + mode.getHeight());
            System.out.println("   Refresh Rate: " + mode.getRefreshRate() + " Hz");
            System.out.println("   Bit Depth: " + mode.getBitDepth());

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            System.out.println(
                    "   Toolkit Screen Size: "
                            + screenSize.getWidth()
                            + "x"
                            + screenSize.getHeight());

            // Check for DPI scaling
            if (mode.getWidth() != screenSize.getWidth()) {
                System.out.println("   ⚠ DPI SCALING DETECTED!");
                System.out.println("     Physical: " + mode.getWidth() + "x" + mode.getHeight());
                System.out.println(
                        "     Logical: " + screenSize.getWidth() + "x" + screenSize.getHeight());
                float scale = (float) screenSize.getWidth() / mode.getWidth();
                System.out.println("     Scale factor: " + scale);
            }

        } catch (Exception e) {
            System.out.println("   Error getting display config: " + e.getMessage());
        }

        // 3. Test different capture methods
        System.out.println("\n3. CAPTURE METHOD TESTS:");

        // Method A: Basic Screen capture
        System.out.println("\n   A. Basic Screen.capture():");
        Screen screen = new Screen();
        ScreenImage capture1 = screen.capture();
        reportCapture(capture1.getImage(), "basic-screen-capture.png");

        // Method B: Primary Screen capture
        System.out.println("\n   B. Screen.getPrimaryScreen().capture():");
        Screen primary = Screen.getPrimaryScreen();
        ScreenImage capture2 = primary.capture();
        reportCapture(capture2.getImage(), "primary-screen-capture.png");

        // Method C: Robot capture
        System.out.println("\n   C. Robot.createScreenCapture():");
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture3 = robot.createScreenCapture(screenRect);
            reportCapture(capture3, "robot-capture.png");
        } catch (AWTException e) {
            System.out.println("     Failed: " + e.getMessage());
        }

        // Method D: GraphicsDevice Robot
        System.out.println("\n   D. GraphicsDevice Robot:");
        try {
            GraphicsDevice gd =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Robot gdRobot = new Robot(gd);
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            BufferedImage capture4 = gdRobot.createScreenCapture(bounds);
            reportCapture(capture4, "gd-robot-capture.png");
        } catch (AWTException e) {
            System.out.println("     Failed: " + e.getMessage());
        }

        // 4. Test with Settings changes
        System.out.println("\n4. TESTING WITH SETTINGS:");

        // Save original settings
        float originalResize = org.sikuli.basics.Settings.AlwaysResize;

        // Test without resize
        org.sikuli.basics.Settings.AlwaysResize = 0;
        System.out.println("\n   With AlwaysResize = 0:");
        ScreenImage capture5 = screen.capture();
        reportCapture(capture5.getImage(), "no-resize-capture.png");

        // Test with resize
        org.sikuli.basics.Settings.AlwaysResize = 0.8f;
        System.out.println("\n   With AlwaysResize = 0.8:");
        ScreenImage capture6 = screen.capture();
        reportCapture(capture6.getImage(), "resize-0.8-capture.png");

        // Restore settings
        org.sikuli.basics.Settings.AlwaysResize = originalResize;

        // 5. Multiple captures to check consistency
        System.out.println("\n5. CONSISTENCY CHECK (5 captures):");
        for (int i = 0; i < 5; i++) {
            ScreenImage capture = screen.capture();
            BufferedImage img = capture.getImage();
            System.out.println(
                    "   Capture " + (i + 1) + ": " + img.getWidth() + "x" + img.getHeight());
            Thread.sleep(500);
        }

        System.out.println("\n=== ANALYSIS ===");
        System.out.println("If captures show 1536x864:");
        System.out.println("  - Java is DPI-aware and capturing at logical resolution");
        System.out.println("  - This happens with Java 9+ on Windows with DPI scaling");
        System.out.println("\nIf captures show 1920x1080:");
        System.out.println("  - Capturing at physical resolution");
        System.out.println("  - Either DPI awareness is disabled or not applicable");
        System.out.println("\nThe KEY question: What changed between your earlier tests and now?");
    }

    private void reportCapture(BufferedImage image, String filename) {
        System.out.println("     Resolution: " + image.getWidth() + "x" + image.getHeight());
        System.out.println("     Type: " + getImageType(image.getType()));

        // Check if it's a black image (WSL issue)
        boolean isBlack = true;
        for (int x = 0; x < Math.min(100, image.getWidth()) && isBlack; x++) {
            for (int y = 0; y < Math.min(100, image.getHeight()) && isBlack; y++) {
                if (image.getRGB(x, y) != 0xFF000000 && image.getRGB(x, y) != 0) {
                    isBlack = false;
                }
            }
        }

        if (isBlack) {
            System.out.println("     ⚠ WARNING: Image appears to be all black!");
        }

        try {
            File file = new File(filename);
            ImageIO.write(image, "png", file);
            System.out.println("     Saved: " + filename + " (" + (file.length() / 1024) + " KB)");
        } catch (Exception e) {
            System.out.println("     Failed to save: " + e.getMessage());
        }
    }

    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR:
                return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "TYPE_4BYTE_ABGR";
            default:
                return "Type " + type;
        }
    }
}
