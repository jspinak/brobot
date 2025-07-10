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
 * Service for monitoring automation execution status.
 * Provides background thread monitoring and UI update notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusMonitoringService {
    
    private final AutomationOrchestrator automationOrchestrator;
    
    // Monitoring state
    private Thread statusThread;
    private final AtomicBoolean monitoringActive = new AtomicBoolean(false);
    private final AtomicBoolean updateInProgress = new AtomicBoolean(false);
    private final AtomicReference<ExecutionStatus> lastStatus = new AtomicReference<>();
    
    // Listeners
    private final List<StatusUpdateListener> statusListeners = new ArrayList<>();
    
    // Configuration
    private MonitoringConfiguration configuration = MonitoringConfiguration.builder().build();
    
    /**
     * Monitoring configuration.
     */
    public static class MonitoringConfiguration {
        private long updateIntervalMs = 500;
        private String threadName = "AutomationStatusUpdater";
        private boolean daemon = true;
        private boolean autoStart = true;
        
        public static MonitoringConfigurationBuilder builder() {
            return new MonitoringConfigurationBuilder();
        }
        
        public static class MonitoringConfigurationBuilder {
            private MonitoringConfiguration config = new MonitoringConfiguration();
            
            public MonitoringConfigurationBuilder updateIntervalMs(long interval) {
                config.updateIntervalMs = interval;
                return this;
            }
            
            public MonitoringConfigurationBuilder threadName(String name) {
                config.threadName = name;
                return this;
            }
            
            public MonitoringConfigurationBuilder daemon(boolean daemon) {
                config.daemon = daemon;
                return this;
            }
            
            public MonitoringConfigurationBuilder autoStart(boolean autoStart) {
                config.autoStart = autoStart;
                return this;
            }
            
            public MonitoringConfiguration build() {
                return config;
            }
        }
    }
    
    /**
     * Sets the configuration.
     */
    public void setConfiguration(MonitoringConfiguration configuration) {
        this.configuration = configuration;
        
        // Restart monitoring if active
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
     * Removes a status update listener.
     */
    public void removeStatusListener(StatusUpdateListener listener) {
        statusListeners.remove(listener);
    }
    
    /**
     * Starts the background monitoring thread.
     */
    public void startMonitoring() {
        if (monitoringActive.compareAndSet(false, true)) {
            statusThread = new Thread(this::monitorStatus);
            statusThread.setDaemon(configuration.daemon);
            statusThread.setName(configuration.threadName);
            statusThread.start();
            log.info("Status monitoring started");
        }
    }
    
    /**
     * Stops the monitoring thread.
     */
    public void stopMonitoring() {
        if (monitoringActive.compareAndSet(true, false)) {
            if (statusThread != null) {
                statusThread.interrupt();
                try {
                    statusThread.join(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                statusThread = null;
            }
            log.info("Status monitoring stopped");
        }
    }
    
    /**
     * Monitoring thread logic.
     */
    private void monitorStatus() {
        log.debug("Status monitor thread started");
        
        while (monitoringActive.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // Check execution status and update UI
                if (automationOrchestrator != null) {
                    updateExecutionStatus();
                }
                
                // Sleep for the configured interval
                Thread.sleep(configuration.updateIntervalMs);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in status update thread", e);
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
     * Updates the execution status and notifies listeners.
     */
    private void updateExecutionStatus() {
        if (updateInProgress.get()) {
            return;
        }
        
        updateInProgress.set(true);
        try {
            ExecutionStatus currentStatus = automationOrchestrator.getExecutionStatus();
            if (currentStatus == null) {
                return;
            }
            
            ExecutionStatus previousStatus = lastStatus.getAndSet(currentStatus);
            
            // Check if status has changed
            if (hasStatusChanged(previousStatus, currentStatus)) {
                notifyStatusUpdate(currentStatus);
            }
            
        } finally {
            updateInProgress.set(false);
        }
    }
    
    /**
     * Checks if status has changed.
     */
    private boolean hasStatusChanged(ExecutionStatus previous, ExecutionStatus current) {
        if (previous == null) {
            return true;
        }
        
        // Check state change
        if (previous.getState() != current.getState()) {
            return true;
        }
        
        // Check progress change
        if (Math.abs(previous.getProgress() - current.getProgress()) > 0.001) {
            return true;
        }
        
        // Check operation change
        String prevOp = previous.getCurrentOperation();
        String currOp = current.getCurrentOperation();
        if (prevOp == null && currOp != null) {
            return true;
        }
        if (prevOp != null && !prevOp.equals(currOp)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Notifies listeners of status update.
     */
    private void notifyStatusUpdate(ExecutionStatus status) {
        // Create update data
        StatusUpdate update = new StatusUpdate(
            status,
            getStatusMessage(status),
            status.getProgress(),
            getPauseResumeButtonText(status),
            isPauseResumeEnabled(status)
        );
        
        // Notify listeners on JavaFX thread
        Platform.runLater(() -> {
            for (StatusUpdateListener listener : new ArrayList<>(statusListeners)) {
                try {
                    listener.onStatusUpdate(update);
                } catch (Exception e) {
                    log.error("Error notifying status listener", e);
                }
            }
        });
    }
    
    /**
     * Gets status message from execution status.
     */
    private String getStatusMessage(ExecutionStatus status) {
        if (status == null) {
            return "Ready";
        }
        
        String operation = status.getCurrentOperation();
        if (operation != null && !operation.isEmpty()) {
            return operation;
        }
        
        return status.getState().getDescription();
    }
    
    /**
     * Gets pause/resume button text based on status.
     */
    private String getPauseResumeButtonText(ExecutionStatus status) {
        return status.getState() == ExecutionState.PAUSED ? 
            "Resume Execution" : "Pause Execution";
    }
    
    /**
     * Checks if pause/resume should be enabled.
     */
    private boolean isPauseResumeEnabled(ExecutionStatus status) {
        ExecutionState state = status.getState();
        return state == ExecutionState.RUNNING || state == ExecutionState.PAUSED;
    }
    
    /**
     * Forces an immediate status update.
     */
    public void forceUpdate() {
        if (automationOrchestrator != null) {
            updateExecutionStatus();
        }
    }
    
    /**
     * Gets the current monitoring state.
     */
    public boolean isMonitoring() {
        return monitoringActive.get();
    }
    
    /**
     * Gets the last known status.
     */
    public ExecutionStatus getLastStatus() {
        return lastStatus.get();
    }
    
    /**
     * Status update data.
     */
    public static class StatusUpdate {
        private final ExecutionStatus status;
        private final String statusMessage;
        private final double progress;
        private final String pauseResumeText;
        private final boolean pauseResumeEnabled;
        
        public StatusUpdate(ExecutionStatus status, String statusMessage, double progress,
                          String pauseResumeText, boolean pauseResumeEnabled) {
            this.status = status;
            this.statusMessage = statusMessage;
            this.progress = progress;
            this.pauseResumeText = pauseResumeText;
            this.pauseResumeEnabled = pauseResumeEnabled;
        }
        
        public ExecutionStatus getStatus() { return status; }
        public String getStatusMessage() { return "Status: " + statusMessage; }
        public double getProgress() { return progress; }
        public String getPauseResumeText() { return pauseResumeText; }
        public boolean isPauseResumeEnabled() { return pauseResumeEnabled; }
    }
    
    /**
     * Listener for status updates.
     */
    @FunctionalInterface
    public interface StatusUpdateListener {
        void onStatusUpdate(StatusUpdate update);
    }
}