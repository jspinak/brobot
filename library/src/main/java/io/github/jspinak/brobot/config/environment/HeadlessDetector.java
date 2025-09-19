package io.github.jspinak.brobot.config.environment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Simplified headless detection for the Brobot framework.
 *
 * <p>Following the Brobot 1.0.7 pattern, this class no longer performs automatic headless
 * detection. Users must explicitly configure headless mode via properties.
 *
 * <p>Configuration via application.properties:
 *
 * <pre>{@code
 * # Explicitly set headless mode for tests/CI (defaults to false)
 * brobot.headless=false
 * }</pre>
 *
 * @since 2.0.0
 */
@Slf4j
@Component
public class HeadlessDetector {

    private final boolean headlessMode;

    /**
     * Constructor that simply uses the configured property value. No automatic detection - users
     * explicitly configure if needed.
     */
    public HeadlessDetector(@Value("${brobot.headless:false}") boolean brobotHeadless) {
        this.headlessMode = brobotHeadless;

        if (this.headlessMode) {
            log.info("[HeadlessDetector] Headless mode ENABLED via brobot.headless property");
        } else {
            log.debug("[HeadlessDetector] Headless mode DISABLED (default)");
        }
    }

    /**
     * Checks if the system is configured to run in headless mode.
     *
     * <p>This now simply returns the configured value, no runtime detection.
     *
     * @return true if configured as headless via properties, false otherwise
     */
    public boolean isHeadless() {
        return headlessMode;
    }

    /**
     * Gets a simple status report.
     *
     * @return status string
     */
    public String getStatusReport() {
        return headlessMode ? "Configured as HEADLESS" : "Configured as GUI mode";
    }
}
