package io.github.jspinak.brobot.logging.formatter;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.LogEntry;

/**
 * Simple human-readable log formatter.
 *
 * <p>Produces concise, readable log output suitable for console display and development debugging.
 * Focuses on the most important information while keeping the output compact.
 *
 * <p>Format pattern:
 *
 * <pre>
 * [HH:mm:ss.SSS] [CATEGORY] LEVEL - MESSAGE [ACTION->TARGET] (duration) [correlation]
 * </pre>
 *
 * <p>Examples:
 *
 * <pre>
 * [14:23:45.123] [ACTIONS] INFO - Clicked submit button [CLICK->submitButton] (0.025s) [abc123]
 * [14:23:45.148] [MATCHING] DEBUG - Found pattern with 95% similarity (0.012s)
 * [14:23:45.160] [PERFORMANCE] WARN - Operation took longer than expected (2.340s)
 * [14:23:45.162] [ACTIONS] ERROR - Failed to find element [FIND->loginForm] [abc123]
 * </pre>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Compact timestamp format (HH:mm:ss.SSS)
 *   <li>Category and level for quick filtering
 *   <li>Action context when available
 *   <li>Duration in human-readable format
 *   <li>Correlation ID for tracking
 *   <li>Error indicators
 * </ul>
 */
@Component
public class SimpleLogFormatter implements LogFormatter {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Override
    public String format(LogEntry entry) {
        StringBuilder sb = new StringBuilder();

        // Timestamp
        sb.append("[")
                .append(
                        entry.getTimestamp()
                                .atZone(java.time.ZoneId.systemDefault())
                                .format(TIME_FORMATTER))
                .append("]");

        // Category
        sb.append(" [").append(entry.getCategory()).append("]");

        // Level
        sb.append(" ").append(entry.getLevel());

        // Message
        sb.append(" - ").append(entry.getMessage() != null ? entry.getMessage() : "");

        // Action context
        if (entry.getActionType() != null) {
            sb.append(" [").append(entry.getActionType());
            if (entry.getActionTarget() != null) {
                sb.append("->").append(entry.getActionTarget());
            }
            sb.append("]");
        }

        // Success/failure indicator for actions
        if (entry.getActionType() != null && entry.getSuccess() != null) {
            if (entry.getSuccess()) {
                sb.append(" ✓");
            } else {
                sb.append(" ✗");
            }
        }

        // Similarity score for matching operations
        if (entry.getSimilarity() != null) {
            sb.append(" (")
                    .append(String.format("%.0f%%", entry.getSimilarity() * 100))
                    .append(")");
        }

        // Duration
        if (entry.hasTiming()) {
            sb.append(" (").append(entry.getFormattedDuration()).append(")");
        }

        // Location (simplified)
        if (entry.hasLocation()) {
            sb.append(" @(")
                    .append(entry.getLocation().getX())
                    .append(",")
                    .append(entry.getLocation().getY())
                    .append(")");
        }

        // Error indicator
        if (entry.isError()) {
            sb.append(" ⚠");
            if (entry.getErrorMessage() != null) {
                sb.append(" ").append(entry.getErrorMessage());
            }
        }

        // Correlation ID (disabled for cleaner logs - uncomment if needed for debugging)
        // if (entry.hasCorrelation()) {
        //     sb.append(" [").append(entry.getCorrelationId()).append("]");
        // }

        // Memory usage (if significant)
        if (entry.getMemoryUsage() != null && entry.getMemoryUsage() > 1024 * 1024) { // > 1MB
            sb.append(" (").append(formatMemory(entry.getMemoryUsage())).append(")");
        }

        return sb.toString();
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.TEXT;
    }

    @Override
    public String getName() {
        return "Simple";
    }

    /**
     * Format memory usage in human-readable format.
     *
     * @param bytes Memory in bytes
     * @return Formatted memory string (e.g., "1.5MB", "2.3GB")
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1fGB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
