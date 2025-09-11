package io.github.jspinak.brobot.runner.ui.log.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.extern.slf4j.Slf4j;

/**
 * Service that adapts between EventBus log events and UI LogEntry models. Handles event
 * subscription, transformation, and buffering.
 */
@Slf4j
@Service
public class LogEventAdapter implements AutoCloseable {

    private static final int DEFAULT_BUFFER_SIZE = 1000;
    private static final long BUFFER_POLL_TIMEOUT = 100;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<LogEntry> buffer = new LinkedBlockingQueue<>();

    private EventBus eventBus;
    private Consumer<BrobotEvent> eventHandler;
    private Consumer<LogEntry> logConsumer;
    private Thread processingThread;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /** Configuration for the event adapter. */
    public static class AdapterConfiguration {
        private int bufferSize = DEFAULT_BUFFER_SIZE;
        private boolean useJavaFxThread = true;
        private boolean batchEvents = true;
        private int batchSize = 10;
        private long batchTimeout = 50; // ms

        public static AdapterConfigurationBuilder builder() {
            return new AdapterConfigurationBuilder();
        }

        public static class AdapterConfigurationBuilder {
            private AdapterConfiguration config = new AdapterConfiguration();

            public AdapterConfigurationBuilder bufferSize(int size) {
                config.bufferSize = size;
                return this;
            }

            public AdapterConfigurationBuilder useJavaFxThread(boolean use) {
                config.useJavaFxThread = use;
                return this;
            }

            public AdapterConfigurationBuilder batchEvents(boolean batch) {
                config.batchEvents = batch;
                return this;
            }

            public AdapterConfigurationBuilder batchSize(int size) {
                config.batchSize = size;
                return this;
            }

            public AdapterConfigurationBuilder batchTimeout(long timeout) {
                config.batchTimeout = timeout;
                return this;
            }

            public AdapterConfiguration build() {
                return config;
            }
        }
    }

    private AdapterConfiguration configuration = AdapterConfiguration.builder().build();

    /** Subscribes to EventBus and starts processing events. */
    public void subscribe(EventBus eventBus, Consumer<LogEntry> logConsumer) {
        if (running.get()) {
            log.warn("LogEventAdapter already running");
            return;
        }

        this.eventBus = eventBus;
        this.logConsumer = logConsumer;
        this.eventHandler = this::handleEvent;

        // Subscribe to log events
        eventBus.subscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);

        // Start processing thread
        running.set(true);
        processingThread = new Thread(this::processEvents, "LogEventAdapter-Processor");
        processingThread.setDaemon(true);
        processingThread.start();

