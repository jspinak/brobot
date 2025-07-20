package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for managing automation execution status monitoring and updates.
 * Provides real-time status tracking and notification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RunnerStatusService {
    
    private final AutomationOrchestrator automationOrchestrator;
    
    // Configuration
    private StatusConfiguration configuration = StatusConfiguration.builder().build();
    
    // Status monitoring
    private Thread statusMonitorThread;
    private final AtomicBoolean monitoringActive = new AtomicBoolean(false);
    private final AtomicReference<ExecutionStatus> lastStatus = new AtomicReference<>();
    
    // Listeners
    private final List<StatusUpdateListener> statusListeners = new ArrayList<>();
    private final List<StateChangeListener> stateChangeListeners = new ArrayList<>();
    
    /**
     * Status monitoring configuration.
     */
    public static class StatusConfiguration {
        private long updateIntervalMs = 500;
        private boolean autoStart = true;
        private boolean notifyOnlyOnChange = true;
        
        public static StatusConfigurationBuilder builder() {
            return new StatusConfigurationBuilder();
        }
        
        public static class StatusConfigurationBuilder {
            private StatusConfiguration config = new StatusConfiguration();
            
            public StatusConfigurationBuilder updateIntervalMs(long interval) {
                config.updateIntervalMs = interval;
                return this;
            }
            
            public StatusConfigurationBuilder autoStart(boolean autoStart) {
                config.autoStart = autoStart;
                return this;
            }
            
            public StatusConfigurationBuilder notifyOnlyOnChange(boolean onlyOnChange) {
                config.notifyOnlyOnChange = onlyOnChange;
                return this;
            }
            
            public StatusConfiguration build() {
                return config;
            }
        }
    }
    
    /**
     * Sets the configuration.
     */
    public void setConfiguration(StatusConfiguration configuration) {
        this.configuration = configuration;
        
        // Restart monitoring if needed
        if (monitoringActive.get()) {
            stopMonitoring();
            startMonitoring();
        }
    }
    
    /**
     * Adds a status update listener.
     */
    public void addStatusListener(StatusUpdateListener listener) {
        statusListeners.add(listener);
    }
    
    /**
     * Adds a state change listener.
     */
    public void addStateChangeListener(StateChangeListener listener) {
        stateChangeListeners.add(listener);
    }
    
    /**
     * Removes a status update listener.
     */
    public void removeStatusListener(StatusUpdateListener listener) {
        statusListeners.remove(listener);
    }
    
    /**
     * Removes a state change listener.
     */
    public void removeStateChangeListener(StateChangeListener listener) {
        stateChangeListeners.remove(listener);
    }
    
    /**
     * Starts status monitoring.
     */
    public void startMonitoring() {
        if (monitoringActive.compareAndSet(false, true)) {
            statusMonitorThread = new Thread(this::monitorStatus, "RunnerStatus-Monitor");
            statusMonitorThread.setDaemon(true);
            statusMonitorThread.start();
            log.info("Status monitoring started");
        }
    }
    
    /**
     * Stops status monitoring.
     */
    public void stopMonitoring() {
        if (monitoringActive.compareAndSet(true, false)) {
            if (statusMonitorThread != null) {
                statusMonitorThread.interrupt();
                try {
                    statusMonitorThread.join(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Status monitoring stopped");
        }
    }
    
    /**
     * Gets the current execution status.
     */
    public ExecutionStatus getCurrentStatus() {
        return automationOrchestrator.getExecutionStatus();
    }
    
    /**
     * Gets the current execution state.
     */
    public ExecutionState getCurrentState() {
        ExecutionStatus status = getCurrentStatus();
        return status != null ? status.getState() : ExecutionState.IDLE;
    }
    
    /**
     * Checks if automation is currently running.
     */
    public boolean isRunning() {
        ExecutionState state = getCurrentState();
        return state == ExecutionState.RUNNING || state == ExecutionState.STARTING;
    }
    
    /**
     * Checks if automation is paused.
     */
    public boolean isPaused() {
        return getCurrentState() == ExecutionState.PAUSED;
    }
    
    /**
     * Checks if automation is active (running or paused).
     */
    public boolean isActive() {
        ExecutionState state = getCurrentState();
        return state.isActive();
    }
    
    /**
     * Forces an immediate status update notification.
     */
    public void forceUpdate() {
        ExecutionStatus status = getCurrentStatus();
        notifyStatusUpdate(status);
    }
    
    /**
     * Monitor thread logic.
     */
    private void monitorStatus() {
        log.debug("Status monitor thread started");
        
        while (monitoringActive.get() && !Thread.currentThread().isInterrupted()) {
            try {
                ExecutionStatus currentStatus = getCurrentStatus();
                
                if (currentStatus != null) {
                    ExecutionStatus previousStatus = lastStatus.get();
                    
                    // Check if we should notify
                    boolean shouldNotify = !configuration.notifyOnlyOnChange ||
                                         previousStatus == null ||
                                         hasStatusChanged(previousStatus, currentStatus);
                    
                    if (shouldNotify) {
                        lastStatus.set(currentStatus);
                        notifyStatusUpdate(currentStatus);
                        
                        // Check for state changes
                        if (previousStatus != null && 
                            previousStatus.getState() != currentStatus.getState()) {
                            notifyStateChange(previousStatus.getState(), currentStatus.getState());
                        }
                    }
                }
                
                Thread.sleep(configuration.updateIntervalMs);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in status monitor", e);
                // Continue monitoring despite errors
                try {
                    Thread.sleep(configuration.updateIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.debug("Status monitor thread stopped");
    }
    
    /**
     * Checks if status has changed.
     */
    private boolean hasStatusChanged(ExecutionStatus previous, ExecutionStatus current) {
        if (previous.getState() != current.getState()) {
            return true;
        }
        
        if (Math.abs(previous.getProgress() - current.getProgress()) > 0.001) {
            return true;
        }
        
        if (!equals(previous.getCurrentOperation(), current.getCurrentOperation())) {
            return true;
        }
        
        // Check for other status changes if needed
        // For now, the above checks are sufficient
        
        return false;
    }
    
    /**
     * Null-safe string comparison.
     */
    private boolean equals(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }
    
    /**
     * Notifies listeners of status update.
     */
    private void notifyStatusUpdate(ExecutionStatus status) {
        // Create a copy of listeners to avoid concurrent modification
        List<StatusUpdateListener> listeners = new ArrayList<>(statusListeners);
        
        for (StatusUpdateListener listener : listeners) {
            try {
                if (Platform.isFxApplicationThread()) {
                    listener.onStatusUpdate(status);
                } else {
                    Platform.runLater(() -> listener.onStatusUpdate(status));
                }
            } catch (Exception e) {
                log.error("Error notifying status listener", e);
            }
        }
    }
    
    /**
     * Notifies listeners of state change.
     */
    private void notifyStateChange(ExecutionState oldState, ExecutionState newState) {
        List<StateChangeListener> listeners = new ArrayList<>(stateChangeListeners);
        
        for (StateChangeListener listener : listeners) {
            try {
                if (Platform.isFxApplicationThread()) {
                    listener.onStateChange(oldState, newState);
                } else {
                    Platform.runLater(() -> listener.onStateChange(oldState, newState));
                }
            } catch (Exception e) {
                log.error("Error notifying state change listener", e);
            }
        }
    }
    
    /**
     * Gets status summary for logging.
     */
    public String getStatusSummary() {
        ExecutionStatus status = getCurrentStatus();
        if (status == null) {
            return "No status available";
        }
        
        return String.format("State: %s, Progress: %.1f%%, Operation: %s",
            status.getState().getDescription(),
            status.getProgress() * 100,
            status.getCurrentOperation() != null ? status.getCurrentOperation() : "None"
        );
    }
    
    /**
     * Listener for status updates.
     */
    @FunctionalInterface
    public interface StatusUpdateListener {
        void onStatusUpdate(ExecutionStatus status);
    }
    
    /**
     * Listener for state changes.
     */
    @FunctionalInterface
    public interface StateChangeListener {
        void onStateChange(ExecutionState oldState, ExecutionState newState);
    }
}