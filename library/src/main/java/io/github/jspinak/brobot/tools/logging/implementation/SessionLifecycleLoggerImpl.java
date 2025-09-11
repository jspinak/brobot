package io.github.jspinak.brobot.tools.logging.implementation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.logging.SessionLifecycleLogger;

import lombok.Setter;

/**
 * Default implementation of {@link SessionLifecycleLogger} that manages automation test sessions in
 * memory with SLF4J logging output.
 *
 * <p>This implementation provides a lightweight session management solution that tracks active
 * automation sessions, their metadata, and current state context. It serves as the foundation for
 * correlating all logs within a test execution and understanding the automation flow.
 *
 * <p><strong>Design Decisions:</strong>
 *
 * <ul>
 *   <li>In-memory storage: Sessions are stored in a HashMap for simplicity and performance. This
 *       means sessions are lost on application restart.
 *   <li>UUID-based session IDs: Ensures globally unique identifiers without coordination
 *   <li>Minimal state tracking: Only essential session data is maintained to reduce memory
 *       footprint
 *   <li>No persistence: This implementation does not persist sessions to disk or database
 * </ul>
 *
 * <p><strong>Why This Implementation Exists:</strong>
 *
 * <ul>
 *   <li>Provides session correlation for distributed logging without external dependencies
 *   <li>Enables tracking of test execution context for better log analysis
 *   <li>Supports concurrent test execution with isolated session contexts
 *   <li>Offers a simple default that can be replaced with more sophisticated implementations
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This implementation is NOT thread-safe. The activeSessions map
 * is not synchronized, which can lead to issues if multiple threads access the same session
 * concurrently. For thread-safe operation, consider using ConcurrentHashMap or synchronizing
 * access.
 *
 * <p><strong>Memory Considerations:</strong> Sessions are held in memory until explicitly ended via
 * {@link #endSession(String)}. Failure to end sessions will result in memory leaks. Consider
 * implementing session timeouts or maximum session limits for production use.
 *
 * <p><strong>Error Handling:</strong> Unknown session IDs are handled gracefully with warning logs
 * rather than exceptions, preventing session management issues from disrupting test execution.
 *
 * @see SessionLifecycleLogger
 * @see ActionLogger
 * @see ActionLoggerImpl
 */
@Component
@Qualifier("sessionLoggerImpl") public class SessionLifecycleLoggerImpl implements SessionLifecycleLogger {
    private static final Logger logger = LoggerFactory.getLogger(SessionLifecycleLoggerImpl.class);

    /**
     * In-memory storage for active sessions.
     *
     * <p><strong>WARNING:</strong> This HashMap is not thread-safe. Concurrent access to the same
     * session or concurrent session creation/deletion may cause issues. Consider using {@code
     * ConcurrentHashMap} for thread-safe operation.
     */
    private final Map<String, SessionInfo> activeSessions = new HashMap<>();

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong>
     *
     * <ul>
     *   <li>Generates a UUID-based session ID ensuring uniqueness across distributed systems
     *   <li>Creates a new {@link SessionInfo} object to track session metadata
     *   <li>Stores the session in memory for the duration of the test execution
     *   <li>Session start time is automatically captured by SessionInfo constructor
     * </ul>
     *
     * <p><strong>Performance Impact:</strong> UUID generation and map insertion are O(1) operations
     * with minimal overhead.
     *
     * <p><strong>Memory Impact:</strong> Each session consumes approximately 200 bytes plus string
     * lengths. Ensure sessions are properly ended to prevent memory accumulation.
     *
     * @param applicationUnderTest The application name is stored but not validated. Null values are
     *     accepted but may cause issues in logging.
     * @return A UUID string in the format "123e4567-e89b-12d3-a456-426614174000"
     */
    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(sessionId, applicationUnderTest);
        activeSessions.put(sessionId, sessionInfo);

