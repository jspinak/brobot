package io.github.jspinak.brobot.logging.correlation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages correlation IDs and session context for log entries.
 *
 * <p>Provides ThreadLocal storage for correlation data, enabling tracking of requests and
 * operations across the entire execution context. Each thread maintains its own correlation context
 * that can be updated and accessed throughout the execution lifecycle.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Automatic correlation ID generation
 *   <li>Session and operation tracking
 *   <li>Thread-safe context management
 *   <li>Hierarchical operation nesting
 *   <li>Custom metadata support
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Start a new session
 * correlationContext.startSession("user-action");
 *
 * // Start a nested operation
 * correlationContext.startOperation("find-element");
 *
 * // Add context data
 * correlationContext.addContext("state", "loginScreen");
 * correlationContext.addContext("target", "submitButton");
 *
 * // End operation and session
 * correlationContext.endOperation();
 * correlationContext.endSession();
 * }</pre>
 */
@Slf4j
@Component
public class CorrelationContext {

    /** Context data structure for a single thread's execution. */
    @Data
    public static class Context {
        private String correlationId;
        private String sessionId;
        private String currentOperation;
        private Instant sessionStartTime;
        private Instant operationStartTime;
        private Map<String, Object> metadata = new ConcurrentHashMap<>();
        private int operationDepth = 0;

        public Context() {
            this.correlationId = generateId();
            this.sessionId = generateId();
            this.sessionStartTime = Instant.now();
        }

        public Context(String correlationId) {
            this.correlationId = correlationId;
            this.sessionId = generateId();
            this.sessionStartTime = Instant.now();
        }

        private static String generateId() {
            return UUID.randomUUID().toString().substring(0, 8);
        }

        /**
         * Create a copy of the context for inheritance.
         *
         * @return A new context with the same correlation ID but new session
         */
        public Context copy() {
            Context copy = new Context(this.correlationId);
            copy.metadata.putAll(this.metadata);
            return copy;
        }
    }

    private final ThreadLocal<Context> threadContext = new ThreadLocal<>();

    /**
     * Get the current thread's correlation context. Creates a new context if none exists.
     *
     * @return The current context
     */
    public Context getCurrentContext() {
        Context context = threadContext.get();
        if (context == null) {
            context = new Context();
            threadContext.set(context);
            log.trace("Created new correlation context: {}", context.getCorrelationId());
        }
        return context;
    }

    /**
     * Get the current correlation ID.
     *
     * @return The correlation ID for this thread
     */
    public String getCorrelationId() {
        return getCurrentContext().getCorrelationId();
    }

    /**
     * Get the current session ID.
     *
     * @return The session ID for this thread
     */
    public String getSessionId() {
        return getCurrentContext().getSessionId();
    }

    /**
     * Get the current operation name.
     *
     * @return The current operation name, or null if no operation is active
     */
    public String getCurrentOperation() {
        return getCurrentContext().getCurrentOperation();
    }

    /**
     * Start a new session with the given name. This resets the session context but preserves the
     * correlation ID.
     *
     * @param sessionName The name of the session
     */
    public void startSession(String sessionName) {
        Context context = getCurrentContext();
        context.setSessionId(Context.generateId());
        context.setSessionStartTime(Instant.now());
        context.setOperationDepth(0);
        context.getMetadata().put("sessionName", sessionName);

        log.trace("Started session '{}' with ID: {}", sessionName, context.getSessionId());
    }

    /**
     * Start a new operation within the current session. Operations can be nested.
     *
     * @param operationName The name of the operation
     */
    public void startOperation(String operationName) {
        Context context = getCurrentContext();
        context.setCurrentOperation(operationName);
        context.setOperationStartTime(Instant.now());
        context.setOperationDepth(context.getOperationDepth() + 1);

        log.trace("Started operation '{}' (depth: {})", operationName, context.getOperationDepth());
    }

    /**
     * End the current operation. Decreases operation depth and clears operation context if depth
     * reaches 0.
     */
    public void endOperation() {
        Context context = getCurrentContext();
        int newDepth = Math.max(0, context.getOperationDepth() - 1);
        context.setOperationDepth(newDepth);

        if (newDepth == 0) {
            String operation = context.getCurrentOperation();
            context.setCurrentOperation(null);
            context.setOperationStartTime(null);
            log.trace("Ended operation '{}'", operation);
        } else {
            log.trace("Ended nested operation (depth: {})", newDepth);
        }
    }

