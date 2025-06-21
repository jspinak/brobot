package io.github.jspinak.brobot.report;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized reporting system with configurable output levels for the Brobot framework.
 * 
 * <p>Report provides a unified interface for all console output throughout the framework, 
 * enabling fine-grained control over verbosity levels. This centralized approach ensures 
 * consistent formatting, allows dynamic adjustment of output detail, and facilitates 
 * debugging by controlling information flow without code changes.</p>
 * 
 * <p>Output level hierarchy:
 * <ul>
 *   <li><b>NONE (0)</b>: Suppresses all output for silent operation</li>
 *   <li><b>LOW (1)</b>: Essential information only - action names and targets</li>
 *   <li><b>HIGH (2)</b>: Detailed information including match coordinates and parameters</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Level-based Filtering</b>: Output only appears if it meets the minimum level</li>
 *   <li><b>Structured Formatting</b>: Consistent format for different types of information</li>
 *   <li><b>Color Support</b>: Integration with ANSI color codes for enhanced readability</li>
 *   <li><b>Overloaded Methods</b>: Multiple signatures for different reporting scenarios</li>
 *   <li><b>Match Reporting</b>: Specialized methods for reporting match results</li>
 * </ul>
 * </p>
 * 
 * <p>Common usage patterns:
 * <ul>
 *   <li>Action execution: Reports what action is being performed on which element</li>
 *   <li>Match results: Shows found elements with coordinates at HIGH level</li>
 *   <li>State transitions: Tracks navigation through the state graph</li>
 *   <li>Error conditions: Highlights problems with color coding</li>
 *   <li>Debug information: Detailed data shown only at HIGH level</li>
 * </ul>
 * </p>
 * 
 * <p>Configuration:
 * <ul>
 *   <li>Set {@code outputLevel} to control global verbosity</li>
 *   <li>Use level-specific methods to ensure appropriate visibility</li>
 *   <li>{@code MaxMockMatchesFindAll} limits match output in mock mode</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Report serves as the framework's communication channel 
 * with developers and operators. By providing graduated levels of detail, it enables 
 * efficient debugging during development while supporting quiet operation in production. 
 * The consistent formatting makes logs parseable for automated analysis.</p>
 * 
 * @since 1.0
 * @see Output
 * @see ANSI
 * @see OutputLevel
 */
public class Report {

    public enum OutputLevel {
        NONE, LOW, HIGH
    }

    public static Map<OutputLevel, Integer> outputLevels = new HashMap<>();
    static {
        outputLevels.put(OutputLevel.NONE, 0);
        outputLevels.put(OutputLevel.LOW, 1);
        outputLevels.put(OutputLevel.HIGH, 2);
    }

    public static OutputLevel outputLevel = OutputLevel.HIGH;
    public static int MaxMockMatchesFindAll = 10;

    public static boolean minReportingLevel(OutputLevel level) {
        return outputLevels.get(level) <= outputLevels.get(outputLevel);
    }

    public static boolean print(Match match, StateObject stateObject, ActionOptions actionOptions) {
        return print(match, stateObject.getName(), actionOptions.getAction().toString());
    }

    public static boolean print(Match match, StateObjectData stateObject, ActionOptions actionOptions) {
        return print(match, stateObject.getStateObjectName(), actionOptions.getAction().toString());
    }

    public static boolean print(Match match, String stateObjectName, String action) {
        if (minReportingLevel(OutputLevel.LOW))
            System.out.format("%s: %s ", action, stateObjectName);
        if (minReportingLevel(OutputLevel.HIGH))
            System.out.format("%s: %s, match=%s ", action, stateObjectName, match.toString());
        return true;
    }

    public static boolean print(String str) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        System.out.format("%s", str);
        return true;
    }

    public static boolean print(OutputLevel outputLevel, String str) {
        if (!minReportingLevel(outputLevel)) return false;
        System.out.format("%s", str);
        return true;
    }

    public static boolean print(OutputLevel outputLevel, String str, String... colors) {
        if (!minReportingLevel(outputLevel)) return false;
        return print(str, colors);
    }

    public static boolean print(String str, String... colors) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        Output.printColor(str, colors);
        return true;
    }

    public static boolean println() {
        return println("");
    }

    public static boolean println(String str) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        System.out.println(str);
        return true;
    }

    public static boolean println(String str, String... colors) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        Output.printColorLn(str, colors);
        return true;
    }

    public static boolean println(OutputLevel outputLevel, String str) {
        if (!minReportingLevel(outputLevel)) return false;
        System.out.println(str);
        return true;
    }

    public static boolean format(String format, Object ... args) {
        if (!minReportingLevel(OutputLevel.HIGH)) return false;
        System.out.format(format, args);
        return true;
    }

    public static boolean formatln(String format, Object ... args) {
        if (!format(format, args)) return false;
        System.out.println();
        return true;
    }

    public static boolean format(OutputLevel outputLevel, String format, Object ... args) {
        if (!minReportingLevel(outputLevel)) return false;
        System.out.format(format, args);
        return true;
    }


}