        logger.info("Started session {} for application: {}", sessionId, applicationUnderTest);
        return sessionId;
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong>
     *
     * <ul>
     *   <li>Sets the session end time before removal to calculate final duration
     *   <li>Logs session duration for performance analysis
     *   <li>Removes session from memory to prevent leaks
     *   <li>Handles unknown session IDs gracefully with warning logs
     * </ul>
     *
     * <p><strong>Race Condition Warning:</strong> The check-then-act pattern (containsKey followed
     * by get/remove) is not atomic. In a multi-threaded environment, the session could be removed
     * between these calls.
     *
     * <p><strong>Idempotency:</strong> Calling this method multiple times with the same sessionId
     * is safe but will log warnings for subsequent calls.
     *
     * @param sessionId The session to end. Unknown IDs are logged as warnings but do not throw
     *     exceptions, maintaining fail-safe behavior.
     */
    @Override
    public void endSession(String sessionId) {
        if (activeSessions.containsKey(sessionId)) {
            SessionInfo session = activeSessions.get(sessionId);
            session.setEndTime(LocalDateTime.now());
            logger.info(
                    "Ended session {} after {} seconds", sessionId, session.getDurationInSeconds());
            activeSessions.remove(sessionId);
        } else {
            logger.warn("Attempted to end unknown session: {}", sessionId);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong>
     *
     * <ul>
     *   <li>Updates the session's current state information in memory
     *   <li>Previous state information is overwritten (no history maintained)
     *   <li>State changes are logged at INFO level for audit trail
     *   <li>Unknown session IDs result in warning logs, not errors
     * </ul>
     *
     * <p><strong>Limitations:</strong>
     *
     * <ul>
     *   <li>No state history is maintained - only the current state is stored
     *   <li>State transitions are not validated or tracked
     *   <li>No timestamps are recorded for state changes
     * </ul>
     *
     * <p><strong>Use Case:</strong> This method should be called whenever the automation navigates
     * to a new application state to provide context for subsequent log entries.
     *
     * @param sessionId Must be a valid session ID returned by {@link #startSession(String)}
     * @param stateName Should be a consistent identifier for the application state (e.g.,
     *     "LoginPage", "Dashboard"). Null values are stored but not recommended.
     * @param stateDescription Additional context about the state. Can include dynamic information
     *     like "Dashboard - 5 notifications pending".
     */
    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        if (activeSessions.containsKey(sessionId)) {
            SessionInfo session = activeSessions.get(sessionId);
            session.setCurrentState(stateName, stateDescription);
            logger.info(
                    "Session {} state changed to: {} - {}", sessionId, stateName, stateDescription);
        } else {
            logger.warn("Attempted to update state for unknown session: {}", sessionId);
        }
    }

    /**
     * Internal data structure for storing session metadata and state.
     *
     * <p>This class encapsulates all information about an active automation session, including
     * timing, application context, and current state. It's designed as a simple data holder with
     * minimal behavior.
     *
     * <p><strong>Design Choices:</strong>
     *
     * <ul>
     *   <li>Immutable core fields (id, application, startTime) ensure session integrity
     *   <li>Mutable state fields allow tracking automation progress
     *   <li>Uses LocalDateTime for easier debugging/logging (vs Instant)
     *   <li>Package-private visibility prevents external manipulation
     * </ul>
     *
     * <p><strong>Thread Safety:</strong> This class is NOT thread-safe. Concurrent access to setter
     * methods may result in inconsistent state. The parent class should synchronize access if
     * needed.
     */
    private static class SessionInfo {
        private final String id;
        private final String application;
        private final LocalDateTime startTime;
        @Setter private LocalDateTime endTime;
        private String currentStateName;
        private String currentStateDescription;

        /**
         * Creates a new session info with the current time as start time.
         *
         * @param id The unique session identifier
         * @param application The name of the application under test
         */
        public SessionInfo(String id, String application) {
            this.id = id;
            this.application = application;
            this.startTime = LocalDateTime.now();
        }

        /**
         * Updates the current state information for this session.
         *
         * <p>Previous state information is discarded. No history is maintained.
         *
         * @param stateName The name of the new current state
         * @param stateDescription Detailed description of the state
         */
        public void setCurrentState(String stateName, String stateDescription) {
            this.currentStateName = stateName;
            this.currentStateDescription = stateDescription;
        }

        /**
         * Calculates the session duration in seconds.
         *
         * <p>If the session has not ended (endTime is null), calculates duration from start time to
         * current time. This allows getting duration for active sessions.
         *
         * @return Duration in seconds, rounded down. May be 0 for very short sessions.
         */
        public long getDurationInSeconds() {
            LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
            return java.time.Duration.between(startTime, end).getSeconds();
        }
    }
}
