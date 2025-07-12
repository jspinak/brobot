package io.github.jspinak.brobot.runner.ui.enhanced.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for monitoring automation execution status.
 * Runs a background thread to update UI components with current status.
 */
@Slf4j
@Service
public class EnhancedStatusMonitoringService {
    
    private final AutomationOrchestrator automationOrchestrator;
    private final AtomicBoolean monitoringActive;
    private final AtomicBoolean updateInProgress;
    private final CopyOnWriteArrayList<StatusUpdateListener> listeners;
    
    private Thread monitoringThread;
    private ExecutionStatus lastStatus;
    
    @Getter
    @Setter
    private MonitoringConfiguration configuration;
    
    /**
     * Listener interface for status updates.
     */
    public interface StatusUpdateListener {
        void onStatusUpdate(StatusUpdate update);
    }
    
    /**
     * Status update data.
     */
    @Getter
    public static class StatusUpdate {
        private final ExecutionStatus status;
        private final String pauseResumeText;
        private final boolean pauseResumeEnabled;
        private final boolean buttonsDisabled;
        
        private StatusUpdate(ExecutionStatus status, String pauseResumeText,
                           boolean pauseResumeEnabled, boolean buttonsDisabled) {
            this.status = status;
            this.pauseResumeText = pauseResumeText;
            this.pauseResumeEnabled = pauseResumeEnabled;
            this.buttonsDisabled = buttonsDisabled;
        }
    }
    
    /**
     * Configuration for monitoring behavior.
     */
    @Getter
    @Setter
    public static class MonitoringConfiguration {
        private int updateIntervalMs;
        private String threadName;
        private boolean daemon;
        private boolean autoStart;
        
        public static MonitoringConfigurationBuilder builder() {
            return new MonitoringConfigurationBuilder();
        }
        
        public static class MonitoringConfigurationBuilder {
            private int updateIntervalMs = 100;
            private String threadName = "EnhancedStatusMonitor";
            private boolean daemon = true;
            private boolean autoStart = true;
            
            public MonitoringConfigurationBuilder updateIntervalMs(int interval) {
                this.updateIntervalMs = interval;
                return this;
            }
            
            public MonitoringConfigurationBuilder threadName(String name) {
                this.threadName = name;
                return this;
            }
            
            public MonitoringConfigurationBuilder daemon(boolean daemon) {
                this.daemon = daemon;
                return this;
            }
            
            public MonitoringConfigurationBuilder autoStart(boolean autoStart) {
                this.autoStart = autoStart;
                return this;
            }
            
            public MonitoringConfiguration build() {
                MonitoringConfiguration config = new MonitoringConfiguration();
                config.updateIntervalMs = updateIntervalMs;
                config.threadName = threadName;
                config.daemon = daemon;
                config.autoStart = autoStart;
                return config;
            }
        }
    }
    
    @Autowired
    public EnhancedStatusMonitoringService(AutomationOrchestrator automationOrchestrator) {
        this.automationOrchestrator = automationOrchestrator;
        this.monitoringActive = new AtomicBoolean(false);
        this.updateInProgress = new AtomicBoolean(false);
        this.listeners = new CopyOnWriteArrayList<>();
        this.configuration = MonitoringConfiguration.builder().build();
    }
    
    @PostConstruct
    public void initialize() {
        if (configuration.autoStart && automationOrchestrator != null) {
            startMonitoring();
        }
    }
    
    @PreDestroy
    public void cleanup() {
        stopMonitoring();
    }
    
    /**
     * Starts the status monitoring thread.
     */
    public void startMonitoring() {
        if (monitoringActive.get()) {
            log.debug("Monitoring already active");
            return;
        }
        
        monitoringActive.set(true);
        
        monitoringThread = new Thread(this::monitoringLoop);
        monitoringThread.setDaemon(configuration.daemon);
        monitoringThread.setName(configuration.threadName);
        monitoringThread.start();
        
        log.info("Status monitoring started");
    }
    
    /**
     * Stops the status monitoring thread.
     */
    public void stopMonitoring() {
        if (!monitoringActive.get()) {
            return;
        }
        
        monitoringActive.set(false);
        
        if (monitoringThread != null) {
            monitoringThread.interrupt();
            try {
                monitoringThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("Status monitoring stopped");
    }
    
    /**
     * Adds a status update listener.
     * @param listener The listener to add
     */
    public void addStatusListener(StatusUpdateListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a status update listener.
     * @param listener The listener to remove
     */
    public void removeStatusListener(StatusUpdateListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Updates the status panel directly.
     * @param statusPanel The status panel to update
     */
    public void updateStatusPanel(AutomationStatusPanel statusPanel) {
        if (statusPanel != null && lastStatus != null) {
            Platform.runLater(() -> statusPanel.updateStatus(lastStatus));
        }
    }
    
    /**
     * Gets the last known execution status.
     * @return The last status or null
     */
    public ExecutionStatus getLastStatus() {
        return lastStatus;
    }
    
    /**
     * Checks if monitoring is currently active.
     * @return true if monitoring is active
     */
    public boolean isMonitoring() {
        return monitoringActive.get();
    }
    
    private void monitoringLoop() {
        while (monitoringActive.get()) {
            try {
                if (automationOrchestrator != null) {
                    updateExecutionStatus();
                }
                Thread.sleep(configuration.updateIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in status monitoring", e);
            }
        }
    }
    
    private void updateExecutionStatus() {
        if (updateInProgress.get()) {
            return;
        }
        
        updateInProgress.set(true);
        try {
            ExecutionStatus status = automationOrchestrator.getExecutionStatus();
            if (status == null || (lastStatus != null && !hasStatusChanged(lastStatus, status))) {
                return;
            }
            
            lastStatus = status;
            
            // Create status update
            StatusUpdate update = createStatusUpdate(status);
            
            // Notify listeners on JavaFX thread
            Platform.runLater(() -> notifyListeners(update));
            
        } finally {
            updateInProgress.set(false);
        }
    }
    
    private boolean hasStatusChanged(ExecutionStatus oldStatus, ExecutionStatus newStatus) {
        if (oldStatus.getState() != newStatus.getState()) {
            return true;
        }
        
        if (oldStatus.getProgress() != newStatus.getProgress()) {
            return true;
        }
        
        String oldOp = oldStatus.getCurrentOperation();
        String newOp = newStatus.getCurrentOperation();
        if (oldOp == null ? newOp != null : !oldOp.equals(newOp)) {
            return true;
        }
        
        return false;
    }
    
    private StatusUpdate createStatusUpdate(ExecutionStatus status) {
        ExecutionState state = status.getState();
        
        String pauseResumeText;
        boolean pauseResumeEnabled;
        
        if (state == ExecutionState.RUNNING) {
            pauseResumeText = "Pause";
            pauseResumeEnabled = true;
        } else if (state == ExecutionState.PAUSED) {
            pauseResumeText = "Resume";
            pauseResumeEnabled = true;
        } else {
            pauseResumeText = "Pause";
            pauseResumeEnabled = state.isActive();
        }
        
        boolean buttonsDisabled = state.isActive();
        
        return new StatusUpdate(status, pauseResumeText, pauseResumeEnabled, buttonsDisabled);
    }
    
    private void notifyListeners(StatusUpdate update) {
        for (StatusUpdateListener listener : listeners) {
            try {
                listener.onStatusUpdate(update);
            } catch (Exception e) {
                log.error("Error notifying status listener", e);
            }
        }
    }
}