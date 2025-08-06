# Tutorial Basics

This tutorial introduces the fundamental concepts of Brobot through a simple three-state demo application.

## Overview

The tutorial demonstrates:

- **State Management** - Define application states with `@State` annotation
- **Transitions** - Navigate between states with `@Transition` annotation
- **StateObjects** - Work with images and regions
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
│   ├── states/                           # State definitions
│   │   ├── HomeState.java               # HOME state
│   │   ├── WorldState.java              # WORLD state
│   │   └── IslandState.java             # ISLAND state
│   ├── transitions/                      # State transitions
│   │   ├── HomeToWorldTransition.java
│   │   ├── WorldToIslandTransition.java
│   │   ├── IslandToWorldTransition.java
│   │   └── WorldToHomeTransition.java
│   └── automation/                       # Demo automation
│       └── BasicAutomation.java
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
@State(name = "HOME", initial = true)
@Component
public class HomeState {
    
    private final StateObject toWorldButton = StateObject.builder()
        .name("ToWorldButton")
        .stateImages(StateImage.builder()
            .addPattern("to_world_button")
            .fixed(true)  // Always same location
            .addSnapshot(new MatchSnapshot(220, 600, 20, 20))
            .build())
        .build();
}
```

Key points:
- `@State` annotation automatically registers the state
- `initial = true` marks the starting state
- StateObjects define UI elements in the state
- Snapshots provide expected locations

### 2. Transitions

Define transitions using the `@Transition` annotation:

```java
@Transition(from = "HOME", to = "WORLD")
@Component
public class HomeToWorldTransition {
    
    public boolean execute() {
        // Click the "To World" button
        ActionResult result = actions.click(
            BrobotEntity.of(homeState.getToWorldButton())
        );
        
        return result.isSuccess();
    }
}
```

Key points:
- `@Transition` annotation automatically registers the transition
- `execute()` method performs the transition
- Return `true` for success, `false` for failure

### 3. StateObjects

StateObjects represent UI elements:

```java
// Image-based object
StateObject button = StateObject.builder()
    .name("Button")
    .stateImages(StateImage.builder()
        .addPattern("button_image")
        .build())
    .build();

// Region-based object
StateObject textArea = StateObject.builder()
    .name("TextArea")
    .region(new Region(100, 100, 200, 50))
    .build();

// Combined object with OCR
StateObject labeledField = StateObject.builder()
    .name("LabeledField")
    .stateImages(StateImage.builder()
        .addPattern("field_label")
        .build())
    .region(new Region(300, 100, 200, 30))
    .addSnapshot(new MatchSnapshot.Builder()
        .setActionConfig(new TextFindOptions.Builder().build())
        .addString("Expected Text")
        .build())
    .build();
```

### 4. Actions

Perform actions using the Actions interface:

```java
// Find
ActionResult findResult = actions.find(
    BrobotEntity.of(stateObject)
        .configure(new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .build())
);

// Click
ActionResult clickResult = actions.click(
    BrobotEntity.of(stateObject)
        .configure(new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .build())
);

// Extract text
ActionResult textResult = actions.text(
    BrobotEntity.of(regionObject)
        .configure(new TextFindOptions.Builder()
            .setLanguage("eng")
            .build())
);
```

### 5. State Navigation

Use StateManager for navigation:

```java
// Go to a specific state
boolean success = stateManager.goToState("WORLD");

// Check current state
String current = stateManager.getCurrentState();

// Check if transition is possible
boolean canGo = stateManager.canTransitionTo("ISLAND");
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
3. Set `brobot.core.mock: false` in `application.yml`
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
4. **Error Recovery** - Shows handling of failed transitions

## Configuration

Key settings in `application.yml`:

```yaml
brobot:
  core:
    mock: true           # Use mock mode
    verbose: true        # Detailed logging
  
  state:
    auto-scan: true      # Find @State classes
    
  transition:
    max-attempts: 3      # Retry failed transitions
    
  mock:
    success-probability: 0.9  # 90% success rate
```

## Next Steps

1. **Modify States** - Add new UI elements to existing states
2. **Create New States** - Add a MENU or SETTINGS state
3. **Complex Transitions** - Add multi-step transitions
4. **Real Application** - Connect to an actual UI
5. **Advanced Features** - Explore:
   - Cross-state dependencies
   - Conditional transitions
   - Action chains
   - Custom success criteria

## Troubleshooting

### States Not Found
- Ensure `@State` annotation is present
- Check component scanning includes your package
- Verify Spring Boot finds your classes

### Transitions Failing
- Check transition method returns boolean
- Verify from/to states exist
- Look for action failures in logs

### Mock Mode Issues
- Adjust `success-probability` for testing failures
- Check `generate-matches` is true for find operations
- Review mock configuration settings

## Key Takeaways

1. **Annotations simplify setup** - `@State` and `@Transition` handle registration
2. **StateObjects are flexible** - Combine images, regions, and text
3. **Actions are configurable** - Use specific option classes
4. **Navigation is automatic** - StateManager finds shortest path
5. **Mock mode enables testing** - Develop without real UI

## Related Documentation

- [States Guide](../../../../01-getting-started/states.md)
- [Transitions Guide](../../../../01-getting-started/transitions.md)
- [Action Configuration](../../../action-config/01-overview.md)
- [Mock Mode](../../../../04-testing/mock-mode.md)