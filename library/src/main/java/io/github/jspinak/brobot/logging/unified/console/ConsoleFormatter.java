package io.github.jspinak.brobot.logging.unified.console;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.logging.unified.LogEvent;

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
    
    /**
     * Detects if running in Windows Terminal which supports Unicode and ANSI.
     */
    private static boolean isWindowsTerminal() {
        // Windows Terminal sets WT_SESSION environment variable
        return System.getenv("WT_SESSION") != null;
    }
    
    /**
     * Checks if ANSI colors are explicitly enabled via system property.
     */
    private static boolean isAnsiEnabled() {
        // Check for explicit ANSI enablement
        String ansiProp = System.getProperty("brobot.console.ansi");
        if (ansiProp != null) {
            return Boolean.parseBoolean(ansiProp);
        }
        
        // Check for ConEmu or other terminal emulators that support ANSI
        return System.getenv("ConEmuANSI") != null || 
               System.getenv("ANSICON") != null ||
               System.getenv("TERM") != null;
    }
    
    // Platform detection
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("windows");
    private static final boolean ANSI_COLORS_SUPPORTED = !IS_WINDOWS || isWindowsTerminal() || isAnsiEnabled();
    
    // ANSI color codes (empty strings on unsupported platforms)
    private static final String RESET = ANSI_COLORS_SUPPORTED ? "\u001B[0m" : "";
    private static final String BLACK = ANSI_COLORS_SUPPORTED ? "\u001B[30m" : "";
    private static final String RED = ANSI_COLORS_SUPPORTED ? "\u001B[31m" : "";
    private static final String GREEN = ANSI_COLORS_SUPPORTED ? "\u001B[32m" : "";
    private static final String YELLOW = ANSI_COLORS_SUPPORTED ? "\u001B[33m" : "";
    private static final String BLUE = ANSI_COLORS_SUPPORTED ? "\u001B[34m" : "";
    private static final String PURPLE = ANSI_COLORS_SUPPORTED ? "\u001B[35m" : "";
    private static final String CYAN = ANSI_COLORS_SUPPORTED ? "\u001B[36m" : "";
    private static final String WHITE = ANSI_COLORS_SUPPORTED ? "\u001B[37m" : "";
    private static final String BRIGHT_RED = ANSI_COLORS_SUPPORTED ? "\u001B[91m" : "";
    private static final String BRIGHT_GREEN = ANSI_COLORS_SUPPORTED ? "\u001B[92m" : "";
    private static final String BRIGHT_YELLOW = ANSI_COLORS_SUPPORTED ? "\u001B[93m" : "";
    private static final String BRIGHT_BLUE = ANSI_COLORS_SUPPORTED ? "\u001B[94m" : "";
    private static final String DIM = ANSI_COLORS_SUPPORTED ? "\u001B[2m" : "";
    private static final String BOLD = ANSI_COLORS_SUPPORTED ? "\u001B[1m" : "";
    
    // Symbol sets for different platforms
    private static final boolean UNICODE_SUPPORTED = ANSI_COLORS_SUPPORTED;
    
    // Unicode symbols for Unix-like systems
    private static final String SUCCESS_SYMBOL_UNICODE = "✓"; // Check mark
    private static final String FAILURE_SYMBOL_UNICODE = "✗"; // X mark
    private static final String ACTION_SYMBOL_UNICODE = "▶"; // Play symbol
    private static final String TRANSITION_SYMBOL_UNICODE = "→"; // Arrow
    private static final String WARNING_SYMBOL_UNICODE = "⚠"; // Warning triangle
    private static final String ERROR_SYMBOL_UNICODE = "⚠"; // Warning triangle
    private static final String INFO_SYMBOL_UNICODE = "ℹ"; // Info symbol
    private static final String DEBUG_SYMBOL_UNICODE = "•"; // Bullet point
    
    // ASCII fallbacks for Windows/legacy terminals
    private static final String SUCCESS_SYMBOL_ASCII = "[OK]";
    private static final String FAILURE_SYMBOL_ASCII = "[FAIL]";
    private static final String ACTION_SYMBOL_ASCII = ">";
    private static final String TRANSITION_SYMBOL_ASCII = "->";
    private static final String WARNING_SYMBOL_ASCII = "[WARN]";
    private static final String ERROR_SYMBOL_ASCII = "[ERROR]";
    private static final String INFO_SYMBOL_ASCII = "[INFO]";
    private static final String DEBUG_SYMBOL_ASCII = "*";
    
    // Select appropriate symbols based on platform
    private static final String SUCCESS_SYMBOL = UNICODE_SUPPORTED ? SUCCESS_SYMBOL_UNICODE : SUCCESS_SYMBOL_ASCII;
    private static final String FAILURE_SYMBOL = UNICODE_SUPPORTED ? FAILURE_SYMBOL_UNICODE : FAILURE_SYMBOL_ASCII;
    private static final String ACTION_SYMBOL = UNICODE_SUPPORTED ? ACTION_SYMBOL_UNICODE : ACTION_SYMBOL_ASCII;
    private static final String TRANSITION_SYMBOL = UNICODE_SUPPORTED ? TRANSITION_SYMBOL_UNICODE : TRANSITION_SYMBOL_ASCII;
    private static final String WARNING_SYMBOL = UNICODE_SUPPORTED ? WARNING_SYMBOL_UNICODE : WARNING_SYMBOL_ASCII;
    private static final String ERROR_SYMBOL = UNICODE_SUPPORTED ? ERROR_SYMBOL_UNICODE : ERROR_SYMBOL_ASCII;
    private static final String INFO_SYMBOL = UNICODE_SUPPORTED ? INFO_SYMBOL_UNICODE : INFO_SYMBOL_ASCII;
    private static final String DEBUG_SYMBOL = UNICODE_SUPPORTED ? DEBUG_SYMBOL_UNICODE : DEBUG_SYMBOL_ASCII;
    
    private final LoggingVerbosityConfig verbosityConfig;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public ConsoleFormatter(LoggingVerbosityConfig verbosityConfig) {
        this.verbosityConfig = verbosityConfig;
    }
    
    /**
     * Formats a log event for console output based on current verbosity settings.
     */
    public String format(LogEvent event) {
        if (verbosityConfig.getVerbosity() == VerbosityLevel.QUIET) {
            return formatQuiet(event);
        } else if (verbosityConfig.getVerbosity() == VerbosityLevel.NORMAL) {
            return formatNormal(event);
        } else {
            return formatVerbose(event);
        }
    }
    
    /**
     * Formats a log event in quiet mode - single line output with minimal information.
     */
    private String formatQuiet(LogEvent event) {
        
        // Only format ACTION events that are COMPLETE or FAILED, skip START events
        if (event.getType() == LogEvent.Type.ACTION && event.getAction() != null) {
            String action = event.getAction();
            
            // Skip START events in quiet mode
            if (action.endsWith("_START")) {
                return null; // Return null to indicate this should not be logged
            }
            
            // Only process COMPLETE or FAILED events
            if (action.endsWith("_COMPLETE") || action.endsWith("_FAILED")) {
                StringBuilder sb = new StringBuilder();
                
                // Success/failure symbol
                sb.append(event.isSuccess() ? SUCCESS_SYMBOL : FAILURE_SYMBOL).append(" ");
                
                // Extract base action name (remove _COMPLETE/_FAILED suffix)
                String baseAction = action.replace("_COMPLETE", "").replace("_FAILED", "");
                // Convert to proper case (FIND -> Find, CLICK -> Click)
                if (baseAction.length() > 0) {
                    baseAction = baseAction.substring(0, 1).toUpperCase() + baseAction.substring(1).toLowerCase();
                }
                sb.append(baseAction);
                
                // Target
                if (event.getTarget() != null) {
                    sb.append(" ").append(event.getTarget());
                }
                
                // Duration if available
                if (event.getMetadata().containsKey("duration")) {
                    sb.append(" ").append("• ").append(event.getMetadata().get("duration")).append("ms");
                }
                
                return sb.toString();
            }
        }
        
        // For non-action events, use minimal formatting
        if (event.getType() == LogEvent.Type.ERROR) {
            return RED + ERROR_SYMBOL + " " + event.getMessage() + RESET;
        } else if (event.getType() == LogEvent.Type.OBSERVATION) {
            return event.getMessage();
        }
        
        // Skip other event types in quiet mode
        return null;
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
        
        // Success/failure indicator - only show for COMPLETE/FAILED actions
        if (event.getAction() != null && 
            (event.getAction().endsWith("_COMPLETE") || event.getAction().endsWith("_FAILED"))) {
            if (event.isSuccess()) {
                sb.append(" ").append(GREEN).append(SUCCESS_SYMBOL).append(RESET);
            } else {
                sb.append(" ").append(RED).append(FAILURE_SYMBOL).append(RESET);
            }
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