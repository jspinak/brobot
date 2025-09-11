package io.github.jspinak.brobot.runner.errorhandling.processors;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.IErrorProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * Error processor that logs errors with appropriate severity levels. Provides detailed logging for
 * debugging and monitoring.
 */
@Slf4j
public class LoggingErrorProcessor implements IErrorProcessor {

    @Override
    public void process(Throwable error, ErrorContext context) {
        String logMessage = formatLogMessage(error, context);

        // Log based on severity
        switch (context.getSeverity()) {
            case CRITICAL:
                log.error("[CRITICAL] {}", logMessage, error);
                break;

            case HIGH:
                log.error("[HIGH] {}", logMessage, error);
                break;

            case MEDIUM:
                log.warn("[MEDIUM] {}", logMessage);
                break;

            case LOW:
                log.info("[LOW] {}", logMessage);
                break;

            default:
                log.debug("{}", logMessage);
        }
    }

    private String formatLogMessage(Throwable error, ErrorContext context) {
        StringBuilder message = new StringBuilder();

        // Error ID for tracking
        message.append("[").append(context.getErrorId()).append("] ");

        // Category and component
        message.append(context.getCategory().getDisplayName());
        if (context.getComponent() != null) {
            message.append(" in ").append(context.getComponent());
        }

        // Operation
        message.append(" - ").append(context.getOperation());

        // Error message
        message.append(": ").append(error.getMessage());

        // Additional context if available
        if (context.getSessionId() != null) {
            message.append(" [Session: ").append(context.getSessionId()).append("]");
        }

        if (context.getUserId() != null) {
            message.append(" [User: ").append(context.getUserId()).append("]");
        }

        // System state if critical
        if (context.getSeverity().getLevel() >= ErrorContext.ErrorSeverity.HIGH.getLevel()) {
            message.append(" [Memory: ")
                    .append(formatBytes(context.getMemoryUsed()))
                    .append(", Threads: ")
                    .append(context.getActiveThreads())
                    .append(", CPU: ")
                    .append(String.format("%.1f%%", context.getCpuUsage() * 100))
                    .append("]");
        }

        return message.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1fGB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
