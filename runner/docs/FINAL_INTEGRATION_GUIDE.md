# Final Integration Guide - Refactored UI Architecture

## Overview

This guide demonstrates how to integrate the refactored UI components using the new LabelManager and UIUpdateManager architecture in the Brobot Runner application.

## Quick Start

### 1. Using the UiComponentFactory

The `UiComponentFactory` has been updated to create refactored components:

```java
@Component
public class MainUI {
    @Autowired
    private UiComponentFactory uiComponentFactory;
    
    public void initializeUI() {
        // Create all refactored panels at once
        UiComponentFactory.RefactoredPanels panels = uiComponentFactory.createAllRefactoredPanels();
        
        // Or create individually
        UnifiedAutomationPanel automationPanel = uiComponentFactory.createAutomationPanel();
        RefactoredResourceMonitorPanel resourcePanel = uiComponentFactory.createRefactoredResourceMonitorPanel();
        RefactoredConfigDetailsPanel configPanel = uiComponentFactory.createRefactoredConfigDetailsPanel();
        RefactoredExecutionDashboardPanel dashboardPanel = uiComponentFactory.createRefactoredExecutionDashboardPanel();
        RefactoredAtlantaLogsPanel logsPanel = uiComponentFactory.createRefactoredAtlantaLogsPanel();
    }
}
```

### 2. Spring Configuration

Ensure all required beans are available:

```java
@Configuration
public class UIConfiguration {
    
    @Bean
    public LabelManager labelManager() {
        return new LabelManager();
    }
    
    @Bean
    public UIUpdateManager uiUpdateManager() {
        UIUpdateManager manager = new UIUpdateManager();
        manager.initialize();
        return manager;
    }
    
    @Bean
    public UIComponentRegistry uiComponentRegistry() {
        return new UIComponentRegistry();
    }
}
```

## Component Usage Examples

### 1. Unified Automation Panel

```java
// The UnifiedAutomationPanel replaces both AutomationPanel and EnhancedAutomationPanel
UnifiedAutomationPanel panel = uiComponentFactory.createAutomationPanel();

// It automatically:
// - Prevents label duplication
// - Manages hotkeys
// - Handles window control
// - Provides thread-safe updates
```

### 2. Resource Monitor Panel

```java
// Create the refactored resource monitor
RefactoredResourceMonitorPanel resourcePanel = uiComponentFactory.createRefactoredResourceMonitorPanel();

// It automatically:
// - Updates every second using UIUpdateManager
// - Manages labels centrally
// - Tracks performance metrics
// - Cleans up resources on shutdown
```

### 3. Config Details Panel

```java
// Create the refactored config details panel
RefactoredConfigDetailsPanel configPanel = uiComponentFactory.createRefactoredConfigDetailsPanel();

// Set a configuration
ConfigEntry config = new ConfigEntry();
config.setName("My Config");
config.setProject("My Project");
// ... set other properties
configPanel.setConfiguration(config);

// It automatically:
// - Manages 6 labels efficiently
// - Loads files asynchronously
// - Provides thread-safe updates
```

### 4. Execution Dashboard

```java
// Create the refactored execution dashboard
RefactoredExecutionDashboardPanel dashboard = uiComponentFactory.createRefactoredExecutionDashboardPanel();

// It automatically:
// - Monitors memory usage
// - Handles execution events
// - Updates UI safely
// - Tracks performance
```

### 5. Logs Panel

```java
// Create the refactored logs panel
RefactoredAtlantaLogsPanel logsPanel = uiComponentFactory.createRefactoredAtlantaLogsPanel();

// It automatically:
// - Manages log entries efficiently
// - Provides real-time filtering
// - Exports logs in multiple formats
// - Uses service-oriented architecture
// - Maintains performance with large log volumes
```

## Migration from Legacy Components

