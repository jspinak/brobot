package io.github.jspinak.brobot.runner.ui.monitoring;

import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Performance monitoring panel that displays real-time metrics from
 * LabelManager and UIUpdateManager.
 * 
 * This panel demonstrates how to monitor the refactored UI architecture
 * and can be used during development to optimize performance.
 */
@Slf4j
@Component
public class PerformanceMonitorPanel extends VBox {
    
    private static final String UPDATE_TASK_ID = "performance-monitor-update";
    
    @Autowired
    private LabelManager labelManager;
    
    @Autowired
    private UIUpdateManager uiUpdateManager;
    
    @Autowired
    private UIComponentRegistry componentRegistry;
    
    // UI Components
    private TableView<TaskMetric> taskMetricsTable;
    private TableView<ComponentMetric> componentMetricsTable;
    private TextArea summaryArea;
    
    // Data
    private final ObservableList<TaskMetric> taskMetrics = FXCollections.observableArrayList();
    private final ObservableList<ComponentMetric> componentMetrics = FXCollections.observableArrayList();
    
    @PostConstruct
    public void initialize() {
        setupUI();
        startMonitoring();
        log.info("PerformanceMonitorPanel initialized");
    }
    
    @PreDestroy
    public void cleanup() {
        uiUpdateManager.cancelScheduledUpdate(UPDATE_TASK_ID);
        labelManager.removeComponentLabels(this);
        log.info("PerformanceMonitorPanel cleaned up");
    }
    
    private void setupUI() {
        setSpacing(16);
        setPadding(new Insets(16));
        getStyleClass().add("performance-monitor");
        
        // Title
        Label title = labelManager.getOrCreateLabel(this, "title", "Performance Monitor");
        title.getStyleClass().addAll("title", "text-bold");
        
        // Task Metrics Card
        AtlantaCard taskCard = new AtlantaCard("UI Update Tasks");
        taskCard.setExpand(true);
        taskMetricsTable = createTaskMetricsTable();
        taskCard.setContent(taskMetricsTable);
        
        // Component Metrics Card
        AtlantaCard componentCard = new AtlantaCard("Component Labels");
        componentCard.setExpand(true);
        componentMetricsTable = createComponentMetricsTable();
        componentCard.setContent(componentMetricsTable);
        
        // Summary Card
        AtlantaCard summaryCard = new AtlantaCard("System Summary");
        summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.setPrefRowCount(6);
        summaryArea.getStyleClass().add("monospace");
        summaryCard.setContent(summaryArea);
        
        // Add all cards
        getChildren().addAll(title, taskCard, componentCard, summaryCard);
        VBox.setVgrow(taskCard, Priority.ALWAYS);
        VBox.setVgrow(componentCard, Priority.ALWAYS);
    }
    
    private TableView<TaskMetric> createTaskMetricsTable() {
        TableView<TaskMetric> table = new TableView<>();
        table.setPlaceholder(labelManager.getOrCreateLabel(this, "noTasks", "No active tasks"));
        
        // Task ID Column
        TableColumn<TaskMetric, String> taskCol = new TableColumn<>("Task ID");
        taskCol.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        taskCol.setPrefWidth(200);
        
        // Update Count Column
        TableColumn<TaskMetric, Long> countCol = new TableColumn<>("Updates");
        countCol.setCellValueFactory(new PropertyValueFactory<>("updateCount"));
        countCol.setPrefWidth(80);
        
        // Average Time Column
        TableColumn<TaskMetric, String> avgCol = new TableColumn<>("Avg (ms)");
        avgCol.setCellValueFactory(new PropertyValueFactory<>("averageTime"));
        avgCol.setPrefWidth(80);
        
        // Max Time Column
        TableColumn<TaskMetric, String> maxCol = new TableColumn<>("Max (ms)");
        maxCol.setCellValueFactory(new PropertyValueFactory<>("maxTime"));
        maxCol.setPrefWidth(80);
        
        // Status Column
        TableColumn<TaskMetric, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        table.getColumns().addAll(taskCol, countCol, avgCol, maxCol, statusCol);
        table.setItems(taskMetrics);
        
        return table;
    }
    
    private TableView<ComponentMetric> createComponentMetricsTable() {
        TableView<ComponentMetric> table = new TableView<>();
        table.setPlaceholder(labelManager.getOrCreateLabel(this, "noComponents", "No registered components"));
        
        // Component Column
        TableColumn<ComponentMetric, String> componentCol = new TableColumn<>("Component");
        componentCol.setCellValueFactory(new PropertyValueFactory<>("componentName"));
        componentCol.setPrefWidth(250);
        
        // Label Count Column
        TableColumn<ComponentMetric, Integer> labelCol = new TableColumn<>("Labels");
        labelCol.setCellValueFactory(new PropertyValueFactory<>("labelCount"));
        labelCol.setPrefWidth(80);
        
        // Type Column
        TableColumn<ComponentMetric, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("componentType"));
        typeCol.setPrefWidth(150);
        
