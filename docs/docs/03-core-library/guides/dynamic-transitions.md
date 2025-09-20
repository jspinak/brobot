---
sidebar_position: 7
title: 'Dynamic Transitions and Hidden States'
---

# Dynamic Transitions and Hidden States

## Overview

A common challenge in GUI automation is dealing with dynamic overlays like menus, dialogs, and pop-ups that can appear at any time and cover other UI elements. Brobot handles this elegantly through its **Hidden States** mechanism and **Dynamic Transitions** using special marker classes.

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
3. **Smart Navigation**: Special marker classes enable dynamic transitions back to hidden states

## Special Marker Classes

Brobot provides special marker classes for dynamic transitions:

### ✅ Currently Implemented

- **`PreviousState`**: Returns to the most recently hidden state (fully functional)
- **`CurrentState`**: Targets the currently active state for self-transitions (fully functional)

### ⚠️ Future Consideration

- **`ExpectedState`**: Would navigate to an expected state determined at runtime (not yet implemented)

## Understanding Each Special State Type

### PreviousState - Return to Hidden State

**What it does**: Returns to whatever state was covered by an overlay.

**Real-world examples**:
- Closing a modal dialog returns to the page that opened it
- Exiting fullscreen video returns to the article or gallery
- Dismissing a popup menu returns to the underlying screen
- Closing a lightbox returns to the product page or article

```java
@TransitionSet(state = ModalDialog.class)
public class ModalDialogTransitions {

    @OutgoingTransition(
        activate = {PreviousState.class},  // Returns to whatever opened this modal
        pathCost = 0,
        description = "Close modal and return to previous state"
    )
    public boolean closeModal() {
        return action.click(closeButton).isSuccess();
    }
}
```

**When to use PreviousState**:
- Modal dialogs that can appear over any page
- Popup menus and dropdowns
- Lightboxes and image viewers
- Fullscreen modes
- Temporary overlays
- Help tooltips
- Quick-view product previews

### CurrentState - Self-Transitions

**What it does**: Performs an action while staying in the same state. The UI might change slightly, but you remain conceptually in the same place. Also useful when transitioning between overlapping states where both remain active.

**Tutorial Example - Island Capture**:
In the tutorial-basics example, the Island state overlays the World state. To capture multiple islands, instead of directly calling transition methods (which bypasses the state management system), use CurrentState:

```java
@TransitionSet(state = IslandState.class)
public class IslandTransitions {

    // Better approach - using CurrentState for re-entry
    @OutgoingTransition(
        activate = {CurrentState.class},  // Re-enter Island state
        pathCost = 0,
        description = "Capture new island (re-enter Island from World)"
    )
    public boolean captureNewIsland() {
        // Since both World and Island are active (Island overlays World),
        // this transition from World back to Island to capture another island
        // This properly uses the state management system
        return action.click(worldState.getNextIsland()).isSuccess();
    }

    // Traditional approach - avoid this pattern
    public void getNewIsland() {
        // This bypasses the state management system
        fromWorld();  // Direct method call - not recommended
    }
}
```

**Other Real-world examples**:
- Pagination (loading more results)
- Sorting or filtering lists
- Refreshing data
- Expanding/collapsing sections
- Form validation errors
- Toggling view modes

```java
@TransitionSet(state = SearchResultsPage.class)
public class SearchResultsTransitions {

    @OutgoingTransition(
        activate = {CurrentState.class},  // Stay on search results
        pathCost = 2,
        description = "Load more results"
    )
    public boolean loadMoreResults() {
        // Clicking "Load More" adds results but stays on same page
        return action.click(loadMoreButton).isSuccess();
    }

    @OutgoingTransition(
        activate = {CurrentState.class},
        pathCost = 3,
        description = "Sort results by price"
    )
    public boolean sortByPrice() {
        // Sorting rearranges items but doesn't leave the page
        action.click(sortDropdown);
        return action.click(priceOption).isSuccess();
    }

    @OutgoingTransition(
        activate = {CurrentState.class},
        pathCost = 5,
        description = "Refresh search results"
    )
    public boolean refreshResults() {
        // F5 refreshes but stays on same page
        return action.type("{F5}").isSuccess();
    }
}
```

**When to use CurrentState**:
- Actions that modify the current page without navigation
- Data refresh operations
- In-page interactions (sorting, filtering, pagination)
- UI state changes (expand/collapse, show/hide)
- Form submissions that stay on the same page (validation errors)
- Re-entering an active state from another active state (overlapping states)
- Game scenarios where capturing items doesn't change the active state

### ExpectedState - Runtime-Determined (Not Yet Implemented)

