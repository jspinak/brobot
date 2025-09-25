package io.github.jspinak.brobot.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for detecting the runtime environment. Used to conditionally disable tests that
 * require real display capabilities.
 */
public class EnvironmentDetector {

    private static Boolean isWSL = null;
    private static Boolean isHeadless = null;
    private static Boolean hasDisplay = null;

    /**
     * Checks if running in Windows Subsystem for Linux (WSL).
     *
     * @return true if running in WSL, false otherwise
     */
    public static boolean isWSL() {
        if (isWSL == null) {
            isWSL = detectWSL();
        }
        return isWSL;
    }

    /**
     * Checks if running in a headless environment (no display).
     *
     * @return true if headless, false otherwise
     */
    public static boolean isHeadless() {
        if (isHeadless == null) {
            isHeadless = java.awt.GraphicsEnvironment.isHeadless();
        }
        return isHeadless;
    }

    /**
     * Checks if a display is available (X11, Wayland, etc.).
     *
     * @return true if display is available, false otherwise
     */
    public static boolean hasDisplay() {
        if (hasDisplay == null) {
            hasDisplay = detectDisplay();
        }
        return hasDisplay;
    }

    /**
     * Checks if real screen capture is available. This requires not being in WSL and having a
     * display.
     *
     * @return true if real screen capture is available
     */
    public static boolean canCaptureScreen() {
        return !isWSL() && !isHeadless() && hasDisplay();
    }

    /**
     * Checks if running in a CI/CD environment.
     *
     * @return true if in CI/CD, false otherwise
     */
    public static boolean isCI() {
        return System.getenv("CI") != null
                || System.getenv("CONTINUOUS_INTEGRATION") != null
                || System.getenv("GITHUB_ACTIONS") != null
                || System.getenv("JENKINS_URL") != null
                || System.getenv("GITLAB_CI") != null;
    }

    /**
     * Gets a descriptive string of the current environment.
     *
     * @return environment description
     */
    public static String getEnvironmentDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Environment: ");

        if (isWSL()) {
            sb.append("WSL");
        } else if (isCI()) {
            sb.append("CI/CD");
        } else if (isHeadless()) {
            sb.append("Headless");
        } else {
            sb.append("Desktop");
        }

        sb.append(" | Display: ").append(hasDisplay() ? "Available" : "None");
        sb.append(" | Screen Capture: ").append(canCaptureScreen() ? "Available" : "Unavailable");

        return sb.toString();
    }

    private static boolean detectWSL() {
        // Check environment variable
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        if (wslDistro != null) {
            return true;
        }

        // Check for WSL-specific files
        Path wslInterop = Paths.get("/proc/sys/fs/binfmt_misc/WSLInterop");
        if (Files.exists(wslInterop)) {
            return true;
        }

        // Check /proc/version for Microsoft/WSL
        try {
            Path procVersion = Paths.get("/proc/version");
            if (Files.exists(procVersion)) {
                String content = Files.readString(procVersion).toLowerCase();
                if (content.contains("microsoft") || content.contains("wsl")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Ignore and continue
        }

        // Check if running.exe exists (WSL indicator)
        try {
            Process process = new ProcessBuilder("which", "wslpath").start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;
            }
        } catch (Exception ignored) {
            // Not in WSL or command failed
        }

        return false;
    }

    private static boolean detectDisplay() {
        // Check DISPLAY environment variable (X11)
        String display = System.getenv("DISPLAY");
        if (display != null && !display.isEmpty()) {
            // In WSL, DISPLAY might be set but not actually work
            if (isWSL()) {
                // Try to verify X11 is actually available
                try {
                    Process process = new ProcessBuilder("xset", "-q").start();
                    int exitCode = process.waitFor();
                    return exitCode == 0;
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        }

        // Check for Wayland
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        if (waylandDisplay != null && !waylandDisplay.isEmpty()) {
            return true;
        }

        // On Windows (not WSL), we assume display is available
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows") && !isWSL()) {
            return true;
        }

        // On macOS, we assume display is available
        if (os.contains("mac")) {
            return true;
        }

        return false;
    }

    /** Resets cached detection results. Useful for testing. */
    static void reset() {
        isWSL = null;
        isHeadless = null;
        hasDisplay = null;
    }
}