        table.getColumns().addAll(componentCol, labelCol, typeCol);
        table.setItems(componentMetrics);
        
        return table;
    }
    
    private void startMonitoring() {
        // Update every 2 seconds
        uiUpdateManager.schedulePeriodicUpdate(
            UPDATE_TASK_ID,
            this::updateMetrics,
            0, 2, TimeUnit.SECONDS
        );
    }
    
    private void updateMetrics() {
        updateTaskMetrics();
        updateComponentMetrics();
        updateSummary();
    }
    
    private void updateTaskMetrics() {
        taskMetrics.clear();
        
        Map<String, UIUpdateManager.UpdateMetrics> metrics = uiUpdateManager.getAllMetrics();
        metrics.forEach((taskId, metric) -> {
            String status = "Active";
            if (metric.getTotalUpdates() == 0) {
                status = "Idle";
            } else if (metric.getMaxDurationMs() > 16.67) { // More than 60 FPS frame time
                status = "Slow";
            }
            
            taskMetrics.add(new TaskMetric(
                taskId,
                metric.getTotalUpdates(),
                String.format("%.2f", metric.getAverageDurationMs()),
                String.format("%.2f", metric.getMaxDurationMs()),
                status
            ));
        });
    }
    
    private void updateComponentMetrics() {
        componentMetrics.clear();
        
        // For now, we'll just show summary statistics
        // In a real implementation, we'd need to add a method to LabelManager
        // to expose component-level metrics
        
        int totalComponents = labelManager.getComponentCount();
        int totalLabels = labelManager.getLabelCount();
        
        // Add a summary row
        if (totalComponents > 0) {
            componentMetrics.add(new ComponentMetric(
                "All Components", 
                totalLabels,
                String.format("%d components total", totalComponents)
            ));
        }
        
        // Note: Individual component metrics would require extending LabelManager
        // For now, we show aggregate statistics only
    }
    
    private void updateSummary() {
        StringBuilder summary = new StringBuilder();
        
        // Overall stats
        summary.append("=== System Performance Summary ===\n\n");
        summary.append(String.format("Total Labels: %d\n", labelManager.getLabelCount()));
        summary.append(String.format("Total Components: %d\n", labelManager.getComponentCount()));
        summary.append(String.format("Active Update Tasks: %d\n", taskMetrics.size()));
        
        // Performance warnings
        long slowTasks = taskMetrics.stream()
            .filter(t -> "Slow".equals(t.getStatus()))
            .count();
        
        if (slowTasks > 0) {
            summary.append(String.format("\n⚠️  Warning: %d slow tasks detected\n", slowTasks));
        }
        
        // Memory estimate
        int labelCount = labelManager.getLabelCount();
        double estimatedMemoryMB = (labelCount * 0.001); // Rough estimate
        summary.append(String.format("\nEstimated Label Memory: %.2f MB\n", estimatedMemoryMB));
        
        summaryArea.setText(summary.toString());
    }
    
    /**
     * Task metric data class.
     */
    @Data
    @AllArgsConstructor
    public static class TaskMetric {
        private String taskId;
        private long updateCount;
        private String averageTime;
        private String maxTime;
        private String status;
    }
    
    /**
     * Component metric data class.
     */
    @Data
    @AllArgsConstructor
    public static class ComponentMetric {
        private String componentName;
        private int labelCount;
        private String componentType;
    }
    
    /**
     * Exports current metrics to a string report.
     */
    public String exportMetricsReport() {
        StringBuilder report = new StringBuilder();
        report.append("Performance Metrics Report\n");
        report.append("=========================\n\n");
        
        report.append("Task Metrics:\n");
        taskMetrics.forEach(task -> {
            report.append(String.format("  %s: %d updates, avg %.2s ms, max %s ms (%s)\n",
                task.getTaskId(), task.getUpdateCount(), 
                task.getAverageTime(), task.getMaxTime(), task.getStatus()));
        });
        
        report.append("\nComponent Metrics:\n");
        componentMetrics.forEach(comp -> {
            report.append(String.format("  %s: %d labels (%s)\n",
                comp.getComponentName(), comp.getLabelCount(), comp.getComponentType()));
        });
        
        report.append("\n").append(summaryArea.getText());
        
        return report.toString();
    }
}