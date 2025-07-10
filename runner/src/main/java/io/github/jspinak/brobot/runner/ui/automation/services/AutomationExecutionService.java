package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.project.TaskButton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for managing automation execution.
 * Handles starting, pausing, stopping, and task execution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationExecutionService {
    
    private final AutomationOrchestrator automationOrchestrator;
    
    // State tracking
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final Map<String, CompletableFuture<?>> runningTasks = new ConcurrentHashMap<>();
    
    // Listeners
    private ExecutionStateListener stateListener;
    private ExecutionProgressListener progressListener;
    private ExecutionLogListener logListener;
    
    /**
     * Sets the state listener.
     */
    public void setStateListener(ExecutionStateListener listener) {
        this.stateListener = listener;
    }
    
    /**
     * Sets the progress listener.
     */
    public void setProgressListener(ExecutionProgressListener listener) {
        this.progressListener = listener;
    }
    
    /**
     * Sets the log listener.
     */
    public void setLogListener(ExecutionLogListener listener) {
        this.logListener = listener;
    }
    
    /**
     * Starts the main automation.
     */
    public CompletableFuture<Void> startAutomation() {
        if (isRunning.get()) {
            log.warn("Automation already running");
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Starting automation");
        updateState(ExecutionState.RUNNING);
        logMessage("Starting automation...");
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Start main automation sequence
                logMessage("Starting main automation sequence");
                
                // TODO: Implement actual automation execution
                // For now, simulate with a delay
                simulateExecution(5000);
                
                logMessage("Automation completed successfully");
            } catch (Exception e) {
                log.error("Error during automation execution", e);
                logMessage("Error: " + e.getMessage());
            } finally {
                updateState(ExecutionState.STOPPED);
            }
        });
    }
    
    /**
     * Executes a specific task.
     */
    public CompletableFuture<Void> executeTask(TaskButton taskButton) {
        if (isRunning.get()) {
            logMessage("Cannot start task - automation already running");
            return CompletableFuture.completedFuture(null);
        }
        
        String taskName = getTaskName(taskButton);
        String taskId = taskButton.getId();
        
        log.info("Executing task: {}", taskName);
        logMessage("Executing task: " + taskName);
        
        // Check if task is already running
        if (runningTasks.containsKey(taskId)) {
            logMessage("Task already running: " + taskName);
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> taskFuture = CompletableFuture.runAsync(() -> {
            try {
                // Update state if this is the only task
                if (runningTasks.isEmpty()) {
                    updateState(ExecutionState.RUNNING);
                }
                
                String functionName = taskButton.getFunctionName();
                if (functionName != null && !functionName.isEmpty()) {
                    logMessage("Executing function: " + functionName);
                    
                    // Execute with parameters
                    Map<String, Object> parameters = taskButton.getParametersAsMap();
                    if (parameters != null && !parameters.isEmpty()) {
                        logMessage("Parameters: " + parameters);
                    }
                    
                    // TODO: Implement actual task execution
                    // For now, simulate
                    simulateExecution(2000);
                    
                    logMessage("Task completed: " + taskName);
                } else {
                    logMessage("No function defined for task: " + taskName);
                }
            } catch (Exception e) {
                log.error("Error executing task: " + taskName, e);
                logMessage("Task failed: " + taskName + " - " + e.getMessage());
            } finally {
                runningTasks.remove(taskId);
                
                // Update state if no more tasks
                if (runningTasks.isEmpty() && isRunning.get()) {
                    updateState(ExecutionState.STOPPED);
                }
            }
        });
        
        runningTasks.put(taskId, taskFuture);
        return taskFuture;
    }
    
    /**
     * Pauses the automation.
     */
    public void pauseAutomation() {
        if (!isRunning.get() || isPaused.get()) {
            return;
        }
        
        log.info("Pausing automation");
        isPaused.set(true);
        updateState(ExecutionState.PAUSED);
        logMessage("Pausing automation...");
        
        try {
            automationOrchestrator.pauseAutomation();
        } catch (Exception e) {
            log.error("Error pausing automation", e);
            logMessage("Error pausing: " + e.getMessage());
        }
    }
    
    /**
     * Resumes the automation.
     */
    public void resumeAutomation() {
        if (!isRunning.get() || !isPaused.get()) {
            return;
        }
        
        log.info("Resuming automation");
        isPaused.set(false);
        updateState(ExecutionState.RUNNING);
        logMessage("Resuming automation...");
        
        try {
            automationOrchestrator.resumeAutomation();
        } catch (Exception e) {
            log.error("Error resuming automation", e);
            logMessage("Error resuming: " + e.getMessage());
        }
    }
    
    /**
     * Stops the automation.
     */
    public void stopAutomation() {
        if (!isRunning.get()) {
            return;
        }
        
        log.info("Stopping automation");
        logMessage("Stopping automation...");
        
        try {
            // Cancel all running tasks
            runningTasks.values().forEach(future -> future.cancel(true));
            runningTasks.clear();
            
            // Stop automation
            automationOrchestrator.stopAllAutomation();
            
            updateState(ExecutionState.STOPPED);
            logMessage("Automation stopped");
        } catch (Exception e) {
            log.error("Error stopping automation", e);
            logMessage("Error stopping: " + e.getMessage());
        }
    }
    
    /**
     * Checks if automation is running.
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * Checks if automation is paused.
     */
    public boolean isPaused() {
        return isPaused.get();
    }
    
    /**
     * Gets the current execution state.
     */
    public ExecutionState getCurrentState() {
        if (!isRunning.get()) {
            return ExecutionState.STOPPED;
        }
        return isPaused.get() ? ExecutionState.PAUSED : ExecutionState.RUNNING;
    }
    
    /**
     * Updates the execution state.
     */
    private void updateState(ExecutionState state) {
        switch (state) {
            case RUNNING:
            case STARTING:
                isRunning.set(true);
                isPaused.set(false);
                break;
            case PAUSED:
                isPaused.set(true);
                break;
            case STOPPED:
            case STOPPING:
            case COMPLETED:
            case FAILED:
            case ERROR:
            case TIMEOUT:
            case IDLE:
                isRunning.set(false);
                isPaused.set(false);
                break;
        }
        
        if (stateListener != null) {
            stateListener.onStateChanged(state);
        }
    }
    
    /**
     * Logs a message.
     */
    private void logMessage(String message) {
        if (logListener != null) {
            logListener.onLogMessage(message);
        }
    }
    
    /**
     * Updates progress.
     */
    private void updateProgress(double progress) {
        if (progressListener != null) {
            progressListener.onProgressUpdate(progress);
        }
    }
    
    /**
     * Gets the display name for a task.
     */
    private String getTaskName(TaskButton taskButton) {
        return taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
    }
    
    /**
     * Simulates execution with progress updates.
     */
    private void simulateExecution(long durationMs) {
        long step = durationMs / 10;
        
        for (int i = 1; i <= 10; i++) {
            if (!isRunning.get() || Thread.currentThread().isInterrupted()) {
                break;
            }
            
            try {
                Thread.sleep(step);
                updateProgress(i / 10.0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Listener for execution state changes.
     */
    @FunctionalInterface
    public interface ExecutionStateListener {
        void onStateChanged(ExecutionState newState);
    }
    
    /**
     * Listener for execution progress updates.
     */
    @FunctionalInterface
    public interface ExecutionProgressListener {
        void onProgressUpdate(double progress);
    }
    
    /**
     * Listener for execution log messages.
     */
    @FunctionalInterface
    public interface ExecutionLogListener {
        void onLogMessage(String message);
    }
}