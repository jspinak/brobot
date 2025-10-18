---
sidebar_position: 3
title: 'States'
---

## What is a Brobot State?

A state in Brobot is a collection of related objects, including images, regions, locations, and strings. This relationship usually involves space (objects are often grouped together) and time (objects often appear together). The defining characteristic of a state is the reliability of expected results: when a state is active, a specific action performed on one of its objects should give the same expected result every time.

In the formal model, a **State (S)** is a collection of related GUI elements chosen to model a distinct configuration of the user interface.

Below is an example of a state in a mobile game. The state holds 5 objects: 1 region, 1 location, and 3 images. Clicking on the image "Raid" should always produce the same result when in this state.

![island state](/img/island-state.jpeg)

### Multiple Active States

In practice, there are usually multiple active states at any time. A key concept in the model is that the visible screen can be described as a set of active states S<sub>Ξ</sub>. States can transition independently without affecting other active states. When designing your automation, think of what might change as a group and what might not; objects that change together should be included in the same state.

The example below shows a screen with multiple states active simultaneously, each highlighted in a different color.

![States Example](/img/states3.png)

#### Impact on Pathfinding

**Important**: Brobot's pathfinding system leverages multi-state activation. When a transition activates multiple states simultaneously (e.g., Login → [Dashboard, Sidebar, Menu]), **each activated state becomes a potential path node** for future navigation. This means:

- More navigation paths are automatically available
- States can be reached through unexpected routes
- The pathfinder considers ALL activated states equally (no "primary" target)

> **📖 See [Pathfinding & Multi-State Activation](pathfinding.md) for detailed explanation**
>
> **📖 Advanced Patterns**: [Multi-State Transitions Guide](../03-core-library/user-guides/multi-state-transitions-guide.md) for detailed multi-state patterns, verification strategies, and best practices

## Defining States in Code (version 1.1.0+)

```java
@State  // Automatically registers as Spring component and Brobot state
@Getter
@Slf4j
public class HomeState { // The state name will be the class name without "State" - here it is "Home".
    private final StateImage toWorldButton = new StateImage.Builder()
        .addPatterns("toWorldButton")  // No .png extension needed
        .setName("ToWorldButton")
        .build();
}
```

Key improvements over state definition in previous versions:
- `@State` annotation handles everything automatically
- No need to manually create a State object
- Framework extracts components via reflection
- Cleaner, less boilerplate code
- Better naming convention (HomeState vs Home)

### Marking Initial States

For states that should be active when the application starts:

```java
@State(initial = true)  // Marks as initial state for state machine
@Getter
@Slf4j
public class HomeState {
    // State definition
}
```

### Setting State Path Costs

States can have a pathCost that affects pathfinding decisions. The total cost of a path includes both state and transition costs:

```java
@State  // Default pathCost = 1
@Getter
@Slf4j
public class NormalState {
    // Standard state with default cost
}

@State(pathCost = 0)  // Free state (no cost)
@Getter
@Slf4j
public class TransientState {
    // State that doesn't add to path cost (e.g., loading screens)
}

@State(pathCost = 5)  // Expensive state
@Getter
@Slf4j
public class SlowLoadingState {
    // Higher cost discourages routing through this state
}
```

**Note**: The default pathCost for states is 1. Lower total path costs are preferred during pathfinding. For comprehensive pathfinding documentation, see the [Pathfinding and Path Costs Guide](../03-core-library/user-guides/pathfinding-and-costs.md).

### State Components and Direct Access

The modern approach encourages exposing frequently-used components:

```java
@State
@Getter
@Slf4j
public class GameMenuState {
    private final StateImage playButton = new StateImage.Builder()
        .addPatterns("menu/play-button")
        .setName("PlayButton")
        .build();
        
    private final StateImage settingsButton = new StateImage.Builder()
        .addPatterns("menu/settings-button")
        .setName("SettingsButton")
        .build();
        
    private final StateImage exitButton = new StateImage.Builder()
        .addPatterns("menu/exit-button")
        .setName("ExitButton")
        .build();
        
    private final StateRegion menuArea = new StateRegion.Builder()
        .setSearchRegion(new Region(100, 100, 400, 600))
        .setName("MenuArea")
        .build();
}
```

> **💡 Visual Debugging Tip**: StateImages support visual highlighting for debugging automation. You can manually highlight found patterns to verify your automation is working correctly. See the [Highlighting Feature Guide](../03-core-library/user-guides/highlighting-feature.md) for details on visual feedback and debugging techniques.

This pattern provides clean access in transitions:
```java
@TransitionSet(state = GamePlayState.class)
@Component
@RequiredArgsConstructor
public class GamePlayTransitions {
    private final GamePlayState gamePlayState;
    private final Action action;
    
    @OutgoingTransition(activate = {MainScreenState.class}, pathCost = 2)
    public boolean toMainScreen() {
        // Clicking the play button should take use to the main screen
        return action.click(gamePlayState.getPlayButton()).isSuccess();
    }
    
    @IncomingTransition
    public boolean verifyArrival() {
        // Verify we're in the game play state
        return action.find(gamePlayState.getGameBoard()).isSuccess();
    }
}
```

### How State Inialization Works

The `@State` annotation uses reflection through `AnnotatedStateBuilder` and `StateComponentExtractor` to:

1. **Automatically extract** all StateImage, StateRegion, StateLocation, and StateString fields from the class
2. **Build a State object** internally using the extracted components  
3. **Register it** with the state management system

This means:
- When you use `@State`, you don't need an explicit `state` field
- The framework creates the State object for you
- It derives the state name from the class name (removing "State" suffix if present)

### Optional: Explicit State with @State

While not necessary, you *can* still define an explicit `state` field with @State if you need direct access:

```java
@State
@Getter
public class HomeState {
    private final State state;  // Optional - only if you need it
    private final StateImage toWorldButton;
    
    public HomeState() {
        toWorldButton = new StateImage.Builder()...
        // Manual state creation - framework will use this instead of creating its own
        state = new State.Builder("HOME")...
    }
}
```

However, this is rarely needed since:
- Transitions work with components directly
- State navigation uses state names/enums
- The framework manages the State object internally

### Migration Tips (1.0.7 to 1.1.0)

When migrating from traditional to modern approach:
1. Add `@State` annotation to existing state classes
2. Remove manual `stateService.save()` calls
3. Remove the explicit `state` field (unless you specifically need it)
4. Consider renaming classes to follow StateNameState convention
5. Expose commonly-used components as fields with getters