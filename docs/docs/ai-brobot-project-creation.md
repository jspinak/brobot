# AI Instructions for Creating Brobot Projects

## Project Setup Sequence

1. **Directory Structure**
   ```
   project-root/
   ├── src/main/java/com/[organization]/[project]/
   │   ├── states/
   │   ├── transitions/
   │   ├── automation/
   │   ├── config/
   │   └── [ProjectName]Application.java
   ├── src/main/resources/
   │   ├── application.properties
   │   └── application.yml          # Alternative to properties
   ├── images/                      # Image resources in project root
   │   └── [state-name]/
   ├── history/                     # Destination for illustrated screenshots from unit testing
   ├── build.gradle
   └── settings.gradle
   ```

2. **Gradle Configuration**
   - Use Spring Boot 3.2.0+ with Java 21
   - For local Brobot library development, use composite build:
   ```gradle
   // build.gradle
   dependencies {
       // Brobot 1.1.0+ includes Spring, Lombok, SLF4J as transitive dependencies
       implementation 'io.github.jspinak:brobot:1.1.0'
       
       // Only need annotation processor for Lombok
       annotationProcessor 'org.projectlombok:lombok:1.18.32'
       
       // Add test dependencies as needed
       testImplementation 'org.springframework.boot:spring-boot-starter-test'
   }
   ```
   ```gradle
   // settings.gradle
   includeBuild('../brobot') {
       dependencySubstitution {
           substitute module('io.github.jspinak:brobot') using project(':library')
       }
   }
   ```

3. **State Creation Pattern (Modern with Annotations)**
   ```java
   @State  // Automatically registers as Spring component and Brobot state
   @Getter
   @Slf4j
   public class ExampleState {
       private final State state;
       private final StateImage mainImage;  // Direct access to components
       
       public enum Name implements StateEnum {
           EXAMPLE
       }
       
       public ExampleState() {
           // Store component for direct access
           mainImage = new StateImage.Builder()
               .addPatterns("folder/image-name1", "folder/image-name2")  // No .png extension needed
               .setName("MainImage")
               .build();
           
           state = new State.Builder(Name.EXAMPLE)
               .addStateImages(mainImage)
               .build();
       }
   }
   ```
   
   **For Initial States:**
   ```java
   @State(initial = true)  // Marks as initial state for state machine
   @Getter
   @Slf4j
   public class HomeState {
       // State definition
   }
   ```

   ### Traditional State Pattern (Without @State)
   
   For reference, here's the traditional approach without annotations (as shown in the research paper):
   
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
   
   This approach requires:
   - Manual state registration in constructor
   - Explicit State object creation
   - Direct StateService dependency
   - Uses older StateImageObject API
   
   ### When to Define an Explicit State Object
   
   **With @State annotation, you have two options:**
   
   1. **Components Only (Recommended)** - Let the framework handle State creation:
      ```java
      @State
      @Getter
      public class MenuState {
          private final StateImage button;  // Only components
          
          public MenuState() {
              button = new StateImage.Builder()...
          }
      }
      ```
   
   2. **Explicit State (Rare)** - Only if you need direct State access:
      ```java
      @State
      @Getter
      public class MenuState {
          private final State state;  // Explicit State object
          private final StateImage button;
          
          public MenuState() {
              button = new StateImage.Builder()...
              state = new State.Builder("MENU")...
          }
      }
      ```
   
   **Use explicit State only when:**
   - Migrating legacy code gradually
   - You need the State object for specific framework interactions
   - You're not using @State annotation (traditional approach)
   
   **Otherwise, stick with components only** - it's cleaner and the framework handles everything automatically.

