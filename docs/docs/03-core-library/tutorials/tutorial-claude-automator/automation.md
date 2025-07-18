# Continuous Monitoring Automation

## Overview

The automation continuously monitors Claude's interface, detecting when the AI has finished responding and automatically reopening the Working state to continue the conversation.

## ClaudeMonitoringAutomation.java

```java
package com.claude.automator.automation;

import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateMemory;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeMonitoringAutomation {
    private final StateService stateService;
    private final StateMemory stateMemory;
    private final StateNavigator stateNavigator;
    private final Action action;
    private final WorkingState workingState;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean running = false;
    
    @PostConstruct
    public void startMonitoring() {
        log.info("Starting Claude monitoring automation");
        running = true;
        
        // Check every 2 seconds
        scheduler.scheduleWithFixedDelay(this::checkClaudeIconStatus, 
                5, 2, TimeUnit.SECONDS);
    }
    
    @PreDestroy
    public void stopMonitoring() {
        log.info("Stopping Claude monitoring automation");
        running = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void checkClaudeIconStatus() {
        if (!running) return;
        
        try {
            // Check if Working state is active
            Long workingStateId = workingState.getState().getId();
            if (!stateMemory.getActiveStates().contains(workingStateId)) {
                log.debug("Working state is not active, skipping check");
                return;
            }
            
            // Quick find to check if icon is still visible
            PatternFindOptions quickFind = new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(0.5)
                    .build();
            
            boolean iconFound = action.perform(quickFind, workingState.getClaudeIcon()).isSuccess();
            
            if (!iconFound) {
                log.info("Claude icon disappeared - removing Working state and reopening");
                
                // Remove Working from active states
                stateMemory.removeInactiveState(workingStateId);
                
                // Reopen Working state using enhanced StateNavigator
                boolean success = stateNavigator.openState(WorkingState.Name.WORKING);
                
                if (success) {
                    log.info("Successfully reopened Working state");
                } else {
                    log.error("Failed to reopen Working state");
                }
            } else {
                log.debug("Claude icon still visible");
            }
        } catch (Exception e) {
            log.error("Error during Claude icon monitoring", e);
        }
    }
}
```

## Key Concepts

### 1. Scheduled Monitoring

```java
scheduler.scheduleWithFixedDelay(
    this::checkClaudeIconStatus,  // Method to run
    5,                            // Initial delay (seconds)
    2,                            // Period between runs (seconds)
    TimeUnit.SECONDS
);
```

### 2. State Management

```java
// Check if state is active
if (!stateMemory.getActiveStates().contains(stateId)) {
    // State is not active
}

// Remove state from active states
stateMemory.removeInactiveState(stateId);
```

### 3. Enhanced Navigation

```java
// Use StateEnum for type-safe navigation
boolean success = stateNavigator.openState(WorkingState.Name.WORKING);
```

## Alternative Implementation Patterns

### Event-Driven Approach

```java
@Component
public class EventDrivenAutomation {
    @EventListener
    public void onStateChange(StateChangeEvent event) {
        if (event.getState().equals(WorkingState.Name.WORKING)) {
            // React to state changes
        }
    }
}
```

### Reactive Approach

```java
@Component
public class ReactiveAutomation {
    public Flux<Boolean> monitorClaudeIcon() {
        return Flux.interval(Duration.ofSeconds(2))
            .map(tick -> checkIconStatus())
            .filter(iconMissing -> iconMissing)
            .doOnNext(missing -> reopenWorkingState());
    }
}
```

## Best Practices

1. **Graceful Shutdown**: Always implement proper cleanup
2. **Thread Safety**: Use `volatile` for shared state
3. **Error Handling**: Never let exceptions kill the monitoring thread
4. **Logging**: Use appropriate log levels (debug for frequent checks)
5. **Performance**: Adjust check frequency based on requirements

## Monitoring Strategies

### Quick Check (Current Implementation)
- Fast icon detection
- Minimal resource usage
- Good for responsive UIs

### Deep Check Alternative
```java
// More thorough validation
ActionResult result = action.perform(
    new PatternFindOptions.Builder()
        .setSearchRegion(SearchRegions.DESKTOP)
        .setSimilarity(0.95)
        .build(),
    workingState.getClaudeIcon()
);
```

## State-Aware Scheduling

### Enhanced Monitoring with State Validation

The framework now supports state-aware scheduling, which automatically validates and manages active states at the beginning of each scheduled cycle.

### StateAwareScheduler Component

```java
@Component
@RequiredArgsConstructor
public class StateAwareScheduler {
    private final StateDetector stateDetector;
    private final StateMemory stateMemory;
    private final StateService stateService;
    
    public void scheduleWithStateCheck(
            ScheduledExecutorService scheduler,
            Runnable task,
            StateCheckConfiguration config,
            long initialDelay,
            long period,
            TimeUnit unit) {
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                performStateCheck(config);
                task.run();
            } catch (Exception e) {
                log.error("Error in state-aware scheduled task", e);
            }
        }, initialDelay, period, unit);
    }
}
```

### Configuration Options

```java
StateCheckConfiguration config = new StateCheckConfiguration.Builder()
    .withRequiredStates(List.of("Prompt", "Working"))  // States that must be active
    .withRebuildOnMismatch(true)                       // Auto-rebuild if states missing
    .withSkipIfStatesMissing(false)                    // Continue even if states missing
    .build();
```

### Enhanced Claude Automation Example

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeMonitoringAutomationV2 {
    private final StateAwareScheduler stateAwareScheduler;
    // ... other dependencies ...
    
    @PostConstruct
    public void startMonitoring() {
        // Configure state validation
        StateCheckConfiguration stateConfig = new StateCheckConfiguration.Builder()
                .withRequiredStates(List.of("Prompt", "Working"))
                .withRebuildOnMismatch(true)
                .build();
        
        // Schedule with automatic state checking
        stateAwareScheduler.scheduleWithStateCheck(
                scheduler,
                this::getClaudeWorking,
                stateConfig,
                initialDelay,
                checkInterval,
                TimeUnit.SECONDS
        );
    }
    
    private void getClaudeWorking() {
        // Task runs after state validation
        // States are guaranteed to be checked/rebuilt
        if (selectClaudePrompt()) {
            checkClaudeIconStatus();
        }
    }
}
```

### Benefits of State-Aware Scheduling

1. **Automatic State Validation**: Ensures required states are active before task execution
2. **Self-Healing**: Can automatically rebuild states if they're missing
3. **Separation of Concerns**: State management logic is isolated from business logic
4. **Configurable Behavior**: Flexible options for different scenarios
5. **Error Recovery**: Handles state mismatches gracefully

### Configuration Properties

```properties
# State-Aware Scheduling Configuration
claude.automator.monitoring.required-states=Prompt,Working
claude.automator.monitoring.rebuild-on-mismatch=true
claude.automator.monitoring.initial-delay=5
claude.automator.monitoring.check-interval=2
```

### Use Cases

1. **GUI State Validation**: Ensure expected GUI states before automation
2. **Recovery from Crashes**: Automatically detect and recover lost states
3. **Complex Workflows**: Maintain state integrity in multi-step processes
4. **Background Monitoring**: Keep automation aligned with application state

## Next Steps

Finally, we'll wire everything together with Spring configuration to create a complete working application.