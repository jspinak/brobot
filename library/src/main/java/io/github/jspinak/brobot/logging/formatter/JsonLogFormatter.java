package io.github.jspinak.brobot.logging.formatter;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.jspinak.brobot.logging.LogEntry;

/**
 * JSON log formatter for machine processing.
 *
 * <p>Produces structured JSON output suitable for log aggregation systems, SIEM tools, and
 * automated log analysis. The JSON format preserves all data types and allows for easy querying and
 * filtering.
 *
 * <p>Output structure:
 *
 * <pre>{@code
 * {
 *   "@timestamp": "2023-12-01T14:23:45.123Z",
 *   "@version": "1",
 *   "category": "ACTIONS",
 *   "level": "INFO",
 *   "message": "Clicked submit button",
 *   "thread": "main",
 *   "correlation": {
 *     "id": "abc123",
 *     "session": "xyz789",
 *     "operation": "user-login"
 *   },
 *   "action": {
 *     "type": "CLICK",
 *     "target": "submitButton",
 *     "success": true
 *   },
 *   "timing": {
 *     "duration": 25,
 *     "startTime": "2023-12-01T14:23:45.098Z"
 *   },
 *   "location": {
 *     "x": 150,
 *     "y": 300
 *   },
 *   "performance": {
 *     "memory": 1048576,
 *     "operations": 1
 *   },
 *   "metadata": {
 *     "custom": "value"
 *   }
 * }
 * }</pre>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>ELK Stack compatible format
 *   <li>ISO 8601 timestamps with @timestamp field
 *   <li>Hierarchical structure for related fields
 *   <li>Type preservation (numbers, booleans, strings)
 *   <li>Complete metadata inclusion
 *   <li>Error details with stack traces
 *   <li>Compact JSON without pretty printing
 * </ul>
 */
@Component
public class JsonLogFormatter implements LogFormatter {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private final ObjectMapper objectMapper;

    public JsonLogFormatter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String format(LogEntry entry) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            // ELK Stack standard fields
            root.put(
                    "@timestamp",
                    entry.getTimestamp().atZone(java.time.ZoneOffset.UTC).format(ISO_FORMATTER));
            root.put("@version", "1");

            // Core fields
            root.put("category", entry.getCategory().toString());
            root.put("level", entry.getLevel().toString());

            if (entry.getMessage() != null) {
                root.put("message", entry.getMessage());
            }

            if (entry.getThreadName() != null) {
                root.put("thread", entry.getThreadName());
            }

            // Correlation context
            if (entry.hasCorrelation()
                    || entry.getSessionId() != null
                    || entry.getOperationName() != null) {
                ObjectNode correlation = root.putObject("correlation");
                if (entry.hasCorrelation()) {
                    correlation.put("id", entry.getCorrelationId());
                }
                if (entry.getSessionId() != null) {
                    correlation.put("session", entry.getSessionId());
                }
                if (entry.getOperationName() != null) {
                    correlation.put("operation", entry.getOperationName());
                }
            }

            // Action context
            if (entry.getActionType() != null
                    || entry.getActionTarget() != null
                    || entry.getSuccess() != null) {
                ObjectNode action = root.putObject("action");
                if (entry.getActionType() != null) {
                    action.put("type", entry.getActionType());
                }
                if (entry.getActionTarget() != null) {
                    action.put("target", entry.getActionTarget());
                }
                if (entry.getSuccess() != null) {
                    action.put("success", entry.getSuccess());
                }
            }

            // Matching context
            if (entry.getSimilarity() != null) {
                ObjectNode match = root.putObject("match");
                match.put("similarity", entry.getSimilarity());
            }

            // Location context
            if (entry.hasLocation()) {
                ObjectNode location = root.putObject("location");
                location.put("x", entry.getLocation().getX());
                location.put("y", entry.getLocation().getY());
            }

