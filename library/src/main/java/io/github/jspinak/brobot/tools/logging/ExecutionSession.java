package io.github.jspinak.brobot.tools.logging;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Thread-local session management utility for tracking automation executions in Brobot.
 *
 * <p>ExecutionSession provides unique session identifiers for each thread executing automation
 * tasks, enabling proper isolation and tracking of concurrent automation runs. This is particularly
 * important in multi-threaded environments where multiple automation sessions may run
 * simultaneously.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Thread Isolation</b>: Each thread maintains its own session ID via ThreadLocal
 *   <li><b>UUID Generation</b>: Globally unique identifiers prevent session conflicts
 *   <li><b>Lifecycle Management</b>: Start, query, and end sessions explicitly
 *   <li><b>Memory Safety</b>: Proper cleanup prevents memory leaks in thread pools
 * </ul>
 *
 * <p>Usage patterns:
 *
 * <ul>
 *   <li><b>Logging Correlation</b>: Tag log entries with session IDs for tracing
 *   <li><b>Resource Tracking</b>: Associate screenshots, files with specific sessions
 *   <li><b>Error Handling</b>: Group errors by session for debugging
 *   <li><b>Performance Metrics</b>: Measure per-session execution times
 *   <li><b>Concurrent Testing</b>: Isolate parallel test executions
 * </ul>
 *
 * <p>Typical lifecycle:
 *
 * <pre>
 * // Start of automation
 * String sessionId = automationSession.startNewSession();
 * logger.info("Starting automation session: " + sessionId);
 *
 * try {
 *     // Perform automation tasks
 *     // All logging can reference getCurrentSessionId()
 * } finally {
 *     // Always clean up
 *     automationSession.endSession();
 * }
 * </pre>
 *
 * <p>Thread pool considerations:
 *
 * <ul>
 *   <li>Always call {@link #endSession()} to prevent memory leaks
 *   <li>Use try-finally blocks to ensure cleanup
 *   <li>Session IDs don't survive thread reuse without explicit management
 *   <li>Consider implementing AutoCloseable wrapper for try-with-resources
 * </ul>
 *
 * <p>Integration with logging system:
 *
 * <ul>
 *   <li>Can be injected into {@link ConsoleReporter} for session-tagged output
 *   <li>Useful for correlating entries across different log files
 *   <li>Enables filtering logs by session in analysis tools
 * </ul>
 *
 * <p>Thread safety: This class is thread-safe. Each thread maintains its own session ID
 * independently through ThreadLocal storage. No synchronization needed for normal operations.
 *
 * @since 1.0
 * @see ConsoleReporter
 * @see ThreadLocal
 */
@Component
public class ExecutionSession {

    /**
     * Thread-local storage for session IDs. Each thread gets its own independent session
     * identifier.
     */
    private static final ThreadLocal<String> currentSessionId = new ThreadLocal<>();

    /**
     * Starts a new automation session for the current thread.
     *
     * <p>Generates a new UUID and stores it in thread-local storage. If a session already exists
     * for this thread, it will be replaced.
     *
     * @return the newly generated session ID (UUID format)
     */
    public String startNewSession() {
        String newId = UUID.randomUUID().toString();
        currentSessionId.set(newId);
        return newId;
    }

    /**
     * Retrieves the current session ID for the calling thread.
     *
     * @return the current session ID, or null if no session is active
     */
    public String getCurrentSessionId() {
        return currentSessionId.get();
    }

    /**
     * Ends the current session for the calling thread.
     *
     * <p>Removes the session ID from thread-local storage to prevent memory leaks in thread pool
     * environments. This method should always be called when an automation session completes.
     */
    public void endSession() {
        currentSessionId.remove();
    }
}
