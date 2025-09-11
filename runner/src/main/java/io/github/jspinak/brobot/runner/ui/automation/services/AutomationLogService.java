package io.github.jspinak.brobot.runner.ui.automation.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing automation execution logs. Handles log formatting, buffering, and UI
 * updates.
 */
@Slf4j
@Service
public class AutomationLogService {

    // Time formatter is created from configuration
    private static final int MAX_LOG_LINES = 10000;
    private static final int BATCH_SIZE = 50;
    private static final long BATCH_DELAY_MS = 100;

    // Log configuration
    private LogConfiguration configuration = LogConfiguration.builder().build();

    // Log storage
    private final List<LogEntry> logHistory = new ArrayList<>();
    private final BlockingQueue<LogEntry> pendingLogs = new LinkedBlockingQueue<>();

    // UI components
    private TextArea logTextArea;
    private final AtomicBoolean autoScroll = new AtomicBoolean(true);

    // Batch processing
    private Thread batchProcessor;
    private final AtomicBoolean processorRunning = new AtomicBoolean(false);

    /** Log configuration. */
    public static class LogConfiguration {
        private boolean timestampEnabled = true;
        private boolean colorCoding = true;
        private int maxLines = MAX_LOG_LINES;
        private String timeFormat = "HH:mm:ss";
        private boolean batchingEnabled = true;

        public static LogConfigurationBuilder builder() {
            return new LogConfigurationBuilder();
        }

        public static class LogConfigurationBuilder {
            private LogConfiguration config = new LogConfiguration();

            public LogConfigurationBuilder timestampEnabled(boolean enabled) {
                config.timestampEnabled = enabled;
                return this;
            }

            public LogConfigurationBuilder colorCoding(boolean enabled) {
                config.colorCoding = enabled;
                return this;
            }

            public LogConfigurationBuilder maxLines(int lines) {
                config.maxLines = lines;
                return this;
            }

            public LogConfigurationBuilder timeFormat(String format) {
                config.timeFormat = format;
                return this;
            }

            public LogConfigurationBuilder batchingEnabled(boolean enabled) {
                config.batchingEnabled = enabled;
                return this;
            }

            public LogConfiguration build() {
                return config;
            }
        }
    }

    /** Sets the log configuration. */
    public void setConfiguration(LogConfiguration configuration) {
        this.configuration = configuration;
    }

    /** Sets the text area for log display. */
    public void setLogTextArea(TextArea textArea) {
        this.logTextArea = textArea;

        // Start batch processor if enabled
        if (configuration.batchingEnabled && !processorRunning.get()) {
            startBatchProcessor();
        }
    }

    /** Sets auto-scroll behavior. */
    public void setAutoScroll(boolean enabled) {
        autoScroll.set(enabled);
    }

    /** Logs a message. */
    public void log(String message) {
        log(message, LogLevel.INFO);
    }

