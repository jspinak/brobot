---
sidebar_position: 4
---

# States

States in Brobot represent distinct screens or UI contexts in your application. With the modern annotation-based approach, defining states is clean, declarative, and automatically integrated with Spring Boot.

## Modern State Definition with @State Annotation

The `@State` annotation automatically registers your state with Brobot's state management system:

```java
import io.github.jspinak.brobot.tools.testing.data.ActionRecordTestUtils;

@State(initial = true)  // Marks this as the initial state
@Component
@Getter
@Slf4j
public class HomeState {
    private final StateImage toWorldButton;
    private final StateImage searchButton;
    
    public HomeState() {
        // Define UI elements with fluent builder pattern
        toWorldButton = new StateImage.Builder()
            .addPatterns("toWorldButton")
            // .setFixed(true)  // Method doesn't exist in current API
            .build();
            
        searchButton = new StateImage.Builder()
            .addPatterns("searchButton")
            .setSearchRegionForAllPatterns(new Region(0, 0, 500, 100))  // Top area only
            .build();
            
        // Initialize with action history for mock testing
        initializeActionHistory();
    }
    
    private void initializeActionHistory() {
        // Add mock action records for testing using the utility class
        // TODO: ActionHistory API needs to be updated for current version
        // toWorldButton.getPatterns().get(0).getActionHistory()
        //     .addSnapshot(ActionRecordTestUtils.createActionRecord(0.95, 220, 600, 20, 20));
            
        // searchButton.getPatterns().get(0).getActionHistory()
        //     .addSnapshot(ActionRecordTestUtils.createActionRecord(0.92, 250, 50, 100, 30));
    }
}
```

## Key Features of Modern States

### 1. Automatic Registration
No manual registration needed - the `@State` annotation handles everything:
```java
@State  // That's it! State is automatically registered
public class WorldState {
    // State definition...
}
```

### 2. Direct Component Access
States are Spring components, accessible via dependency injection:
```java
@Component
public class MyAutomation {
    private final HomeState homeState;
    private final WorldState worldState;
    
    public MyAutomation(HomeState homeState, WorldState worldState) {
        this.homeState = homeState;
        this.worldState = worldState;
    }
}
```

### 3. StateImage with Modern ActionConfig
Use the fluent builder pattern with modern configuration:
```java
StateImage island = new StateImage.Builder()
    .addPatterns("island_farm", "island_mine")  // Multiple patterns
    // .setFixed(false)  // Method doesn't exist in current API
    .setSearchRegionForAllPatterns(new Region(100, 100, 800, 600))
    .build();

// Add action history for testing (optional)
island.getPatterns().forEach(pattern -> 
    pattern.getActionHistory().addSnapshot(
        new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(400, 300, 50, 50)
                .setSimScore(0.90)
                .build())
            .setActionSuccess(true)
            .build()
    )
);
```

## Defining Multiple UI Elements

States typically contain multiple UI elements:

```java
@State
@Component
@Getter
public class WorldState {
    private final StateImage minimap;
    private final StateImage castle;
    private final StateImage farms;
    private final StateImage mines;
    private final StateRegion gameArea;
    
    public WorldState() {
        minimap = new StateImage.Builder()
            .addPatterns("minimap")
            // .setFixed(true)  // Method doesn't exist in current API
            .setSearchRegionForAllPatterns(new Region(900, 0, 124, 124))
            .build();
            
        castle = new StateImage.Builder()
            .addPatterns("castle")
            .build();
            
        farms = new StateImage.Builder()
            .addPatterns("farms")
            .build();
            
        mines = new StateImage.Builder()
            .addPatterns("mines")
            .build();
            
        // Define regions for area-based interactions
        gameArea = new StateRegion.Builder()
            .setSearchRegion(new Region(100, 100, 800, 600))
            .build();
    }
}
```

## State Attributes

### Initial State
Mark the entry point of your automation:
```java
@State(initial = true)
public class LoginState {
    // First state in the automation flow
}
```

### State Names
By default, the class name is used. Override if needed:
```java
@State(name = "MainMenu")
public class HomeState {
    // Will be registered as "MainMenu" instead of "HomeState"
}
```

## Best Practices

### 1. One State Per Screen
Each distinct screen or UI context should have its own state:
```java
@State public class LoginState { }
@State public class DashboardState { }  
@State public class SettingsState { }
```

### 2. Use Descriptive Names
State names should clearly indicate the UI context:
```java
// Good
@State public class ProductDetailsState { }
@State public class ShoppingCartState { }

// Avoid
@State public class State1 { }
@State public class PageState { }
```

