package io.github.jspinak.brobot.runner.execution.service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.execution.context.ExecutionContext;
import io.github.jspinak.brobot.runner.execution.context.ExecutionOptions;
import io.github.jspinak.brobot.runner.execution.safety.ExecutionSafetyService;
import io.github.jspinak.brobot.runner.execution.thread.ExecutionThreadManager;
import io.github.jspinak.brobot.runner.execution.timeout.ExecutionTimeoutManager;
import io.github.jspinak.brobot.runner.execution.timeout.ExecutionTimeoutManager.TimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Core service for executing automation tasks.
 * 
 * This service orchestrates the execution of automation tasks, coordinating
 * with thread management, timeout monitoring, and safety services.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ExecutionService implements DiagnosticCapable {
    
    private final ExecutionThreadManager threadManager;
    private final ExecutionTimeoutManager timeoutManager;
    private final ExecutionSafetyService safetyService;
    
    // Active executions
    private final Map<String, ExecutionHandle> activeExecutions = new ConcurrentHashMap<>();
    
    // Status update listeners
    private final Map<String, Consumer<ExecutionStatus>> statusListeners = new ConcurrentHashMap<>();
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    @Autowired
    public ExecutionService(ExecutionThreadManager threadManager,
                          ExecutionTimeoutManager timeoutManager,
                          ExecutionSafetyService safetyService) {
        this.threadManager = threadManager;
        this.timeoutManager = timeoutManager;
        this.safetyService = safetyService;
        
        log.info("ExecutionService initialized");
    }
    
    /**
     * Executes a task with the given options.
     * 
     * @param task the task to execute
     * @param taskName name of the task
     * @param options execution options
     * @return CompletableFuture representing the execution
     */
    public CompletableFuture<Void> execute(Runnable task, String taskName, ExecutionOptions options) {
        if (task == null || taskName == null) {
            throw new IllegalArgumentException("Task and taskName must not be null");
        }
        
        ExecutionOptions effectiveOptions = options != null ? options : ExecutionOptions.defaultOptions();
        
        // Create execution context
        ExecutionContext context = ExecutionContext.builder()
                .taskName(taskName)
                .correlationId(generateCorrelationId())
                .options(effectiveOptions)
                .build();
        
        log.info("Starting execution - Task: {}, ID: {}, Correlation: {}",
                taskName, context.getId(), context.getCorrelationId());
        
        // Create execution handle
        ExecutionHandle handle = new ExecutionHandle(context);
        activeExecutions.put(context.getId(), handle);
        
        // Start execution with delay if specified
        if (effectiveOptions.getStartDelay().toMillis() > 0) {
            return CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(effectiveOptions.getStartDelay().toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Execution interrupted during start delay", e);
                }
            }).thenCompose(v -> executeInternal(task, context, handle));
        } else {
            return executeInternal(task, context, handle);
        }
    }
    
    /**
     * Cancels an execution.
     * 
     * @param executionId the execution ID
     * @return true if cancelled successfully
     */
    public boolean cancel(String executionId) {
        ExecutionHandle handle = activeExecutions.get(executionId);
        if (handle == null) {
            log.warn("Cannot cancel execution {} - not found", executionId);
            return false;
        }
        
        log.info("Cancelling execution - ID: {}, Task: {}",
                executionId, handle.getContext().getTaskName());
        
        // Update status
        updateStatus(handle, status -> {
            status.setState(ExecutionState.STOPPING);
            status.setCurrentOperation("Cancellation requested");
        });
        
        // Cancel the execution
        boolean cancelled = threadManager.cancel(executionId, 
                handle.getContext().getOptions().isInterruptible());
        
        if (cancelled) {
            // Stop timeout monitoring
            timeoutManager.stopMonitoring(executionId);
            
            // Update final status
            updateStatus(handle, status -> {
                status.setState(ExecutionState.STOPPED);
                status.setEndTime(Instant.now());
            });
            
            // Cleanup
            cleanup(handle);
        }
        
        return cancelled;
    }
    
    /**
     * Gets the current status of an execution.
     * 
     * @param executionId the execution ID
     * @return execution status or null if not found
     */
    public ExecutionStatus getStatus(String executionId) {
        ExecutionHandle handle = activeExecutions.get(executionId);
        return handle != null ? handle.getStatus().copy() : null;
    }
    
    /**
     * Registers a status update listener.
     * 
     * @param executionId the execution ID
     * @param listener status update consumer
     */
    public void addStatusListener(String executionId, Consumer<ExecutionStatus> listener) {
        if (listener != null) {
            statusListeners.put(executionId, listener);
        }
    }
    
    /**
     * Removes a status update listener.
     * 
     * @param executionId the execution ID
     */
    public void removeStatusListener(String executionId) {
        statusListeners.remove(executionId);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("activeExecutions", activeExecutions.size());
        states.put("statusListeners", statusListeners.size());
        
        // Add execution details
        activeExecutions.forEach((id, handle) -> {
            ExecutionStatus status = handle.getStatus();
            states.put("execution." + id + ".task", handle.getContext().getTaskName());
            states.put("execution." + id + ".state", status.getState());
            states.put("execution." + id + ".progress", String.format("%.1f%%", status.getProgress() * 100));
            states.put("execution." + id + ".duration", 
                    status.getDuration() != null ? status.getDuration().toSeconds() + "s" : "not started");
        });
        
        return DiagnosticInfo.builder()
                .component("ExecutionService")
                .states(states)
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
    
    /**
     * Internal execution implementation.
     */
    private CompletableFuture<Void> executeInternal(Runnable task, ExecutionContext context, 
                                                   ExecutionHandle handle) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // Update initial status
        updateStatus(handle, status -> {
            status.setState(ExecutionState.STARTING);
            status.setStartTime(Instant.now());
            status.setCurrentTaskName(context.getTaskName());
        });
        
        // Create wrapped task with lifecycle management
        Runnable wrappedTask = () -> {
            try {
                // Check safety before starting
                if (!safetyService.checkActionSafety(context, "execution_start")) {
                    throw new SecurityException("Execution blocked by safety service");
                }
                
                // Update to running state
                updateStatus(handle, status -> {
                    status.setState(ExecutionState.RUNNING);
                    status.setCurrentOperation("Executing task");
                });
                
                // Execute the actual task
                task.run();
                
                // Record success
                safetyService.recordSuccess(context);
                
                // Update to completed state
                updateStatus(handle, status -> {
                    status.setState(ExecutionState.COMPLETED);
                    status.setEndTime(Instant.now());
                    status.setProgress(1.0);
                });
                
                future.complete(null);
                
            } catch (Exception e) {
                handleExecutionError(context, handle, e);
                future.completeExceptionally(e);
            } finally {
                cleanup(handle);
            }
        };
        
        // Set up timeout monitoring
        TimeoutHandler timeoutHandler = (id, taskName, elapsed) -> {
            log.error("Execution timeout - ID: {}, Task: {}, Elapsed: {}",
                    id, taskName, elapsed);
            
            updateStatus(handle, status -> {
                status.setState(ExecutionState.TIMEOUT);
                status.setEndTime(Instant.now());
                status.setErrorMessage("Execution timed out after " + elapsed);
            });
            
            // Cancel the execution
            threadManager.cancel(id, true);
            
            future.completeExceptionally(
                    new java.util.concurrent.TimeoutException("Execution timed out after " + elapsed)
            );
        };
        
        timeoutManager.monitor(context, timeoutHandler);
        
        // Submit to thread manager
        Future<?> threadFuture = threadManager.submit(wrappedTask, context);
        handle.setThreadFuture(threadFuture);
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Execution started - ID: {}, Task: {}, Timeout: {}",
                    context.getId(), context.getTaskName(), context.getOptions().getTimeout());
        }
        
        return future;
    }
    
    /**
     * Handles execution errors.
     */
    private void handleExecutionError(ExecutionContext context, ExecutionHandle handle, Exception error) {
        log.error("Execution failed - ID: {}, Task: {}",
                context.getId(), context.getTaskName(), error);
        
        // Record failure in safety service
        safetyService.recordFailure(context, error);
        
        // Update status
        updateStatus(handle, status -> {
            status.setState(ExecutionState.ERROR);
            status.setEndTime(Instant.now());
            status.setError(error);
            status.setErrorMessage(error.getMessage());
        });
    }
    
    /**
     * Updates execution status and notifies listeners.
     */
    private void updateStatus(ExecutionHandle handle, Consumer<ExecutionStatus> updater) {
        ExecutionStatus status = handle.getStatus();
        updater.accept(status);
        
        // Notify listener if registered
        Consumer<ExecutionStatus> listener = statusListeners.get(handle.getContext().getId());
        if (listener != null) {
            try {
                listener.accept(status.copy());
            } catch (Exception e) {
                log.error("Error notifying status listener", e);
            }
        }
    }
    
    /**
     * Cleans up after execution completes.
     */
    private void cleanup(ExecutionHandle handle) {
        String executionId = handle.getContext().getId();
        
        // Stop timeout monitoring
        timeoutManager.stopMonitoring(executionId);
        
        // Clean up safety tracking
        safetyService.cleanupExecution(executionId);
        
        // Remove from active executions
        activeExecutions.remove(executionId);
        
        // Remove status listener
        statusListeners.remove(executionId);
        
        log.debug("Cleaned up execution - ID: {}", executionId);
    }
    
    /**
     * Generates a correlation ID for tracing.
     */
    private String generateCorrelationId() {
        return "exec-" + UUID.randomUUID().toString();
    }
    
    /**
     * Internal class representing an execution handle.
     */
    private static class ExecutionHandle {
        private final ExecutionContext context;
        private final ExecutionStatus status;
        private volatile Future<?> threadFuture;
        
        public ExecutionHandle(ExecutionContext context) {
            this.context = context;
            this.status = new ExecutionStatus();
        }
        
        public ExecutionContext getContext() {
            return context;
        }
        
        public ExecutionStatus getStatus() {
            return status;
        }
        
        public void setThreadFuture(Future<?> future) {
            this.threadFuture = future;
        }
        
        public Future<?> getThreadFuture() {
            return threadFuture;
        }
    }
}