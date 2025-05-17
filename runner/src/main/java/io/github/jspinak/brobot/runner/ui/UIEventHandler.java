package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.events.*;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * UI event handler that subscribes to events and updates the UI accordingly.
 */
@Component
public class UIEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(UIEventHandler.class);

    private final EventBus eventBus;
    final Map<BrobotEvent.EventType, Consumer<BrobotEvent>> eventHandlers = new HashMap<>();

    public UIEventHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initialize() {
        // Set up event handlers
        setupEventHandlers();

        // Subscribe to events
        subscribeToEvents();

        logger.info("UI Event Handler initialized");
    }

    @PreDestroy
    public void cleanup() {
        // Unsubscribe from events
        for (Map.Entry<BrobotEvent.EventType, Consumer<BrobotEvent>> entry : eventHandlers.entrySet()) {
            eventBus.unsubscribe(entry.getKey(), entry.getValue());
        }
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

    // Event handlers

    private void handleExecutionStarted(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        logger.debug("Handling execution started event: {}", statusEvent.getMessage());

        Platform.runLater(() -> {
            // Update AutomationPanel UI elements if available
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.setStatusMessage(statusEvent.getMessage());
                panel.setProgressValue(statusEvent.getProgress());
                panel.log("Execution started: " + statusEvent.getMessage());
            });
        });
    }

    private void handleExecutionProgress(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        logger.debug("Handling execution progress event: {}", statusEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.setProgressValue(statusEvent.getProgress());
                // Don't log every progress update to avoid log spam
            });
        });
    }

    private void handleExecutionCompleted(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        logger.debug("Handling execution completed event: {}", statusEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.setStatusMessage("Completed successfully");
                panel.setProgressValue(1.0);
                panel.log("Execution completed successfully");
                panel.updateButtonStates(false);
            });
        });
    }

    private void handleExecutionFailed(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        logger.debug("Handling execution failed event: {}", statusEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.setStatusMessage("Failed: " + statusEvent.getMessage());
                panel.log("Execution failed: " + statusEvent.getMessage());
                panel.updateButtonStates(false);
            });
        });
    }

    private void handleExecutionPaused(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        logger.debug("Handling execution paused event: {}", statusEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.setStatusMessage("Paused");
                panel.log("Execution paused");
                panel.updatePauseResumeButton(true);
            });
        });
    }

    private void handleExecutionResumed(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        logger.debug("Handling execution resumed event: {}", statusEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.setStatusMessage("Running");
                panel.log("Execution resumed");
                panel.updatePauseResumeButton(false);
            });
        });
    }

    private void handleExecutionStopped(BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        logger.debug("Handling execution stopped event: {}", statusEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.setStatusMessage("Stopped");
                panel.log("Execution stopped");
                panel.updateButtonStates(false);
            });
        });
    }

    private void handleLogMessage(BrobotEvent event) {
        LogEvent logEvent = (LogEvent) event;
        logger.debug("Handling log message event: {}", logEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                if (logEvent.getLevel().ordinal() >= LogEvent.LogLevel.INFO.ordinal()) {
                    panel.log(logEvent.getMessage());
                }
            });
        });
    }

    private void handleLogWarning(BrobotEvent event) {
        LogEvent logEvent = (LogEvent) event;
        logger.debug("Handling log warning event: {}", logEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.log("WARNING: " + logEvent.getMessage());
            });
        });
    }

    private void handleLogError(BrobotEvent event) {
        LogEvent logEvent = (LogEvent) event;
        logger.debug("Handling log error event: {}", logEvent.getMessage());

        Platform.runLater(() -> {
            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.log("ERROR: " + logEvent.getMessage());
            });
        });
    }

    private void handleErrorOccurred(BrobotEvent event) {
        ErrorEvent errorEvent = (ErrorEvent) event;
        logger.debug("Handling error event: {}", errorEvent.getErrorMessage());

        // Only show UI errors for high and fatal severity errors
        if (errorEvent.getSeverity() == ErrorEvent.ErrorSeverity.HIGH ||
                errorEvent.getSeverity() == ErrorEvent.ErrorSeverity.FATAL) {

            Platform.runLater(() -> {
                showErrorDialog(errorEvent);

                AutomationPanel.getInstance().ifPresent(panel -> {
                    panel.log("ERROR: " + errorEvent.getErrorMessage());
                });
            });
        }
    }

    private void handleConfigLoaded(BrobotEvent event) {
        ConfigurationEvent configEvent = (ConfigurationEvent) event;
        logger.debug("Handling config loaded event: {}", configEvent.getConfigName());

        Platform.runLater(() -> {
            ConfigurationPanel.getInstance().ifPresent(panel -> {
                panel.updateStatus("Configuration loaded: " + configEvent.getConfigName());
            });

            AutomationPanel.getInstance().ifPresent(panel -> {
                panel.log("Configuration loaded: " + configEvent.getConfigName());
                panel.refreshAutomationButtons();
            });
        });
    }

    private void handleConfigLoadingFailed(BrobotEvent event) {
        ConfigurationEvent configEvent = (ConfigurationEvent) event;
        logger.debug("Handling config loading failed event: {}", configEvent.getConfigName());

        Platform.runLater(() -> {
            ConfigurationPanel.getInstance().ifPresent(panel -> {
                panel.updateStatus("Configuration loading failed: " + configEvent.getDetails(), true);
            });

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