4. **Transitions (Two Approaches)**

   **Modern Approach with @Transition Annotation (Recommended):**
   ```java
   @Transition(from = ExampleState.class, to = TargetState.class)
   @RequiredArgsConstructor
   @Slf4j
   public class ExampleToTargetTransition {
       private final ExampleState exampleState;
       private final Action action;
       
       public boolean execute() {
           log.info("Executing transition from Example to Target");
           // Use convenience methods for clean code
           return action.click(exampleState.getMainImage()).isSuccess();
       }
   }
   ```

   **Traditional Approach with JavaStateTransition:**
   ```java
   @Component
   @RequiredArgsConstructor
   @Slf4j
   public class ExampleTransitions {
       private final ExampleState exampleState;
       private final Action action;
       
       public StateTransitions getStateTransitions() {
           return new StateTransitions.Builder(ExampleState.Name.EXAMPLE.toString())
               .addTransition(createOutgoingTransition())
               .addTransitionFinish(() -> verifyInState())
               .build();
       }
       
       private JavaStateTransition createOutgoingTransition() {
           return new JavaStateTransition.Builder()
               .setFunction(() -> executeTransition())
               .addToActivate(TargetState.Name.TARGET.toString())
               .setStaysVisibleAfterTransition(true)
               .build();
       }
       
       private boolean executeTransition() {
           return action.click(exampleState.getMainImage()).isSuccess();
       }
       
       private boolean verifyInState() {
           return action.find(exampleState.getMainImage()).isSuccess();
       }
   }
   ```

