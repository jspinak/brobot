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

    public HeadlessDetector(
            @Value("${brobot.headless:false}") boolean brobotHeadless,
            @Value("${brobot.headless.debug:false}") boolean debugEnabled) {

        // Only use the brobot.headless property - ignore java.awt.headless
        // because GraphicsEnvironment may already be initialized incorrectly
        this.headlessMode = brobotHeadless;

        if (this.headlessMode) {
            log.info("[HeadlessDetector] Headless mode ENABLED via brobot.headless property");
        } else {
            log.info("[HeadlessDetector] Headless mode DISABLED (GUI mode enabled)");
        }

        // Log warning if there's a mismatch with java.awt.headless
        String javaHeadlessProperty = System.getProperty("java.awt.headless");
        if ("true".equalsIgnoreCase(javaHeadlessProperty) && !this.headlessMode) {
            log.warn(
                    "[HeadlessDetector] WARNING: java.awt.headless is set to 'true' but "
                            + "brobot.headless is 'false'. This may cause issues. "
                            + "Please start with -Djava.awt.headless=false");
        }

        if (debugEnabled) {
            log.debug("[HeadlessDetector] Configuration:");
            log.debug("  brobot.headless: {}", brobotHeadless);
            log.debug("  java.awt.headless: {}", javaHeadlessProperty);
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
        report.append("  OS: ").append(System.getProperty("os.name")).append("\n");
        return report.toString();
    }
}
