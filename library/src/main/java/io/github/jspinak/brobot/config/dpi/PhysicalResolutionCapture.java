package io.github.jspinak.brobot.config.dpi;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

/**
 * Provides methods to capture at physical resolution even when DPI awareness is enabled.
 *
 * <p>This is a workaround for when DPI awareness cannot be disabled at JVM startup.
 *
 * @since 1.1.0
 */
public class PhysicalResolutionCapture {

    private static final boolean DPI_AWARE =
            !"false".equals(System.getProperty("sun.java2d.dpiaware"));
    private static Double cachedScaleFactor = null;

    /**
     * Captures the screen and scales it to physical resolution if needed.
     *
     * @param screen the screen to capture
     * @return BufferedImage at physical resolution
     */
    public static BufferedImage capturePhysical(Screen screen) {
        // Get the logical capture
        ScreenImage screenImage = screen.capture();
        BufferedImage logicalCapture = screenImage.getImage();

        // If DPI awareness is disabled, we're already at physical resolution
        if (!DPI_AWARE) {
            return logicalCapture;
        }

        // Calculate scale factor
        double scaleFactor = getScaleFactor();

        // If no scaling needed, return original
        if (Math.abs(scaleFactor - 1.0) < 0.01) {
            return logicalCapture;
        }

        // Scale up to physical resolution
        int physicalWidth = (int) (logicalCapture.getWidth() * scaleFactor);
        int physicalHeight = (int) (logicalCapture.getHeight() * scaleFactor);

        System.out.println(
                String.format(
                        "[PhysicalCapture] Scaling from %dx%d to %dx%d (factor: %.2f)",
                        logicalCapture.getWidth(),
                        logicalCapture.getHeight(),
                        physicalWidth,
                        physicalHeight,
                        scaleFactor));

        return scaleImage(logicalCapture, physicalWidth, physicalHeight);
    }

    /**
     * Detects the DPI scale factor.
     *
     * @return scale factor (e.g., 1.25 for 125% scaling)
     */
    public static double getScaleFactor() {
        if (cachedScaleFactor != null) {
            return cachedScaleFactor;
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        // Get the transform scale
        double scaleX = gc.getDefaultTransform().getScaleX();

        // Alternative detection: compare logical vs expected physical
        DisplayMode dm = gd.getDisplayMode();
        Dimension toolkit = Toolkit.getDefaultToolkit().getScreenSize();

        // If transform scale is available and > 1, use it
        if (scaleX > 1.0) {
            cachedScaleFactor = scaleX;
            return scaleX;
        }

        // Otherwise try to detect from resolution mismatch
        // Common physical resolutions and their expected logical sizes at various scales
        int physicalWidth = dm.getWidth();
        int logicalWidth = toolkit.width;

        if (physicalWidth == 1920 && logicalWidth == 1536) {
            cachedScaleFactor = 1.25; // 125% scaling
        } else if (physicalWidth == 1920 && logicalWidth == 1280) {
            cachedScaleFactor = 1.5; // 150% scaling
        } else if (physicalWidth == 3840 && logicalWidth == 1920) {
            cachedScaleFactor = 2.0; // 200% scaling
        } else {
            // Try to calculate
            cachedScaleFactor = (double) physicalWidth / logicalWidth;
        }

        return cachedScaleFactor;
    }

    /** Scales an image to the specified dimensions. */
    private static BufferedImage scaleImage(
            BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, original.getType());
        Graphics2D g2d = scaled.createGraphics();

        // Use high-quality rendering for scaling
        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaled;
    }

    /** Checks if the system appears to have DPI scaling. */
    public static boolean hasScaling() {
        return Math.abs(getScaleFactor() - 1.0) > 0.01;
    }

    /** Gets a description of the current capture mode. */
    public static String getCaptureMode() {
        if (!DPI_AWARE) {
            return "Physical resolution (DPI awareness disabled)";
        }

        double scale = getScaleFactor();
        if (Math.abs(scale - 1.0) < 0.01) {
            return "Physical resolution (no scaling detected)";
        }

        return String.format(
                "Logical resolution with %.0f%% scaling (will scale up captures)", scale * 100);
    }
}
