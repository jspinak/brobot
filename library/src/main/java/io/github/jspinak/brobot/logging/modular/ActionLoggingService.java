package io.github.jspinak.brobot.logging.modular;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Centralized service for logging action executions using modular formatters.
 * 
 * This service acts as the single point of entry for action logging, routing
 * ActionResult objects to the appropriate formatter based on configured
 * verbosity level and outputting the formatted messages.
 * 
 * Design principles:
 * - Single responsibility: Only handles logging coordination
 * - Configurable: Verbosity level driven by configuration
 * - Extensible: Easy to add new formatters or output destinations
 * - Testable: Clear separation of concerns
 */
@Service
@Slf4j
public class ActionLoggingService {
    
    private final Map<ActionLogFormatter.VerbosityLevel, ActionLogFormatter> formatters;
    
    @Value("${brobot.logging.verbosity:QUIET}")
    private String verbosityConfig;
    
    @Value("${brobot.logging.console.enabled:true}")
    private boolean consoleLoggingEnabled;
    
    @Value("${brobot.logging.file.enabled:false}")
    private boolean fileLoggingEnabled;
    
    @Autowired
    public ActionLoggingService(List<ActionLogFormatter> formatterList) {
        // Build map of verbosity level to formatter
        this.formatters = formatterList.stream()
            .collect(Collectors.toMap(
                ActionLogFormatter::getVerbosityLevel,
                Function.identity()
            ));
        
        log.debug("ActionLoggingService initialized with {} formatters: {}", 
                 formatters.size(), 
                 formatters.keySet().stream()
                     .map(Enum::name)
                     .collect(Collectors.joining(", ")));
    }
    
    /**
     * Log an action execution using the configured verbosity level.
     * 
     * @param actionResult the result of action execution
     */
    public void logAction(ActionResult actionResult) {
        if (!isLoggingEnabled() || actionResult == null) {
            return;
        }
        
        ActionLogFormatter.VerbosityLevel level = getEffectiveVerbosityLevel();
        ActionLogFormatter formatter = formatters.get(level);
        
        if (formatter == null) {
            log.warn("No formatter found for verbosity level: {}", level);
            return;
        }
        
        try {
            if (!formatter.shouldLog(actionResult)) {
                log.debug("Formatter {} decided not to log this action", formatter.getClass().getSimpleName());
                return;
            }
            
            String formattedMessage = formatter.format(actionResult);
            if (formattedMessage != null && !formattedMessage.trim().isEmpty()) {
                outputFormattedMessage(formattedMessage, level);
            }
            
        } catch (Exception e) {
            log.error("Error formatting action log with {}: {}", 
                     formatter.getClass().getSimpleName(), e.getMessage(), e);
        }
    }
    
    /**
     * Log an action with explicit verbosity level override.
     * 
     * @param actionResult the result of action execution  
     * @param verbosityLevel the specific verbosity level to use
     */
    public void logAction(ActionResult actionResult, ActionLogFormatter.VerbosityLevel verbosityLevel) {
        if (!isLoggingEnabled() || actionResult == null) {
            return;
        }
        
        ActionLogFormatter formatter = formatters.get(verbosityLevel);
        if (formatter == null) {
            log.warn("No formatter found for verbosity level: {}", verbosityLevel);
            return;
        }
        
        try {
            if (formatter.shouldLog(actionResult)) {
                String formattedMessage = formatter.format(actionResult);
                if (formattedMessage != null && !formattedMessage.trim().isEmpty()) {
                    outputFormattedMessage(formattedMessage, verbosityLevel);
                }
            }
        } catch (Exception e) {
            log.error("Error formatting action log with {}: {}", 
                     formatter.getClass().getSimpleName(), e.getMessage(), e);
        }
    }
    
    /**
     * Check if a specific action would be logged at current verbosity level.
     * 
     * @param actionResult the action result to check
     * @return true if this action would produce log output
     */
    public boolean wouldLog(ActionResult actionResult) {
        if (!isLoggingEnabled() || actionResult == null) {
            return false;
        }
        
        ActionLogFormatter.VerbosityLevel level = getEffectiveVerbosityLevel();
        ActionLogFormatter formatter = formatters.get(level);
        
        return formatter != null && formatter.shouldLog(actionResult);
    }
    
    /**
     * Get the effective verbosity level based on configuration.
     */
    private ActionLogFormatter.VerbosityLevel getEffectiveVerbosityLevel() {
        try {
            return ActionLogFormatter.VerbosityLevel.valueOf(verbosityConfig.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid verbosity level '{}', defaulting to QUIET", verbosityConfig);
            return ActionLogFormatter.VerbosityLevel.QUIET;
        }
    }
    
    /**
     * Check if any form of logging is enabled.
     */
    private boolean isLoggingEnabled() {
        return consoleLoggingEnabled || fileLoggingEnabled;
    }
    
    /**
     * Output the formatted message to configured destinations.
     */
    private void outputFormattedMessage(String message, ActionLogFormatter.VerbosityLevel level) {
        if (consoleLoggingEnabled) {
            outputToConsole(message, level);
        }
        
        if (fileLoggingEnabled) {
            outputToFile(message, level);
        }
    }
    
    /**
     * Output message to console.
     */
    private void outputToConsole(String message, ActionLogFormatter.VerbosityLevel level) {
        // Use System.out for action logs to avoid interference with other logging systems
        // This ensures clean output that matches the user's expectations
        System.out.println(message);
    }
    
    /**
     * Output message to file (placeholder for future file logging implementation).
     */
    private void outputToFile(String message, ActionLogFormatter.VerbosityLevel level) {
        // TODO: Implement file logging when file output is needed
        log.debug("File logging not yet implemented: {}", message);
    }
    
    /**
     * Get available formatters for testing/debugging.
     */
    public Map<ActionLogFormatter.VerbosityLevel, ActionLogFormatter> getAvailableFormatters() {
        return Map.copyOf(formatters);
    }
    
    /**
     * Get current verbosity level for external inspection.
     */
    public ActionLogFormatter.VerbosityLevel getCurrentVerbosityLevel() {
        return getEffectiveVerbosityLevel();
    }
}