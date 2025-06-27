package io.github.jspinak.brobot.tools.logging.ansi;

import io.github.jspinak.brobot.tools.logging.MessageFormatter;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.AnsiConsole;

/**
 * ANSI escape code constants for colored terminal output in Brobot reporting.
 * 
 * <p>AnsiColor provides a comprehensive set of escape sequences for formatting console output 
 * with colors, styles, and backgrounds. This enables the framework to produce visually 
 * distinctive reports, highlight important information, and improve readability of 
 * automation logs in terminal environments that support ANSI codes.</p>
 * 
 * <p>Color categories:
 * <ul>
 *   <li><b>Regular Colors</b>: Standard 8-color palette for normal text</li>
 *   <li><b>Bold Colors</b>: Emphasized versions with increased brightness</li>
 *   <li><b>Underlined Colors</b>: Text with underline decoration</li>
 *   <li><b>Background Colors</b>: Colored backgrounds for highlighting</li>
 *   <li><b>Bright Colors</b>: High-intensity variants for better visibility</li>
 *   <li><b>Bold Bright Colors</b>: Maximum emphasis combining bold and bright</li>
 * </ul>
 * </p>
 * 
 * <p>Common usage patterns:
 * <ul>
 *   <li>GREEN for successful operations and passed tests</li>
 *   <li>RED for errors, failures, and warnings</li>
 *   <li>YELLOW for important notices and state changes</li>
 *   <li>BLUE for informational messages and links</li>
 *   <li>CYAN for debug information and timestamps</li>
 *   <li>PURPLE for special states or unique identifiers</li>
 * </ul>
 * </p>
 * 
 * <p>Terminal compatibility:
 * <ul>
 *   <li>Works in most Unix/Linux terminals</li>
 *   <li>Supported in macOS Terminal and iTerm</li>
 *   <li>Windows Terminal and PowerShell (Windows 10+)</li>
 *   <li>VS Code integrated terminal</li>
 *   <li>IntelliJ IDEA console</li>
 *   <li>Ignored in environments without ANSI support</li>
 * </ul>
 * </p>
 * 
 * <p>Example usage in reports:
 * <pre>
 * System.out.println(ANSI.GREEN + "✓ Test passed" + ANSI.RESET);
 * System.out.println(ANSI.RED_BOLD + "✗ Error: " + ANSI.RESET + message);
 * System.out.println(ANSI.YELLOW_BACKGROUND + " WARNING " + ANSI.RESET);
 * </pre>
 * </p>
 * 
 * <p>Best practices:
 * <ul>
 *   <li>Always end colored sections with RESET to avoid color bleeding</li>
 *   <li>Use bold variants for headers and critical information</li>
 *   <li>Combine foreground and background for maximum visibility</li>
 *   <li>Test output in target terminal environments</li>
 *   <li>Provide non-colored alternatives for accessibility</li>
 * </ul>
 * </p>
 * 
 * <p>Accessibility considerations:
 * <ul>
 *   <li>Not all terminals support all color combinations</li>
 *   <li>Some users may have color blindness</li>
 *   <li>Screen readers ignore ANSI codes</li>
 *   <li>Consider providing plain-text logs as alternative</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ANSI colors help distinguish different types of 
 * automation events, making logs easier to scan and debug. The visual differentiation 
 * is particularly valuable when monitoring real-time automation execution or analyzing 
 * large log files for specific events or errors.</p>
 * 
 * @since 1.0
 * @see ConsoleReporter
 * @see MessageFormatter
 * @see AnsiConsole
 */
public class AnsiColor {
    
    // === RESET SEQUENCE ===
    /**
     * Resets all text attributes to terminal defaults.
     * Must be used after any color/style to prevent bleeding.
     */
    public static final String RESET = "\033[0m";  // Text Reset

    // === REGULAR COLORS (NORMAL INTENSITY) ===
    /** Black text - rarely visible on dark terminals */
    public static final String BLACK = "\033[0;30m";   // BLACK
    /** Red text - commonly used for errors and failures */
    public static final String RED = "\033[0;31m";     // RED
    /** Green text - commonly used for success and passed tests */
    public static final String GREEN = "\033[0;32m";   // GREEN
    /** Yellow text - commonly used for warnings and notices */
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    /** Blue text - commonly used for information and links */
    public static final String BLUE = "\033[0;34m";    // BLUE
    /** Purple/Magenta text - commonly used for special states */
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    /** Cyan text - commonly used for debug info and timestamps */
    public static final String CYAN = "\033[0;36m";    // CYAN
    /** White text - default on dark terminals */
    public static final String WHITE = "\033[0;37m";   // WHITE

    // === BOLD COLORS (INCREASED WEIGHT) ===
    /** Bold black - more visible than regular black */
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    /** Bold red - emphasized errors and critical failures */
    public static final String RED_BOLD = "\033[1;31m";    // RED
    /** Bold green - emphasized success messages */
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    /** Bold yellow - emphasized warnings */
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    /** Bold blue - emphasized information */
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    /** Bold purple - emphasized special states */
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    /** Bold cyan - emphasized debug information */
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    /** Bold white - maximum contrast on dark backgrounds */
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // === UNDERLINED COLORS (WITH UNDERLINE DECORATION) ===
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // === BACKGROUND COLORS (NORMAL INTENSITY) ===
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // === HIGH INTENSITY FOREGROUND COLORS (BRIGHT VARIANTS) ===
    public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String RED_BRIGHT = "\033[0;91m";    // RED
    public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // === BOLD HIGH INTENSITY COLORS (MAXIMUM EMPHASIS) ===
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // === HIGH INTENSITY BACKGROUND COLORS (BRIGHT BACKGROUNDS) ===
    public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE

}
