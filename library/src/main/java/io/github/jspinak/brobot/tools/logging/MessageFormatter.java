package io.github.jspinak.brobot.tools.logging;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.logging.ansi.AnsiColor;

/**
 * Core output utility for colored console messages in the Brobot logging system.
 *
 * <p>MessageFormatter provides fundamental methods for printing colored text to the console,
 * serving as the foundation for the framework's visual feedback system. It handles ANSI color code
 * application and proper reset sequences to ensure clean console output across different terminal
 * environments.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Color Support</b>: Accepts multiple ANSI color codes for flexible formatting
 *   <li><b>Automatic Reset</b>: Always appends ANSI.RESET to prevent color bleeding
 *   <li><b>Unicode Characters</b>: Provides check (✓) and fail (✘) symbols for status indication
 *   <li><b>Thread-Safe</b>: Static methods use synchronized System.out for concurrent access
 * </ul>
 *
 * <p>Usage patterns:
 *
 * <ul>
 *   <li>Direct console output for immediate feedback
 *   <li>Building block for higher-level reporting classes
 *   <li>Status indicators using check/fail symbols
 *   <li>Multi-color output by combining color codes
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>
 * // Single color output
 * MessageFormatter.printColorLn("Success!", ANSI.GREEN);
 *
 * // Multiple colors (e.g., background + foreground)
 * MessageFormatter.printColor("Error: ", ANSI.RED_BACKGROUND, ANSI.WHITE_BOLD);
 *
 * // Using status symbols
 * MessageFormatter.printColorLn(MessageFormatter.check + " Test passed", ANSI.GREEN);
 * MessageFormatter.printColorLn(MessageFormatter.fail + " Test failed", ANSI.RED);
 * </pre>
 *
 * <p>Integration notes:
 *
 * <ul>
 *   <li>Used by {@link ConsoleReporter} for level-based output control
 *   <li>Used by {@link AnsiConsole} for test result formatting
 *   <li>Relies on {@link AnsiColor} for color code constants
 *   <li>Spring component for dependency injection
 * </ul>
 *
 * <p>Thread safety: The static methods use System.out which is synchronized, making them safe for
 * concurrent use. However, multiple calls may interleave in multi-threaded environments.
 *
 * @since 1.0
 * @see ConsoleReporter
 * @see AnsiConsole
 * @see AnsiColor
 */
@Component
public class MessageFormatter {

    /** Unicode check mark symbol (✓) for indicating success or positive status. UTF-8: U+2713 */
    public static String check = "✓"; // '\u2713';

    /** Unicode cross mark symbol (✘) for indicating failure or negative status. UTF-8: U+2718 */
    public static String fail = "✘"; // '\u2718';

    /**
     * Prints a message to the console with specified ANSI color codes.
     *
     * <p>This method applies one or more ANSI color codes to the message text, automatically
     * appending ANSI.RESET to prevent color bleeding into subsequent output. Multiple color codes
     * can be combined for effects like colored backgrounds with bold text.
     *
     * @param message the text to print (must not be null)
     * @param colors varargs array of ANSI color code strings from {@link AnsiColor} (e.g.,
     *     ANSI.RED, ANSI.BOLD, ANSI.YELLOW_BACKGROUND)
     * @see AnsiColor
     */
    public static void printColor(String message, String... colors) {
        // System.out.print("| ");
        Arrays.stream(colors).forEach(System.out::print);
        System.out.print(message + AnsiColor.RESET);
    }

    /**
     * Prints a message to the console with specified ANSI color codes, followed by a newline.
     *
     * <p>Convenience method that calls {@link #printColor(String, String...)} and adds a line
     * break. This is the most commonly used method for colored console output in the framework.
     *
     * @param message the text to print (must not be null)
     * @param colors varargs array of ANSI color code strings from {@link AnsiColor}
     * @see #printColor(String, String...)
     * @see AnsiColor
     */
    public static void printColorLn(String message, String... colors) {
        printColor(message, colors);
        System.out.println();
    }
}
