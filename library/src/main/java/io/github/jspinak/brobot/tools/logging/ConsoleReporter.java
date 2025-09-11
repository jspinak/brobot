package io.github.jspinak.brobot.tools.logging;

import java.util.HashMap;
import java.util.Map;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.tools.logging.ansi.AnsiColor;

/**
 * Centralized reporting system with configurable output levels for the Brobot framework.
 *
 * <p>ConsoleReporter provides a unified interface for all console output throughout the framework,
 * enabling fine-grained control over verbosity levels. This centralized approach ensures consistent
 * formatting, allows dynamic adjustment of output detail, and facilitates debugging by controlling
 * information flow without code changes.
 *
 * <p>Output level hierarchy:
 *
 * <ul>
 *   <li><b>NONE (0)</b>: Suppresses all output for silent operation
 *   <li><b>LOW (1)</b>: Essential information only - action names and targets
 *   <li><b>HIGH (2)</b>: Detailed information including match coordinates and parameters
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Level-based Filtering</b>: Output only appears if it meets the minimum level
 *   <li><b>Structured Formatting</b>: Consistent format for different types of information
 *   <li><b>Color Support</b>: Integration with ANSI color codes for enhanced readability
 *   <li><b>Overloaded Methods</b>: Multiple signatures for different reporting scenarios
 *   <li><b>Match Reporting</b>: Specialized methods for reporting match results
 * </ul>
 *
 * <p>Common usage patterns:
 *
 * <ul>
 *   <li>Action execution: Reports what action is being performed on which element
 *   <li>Match results: Shows found elements with coordinates at HIGH level
 *   <li>State transitions: Tracks navigation through the state graph
 *   <li>Error conditions: Highlights problems with color coding
 *   <li>Debug information: Detailed data shown only at HIGH level
 * </ul>
 *
 * <p>Configuration:
 *
 * <ul>
 *   <li>Set {@code outputLevel} to control global verbosity
 *   <li>Use level-specific methods to ensure appropriate visibility
 *   <li>{@code MaxMockMatchesFindAll} limits match output in mock mode
 * </ul>
 *
 * <p>In the model-based approach, ConsoleReporter serves as the framework's communication channel
 * with developers and operators. By providing graduated levels of detail, it enables efficient
 * debugging during development while supporting quiet operation in production. The consistent
 * formatting makes logs parseable for automated analysis.
 *
 * @since 1.0
 * @see MessageFormatter
 * @see AnsiColor
 * @see OutputLevel
 */
public class ConsoleReporter {

    /**
     * Output verbosity levels for controlling report detail.
     *
     * <p>Levels are hierarchical - setting a higher level includes all lower levels:
     *
     * <ul>
     *   <li>{@code NONE} - No output, silent operation
     *   <li>{@code LOW} - Essential information only
     *   <li>{@code HIGH} - Detailed information including coordinates
     * </ul>
     */
    public enum OutputLevel {
        /** Suppresses all output for silent operation */
        NONE,
        /** Essential information only - action names and targets */
        LOW,
        /** Detailed information including match coordinates and parameters */
        HIGH
    }

    /**
     * Maps output levels to numeric values for comparison. Thread-safe due to initialization in
     * static block.
     */
    public static Map<OutputLevel, Integer> outputLevels = new HashMap<>();

    static {
        outputLevels.put(OutputLevel.NONE, 0);
        outputLevels.put(OutputLevel.LOW, 1);
        outputLevels.put(OutputLevel.HIGH, 2);
    }

    /**
     * Current global output level for all ConsoleReporter methods. Default is HIGH for maximum
     * verbosity during development. Change to LOW or NONE for production environments.
     */
    public static OutputLevel outputLevel = OutputLevel.HIGH;

    /**
     * Maximum number of matches to display when using FindAll in mock mode. Prevents console
     * flooding when many matches are found.
     */
    public static int MaxMockMatchesFindAll = 10;

    /**
     * Static instance of BrobotLogger for unified logging. This is set by Spring during application
     * initialization.
     */
    private static BrobotLogger brobotLogger;

