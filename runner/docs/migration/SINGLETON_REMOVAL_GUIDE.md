# Singleton Removal Migration Guide

## Overview

This guide helps you migrate UI components from singleton pattern to proper Spring dependency injection.

## Current Singleton Components

1. **AutomationPanel** → Use `UnifiedAutomationPanel`
2. **EnhancedAutomationPanel** → Use `UnifiedAutomationPanel`
3. **RefactoredBasicAutomationPanel** → Remove or migrate to `UnifiedAutomationPanel`
4. **RefactoredEnhancedAutomationPanel** → Remove or migrate to `UnifiedAutomationPanel`

## Migration Steps

### Step 1: Replace Singleton Access

**Before:**
```java
AutomationPanel.getInstance().ifPresent(panel -> {
    panel.setStatusMessage("Status update");
    panel.log("Log message");
});
```

**After:**
```java
@Autowired
private UIComponentRegistry componentRegistry;

// Get the panel from registry
UnifiedAutomationPanel panel = componentRegistry.getComponent("automationPanel", UnifiedAutomationPanel.class);
if (panel != null) {
    panel.setStatusMessage("Status update");
    panel.log("Log message");
}
```

### Step 2: Update UIEventHandler References

**Before:**
```java
// In UIEventHandler
AutomationPanel.getInstance().ifPresent(panel -> {
    panel.updateSomething();
});
```

**After:**
```java
// Use RefactoredUIEventHandler which is already implemented
// It uses UIComponentRegistry instead of singletons
```

### Step 3: Update Component Creation

**Before:**
```java
// Singleton creation
private static AutomationPanel INSTANCE;

public static AutomationPanel getInstance() {
    return INSTANCE;
}
```

**After:**
```java
// Spring managed component
@Component
public class UnifiedAutomationPanel {
    // No static instance
    // Injected via Spring DI
}

// Creation via factory
@Autowired
private UiComponentFactory factory;

UnifiedAutomationPanel panel = factory.createAutomationPanel();
```

### Step 4: Use LabelManager for Label Management

**Before:**
```java
// Direct label creation causing duplication
Label statusLabel = new Label("Status");
updateContainer.getChildren().add(statusLabel);
```

**After:**
```java
@Autowired
private LabelManager labelManager;

// Managed label creation
Label statusLabel = labelManager.getOrCreateLabel(this, "statusLabel", "Status");
updateContainer.getChildren().add(statusLabel);

// Updates
labelManager.updateLabel(this, "statusLabel", "New Status");
```

### Step 5: Use UIUpdateManager for Updates

**Before:**
```java
// Direct Platform.runLater calls
Platform.runLater(() -> {
    statusLabel.setText("Updated");
});

// Manual thread management
ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
executor.scheduleAtFixedRate(() -> {
    Platform.runLater(() -> updateUI());
}, 0, 1, TimeUnit.SECONDS);
```

**After:**
```java
@Autowired
private UIUpdateManager updateManager;

// Immediate update
updateManager.executeUpdate("statusUpdate", () -> {
    statusLabel.setText("Updated");
});

// Scheduled updates
updateManager.schedulePeriodicUpdate(
    "periodicUpdate",
    this::updateUI,
    0, 1, TimeUnit.SECONDS
);
```

## Component Lifecycle Management

### Initialization
```java
@PostConstruct
public void initialize() {
    // Setup UI
    // Register with managers
    // Start scheduled tasks
}
```

### Cleanup
```java
@PreDestroy
public void cleanup() {
    // Cancel scheduled tasks
    updateManager.cancelScheduledUpdate("taskId");
    
    // Remove managed labels
    labelManager.removeComponentLabels(this);
    
    // Unregister from registry if needed
}
```

## Benefits of Migration

1. **Better Testing**: Components can be easily mocked
2. **No Memory Leaks**: Proper lifecycle management
3. **Thread Safety**: Centralized update management
4. **Performance Tracking**: Built-in metrics
5. **Cleaner Code**: No static references

## Example: Complete Migration

See `ExampleLabelManagedPanel.java` for a complete example of:
- Spring dependency injection
- LabelManager usage
- UIUpdateManager usage
- Proper lifecycle management
- Performance metrics

## Verification Steps

1. **Check for Static References**:
   ```bash
   grep -r "getInstance()" src/
   grep -r "private static.*INSTANCE" src/
   ```

2. **Verify Spring Wiring**:
   - All components have `@Component` or `@Service`
   - Dependencies use `@Autowired`
   - No manual instantiation of managed components

3. **Test Lifecycle**:
   - Components initialize properly
   - Cleanup happens on shutdown
   - No resource leaks

## Common Pitfalls

1. **Forgetting Cleanup**: Always implement `@PreDestroy`
2. **Direct Label Creation**: Use LabelManager
3. **Manual Threading**: Use UIUpdateManager
4. **Static References**: Remove all static instance variables

## Real-World Migration Examples

### ResourceMonitorPanel Migration

The ResourceMonitorPanel migration demonstrates best practices for using both LabelManager and UIUpdateManager:

#### Before (Original Implementation)
```java
public class ResourceMonitorPanel extends BrobotPanel {
    private Label totalResourcesLabel;
    private Label imageResourcesLabel;
    // Direct label creation and management
    
    @PostConstruct
    public void postConstruct() {
        monitoringService.startMonitoring(this::updateUI);
    }
    
    private void updateUI(ResourceData data) {
        Platform.runLater(() -> {
            totalResourcesLabel.setText(String.valueOf(data.getTotalResources()));
            // Direct label updates
        });
    }
}
```

#### After (Refactored Implementation)
```java
@Component
public class RefactoredResourceMonitorPanel extends BrobotPanel {
    private static final String UPDATE_TASK_ID = "resource-monitor-update";
    
    @Autowired
    private LabelManager labelManager;
    
    @Autowired
    private UIUpdateManager uiUpdateManager;
    
    @PostConstruct
    public void postConstruct() {
        // Use UIUpdateManager for scheduled updates
        uiUpdateManager.schedulePeriodicUpdate(
            UPDATE_TASK_ID,
            this::performScheduledUpdate,
            0, 1000, TimeUnit.MILLISECONDS
        );
    }
    
    @PreDestroy
    public void preDestroy() {
        // Clean up on shutdown
        uiUpdateManager.cancelScheduledUpdate(UPDATE_TASK_ID);
        labelManager.removeComponentLabels(this);
    }
    
    private void setupContent() {
        // Use LabelManager for label creation
        Label totalLabel = labelManager.getOrCreateLabel(
            this, "resource-status-total", "0"
        );
    }
    
    private void updateUI(ResourceData data) {
        // Use LabelManager for updates
        labelManager.updateLabel(
            this, "resource-status-total",
            String.valueOf(data.getTotalResources())
        );
    }
}
```

#### Key Benefits Demonstrated
1. **No Label Duplication**: LabelManager ensures single instance per ID
2. **Centralized Updates**: UIUpdateManager handles all scheduling
3. **Performance Tracking**: Built-in metrics for monitoring
4. **Proper Cleanup**: Lifecycle methods handle resource disposal
5. **Thread Safety**: Automatic Platform.runLater() handling

## Next Steps

1. Remove deprecated singleton classes
2. Update all references to use new components
3. Add tests for migrated components
4. Monitor performance metrics