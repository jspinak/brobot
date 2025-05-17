package io.github.jspinak.brobot.runner.automation;

import io.github.jspinak.brobot.datatypes.project.Button;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.execution.ExecutionController;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * This class is responsible for executing automation functions.
 * It interacts with the Brobot library to run the specified functions.
 */
@Getter
@Setter
@Component
public class AutomationExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AutomationExecutor.class);

    private final BrobotRunnerProperties properties;
    private final ExecutionController executionController;

    /**
     * -- SETTER --
     *  Set a callback for logging
     */
    @Setter
    private Consumer<String> logCallback;

    public AutomationExecutor(BrobotRunnerProperties properties, ExecutionController executionController) {
        this.properties = properties;
        this.executionController = executionController;
        this.executionController.setLogCallback(message -> {
            if (logCallback != null) {
                logCallback.accept(message);
            }
        });
    }

    /**
     * Execute an automation function from a button definition
     */
    public void executeAutomation(Button button) {
        // Create a runnable that contains the automation logic
        Runnable automationTask = () -> {
            try {
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
            } catch (Exception e) {
                log("Error in automation task: " + e.getMessage());
                throw new RuntimeException("Automation execution failed", e);
            }
        };

        // Execute the automation with the execution controller
        // Set a default timeout of 5 minutes (can be adjusted based on needs)
        long timeoutMillis = 5 * 60 * 1000;

        try {
            executionController.executeAutomation(button, automationTask, timeoutMillis, this::onStatusUpdate);
        } catch (Exception e) {
            log("Failed to start automation: " + e.getMessage());
            logger.error("Failed to start automation", e);
        }
    }

    /**
     * Stops all running automation
     */
    public void stopAllAutomation() {
        executionController.stopExecution();
        log("Stop requested for all automation");
    }

    /**
     * Pauses the current automation
     */
    public void pauseAutomation() {
        executionController.pauseExecution();
        log("Pause requested for automation");
    }

    /**
     * Resumes the paused automation
     */
    public void resumeAutomation() {
        executionController.resumeExecution();
        log("Resume requested for automation");
    }

    /**
     * Gets the current execution status
     */
    public ExecutionStatus getExecutionStatus() {
        return executionController.getStatus();
    }

    /**
     * Handler for status updates
     */
    private void onStatusUpdate(ExecutionStatus status) {
        // This method is called whenever the execution status changes
        // You could use this to update UI elements or log status changes
        logger.debug("Execution status update: {}", status);
    }

    private void log(String message) {
        logger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
}