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
2. **Dynamic Tracking**: The framework maintains a set of hidden states for each active state
3. **Smart Navigation**: Special marker classes enable dynamic transitions back to hidden states

**Visual Example:**
```
Initial State:           User Opens Menu:         User Opens Dialog:
┌─────────────┐         ┌─────────────┐          ┌─────────────┐
│  Settings   │         │    Menu     │          │   Dialog    │
│   (Active)  │   ───>  │  (Active)   │   ───>   │  (Active)   │
└─────────────┘         └─────────────┘          └─────────────┘
                        ┌─────────────┐          ┌─────────────┐
                        │  Settings   │          │    Menu     │
                        │  (Hidden)   │          │  (Hidden)   │
                        └─────────────┘          └─────────────┘
                                                 ┌─────────────┐
                                                 │  Settings   │
                                                 │  (Hidden)   │
                                                 └─────────────┘

Close Dialog (PreviousState):    Close Menu (PreviousState):
┌─────────────┐                  ┌─────────────┐
│    Menu     │                  │  Settings   │
│  (Active)   │   ───────>       │   (Active)  │
└─────────────┘                  └─────────────┘
┌─────────────┐
│  Settings   │
│  (Hidden)   │
└─────────────┘
```

## Special Marker Classes

Brobot provides special marker classes for dynamic transitions:

### ✅ Currently Implemented

- **`PreviousState`**: Returns to the most recently hidden state (fully functional)
- **`CurrentState`**: Targets the currently active state for self-transitions (fully functional)

### ⚠️ Future Consideration

- **`ExpectedState`**: Would navigate to an expected state determined at runtime (not yet implemented)

### Quick Comparison

| Feature | Regular Transition | PreviousState | CurrentState |
|---------|-------------------|---------------|--------------|
| **Target** | Known at compile-time | Determined at runtime | Same as source |
| **Use Case** | Navigate to specific state | Close overlay, return to hidden | Refresh, pagination, self-loop |
| **Example** | `HomePage.class` | Modal → previous page | Search → Search (load more) |
| **Path Cost** | Typical: 1-10 | Recommended: 0 | Typical: 0-5 |
| **Requires `canHide`** | No | Yes (in covering state) | No |
| **Best For** | Standard navigation | Modals, menus, popups | In-page actions, re-entry |
| **Fallback Needed?** | No | Yes (in case no hidden state) | No |

## Understanding Each Special State Type

### PreviousState - Return to Hidden State

**What it does**: Returns to whatever state was covered by an overlay.

**Real-world examples**:
- Closing a modal dialog returns to the page that opened it
- Exiting fullscreen video returns to the article or gallery
- Dismissing a popup menu returns to the underlying screen
- Closing a lightbox returns to the product page or article

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.special.PreviousState;
import org.springframework.beans.factory.annotation.Autowired;

@TransitionSet(state = ModalDialog.class)
public class ModalDialogTransitions {

    @Autowired
    private Action action;

    private StateImage closeButton;  // Defined elsewhere in the class

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
In the [tutorial-basics](/docs/core-library/tutorials/tutorial-basics) example, the Island state overlays the World state. To capture multiple islands, instead of directly calling transition methods (which bypasses the state management system), use CurrentState:

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.special.CurrentState;
import org.springframework.beans.factory.annotation.Autowired;

@TransitionSet(state = IslandState.class)
public class IslandTransitions {

    @Autowired
    private Action action;

    @Autowired
    private WorldState worldState;  // Reference to world state

