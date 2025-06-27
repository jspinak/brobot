package io.github.jspinak.brobot.tools.logging;

import java.util.List;

import io.github.jspinak.brobot.tools.logging.model.LogData;

/**
 * Defines the contract for sending log updates to external consumers or systems.
 * <p>
 * This interface enables real-time or batch transmission of log data to external
 * monitoring systems, dashboards, or log aggregation services. It complements the
 * {@link io.github.jspinak.brobot.tools.logging.spi.LogSink} interface by providing a
 * push-based mechanism for log distribution.
 * <p>
 * Common use cases:
 * <ul>
 * <li>Real-time log streaming to monitoring dashboards</li>
 * <li>Batch updates to log aggregation services</li>
 * <li>WebSocket updates to connected clients</li>
 * <li>Integration with external logging platforms</li>
 * <li>Triggering alerts based on log patterns</li>
 * </ul>
 * <p>
 * The default no-op implementation allows the interface to be optional,
 * enabling the logging system to function without requiring external integrations.
 * <p>
 * Implementation considerations:
 * <ul>
 * <li>Implementations should handle network failures gracefully</li>
 * <li>Consider buffering logs if the external system is temporarily unavailable</li>
 * <li>The method may be called frequently, so performance is important</li>
 * <li>Thread safety may be required if called from multiple threads</li>
 * </ul>
 *
 * @see LogData
 * @see io.github.jspinak.brobot.tools.logging.spi.LogSink
 */
public interface LogEventDispatcher {
    /**
     * Sends a batch of log entries to external consumers or monitoring systems.
     * <p>
     * This method is typically called with accumulated log entries for efficient
     * batch processing. The list may contain logs of different types (actions,
     * transitions, errors, etc.) from the same session or time period.
     * <p>
     * Implementations should:
     * <ul>
     * <li>Process the entire batch atomically if possible</li>
     * <li>Handle partial failures gracefully (e.g., retry failed entries)</li>
     * <li>Not modify the provided list or LogData objects</li>
     * <li>Complete quickly or delegate to async processing</li>
     * <li>Log any transmission errors rather than throwing exceptions</li>
     * </ul>
     * <p>
     * The default implementation does nothing, allowing the system to function
     * without external log transmission.
     *
     * @param logEntries The list of log entries to send. May be empty but not null.
     *                   The list and its contents should be treated as immutable.
     */
    default void sendLogUpdate(List<LogData> logEntries) {
        // No-op implementation
    }
}
