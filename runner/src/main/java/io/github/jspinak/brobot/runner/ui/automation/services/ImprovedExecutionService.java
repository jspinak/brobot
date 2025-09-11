package io.github.jspinak.brobot.runner.ui.automation.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.project.TaskButton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing automation execution control. Handles task execution, state management, and
 * confirmation dialogs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImprovedExecutionService {

    private final AutomationOrchestrator automationOrchestrator;

    // State tracking
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    // Listeners
    private final List<ExecutionStateListener> stateListeners = new ArrayList<>();
    private Consumer<String> logHandler;

    // Configuration
    private ExecutionConfiguration configuration = ExecutionConfiguration.builder().build();

    /** Execution configuration. */
    public static class ExecutionConfiguration {
        private boolean confirmationEnabled = true;
        private String defaultConfirmationTitle = "Confirm Execution";
        private String defaultConfirmationHeader = "Task Confirmation";
        private boolean autoLogExecution = true;

        public static ExecutionConfigurationBuilder builder() {
            return new ExecutionConfigurationBuilder();
        }

        public static class ExecutionConfigurationBuilder {
            private ExecutionConfiguration config = new ExecutionConfiguration();

            public ExecutionConfigurationBuilder confirmationEnabled(boolean enabled) {
                config.confirmationEnabled = enabled;
                return this;
            }

            public ExecutionConfigurationBuilder defaultConfirmationTitle(String title) {
                config.defaultConfirmationTitle = title;
                return this;
            }

            public ExecutionConfigurationBuilder defaultConfirmationHeader(String header) {
                config.defaultConfirmationHeader = header;
                return this;
            }

            public ExecutionConfigurationBuilder autoLogExecution(boolean autoLog) {
                config.autoLogExecution = autoLog;
                return this;
            }

            public ExecutionConfiguration build() {
                return config;
            }
        }
    }

    /** Sets the configuration. */
    public void setConfiguration(ExecutionConfiguration configuration) {
        this.configuration = configuration;
    }

    /** Sets the log handler. */
    public void setLogHandler(Consumer<String> logHandler) {
        this.logHandler = logHandler;
    }

    /** Adds a state listener. */
    public void addStateListener(ExecutionStateListener listener) {
        stateListeners.add(listener);
    }

    /** Removes a state listener. */
    public void removeStateListener(ExecutionStateListener listener) {
        stateListeners.remove(listener);
    }

    /** Checks if automation is running. */
    public boolean isRunning() {
        return isRunning.get();
    }

    /** Checks if automation is paused. */
    public boolean isPaused() {
        return isPaused.get();
    }

    /** Executes a task with confirmation if required. */
    public CompletableFuture<Boolean> executeTask(TaskButton taskButton) {
        if (isRunning.get()) {
            log("Cannot start task - automation already running");
            return CompletableFuture.completedFuture(false);
        }

        String taskName = getTaskName(taskButton);

        if (taskButton.isConfirmationRequired() && configuration.confirmationEnabled) {
            return confirmExecution(taskButton)
                    .thenCompose(
                            confirmed -> {
                                if (confirmed) {
                                    return runTask(taskButton);
                                } else {
                                    log("Task execution cancelled: " + taskName);
                                    return CompletableFuture.completedFuture(false);
                                }
                            });
        } else {
            return runTask(taskButton);
        }
    }

    /** Starts general automation. */
    public CompletableFuture<Boolean> startAutomation() {
        if (isRunning.get()) {
            log("Automation already running");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        log("Starting automation...");
                        updateState(ExecutionState.RUNNING);

                        // TODO: Implement actual automation start via orchestrator
                        // For now, just update state

                        log("Automation started");
                        return true;
                    } catch (Exception e) {
                        log("Error starting automation: " + e.getMessage());
                        log.error("Failed to start automation", e);
                        updateState(ExecutionState.STOPPED);
                        return false;
                    }
                });
    }

    /** Toggles pause state. */
    public void togglePause() {
        if (!isRunning.get()) {
            log("Cannot pause - automation not running");
            return;
        }

        if (isPaused.compareAndSet(false, true)) {
            log("Pausing automation...");
            updateState(ExecutionState.PAUSED);
            automationOrchestrator.pauseAutomation();
            log("Automation paused");
        } else if (isPaused.compareAndSet(true, false)) {
            log("Resuming automation...");
            updateState(ExecutionState.RUNNING);
            automationOrchestrator.resumeAutomation();
            log("Automation resumed");
        }
    }

    /** Stops automation. */
    public void stopAutomation() {
        if (!isRunning.get()) {
            log("Automation not running");
            return;
        }

        log("Stopping automation...");
        updateState(ExecutionState.STOPPED);
        automationOrchestrator.stopAllAutomation();
        log("Automation stopped");
    }

    /** Updates the execution state. */
    private void updateState(ExecutionState state) {
        Platform.runLater(
                () -> {
                    switch (state) {
                        case RUNNING:
                            isRunning.set(true);
                            isPaused.set(false);
                            break;

                        case PAUSED:
                            isPaused.set(true);
                            break;

                        case STOPPED:
                        case IDLE:
                        case COMPLETED:
                        case FAILED:
                        case ERROR:
                            isRunning.set(false);
                            isPaused.set(false);
                            break;

                        default:
                            break;
                    }

                    notifyStateListeners(state);
                });
    }

    /** Notifies state listeners. */
    private void notifyStateListeners(ExecutionState state) {
        for (ExecutionStateListener listener : new ArrayList<>(stateListeners)) {
            try {
                listener.onStateChanged(state, isRunning.get(), isPaused.get());
            } catch (Exception e) {
                log.error("Error notifying state listener", e);
            }
        }
    }

    /** Confirms execution with user. */
    private CompletableFuture<Boolean> confirmExecution(TaskButton taskButton) {
        return CompletableFuture.supplyAsync(
                () -> {
                    String taskName = getTaskName(taskButton);
                    String message =
                            taskButton.getConfirmationMessage() != null
                                    ? taskButton.getConfirmationMessage()
                                    : "Are you sure you want to execute " + taskName + "?";

                    CompletableFuture<Boolean> result = new CompletableFuture<>();

                    Platform.runLater(
                            () -> {
                                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                                confirm.setTitle(configuration.defaultConfirmationTitle);
                                confirm.setHeaderText(configuration.defaultConfirmationHeader);
                                confirm.setContentText(message);

                                confirm.showAndWait()
                                        .ifPresent(
                                                response -> {
                                                    result.complete(response == ButtonType.OK);
                                                });

                                // Handle case where dialog is closed without selection
                                if (!result.isDone()) {
                                    result.complete(false);
                                }
                            });

                    try {
                        return result.get();
                    } catch (Exception e) {
                        log.error("Error waiting for confirmation", e);
                        return false;
                    }
                });
    }

    /** Runs a task. */
    private CompletableFuture<Boolean> runTask(TaskButton taskButton) {
        String functionName = taskButton.getFunctionName();
        String taskName = getTaskName(taskButton);

        if (functionName == null || functionName.isEmpty()) {
            log("No function defined for task: " + taskName);
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        log("Executing task: " + taskName);
                        log("Executing function: " + functionName);
                        updateState(ExecutionState.RUNNING);

                        // TODO: Execute via orchestrator with parameters
                        // automationOrchestrator.executeTask(taskButton);

                        // Simulate execution for now
                        simulateTaskExecution(taskButton);

                        updateState(ExecutionState.STOPPED);
                        log("Task completed: " + taskName);
                        return true;

                    } catch (Exception e) {
                        log("Error executing task: " + e.getMessage());
                        log.error("Failed to execute task", e);
                        updateState(ExecutionState.STOPPED);
                        return false;
                    }
                });
    }

    /** Simulates task execution. */
    private void simulateTaskExecution(TaskButton taskButton) {
        try {
            // Simulate some work
            Thread.sleep(1000);

            // Log progress
            if (configuration.autoLogExecution) {
                log("Task execution in progress: " + getTaskName(taskButton));
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Task execution interrupted", e);
        }
    }

    /** Gets task name. */
    private String getTaskName(TaskButton taskButton) {
        return taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
    }

    /** Logs a message. */
    private void log(String message) {
        if (logHandler != null) {
            logHandler.accept(message);
        }
        log.info(message);
    }

    /** Gets current state summary. */
    public ExecutionStateSummary getStateSummary() {
        return new ExecutionStateSummary(isRunning.get(), isPaused.get());
    }

    /** Execution state summary. */
    public static class ExecutionStateSummary {
        private final boolean running;
        private final boolean paused;

        public ExecutionStateSummary(boolean running, boolean paused) {
            this.running = running;
            this.paused = paused;
        }

        public boolean isRunning() {
            return running;
        }

        public boolean isPaused() {
            return paused;
        }

        public boolean isStopped() {
            return !running;
        }

        public String getStatusText() {
            if (paused) return "Paused";
            if (running) return "Running";
            return "Stopped";
        }
    }

    /** Listener for execution state changes. */
    @FunctionalInterface
    public interface ExecutionStateListener {
        void onStateChanged(ExecutionState state, boolean isRunning, boolean isPaused);
    }
}
