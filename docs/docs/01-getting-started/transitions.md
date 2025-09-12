---
sidebar_position: 4
title: 'Transitions'
---

# Transitions

## Introduction

While States define "where you can be" in a GUI, **Transitions** define "how you get there." Every State that is reachable needs an associated `StateTransitions` class that defines the pathways to and from other states. 

Formally, a transition is a process or a sequence of actions that changes the GUI from one state to another.  They form the "edges" of the state graph and are the building blocks used by the framework's pathfinder to navigate the application. 

## The Brobot Implementation: FromTransition and ToTransition

Brobot implements this concept by splitting transitions into two types: **FromTransitions** and **ToTransitions**.  This two-part system provides a clear and reusable structure for managing navigation logic.

![Transition Diagram](/img/paper/transitions.png) 
_This diagram is based on Figure 8 from the research paper._ 

* **FromTransition**: This handles the process of leaving the *current* state to go to another state. For example, to go from `State A` to `State B`, the FromTransition `A -> B` is called. It contains the specific actions needed to initiate the move from State A.

* **ToTransition**: This handles the final steps of arriving at a new state, regardless of which state started the process. There can be multiple FromTransitions going to `State B` from different states, but there is only one ToTransition for `State B`. This `-> B` transition contains generic actions that must always run to confirm `State B` is active.

### Defining Transitions in Code

In the `StateTransitions` Builder, you define these two types of transitions using specific commands:
* `addTransitionFinish` creates the ToTransition. The method passed to it is typically named `finishTransition()`. 
* `addTransition` creates a FromTransition. The methods passed to it can have any name and contain the actions for that specific path. 

```java
// From the DoT test application in the paper 
public class WorldTransitions {
    // ...
    StateTransitions transitions =
        new StateTransitions.Builder(WORLD) // Defines transitions from the WORLD state
            .addTransitionFinish(this::finishTransition) // This is the ToTransition for WORLD
            .addTransition(new StateTransition.Builder() // This is a FromTransition
                .addToActivate(ISLAND) // The target state is ISLAND
                .setFunction(this::goToIsland) // The method with the actions
                .build())
            .build();
    // ...
}
```

## Complete Example: Traditional vs Modern

### Traditional Approach
```java
// WorldTransitions.java
@Component
@RequiredArgsConstructor
public class WorldTransitions {
    private final WorldState worldState;
    private final Action action;
    
    public StateTransitions getStateTransitions() {
        return new StateTransitions.Builder(WORLD)
            .addTransitionFinish(this::finishTransition)
            .addTransition(new StateTransition.Builder()
                .addToActivate(ISLAND)
                .setFunction(this::goToIsland)
                .build())
            .build();
    }
    
    private boolean goToIsland() {
        return action.click(worldState.getIslandPortal()).isSuccess();
    }
    
    private boolean finishTransition() {
        return action.find(worldState.getWorldMap()).isSuccess();
    }
}
```

### Modern Approach
```java
// WorldToIslandTransition.java
@Transition(from = WorldState.class, to = IslandState.class)
@RequiredArgsConstructor
public class WorldToIslandTransition {
    private final WorldState worldState;
    private final Action action;
    
    public boolean execute() {
        return action.click(worldState.getIslandPortal()).isSuccess();
    }
}

// ToWorldTransition.java (replaces finishTransition)
@Transition(to = WorldState.class)
@RequiredArgsConstructor
public class ToWorldTransition {
    private final WorldState worldState;
    private final Action action;
    
    public boolean execute() {
        // Verify we're in the World state
        return action.find(worldState.getWorldMap()).isSuccess();
    }
}
```

## The Formal Model (Under the Hood)

The academic paper provides a formal definition for a transition as a tuple **t = (A, S<sub>t</sub><sup>def</sup>)**. 

* **A** is a **process**, which is a sequence of one or more actions `(a¹, a², ..., aⁿ)`.  This corresponds to the method you pass to the builder (e.g., `goToIsland`).
* **S<sub>t</sub><sup>def</sup>** is the **intended state information**.  This is an explicit definition of which states should become active or inactive if the transition succeeds. This makes state management more robust and predictable, as the framework doesn't have to guess the outcome.  This corresponds to builder methods like `.addToActivate(ISLAND)`. 

