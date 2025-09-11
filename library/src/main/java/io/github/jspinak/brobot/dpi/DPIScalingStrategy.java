package io.github.jspinak.brobot.dpi;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import org.sikuli.basics.Settings;

/**
 * Strategy for handling DPI scaling in pattern matching.
 *
 * <p>Key concepts: - Physical pixels: Actual screen pixels (what SikuliX IDE captures) - Logical
 * pixels: DPI-scaled pixels (what Windows/applications report) - Pattern scaling: Adjusting
 * patterns to match screen capture dimensions
 *
 * <p>Windows with 125% scaling: - Logical 100x100 = Physical 125x125 - Pattern scale factor =
 * 1/1.25 = 0.8
 */
public class DPIScalingStrategy {

    /** Pattern source types */
    public enum PatternSource {
        SIKULI_IDE, // Captured with SikuliX IDE (physical pixels)
        WINDOWS_TOOL, // Captured with Windows snipping tool (logical pixels)
        UNKNOWN // Unknown source, assume physical pixels
    }

    /** Detects the current display scaling factor */
    public static double detectDisplayScaling() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (ge != null && ge.getDefaultScreenDevice() != null) {
                return ge.getDefaultScreenDevice()
                        .getDefaultConfiguration()
                        .getDefaultTransform()
                        .getScaleX();
            }
        } catch (Exception e) {
            System.err.println("[DPI] Could not detect display scaling: " + e.getMessage());
        }
        return 1.0; // Default to no scaling
    }

    /**
     * Calculates the pattern scale factor for SikuliX matching
     *
     * @param displayScale The display scaling factor (e.g., 1.25 for 125%)
     * @return The pattern scale factor to use with Settings.AlwaysResize
     */
    public static float calculatePatternScaleFactor(double displayScale) {
        // For patterns in physical pixels, we need to scale them down
        // to match the logical pixel dimensions that SikuliX sees
        return (float) (1.0 / displayScale);
    }

    /**
     * Determines optimal Settings.AlwaysResize value based on pattern source
     *
     * @param source The source of the pattern images
     * @return The recommended Settings.AlwaysResize value
     */
    public static float getOptimalResizeFactor(PatternSource source) {
        double displayScale = detectDisplayScaling();

        switch (source) {
            case SIKULI_IDE:
                // SikuliX IDE captures in physical pixels
                // Need to scale down for DPI-aware matching
                return calculatePatternScaleFactor(displayScale);

            case WINDOWS_TOOL:
                // Windows tools capture in logical pixels
                // These already match the application's view
                // But SikuliX still needs scaling for matching
                return calculatePatternScaleFactor(displayScale);

            case UNKNOWN:
            default:
                // Assume physical pixels (most conservative approach)
                return calculatePatternScaleFactor(displayScale);
        }
    }

    /**
     * Analyzes a pattern image to guess its source type
     *
     * @param image The pattern image
     * @param expectedLogicalWidth Expected width in logical pixels
     * @param expectedLogicalHeight Expected height in logical pixels
     * @return The likely pattern source
     */
    public static PatternSource analyzePatternSource(
            BufferedImage image, int expectedLogicalWidth, int expectedLogicalHeight) {
        double displayScale = detectDisplayScaling();
        int actualWidth = image.getWidth();
        int actualHeight = image.getHeight();

        // Calculate expected physical dimensions
        int expectedPhysicalWidth = (int) (expectedLogicalWidth * displayScale);
        int expectedPhysicalHeight = (int) (expectedLogicalHeight * displayScale);

        // Allow 5% tolerance for dimension matching
        double tolerance = 0.05;

        // Check if dimensions match logical pixels (Windows tool capture)
        if (Math.abs(actualWidth - expectedLogicalWidth) <= expectedLogicalWidth * tolerance
                && Math.abs(actualHeight - expectedLogicalHeight)
                        <= expectedLogicalHeight * tolerance) {
            return PatternSource.WINDOWS_TOOL;
        }

        // Check if dimensions match physical pixels (SikuliX IDE capture)
        if (Math.abs(actualWidth - expectedPhysicalWidth) <= expectedPhysicalWidth * tolerance
                && Math.abs(actualHeight - expectedPhysicalHeight)
                        <= expectedPhysicalHeight * tolerance) {
            return PatternSource.SIKULI_IDE;
        }

        return PatternSource.UNKNOWN;
    }

    /**
     * Configures SikuliX settings for optimal pattern matching
     *
     * @param source The source of pattern images
     * @param minSimilarity Minimum similarity threshold
     */
    public static void configureSikuliX(PatternSource source, float minSimilarity) {
        float resizeFactor = getOptimalResizeFactor(source);
        double displayScale = detectDisplayScaling();

        Settings.AlwaysResize = resizeFactor;
        Settings.MinSimilarity = minSimilarity;
        Settings.CheckLastSeen = true; // Performance optimization

        System.out.println("[DPI] SikuliX Configuration:");
        System.out.println("  Display scaling: " + (int) (displayScale * 100) + "%");
        System.out.println("  Pattern source: " + source);
        System.out.println("  Settings.AlwaysResize: " + resizeFactor);
        System.out.println("  Settings.MinSimilarity: " + minSimilarity);

        if (resizeFactor != 1.0f) {
            System.out.println(
                    "  Pattern scaling: "
                            + (resizeFactor < 1.0f ? "DOWNSCALE" : "UPSCALE")
                            + " by factor "
                            + resizeFactor);
        }
    }

    /** Provides diagnostic information about DPI scaling */
    public static void printDiagnostics() {
        double displayScale = detectDisplayScaling();
        float patternScale = calculatePatternScaleFactor(displayScale);

        System.out.println("\n=== DPI Scaling Diagnostics ===");
        System.out.println("Display Information:");
        System.out.println("  Scaling factor: " + (int) (displayScale * 100) + "%");
        System.out.println("  Physical/Logical ratio: " + displayScale);

        System.out.println("\nPattern Matching Configuration:");
        System.out.println("  Recommended Settings.AlwaysResize: " + patternScale);
        System.out.println("  Current Settings.AlwaysResize: " + Settings.AlwaysResize);

        System.out.println("\nDimension Conversions (example 100x100 logical):");
        System.out.println("  Logical pixels: 100x100");
        System.out.println(
                "  Physical pixels: "
                        + (int) (100 * displayScale)
                        + "x"
                        + (int) (100 * displayScale));
        System.out.println("  Pattern scale needed: " + patternScale);

        System.out.println("\nRecommendations:");
        if (Math.abs(displayScale - 1.0) < 0.01) {
            System.out.println("  ✓ No DPI scaling detected - patterns should match directly");
        } else {
            System.out.println("  ⚠ DPI scaling active - ensure patterns are properly scaled");
            System.out.println(
                    "  - SikuliX IDE patterns: Use Settings.AlwaysResize = " + patternScale);
            System.out.println("  - Windows tool patterns: May need pre-processing or scaling");
        }
        System.out.println("================================\n");
    }
}
