---
sidebar_position: 7
title: 'Dynamic Transitions and Hidden States'
---

# Dynamic Transitions and Hidden States

## Overview

A common challenge in GUI automation is dealing with dynamic overlays like menus, dialogs, and pop-ups that can appear at any time and cover other UI elements. Brobot handles this elegantly through its **Hidden States** mechanism and **Dynamic Transitions**.

## The Problem

Consider a typical application with:
- A main window with various screens (Home, Settings, Profile, etc.)
- A menu that can be opened from any screen
- Dialogs that can appear over any state
- Pop-ups that temporarily cover content

When a menu opens over the Settings screen, the Settings state becomes temporarily inaccessible. When the menu is closed, you want to return to Settings. But what if the menu was opened from the Home screen instead? The same action that transitions from Menu to Settings would also transition from Menu to Home. If transitions were hard-coded, it would create ambiguity in the pathfinding algorithm. 

## The Solution: Hidden States

Brobot solves this with the Hidden States mechanism:

1. **Automatic Registration**: When a state opens and covers another, the covered state is automatically registered as "hidden"
2. **Dynamic Tracking**: The framework maintains a stack of hidden states for each active state
3. **Smart Navigation**: Special dynamic transitions can navigate back to the most recently hidden state

## How Hidden States Work

### State Registration

When a state becomes active and covers another state, Brobot:
1. Detects which states are now covered
2. Registers them in the covering state's `hiddenStates` field
3. Maintains this information until the covering state is closed

```java
// Example: When MenuState opens, it registers what it covered
public class MenuState extends State {
    // The framework automatically tracks what this menu is covering
    // This could be HomeState, SettingsState, or any other state
}
```

### The PREVIOUS Transition Target

Instead of defining static transitions to every possible state, you can use the special `PREVIOUS` target:

```java
// Traditional approach - needs many transitions
@Transition(from = MenuState.class, to = HomeState.class)
@Transition(from = MenuState.class, to = SettingsState.class)
@Transition(from = MenuState.class, to = ProfileState.class)
// ... many more

// Dynamic approach - one transition handles all cases
@Transition(
    from = MenuState.class,
    to = PreviousState.class,  // Special marker for dynamic transitions
    description = "Close menu and return to whatever was underneath"
)
```

## Implementation Examples

### Modern Approach with @Transition

```java
import io.github.jspinak.brobot.state.PreviousState;

@Transition(
    from = MenuState.class,
    to = PreviousState.class,
    description = "Close menu and return to previous state"
)
@RequiredArgsConstructor
public class CloseMenuTransition {
    private final MenuState menuState;
    private final Action action;
    
    public boolean execute() {
        // Close the menu
        if (action.click(menuState.getCloseButton()).isSuccess()) {
            return true;
        }
        
        // Fallback: press ESC
        return action.type(new ObjectCollection.Builder()
            .withStrings("\u001B")
            .build()).isSuccess();
    }
}
```

### Traditional Approach with StateTransitions

```java
public class MenuTransitions {
    public StateTransitions getStateTransitions() {
        return new StateTransitions.Builder(MENU)
            .addTransition(new StateTransition.Builder()
                .addToActivate(PREVIOUS)  // Dynamic target
                .setFunction(this::closeMenu)
                .build())
            .build();
    }
    
    private boolean closeMenu() {
        // Implementation to close the menu
        return action.click(closeButton).isSuccess();
    }
}
```

## Advanced Patterns

### Multi-Level Overlays

Hidden states can be nested. For example:
1. User is in Settings
2. Opens Menu (Settings becomes hidden)
3. Opens Help Dialog from Menu (Menu becomes hidden)
4. Closing Help returns to Menu
5. Closing Menu returns to Settings

```java
// Each overlay maintains its own hidden state information
@Transition(from = HelpDialog.class, to = PreviousState.class)
public class CloseHelpTransition {
    // Returns to Menu if opened from Menu
}

@Transition(from = MenuState.class, to = PreviousState.class)
public class CloseMenuTransition {
    // Returns to Settings or whatever was underneath
}
```

### Conditional Hidden States

Sometimes you want to return to a previous state only under certain conditions:

```java
@Transition(from = ErrorDialog.class, to = PreviousState.class)
@RequiredArgsConstructor
public class ErrorRecoveryTransition {
    private final ErrorDialog errorDialog;
    private final Action action;
    private final StateMemory stateMemory;
    
    public boolean execute() {
        // Check if error is recoverable
        if (errorDialog.isRecoverable()) {
            // Dismiss and return to previous state
            action.click(errorDialog.getDismissButton());
            return true;
        } else {
            // Critical error - go to home instead
            action.click(errorDialog.getHomeButton());
            return false;  // Don't use PREVIOUS
        }
    }
}
```

### Combining with State Verification

Ensure the hidden state is still valid before returning:

