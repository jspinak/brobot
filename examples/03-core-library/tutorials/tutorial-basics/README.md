# Tutorial Basics

This tutorial introduces the fundamental concepts of Brobot through a simple three-state demo application.

## Overview

The tutorial demonstrates:

- **State Management** - Define application states with `@State` annotation
- **Transitions** - Navigate between states with `@TransitionSet` and `@OutgoingTransition`
- **StateImages** - Work with images using Builder pattern
- **Actions** - Perform find, click, and text operations
- **Mock Mode** - Test without a real UI

## The Demo Application

Our demo simulates a simple game with three states:

1. **HOME** - The main menu
2. **WORLD** - A world map with islands
3. **ISLAND** - Individual island views

```
    HOME
     ↓↑
   WORLD
     ↓↑
   ISLAND
```

## Project Structure

```
tutorial-basics/
├── src/main/java/com/example/basics/
│   ├── TutorialBasicsApplication.java    # Spring Boot main
│   ├── TutorialRunner.java               # Runs the tutorial
│   ├── StateNames.java                   # State name constants
│   ├── states/                           # State definitions
│   │   ├── HomeState.java               # HOME state
│   │   ├── WorldState.java              # WORLD state
│   │   └── IslandState.java             # ISLAND state
│   ├── transitions/                      # State transitions
│   │   ├── HomeTransitions.java         # HOME state transitions
│   │   ├── WorldTransitions.java        # WORLD state transitions
│   │   └── IslandTransitions.java       # ISLAND state transitions
│   └── automation/                       # Demo automation
│       ├── BasicAutomation.java         # Main automation examples
│       ├── GetNewIsland.java            # Island type detection
│       ├── IslandRegion.java            # Declarative region demo
│       └── SaveLabeledImages.java       # Image capture automation
├── src/main/resources/
│   └── application.yml                   # Configuration
├── images/                               # Image patterns
│   ├── home/                            # HOME state images
│   ├── world/                           # WORLD state images
│   └── island/                          # ISLAND state images
├── build.gradle
└── settings.gradle
```

## Key Concepts

### 1. States

Define states using the `@State` annotation:

```java
@State(initial = true)
@Component
@Getter
public class HomeState {

    private final StateImage toWorldButton;

    public HomeState() {
        this.toWorldButton = new StateImage.Builder()
            .addPatterns("toWorldButton")
            .setName("ToWorldButton")
            .build();
    }
}
```

Key points:
- `@State` annotation automatically registers the state
- `initial = true` marks the starting state
- Use `StateImage.Builder()` for creating UI element patterns
- `@Getter` from Lombok provides getters automatically

### 2. Transitions (v1.1.0+ Pattern)

Define transitions using `@TransitionSet` to group all transitions for a state:

```java
@TransitionSet(state = HomeState.class, description = "Home state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeTransitions {

    private final Action action;
    private final HomeState homeState;

    @OutgoingTransition(activate = {WorldState.class}, pathCost = 1,
                        description = "Navigate to World state")
    public boolean toWorld() {
        log.info("Transitioning from Home to World");
        return action.click(homeState.getToWorldButton()).isSuccess();
    }

    @IncomingTransition(description = "Verify arrival at Home")
    public boolean verifyArrival() {
        log.info("Verifying Home state");
        return action.find(homeState.getToWorldButton()).isSuccess();
    }
}
```

Key points:
- `@TransitionSet` groups all transitions for a state
- `@OutgoingTransition` marks methods that navigate to other states
- `@IncomingTransition` marks methods that verify state arrival
- Use dependency injection for `Action` and state classes
- Return `true` for success, `false` for failure

### 3. StateImages

StateImages represent UI elements using Builder pattern:

```java
// Image-based pattern
StateImage button = new StateImage.Builder()
    .addPatterns("button_image")
    .setName("Button")
    .build();

// Multiple patterns for better matching
StateImage loginButton = new StateImage.Builder()
    .addPatterns("login_btn_1", "login_btn_2", "login_btn_3")
    .setName("LoginButton")
    .build();

// With search region
StateImage searchButton = new StateImage.Builder()
    .addPatterns("search_icon")
    .setSearchRegion(new SearchRegion(100, 100, 200, 200))
    .build();
```

### 4. Declarative Search Regions

Define search regions relative to other objects using `SearchRegionOnObject`:

```java
StateImage captureRegion = new StateImage.Builder()
    .setSearchRegionOnObject(
        SearchRegionOnObject.builder()
            .setTargetObjectName("searchButton")
            .setAdjustments(
                MatchAdjustmentOptions.builder()
                    .setAddX(-50)
                    .setAddY(-250)
                    .setAbsoluteW(200)
                    .setAbsoluteH(200)
                    .build())
            .build())
    .build();
```

This creates a region relative to another object's position - powerful for dynamic UIs!

### 5. Actions (v1.1.0+ API)

Perform actions using the Action service with ActionConfig options:

```java
@Component
@RequiredArgsConstructor
public class MyAutomation {
    private final Action action;

    public void performActions(StateImage target) {
        // Find with configuration
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .build();
        ObjectCollection findTarget = new ObjectCollection.Builder()
            .withImages(target)
            .build();
        ActionResult findResult = action.perform(findOptions, findTarget);

        // Click (convenience method)
        ActionResult clickResult = action.click(target);

        // Click with configuration
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .build();
        ObjectCollection clickTarget = new ObjectCollection.Builder()
            .withImages(target)
            .build();
        action.perform(clickOptions, clickTarget);
    }
}
```

