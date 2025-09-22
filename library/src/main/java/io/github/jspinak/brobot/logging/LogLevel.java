package io.github.jspinak.brobot.logging;

/**
 * Standard log levels for the Brobot logging system.
 *
 * <p>Follows SLF4J/Log4j2 standards with clear semantics:
 * <ul>
 *   <li>OFF - No logging
 *   <li>ERROR - Error conditions that need immediate attention
 *   <li>WARN - Warning conditions, potential issues
 *   <li>INFO - Informational messages, key business events
 *   <li>DEBUG - Detailed information for debugging
 *   <li>TRACE - Most detailed information, method entry/exit
 * </ul>
 *
 * <p>The ordering is important for level comparison - lower ordinal values
 * indicate higher priority levels.
 */
public enum LogLevel {
    /** No logging */
    OFF,

    /** Error conditions that need immediate attention */
    ERROR,

    /** Warning conditions, potential issues */
    WARN,

    /** Informational messages, key business events */
    INFO,

    /** Detailed information for debugging */
    DEBUG,

    /** Most detailed information, method entry/exit */
    TRACE
}
