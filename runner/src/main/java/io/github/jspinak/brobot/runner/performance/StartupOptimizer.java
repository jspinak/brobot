package io.github.jspinak.brobot.runner.performance;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.StartupProgressEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupOptimizer {

    private final EventBus eventBus;
    private final PerformanceProfiler profiler;
    
    private final ExecutorService startupExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        r -> {
            Thread t = new Thread(r, "startup-optimizer");
            t.setDaemon(true);
            return t;
        }
    );
    
    private final Map<StartupPhase, List<IStartupTask>> phasedTasks = new ConcurrentHashMap<>();
    private final AtomicInteger completedTasks = new AtomicInteger();
    private volatile int totalTasks = 0;
    
    public enum StartupPhase {
        CRITICAL(1),      // Must complete before UI shows
        EARLY(2),         // Should complete early but not blocking
        NORMAL(3),        // Regular priority
        DEFERRED(4),      // Can be loaded after UI is shown
        BACKGROUND(5);    // Low priority background tasks
        
        private final int order;
        
        StartupPhase(int order) {
            this.order = order;
        }
    }
    
    @FunctionalInterface
    public interface IStartupTask {
        CompletableFuture<Void> execute();
        
        default String getName() {
            return this.getClass().getSimpleName();
        }
        
        default boolean isRetryable() {
            return false;
        }
    }
    
    public void registerTask(StartupPhase phase, IStartupTask task) {
        phasedTasks.computeIfAbsent(phase, k -> new CopyOnWriteArrayList<>()).add(task);
        totalTasks++;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MIN_VALUE) // Execute first
    public void optimizeStartup() {
        log.info("Starting optimized application initialization");
        
        try (var timer = profiler.startOperation("startup-optimization")) {
            executeStartupPhases();
        }
    }
    
    private void executeStartupPhases() {
        // Execute phases in order
        Arrays.stream(StartupPhase.values())
            .sorted(Comparator.comparingInt(p -> p.order))
            .forEach(this::executePhase);
            
        // Shutdown executor after all tasks complete
        startupExecutor.shutdown();
        
        try {
            if (!startupExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Startup tasks did not complete within timeout");
                startupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for startup completion", e);
        }
    }
    
    private void executePhase(StartupPhase phase) {
        List<IStartupTask> tasks = phasedTasks.get(phase);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        
        log.debug("Executing startup phase: {}", phase);
        
        CompletableFuture<?>[] futures = tasks.stream()
            .map(task -> executeTask(phase, task))
            .toArray(CompletableFuture[]::new);
            
        // Wait for critical phase to complete before proceeding
        if (phase == StartupPhase.CRITICAL) {
            try {
                CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Critical startup phase failed", e);
                throw new RuntimeException("Failed to complete critical startup tasks", e);
            }
        }
    }
    
    private CompletableFuture<Void> executeTask(StartupPhase phase, IStartupTask task) {
        return CompletableFuture
            .runAsync(() -> {
                String taskName = task.getName();
                log.debug("Starting task: {} in phase: {}", taskName, phase);
                
                try (var timer = profiler.startOperation("startup-" + taskName)) {
                    task.execute().join();
                    
                    int completed = completedTasks.incrementAndGet();
                    publishProgress(taskName, completed);
                    
                    log.debug("Completed task: {}", taskName);
                } catch (Exception e) {
                    handleTaskFailure(phase, task, e);
                }
            }, startupExecutor)
            .exceptionally(throwable -> {
                log.error("Unexpected error in task: {}", task.getName(), throwable);
                return null;
            });
    }
    
    private void handleTaskFailure(StartupPhase phase, IStartupTask task, Exception e) {
        String taskName = task.getName();
        
        if (phase == StartupPhase.CRITICAL) {
            log.error("Critical startup task failed: {}", taskName, e);
            throw new RuntimeException("Critical startup task failed: " + taskName, e);
        }
        
        if (task.isRetryable()) {
            log.warn("Retrying failed task: {}", taskName, e);
            try {
                task.execute().get(5, TimeUnit.SECONDS);
                log.info("Task retry successful: {}", taskName);
            } catch (Exception retryException) {
                log.error("Task retry failed: {}", taskName, retryException);
            }
        } else {
            log.error("Non-critical startup task failed: {}", taskName, e);
        }
    }
    
    private void publishProgress(String taskName, int completed) {
        double progress = totalTasks > 0 ? (double) completed / totalTasks * 100 : 0;
        
        eventBus.publish(new StartupProgressEvent(
            taskName,
            completed,
            totalTasks,
            progress
        ));
    }
    
    // Utility method to create deferred loading tasks
    public static IStartupTask deferredLoader(String name, Runnable loader) {
        return new IStartupTask() {
            @Override
            public CompletableFuture<Void> execute() {
                return CompletableFuture.runAsync(loader);
            }
            
            @Override
            public String getName() {
                return name;
            }
        };
    }
    
    // Utility method for retryable tasks
    public static IStartupTask retryableTask(String name, Supplier<CompletableFuture<Void>> supplier) {
        return new IStartupTask() {
            @Override
            public CompletableFuture<Void> execute() {
                return supplier.get();
            }
            
            @Override
            public String getName() {
                return name;
            }
            
            @Override
            public boolean isRetryable() {
                return true;
            }
        };
    }
}