    // Better approach - using CurrentState for re-entry
    @OutgoingTransition(
        activate = {CurrentState.class},  // Re-enter Island state
        pathCost = 0,
        description = "Capture new island (re-enter Island from World)"
    )
    public boolean captureNewIsland() {
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
- Toggling view modes

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.special.CurrentState;
import org.springframework.beans.factory.annotation.Autowired;

@TransitionSet(state = SearchResultsPage.class)
public class SearchResultsTransitions {

    @Autowired
    private Action action;

    private StateImage loadMoreButton;  // Defined elsewhere in the class
    private StateImage sortDropdown;    // Defined elsewhere in the class
    private StateImage priceOption;     // Defined elsewhere in the class

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
        // Note: First click opens dropdown; second click selects option
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

**When to use CurrentState transitions** (vs. helper methods):
- **Re-entering from overlapping states**: When transitioning from one active state back to another that remains active (e.g., Island → Island via World in [tutorial-basics](/docs/core-library/tutorials/tutorial-basics))
- **Actions requiring pathfinding**: When you need the framework to navigate to this state as part of a longer path
- **State-tracked operations**: When you want the state management system to track this as a transition
- **Page refreshes**: Operations that reload or refresh the entire page content
- **View mode changes**: Switching between different views of the same conceptual page (list/grid, compact/expanded)

**When to use helper methods instead**:
- **Direct manipulation**: Operations you call directly without needing pathfinding (pagination, sorting, filtering)
- **Multiple similar operations**: When you'd need many transitions to CurrentState (would confuse pathfinder)
- **Simple UI changes**: Basic interactions that don't warrant transition tracking (expand/collapse, show/hide)
- **Form interactions**: Typing, selecting, toggling within a form
- **Quick actions**: Operations that are too granular to be considered state transitions

**Rule of thumb**: If you're calling the method directly and don't need pathfinding to reach it, use a helper method. If the framework should be able to navigate to this operation as part of a path, use a CurrentState transition.

### ExpectedState - Runtime-Determined (Not Yet Implemented)

**Concept**: Would allow transitions where the target state is determined at runtime based on conditions like user preferences, roles, or application state.

**Current alternatives**: Most "expected state" scenarios can be handled today using:

1. **Multiple transitions with different path costs**:

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;

// Multiple ways to reach the same destination with different costs
// Brobot will prefer lower-cost paths when multiple options are available

@Autowired
private Action action;

private StateImage menuButton;       // Defined elsewhere in the class
private StateImage settingsMenuItem; // Defined elsewhere in the class
private StateImage homeButton;       // Defined elsewhere in the class
private StateImage profileIcon;      // Defined elsewhere in the class
private StateImage settingsLink;     // Defined elsewhere in the class

@OutgoingTransition(activate = {SettingsPage.class}, pathCost = 0)
public boolean settingsViaKeyboard() {
    // Fastest way - keyboard shortcut
    return action.type("{CTRL+,}").isSuccess();
}

@OutgoingTransition(activate = {SettingsPage.class}, pathCost = 3)
public boolean settingsViaMenu() {
    // Slower way - through menu system
    action.click(menuButton);
    return action.click(settingsMenuItem).isSuccess();
}

@OutgoingTransition(activate = {SettingsPage.class}, pathCost = 10)
public boolean settingsViaUrl() {
    // Slowest/most expensive - navigate via home then settings
    action.click(homeButton);
    action.click(profileIcon);
    return action.click(settingsLink).isSuccess();
}
```

2. **Conditional logic in transition methods**:

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.annotations.OutgoingTransition;

// user and performLogin() defined elsewhere in the class

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
        // Try to navigate to login page via home button or known element
        return action.click(homeButton).isSuccess();
    }
}

// INCORRECT: Never transition TO UnknownState
// @OutgoingTransition(activate = {UnknownState.class}) // ❌ DON'T DO THIS
```

### How the Framework Selects Recovery Paths

When the automation is in UnknownState and needs to reach a target state (e.g., DashboardState), the framework uses a sophisticated pathfinding algorithm:

**The Process:**
1. **Find All Paths**: The pathfinder discovers ALL possible paths from UnknownState to the target state
2. **Calculate Total Costs**: For each path, it sums up:
   - All state costs along the path
   - All transition costs along the path
3. **Sort by Cost**: Paths are sorted by their total cost
4. **Execute Lowest-Cost Path**: The framework attempts the path with the lowest total cost first

**Important:** The framework doesn't simply choose based on individual transition costs from UnknownState. It evaluates complete paths to the destination.

**Example Scenario:**

```java
// Suppose we want to reach DashboardState from UnknownState
// Two possible paths exist:

// Path A: Unknown → Home → Dashboard
//   - Unknown → Home transition: pathCost = 12
//   - Home → Dashboard transition: pathCost = 5
//   - Total: 17

// Path B: Unknown → Login → Dashboard
//   - Unknown → Login transition: pathCost = 10
//   - Login → Dashboard transition: pathCost = 8
//   - Total: 18

// The framework chooses Path A (total cost 17) even though the first
// transition from Unknown to Login has a lower individual cost (10).
```

**Key Insight:** Even though `recoverToLogin()` has a lower cost (10) than `recoverToHome()` (12), the framework might still choose the home path if subsequent transitions make it more efficient overall. The pathfinding algorithm considers the **complete journey**, not just the first step.

**Practical Implications:**
- Design your transition costs considering the full navigation graph
- Lower-cost recovery transitions don't guarantee selection if they lead to longer overall paths
- The framework will try alternative paths if the lowest-cost path fails

## Complete Working Example: Special States

The following is a complete, tested example from the special-states-example project that demonstrates PreviousState and CurrentState in action. The full project is available at `examples/03-core-library/guides/dynamic-transitions/special-states-example/`.

### Understanding the Hidden State Mechanism

For PreviousState transitions to work, the framework needs to know which states can be hidden by the covering state. This is configured in the State definition using `.canHide()`:

**How it works:**
1. **State Definition**: The covering state (e.g., ModalDialog) declares which states it can hide using `.canHide("StateName1", "StateName2")`
2. **Transition Occurs**: When transitioning TO the covering state with `staysVisible = true` on the source state, the framework checks if any currently active states are in the covering state's `canHide` list
3. **States Hidden**: Matching active states are moved from "active" to "hidden" status
4. **PreviousState Transition**: When the covering state executes a transition with `activate = {PreviousState.class}`, the framework returns to the hidden state

**Important:** You must list ALL states that this state might potentially hide. If a state isn't in the `canHide` list, it won't be tracked as hidden, and PreviousState transitions won't be able to return to it.

### Modal Dialog State with PreviousState

The modal dialog state must specify which states it can hide using `canHide()` in the State.Builder:

**Example (Complete Runnable Class):**
```java
package com.example.specialstates.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Modal dialog state that overlays other pages. It specifies which states can be hidden
 * when the modal appears.
 */
@Component
@Getter
@Slf4j
public class ModalDialogState {

    private final State state;
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

        // IMPORTANT: Specify which states this modal can hide
        state = new State.Builder("ModalDialog")
                .withImages(dialogTitle, confirmButton, cancelButton, closeButton)
                .canHide("MainPage", "SettingsPage")  // List all states that can be hidden
                .build();
    }
}
```

### Modal Dialog Transitions Using PreviousState

**Example (Complete Runnable Class):**
```java
package com.example.specialstates.transitions;

import org.springframework.stereotype.Component;

import com.example.specialstates.states.ModalDialogState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.objects.ObjectCollection;
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
        return action.find(modalDialogState.getDialogTitle()).isSuccess();
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Close modal and return to previous state")
    public boolean closeModal() {
        // Use ObjectCollection to accept either confirm or cancel button
        ObjectCollection closeButtons = new ObjectCollection.Builder()
                .withImages(modalDialogState.getConfirmButton(),
                           modalDialogState.getCancelButton())
                .build();
        return action.click(closeButtons).isSuccess();
    }
}
```

### Main Page with CurrentState Self-Transitions

**Example (Complete Runnable Class):**
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
        return action.find(mainPageState.getMainContent()).isSuccess();
    }

    @OutgoingTransition(
            activate = {ModalDialogState.class},
            staysVisible = true, // MainPage stays visible behind modal
            pathCost = 0,
            description = "Open modal dialog over main page")
    public boolean openModal() {
        return action.click(mainPageState.getOpenModalButton()).isSuccess();
    }

    @OutgoingTransition(
            activate = {SettingsPageState.class},
            pathCost = 1,
            description = "Navigate to settings page")
    public boolean toSettings() {
        return action.click(mainPageState.getSettingsButton()).isSuccess();
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 2,
            description = "Refresh main page")
    public boolean refresh() {
        return action.click(mainPageState.getRefreshButton()).isSuccess();
    }
}
```

### Data Grid Helper Methods (Not Transitions)

For data grid operations like pagination, sorting, and filtering that don't change the conceptual state, use regular helper methods instead of transitions:

**Example (Conceptual Snippet):**
```java
@TransitionSet(state = DataGridState.class)
@Component
public class DataGridTransitions {
    private final DataGridState grid;
    private final Action action;

