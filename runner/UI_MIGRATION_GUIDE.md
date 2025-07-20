# UI Architecture Migration Guide

## Overview

This guide documents the migration from the legacy singleton-based UI architecture to the new Spring DI-based architecture in the Brobot Runner module. The new architecture solves label duplication issues, improves testability, and provides better lifecycle management.

## Key Changes

### 1. Singleton Pattern Removal

**Old Approach:**
```java
// Singleton anti-pattern
public class AutomationPanel {
    private static AutomationPanel INSTANCE;
    
    public static Optional<AutomationPanel> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }
}
```

**New Approach:**
```java
// Spring-managed component
@Component
public class UnifiedAutomationPanel extends BasePanel {
    @Autowired private UIComponentRegistry componentRegistry;
    @Autowired private UIUpdateManager updateManager;
    @Autowired private LabelManager labelManager;
}
```

### 2. Label Duplication Prevention

**Old Problem:**
- Labels were created multiple times on refresh
- No centralized tracking of UI components
- Memory leaks from orphaned labels

**New Solution:**
- `LabelManager` ensures single label instances
- `UIComponentRegistry` tracks all components with WeakReferences
- Automatic cleanup of unused components

### 3. Component Architecture

#### New Base Classes

1. **UIComponent Interface**
   ```java
   public interface UIComponent {
       void initialize();
       void refresh();
       void cleanup();
       boolean isValid();
       String getComponentId();
   }
   ```

2. **BasePanel Abstract Class**
   - Implements UIComponent lifecycle
   - Automatic registration/unregistration
   - Built-in update scheduling

#### Core Managers

1. **UIComponentRegistry**
   - Central registry for all UI components
   - WeakReference-based to prevent memory leaks
   - Component lookup and lifecycle management

2. **LabelManager**
   - Singleton label instances
   - Thread-safe label updates
   - Automatic style class management

3. **UIUpdateManager**
   - Centralized update scheduling
   - Single thread pool for all UI updates
   - Performance monitoring

4. **BrobotThemeManager**
   - Extends AtlantaFX theme manager
   - Custom CSS overlays
   - System dark mode detection

## Migration Steps

### Step 1: Replace Deprecated Panels

**Old:**
```java
@Autowired private AutomationPanel automationPanel;
// or
@Autowired private EnhancedAutomationPanel enhancedPanel;
```

**New:**
```java
@Autowired private UnifiedAutomationPanel unifiedPanel;
// or
@Autowired private RefactoredAutomationPanel refactoredPanel;
```

### Step 2: Update Event Handlers

**Old (UIEventHandler with singletons):**
```java
AutomationPanel.getInstance().ifPresent(panel -> {
    panel.setStatusMessage(statusEvent.getMessage());
});
```

**New (RefactoredUIEventHandler with events):**
```java
notifyUIComponents("execution.started", Map.of(
    "message", statusEvent.getMessage(),
    "progress", statusEvent.getProgress()
));
```

### Step 3: Migrate Custom Panels

1. Extend `BasePanel` instead of VBox directly
2. Implement lifecycle methods (doInitialize, doRefresh, doCleanup)
3. Use managers for label creation and updates

**Example:**
```java
@Component
public class MyCustomPanel extends BasePanel {
    
    @Override
    protected void doInitialize() {
        // Create UI using managers
        Label title = labelManager.getOrCreateLabel("my_title", "Title");
        getChildren().add(title);
        
        // Schedule updates
        schedulePeriodicUpdate(this::updateStatus, 1);
    }
    
    @Override
    protected void doRefresh() {
        // Refresh logic
    }
    
    @Override
    protected void doCleanup() {
        // Cleanup logic
    }
}
```

### Step 4: Update Configuration

1. Remove any singleton initialization code
2. Let Spring manage component lifecycle
3. Use `@PostConstruct` for initialization

### Step 5: Apply Themes

```java
@Autowired private BrobotThemeManager themeManager;

// Apply theme
themeManager.setTheme(AtlantaTheme.PRIMER_DARK);

// Apply preset
themeManager.applyPreset(ThemePreset.HIGH_CONTRAST);
```

## Best Practices

### 1. Label Management

**Always use LabelManager:**
```java
// DON'T
Label label = new Label("Text");
getChildren().add(label);

// DO
Label label = labelManager.getOrCreateLabel("unique_id", "Text");
getChildren().add(label);
```

### 2. Component Registration

**Let BasePanel handle it:**
```java
// Automatic registration in BasePanel.initialize()
// No manual registration needed
```

### 3. Updates and Threading

**Use UIUpdateManager:**
```java
// DON'T
new Thread(() -> {
    Platform.runLater(() -> updateUI());
}).start();

// DO
updateManager.scheduleUpdate("task_id", this::updateUI, 0, 1, TimeUnit.SECONDS);
```

### 4. Event-Driven Updates

**Use events instead of direct calls:**
```java
// DON'T
otherPanel.updateStatus("New Status");

// DO
eventBus.publish(new UIUpdateEvent(this, "status.changed", "New Status"));
```

## Debugging

### 1. Enable Debug Mode

```java
@Autowired private UIDebugger debugger;

// Show debug window
debugger.showDebugWindow();
```

### 2. Monitor Performance

```java
@Autowired private UIPerformanceMonitor monitor;

// Get metrics
PerformanceMetrics metrics = monitor.getMetrics();
```

### 3. Check Component Registry

```java
Map<String, String> components = componentRegistry.getComponentSummary();
log.info("Registered components: {}", components);
```

## Common Issues and Solutions

### Issue 1: Labels Still Duplicating

**Cause:** Not using unique IDs for labels
**Solution:** Use consistent, unique IDs based on label purpose

### Issue 2: Memory Leaks

**Cause:** Not calling cleanup() on panels
**Solution:** Extend BasePanel which handles cleanup automatically

### Issue 3: Updates Not Working

**Cause:** Component not registered or not valid
**Solution:** Check isValid() and ensure proper initialization

### Issue 4: Theme Not Applied

**Cause:** CSS files not in resources
**Solution:** Ensure `/css/custom/` directory exists with CSS files

## Testing

### Unit Testing New Components

```java
@ExtendWith(ApplicationExtension.class)
class MyPanelTest {
    @Test
    void testNoDuplicateLabels() {
        MyPanel panel = new MyPanel();
        panel.initialize();
        panel.refresh();
        panel.refresh();
        
        // Should still have only one label
        assertEquals(1, panel.getChildren().size());
    }
}
```

## Rollback Plan

If issues arise:

1. Deprecated classes are still available
2. Can temporarily use old panels with `@SuppressWarnings("deprecation")`
3. Gradual migration panel by panel
4. Both architectures can coexist during transition

## Timeline

1. **Phase 1** (Complete): Core infrastructure
2. **Phase 2** (In Progress): Panel migration
3. **Phase 3**: Remove deprecated code
4. **Phase 4**: Performance optimization

## Support

For questions or issues:
1. Check the debug output
2. Review test cases for examples
3. Use UIDebugger for runtime inspection
4. File issues with specific error messages