5. **Modern Action Patterns**
   - **Use ActionConfig classes, NOT ActionOptions**:
     ```java
     // Good - Modern approach
     PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
     ClickOptions clickOptions = new ClickOptions.Builder().build();
     TypeOptions typeOptions = new TypeOptions.Builder().build();
     ColorFindOptions colorOptions = new ColorFindOptions.Builder().build();
     MotionFindOptions motionOptions = new MotionFindOptions.Builder().build();
     ```
   
   - **Fluent API with Action Chaining (Two Approaches)**:
   
   ### When to Use ActionChainBuilder vs .then()
   
   **Use ActionChainBuilder when you need:**
   - More than 2 actions in sequence
   - Explicit control over chaining strategy (NESTED vs CONFIRM)
   - Chain-level configuration (pauseBeforeBegin, pauseAfterEnd for entire chain)
   - Better readability for complex sequences
   - To build reusable chain configurations
   
   ```java
   // Example: Complex 3+ action chain with strategy control
   ActionChainOptions validateAndSubmit = ActionChainBuilder
       .of(new PatternFindOptions.Builder()
           .setStrategy(PatternFindOptions.Strategy.BEST)
           .build())
       .then(new ColorFindOptions.Builder()
           .setColorStrategy(ColorFindOptions.Color.MU)
           .build())
       .then(new ClickOptions.Builder()
           .setNumberOfClicks(2)
           .build())
       .then(new TypeOptions.Builder()
           .setText("Submit")
           .build())
       .withStrategy(ActionChainOptions.ChainingStrategy.CONFIRM) // Explicit strategy
       .pauseBeforeBegin(2.0)  // Wait before entire chain starts
       .pauseAfterEnd(1.0)     // Wait after entire chain completes
       .build();
   ```
   
   **Use .then() on ActionConfig when you have:**
   - Simple 2-action sequences (find→click, find→type)
   - No need for explicit strategy control (defaults to sequential execution)
   - Quick inline action chains
   - Actions that naturally flow together
   
   ```java
   // Example: Simple find and click - concise and readable
   ActionResult result = action.perform(
       new PatternFindOptions.Builder()
           .setStrategy(PatternFindOptions.Strategy.BEST)
           .then(new ClickOptions.Builder()
               .setNumberOfClicks(1)
               .build())
           .build(),
       stateImage
   );
   
   // Example: Find and type - common pattern
   PatternFindOptions findAndType = new PatternFindOptions.Builder()
       .setStrategy(PatternFindOptions.Strategy.FIRST)
       .then(new TypeOptions.Builder()
           .setText("Hello World")
           .build())
       .build();
   ```
   
   **Key Differences:**
   - **ActionChainBuilder** → Creates ActionChainOptions with full chain control
   - **.then()** → Creates a simple subsequent action list (convenience method)
   - **ActionChainBuilder** → Supports chain-wide configuration and strategies
   - **.then()** → Inherits Action's default sequential execution behavior
   
   **Rule of Thumb:** Start with .then() for simple cases. Switch to ActionChainBuilder when you need more control or have 3+ actions.
   
   ### ConditionalActionChain - The Most Elegant Approach
   
   **ConditionalActionChain provides the cleanest API for conditional execution flows:**
   
   ```java
   // Basic pattern: find → if found do X → if not found do Y
   ConditionalActionChain
       .find(new PatternFindOptions.Builder().build())
       .ifFound(new ClickOptions.Builder().build())
       .ifNotFound(log("Button not found"))
       .perform(action, objectCollection);
   ```
   
   **Key Features:**
   - **Conditional Flow**: `ifFound()` and `ifNotFound()` branches
   - **Sequential Actions**: Use `.then()` to chain actions
   - **Multiple Conditions**: Add multiple `ifFound`/`ifNotFound` handlers
   - **Custom Actions**: Use lambdas for inline custom logic
   - **Automatic Result Propagation**: Results flow through the chain
   
   **Common Patterns:**
   
   1. **Find and Click with Error Handling:**
   ```java
   ConditionalActionChain
       .find(findOptions)
       .ifFound(clickOptions)
       .ifNotFound(log("ERROR: Critical button missing"))
       .perform(action, buttonImage);
   ```
   
   2. **Multi-Step Workflow:**
   ```java
   ConditionalActionChain
       .find(findExportButton)           // Find export button
       .ifFound(clickOptions)            // Click it
       .then(findFileNameField)          // Find filename field
       .ifFound(typeFileName)            // Type the filename
       .then(findConfirmButton)          // Find confirm button
       .ifFound(clickOptions)            // Click confirm
       .perform(action, objectCollection);
   ```
   
   3. **Custom Logic with Lambdas:**
   ```java
   ConditionalActionChain
       .find(criticalElement)
       .ifFoundDo(result -> {
           log.info("Found {} matches", result.getMatchList().size());
           // Custom processing logic
           processMatches(result.getMatchList());
       })
       .ifNotFoundDo(result -> {
           takeScreenshot("error-state");
           notifyTeam("Critical element missing");
       })
       .perform(action, objectCollection);
   ```
   
   4. **Combining with Pure Actions:**
   ```java
   // For simple operations on known locations/regions
   Location buttonLocation = new Location(100, 200);
   action.perform(ActionType.CLICK, buttonLocation);
   
   // For complex conditional flows
   ConditionalActionChain
       .find(dynamicButton)
       .ifFound(click())
       .perform(action, objectCollection);
   ```
   
   **When to Use ConditionalActionChain:**
   - **Always for UI interactions** - Handles element not found gracefully
   - **Multi-step workflows** - Clean sequential flow with error handling
   - **When you need both success and failure handling**
   - **For readable, self-documenting code**
   
   **When NOT to Use:**
   - **Known coordinates** - Use `action.perform(ActionType.CLICK, location)`
   - **Simple unconditional actions** - Use convenience methods
   - **Performance-critical loops** - Use traditional find/action separation
   
   - **Convenience Methods**:
     ```java
     // Simple find
     action.find(state.getImage()).isSuccess()
     
     // Find with timeout
     action.findWithTimeout(10.0, state.getImage()).isSuccess()
     
     // Direct click
     action.click(state.getImage())
     
     // Type with object collection
     action.type(new ObjectCollection.Builder()
         .withStrings(state.getTextCommand())
         .build())
     ```

6. **State Registration with Event Listener (Recommended)**
   ```java
   import org.springframework.boot.context.event.ApplicationReadyEvent;
   import org.springframework.context.event.EventListener;
   import io.github.jspinak.brobot.config.FrameworkInitializer;
   
   @Component
   @RequiredArgsConstructor
   @Slf4j
   public class StateRegistrationListener {
       private final StateService stateService;
       private final StateTransitionStore stateTransitionStore;
       private final StateMemory stateMemory;
       private final FrameworkInitializer frameworkInitializer;
       private final ExampleState exampleState;
       private final ExampleTransitions exampleTransitions;
       
       @EventListener(ApplicationReadyEvent.class)
       public void onApplicationReady() {
           log.info("Application ready - registering states");
           
           try {
               // Register states
               stateService.save(exampleState.getState());
               
               // Register transitions
               stateTransitionStore.add(exampleTransitions.getStateTransitions());
               
               // Critical: Initialize state structure after ALL states registered
               frameworkInitializer.initializeStateStructure();
               
               // Set initial active state
               Long stateId = stateService.getStateId(ExampleState.Name.EXAMPLE.toString());
               if (stateId != null) {
                   stateMemory.addActiveState(stateId);
               }
           } catch (Exception e) {
               log.error("Error registering states: ", e);
           }
       }
   }
   ```
   
   **Why use @EventListener instead of @PostConstruct:**
   - Ensures framework is fully initialized before state registration
   - Prevents image loading errors during initialization
   - Better integration with Spring Boot lifecycle
   - Required call to `frameworkInitializer.initializeStateStructure()` after all states are registered

