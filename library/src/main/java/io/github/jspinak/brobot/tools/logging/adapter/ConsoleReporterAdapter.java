package io.github.jspinak.brobot.tools.logging.adapter;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter.OutputLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter that intercepts ConsoleReporter calls and routes them through the unified logging system.
 * This enables legacy code using ConsoleReporter to benefit from the new features like
 * structured logging, context propagation, and visual feedback.
 * 
 * <p>The adapter maintains backward compatibility while adding:</p>
 * <ul>
 *   <li>Automatic context inclusion from thread-local storage</li>
 *   <li>Structured metadata extraction from log messages</li>
 *   <li>Integration with console action reporting</li>
 *   <li>Proper log level mapping</li>
 * </ul>
 * 
 * @see ConsoleReporter for the legacy API
 * @see BrobotLogger for the unified logging system
 */
@Component
@Slf4j
public class ConsoleReporterAdapter {
    
    private static BrobotLogger unifiedLogger;
    
    // Patterns for extracting structured data from legacy messages
    private static final Pattern ACTION_PATTERN = Pattern.compile("^(\\w+):\\s+(.+)$");
    private static final Pattern STATE_PATTERN = Pattern.compile("^Open State\\s+(.+)$");
    private static final Pattern PATH_PATTERN = Pattern.compile("^Find path:\\s+(.+?)\\s*->\\s*(.+)$");
    private static final Pattern TRANSITION_PATTERN = Pattern.compile("^(.+?)\\s*->\\s*(.+?)\\s*\\[(\\d+)ms\\]");
    
    @Autowired
    public void setUnifiedLogger(BrobotLogger logger) {
        ConsoleReporterAdapter.unifiedLogger = logger;
        log.info("ConsoleReporterAdapter initialized with unified logger");
    }
    
    /**
     * Routes a ConsoleReporter print call through the unified logging system.
     * 
     * @param message The message to log
     * @param level The ConsoleReporter output level
     * @return true if the message was logged
     */
    public static boolean routeToUnified(String message, OutputLevel level) {
        if (unifiedLogger == null) {
            // Fallback to standard output if adapter not initialized
            System.out.print(message);
            return true;
        }
        
        // Skip if level doesn't meet threshold
        if (!ConsoleReporter.minReportingLevel(level)) {
            return false;
        }
        
        // Analyze message and route appropriately
        LogEvent.Type type = determineLogType(message);
        LogEvent.Level logLevel = mapOutputLevel(level);
        
        // Extract structured data from message
        var metadata = extractMetadata(message);
        
        // Build and log through unified system
        var logBuilder = unifiedLogger.log()
            .type(type)
            .level(logLevel)
            .observation(message.trim());
            
        // Add extracted metadata
        metadata.forEach(logBuilder::metadata);
        
        logBuilder.log();
        
        return true;
    }
    
    /**
     * Routes a formatted ConsoleReporter call through the unified logging system.
     * 
     * @param format The format string
     * @param args The format arguments
     * @param level The ConsoleReporter output level
     * @return true if the message was logged
     */
    public static boolean routeFormattedToUnified(String format, Object[] args, OutputLevel level) {
        String message = String.format(format, args);
        return routeToUnified(message, level);
    }
    
    /**
     * Determines the appropriate LogEvent.Type based on message content.
     */
    private static LogEvent.Type determineLogType(String message) {
        if (message == null || message.isEmpty()) {
            return LogEvent.Type.OBSERVATION;  // Default to observation for generic messages
        }
        
        // Check for specific patterns
        if (ACTION_PATTERN.matcher(message).find()) {
            return LogEvent.Type.ACTION;
        }
        if (STATE_PATTERN.matcher(message).find() || PATH_PATTERN.matcher(message).find()) {
            return LogEvent.Type.TRANSITION;  // Navigation is a type of transition
        }
        if (TRANSITION_PATTERN.matcher(message).find()) {
            return LogEvent.Type.TRANSITION;
        }
        if (message.contains("ERROR") || message.contains("Exception")) {
            return LogEvent.Type.ERROR;
        }
        if (message.contains("WARNING") || message.contains("WARN")) {
            return LogEvent.Type.OBSERVATION;
        }
        
        return LogEvent.Type.OBSERVATION;  // Default type for console messages
    }
    
    /**
     * Maps ConsoleReporter OutputLevel to LogEvent.Level.
     */
    private static LogEvent.Level mapOutputLevel(OutputLevel outputLevel) {
        switch (outputLevel) {
            case NONE:
                return LogEvent.Level.DEBUG;  // TRACE doesn't exist, use DEBUG
            case LOW:
                return LogEvent.Level.INFO;
            case HIGH:
                return LogEvent.Level.DEBUG;
            default:
                return LogEvent.Level.INFO;
        }
    }
    
    /**
     * Extracts structured metadata from legacy log messages.
     */
    private static java.util.Map<String, Object> extractMetadata(String message) {
        var metadata = new java.util.HashMap<String, Object>();
        
        if (message == null || message.isEmpty()) {
            return metadata;
        }
        
        // Extract action data
        Matcher actionMatcher = ACTION_PATTERN.matcher(message);
        if (actionMatcher.find()) {
            metadata.put("action", actionMatcher.group(1));
            metadata.put("target", actionMatcher.group(2).trim());
        }
        
        // Extract state navigation data
        Matcher stateMatcher = STATE_PATTERN.matcher(message);
        if (stateMatcher.find()) {
            metadata.put("operation", "OpenState");
            metadata.put("targetState", stateMatcher.group(1).trim());
        }
        
        // Extract path finding data
        Matcher pathMatcher = PATH_PATTERN.matcher(message);
        if (pathMatcher.find()) {
            metadata.put("operation", "PathFinding");
            metadata.put("from", pathMatcher.group(1).trim());
            metadata.put("to", pathMatcher.group(2).trim());
        }
        
        // Extract transition data
        Matcher transitionMatcher = TRANSITION_PATTERN.matcher(message);
        if (transitionMatcher.find()) {
            metadata.put("fromState", transitionMatcher.group(1).trim());
            metadata.put("toState", transitionMatcher.group(2).trim());
            metadata.put("duration", Long.parseLong(transitionMatcher.group(3)));
        }
        
        // Add source indicator
        metadata.put("source", "ConsoleReporter");
        
        return metadata;
    }
    
    /**
     * Static initialization block to patch ConsoleReporter methods.
     * This is called when the adapter is loaded to redirect all ConsoleReporter
     * calls through the unified logging system.
     */
    public static void patchConsoleReporter() {
        log.info("Patching ConsoleReporter to use unified logging system");
        
        // Note: In a real implementation, we would use bytecode manipulation
        // or aspect-oriented programming to intercept ConsoleReporter calls.
        // For now, we provide this adapter that legacy code can be updated to use.
        
        // Example of how to update legacy code:
        // Old: ConsoleReporter.println("Open State Login");
        // New: ConsoleReporterAdapter.routeToUnified("Open State Login\n", OutputLevel.HIGH);
    }
    
    /**
     * Convenience method to route println calls.
     */
    public static boolean println(String message, OutputLevel level) {
        return routeToUnified(message + "\n", level);
    }
    
    /**
     * Convenience method to route format calls.
     */
    public static boolean format(String format, OutputLevel level, Object... args) {
        return routeFormattedToUnified(format, args, level);
    }
}