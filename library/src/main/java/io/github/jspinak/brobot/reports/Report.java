package io.github.jspinak.brobot.reports;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;

import java.util.HashMap;
import java.util.Map;

/**
 * Prints to the console if the output meets the required reporting level.
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