7. **Spring Boot Application**
   ```java
   @SpringBootApplication
   @ComponentScan(basePackages = {
       "com.yourorg.yourproject",
       "io.github.jspinak.brobot"  // Include Brobot components
   })
   public class YourApplication {
       public static void main(String[] args) {
           SpringApplication.run(YourApplication.class, args);
       }
   }
   ```

8. **Enhanced StateNavigator**
   ```java
   // Add this method to StateNavigator for cleaner code
   public boolean openState(StateEnum stateEnum) {
       return openState(stateEnum.toString());
   }
   
   // Usage becomes cleaner
   stateNavigator.openState(WorkingState.Name.WORKING);
   ```

9. **Image and Resource Organization**
   - Place images in `images/[state-name]/` at project root
   - Create `history/` folder at project root for illustrated test screenshots
   - Build.gradle should copy images to build directory:
     ```gradle
     task copyImages(type: Copy) {
         from 'images'
         into "$buildDir/resources/main/images"
     }
     processResources.dependsOn copyImages
     ```

## Brobot Annotations

### @State Annotation
- **Purpose**: Marks a class as a Brobot state and Spring component
- **Parameters**:
  - `initial`: boolean (default false) - marks as initial state
  - `name`: String (default "") - optional state name override
  - `description`: String (default "") - state documentation
- **Required with**: `@Getter` and `@Slf4j`

### @Transition Annotation
- **Purpose**: Marks a class as a Brobot transition and Spring component
- **Parameters**:
  - `from`: Class\<?\>[] - source state class(es)
  - `to`: Class\<?\>[] - target state class(es)
  - `method`: String (default "execute") - transition method name
  - `priority`: int (default 0) - transition priority
  - `description`: String (default "") - transition documentation
- **Required with**: `@RequiredArgsConstructor` and `@Slf4j`

### @CollectData Annotation (Advanced)
- **Purpose**: Marks methods for automatic ML dataset collection
- **Parameters**:
  - `category`: String - data category (default "general")
  - `features`: String[] - specific features to collect (empty = all)
  - `captureScreenshots`: boolean - capture before/after screenshots (default true)
  - `captureIntermediateStates`: boolean - capture multi-step operations (default false)
  - `samplingRate`: double - collection rate 0.0-1.0 (default 1.0)
  - `maxSamples`: int - max samples to collect, -1 = unlimited (default -1)
  - `onlySuccess`: boolean - collect only successful executions (default false)
  - `format`: DataFormat - storage format (JSON, CSV, BINARY, TFRECORD, PARQUET)
  - `labels`: String[] - labels for supervised learning
- **Use Case**: Training ML models on automation behavior

Example usage:
```java
@CollectData(
    category = "click_accuracy",
    captureScreenshots = true,
    samplingRate = 0.1  // Collect 10% of executions
)
public ActionResult performCriticalClick(StateImage target) {
    // Click logic that will have data collected
    return action.click(target);
}
```

## Best Practices

1. **Code Organization**
   - Keep states simple with direct access to components
   - Use @State and @Transition annotations for cleaner code
   - Separate transitions into dedicated classes
   - Use Spring dependency injection throughout

2. **Modern API Usage**
   - Always use ActionConfig classes (PatternFindOptions, ClickOptions, etc.)
   - Leverage fluent API for action chaining
   - Use convenience methods to reduce boilerplate
   - Implement StateEnum overloads for cleaner code