**Concept**: Would allow transitions where the target state is determined at runtime based on conditions like user preferences, roles, or application state.

**Current alternatives**: Most "expected state" scenarios can be handled today using:

1. **Multiple transitions with different path costs**:
```java
@OutgoingTransition(activate = {AdminDashboard.class}, pathCost = 0)
public boolean loginAsAdmin() {
    if (!user.isAdmin()) return false;
    return performLogin();
}

@OutgoingTransition(activate = {UserHome.class}, pathCost = 0)
public boolean loginAsUser() {
    if (user.isAdmin()) return false;
    return performLogin();
}
```

2. **Conditional logic in transition methods**:
```java
@OutgoingTransition(activate = {HomePage.class}, pathCost = 0)
public boolean navigateToHome() {
    // The HomePage state can handle different user types internally
    return action.click(homeButton).isSuccess();
}
```

3. **State detection after transition**:
```java
// Let the framework detect which state we ended up in
@OutgoingTransition(activate = {HomePage.class}, pathCost = 10)
public boolean attemptNavigation() {
    action.click(navigationButton);
    // Framework will verify actual state after transition
    return true;
}
```

## Understanding UnknownState (Not a Special Marker)

**Important**: `UnknownState` is NOT a special marker class. It's an actual state that exists in every Brobot project.

### How UnknownState Works

1. **You don't navigate TO UnknownState** - you end up there when state detection fails
2. **UnknownState has outgoing transitions** - these are your recovery mechanisms
3. **It's your safety net** - ensures automation can always recover

```java
// CORRECT: UnknownState has outgoing transitions for recovery
@TransitionSet(state = UnknownState.class)
public class UnknownStateTransitions {

    @OutgoingTransition(
        activate = {HomePage.class},
        pathCost = 10,
        description = "Recover to home"
    )
    public boolean recoverToHome() {
        // Try to get back to a known state
        action.type("{ESC}");  // Close any popups
        return action.click(homeButton).isSuccess();
    }

    @OutgoingTransition(
        activate = {LoginPage.class},
        pathCost = 20,
        description = "Recover to login if session expired"
    )
    public boolean recoverToLogin() {
        // Navigate to login page
        return action.goToUrl(loginUrl).isSuccess();
    }
}

// INCORRECT: Never transition TO UnknownState
// @OutgoingTransition(activate = {UnknownState.class}) // ❌ DON'T DO THIS
```

## Complete Working Example: Special States

The following is a complete, tested example from the special-states-example project that demonstrates PreviousState and CurrentState in action. The full project is available at `examples/03-core-library/guides/dynamic-transitions/special-states-example/`.

### Modal Dialog State with PreviousState

```java
package com.example.specialstates.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Modal dialog state that overlays the main page. This state hides whatever state was active before
 * it.
 */
@State(description = "Modal dialog overlay")
@Component
@Getter
@Slf4j
public class ModalDialogState {

    private final StateImage dialogTitle;
    private final StateImage confirmButton;
    private final StateImage cancelButton;
    private final StateImage closeButton;

    public ModalDialogState() {
        log.info("Initializing ModalDialogState");

        dialogTitle =
                new StateImage.Builder().addPatterns("dialogTitle").setName("dialogTitle").build();

        confirmButton =
                new StateImage.Builder().addPatterns("confirmBtn").setName("confirmButton").build();

        cancelButton =
                new StateImage.Builder().addPatterns("cancelBtn").setName("cancelButton").build();

        closeButton =
                new StateImage.Builder()
                        .addPatterns("closeBtn", "xButton")
                        .setName("closeButton")
                        .build();
    }
}
```

### Modal Dialog Transitions Using PreviousState

```java
package com.example.specialstates.transitions;

import org.springframework.stereotype.Component;

import com.example.specialstates.states.ModalDialogState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.special.PreviousState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transitions for the ModalDialog state. Uses PreviousState to return to whatever state was hidden
 * by the modal.
 */
@TransitionSet(state = ModalDialogState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class ModalDialogTransitions {

    private final ModalDialogState modalDialogState;
    private final Action action;

    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at ModalDialog");
        // In mock mode, always return true
        return true;
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Confirm and close modal, returning to previous state")
    public boolean confirmAndClose() {
        log.info("Confirming and closing modal - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        // In mock mode, just return true
        return true;
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Cancel and close modal, returning to previous state")
    public boolean cancelAndClose() {
        log.info("Cancelling and closing modal - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        // In mock mode, just return true
        return true;
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Close modal with X button, returning to previous state")
    public boolean closeModal() {
        log.info("Closing modal with X button - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        // In mock mode, just return true
        return true;
    }
}
```

### Main Page with CurrentState Self-Transitions

