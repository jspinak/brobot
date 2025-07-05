package io.github.jspinak.brobot.runner.automation;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.ErrorEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionEventPublisher;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionController;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.project.TaskButton;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Orchestrates automation execution by coordinating between the UI, event system, and execution layer.
 * This class acts as a high-level facade that manages automation tasks initiated from TaskButtons,
 * publishes events throughout the execution lifecycle, and provides control over automation flow.
 */
@Getter
@Setter
@Component
public class AutomationOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(AutomationOrchestrator.class);

    private final BrobotRunnerProperties properties;
    private final ExecutionController executionController;
    private final EventBus eventBus;
    private final ExecutionEventPublisher executionEventPublisher;

    /**
     * Set a callback for logging
     */
    @Setter
    private Consumer<String> logCallback;

    public AutomationOrchestrator(BrobotRunnerProperties properties,
                                   ExecutionController executionController,
                                   EventBus eventBus,
                                   ExecutionEventPublisher executionEventPublisher) {
        this.properties = properties;
        this.executionController = executionController;
        this.eventBus = eventBus;
        this.executionEventPublisher = executionEventPublisher;

        // Set up the status consumer to integrate with the event system
        this.executionController.setLogCallback(message -> {
            if (logCallback != null) {
                logCallback.accept(message);
            }
            // Also publish as a log event
            eventBus.publish(LogEvent.info(this, message, "Automation"));
        });

        // Register the event publisher's status consumer with the execution controller
        this.executionController.setStatusConsumer(executionEventPublisher.getStatusConsumer());
    }

    /**
     * Execute an automation function from a button definition
     */
    public void executeAutomation(TaskButton button) {
        // Create a runnable that contains the automation logic
        Runnable automationTask = () -> {
            try {
                // Publish start log event
                eventBus.publish(LogEvent.info(this,
                        "Starting automation: " + button.getLabel(), "Automation"));

                // This is where you would integrate with the Brobot library to execute the function
                // For example:
                //   1. Look up the function by name from the loaded configuration
                //   2. Execute the function with the provided parameters
                //   3. Handle the results

                // Sample execution steps
                for (int i = 0; i < 5; i++) {
                    if (executionController.getStatus().getState().isTerminated()) {
                        return;
                    }

                    log("Execution step " + (i+1));
                    Thread.sleep(1000);
                }

                // Publish completion log event
                eventBus.publish(LogEvent.info(this,
                        "Completed automation: " + button.getLabel(), "Automation"));
            } catch (Exception e) {
                // Log error
                log("Error in automation task: " + e.getMessage());

                // Publish error event
                eventBus.publish(ErrorEvent.high(this,
                        "Automation execution failed: " + e.getMessage(),
                        e,
                        "AutomationExecution"));

                throw new RuntimeException("Automation execution failed", e);
            }
        };

        // Execute the automation with the execution controller
        // Set a default timeout of 5 minutes (can be adjusted based on needs)
        long timeoutMillis = 5 * 60 * 1000;

        try {
            executionController.executeAutomation(button, automationTask, timeoutMillis, executionEventPublisher.getStatusConsumer());
        } catch (Exception e) {
            log("Failed to start automation: " + e.getMessage());

            // Publish error event
            eventBus.publish(ErrorEvent.high(this,
                    "Failed to start automation: " + e.getMessage(),
                    e,
                    "AutomationExecution"));

            logger.error("Failed to start automation", e);
        }
    }

    /**
     * Stops all running automation
     */
    public void stopAllAutomation() {
        executionController.stopExecution();
        log("Stop requested for all automation");
        eventBus.publish(LogEvent.info(this, "Stop requested for all automation", "Automation"));
    }

    /**
     * Pauses the current automation
     */
    public void pauseAutomation() {
        executionController.pauseExecution();
        log("Pause requested for automation");
        eventBus.publish(LogEvent.info(this, "Pause requested for automation", "Automation"));
    }

    /**
     * Resumes the paused automation
     */
    public void resumeAutomation() {
        executionController.resumeExecution();
        log("Resume requested for automation");
        eventBus.publish(LogEvent.info(this, "Resume requested for automation", "Automation"));
    }

    /**
     * Gets the current execution status
     */
    public ExecutionStatus getExecutionStatus() {
        return executionController.getStatus();
    }

    /**
     * Checks if the execution is currently paused
     */
    public boolean isPaused() {
        ExecutionStatus status = executionController.getStatus();
        return status != null && status.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.PAUSED;
    }

    /**
     * Starts a new execution
     */
    public void startExecution() {
        // This would typically be called when no automation is running
        // For now, log that execution was requested
        log("Start execution requested");
        eventBus.publish(LogEvent.info(this, "Start execution requested", "Automation"));
    }

    /**
     * Resumes a paused execution
     */
    public void resume() {
        resumeAutomation();
    }

    /**
     * Pauses the current execution
     */
    public void pause() {
        pauseAutomation();
    }

    /**
     * Stops the current execution
     */
    public void stop() {
        stopAllAutomation();
    }

    private void log(String message) {
        logger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
}