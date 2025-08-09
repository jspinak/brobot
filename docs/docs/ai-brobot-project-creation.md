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
       private final StateImage mainImage;  // Direct access to components
       private final StateImage secondaryImage;
       private final StateString textRegion;
       
       public ExampleState() {
           // Create state components
           mainImage = new StateImage.Builder()
               .addPatterns("folder/image-name1", "folder/image-name2")  // No .png extension needed
               .setName("MainImage")
               .build();
               
           secondaryImage = new StateImage.Builder()
               .addPatterns("folder/secondary-image")
               .setName("SecondaryImage")
               .build();
               
           textRegion = new StateString.Builder()
               .setName("TextRegion")
               .setRegion(new Region(100, 100, 200, 50))
               .build();
           
           // No need to build State object - annotation handles it
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
   
   **Important Naming Convention:**
   - Classes with `@State` annotation are registered with the "State" suffix removed
   - Example: `WorldState` class is registered as `"World"`
   - Example: `HomeState` class is registered as `"Home"`
   - When referencing states (e.g., in `targetStateName`), use the name without "State"

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

4. **Declarative Search Region Definition (v1.1.0+)**

   Brobot supports defining search regions relative to other objects, even across states:

   ```java
   @State
   @Getter
   public class WorkingState {
       private final StateImage claudeIcon;
       
       public WorkingState() {
           // ClaudeIcon's search region is defined relative to ClaudePrompt
           claudeIcon = new StateImage.Builder()
               .addPatterns("working/claude-icon-1", "working/claude-icon-2")
               .setName("ClaudeIcon")
               .setSearchRegionOnObject(SearchRegionOnObject.builder()
                   .targetType(StateObject.Type.IMAGE)
                   .targetStateName("Prompt")  // Can reference other states
                   .targetObjectName("ClaudePrompt")
                   .adjustments(MatchAdjustmentOptions.builder()
                       .addX(3)    // Offset from ClaudePrompt location
                       .addY(10)
                       .addW(30)   // Expand width by 30 pixels
                       .addH(55)   // Expand height by 55 pixels
                       .build())
                   .build())
               .build();
       }
   }
   ```

   **Key Benefits:**
   - **Automatic Updates**: When ClaudePrompt is found, ClaudeIcon's search region updates automatically
   - **Cross-State References**: Objects can depend on objects in other states
   - **No Manual Calculations**: Framework handles all region math
   - **Cleaner Code**: Define relationships declaratively, not imperatively

   **Common Patterns:**
   ```java
   // Simple relative positioning
   .setSearchRegionOnObject(SearchRegionOnObject.builder()
       .targetType(StateObject.Type.IMAGE)
       .targetStateName("Menu")
       .targetObjectName("MenuBar")
       .adjustments(MatchAdjustmentOptions.builder()
           .addY(50)  // 50 pixels below menu bar
           .build())
       .build())
   
   // Fixed dimensions with relative position
   .setSearchRegionOnObject(SearchRegionOnObject.builder()
       .targetType(StateObject.Type.IMAGE)
       .targetStateName("Form")
       .targetObjectName("FormTitle")
       .adjustments(MatchAdjustmentOptions.builder()
           .addY(100)     // Below title
           .absoluteW(200) // Fixed 200x50 region
           .absoluteH(50)
           .build())
       .build())
   ```

   **How It Works:**
   1. Dependencies are registered when states load (via `SearchRegionDependencyInitializer`)
   2. When a FIND operation succeeds, `FindPipeline` updates dependent regions
   3. Next search uses the updated region automatically

   **For detailed documentation, see:** `docs/03-core-library/guides/declarative-region-definition.md`

5. **Transitions (Two Approaches)**

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
   - **ActionChainBuilder**:
     - Creates ActionChainOptions with full chain control
     - Supports chain-wide configuration and strategies (NESTED, CONFIRM)
     - Best for complex multi-action sequences requiring precise control
   - **.then() method**:
     - Creates a simple subsequent action list (convenience method)
     - Inherits the parent action's default sequential execution behavior
     - Best for simple 2-3 step sequences without special requirements
   
   **Rule of Thumb:** Start with .then() for simple cases. Switch to ActionChainBuilder when you need more control or have 3+ actions.
   
   ### ConditionalActionChain - The Foundation
   
   **ConditionalActionChain provides the core fluent API for building complex action sequences with conditional execution:**
   
   Located in the examples at `examples/03-core-library/action-config/conditional-chains-examples/`, this class demonstrates the patterns for building conditional action sequences that handle different UI states and conditions.
   
   ```java
   import io.github.jspinak.brobot.model.conditional.ConditionalActionChain;
   
   // Basic usage pattern
   ConditionalActionChain
       .find(loginButton)           // Start with find
       .ifFound(clickOptions)        // Execute if found
       .ifNotFoundLog("Not found")   // Log if not found
       .then(usernameField)          // Continue to next element
       .ifFoundType("username")      // Type if found
       .perform(action, objectCollection);
   ```
   
   **Key Methods:**
   - **Starting methods**: `find()`, `start()` - Begin a chain
   - **Conditional methods**: `ifFound()`, `ifNotFound()`, `ifFoundDo()`, `ifNotFoundDo()`
   - **Sequential methods**: `then()`, `always()` - Continue the chain
   - **Action methods**: `click()`, `type()`, `clearAndType()`, `highlight()`
   - **Control methods**: `stopChain()`, `throwError()`, `wait()`
   - **Utility methods**: `takeScreenshot()`, `log()`, `logAction()`
   
   ### EnhancedConditionalActionChain - The Most Elegant Approach
   
   **EnhancedConditionalActionChain extends ConditionalActionChain with additional convenience methods and better sequential composition:**
   
   ```java
   // Basic pattern: find → if found do X → if not found do Y
   EnhancedConditionalActionChain
       .find(buttonImage)
       .ifFoundClick()
       .ifNotFoundLog("Button not found")
       .perform(action, new ObjectCollection.Builder().build());
   ```
   
   **Key Feature - The then() Method:**
   ```java
   // Sequential actions with then() - the missing piece!
   EnhancedConditionalActionChain
       .find(loginButton)
       .ifFoundClick()
       .then(usernameField)  // Move to next element
       .ifFoundType("username")
       .then(passwordField)  // Continue the flow
       .ifFoundType("password")
       .then(submitButton)   // Keep going
       .ifFoundClick()
       .perform(action, new ObjectCollection.Builder().build());
   ```
   
   **Enhanced Features:**
   - **Sequential Composition**: The crucial `then()` method for multi-step workflows
   - **Convenience Methods**: Direct `click()`, `type()`, `scrollDown()` methods
   - **Keyboard Shortcuts**: Built-in `pressEnter()`, `pressTab()`, `pressCtrlS()`
   - **No wait() Method**: Follows model-based principles - timing via action configurations
   - **Control Flow**: `stopChain()`, `retry()`, `throwError()` methods
   - **Conditional Logic**: Proper if/then/else execution with `ifFound()` and `ifNotFound()`
   
   **Common Patterns:**
   
   1. **Login Flow with Sequential Actions:**
   ```java
   EnhancedConditionalActionChain
       .find(loginButton)
       .ifFoundClick()
       .then(usernameField)      // Sequential action
       .ifFoundType("username")
       .then(passwordField)      // Continue flow
       .ifFoundType("password")
       .then(submitButton)       // Keep going
       .ifFoundClick()
       .perform(action, new ObjectCollection.Builder().build());
   ```
   
   2. **Form Filling with Convenience Methods:**
   ```java
   EnhancedConditionalActionChain
       .find(formTitle)
       .ifNotFoundDo(res -> { throw new RuntimeException("Form not found"); })
       .then(nameField)
       .ifFoundClick()
       .clearAndType("John Doe")    // Convenience method
       .pressTab()                   // Keyboard shortcut
       .type("john@example.com")
       .then(submitButton)
       .ifFoundClick()
       .takeScreenshot("form-submitted")
       .perform(action, new ObjectCollection.Builder().build());
   ```
   
   3. **Error Handling with Control Flow:**
   ```java
   EnhancedConditionalActionChain
       .find(submitButton)
       .ifFoundClick()
       .then(errorDialog)
       .ifFoundLog("Error appeared")
       .stopIf(res -> res.getText() != null && 
               !res.getText().isEmpty() && 
               res.getText().get(0).contains("CRITICAL"))
       .then(retryButton)
       .ifFoundClick()
       .perform(action, new ObjectCollection.Builder().build());
   ```
   
   4. **Retry Pattern and Keyboard Shortcuts:**
   ```java
   // Retry with convenience methods
   EnhancedConditionalActionChain
       .retry(new PatternFindOptions.Builder().build(), 3)
       .ifFoundClick()
       .ifNotFoundLog("Failed after retries")
       .perform(action, objectCollection);
   
   // Keyboard shortcuts workflow
   EnhancedConditionalActionChain
       .find(editorField)
       .ifFoundClick()
       .pressCtrlA()      // Select all
       .pressDelete()     // Delete
       .type("New text")
       .pressCtrlS()      // Save
       .perform(action, new ObjectCollection.Builder().build());
   ```
   
   **When to Use EnhancedConditionalActionChain:**
   - **Always for UI interactions** - Handles element not found gracefully
   - **Multi-step workflows** - Clean sequential flow with error handling
   - **When you need both success and failure handling**
   - **For readable, self-documenting code**
   
   **When NOT to Use:**
   - **Known coordinates** - Use `action.perform(ActionType.CLICK, location)`
   - **Simple unconditional actions** - Use convenience methods
   - **Performance-critical loops** - Use traditional find/action separation
   
   ### Real-World ConditionalActionChain Examples
   
   **Complex Dialog Handling:**
   ```java
   // Handle multi-path dialog with various UI states
   ConditionalActionChain
       .find(dialogTitle)
       .ifNotFoundLog("Dialog not present")
       .stopChain()  // Exit early if no dialog
       .then(errorMessage)
       .ifFoundLog("Error detected")
       .ifFoundDo(result -> {
           // Custom error handling logic
           String error = result.getText().get(0);
           logger.error("Dialog error: {}", error);
       })
       .then(retryButton)
       .ifFound(new ClickOptions.Builder().build())
       .ifNotFound(cancelButton)
       .ifFound(new ClickOptions.Builder().build())
       .perform(action, objectCollection);
   ```
   
   **Form Validation Flow:**
   ```java
   // Validate and submit form with error highlighting
   ConditionalActionChain
       .start(validateButton)  // Start without initial find
       .click()
       .wait(0.5)  // Allow validation to complete
       .then(errorFields)
       .ifFoundDo(result -> {
           // Highlight all error fields
           result.getMatchList().forEach(match -> {
               action.highlight(match.getRegion());
           });
       })
       .highlightErrors()  // Additional error highlighting
       .ifNotFound(submitButton)  // No errors, submit
       .ifFoundClick()
       .logAction()
       .perform(action, objectCollection);
   ```
   
   **State-Aware Navigation:**
   ```java
   // Navigate based on current state
   ConditionalActionChain
       .checkPrerequisites()  // Custom prerequisite check
       .then(homeIcon)
       .ifFound(new ClickOptions.Builder().build())
       .waitVanish(loadingSpinner)  // Wait for load
       .then(navigationMenu)
       .ifNotFoundLog("Menu not accessible")
       .throwError("Navigation failed")
       .ifFound(menuItems)
       .analyzeItem()  // Analyze menu structure
       .processItemDialog()  // Handle item selection
       .cleanupResources()  // Cleanup after navigation
       .perform(action, objectCollection);
   ```
   
   **Document Processing:**
   ```java
   // Process documents with different handlers
   ConditionalActionChain
       .find(documentIcon)
       .ifFoundDo(result -> {
           // Determine document type
           String docType = analyzeDocumentType(result);
           context.put("docType", docType);
       })
       .then(pdfIcon)
       .ifFound(openDocument())  // PDF handler
       .ifNotFound(imageIcon)
       .ifFound(viewImage())     // Image handler
       .ifNotFound(textIcon)
       .ifFound(openDocument())  // Text handler
       .always(showProperties()) // Always show properties
       .perform(action, objectCollection);
   ```
   
   **Keyboard Navigation Example:**
   ```java
   // Navigate using keyboard shortcuts
   ConditionalActionChain
       .find(textEditor)
       .ifFoundClick()
       .pressCtrlA()      // Select all
       .pressDelete()     // Clear
       .type("New content")
       .pressCtrlS()      // Save
       .handleConfirmation()  // Handle save dialog
       .perform(action, objectCollection);
   ```
   
   **Checkbox and Toggle Handling:**
   ```java
   // Smart checkbox handling
   ConditionalActionChain
       .find(checkbox)
       .clickIfNotChecked()  // Only click if unchecked
       .then(relatedOption)
       .ifFoundLog("Related option appeared")
       .validateFields()  // Validate dependent fields
       .perform(action, objectCollection);
   ```
   
   **Region-Based Actions:**
   ```java
   // Work with specific regions
   Region searchArea = new Region(100, 100, 400, 300);
   Region clickArea = new Region(150, 150, 50, 30);
   
   ConditionalActionChain
       .find(new PatternFindOptions.Builder()
           .setSearchRegion(searchArea)
           .build())
       .ifFound(highlightRegion(searchArea))
       .clickRegion(clickArea)
       .perform(action, objectCollection);
   ```
   
   **ConditionalActionWrapper for Spring Applications:**
   
   When using Spring Boot, you can use `ConditionalActionWrapper` with EnhancedConditionalActionChain:
   
   ```java
   @Component
   public class LoginAutomation {
       @Autowired
       private ConditionalActionWrapper actions;
       
       public void performLogin(StateImage loginButton, String username) {
           // Simple find and click
           actions.findAndClick(loginButton);
           
           // Find and type
           actions.findAndType(usernameField, username);
           
           // Complex conditional chain
           actions.createChain()
               .find(submitButton)
               .ifFound(ConditionalActionWrapper.click())
               .ifNotFoundLog("Submit button not found")
               .execute();
       }
   }
   ```
   
   **ConditionalActionWrapper vs FindAndClick/FindAndType:**
   
   - **FindAndClick/FindAndType**: Simple composite actions that always execute all steps
     ```java
     // Always finds then clicks, no conditions
     FindAndClick findAndClick = new FindAndClick.Builder()
         .withSimilarity(0.9)
         .build();
     action.perform(findAndClick, objectCollection);
     ```
   
   - **ConditionalActionWrapper**: Spring component with conditional execution
     ```java
     // Only clicks if found, with logging
     @Autowired ConditionalActionWrapper actions;
     actions.findAndClick(submitButton);  // Handles errors gracefully
     ```
   
   **Use ConditionalActionWrapper when:**
   - Working in a Spring Boot application
   - Need conditional execution paths
   - Want automatic error handling and logging
   - Building complex conditional workflows
   
   **Use FindAndClick/FindAndType when:**
   - Not using Spring dependency injection
   - Always want to execute all steps
   - Working with simple, predictable sequences
   
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
   - Use declarative search regions for UI elements with predictable relationships

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

6. **Declarative Search Regions Not Working**
   - Verify target state and object names match exactly (case-sensitive)
   - Check logs for "Registered search region dependency" messages
   - Ensure target object has been found at least once
   - Enable debug logging: `logging.level.io.github.jspinak.brobot.action.internal.region=DEBUG`
   - Verify Spring component scanning includes `io.github.jspinak.brobot`

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

## Movement and Navigation in v1.1.0

### Available Movement APIs

Brobot v1.1.0 provides comprehensive movement control:

#### Mouse Movement
```java
// Direct mouse movement with speed control
MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
    .setMoveMouseDelay(0.5f)  // 0.0 = instant, 1.0 = slow
    .setPauseAfterEnd(1.0)
    .build();

action.move(moveOptions, objectCollection);
```

#### Scrolling
```java
// Native mouse wheel scrolling
ScrollOptions scrollDown = new ScrollOptions.Builder()
    .setDirection(ScrollOptions.Direction.DOWN)
    .setScrollSteps(5)  // Number of scroll wheel clicks
    .setPauseAfterEnd(0.5)
    .build();

action.scroll(scrollDown, objectCollection);
```

#### Mouse Button Control
```java
// Press and hold mouse button
MouseDownOptions pressOptions = new MouseDownOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .button(MouseButton.LEFT)
        .pauseAfterMouseDown(0.5)
        .build())
    .build();

action.mouseDown(pressOptions, location);

// Release mouse button
MouseUpOptions releaseOptions = new MouseUpOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .button(MouseButton.LEFT)
        .build())
    .build();

action.mouseUp(releaseOptions, location);
```

#### Drag Operations
```java
DragOptions dragOptions = new DragOptions.Builder()
    .setFromOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setToOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setDragDelay(0.5)  // Hold duration before dragging
    .build();

action.drag(dragOptions, dragCollection);
```

## Illustration System (v1.1.0)

### IllustrationController API

v1.1.0 provides the IllustrationController for programmatic control:

```java
@Component
@RequiredArgsConstructor
public class MyAutomation {
    private final IllustrationController illustrationController;
    
    public void checkIllustration(ActionConfig config, ObjectCollection objects) {
        // Check if action will be illustrated
        boolean willIllustrate = illustrationController.okToIllustrate(config, objects);
        
        // Manually create illustration
        ActionResult result = action.find(objects);
        boolean illustrated = illustrationController.illustrateWhenAllowed(
            result, searchRegions, config, objects
        );
    }
}
```

### Configuration Properties

```properties
# Enable/disable illustrations by action type
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-drag=true
brobot.illustration.draw-move=true
brobot.illustration.draw-highlight=true

# Screenshot and history settings
brobot.screenshot.save-history=true
brobot.screenshot.save-snapshots=false
brobot.screenshot.path=screenshots/
brobot.screenshot.history-path=history/
brobot.screenshot.filename=screen
brobot.screenshot.history-filename=hist
```

### Environment-Specific Configurations

Create different property files for different environments:

```properties
# application-dev.properties - Full debugging
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.screenshot.save-history=true

# application-test.properties - Selective
brobot.illustration.draw-find=false
brobot.illustration.draw-click=true
brobot.screenshot.save-history=true

# application-prod.properties - Minimal
brobot.illustration.draw-find=false
brobot.illustration.draw-click=false
brobot.screenshot.save-history=false
```

Run with: `java -jar app.jar --spring.profiles.active=dev`

## State Management in v1.1.0

### Recommended: Using Annotations

The modern approach uses @State and @Transition annotations for automatic registration:

```java
// State with @State annotation
@State(initial = true)  // Marks as initial state
@Getter
public class HomeState {
    private final StateImage loginButton;
    
    public HomeState() {
        loginButton = new StateImage.Builder()
            .setName("LoginButton")
            .addPatterns("home/login_button")
            .setFixed(true)
            .build();
    }
}

// Transition with @Transition annotation
@Transition(from = HomeState.class, to = WorldState.class)
@RequiredArgsConstructor
public class HomeToWorldTransition {
    private final HomeState homeState;
    private final Action action;
    
    public boolean execute() {
        return action.click(homeState.getLoginButton()).isSuccess();
    }
}

// No manual configuration needed - annotations handle registration!
```

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

## ActionHistory for Integration Testing

### Overview

ActionHistory is Brobot's core component for mock testing and probabilistic automation. It records action execution patterns over time, enabling realistic testing without the target application.

### Basic ActionHistory Setup

```java
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;

// Initialize StateImage with ActionHistory for mock testing
@State
@Getter
public class LoginState {
    private final StateImage loginButton;
    
    public LoginState() {
        loginButton = new StateImage.Builder()
            .addPatterns("login/button")
            .setName("LoginButton")
            .build();
        
        // Add historical data for realistic mocking
        initializeActionHistory();
    }
    
    private void initializeActionHistory() {
        ActionHistory history = new ActionHistory();
        
        // Simulate 90% success rate with varying match scores
        for (int i = 0; i < 100; i++) {
            boolean success = i < 90;  // 90% success rate
            
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.BEST)
                    .setSimilarity(0.85)
                    .build())
                .addMatch(success ? new Match.Builder()
                    .setRegion(500, 400, 100, 40)
                    .setSimScore(0.85 + Math.random() * 0.1)
                    .build() : null)
                .setActionSuccess(success)
                .setDuration(success ? 200 + (long)(Math.random() * 300) : 5000)
                .build();
            
            history.addSnapshot(record);
        }
        
        loginButton.setActionHistory(history);
    }
}
```

### Recording Different Action Types

```java
// Click actions with timing data
ActionRecord clickRecord = new ActionRecord.Builder()
    .setActionConfig(new ClickOptions.Builder()
        .setClickType(ClickOptions.Type.DOUBLE)
        .setNumberOfClicks(2)
        .build())
    .addMatch(new Match.Builder()
        .setRegion(150, 250, 40, 20)
        .setSimScore(0.92)
        .build())
    .setActionSuccess(true)
    .setDuration(250)  // milliseconds
    .build();

// Type actions with text capture
ActionRecord typeRecord = new ActionRecord.Builder()
    .setActionConfig(new TypeOptions.Builder()
        .setPauseBeforeBegin(200)
        .build())
    .setText("user@example.com")
    .setActionSuccess(true)
    .build();

// Vanish actions for element disappearance
ActionRecord vanishRecord = new ActionRecord.Builder()
    .setActionConfig(new VanishOptions.Builder()
        .setWaitTime(5.0)
        .build())
    .setActionSuccess(true)
    .setDuration(3500)  // Vanished after 3.5 seconds
    .build();
```

### Integration Test Configuration

```yaml
# application-test.yml
brobot:
  mock:
    enabled: true
    use-real-screenshots: false
    
  testing:
    deterministic: true  # Use seeded random for reproducible tests
    seed: 12345
```

**ActionHistory Persistence Options**:

1. **In Java Projects (Library-based)**:
   - Use `ActionHistoryJsonConverter` from the library for serialize/deserialize
   - Implement your own persistence layer (as shown above)
   - Full control over when and how to save histories
   - Best for: Custom automation projects, integration tests

2. **In Brobot Runner Application**:
   - Automatic recording during execution
   - GUI for viewing and managing histories
   - Export/import functionality built-in
   - Best for: Interactive development, visual debugging

To use recorded data from the Runner in your Java tests:

1. **Record sessions** in the Runner during live automation
2. **Export sessions** as JSON files from the Runner UI
3. **Import in tests** using ObjectMapper or custom loaders
4. **Apply to StateImages** for realistic mock testing

### Writing Integration Tests with ActionHistory

```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.mock.enabled=true",
    "brobot.testing.deterministic=true"
})
public class WorkflowIntegrationTest {
    
    @Autowired
    private LoginState loginState;
    
    @Autowired
    private Action action;
    
    @Test
    public void testLoginWorkflowWithHistory() {
        // Pre-populate with realistic data
        prepareLoginHistory();
        
        // Execute workflow - uses ActionHistory for mocking
        boolean success = performLogin();
        
        // Verify based on historical success rate
        assertTrue(success || getRetryCount() > 0, 
            "Should succeed or retry based on history");
    }
    
    private void prepareLoginHistory() {
        ActionHistory history = loginState.getLoginButton().getActionHistory();
        
        // Add time-based patterns (morning = higher success)
        LocalDateTime morning = LocalDateTime.now().withHour(9);
        LocalDateTime evening = LocalDateTime.now().withHour(18);
        
        // Morning: 95% success
        addTimeBasedRecords(history, morning, 0.95, 20);
        
        // Evening: 75% success (system load)
        addTimeBasedRecords(history, evening, 0.75, 20);
    }
    
    private void addTimeBasedRecords(ActionHistory history, 
                                     LocalDateTime time, 
                                     double successRate, 
                                     int count) {
        for (int i = 0; i < count; i++) {
            boolean success = Math.random() < successRate;
            
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setActionSuccess(success)
                .setTimestamp(time.plusMinutes(i))
                .setDuration(success ? 200 : 5000)
                .build();
            
            history.addSnapshot(record);
        }
    }
}
```

### State-Specific ActionHistory

```java
// Different success patterns for different states
public class StateAwareActionHistory {
    
    public void setupStateSpecificHistory(StateImage image) {
        ActionHistory history = new ActionHistory();
        
        // Login state: high reliability
        Long loginStateId = 1L;
        addStateRecords(history, loginStateId, 0.95, 50);
        
        // Error state: lower reliability
        Long errorStateId = 2L;
        addStateRecords(history, errorStateId, 0.60, 50);
        
        image.setActionHistory(history);
    }
    
    private void addStateRecords(ActionHistory history, 
                                 Long stateId, 
                                 double successRate, 
                                 int count) {
        for (int i = 0; i < count; i++) {
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setStateId(stateId)
                .setActionSuccess(Math.random() < successRate)
                .build();
            
            history.addSnapshot(record);
        }
    }
    
    // Query state-specific records
    public Optional<ActionRecord> getStateSnapshot(ActionHistory history, 
                                                   Long stateId) {
        PatternFindOptions config = new PatternFindOptions.Builder().build();
        return history.getRandomSnapshot(config, stateId);
    }
}
```

### Saving and Loading ActionHistory in Java Projects

The Brobot library now includes comprehensive ActionHistory utilities in the `io.github.jspinak.brobot.tools.actionhistory` package:

#### Built-in Library Classes

**ActionHistoryPersistence** - Core persistence functionality:
```java
import io.github.jspinak.brobot.tools.actionhistory.ActionHistoryPersistence;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.element.Pattern;

@Autowired
private ActionHistoryPersistence persistence;

// Save ActionHistory to JSON
persistence.saveToFile(history, "my-history.json");

// Load ActionHistory from JSON
ActionHistory loaded = persistence.loadFromFile("my-history.json");

// Save from Pattern with session metadata
persistence.saveSessionHistory(pattern, "session-name");

// Capture current execution
persistence.captureCurrentExecution(result, pattern, config);

// Batch load multiple histories
Map<String, ActionHistory> histories = persistence.loadAllHistories();

// Clean old history files
int deleted = persistence.cleanOldHistories("histories", 30); // Keep 30 days
```

**RecordingActionWrapper** - Automatic recording during execution:
```java
import io.github.jspinak.brobot.tools.actionhistory.RecordingActionWrapper;

@Autowired
private RecordingActionWrapper recorder;

// Start recording session
recorder.startSession("test-session");

// Actions are automatically recorded
ActionResult result = recorder.find(stateImage);
result = recorder.click(stateImage);

// End session and save all histories
recorder.endSession("test-session", patterns);

// Get recording statistics
Map<String, Integer> stats = recorder.getRecordingStatistics();
```

**ActionHistoryExporter** - Export to various formats:
```java
import io.github.jspinak.brobot.tools.actionhistory.ActionHistoryExporter;

@Autowired
private ActionHistoryExporter exporter;

// Export to CSV
exporter.exportToCSV(history, "report.csv");

// Export to HTML with visualizations
exporter.exportToHTML(history, "report.html");

// Generate summary statistics
Map<String, Object> summary = exporter.generateSummary(history);

// Filter and export
ActionHistory filtered = exporter.filterHistory(history, 
    true, // successOnly
    0,    // minDuration
    5000  // maxDuration
);

// Batch export multiple histories
exporter.batchExport(histories, "combined.html", ExportFormat.HTML);
```

**PerformanceValidator** - Validate performance characteristics:
```java
import io.github.jspinak.brobot.tools.actionhistory.PerformanceValidator;

@Autowired
private PerformanceValidator validator;

// Validate with default thresholds
ValidationResult result = validator.validate(history);
if (!result.isValid()) {
    log.error("Performance issues: {}", result.getErrors());
}

// Custom validation config
ValidationConfig config = ValidationConfig.getDefault()
    .withMinSuccessRate(90.0)
    .withMaxAverageDuration(1000);
ValidationResult custom = validator.validate(history, config);

// Compare baseline vs current
ComparisonResult comparison = validator.compare(baselineHistory, currentHistory);
if (comparison.isRegression()) {
    log.warn("Performance regression detected: {}", comparison.getIssues());
}
```

### Recording ActionHistory During Live Automation

The library's `RecordingActionWrapper` class automatically captures ActionHistory during automation runs. Use it in your transitions and actions:

```java
// Use in transitions
@Transition(from = HomeState.class, to = WorldState.class)
@RequiredArgsConstructor
public class RecordedTransition {
    
    @Autowired
    private RecordingActionWrapper recorder;
    private final HomeState homeState;
    
    public boolean execute() {
        // Start recording session
        recorder.startSession("transition-session");
        
        // All actions are automatically recorded
        ActionResult result = recorder.click(homeState.getButton());
        
        // End session and save
        recorder.endSession("transition-session", 
            Arrays.asList(homeState.getButton().getPatterns()));
        
        return result.isSuccess();
    }
}
```

### Export/Import Between Projects

Use the library's `ActionHistoryExporter` for sharing histories between projects:

```java
@Autowired
private ActionHistoryExporter exporter;
@Autowired
private ActionHistoryPersistence persistence;

// Export multiple histories for sharing
public void exportForProject(String projectName) throws IOException {
    // Load all histories from the project
    Map<String, ActionHistory> histories = persistence.loadAllHistories();
    
    // Batch export with metadata
    exporter.batchExport(histories, 
        String.format("%s-export.html", projectName),
        ActionHistoryExporter.ExportFormat.HTML);
    
    // Also export as CSV for analysis
    exporter.batchExport(histories,
        String.format("%s-export.csv", projectName),
        ActionHistoryExporter.ExportFormat.CSV);
    
    // Generate summary report
    for (Map.Entry<String, ActionHistory> entry : histories.entrySet()) {
        Map<String, Object> summary = exporter.generateSummary(entry.getValue());
        log.info("History {}: {} actions, {:.1f}% success rate", 
            entry.getKey(), 
            summary.get("totalActions"),
            summary.get("successRate"));
    }
}
```

### Performance Testing with ActionHistory

Use the library's `PerformanceValidator` for comprehensive performance testing:

```java
@Autowired
private PerformanceValidator validator;

@Test
public void validateActionPerformance() {
    ActionHistory performanceHistory = loadPerformanceHistory();
    
    // Validate with custom thresholds
    PerformanceValidator.ValidationConfig config = 
        PerformanceValidator.ValidationConfig.getDefault()
            .withMinSuccessRate(85.0)
            .withMaxAverageDuration(1500)
            .withMaxDuration(3000);
    
    PerformanceValidator.ValidationResult result = 
        validator.validate(performanceHistory, config);
    
    // Assert no performance issues
    assertTrue(result.isValid(), 
        "Performance issues detected: " + result.getErrors());
    
    // Check for anomalies
    assertEquals(0, result.getAnomalyCount(), 
        "Anomalies detected: " + result.getAnomalies());
    
    // Analyze timing patterns
        DoubleSummaryStatistics stats = performanceHistory.getSnapshots().stream()
            .filter(ActionRecord::isActionSuccess)
            .mapToDouble(ActionRecord::getDuration)
            .summaryStatistics();
        
        // Assert performance requirements
        assertTrue(stats.getAverage() < 500, 
            "Average response time should be under 500ms");
        assertTrue(stats.getMax() < 2000, 
            "Max response time should be under 2s");
        
        // Check success rate
        double successRate = performanceHistory.getTimesFound() * 100.0 / 
                           performanceHistory.getTimesSearched();
        assertTrue(successRate > 85, 
            "Success rate should be above 85%");
    }
    
    private ActionHistory createPerformanceHistory() {
        ActionHistory history = new ActionHistory();
        Random random = new Random(42);  // Deterministic
        
        // Simulate performance distribution
        for (int i = 0; i < 1000; i++) {
            // 90% fast responses, 10% slow
            boolean fast = random.nextDouble() < 0.9;
            long duration = fast ? 
                100 + random.nextInt(400) :  // 100-500ms
                1000 + random.nextInt(4000); // 1-5s
            
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setDuration(duration)
                .setActionSuccess(duration < 2000)  // Timeout at 2s
                .build();
            
            history.addSnapshot(record);
        }
        
        return history;
    }
}
```

### Best Practices for ActionHistory

1. **Realistic Data Generation**: Create patterns that reflect real-world behavior
2. **Deterministic Testing**: Use seeded random for reproducible tests
3. **State-Aware Patterns**: Different states should have different success patterns
4. **Time-Based Variations**: Model performance changes throughout the day
5. **Gradual Degradation**: Simulate system degradation over time
6. **Regular Persistence**: Save histories periodically during long automation runs
7. **Version Control**: Include history JSON files in test resources for regression testing

### ActionHistory Utility Classes

The Brobot library now includes comprehensive ActionHistory utilities in the `io.github.jspinak.brobot.tools.actionhistory` package:

1. **ActionHistoryPersistence** - Standardized save/load operations with JSON serialization
2. **RecordingActionWrapper** - Automatic recording during execution with session management
3. **ActionHistoryExporter** - Export to CSV/HTML formats with summary statistics
4. **PerformanceValidator** - Performance analysis, anomaly detection, and regression testing

These classes provide production-ready functionality for capturing, persisting, and analyzing automation performance.

### Migration from Legacy ActionOptions

If you have existing code using the deprecated `ActionOptions` API:

```java
// Legacy (deprecated)
ActionRecord legacyRecord = new ActionRecord.Builder()
    .setActionOptions(new ActionOptions.Builder()
        .setAction(ActionOptions.Action.FIND)
        .setFind(ActionOptions.Find.BEST)
        .build())
    .build();

// Modern (use this)
ActionRecord modernRecord = new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .build())
    .build();
```

For detailed migration instructions, see the [ActionHistory Migration Guide](testing/actionhistory-integration-testing).

## Screen-Adaptive Region Building

The Brobot library now includes enhanced `RegionBuilder` with Position integration for creating screen-size aware regions that adapt to different resolutions.

### Position-Based Region Building

The `RegionBuilder` class now integrates with `Position` and `Positions.Name` for intuitive region placement:

```java
import static io.github.jspinak.brobot.model.element.Positions.Name.*;

// Position a region at 70% width, 30% height of screen
Region customPos = Region.builder()
    .withSize(200, 150)
    .withPosition(new Position(0.7, 0.3))
    .build();

// Use named positions for quick placement
Region topRight = Region.builder()
    .withSize(300, 200)
    .withPosition(TOPRIGHT)
    .build();

// Position relative to another region
Region tooltip = Region.builder()
    .withSize(200, 50)
    .positionRelativeTo(dialogRegion, TOPMIDDLE)
    .adjustY(-10)  // 10px gap above
    .build();

// Create centered dialog with margins
Region dialog = Region.builder()
    .withScreenPercentageSize(0.6, 0.4)  // 60% width, 40% height
    .centerOnScreen()
    .build();
```

### Advanced Positioning Features

```java
// Custom anchor points
Region customAnchor = Region.builder()
    .withSize(100, 100)
    .withAnchor(new Position(0.75, 0.25))  // Custom anchor at 75% width, 25% height
    .build();

// Named anchor helpers
Region bottomCorner = Region.builder()
    .withSize(120, 40)
    .bottomRight()
    .adjustX(-20)  // 20px margin from edges
    .adjustY(-20)
    .build();

// Position chains for complex layouts
Region sidebar = Region.builder()
    .withScreenPercentageSize(0.2, 1.0)  // 20% width, full height
    .leftCenter()
    .build();

Region content = Region.builder()
    .positionRelativeTo(sidebar, MIDDLERIGHT)
    .withScreenPercentageSize(0.8, 1.0)
    .build();
```

### Screen Scaling and Adaptation

```java
// Scale regions designed for specific resolutions
Region scaledButton = Region.builder()
    .withBaseScreenSize(1920, 1080)  // Original design resolution
    .withBaseRegion(1800, 1000, 100, 50)  // Original button position
    .adjustToCurrentScreen()  // Automatically scale to current resolution
    .build();

// Percentage-based regions that adapt automatically
Region searchArea = Region.builder()
    .withScreenPercentage(0.1, 0.1, 0.8, 0.3)  // 10% margins, 80% width, 30% height
    .build();
```

### Integration with Position Class

The `Position` class provides percentage-based coordinates (0.0 to 1.0) that work seamlessly with `RegionBuilder`:

- **Positions.Name enum**: Predefined positions like `TOPLEFT`, `MIDDLEMIDDLE`, `BOTTOMRIGHT`
- **Custom positions**: Any percentage-based position using `new Position(x, y)`
- **Position math**: Add offsets and scale positions for dynamic layouts

This integration makes it easy to create regions that adapt to different screen sizes while maintaining their relative positions and proportions.

## Summary

Modern Brobot development emphasizes:
- Clean, readable code with minimal boilerplate
- Type safety through StateEnum and proper generics
- Dependency injection with Spring Boot
- Fluent APIs and method chaining
- **EnhancedConditionalActionChain for elegant UI interactions with proper sequential composition**
- **ActionHistory for probabilistic mock testing and integration tests**
- **Screen-adaptive RegionBuilder with Position integration for resolution-independent automation**
- Direct access to state components
- Automatic initial state verification
- Configuration-driven behavior
- Advanced color and motion detection capabilities
- Flexible action chaining with nested/confirmed strategies
- Comprehensive testing with historical action data

Follow these patterns for maintainable, professional Brobot applications.