    /** Logs a message with specific level. */
    public void log(String message, LogLevel level) {
        LogEntry entry = new LogEntry(LocalDateTime.now(), message, level);

        // Add to history
        synchronized (logHistory) {
            logHistory.add(entry);

            // Trim history if needed
            if (logHistory.size() > configuration.maxLines) {
                logHistory.subList(0, logHistory.size() - configuration.maxLines).clear();
            }
        }

        // Queue for UI update
        if (logTextArea != null) {
            if (configuration.batchingEnabled) {
                pendingLogs.offer(entry);
            } else {
                updateUI(entry);
            }
        }

        // Also log to standard logger
        switch (level) {
            case ERROR:
                log.error(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case DEBUG:
                log.debug(message);
                break;
            default:
                log.info(message);
        }
    }

    /** Logs an error with exception. */
    public void logError(String message, Throwable throwable) {
        log(message + ": " + throwable.getMessage(), LogLevel.ERROR);
        if (throwable.getCause() != null) {
            log("Caused by: " + throwable.getCause().getMessage(), LogLevel.ERROR);
        }
    }

    /** Clears the log. */
    public void clear() {
        synchronized (logHistory) {
            logHistory.clear();
        }
        pendingLogs.clear();

        if (logTextArea != null) {
            Platform.runLater(() -> logTextArea.clear());
        }
    }

    /** Gets the log history. */
    public List<LogEntry> getHistory() {
        synchronized (logHistory) {
            return new ArrayList<>(logHistory);
        }
    }

    /** Exports logs to string. */
    public String exportLogs() {
        StringBuilder sb = new StringBuilder();
        synchronized (logHistory) {
            for (LogEntry entry : logHistory) {
                sb.append(formatLogEntry(entry)).append("\n");
            }
        }
        return sb.toString();
    }

    /** Updates the UI with a log entry. */
    private void updateUI(LogEntry entry) {
        if (logTextArea == null) {
            return;
        }

        Platform.runLater(
                () -> {
                    String formatted = formatLogEntry(entry);
                    logTextArea.appendText(formatted + "\n");

                    if (autoScroll.get()) {
                        logTextArea.setScrollTop(Double.MAX_VALUE);
                    }

                    // Trim if needed
                    if (logTextArea.getLength() > configuration.maxLines * 100) { // Rough estimate
                        String text = logTextArea.getText();
                        int trimPoint = text.length() / 2;
                        int newlineIndex = text.indexOf('\n', trimPoint);
                        if (newlineIndex > 0) {
                            logTextArea.deleteText(0, newlineIndex + 1);
                        }
                    }
                });
    }

    /** Formats a log entry. */
    private String formatLogEntry(LogEntry entry) {
        StringBuilder sb = new StringBuilder();

        if (configuration.timestampEnabled) {
            sb.append("[")
                    .append(
                            entry.getTimestamp()
                                    .format(DateTimeFormatter.ofPattern(configuration.timeFormat)))
                    .append("] ");
        }

        if (configuration.colorCoding) {
            // Add level prefix
            sb.append(entry.getLevel().getPrefix()).append(" ");
        }

        sb.append(entry.getMessage());

        return sb.toString();
    }

    /** Starts the batch processor. */
    private void startBatchProcessor() {
        if (processorRunning.compareAndSet(false, true)) {
            batchProcessor = new Thread(this::processBatches, "AutomationLog-BatchProcessor");
            batchProcessor.setDaemon(true);
            batchProcessor.start();
        }
    }

    /** Processes log batches. */
    private void processBatches() {
        List<LogEntry> batch = new ArrayList<>();

        while (processorRunning.get()) {
            try {
                // Wait for first log
                LogEntry first = pendingLogs.take();
                batch.add(first);

                // Collect more logs for batch
                Thread.sleep(BATCH_DELAY_MS);
                pendingLogs.drainTo(batch, BATCH_SIZE - 1);

                // Update UI with batch
                if (!batch.isEmpty() && logTextArea != null) {
                    updateUIBatch(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in batch processor", e);
            }
        }
    }

    /** Updates UI with a batch of log entries. */
    private void updateUIBatch(List<LogEntry> batch) {
        Platform.runLater(
                () -> {
                    if (logTextArea == null) {
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (LogEntry entry : batch) {
                        sb.append(formatLogEntry(entry)).append("\n");
                    }

                    logTextArea.appendText(sb.toString());

                    if (autoScroll.get()) {
                        logTextArea.setScrollTop(Double.MAX_VALUE);
                    }
                });
    }

    /** Stops the batch processor. */
    public void stop() {
        processorRunning.set(false);
        if (batchProcessor != null) {
            batchProcessor.interrupt();
        }
    }

    /** Log entry data class. */
    public static class LogEntry {
        private final LocalDateTime timestamp;
        private final String message;
        private final LogLevel level;

        public LogEntry(LocalDateTime timestamp, String message, LogLevel level) {
            this.timestamp = timestamp;
            this.message = message;
            this.level = level;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public LogLevel getLevel() {
            return level;
        }
    }

    /** Log levels. */
    public enum LogLevel {
        DEBUG("[DEBUG]"),
        INFO("[INFO]"),
        WARN("[WARN]"),
        ERROR("[ERROR]");

        private final String prefix;

        LogLevel(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    /** Gets log statistics. */
    public LogStatistics getStatistics() {
        synchronized (logHistory) {
            int totalLogs = logHistory.size();
            int errors = 0;
            int warnings = 0;
            int debugs = 0;

            for (LogEntry entry : logHistory) {
                switch (entry.getLevel()) {
                    case ERROR:
                        errors++;
                        break;
                    case WARN:
                        warnings++;
                        break;
                    case DEBUG:
                        debugs++;
                        break;
                    case INFO:
                        // INFO logs are counted in totalLogs
                        break;
                }
            }

            return new LogStatistics(totalLogs, errors, warnings, debugs);
        }
    }

    /** Log statistics. */
    public static class LogStatistics {
        private final int totalLogs;
        private final int errors;
        private final int warnings;
        private final int debugs;

        public LogStatistics(int totalLogs, int errors, int warnings, int debugs) {
            this.totalLogs = totalLogs;
            this.errors = errors;
            this.warnings = warnings;
            this.debugs = debugs;
        }

        public int getTotalLogs() {
            return totalLogs;
        }

        public int getErrors() {
            return errors;
        }

        public int getWarnings() {
            return warnings;
        }

        public int getDebugs() {
            return debugs;
        }
    }
}
