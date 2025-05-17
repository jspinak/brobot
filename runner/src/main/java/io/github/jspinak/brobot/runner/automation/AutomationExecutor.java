package io.github.jspinak.brobot.runner.automation;

import io.github.jspinak.brobot.datatypes.project.Button;
import io.github.jspinak.brobot.dsl.AutomationFunction;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
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
    private volatile boolean stopRequested = false;
    /**
     * -- SETTER --
     *  Set a callback for logging
     */
    @Setter
    private Consumer<String> logCallback;

    public AutomationExecutor(BrobotRunnerProperties properties) {
        this.properties = properties;
    }

    /**
     * Execute an automation function from a button definition
     */
    public void executeAutomation(Button button) {
        stopRequested = false;

        // Log start
        log("Executing automation function: " + button.getFunctionName());

        try {
            // This is where you would integrate with the Brobot library to execute the function
            // For example:
            //   1. Look up the function by name from the loaded configuration
            //   2. Execute the function with the provided parameters
            //   3. Handle the results

            // Sample execution
            log("Function execution: " + button.getFunctionName());

            // Simulate execution time
            for (int i = 0; i < 5; i++) {
                if (stopRequested) {
                    log("Execution stopped");
                    return;
                }

                log("Execution step " + (i+1));
                Thread.sleep(1000);
            }

            log("Function completed: " + button.getFunctionName());
        } catch (Exception e) {
            log("Error executing function: " + e.getMessage());
            logger.error("Error executing automation", e);
        }
    }

    /**
     * Stops all running automation
     */
    public void stopAllAutomation() {
        stopRequested = true;
        log("Stop requested for all automation");
    }

    private void log(String message) {
        logger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
}