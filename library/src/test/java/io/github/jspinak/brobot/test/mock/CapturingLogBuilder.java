package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

import java.time.Duration;

/**
 * A LogBuilder implementation that captures log entries for testing.
 * Records all builder method calls and creates a LogEntry when log() is called.
 */
public class CapturingLogBuilder implements BrobotLogger.LogBuilder {

    private final LogCategory category;
    private final LogCapture logCapture;
    private final LogCapture.LogEntry entry;

    /**
     * Creates a new capturing log builder.
     *
     * @param category The log category
     * @param logCapture The LogCapture instance to record entries to
     */
    public CapturingLogBuilder(LogCategory category, LogCapture logCapture) {
        this.category = category;
        this.logCapture = logCapture;
        this.entry = new LogCapture.LogEntry(category);
    }

    @Override
    public BrobotLogger.LogBuilder level(LogLevel level) {
        entry.setLevel(level);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder message(String message, Object... args) {
        entry.setMessage(message, args);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder context(String key, Object value) {
        // Context could be stored in a map if needed
        // For now, we'll include it in the message
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder context(java.util.Map<String, Object> context) {
        // Context could be stored if needed
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder action(String actionType, String target) {
        entry.setAction(actionType, target);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder result(boolean success, double similarity, io.github.jspinak.brobot.model.element.Location location) {
        // Store result information
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder duration(Duration duration) {
        entry.setDuration(duration);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder durationSince(java.time.Instant startTime) {
        entry.setDuration(Duration.between(startTime, java.time.Instant.now()));
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder error(Throwable throwable) {
        entry.setError(throwable);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder error(String errorMessage) {
        // Store error message
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder correlationId(String correlationId) {
        entry.setCorrelationId(correlationId);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder state(String stateName) {
        entry.setState(stateName);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder memory(long memoryBytes) {
        // Store memory usage
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder operationCount(long count) {
        // Store operation count
        return this;
    }

    @Override
    public void log() {
        // Add the entry to the capture when log is called
        logCapture.addEntry(entry);
    }
}