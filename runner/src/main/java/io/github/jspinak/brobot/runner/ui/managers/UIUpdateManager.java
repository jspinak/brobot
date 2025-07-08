package io.github.jspinak.brobot.runner.ui.managers;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Centralized manager for UI updates.
 * This component consolidates all periodic UI updates into a single
 * executor service, preventing the proliferation of update threads
 * and ensuring proper synchronization.
 * 
 * Key features:
 * - Single thread pool for all UI updates
 * - Automatic JavaFX Platform.runLater() wrapping
 * - Task cancellation and lifecycle management
 * - Performance monitoring
 */
@Slf4j
@Component
public class UIUpdateManager {
    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final Map<String, Long> taskExecutionTimes = new ConcurrentHashMap<>();
    
    public UIUpdateManager() {
        // Create a scheduled executor with a reasonable thread pool size
        this.executor = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("UIUpdateManager-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });
        
        log.info("UIUpdateManager initialized with 2 threads");
    }
    
    /**
     * Schedules a periodic UI update task.
     * The task will be automatically wrapped in Platform.runLater().
     * 
     * @param taskId Unique identifier for the task
     * @param task The task to execute
     * @param initialDelay Initial delay before first execution
     * @param period Period between executions
     * @param unit Time unit for delays
     */
    public void scheduleUpdate(String taskId, Runnable task, long initialDelay, long period, TimeUnit unit) {
        cancelTask(taskId);
        
        Runnable wrappedTask = wrapTaskWithErrorHandling(taskId, task);
        
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(wrappedTask, initialDelay, period, unit);
        tasks.put(taskId, future);
        
        log.debug("Scheduled UI update task '{}' with period {} {}", taskId, period, unit);
    }
    
    /**
     * Schedules a one-time UI update task.
     * 
     * @param taskId Unique identifier for the task
     * @param task The task to execute
     * @param delay Delay before execution
     * @param unit Time unit for delay
     */
    public void scheduleOnce(String taskId, Runnable task, long delay, TimeUnit unit) {
        cancelTask(taskId);
        
        Runnable wrappedTask = wrapTaskWithErrorHandling(taskId, () -> {
            task.run();
            tasks.remove(taskId); // Remove after execution
        });
        
        ScheduledFuture<?> future = executor.schedule(wrappedTask, delay, unit);
        tasks.put(taskId, future);
        
        log.debug("Scheduled one-time UI update task '{}' with delay {} {}", taskId, delay, unit);
    }
    
    /**
     * Executes a UI update immediately on the JavaFX thread.
     * 
     * @param task The task to execute
     */
    public void executeNow(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
    
    /**
     * Cancels a scheduled task.
     * 
     * @param taskId The task ID to cancel
     * @return true if the task was cancelled
     */
    public boolean cancelTask(String taskId) {
        ScheduledFuture<?> future = tasks.remove(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            log.debug("Cancelled UI update task '{}': {}", taskId, cancelled);
            return cancelled;
        }
        return false;
    }
    
    /**
     * Cancels all scheduled tasks.
     */
    public void cancelAllTasks() {
        int count = tasks.size();
        tasks.forEach((id, future) -> future.cancel(false));
        tasks.clear();
        taskExecutionTimes.clear();
        log.info("Cancelled {} UI update tasks", count);
    }
    
    /**
     * Gets the number of active tasks.
     * 
     * @return The number of scheduled tasks
     */
    public int getActiveTaskCount() {
        return tasks.size();
    }
    
    /**
     * Gets the execution time statistics for tasks.
     * 
     * @return Map of task IDs to their last execution time in milliseconds
     */
    public Map<String, Long> getTaskExecutionTimes() {
        return new ConcurrentHashMap<>(taskExecutionTimes);
    }
    
    /**
     * Gets tasks that are running slower than the specified threshold.
     * 
     * @param thresholdMs Threshold in milliseconds
     * @return Map of slow task IDs to their execution times
     */
    public Map<String, Long> getSlowTasks(long thresholdMs) {
        return taskExecutionTimes.entrySet().stream()
            .filter(entry -> entry.getValue() > thresholdMs)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e2,
                ConcurrentHashMap::new
            ));
    }
    
    /**
     * Wraps a task with error handling and performance monitoring.
     * 
     * @param taskId The task identifier
     * @param task The task to wrap
     * @return The wrapped task
     */
    private Runnable wrapTaskWithErrorHandling(String taskId, Runnable task) {
        return () -> {
            long startTime = System.currentTimeMillis();
            try {
                Platform.runLater(task);
                long executionTime = System.currentTimeMillis() - startTime;
                taskExecutionTimes.put(taskId, executionTime);
                
                if (executionTime > 100) {
                    log.warn("Slow UI update detected for task '{}': {}ms", taskId, executionTime);
                }
            } catch (Exception e) {
                log.error("Error in UI update task '{}'", taskId, e);
            }
        };
    }
    
    /**
     * Checks if a task is currently scheduled.
     * 
     * @param taskId The task ID to check
     * @return true if the task is scheduled
     */
    public boolean isTaskScheduled(String taskId) {
        ScheduledFuture<?> future = tasks.get(taskId);
        return future != null && !future.isDone() && !future.isCancelled();
    }
    
    /**
     * Logs the current state of the update manager.
     * Useful for debugging performance issues.
     */
    public void logManagerState() {
        log.info("UI Update Manager State:");
        log.info("Active tasks: {}", tasks.size());
        
        if (log.isDebugEnabled()) {
            tasks.forEach((id, future) -> {
                Long executionTime = taskExecutionTimes.get(id);
                log.debug("  {} - Done: {}, Cancelled: {}, Last execution: {}ms", 
                    id, 
                    future.isDone(), 
                    future.isCancelled(),
                    executionTime != null ? executionTime : "N/A");
            });
        }
    }
    
    /**
     * Shuts down the update manager.
     * Called automatically by Spring on application shutdown.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down UIUpdateManager");
        cancelAllTasks();
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.warn("Forced shutdown of UIUpdateManager executor");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Creates a builder for more complex update configurations.
     * 
     * @param taskId The task ID
     * @return A new update task builder
     */
    public UpdateTaskBuilder updateTask(String taskId) {
        return new UpdateTaskBuilder(this, taskId);
    }
    
    /**
     * Builder for creating update tasks with fluent API.
     */
    public static class UpdateTaskBuilder {
        private final UIUpdateManager manager;
        private final String taskId;
        private Runnable task;
        private long initialDelay = 0;
        private long period = 1;
        private TimeUnit unit = TimeUnit.SECONDS;
        private boolean oneTime = false;
        
        private UpdateTaskBuilder(UIUpdateManager manager, String taskId) {
            this.manager = manager;
            this.taskId = taskId;
        }
        
        public UpdateTaskBuilder withTask(Runnable task) {
            this.task = task;
            return this;
        }
        
        public UpdateTaskBuilder withInitialDelay(long delay, TimeUnit unit) {
            this.initialDelay = delay;
            this.unit = unit;
            return this;
        }
        
        public UpdateTaskBuilder withPeriod(long period, TimeUnit unit) {
            this.period = period;
            this.unit = unit;
            this.oneTime = false;
            return this;
        }
        
        public UpdateTaskBuilder oneTime() {
            this.oneTime = true;
            return this;
        }
        
        public void schedule() {
            if (task == null) {
                throw new IllegalStateException("Task must be specified");
            }
            
            if (oneTime) {
                manager.scheduleOnce(taskId, task, initialDelay, unit);
            } else {
                manager.scheduleUpdate(taskId, task, initialDelay, period, unit);
            }
        }
    }
}