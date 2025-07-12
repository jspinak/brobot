package io.github.jspinak.brobot.tools.logging.implementation;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import io.github.jspinak.brobot.tools.logging.spi.LogSink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Default implementation of {@link ActionLogger} that creates structured log entries
 * and delegates persistence to a configured {@link LogSink}.
 * <p>
 * This implementation serves as the primary logging mechanism for the Brobot automation
 * framework, capturing detailed information about actions, state transitions, observations,
 * performance metrics, errors, and video recordings. It acts as a bridge between the
 * high-level logging API and the pluggable storage backend.
 * <p>
 * <strong>Design Decisions:</strong>
 * <ul>
 * <li>Separation of concerns: This class focuses on log entry creation and formatting,
 *     while the {@link LogSink} handles persistence</li>
 * <li>Dual logging: Uses SLF4J for immediate console/file output and LogSink for
 *     structured data persistence</li>
 * <li>Fail-safe: Methods do not throw exceptions to prevent logging failures from
 *     disrupting automation execution</li>
 * <li>Stateless design: No session state is maintained, ensuring thread safety</li>
 * </ul>
 * <p>
 * <strong>Log Flow:</strong>
 * <ol>
 * <li>Automation framework calls logging method (e.g., logAction)</li>
 * <li>This class creates a {@link LogData} entry with appropriate metadata</li>
 * <li>Entry is logged via SLF4J for immediate visibility</li>
 * <li>Entry is passed to LogSink for structured persistence</li>
 * </ol>
 * <p>
 * <strong>Thread Safety:</strong>
 * This implementation is thread-safe. All methods are stateless and the LogSink
 * is expected to handle concurrent calls.
 * <p>
 * <strong>Performance Considerations:</strong>
 * <ul>
 * <li>Log entry creation is lightweight with minimal object allocation</li>
 * <li>UUID generation for IDs may have minor performance impact</li>
 * <li>Actual performance depends heavily on the LogSink implementation</li>
 * </ul>
 *
 * @see ActionLogger
 * @see LogSink
 * @see LogData
 * @see io.github.jspinak.brobot.tools.logging.spi.NoOpLogSink
 */
@Component
@Qualifier("actionLoggerImpl")
public class ActionLoggerImpl implements ActionLogger {
    private static final Logger logger = LoggerFactory.getLogger(ActionLoggerImpl.class);
    private final LogSink logSink;

