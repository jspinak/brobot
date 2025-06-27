/**
 * Provides ANSI escape code utilities for enhanced terminal output formatting.
 * 
 * <p>This package contains utilities for creating visually distinctive console
 * output using ANSI escape codes. It enables the logging framework to produce
 * colored, styled, and formatted text that improves readability and helps
 * developers quickly identify different types of log messages.
 * 
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.ansi.AnsiColor} - Comprehensive
 *       collection of ANSI escape code constants for text styling</li>
 * </ul>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Standard foreground colors (BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE)</li>
 *   <li>Bright color variants for enhanced visibility</li>
 *   <li>Background color support for highlighting</li>
 *   <li>Text styling options (BOLD, UNDERLINED)</li>
 *   <li>Special effects (DIM, BLINK, REVERSE, HIDDEN)</li>
 *   <li>Reset sequences for restoring default formatting</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Creating colored console output
 * System.out.println(AnsiColor.RED + "Error: " + AnsiColor.RESET + message);
 * System.out.println(AnsiColor.GREEN_BOLD + "Success!" + AnsiColor.RESET);
 * System.out.println(AnsiColor.YELLOW_BACKGROUND + AnsiColor.BLACK + 
 *                    "Warning" + AnsiColor.RESET);
 * }</pre>
 * 
 * <h2>Compatibility Note</h2>
 * <p>ANSI escape codes are widely supported in Unix-like terminals (Linux, macOS)
 * and modern Windows terminals (Windows Terminal, PowerShell 7+). Legacy Windows
 * console may require additional configuration or third-party libraries for
 * proper ANSI support.
 * 
 * @see io.github.jspinak.brobot.tools.logging.ConsoleReporter
 * @see io.github.jspinak.brobot.tools.logging.AnsiConsole
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.logging.ansi;