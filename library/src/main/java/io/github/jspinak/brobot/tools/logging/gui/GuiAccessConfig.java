package io.github.jspinak.brobot.tools.logging.gui;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for GUI access monitoring and problem reporting.
 * 
 * <p>This configuration can be set via properties files using the prefix
 * "brobot.gui-access". For example:</p>
 * <pre>
 * brobot.gui-access.report-problems=true
 * brobot.gui-access.verbose-errors=true
 * brobot.gui-access.suggest-solutions=true
 * </pre>
 * 
 * @see GuiAccessMonitor for the implementation that uses this config
 */
@Data
@Component
@ConfigurationProperties(prefix = "brobot.gui-access")
public class GuiAccessConfig {
    
    /**
     * Whether to report GUI access problems to console/logs.
     * Default: true
     */
    private boolean reportProblems = true;
    
    /**
     * Whether to show verbose error details.
     * Default: true
     */
    private boolean verboseErrors = true;
    
    /**
     * Whether to suggest solutions for detected problems.
     * Default: true
     */
    private boolean suggestSolutions = true;
    
    /**
     * Whether to check GUI access on startup.
     * Default: true
     */
    private boolean checkOnStartup = true;
    
    /**
     * Whether to continue execution despite GUI problems.
     * Default: false (fail fast)
     */
    private boolean continueOnError = false;
    
    /**
     * Interval in seconds between GUI access checks (0 = no periodic checks).
     * Default: 0
     */
    private int checkInterval = 0;
    
    /**
     * Whether to log successful GUI checks.
     * Default: false (only log problems)
     */
    private boolean logSuccessfulChecks = false;
    
    /**
     * Whether to include platform-specific advice.
     * Default: true
     */
    private boolean platformSpecificAdvice = true;
    
    /**
     * Whether to check for screen recording permissions on macOS.
     * Default: true
     */
    private boolean checkMacPermissions = true;
    
    /**
     * Whether to warn about remote desktop sessions.
     * Default: true
     */
    private boolean warnRemoteDesktop = true;
    
    /**
     * Minimum required screen width.
     * Default: 800
     */
    private int minScreenWidth = 800;
    
    /**
     * Minimum required screen height.
     * Default: 600
     */
    private int minScreenHeight = 600;
}