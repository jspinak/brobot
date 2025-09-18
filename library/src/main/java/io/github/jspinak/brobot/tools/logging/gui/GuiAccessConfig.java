package io.github.jspinak.brobot.tools.logging.gui;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration for GUI access monitoring and problem reporting.
 *
 * <p>This configuration can be set via properties files using the prefix "brobot.gui-access". For
 * example:
 *
 * <pre>
 * brobot.gui-access.report-problems=true
 * brobot.gui-access.verbose-errors=true
 * brobot.gui-access.suggest-solutions=true
 * </pre>
 *
 * @see GuiAccessMonitor for the implementation that uses this config
 */
@Data
@ConfigurationProperties(prefix = "brobot.gui-access")
public class GuiAccessConfig {

    /** Whether to report GUI access problems to console/logs. */
    private boolean reportProblems;

    /** Whether to show verbose error details. */
    private boolean verboseErrors;

    /** Whether to suggest solutions for detected problems. */
    private boolean suggestSolutions;

    /** Whether to check GUI access on startup. */
    private boolean checkOnStartup;

    /** Whether to continue execution despite GUI problems. */
    private boolean continueOnError;

    /** Interval in seconds between GUI access checks (0 = no periodic checks). */
    private int checkInterval;

    /** Whether to log successful GUI checks. */
    private boolean logSuccessfulChecks;

    /** Whether to include platform-specific advice. */
    private boolean platformSpecificAdvice;

    /** Whether to check for screen recording permissions on macOS. */
    private boolean checkMacPermissions;

    /** Whether to warn about remote desktop sessions. */
    private boolean warnRemoteDesktop;

    /** Minimum required screen width. */
    private int minScreenWidth;

    /** Minimum required screen height. */
    private int minScreenHeight;
}
