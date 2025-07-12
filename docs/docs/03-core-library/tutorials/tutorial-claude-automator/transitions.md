# Implementing Transitions with Annotations

## Overview

Transitions define how to navigate between states. With the new annotation system, transitions are dramatically simplified - just use `@Transition` and let the framework handle registration automatically.

## Prompt to Working Transition

### PromptToWorkingTransition.java

```java
package com.claude.automator.transitions;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.annotations.Transition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptToWorkingTransition {

    private final PromptState promptState;
    private final Action action;

    public boolean execute() {
        try {
            log.info("Executing transition from Prompt to Working state");
            
            // Using the fluent API to chain actions: find -> click -> type
            PatternFindOptions findClickType = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(0.5) // Pause before clicking
                    .then(new ClickOptions.Builder()
                            .setPauseAfterEnd(0.5) // Pause before typing
                            .build())
                    .then(new TypeOptions.Builder()
                            .build())
                    .build();
            
            // Create target objects for the chained action
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt()) // For find & click
                    .withStrings(promptState.getContinueCommand()) // For type (continue with Enter)
                    .build();
            
            // Execute the chained action
            ActionResult result = action.perform(findClickType, target);
            
            if (result.isSuccess()) {
                log.info("Successfully executed transition from Prompt to Working");
                return true;
            } else {
                log.warn("Failed to execute transition: {}", result.getActionDescription());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error during Prompt to Working transition", e);
            return false;
        }
    }
}
```

### Key Features:

1. **@Transition Annotation**: Declares source and target states declaratively
2. **Automatic Registration**: No need for manual StateTransitions setup
3. **Action Chaining**: Uses `then()` to chain find → click → type
4. **State Component Usage**: Uses both StateImage and StateString from PromptState
5. **Simple Execute Method**: Just return true/false for success/failure
6. **Clean Structure**: Focus on transition logic, not framework setup

## Comparison: Before and After

### Before (Manual Registration):
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptTransitions {
    // Lots of setup code...
    
    public StateTransitions getStateTransitions() {
        return new StateTransitions.Builder(PromptState.Name.PROMPT.toString())
                .addTransition(getPromptToWorkingTransition())
                .addTransitionFinish(() -> findPromptImage())
                .build();
    }
    
    private JavaStateTransition getPromptToWorkingTransition() {
        return new JavaStateTransition.Builder()
                .setFunction(() -> executePromptToWorking())
                .addToActivate(WorkingState.Name.WORKING.toString())
                .setStaysVisibleAfterTransition(true)
                .build();
    }
    
    // More boilerplate...
}
```

### After (With Annotations):
```java
@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptToWorkingTransition {
    // Just implement execute() method
    public boolean execute() {
        // Your transition logic
    }
}
```

## Transition Annotation Options

### Basic Transition
```java
@Transition(from = StateA.class, to = StateB.class)
public class SimpleTransition {
    public boolean execute() {
        // Transition logic
        return true;
    }
}
```

### Multiple Source States
```java
@Transition(
    from = {ErrorState.class, TimeoutState.class},
    to = HomeState.class
)
public class RecoveryTransition {
    public boolean execute() {
        // Recovery logic
        return true;
    }
}
```

### Multiple Target States
```java
@Transition(
    from = ProcessingState.class,
    to = {SuccessState.class, ErrorState.class}
)
public class ProcessingTransition {
    public boolean execute() {
        if (processSuccessful()) {
            return true; // Goes to SuccessState
        }
        return false; // Goes to ErrorState
    }
}
```

### Custom Method Name
```java
@Transition(
    from = SearchState.class,
    to = ResultsState.class,
    method = "performSearch"
)
public class SearchTransition {
    public boolean performSearch() {
        // Search logic
        return true;
    }
}
```

### Transition Priority
```java
@Transition(
    from = MenuState.class,
    to = SettingsState.class,
    priority = 10  // Higher priority for path selection
)
public class MenuToSettingsTransition {
    public boolean execute() {
        // Navigation logic
        return true;
    }
}
```

## Action Chaining Pattern

The fluent API enables elegant action sequences:

```java
// Chain multiple actions in sequence
PatternFindOptions chainedAction = new PatternFindOptions.Builder()
    .setPauseAfterEnd(0.5)              // Wait after finding
    .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)       // Wait after clicking
            .build())
    .then(new TypeOptions.Builder()
            .build())                     // Type text
    .build();

// Execute all actions in sequence
ActionResult result = action.perform(chainedAction, target);
```

## Best Practices

1. **Use Required Annotations**:
   ```java
   @Transition(from = X.class, to = Y.class)
   @RequiredArgsConstructor  // For dependency injection
   @Slf4j                   // For logging
   ```

2. **Keep Transitions Focused**: One transition = one navigation path

3. **Handle Failures Gracefully**:
   ```java
   public boolean execute() {
       try {
           // Transition logic
           return action.click("button").isSuccess();
       } catch (Exception e) {
           log.error("Transition failed", e);
           return false;
       }
   }
   ```

4. **Log Appropriately**: Info for success, warn for expected failures, error for exceptions

5. **Use Dependency Injection**: Inject states and actions via constructor

## Benefits of Annotation Approach

1. **80% Less Code**: Eliminate StateTransitions builders and manual wiring
2. **Clearer Intent**: `@Transition(from = A.class, to = B.class)` is self-documenting
3. **Compile-Time Safety**: IDE immediately shows if states don't exist
4. **Better Organization**: Transitions are standalone classes, not nested methods
5. **Easier Testing**: Each transition can be unit tested independently

## Next Steps

With states and transitions defined using annotations, the entire state machine is automatically configured. No manual registration, no complex setup - just focus on your automation logic!