    /** End the current session. Clears all session context. */
    public void endSession() {
        Context context = getCurrentContext();
        String sessionId = context.getSessionId();

        // Reset session data but keep correlation ID
        context.setSessionId(null);
        context.setCurrentOperation(null);
        context.setSessionStartTime(null);
        context.setOperationStartTime(null);
        context.setOperationDepth(0);
        context.getMetadata().clear();

        log.trace("Ended session: {}", sessionId);
    }

    /**
     * Add metadata to the current context.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addContext(String key, Object value) {
        getCurrentContext().getMetadata().put(key, value);
        log.trace("Added context: {} = {}", key, value);
    }

    /**
     * Add multiple metadata entries to the current context.
     *
     * @param metadata Map of metadata entries
     */
    public void addContext(Map<String, Object> metadata) {
        getCurrentContext().getMetadata().putAll(metadata);
        log.trace("Added {} context entries", metadata.size());
    }

    /**
     * Get metadata from the current context.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    public Object getContext(String key) {
        return getCurrentContext().getMetadata().get(key);
    }

    /**
     * Get all metadata from the current context.
     *
     * @return A copy of the current metadata map
     */
    public Map<String, Object> getAllContext() {
        return new ConcurrentHashMap<>(getCurrentContext().getMetadata());
    }

    /**
     * Remove metadata from the current context.
     *
     * @param key The metadata key to remove
     * @return The previous value, or null if key was not present
     */
    public Object removeContext(String key) {
        Object removed = getCurrentContext().getMetadata().remove(key);
        if (removed != null) {
            log.trace("Removed context: {}", key);
        }
        return removed;
    }

    /** Clear all metadata from the current context. */
    public void clearContext() {
        int size = getCurrentContext().getMetadata().size();
        getCurrentContext().getMetadata().clear();
        log.trace("Cleared {} context entries", size);
    }

    /**
     * Set a specific correlation ID for the current thread. Useful for inheriting correlation from
     * external sources.
     *
     * @param correlationId The correlation ID to set
     */
    public void setCorrelationId(String correlationId) {
        Context context = getCurrentContext();
        String oldId = context.getCorrelationId();
        context.setCorrelationId(correlationId);
        log.trace("Changed correlation ID from {} to {}", oldId, correlationId);
    }

    /**
     * Inherit context from another thread's context. Creates a new session with the same
     * correlation ID.
     *
     * @param parentContext The context to inherit from
     */
    public void inheritContext(Context parentContext) {
        Context newContext = parentContext.copy();
        threadContext.set(newContext);
        log.trace("Inherited context with correlation ID: {}", newContext.getCorrelationId());
    }

    /** Clear the current thread's context completely. The next access will create a new context. */
    public void clearCurrentContext() {
        Context context = threadContext.get();
        if (context != null) {
            String correlationId = context.getCorrelationId();
            threadContext.remove();
            log.trace("Cleared context for correlation ID: {}", correlationId);
        }
    }

    /**
     * Get the session duration for the current context.
     *
     * @return Session duration in milliseconds, or 0 if no session is active
     */
    public long getSessionDurationMs() {
        Context context = getCurrentContext();
        if (context.getSessionStartTime() != null) {
            return Instant.now().toEpochMilli() - context.getSessionStartTime().toEpochMilli();
        }
        return 0;
    }

    /**
     * Get the operation duration for the current context.
     *
     * @return Operation duration in milliseconds, or 0 if no operation is active
     */
    public long getOperationDurationMs() {
        Context context = getCurrentContext();
        if (context.getOperationStartTime() != null) {
            return Instant.now().toEpochMilli() - context.getOperationStartTime().toEpochMilli();
        }
        return 0;
    }

    /**
     * Check if there is an active session.
     *
     * @return true if a session is active
     */
    public boolean hasActiveSession() {
        return getCurrentContext().getSessionId() != null;
    }

    /**
     * Check if there is an active operation.
     *
     * @return true if an operation is active
     */
    public boolean hasActiveOperation() {
        return getCurrentContext().getCurrentOperation() != null;
    }
}
