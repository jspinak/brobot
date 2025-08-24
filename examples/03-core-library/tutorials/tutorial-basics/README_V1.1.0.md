# Tutorial Basics - Brobot v1.1.0

This tutorial demonstrates the fundamentals of Brobot v1.1.0 using modern annotations.

## Key Concepts

### 1. States with @State Annotation
States are automatically registered using the @State annotation:
```java
@State(initial = true)  // Mark as initial state
@Getter
public class HomeState {
    private final StateImage toWorldButton;
    // State components are automatically detected
}
```

### 2. StateImages
Images that appear in states:
```java
toWorldButton = new StateImage.Builder()
    .setName("ToWorldButton")
    .addPatterns("home/to_world_button")
    .setFixed(true)
    .build();
```

### 3. StateStrings
Text regions for OCR:
```java
goldCounter = new StateString.Builder()
    .setName("GoldCounter")
    .setRegion(new Region(1600, 100, 200, 40))
    .build();
```

### 4. Transitions with @Transition Annotation
Transitions are automatically registered:
```java
@Transition(from = HomeState.class, to = WorldState.class)
@RequiredArgsConstructor
public class HomeToWorldTransition {
    private final HomeState homeState;
    private final Action action;
    
    public boolean execute() {
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
}
```

### 5. Actions with ObjectCollection
All actions require ObjectCollection:
```java
ObjectCollection collection = new ObjectCollection.Builder()
    .withImages(worldState.getCastleIsland())
    .withRegions(worldState.getMapRegion())
    .build();

ActionResult result = action.perform(findOptions, collection);
```

## Project Structure

```
tutorial-basics/
├── states/
│   ├── HomeState.java      # Main menu state
│   ├── WorldState.java     # World map with islands
│   └── IslandState.java    # Individual island view
├── transitions/
│   ├── HomeToWorldTransition.java
│   ├── WorldToIslandTransition.java
│   ├── IslandToWorldTransition.java
│   └── WorldToHomeTransition.java
├── automation/
│   └── BasicAutomation.java  # Example automation tasks
├── TutorialRunner.java      # Main automation runner
└── TutorialBasicsApplication.java  # Spring Boot app
```

## State Flow

```
HOME → WORLD → ISLAND
  ↑              ↓
  └──────────────┘
```

## Running the Tutorial

1. Ensure you have the required images in `src/main/resources/images/`:
   - `home/to_world_button.png`
   - `home/home_logo.png`
   - `home/menu_bar.png`
   - `world/home_button.png`
   - `world/castle_island_1.png`, `world/castle_island_2.png`
   - `world/mines_island_1.png`, `world/mines_island_2.png`
   - `island/back_to_world.png`
   - `island/collect_button.png`
   - `island/upgrade_button.png`

2. Run in mock mode (default) to test without a real UI:
   ```bash
   mvn spring-boot:run
   ```

3. For real UI testing, set `mock=false` in your application properties.

## Key Features of v1.1.0

1. **@State/@Transition annotations** - Automatic registration
2. **ObjectCollection required** - All actions need ObjectCollection
3. **Automatic state management** - No manual registration needed
4. **Action interface** - Single Action service
5. **Modern configuration system** - ActionConfig hierarchy

## Common Patterns

### Finding Multiple Objects
```java
PatternFindOptions findAll = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .build();

ObjectCollection collection = new ObjectCollection.Builder()
    .withImages(stateImage)
    .withRegions(searchRegion)
    .build();

ActionResult result = action.perform(findAll, collection);
int count = result.getMatchList().size();
```

### Reading Text
```java
TextFindOptions textOptions = new TextFindOptions.Builder()
    .setLanguage("eng")
    .build();

ObjectCollection textCollection = new ObjectCollection.Builder()
    .withStateStrings(stateString)
    .build();

ActionResult result = action.perform(textOptions, textCollection);
String text = result.getText();
```

### State Navigation
```java
if (stateManager.goToState("WORLD")) {
    log.info("Successfully navigated to WORLD");
}
```

## Next Steps

1. Add more complex state logic
2. Implement ConditionalActionChain for workflows
3. Add real-time state verification
4. Create custom action configurations
5. Explore advanced features like dynamic object finding