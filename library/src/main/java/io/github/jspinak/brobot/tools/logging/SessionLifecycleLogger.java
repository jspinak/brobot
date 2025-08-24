package io.github.jspinak.brobot.tools.logging;

/**
 * Manages the lifecycle and context of automation test sessions for logging purposes.
 * <p>
 * This interface provides session management capabilities for the logging system,
 * allowing multiple concurrent automation sessions to be tracked independently.
 * Each session represents a complete automation run with its own unique identifier,
 * enabling correlation of all logs, metrics, and recordings within that session.
 * <p>
 * Session management is crucial for:
 * <ul>
 * <li>Correlating all logs from a single automation run</li>
 * <li>Supporting parallel test execution with isolated logging</li>
 * <li>Tracking session-level metadata like application under test</li>
 * <li>Managing session state context for more meaningful logs</li>
 * </ul>
 * <p>
 * All methods provide default no-op implementations, making this interface optional
 * for simple logging scenarios while supporting sophisticated session tracking when needed.
 *
 * @see ActionLogger
 * @see io.github.jspinak.brobot.tools.logging.implementation.SessionLifecycleLoggerImpl
 */
public interface SessionLifecycleLogger {
    /**
     * Starts a new logging session for an automation test run.
     * <p>
     * This method initializes a new session context and returns a unique session identifier
     * that should be used for all subsequent logging calls within this automation run.
     * The session tracks metadata about the test execution and provides correlation for
     * all related log entries.
     * <p>
     * Implementations typically:
     * <ul>
     * <li>Generate a unique session ID (e.g., UUID)</li>
     * <li>Record session start time</li>
     * <li>Initialize session storage or log files</li>
     * <li>Capture environment information</li>
     * </ul>
     *
     * @param applicationUnderTest The name or identifier of the application being automated.
     *                            This helps distinguish logs when testing multiple applications.
     * @return A unique session identifier to use for all subsequent logging calls,
     *         or null for no-op implementations
     */
    default String startSession(String applicationUnderTest) {
        // No-op implementation, returning null
        return null;
    }

    /**
     * Ends an automation logging session and performs cleanup.
     * <p>
     * This method finalizes the session, ensuring all logs are flushed, resources are
     * released, and any session summary information is generated. It should be called
     * when the automation run completes, whether successfully or due to failure.
     * <p>
     * Implementations typically:
     * <ul>
     * <li>Record session end time and calculate total duration</li>
     * <li>Flush any buffered log entries</li>
     * <li>Generate session summary reports</li>
     * <li>Close file handles or database connections</li>
     * <li>Archive or compress log files if configured</li>
     * </ul>
     *
     * @param sessionId The unique identifier of the session to end.
     *                  Must match a sessionId returned by startSession.
     */
    default void endSession(String sessionId) {
        // No-op implementation
    }

    /**
     * Sets the current application state context for the logging session.
     * <p>
     * This method updates the session's understanding of the current application state,
     * which provides important context for subsequent log entries. All logs generated
     * after this call will be associated with this state until it's changed again.
     * This context is invaluable for understanding where in the application flow
     * events occurred.
     * <p>
     * State context is used to:
     * <ul>
     * <li>Provide context for action and error logs</li>
     * <li>Track navigation flow through the application</li>
     * <li>Group related logs by application state</li>
     * <li>Aid in debugging by showing what state errors occurred in</li>
     * </ul>
     *
     * @param sessionId The unique identifier of the current session
     * @param stateName The name of the current application state (e.g., "LoginPage", "Dashboard")
     * @param stateDescription A detailed description of the state, including any relevant
     *                         context about the state's condition or purpose
     */
    default void setCurrentState(String sessionId, String stateName, String stateDescription) {
        // No-op implementation
    }
}


