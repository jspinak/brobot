# Action Hierarchy Example

This example demonstrates the action hierarchy in Brobot, showing different approaches to implementing complex actions.

## Documentation Reference

This example corresponds to: `/docs/01-getting-started/action-hierarchy.md`

## What This Example Shows

1. **Basic Actions** - The fundamental building blocks (Find, Click, Type, Move)
2. **Complex Actions** - Multi-step operations combining basic actions
3. **Four different approaches to "click until found"**:
   - Traditional loop approach
   - Fluent API with action chaining
   - Built-in ClickUntilOptions (deprecated but available)
   - Reusable function approach

## Key Classes

- `ComplexActionExamples.java` - Contains all four implementation methods
- `ExampleState.java` - Demonstrates modern @State annotation usage
- `ExampleRunner.java` - Runs the examples on application startup

## Running the Example

1. Ensure you have Java 21 installed
2. From this directory, run:
   ```bash
   ./gradlew bootRun
   ```

## Configuration

The example runs in mock mode by default (configured in `application.yml`). This means:
- No actual GUI interaction occurs
- Actions are simulated with configured delays
- Perfect for testing and understanding the API

## Creating Test Images

To run with real GUI interaction:
1. Create an `images/buttons/` directory
2. Add screenshots of buttons named:
   - `next-button.png`
   - `finish-button.png`
   - `submit-button.png`
3. Set `brobot.core.mock: false` in application.yml

## Key Concepts Demonstrated

### Basic Actions
```java
// Simple find action
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.9)
    .build();
```

### Action Chaining (Fluent API)
```java
ClickOptions clickAndCheck = new ClickOptions.Builder()
    .setPauseAfterEnd(1.0)
    .then(new PatternFindOptions.Builder()
        .withSuccessLog("Target found!")
        .build())
    .build();
```

### Repetition
```java
.setRepetition(new RepetitionOptions.Builder()
    .setMaxTimesToRepeatActionSequence(10)
    .setPauseBetweenActionSequences(0.5)
    .build())
```

## Best Practices Shown

1. Use of modern @State annotation
2. Proper logging configuration
3. Clean separation of concerns
4. Multiple implementation approaches for comparison
5. Type-safe ActionConfig usage