### 3. Leverage Fixed Positions
Mark UI elements that don't move as fixed for faster matching:
```java
StateImage logo = new StateImage.Builder()
    .addPatterns("company_logo")
    // .setFixed(true)  // Method doesn't exist in current API - Always in same position
    .setSearchRegionForAllPatterns(new Region(10, 10, 100, 50))
    .build();
```

### 4. Define Search Regions
Constrain searches to improve performance:
```java
StateImage submitButton = new StateImage.Builder()
    .addPatterns("submit")
    .setSearchRegionForAllPatterns(new Region(0, 500, 1024, 268))  // Bottom area only
    .build();
```

## Working with State Objects

### StateImage
For image-based UI elements:
```java
StateImage button = new StateImage.Builder()
    .addPatterns("button.png")
    .setSearchRegionForAllPatterns(region)
    .build();

// Optionally add action history for mock testing
button.getPatterns().get(0).getActionHistory().addSnapshot(
    new ActionRecord.Builder()
        .setActionConfig(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.90)
            .build())
        .addMatch(new Match.Builder()
            .setRegion(100, 200, 80, 30)
            .setSimScore(0.92)
            .build())
        .setActionSuccess(true)
        .build()
);
```

### StateRegion
For area-based interactions:
```java
StateRegion dropZone = new StateRegion.Builder()
    .setSearchRegion(new Region(200, 200, 400, 300))
    .build();
```

### StateLocation
For specific coordinates:
```java
StateLocation centerPoint = new StateLocation.Builder()
    .setLocation(new Location(512, 384))
    .build();
```

### StateString
For text elements:
```java
StateString username = new StateString.Builder()
    .setString("admin")
    .setSearchRegion(new Region(300, 200, 200, 30))
    .build();
```

## ActionRecords for Mock Testing

For integration testing, you can initialize states with ActionRecords that provide mock data:

```java
import io.github.jspinak.brobot.tools.testing.data.ActionRecordTestUtils;

@State
@Component
@Getter
public class TestableState {
    private final StateImage element;
    
    public TestableState() {
        element = new StateImage.Builder()
            .addPatterns("element.png")
            .build();
        
        // Initialize with realistic test data
        initializeForTesting();
    }
    
    private void initializeForTesting() {
        Pattern pattern = element.getPatterns().get(0);
        ActionHistory history = pattern.getActionHistory();
        
        // Add multiple snapshots for varied test outcomes using the utility class
        history.addSnapshot(ActionRecordTestUtils.createSuccessRecord(0.98, 100, 200));
        history.addSnapshot(ActionRecordTestUtils.createSuccessRecord(0.95, 102, 201));
        history.addSnapshot(ActionRecordTestUtils.createFailureRecord());
    }
}
```

**Note**: The ActionHistory class is currently transitioning from ActionOptions to ActionConfig. While the examples above show the intended modern API, some internal ActionHistory methods may still require ActionOptions.Action for backward compatibility.

## Integration with Spring Boot

States integrate seamlessly with Spring Boot's dependency injection:

```java
@Service
public class AutomationService {
    private final HomeState homeState;
    private final Action action;
    
    @Autowired
    public AutomationService(HomeState homeState, Action action) {
        this.homeState = homeState;
        this.action = action;
    }
    
    public void navigateToWorld() {
        action.click(homeState.getToWorldButton());
    }
}
```

## Complete Example

Here's a complete state definition for our Day of Towers automation:

```java
@State(initial = true)
@Component
@Getter
@Slf4j
public class MainMenuState {
    private final StateImage playButton;
    private final StateImage settingsButton;
    private final StateImage exitButton;
    private final StateRegion menuArea;
    
    public MainMenuState() {
        log.info("Initializing MainMenuState");
        
        playButton = new StateImage.Builder()
            .addPatterns("play_button", "play_button_hover")
            // .setFixed(true)  // Method doesn't exist in current API
            .setSearchRegionForAllPatterns(new Region(400, 300, 224, 80))
            .build();
            
        settingsButton = new StateImage.Builder()
            .addPatterns("settings_icon")
            // .setFixed(true)  // Method doesn't exist in current API
            .setSearchRegionForAllPatterns(new Region(950, 10, 64, 64))
            .build();
            
        exitButton = new StateImage.Builder()
            .addPatterns("exit_button")
            // .setFixed(true)  // Method doesn't exist in current API
            .setSearchRegionForAllPatterns(new Region(400, 500, 224, 80))
            .build();
            
        menuArea = new StateRegion.Builder()
            .setSearchRegion(new Region(0, 0, 1024, 768))
            .build();
    }
}
```

## Next Steps

Now that you understand states, learn about [Transitions](transitions) to connect states and create automation workflows.