```java
package com.example.specialstates.transitions;

import org.springframework.stereotype.Component;

import com.example.specialstates.states.MainPageState;
import com.example.specialstates.states.ModalDialogState;
import com.example.specialstates.states.SettingsPageState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.special.CurrentState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Transitions for the MainPage state. Includes self-transitions using CurrentState. */
@TransitionSet(state = MainPageState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class MainPageTransitions {

    private final MainPageState mainPageState;
    private final Action action;

    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at MainPage");
        // In mock mode, always return true
        return true;
    }

    @OutgoingTransition(
            activate = {ModalDialogState.class},
            staysVisible = true, // MainPage stays visible behind modal
            pathCost = 0,
            description = "Open modal dialog over main page")
    public boolean openModal() {
        log.info("Opening modal dialog from MainPage");
        // In mock mode, just return true
        return true;
    }

    @OutgoingTransition(
            activate = {SettingsPageState.class},
            pathCost = 1,
            description = "Navigate to settings page")
    public boolean toSettings() {
        log.info("Navigating from MainPage to Settings");
        return true;
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 2,
            description = "Refresh main page")
    public boolean refresh() {
        log.info("Refreshing MainPage (self-transition using CurrentState)");
        // This demonstrates a self-transition
        return true;
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 3,
            description = "Load next page of results")
    public boolean nextPage() {
        log.info("Loading next page of results (self-transition using CurrentState)");
        // Another self-transition example
        return true;
    }
}
```

### Generic Data Grid Example with Self-Transitions

```java
@TransitionSet(state = DataGridState.class)
@Component
public class DataGridTransitions {
    private final DataGridState grid;
    private final Action action;

    @OutgoingTransition(
        activate = {CurrentState.class},  // Stay in same state
        pathCost = 5,
        description = "Load next page of data"
    )
    public boolean nextPage() {
        return action.click(grid.getNextPageButton()).isSuccess();
    }

    @OutgoingTransition(
        activate = {CurrentState.class},
        pathCost = 5,
        description = "Load previous page of data"
    )
    public boolean previousPage() {
        return action.click(grid.getPrevPageButton()).isSuccess();
    }

    @OutgoingTransition(
        activate = {CurrentState.class},
        pathCost = 3,
        description = "Sort by column"
    )
    public boolean sortByColumn() {
        return action.click(grid.getColumnHeader()).isSuccess();
    }

    @OutgoingTransition(
        activate = {CurrentState.class},
        pathCost = 2,
        description = "Apply filter"
    )
    public boolean applyFilter() {
        action.type(grid.getFilterInput(), filterText);
        return action.click(grid.getApplyButton()).isSuccess();
    }
}
```

### Multi-Level Overlays

```java
// Settings can be covered by Menu
@State
public class SettingsPage {
    // Settings page elements
}

// Menu can cover any page
@State
public class MenuOverlay {
    // Menu elements
}

// Help can cover the Menu
@State
public class HelpDialog {
    // Help dialog elements
}

// Transitions maintain the hidden state stack
@TransitionSet(state = HelpDialog.class)
public class HelpDialogTransitions {
    @OutgoingTransition(activate = {PreviousState.class}, pathCost = 0)
    public boolean closeHelp() {
        // Returns to Menu (which is covering Settings)
        return action.click(closeButton).isSuccess();
    }
}

@TransitionSet(state = MenuOverlay.class)
public class MenuTransitions {
    @OutgoingTransition(activate = {PreviousState.class}, pathCost = 0)
    public boolean closeMenu() {
        // Returns to Settings (or whatever was covered)
        return action.click(closeButton).isSuccess();
    }
}
```

## Decision Guide

```
Which transition type should I use?

1. Does the action change the page/screen?
   NO → Use CurrentState (self-transition)
   YES → Continue to #2

2. Is this closing an overlay that covers other content?
   YES → Use PreviousState (return to hidden)
   NO → Continue to #3

3. Do I know at compile-time where this will go?
   YES → Use a regular state class (HomeState.class, etc.)
   NO → Consider the alternatives mentioned for ExpectedState
```

## Best Practices

### 1. Use PreviousState for True Overlays

Dynamic transitions work best for UI elements that truly overlay others:
- Modal dialogs
- Dropdown menus
- Pop-up notifications
- Sidebars that slide over content

### 2. Use CurrentState for In-Page Actions

Self-transitions are perfect for actions that don't leave the current context:
- Pagination
- Sorting and filtering
- Data refresh
- UI toggles

### 3. Provide Fallbacks

Always have a fallback plan if dynamic transitions might fail:

