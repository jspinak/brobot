---
sidebar_position: 5
---

# Transitions

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0 with the new annotation system.
:::
 
Transitions define how to navigate between states. With the new annotation system, transitions are now separate classes with the `@Transition` annotation.

Here are the transition classes:

## Homepage to Harmony Transition

```java
@Transition(from = Homepage.class, to = Harmony.class)
@RequiredArgsConstructor
@Slf4j
public class HomepageToHarmonyTransition {
    
    private final Action action;
    private final Homepage homepage;
    
    public boolean execute() {
        log.info("Transitioning from Homepage to Harmony");
        return action.perform(CLICK, homepage.getHarmony()).isSuccess();
    }
}
```

## Harmony to About Transition

```java
@Transition(from = Harmony.class, to = About.class)
@RequiredArgsConstructor
@Slf4j
public class HarmonyToAboutTransition {
    
    private final Action action;
    private final Harmony harmony;
    
    public boolean execute() {
        log.info("Transitioning from Harmony to About");
        return action.perform(CLICK, harmony.getAbout()).isSuccess();
    }
}
```

## Key Improvements with Annotations

### Before (Manual Registration)
```java
@Component
public class HomepageTransitions {
    
    public HomepageTransitions(StateTransitionsRepository repository,
                               Action action, Homepage homepage) {
        StateTransitions transitions = new StateTransitions.Builder("homepage")
                .addTransitionFinish(this::finishTransition)
                .addTransition(new StateTransition.Builder()
                        .addToActivate("harmony")
                        .setFunction(this::gotoHarmony)
                        .build())
                .build();
        repository.add(transitions);  // Manual registration
    }
    
    private boolean finishTransition() { /* ... */ }
    private boolean gotoHarmony() { /* ... */ }
}
```

### After (Automatic Registration)
```java
@Transition(from = Homepage.class, to = Harmony.class)
@RequiredArgsConstructor
@Slf4j
public class HomepageToHarmonyTransition {
    public boolean execute() {
        // Just implement the transition logic
        return action.perform(CLICK, homepage.getHarmony()).isSuccess();
    }
}
```

## Benefits

1. **Declarative**: `@Transition(from = X.class, to = Y.class)` clearly shows the navigation
2. **Less Code**: No StateTransitions.Builder or manual registration
3. **Single Responsibility**: Each transition is a focused class
4. **Automatic Wiring**: Framework handles all the connections
5. **Type Safety**: Compile-time checking of state relationships

## Transition Patterns

### Simple Click Transition
```java
@Transition(from = SourceState.class, to = TargetState.class)
public class SimpleTransition {
    public boolean execute() {
        return action.click(element).isSuccess();
    }
}
```

### Multiple Actions
```java
@Transition(from = FormState.class, to = ResultState.class)
public class FormSubmitTransition {
    public boolean execute() {
        return action.type(nameField, "John Doe").isSuccess() &&
               action.type(emailField, "john@example.com").isSuccess() &&
               action.click(submitButton).isSuccess();
    }
}
```

### Error Handling
```java
@Transition(from = SearchState.class, to = ResultsState.class)
public class SearchTransition {
    public boolean execute() {
        try {
            action.type(searchBox, searchTerm);
            action.click(searchButton);
            return action.find(resultsPanel).isSuccess();
        } catch (Exception e) {
            log.error("Search transition failed", e);
            return false;
        }
    }
}
```

## Best Practices

1. Use these standard annotations:
   ```java
   @Transition(from = X.class, to = Y.class)
   @RequiredArgsConstructor  // For dependency injection
   @Slf4j                   // For logging
   ```

2. Keep transitions simple and focused on navigation

3. Log important steps for debugging

4. Return `true` for success, `false` for failure

5. Inject dependencies via constructor (Action, states, etc.)

With states and transitions using annotations, the entire state machine is automatically configured!