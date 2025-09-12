---
sidebar_position: 4
title: 'Transitions'
---

# Transitions

## Introduction

While States define "where you can be" in a GUI, **Transitions** define "how you get there." Every State that is reachable needs an associated transitions class that defines the pathways to and from other states. 

Formally, a transition is a process or a sequence of actions that changes the GUI from one state to another. They form the "edges" of the state graph and are the building blocks used by the framework's pathfinder to navigate the application. 

## The Brobot Implementation: FromTransition and ToTransition

Brobot implements this concept by splitting transitions into two types: **FromTransitions** and **ToTransitions**. This two-part system provides a clear and reusable structure for managing navigation logic.

![Transition Diagram](/img/paper/transitions.png) 
_This diagram is based on Figure 8 from the research paper._ 

* **FromTransition**: This handles the process of leaving another state to arrive at *this* state. For example, to go from `State A` to `State B`, the FromTransition in `BTransitions` defines how to navigate from A. It contains the specific actions needed to trigger the move from State A.

* **ToTransition**: This handles the verification of arriving at a state, regardless of which state started the process. There can be multiple FromTransitions coming to `State B` from different states, but there is only one ToTransition for `State B`. This verification contains checks to confirm `State B` is active.

## Modern Approach: Using @TransitionSet Annotation

Brobot 1.2.0+ uses a cohesive, method-level annotation approach that groups all transitions for a state in one class. This maintains high cohesion while providing clear, annotation-based configuration.

### Complete Example

```java
@TransitionSet(state = PricingState.class, description = "Pricing page transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class PricingTransitions {
    
    private final MenuState menuState;
    private final HomepageState homepageState;
    private final PricingState pricingState;
    private final Action action;
    
    /**
     * Navigate from Menu to Pricing by clicking the pricing menu item.
     */
    @FromTransition(from = MenuState.class, priority = 1, description = "Navigate from Menu to Pricing")
    public boolean fromMenu() {
        log.info("Navigating from Menu to Pricing");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        return action.click(menuState.getPricingButton()).isSuccess();
    }
    
    /**
     * Navigate from Homepage to Pricing.
     */
    @FromTransition(from = HomepageState.class, priority = 2, description = "Navigate from Homepage to Pricing")
    public boolean fromHomepage() {
        log.info("Navigating from Homepage to Pricing");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        return action.click(homepageState.getPricingLink()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the Pricing state.
     * Checks for the presence of pricing-specific elements.
     */
    @ToTransition(description = "Verify arrival at Pricing state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Pricing state");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        boolean found = action.find(pricingState.getStartForFreeButton()).isSuccess();
        
        if (found) {
            log.info("Successfully confirmed Pricing state is active");
            return true;
        } else {
            log.error("Failed to confirm Pricing state - button not found");
            return false;
        }
    }
}
```

### Annotation Types

#### @TransitionSet
Marks a class as containing all transitions for a specific state:
- **state**: The state class these transitions belong to (required)
- **name**: Optional state name override (defaults to class name without "State" suffix)
- **description**: Documentation for the transition set

#### @FromTransition
Defines a transition FROM another state TO this state:
- **from**: The source state class (required)
- **priority**: Transition priority - higher values are preferred when multiple paths exist (default: 0)
- **description**: Documentation for this transition
- **timeout**: Timeout in seconds (optional)

#### @ToTransition
Verifies successful arrival at the state:
- **description**: Documentation for the verification
- **timeout**: Verification timeout in seconds (optional)
- **required**: Whether verification must succeed (default: false)

## World State Example

Here's another complete example showing transitions for a World state with multiple entry and exit points:

```java
@TransitionSet(state = WorldState.class, description = "World map transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldTransitions {
    
    private final HomeState homeState;
    private final WorldState worldState;
    private final IslandState islandState;
    private final Action action;
    
    /**
     * Navigate from Home to World by clicking the world button.
     */
    @FromTransition(from = HomeState.class, priority = 1, description = "Navigate from Home to World")
    public boolean fromHome() {
        log.info("Navigating from Home to World");
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
    
    /**
     * Navigate from Island back to World.
     */
    @FromTransition(from = IslandState.class, priority = 2, description = "Return from Island to World")
    public boolean fromIsland() {
        log.info("Navigating from Island to World");
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Click back button or world map icon
        return action.click(islandState.getBackToWorldButton()).isSuccess();
    }
    
    /**
     * Verify arrival at World state by checking for the world map.
     */
    @ToTransition(description = "Verify arrival at World state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at World state");
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for world-specific elements
        boolean foundMap = action.find(worldState.getWorldMap()).isSuccess();
        boolean foundIslands = action.find(worldState.getIsland1()).isSuccess();
        
        if (foundMap || foundIslands) {
            log.info("Successfully confirmed World state is active");
            return true;
        } else {
            log.error("Failed to confirm World state - world elements not found");
            return false;
        }
    }
}
```

## Key Benefits of @TransitionSet

1. **High Cohesion**: All transitions for a state in ONE class - easy to find and maintain
2. **Clear Separation**: FromTransitions handle navigation logic, ToTransition verifies arrival
3. **Natural Organization**: File structure mirrors state structure (one transitions class per state)
4. **Spring Integration**: Full dependency injection support with @Component
5. **Type Safety**: Class-based state references prevent typos and enable IDE refactoring
6. **Mock Mode Support**: Easy to add testing support with framework settings check
7. **Reduced Boilerplate**: Method-level annotations are more concise than builders

## File Organization

Organize transition classes alongside state classes for clarity:

```
src/main/java/com/example/app/
├── states/
│   ├── HomeState.java
│   ├── WorldState.java
│   ├── IslandState.java
│   └── PricingState.java
└── transitions/
    ├── HomeTransitions.java     # All transitions for Home state
    ├── WorldTransitions.java    # All transitions for World state
    ├── IslandTransitions.java   # All transitions for Island state
    └── PricingTransitions.java  # All transitions for Pricing state
```

