package io.github.jspinak.brobot.logging.unified.console;

import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig.VerbosityLevel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Formats log events for console output with appropriate styling and structure.
 * Supports both normal and verbose output modes with color coding and symbols.
 */
@Component
public class ConsoleFormatter {
    
    // ANSI color codes for cross-platform compatibility
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BRIGHT_RED = "\u001B[91m";
    private static final String BRIGHT_GREEN = "\u001B[92m";
    private static final String BRIGHT_YELLOW = "\u001B[93m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String DIM = "\u001B[2m";
    private static final String BOLD = "\u001B[1m";
    
    // Unicode symbols that work across most terminals
    private static final String SUCCESS_SYMBOL = "✓"; // Check mark
    private static final String FAILURE_SYMBOL = "✗"; // X mark
    private static final String ACTION_SYMBOL = "▶"; // Play symbol
    private static final String TRANSITION_SYMBOL = "→"; // Arrow
    private static final String WARNING_SYMBOL = "⚠"; // Warning triangle
    private static final String ERROR_SYMBOL = "⚠"; // Warning triangle (fallback for cross-platform)
    private static final String INFO_SYMBOL = "ℹ"; // Info symbol
    private static final String DEBUG_SYMBOL = "•"; // Bullet point
    
    private final LoggingVerbosityConfig verbosityConfig;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public ConsoleFormatter(LoggingVerbosityConfig verbosityConfig) {
        this.verbosityConfig = verbosityConfig;
    }
    
    /**
     * Formats a log event for console output based on current verbosity settings.
     */
    public String format(LogEvent event) {
        if (verbosityConfig.getVerbosity() == VerbosityLevel.NORMAL) {
            return formatNormal(event);
        } else {
            return formatVerbose(event);
        }
    }
    
    /**
     * Formats a log event in normal mode - concise and focused on essentials.
     */
    private String formatNormal(LogEvent event) {
        StringBuilder sb = new StringBuilder();
        
        // Time (if enabled)
        if (verbosityConfig.getNormal().isShowTiming()) {
            LocalDateTime time = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getTimestamp()), 
                ZoneId.systemDefault()
            );
            sb.append(DIM).append(timeFormatter.format(time)).append(RESET).append(" ");
        }
        
        // Type-specific formatting
        switch (event.getType()) {
            case ACTION:
                formatActionNormal(sb, event);
                break;
            case TRANSITION:
                formatTransitionNormal(sb, event);
                break;
            case OBSERVATION:
                formatObservationNormal(sb, event);
                break;
            case ERROR:
                formatErrorNormal(sb, event);
                break;
            case PERFORMANCE:
                formatPerformanceNormal(sb, event);
                break;
            default:
                formatDefaultNormal(sb, event);
        }
        
