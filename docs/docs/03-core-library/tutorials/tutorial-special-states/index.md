---
sidebar_position: 4
title: 'Special States Tutorial'
---

# Special States Tutorial

This tutorial demonstrates the use of special state markers in Brobot for handling dynamic transitions and overlays.

## What You'll Learn

- How to use **PreviousState** for returning to hidden states
- How to use **CurrentState** for self-transitions
- How hidden state management works automatically
- Best practices for overlay handling

## Project Structure

The complete example project is located in `examples/03-core-library/guides/dynamic-transitions/special-states-example/` and includes:

```
special-states-example/
├── src/main/java/com/example/specialstates/
│   ├── states/
│   │   ├── MainPageState.java       # Main page that can be hidden by modal
│   │   ├── ModalDialogState.java    # Modal that overlays other states
│   │   └── SettingsPageState.java   # Settings page that can also be hidden
│   ├── transitions/
│   │   ├── MainPageTransitions.java     # Includes CurrentState transitions
│   │   ├── ModalDialogTransitions.java  # Uses PreviousState to return
│   │   └── SettingsPageTransitions.java # Can open modal and self-transition
│   ├── runner/
│   │   └── SpecialStatesTestRunner.java # Test scenarios
│   └── SpecialStatesApplication.java    # Main Spring Boot application
└── src/main/resources/
    └── application.properties        # Mock mode configuration
```

## Key Concepts Demonstrated

### PreviousState - Dynamic Returns

When a modal dialog opens over any page, it "hides" that page. Using `PreviousState` as the target allows the modal to return to whatever was hidden, without hard-coding specific states:

```java
@OutgoingTransition(
    to = PreviousState.class,  // Returns to whatever was hidden
    staysVisible = false,      // Modal closes
    pathCost = 0
)
public boolean closeModal() {
    // This returns to MainPage OR SettingsPage
    // depending on what was hidden
    return action.click(closeButton).isSuccess();
}
```

### CurrentState - Self-Transitions

Actions that modify the current page without navigation use `CurrentState`:

```java
@OutgoingTransition(
    to = CurrentState.class,  // Stay in current state
    pathCost = 2,
    description = "Refresh page"
)
public boolean refresh() {
    // Executes action but stays on same page
    return action.type("{F5}").isSuccess();
}
```

## Test Scenarios

The example includes three test scenarios that demonstrate these concepts:

### Test 1: PreviousState with MainPage Hidden
1. Navigate to MainPage
2. Open Modal (MainPage becomes hidden)
3. Navigate to MainPage target (triggers PreviousState transition from Modal)
4. Verify return to MainPage

### Test 2: PreviousState with SettingsPage Hidden
1. Navigate to SettingsPage
2. Open Modal (SettingsPage becomes hidden)
3. Navigate to SettingsPage target (triggers PreviousState transition from Modal)
4. Verify return to SettingsPage

### Test 3: CurrentState Self-Transitions
1. Navigate to MainPage
2. Execute refresh (CurrentState transition)
3. Navigate to SettingsPage
4. Execute save settings (CurrentState transition)

## Running the Example

From the brobot root directory:

```bash
# Build the example
./gradlew :examples:03-core-library:guides:dynamic-transitions:special-states-example:build

# Run the example
./gradlew :examples:03-core-library:guides:dynamic-transitions:special-states-example:bootRun
```

The application runs in mock mode (no real GUI required) and demonstrates:
- ✅ PreviousState returns to correct hidden state
- ✅ CurrentState maintains current state
- ✅ Hidden states are tracked automatically

## Configuration

The `application.properties` enables mock mode for testing:

```properties
# Enable mock mode for testing without GUI
brobot.mock=true

# Enable verbose logging to see state transitions
brobot.logging.verbosity=VERBOSE
brobot.console.actions.enabled=true
brobot.console.actions.level=VERBOSE

# Fast mock timings
brobot.action.maxWait=0.04
brobot.action.pauseTime=0.01
```

## Integration with Your Project

To use special states in your own Brobot project:

1. **Import the special marker classes:**
```java
import io.github.jspinak.brobot.model.state.special.PreviousState;
import io.github.jspinak.brobot.model.state.special.CurrentState;
```

2. **Use them in your transitions:**
```java
@OutgoingTransition(to = PreviousState.class, ...)
@OutgoingTransition(to = CurrentState.class, ...)
```

3. **Configure states that can be hidden:**
```java
@OutgoingTransition(
    to = OverlayState.class,
    staysVisible = true,  // Current state stays visible (hidden)
    ...
)
```

## Best Practices

1. **Use PreviousState for true overlays** - dialogs, menus, popups
2. **Use CurrentState for in-page actions** - refresh, pagination, sorting
3. **Set staysVisible correctly** - determines if a state becomes hidden
4. **Test with mock mode first** - ensures logic works before GUI automation
5. **Use StateNavigator.openState()** - leverages the state management system

## Learn More

- [Dynamic Transitions Guide](/docs/core-library/guides/dynamic-transitions) - Complete reference
- [Core Concepts](/docs/getting-started/core-concepts) - Understanding states
- [Transitions Overview](/docs/getting-started/transitions) - Transition basics

## Source Code

The complete source code for this tutorial is available in the `examples/03-core-library/guides/dynamic-transitions/special-states-example/` directory.