    /**
     * Constructs an ActionLoggerImpl with the specified log sink.
     * <p>
     * The provided LogSink will receive all generated log entries for persistence.
     * If a no-op sink is provided, logs will only appear in SLF4J output.
     *
     * @param logSink The sink implementation to handle log persistence.
     *                Must not be null. Use {@link io.github.jspinak.brobot.tools.logging.spi.NoOpLogSink}
     *                for testing or when persistence is not required.
     */
    public ActionLoggerImpl(LogSink logSink) {
        this.logSink = logSink;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Implementation Details:</strong>
     * <ul>
     * <li>Extracts action description from {@link ActionResult#getOutputText()}</li>
     * <li>Handles null results gracefully with "No results data" fallback</li>
     * <li>Success status is derived from {@link ActionResult#isSuccess()}</li>
     * <li>Currently does not persist ObjectCollection details (potential enhancement)</li>
     * </ul>
     * <p>
     * <strong>Error Handling:</strong>
     * This method will not throw exceptions. If the LogSink fails, the error
     * is logged via SLF4J but does not propagate to the caller.
     */
    @Override
    public LogData logAction(String sessionId, ActionResult results, ObjectCollection objectCollection) {
        String description = results != null ? results.getOutputText() : "No results data";
        boolean success = results != null && results.isSuccess();

        LogData entry = createLogEntry(sessionId, LogEventType.ACTION, description, success);
        logger.info("Action logged: {} - {}", sessionId, description);
        return entry;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Implementation Details:</strong>
     * <ul>
     * <li>Formats state sets into readable strings using {@link #formatStateSet(Set)}</li>
     * <li>Includes transition timing in the description for performance analysis</li>
     * <li>The beforeStates parameter is currently not included in the description
     *     (could be added for more detailed transition analysis)</li>
     * <li>Empty or null state sets are displayed as "None"</li>
     * </ul>
     * <p>
     * <strong>Description Format:</strong>
     * "Transition from [State1, State2] to [State3] (150ms)"
     */
    @Override
    public LogData logStateTransition(String sessionId, Set<State> fromStates, Set<State> toStates,
                                      Set<State> beforeStates, boolean success, long transitionTime) {
        String description = "Transition from " + formatStateSet(fromStates) +
                " to " + formatStateSet(toStates) +
                " (" + transitionTime + "ms)";

        LogData logData = createLogEntry(sessionId, LogEventType.TRANSITION, description, success);
        logger.info("State transition logged: {} - {}", sessionId, description);
        return logData;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Implementation Details:</strong>
     * <ul>
     * <li>Combines observationType and description with ": " separator</li>
     * <li>Always sets success to true (observations are informational)</li>
     * <li>The severity parameter is currently not persisted in LogData
     *     (potential enhancement for filtering/alerting)</li>
     * </ul>
     * <p>
     * <strong>Description Format:</strong>
     * "UI_STATE: Login button is disabled"
     */
    @Override
    public LogData logObservation(String sessionId, String observationType, String description, String severity) {
        LogData entry = createLogEntry(sessionId, LogEventType.OBSERVATION,
                observationType + ": " + description, true);
        logger.info("Observation logged: {} - {}: {}", sessionId, observationType, description);
        return entry;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Implementation Details:</strong>
     * <ul>
     * <li>Formats all timing values into a human-readable string</li>
     * <li>Always sets success to true (metrics are informational)</li>
     * <li>Individual metric values are not stored separately in LogData,
     *     making it difficult to query/aggregate (potential enhancement)</li>
     * </ul>
     * <p>
     * <strong>Description Format:</strong>
     * "Performance metrics - Action: 250ms, Page load: 1500ms, Total: 5000ms"
     * <p>
     * <strong>Future Enhancement:</strong>
     * Consider storing metrics as structured data for better analysis capabilities.
     */
    @Override
    public LogData logPerformanceMetrics(String sessionId, long actionDuration,
                                         long pageLoadTime, long totalTestDuration) {
        String description = "Performance metrics - Action: " + actionDuration +
                "ms, Page load: " + pageLoadTime +
                "ms, Total: " + totalTestDuration + "ms";

        LogData entry = createLogEntry(sessionId, LogEventType.METRICS, description, true);
        logger.info("Performance metrics logged: {} - {}", sessionId, description);
        return entry;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Implementation Details:</strong>
     * <ul>
     * <li>Appends screenshot path to description when available</li>
     * <li>Always sets success to false (errors indicate failure)</li>
     * <li>Uses logger.error() for SLF4J output to ensure visibility</li>
     * <li>Screenshot path is embedded in description rather than stored
     *     separately (potential enhancement for better querying)</li>
     * </ul>
     * <p>
     * <strong>Description Format:</strong>
     * <ul>
     * <li>Without screenshot: "Element not found after 30 seconds"</li>
     * <li>With screenshot: "Element not found after 30 seconds (Screenshot: /logs/error_12345.png)"</li>
     * </ul>
     */
    @Override
    public LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        String description = errorMessage;
        if (screenshotPath != null) {
            description += " (Screenshot: " + screenshotPath + ")";
        }

        LogData entry = createLogEntry(sessionId, LogEventType.ERROR, description, false);
        logger.error("Error logged: {} - {}", sessionId, errorMessage);
        return entry;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Implementation Note:</strong>
     * This implementation only logs the intent to start recording. Actual video
     * recording must be handled by a separate component. This design allows the
     * logging system to track recording lifecycle without being responsible for
     * the recording mechanism itself.
     * <p>
     * <strong>Limitations:</strong>
     * <ul>
     * <li>Does not perform actual video recording</li>
     * <li>Does not store recording file path or settings</li>
     * <li>Throws declared exceptions for API compatibility only</li>
     * </ul>
     */
    @Override
    public LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        LogData entry = createLogEntry(sessionId, LogEventType.VIDEO, "Started video recording", true);
        logger.info("Video recording started: {}", sessionId);
        return entry;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Implementation Note:</strong>
     * Like {@link #startVideoRecording(String)}, this method only logs the intent
     * to stop recording. It does not interact with actual recording components.
     * <p>
     * <strong>Limitations:</strong>
     * <ul>
     * <li>Does not perform actual recording stop</li>
     * <li>Does not capture recording duration or file size</li>
     * <li>Does not validate that recording was actually started</li>
     * </ul>
     */
    @Override
    public LogData stopVideoRecording(String sessionId) throws IOException {
        LogData entry = createLogEntry(sessionId, LogEventType.VIDEO, "Stopped video recording", true);
        logger.info("Video recording stopped: {}", sessionId);
        return entry;
    }

    /**
     * Creates a standardized log entry with common metadata.
     * <p>
     * This helper method centralizes the creation of {@link LogData} objects,
     * ensuring consistent structure across all log types. It handles:
     * <ul>
     * <li>Unique ID generation using UUID (note: converts to Long which may cause issues)</li>
     * <li>Timestamp capture using current system time</li>
     * <li>Setting all required fields for the LogData object</li>
     * </ul>
     * <p>
     * <strong>Known Issues:</strong>
     * <ul>
     * <li>Converting UUID to Long via toString() will fail with NumberFormatException.
     *     Consider using UUID.getMostSignificantBits() or a different ID strategy.</li>
     * <li>Uses LocalDateTime.now() then converts to Instant, which assumes system default timezone.
     *     Consider using Instant.now() directly for UTC timestamps.</li>
     * </ul>
     *
     * @param sessionId The session identifier for correlation
     * @param type The type of log entry being created
     * @param description Human-readable description of the logged event
     * @param success Whether the logged operation was successful
     * @return A fully populated LogData object ready for persistence
     */
    private LogData createLogEntry(String sessionId, LogEventType type, String description, boolean success) {
        LogData entry = new LogData();
        // Use UUID's most significant bits as a Long ID
        entry.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        entry.setSessionId(sessionId);
        entry.setTimestamp(Instant.now());
        entry.setType(type);
        entry.setDescription(description);
        entry.setSuccess(success);
        return entry;
    }

    /**
     * Formats a set of states into a human-readable string representation.
     * <p>
     * This helper method creates consistent formatting for state sets in log
     * descriptions, making logs easier to read and parse. The format follows
     * a list-style notation that clearly shows multiple states.
     * <p>
     * <strong>Format Examples:</strong>
     * <ul>
     * <li>Empty/null set: "None"</li>
     * <li>Single state: "[LoginPage]"</li>
     * <li>Multiple states: "[HomePage, Dashboard, Settings]"</li>
     * </ul>
     * <p>
     * <strong>Performance Note:</strong>
     * Uses StringBuilder for efficient string concatenation. For large state
     * sets, consider using Java 8 Streams with Collectors.joining().
     *
     * @param states The set of states to format. May be null or empty.
     * @return A formatted string representation of the state set
     */
    private String formatStateSet(Set<State> states) {
        if (states == null || states.isEmpty()) {
            return "None";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (State state : states) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(state.getName());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}