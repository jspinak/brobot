package io.github.jspinak.brobot.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.automation.AutomationConfig;
import io.github.jspinak.brobot.exception.AutomationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class for automation runners that provides graceful failure handling.
 * 
 * <p>This class wraps automation execution with proper error handling based on
 * the AutomationConfig settings. Applications should extend this class or use
 * it to wrap their automation logic to ensure failures are handled gracefully.
 * 
 * <p>Features:
 * <ul>
 *   <li>Automatic retry support based on configuration
 *   <li>Graceful failure handling without crashing
 *   <li>Comprehensive logging of failures
 *   <li>Optional exception throwing for programmatic handling
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * @Service
 * public class MyAutomation {
 *     @Autowired
 *     private AutomationRunner runner;
 *     
 *     public void runMyAutomation() {
 *         boolean success = runner.run(() -> {
 *             // Your automation logic here
 *             return performAutomationSteps();
 *         });
 *         
 *         if (!success) {
 *             // Handle failure gracefully
 *             log.error("Automation failed but application continues");
 *         }
 *     }
 * }
 * }
 * </pre>
 * 
 * @since 1.0
 */
@Slf4j
@Component
public class AutomationRunner {
    
    @Autowired(required = false)
    private AutomationConfig config;
    
    /**
     * Functional interface for automation tasks.
     */
    @FunctionalInterface
    public interface AutomationTask {
        /**
         * Executes the automation task.
         * 
         * @return true if successful, false otherwise
         * @throws Exception if an error occurs during execution
         */
        boolean execute() throws Exception;
    }
    
    /**
     * Runs an automation task with configured error handling and retry logic.
     * 
     * @param task The automation task to execute
     * @return true if the task succeeded, false otherwise
     */
    public boolean run(AutomationTask task) {
        return run(task, "Automation");
    }
    
    /**
     * Runs an automation task with configured error handling and retry logic.
     * 
     * @param task The automation task to execute
     * @param taskName Name of the task for logging
     * @return true if the task succeeded, false otherwise
     */
    public boolean run(AutomationTask task, String taskName) {
        if (config == null) {
            config = new AutomationConfig(); // Use defaults
        }
        
        int maxAttempts = Math.max(1, config.getMaxRetries() + 1);
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Starting {} (attempt {} of {})", taskName, attempt, maxAttempts);
                
                boolean success = task.execute();
                
                if (success) {
                    log.info("{} completed successfully", taskName);
                    return true;
                } else {
                    log.warn("{} failed on attempt {} of {}", taskName, attempt, maxAttempts);
                    
                    if (!config.isContinueOnFailure() && attempt < maxAttempts) {
                        // If not continuing on failure, retry after delay
                        if (config.getRetryDelayMs() > 0) {
                            log.info("Waiting {}ms before retry", config.getRetryDelayMs());
                            Thread.sleep(config.getRetryDelayMs());
                        }
                    } else if (config.isContinueOnFailure()) {
                        // Continue on failure means don't retry, just log and return
                        log.info("Continuing despite failure (continueOnFailure=true)");
                        return false;
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("{} interrupted", taskName);
                lastException = e;
                break;
                
            } catch (Exception e) {
                lastException = e;
                if (config.isLogStackTraces()) {
                    log.error("{} threw exception on attempt {} of {}", taskName, attempt, maxAttempts, e);
                } else {
                    log.error("{} threw exception on attempt {} of {}: {}", 
                             taskName, attempt, maxAttempts, e.getMessage());
                }
                
                if (attempt < maxAttempts && config.getRetryDelayMs() > 0) {
                    try {
                        log.info("Waiting {}ms before retry", config.getRetryDelayMs());
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry delay interrupted");
                        break;
                    }
                }
            }
        }
        
        // All attempts failed
        handleAutomationFailure(taskName, lastException);
        return false;
    }
    
    /**
     * Handles automation failure based on configuration.
     * 
     * @param taskName Name of the failed task
     * @param exception The exception that caused the failure (may be null)
     */
    private void handleAutomationFailure(String taskName, Exception exception) {
        String errorMsg = String.format("%s failed after all retry attempts", taskName);
        
        if (exception != null) {
            if (config.isLogStackTraces()) {
                log.error(errorMsg, exception);
            } else {
                log.error("{}: {}", errorMsg, exception.getMessage());
            }
        } else {
            log.error(errorMsg);
        }
        
        // Throw exception if configured
        if (config.isThrowOnFailure()) {
            if (exception instanceof AutomationException) {
                throw (AutomationException) exception;
            } else {
                throw new AutomationException(errorMsg, exception);
            }
        }
        
        // Exit application if configured
        if (config.isExitOnFailure()) {
            log.error("Exiting application due to automation failure (brobot.automation.exitOnFailure=true)");
            System.exit(config.getFailureExitCode());
        }
        
        // Otherwise, just log and return false
        log.info("Automation failed but application continues (brobot.automation.exitOnFailure=false)");
    }
    
    /**
     * Wraps a runnable task to return a boolean AutomationTask.
     * 
     * @param runnable The runnable to wrap
     * @return An AutomationTask that always returns true after execution
     */
    public static AutomationTask wrap(Runnable runnable) {
        return () -> {
            runnable.run();
            return true;
        };
    }
    
    /**
     * Gets the current automation configuration.
     * 
     * @return The automation configuration, or defaults if not configured
     */
    public AutomationConfig getConfig() {
        if (config == null) {
            config = new AutomationConfig();
        }
        return config;
    }
}