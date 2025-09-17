---
sidebar_position: 4
title: 'Transitions'
---

# Transitions

## Introduction

While States define "where you can be" in a GUI, **Transitions** define "how you get there." Every State that is reachable needs an associated transitions class that defines the pathways to and from other states. 

Formally, a transition is a process or a sequence of actions that changes the GUI from one state to another. They form the "edges" of the state graph and are the building blocks used by the framework's pathfinder to navigate the application. 

## The Brobot Implementation: IncomingTransition and OutgoingTransition

Brobot implements transitions using a cohesive pattern where each state's transition class contains:

![Transition Diagram](/img/paper/transitions.png)
_This diagram is based on Figure 8 from the research paper._

* **IncomingTransition**: This verifies successful arrival at the state, regardless of which state initiated the transition. There is only one IncomingTransition per state, and it contains checks to confirm the state is active.

* **OutgoingTransition**: These handle navigation FROM the current state TO other states. Since these transitions use the current state's images and UI elements, grouping them in the state's transition class creates better cohesion. Each OutgoingTransition contains the specific actions needed to navigate to a target state.

## Using @TransitionSet Annotation

Brobot uses a cohesive, method-level annotation approach that groups all transitions for a state in one class. Each transition class contains:
- The IncomingTransition to verify arrival at the state
- All OutgoingTransitions that navigate FROM this state to other states

This pattern maintains high cohesion since outgoing transitions use the current state's images.

### Complete Example

```java
// Note: BrobotProperties must be injected as a dependency
@Autowired
private BrobotProperties brobotProperties;

@TransitionSet(state = PricingState.class, description = "Pricing page transitions")
@RequiredArgsConstructor
@Slf4j
public class PricingTransitions {

    private final PricingState pricingState;
    private final Action action;
    
    /**
     * Verify that we have successfully arrived at the Pricing state.
     * Checks for the presence of pricing-specific elements.
     */
    @IncomingTransition(description = "Verify arrival at Pricing state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Pricing state");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
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

    /**
     * Navigate from Pricing to Homepage by clicking the home/logo button.
     */
    @OutgoingTransition(to = HomepageState.class, priority = 1, description = "Navigate from Pricing to Homepage")
    public boolean toHomepage() {
        log.info("Navigating from Pricing to Homepage");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        return action.click(pricingState.getHomeLink()).isSuccess();
    }

    /**
     * Navigate from Pricing to Menu by clicking the menu icon.
     */
    @OutgoingTransition(to = MenuState.class, priority = 2, description = "Navigate from Pricing to Menu")
    public boolean toMenu() {
        log.info("Navigating from Pricing to Menu");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        return action.click(pricingState.getMenuIcon()).isSuccess();
    }
}
```

### Annotation Types

#### @TransitionSet
Marks a class as containing all transitions for a specific state:
- **state**: The state class these transitions belong to (required)
- **name**: Optional state name override (defaults to class name without "State" suffix)
- **description**: Documentation for the transition set

#### @IncomingTransition
Verifies successful arrival at the state:
- **description**: Documentation for the verification
- **timeout**: Verification timeout in seconds (optional)
- **required**: Whether verification must succeed (default: false)

#### @OutgoingTransition
Defines a transition FROM the current state TO another state:
- **to**: The target state class (required)
- **priority**: Transition priority - higher values are preferred when multiple paths exist (default: 0)
- **description**: Documentation for this transition
- **timeout**: Timeout in seconds (optional)

## World State Example

Here's another complete example showing transitions for a World state with multiple entry and exit points:

```java
@TransitionSet(state = WorldState.class, description = "World map transitions")
@RequiredArgsConstructor
@Slf4j
public class WorldTransitions {

    private final WorldState worldState;
    private final Action action;
    
    /**
     * Verify arrival at World state by checking for the world map.
     */
    @IncomingTransition(description = "Verify arrival at World state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at World state");
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
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

    /**
     * Navigate from World to Home by clicking the home button.
     */
    @OutgoingTransition(to = HomeState.class, priority = 1, description = "Navigate from World to Home")
    public boolean toHome() {
        log.info("Navigating from World to Home");
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        return action.click(worldState.getHomeButton()).isSuccess();
    }

    /**
     * Navigate from World to Island by clicking on an island.
     */
    @OutgoingTransition(to = IslandState.class, priority = 2, description = "Navigate from World to Island")
    public boolean toIsland() {
        log.info("Navigating from World to Island");
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Click on first island
        return action.click(worldState.getIsland1()).isSuccess();
    }
}
```

