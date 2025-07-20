# Developer Quick Reference - Refactored UI Architecture

## Quick Start Checklist

### Creating a New UI Component
```java
@Component
@Slf4j
public class MyNewPanel extends VBox {
    @Autowired private LabelManager labelManager;
    @Autowired private UIUpdateManager uiUpdateManager;
    
    @PostConstruct
    public void initialize() {
        // Setup UI
        Label myLabel = labelManager.getOrCreateLabel(this, "myLabel", "Initial Text");
        getChildren().add(myLabel);
        
        // Schedule updates
        uiUpdateManager.schedulePeriodicUpdate("myUpdate", 
            () -> updateUI(), 0, 1, TimeUnit.SECONDS);
    }
    
    @PreDestroy
    public void cleanup() {
        uiUpdateManager.cancelScheduledUpdate("myUpdate");
        labelManager.removeComponentLabels(this);
    }
}
```

### Common Patterns

#### 1. Creating Managed Labels
```java
// DO THIS
Label statusLabel = labelManager.getOrCreateLabel(this, "status", "Ready");

// NOT THIS
Label statusLabel = new Label("Ready");
```

#### 2. Updating UI Safely
```java
// DO THIS
uiUpdateManager.executeUpdate("update-status", () -> {
    labelManager.updateLabel(this, "status", "Running");
});

// NOT THIS
Platform.runLater(() -> statusLabel.setText("Running"));
```

#### 3. Scheduling Periodic Updates
```java
// DO THIS
uiUpdateManager.schedulePeriodicUpdate("resource-monitor",
    this::updateResourceDisplay, 0, 1, TimeUnit.SECONDS);

// NOT THIS
ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
executor.scheduleAtFixedRate(...);
```

## Component Creation

### Using UiComponentFactory
```java
@Autowired
private UiComponentFactory factory;

// Create individual components
UnifiedAutomationPanel automationPanel = factory.createAutomationPanel();
RefactoredResourceMonitorPanel resourcePanel = factory.createRefactoredResourceMonitorPanel();

// Create all components at once
var panels = factory.createAllRefactoredPanels();
```

### Spring Integration
```java
@Configuration
public class UIConfiguration {
    @Bean
    public MyCustomPanel myCustomPanel() {
        return new MyCustomPanel();
    }
}
```

## Performance Monitoring

### Getting Metrics
```java
// In your component
public String getPerformanceReport() {
    UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("myUpdate");
    return String.format("Updates: %d, Avg: %.2f ms", 
        metrics.getTotalUpdates(), 
        metrics.getAverageDurationMs());
}
```

### Label Count Monitoring
```java
int totalLabels = labelManager.getLabelCount();
int myLabels = labelManager.getComponentLabelCount(this);
String summary = labelManager.getSummary();
```

## Testing Components

### Unit Testing
```java
@Test
void testPanelCreation() {
    LabelManager labelManager = new LabelManager();
    UIUpdateManager uiUpdateManager = new UIUpdateManager();
    uiUpdateManager.initialize();
    
    MyPanel panel = new MyPanel();
    TestHelper.injectField(panel, "labelManager", labelManager);
    TestHelper.injectField(panel, "updateManager", uiUpdateManager);
    
    panel.initialize();
    
    assertEquals(3, labelManager.getComponentLabelCount(panel));
}
```

### Using TestHelper
```java
// Create test ConfigEntry
ConfigEntry config = TestHelper.createTestConfigEntry();

// Create ExamplePanel with dependencies
ExampleLabelManagedPanel panel = TestHelper.createExamplePanel(labelManager, uiUpdateManager);
```

## Migration Guide

### Old Code
```java
public class OldPanel extends VBox {
    private static OldPanel instance;
    private Label statusLabel = new Label("Status");
    
    public static OldPanel getInstance() {
        if (instance == null) {
            instance = new OldPanel();
        }
        return instance;
    }
    
    public void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }
}
```

### New Code
```java
@Component
public class NewPanel extends VBox {
    @Autowired private LabelManager labelManager;
    @Autowired private UIUpdateManager uiUpdateManager;
    
    @PostConstruct
    public void initialize() {
        Label statusLabel = labelManager.getOrCreateLabel(this, "status", "Status");
        getChildren().add(statusLabel);
    }
    
    public void updateStatus(String status) {
        labelManager.updateLabel(this, "status", status);
    }
}
```

## Common Issues and Solutions

### Issue: Label not updating
```java
// Check label exists
boolean exists = labelManager.hasLabel(this, "myLabel");

// Force update on JavaFX thread
uiUpdateManager.executeUpdate("force-update", () -> {
    labelManager.updateLabel(this, "myLabel", "New Value");
});
```

### Issue: Memory leak
```java
// Always cleanup in @PreDestroy
@PreDestroy
public void cleanup() {
    // Cancel all scheduled tasks
    uiUpdateManager.cancelScheduledUpdate("task1");
    uiUpdateManager.cancelScheduledUpdate("task2");
    
    // Remove all labels
    labelManager.removeComponentLabels(this);
}
```

### Issue: Performance degradation
```java
// Use queued updates for high-frequency changes
uiUpdateManager.queueUpdate("high-freq", () -> {
    // This will be batched with other updates
    updateMultipleLabels();
});

// Check metrics
UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("high-freq");
if (metrics.getMaxDurationMs() > 16) { // More than one frame
    log.warn("Update taking too long: {} ms", metrics.getMaxDurationMs());
}
```

## Best Practices

1. **Always use @PostConstruct/@PreDestroy** for initialization and cleanup
2. **Never create labels directly** - always use LabelManager
3. **Never use Platform.runLater()** - always use UIUpdateManager
4. **Use meaningful task IDs** for updates (helps with debugging)
5. **Monitor performance metrics** in development
6. **Test with TestHelper utilities** for consistent test data
7. **Follow naming conventions**:
   - Label IDs: `camelCase` (e.g., "statusLabel", "progressBar")
   - Task IDs: `kebab-case` (e.g., "update-status", "refresh-data")
   - Component names: `PascalCase` with "Panel" suffix

## Useful Commands

```bash
# Compile only main code (skip tests)
./gradlew :runner:classes

# Run with performance logging
java -Dlogging.level.io.github.jspinak.brobot.runner.ui.management=DEBUG -jar runner.jar

# Check for deprecated usage
./gradlew :runner:compileJava -Xlint:deprecation
```

## Resources

- **Full Integration Guide**: `docs/FINAL_INTEGRATION_GUIDE.md`
- **Refactoring Summary**: `docs/REFACTORING_SUMMARY.md`
- **API Documentation**: Run `./gradlew :runner:javadoc`
- **Example Code**: See `ExampleLabelManagedPanel.java`