Key patterns:
- Use specific `*Options.Builder()` classes (PatternFindOptions, ClickOptions, etc.)
- Use `ObjectCollection.Builder()` to wrap targets
- Use `action.perform(options, collection)` for full control
- Use convenience methods `action.click(stateImage)`, `action.find(stateImage)` for simple cases

### 6. State Navigation

Use StateNavigator for navigation:

```java
@Component
@RequiredArgsConstructor
public class NavigationExample {
    private final StateNavigator stateNavigator;

    public void navigateToWorld() {
        // Navigate to a state by name
        if (stateNavigator.openState("WORLD")) {
            log.info("Successfully navigated to WORLD");
        }

        // Navigate using state class
        if (stateNavigator.openState(WorldState.class)) {
            log.info("Successfully navigated using class");
        }
    }
}
```

## Running the Tutorial

### 1. In Mock Mode (Default)

```bash
./gradlew bootRun
```

Mock mode simulates the UI without requiring a real application. Perfect for:
- Learning Brobot concepts
- Testing state logic
- Developing transitions

### 2. With Real UI

1. Prepare your application with matching UI elements
2. Add screenshots to the `images/` directories
3. Set `brobot.mock: false` in `application.yml`
4. Run the tutorial

### 3. Adding Images

Place images in the appropriate directories:
- `images/home/` - HOME state UI elements
- `images/world/` - WORLD state UI elements
- `images/island/` - ISLAND state UI elements

Image naming should match the patterns in state definitions.

## What the Tutorial Does

1. **State Verification** - Confirms all states are registered
2. **Navigation Demo** - Moves through all states:
   - HOME → WORLD
   - WORLD → ISLAND
   - ISLAND → WORLD
   - WORLD → HOME
3. **Island Exploration** - Demonstrates finding and text extraction
4. **Declarative Regions** - Shows SearchRegionOnObject usage
5. **Error Recovery** - Shows handling of failed transitions

## Configuration

Key settings in `application.yml`:

```yaml
brobot:
  mock: true              # Use mock mode

  logging:
    verbosity: VERBOSE    # Detailed logging

  state:
    auto-scan: true       # Find @State classes

  transition:
    max-attempts: 3       # Retry failed transitions
```

## Brobot 1.1.0+ Patterns Used

This tutorial demonstrates modern Brobot patterns:

1. **@TransitionSet Pattern** - Groups transitions by state (replaces individual @Transition classes)
2. **@OutgoingTransition** - Marks navigation methods with activation targets
3. **@IncomingTransition** - Marks verification methods
4. **StateImage.Builder()** - Modern pattern for creating state images
5. **ActionConfig Builders** - PatternFindOptions, ClickOptions, etc.
6. **ObjectCollection.Builder()** - Wraps action targets
7. **Action.perform()** - Main action execution method
8. **Convenience Methods** - action.click(), action.find() for simple cases
9. **SearchRegionOnObject** - Declarative relative positioning

## Migration from v1.0

If you see older documentation patterns, here's how they map to v1.1.0+:

| v1.0 Pattern | v1.1.0+ Pattern |
|--------------|-----------------|
| `@Transition(from, to)` | `@TransitionSet` + `@OutgoingTransition` |
| `StateObject.builder()` | `StateImage.Builder()` |
| `BrobotEntity.of()` | Direct `StateImage` usage |
| `ActionOptions` | Specific `*Options.Builder()` classes |
| Individual transition classes | Grouped in `*Transitions` classes |

## Next Steps

1. **Modify States** - Add new UI elements to existing states
2. **Create New States** - Add a MENU or SETTINGS state
3. **Complex Transitions** - Add multi-step transitions
4. **Real Application** - Connect to an actual UI
5. **Advanced Features** - Explore:
   - Cross-state dependencies with SearchRegionOnObject
   - Conditional transitions
   - ConditionalActionChain
   - Custom success criteria

## Troubleshooting

### States Not Found
- Ensure `@State` annotation is present
- Check component scanning includes your package
- Verify Spring Boot finds your classes

### Transitions Failing
- Check transition method returns boolean
- Verify state classes are injected correctly
- Look for action failures in logs
- Ensure @OutgoingTransition specifies correct target states

### Mock Mode Issues
- Check `brobot.mock: true` in application.yml
- Review mock configuration settings
- Ensure mock mode is enabled for testing

## Key Takeaways

1. **@TransitionSet groups transitions** - One class per state's transitions
2. **StateImage.Builder() is the pattern** - Use for all image-based elements
3. **ActionConfig uses Builders** - PatternFindOptions, ClickOptions, etc.
4. **SearchRegionOnObject is powerful** - Define regions relative to other objects
5. **Navigation is automatic** - StateNavigator finds shortest path
6. **Mock mode enables testing** - Develop without real UI

## Related Documentation

- [States Guide](../../../../01-getting-started/states.md)
- [Transitions Guide](../../../../01-getting-started/transitions.md)
- [Action Configuration](../../../action-config/01-overview.md)
- [Mock Mode](../../../../04-testing/mock-mode.md)
