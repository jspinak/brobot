---
sidebar_position: 8
title: 'Pathfinding and Path Costs'
---

# Pathfinding and Path Costs

## Overview

Brobot uses a cost-based pathfinding system to navigate between states. When multiple paths exist between two states, Brobot automatically selects the path with the lowest total cost. Understanding how path costs work is essential for building efficient and predictable automation.

## Default Path Costs

As of version 1.1.0, Brobot uses the following defaults:

| Component | Default Cost | Description |
|-----------|-------------|-------------|
| **State** | 1 | Cost of being in a state |
| **Transition** | 1 | Cost of executing a transition |

### Why Default to 1?

The default of 1 (instead of 0) makes cost calculations more intuitive:
- Every state visit and transition has a base cost
- The total cost naturally represents the complexity of a path
- You can explicitly set 0 for "free" states or transitions
- Higher values (5, 10, 20+) clearly indicate expensive operations

## How Path Costs Are Calculated

The total cost of a path is the sum of:
1. **All state costs** in the path
2. **All transition costs** in the path

### Formula

```
Total Path Cost = Σ(State Costs) + Σ(Transition Costs)
```

### Example Calculation

Consider navigating from `LoginPage` to `UserDashboard`:

**Path 1: Direct Route**
```
LoginPage (cost: 1)
  → [transition: login] (cost: 1)
Dashboard (cost: 1)

Total = 1 + 1 + 1 = 3
```

**Path 2: Through Welcome Screen**
```
LoginPage (cost: 1)
  → [transition: login] (cost: 1)
WelcomeScreen (cost: 1)
  → [transition: continue] (cost: 2)
Dashboard (cost: 1)

Total = 1 + 1 + 1 + 2 + 1 = 6
```

**Result**: Path 1 is chosen (cost: 3 < 6)

## Setting Custom Path Costs

### State Path Costs

Set in the `@State` annotation:

**Example (Conceptual Snippets):**
```java
import io.github.jspinak.brobot.annotations.State;

@State  // Default pathCost = 1
public class NormalPage {
    // Standard page with default cost
}

@State(pathCost = 0)  // Free state
public class SplashScreen {
    // No cost to be in this state (e.g., automatic/transient states)
}

@State(pathCost = 5)  // Expensive state
public class SlowLoadingPage {
    // Higher cost discourages routing through this state
}

@State(pathCost = 10)  // Very expensive
public class ErrorRecoveryState {
    // High cost - only use as last resort
}
```

### Transition Path Costs

Set in the `@OutgoingTransition` annotation:

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@TransitionSet(state = HomePage.class)
@Component
public class HomePageTransitions {

    @Autowired
    private Action action;

    @Autowired
    private HomePage homePage;  // Contains profileLink, menuButton, etc.

    @OutgoingTransition(activate = {ProfilePage.class})  // Default pathCost = 1
    public boolean toProfile() {
        // Normal transition with default cost
        return action.click(homePage.getProfileLink()).isSuccess();
    }

    @OutgoingTransition(
        activate = {ProfilePage.class},
        pathCost = 0  // Free transition
    )
    public boolean quickProfile() {
        // Keyboard shortcut - no cost
        return action.type("{CTRL+P}").isSuccess();
    }

    @OutgoingTransition(
        activate = {ProfilePage.class},
        pathCost = 10  // Expensive fallback
    )
    public boolean toProfileViaMenu() {
        // Slower route through menu - discouraged
        action.click(homePage.getMenuButton());
        action.click(homePage.getProfileMenuItem());
        return true;
    }
}
```

## Path Selection Algorithm

Brobot's pathfinding follows these rules:

1. **Find all possible paths** from current state(s) to target state
2. **Calculate total cost** for each path
3. **Sort paths by total cost** (ascending order)
4. **Select and execute the lowest cost path**
5. **If the selected path fails**, try the next lowest cost path

### Path Selection Example

Consider multiple routes from `HomePage` to `SettingsPage`:

```java
// Path A: Direct navigation
HomePage (1) → [click settings] (1) → SettingsPage (1)
Total: 3

// Path B: Through menu
HomePage (1) → [open menu] (2) → Menu (1) → [click settings] (1) → SettingsPage (1)
Total: 6

