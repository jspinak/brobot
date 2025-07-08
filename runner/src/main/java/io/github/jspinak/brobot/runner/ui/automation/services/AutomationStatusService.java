package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.ui.automation.models.AutomationStatus;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Service for monitoring automation execution status.
 * Provides real-time status updates and manages status polling.
 */
@Slf4j
@Service
public class AutomationStatusService {
    
    private final AutomationOrchestrator orchestrator;
    
    private ScheduledExecutorService statusExecutor;
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);
    
    // Status listeners
    private final List<Consumer<AutomationStatus>> statusListeners = new ArrayList<>();
    
    // Current status cache
    private volatile AutomationStatus currentStatus = AutomationStatus.idle();
    
    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL = 100;
    
    @Autowired
    public AutomationStatusService(AutomationOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }
    
    /**
     * Starts monitoring automation status.
     */
    public void startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            log.debug("Starting automation status monitoring");
            
            statusExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "AutomationStatusMonitor");
                t.setDaemon(true);
                return t;
            });
            
            statusExecutor.scheduleAtFixedRate(
                this::updateStatus,
                0,
                UPDATE_INTERVAL,
                TimeUnit.MILLISECONDS
            );
        }
    }
    
    /**
     * Stops monitoring automation status.
     */
    public void stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            log.debug("Stopping automation status monitoring");
            
            if (statusExecutor != null) {
                statusExecutor.shutdown();
                try {
                    if (!statusExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                        statusExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    statusExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Updates the current status by polling the orchestrator.
     */
    private void updateStatus() {
        try {
            AutomationStatus newStatus = pollOrchestratorStatus();
            
            // Only notify if status changed
            if (!newStatus.equals(currentStatus)) {
                currentStatus = newStatus;
                notifyStatusListeners(newStatus);
            }
            
        } catch (Exception e) {
            log.error("Error updating automation status", e);
        }
    }
    
    /**
     * Polls the orchestrator for current status.
     */
    private AutomationStatus pollOrchestratorStatus() {
        var builder = AutomationStatus.builder();
        
        // Get execution status
        io.github.jspinak.brobot.runner.execution.ExecutionStatus execStatus = orchestrator.getExecutionStatus();
        
        if (execStatus != null) {
            // Check running state
            boolean isRunning = execStatus.getState() != null && 
                               execStatus.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING;
            builder.running(isRunning);
            
            // Check paused state
            boolean isPaused = orchestrator.isPaused();
            builder.paused(isPaused);
            
            // Get current automation info
            if (isRunning || isPaused) {
                String currentAutomation = execStatus.getCurrentTaskName();
                builder.currentAutomationName(currentAutomation != null ? currentAutomation : "Unknown");
                
                // Get progress if available
                double progress = execStatus.getProgress();
                builder.progress(progress);
                
                // Get current action
                String currentAction = execStatus.getCurrentAction();
                builder.currentAction(currentAction);
                
                // Get elapsed time
                Instant startTime = execStatus.getStartTime();
                if (startTime != null) {
                    long elapsed = System.currentTimeMillis() - startTime.toEpochMilli();
                    builder.elapsedTime(elapsed);
                }
            } else {
                builder.currentAutomationName("None");
                builder.progress(0.0);
            }
            
            // Check for errors
            if (execStatus.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.FAILED) {
                builder.hasError(true);
                builder.errorMessage(execStatus.getErrorMessage());
            }
            
            // Get completion info from status
            builder.completedCount(execStatus.getCompletedSteps());
            builder.totalCount(execStatus.getTotalSteps());
        } else {
            // No execution status available
            builder.running(false);
            builder.paused(false);
            builder.currentAutomationName("None");
            builder.progress(0.0);
        }
        
        return builder.build();
    }
    
    /**
     * Registers a status listener.
     */
    public void addStatusListener(Consumer<AutomationStatus> listener) {
        statusListeners.add(listener);
        
        // Immediately notify with current status
        listener.accept(currentStatus);
    }
    
    /**
     * Removes a status listener.
     */
    public void removeStatusListener(Consumer<AutomationStatus> listener) {
        statusListeners.remove(listener);
    }
    
    /**
     * Notifies all listeners of status change.
     */
    private void notifyStatusListeners(AutomationStatus status) {
        Platform.runLater(() -> {
            for (Consumer<AutomationStatus> listener : statusListeners) {
                try {
                    listener.accept(status);
                } catch (Exception e) {
                    log.error("Error notifying status listener", e);
                }
            }
        });
    }
    
    /**
     * Gets the current status.
     */
    public AutomationStatus getCurrentStatus() {
        return currentStatus;
    }
    
    /**
     * Checks if any automation is currently running.
     */
    public boolean isRunning() {
        return currentStatus.isRunning();
    }
    
    /**
     * Checks if automation is paused.
     */
    public boolean isPaused() {
        return currentStatus.isPaused();
    }
    
    /**
     * Gets a formatted status message.
     */
    public String getStatusMessage() {
        if (!currentStatus.isRunning()) {
            return "Ready";
        }
        
        StringBuilder message = new StringBuilder();
        
        if (currentStatus.isPaused()) {
            message.append("PAUSED - ");
        }
        
        message.append("Running: ").append(currentStatus.getCurrentAutomationName());
        
        if (currentStatus.getCurrentAction() != null) {
            message.append(" | ").append(currentStatus.getCurrentAction());
        }
        
        if (currentStatus.getProgress() > 0) {
            message.append(String.format(" (%.0f%%)", currentStatus.getProgress() * 100));
        }
        
        return message.toString();
    }
    
    /**
     * Gets a formatted time string for elapsed time.
     */
    public String getElapsedTimeString() {
        long elapsed = currentStatus.getElapsedTime();
        if (elapsed <= 0) {
            return "00:00";
        }
        
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds % 60);
        }
    }
    
    /**
     * Forces an immediate status update.
     */
    public void forceUpdate() {
        if (isMonitoring.get()) {
            updateStatus();
        }
    }
    
    /**
     * Resets the status to idle.
     */
    public void reset() {
        currentStatus = AutomationStatus.idle();
        notifyStatusListeners(currentStatus);
    }
}