### Before (Legacy)
```java
// Old singleton pattern
AutomationPanel.getInstance().ifPresent(panel -> {
    panel.setStatusMessage("Running...");
    panel.log("Started task");
});

// Direct label creation
Label statusLabel = new Label("Status");
Platform.runLater(() -> statusLabel.setText("Updated"));

// Manual thread management
ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
executor.scheduleAtFixedRate(() -> updateUI(), 0, 1, TimeUnit.SECONDS);
```

### After (Refactored)
```java
// Dependency injection
@Autowired
private UnifiedAutomationPanel automationPanel;

// Use the panel directly
automationPanel.setStatusMessage("Running...");
automationPanel.log("Started task");

// Managed labels
Label statusLabel = labelManager.getOrCreateLabel(this, "status", "Status");
labelManager.updateLabel(this, "status", "Updated");

// Managed updates
uiUpdateManager.schedulePeriodicUpdate("my-update", this::updateUI, 0, 1, TimeUnit.SECONDS);
```

## Complete Application Example

```java
@SpringBootApplication
public class BrobotRunnerApplication {
    
    @Component
    public static class MainApplicationUI extends BorderPane {
        
        @Autowired
        public MainApplicationUI(UiComponentFactory factory) {
            // Create all refactored panels
            var panels = factory.createAllRefactoredPanels();
            
            // Create tab pane
            TabPane tabPane = new TabPane();
            
            // Automation tab
            Tab automationTab = new Tab("Automation");
            automationTab.setContent(panels.automationPanel());
            automationTab.setClosable(false);
            
            // Resources tab
            Tab resourcesTab = new Tab("Resources");
            resourcesTab.setContent(panels.resourceMonitorPanel());
            resourcesTab.setClosable(false);
            
            // Configuration tab
            Tab configTab = new Tab("Configuration");
            configTab.setContent(panels.configDetailsPanel());
            configTab.setClosable(false);
            
            // Execution tab
            Tab executionTab = new Tab("Execution");
            executionTab.setContent(panels.executionDashboardPanel());
            executionTab.setClosable(false);
            
            // Logs tab
            Tab logsTab = new Tab("Logs");
            logsTab.setContent(panels.logsPanel());
            logsTab.setClosable(false);
            
            tabPane.getTabs().addAll(automationTab, resourcesTab, configTab, executionTab, logsTab);
            
            // Set as center
            setCenter(tabPane);
            
            // Add status bar
            setBottom(createStatusBar());
        }
        
        private HBox createStatusBar() {
            HBox statusBar = new HBox(10);
            statusBar.setPadding(new Insets(5));
            statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
            
            Label statusLabel = new Label("Ready");
            Label memoryLabel = new Label("Memory: 0 MB");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            statusBar.getChildren().addAll(statusLabel, spacer, memoryLabel);
            
            return statusBar;
        }
    }
}
```

## Performance Monitoring

### Enable Performance Logging

```java
@Component
public class PerformanceLogger {
    
    @Autowired
    private UIUpdateManager uiUpdateManager;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void logPerformanceMetrics() {
        Map<String, UIUpdateManager.UpdateMetrics> metrics = uiUpdateManager.getAllMetrics();
        
        metrics.forEach((taskId, metric) -> {
            log.info("Task: {} - Updates: {}, Avg: {:.2f}ms, Max: {:.2f}ms",
                taskId,
                metric.getTotalUpdates(),
                metric.getAverageDurationMs(),
                metric.getMaxDurationMs()
            );
        });
    }
}
```

### Visual Performance Dashboard

