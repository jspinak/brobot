package io.github.jspinak.brobot.action.logging;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.logging.LogLevel;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Provides fluent API methods for adding comprehensive logging capabilities to ActionConfig objects.
 * This enhancer allows for before/after action logging, conditional logging, and custom log levels.
 */
@Component
public class ActionLoggingEnhancer {
    
    private final Map<ActionConfig, LoggingConfiguration> loggingConfigs = new HashMap<>();
    
    /**
     * Configuration holder for logging settings on an ActionConfig
     */
    @Data
    @Builder
    public static class LoggingConfiguration {
        private Consumer<ActionConfig> beforeActionLog;
        private BiConsumer<ActionConfig, ActionResult> afterActionLog;
        private LogLevel logLevel;
        private boolean logOnSuccess;
        private boolean logOnFailure;
        private String messageTemplate;
        
        public static LoggingConfiguration defaultConfig() {
            return LoggingConfiguration.builder()
                .logLevel(LogLevel.INFO)
                .logOnSuccess(true)
                .logOnFailure(true)
                .build();
        }
    }
    
    /**
     * Adds a before-action logging callback to the ActionConfig
     */
    public ActionConfig withBeforeActionLog(ActionConfig config, Consumer<ActionConfig> beforeLog) {
        LoggingConfiguration loggingConfig = loggingConfigs.computeIfAbsent(
            config, k -> LoggingConfiguration.defaultConfig()
        );
        loggingConfig.setBeforeActionLog(beforeLog);
        return config;
    }
    
    /**
     * Adds an after-action logging callback to the ActionConfig
     */
    public ActionConfig withAfterActionLog(ActionConfig config, 
                                          BiConsumer<ActionConfig, ActionResult> afterLog) {
        LoggingConfiguration loggingConfig = loggingConfigs.computeIfAbsent(
            config, k -> LoggingConfiguration.defaultConfig()
        );
        loggingConfig.setAfterActionLog(afterLog);
        return config;
    }
    
    /**
     * Sets the log level for this action
     */
    public ActionConfig withLogLevel(ActionConfig config, LogLevel level) {
        LoggingConfiguration loggingConfig = loggingConfigs.computeIfAbsent(
            config, k -> LoggingConfiguration.defaultConfig()
        );
        loggingConfig.setLogLevel(level);
        return config;
    }
    
    /**
     * Configures whether to log on successful action completion
     */
    public ActionConfig withLogOnSuccess(ActionConfig config, boolean logOnSuccess) {
        LoggingConfiguration loggingConfig = loggingConfigs.computeIfAbsent(
            config, k -> LoggingConfiguration.defaultConfig()
        );
        loggingConfig.setLogOnSuccess(logOnSuccess);
        return config;
    }
    
    /**
     * Configures whether to log on action failure
     */
    public ActionConfig withLogOnFailure(ActionConfig config, boolean logOnFailure) {
        LoggingConfiguration loggingConfig = loggingConfigs.computeIfAbsent(
            config, k -> LoggingConfiguration.defaultConfig()
        );
        loggingConfig.setLogOnFailure(logOnFailure);
        return config;
    }
    
    /**
     * Sets a message template for logging
     */
    public ActionConfig withMessageTemplate(ActionConfig config, String template) {
        LoggingConfiguration loggingConfig = loggingConfigs.computeIfAbsent(
            config, k -> LoggingConfiguration.defaultConfig()
        );
        loggingConfig.setMessageTemplate(template);
        return config;
    }
    
    /**
     * Execute before-action logging if configured
     */
    public void executeBeforeLogging(ActionConfig config) {
        LoggingConfiguration loggingConfig = loggingConfigs.get(config);
        if (loggingConfig != null && loggingConfig.getBeforeActionLog() != null) {
            loggingConfig.getBeforeActionLog().accept(config);
        }
    }
    
    /**
     * Execute after-action logging if configured
     */
    public void executeAfterLogging(ActionConfig config, ActionResult result) {
        LoggingConfiguration loggingConfig = loggingConfigs.get(config);
        if (loggingConfig != null && loggingConfig.getAfterActionLog() != null) {
            boolean shouldLog = (result.isSuccess() && loggingConfig.isLogOnSuccess()) ||
                              (!result.isSuccess() && loggingConfig.isLogOnFailure());
            if (shouldLog) {
                loggingConfig.getAfterActionLog().accept(config, result);
            }
        }
    }
    
    /**
     * Get the logging configuration for an ActionConfig
     */
    public LoggingConfiguration getLoggingConfiguration(ActionConfig config) {
        return loggingConfigs.get(config);
    }
    
    /**
     * Clear logging configuration for an ActionConfig
     */
    public void clearLoggingConfiguration(ActionConfig config) {
        loggingConfigs.remove(config);
    }
}