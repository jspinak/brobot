package io.github.jspinak.brobot.logging;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.correlation.CorrelationContext;
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.logging.events.MatchEvent;
import io.github.jspinak.brobot.logging.events.PerformanceEvent;
import io.github.jspinak.brobot.logging.events.TransitionEvent;
import io.github.jspinak.brobot.logging.formatter.JsonLogFormatter;
import io.github.jspinak.brobot.logging.formatter.LogFormatter;
import io.github.jspinak.brobot.logging.formatter.SimpleLogFormatter;
import io.github.jspinak.brobot.logging.formatter.StructuredLogFormatter;

import lombok.extern.slf4j.Slf4j;

/**
 * Main implementation of the BrobotLogger interface.
 *
 * <p>Handles log filtering based on levels, delegates to formatters, and integrates with SLF4J for
 * actual output. Provides the complete logging infrastructure for the Brobot framework.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Category and level-based filtering
 *   <li>Multiple output formatters
 *   <li>Correlation context integration
 *   <li>Performance optimizations
 *   <li>SLF4J integration for output
 *   <li>Thread-safe operation
 * </ul>
 *
 * <p>The logger automatically enriches log entries with correlation information from the current
 * thread context and applies filtering based on the configured logging levels.
 */
@Slf4j
@Component
public class BrobotLoggerImpl implements BrobotLogger {

    private final LoggingConfiguration config;
    private final CorrelationContext correlationContext;
    private final Map<String, LogFormatter> formatters;
    private final LogFormatter defaultFormatter;

    @Autowired
    public BrobotLoggerImpl(
            LoggingConfiguration config,
            CorrelationContext correlationContext,
            SimpleLogFormatter simpleFormatter,
            StructuredLogFormatter structuredFormatter,
            JsonLogFormatter jsonFormatter) {
        this.config = config;
        this.correlationContext = correlationContext;
        this.formatters = new ConcurrentHashMap<>();

        // Register available formatters
        formatters.put("SIMPLE", simpleFormatter);
        formatters.put("STRUCTURED", structuredFormatter);
        formatters.put("JSON", jsonFormatter);

        // Set default formatter based on configuration
        this.defaultFormatter = getFormatterForOutputFormat(config.getOutput().getFormat());

        log.debug(
                "BrobotLogger initialized with {} formatters, default format: {}",
                formatters.size(),
                config.getOutput().getFormat());
    }

    @Override
    public void logAction(ActionEvent event) {
        if (!isLoggingEnabled(LogCategory.ACTIONS, event.getLevel())) {
            return;
        }

        LogEntry entry =
                LogEntry.builder()
                        .timestamp(Instant.now())
                        .category(LogCategory.ACTIONS)
                        .level(event.getLevel())
                        .message(event.getMessage())
                        .threadName(Thread.currentThread().getName())
                        .actionType(event.getActionType())
                        .actionTarget(event.getTarget())
                        .success(event.isSuccess())
                        .similarity(event.getSimilarity())
                        .location(event.getLocation())
                        .duration(event.getDuration())
                        .error(event.getError())
                        .metadata(event.getMetadata())
                        .build();

        logEntryWithCorrelation(entry);
    }

    @Override
    public void logTransition(TransitionEvent event) {
        if (!isLoggingEnabled(LogCategory.TRANSITIONS, event.getLevel())) {
            return;
        }

        LogEntry entry =
                LogEntry.builder()
                        .timestamp(Instant.now())
                        .category(LogCategory.TRANSITIONS)
                        .level(event.getLevel())
                        .message(event.getMessage())
                        .threadName(Thread.currentThread().getName())
                        .currentState(event.getFromState())
                        .targetState(event.getToState())
                        .success(event.isSuccess())
                        .duration(event.getDuration())
                        .error(event.getError())
                        .metadata(event.getMetadata())
                        .build();

        logEntryWithCorrelation(entry);
    }

