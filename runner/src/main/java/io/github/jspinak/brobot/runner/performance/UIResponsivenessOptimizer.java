package io.github.jspinak.brobot.runner.performance;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
public class UIResponsivenessOptimizer {

    private final ExecutorService uiTaskExecutor = new ThreadPoolExecutor(
        2, 4,
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        r -> {
            Thread t = new Thread(r, "ui-async-task");
            t.setDaemon(true);
            return t;
        },
        new ThreadPoolExecutor.CallerRunsPolicy()
    );
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> new Thread(r, "ui-scheduler")
    );
    
    // Frame rate monitoring
    private final FrameRateMonitor frameRateMonitor = new FrameRateMonitor();
    private volatile double targetFrameRate = 60.0;
    private volatile double lowFrameRateThreshold = 30.0;
    
    // UI update batching
    private final Map<String, BatchedUIUpdate> batchedUpdates = new ConcurrentHashMap<>();
    private final AtomicBoolean batchingEnabled = new AtomicBoolean(true);
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing UI responsiveness optimizer");
        frameRateMonitor.start();
        
        // Schedule periodic UI optimization checks
        scheduler.scheduleAtFixedRate(this::optimizeUIPerformance, 1, 1, TimeUnit.SECONDS);
    }
    
    @PreDestroy
    public void shutdown() {
        frameRateMonitor.stop();
        scheduler.shutdownNow();
        uiTaskExecutor.shutdownNow();
    }
    
    /**
     * Execute a task asynchronously and update UI with the result.
     */
    public <T> void executeAsync(Supplier<T> backgroundTask, Consumer<T> uiUpdate) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return backgroundTask.get();
            }
        };
        
        task.setOnSucceeded(e -> {
            T result = task.getValue();
            if (Platform.isFxApplicationThread()) {
                uiUpdate.accept(result);
            } else {
                Platform.runLater(() -> uiUpdate.accept(result));
            }
        });
        
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            log.error("Async UI task failed", exception);
        });
        
        uiTaskExecutor.execute(task);
    }
    
    /**
     * Execute a task with progress updates.
     */
    public <T> Task<T> executeWithProgress(ProgressTask<T> progressTask) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return progressTask.execute(this::updateProgress, this::updateMessage);
            }
        };
        
        uiTaskExecutor.execute(task);
        return task;
    }
    
    /**
     * Batch UI updates to reduce overhead.
     */
    public void batchUpdate(String groupId, Runnable update, long maxDelayMs) {
        if (!batchingEnabled.get()) {
            Platform.runLater(update);
            return;
        }
        
        BatchedUIUpdate batch = batchedUpdates.computeIfAbsent(groupId, 
            k -> new BatchedUIUpdate(groupId, maxDelayMs));
        
        batch.addUpdate(update);
    }
    
    /**
     * Defer non-critical UI updates when frame rate is low.
     */
    public void deferrableUpdate(Runnable update, Priority priority) {
        double currentFps = frameRateMonitor.getCurrentFPS();
        
        if (currentFps < lowFrameRateThreshold && priority == Priority.LOW) {
            // Defer low priority updates
            scheduler.schedule(() -> Platform.runLater(update), 100, TimeUnit.MILLISECONDS);
        } else {
            Platform.runLater(update);
        }
    }
    
    /**
     * Create a virtualized list/table data provider for large datasets.
     */
    public <T> VirtualDataProvider<T> createVirtualDataProvider(List<T> data, int pageSize) {
        return new VirtualDataProvider<>(data, pageSize);
    }
    
    private void optimizeUIPerformance() {
        double currentFps = frameRateMonitor.getCurrentFPS();
        
        if (currentFps < lowFrameRateThreshold) {
            log.warn("Low frame rate detected: {} FPS", currentFps);
            
            // Enable more aggressive batching
            batchingEnabled.set(true);
            
            // Increase batch delays for better grouping
            batchedUpdates.values().forEach(batch -> 
                batch.setMaxDelay(Math.min(batch.getMaxDelay() * 2, 200))
            );
        } else if (currentFps > targetFrameRate * 0.9) {
            // Performance is good, reduce batching delays
            batchedUpdates.values().forEach(batch -> 
                batch.setMaxDelay(Math.max(batch.getMaxDelay() / 2, 10))
            );
        }
    }
    
    @FunctionalInterface
    public interface ProgressTask<T> {
        T execute(ProgressUpdater progressUpdater, MessageUpdater messageUpdater) throws Exception;
    }
    
    @FunctionalInterface
    public interface ProgressUpdater {
        void update(double workDone, double totalWork);
    }
    
    @FunctionalInterface
    public interface MessageUpdater {
        void update(String message);
    }
    
    public enum Priority {
        LOW, NORMAL, HIGH
    }
    
    /**
     * Monitors JavaFX frame rate.
     */
    private static class FrameRateMonitor extends AnimationTimer {
        private long lastTime = 0;
        private final List<Double> frameRates = new ArrayList<>();
        private final int maxSamples = 60;
        
        @Override
        public void handle(long now) {
            if (lastTime != 0) {
                double frameTime = (now - lastTime) / 1_000_000_000.0;
                double fps = 1.0 / frameTime;
                
                synchronized (frameRates) {
                    frameRates.add(fps);
                    if (frameRates.size() > maxSamples) {
                        frameRates.remove(0);
                    }
                }
            }
            lastTime = now;
        }
        
        public double getCurrentFPS() {
            synchronized (frameRates) {
                if (frameRates.isEmpty()) return 60.0;
                
                return frameRates.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(60.0);
            }
        }
    }
    
    /**
     * Batches UI updates to reduce overhead.
     */
    private class BatchedUIUpdate {
        private final String groupId;
        private volatile long maxDelay;
        private final List<Runnable> pendingUpdates = new CopyOnWriteArrayList<>();
        private final AtomicLong lastFlushTime = new AtomicLong(System.currentTimeMillis());
        private final AtomicBoolean flushScheduled = new AtomicBoolean(false);
        
        BatchedUIUpdate(String groupId, long maxDelay) {
            this.groupId = groupId;
            this.maxDelay = maxDelay;
        }
        
        void addUpdate(Runnable update) {
            pendingUpdates.add(update);
            scheduleFlush();
        }
        
        void setMaxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
        }
        
        long getMaxDelay() {
            return maxDelay;
        }
        
        private void scheduleFlush() {
            if (flushScheduled.compareAndSet(false, true)) {
                long timeSinceLastFlush = System.currentTimeMillis() - lastFlushTime.get();
                long delay = Math.max(0, maxDelay - timeSinceLastFlush);
                
                scheduler.schedule(this::flush, delay, TimeUnit.MILLISECONDS);
            }
        }
        
        private void flush() {
            List<Runnable> updates = new ArrayList<>(pendingUpdates);
            pendingUpdates.clear();
            flushScheduled.set(false);
            lastFlushTime.set(System.currentTimeMillis());
            
            if (!updates.isEmpty()) {
                Platform.runLater(() -> {
                    updates.forEach(Runnable::run);
                });
            }
        }
    }
    
    /**
     * Provides virtualized access to large datasets.
     */
    public static class VirtualDataProvider<T> {
        private final List<T> data;
        private final int pageSize;
        private final Map<Integer, CompletableFuture<List<T>>> pageCache = new ConcurrentHashMap<>();
        
        VirtualDataProvider(List<T> data, int pageSize) {
            this.data = data;
            this.pageSize = pageSize;
        }
        
        public CompletableFuture<List<T>> getPage(int pageNumber) {
            return pageCache.computeIfAbsent(pageNumber, this::loadPage);
        }
        
        private CompletableFuture<List<T>> loadPage(int pageNumber) {
            return CompletableFuture.supplyAsync(() -> {
                int start = pageNumber * pageSize;
                int end = Math.min(start + pageSize, data.size());
                
                if (start >= data.size()) {
                    return Collections.emptyList();
                }
                
                // Simulate some loading delay for demonstration
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return new ArrayList<>(data.subList(start, end));
            });
        }
        
        public int getTotalPages() {
            return (data.size() + pageSize - 1) / pageSize;
        }
        
        public int getTotalItems() {
            return data.size();
        }
        
        public void clearCache() {
            pageCache.clear();
        }
    }
}