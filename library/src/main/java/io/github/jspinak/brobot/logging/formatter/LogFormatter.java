package io.github.jspinak.brobot.logging.formatter;

import io.github.jspinak.brobot.logging.LogEntry;

/**
 * Interface for formatting log entries into string output.
 *
 * <p>Log formatters convert LogEntry objects into formatted strings suitable for various output
 * destinations (console, file, network). Different formatters provide different levels of detail
 * and structure.
 *
 * <p>Implementations should be:
 *
 * <ul>
 *   <li>Thread-safe for concurrent use
 *   <li>Fast and efficient for high-volume logging
 *   <li>Consistent in their output format
 *   <li>Configurable for different needs
 * </ul>
 *
 * <p>Available formatters:
 *
 * <ul>
 *   <li>{@link SimpleLogFormatter} - Simple human-readable format
 *   <li>{@link StructuredLogFormatter} - Structured format with metadata
 *   <li>{@link JsonLogFormatter} - JSON format for machine processing
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * LogFormatter formatter = new SimpleLogFormatter();
 * String output = formatter.format(logEntry);
 * System.out.println(output);
 * }</pre>
 */
public interface LogFormatter {

    /**
     * Format a log entry into a string.
     *
     * @param entry The log entry to format
     * @return The formatted string representation
     */
    String format(LogEntry entry);

    /**
     * Get the name of this formatter for identification.
     *
     * @return The formatter name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Check if this formatter supports the given log entry. Most formatters support all entries,
     * but specialized formatters might only handle specific types.
     *
     * @param entry The log entry to check
     * @return true if this formatter can format the entry
     */
    default boolean supports(LogEntry entry) {
        return true;
    }

    /**
     * Get the expected output format type. Used for routing to appropriate output destinations.
     *
     * @return The format type
     */
    default FormatType getFormatType() {
        return FormatType.TEXT;
    }

    /** Output format types for different use cases. */
    enum FormatType {
        /** Plain text format for human reading */
        TEXT,
        /** Structured text with consistent field placement */
        STRUCTURED,
        /** JSON format for machine processing */
        JSON,
        /** XML format for enterprise systems */
        XML,
        /** Custom binary or specialized format */
        CUSTOM
    }
}
