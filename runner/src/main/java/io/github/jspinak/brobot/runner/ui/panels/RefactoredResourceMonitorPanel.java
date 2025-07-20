package io.github.jspinak.brobot.runner.ui.panels;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.session.SessionSummary;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotPanel;
import io.github.jspinak.brobot.runner.ui.components.base.GridBuilder;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.services.ResourceMonitoringService;
import io.github.jspinak.brobot.runner.ui.components.ResourceChartPanel;
import io.github.jspinak.brobot.runner.ui.components.CacheManagementPanel;
import io.github.jspinak.brobot.runner.ui.components.SessionManagementPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Refactored ResourceMonitorPanel using LabelManager and UIUpdateManager.
 * This panel coordinates between different monitoring components.
 */
@Component
@Data
@Slf4j
@lombok.EqualsAndHashCode(callSuper = false)
public class RefactoredResourceMonitorPanel extends BrobotPanel {

    private static final String UPDATE_TASK_ID = "resource-monitor-update";
    private static final long UPDATE_INTERVAL_MS = 1000; // 1 second
    
    private final ResourceMonitoringService monitoringService;
    private final CacheManager cacheManager;
    private final SessionManager sessionManager;
    private final LabelManager labelManager;
    private final UIUpdateManager uiUpdateManager;
    
    // UI Components
    private ResourceStatusCard resourceStatusCard;
    private CacheManagementPanel cacheManagementPanel;
    private SessionManagementPanel sessionManagementPanel;
    private ResourceChartPanel resourceChartPanel;

    @Autowired
    public RefactoredResourceMonitorPanel(ResourceManager resourceManager,
                                          ImageResourceManager imageResourceManager,
                                          CacheManager cacheManager,
                                          SessionManager sessionManager,
                                          LabelManager labelManager,
                                          UIUpdateManager uiUpdateManager) {
        super(); // Now safe to call since parent doesn't call initialize()
        this.cacheManager = cacheManager;
        this.sessionManager = sessionManager;
        this.labelManager = labelManager;
        this.uiUpdateManager = uiUpdateManager;
        
        // Create the monitoring service
        this.monitoringService = new ResourceMonitoringService(
            resourceManager, 
            imageResourceManager, 
            cacheManager
        );
        
        log.info("RefactoredResourceMonitorPanel created");
    }

