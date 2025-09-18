package io.github.jspinak.brobot.config.environment;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized headless detection for the Brobot framework.
 *
 * <p>This class consolidates all headless detection logic into a single place to avoid
 * fragmentation and inconsistent behavior across the codebase. It provides:
 *
 * <ul>
 *   <li>Unified detection logic used by all Brobot components
 *   <li>Comprehensive debug logging for troubleshooting
 *   <li>Caching to avoid repeated expensive checks
 *   <li>Platform-specific detection logic
 *   <li>Clear separation between headless detection and mock mode
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @Autowired
 * private HeadlessDetector headlessDetector;
 *
 * if (headlessDetector.isHeadless()) {
 *     // Skip GUI operations
 * }
 * }</pre>
 *
 * @since 2.0.0
 */
@Slf4j
@Component
public class HeadlessDetector {

    // Cache settings
    private final AtomicBoolean cachedResult = new AtomicBoolean(false);
    private final AtomicBoolean cacheValid = new AtomicBoolean(false);
    private final AtomicLong lastCheckTime = new AtomicLong(0);
    private static final long CACHE_DURATION_MS = 60000; // 1 minute

    // Debug flag
    private volatile boolean debugLoggingEnabled = true;

    /**
     * Checks if the system is running in headless mode.
     *
     * <p>This method performs comprehensive checks including:
     *
     * <ul>
     *   <li>System property java.awt.headless
     *   <li>GraphicsEnvironment.isHeadless()
     *   <li>Graphics device availability
     *   <li>Platform-specific indicators
     * </ul>
     *
     * @return true if running in headless mode, false otherwise
     */
    public boolean isHeadless() {
        // Check cache first
        long currentTime = System.currentTimeMillis();
        if (cacheValid.get() && (currentTime - lastCheckTime.get()) < CACHE_DURATION_MS) {
            return cachedResult.get();
        }

        // Perform comprehensive check
        boolean result = performHeadlessCheck();

        // Update cache
        cachedResult.set(result);
        cacheValid.set(true);
        lastCheckTime.set(currentTime);

        return result;
    }

    /**
     * Forces a refresh of the cached headless state.
     *
     * <p>Useful when the environment may have changed (e.g., after setting system properties).
     */
    public void refreshCache() {
        cacheValid.set(false);
        if (debugLoggingEnabled) {
            log.debug("[HeadlessDetector] Cache invalidated, will recheck on next call");
        }
    }

    /**
     * Enables or disables debug logging for headless detection.
     *
     * @param enabled true to enable debug logging
     */
    public void setDebugLogging(boolean enabled) {
        this.debugLoggingEnabled = enabled;
    }

    /** Performs the actual headless detection with comprehensive logging. */
    private boolean performHeadlessCheck() {
        if (debugLoggingEnabled) {
            log.info("[HeadlessDetector] === Starting Headless Detection ===");
            logEnvironment();
        }

        // Step 1: Check if explicitly forced to non-headless
        String headlessProperty = System.getProperty("java.awt.headless");
        if ("false".equalsIgnoreCase(headlessProperty)) {
            // Even if java.awt.headless=false, we need to verify display access
            // because GraphicsEnvironment might have been initialized as headless
            boolean displayVerified = verifyDisplayAccess();
            if (displayVerified) {
                if (debugLoggingEnabled) {
                    log.info(
                            "[HeadlessDetector] ✓ Explicitly set to non-headless and display"
                                    + " access verified");
                }
                return false;
            } else {
                if (debugLoggingEnabled) {
                    log.warn(
                            "[HeadlessDetector] ⚠ java.awt.headless=false but display access"
                                    + " failed - treating as headless");
                }
                return true;
            }
        }

        // Step 2: Check if explicitly forced to headless
        if ("true".equalsIgnoreCase(headlessProperty)) {
            if (debugLoggingEnabled) {
                log.info(
                        "[HeadlessDetector] ✗ Explicitly set to headless via"
                                + " java.awt.headless=true");
            }
            return true;
        }

        // Step 3: Check GraphicsEnvironment
        boolean graphicsHeadless = false;
        try {
            graphicsHeadless = GraphicsEnvironment.isHeadless();
            if (debugLoggingEnabled) {
                log.info(
                        "[HeadlessDetector] GraphicsEnvironment.isHeadless() = {}",
                        graphicsHeadless);
            }

            // If GraphicsEnvironment says it's not headless, verify we can actually access displays
            if (!graphicsHeadless) {
                boolean displayVerified = verifyDisplayAccess();
                if (!displayVerified) {
                    if (debugLoggingEnabled) {
                        log.warn(
                                "[HeadlessDetector] GraphicsEnvironment reports non-headless but"
                                        + " display access failed");
                    }
                    return true; // Treat as headless if we can't access displays
                }
            }
        } catch (HeadlessException e) {
            if (debugLoggingEnabled) {
                log.info("[HeadlessDetector] HeadlessException caught - running in headless mode");
            }
            return true;
        } catch (Exception e) {
            if (debugLoggingEnabled) {
                log.warn(
                        "[HeadlessDetector] Error checking GraphicsEnvironment: {}",
                        e.getMessage());
            }
            return true; // Assume headless if we can't check
        }

        // Step 4: Platform-specific checks
        if (!graphicsHeadless) {
            boolean platformHeadless = checkPlatformSpecific();
            if (platformHeadless) {
                if (debugLoggingEnabled) {
                    log.info("[HeadlessDetector] Platform-specific check indicates headless mode");
                }
                return true;
            }
        }

        // Final result
        if (debugLoggingEnabled) {
            log.info(
                    "[HeadlessDetector] === Detection Complete: {} ===",
                    graphicsHeadless ? "HEADLESS" : "GUI AVAILABLE");
        }

        return graphicsHeadless;
    }

