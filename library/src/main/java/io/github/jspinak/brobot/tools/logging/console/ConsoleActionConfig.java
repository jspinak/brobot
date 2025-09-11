package io.github.jspinak.brobot.tools.logging.console;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration for console action reporting. Controls how actions are displayed in the console
 * during automation execution.
 *
 * <p>This configuration can be set via properties files using the prefix "brobot.console.actions".
 * For example:
 *
 * <pre>
 * brobot.console.actions.enabled=true
 * brobot.console.actions.level=VERBOSE
 * brobot.console.actions.show-match-details=true
 * </pre>
 *
 * <p><b>Important Note:</b> This class uses {@code @ConfigurationProperties} which automatically
 * creates a Spring bean when used with {@code @EnableConfigurationProperties}. DO NOT add
 * {@code @Component} or {@code @Configuration} annotations to this class as it will create
 * duplicate beans and cause conflicts. The bean is created by the framework when {@link
 * io.github.jspinak.brobot.config.logging.ActionLoggingConfig} includes this class in its
 * {@code @EnableConfigurationProperties} annotation.
 *
 * @see ConsoleActionReporter for the implementation that uses this config
 * @see io.github.jspinak.brobot.config.logging.ActionLoggingConfig for the configuration that
 *     enables this properties class
 */
@Data
@ConfigurationProperties(prefix = "brobot.console.actions")
public class ConsoleActionConfig {

    /** Verbosity levels for console output. */
    public enum Level {
        /** No console output */
        QUIET,
        /** Minimal output - success/failure only */
        NORMAL,
        /** Detailed output with timing and match information */
        VERBOSE
    }

    /** Whether console action reporting is enabled. Default: true */
    private boolean enabled = true;

    /** The verbosity level for console output. Default: NORMAL */
    private Level level = Level.NORMAL;

    /**
     * Whether to show detailed match information (location, score). Only applies when level is
     * VERBOSE. Default: true
     */
    private boolean showMatchDetails = true;

    /** Whether to show timing information for actions. Default: true */
    private boolean showTiming = true;

    /** Whether to use colored output (requires ANSI support). Default: true */
    private boolean useColors = true;

    /** Whether to use unicode icons in output. Default: true */
    private boolean useIcons = true;

    // Action type filters

    /** Whether to report FIND actions. Default: true */
    private boolean reportFind = true;

    /** Whether to report CLICK actions. Default: true */
    private boolean reportClick = true;

    /** Whether to report TYPE actions. Default: true */
    private boolean reportType = true;

    /** Whether to report DRAG actions. Default: true */
    private boolean reportDrag = true;

    /** Whether to report HIGHLIGHT actions. Default: false */
    private boolean reportHighlight = false;

    /** Whether to report state transitions. Default: true */
    private boolean reportTransitions = true;

    // Performance thresholds

    /**
     * Threshold in milliseconds for performance warnings. Actions taking longer than this will
     * trigger a warning. Default: 1000ms
     */
    private long performanceWarnThreshold = 1000;

    /**
     * Threshold in milliseconds for performance errors. Actions taking longer than this will be
     * highlighted as errors. Default: 5000ms
     */
    private long performanceErrorThreshold = 5000;

    // Output formatting

    /** Prefix for indented output (e.g., match details). Default: " └─ " */
    private String indentPrefix = "   └─ ";

    /** Maximum length for displayed text before truncation. Default: 50 */
    private int maxTextLength = 50;

    /** Whether to group related actions together. Default: false */
    private boolean groupRelatedActions = false;

    /** Time window in milliseconds for grouping related actions. Default: 100ms */
    private long groupingTimeWindow = 100;
}