    /**
     * Sets the BrobotLogger instance to use for unified logging. This is called during Spring
     * initialization.
     *
     * @param logger the BrobotLogger instance
     */
    public static void setBrobotLogger(BrobotLogger logger) {
        brobotLogger = logger;
    }

    /**
     * Checks if the specified level meets the minimum reporting threshold.
     *
     * @param level the output level to check
     * @return true if messages at this level should be displayed
     */
    public static boolean minReportingLevel(OutputLevel level) {
        return outputLevels.get(level) <= outputLevels.get(outputLevel);
    }

    // Removed ActionOptions-based print method - use ActionConfig version instead

    // Removed ActionOptions-based print method - use ActionConfig version instead

    /**
     * Reports an action performed on a StateObject with match details using modern ActionConfig.
     *
     * @param match the match result from the action
     * @param stateObject the state object being acted upon
     * @param actionConfig the action configuration including action type
     * @return always returns true for chaining
     */
    public static boolean print(Match match, StateObject stateObject, ActionConfig actionConfig) {
        String actionType = getActionTypeFromConfig(actionConfig);
        return print(match, stateObject.getName(), actionType);
    }

    /**
     * Helper method to extract action type from ActionConfig based on its class name.
     *
     * @param actionConfig the configuration to extract action type from
     * @return string representation of the action type
     */
    private static String getActionTypeFromConfig(ActionConfig actionConfig) {
        String className = actionConfig.getClass().getSimpleName();

        // Map config class names to action types
        if (className.contains("Click")) return "CLICK";
        if (className.contains("Find") || className.contains("Pattern")) return "FIND";
        if (className.contains("Type")) return "TYPE";
        if (className.contains("Drag")) return "DRAG";
        if (className.contains("Move") || className.contains("Mouse")) return "MOVE";
        if (className.contains("Highlight")) return "HIGHLIGHT";
        if (className.contains("Define")) return "DEFINE";
        if (className.contains("Vanish")) return "VANISH";
        if (className.contains("Scroll")) return "SCROLL";
        if (className.contains("KeyDown")) return "KEY_DOWN";
        if (className.contains("KeyUp")) return "KEY_UP";

        // Default to class name if no mapping found
        return className.replace("Options", "").toUpperCase();
    }

    /**
     * Reports an action performed on a StateObjectData with match details using modern
     * ActionConfig.
     *
     * @param match the match result from the action
     * @param stateObject the state object data being acted upon
     * @param actionConfig the action configuration including action type
     * @return always returns true for chaining
     */
    public static boolean print(
            Match match, StateObjectMetadata stateObject, ActionConfig actionConfig) {
        String actionType = getActionTypeFromConfig(actionConfig);
        return print(match, stateObject.getStateObjectName(), actionType);
    }

    /**
     * Core method for reporting actions with level-based detail.
     *
     * <p>Output format varies by level:
     *
     * <ul>
     *   <li>LOW: "ACTION: objectName "
     *   <li>HIGH: "ACTION: objectName, match=[x,y,w,h] "
     * </ul>
     *
     * @param match the match result containing coordinates
     * @param stateObjectName name of the object being acted upon
     * @param action the action being performed (e.g., "CLICK", "TYPE")
     * @return always returns true for chaining
     */
    public static boolean print(Match match, String stateObjectName, String action) {
        if (brobotLogger != null) {
            // Use unified logging system
            if (minReportingLevel(OutputLevel.LOW)) {
                var logBuilder =
                        brobotLogger
                                .log()
                                .type(LogEvent.Type.ACTION)
                                .observation(String.format("%s: %s", action, stateObjectName))
                                .metadata("action", action)
                                .metadata("target", stateObjectName);

                if (minReportingLevel(OutputLevel.HIGH) && match != null) {
                    logBuilder
                            .metadata("match", match.toString())
                            .metadata("matchX", match.x())
                            .metadata("matchY", match.y())
                            .metadata("matchW", match.w())
                            .metadata("matchH", match.h());
                }

                logBuilder.log();
            }
        } else {
            // Fallback to direct console output
            if (minReportingLevel(OutputLevel.LOW))
                System.out.format("%s: %s ", action, stateObjectName);
            if (minReportingLevel(OutputLevel.HIGH))
                System.out.format("%s: %s, match=%s ", action, stateObjectName, match.toString());
        }
        return true;
    }

