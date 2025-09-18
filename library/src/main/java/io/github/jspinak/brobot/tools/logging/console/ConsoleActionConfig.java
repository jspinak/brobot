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

    /** Whether console action reporting is enabled. */
    private boolean enabled;

    /** The verbosity level for console output. */
    private Level level;

    /**
     * Whether to show detailed match information (location, score). Only applies when level is
     * VERBOSE.
     */
    private boolean showMatchDetails;

    /** Whether to show timing information for actions. */
    private boolean showTiming;

    /** Whether to use colored output (requires ANSI support). */
    private boolean useColors;

    /** Whether to use unicode icons in output. */
    private boolean useIcons;

    // Action type filters

    /** Whether to report FIND actions. */
    private boolean reportFind;

    /** Whether to report CLICK actions. */
    private boolean reportClick;

    /** Whether to report TYPE actions. */
    private boolean reportType;

    /** Whether to report DRAG actions. */
    private boolean reportDrag;

    /** Whether to report HIGHLIGHT actions. */
    private boolean reportHighlight;

    /** Whether to report state transitions. */
    private boolean reportTransitions;

    // Performance thresholds

    /**
     * Threshold in milliseconds for performance warnings. Actions taking longer than this will
     * trigger a warning.
     */
    private long performanceWarnThreshold;

    /**
     * Threshold in milliseconds for performance errors. Actions taking longer than this will be
     * highlighted as errors.
     */
    private long performanceErrorThreshold;

    // Output formatting

    /** Prefix for indented output (e.g., match details). */
    private String indentPrefix;

    /** Maximum length for displayed text before truncation. */
    private int maxTextLength;

    /** Whether to group related actions together. */
    private boolean groupRelatedActions;

    /** Time window in milliseconds for grouping related actions. */
    private long groupingTimeWindow;
}
