package io.github.jspinak.brobot.capture;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple static holder for screen dimensions. Initialized once during application startup based on
 * the capture provider. All Region and pattern matching operations use these dimensions.
 */
@Slf4j
public class ScreenDimensions {

    private static int screenWidth = 1920; // Default fallback
    private static int screenHeight = 1080; // Default fallback
    private static boolean initialized = false;
    private static String captureProvider = "UNKNOWN";

    /**
     * Initialize screen dimensions based on the capture provider. Should be called once during
     * application startup.
     *
     * @param provider the capture provider name (JAVACV_FFMPEG, ROBOT, SIKULIX, etc.)
     * @param width the screen width to use
     * @param height the screen height to use
     */
    public static void initialize(String provider, int width, int height) {
        if (initialized) {
            log.warn("ScreenDimensions already initialized. Ignoring re-initialization attempt.");
            return;
        }

        captureProvider = provider;
        screenWidth = width;
        screenHeight = height;
        initialized = true;

        log.info("=== Screen Dimensions Initialized ===");
        log.info("Capture Provider: {}", captureProvider);
        log.info("Screen Resolution: {}x{}", screenWidth, screenHeight);
        log.info("=====================================");
    }

    /**
     * Get the screen width.
     *
     * @return the screen width
     */
    public static int getWidth() {
        if (!initialized) {
            log.warn("ScreenDimensions not initialized, using default: {}", screenWidth);
        }
        return screenWidth;
    }

    /**
     * Get the screen height.
     *
     * @return the screen height
     */
    public static int getHeight() {
        if (!initialized) {
            log.warn("ScreenDimensions not initialized, using default: {}", screenHeight);
        }
        return screenHeight;
    }

    /**
     * Check if dimensions have been initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get the capture provider name.
     *
     * @return the capture provider name
     */
    public static String getCaptureProvider() {
        return captureProvider;
    }

    /**
     * Get a descriptive string of the current configuration.
     *
     * @return description of current screen dimensions
     */
    public static String getInfo() {
        return String.format(
                "%dx%d (provider: %s, initialized: %s)",
                screenWidth, screenHeight, captureProvider, initialized);
    }

    /** Reset for testing purposes only. Should not be used in production code. */
    static void resetForTesting() {
        initialized = false;
        screenWidth = 1920;
        screenHeight = 1080;
        captureProvider = "UNKNOWN";
    }
}