    @IncomingTransition
    public boolean verifyArrival() {
        return action.find(grid.getDataTable()).isSuccess();
    }

    // These are helper methods, NOT @OutgoingTransition
    // They manipulate the grid but don't trigger pathfinding

    /**
     * Navigate to next page of results.
     * Call directly when you need pagination.
     */
    public boolean nextPage() {
        return action.click(grid.getNextPageButton()).isSuccess();
    }

    /**
     * Navigate to previous page of results.
     */
    public boolean previousPage() {
        return action.click(grid.getPrevPageButton()).isSuccess();
    }

    /**
     * Sort data by clicking column header.
     */
    public boolean sortByColumn() {
        return action.click(grid.getColumnHeader()).isSuccess();
    }

    /**
     * Apply filter to the data grid.
     */
    public boolean applyFilter(String filterText) {
        action.click(grid.getFilterInput());
        action.type(filterText);
        return action.click(grid.getApplyButton()).isSuccess();
    }

    // If you need to navigate AWAY from the data grid, use @OutgoingTransition
    @OutgoingTransition(activate = {DashboardState.class}, pathCost = 1)
    public boolean backToDashboard() {
        return action.click(grid.getBackButton()).isSuccess();
    }
}
```

**Why not use CurrentState transitions?**
- These operations don't need pathfinding - you call them directly
- Having multiple transitions to the same destination (CurrentState) would confuse the pathfinder
- Helper methods are simpler and more appropriate for UI manipulation within a state

### Multi-Level Overlays

For multi-level overlays, each covering state must specify what it can hide:

**Example (Complete Runnable Classes):**
```java
// Settings page - base layer
@Component
@Getter
public class SettingsPageState {
    private final State state;
    private final StateImage settingsPanel;

