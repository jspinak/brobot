package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for action logging behavior.
 * <p>
 * This class defines how actions should be logged, including custom messages
 * for different stages of action execution and conditions.
 * <p>
 * It is an immutable object designed to be composed within other {@code Options} classes.
 * 
 * @since 1.0
 */
@Getter
@Builder(toBuilder = true, builderClassName = "LoggingOptionsBuilder")
public final class LoggingOptions {

    /**
     * Message to log before the action is executed.
     */
    @Builder.Default
    private final String beforeActionMessage = "";
    
    /**
     * Message to log after the action is executed.
     */
    @Builder.Default
    private final String afterActionMessage = "";
    
    /**
     * Message to log when the action succeeds.
     */
    @Builder.Default
    private final String successMessage = "";
    
    /**
     * Message to log when the action fails.
     */
    @Builder.Default
    private final String failureMessage = "";
    
    /**
     * Whether to log before the action is executed.
     */
    @Builder.Default
    private final boolean logBeforeAction = false;
    
    /**
     * Whether to log after the action is executed.
     */
    @Builder.Default
    private final boolean logAfterAction = false;
    
    /**
     * Whether to log when the action succeeds.
     */
    @Builder.Default
    private final boolean logOnSuccess = false;
    
    /**
     * Whether to log when the action fails.
     */
    @Builder.Default
    private final boolean logOnFailure = true;
    
    /**
     * Log level for before action messages.
     */
    @Builder.Default
    private final LogEventType beforeActionLevel = LogEventType.ACTION;
    
    /**
     * Log level for after action messages.
     */
    @Builder.Default
    private final LogEventType afterActionLevel = LogEventType.ACTION;
    
    /**
     * Log level for success messages.
     */
    @Builder.Default
    private final LogEventType successLevel = LogEventType.ACTION;
    
    /**
     * Log level for failure messages.
     */
    @Builder.Default
    private final LogEventType failureLevel = LogEventType.ERROR;
}