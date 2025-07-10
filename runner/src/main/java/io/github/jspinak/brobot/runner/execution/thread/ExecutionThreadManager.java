package io.github.jspinak.brobot.runner.execution.thread;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages execution threads for automation tasks.
 * 
 * This class is responsible for thread lifecycle management, including
 * creation, scheduling, monitoring, and cleanup of execution threads.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
public class ExecutionThreadManager implements DiagnosticCapable {
    
    // Thread pool configuration constants
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final String THREAD_NAME_PREFIX = "brobot-exec-";
    
    // Thread pool for executing tasks
    private final ThreadPoolExecutor executorService;
    
    // Thread factory for creating named threads
    private final ThreadFactory threadFactory;
    
    // Tracks active executions
    private final Map<String, Future<?>> activeExecutions = new ConcurrentHashMap<>();
    
    // Thread counter for naming
    private final AtomicInteger threadCounter = new AtomicInteger(0);
    
    // Diagnostic mode flag
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    public ExecutionThreadManager() {
        this.threadFactory = this::createThread;
        this.executorService = createExecutorService();
        
        log.info("ExecutionThreadManager initialized with core pool size: {}, max pool size: {}",
                CORE_POOL_SIZE, MAX_POOL_SIZE);
    }
    
    /**
     * Submits a task for execution with the given context.
     * 
     * @param task the task to execute
     * @param context execution context
     * @return Future representing the task execution
     */
    public Future<?> submit(Runnable task, ExecutionContext context) {
        if (task == null || context == null) {
            throw new IllegalArgumentException("Task and context must not be null");
        }
        
        log.debug("Submitting task {} with correlation ID {}",
                context.getTaskName(), context.getCorrelationId());
        
        // Wrap task with context-aware error handling
        Runnable wrappedTask = wrapTask(task, context);
        
        // Submit to executor
        Future<?> future = executorService.submit(wrappedTask);
        
        // Track active execution
        activeExecutions.put(context.getId(), future);
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Task submitted - ID: {}, Name: {}, Active tasks: {}",
                    context.getId(), context.getTaskName(), activeExecutions.size());
        }
        
        return future;
    }
    
    /**
     * Cancels an execution by its ID.
     * 
     * @param executionId the execution ID
     * @param mayInterruptIfRunning whether to interrupt if running
     * @return true if cancellation was successful
     */
    public boolean cancel(String executionId, boolean mayInterruptIfRunning) {
        Future<?> future = activeExecutions.get(executionId);
        if (future == null) {
            log.warn("Cannot cancel execution {} - not found", executionId);
            return false;
        }
        
        boolean cancelled = future.cancel(mayInterruptIfRunning);
        if (cancelled) {
            activeExecutions.remove(executionId);
            log.info("Execution {} cancelled", executionId);
        }
        
        return cancelled;
    }
    
    /**
     * Gets the number of active executions.
     */
    public int getActiveExecutionCount() {
        return activeExecutions.size();
    }
    
    /**
     * Gets the current thread pool size.
     */
    public int getPoolSize() {
        return executorService.getPoolSize();
    }
    
    /**
     * Checks if an execution is still active.
     */
    public boolean isActive(String executionId) {
        Future<?> future = activeExecutions.get(executionId);
        return future != null && !future.isDone();
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
                .component("ExecutionThreadManager")
                .states(Map.of(
                        "activeExecutions", activeExecutions.size(),
                        "poolSize", executorService.getPoolSize(),
                        "activeCount", executorService.getActiveCount(),
                        "completedTaskCount", executorService.getCompletedTaskCount(),
                        "taskCount", executorService.getTaskCount(),
                        "queueSize", executorService.getQueue().size(),
                        "largestPoolSize", executorService.getLargestPoolSize()
                ))
                .build();
    }
    
    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }
    
    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ExecutionThreadManager...");
        
        // Cancel all active executions
        activeExecutions.forEach((id, future) -> {
            if (!future.isDone()) {
                log.warn("Cancelling active execution: {}", id);
                future.cancel(true);
            }
        });
        
        // Shutdown executor
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                log.warn("Forced shutdown of executor service");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("ExecutionThreadManager shutdown complete");
    }
    
    /**
     * Creates the thread pool executor with appropriate configuration.
     */
    private ThreadPoolExecutor createExecutorService() {
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    /**
     * Creates a new thread with appropriate naming and configuration.
     */
    private Thread createThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(THREAD_NAME_PREFIX + threadCounter.incrementAndGet());
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler(this::handleUncaughtException);
        return thread;
    }
    
    /**
     * Wraps a task with context-aware error handling and cleanup.
     */
    private Runnable wrapTask(Runnable task, ExecutionContext context) {
        return () -> {
            String originalThreadName = Thread.currentThread().getName();
            try {
                // Set thread name to include task info
                Thread.currentThread().setName(
                        originalThreadName + "-" + context.getTaskName()
                );
                
                // Set thread priority based on context
                if (context.getOptions() != null) {
                    Thread.currentThread().setPriority(context.getOptions().getPriority());
                }
                
                log.debug("Starting execution of task {} on thread {}",
                        context.getTaskName(), Thread.currentThread().getName());
                
                // Execute the task
                task.run();
                
                log.debug("Completed execution of task {}", context.getTaskName());
                
            } catch (Exception e) {
                log.error("Error executing task {} with correlation ID {}",
                        context.getTaskName(), context.getCorrelationId(), e);
                throw e;
            } finally {
                // Cleanup
                activeExecutions.remove(context.getId());
                Thread.currentThread().setName(originalThreadName);
            }
        };
    }
    
    /**
     * Handles uncaught exceptions in execution threads.
     */
    private void handleUncaughtException(Thread thread, Throwable throwable) {
        log.error("Uncaught exception in thread {}", thread.getName(), throwable);
        
        // Additional error handling could be added here
        // For example: sending alerts, updating metrics, etc.
    }
}