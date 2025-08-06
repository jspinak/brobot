# ConditionalActionChain Examples

This project demonstrates Brobot's **ConditionalActionChain** - an elegant API for creating conditional execution flows in automation.

## Overview

ConditionalActionChain allows you to:
- Chain actions with success/failure handling
- Create if-then-else flows for find operations
- Build complex multi-step workflows
- Add custom logic with lambdas
- Handle errors gracefully

## Project Structure

```
conditional-chains-examples/
├── src/main/java/com/example/conditionalchains/
│   ├── ConditionalChainsApplication.java    # Spring Boot main
│   ├── ConditionalChainsRunner.java         # Runs all examples
│   └── examples/
│       ├── BasicFindExample.java            # Basic patterns
│       ├── MultiStepWorkflowExample.java    # Complex workflows
│       └── CustomLogicExample.java          # Advanced patterns
├── src/main/resources/
│   └── application.yml                       # Configuration
├── images/                                   # Place test images here
│   ├── buttons/
│   ├── forms/
│   └── indicators/
├── build.gradle
└── settings.gradle
```

## Examples Demonstrated

### 1. Basic Find Patterns (`BasicFindExample.java`)

**Simple find with handling:**
```java
conditionalWrapper
    .find(submitButton)
    .ifFound(() -> {
        // Handle found case
        click(submitButton);
    })
    .ifNotFound(() -> {
        // Handle not found case
        click(cancelButton);
    })
    .build()
    .execute();
```

**Chained finds with fallbacks:**
```java
conditionalWrapper
    .find(primaryButton)
    .ifFound(() -> click(primaryButton))
    .ifNotFound(() -> {
        // Try secondary option
        conditionalWrapper
            .find(secondaryButton)
            .ifFound(() -> click(secondaryButton))
            .ifNotFound(() -> handleNoButtons())
            .build()
            .execute();
    })
    .build()
    .execute();
```

### 2. Multi-Step Workflows (`MultiStepWorkflowExample.java`)

**Login workflow example:**
- Click login button
- Find and fill username field
- Find and fill password field  
- Submit form
- Verify success or handle errors

Each step uses conditional chains to handle failures gracefully.

### 3. Custom Logic Patterns (`CustomLogicExample.java`)

**Search with retry and timing:**
- Measure operation timings
- Implement retry logic
- Wait for loading indicators
- Process results dynamically

**Dynamic action selection:**
```java
// Choose action based on application state
StateObject targetButton = isBusinessHours ? normalMode : afterHoursMode;

conditionalWrapper
    .find(targetButton)
    .ifFound(() -> activateMode(targetButton))
    .build()
    .execute();
```

## Running the Examples

1. **In Mock Mode** (default):
   ```bash
   ./gradlew bootRun
   ```
   This runs all examples with simulated UI interactions.

2. **With Real UI**:
   - Add screenshots to the `images/` directory
   - Set `brobot.core.mock: false` in `application.yml`
   - Run the application

## Key Concepts

### 1. **Action Types**
- `find()` - Locate elements on screen
- `click()` - Click on elements
- `type()` - Type text
- `drag()` - Drag elements
- Custom actions via lambdas

### 2. **Conditional Handlers**
- `ifFound()` / `ifNotFound()` - For find operations
- `ifSuccess()` / `ifFailure()` - For action operations
- Custom lambdas for complex logic

### 3. **Chaining**
- Actions can be chained sequentially
- Nested chains for complex flows
- Each chain is independent and reusable

### 4. **Error Handling**
- Built-in retry mechanisms
- Graceful degradation
- Custom error handlers

## Best Practices

1. **Keep chains focused** - Each chain should have a single responsibility
2. **Use descriptive logging** - Log at each decision point
3. **Handle all cases** - Always provide both success and failure handlers
4. **Avoid deep nesting** - Extract complex logic into methods
5. **Test incrementally** - Build and test chains step by step

## Configuration

Key settings in `application.yml`:

```yaml
brobot:
  core:
    mock: true          # Use mock mode for testing
    verbose: true       # Show detailed logs
  action:
    max-wait: 5         # Seconds to wait for elements
  find:
    similarity: 0.8     # Image match threshold
```

## Logging

The examples include comprehensive logging to show:
- Chain execution flow
- Decision points
- Success/failure outcomes
- Timing information

## Next Steps

1. Study the example code to understand patterns
2. Run examples and observe the execution flow
3. Modify examples to match your use cases
4. Create your own conditional chains
5. Combine with other Brobot features

## Related Documentation

- [Action Configuration Guide](../../README.md)
- [Find Operations](../find-action-examples/README.md)
- [State Management](../../../01-getting-started/states.md)