```java
public class PerformanceDashboard extends VBox {
    
    @Autowired
    private UIUpdateManager uiUpdateManager;
    
    @Autowired
    private LabelManager labelManager;
    
    public PerformanceDashboard() {
        setPadding(new Insets(10));
        setSpacing(10);
        
        Label title = new Label("Performance Metrics");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        TableView<TaskMetric> table = new TableView<>();
        
        TableColumn<TaskMetric, String> taskCol = new TableColumn<>("Task");
        taskCol.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        
        TableColumn<TaskMetric, Long> countCol = new TableColumn<>("Updates");
        countCol.setCellValueFactory(new PropertyValueFactory<>("updateCount"));
        
        TableColumn<TaskMetric, Double> avgCol = new TableColumn<>("Avg (ms)");
        avgCol.setCellValueFactory(new PropertyValueFactory<>("averageTime"));
        
        table.getColumns().addAll(taskCol, countCol, avgCol);
        
        // Update every 5 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateMetrics(table)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        getChildren().addAll(title, table);
    }
    
    private void updateMetrics(TableView<TaskMetric> table) {
        ObservableList<TaskMetric> items = FXCollections.observableArrayList();
        
        uiUpdateManager.getAllMetrics().forEach((taskId, metrics) -> {
            items.add(new TaskMetric(
                taskId,
                metrics.getTotalUpdates(),
                metrics.getAverageDurationMs()
            ));
        });
        
        table.setItems(items);
    }
}
```

## Best Practices

### 1. Always Use Factory Methods
```java
// Good
RefactoredResourceMonitorPanel panel = uiComponentFactory.createRefactoredResourceMonitorPanel();

// Bad - direct instantiation
RefactoredResourceMonitorPanel panel = new RefactoredResourceMonitorPanel(...);
```

### 2. Leverage Component Registry
```java
// Register custom components
@Component
public class MyCustomPanel extends VBox {
    @PostConstruct
    public void init() {
        componentRegistry.register("myCustomPanel", this);
    }
}

// Retrieve anywhere
MyCustomPanel panel = componentRegistry.getComponent("myCustomPanel", MyCustomPanel.class);
```

### 3. Use Lifecycle Methods
```java
@Component
public class MyPanel extends VBox {
    @PostConstruct
    public void initialize() {
        // Setup UI
        // Schedule updates
        // Register event handlers
    }
    
    @PreDestroy
    public void cleanup() {
        // Cancel scheduled updates
        // Remove labels
        // Unregister handlers
    }
}
```

### 4. Monitor Performance
```java
// In your panel
public String getPerformanceSummary() {
    return String.format(
        "Panel Performance:\n" +
        "  Updates: %d\n" +
        "  Average: %.2f ms\n" +
        "  Labels: %d",
        uiUpdateManager.getMetrics(TASK_ID).getTotalUpdates(),
        uiUpdateManager.getMetrics(TASK_ID).getAverageDurationMs(),
        labelManager.getLabelCount()
    );
}
```

## Troubleshooting

### Issue: Labels Not Updating
```java
// Check label is registered
Label label = labelManager.getOrCreateLabel(this, "myLabel", "Initial");

// Update using manager
labelManager.updateLabel(this, "myLabel", "New Value");

// Verify in debug
log.debug("Label count: {}", labelManager.getLabelCount());
```

### Issue: Updates Not Running
```java
// Check task is scheduled
boolean scheduled = uiUpdateManager.schedulePeriodicUpdate(...);
if (!scheduled) {
    log.error("Failed to schedule update - task ID may already exist");
}

// Check metrics
UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("my-task");
if (metrics == null) {
    log.error("No metrics found - task may not be running");
}
```

### Issue: Memory Leaks
```java
// Always clean up in @PreDestroy
@PreDestroy
public void cleanup() {
    // Cancel all scheduled tasks
    uiUpdateManager.cancelScheduledUpdate("my-task");
    
    // Remove all component labels
    labelManager.removeComponentLabels(this);
    
    // Log final metrics
    log.info("Cleanup complete - {} labels removed", labelManager.getLabelCount());
}
```

## Summary

The refactored UI architecture provides:

1. **Centralized Label Management** - No more duplicate labels
2. **Thread-Safe Updates** - All UI updates properly synchronized
3. **Performance Visibility** - Built-in metrics for monitoring
4. **Clean Architecture** - Spring DI, proper lifecycle management
5. **Easy Migration** - Factory methods and compatibility layers

Use the `UiComponentFactory` to create components and follow the patterns demonstrated in this guide for a maintainable, performant UI.