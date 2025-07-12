---
sidebar_position: 2
---

# Annotations

Brobot provides a powerful annotation system that simplifies state and transition configuration by using declarative annotations instead of manual registration code.

## Overview

The annotation system introduces two key annotations:
- **`@State`** - Marks a class as a Brobot state
- **`@Transition`** - Marks a class as containing transition logic

These annotations work with Spring's component scanning to automatically discover and register your states and transitions at application startup.

## Benefits

### Before (Manual Registration)
```java
@Component
@EventListener(ApplicationReadyEvent.class)
public class StateRegistrationListener {
    private final StateService stateService;
    private final ActionPipeline action;
    private final StateTransitionsJointTable jointTable;
    // ... 67 lines of manual registration code
}
```

### After (With Annotations)
```java
@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    private StateObject submitButton = new StateObject.Builder()
        .withImage("submit")
        .build();
}

@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptToWorkingTransition {
    // Transition logic
}
```

## @State Annotation

The `@State` annotation marks a class as a Brobot state and includes Spring's `@Component` for automatic discovery.

### Basic Usage

```java
@State
@Getter
@Slf4j
public class LoginState {
    private StateObject loginButton = new StateObject.Builder()
        .withImage("login-button")
        .build();
    
    private StateObject usernameField = new StateObject.Builder()
        .withImage("username-field")
        .build();
}
```

### Marking Initial States

Use the `initial` parameter to designate starting states:

```java
@State(initial = true)
@Getter
@Slf4j
public class HomeState {
    // State definition
}
```

### Custom State Names

By default, the state name is derived from the class name (removing "State" suffix if present). You can override this:

```java
@State(name = "Dashboard", description = "Main application dashboard")
@Getter
@Slf4j
public class DashboardState {
    // State definition
}
```

## @Transition Annotation

The `@Transition` annotation marks a class as containing transition logic and includes Spring's `@Component`.

### Basic Transition

```java
@Transition(from = LoginState.class, to = DashboardState.class)
@RequiredArgsConstructor
@Slf4j
public class LoginToDashboardTransition {
    
    private final ActionOptions actionOptions;
    
    public boolean execute() {
        log.info("Transitioning from Login to Dashboard");
        // Perform login actions
        return action.click("login-button")
            .find("dashboard-header")
            .isSuccess();
    }
}
```

### Multiple Source or Target States

```java
@Transition(
    from = {ErrorState.class, TimeoutState.class},
    to = HomeState.class
)
@RequiredArgsConstructor
@Slf4j
public class RecoveryTransition {
    
    public boolean execute() {
        // Recovery logic to return to home
        return true;
    }
}
```

### Conditional Transitions

```java
@Transition(
    from = WorkingState.class,
    to = {SuccessState.class, ErrorState.class}
)
@RequiredArgsConstructor
@Slf4j
public class WorkingTransitions {
    
    public boolean execute() {
        if (checkSuccess()) {
            // The framework will navigate to SuccessState
            return true;
        } else {
            // The framework will navigate to ErrorState
            return false;
        }
    }
}
```

### Custom Method Names

By default, the framework looks for an `execute()` method. You can specify a different method:

```java
@Transition(
    from = SearchState.class,
    to = ResultsState.class,
    method = "performSearch"
)
@RequiredArgsConstructor
@Slf4j
public class SearchTransition {
    
    public boolean performSearch() {
        // Search logic
        return true;
    }
}
```

### Transition Priority

Control path-finding preferences with priority:

```java
@Transition(
    from = MainMenuState.class,
    to = SettingsState.class,
    priority = 10  // Higher priority paths are preferred
)
```

## Complete Example

Here's a complete example showing how annotations simplify a typical automation flow:

