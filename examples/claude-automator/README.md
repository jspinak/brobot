# Claude Automator Example

A complete working example of Brobot 1.1.0 that demonstrates continuous monitoring and interaction with Claude's web interface.

## Features Demonstrated

- **Annotation-Based Configuration**: Uses `@State` and `@Transition` annotations
- **State Management**: Two states (Prompt and Working) with automatic registration
- **Action Chaining**: Fluent API for find → click → type sequences
- **Continuous Monitoring**: Scheduled tasks that monitor UI state
- **Automatic Recovery**: Reopens states when UI elements disappear

## Project Structure

```
claude-automator/
├── src/main/java/com/claude/automator/
│   ├── states/              # State definitions with @State annotation
│   ├── transitions/         # Transitions with @Transition annotation
│   ├── automation/          # Monitoring and automation logic
│   └── ClaudeAutomatorApplication.java
├── src/main/resources/
│   ├── application.yml      # Spring Boot and Brobot configuration
│   └── images/             # UI element screenshots
│       ├── working/        # Claude response icon variants
│       └── prompt/         # Claude prompt interface variants
├── build.gradle            # Gradle build configuration
└── settings.gradle         # Local Brobot library reference
```

## Prerequisites

- Java 21 or higher
- Gradle 8.x
- Claude web interface accessible
- Screenshot images of Claude UI elements

## Setup

1. **Capture Images**: Follow the instructions in `src/main/resources/images/README.md` to capture the required UI screenshots.

2. **Configure Application**: The `application.yml` file is pre-configured with sensible defaults. Adjust if needed:
   ```yaml
   brobot:
     core:
       mock: false  # Set to true for testing without real UI
   ```

3. **Build the Project**:
   ```bash
   ./gradlew build
   ```

## Running the Application

```bash
./gradlew bootRun
```

The application will:
1. Start Spring Boot context
2. Automatically discover and register states via annotations
3. Verify that required images exist
4. Begin monitoring Claude's interface
5. Automatically reopen the Working state when Claude finishes responding

## Key Components

### States (Annotation-Based)

```java
@State(initial = true, name = "Prompt")
@Getter
@Slf4j
public class PromptState {
    private final StateObject claudePrompt;
    private final StateObject continueCommand;
    // ...
}
```

### Transitions (Annotation-Based)

```java
@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptToWorkingTransition {
    public boolean execute() {
        // Transition logic
    }
}
```

### Action Chaining

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

## Customization

- **Monitoring Interval**: Adjust the check frequency in `ClaudeMonitoringAutomation`
- **UI Elements**: Update state definitions to match your specific UI
- **Transitions**: Add more transitions for complex workflows

## Troubleshooting

1. **Images Not Found**: Ensure images are in the correct directories and named properly
2. **State Not Active**: Check logs for state registration issues
3. **Transition Failures**: Enable DEBUG logging for detailed execution traces

## Learning Points

This example demonstrates:
- Modern Brobot patterns with annotations
- Clean separation of concerns
- Minimal boilerplate code
- Focus on automation logic rather than framework setup