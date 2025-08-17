package io.github.jspinak.brobot.util.image.core;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Utility class for screen capture tests to handle environment detection.
 */
public class ScreenCaptureTestUtils {
    
    /**
     * Skip test if running in an environment where screen capture might not work properly.
     * This includes headless environments, WSL, CI/CD pipelines, etc.
     */
    public static void skipIfScreenCaptureUnavailable() {
        assumeFalse(GraphicsEnvironment.isHeadless(), 
                    "Skipping test in headless environment");
        assumeFalse(System.getenv("WSL_DISTRO_NAME") != null, 
                    "Skipping test in WSL environment where screen capture returns black images");
        assumeFalse(System.getenv("CI") != null, 
                    "Skipping test in CI environment");
        assumeFalse(System.getenv("GITHUB_ACTIONS") != null, 
                    "Skipping test in GitHub Actions");
        assumeFalse("true".equals(System.getProperty("java.awt.headless")), 
                    "Skipping test when java.awt.headless is true");
    }
    
    /**
     * Check if the current environment supports screen capture.
     * @return true if screen capture is likely to work, false otherwise
     */
    public static boolean isScreenCaptureAvailable() {
        return !GraphicsEnvironment.isHeadless() &&
               System.getenv("WSL_DISTRO_NAME") == null &&
               System.getenv("CI") == null &&
               System.getenv("GITHUB_ACTIONS") == null &&
               !"true".equals(System.getProperty("java.awt.headless"));
    }
}