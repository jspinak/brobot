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
   │   ├── application.yml         # Alternative to properties
   │   └── images/                  # Default location for classpath images
   │       └── [state-name]/
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

3. **State Creation Pattern (Modern)**
   ```java
   @Component
   @Getter
   public class ExampleState {
       private final State state;
       private final StateImage mainImage;  // Direct access to components
       
       public enum Name implements StateEnum {
           EXAMPLE
       }
       
       public ExampleState() {
           // Store component for direct access
           mainImage = new StateImage.Builder()
               .addPatterns("folder/image-name1", "folder/image-name2")  // NO .png extension
               .setName("MainImage")
               .build();
           
           state = new State.Builder(Name.EXAMPLE)
               .withImages(mainImage)
               .build();
       }
   }
   ```

4. **Transitions with JavaStateTransition**
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
           // Use convenience methods for clean code
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
     ```
   
   - **Fluent API with Action Chaining**:
     ```java
     PatternFindOptions findClickType = new PatternFindOptions.Builder()
         .setPauseAfterEnd(0.5)
         .then(new ClickOptions.Builder()
                 .setPauseAfterEnd(0.5)
                 .build())
         .then(new TypeOptions.Builder()
                 .build())
         .build();
     ```
   
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
   - Place images in `src/main/resources/images/[state-name]/`
   - Build.gradle should copy images:
     ```gradle
     task copyImages(type: Copy) {
         from 'images'
         into "$buildDir/resources/main/images"
     }
     processResources.dependsOn copyImages
     ```

## Best Practices

1. **Code Organization**
   - Keep states simple with direct access to components
   - Use JavaStateTransition for code-based transitions
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

### Default Configuration
Brobot includes sensible defaults in `brobot-defaults.properties`. You only need to override what you want to change.

### Image Path Configuration
```yaml
# application.yml
brobot:
  core:
    image-path: classpath:images/    # Load from classpath (recommended)
    # image-path: /absolute/path/    # Absolute path
    # image-path: relative/path/     # Relative to working directory
```

### Complete Configuration Example
```yaml
brobot:
  core:
    image-path: classpath:images/
    mock: false
    headless: false
  startup:
    verify-initial-states: true
    initial-states: HOME,LOGIN
    fallback-search: true
    startup-delay: 2
  mouse:
    move-delay: 0.5
    pause-before-down: 0.0
  screenshot:
    save-snapshots: false
    path: screenshots/
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

## Summary

Modern Brobot development emphasizes:
- Clean, readable code with minimal boilerplate
- Type safety through StateEnum and proper generics
- Dependency injection with Spring Boot
- Fluent APIs and method chaining
- Direct access to state components
- Automatic initial state verification
- Configuration-driven behavior

Follow these patterns for maintainable, professional Brobot applications.