    public SettingsPageState() {
        settingsPanel = new StateImage.Builder()
                .addPatterns("settingsPanel")
                .setName("settingsPanel")
                .build();

        state = new State.Builder("SettingsPage")
                .withImages(settingsPanel)
                // Settings doesn't hide anything - it's a base page
                .build();
    }
}

// Menu can cover Settings (and other pages)
@Component
@Getter
public class MenuOverlayState {
    private final State state;
    private final StateImage menuContainer;
    private final StateImage closeButton;

    public MenuOverlayState() {
        menuContainer = new StateImage.Builder()
                .addPatterns("menuContainer")
                .setName("menuContainer")
                .build();

        closeButton = new StateImage.Builder()
                .addPatterns("menuCloseBtn")
                .setName("closeButton")
                .build();

        state = new State.Builder("MenuOverlay")
                .withImages(menuContainer, closeButton)
                .canHide("SettingsPage", "HomePage", "ProfilePage")  // Can cover multiple pages
                .build();
    }
}

// Help dialog can cover the Menu
@Component
@Getter
public class HelpDialogState {
    private final State state;
    private final StateImage dialogTitle;
    private final StateImage closeButton;

    public HelpDialogState() {
        dialogTitle = new StateImage.Builder()
                .addPatterns("helpDialogTitle")
                .setName("dialogTitle")
                .build();

        closeButton = new StateImage.Builder()
                .addPatterns("helpCloseBtn", "xButton")
                .setName("closeButton")
                .build();

        state = new State.Builder("HelpDialog")
                .withImages(dialogTitle, closeButton)
                .canHide("MenuOverlay")  // Can cover the menu
                .build();
    }
}

// Transitions maintain the hidden state stack
@TransitionSet(state = HelpDialogState.class)
@Component
@RequiredArgsConstructor
public class HelpDialogTransitions {
    private final HelpDialogState helpDialogState;
    private final Action action;

