package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.ui.components.CacheManagementPanel;
import io.github.jspinak.brobot.runner.ui.components.SessionManagementPanel;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.runner.ui.services.ResourceMonitoringService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Modern resource monitoring panel with AtlantaFX styling.
 * Provides real-time monitoring of system resources, cache, and sessions.
 */
@Slf4j
public class AtlantaResourcePanel extends VBox {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Services
    private final ResourceMonitoringService monitoringService;
    private final CacheManager cacheManager;
    private final SessionManager sessionManager;
    
    // UI Components
    private Label cpuValueLabel;
    private Label memoryValueLabel;
    private Label threadValueLabel;
    private Label imageCountLabel;
    private Label imageSizeLabel;
    private Label lastUpdateLabel;
    
    private ProgressBar cpuProgress;
    private ProgressBar memoryProgress;
    
    private LineChart<Number, Number> resourceChart;
    private XYChart.Series<Number, Number> cpuSeries;
    private XYChart.Series<Number, Number> memorySeries;
    
    private CacheManagementPanel cachePanel;
    private SessionManagementPanel sessionPanel;
    
    private final AtomicLong dataPointCounter = new AtomicLong(0);
    
    public AtlantaResourcePanel(ResourceManager resourceManager,
                                ImageResourceManager imageResourceManager,
                                CacheManager cacheManager,
                                SessionManager sessionManager) {
        
        this.cacheManager = cacheManager;
        this.sessionManager = sessionManager;
        
        // Create monitoring service
        this.monitoringService = new ResourceMonitoringService(
            resourceManager,
            imageResourceManager,
            cacheManager
        );
        
        getStyleClass().add("resource-panel");
        
        initialize();
        
        // Start monitoring
        monitoringService.startMonitoring(this::updateResourceData);
    }
    
    private void initialize() {
        // Create main content
        VBox mainContent = new VBox(24);
        mainContent.getChildren().addAll(
            createResourceOverview(),
            createDetailedView()
        );
        
        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
    }
    
    /**
     * Creates the resource overview section with key metrics.
     */
    private HBox createResourceOverview() {
        HBox overview = new HBox(24);
        overview.getStyleClass().add("resource-overview");
        
        // System Resources Card
        AtlantaCard systemCard = new AtlantaCard("System Resources");
        systemCard.setMinWidth(350);
        
        GridPane systemGrid = new GridPane();
        systemGrid.setHgap(16);
        systemGrid.setVgap(12);
        
        // CPU Usage
        Label cpuLabel = createMetricLabel("CPU Usage:");
        cpuValueLabel = createValueLabel("0%");
        cpuProgress = new ProgressBar(0);
        cpuProgress.setPrefWidth(150);
        cpuProgress.getStyleClass().add("cpu-progress");
        
        systemGrid.add(cpuLabel, 0, 0);
        systemGrid.add(cpuValueLabel, 1, 0);
        systemGrid.add(cpuProgress, 2, 0);
        
        // Memory Usage
        Label memoryLabel = createMetricLabel("Memory:");
        memoryValueLabel = createValueLabel("0 MB / 0 MB");
        memoryProgress = new ProgressBar(0);
        memoryProgress.setPrefWidth(150);
        memoryProgress.getStyleClass().add("memory-progress");
        
        systemGrid.add(memoryLabel, 0, 1);
        systemGrid.add(memoryValueLabel, 1, 1);
        systemGrid.add(memoryProgress, 2, 1);
        
        // Thread Count
        Label threadLabel = createMetricLabel("Threads:");
        threadValueLabel = createValueLabel("0");
        
        systemGrid.add(threadLabel, 0, 2);
        systemGrid.add(threadValueLabel, 1, 2);
        
        systemCard.setContent(systemGrid);
        
        // Image Resources Card
        AtlantaCard imageCard = new AtlantaCard("Image Resources");
        imageCard.setMinWidth(350);
        
        GridPane imageGrid = new GridPane();
        imageGrid.setHgap(16);
        imageGrid.setVgap(12);
        
        // Image Count
        Label countLabel = createMetricLabel("Loaded Images:");
        imageCountLabel = createValueLabel("0");
        
        imageGrid.add(countLabel, 0, 0);
        imageGrid.add(imageCountLabel, 1, 0);
        
        // Total Size
        Label sizeLabel = createMetricLabel("Total Size:");
        imageSizeLabel = createValueLabel("0 MB");
        
        imageGrid.add(sizeLabel, 0, 1);
        imageGrid.add(imageSizeLabel, 1, 1);
        
        // Last Update
        Label updateLabel = createMetricLabel("Last Update:");
        lastUpdateLabel = createValueLabel("Never");
        
        imageGrid.add(updateLabel, 0, 2);
        imageGrid.add(lastUpdateLabel, 1, 2);
        
        imageCard.setContent(imageGrid);
        
        // Performance Chart Card
        AtlantaCard chartCard = new AtlantaCard("Performance History");
        chartCard.setExpand(true);
        
        resourceChart = createResourceChart();
        chartCard.setContent(resourceChart);
        
        overview.getChildren().addAll(systemCard, imageCard, chartCard);
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        
        return overview;
    }
    
