package io.github.jspinak.brobot.runner.execution;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.context.ExecutionOptions;
import io.github.jspinak.brobot.runner.execution.control.ExecutionControl;
import io.github.jspinak.brobot.runner.execution.control.PausableExecutionControl;
import io.github.jspinak.brobot.runner.execution.service.ExecutionService;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Thin orchestrator for managing execution of automation tasks.
 * 
 * This refactored controller delegates responsibilities to specialized services:
 * - ExecutionService: Core execution logic
 * - ExecutionThreadManager: Thread management
 * - ExecutionTimeoutManager: Timeout handling
 * - ExecutionSafetyService: Safety checks
 * 
 * The controller now focuses solely on:
 * - API coordination
 * - Control flow (pause/resume/stop)
 * - Status access
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 2.0.0
 */
@Slf4j
@Component
public class ExecutionController implements AutoCloseable, DiagnosticCapable {
    
    private final ExecutionService executionService;
    private final ExecutionStatusManager statusManager;
    private final SafetyManager safetyManager;
    
    // Current execution tracking
    private final AtomicReference<String> currentExecutionId = new AtomicReference<>();
    private final AtomicReference<ExecutionControl> currentControl = new AtomicReference<>();
    private final Map<String, CompletableFuture<Void>> activeFutures = new ConcurrentHashMap<>();
    
    // Status tracking
    @Getter
    private final ExecutionStatus status = new ExecutionStatus();
    
    // Logging callback
    @Setter
    private Consumer<String> logCallback;
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    @Autowired
    public ExecutionController(ExecutionService executionService,
                             ResourceManager resourceManager) {
        this.executionService = executionService;
        this.statusManager = new ExecutionStatusManager(this.status);
        this.safetyManager = new SafetyManager();
        
        // Register with ResourceManager for auto-cleanup
        resourceManager.registerResource(this, "ExecutionController");
        
        log.info("ExecutionController initialized (refactored version)");
    }
    
    /**
     * Sets a consumer that will be notified of status changes.
     *
     * @param statusConsumer The consumer to be notified when status changes
     */
    public void setStatusConsumer(Consumer<ExecutionStatus> statusConsumer) {
        statusManager.setStatusConsumer(statusConsumer);
    }
    
    /**
     * Executes an automation function based on a Button definition.
     *
     * @param button Button definition containing execution parameters
     * @param automationTask The actual task to execute
     * @param timeoutMillis Timeout in milliseconds, or 0 for no timeout
     * @param statusConsumer Optional consumer for status updates
     */
    public void executeAutomation(TaskButton button, Runnable automationTask, 
                                long timeoutMillis, Consumer<ExecutionStatus> statusConsumer) {
        if (isRunning()) {
            throw new IllegalStateException("An automation task is already running");
        }
        
        log("Starting automation: " + button.getLabel());
        
        // Create execution options
        ExecutionOptions options = ExecutionOptions.builder()
                .timeout(timeoutMillis > 0 ? Duration.ofMillis(timeoutMillis) : Duration.ofMinutes(30))
                .safeMode(true)
                .diagnosticMode(diagnosticMode.get())
                .build();
        
        // Create pausable control
        String executionId = "exec-" + System.currentTimeMillis();
        PausableExecutionControl control = new PausableExecutionControl(executionId);
        
        // Store current execution info
        currentExecutionId.set(executionId);
        currentControl.set(control);
        
        // Reset and set status management
        statusManager.reset();
        if (statusConsumer != null) {
            statusManager.setStatusConsumer(statusConsumer);
        }
        
        // Create wrapped task with pause/stop support and safety checks
        Runnable wrappedTask = () -> {
            try {
                // Initial safety check
                safetyManager.performSafetyCheck();
                
                // Check for immediate stop
                control.checkPaused();
                
                // Run the automation task
                automationTask.run();
                
                log("Function completed: " + button.getLabel());
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Execution interrupted: " + e.getMessage());
                throw new RuntimeException("Execution interrupted", e);
            } catch (Exception e) {
                log("Error executing function: " + e.getMessage());
                throw e;
            }
        };
        
        // Register status listener with execution service
        executionService.addStatusListener(executionId, status -> {
            // Update our status from execution service status
            updateStatusFromService(status);
            
            // Handle pause state synchronization
            if (control.isPaused() && status.getState() == ExecutionState.RUNNING) {
                statusManager.updateState(ExecutionState.PAUSED);
            }
        });
        
        // Execute through service
        CompletableFuture<Void> future = executionService.execute(
                wrappedTask, 
                button.getLabel(), 
                options
        );
        
        // Store future
        activeFutures.put(executionId, future);
        
        // Handle completion
        future.whenComplete((result, error) -> {
            cleanupExecution(executionId);
            
            if (error != null) {
                log("Execution failed: " + error.getMessage());
            } else if (control.isStopRequested()) {
                log("Execution stopped by user");
            } else {
                log("Execution completed successfully");
            }
        });
    }
    