3. **State Components**
   - Expose frequently used StateImages/StateStrings as fields
   - Create getter methods for direct access
   - Combine related actions in single StateString (e.g., "continue\n")

4. **Error Handling**
   - Wrap transitions in try-catch blocks
   - Log at appropriate levels (info for success, warn for failures)
   - Return boolean from transition functions

5. **Lombok Considerations**
   - Use @RequiredArgsConstructor for dependency injection
   - Use @Slf4j for logging
   - If Lombok issues occur, create manual constructors
   - Ensure Lombok version matches Brobot's (1.18.32)

## Common Issues and Solutions

1. **ActionOptions Deprecated**
   - Always use specific ActionConfig implementations
   - ActionOptions will be removed in future versions

2. **State Component Access**
   - Don't navigate through: `state.getStateImages().iterator().next()`
   - Do provide direct access: `getMainImage()`

3. **Import Paths**
   - Use `io.github.jspinak.brobot.model.state.*` (not primitives)
   - ActionExecution is in `internal.execution` package
   - StateMemory is in `statemanagement` package

4. **Method Names**
   - Use `stateService.save()` not `addState()`
   - Use `stateMemory.addActiveState()` not `setActiveState()`
   - Use `isSuccess()` on ActionResult

5. **Transitions**
   - Use StateTransitions.Builder pattern
   - Use `addTransitionFinish()` not `setTransitionFinish()`
   - Use `setPauseBeforeBegin()` for timeouts

## Testing Patterns

```java
// Quick verification in transitions
private boolean findWithTimeout() {
    PatternFindOptions options = new PatternFindOptions.Builder()
        .setPauseBeforeBegin(10.0)
        .build();
    return action.perform(options, state.getImage()).isSuccess();
}

// Or use convenience method
private boolean findWithTimeout() {
    return action.findWithTimeout(10.0, state.getImage()).isSuccess();
}
```

## Configuration (v1.1.0+)

### Important: Configuration Best Practices

**DO:** Configure through `application.yml` or `application.properties`  
**DO NOT:** Set FrameworkSettings fields directly (deprecated)

```java
// ❌ WRONG - Direct static field access is deprecated
FrameworkSettings.mock = true;
FrameworkSettings.saveHistory = true;

// ✅ CORRECT - Use application properties
// Configure in application.yml or application.properties
```

### Default Configuration
Brobot includes sensible defaults in `brobot-defaults.properties`. You only need to override what you want to change.

### Image Path Configuration
```yaml
# application.yml
brobot:
  core:
    image-path: images/              # Load from project root images folder
    # image-path: classpath:images/  # Load from classpath (after build copies them)
    # image-path: /absolute/path/    # Absolute path
```

### Complete Configuration Example

#### Using application.yml (Recommended):
```yaml
brobot:
  core:
    image-path: images/
    mock: false                      # Enable/disable mock mode
    headless: false                  # Run without GUI (for CI/CD)
    package-name: com.example        # Base package for scanning
  
  startup:
    verify-initial-states: true
    initial-states: HOME,LOGIN
    fallback-search: true
    startup-delay: 2
  
  mouse:
    move-delay: 0.5                  # Delay for mouse movements
    pause-before-down: 0.0           # Pause before mouse down
    pause-after-down: 0.0            # Pause after mouse down
    pause-before-up: 0.0             # Pause before mouse up
    pause-after-up: 0.0              # Pause after mouse up
  
  mock:                              # Mock mode timings (seconds)
    time-find-first: 0.1
    time-find-all: 0.2
    time-drag: 0.3
    time-click: 0.05
    time-move: 0.1
    time-find-histogram: 0.3
    time-find-color: 0.3
    time-classify: 0.4
  
  screenshot:
    save-snapshots: false            # Save screenshots during execution
    save-history: false              # Save illustrated action history
    path: screenshots/               # Screenshot directory
    filename: screen                 # Screenshot filename prefix
    history-path: history/           # History directory
    history-filename: hist           # History filename prefix
  
  illustration:                      # What to draw in history screenshots
    draw-find: true
    draw-click: true
    draw-drag: true
    draw-move: true
    draw-highlight: true
    draw-repeated-actions: true
    draw-classify: true
    draw-define: true
  
  analysis:                          # Color analysis settings
    k-means-in-profile: 5
    max-k-means-to-store: 10
  
  recording:                         # Screen recording settings
    enabled: false
    path: recordings/
    fps: 10
    quality: 0.7
  
  dataset:                           # ML dataset generation
    enabled: false
    path: dataset/
    save-format: JSON
  
  testing:                           # Unit test settings
    mock: true
    save-snapshots: true
```