    @OutgoingTransition(activate = {PreviousState.class}, pathCost = 0)
    public boolean closeHelp() {
        // Returns to MenuOverlay (which is covering SettingsPage)
        return action.click(helpDialogState.getCloseButton()).isSuccess();
    }
}

@TransitionSet(state = MenuOverlayState.class)
@Component
@RequiredArgsConstructor
public class MenuTransitions {
    private final MenuOverlayState menuOverlayState;
    private final Action action;

    @OutgoingTransition(activate = {PreviousState.class}, pathCost = 0)
    public boolean closeMenu() {
        // Returns to SettingsPage (or whichever page was covered)
        return action.click(menuOverlayState.getCloseButton()).isSuccess();
    }
}
```

**Stack Visualization:**
```
User opens Settings → Menu → Help

Active: HelpDialog
Hidden by Help: MenuOverlay
Hidden by Menu: SettingsPage

User closes Help (PreviousState) → MenuOverlay becomes active
User closes Menu (PreviousState) → SettingsPage becomes active
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

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.special.PreviousState;
import org.springframework.beans.factory.annotation.Autowired;

@TransitionSet(state = MenuState.class)
public class MenuTransitions {

    @Autowired
    private Action action;

    private StateImage closeButton;  // Defined elsewhere in the class
    private StateImage homeButton;   // Defined elsewhere in the class

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
    private StateImage dialogHeader;  
    private StateImage darkOverlay;   
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
    // Detects when activate = {PreviousState.class} or {CurrentState.class}
    // Sets special state IDs (-2L, -3L) instead of regular state names
    // Enables dynamic resolution at runtime
}
```

### StateVisibilityManager

Manages the conversion of active states to hidden states during transitions:

```java
@Component
public class StateVisibilityManager {
    // Invoked when a new state becomes active
    public boolean set(Long stateToSet) {
        // Examines currently active states
        // Moves states to hidden if they match the new state's canHide list
        // Removes hidden states from StateMemory's active list
        return true;
    }
}
```

### StateMemory

Maintains runtime memory of currently active states:

```java
@Component
public class StateMemory {
    // Tracks which states are currently active
    // Provides active state list for visibility decisions
    // Updated when states are hidden or activated
}
```

## Troubleshooting

### Hidden State Not Found

**Symptom**: PreviousState transition fails or pathfinding can't find a route back.

**Common Error Messages**:
```
WARN: No hidden states found for PreviousState transition
ERROR: Cannot resolve PreviousState - no states are currently hidden
INFO: Attempting fallback transition to HomePage.class
```

**Solutions**:
1. **Check `canHide` configuration**: Ensure the covering state's `.canHide()` list includes the state name
   ```java
   // ✅ Correct - state name without "State" suffix
   state = new State.Builder("ModalDialog")
           .canHide("MainPage", "SettingsPage")
           .build();

   // ❌ Wrong - using class names
   state = new State.Builder("ModalDialog")
           .canHide("MainPageState", "SettingsPageState")  // Won't work!
           .build();
   ```

2. **Verify state names match**: The names in `.canHide()` must exactly match the state names in State.Builder
   - With `@State` annotation: state name = class name minus "State" suffix
   - `MainPageState` class → state name is `"MainPage"`

3. **Check `staysVisible` flag**: Source state must have `staysVisible = true` in the transition
   ```java
   @OutgoingTransition(
       activate = {ModalDialogState.class},
       staysVisible = true,  // ← Required for state to be tracked as hidden
       pathCost = 0
   )
   ```

4. **Verify state order**: The state must be active BEFORE the overlay opens
   ```
   Correct flow:
   MainPage (active) → Open Modal (MainPage becomes hidden) → Close Modal (returns to MainPage)

