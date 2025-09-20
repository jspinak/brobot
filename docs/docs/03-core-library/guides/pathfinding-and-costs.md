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

```java
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

```java
@TransitionSet(state = HomePage.class)
public class HomePageTransitions {

    @OutgoingTransition(to = ProfilePage.class)  // Default pathCost = 1
    public boolean toProfile() {
        // Normal transition with default cost
        return action.click(profileLink).isSuccess();
    }

    @OutgoingTransition(
        to = ProfilePage.class,
        pathCost = 0  // Free transition
    )
    public boolean quickProfile() {
        // Keyboard shortcut - no cost
        return action.type("{CTRL+P}").isSuccess();
    }

    @OutgoingTransition(
        to = ProfilePage.class,
        pathCost = 10  // Expensive fallback
    )
    public boolean toProfileViaMenu() {
        // Slower route through menu - discouraged
        action.click(menuButton);
        action.click(profileMenuItem);
        return true;
    }
}
```

## Path Selection Algorithm

Brobot's pathfinding follows these rules:

1. **Find all possible paths** from current state(s) to target state
2. **Calculate total cost** for each path
3. **Select the path with lowest cost**
4. **If costs are equal**, prefer the path with fewer transitions
5. **Execute the selected path**

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

```java
@TransitionSet(state = SearchPage.class)
public class SearchTransitions {

    @OutgoingTransition(
        to = ResultsPage.class,
        pathCost = 1  // Preferred: direct search
    )
    public boolean search() {
        return action.click(searchButton).isSuccess();
    }

    @OutgoingTransition(
        to = ResultsPage.class,
        pathCost = 10  // Fallback: advanced search
    )
    public boolean advancedSearch() {
        action.click(advancedButton);
        // ... more complex steps
        return action.click(searchButton).isSuccess();
    }
}
```

### 2. Free Transitions for Instant Operations

```java
@OutgoingTransition(
    to = CurrentState.class,
    pathCost = 0  // Free - no actual navigation
)
public boolean refresh() {
    // Instant operation, no cost
    return action.type("{F5}").isSuccess();
}

@OutgoingTransition(
    to = PreviousState.class,
    pathCost = 0  // Free - closing overlay
)
public boolean closeModal() {
    // Modal closes instantly, no navigation cost
    return action.type("{ESC}").isSuccess();
}
```

### 3. Expensive States to Avoid

```java
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

```java
@TransitionSet(state = LoginPage.class)
public class LoginTransitions {

    @OutgoingTransition(to = Dashboard.class, pathCost = 1)
    public boolean normalLogin() {
        // First attempt - lowest cost
        return performLogin();
    }

    @OutgoingTransition(to = Dashboard.class, pathCost = 5)
    public boolean retryLogin() {
        // Second attempt - higher cost
        clearFields();
        return performLogin();
    }

    @OutgoingTransition(to = Dashboard.class, pathCost = 20)
    public boolean recoveryLogin() {
        // Last resort - highest cost
        refreshPage();
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

```properties
# application.properties
brobot.pathfinding.logging=DEBUG
brobot.pathfinding.show-costs=true
brobot.pathfinding.show-all-paths=true
```

### Sample Debug Output

```
[PathFinder] Finding path from HomePage to SettingsPage
[PathFinder] Found 3 possible paths:
  Path 1: HomePage(1) -> [direct](1) -> SettingsPage(1) = Cost: 3
  Path 2: HomePage(1) -> [menu](2) -> Menu(1) -> [select](1) -> SettingsPage(1) = Cost: 6
  Path 3: HomePage(1) -> [shortcut](0) -> SettingsPage(1) = Cost: 2
[PathFinder] Selected Path 3 (lowest cost: 2)
```

## Advanced Scenarios

### Dynamic Cost Adjustment

While Brobot doesn't support runtime cost changes, you can achieve similar effects with multiple transitions:

```java
@TransitionSet(state = DataPage.class)
public class DataPageTransitions {

    @Autowired
    private SystemLoad systemLoad;

    @OutgoingTransition(to = ReportPage.class, pathCost = 1)
    public boolean fastGenerate() {
        if (systemLoad.isHigh()) return false;  // Fail if load is high
        return generateReport();
    }

    @OutgoingTransition(to = ReportPage.class, pathCost = 10)
    public boolean slowGenerate() {
        // Always works but with higher cost
        return generateReport();
    }
}
```

### Cost-Based Load Balancing

Distribute load across multiple paths:

```java
@State(pathCost = 1)
public class Server1Page { }

@State(pathCost = 2)  // Slightly discourage
public class Server2Page { }

@State(pathCost = 3)  // Further discourage
public class Server3Page { }

// Brobot naturally prefers Server1, falls back to others
```

## Migration Guide

If upgrading from versions before 1.1.0:

### Old Defaults (Pre-1.1.0)
- State pathCost: 1
- Transition pathCost: 0

### New Defaults (1.3.0+)
- State pathCost: 1
- Transition pathCost: 1

### Migration Steps

1. **Review existing pathCost = 0 transitions** - These are now explicitly free (intentional)

2. **Check path selection changes** - Paths may change due to different default costs

3. **Update tests** - Path selection tests may need updating

4. **Explicit costs for critical paths** - Add explicit pathCost values where path selection is critical:

```java
// Before (relied on default 0)
@OutgoingTransition(to = CriticalState.class)

// After (explicit to ensure preference)
@OutgoingTransition(to = CriticalState.class, pathCost = 0)
```

## Summary

The path cost system in Brobot provides fine-grained control over navigation paths:

- **Default costs of 1** make calculations intuitive
- **Total cost = Sum of state costs + Sum of transition costs**
- **Lower costs are preferred** in pathfinding
- **Use 0 for free operations**, higher values for expensive ones
- **Consistent cost assignment** leads to predictable navigation

By understanding and properly configuring path costs, you can ensure your automation takes the most efficient routes through your application's state space.