```java
@Transition(from = PopupState.class, to = PreviousState.class)
public class SmartPopupCloseTransition {
    
    public boolean execute() {
        // Close the popup
        action.click(closeButton);
        
        // Wait for popup to disappear
        action.vanish(popupElement);
        
        // The framework will verify the previous state is active
        // If not, it will trigger appropriate recovery transitions
        return true;
    }
}
```

## Framework Components

### SetHiddenStates

The `SetHiddenStates` service manages the registration and tracking of hidden states:

```java
@Component
public class SetHiddenStates {
    // Automatically invoked by the framework when states change
    public void setHiddenStates(State coveringState, Set<State> coveredStates) {
        // Registers which states are hidden by the covering state
    }
    
    public Set<State> getHiddenStates(State state) {
        // Returns the states hidden by this state
    }
}
```

### StateMemory

The `StateMemory` service maintains the history of state transitions and hidden states:

```java
@Component
public class StateMemory {
    // Tracks the sequence of state activations
    // Used to determine the "previous" state for dynamic transitions
    
    public Optional<State> getPreviousState() {
        // Returns the most recently hidden state
    }
    
    public void recordStateChange(State from, State to) {
        // Updates the state history
    }
}
```

## Best Practices

### 1. Use for True Overlays

Dynamic transitions work best for UI elements that truly overlay others:
- Modal dialogs
- Dropdown menus
- Pop-up notifications
- Sidebars that slide over content

### 2. Provide Fallbacks

Always have a fallback plan if the dynamic transition fails:

```java
@Transition(from = MenuState.class, to = PreviousState.class, priority = 1)
public class CloseMenuToPrevious {
    // Primary: try to return to previous
}

@Transition(from = MenuState.class, to = HomeState.class, priority = 2)
public class CloseMenuToHome {
    // Fallback: go to home if previous fails
}
```

### 3. Clear Visual Indicators

Ensure overlays have clear visual indicators for state detection:

```java
public class DialogState extends State {
    @StateImage
    private StateImage dialogHeader;  // Unique to this dialog
    
    @StateImage
    private StateImage darkOverlay;   // Common overlay indicator
}
```

### 4. Handle Edge Cases

Consider edge cases in your transitions:
- What if the previous state no longer exists?
- What if multiple overlays are stacked?
- What if the application crashed and restarted?

```java
@Transition(from = MenuState.class, to = PreviousState.class)
public class RobustMenuClose {
    
    public boolean execute() {
        // Try to close menu
        if (!action.click(closeButton).isSuccess()) {
            // Fallback 1: ESC key
            action.type("\u001B");
        }
        
        // Verify menu is gone
        if (action.vanish(menuElement).isSuccess()) {
            return true;  // Let framework handle PREVIOUS
        }
        
        // Menu still visible - force navigation
        log.warn("Menu failed to close, forcing home navigation");
        return action.click(homeButton).isSuccess();
    }
}
```

## Common Use Cases

### 1. Modal Dialogs

```java
@State
public class ConfirmDialog extends State {
    // This dialog can appear over any state
}

@Transition(from = ConfirmDialog.class, to = PreviousState.class)
public class ConfirmDialogHandler {
    public boolean execute() {
        // Handle confirm/cancel and return to whatever was underneath
        return action.click(confirmButton).isSuccess();
    }
}
```

### 2. Navigation Menus

```java
@State
public class NavigationMenu extends State {
    // Hamburger menu that slides over content
}

@Transition(from = NavigationMenu.class, to = PreviousState.class)
public class CloseNavMenu {
    public boolean execute() {
        // Swipe or click to close menu
        return action.click(menuOverlay).isSuccess();
    }
}
```

### 3. Help Overlays

```java
@State
public class HelpOverlay extends State {
    // Tutorial or help screens that overlay the application
}

@Transition(from = HelpOverlay.class, to = PreviousState.class)
public class DismissHelp {
    public boolean execute() {
        // Click "Got it" or press ESC
        return action.click(gotItButton).isSuccess();
    }
}
```

## Troubleshooting

### Hidden State Not Found

If a dynamic transition fails to find the previous state:
1. Check that states are properly detecting overlays
2. Verify StateImage definitions don't overlap incorrectly
3. Ensure the covering state is properly registered
4. Check logs for state transition history

### Multiple States Claiming to be Active

This can happen with poor state definition:
1. Make StateImages more specific
2. Use unique identifiers for each state
3. Adjust pattern matching thresholds
4. Consider using state priorities

### Transition Loops

Prevent infinite loops between states:
1. Use transition priorities
2. Implement maximum retry counts
3. Add cooldown periods between transitions
4. Log and monitor transition patterns

## Related Documentation

- [Transitions Overview](/docs/getting-started/transitions) - General transition concepts
- [Core Concepts](/docs/getting-started/core-concepts#handling-dynamic-overlays-hidden-states) - Brief hidden states overview
- [States Overview](/docs/getting-started/states) - Understanding states in Brobot
- [State-Aware Scheduling](/docs/core-library/guides/state-aware-scheduling) - Advanced state scheduling patterns