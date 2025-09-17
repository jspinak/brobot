package io.github.jspinak.brobot.capture.provider;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Robot-based screen capture provider.
 *
 * <p>Uses Java's Robot API with scaling to achieve physical resolution capture. In Java 21, Robot
 * captures at logical resolution by default, but we can scale the image to physical resolution if
 * needed.
 *
 * @since 1.1.0
 */
@Component
public class RobotCaptureProvider implements CaptureProvider {

    @Value("${brobot.capture.robot.scale-to-physical:true}")
    private boolean scaleToPhysical;

    @Value("${brobot.capture.robot.expected-physical-width:1920}")
    private int expectedPhysicalWidth;

    @Value("${brobot.capture.robot.expected-physical-height:1080}")
    private int expectedPhysicalHeight;

    private Robot robot;
    private double detectedScale = 1.0;
    private boolean scaleDetected = false;

    public RobotCaptureProvider() {
        try {
            // Check if we should preserve the headless setting
            String preserveHeadless = System.getProperty("brobot.preserve.headless.setting");
            if (!"true".equals(preserveHeadless)) {
                // For GUI automation, ensure headless is false before creating Robot
                String currentHeadless = System.getProperty("java.awt.headless");
                if ("true".equals(currentHeadless)) {
                    System.setProperty("java.awt.headless", "false");
                    System.out.println(
                            "[Robot] Reset java.awt.headless from 'true' to 'false' for GUI"
                                    + " automation");
                }
            }

            this.robot = new Robot();
            detectScaling();
        } catch (AWTException e) {
            System.err.println("[Robot] Failed to initialize Robot: " + e.getMessage());
        }
    }

    @Override
    public BufferedImage captureScreen() throws IOException {
        return captureScreen(0);
    }

    @Override
    public BufferedImage captureScreen(int screenId) throws IOException {
        if (robot == null) {
            throw new IOException("Robot not initialized");
        }

        // Get screen bounds
        Rectangle bounds = getScreenBounds(screenId);

        // Capture
        BufferedImage capture = robot.createScreenCapture(bounds);

        // Scale to physical if needed
        if (scaleToPhysical && needsScaling(capture)) {
            capture = scaleToPhysicalResolution(capture);
        }

        logCapture("Screen " + screenId, capture.getWidth(), capture.getHeight());
        return capture;
    }

    @Override
    public BufferedImage captureRegion(Rectangle region) throws IOException {
        // Always capture full screen for consistent behavior
        // Region filtering should happen at a higher level
        return captureScreen();
    }

    @Override
    public BufferedImage captureRegion(int screenId, Rectangle region) throws IOException {
        // Always capture full screen for consistent behavior
        // Region filtering should happen at a higher level
        return captureScreen(screenId);
    }

    /** Detects if the system has DPI scaling. */
    private void detectScaling() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();

            // Check transform scale (works on some systems)
            double scaleX = gc.getDefaultTransform().getScaleX();
            if (scaleX > 1.0) {
                detectedScale = scaleX;
                scaleDetected = true;
                System.out.println("[Robot] Detected DPI scale: " + String.format("%.2f", scaleX));
                return;
            }

            // Try to detect by comparing expected vs actual resolution
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (screenSize.width < expectedPhysicalWidth) {
                // Likely scaled - common scenarios
                if (screenSize.width == 1536 && expectedPhysicalWidth == 1920) {
                    detectedScale = 1.25; // 125% scaling
                    scaleDetected = true;
                } else if (screenSize.width == 1280 && expectedPhysicalWidth == 1920) {
                    detectedScale = 1.5; // 150% scaling
                    scaleDetected = true;
                } else if (screenSize.width == 960 && expectedPhysicalWidth == 1920) {
                    detectedScale = 2.0; // 200% scaling
                    scaleDetected = true;
                } else {
                    // Try to calculate
                    detectedScale = (double) expectedPhysicalWidth / screenSize.width;
                    scaleDetected = true;
                }

                if (scaleDetected) {
                    System.out.println(
                            "[Robot] Detected scaling based on resolution: "
                                    + String.format("%.0f%%", detectedScale * 100));
                }
            }

        } catch (Exception e) {
            System.err.println("[Robot] Could not detect scaling: " + e.getMessage());
        }
    }

    /** Checks if captured image needs scaling to physical resolution. */
    private boolean needsScaling(BufferedImage capture) {
        if (!scaleDetected) {
            return false;
        }

        // Check if capture width suggests logical resolution
        return capture.getWidth() < expectedPhysicalWidth;
    }

    /** Scales captured image to physical resolution. */
    private BufferedImage scaleToPhysicalResolution(BufferedImage source) {
        int targetWidth = (int) (source.getWidth() * detectedScale);
        int targetHeight = (int) (source.getHeight() * detectedScale);

        return scaleImage(source, targetWidth, targetHeight);
    }

    /** High-quality image scaling. */
    private BufferedImage scaleImage(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, source.getType());
        Graphics2D g2d = scaled.createGraphics();

        // Use high-quality rendering
        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaled;
    }

    /** Gets screen bounds for the specified screen. */
    private Rectangle getScreenBounds(int screenId) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        if (screenId >= 0 && screenId < devices.length) {
            return devices[screenId].getDefaultConfiguration().getBounds();
        }

        // Default to primary screen
        return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }

    private void logCapture(String target, int width, int height) {
        String resType =
                scaleToPhysical && scaleDetected
                        ? "scaled to physical"
                        : (scaleDetected ? "logical" : "native");
        System.out.println(
                String.format(
                        "[Robot] Captured %s at %dx%d (%s resolution)",
                        target, width, height, resType));
    }

    @Override
    public boolean isAvailable() {
        return robot != null;
    }

    @Override
    public String getName() {
        return "Robot";
    }

    @Override
    public ResolutionType getResolutionType() {
        if (scaleToPhysical && scaleDetected) {
            return ResolutionType.PHYSICAL; // We scale to physical
        }

        // Java 21 captures at logical by default
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.8")) {
            return ResolutionType.PHYSICAL;
        }
        return ResolutionType.LOGICAL;
    }
}