## Complex Transitions with Action Chains

For transitions that require multiple steps, you can chain actions together:

```java
@TransitionSet(state = WorkingState.class, description = "Claude Working state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkingTransitions {
    
    private final PromptState promptState;
    private final WorkingState workingState;
    private final Action action;
    
    /**
     * Navigate from Prompt to Working by submitting a command.
     */
    @FromTransition(from = PromptState.class, priority = 1)
    public boolean fromPrompt() {
        try {
            log.info("Navigating from Prompt to Working");
            
            if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
                return true;
            }

            // Chain multiple actions: find -> click -> type
            PatternFindOptions findClickType =
                    new PatternFindOptions.Builder()
                            .setPauseAfterEnd(0.5) // Pause before clicking
                            .then(new ClickOptions.Builder()
                                    .setPauseAfterEnd(0.5) // Pause before typing
                                    .build())
                            .then(new TypeOptions.Builder().build())
                            .build();

            // Create target objects for the chained action
            ObjectCollection target =
                    new ObjectCollection.Builder()
                            .withImages(promptState.getClaudePrompt()) // For find & click
                            .withStrings(promptState.getContinueCommand()) // For type
                            .build();

            // Execute the chained action
            ActionResult result = action.perform(findClickType, target);
            return result.isSuccess();
            
        } catch (Exception e) {
            log.error("Error during Prompt to Working transition", e);
            return false;
        }
    }
    
    @ToTransition(required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Working state");
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            return true;
        }
        return action.find(workingState.getWorkingIndicator()).isSuccess();
    }
}
```

## The Formal Model (Under the Hood)

The academic paper provides a formal definition for a transition as a tuple **t = (A, S<sub>t</sub><sup>def</sup>)**. 

* **A** is a **process**, which is a sequence of one or more actions `(a¹, a², ..., aⁿ)`. This corresponds to the method body in your @FromTransition methods.
* **S<sub>t</sub><sup>def</sup>** is the **intended state information**. This is handled automatically by the framework based on the @TransitionSet's state parameter and the @FromTransition's from parameter.

## Dynamic Transitions for Hidden States

Brobot supports dynamic transitions to handle common UI patterns like menus and pop-ups. When a state opens and covers another, the covered state is registered as "hidden."

You can define transitions that return to the previous state dynamically:

```java
@TransitionSet(state = MenuState.class, description = "Menu overlay transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuTransitions {
    
    private final MenuState menuState;
    private final Action action;
    
    /**
     * Close menu and return to whatever state was underneath.
     * This uses the PREVIOUS special state for dynamic navigation.
     */
    @FromTransition(
        from = io.github.jspinak.brobot.model.state.special.SpecialStateType.PREVIOUS.class,
        priority = 1,
        description = "Close menu and return to previous state"
    )
    public boolean fromPrevious() {
        log.info("Closing menu to return to previous state");
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            return true;
        }
        // Click close button or press ESC
        return action.click(menuState.getCloseButton()).isSuccess() ||
               action.type("\u001B").isSuccess(); // ESC key
    }
    
    @ToTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at Menu state");
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            return true;
        }
        return action.find(menuState.getMenuHeader()).isSuccess();
    }
}
```

## Testing Transitions

The new format makes testing easier and more straightforward:

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class PricingTransitionsTest {
    
    @Autowired
    private PricingTransitions pricingTransitions;
    
    @MockBean
    private Action action;
    
    @Test
    public void testFromMenuTransition() {
        // Given
        when(action.click(any())).thenReturn(new ActionResult.Builder().setSuccess(true).build());
        
        // When
        boolean result = pricingTransitions.fromMenu();
        
        // Then
        assertTrue(result);
        verify(action).click(menuState.getPricingButton());
    }
    
    @Test
    public void testVerifyArrival() {
        // Given
        when(action.find(any())).thenReturn(new ActionResult.Builder().setSuccess(true).build());
        
        // When
        boolean arrived = pricingTransitions.verifyArrival();
        
        // Then
        assertTrue(arrived);
        verify(action).find(pricingState.getStartForFreeButton());
    }
}
```

## Migration from Legacy Format

If you're migrating from older Brobot versions, here's a quick comparison:

### Old Format (Pre-1.2.0)
```java
// Separate class for each transition
@Transition(from = HomeState.class, to = WorldState.class)
@Component
public class HomeToWorldTransition {
    public boolean execute() {
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
}
```

### New Format (1.2.0+)
```java
// All transitions for a state in one class
@TransitionSet(state = WorldState.class)
@Component
public class WorldTransitions {
    
    @FromTransition(from = HomeState.class, priority = 1)
    public boolean fromHome() {
        if (FrameworkSettings.mock) return true;
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
    
    @ToTransition(required = true)
    public boolean verifyArrival() {
        if (FrameworkSettings.mock) return true;
        return action.find(worldState.getWorldMap()).isSuccess();
    }
}
```

## Best Practices

1. **Always include mock mode support** for testing environments
2. **Use descriptive method names** like `fromMenu()`, `fromHomepage()` 
3. **Add logging** to track navigation flow during debugging
4. **Verify critical elements** in ToTransition to ensure state is truly active
5. **Set appropriate priorities** when multiple paths exist to the same state
6. **Handle exceptions** gracefully in complex transitions
7. **Keep transitions focused** - each method should do one thing well

## Summary

The @TransitionSet approach provides a clean, maintainable way to define state transitions in Brobot. By grouping all transitions for a state in a single class and using clear annotations to distinguish navigation logic (FromTransition) from verification logic (ToTransition), your automation code becomes easier to understand, test, and maintain.