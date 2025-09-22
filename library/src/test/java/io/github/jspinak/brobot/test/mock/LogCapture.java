package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class for capturing log entries during tests.
 * Provides methods to verify logged messages and their properties.
 */
public class LogCapture {

    private final List<LogEntry> capturedLogs = Collections.synchronizedList(new ArrayList<>());

    /**
     * Creates a capturing logger that records all log entries.
     *
     * @return A BrobotLogger that captures all log entries
     */
    public BrobotLogger createCapturingLogger() {
        BrobotLogger logger = mock(BrobotLogger.class);

        when(logger.builder(any(LogCategory.class))).thenAnswer(invocation -> {
            LogCategory category = invocation.getArgument(0);
            return new CapturingLogBuilder(category, this);
        });

        return logger;
    }

    /**
     * Adds a log entry to the capture list.
     *
     * @param entry The log entry to capture
     */
    void addEntry(LogEntry entry) {
        capturedLogs.add(entry);
    }

    /**
     * Gets all captured log entries.
     *
     * @return A copy of the captured log entries
     */
    public List<LogEntry> getCapturedLogs() {
        return new ArrayList<>(capturedLogs);
    }

    /**
     * Gets captured logs filtered by category.
     *
     * @param category The category to filter by
     * @return Log entries matching the category
     */
    public List<LogEntry> getLogsByCategory(LogCategory category) {
        return capturedLogs.stream()
            .filter(log -> log.getCategory() == category)
            .collect(Collectors.toList());
    }

    /**
     * Gets captured logs filtered by level.
     *
     * @param level The level to filter by
     * @return Log entries matching the level
     */
    public List<LogEntry> getLogsByLevel(LogLevel level) {
        return capturedLogs.stream()
            .filter(log -> log.getLevel() == level)
            .collect(Collectors.toList());
    }

    /**
     * Clears all captured logs.
     */
    public void clear() {
        capturedLogs.clear();
    }

    /**
     * Asserts that a message was logged containing the specified text.
     *
     * @param expectedMessage The expected message or substring
     * @return true if a matching log was found
     */
    public boolean assertLoggedWithMessage(String expectedMessage) {
        return capturedLogs.stream()
            .anyMatch(log -> log.getMessage() != null &&
                           log.getMessage().contains(expectedMessage));
    }

    /**
     * Asserts that an error was logged.
     *
     * @param errorClass The expected error class
     * @return true if a matching error log was found
     */
    public boolean assertErrorLogged(Class<? extends Throwable> errorClass) {
        return capturedLogs.stream()
            .anyMatch(log -> log.getError() != null &&
                           errorClass.isInstance(log.getError()));
    }

    /**
     * Asserts that an action was logged.
     *
     * @param actionType The expected action type
     * @param target The expected action target (can be null)
     * @return true if a matching action log was found
     */
    public boolean assertActionLogged(String actionType, String target) {
        return capturedLogs.stream()
            .anyMatch(log -> actionType.equals(log.getActionType()) &&
                           (target == null || target.equals(log.getActionTarget())));
    }

    /**
     * Gets the count of captured logs.
     *
     * @return The number of captured log entries
     */
    public int getLogCount() {
        return capturedLogs.size();
    }

    /**
     * Gets the last captured log entry.
     *
     * @return The last log entry, or null if none captured
     */
    public LogEntry getLastLog() {
        return capturedLogs.isEmpty() ? null :
               capturedLogs.get(capturedLogs.size() - 1);
    }

    /**
     * Represents a captured log entry with all its properties.
     */
    public static class LogEntry {
        private final LogCategory category;
        private LogLevel level;
        private String message;
        private Object[] messageArgs;
        private String actionType;
        private String actionTarget;
        private Throwable error;
        private Duration duration;
        private String correlationId;
        private String state;
        private final LocalDateTime timestamp;

        public LogEntry(LogCategory category) {
            this.category = category;
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public LogCategory getCategory() { return category; }
        public LogLevel getLevel() { return level; }
        public String getMessage() { return message; }
        public Object[] getMessageArgs() { return messageArgs; }
        public String getActionType() { return actionType; }
        public String getActionTarget() { return actionTarget; }
        public Throwable getError() { return error; }
        public Duration getDuration() { return duration; }
        public String getCorrelationId() { return correlationId; }
        public String getState() { return state; }
        public LocalDateTime getTimestamp() { return timestamp; }

        // Package-private setters for CapturingLogBuilder
        void setLevel(LogLevel level) { this.level = level; }
        void setMessage(String message, Object... args) {
            this.message = message;
            this.messageArgs = args;
        }
        void setAction(String type, String target) {
            this.actionType = type;
            this.actionTarget = target;
        }
        void setError(Throwable error) { this.error = error; }
        void setDuration(Duration duration) { this.duration = duration; }
        void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        void setState(String state) { this.state = state; }

        /**
         * Gets the formatted message with arguments applied.
         *
         * @return The formatted message string
         */
        public String getFormattedMessage() {
            if (message == null) return null;
            if (messageArgs == null || messageArgs.length == 0) return message;
            try {
                return String.format(message, messageArgs);
            } catch (Exception e) {
                return message; // Return unformatted if format fails
            }
        }

        @Override
        public String toString() {
            return String.format("[%s][%s] %s",
                category, level, getFormattedMessage());
        }
    }
}