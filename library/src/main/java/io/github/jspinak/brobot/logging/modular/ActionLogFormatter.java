package io.github.jspinak.brobot.logging.modular;

import io.github.jspinak.brobot.action.ActionResult;

/**
 * Interface for formatting ActionResult data into log messages. Each implementation handles a
 * specific verbosity level (QUIET, NORMAL, VERBOSE).
 *
 * <p>Formatters are responsible for:
 *
 * <ul>
 *   <li>Determining if an action should be logged at their verbosity level
 *   <li>Extracting relevant data from ActionResult
 *   <li>Formatting the data into appropriate string representation
 *   <li>Handling edge cases (missing data, null values, etc.)
 * </ul>
 *
 * <p>Design principles:
 *
 * <ul>
 *   <li>Formatters are stateless - can be safely used by multiple threads
 *   <li>Formatters only read data - never modify ActionResult
 *   <li>Each formatter is responsible for one verbosity level only
 *   <li>Null return values indicate the action should not be logged
 * </ul>
 */
public interface ActionLogFormatter {

    /**
     * Format an ActionResult into a log message string.
     *
     * @param actionResult the result of an action execution
     * @return formatted log message, or null if this action should not be logged
     */
    String format(ActionResult actionResult);

    /**
     * Determine if an action should be logged at this verbosity level.
     *
     * @param actionResult the result to check
     * @return true if this action should produce log output
     */
    boolean shouldLog(ActionResult actionResult);

    /**
     * Get the verbosity level this formatter handles.
     *
     * @return the verbosity level
     */
    VerbosityLevel getVerbosityLevel();

    /** Verbosity levels supported by the logging system. */
    enum VerbosityLevel {
        QUIET, // Minimal output: ✗ Find Working.ClaudeIcon • 234ms
        NORMAL, // Balanced output: timestamps, key info, success indicators
        VERBOSE // Detailed output: full metadata, environment info, timing details
    }
}
