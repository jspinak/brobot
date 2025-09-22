package io.github.jspinak.brobot.navigation.monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.statemanagement.StateMemory;
// Removed old logging import: 
import lombok.Getter;
import lombok.Setter;

/**
 * Service for executing continuous monitoring and automation tasks in the Brobot framework.
 *
 * <p>MonitoringService provides infrastructure for long-running automation processes that need to
 * continuously monitor GUI state and execute actions based on conditions. It implements a scheduled
 * execution model with built-in error handling, failure tolerance, and graceful shutdown
 * capabilities, making it ideal for background automation tasks that run alongside user
 * interactions.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Scheduled Execution</b>: Tasks run at configurable intervals using a dedicated thread
 *       pool
 *   <li><b>Conditional Continuance</b>: Tasks continue only while specified conditions remain true
 *   <li><b>Failure Tolerance</b>: Automatic retry with configurable failure thresholds
 *   <li><b>State Monitoring</b>: Direct integration with StateMemory for state-based triggers
 *   <li><b>Resource Management</b>: Proper cleanup of threads and scheduled tasks
 * </ul>
 *
 * <p>Execution model:
 *
 * <ul>
 *   <li>Tasks run on a single-threaded scheduled executor
 *   <li>Each task execution is wrapped in exception handling
 *   <li>Consecutive failures are tracked and limited
 *   <li>Tasks can be stopped manually or by condition failure
 *   <li>Graceful shutdown ensures tasks complete before termination
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li><b>Application Monitoring</b>: Watch for error dialogs or unexpected states
 *   <li><b>Periodic Maintenance</b>: Clear caches, refresh data, or reset UI state
 *   <li><b>Watchdog Functions</b>: Detect and recover from application hangs
 *   <li><b>Background Workflows</b>: Process queues or sync data while user works
 *   <li><b>State-based Triggers</b>: Execute actions when specific states become active
 * </ul>
 *
 * <p>Error handling strategy:
 *
 * <ul>
 *   <li>Individual task failures don't stop the service
 *   <li>Consecutive failures are counted and limited
 *   <li>Successful execution resets the failure counter
 *   <li>Service stops after reaching maximum consecutive failures
 * </ul>
 *
 * <p>In the model-based approach, MonitoringService extends the framework's capabilities beyond
 * discrete automation tasks to continuous monitoring scenarios. This enables automation that can
 * adapt to asynchronous events, maintain application health, and provide resilience against
 * unexpected conditions.
 *
 * @since 1.1.0
 * @see StateMemory
 * @see AutomationScript
 * @see ScheduledExecutorService
 */
@Component
@Getter
@Setter
public class MonitoringService {

    private final StateMemory stateMemory;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledTask;
    private boolean isRunning = false;

    /** The default delay between automation cycles in seconds. */
    private long defaultDelaySeconds = 5;

    /** Maximum number of consecutive failures before stopping. */
    private int maxConsecutiveFailures = 10;

    private int consecutiveFailures = 0;

    public MonitoringService(StateMemory stateMemory) {
        this.stateMemory = stateMemory;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Starts a continuous automation task that runs while the condition is true.
     *
     * @param task The automation task to run
     * @param continueCondition Condition that must be true to continue running
     * @param delaySeconds Delay between executions in seconds
     */
    public void startContinuousTask(
            Runnable task, BooleanSupplier continueCondition, long delaySeconds) {
        if (isRunning) {
            return;
        }

        if (executorService.isShutdown()) {
            return;
        }

        isRunning = true;
        consecutiveFailures = 0;

        scheduledTask =
                executorService.scheduleWithFixedDelay(
                        () -> {
                            try {
                                if (!continueCondition.getAsBoolean()
                                        || consecutiveFailures >= maxConsecutiveFailures) {
                                    stop();
                                    return;
                                }

                                task.run();
                                consecutiveFailures = 0; // Reset on successful execution

                            } catch (Exception e) {
                                consecutiveFailures++;
                                if (consecutiveFailures >= maxConsecutiveFailures) {
                                    stop();
                                }
                            }
                        },
                        0,
                        delaySeconds,
                        TimeUnit.SECONDS);
    }

    /**
     * Starts a continuous automation task with the default delay.
     *
     * @param task The automation task to run
     * @param continueCondition Condition that must be true to continue running
     */
    public void startContinuousTask(Runnable task, BooleanSupplier continueCondition) {
        startContinuousTask(task, continueCondition, defaultDelaySeconds);
    }

    /**
     * Monitors a specific state and executes a task when that state becomes active.
     *
     * @param targetState The state to monitor
     * @param task The task to execute when the state is active
     * @param delaySeconds Delay between checks in seconds
     */
    public void monitorStateAndExecute(State targetState, Runnable task, long delaySeconds) {
        BooleanSupplier stateActiveCondition =
                () -> stateMemory.getActiveStateList().contains(targetState);

        startContinuousTask(
                () -> {
                    if (stateActiveCondition.getAsBoolean()) {
                        task.run();
                    }
                },
                () -> true,
                delaySeconds); // Continue indefinitely until manually stopped
    }

    /** Stops the current continuous automation task. */
    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }
        isRunning = false;
    }

    /** Shuts down the executor service. Should be called when the application is closing. */
    public void shutdown() {
        stop();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks if a continuous automation task is currently running.
     *
     * @return true if a task is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning && scheduledTask != null && !scheduledTask.isCancelled();
    }
}