        return sb.toString();
    }
    
    /**
     * Formats a log event in verbose mode - detailed with all available information.
     */
    private String formatVerbose(LogEvent event) {
        StringBuilder sb = new StringBuilder();
        
        // Timestamp
        LocalDateTime time = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(event.getTimestamp()), 
            ZoneId.systemDefault()
        );
        sb.append(DIM).append(timeFormatter.format(time)).append(RESET).append(" ");
        
        // Level indicator
        sb.append(formatLevel(event.getLevel())).append(" ");
        
        // Type-specific formatting
        switch (event.getType()) {
            case ACTION:
                formatActionVerbose(sb, event);
                break;
            case TRANSITION:
                formatTransitionVerbose(sb, event);
                break;
            case OBSERVATION:
                formatObservationVerbose(sb, event);
                break;
            case ERROR:
                formatErrorVerbose(sb, event);
                break;
            case PERFORMANCE:
                formatPerformanceVerbose(sb, event);
                break;
            default:
                formatDefaultVerbose(sb, event);
        }
        
        // Metadata in verbose mode
        if (!event.getMetadata().isEmpty()) {
            sb.append(DIM).append(" [");
            boolean first = true;
            for (Map.Entry<String, Object> entry : event.getMetadata().entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("]").append(RESET);
        }
        
        return sb.toString();
    }
    
    // Normal mode formatters
    
    private void formatActionNormal(StringBuilder sb, LogEvent event) {
        sb.append(BLUE).append(ACTION_SYMBOL).append(" ");
        
        if (event.getAction() != null) {
            sb.append(BOLD).append(event.getAction()).append(RESET).append(": ");
        }
        
        if (event.getTarget() != null) {
            sb.append(event.getTarget());
        } else if (event.getMessage() != null) {
            sb.append(event.getMessage());
        }
        
        // Success/failure indicator
        if (event.isSuccess()) {
            sb.append(" ").append(GREEN).append(SUCCESS_SYMBOL).append(RESET);
        } else {
            sb.append(" ").append(RED).append(FAILURE_SYMBOL).append(RESET);
        }
        
        // Match coordinates if enabled
        if (verbosityConfig.getNormal().isShowMatchCoordinates() && event.getMetadata().containsKey("matchX")) {
            sb.append(" ").append(DIM).append("[")
              .append(event.getMetadata().get("matchX")).append(",")
              .append(event.getMetadata().get("matchY")).append("]")
              .append(RESET);
        }
    }
    
    private void formatTransitionNormal(StringBuilder sb, LogEvent event) {
        sb.append(PURPLE).append(TRANSITION_SYMBOL).append(" ");
        
        if (event.getFromState() != null && event.getToState() != null) {
            sb.append(event.getFromState())
              .append(" ").append(PURPLE).append(TRANSITION_SYMBOL).append(RESET).append(" ")
              .append(BOLD).append(event.getToState()).append(RESET);
        } else if (event.getMessage() != null) {
            sb.append(event.getMessage());
        }
        
        // Success indicator
        if (event.isSuccess()) {
            sb.append(" ").append(GREEN).append(SUCCESS_SYMBOL).append(RESET);
        } else {
            sb.append(" ").append(RED).append(FAILURE_SYMBOL).append(RESET);
        }
        
        // Duration if timing enabled
        if (verbosityConfig.getNormal().isShowTiming() && event.getDuration() != null) {
            sb.append(" ").append(DIM).append("(").append(event.getDuration()).append("ms)").append(RESET);
        }
    }
    
    private void formatObservationNormal(StringBuilder sb, LogEvent event) {
        // Only show important observations in normal mode
        if (event.getLevel() == LogEvent.Level.INFO || 
            event.getLevel() == LogEvent.Level.WARNING || 
            event.getLevel() == LogEvent.Level.ERROR) {
            
            String symbol = INFO_SYMBOL;
            String color = CYAN;
            
            if (event.getLevel() == LogEvent.Level.WARNING) {
                symbol = WARNING_SYMBOL;
                color = YELLOW;
            } else if (event.getLevel() == LogEvent.Level.ERROR) {
                symbol = ERROR_SYMBOL;
                color = RED;
            }
            
            sb.append(color).append(symbol).append(" ").append(RESET);
            sb.append(event.getMessage());
        }
    }
    
    private void formatErrorNormal(StringBuilder sb, LogEvent event) {
        sb.append(BRIGHT_RED).append(ERROR_SYMBOL).append(" ERROR: ").append(RESET);
        sb.append(RED).append(event.getMessage()).append(RESET);
        
        if (event.getError() != null) {
            sb.append("\n  ").append(DIM).append(event.getError().getClass().getSimpleName())
              .append(": ").append(event.getError().getMessage()).append(RESET);
        }
    }
    
    private void formatPerformanceNormal(StringBuilder sb, LogEvent event) {
        // Only show performance in verbose mode
    }
    
    private void formatDefaultNormal(StringBuilder sb, LogEvent event) {
        if (event.getMessage() != null) {
            sb.append(event.getMessage());
        }
    }
    
    // Verbose mode formatters
    
    private void formatActionVerbose(StringBuilder sb, LogEvent event) {
        sb.append(BLUE).append("[ACTION]").append(RESET).append(" ");
        formatActionNormal(sb, event);
    }
    
    private void formatTransitionVerbose(StringBuilder sb, LogEvent event) {
        sb.append(PURPLE).append("[TRANSITION]").append(RESET).append(" ");
        formatTransitionNormal(sb, event);
    }
    
    private void formatObservationVerbose(StringBuilder sb, LogEvent event) {
        sb.append(CYAN).append("[OBSERVE]").append(RESET).append(" ");
        sb.append(event.getMessage());
    }
    
    private void formatErrorVerbose(StringBuilder sb, LogEvent event) {
        sb.append(BRIGHT_RED).append("[ERROR]").append(RESET).append(" ");
        formatErrorNormal(sb, event);
    }
    
    private void formatPerformanceVerbose(StringBuilder sb, LogEvent event) {
        sb.append(GREEN).append("[PERF]").append(RESET).append(" ");
        sb.append(event.getMessage());
        if (event.getDuration() != null) {
            sb.append(" - ").append(BOLD).append(event.getDuration()).append("ms").append(RESET);
        }
    }
    
    private void formatDefaultVerbose(StringBuilder sb, LogEvent event) {
        sb.append("[").append(event.getType()).append("] ");
        formatDefaultNormal(sb, event);
    }
    
    // Helper methods
    
    private String formatLevel(LogEvent.Level level) {
        switch (level) {
            case ERROR:
                return BRIGHT_RED + "ERROR" + RESET;
            case WARNING:
                return BRIGHT_YELLOW + "WARN " + RESET;
            case INFO:
                return BRIGHT_GREEN + "INFO " + RESET;
            case DEBUG:
                return DIM + "DEBUG" + RESET;
            default:
                return DIM + "TRACE" + RESET;
        }
    }
}