    /**
     * Prints a string at HIGH output level.
     *
     * @param str the string to print
     * @return true if printed, false if suppressed by output level
     */
    public static boolean print(String str) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;

        if (brobotLogger != null) {
            brobotLogger.log().observation(str).level(LogEvent.Level.DEBUG).log();
        } else {
            System.out.format("%s", str);
        }
        return true;
    }

    /**
     * Prints a string at the specified output level.
     *
     * @param outputLevel the minimum level required to display this message
     * @param str the string to print
     * @return true if printed, false if suppressed by output level
     */
    public static boolean print(OutputLevel outputLevel, String str) {
        if (!minReportingLevel(outputLevel)) return false;
        System.out.format("%s", str);
        return true;
    }

    /**
     * Prints a colored string at the specified output level.
     *
     * @param outputLevel the minimum level required to display this message
     * @param str the string to print
     * @param colors ANSI color codes to apply
     * @return true if printed, false if suppressed by output level
     */
    public static boolean print(OutputLevel outputLevel, String str, String... colors) {
        if (!minReportingLevel(outputLevel)) return false;
        return print(str, colors);
    }

    /**
     * Prints a colored string at HIGH output level.
     *
     * @param str the string to print
     * @param colors ANSI color codes to apply
     * @return true if printed, false if suppressed by output level
     */
    public static boolean print(String str, String... colors) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        MessageFormatter.printColor(str, colors);
        return true;
    }

    /**
     * Prints a blank line at HIGH output level.
     *
     * @return true if printed, false if suppressed by output level
     */
    public static boolean println() {
        return println("");
    }

    /**
     * Prints a string with newline at HIGH output level.
     *
     * @param str the string to print
     * @return true if printed, false if suppressed by output level
     */
    public static boolean println(String str) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;

        if (brobotLogger != null) {
            brobotLogger.log().observation(str).level(LogEvent.Level.DEBUG).log();
        } else {
            System.out.println(str);
        }
        return true;
    }

    /**
     * Prints a colored string with newline at HIGH output level.
     *
     * @param str the string to print
     * @param colors ANSI color codes to apply
     * @return true if printed, false if suppressed by output level
     */
    public static boolean println(String str, String... colors) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        MessageFormatter.printColorLn(str, colors);
        return true;
    }

    /**
     * Prints a string with newline at the specified output level.
     *
     * @param outputLevel the minimum level required to display this message
     * @param str the string to print
     * @return true if printed, false if suppressed by output level
     */
    public static boolean println(OutputLevel outputLevel, String str) {
        if (!minReportingLevel(outputLevel)) return false;

        if (brobotLogger != null) {
            LogEvent.Level level =
                    outputLevel == OutputLevel.LOW ? LogEvent.Level.INFO : LogEvent.Level.DEBUG;
            brobotLogger.log().observation(str).level(level).log();
        } else {
            System.out.println(str);
        }
        return true;
    }

    /**
     * Formats and prints a string at HIGH output level.
     *
     * @param format format string as per {@link String#format(String, Object...)}
     * @param args arguments referenced by format specifiers
     * @return true if printed, false if suppressed by output level
     */
    public static boolean format(String format, Object... args) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        System.out.format(format, args);
        return true;
    }

    /**
     * Formats and prints a string with newline at HIGH output level.
     *
     * @param format format string as per {@link String#format(String, Object...)}
     * @param args arguments referenced by format specifiers
     * @return true if printed, false if suppressed by output level
     */
    public static boolean formatln(String format, Object... args) {
        if (!format(format, args)) return false;
        System.out.println();
        return true;
    }

    /**
     * Formats and prints a string at the specified output level.
     *
     * @param outputLevel the minimum level required to display this message
     * @param format format string as per {@link String#format(String, Object...)}
     * @param args arguments referenced by format specifiers
     * @return true if printed, false if suppressed by output level
     */
    public static boolean format(OutputLevel outputLevel, String format, Object... args) {
        if (!minReportingLevel(outputLevel)) return false;
        System.out.format(format, args);
        return true;
    }
}
