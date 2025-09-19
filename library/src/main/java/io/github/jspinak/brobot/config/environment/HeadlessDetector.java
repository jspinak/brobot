package io.github.jspinak.brobot.config.environment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Simplified headless detection for the Brobot framework.
 *
 * <p>This class now primarily relies on user configuration via properties to determine headless
 * mode, avoiding the problematic automatic detection that causes issues with GraphicsEnvironment
 * initialization.
 *
 * <p>Configuration via application.properties:
 *
 * <pre>{@code
 * # Explicitly set headless mode (defaults to false)
 * brobot.headless=false
 *
 * # Or use the standard Java property at JVM startup
 * java -Djava.awt.headless=false -jar your-app.jar
 * }</pre>
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

    private final boolean headlessMode;
    private final boolean forcedMode;

    // REMOVED static initializer to prevent early GraphicsEnvironment initialization
    // This was causing headless detection issues on Windows

    public HeadlessDetector(
            @Value("${brobot.headless:false}") boolean brobotHeadless,
            @Value("${brobot.headless.debug:false}") boolean debugEnabled) {

        // Don't call ForceNonHeadlessInitializer as it triggers GraphicsEnvironment
        this.forcedMode = false;

        // Use property setting, but warn if GraphicsEnvironment disagrees
        this.headlessMode = brobotHeadless;

        // DON'T check GraphicsEnvironment.isHeadless() here as it triggers initialization
        // This was causing the headless detection problem on Windows
        // We rely on the configured property value instead
        boolean actualHeadless = false; // We'll assume non-headless unless told otherwise

        if (this.headlessMode) {
            log.info("[HeadlessDetector] Headless mode ENABLED via brobot.headless property");
        } else {
            log.info("[HeadlessDetector] Headless mode DISABLED (GUI mode enabled)");

            // Warn if there's a mismatch
            if (actualHeadless) {
                log.warn(
                        "[HeadlessDetector] WARNING: brobot.headless=false but"
                                + " GraphicsEnvironment.isHeadless()=true");
                log.warn("[HeadlessDetector] This may cause issues with Robot and screen capture");
                log.warn(
                        "[HeadlessDetector] ForceNonHeadlessInitializer attempted override: {}",
                        forcedMode);
                log.warn(
                        "[HeadlessDetector] Add -Djava.awt.headless=false to JVM arguments or"
                                + " gradle.properties");
            } else {
                log.info("[HeadlessDetector] ✓ GraphicsEnvironment confirms non-headless mode");
            }
        }

        if (debugEnabled) {
            log.debug("[HeadlessDetector] Detailed Configuration:");
            log.debug("  brobot.headless: {}", brobotHeadless);
            log.debug("  java.awt.headless property: {}", System.getProperty("java.awt.headless"));
            // Don't call GraphicsEnvironment.isHeadless() or ForceNonHeadlessInitializer methods
            // as they trigger early initialization
            log.debug("  Final headless mode: {}", this.headlessMode);
        }
    }

    /**
     * Checks if the system is running in headless mode.
     *
     * <p>This method now simply returns the configured headless mode value, avoiding problematic
     * runtime detection that can cause GraphicsEnvironment issues.
     *
     * @return true if running in headless mode, false otherwise
     */
    public boolean isHeadless() {
        return headlessMode;
    }

    /**
     * Returns whether the system was configured as headless at startup.
     *
     * @deprecated Use isHeadless() instead
     * @return true if configured as headless
     */
    @Deprecated
    public boolean isConfiguredHeadless() {
        return headlessMode;
    }

    /**
     * Enables or disables debug logging for headless detection.
     *
     * @param enabled true to enable debug logging
     * @deprecated Debug logging is now controlled via brobot.headless.debug property
     */
    @Deprecated
    public void setDebugLogging(boolean enabled) {
        // This method is kept for backward compatibility but does nothing
        // Debug logging is now controlled via properties
    }

    /**
     * Forces a refresh of the cached headless state.
     *
     * @deprecated No longer needed as headless state is determined at startup
     */
    @Deprecated
    public void refreshCache() {
        // This method is kept for backward compatibility but does nothing
        // Headless state is now determined at startup via properties
    }

    /**
     * Returns whether ForceNonHeadlessInitializer had to force the mode.
     *
     * @return true if the initializer had to override headless settings
     */
    public boolean wasForcedNonHeadless() {
        return forcedMode;
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
                .append(headlessMode ? "HEADLESS" : "GUI AVAILABLE")
                .append("\n");
        report.append("  Configuration Source: ")
                .append("application properties (brobot.headless)")
                .append("\n");
        report.append("  java.awt.headless: ")
                .append(System.getProperty("java.awt.headless"))
                .append("\n");

        // Don't check GraphicsEnvironment.isHeadless() as it triggers initialization
        report.append("  GraphicsEnvironment check: SKIPPED (to avoid early initialization)\n");
        report.append("  OS: ").append(System.getProperty("os.name")).append("\n");
        return report.toString();
    }
}
