package io.github.jspinak.brobot.runner.ui.automation.services;

import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionStatusEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.project.TaskButton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for basic automation execution control from UI panels. Handles task execution,
 * pause/resume, stop operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAutomationControlService {

    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;

    // Log handler
    private Consumer<String> logHandler;

    // Configuration
    private ControlConfiguration configuration = ControlConfiguration.builder().build();

    /** Control service configuration. */
    public static class ControlConfiguration {
        private boolean confirmationEnabled = true;
        private String confirmationTitle = "Confirm Automation";
        private String defaultConfirmationMessage = "Are you sure you want to run this automation?";
        private boolean publishEvents = true;

        public static ControlConfigurationBuilder builder() {
            return new ControlConfigurationBuilder();
        }

        public static class ControlConfigurationBuilder {
            private ControlConfiguration config = new ControlConfiguration();

            public ControlConfigurationBuilder confirmationEnabled(boolean enabled) {
                config.confirmationEnabled = enabled;
                return this;
            }

            public ControlConfigurationBuilder confirmationTitle(String title) {
                config.confirmationTitle = title;
                return this;
            }

            public ControlConfigurationBuilder defaultConfirmationMessage(String message) {
                config.defaultConfirmationMessage = message;
                return this;
            }

            public ControlConfigurationBuilder publishEvents(boolean publish) {
                config.publishEvents = publish;
                return this;
            }

            public ControlConfiguration build() {
                return config;
            }
        }
    }

    /** Sets the configuration. */
    public void setConfiguration(ControlConfiguration configuration) {
        this.configuration = configuration;
    }

    /** Sets the log handler. */
    public void setLogHandler(Consumer<String> logHandler) {
        this.logHandler = logHandler;
    }

    /** Gets the current execution status. */
    public ExecutionStatus getExecutionStatus() {
        return automationOrchestrator.getExecutionStatus();
    }

    /** Gets the current execution state. */
    public ExecutionState getExecutionState() {
        ExecutionStatus status = getExecutionStatus();
        return status != null ? status.getState() : ExecutionState.IDLE;
    }

    /** Checks if automation is active. */
    public boolean isAutomationActive() {
        ExecutionStatus status = getExecutionStatus();
        return status != null && status.getState().isActive();
    }

    /** Checks if automation is paused. */
    public boolean isAutomationPaused() {
        return getExecutionState() == ExecutionState.PAUSED;
    }

    /** Runs the automation function associated with the button. */
    public void runAutomation(TaskButton buttonDef) {
        // Check if another automation is already running
        if (isAutomationActive()) {
            log("Another automation task is already running. Please wait or stop it first.");
            return;
        }

        // Add confirmation dialog if required
        if (configuration.confirmationEnabled && buttonDef.isConfirmationRequired()) {
            if (!showConfirmationDialog(buttonDef)) {
                return;
            }
        }

        log("Starting automation: " + buttonDef.getLabel());

        if (configuration.publishEvents && eventBus != null) {
            eventBus.publish(
                    ExecutionStatusEvent.started(
                            this,
                            getExecutionStatus(),
                            "Starting automation: " + buttonDef.getLabel()));
        }

        automationOrchestrator.executeAutomation(buttonDef);
    }

    /** Shows confirmation dialog. */
    private boolean showConfirmationDialog(TaskButton buttonDef) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(configuration.confirmationTitle);
        alert.setHeaderText("Run " + buttonDef.getLabel() + "?");
        alert.setContentText(
                buttonDef.getConfirmationMessage() != null
                        ? buttonDef.getConfirmationMessage()
                        : configuration.defaultConfirmationMessage);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Toggles between pause and resume based on current state. */
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

        log("Pausing automation...");
        automationOrchestrator.pauseAutomation();
    }

    /** Resumes the paused automation. */
    public void resumeAutomation() {
        if (getExecutionState() != ExecutionState.PAUSED) {
            return;
        }

        log("Resuming automation...");
        automationOrchestrator.resumeAutomation();
    }

    /** Stops all running automation. */
    public void stopAllAutomation() {
        if (!isAutomationActive()) {
            log("No automation is currently running.");
            return;
        }

        log("Stopping all automation...");

        if (configuration.publishEvents && eventBus != null) {
            eventBus.publish(
                    ExecutionStatusEvent.stopped(
                            this, getExecutionStatus(), "Stopping all automation"));
        }

        automationOrchestrator.stopAllAutomation();
    }

    /** Gets pause/resume button text based on current state. */
    public String getPauseResumeButtonText() {
        return getExecutionState() == ExecutionState.PAUSED
                ? "Resume Execution"
                : "Pause Execution";
    }

    /** Checks if pause/resume button should be enabled. */
    public boolean isPauseResumeEnabled() {
        ExecutionState state = getExecutionState();
        return state == ExecutionState.RUNNING || state == ExecutionState.PAUSED;
    }

    /** Gets status message from execution status. */
    public String getStatusMessage() {
        ExecutionStatus status = getExecutionStatus();
        if (status == null) {
            return "Ready";
        }

        // Check if there's a custom status message method
        String message = status.getCurrentOperation();
        if (message != null && !message.isEmpty()) {
            return message;
        }

        // Default to state description
        return status.getState().getDescription();
    }

    /** Gets progress value from execution status. */
    public double getProgress() {
        ExecutionStatus status = getExecutionStatus();
        return status != null ? status.getProgress() : 0.0;
    }

    /** Logs a message. */
    private void log(String message) {
        if (logHandler != null) {
            logHandler.accept(message);
        }
        log.info(message);
    }
}