// Path C: Keyboard shortcut
HomePage (1) → [press Alt+S] (0) → SettingsPage (1)
Total: 2
```

**Winner**: Path C (cost: 2) - The keyboard shortcut wins due to its 0-cost transition

## Common Path Cost Patterns

### 1. Preferred vs Fallback Routes

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import org.springframework.beans.factory.annotation.Autowired;

@TransitionSet(state = SearchPage.class)
public class SearchTransitions {

    @Autowired
    private Action action;

    @Autowired
    private SearchPage searchPage;  // Contains searchButton, advancedButton

    @OutgoingTransition(
        activate = {ResultsPage.class},
        pathCost = 1  // Preferred: direct search
    )
    public boolean search() {
        return action.click(searchPage.getSearchButton()).isSuccess();
    }

    @OutgoingTransition(
        activate = {ResultsPage.class},
        pathCost = 10  // Fallback: advanced search
    )
    public boolean advancedSearch() {
        action.click(searchPage.getAdvancedButton());
        // ... more complex steps
        return action.click(searchPage.getSearchButton()).isSuccess();
    }
}
```

### 2. Free Transitions for Instant Operations

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.model.state.special.CurrentState;
import io.github.jspinak.brobot.model.state.special.PreviousState;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private Action action;

@OutgoingTransition(
    activate = {CurrentState.class},
    pathCost = 0  // Free - no actual navigation
)
public boolean refresh() {
    // Instant operation, no cost
    return action.type("{F5}").isSuccess();
}

@OutgoingTransition(
    activate = {PreviousState.class},
    pathCost = 0  // Free - closing overlay
)
public boolean closeModal() {
    // Modal closes instantly, no navigation cost
    return action.type("{ESC}").isSuccess();
}
```

### 3. Expensive States to Avoid

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.annotations.State;

@State(pathCost = 20)  // Very expensive
public class MaintenancePage {
    // High cost prevents routing through this state
    // Only accessed when explicitly targeted
}

@State(pathCost = 100)  // Prohibitive cost
public class CrashRecoveryState {
    // Emergency state - never route through
    // Only used for error recovery
}
```

### 4. Progressive Cost Increase for Retries

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

@TransitionSet(state = LoginPage.class)
public class LoginTransitions {

    @OutgoingTransition(activate = {Dashboard.class}, pathCost = 1)
    public boolean normalLogin() {
        // First attempt - lowest cost
        return performLogin();  // Your implementation
    }

    @OutgoingTransition(activate = {Dashboard.class}, pathCost = 5)
    public boolean retryLogin() {
        // Second attempt - higher cost
        clearFields();  // Your implementation
        return performLogin();
    }

    @OutgoingTransition(activate = {Dashboard.class}, pathCost = 20)
    public boolean recoveryLogin() {
        // Last resort - highest cost
        refreshPage();  // Your implementation
        clearCookies();
        return performLogin();
    }
}
```

## Cost Guidelines

### Recommended Cost Ranges

| Cost | Use Case | Example |
|------|----------|---------|
| **0** | Free/instant operations | Keyboard shortcuts, closing overlays, already visible elements |
| **1** | Standard operations (default) | Normal clicks, typical navigation |
| **2-4** | Slightly slower operations | Operations with animations, short waits |
| **5-9** | Noticeably slower operations | Multi-step processes, operations with loading |
| **10-19** | Fallback routes | Alternative paths when primary fails |
| **20-49** | Recovery operations | Error recovery, cleanup paths |
| **50-99** | Last resort operations | Major recovery, restart sequences |
| **100+** | Prohibitive | States/transitions to avoid unless explicitly required |

### Best Practices

1. **Keep default costs for normal operations** - Most states and transitions should use the default cost of 1

2. **Use 0 for truly free operations** - Only when there's no time cost or complexity:
   - Keyboard shortcuts that instantly navigate
   - Closing overlays with ESC
   - States that are transient/automatic

3. **Reserve high costs for fallbacks** - Use 10+ for alternative routes that should only be used when necessary

4. **Be consistent across your application** - Similar operations should have similar costs

5. **Consider total path cost** - Remember that costs accumulate across the entire path

## Debugging Path Selection

To understand why Brobot chose a particular path:

### Enable Path Logging

Brobot uses standard SLF4J logging. Enable DEBUG level for pathfinding classes:

**Option 1: application.properties**
```properties
# application.properties
logging.level.io.github.jspinak.brobot.navigation.path=DEBUG
```

**Option 2: logback.xml**
```xml
<logger name="io.github.jspinak.brobot.navigation.path" level="DEBUG"/>
```

### Sample Debug Output

```
INFO  - Finding path from [HomePage] to SettingsPage
INFO  - Found 3 path(s) from [HomePage] to SettingsPage
INFO  - Paths found (3)
DEBUG - Path 1: HomePage -> Menu -> SettingsPage (cost: 6)
DEBUG - Path 2: HomePage -> SettingsPage (cost: 3)
DEBUG - Path 3: HomePage -> SettingsPage (cost: 2, via keyboard shortcut)
INFO  - Best path (score 2): HomePage -> SettingsPage
```

## Advanced Scenarios

### Dynamic Cost Adjustment

While Brobot doesn't support runtime cost changes, you can achieve similar effects with multiple transitions:

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import org.springframework.beans.factory.annotation.Autowired;

@TransitionSet(state = DataPage.class)
public class DataPageTransitions {

    @Autowired
    private SystemLoad systemLoad;  // Your custom service

    @OutgoingTransition(activate = {ReportPage.class}, pathCost = 1)
    public boolean fastGenerate() {
        if (systemLoad.isHigh()) return false;  // Fail if load is high
        return generateReport();  // Your implementation
    }

    @OutgoingTransition(activate = {ReportPage.class}, pathCost = 10)
    public boolean slowGenerate() {
        // Always works but with higher cost
        return generateReport();
    }
}
```

