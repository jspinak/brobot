---
sidebar_position: 10
title: 'State-Aware Scheduling'
---

# State-Aware Scheduling

## Overview

State-aware scheduling extends Brobot's scheduling capabilities to automatically validate and manage active states before executing scheduled tasks. This ensures that automation runs with the correct GUI context and can recover from unexpected state changes.

## Core Concepts

### The Challenge

Traditional scheduled tasks in GUI automation face several challenges:
- GUI state may change unexpectedly between task executions
- Applications may crash or navigate to unexpected screens
- Background processes may alter the interface
- User interactions may interfere with automation

### The Solution

State-aware scheduling addresses these challenges by:
1. Validating required states before each task execution
2. Automatically rebuilding states when necessary
3. Providing configurable behavior for different scenarios
4. Maintaining separation between scheduling and state management concerns

## Implementation

### How StateAwareScheduler Works

The `StateAwareScheduler` automatically performs state validation before each task execution. When you schedule a task with state checking, the scheduler internally:

1. Validates that all required states are active based on your configuration
2. Optionally rebuilds states if they're missing (when `rebuildOnMismatch` is true)
3. Executes your task only after successful state validation
4. Handles errors based on your `skipIfStatesMissing` setting

### Using StateAwareScheduler

Here's how to use the `StateAwareScheduler` in your own service:

```java
@Component
@RequiredArgsConstructor
public class MyAutomationService {
    private final StateAwareScheduler stateAwareScheduler;
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);

    public void startAutomation() {
        // Configure what states must be active
        StateAwareScheduler.StateCheckConfiguration config =
            new StateAwareScheduler.StateCheckConfiguration.Builder()
                .withRequiredStates(List.of("Dashboard", "LoggedIn"))
                .withRebuildOnMismatch(true)
                .build();

        // Schedule your task with automatic state checking
        ScheduledFuture<?> future = stateAwareScheduler.scheduleWithStateCheck(
            scheduler,
            this::myAutomationTask,  // Your task
            config,                   // State requirements
            0,                       // Initial delay
            10,                      // Period
            TimeUnit.SECONDS
        );
    }

    private void myAutomationTask() {
        // This runs AFTER states are validated
        // Dashboard and LoggedIn states are guaranteed to be checked
        log.info("Running automation with verified states");
    }
}
```

The state validation happens automatically before your task runs. Depending on your `checkMode` configuration:
- **CHECK_ALL**: Validates all required states every time
- **CHECK_INACTIVE_ONLY**: Only validates states that are currently inactive (more efficient)

### Configuration Options

Configure state checking behavior with the builder pattern:

```java
StateAwareScheduler.StateCheckConfiguration config = new StateAwareScheduler.StateCheckConfiguration.Builder()
    .withRequiredStates(List.of("MainMenu", "Dashboard"))
    .withRebuildOnMismatch(true)
    .withSkipIfStatesMissing(false)
    .withCheckMode(StateAwareScheduler.StateCheckConfiguration.CheckMode.CHECK_INACTIVE_ONLY)
    .withMaxIterations(100)  // Optional: limit iterations
    .build();
```

#### Configuration Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `requiredStates` | `List<String>` | Empty | States that must be active before task execution |
| `rebuildOnMismatch` | boolean | true | Whether to rebuild states if requirements not met |
| `skipIfStatesMissing` | boolean | false | Skip task execution if states cannot be validated |
| `checkMode` | CheckMode | CHECK_INACTIVE_ONLY | CHECK_ALL checks all states, CHECK_INACTIVE_ONLY only checks inactive ones |
| `maxIterations` | int | -1 (unlimited) | Maximum number of task executions before stopping |

#### Check Modes

- **CHECK_ALL**: Validates all required states every time, regardless of current active status
- **CHECK_INACTIVE_ONLY**: Only validates states that are currently inactive (more efficient)

## Usage Examples

### Basic Monitoring Task

```java
@Service
public class ApplicationMonitor {
    private final StateAwareScheduler stateAwareScheduler;
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(1);
    
    @PostConstruct
    public void startMonitoring() {
        StateAwareScheduler.StateCheckConfiguration config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                .withRequiredStates(List.of("Application"))
                .withRebuildOnMismatch(true)
                .build();
        
        stateAwareScheduler.scheduleWithStateCheck(
                scheduler,
                this::checkApplicationHealth,
                config,
                5, // initial delay
                10, // period
                TimeUnit.SECONDS
        );
    }
    
    private void checkApplicationHealth() {
        // This runs only after automatic state validation
        // The scheduler has already verified "Application" state is active
        // No need to manually check states here
    }
}
```

### Complex Workflow Automation

```java
@Component
public class DataSyncAutomation {
    
    public void setupPeriodicSync() {
        // Different configurations for different stages
        StateAwareScheduler.StateCheckConfiguration loginConfig = new StateAwareScheduler.StateCheckConfiguration.Builder()
                .withRequiredStates(List.of("LoginScreen"))
                .withRebuildOnMismatch(true)
                .withSkipIfStatesMissing(true) // Skip if can't find login
                .build();

        StateAwareScheduler.StateCheckConfiguration dataConfig = new StateAwareScheduler.StateCheckConfiguration.Builder()
                .withRequiredStates(List.of("DataDashboard", "SyncPanel"))
                .withRebuildOnMismatch(false) // Don't rebuild, just skip
                .withSkipIfStatesMissing(true)
                .build();
        
        // Chain multiple state-aware tasks
        CompletableFuture.runAsync(() -> {
            performStateAwareTask(loginConfig, this::performLogin);
        }).thenRun(() -> {
            performStateAwareTask(dataConfig, this::syncData);
        });
    }
}
```