```java
@TransitionSet(state = MenuState.class)
public class MenuTransitions {

    @OutgoingTransition(activate = {PreviousState.class}, pathCost = 0)
    public boolean closeToPrevious() {
        // Primary: try to return to previous
        return action.click(closeButton).isSuccess();
    }

    @OutgoingTransition(activate = {HomePage.class}, pathCost = 10)
    public boolean closeToHome() {
        // Fallback: go to home if previous fails
        return action.click(homeButton).isSuccess();
    }
}
```

### 4. Clear Visual Indicators

Ensure overlays have clear visual indicators for state detection:

```java
@State
public class DialogState {
    @StateImage
    private StateImage dialogHeader;  // Unique to this dialog

    @StateImage
    private StateImage darkOverlay;   // Common overlay indicator
}
```

### 5. Handle Edge Cases

Consider edge cases in your transitions:
- What if the previous state no longer exists?
- What if multiple overlays are stacked?
- What if the application crashed and restarted?

## Framework Components

### Special Marker Classes

Located in `io.github.jspinak.brobot.model.state.special`:

```java
// PreviousState.java
public final class PreviousState {
    public static final Long ID = SpecialStateType.PREVIOUS.getId(); // -2L
    private PreviousState() {} // Cannot be instantiated
}

// CurrentState.java
public final class CurrentState {
    public static final Long ID = SpecialStateType.CURRENT.getId(); // -3L
    private CurrentState() {} // Cannot be instantiated
}

// ExpectedState.java (marker exists but not yet functional)
public final class ExpectedState {
    public static final Long ID = SpecialStateType.EXPECTED.getId(); // -4L
    private ExpectedState() {} // Cannot be instantiated
}
```

### TransitionSetProcessor

The processor recognizes special marker classes and handles them appropriately:

```java
@Component
public class TransitionSetProcessor {
    // Detects when to = PreviousState.class or CurrentState.class
    // Sets special state IDs instead of regular state names
    // Enables dynamic resolution at runtime
}
```

### SetHiddenStates

Manages the registration and tracking of hidden states:

```java
@Component
public class SetHiddenStates {
    // Automatically invoked when states change
    public void setHiddenStates(State coveringState, Set<State> coveredStates) {
        // Registers which states are hidden by the covering state
    }
}
```

### StateMemory

Maintains the history of state transitions and hidden states:

```java
@Component
public class StateMemory {
    // Tracks the sequence of state activations
    // Used to determine the "previous" state for dynamic transitions
}
```

## Troubleshooting

### Hidden State Not Found

If a dynamic transition fails to find the previous state:
1. Check that states are properly detecting overlays
2. Verify StateImage definitions don't overlap incorrectly
3. Ensure the covering state is properly registered
4. Check logs for state transition history

### Self-Transitions Not Working

If CurrentState transitions aren't working:
1. Verify the action actually completes
2. Check that the state detection still passes after the action
3. Ensure the UI change doesn't trigger a different state detection

### Multiple States Claiming to be Active

This can happen with poor state definition:
1. Make StateImages more specific
2. Use unique identifiers for each state
3. Adjust pattern matching thresholds
4. Consider using state priorities

## Migration from Old Annotations

If you have old code using the deprecated `@Transition` annotation:

```java
// OLD (no longer supported)
@Transition(from = MenuState.class, to = PreviousState.class)

// NEW
@TransitionSet(state = MenuState.class)
public class MenuTransitions {
    @OutgoingTransition(to = PreviousState.class, pathCost = 0)
    public boolean closeToPrevious() {
        // Implementation
    }
}
```

## Summary

Brobot's special state transitions provide powerful patterns for handling dynamic UI behavior:

- **PreviousState**: Perfect for overlays that need to return to whatever they covered
- **CurrentState**: Ideal for actions that modify the current page without navigation
- **ExpectedState**: Future consideration for runtime-determined navigation
- **UnknownState**: Not a target, but a safety net with recovery transitions

These special states make your automation more resilient and adaptable to the dynamic nature of modern applications.

## Complete Tutorial and Example Project

A complete, runnable example demonstrating all special state concepts is available:
- **[Special States Tutorial](/docs/core-library/tutorials/tutorial-special-states)** - Step-by-step tutorial with full source code
- **Example Project**: `examples/03-core-library/guides/dynamic-transitions/special-states-example/`

## Related Documentation

- [Transitions Overview](/docs/getting-started/transitions) - General transition concepts
- [Core Concepts](/docs/getting-started/core-concepts#handling-dynamic-overlays-hidden-states) - Brief hidden states overview
- [States Overview](/docs/getting-started/states) - Understanding states in Brobot
- [State-Aware Scheduling](/docs/core-library/guides/state-aware-scheduling) - Advanced state scheduling patterns