            // Timing information
            if (entry.hasTiming()
                    || entry.getOperationStartTime() != null
                    || entry.getOperationDepth() != null) {
                ObjectNode timing = root.putObject("timing");
                if (entry.hasTiming()) {
                    timing.put("duration", entry.getDurationMs());
                    timing.put("durationFormatted", entry.getFormattedDuration());
                }
                if (entry.getOperationStartTime() != null) {
                    timing.put(
                            "startTime",
                            entry.getOperationStartTime()
                                    .atZone(java.time.ZoneOffset.UTC)
                                    .format(ISO_FORMATTER));
                }
                if (entry.getOperationDepth() != null) {
                    timing.put("depth", entry.getOperationDepth());
                }
            }

            // Performance metrics
            if (entry.getMemoryUsage() != null || entry.getOperationCount() != null) {
                ObjectNode performance = root.putObject("performance");
                if (entry.getMemoryUsage() != null) {
                    performance.put("memory", entry.getMemoryUsage());
                    performance.put("memoryFormatted", formatMemory(entry.getMemoryUsage()));
                }
                if (entry.getOperationCount() != null) {
                    performance.put("operations", entry.getOperationCount());
                }
            }

            // State context
            if (entry.getCurrentState() != null || entry.getTargetState() != null) {
                ObjectNode state = root.putObject("state");
                if (entry.getCurrentState() != null) {
                    state.put("current", entry.getCurrentState());
                }
                if (entry.getTargetState() != null) {
                    state.put("target", entry.getTargetState());
                }
            }

            // Error information
            if (entry.isError()) {
                ObjectNode error = root.putObject("error");
                error.put("present", true);
                if (entry.getErrorMessage() != null) {
                    error.put("message", entry.getErrorMessage());
                }
                if (entry.getError() != null) {
                    error.put("type", entry.getError().getClass().getName());
                    error.put("simpleType", entry.getError().getClass().getSimpleName());

                    // Include stack trace for detailed analysis
                    if (entry.getError().getStackTrace() != null
                            && entry.getError().getStackTrace().length > 0) {
                        StringBuilder stackTrace = new StringBuilder();
                        for (StackTraceElement element : entry.getError().getStackTrace()) {
                            stackTrace.append(element.toString()).append("\n");
                        }
                        error.put("stackTrace", stackTrace.toString().trim());
                    }
                }
            }

            // Custom metadata
            if (!entry.getMetadata().isEmpty()) {
                ObjectNode metadata = root.putObject("metadata");
                for (java.util.Map.Entry<String, Object> metaEntry :
                        entry.getMetadata().entrySet()) {
                    addJsonValue(metadata, metaEntry.getKey(), metaEntry.getValue());
                }
            }

            // Additional fields for searchability
            root.put("hasError", entry.isError());
            root.put("isAction", entry.getActionType() != null);
            root.put("hasCorrelation", entry.hasCorrelation());
            root.put("hasTiming", entry.hasTiming());
            root.put("hasLocation", entry.hasLocation());

            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            // Fallback to simple format if JSON serialization fails
            return String.format(
                    "{\"@timestamp\":\"%s\",\"level\":\"ERROR\",\"message\":\"JSON formatting"
                            + " failed: %s\",\"originalMessage\":\"%s\"}",
                    entry.getTimestamp().atZone(java.time.ZoneOffset.UTC).format(ISO_FORMATTER),
                    e.getMessage(),
                    entry.getMessage() != null ? entry.getMessage().replace("\"", "\\\"") : "null");
        }
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.JSON;
    }

    @Override
    public String getName() {
        return "JSON";
    }

    /**
     * Add a value to a JSON object node with proper type handling.
     *
     * @param node The JSON object node
     * @param key The field key
     * @param value The value to add
     */
    private void addJsonValue(ObjectNode node, String key, Object value) {
        if (value == null) {
            node.putNull(key);
        } else if (value instanceof String) {
            node.put(key, (String) value);
        } else if (value instanceof Integer) {
            node.put(key, (Integer) value);
        } else if (value instanceof Long) {
            node.put(key, (Long) value);
        } else if (value instanceof Double) {
            node.put(key, (Double) value);
        } else if (value instanceof Float) {
            node.put(key, (Float) value);
        } else if (value instanceof Boolean) {
            node.put(key, (Boolean) value);
        } else {
            // Convert other types to string
            node.put(key, value.toString());
        }
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
