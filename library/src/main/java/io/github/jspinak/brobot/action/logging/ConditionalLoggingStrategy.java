package io.github.jspinak.brobot.action.logging;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implements conditional logging based on action results.
 * Allows for fine-grained control over when and what to log.
 */
@Component
public class ConditionalLoggingStrategy {
    
    @Autowired(required = false)
    private BrobotLogger logger;
    
    private final List<LoggingCondition> conditions = new ArrayList<>();
    
    /**
     * Represents a logging condition with its associated action
     */
    public static class LoggingCondition {
        private final Predicate<ActionResult> condition;
        private final Runnable loggingAction;
        private final LogLevel logLevel;
        private final String description;
        
        public LoggingCondition(Predicate<ActionResult> condition, 
                               Runnable loggingAction,
                               LogLevel logLevel,
                               String description) {
            this.condition = condition;
            this.loggingAction = loggingAction;
            this.logLevel = logLevel;
            this.description = description;
        }
        
        public boolean test(ActionResult result) {
            return condition.test(result);
        }
        
        public void execute() {
            loggingAction.run();
        }
        
        public LogLevel getLogLevel() {
            return logLevel;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Check if logging should occur based on the action result
     */
    public boolean shouldLog(ActionResult result) {
        return conditions.stream().anyMatch(c -> c.test(result));
    }
    
    /**
     * Log if the action was successful
     */
    public void logIfSuccess(ActionResult result, String message) {
        if (result.isSuccess() && logger != null) {
            logger.log()
                .level(LogEvent.Level.INFO)
                .message(message)
                .log();
        }
    }
    
    /**
     * Log if the action failed
     */
    public void logIfFailure(ActionResult result, String message) {
        if (!result.isSuccess() && logger != null) {
            logger.log()
                .level(LogEvent.Level.WARNING)
                .message(message)
                .log();
        }
    }
    
    /**
     * Add a custom condition for logging
     */
    public ConditionalLoggingStrategy withCondition(Predicate<ActionResult> condition,
                                                   Runnable loggingAction,
                                                   LogLevel logLevel,
                                                   String description) {
        conditions.add(new LoggingCondition(condition, loggingAction, logLevel, description));
        return this;
    }
    
    /**
     * Add a condition that logs when matches are found
     */
    public ConditionalLoggingStrategy withMatchFoundCondition(String message) {
        return withCondition(
            result -> !result.getMatchList().isEmpty(),
            () -> {
                if (logger != null) {
                    logger.log()
                        .level(LogEvent.Level.INFO)
                        .message(message)
                        .log();
                }
            },
            LogLevel.INFO,
            "Log when matches found"
        );
    }
    
    /**
     * Add a condition that logs when no matches are found
     */
    public ConditionalLoggingStrategy withNoMatchCondition(String message) {
        return withCondition(
            result -> result.getMatchList().isEmpty(),
            () -> {
                if (logger != null) {
                    logger.log()
                        .level(LogEvent.Level.WARNING)
                        .message(message)
                        .log();
                }
            },
            LogLevel.WARN,
            "Log when no matches found"
        );
    }
    
    /**
     * Add a condition that logs when score exceeds threshold
     */
    public ConditionalLoggingStrategy withScoreThresholdCondition(double threshold, String message) {
        return withCondition(
            result -> result.getMatchList().stream()
                .anyMatch(m -> m.getScore() >= threshold),
            () -> {
                if (logger != null) {
                    logger.log()
                        .level(LogEvent.Level.INFO)
                        .message(message)
                        .log();
                }
            },
            LogLevel.INFO,
            "Log when score >= " + threshold
        );
    }
    
    /**
     * Add a condition that logs when action takes longer than threshold
     */
    public ConditionalLoggingStrategy withDurationThresholdCondition(long milliseconds, String message) {
        return withCondition(
            result -> result.getDuration() != null && 
                     result.getDuration().toMillis() > milliseconds,
            () -> {
                if (logger != null) {
                    logger.log()
                        .level(LogEvent.Level.WARNING)
                        .message(message)
                        .log();
                }
            },
            LogLevel.WARN,
            "Log when duration > " + milliseconds + "ms"
        );
    }
    
    /**
     * Execute all matching conditions
     */
    public void executeConditionalLogging(ActionResult result) {
        conditions.stream()
            .filter(c -> c.test(result))
            .forEach(LoggingCondition::execute);
    }
    
    /**
     * Clear all conditions
     */
    public void clearConditions() {
        conditions.clear();
    }
    
    /**
     * Get all configured conditions
     */
    public List<LoggingCondition> getConditions() {
        return new ArrayList<>(conditions);
    }
}