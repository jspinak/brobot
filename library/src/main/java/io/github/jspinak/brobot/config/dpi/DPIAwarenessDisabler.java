package io.github.jspinak.brobot.config.dpi;

/**
 * Disables DPI awareness in Java 21+ to ensure screen captures are at physical resolution.
 *
 * <p>This class must be initialized BEFORE any AWT/Swing classes are loaded. The system properties
 * it sets are only effective if set before the Java 2D subsystem initializes.
 *
 * <p>When DPI awareness is disabled:
 *
 * <ul>
 *   <li>Screen captures will be at physical resolution (e.g., 1920x1080)
 *   <li>Pattern matching will work consistently with SikuliX IDE patterns
 *   <li>No scaling compensation will be needed for high-DPI displays
 * </ul>
 *
 * @since 1.1.0
 */
public class DPIAwarenessDisabler {

    private static boolean initialized = false;
    private static boolean dpiAwarenessDisabled = false;

    /**
     * Static initializer block ensures this runs before any AWT classes are loaded. This is
     * critical for the system properties to take effect.
     */
    static {
        initializeEarly();
    }

    /**
     * Initializes DPI settings early in the JVM lifecycle. This method is idempotent - calling it
     * multiple times has no additional effect.
     */
    public static synchronized void initializeEarly() {
        if (initialized) {
            return;
        }

        initialized = true;

        // Check if DPI awareness should be disabled via environment variable or property
        String disableDPI =
                System.getProperty("brobot.dpi.disable", System.getenv("BROBOT_DISABLE_DPI"));

        // Default to false (keep DPI awareness enabled) to allow auto-detection
        // DPI awareness enabled = captures at logical resolution, auto resize-factor handles
        // scaling
        boolean shouldDisable = "true".equalsIgnoreCase(disableDPI);

        if (shouldDisable) {
            disableDPIAwareness();
        } else {
            // DPI awareness is enabled by default, log at debug level
            dpiAwarenessDisabled = false;
        }
    }

    /**
     * Disables DPI awareness by setting appropriate system properties. Must be called before any
     * AWT/Swing classes are loaded.
     */
    private static void disableDPIAwareness() {
        try {
            // Primary property for disabling DPI awareness
            System.setProperty("sun.java2d.dpiaware", "false");

            // Additional properties to ensure 1:1 pixel mapping
            System.setProperty("sun.java2d.uiScale", "1.0");
            System.setProperty("sun.java2d.win.uiScale", "1.0");

            // For Linux systems with GTK
            System.setProperty("sun.java2d.uiScale.enabled", "false");

            // For macOS Retina displays
            System.setProperty("sun.java2d.metal.uiScale", "1.0");

            dpiAwarenessDisabled = true;

            System.out.println(
                    "[Brobot DPI] DPI awareness DISABLED - captures will be at physical"
                            + " resolution");
            System.out.println("[Brobot DPI] Properties set:");
            System.out.println("  sun.java2d.dpiaware = false");
            System.out.println("  sun.java2d.uiScale = 1.0");
            System.out.println("  sun.java2d.win.uiScale = 1.0");
            System.out.println("  sun.java2d.uiScale.enabled = false");
            System.out.println("  sun.java2d.metal.uiScale = 1.0");

        } catch (SecurityException e) {
            System.err.println(
                    "[Brobot DPI] Warning: Could not set system properties: " + e.getMessage());
            System.err.println("[Brobot DPI] You may need to set these as JVM arguments:");
            System.err.println("  -Dsun.java2d.dpiaware=false");
            System.err.println("  -Dsun.java2d.uiScale=1.0");
        }
    }

    /**
     * Checks if DPI awareness has been disabled.
     *
     * @return true if DPI awareness is disabled, false otherwise
     */
    public static boolean isDPIAwarenessDisabled() {
        return dpiAwarenessDisabled;
    }

    /**
     * Gets a description of the current DPI settings.
     *
     * @return human-readable description of DPI configuration
     */
    public static String getDPIStatus() {
        if (!initialized) {
            return "Not initialized";
        }

        if (dpiAwarenessDisabled) {
            return "DPI awareness DISABLED - physical resolution capture (compatible with SikuliX"
                    + " IDE)";
        } else {
            return "DPI awareness ENABLED - resolution depends on capture provider (FFmpeg/Robot:"
                    + " Physical, SikuliX: Logical)";
        }
    }

    /**
     * Forces initialization if it hasn't happened yet. Useful for ensuring DPI settings are applied
     * early.
     */
    public static void ensureInitialized() {
        if (!initialized) {
            initializeEarly();
        }
    }
}
