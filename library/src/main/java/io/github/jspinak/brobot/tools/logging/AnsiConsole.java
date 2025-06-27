package io.github.jspinak.brobot.tools.logging;

import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.tools.logging.ansi.AnsiColor;

/**
 * Specialized test output utility for visual assertion feedback in Brobot automation.
 * 
 * <p>AnsiConsole provides assertion-style methods with colored console output to visually
 * indicate test results during automation execution. Unlike traditional unit test assertions
 * that throw exceptions, these methods return boolean results while providing immediate
 * visual feedback through color-coded output.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Visual Assertions</b>: Green for passed, red for failed comparisons</li>
 *   <li><b>Multi-type Support</b>: Overloaded methods for int, double, boolean, String, StateEnum</li>
 *   <li><b>Pair-wise Comparison</b>: Expected values followed by actual values</li>
 *   <li><b>Inline Results</b>: Compact output format for real-time monitoring</li>
 *   <li><b>Non-throwing</b>: Returns boolean instead of throwing exceptions</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases:
 * <ul>
 *   <li><b>Live Testing</b>: Monitor automation execution with visual feedback</li>
 *   <li><b>Mock Verification</b>: Validate mock behavior with colored output</li>
 *   <li><b>Companion Apps</b>: Watch screens and report expected/unexpected events</li>
 *   <li><b>Debug Mode</b>: Quick visual verification during development</li>
 * </ul>
 * </p>
 * 
 * <p>Output format:
 * <pre>
 * // For matching values (green):
 * expected:actual expected:actual ✓ Test message
 * 
 * // For non-matching values (red for mismatches):
 * expected:actual expected:actual ✗ Test message
 * 
 * // Error cases (yellow):
 * no values to compare
 * odd number of comparison values
 * </pre>
 * </p>
 * 
 * <p>Example usage:
 * <pre>
 * // Compare integers (expected, actual pairs)
 * AnsiConsole.assertTrue("Button count", 3, buttonList.size(), 5, activeButtons);
 * 
 * // Compare states
 * AnsiConsole.assertTrue("State transition", StateEnum.HOME, currentState);
 * 
 * // Multiple comparisons in one call
 * AnsiConsole.assertTrue("Coordinates", 
 *     100, match.x(),     // expected x, actual x
 *     200, match.y(),     // expected y, actual y  
 *     50, match.w(),      // expected width, actual width
 *     75, match.h()       // expected height, actual height
 * );
 * </pre>
 * </p>
 * 
 * <p>Best practices:
 * <ul>
 *   <li>Disable other console output when using live testing to avoid clutter</li>
 *   <li>Group related assertions with descriptive messages</li>
 *   <li>Use for visual monitoring, not critical test infrastructure</li>
 *   <li>Consider terminal width when formatting messages</li>
 * </ul>
 * </p>
 * 
 * <p>Thread safety: Methods are not synchronized. In multi-threaded environments,
 * output may interleave. Consider external synchronization if needed.</p>
 * 
 * @since 1.0
 * @see MessageFormatter
 * @see AnsiColor
 * @see ConsoleReporter
 */
public class AnsiConsole {

    /**
     * Prints a single value comparison with color-coded result.
     * 
     * <p>Format: value1:value2 (where value2 is colored based on match result)</p>
     * 
     * @param value1 the expected value
     * @param value2 the actual value
     * @param color  ANSI color code for the actual value (typically GREEN or RED)
     */
    public static void printValueComparison(String value1, String value2, String color) {
        System.out.print(value1 + ":" + color + value2 + " " + AnsiColor.RESET);
    }

    /**
     * Validates the input array for assertions and reports errors.
     * 
     * @param size the number of values to compare
     * @return true if there's an error (empty array or odd number of values), false otherwise
     */
    private static boolean assertError(int size) {
        if (size == 0) {
            MessageFormatter.printColorLn("no values to compare", AnsiColor.YELLOW);
            return true;
        }
        if (size % 2 == 1) {
            MessageFormatter.printColorLn("odd number of comparison values", AnsiColor.YELLOW);
            return true;
        }
        return false;
    }

    /**
     * Asserts that integer values match their expected values with visual feedback.
     * 
     * <p>Values must be provided in expected/actual pairs. Each pair is compared,
     * with green output for matches and red for mismatches.</p>
     * 
     * @param message         descriptive message about what is being tested
     * @param valuesToCompare pairs of expected and actual integer values
     * @return true if all actual values match their expected values
     * @throws IllegalArgumentException if odd number of values provided
     */
    public static boolean assertTrue(String message, int... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = Integer.toString(valuesToCompare[i]);
        return assertTrue(message, values);
    }

    /**
     * Asserts that double values match their expected values with visual feedback.
     * 
     * <p>Values must be provided in expected/actual pairs. Note: Uses exact equality
     * comparison, consider using a tolerance-based comparison for floating point values.</p>
     * 
     * @param message         descriptive message about what is being tested
     * @param valuesToCompare pairs of expected and actual double values
     * @return true if all actual values match their expected values
     */
    public static boolean assertTrue(String message, double... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = Double.toString(valuesToCompare[i]);
        return assertTrue(message, values);
    }

    /**
     * Asserts that boolean values match their expected values with visual feedback.
     * 
     * @param message         descriptive message about what is being tested
     * @param valuesToCompare pairs of expected and actual boolean values
     * @return true if all actual values match their expected values
     */
    public static boolean assertTrue(String message, boolean... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = Boolean.toString(valuesToCompare[i]);
        return assertTrue(message, values);
    }

    /**
     * Asserts that StateEnum values match their expected values with visual feedback.
     * 
     * <p>Useful for validating state transitions in the automation framework.</p>
     * 
     * @param message         descriptive message about what is being tested
     * @param valuesToCompare pairs of expected and actual StateEnum values
     * @return true if all actual states match their expected states
     * @see StateEnum
     */
    public static boolean assertTrue(String message, StateEnum... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = valuesToCompare[i].toString();
        return assertTrue(message, values);
    }

    /**
     * Core assertion method that compares string values with visual feedback.
     * 
     * <p>This is the base implementation used by all type-specific assertion methods.
     * Compares values in expected/actual pairs, printing each comparison with
     * appropriate color coding. The final message is colored based on overall result.</p>
     * 
     * <p>Output format:
     * <pre>
     * expected1:actual1 expected2:actual2 Message
     * </pre>
     * Where mismatches are shown in red and matches in green.</p>
     * 
     * @param message         descriptive message about what is being tested
     * @param valuesToCompare pairs of expected and actual string values
     * @return true if all actual values match their expected values
     */
    public static boolean assertTrue(String message, String... valuesToCompare) {
        if (assertError(valuesToCompare.length)) return false;
        String color = AnsiColor.GREEN;
        int i = 0;
        while (i < valuesToCompare.length) {
            if (!valuesToCompare[i].equals(valuesToCompare[i+1])) {
                color = AnsiColor.RED;
                printValueComparison(valuesToCompare[i], valuesToCompare[i+1], AnsiColor.RED);
            } else {
                printValueComparison(valuesToCompare[i], valuesToCompare[i + 1], AnsiColor.GREEN);
            }
            i += 2;
        }
        MessageFormatter.printColorLn(message, color);
        return color.equals(AnsiColor.GREEN);
    }
}
