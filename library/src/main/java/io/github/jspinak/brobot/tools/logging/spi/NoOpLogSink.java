package io.github.jspinak.brobot.tools.logging.spi;

import io.github.jspinak.brobot.tools.logging.model.LogData;

/**
 * A no-operation implementation of {@link LogSink} that discards all log data.
 * <p>
 * This implementation follows the Null Object pattern, providing a safe default
 * behavior when no actual logging is desired. It allows the Brobot framework to
 * function without requiring users to configure logging, making it ideal for:
 * <ul>
 * <li>Getting started quickly without logging setup</li>
 * <li>Running tests where logging output is not needed</li>
 * <li>Performance testing without logging overhead</li>
 * <li>Scenarios where logging is explicitly disabled</li>
 * </ul>
 * <p>
 * By providing this default implementation, the framework avoids null checks
 * throughout the codebase and ensures that logging calls never cause failures
 * even when no real log sink is configured.
 * <p>
 * Usage example:
 * <pre>{@code
 * // Use NoOpLogSink when you want to disable logging
 * LogSink sink = new NoOpLogSink();
 * ActionLogger logger = new ActionLoggerImpl(sink);
 * }</pre>
 *
 * @see LogSink
 * @see LogData
 */
public class NoOpLogSink implements LogSink {
    /**
     * Discards the provided log data without any processing or persistence.
     * <p>
     * This method implements the no-operation pattern by accepting the log data
     * and immediately discarding it. No validation, processing, or storage occurs.
     * The method is thread-safe as it performs no operations that could cause
     * concurrency issues.
     *
     * @param logData The log data to be discarded. Can be any valid LogData instance.
     *                The parameter is accepted but not used.
     */
    @Override
    public void save(LogData logData) {
        // This is a no-operation sink. It does not persist logs.
    }
}
