package io.github.jspinak.brobot.config.dpi;

import java.awt.*;

import org.springframework.stereotype.Component;

/**
 * Automatically detects DPI scaling factor for the primary monitor. This helps configure pattern
 * scaling to match screen capture resolution.
 *
 * <p>Part of the Brobot library - available to all Brobot applications.
 */
@Component
public class DPIAutoDetector {

    /**
     * Detects the DPI scaling factor by comparing logical vs physical screen dimensions.
     *
     * @return The scaling factor to apply to patterns (e.g., 0.8 for 125% Windows scaling)
     */
    public float detectScalingFactor() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();

            // Get the default toolkit
            Toolkit toolkit = Toolkit.getDefaultToolkit();

            // Get logical screen size (what Java/SikuliX sees)
            Dimension screenSize = toolkit.getScreenSize();
            int logicalWidth = screenSize.width;
            int logicalHeight = screenSize.height;

            // Get the graphics transform
            var transform = gc.getDefaultTransform();
            double scaleX = transform.getScaleX();
            double scaleY = transform.getScaleY();

            System.out.println("[Brobot DPI Detection]");
            System.out.println("  Logical resolution: " + logicalWidth + "x" + logicalHeight);
            System.out.println("  Transform scale: " + scaleX + "x" + scaleY);

            // If transform scale is available and not 1.0, we have DPI scaling
            if (scaleX > 1.0) {
                // Physical resolution = logical * scale
                int physicalWidth = (int) (logicalWidth * scaleX);
                int physicalHeight = (int) (logicalHeight * scaleY);

                System.out.println(
                        "  Physical resolution: " + physicalWidth + "x" + physicalHeight);
                System.out.println("  Windows DPI scaling: " + (int) (scaleX * 100) + "%");

                // Patterns need to be scaled DOWN to match logical resolution
                // If Windows is at 125% (1.25x), patterns need 0.8x scaling (1/1.25 = 0.8)
                float patternScale = (float) (1.0 / scaleX);
                System.out.println("  Pattern scale factor: " + patternScale);

                return patternScale;
            }

            // No scaling detected - try alternative detection method
            return detectScalingByResolution(logicalWidth, logicalHeight);

        } catch (Exception e) {
            System.err.println("[Brobot] Error detecting DPI scaling: " + e.getMessage());
            return 1.0f; // Default to no scaling on error
        }
    }

    /**
     * Alternative detection method based on common resolutions. Assumes standard physical
     * resolutions and detects scaling by logical size.
     */
    private float detectScalingByResolution(int logicalWidth, int logicalHeight) {
        // Common scaled resolutions for 1920x1080
        if (logicalWidth == 1536 && logicalHeight == 864) {
            System.out.println("  Detected 1536x864 - likely 125% scaling of 1920x1080");
            return 0.8f;
        }
        if (logicalWidth == 1280 && logicalHeight == 720) {
            System.out.println("  Detected 1280x720 - likely 150% scaling of 1920x1080");
            return 0.667f;
        }

        // Common scaled resolutions for 2560x1440
        if (logicalWidth == 2048 && logicalHeight == 1152) {
            System.out.println("  Detected 2048x1152 - likely 125% scaling of 2560x1440");
            return 0.8f;
        }
        if (logicalWidth == 1707 && logicalHeight == 960) {
            System.out.println("  Detected 1707x960 - likely 150% scaling of 2560x1440");
            return 0.667f;
        }

        // Common scaled resolutions for 3840x2160 (4K)
        if (logicalWidth == 3072 && logicalHeight == 1728) {
            System.out.println("  Detected 3072x1728 - likely 125% scaling of 3840x2160");
            return 0.8f;
        }
        if (logicalWidth == 2560 && logicalHeight == 1440) {
            // Could be native QHD or scaled 4K
            System.out.println("  Detected 2560x1440 - could be native QHD or 150% scaled 4K");
            return 1.0f; // Assume native unless we can detect otherwise
        }
        if (logicalWidth == 1920 && logicalHeight == 1080) {
            // Could be native FHD or 200% scaled 4K
            System.out.println("  Detected 1920x1080 - likely native resolution");
            return 1.0f;
        }

        System.out.println(
                "  No DPI scaling detected (resolution: "
                        + logicalWidth
                        + "x"
                        + logicalHeight
                        + ")");
        return 1.0f; // No scaling detected
    }

    /** Gets a human-readable description of the current DPI configuration. */
    public String getScalingDescription() {
        float scale = detectScalingFactor();

        if (Math.abs(scale - 1.0f) < 0.01f) {
            return "No DPI scaling detected (100%)";
        } else if (Math.abs(scale - 0.8f) < 0.01f) {
            return "125% DPI scaling detected";
        } else if (Math.abs(scale - 0.667f) < 0.01f) {
            return "150% DPI scaling detected";
        } else if (Math.abs(scale - 0.5f) < 0.01f) {
            return "200% DPI scaling detected";
        } else {
            int percent = (int) (100.0f / scale);
            return percent + "% DPI scaling detected";
        }
    }
}
