package io.github.jspinak.brobot.runner.ui.enhanced.services;

import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionStatusEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.project.TaskButton;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing automation execution control. Handles starting, pausing, resuming, and
 * stopping automation tasks.
 */
@Slf4j
@Service
public class EnhancedExecutionService {

    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;

    @Getter @Setter private ExecutionConfiguration configuration;

    private Consumer<String> logHandler;

    /** Configuration for execution behavior. */
    @Getter
    @Setter
    public static class ExecutionConfiguration {
        private boolean confirmationEnabled;
        private String confirmationTitle;
        private String defaultConfirmationMessage;
        private boolean publishEvents;
        private boolean checkConcurrency;

        public static ExecutionConfigurationBuilder builder() {
            return new ExecutionConfigurationBuilder();
        }

        public static class ExecutionConfigurationBuilder {
            private boolean confirmationEnabled = true;
            private String confirmationTitle = "Confirm Automation";
            private String defaultConfirmationMessage =
                    "Are you sure you want to run this automation?";
            private boolean publishEvents = true;
            private boolean checkConcurrency = true;

            public ExecutionConfigurationBuilder confirmationEnabled(boolean enabled) {
                this.confirmationEnabled = enabled;
                return this;
            }

            public ExecutionConfigurationBuilder confirmationTitle(String title) {
                this.confirmationTitle = title;
                return this;
            }

            public ExecutionConfigurationBuilder defaultConfirmationMessage(String message) {
                this.defaultConfirmationMessage = message;
                return this;
            }

            public ExecutionConfigurationBuilder publishEvents(boolean publish) {
                this.publishEvents = publish;
                return this;
            }

            public ExecutionConfigurationBuilder checkConcurrency(boolean check) {
                this.checkConcurrency = check;
                return this;
            }

            public ExecutionConfiguration build() {
                ExecutionConfiguration config = new ExecutionConfiguration();
                config.confirmationEnabled = confirmationEnabled;
                config.confirmationTitle = confirmationTitle;
                config.defaultConfirmationMessage = defaultConfirmationMessage;
                config.publishEvents = publishEvents;
                config.checkConcurrency = checkConcurrency;
                return config;
            }
        }
    }

    @Autowired
    public EnhancedExecutionService(
            AutomationOrchestrator automationOrchestrator, EventBus eventBus) {
        this.automationOrchestrator = automationOrchestrator;
        this.eventBus = eventBus;
        this.configuration = ExecutionConfiguration.builder().build();
    }

    /**
     * Runs an automation task.
     *
     * @param buttonDef The task button definition
     * @param preExecutionHandler Optional handler to run before execution (e.g., minimize window)
     * @return true if execution started
     */
    public boolean runAutomation(TaskButton buttonDef, Runnable preExecutionHandler) {
        // Check concurrency
        if (configuration.checkConcurrency && isAutomationActive()) {
            logMessage("Another automation task is already running. Please wait or stop it first.");
            return false;
        }

        // Confirmation dialog if required
        if (shouldShowConfirmation(buttonDef)) {
            if (!showConfirmationDialog(buttonDef)) {
                return false;
            }
        }

        logMessage("Starting automation: " + buttonDef.getLabel());

        // Run pre-execution handler (e.g., minimize window)
        if (preExecutionHandler != null) {
            preExecutionHandler.run();
        }

        // Publish start event
        if (configuration.publishEvents) {
            publishStartEvent(buttonDef);
        }

        // Execute automation
        automationOrchestrator.executeAutomation(buttonDef);

        return true;
    }

    /** Toggles between pause and resume states. */
    public void togglePauseResume() {
        ExecutionState state = getExecutionState();

        if (state == ExecutionState.RUNNING) {
            pauseAutomation();
        } else if (state == ExecutionState.PAUSED) {
            resumeAutomation();
        }
    }

    /** Pauses the current automation. */
    public void pauseAutomation() {
        ExecutionState state = getExecutionState();

        if (!state.isActive() || state == ExecutionState.PAUSED) {
            return;
        }

        logMessage("Pausing automation...");
        automationOrchestrator.pauseAutomation();
    }

    /** Resumes the paused automation. */
    public void resumeAutomation() {
        ExecutionState state = getExecutionState();

        if (state != ExecutionState.PAUSED) {
            return;
        }

        logMessage("Resuming automation...");
        automationOrchestrator.resumeAutomation();
    }

    /**
     * Stops all automation.
     *
     * @param postStopHandler Optional handler to run after stopping (e.g., restore window)
     */
    public void stopAllAutomation(Runnable postStopHandler) {
        ExecutionStatus status = getExecutionStatus();

        if (status == null || !status.getState().isActive()) {
            logMessage("No automation is currently running.");
            return;
        }

        logMessage("Stopping all automation...");

        // Publish stop event
        if (configuration.publishEvents) {
            publishStopEvent(status);
        }

        // Stop automation
        automationOrchestrator.stopAllAutomation();

        // Run post-stop handler (e.g., restore window)
        if (postStopHandler != null) {
            postStopHandler.run();
        }
    }

    /**
     * Gets the current execution status.
     *
     * @return The execution status or null
     */
    public ExecutionStatus getExecutionStatus() {
        return automationOrchestrator.getExecutionStatus();
    }

    /**
     * Gets the current execution state.
     *
     * @return The execution state
     */
    public ExecutionState getExecutionState() {
        ExecutionStatus status = getExecutionStatus();
        return status != null ? status.getState() : ExecutionState.IDLE;
    }

    /**
     * Checks if automation is currently active.
     *
     * @return true if active
     */
    public boolean isAutomationActive() {
        return getExecutionState().isActive();
    }

    /**
     * Sets the log handler.
     *
     * @param logHandler The log handler
     */
    public void setLogHandler(Consumer<String> logHandler) {
        this.logHandler = logHandler;
    }

    private boolean shouldShowConfirmation(TaskButton buttonDef) {
        return configuration.confirmationEnabled && buttonDef.isConfirmationRequired();
    }

    private boolean showConfirmationDialog(TaskButton buttonDef) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(configuration.confirmationTitle);
        alert.setHeaderText("Run " + buttonDef.getLabel() + "?");

        String message =
                buttonDef.getConfirmationMessage() != null
                        ? buttonDef.getConfirmationMessage()
                        : configuration.defaultConfirmationMessage;
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void publishStartEvent(TaskButton buttonDef) {
        eventBus.publish(
                ExecutionStatusEvent.started(
                        this,
                        getExecutionStatus(),
                        "Starting automation: " + buttonDef.getLabel()));
    }

    private void publishStopEvent(ExecutionStatus status) {
        eventBus.publish(ExecutionStatusEvent.stopped(this, status, "Stopping all automation"));
    }

    private void logMessage(String message) {
        log.info(message);
        if (logHandler != null) {
            logHandler.accept(message);
        }
    }
}