        log.info("LogEventAdapter subscribed and started");
    }

    /** Configures the adapter. */
    public void configure(AdapterConfiguration configuration) {
        this.configuration = configuration;
        this.bufferSize = configuration.bufferSize;
    }

    /** Handles incoming events from EventBus. */
    private void handleEvent(BrobotEvent event) {
        try {
            LogEntry entry = transformEvent(event);
            if (entry != null) {
                // Check buffer size
                if (buffer.size() >= bufferSize) {
                    buffer.poll(); // Remove oldest
                    log.debug("Buffer full, removing oldest entry");
                }

                buffer.offer(entry);
            }
        } catch (Exception e) {
            log.error("Error handling event: {}", event, e);
        }
    }

    /** Transforms BrobotEvent to LogEntry. */
    private LogEntry transformEvent(BrobotEvent event) {
        if (event instanceof LogEntryEvent logEntryEvent) {
            LogData logData = logEntryEvent.getLogEntry();
            if (logData != null) {
                return transformLogData(logData);
            }
        } else if (event instanceof LogEvent logEvent) {
            return transformLogEvent(logEvent);
        }

        // Generic event transformation
        return LogEntry.builder()
                .id(generateId())
                .timestamp(LocalDateTime.now())
                .type(event.getEventType().name())
                .level(determineLevel(event))
                .message(event.toString())
                .source("EventBus")
                .build();
    }

    /** Transforms LogData to LogEntry. */
    private LogEntry transformLogData(LogData logData) {
        LocalDateTime timestamp =
                LocalDateTime.ofInstant(logData.getTimestamp(), ZoneId.systemDefault());

        LogEntry.LogLevel level = determineLogLevel(logData);
        String type = logData.getType() != null ? logData.getType().toString() : "SYSTEM";

        return LogEntry.builder()
                .id(generateId())
                .timestamp(timestamp)
                .type(type)
                .level(level)
                .message(logData.getDescription())
                .source(
                        logData.getCurrentStateName() != null
                                ? logData.getCurrentStateName()
                                : "System")
                .details(buildDetails(logData))
                .stateName(logData.getCurrentStateName())
                .actionName(
                        logData.getType() == LogEventType.ACTION
                                ? extractActionName(logData)
                                : null)
                .metadata(
                        logData.getPerformance() != null
                                ? java.util.Map.of(
                                        "duration", logData.getPerformance().getActionDuration())
                                : null)
                .build();
    }

    /** Transforms LogEvent to LogEntry. */
    private LogEntry transformLogEvent(LogEvent logEvent) {
        LocalDateTime timestamp =
                LocalDateTime.ofInstant(logEvent.getTimestamp(), ZoneId.systemDefault());

        LogEntry.LogLevel level = mapLogLevel(logEvent.getLevel());
        String type = tryMapToLogEventType(logEvent.getCategory());

        return LogEntry.builder()
                .id(generateId())
                .timestamp(timestamp)
                .type(type)
                .level(level)
                .message(logEvent.getMessage())
                .source(logEvent.getSource() != null ? logEvent.getSource().toString() : "System")
                .exception(logEvent.getException())
                .build();
    }

    /** Processes events from the buffer. */
    private void processEvents() {
        log.debug("LogEventAdapter processing thread started");

        while (running.get()) {
            try {
                if (configuration.batchEvents) {
                    processBatch();
                } else {
                    processSingle();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("Processing thread interrupted");
                break;
            } catch (Exception e) {
                log.error("Error processing events", e);
            }
        }

        log.debug("LogEventAdapter processing thread stopped");
    }

    /** Processes a single event. */
    private void processSingle() throws InterruptedException {
        LogEntry entry = buffer.poll(BUFFER_POLL_TIMEOUT, TimeUnit.MILLISECONDS);
        if (entry != null && logConsumer != null) {
            deliverEntry(entry);
        }
    }

    /** Processes a batch of events. */
    private void processBatch() throws InterruptedException {
        java.util.List<LogEntry> batch = new java.util.ArrayList<>();

        // Wait for first entry
        LogEntry first = buffer.poll(BUFFER_POLL_TIMEOUT, TimeUnit.MILLISECONDS);
        if (first != null) {
            batch.add(first);

            // Collect more entries up to batch size or timeout
            long deadline = System.currentTimeMillis() + configuration.batchTimeout;
            while (batch.size() < configuration.batchSize
                    && System.currentTimeMillis() < deadline) {
                LogEntry entry = buffer.poll(1, TimeUnit.MILLISECONDS);
                if (entry != null) {
                    batch.add(entry);
                } else {
                    Thread.sleep(1);
                }
            }

            // Deliver batch
            if (!batch.isEmpty() && logConsumer != null) {
                deliverBatch(batch);
            }
        }
    }

    /** Delivers a single entry to the consumer. */
    private void deliverEntry(LogEntry entry) {
        if (configuration.useJavaFxThread) {
            Platform.runLater(() -> logConsumer.accept(entry));
        } else {
            logConsumer.accept(entry);
        }
    }

    /** Delivers a batch of entries to the consumer. */
    private void deliverBatch(java.util.List<LogEntry> batch) {
        if (configuration.useJavaFxThread) {
            Platform.runLater(() -> batch.forEach(logConsumer));
        } else {
            batch.forEach(logConsumer);
        }
    }

    /** Determines log level from LogData. */
    private LogEntry.LogLevel determineLogLevel(LogData logData) {
        if (logData.getType() == LogEventType.ERROR) {
            return LogEntry.LogLevel.ERROR;
        } else if (!logData.isSuccess()) {
            return LogEntry.LogLevel.WARNING;
        } else if (logData.getType() == LogEventType.SYSTEM) {
            return LogEntry.LogLevel.DEBUG;
        } else {
            return LogEntry.LogLevel.INFO;
        }
    }

    /** Maps LogEvent level to LogEntry level. */
    private LogEntry.LogLevel mapLogLevel(LogEvent.LogLevel level) {
        return switch (level) {
            case DEBUG -> LogEntry.LogLevel.DEBUG;
            case INFO -> LogEntry.LogLevel.INFO;
            case WARNING -> LogEntry.LogLevel.WARNING;
            case ERROR -> LogEntry.LogLevel.ERROR;
            case CRITICAL -> LogEntry.LogLevel.FATAL;
        };
    }

    /** Determines level from generic BrobotEvent. */
    private LogEntry.LogLevel determineLevel(BrobotEvent event) {
        return switch (event.getEventType()) {
            case LOG_ERROR -> LogEntry.LogLevel.ERROR;
            case LOG_WARNING -> LogEntry.LogLevel.WARNING;
            default -> LogEntry.LogLevel.INFO;
        };
    }

    /** Tries to map category to LogEventType. */
    private String tryMapToLogEventType(String category) {
        if (category == null) return "SYSTEM";

        try {
            return LogEventType.valueOf(category.toUpperCase()).toString();
        } catch (Exception e) {
            return category.toUpperCase();
        }
    }

    /** Builds details string from LogData. */
    private String buildDetails(LogData logData) {
        StringBuilder details = new StringBuilder();

        if (logData.getErrorMessage() != null) {
            details.append("Error: ").append(logData.getErrorMessage()).append("\n");
        }

        if (logData.getCurrentStateName() != null) {
            details.append("State: ").append(logData.getCurrentStateName()).append("\n");
        }

        if (logData.getPerformance() != null) {
            details.append("Duration: ")
                    .append(logData.getPerformance().getActionDuration())
                    .append(" ms\n");
        }

        return details.toString().trim();
    }

    /** Extracts action name from LogData. */
    private String extractActionName(LogData logData) {
        // Try to extract from description or other fields
        String desc = logData.getDescription();
        if (desc != null && desc.startsWith("Action:")) {
            return desc.substring(7).trim();
        }
        return desc;
    }

    /** Generates unique ID. */
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    /** Gets current buffer size. */
    public int getBufferSize() {
        return buffer.size();
    }

    /** Clears the buffer. */
    public void clearBuffer() {
        buffer.clear();
        log.debug("Buffer cleared");
    }

    /** Sets buffer size limit. */
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    /** Checks if adapter is running. */
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void close() {
        log.info("Closing LogEventAdapter");

        running.set(false);

        // Unsubscribe from events
        if (eventBus != null && eventHandler != null) {
            eventBus.unsubscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
            eventBus.unsubscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
            eventBus.unsubscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);
        }

        // Stop processing thread
        if (processingThread != null) {
            processingThread.interrupt();
            try {
                processingThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Clear buffer
        buffer.clear();

        log.info("LogEventAdapter closed");
    }
}