## Modern Approach: Using @TransitionSet Annotation

Brobot 1.2.0+ introduces a cohesive, method-level annotation approach that groups all transitions for a state in one class. This maintains high cohesion while providing clear, annotation-based configuration.

### @TransitionSet Annotation

The `@TransitionSet` annotation marks a class as containing all transitions for a specific state:

```java
@TransitionSet(state = PricingState.class)
@RequiredArgsConstructor
@Slf4j
public class PricingTransitions {
    private final MenuState menuState;
    private final HomepageState homepageState;
    private final PricingState pricingState;
    private final Action action;
    
    @FromTransition(from = MenuState.class, priority = 1)
    public boolean fromMenu() {
        log.info("Navigating from Menu to Pricing");
        return action.click(menuState.getPricingButton()).isSuccess();
    }
    
    @FromTransition(from = HomepageState.class, priority = 2)
    public boolean fromHomepage() {
        log.info("Navigating from Homepage to Pricing");
        return action.click(homepageState.getPricingLink()).isSuccess();
    }
    
    @ToTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at Pricing state");
        return action.find(pricingState.getStartForFreeButton()).isSuccess();
    }
}
```

### Annotation Types

#### @TransitionSet
- **state**: The state class these transitions belong to
- **name**: Optional state name override
- **description**: Documentation for the transition set

#### @FromTransition
- **from**: The source state class
- **priority**: Transition priority (higher = preferred)
- **description**: Documentation
- **timeout**: Timeout in seconds

#### @ToTransition
- **description**: Documentation
- **timeout**: Verification timeout
- **required**: Whether verification must succeed

### Key Benefits of @TransitionSet

1. **High Cohesion**: All transitions for a state in ONE class
2. **Clear Separation**: FromTransitions handle navigation, ToTransition verifies arrival
3. **Natural Organization**: Easy to find all paths to/from a state
4. **Spring Integration**: Full dependency injection support
5. **Type Safety**: Class-based state references
6. **Reduced Boilerplate**: Method-level annotations are concise

### Comparison: Traditional vs Modern

| Aspect | Traditional StateTransitions | Modern @TransitionSet |
|--------|------------------------------|------------------------|
| **Organization** | All transitions in one builder | All transitions in one class with methods |
| **Cohesion** | High (single class) | High (single class) |
| **FromTransitions** | addTransition() calls | @FromTransition methods |
| **ToTransition** | addTransitionFinish() | @ToTransition method |
| **Registration** | Manual in listener | Automatic with Spring |
| **Type Safety** | String-based names | Class-based references |
| **Spring Integration** | Partial | Full integration |
| **Clarity** | Builder pattern complexity | Clear method annotations |

### When to Use Each Approach

- **Use @TransitionSet (Modern)** for:
  - New projects starting with Brobot 1.2.0+
  - Projects that value cohesion and organization
  - Better Spring Boot integration
  - Cleaner, more maintainable code

- **Use StateTransitions (Traditional)** for:
  - Legacy projects that haven't migrated
  - Dynamic transition generation at runtime
  - Complex programmatic transition logic

## Dynamic Transitions for Hidden States

In addition to these statically defined transitions, Brobot also supports dynamic transitions to handle common UI patterns like menus and pop-ups. When a state opens and covers another, the covered state is registered as "hidden."

You can then define a transition with a dynamic target called **`PREVIOUS`**. When this transition is executed (e.g., by closing the menu), the framework intelligently navigates back to whatever state was most recently hidden, without you needing to pre-define every possible combination.

### Dynamic Transitions with @Transition

```java
@Transition(
    from = MenuState.class,
    to = PreviousState.class,  // Special marker class for dynamic transitions
    description = "Close menu and return to previous state"
)
@RequiredArgsConstructor
public class CloseMenuTransition {
    private final MenuState menuState;
    private final Action action;
    
    public boolean execute() {
        // Click close button or press ESC
        return action.click(menuState.getCloseButton()).isSuccess();
    }
}
```



