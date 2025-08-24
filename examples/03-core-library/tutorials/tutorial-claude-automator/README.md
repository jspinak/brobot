# Claude Automator Tutorial

This tutorial demonstrates advanced Brobot features through a simplified Claude AI automation example.

## Key Concepts Demonstrated

### 1. **State-Based Automation**
- Using `@State` annotation for automatic state registration
- Defining initial states with `@State(initial = true)`
- Organizing automation logic around application states

### 2. **Declarative Search Regions**
The highlight of this tutorial is the **cross-state search region dependency**:

```java
.setSearchRegionOnObject(SearchRegionOnObject.builder()
    .targetType(StateObject.Type.IMAGE)
    .targetStateName("Prompt")           // References different state!
    .targetObjectName("ClaudePrompt")    
    .adjustments(MatchAdjustmentOptions.builder()
        .addX(3)      // Offset from prompt location
        .addY(10)     
        .addW(30)     // Expand search area
        .addH(55)     
        .build())
    .build())
```

This means:
- `ClaudeIcon` in `WorkingState` searches relative to where `ClaudePrompt` was found
- No manual region calculations needed
- Search region updates automatically when prompt moves

### 3. **Automatic Transitions**
- Using `@Transition` annotation for state transitions
- Framework automatically checks transitions
- Clean separation of transition logic

## Project Structure

```
tutorial-claude-automator/
├── src/main/java/com/example/claudeautomator/
│   ├── ClaudeAutomatorApplication.java    # Spring Boot main
│   ├── states/
│   │   ├── PromptState.java              # Initial state
│   │   └── WorkingState.java             # Shows declarative regions
│   ├── transitions/
│   │   └── PromptToWorkingTransition.java
│   └── automation/
│       └── ClaudeAutomatorRunner.java    # Demo runner
├── src/main/resources/
│   └── application.yml                    # Configuration
├── images/                                # Place images here
│   ├── prompt/
│   │   └── claude-prompt-*.png
│   └── working/
│       └── claude-icon-*.png
├── build.gradle
└── settings.gradle
```

## Running the Tutorial

1. **Add Images**: Place Claude UI screenshots in the `images/` directory:
   - `prompt/claude-prompt-1.png` - The prompt input area
   - `working/claude-icon-1.png` - The spinning icon during processing

2. **Run in Mock Mode** (default):
   ```bash
   ./gradlew bootRun
   ```
   This simulates the automation flow without needing Claude open.

3. **Run with Real UI**:
   - Set `brobot.core.mock: false` in `application.yml`
   - Open Claude in your browser
   - Run the application

## Understanding the Flow

1. **Startup**: Spring Boot starts and Brobot initializes
2. **State Registration**: `@State` classes are automatically registered
3. **Initial State**: `PromptState` is set as active (due to `initial = true`)
4. **Dependency Registration**: The system registers that `ClaudeIcon` depends on `ClaudePrompt` location
5. **Execution**:
   - Finds `ClaudePrompt` in the UI
   - When prompt is found, its location is recorded
   - When searching for `ClaudeIcon`, the search region is calculated as:
     - X: prompt.x + 3
     - Y: prompt.y + 10  
     - Width: prompt.width + 30
     - Height: prompt.height + 55

## Key Takeaways

- **No Manual Region Math**: The framework handles all region calculations
- **Cross-State Dependencies**: Objects can reference objects in other states
- **Dynamic Updates**: Regions update automatically based on found objects
- **Clean Code**: Declarative approach reduces boilerplate

## Next Steps

- Try modifying the adjustments to see how the search region changes
- Add more states (e.g., `ResponseState` when Claude finishes)
- Implement actual automation actions (click, type, etc.)
- Explore other Brobot features like color finding and motion detection

## Related Documentation

- [Declarative Region Definition Guide](../../../guides/declarative-region-definition.md)
- [States and Transitions](../../../../01-getting-started/states.md)
- [Action Configuration](../../action-config/README.md)