package io.github.jspinak.brobot.runner.ui;

import lombok.Data;

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
import io.github.jspinak.brobot.runner.ui.services.ResourceMonitoringService;
import io.github.jspinak.brobot.runner.ui.components.ResourceChartPanel;
import io.github.jspinak.brobot.runner.ui.components.CacheManagementPanel;
import io.github.jspinak.brobot.runner.ui.components.SessionManagementPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Refactored ResourceMonitorPanel using composition and single responsibility principle.
 * This panel coordinates between different monitoring components.
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.runner.ui.panels.RefactoredResourceMonitorPanel} instead.
 *             This class uses the new LabelManager and UIUpdateManager architecture for better resource management.
 *             Will be removed in version 3.0.
 */
@Deprecated(since = "2.5", forRemoval = true)
@Component
@Data
@lombok.EqualsAndHashCode(callSuper = false)
public class ResourceMonitorPanel extends BrobotPanel {

    private final ResourceMonitoringService monitoringService;
    private final CacheManager cacheManager;
    private final SessionManager sessionManager;
    
    // UI Components
    private ResourceStatusCard resourceStatusCard;
    private CacheManagementPanel cacheManagementPanel;
    private SessionManagementPanel sessionManagementPanel;
    private ResourceChartPanel resourceChartPanel;

    @Autowired
    public ResourceMonitorPanel(ResourceManager resourceManager,
                                ImageResourceManager imageResourceManager,
                                CacheManager cacheManager,
                                SessionManager sessionManager) {
        super();
        this.cacheManager = cacheManager;
        this.sessionManager = sessionManager;
        
        // Create the monitoring service
        this.monitoringService = new ResourceMonitoringService(
            resourceManager, 
            imageResourceManager, 
            cacheManager
        );
    }

    @PostConstruct
    public void postConstruct() {
        monitoringService.startMonitoring(this::updateUI);
    }

    @PreDestroy
    public void preDestroy() {
        monitoringService.stopMonitoring();
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
    }
    
    /**
     * Update UI with latest monitoring data.
     * Called by the monitoring service on the JavaFX thread.
     */
    private void updateUI(ResourceMonitoringService.ResourceData data) {
        Platform.runLater(() -> {
            resourceStatusCard.updateData(data);
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
        });
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
     * This demonstrates composition within the panel.
     */
    private class ResourceStatusCard extends BrobotCard {
        private Label totalResourcesLabel;
        private Label imageResourcesLabel;
        private Label matResourcesLabel;
        private Label memoryCachedLabel;
        private ResourceChartPanel chartPanel;
        
        public ResourceStatusCard() {
            super("Resource Monitor");
            setupContent();
        }
        
        private void setupContent() {
            // Use GridBuilder to create properly constrained grid
            GridPane resourceGrid = new GridBuilder()
                    .withLabelValueColumns()
                    .addRow("Total Managed Resources:", totalResourcesLabel = createValueLabel())
                    .addRow("Cached Images:", imageResourcesLabel = createValueLabel())
                    .addRow("Active OpenCV Mats:", matResourcesLabel = createValueLabel())
                    .addRow("Memory Cached:", memoryCachedLabel = createValueLabel())
                    .build();
            
            // Create chart panel
            chartPanel = new ResourceChartPanel();
            
            addContent(resourceGrid, chartPanel);
        }
        
        private Label createValueLabel() {
            Label label = new Label("0");
            label.getStyleClass().addAll(Styles.TEXT_BOLD, "value-label");
            return label;
        }
        
        public void updateData(ResourceMonitoringService.ResourceData data) {
            totalResourcesLabel.setText(String.valueOf(data.getTotalResources()));
            imageResourcesLabel.setText(String.valueOf(data.getCachedImages()));
            matResourcesLabel.setText(String.valueOf(data.getActiveMats()));
            memoryCachedLabel.setText(String.format("%.2f MB", data.getMemoryMB()));
            
            chartPanel.addDataPoint(data.getMemoryMB());
        }
    }
}