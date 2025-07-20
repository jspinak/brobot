package io.github.jspinak.brobot.runner.ui.management;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Centralized manager for UI updates that ensures all updates happen on the JavaFX thread
 * and provides performance tracking and throttling capabilities.
 */
@Slf4j
@Component
public class UIUpdateManager {
    
    // Single thread pool for all periodic updates
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Track scheduled tasks
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    // Performance tracking
    private final Map<String, UpdateMetrics> metricsMap = new ConcurrentHashMap<>();
    
    // Update queue for batching
    private final BlockingQueue<Runnable> updateQueue = new LinkedBlockingQueue<>();
    
    // Batch processing
    private static final long BATCH_INTERVAL_MS = 16; // ~60 FPS
    private ScheduledFuture<?> batchProcessor;
    
    @PostConstruct
    public void initialize() {
        // Start batch processor
        batchProcessor = scheduler.scheduleAtFixedRate(
            this::processBatch, 0, BATCH_INTERVAL_MS, TimeUnit.MILLISECONDS
        );
        log.info("UIUpdateManager initialized with batch interval: {}ms", BATCH_INTERVAL_MS);
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down UIUpdateManager");
        
        // Cancel all scheduled tasks
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();
        
        // Stop batch processor
        if (batchProcessor != null) {
            batchProcessor.cancel(false);
        }
        
        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("UIUpdateManager shutdown complete");
    }
    
    /**
     * Executes a UI update immediately on the JavaFX thread.
     * 
     * @param taskId Identifier for tracking
     * @param update The update to execute
     */
    public void executeUpdate(String taskId, Runnable update) {
        long startTime = System.nanoTime();
        
        if (Platform.isFxApplicationThread()) {
            // Already on FX thread, execute directly
            try {
                update.run();
                recordMetrics(taskId, startTime);
            } catch (Exception e) {
                log.error("Error executing UI update: {}", taskId, e);
            }
        } else {
            // Queue for FX thread execution
            Platform.runLater(() -> {
                try {
                    update.run();
                    recordMetrics(taskId, startTime);
                } catch (Exception e) {
                    log.error("Error executing UI update: {}", taskId, e);
                }
            });
        }
    }
    
    /**
     * Queues a UI update for batch processing.
     * This is more efficient for high-frequency updates.
     * 
     * @param taskId Identifier for tracking
     * @param update The update to execute
     */
    public void queueUpdate(String taskId, Runnable update) {
        updateQueue.offer(() -> {
            long startTime = System.nanoTime();
            try {
                update.run();
                recordMetrics(taskId, startTime);
            } catch (Exception e) {
                log.error("Error executing queued update: {}", taskId, e);
            }
        });
    }
    
    /**
     * Schedules a periodic UI update.
     * 
     * @param taskId Unique identifier for the task
     * @param update The update to execute
     * @param initialDelay Initial delay before first execution
     * @param period Period between executions
     * @param unit Time unit
     * @return true if scheduled successfully, false if a task with this ID already exists
     */
    public boolean schedulePeriodicUpdate(String taskId, Runnable update, 
                                         long initialDelay, long period, TimeUnit unit) {
        if (scheduledTasks.containsKey(taskId)) {
            log.warn("Task already scheduled: {}", taskId);
            return false;
        }
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            () -> executeUpdate(taskId, update),
            initialDelay, period, unit
        );
        
