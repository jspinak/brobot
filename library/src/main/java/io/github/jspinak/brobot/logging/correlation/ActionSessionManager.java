package io.github.jspinak.brobot.logging.correlation;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;

import lombok.RequiredArgsConstructor;

/**
 * Manages action sessions for correlated logging. Uses SLF4J MDC (Mapped Diagnostic Context) to add
 * session and sequence information to all logs.
 *
 * <p>This allows tracking of related actions across a workflow, making it easier to debug complex
 * automation scenarios and understand action sequences.
 *
 * <p>Actions are automatically tracked - no need to call nextAction() manually:
 *
 * <pre>
 * sessionManager.startSession("Login Workflow");
 * action.find(usernameField);     // Automatically tracked as seq:001
 * action.type(usernameField, username);  // Automatically tracked as seq:002
 * action.click(submitButton);     // Automatically tracked as seq:003
 * sessionManager.endSession();
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class ActionSessionManager {

    private static final String SESSION_KEY = "session";
    private static final String SEQUENCE_KEY = "seq";
    private static final String TASK_KEY = "task";

    private final BrobotLogger logger;
    private final AtomicInteger sequenceCounter = new AtomicInteger(0);
    private String currentSession;

    /**
     * Start a new action session with a descriptive name. All subsequent logs will include the
     * session ID for correlation.
     *
     * @param taskName A descriptive name for the task being performed
     */
    public void startSession(String taskName) {
        currentSession = UUID.randomUUID().toString().substring(0, 8);
        sequenceCounter.set(0);

        MDC.put(SESSION_KEY, currentSession);
        MDC.put(TASK_KEY, taskName);

        String message =
                String.format("=== Starting Task: %s | Session: %s ===", taskName, currentSession);
        logger.info(LogCategory.LIFECYCLE, message);
    }

    /**
     * Increment and set the sequence number for the next action. This helps track the order of
     * actions within a session.
     *
     * @deprecated Since actions are now tracked automatically, this method is no longer needed.
     *     It's kept for backward compatibility but will be called automatically.
     */
    @Deprecated
    public void nextAction() {
        incrementSequence();
    }

    /**
     * Automatically increment the sequence counter when an action is performed. This is called
     * internally by the framework.
     *
     * @return The new sequence number
     */
    public int incrementSequence() {
        if (!hasActiveSession()) {
            return 0; // No session, no sequence tracking
        }
        int seq = sequenceCounter.incrementAndGet();
        MDC.put(SEQUENCE_KEY, String.format("%03d", seq));
        return seq;
    }

    /**
     * Get the current sequence number without incrementing.
     *
     * @return The current sequence number, or 0 if no session is active
     */
    public int getCurrentSequence() {
        return hasActiveSession() ? sequenceCounter.get() : 0;
    }

    /**
     * End the current session and clear MDC context. Logs a summary of the session including total
     * actions performed.
     */
    public void endSession() {
        if (currentSession == null) {
            return; // No active session
        }

        String task = MDC.get(TASK_KEY);
        String message =
                String.format(
                        "=== Completed Task: %s | Session: %s | Total Actions: %d ===",
                        task, currentSession, sequenceCounter.get());
        logger.info(LogCategory.LIFECYCLE, message);

        MDC.clear();
        currentSession = null;
        sequenceCounter.set(0);
    }

    /**
     * Execute a task with automatic session management. The session will be automatically started
     * and ended, even if an exception occurs.
     *
     * @param taskName A descriptive name for the task
     * @param task The task to execute
     */
    public void executeWithSession(String taskName, Runnable task) {
        try {
            startSession(taskName);
            task.run();
        } finally {
            endSession();
        }
    }

    /**
     * Execute a task with automatic session management and return a result. The session will be
     * automatically started and ended, even if an exception occurs.
     *
     * @param taskName A descriptive name for the task
     * @param task The task to execute
     * @param <T> The type of result returned
     * @return The result of the task execution
     */
    public <T> T executeWithSession(String taskName, java.util.concurrent.Callable<T> task)
            throws Exception {
        try {
            startSession(taskName);
            return task.call();
        } finally {
            endSession();
        }
    }

    /**
     * Check if a session is currently active
     *
     * @return true if a session is active, false otherwise
     */
    public boolean hasActiveSession() {
        return currentSession != null;
    }

    /**
     * Get the current session ID
     *
     * @return The current session ID, or null if no session is active
     */
    public String getCurrentSession() {
        return currentSession;
    }
}
