package io.github.jspinak.brobot.logging.formatter;

import io.github.jspinak.brobot.logging.LogEntry;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Structured log formatter with consistent field placement.
 *
 * <p>Produces structured, parseable log output with consistent field
 * positioning and comprehensive metadata inclusion. Suitable for
 * log aggregation systems and detailed analysis.
 *
 * <p>Format pattern:
 * <pre>
 * timestamp=2023-12-01T14:23:45.123Z category=ACTIONS level=INFO thread=main
 * correlation=abc123 session=xyz789 message="Clicked submit button"
 * action.type=CLICK action.target=submitButton action.success=true
 * timing.duration=0.025s performance.memory=1024KB metadata={key=value}
 * </pre>
 *
 * <p>Examples:
 * <pre>
 * timestamp=2023-12-01T14:23:45.123Z category=ACTIONS level=INFO
 * correlation=abc123 message="Clicked submit button" action.type=CLICK
 * action.target=submitButton action.success=true timing.duration=0.025s
 *
 * timestamp=2023-12-01T14:23:45.148Z category=MATCHING level=DEBUG
 * correlation=abc123 message="Pattern match completed" match.similarity=0.95
 * timing.duration=0.012s location.x=150 location.y=300
 *
 * timestamp=2023-12-01T14:23:45.160Z category=PERFORMANCE level=WARN
 * correlation=abc123 message="Slow operation detected" timing.duration=2.340s
 * performance.memory=15.2MB performance.operations=1250
 * </pre>
 *
 * <p>Features:
 * <ul>
 *   <li>ISO 8601 timestamp format
 *   <li>Key-value pairs for easy parsing
 *   <li>Hierarchical field names (action.type, timing.duration)
 *   <li>Complete metadata inclusion
 *   <li>Consistent field ordering
 *   <li>Proper escaping of special characters
 * </ul>
 */
@Component
public class StructuredLogFormatter implements LogFormatter {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    public String format(LogEntry entry) {
        StringBuilder sb = new StringBuilder();

        // Core fields
        appendField(sb, "timestamp", entry.getTimestamp().atZone(java.time.ZoneOffset.UTC).format(ISO_FORMATTER));
        appendField(sb, "category", entry.getCategory());
        appendField(sb, "level", entry.getLevel());

        // Thread information
        if (entry.getThreadName() != null) {
            appendField(sb, "thread", entry.getThreadName());
        }

        // Correlation context
        if (entry.hasCorrelation()) {
            appendField(sb, "correlation", entry.getCorrelationId());
        }
        if (entry.getSessionId() != null) {
            appendField(sb, "session", entry.getSessionId());
        }
        if (entry.getOperationName() != null) {
            appendField(sb, "operation", entry.getOperationName());
        }

        // Message
        if (entry.getMessage() != null) {
            appendField(sb, "message", escapeValue(entry.getMessage()));
        }

        // Action context
        if (entry.getActionType() != null) {
            appendField(sb, "action.type", entry.getActionType());
        }
        if (entry.getActionTarget() != null) {
            appendField(sb, "action.target", entry.getActionTarget());
        }
        if (entry.getSuccess() != null) {
            appendField(sb, "action.success", entry.getSuccess());
        }

        // Matching context
        if (entry.getSimilarity() != null) {
            appendField(sb, "match.similarity", String.format("%.3f", entry.getSimilarity()));
        }

        // Location context
        if (entry.hasLocation()) {
            appendField(sb, "location.x", entry.getLocation().getX());
            appendField(sb, "location.y", entry.getLocation().getY());
        }

        // Timing information
        if (entry.hasTiming()) {
            appendField(sb, "timing.duration", entry.getFormattedDuration());
        }
        if (entry.getOperationStartTime() != null) {
            appendField(sb, "timing.startTime", entry.getOperationStartTime().atZone(java.time.ZoneOffset.UTC).format(ISO_FORMATTER));
        }
        if (entry.getOperationDepth() != null) {
            appendField(sb, "timing.depth", entry.getOperationDepth());
        }

        // Performance metrics
        if (entry.getMemoryUsage() != null) {
            appendField(sb, "performance.memory", formatMemory(entry.getMemoryUsage()));
        }
        if (entry.getOperationCount() != null) {
            appendField(sb, "performance.operations", entry.getOperationCount());
        }

        // State context
        if (entry.getCurrentState() != null) {
            appendField(sb, "state.current", entry.getCurrentState());
        }
        if (entry.getTargetState() != null) {
            appendField(sb, "state.target", entry.getTargetState());
        }

        // Error information
        if (entry.isError()) {
            appendField(sb, "error.present", true);
            if (entry.getErrorMessage() != null) {
                appendField(sb, "error.message", escapeValue(entry.getErrorMessage()));
            }
            if (entry.getError() != null) {
                appendField(sb, "error.type", entry.getError().getClass().getSimpleName());
            }
        }

        // Custom metadata
        if (!entry.getMetadata().isEmpty()) {
            appendMetadata(sb, entry.getMetadata());
        }

        // Remove trailing space
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.STRUCTURED;
    }

    @Override
    public String getName() {
        return "Structured";
    }

    /**
     * Append a field to the structured output.
     *
     * @param sb The StringBuilder to append to
     * @param key The field key
     * @param value The field value
     */
    private void appendField(StringBuilder sb, String key, Object value) {
        if (value != null) {
            sb.append(key).append("=").append(value).append(" ");
        }
    }

    /**
     * Append metadata fields to the structured output.
     *
     * @param sb The StringBuilder to append to
     * @param metadata The metadata map
     */
    private void appendMetadata(StringBuilder sb, Map<String, Object> metadata) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = "meta." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                appendField(sb, key, escapeValue((String) value));
            } else {
                appendField(sb, key, value);
            }
        }
    }

    /**
     * Escape special characters in string values.
     *
     * @param value The string value to escape
     * @return The escaped string
     */
    private String escapeValue(String value) {
        if (value == null) {
            return null;
        }

        // If the value contains spaces, quotes, or special characters, quote it
        if (value.contains(" ") || value.contains("\"") || value.contains("=") || value.contains("\n") || value.contains("\t")) {
            return "\"" + value.replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t") + "\"";
        }

        return value;
    }

    /**
     * Format memory usage in human-readable format.
     *
     * @param bytes Memory in bytes
     * @return Formatted memory string
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