    @Override
    public void logMatch(MatchEvent event) {
        if (!isLoggingEnabled(LogCategory.MATCHING, event.getLevel())) {
            return;
        }

        LogEntry entry =
                LogEntry.builder()
                        .timestamp(Instant.now())
                        .category(LogCategory.MATCHING)
                        .level(event.getLevel())
                        .message(event.getMessage())
                        .threadName(Thread.currentThread().getName())
                        .actionTarget(event.getPattern())
                        .success(event.isFound())
                        .similarity(event.getSimilarity())
                        .location(event.getLocation())
                        .duration(event.getDuration())
                        .operationCount((long) event.getMatchCount())
                        .metadata(event.getMetadata())
                        .build();

        logEntryWithCorrelation(entry);
    }

    @Override
    public void logPerformance(PerformanceEvent event) {
        if (!isLoggingEnabled(LogCategory.PERFORMANCE, event.getLevel())) {
            return;
        }

        LogEntry entry =
                LogEntry.builder()
                        .timestamp(Instant.now())
                        .category(LogCategory.PERFORMANCE)
                        .level(event.getLevel())
                        .message(event.getMessage())
                        .threadName(Thread.currentThread().getName())
                        .duration(event.getDuration())
                        .memoryUsage(event.getMemoryUsage())
                        .operationCount(event.getOperationCount())
                        .metadata(event.getMetadata())
                        .build();

        logEntryWithCorrelation(entry);
    }

    @Override
    public void log(
            LogCategory category, LogLevel level, String message, Map<String, Object> context) {
        if (!isLoggingEnabled(category, level)) {
            return;
        }

        LogEntry entry =
                LogEntry.builder()
                        .timestamp(Instant.now())
                        .category(category)
                        .level(level)
                        .message(message)
                        .threadName(Thread.currentThread().getName())
                        .metadata(context != null ? context : java.util.Collections.emptyMap())
                        .build();

        logEntryWithCorrelation(entry);
    }

    @Override
    public boolean isLoggingEnabled(LogCategory category, LogLevel level) {
        return config.isLoggingEnabled(category, level);
    }

    @Override
    public LogBuilder builder(LogCategory category) {
        return new LogBuilderImpl(category, this);
    }

    /**
     * Log an entry with correlation context enrichment.
     *
     * @param entry The log entry to enrich and output
     */
    private void logEntryWithCorrelation(LogEntry entry) {
        try {
            // Enrich with correlation context
            CorrelationContext.Context context = correlationContext.getCurrentContext();
            LogEntry enrichedEntry =
                    entry.toBuilder()
                            .correlationId(context.getCorrelationId())
                            .sessionId(context.getSessionId())
                            .operationName(context.getCurrentOperation())
                            .build();

            // Add context metadata
            if (!context.getMetadata().isEmpty()) {
                enrichedEntry = enrichedEntry.withAdditionalMetadata(context.getMetadata());
            }

            // Format and output
            String formattedMessage = defaultFormatter.format(enrichedEntry);
            outputToSlf4j(enrichedEntry.getCategory(), enrichedEntry.getLevel(), formattedMessage);

        } catch (Exception e) {
            // Fallback logging to prevent log failures from breaking the application
            log.error("Failed to process log entry: {} - {}", entry.getSummary(), e.getMessage());
        }
    }

    /**
     * Output the formatted message to SLF4J.
     *
     * @param category The log category
     * @param level The log level
     * @param message The formatted message
     */
    private void outputToSlf4j(LogCategory category, LogLevel level, String message) {
        // Use category-specific logger for better log organization
        org.slf4j.Logger categoryLogger =
                org.slf4j.LoggerFactory.getLogger("brobot." + category.toString().toLowerCase());

        switch (level) {
            case ERROR:
                categoryLogger.error(message);
                break;
            case WARN:
                categoryLogger.warn(message);
                break;
            case INFO:
                categoryLogger.info(message);
                break;
            case DEBUG:
                categoryLogger.debug(message);
                break;
            case TRACE:
                categoryLogger.trace(message);
                break;
            case OFF:
            default:
                // Do nothing
                break;
        }
    }

