package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.events.*;
import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.application.Platform;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Refactored UI event handler that uses dependency injection instead of singletons.
 * This handler subscribes to events and updates UI components through the UIComponentRegistry.
 */
@Component
@Slf4j
@Data
public class RefactoredUIEventHandler {
    
    @Autowired private EventBus eventBus;
    @Autowired private UIComponentRegistry componentRegistry;
    
    private final Map<BrobotEvent.EventType, Consumer<BrobotEvent>> eventHandlers = new HashMap<>();
    
    @PostConstruct
    public void initialize() {
        setupEventHandlers();
        subscribeToEvents();
        log.info("Refactored UI Event Handler initialized");
    }
    
    @PreDestroy
    public void cleanup() {
        // Unsubscribe from events
        for (Map.Entry<BrobotEvent.EventType, Consumer<BrobotEvent>> entry : eventHandlers.entrySet()) {
            eventBus.unsubscribe(entry.getKey(), entry.getValue());
        }
        log.info("Refactored UI Event Handler cleaned up");
    }
    
    private void setupEventHandlers() {
        // Execution events
        eventHandlers.put(BrobotEvent.EventType.EXECUTION_STARTED, this::handleExecutionStarted);
        eventHandlers.put(BrobotEvent.EventType.EXECUTION_PROGRESS, this::handleExecutionProgress);
        eventHandlers.put(BrobotEvent.EventType.EXECUTION_COMPLETED, this::handleExecutionCompleted);
        eventHandlers.put(BrobotEvent.EventType.EXECUTION_FAILED, this::handleExecutionFailed);
        eventHandlers.put(BrobotEvent.EventType.EXECUTION_PAUSED, this::handleExecutionPaused);
        eventHandlers.put(BrobotEvent.EventType.EXECUTION_RESUMED, this::handleExecutionResumed);
        eventHandlers.put(BrobotEvent.EventType.EXECUTION_STOPPED, this::handleExecutionStopped);
        
        // Log events
        eventHandlers.put(BrobotEvent.EventType.LOG_MESSAGE, this::handleLogMessage);
        eventHandlers.put(BrobotEvent.EventType.LOG_WARNING, this::handleLogWarning);
        eventHandlers.put(BrobotEvent.EventType.LOG_ERROR, this::handleLogError);
        
        // Error events
        eventHandlers.put(BrobotEvent.EventType.ERROR_OCCURRED, this::handleErrorOccurred);
        
        // Configuration events
        eventHandlers.put(BrobotEvent.EventType.CONFIG_LOADED, this::handleConfigLoaded);
        eventHandlers.put(BrobotEvent.EventType.CONFIG_LOADING_FAILED, this::handleConfigLoadingFailed);
    }
    
