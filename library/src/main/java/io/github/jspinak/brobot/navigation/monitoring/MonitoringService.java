package io.github.jspinak.brobot.navigation.monitoring;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Service for executing continuous monitoring and automation tasks in the Brobot framework.
 * 
 * <p>MonitoringService provides infrastructure for long-running automation processes that need 
 * to continuously monitor GUI state and execute actions based on conditions. It implements a 
 * scheduled execution model with built-in error handling, failure tolerance, and graceful 
 * shutdown capabilities, making it ideal for background automation tasks that run alongside 
 * user interactions.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Scheduled Execution</b>: Tasks run at configurable intervals using a dedicated 
 *       thread pool</li>
 *   <li><b>Conditional Continuance</b>: Tasks continue only while specified conditions 
 *       remain true</li>
 *   <li><b>Failure Tolerance</b>: Automatic retry with configurable failure thresholds</li>
 *   <li><b>State Monitoring</b>: Direct integration with StateMemory for state-based triggers</li>
 *   <li><b>Resource Management</b>: Proper cleanup of threads and scheduled tasks</li>
 * </ul>
 * </p>
 * 
 * <p>Execution model:
 * <ul>
 *   <li>Tasks run on a single-threaded scheduled executor</li>
 *   <li>Each task execution is wrapped in exception handling</li>
 *   <li>Consecutive failures are tracked and limited</li>
 *   <li>Tasks can be stopped manually or by condition failure</li>
 *   <li>Graceful shutdown ensures tasks complete before termination</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li><b>Application Monitoring</b>: Watch for error dialogs or unexpected states</li>
 *   <li><b>Periodic Maintenance</b>: Clear caches, refresh data, or reset UI state</li>
 *   <li><b>Watchdog Functions</b>: Detect and recover from application hangs</li>
 *   <li><b>Background Workflows</b>: Process queues or sync data while user works</li>
 *   <li><b>State-based Triggers</b>: Execute actions when specific states become active</li>
 * </ul>
 * </p>
 * 
 * <p>Error handling strategy:
 * <ul>
 *   <li>Individual task failures don't stop the service</li>
 *   <li>Consecutive failures are counted and limited</li>
 *   <li>Successful execution resets the failure counter</li>
 *   <li>Service stops after reaching maximum consecutive failures</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, MonitoringService extends the framework's capabilities 
 * beyond discrete automation tasks to continuous monitoring scenarios. This enables 
 * automation that can adapt to asynchronous events, maintain application health, and 
 * provide resilience against unexpected conditions.</p>
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
    
    /**
     * The default delay between automation cycles in seconds.
     */
    private long defaultDelaySeconds = 5;
    
    /**
     * Maximum number of consecutive failures before stopping.
     */
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
    public void startContinuousTask(Runnable task, BooleanSupplier continueCondition, long delaySeconds) {
        if (isRunning) {
            ConsoleReporter.println("ContinuousAutomation is already running. Stop it first before starting a new task.");
            return;
        }
        
        if (executorService.isShutdown()) {
            ConsoleReporter.println("ContinuousAutomation executor has been shutdown. Cannot start new tasks.");
            return;
        }
        
        isRunning = true;
        consecutiveFailures = 0;
        
        scheduledTask = executorService.scheduleWithFixedDelay(() -> {
            try {
                if (!continueCondition.getAsBoolean() || consecutiveFailures >= maxConsecutiveFailures) {
                    stop();
                    return;
                }
                
                task.run();
                consecutiveFailures = 0; // Reset on successful execution
                
            } catch (Exception e) {
                consecutiveFailures++;
                ConsoleReporter.println("Error in continuous automation task: " + e.getMessage());
                if (consecutiveFailures >= maxConsecutiveFailures) {
                    ConsoleReporter.println("Maximum consecutive failures reached. Stopping continuous automation.");
                    stop();
                }
            }
        }, 0, delaySeconds, TimeUnit.SECONDS);
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
        BooleanSupplier stateActiveCondition = () -> 
            stateMemory.getActiveStateList().contains(targetState);
        
        startContinuousTask(() -> {
            if (stateActiveCondition.getAsBoolean()) {
                task.run();
            }
        }, () -> true, delaySeconds); // Continue indefinitely until manually stopped
    }

    /**
     * Stops the current continuous automation task.
     */
    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }
        isRunning = false;
        ConsoleReporter.println("ContinuousAutomation stopped.");
    }

    /**
     * Shuts down the executor service. Should be called when the application is closing.
     */
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