    /**
     * Get the appropriate formatter for the given output format.
     *
     * @param format The output format
     * @return The corresponding formatter
     */
    private LogFormatter getFormatterForOutputFormat(LoggingConfiguration.OutputFormat format) {
        switch (format) {
            case SIMPLE:
                return formatters.get("SIMPLE");
            case STRUCTURED:
                return formatters.get("STRUCTURED");
            case JSON:
                return formatters.get("JSON");
            default:
                return formatters.get("STRUCTURED");
        }
    }

    /** Implementation of the fluent LogBuilder interface. */
    private static class LogBuilderImpl implements LogBuilder {
        private final LogCategory category;
        private final BrobotLoggerImpl logger;
        private LogLevel level = LogLevel.INFO;
        private String message;
        private final Map<String, Object> context = new ConcurrentHashMap<>();
        private String actionType;
        private String actionTarget;
        private Boolean success;
        private Double similarity;
        private io.github.jspinak.brobot.model.element.Location location;
        private Duration duration;
        private String correlationId;
        private String currentState;
        private Throwable error;
        private String errorMessage;
        private Long memoryBytes;
        private Long operationCount;

        public LogBuilderImpl(LogCategory category, BrobotLoggerImpl logger) {
            this.category = category;
            this.logger = logger;
        }

        @Override
        public LogBuilder level(LogLevel level) {
            this.level = level;
            return this;
        }

        @Override
        public LogBuilder message(String message, Object... args) {
            this.message = args.length > 0 ? String.format(message, args) : message;
            return this;
        }

        @Override
        public LogBuilder context(String key, Object value) {
            this.context.put(key, value);
            return this;
        }

        @Override
        public LogBuilder context(Map<String, Object> context) {
            this.context.putAll(context);
            return this;
        }

        @Override
        public LogBuilder action(String actionType, String target) {
            this.actionType = actionType;
            this.actionTarget = target;
            return this;
        }

        @Override
        public LogBuilder result(
                boolean success,
                double similarity,
                io.github.jspinak.brobot.model.element.Location location) {
            this.success = success;
            this.similarity = similarity;
            this.location = location;
            return this;
        }

        @Override
        public LogBuilder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public LogBuilder durationSince(Instant startTime) {
            this.duration = Duration.between(startTime, Instant.now());
            return this;
        }

        @Override
        public LogBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        @Override
        public LogBuilder state(String state) {
            this.currentState = state;
            return this;
        }

        @Override
        public LogBuilder error(Throwable error) {
            this.error = error;
            this.errorMessage = error != null ? error.getMessage() : null;
            if (this.level == LogLevel.INFO) { // Auto-escalate to ERROR level
                this.level = LogLevel.ERROR;
            }
            return this;
        }

        @Override
        public LogBuilder error(String errorMessage) {
            this.errorMessage = errorMessage;
            if (this.level == LogLevel.INFO) { // Auto-escalate to ERROR level
                this.level = LogLevel.ERROR;
            }
            return this;
        }

        @Override
        public LogBuilder memory(long memoryBytes) {
            this.memoryBytes = memoryBytes;
            return this;
        }

        @Override
        public LogBuilder operationCount(long count) {
            this.operationCount = count;
            return this;
        }

        @Override
        public void log() {
            if (!logger.isLoggingEnabled(category, level)) {
                return;
            }

            LogEntry entry =
                    LogEntry.builder()
                            .timestamp(Instant.now())
                            .category(category)
                            .level(level)
                            .message(message)
                            .threadName(Thread.currentThread().getName())
                            .actionType(actionType)
                            .actionTarget(actionTarget)
                            .success(success)
                            .similarity(similarity)
                            .location(location)
                            .duration(duration)
                            .currentState(currentState)
                            .error(error)
                            .errorMessage(errorMessage)
                            .memoryUsage(memoryBytes)
                            .operationCount(operationCount)
                            .metadata(context)
                            .build();

            // Override correlation ID if explicitly set
            if (correlationId != null) {
                entry = entry.withCorrelation(correlationId, null);
            }

            logger.logEntryWithCorrelation(entry);
        }
    }
}