    private void subscribeToEvents() {
        for (Map.Entry<BrobotEvent.EventType, Consumer<BrobotEvent>> entry : eventHandlers.entrySet()) {
            eventBus.subscribe(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Publishes an event to notify UI components to update themselves.
     * This approach decouples the event handler from specific UI components.
     */
    private void notifyUIComponents(String eventType, Object data) {
        Platform.runLater(() -> {
            // Create a custom event for UI updates
            UIUpdateEvent updateEvent = new UIUpdateEvent(this, eventType, data);
            eventBus.publish(updateEvent);
        });
    }
    
    
    // Event handlers using the new approach
    
    private void handleExecutionStarted(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        log.debug("Handling execution started event: {}", statusEvent.getMessage());
        
        notifyUIComponents("execution.started", Map.of(
            "message", statusEvent.getMessage(),
            "progress", statusEvent.getProgress()
        ));
    }
    
    private void handleExecutionProgress(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        log.debug("Handling execution progress event: {}", statusEvent.getMessage());
        
        notifyUIComponents("execution.progress", Map.of(
            "progress", statusEvent.getProgress()
        ));
    }
    
    private void handleExecutionCompleted(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        log.debug("Handling execution completed event: {}", statusEvent.getMessage());
        
        notifyUIComponents("execution.completed", Map.of(
            "message", "Completed successfully"
        ));
    }
    
    private void handleExecutionFailed(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        log.debug("Handling execution failed event: {}", statusEvent.getMessage());
        
        notifyUIComponents("execution.failed", Map.of(
            "message", statusEvent.getMessage()
        ));
    }
    
    private void handleExecutionPaused(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        log.debug("Handling execution paused event: {}", statusEvent.getMessage());
        
        notifyUIComponents("execution.paused", Map.of(
            "message", "Paused"
        ));
    }
    
    private void handleExecutionResumed(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        log.debug("Handling execution resumed event: {}", statusEvent.getMessage());
        
        notifyUIComponents("execution.resumed", Map.of(
            "message", "Running"
        ));
    }
    
    private void handleExecutionStopped(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        log.debug("Handling execution stopped event: {}", statusEvent.getMessage());
        
        notifyUIComponents("execution.stopped", Map.of(
            "message", "Stopped"
        ));
    }
    
    private void handleLogMessage(BrobotEvent event) {
        LogEvent logEvent = (LogEvent) event;
        log.debug("Handling log message event: {}", logEvent.getMessage());
        
        if (logEvent.getLevel().ordinal() >= LogEvent.LogLevel.INFO.ordinal()) {
            notifyUIComponents("log.message", Map.of(
                "level", logEvent.getLevel(),
                "message", logEvent.getMessage()
            ));
        }
    }
    
    private void handleLogWarning(BrobotEvent event) {
        LogEvent logEvent = (LogEvent) event;
        log.debug("Handling log warning event: {}", logEvent.getMessage());
        
        notifyUIComponents("log.warning", Map.of(
            "message", "WARNING: " + logEvent.getMessage()
        ));
    }
    
    private void handleLogError(BrobotEvent event) {
        LogEvent logEvent = (LogEvent) event;
        log.debug("Handling log error event: {}", logEvent.getMessage());
        
        notifyUIComponents("log.error", Map.of(
            "message", "ERROR: " + logEvent.getMessage()
        ));
    }
    
    private void handleErrorOccurred(BrobotEvent event) {
        ErrorEvent errorEvent = (ErrorEvent) event;
        log.debug("Handling error event: {}", errorEvent.getErrorMessage());
        
        // Only show UI errors for high and fatal severity errors
        if (errorEvent.getSeverity() == ErrorEvent.ErrorSeverity.HIGH ||
                errorEvent.getSeverity() == ErrorEvent.ErrorSeverity.FATAL) {
            
            Platform.runLater(() -> {
                showErrorDialog(errorEvent);
                
                notifyUIComponents("error.occurred", Map.of(
                    "message", errorEvent.getErrorMessage(),
                    "severity", errorEvent.getSeverity()
                ));
            });
        }
    }
    
    private void handleConfigLoaded(BrobotEvent event) {
        ConfigurationEvent configEvent = (ConfigurationEvent) event;
        log.debug("Handling config loaded event: {}", configEvent.getConfigName());
        
        notifyUIComponents("config.loaded", Map.of(
            "configName", configEvent.getConfigName()
        ));
    }
    
    private void handleConfigLoadingFailed(BrobotEvent event) {
        ConfigurationEvent configEvent = (ConfigurationEvent) event;
        log.debug("Handling config loading failed event: {}", configEvent.getConfigName());
        
        Platform.runLater(() -> {
            notifyUIComponents("config.failed", Map.of(
                "configName", configEvent.getConfigName(),
                "details", configEvent.getDetails()
            ));
            
            showErrorDialog("Configuration Error", "Failed to load configuration",
                    configEvent.getDetails(), configEvent.getError());
        });
    }
    
    private void showErrorDialog(ErrorEvent errorEvent) {
        String title = "Error - " + errorEvent.getSeverity();
        String header = errorEvent.getComponentName() + " Error";
        String content = errorEvent.getErrorMessage();
        Exception exception = errorEvent.getException();
        
        showErrorDialog(title, header, content, exception);
    }
    
    private void showErrorDialog(String title, String header, String content, Exception exception) {
        StringBuilder fullContent = new StringBuilder(content);
        
        if (exception != null) {
            fullContent.append("\n\nException details: ")
                    .append(exception.getClass().getName())
                    .append(": ")
                    .append(exception.getMessage());
            
            // Add stack trace for detailed error info
            StackTraceElement[] stackTrace = exception.getStackTrace();
            if (stackTrace.length > 0) {
                fullContent.append("\n\nStack trace:\n");
                Arrays.stream(stackTrace)
                        .limit(10) // Limit stack trace to avoid huge dialogs
                        .forEach(element -> fullContent.append("  at ").append(element).append("\n"));
                
                if (stackTrace.length > 10) {
                    fullContent.append("  ... ").append(stackTrace.length - 10).append(" more");
                }
            }
        }
        
        io.github.jspinak.brobot.runner.ui.dialogs.ErrorDialog.show(title, header, fullContent.toString());
    }
}