## Key Benefits of This Pattern

1. **High Cohesion**: Each transition class only needs its own state as a dependency, since outgoing transitions use that state's images
2. **Clear Separation**: IncomingTransition verifies arrival, OutgoingTransitions handle navigation FROM the state
3. **Natural Organization**: File structure mirrors state structure (one transitions class per state)
4. **Reduced Dependencies**: No need to inject other states just for their images in incoming transitions
5. **Spring Integration**: Full dependency injection support (@TransitionSet includes @Component)
6. **Type Safety**: Class-based state references prevent typos and enable IDE refactoring
7. **Mock Mode Support**: Easy to add testing support with framework settings check
8. **Cleaner Code**: Each transition class is self-contained with its state's navigation logic

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
@RequiredArgsConstructor
@Slf4j
public class WorkingTransitions {

    private final WorkingState workingState;
    private final Action action;
    
    @IncomingTransition(required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Working state");
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            return true;
        }
        return action.find(workingState.getWorkingIndicator()).isSuccess();
    }

    /**
     * Navigate from Working to Prompt when work is complete.
     */
    @OutgoingTransition(to = PromptState.class, priority = 1)
    public boolean toPrompt() {
        try {
            log.info("Navigating from Working to Prompt");

            if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
                return true;
            }

            // Wait for work to complete and return to prompt
            // This might involve clicking a button or waiting for the prompt to reappear
            return action.click(workingState.getStopButton()).isSuccess();

        } catch (Exception e) {
            log.error("Error during Working to Prompt transition", e);
            return false;
        }
    }
}
```

## The Formal Model (Under the Hood)

The academic paper provides a formal definition for a transition as a tuple **t = (A, S<sub>t</sub><sup>def</sup>)**. 

* **A** is a **process**, which is a sequence of one or more actions `(a¹, a², ..., aⁿ)`. This corresponds to the method body in your @OutgoingTransition methods.
* **S<sub>t</sub><sup>def</sup>** is the **intended state information**. This is handled automatically by the framework based on the @TransitionSet's state parameter and the @OutgoingTransition's to parameter.

## Dynamic Transitions for Hidden States

Brobot supports dynamic transitions to handle common UI patterns like menus and pop-ups. When a state opens and covers another, the covered state is registered as "hidden."

You can define transitions that return to the previous state dynamically:

```java
@TransitionSet(state = MenuState.class, description = "Menu overlay transitions")
@RequiredArgsConstructor
@Slf4j
public class MenuTransitions {
    
    private final MenuState menuState;
    private final Action action;
    
    /**
     * Close menu and return to whatever state was underneath.
     * This uses the PREVIOUS special state for dynamic navigation.
     */
    @OutgoingTransition(
        to = io.github.jspinak.brobot.model.state.special.SpecialStateType.PREVIOUS.class,
        priority = 1,
        description = "Close menu and return to previous state"
    )
    public boolean toPrevious() {
        log.info("Closing menu to return to previous state");
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            return true;
        }
        // Click close button or press ESC
        return action.click(menuState.getCloseButton()).isSuccess() ||
               action.type("\u001B").isSuccess(); // ESC key
    }
    
    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at Menu state");
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
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

## Best Practices

1. **Always include mock mode support** for testing environments
2. **Use descriptive method names** like `toMenu()`, `toHomepage()` for outgoing transitions
3. **Add logging** to track navigation flow during debugging
4. **Verify critical elements** in ToTransition to ensure state is truly active
5. **Set appropriate priorities** when multiple paths exist to the same state
6. **Handle exceptions** gracefully in complex transitions
7. **Keep transitions focused** - each method should do one thing well
8. **Minimize dependencies** - each transition class should only need its own state

## Summary

The @TransitionSet pattern with @OutgoingTransition and @IncomingTransition provides a clean, maintainable way to define state transitions in Brobot. By grouping a state's verification logic (IncomingTransition) with its outgoing navigation logic (OutgoingTransition) in a single class, you achieve:

- **Better cohesion**: Outgoing transitions use the current state's images
- **Fewer dependencies**: Each transition class only needs its own state
- **Clearer organization**: Navigation logic flows naturally from each state
- **Easier maintenance**: All transitions for a state are in one place

This pattern makes your automation code easier to understand, test, and maintain.