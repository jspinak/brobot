---
sidebar_position: 3
title: 'States'
---

## What is a Brobot State?

A state in Brobot is a collection of related objects, including images, regions, and locations. This relationship usually involves space (objects are often grouped together) and time (objects often appear together). The defining characteristic of a state is the reliability of expected results: when a state is active, a specific action performed on one of its objects should give the same expected result every time.

In the formal model, a **State (S)** is a collection of related GUI elements chosen to model a distinct configuration of the user interface.

Below is an example of a state in a mobile game. The state holds 5 objects: 1 region, 1 location, and 3 images. Clicking on the image "Raid" should always produce the same result when in this state.

![island state](/img/island-state.jpeg)

### Multiple Active States

In practice, there are usually multiple active states at any time. A key concept in the model is that the visible screen can be described as a set of active states S<sub>Ξ</sub>. States can transition independently without affecting other active states. When designing your automation, think of what might change as a group and what might not; objects that change together should be included in the same state.

The example below shows a screen with multiple states active simultaneously, each highlighted in a different color.

![States Example](/img/states3.png)

## Defining States in Code

Brobot provides two approaches for defining states: the traditional approach shown in the research paper and the modern annotation-based approach introduced in version 1.1.0+.

### Traditional Approach (From the Paper)

This approach uses manual registration with the StateService:

```java
@Component
@Getter
public class Home { 
    public enum Name implements StateEnum { HOME } 
    
    private StateImageObject toWorldButton = new StateImageObject.Builder() 
        .withImage("toWorldButton") 
        .isFixed(true) 
        .addSnapshot(new MatchSnapshot(220, 600, 20, 20)) 
        .build(); 

    private State state = new State.Builder(HOME) 
        .withImages(toWorldButton) 
        .build(); 

    public Home(StateService stateService) { 
        stateService.save(state); 
    } 
}
```

Key characteristics:
- Manual state registration in constructor
- Uses `StateImageObject` (older API)
- Requires explicit `@Component` annotation
- State saved during construction

### Modern Approach with @State Annotation (Recommended)

The modern approach uses the `@State` annotation for automatic registration and cleaner code:

```java
@State  // Automatically registers as Spring component and Brobot state
@Getter
@Slf4j
public class HomeState {
    private final StateImage toWorldButton;  // Only define the components you need
    
    public enum Name implements StateEnum { HOME }
    
    public HomeState() {
        // Just initialize the components - no State object needed!
        toWorldButton = new StateImage.Builder()
            .addPatterns("toWorldButton")  // No .png extension needed
            .setName("ToWorldButton")
            .build();
    }
}
```

Key improvements:
- `@State` annotation handles everything automatically
- No need to manually create a State object
- Framework extracts components via reflection
- Uses `StateImage` (modern API) instead of `StateImageObject`
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

### State Components and Direct Access

The modern approach encourages exposing frequently-used components:

```java
@State
@Getter
@Slf4j
public class GameMenuState {
    private final StateImage playButton;
    private final StateImage settingsButton;
    private final StateImage exitButton;
    private final StateRegion menuArea;
    
    public enum Name implements StateEnum { GAME_MENU }
    
    public GameMenuState() {
        // Create all components with direct access
        playButton = new StateImage.Builder()
            .addPatterns("menu/play-button")
            .setName("PlayButton")
            .build();
            
        settingsButton = new StateImage.Builder()
            .addPatterns("menu/settings-button")
            .setName("SettingsButton")
            .build();
            
        exitButton = new StateImage.Builder()
            .addPatterns("menu/exit-button")
            .setName("ExitButton")
            .build();
            
        menuArea = new StateRegion.Builder()
            .setSearchRegion(new Region(100, 100, 400, 600))
            .setName("MenuArea")
            .build();
    }
}
```

This pattern provides clean access in transitions:
```java
@Transition(from = GameMenuState.class, to = GamePlayState.class)
public class StartGameTransition {
    private final GameMenuState menuState;
    private final Action action;
    
    public boolean execute() {
        // Direct, readable access to state components
        return action.click(menuState.getPlayButton()).isSuccess();
    }
}
```

### Why Both Approaches Work

The `@State` annotation uses reflection through `AnnotatedStateBuilder` and `StateComponentExtractor` to:

1. **Automatically extract** all StateImage, StateRegion, StateLocation, and StateString fields from the class
2. **Build a State object** internally using the extracted components  
3. **Register it** with the state management system

This means:
- When you use `@State`, you don't need an explicit `state` field
- The framework creates the State object for you
- It derives the state name from the class name (removing "State" suffix if present)

### When to Use Each Approach

**Use the traditional approach (with explicit State object) when:**
- Working with legacy code that predates @State
- You need explicit control over state construction

**Use the modern approach (components only) when:**
- You want cleaner, less boilerplate code
- You only need access to the components (most common case)
- Building new projects with Brobot 1.1.0+

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

### Migration Tips

When migrating from traditional to modern approach:
1. Add `@State` annotation to existing state classes
2. Remove manual `stateService.save()` calls
3. Remove the explicit `state` field (unless you specifically need it)
4. Update `StateImageObject` to `StateImage`
5. Consider renaming classes to follow StateNameState convention
6. Expose commonly-used components as fields with getters