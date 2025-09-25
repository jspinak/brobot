package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.annotations.DisabledInHeadlessEnvironment;

/**
 * Tests methods to capture screenshots at physical resolution (1920x1080) instead of logical
 * resolution (1536x864) on 125% scaled displays.
 */
@DisabledInHeadlessEnvironment("Debug test requires real images and display")
public class PhysicalResolutionCaptureTest extends DebugTestBase {

    @Test
    public void testPhysicalResolutionCapture() throws Exception {
        System.out.println("=== PHYSICAL RESOLUTION CAPTURE TEST ===\n");

        // First, try setting system properties
        System.out.println("1. SETTING DPI AWARENESS PROPERTIES:");
        System.setProperty("sun.java2d.dpiaware", "false");
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("sun.java2d.win.uiScale", "1.0");

        System.out.println("   sun.java2d.dpiaware: " + System.getProperty("sun.java2d.dpiaware"));
        System.out.println("   sun.java2d.uiScale: " + System.getProperty("sun.java2d.uiScale"));
        System.out.println(
                "   sun.java2d.win.uiScale: " + System.getProperty("sun.java2d.win.uiScale"));

        // Test capture after setting properties
        System.out.println("\n2. CAPTURE WITH DPI AWARENESS DISABLED:");
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("   Toolkit size: " + screenSize.width + "x" + screenSize.height);

        BufferedImage capture = robot.createScreenCapture(new Rectangle(screenSize));
        System.out.println("   Captured: " + capture.getWidth() + "x" + capture.getHeight());
        saveImage(capture, "test-dpi-disabled.png");

        // Method 2: Scale up the captured image
        System.out.println("\n3. SCALE UP CAPTURED IMAGE:");
        if (capture.getWidth() == 1536 && capture.getHeight() == 864) {
            BufferedImage scaledCapture = scaleImage(capture, 1920, 1080);
            System.out.println(
                    "   Scaled to: " + scaledCapture.getWidth() + "x" + scaledCapture.getHeight());
            saveImage(scaledCapture, "test-scaled-up.png");
        }

        // Method 3: Use MultiResolutionImage (Java 9+)
        System.out.println("\n4. MULTI-RESOLUTION IMAGE TEST:");
        testMultiResolutionCapture();

        // Method 4: Test with different Robot configurations
        System.out.println("\n5. ROBOT CONFIGURATION TEST:");
        testRobotConfigurations();

        System.out.println("\n=== TEST COMPLETE ===");
        System.out.println("Check the saved images for quality comparison.");
    }

    private void testMultiResolutionCapture() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();

            for (int i = 0; i < devices.length; i++) {
                GraphicsDevice device = devices[i];
                GraphicsConfiguration[] configs = device.getConfigurations();

                System.out.println("   Device " + i + ":");
                for (GraphicsConfiguration config : configs) {
                    Rectangle bounds = config.getBounds();
                    System.out.println("     Config bounds: " + bounds.width + "x" + bounds.height);

                    // Try to capture with this specific configuration
                    try {
                        Robot configRobot = new Robot(device);
                        BufferedImage capture = configRobot.createScreenCapture(bounds);
                        System.out.println(
                                "     Captured: " + capture.getWidth() + "x" + capture.getHeight());

                        if (capture.getWidth() > 1536 || capture.getHeight() > 864) {
                            System.out.println("     → Found higher resolution capture!");
                            saveImage(capture, "test-high-res-config.png");
                        }
                    } catch (Exception e) {
                        System.out.println("     Error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }
    }

    private void testRobotConfigurations() {
        try {
            // Test 1: Create robot with explicit screen bounds
            System.out.println("   Test 1: Robot with physical bounds");
            Robot robot = new Robot();
            Rectangle physicalBounds = new Rectangle(0, 0, 1920, 1080);
            BufferedImage capture = robot.createScreenCapture(physicalBounds);
            System.out.println("     Captured: " + capture.getWidth() + "x" + capture.getHeight());
            saveImage(capture, "test-physical-bounds.png");

            // Test 2: Capture beyond logical bounds
            System.out.println("   Test 2: Capture beyond logical bounds");
            Rectangle beyondBounds = new Rectangle(-192, -108, 2304, 1296); // 1.5x logical size
            try {
                BufferedImage beyondCapture = robot.createScreenCapture(beyondBounds);
                System.out.println(
                        "     Captured: "
                                + beyondCapture.getWidth()
                                + "x"
                                + beyondCapture.getHeight());
                saveImage(beyondCapture, "test-beyond-bounds.png");
            } catch (Exception e) {
                System.out.println("     Error: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }
    }

    private BufferedImage scaleImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, original.getType());
        Graphics2D g2d = scaled.createGraphics();

        // Use high-quality rendering hints for better scaling
        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaled;
    }

    private void saveImage(BufferedImage image, String filename) {
        try {
            File file = new File(filename);
            ImageIO.write(image, "png", file);
            System.out.println(
                    "     Saved: "
                            + file.getName()
                            + " ("
                            + image.getWidth()
                            + "x"
                            + image.getHeight()
                            + ")");
        } catch (IOException e) {
            System.out.println("     Failed to save: " + e.getMessage());
        }
    }

    @Test
    public void testJavaVersionComparison() {
        System.out.println("=== JAVA VERSION COMPARISON ===\n");

        System.out.println("Current Java version: " + System.getProperty("java.version"));
        System.out.println("\nSikuliX IDE uses Java 8 which is less DPI-aware.");
        System.out.println("This might be why IDE captures sharp images.\n");

        System.out.println("Options to fix:");
        System.out.println("1. Run Brobot with Java 8 (not recommended)");
        System.out.println("2. Disable DPI awareness in Java 21");
        System.out.println("3. Use native Windows API for screen capture");
        System.out.println("4. Scale up captured images with high-quality interpolation");

        System.out.println("\nTo disable DPI awareness, add these JVM arguments:");
        System.out.println("  -Dsun.java2d.dpiaware=false");
        System.out.println("  -Dsun.java2d.uiScale=1.0");
        System.out.println("  -Dsun.java2d.win.uiScale=1.0");
    }
}