### Error Recovery Automation

```java
@Service
public class ErrorRecoveryService {
    
    public void setupErrorMonitoring() {
        StateAwareScheduler.StateCheckConfiguration config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                .withRequiredStates(List.of("NormalOperation"))
                .withRebuildOnMismatch(true)
                .build();
        
        // Check every 30 seconds for error states
        stateAwareScheduler.scheduleWithStateCheck(
                scheduler,
                this::checkAndRecoverFromErrors,
                config,
                0, 30, TimeUnit.SECONDS
        );
    }
    
    private void checkAndRecoverFromErrors() {
        // If we're here, normal operation state was validated
        // Now check for any error dialogs
        if (stateDetector.findState("ErrorDialog").isPresent()) {
            handleErrorDialog();
            // State will be rebuilt on next cycle
        }
    }
}
```

## Integration with MonitoringService

State-aware scheduling complements the existing `MonitoringService`:

```java
@Component
public class EnhancedMonitoring {
    private final MonitoringService monitoringService;
    private final StateAwareScheduler stateAwareScheduler;
    
    public void startStateAwareMonitoring() {
        // Create state validation hook
        Runnable stateCheck = stateAwareScheduler.createStateCheckHook(
            new StateAwareScheduler.StateCheckConfiguration.Builder()
                .withRequiredStates(List.of("Application"))
                .build()
        );
        
        // Combine with monitoring service
        monitoringService.startContinuousTask(
            () -> {
                stateCheck.run(); // Validate states first
                performMonitoringTask();
            },
            () -> isApplicationRunning(),
            5 // delay seconds
        );
    }
}
```

## Best Practices

### 1. Choose Appropriate Required States

```java
// Too specific - might fail unnecessarily
.withRequiredStates(List.of("MainMenu", "SubMenu", "SpecificDialog"))

// Better - focus on essential states
.withRequiredStates(List.of("Application", "MainWorkflow"))
```

### 2. Configure Rebuild Behavior Carefully

```java
// For critical workflows - always rebuild
.withRebuildOnMismatch(true)
.withSkipIfStatesMissing(false)

// For optional tasks - skip if states missing
.withRebuildOnMismatch(false)
.withSkipIfStatesMissing(true)
```

### 3. Handle State Check Failures

```java
public void scheduleWithFallback() {
    try {
        stateAwareScheduler.scheduleWithStateCheck(
            scheduler, mainTask, mainConfig, 0, 10, TimeUnit.SECONDS
        );
    } catch (IllegalStateException e) {
        // Fallback to simpler state requirements
        stateAwareScheduler.scheduleWithStateCheck(
            scheduler, fallbackTask, fallbackConfig, 0, 10, TimeUnit.SECONDS
        );
    }
}
```

### 4. Use Property Configuration

```properties
# application.properties
app.monitoring.required-states=Dashboard,Navigation
app.monitoring.rebuild-on-mismatch=true
app.monitoring.check-interval=5
```

```java
@Value("${app.monitoring.required-states}")
private List<String> requiredStates;

@Value("${app.monitoring.rebuild-on-mismatch}")
private boolean rebuildOnMismatch;
```

## Performance Considerations

1. **State Detection Overhead**: State checking adds overhead to each task execution
2. **Rebuild Cost**: Full state rebuilding can be expensive - use judiciously
3. **Frequency Balance**: Balance checking frequency with performance impact

```java
// High-frequency task with minimal state checking
StateAwareScheduler.StateCheckConfiguration lightConfig = new StateAwareScheduler.StateCheckConfiguration.Builder()
    .withRequiredStates(List.of("MainApp")) // Single state check
    .withRebuildOnMismatch(false)          // No rebuild
    .build();

// Low-frequency task with thorough checking
StateAwareScheduler.StateCheckConfiguration thoroughConfig = new StateAwareScheduler.StateCheckConfiguration.Builder()
    .withRequiredStates(List.of("App", "Module", "Feature"))
    .withRebuildOnMismatch(true)
    .build();
```

## Troubleshooting

### Common Issues

1. **States Not Found**: Ensure state names match exactly
2. **Excessive Rebuilding**: Consider reducing rebuild frequency
3. **Task Skipping**: Check logs for state validation failures

### Debug Logging

Enable debug logging to troubleshoot state checking:

```properties
logging.level.com.yourapp.scheduling=DEBUG
logging.level.io.github.jspinak.brobot.statemanagement=DEBUG
```

## Future Enhancements

Potential improvements to state-aware scheduling:

1. **Machine Learning Integration**: Predict state changes and pre-validate
2. **State Change Events**: React to state changes rather than polling
3. **Hierarchical State Checking**: Check parent states before children
4. **Performance Optimization**: Cache state detection results
5. **Advanced Recovery**: Custom recovery strategies per state

## Summary

State-aware scheduling provides a robust foundation for reliable GUI automation by:
- Ensuring correct state context before task execution
- Automatically recovering from state mismatches
- Maintaining clean separation of concerns
- Providing flexible configuration options

This pattern is especially valuable for long-running automation, background monitoring, and complex workflows where GUI state integrity is critical.