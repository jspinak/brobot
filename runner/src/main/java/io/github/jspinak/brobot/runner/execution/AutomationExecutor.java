package io.github.jspinak.brobot.runner.execution;

import java.util.concurrent.*;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionState;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.navigation.monitoring.AutomationScript;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bridges the UI layer with the library's execution control mechanism.
 *
 * <p>This class provides a high-level interface for starting, pausing, resuming, and stopping
 * automation scripts. It manages the execution thread and handles the lifecycle of automation runs,
 * making it easy for UI components to control automation execution.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Asynchronous execution of automation tasks
 *   <li>Thread management for automation scripts
 *   <li>Integration with ExecutionController for pause/resume/stop
 *   <li>Event callbacks for execution state changes
 *   <li>Exception handling and logging
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * // Start an automation
 * automationExecutor.startExecution(() -> {
 *     myAutomationScript.start();
 * });
 *
 * // Pause execution
 * automationExecutor.pause();
 *
 * // Resume execution
 * automationExecutor.resume();
 *
 * // Stop execution
 * automationExecutor.stop();
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class AutomationExecutor {

    private final ExecutionController executionController;
    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread t = new Thread(r, "automation-executor");
                        t.setDaemon(false);
                        return t;
                    });

    private Future<?> currentExecution;
    private AutomationScript currentScript;
    private Consumer<ExecutionState> stateChangeListener;

    /**
     * Starts execution of an automation task.
     *
     * <p>This method runs the provided task asynchronously in a dedicated thread. The task should
     * contain the automation logic to be executed.
     *
     * @param automationTask The task to execute
     * @throws IllegalStateException if an execution is already in progress
     */
    public void startExecution(Runnable automationTask) {
        if (isExecutionInProgress()) {
            throw new IllegalStateException("An execution is already in progress");
        }

        log.info("Starting automation execution");
        executionController.reset();
        executionController.start();
        notifyStateChange(ExecutionState.RUNNING);

        currentExecution =
                executorService.submit(
                        () -> {
                            try {
                                automationTask.run();
                                log.info("Automation execution completed successfully");
                            } catch (ExecutionStoppedException e) {
                                log.info("Automation execution stopped by user");
                            } catch (Exception e) {
                                log.error("Automation execution failed with error", e);
                            } finally {
                                executionController.reset();
                                notifyStateChange(ExecutionState.IDLE);
                                currentExecution = null;
                            }
                        });
    }

    /**
     * Starts execution of an AutomationScript.
     *
     * <p>This is a convenience method that starts the provided AutomationScript and manages its
     * lifecycle.
     *
     * @param script The automation script to execute
     * @throws IllegalStateException if an execution is already in progress
     */
    public void startExecution(AutomationScript script) {
        if (isExecutionInProgress()) {
            throw new IllegalStateException("An execution is already in progress");
        }

        currentScript = script;
        startExecution(
                () -> {
                    try {
                        script.start();
                        // Wait for script to complete
                        while (script.isRunning()) {
                            Thread.sleep(100);
                            // The script should be checking pause points internally
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.debug("Automation execution interrupted");
                    }
                });
    }

    /**
     * Pauses the current execution.
     *
     * <p>The execution will pause at the next checkpoint. This method returns immediately; the
     * actual pause happens asynchronously.
     */
    public void pause() {
        log.info("Pausing automation execution");
        executionController.pause();
        notifyStateChange(ExecutionState.PAUSED);
    }

    /**
     * Resumes a paused execution.
     *
     * @throws IllegalStateException if the execution is not paused
     */
    public void resume() {
        log.info("Resuming automation execution");
        executionController.resume();
        notifyStateChange(ExecutionState.RUNNING);
    }

    /**
     * Stops the current execution.
     *
     * <p>This method signals the execution to stop and optionally waits for it to complete. The
     * execution will stop at the next checkpoint.
     *
     * @param waitForCompletion If true, blocks until the execution stops
     * @param timeoutSeconds Maximum time to wait for completion (if waiting)
     * @return true if the execution stopped within the timeout, false otherwise
     */
    public boolean stop(boolean waitForCompletion, long timeoutSeconds) {
        log.info("Stopping automation execution");
        executionController.stop();
        notifyStateChange(ExecutionState.STOPPING);

        if (currentScript != null) {
            currentScript.stop();
        }

        if (waitForCompletion && currentExecution != null) {
            try {
                currentExecution.get(timeoutSeconds, TimeUnit.SECONDS);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for execution to stop");
            } catch (ExecutionException e) {
                log.error("Execution ended with error", e.getCause());
            } catch (TimeoutException e) {
                log.warn("Timeout waiting for execution to stop");
                currentExecution.cancel(true);
                return false;
            }
        } else if (currentExecution != null) {
            currentExecution.cancel(true);
        }

        notifyStateChange(ExecutionState.STOPPED);
        return true;
    }

    /**
     * Stops the current execution immediately.
     *
     * <p>This is a convenience method that calls stop(false, 0).
     */
    public void stop() {
        stop(false, 0);
    }

    /**
     * Checks if the execution is currently paused.
     *
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return executionController.isPaused();
    }

    /**
     * Checks if the execution is currently running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return executionController.isRunning();
    }

    /**
     * Gets the current execution state.
     *
     * @return the current ExecutionState
     */
    public ExecutionState getState() {
        return executionController.getState();
    }

    /**
     * Checks if an execution is currently in progress.
     *
     * @return true if an execution is active (running or paused)
     */
    public boolean isExecutionInProgress() {
        return currentExecution != null && !currentExecution.isDone();
    }

    /**
     * Sets a listener for execution state changes.
     *
     * @param listener The listener to notify on state changes
     */
    public void setStateChangeListener(Consumer<ExecutionState> listener) {
        this.stateChangeListener = listener;
    }

    /**
     * Shuts down the executor service.
     *
     * <p>Should be called when the application is shutting down to clean up resources.
     */
    public void shutdown() {
        log.info("Shutting down automation executor");
        stop(true, 5);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void notifyStateChange(ExecutionState newState) {
        if (stateChangeListener != null) {
            try {
                stateChangeListener.accept(newState);
            } catch (Exception e) {
                log.error("Error notifying state change listener", e);
            }
        }
    }
}