### Cost-Based Load Balancing

Distribute load across multiple paths:

**Example (Conceptual Snippet):**
```java
import io.github.jspinak.brobot.annotations.State;

@State(pathCost = 1)
public class Server1Page { }

@State(pathCost = 2)  // Slightly discourage
public class Server2Page { }

@State(pathCost = 3)  // Further discourage
public class Server3Page { }

// Brobot naturally prefers Server1, falls back to others
```

## Migration Guide

If upgrading to version 1.1.0 or later:

### Version 1.1.0 Changes

**New Defaults:**
- State pathCost: 1 (unchanged)
- Transition pathCost: 1 (changed from 0 in pre-1.1.0 versions)

### Migration Steps

1. **Review existing pathCost = 0 transitions** - These are now explicitly free (intentional)

2. **Check path selection changes** - Paths may change due to different default transition costs

3. **Update tests** - Path selection tests may need updating

4. **Explicit costs for critical paths** - Add explicit pathCost values where path selection is critical:

```java
// Before (relied on old default 0 for transitions)
@OutgoingTransition(activate = {CriticalState.class})

// After (explicit to ensure it remains free)
@OutgoingTransition(activate = {CriticalState.class}, pathCost = 0)
```

## Summary

The path cost system in Brobot provides fine-grained control over navigation paths:

- **Default costs of 1** make calculations intuitive
- **Total cost = Sum of state costs + Sum of transition costs**
- **Lower costs are preferred** in pathfinding
- **Use 0 for free operations**, higher values for expensive ones
- **Consistent cost assignment** leads to predictable navigation

By understanding and properly configuring path costs, you can ensure your automation takes the most efficient routes through your application's state space.

## Related Documentation

- **[States Guide](/docs/getting-started/states)** - Understanding states and the @State annotation
- **[Transitions Guide](/docs/getting-started/transitions)** - Defining transitions between states
- **[Pathfinding & Multi-State Activation](/docs/getting-started/pathfinding)** - Introduction to pathfinding concepts
- **[Annotations Guide](/docs/core-library/guides/user-guides/annotations)** - Complete reference for @State, @TransitionSet, and transition annotations
- **[Dynamic Transitions and Hidden States](/docs/core-library/guides/user-guides/dynamic-transitions)** - Using PreviousState and CurrentState special markers
- **[Core Concepts](/docs/getting-started/core-concepts)** - Overview of Brobot's architecture

## Example Projects

See path costs in action in these tutorials:
- **[Tutorial Basics](/docs/core-library/tutorials/tutorial-basics)** - Basic state and transition setup
- **[Special States Tutorial](/docs/core-library/tutorials/tutorial-special-states)** - Using CurrentState and PreviousState with path costs