   Wrong flow:
   No active state → Open Modal → Close Modal (no previous state to return to)
   ```

5. **Check logs for state transition history**:
   ```
   INFO: State transition: MainPage → ModalDialog
   INFO: Hidden states: [MainPage]
   INFO: Active states: [ModalDialog]
   ```

### Self-Transitions Not Working

**Symptom**: CurrentState transitions don't execute or pathfinding fails to find self-loop paths.

**Common Error Messages**:
```
WARN: CurrentState transition succeeded but state verification failed
ERROR: Expected to remain in SearchResultsPage but now in UnknownState
INFO: State detection changed after CurrentState action
```

**Solutions**:
1. **Verify the action completes successfully**:
   ```java
   @OutgoingTransition(activate = {CurrentState.class}, pathCost = 2)
   public boolean loadMoreResults() {
       ActionResult result = action.click(loadMoreButton);
       if (!result.isSuccess()) {
           log.error("Load more button click failed");
           return false;
       }
       return true;
   }
   ```

2. **Check state detection after the action**:
   - UI changes from the action shouldn't trigger different state detection
   - State images should still be visible after pagination/sorting/refresh
   - Test: Does `@IncomingTransition verifyArrival()` still pass after the action?

3. **Ensure StateImage stability**:
   ```java
   // ✅ Good - Header persists through pagination
   StateImage pageHeader = new StateImage.Builder()
           .addPatterns("searchHeader")  // Visible on all pages
           .build();

   // ❌ Bad - Only exists on page 1
   StateImage page1Indicator = new StateImage.Builder()
           .addPatterns("page1dot")  // Disappears when loading page 2
           .build();
   ```

4. **Consider using helper methods instead**:
   - If you have many similar CurrentState transitions (10+ pagination/sort options)
   - This can confuse the pathfinder with too many self-loops
   - Helper methods are better for direct manipulation

### Pathfinding Chooses Unexpected Route

**Symptom**: Framework picks a longer path instead of expected PreviousState transition.

**Common Error Messages**:
```
INFO: Found 3 paths to DashboardState
INFO: Selected path: UnknownState → LoginPage → Dashboard (cost: 18)
INFO: Alternative path available: UnknownState → HomePage → Dashboard (cost: 20)
```

**Solutions**:
1. **Review total path costs**: Pathfinding sums ALL transition costs in the complete path
   ```
   Path A: Unknown(12) → Home(5) → Dashboard = 17 total ← CHOSEN
   Path B: Unknown(10) → Login(8) → Dashboard = 18 total
   ```

2. **Adjust path costs strategically**:
   - PreviousState transitions: typically pathCost = 0 (instant, no navigation)
   - CurrentState self-loops: typically pathCost = 0-5 (in-page actions)
   - Standard transitions: pathCost = 1-10 (varies by complexity)
   - Recovery transitions: pathCost = 10-20 (fallback only)

3. **Check for missing `canHide` entries**:
   - If state isn't in `canHide` list, PreviousState can't target it
   - Framework will choose alternative path instead

4. **Enable pathfinding debug logs**:
   ```properties
   logging.level.io.github.jspinak.brobot.pathfinding=DEBUG
   ```
   This shows all discovered paths and cost calculations

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
- [State-Aware Scheduling](/docs/core-library/user-guides/state-aware-scheduling) - Advanced state scheduling patterns
- [Pathfinding](/docs/getting-started/core-concepts#pathfinding) - How Brobot calculates optimal paths between states
- [ActionConfig API](/docs/core-library/action-config/overview) - Type-safe action configuration in Brobot 1.1.0+