    /** Verifies that we can actually access display devices. */
    private boolean verifyDisplayAccess() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultDevice = ge.getDefaultScreenDevice();

            if (defaultDevice == null) {
                if (debugLoggingEnabled) {
                    log.warn("[HeadlessDetector] No default screen device found");
                }
                return false;
            }

            // Try to get screen devices
            GraphicsDevice[] devices = ge.getScreenDevices();
            if (devices == null || devices.length == 0) {
                if (debugLoggingEnabled) {
                    log.warn("[HeadlessDetector] No screen devices available");
                }
                return false;
            }

            if (debugLoggingEnabled) {
                log.info(
                        "[HeadlessDetector] ✓ Display access verified: {} screen(s) available",
                        devices.length);
            }

            return true;
        } catch (Exception e) {
            if (debugLoggingEnabled) {
                log.warn("[HeadlessDetector] Failed to verify display access: {}", e.getMessage());
            }
            return false;
        }
    }

    /** Performs platform-specific headless checks. */
    private boolean checkPlatformSpecific() {
        String os = System.getProperty("os.name", "").toLowerCase();

        // Check for CI environment
        if (isRunningInCI()) {
            if (debugLoggingEnabled) {
                log.info("[HeadlessDetector] Detected CI environment");
            }
            return true;
        }

        // Linux/Unix: Check DISPLAY variable
        if (os.contains("nix") || os.contains("nux")) {
            String display = System.getenv("DISPLAY");
            if (display == null || display.isEmpty()) {
                if (debugLoggingEnabled) {
                    log.info("[HeadlessDetector] Linux/Unix: No DISPLAY variable set");
                }
                return true;
            }
        }

        // WSL: Special handling
        if (isWSL()) {
            String display = System.getenv("DISPLAY");
            if (display == null || display.isEmpty()) {
                if (debugLoggingEnabled) {
                    log.info("[HeadlessDetector] WSL: No DISPLAY variable set");
                }
                return true;
            }
        }

        // Windows: Generally has display unless in special environments
        if (os.contains("windows") && !isWSL()) {
            // Windows usually has display, but verify through GraphicsEnvironment
            return false;
        }

        // macOS: Check for SSH session
        if (os.contains("mac")) {
            String sshConnection = System.getenv("SSH_CONNECTION");
            if (sshConnection != null && !sshConnection.isEmpty()) {
                if (debugLoggingEnabled) {
                    log.info("[HeadlessDetector] macOS: SSH connection detected");
                }
                return true;
            }
        }

        return false;
    }

    /** Logs detailed environment information for debugging. */
    private void logEnvironment() {
        log.info("[HeadlessDetector] Environment Information:");
        log.info("  OS: {}", System.getProperty("os.name"));
        log.info("  OS Version: {}", System.getProperty("os.version"));
        log.info("  Java Version: {}", System.getProperty("java.version"));
        log.info("  java.awt.headless: {}", System.getProperty("java.awt.headless"));
        log.info(
                "  brobot.preserve.headless.setting: {}",
                System.getProperty("brobot.preserve.headless.setting"));
        log.info("  DISPLAY: {}", System.getenv("DISPLAY"));
        log.info("  WSL_DISTRO_NAME: {}", System.getenv("WSL_DISTRO_NAME"));
        log.info("  CI: {}", System.getenv("CI"));
        log.info("  GITHUB_ACTIONS: {}", System.getenv("GITHUB_ACTIONS"));
        log.info("  SSH_CONNECTION: {}", System.getenv("SSH_CONNECTION"));
    }

    /** Checks if running in WSL (Windows Subsystem for Linux). */
    private boolean isWSL() {
        return System.getenv("WSL_DISTRO_NAME") != null
                || System.getenv("WSL_INTEROP") != null
                || System.getenv("WSL_DISTRO") != null;
    }

    /** Checks if running in CI/CD environment. */
    private boolean isRunningInCI() {
        return System.getenv("CI") != null
                || System.getenv("CONTINUOUS_INTEGRATION") != null
                || System.getenv("JENKINS_URL") != null
                || System.getenv("GITHUB_ACTIONS") != null
                || System.getenv("GITLAB_CI") != null
                || System.getenv("CIRCLECI") != null
                || System.getenv("TRAVIS") != null
                || System.getenv("APPVEYOR") != null
                || System.getenv("DRONE") != null
                || System.getenv("TEAMCITY_VERSION") != null
                || System.getenv("BUILDKITE") != null
                || System.getenv("BITBUCKET_PIPELINES") != null;
    }

    /**
     * Gets a detailed status report of the headless detection.
     *
     * @return detailed status string
     */
    public String getStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("HeadlessDetector Status:\n");
        report.append("  Current State: ")
                .append(isHeadless() ? "HEADLESS" : "GUI AVAILABLE")
                .append("\n");
        report.append("  OS: ").append(System.getProperty("os.name")).append("\n");
        report.append("  java.awt.headless: ")
                .append(System.getProperty("java.awt.headless"))
                .append("\n");
        report.append("  GraphicsEnvironment.isHeadless(): ")
                .append(GraphicsEnvironment.isHeadless())
                .append("\n");
        report.append("  CI Environment: ").append(isRunningInCI() ? "Yes" : "No").append("\n");
        report.append("  WSL: ").append(isWSL() ? "Yes" : "No").append("\n");
        return report.toString();
    }
}