#### Using application.properties:
```properties
# Core settings
brobot.core.image-path=images/
brobot.core.mock=false
brobot.core.headless=false
brobot.core.package-name=com.example

# Startup settings
brobot.startup.verify-initial-states=true
brobot.startup.initial-states=HOME,LOGIN
brobot.startup.fallback-search=true
brobot.startup.startup-delay=2

# Mouse settings
brobot.mouse.move-delay=0.5
brobot.mouse.pause-before-down=0.0
brobot.mouse.pause-after-down=0.0

# Mock mode timings
brobot.mock.time-find-first=0.1
brobot.mock.time-find-all=0.2
brobot.mock.time-click=0.05

# Screenshot settings
brobot.screenshot.save-snapshots=false
brobot.screenshot.save-history=false
brobot.screenshot.path=screenshots/
brobot.screenshot.history-path=history/

# Illustration settings
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true

# Testing settings
brobot.testing.mock=true
brobot.testing.save-snapshots=true
```

### Configuration Classes

**BrobotProperties**: The main configuration class that maps to `brobot.*` properties
- Uses Spring's `@ConfigurationProperties(prefix = "brobot")`
- Automatically initialized on startup
- Values are copied to FrameworkSettings for backward compatibility

**Key Configuration Groups**:
- `brobot.core.*` - Essential framework settings
- `brobot.mouse.*` - Mouse action timing and behavior
- `brobot.mock.*` - Simulated execution timings
- `brobot.screenshot.*` - Screen capture and history
- `brobot.illustration.*` - Visual feedback settings
- `brobot.analysis.*` - Color profiling settings
- `brobot.testing.*` - Test-specific overrides

## Initial State Verification (v1.1.0+)

Modern Brobot provides automatic initial state verification:

### Configuration-Based (Recommended)
```properties
# application.properties
brobot.startup.verify-initial-states=true
brobot.startup.initial-states=HOME,LOGIN
brobot.startup.fallback-search=true
brobot.startup.startup-delay=2
```

### Programmatic Usage
```java
@Component
public class MyAutomation implements ApplicationRunner {
    private final InitialStateVerifier stateVerifier;
    
    @Override
    public void run(ApplicationArguments args) {
        // Simple verification
        stateVerifier.verify(HomePage.Name.HOME, LoginPage.Name.LOGIN);
        
        // Advanced verification
        stateVerifier.builder()
            .withStates(HomePage.Name.HOME, Dashboard.Name.MAIN)
            .withFallbackSearch(true)
            .activateFirstOnly(true)
            .verify();
    }
}
```

### Mock Testing Support
```java
// For testing with probabilities
stateVerifier.builder()
    .withState(HomePage.Name.HOME, 70)    // 70% probability
    .withState(LoginPage.Name.LOGIN, 30)  // 30% probability
    .verify();
```

## Color Finding and Motion Detection

### Color Finding with ColorFindOptions

Brobot supports three color analysis strategies:

```java
// 1. KMEANS - Find dominant colors using k-means clustering
ColorFindOptions kmeansColor = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.KMEANS)
    .setKmeans(3)  // Find 3 dominant colors
    .setDiameter(5) // Minimum cluster size
    .setSimilarity(0.9)
    .build();

// 2. MU - Use mean color statistics (default)
ColorFindOptions meanColor = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.MU)
    .setDiameter(5)
    .setSimilarity(0.95)
    .build();

// 3. CLASSIFICATION - Multi-class pixel classification
ColorFindOptions classification = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
    .setSimilarity(0.8)
    .build();
```

### Nested vs Confirmed Finds with ActionChainOptions

**Key Difference**: 
- **NESTED**: Each action searches WITHIN the results of the previous action
- **CONFIRM**: Each action validates the results of the previous action; returns original match if confirmed