        scheduledTasks.put(taskId, future);
        log.debug("Scheduled periodic update: {} with period: {} {}", taskId, period, unit);
        return true;
    }
    
    /**
     * Schedules a one-time delayed UI update.
     * 
     * @param taskId Unique identifier for the task
     * @param update The update to execute
     * @param delay Delay before execution
     * @param unit Time unit
     * @return true if scheduled successfully, false if a task with this ID already exists
     */
    public boolean scheduleDelayedUpdate(String taskId, Runnable update, long delay, TimeUnit unit) {
        if (scheduledTasks.containsKey(taskId)) {
            log.warn("Task already scheduled: {}", taskId);
            return false;
        }
        
        ScheduledFuture<?> future = scheduler.schedule(
            () -> {
                executeUpdate(taskId, update);
                scheduledTasks.remove(taskId); // Auto-remove one-time tasks
            },
            delay, unit
        );
        
        scheduledTasks.put(taskId, future);
        log.debug("Scheduled delayed update: {} with delay: {} {}", taskId, delay, unit);
        return true;
    }
    
    /**
     * Cancels a scheduled update.
     * 
     * @param taskId The task identifier
     * @return true if the task was cancelled, false if not found
     */
    public boolean cancelScheduledUpdate(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            log.debug("Cancelled scheduled update: {} (success: {})", taskId, cancelled);
            return cancelled;
        }
        return false;
    }
    
    /**
     * Gets performance metrics for a specific task.
     * 
     * @param taskId The task identifier
     * @return The metrics, or null if not found
     */
    public UpdateMetrics getMetrics(String taskId) {
        return metricsMap.get(taskId);
    }
    
    /**
     * Gets all performance metrics.
     * 
     * @return Map of task IDs to metrics
     */
    public Map<String, UpdateMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(metricsMap);
    }
    
    /**
     * Clears performance metrics for a specific task.
     * 
     * @param taskId The task identifier
     */
    public void clearMetrics(String taskId) {
        metricsMap.remove(taskId);
    }
    
    /**
     * Clears all performance metrics.
     */
    public void clearAllMetrics() {
        metricsMap.clear();
        log.info("Cleared all UI update metrics");
    }
    
    /**
     * Processes the batch of queued updates.
     */
    private void processBatch() {
        if (updateQueue.isEmpty()) {
            return;
        }
        
        // Collect all pending updates
        final int batchSize = updateQueue.size();
        final Runnable[] updates = new Runnable[batchSize];
        for (int i = 0; i < batchSize && !updateQueue.isEmpty(); i++) {
            updates[i] = updateQueue.poll();
        }
        
        // Execute batch on FX thread
        Platform.runLater(() -> {
            long batchStart = System.nanoTime();
            int executed = 0;
            
            for (Runnable update : updates) {
                if (update != null) {
                    update.run();
                    executed++;
                }
            }
            
            if (executed > 0) {
                long duration = System.nanoTime() - batchStart;
                log.trace("Processed batch of {} updates in {}ms", 
                         executed, TimeUnit.NANOSECONDS.toMillis(duration));
            }
        });
    }
    
    /**
     * Records performance metrics for an update.
     */
    private void recordMetrics(String taskId, long startTime) {
        long duration = System.nanoTime() - startTime;
        metricsMap.computeIfAbsent(taskId, k -> new UpdateMetrics(k))
                  .recordUpdate(duration);
    }
    
    /**
     * Performance metrics for UI updates.
     */
    public static class UpdateMetrics {
        private final String taskId;
        private final AtomicLong totalUpdates = new AtomicLong();
        private final AtomicLong totalDuration = new AtomicLong();
        private final AtomicLong maxDuration = new AtomicLong();
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        
        public UpdateMetrics(String taskId) {
            this.taskId = taskId;
        }
        
        void recordUpdate(long durationNanos) {
            totalUpdates.incrementAndGet();
            totalDuration.addAndGet(durationNanos);
            
            // Update max
            long currentMax;
            do {
                currentMax = maxDuration.get();
            } while (durationNanos > currentMax && !maxDuration.compareAndSet(currentMax, durationNanos));
            
            // Update min
            long currentMin;
            do {
                currentMin = minDuration.get();
            } while (durationNanos < currentMin && !minDuration.compareAndSet(currentMin, durationNanos));
        }
        
        public String getTaskId() {
            return taskId;
        }
        
        public long getTotalUpdates() {
            return totalUpdates.get();
        }
        
        public double getAverageDurationMs() {
            long updates = totalUpdates.get();
            if (updates == 0) return 0;
            return TimeUnit.NANOSECONDS.toMicros(totalDuration.get()) / 1000.0 / updates;
        }
        
        public double getMaxDurationMs() {
            return TimeUnit.NANOSECONDS.toMicros(maxDuration.get()) / 1000.0;
        }
        
        public double getMinDurationMs() {
            long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0 : TimeUnit.NANOSECONDS.toMicros(min) / 1000.0;
        }
        
        @Override
        public String toString() {
            return String.format("UpdateMetrics[task=%s, updates=%d, avg=%.2fms, max=%.2fms, min=%.2fms]",
                taskId, getTotalUpdates(), getAverageDurationMs(), getMaxDurationMs(), getMinDurationMs());
        }
    }
}