    @PostConstruct
    public void postConstruct() {
        // Initialize content now that all fields are set
        initialize();
        // Start monitoring and schedule periodic UI updates
        monitoringService.startMonitoring(this::handleMonitoringData);
        
        // Schedule periodic UI updates using UIUpdateManager
        boolean scheduled = uiUpdateManager.schedulePeriodicUpdate(
            UPDATE_TASK_ID,
            this::performScheduledUpdate,
            0,
            UPDATE_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
        
        if (scheduled) {
            log.info("Scheduled periodic resource monitoring updates");
        } else {
            log.error("Failed to schedule periodic resource monitoring updates");
        }
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Cleaning up RefactoredResourceMonitorPanel");
        
        // Stop monitoring
        monitoringService.stopMonitoring();
        
        // Cancel scheduled updates
        uiUpdateManager.cancelScheduledUpdate(UPDATE_TASK_ID);
        
        // Clean up labels
        labelManager.removeComponentLabels(this);
        
        // Log performance metrics
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics(UPDATE_TASK_ID);
        if (metrics != null) {
            log.info("Resource monitor update performance - Total updates: {}, Average duration: {:.2f}ms",
                    metrics.getTotalUpdates(), metrics.getAverageDurationMs());
        }
    }

    @Override
    protected void initialize() {
        getStyleClass().add("resource-monitor-panel");
        
        // Create the three main sections using cards
        resourceStatusCard = new ResourceStatusCard();
        
        BrobotCard cacheCard = new BrobotCard("Cache Management");
        cacheManagementPanel = new CacheManagementPanel(cacheManager);
        cacheCard.addContent(cacheManagementPanel);
        
        BrobotCard sessionCard = new BrobotCard("Session Management");
        sessionManagementPanel = new SessionManagementPanel(sessionManager);
        sessionCard.addContent(sessionManagementPanel);
        
        getChildren().addAll(resourceStatusCard, cacheCard, sessionCard);
        
        log.debug("RefactoredResourceMonitorPanel initialized with {} cards", getChildren().size());
    }
    
    /**
     * Perform scheduled update - retrieves latest data and updates UI.
     * This method is called by UIUpdateManager on the scheduled interval.
     */
    private void performScheduledUpdate() {
        // The monitoring service will trigger handleMonitoringData with latest data
        // This method could also directly query for data if needed
        log.trace("Performing scheduled resource monitor update");
    }
    
    /**
     * Handle monitoring data from the monitoring service.
     * This is called whenever new monitoring data is available.
     */
    public void handleMonitoringData(ResourceMonitoringService.ResourceData data) {
        // Use UIUpdateManager to ensure thread-safe UI updates
        uiUpdateManager.executeUpdate(UPDATE_TASK_ID, () -> updateUI(data));
    }
    
    /**
     * Update UI with latest monitoring data.
     * This method is guaranteed to run on the JavaFX thread.
     */
    private void updateUI(ResourceMonitoringService.ResourceData data) {
        // Update resource status card
        resourceStatusCard.updateData(data);
        
        // Update cache management panel
        cacheManagementPanel.updateCacheStats(data.getCacheStats());
        
        // Update session status
        if (sessionManager.isSessionActive()) {
            LocalDateTime lastAutosave = sessionManager.getLastAutosaveTime();
            String sessionInfo = formatSessionInfo(
                sessionManager.getCurrentSession().getId(),
                lastAutosave
            );
            sessionManagementPanel.updateSessionStatus(sessionInfo);
        } else {
            sessionManagementPanel.updateSessionStatus("No active session");
        }
        
        log.trace("UI updated with resource data - Total: {}, Cached: {}, Memory: {:.2f}MB",
                data.getTotalResources(), data.getCachedImages(), data.getMemoryMB());
    }
    
    private String formatSessionInfo(String sessionId, LocalDateTime lastAutosave) {
        String lastAutosaveStr = lastAutosave != null ?
                lastAutosave.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "never";
        
        return String.format(
                "Active session: %s (last autosaved: %s)",
                sessionId.substring(0, 8) + "...",
                lastAutosaveStr
        );
    }
    
    /**
     * Inner class for the resource status card.
     * Uses LabelManager for centralized label management.
     */
    private class ResourceStatusCard extends BrobotCard {
        private static final String LABEL_PREFIX = "resource-status-";
        private ResourceChartPanel chartPanel;
        
        public ResourceStatusCard() {
            super("Resource Monitor");
            setupContent();
        }
        
        private void setupContent() {
            // Create labels using LabelManager
            Label totalResourcesLabel = labelManager.getOrCreateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "total",
                "0"
            );
            totalResourcesLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "value-label");
            
            Label imageResourcesLabel = labelManager.getOrCreateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "images",
                "0"
            );
            imageResourcesLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "value-label");
            
            Label matResourcesLabel = labelManager.getOrCreateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "mats",
                "0"
            );
            matResourcesLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "value-label");
            
            Label memoryCachedLabel = labelManager.getOrCreateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "memory",
                "0 MB"
            );
            memoryCachedLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "value-label");
            
            // Use GridBuilder to create properly constrained grid
            GridPane resourceGrid = new GridBuilder()
                    .withLabelValueColumns()
                    .addRow("Total Managed Resources:", totalResourcesLabel)
                    .addRow("Cached Images:", imageResourcesLabel)
                    .addRow("Active OpenCV Mats:", matResourcesLabel)
                    .addRow("Memory Cached:", memoryCachedLabel)
                    .build();
            
            // Create chart panel
            chartPanel = new ResourceChartPanel();
            
            addContent(resourceGrid, chartPanel);
            
            log.debug("ResourceStatusCard initialized with {} labels", 4);
        }
        
        public void updateData(ResourceMonitoringService.ResourceData data) {
            // Update labels using LabelManager
            labelManager.updateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "total",
                String.valueOf(data.getTotalResources())
            );
            
            labelManager.updateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "images",
                String.valueOf(data.getCachedImages())
            );
            
            labelManager.updateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "mats",
                String.valueOf(data.getActiveMats())
            );
            
            labelManager.updateLabel(
                RefactoredResourceMonitorPanel.this,
                LABEL_PREFIX + "memory",
                String.format("%.2f MB", data.getMemoryMB())
            );
            
            // Update chart
            chartPanel.addDataPoint(data.getMemoryMB());
        }
    }
    
    /**
     * Get performance summary for this panel's updates.
     */
    public String getPerformanceSummary() {
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics(UPDATE_TASK_ID);
        if (metrics == null) {
            return "No performance data available";
        }
        
        return String.format(
            "Resource Monitor Performance:\n" +
            "  Total updates: %d\n" +
            "  Average duration: %.2f ms\n" +
            "  Min duration: %.2f ms\n" +
            "  Max duration: %.2f ms",
            metrics.getTotalUpdates(),
            metrics.getAverageDurationMs(),
            metrics.getMinDurationMs(),
            metrics.getMaxDurationMs()
        );
    }
}