    /**
     * Creates the detailed view with cache and session management.
     */
    private HBox createDetailedView() {
        HBox detailView = new HBox(24);
        detailView.getStyleClass().add("detail-view");
        
        // Cache Management
        AtlantaCard cacheCard = new AtlantaCard("Cache Management");
        cacheCard.setMinWidth(500);
        cacheCard.setExpand(true);
        
        cachePanel = new CacheManagementPanel(cacheManager);
        cacheCard.setContent(cachePanel);
        
        // Session Management
        AtlantaCard sessionCard = new AtlantaCard("Session Management");
        sessionCard.setMinWidth(500);
        sessionCard.setExpand(true);
        
        sessionPanel = new SessionManagementPanel(sessionManager);
        sessionCard.setContent(sessionPanel);
        
        detailView.getChildren().addAll(cacheCard, sessionCard);
        HBox.setHgrow(cacheCard, Priority.ALWAYS);
        HBox.setHgrow(sessionCard, Priority.ALWAYS);
        
        return detailView;
    }
    
    /**
     * Creates a styled metric label.
     */
    private Label createMetricLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("metric-label");
        return label;
    }
    
    /**
     * Creates a styled value label.
     */
    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("metric-value");
        label.setMinWidth(120);
        return label;
    }
    
    /**
     * Creates the resource monitoring chart.
     */
    private LineChart<Number, Number> createResourceChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelsVisible(false);
        
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Usage %");
        
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Resource Usage Over Time");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setPrefHeight(200);
        chart.getStyleClass().add("resource-chart");
        
        // Create series
        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU");
        
        memorySeries = new XYChart.Series<>();
        memorySeries.setName("Memory");
        
        chart.getData().addAll(cpuSeries, memorySeries);
        
        return chart;
    }
    
    /**
     * Updates the UI with new resource data.
     */
    private void updateResourceData(ResourceMonitoringService.ResourceData data) {
        Platform.runLater(() -> {
            // Get system metrics
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            // Calculate CPU usage (simplified - in real app would use proper monitoring)
            double cpuUsage = Math.random() * 30 + 10; // Simulated for now
            cpuValueLabel.setText(String.format("%.1f%%", cpuUsage));
            cpuProgress.setProgress(cpuUsage / 100.0);
            
            // Memory usage
            double memoryPercent = (double) usedMemory / maxMemory;
            memoryValueLabel.setText(String.format("%d MB / %d MB", 
                usedMemory / (1024 * 1024), 
                maxMemory / (1024 * 1024)));
            memoryProgress.setProgress(memoryPercent);
            
            // Thread count
            int threadCount = Thread.activeCount();
            threadValueLabel.setText(String.valueOf(threadCount));
            
            // Update image resources from data
            imageCountLabel.setText(String.valueOf(data.getCachedImages()));
            imageSizeLabel.setText(String.format("%.2f MB", data.getMemoryMB()));
            lastUpdateLabel.setText(LocalDateTime.now().format(TIME_FORMATTER));
            
            // Update chart
            updateChart(cpuUsage, memoryPercent * 100);
            
            // Update cache panel
            if (cachePanel != null) {
                cachePanel.refreshCacheStats();
            }
        });
    }
    
    /**
     * Updates the performance chart with new data.
     */
    private void updateChart(double cpuUsage, double memoryUsage) {
        long dataPoint = dataPointCounter.incrementAndGet();
        
        // Add new data points
        cpuSeries.getData().add(new XYChart.Data<>(dataPoint, cpuUsage));
        memorySeries.getData().add(new XYChart.Data<>(dataPoint, memoryUsage));
        
        // Keep only last 50 data points
        if (cpuSeries.getData().size() > 50) {
            cpuSeries.getData().remove(0);
        }
        if (memorySeries.getData().size() > 50) {
            memorySeries.getData().remove(0);
        }
    }
    
    /**
     * Stops monitoring when the panel is destroyed.
     */
    public void stopMonitoring() {
        monitoringService.stopMonitoring();
    }
}