#### Nested Finds Example
```java
// Find yellow bars: first find all bars, then find yellow within those bars
PatternFindOptions findBars = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setSimilarity(0.7)  // Lower to catch all bars
    .build();

ColorFindOptions findYellow = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.MU)
    .setDiameter(10)
    .setSimilarity(0.9)
    .build();

ActionChainOptions nestedFind = new ActionChainOptions.Builder(findBars)
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(findYellow)
    .build();

// Result: Returns yellow regions found INSIDE the bar patterns
Action action = // obtain from Spring context
ActionResult yellowRegions = action.perform(nestedFind, objectCollection);
```

#### Confirmed Finds Example
```java
// Confirm buttons by checking they have the right color
PatternFindOptions findButtons = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setSimilarity(0.8)
    .build();

ColorFindOptions confirmColor = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.MU)
    .setSimilarity(0.85)
    .build();

ActionChainOptions confirmedFind = new ActionChainOptions.Builder(findButtons)
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(confirmColor)
    .build();

// Result: Returns original button matches that passed color confirmation
ActionResult confirmedButtons = action.perform(confirmedFind, objectCollection);
```

### Motion Detection with MotionFindOptions

Motion detection requires 3 scenes to determine direction of movement:

```java
// Configure motion detection
MotionFindOptions motionOptions = new MotionFindOptions.Builder()
    .setMaxMovement(150)     // Max pixels object can move between scenes
    .setMinArea(100)         // Filter out small movements
    .setMaxMatchesToActOn(5) // Track up to 5 moving objects
    .setSimilarity(0.75)     // Match threshold across scenes
    .build();

// Three ways to provide scenes:

// 1. From files (mock mode)
// Configure in application.properties:
// brobot.core.mock=true
// brobot.screenshot.path=screenshots/
ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
ActionResult motionFromFiles = action.perform(motionOptions, emptyCollection);

// 2. From Brobot images
ObjectCollection scenes = new ObjectCollection.Builder()
    .withScenes(scene1, scene2, scene3)
    .build();
ActionResult motionFromImages = action.perform(motionOptions, scenes);

// 3. From live screen (mock=false)
// brobot.core.mock=false
ActionResult motionFromScreen = action.perform(motionOptions, emptyCollection);

// Access results
List<Match> movingObjects = motionFromScreen.getMatchList();
SceneAnalysisCollection sceneAnalysis = motionFromScreen.getSceneAnalysis();
```

### Histogram Finding

```java
HistogramFindOptions histogramFind = new HistogramFindOptions.Builder()
    .setSimilarity(0.8)
    .setBinOptions(HSVBinOptions.builder()
        .hueBins(90)
        .saturationBins(2)
        .valueBins(1))
    .setMaxMatchesToActOn(5)
    .build();
```

### Practical Color + Pattern Example

```java
// Find red error buttons using pattern + color confirmation
public boolean clickErrorButton() {
    // Define the chain
    ActionChainOptions findRedButton = ActionChainBuilder
        .of(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .setSimilarity(0.7)
            .build())
        .then(new ColorFindOptions.Builder()
            .setColorStrategy(ColorFindOptions.Color.MU)
            .setSimilarity(0.9)
            .build())
        .withStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
        .build();
    
    // Execute find
    ActionResult result = action.perform(findRedButton, 
        new ObjectCollection.Builder()
            .withImages(errorButtonImage)
            .build());
    
    // Click if found
    if (result.isSuccess() && !result.getMatchList().isEmpty()) {
        return action.click(result.getMatchList().get(0)).isSuccess();
    }
    return false;
}
```

## Summary

Modern Brobot development emphasizes:
- Clean, readable code with minimal boilerplate
- Type safety through StateEnum and proper generics
- Dependency injection with Spring Boot
- Fluent APIs and method chaining
- **ConditionalActionChain for elegant UI interactions with error handling**
- Direct access to state components
- Automatic initial state verification
- Configuration-driven behavior
- Advanced color and motion detection capabilities
- Flexible action chaining with nested/confirmed strategies

Follow these patterns for maintainable, professional Brobot applications.