```java
// Initial state
@State(initial = true)
@Getter
@Slf4j
public class LoginPageState {
    private StateObject logo = new StateObject.Builder()
        .withImage("app-logo")
        .build();
    
    private StateObject usernameField = new StateObject.Builder()
        .withImage("username-field")
        .build();
    
    private StateObject passwordField = new StateObject.Builder()
        .withImage("password-field")
        .build();
    
    private StateObject loginButton = new StateObject.Builder()
        .withImage("login-button")
        .build();
}

// Main application state
@State
@Getter
@Slf4j
public class DashboardState {
    private StateObject dashboardHeader = new StateObject.Builder()
        .withImage("dashboard-header")
        .build();
    
    private StateObject menuButton = new StateObject.Builder()
        .withImage("menu-button")
        .build();
}

// Login transition
@Transition(from = LoginPageState.class, to = DashboardState.class)
@RequiredArgsConstructor
@Slf4j
public class LoginTransition {
    
    private final ActionOptions actionOptions;
    private final String username;
    private final String password;
    
    public boolean execute() {
        log.info("Performing login");
        
        return action.click("username-field")
            .type(username)
            .click("password-field")
            .type(password)
            .click("login-button")
            .find("dashboard-header")
            .isSuccess();
    }
}
```

## How It Works

1. **Component Scanning**: Spring automatically discovers all classes annotated with `@State` and `@Transition`
2. **Annotation Processing**: The `AnnotationProcessor` processes these annotations at application startup
3. **State Registration**: States are registered with the framework and initial states are marked
4. **Transition Wiring**: Transitions are created and connected between the appropriate states
5. **Automatic Configuration**: The state machine is fully configured without manual registration code

## Best Practices

### 1. Always Include Required Lombok Annotations

```java
@State
@Getter  // Required for state objects
@Slf4j   // Recommended for logging
public class MyState {
    // State definition
}

@Transition(from = StateA.class, to = StateB.class)
@RequiredArgsConstructor  // For dependency injection
@Slf4j                   // For logging
public class MyTransition {
    // Transition logic
}
```

### 2. Use Descriptive Names

While class names are used by default, consider adding descriptions for clarity:

```java
@State(
    name = "UserProfile",
    description = "User profile page with account settings and preferences"
)
```

### 3. Keep Transitions Focused

Each transition class should handle one specific navigation path:

```java
// Good: Single responsibility
@Transition(from = CartState.class, to = CheckoutState.class)
public class ProceedToCheckoutTransition { }

// Avoid: Multiple unrelated transitions in one class
```

### 4. Handle Errors Gracefully

```java
@Transition(from = ProcessingState.class, to = {CompleteState.class, ErrorState.class})
public class ProcessingTransition {
    public boolean execute() {
        try {
            // Processing logic
            return processSuccessfully();
        } catch (Exception e) {
            log.error("Processing failed", e);
            return false; // Will transition to ErrorState
        }
    }
}
```

## Migration Guide

To migrate existing code to use annotations:

1. **Remove StateRegistrationListener**: Delete manual registration classes
2. **Add @State annotations**: Mark all state classes
3. **Add @Transition annotations**: Mark transition classes
4. **Add initial parameter**: Mark starting states with `@State(initial = true)`
5. **Add Lombok annotations**: Include @Getter and @Slf4j as needed
6. **Test the migration**: Verify all states and transitions are discovered

## Troubleshooting

### States Not Being Discovered

Ensure your states are in a package scanned by Spring:
- Check your `@ComponentScan` configuration
- Verify `@State` is properly imported
- Confirm the class is public

### Transitions Not Working

- Verify the transition method returns `boolean`
- Check that source and target states exist
- Review logs for registration errors
- Ensure `@Transition` includes valid `from` and `to` classes

### Initial States Not Set

- At least one state must have `@State(initial = true)`
- Check logs for "Marked X as initial state" messages
- Verify the state class is being discovered

## Summary

The Brobot annotation system dramatically simplifies state machine configuration by:
- Eliminating boilerplate registration code
- Providing clear, declarative configuration
- Integrating seamlessly with Spring
- Supporting complex transition scenarios
- Enabling better code organization

By using `@State` and `@Transition` annotations, you can focus on your automation logic rather than framework setup, making your code more maintainable and easier to understand.