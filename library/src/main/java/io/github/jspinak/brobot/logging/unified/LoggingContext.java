package io.github.jspinak.brobot.logging.unified;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;

/**
 * Thread-local context management for the unified logging system.
 *
 * <p>LoggingContext maintains contextual information that should be automatically included in all
 * log entries. This includes session IDs, current state, active operations, and custom metadata.
 * The context is thread-local, ensuring that concurrent automation runs don't interfere with each
 * other.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Thread-local storage for isolation between concurrent executions
 *   <li>Hierarchical operation tracking (stack-based)
 *   <li>Custom metadata support
 *   <li>Context snapshots for async operations
 *   <li>Automatic cleanup to prevent memory leaks
 * </ul>
 *
 * <p>The context automatically propagates to all log entries created in the same thread,
 * eliminating the need to manually pass context information through method calls.
 *
 * @since 2.0
 * @see BrobotLogger
 * @see LogEvent
 */
@Component
public class LoggingContext {

    /** Thread-local storage for context data. */
    private static final ThreadLocal<Context> contextHolder = ThreadLocal.withInitial(Context::new);

    /** Internal context data structure. */
    static class Context {
        private String sessionId;
        private State currentState;
        private String currentAction;
        private final Deque<String> operationStack = new ArrayDeque<>();
        private final Map<String, Object> metadata = new ConcurrentHashMap<>();
        private long threadId = Thread.currentThread().getId();

        /**
         * Creates a deep copy of this context. Used for capturing snapshots for async operations.
         */
        Context snapshot() {
            Context copy = new Context();
            copy.sessionId = this.sessionId;
            copy.currentState = this.currentState;
            copy.currentAction = this.currentAction;
            copy.operationStack.addAll(this.operationStack);
            copy.metadata.putAll(this.metadata);
            copy.threadId = this.threadId;
            return copy;
        }

        /** Clears all context data. */
        void clear() {
            sessionId = null;
            currentState = null;
            currentAction = null;
            operationStack.clear();
            metadata.clear();
        }
    }

    /**
     * Sets the session ID for the current thread.
     *
     * @param sessionId The session identifier
     */
    public void setSessionId(String sessionId) {
        contextHolder.get().sessionId = sessionId;
    }

    /**
     * Gets the current session ID.
     *
     * @return The session ID or null if not set
     */
    public String getSessionId() {
        return contextHolder.get().sessionId;
    }

    /**
     * Sets the current state for the thread.
     *
     * @param state The current application state
     */
    public void setCurrentState(State state) {
        contextHolder.get().currentState = state;
    }

    /**
     * Gets the current state.
     *
     * @return The current state or null if not set
     */
    public State getCurrentState() {
        return contextHolder.get().currentState;
    }

    /**
     * Sets the current action being performed.
     *
     * @param action The action type (e.g., "CLICK", "TYPE")
     */
    public void setCurrentAction(String action) {
        contextHolder.get().currentAction = action;
    }

    /**
     * Gets the current action.
     *
     * @return The current action or null if not set
     */
    public String getCurrentAction() {
        return contextHolder.get().currentAction;
    }

    /**
     * Pushes an operation onto the operation stack. Operations are tracked hierarchically for
     * nested operations.
     *
     * @param operationName The name of the operation
     */
    public void pushOperation(String operationName) {
        contextHolder.get().operationStack.push(operationName);
    }

    /**
     * Pops the most recent operation from the stack.
     *
     * @return The operation name or null if stack is empty
     */
    public String popOperation() {
        Deque<String> stack = contextHolder.get().operationStack;
        return stack.isEmpty() ? null : stack.pop();
    }

    /**
     * Gets the current operation (top of stack).
     *
     * @return The current operation or null if none
     */
    public String getCurrentOperation() {
        Deque<String> stack = contextHolder.get().operationStack;
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * Gets all operations in the stack.
     *
     * @return An unmodifiable list of operations (oldest to newest)
     */
    public List<String> getOperationStack() {
        return new ArrayList<>(contextHolder.get().operationStack);
    }

    /**
     * Adds custom metadata to the context.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, Object value) {
        contextHolder.get().metadata.put(key, value);
    }

    /**
     * Removes metadata from the context.
     *
     * @param key The metadata key to remove
     */
    public void removeMetadata(String key) {
        contextHolder.get().metadata.remove(key);
    }

    /**
     * Gets metadata value.
     *
     * @param key The metadata key
     * @return The value or null if not present
     */
    public Object getMetadata(String key) {
        return contextHolder.get().metadata.get(key);
    }

    /**
     * Gets all metadata as an unmodifiable map.
     *
     * @return The metadata map
     */
    public Map<String, Object> getAllMetadata() {
        return Collections.unmodifiableMap(contextHolder.get().metadata);
    }

    /**
     * Creates a snapshot of the current context. Useful for capturing context before async
     * operations.
     *
     * @return A deep copy of the current context
     */
    public Context snapshot() {
        return contextHolder.get().snapshot();
    }

    /**
     * Restores context from a snapshot.
     *
     * @param snapshot The context snapshot to restore
     */
    public void restore(Context snapshot) {
        contextHolder.set(snapshot);
    }

    /** Clears the session-related context while preserving other context. */
    public void clearSession() {
        Context ctx = contextHolder.get();
        ctx.sessionId = null;
        ctx.operationStack.clear();
    }

    /**
     * Clears all context for the current thread. Should be called at the end of an automation run
     * to prevent memory leaks.
     */
    public void clear() {
        contextHolder.get().clear();
        contextHolder.remove();
    }

    /**
     * Gets the thread ID associated with this context.
     *
     * @return The thread ID
     */
    public long getThreadId() {
        return contextHolder.get().threadId;
    }

    /**
     * Checks if the context has a session ID set.
     *
     * @return true if session ID is set
     */
    public boolean hasSession() {
        return contextHolder.get().sessionId != null;
    }

    /**
     * Executes a runnable with a temporary context. The original context is restored after
     * execution.
     *
     * @param temporaryContext The temporary context to use
     * @param runnable The code to execute
     */
    public void withContext(Context temporaryContext, Runnable runnable) {
        Context original = snapshot();
        try {
            restore(temporaryContext);
            runnable.run();
        } finally {
            restore(original);
        }
    }

    /**
     * Gets a string representation of the current context for debugging.
     *
     * @return A formatted string with context information
     */
    @Override
    public String toString() {
        Context ctx = contextHolder.get();
        return String.format(
                "LoggingContext[session=%s, state=%s, action=%s, operations=%s, metadata=%s]",
                ctx.sessionId,
                ctx.currentState != null ? ctx.currentState.getName() : "null",
                ctx.currentAction,
                ctx.operationStack,
                ctx.metadata);
    }
}