    /**
     * Pauses the current execution if running.
     */
    public void pauseExecution() {
        ExecutionControl control = currentControl.get();
        if (control != null && isRunning()) {
            control.pause();
            statusManager.updateState(ExecutionState.PAUSED);
            log("Execution paused");
        }
    }
    
    /**
     * Resumes the execution if paused.
     */
    public void resumeExecution() {
        ExecutionControl control = currentControl.get();
        if (control != null && control.isPaused()) {
            control.resume();
            statusManager.updateState(ExecutionState.RUNNING);
            log("Execution resumed");
        }
    }
    
    /**
     * Stops the current execution if running.
     */
    public void stopExecution() {
        String executionId = currentExecutionId.get();
        ExecutionControl control = currentControl.get();
        
        if (executionId == null || control == null || !isRunning()) {
            return;
        }
        
        log("Stopping execution");
        
        // Signal stop through control
        control.stop();
        
        // Cancel through execution service
        executionService.cancel(executionId);
        
        statusManager.updateState(ExecutionState.STOPPING);
    }
    
    /**
     * Checks if an automation task is currently running.
     */
    public boolean isRunning() {
        ExecutionState state = status.getState();
        return state == ExecutionState.STARTING || 
               state == ExecutionState.RUNNING ||
               state == ExecutionState.PAUSED || 
               state == ExecutionState.STOPPING;
    }
    
    /**
     * Checks if execution is paused.
     */
    public boolean isPaused() {
        ExecutionControl control = currentControl.get();
        return control != null && control.isPaused() && 
               status.getState() == ExecutionState.PAUSED;
    }
    
    /**
     * Shuts down the controller and releases resources.
     */
    public void shutdown() {
        log.info("Shutting down ExecutionController");
        
        // Stop any running execution
        if (isRunning()) {
            stopExecution();
        }
        
        // Clear references
        currentExecutionId.set(null);
        currentControl.set(null);
        activeFutures.clear();
    }
    
    @Override
    public void close() {
        shutdown();
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("currentExecutionId", currentExecutionId.get());
        states.put("isRunning", isRunning());
        states.put("isPaused", isPaused());
        states.put("currentState", status.getState());
        states.put("activeFutures", activeFutures.size());
        
        return DiagnosticInfo.builder()
                .component("ExecutionController")
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
     * Updates our status from the execution service status.
     */
    private void updateStatusFromService(ExecutionStatus serviceStatus) {
        status.setState(serviceStatus.getState());
        status.setStartTime(serviceStatus.getStartTime());
        status.setEndTime(serviceStatus.getEndTime());
        status.setProgress(serviceStatus.getProgress());
        status.setCurrentOperation(serviceStatus.getCurrentOperation());
        status.setCurrentAction(serviceStatus.getCurrentAction());
        status.setError(serviceStatus.getError());
        status.setErrorMessage(serviceStatus.getErrorMessage());
        
        // Notify status manager's consumer
        statusManager.notifyConsumer();
    }
    
    /**
     * Cleans up after execution completes.
     */
    private void cleanupExecution(String executionId) {
        // Remove from active futures
        activeFutures.remove(executionId);
        
        // Clear current execution if it matches
        currentExecutionId.compareAndSet(executionId, null);
        currentControl.set(null);
        
        // Remove status listener
        executionService.removeStatusListener(executionId);
    }
    
    /**
     * Logs a message to the configured log callback and the logger.
     */
    private void log(String message) {
        log.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
}