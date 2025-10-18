# Action Hierarchy Example

This example demonstrates the action hierarchy in Brobot, showing different approaches to implementing complex actions.

## Documentation Reference

This example corresponds to: `/docs/01-getting-started/action-hierarchy.md`

## What This Example Shows

1. **Basic Actions** - The fundamental building blocks (Find, Click, Type, Move)
2. **Complex Actions** - Multi-step operations combining basic actions
3. **Three approaches to "click until found"** (Brobot 1.1.0+):
   - Traditional loop approach
   - ConditionalActionChain (fluent API)
   - ActionChainOptions for flexible chaining

## Key Classes

- `ComplexActionExamples.java` - Contains all three implementation methods (134 lines)
- `ExampleState.java` - Demonstrates modern @State annotation usage (39 lines)
- `ExampleRunner.java` - Runs the examples on application startup (42 lines)
- `ActionHierarchyApplication.java` - Spring Boot main class (30 lines)

**Total Code**: 245 lines across 4 Java files

## Running the Example

1. Ensure you have Java 21 installed
2. From this directory, run:
   ```bash
   ./gradlew bootRun
   ```

The example will:
- Initialize Brobot in mock mode
- Demonstrate all three "click until found" patterns
- Log results to console with clear pattern identification

## Configuration

The example runs in mock mode by default (configured in `application.yml`). This means:
- No actual GUI interaction occurs
- Actions are simulated with configured delays
- Perfect for testing and understanding the API

```yaml
brobot:
  mock: true
  logging:
    verbosity: VERBOSE
```

## Creating Test Images

To run with real GUI interaction:
1. Create an `images/buttons/` directory
2. Add screenshots of buttons named:
   - `next-button.png`
   - `finish-button.png`
   - `submit-button.png`
3. Set `brobot.mock: false` in application.yml

## Implementation Approaches Demonstrated

### Approach 1: Traditional Loop (Simple but Verbose)

```java
public void clickUntilFoundTraditional(StateImage target, StateImage button) {
    log.info("Method 1: Traditional loop approach");

    while (true) {
        ActionResult result = action.perform(
            new PatternFindOptions.Builder().build(),
            target
        );

        if (result.isSuccess()) {
            log.info("Target found!");
            break;
        }

        action.perform(new ClickOptions.Builder().build(), button);
    }
}
```

**Pros**:
- Simple to understand
- Full control over loop logic
- Easy to debug

**Cons**:
- Verbose
- No automatic retry limits
- Manual infinite loop protection needed

### Approach 2: ConditionalActionChain (Fluent & Declarative)

```java
public void clickUntilFoundChain(StateImage target, StateImage button) {
    log.info("Method 2: ConditionalActionChain approach");

    boolean found = false;
    while (!found) {
        ObjectCollection buttonCollection = new ObjectCollection.Builder()
            .withImages(button)
            .build();

        ObjectCollection targetCollection = new ObjectCollection.Builder()
            .withImages(target)
            .build();

        ActionResult clickResult = ConditionalActionChain
            .click(new ClickOptions.Builder().build())
            .perform(action, buttonCollection);

        found = action.find(target).isSuccess();
    }
}
```

**Pros**:
- Fluent, readable API
- Declarative style
- Built-in conditional logic

**Cons**:
- Still needs manual loop
- Slightly more verbose setup

### Approach 3: ActionChainOptions (Recommended - Most Flexible)

```java
public void clickUntilFoundChainOptions(StateImage target, StateImage button) {
    log.info("Method 3: ActionChainOptions approach (recommended)");

    // Define the action sequence
    ActionChainOptions chain = new ActionChainOptions.Builder()
        .addAction(ActionType.CLICK)           // Click button
        .addAction(ActionType.FIND)            // Check if target appears
        .setMaxAttempts(10)                     // Safety limit
        .setRepeatUntil(ActionResult::isSuccess)  // Stop when target found
        .build();

    // Create object collections
    ObjectCollection buttonAndTarget = new ObjectCollection.Builder()
        .withImages(button, target)            // Both images in sequence
        .build();

    // Execute the chain
    ActionResult result = action.perform(chain, buttonAndTarget);

    if (result.isSuccess()) {
        log.info("Target found using ActionChainOptions!");
    }
}
```

**Pros**:
- Most concise and declarative
- Built-in retry limits (safety)
- Clear separation of what vs how
- Reusable configuration

**Cons**:
- Requires understanding ActionChainOptions API
- Less flexible for complex conditional logic

## Key Concepts Demonstrated

### 1. Basic Action Configuration

All approaches use modern Brobot 1.1.0+ Builder pattern:

```java
// PatternFindOptions for find operations
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .setPauseBeforeBegin(0.5)
    .build();

// ClickOptions for click operations
ClickOptions clickOptions = new ClickOptions.Builder()
    .setPauseAfterEnd(1.0)
    .build();
```

### 2. ObjectCollection Pattern

Wrap targets for action execution:

```java
ObjectCollection collection = new ObjectCollection.Builder()
    .withImages(stateImage1, stateImage2)  // Multiple images
    .build();

ActionResult result = action.perform(options, collection);
```

### 3. State Management with @State

Modern state definition using annotations:

```java
@State
@Component
@Getter
public class ExampleState {
    private final StateImage nextButton;
    private final StateImage finishButton;
    private final StateImage submitButton;

    public ExampleState() {
        nextButton = new StateImage.Builder()
            .addPatterns("buttons/next-button")
            .build();
        // ... other buttons
    }
}
```

**Key Points**:
- `@State` automatically registers the state with Brobot
- `@Component` makes it a Spring bean
- `@Getter` provides automatic getters via Lombok
- Constructor initializes StateImages using Builder pattern

### 4. Repetition Strategies (Brobot 1.1.0+)

**❌ Removed in v1.1.0**:
- `RepetitionOptions` - No longer available
- `ClickUntilOptions` - No longer available

**✅ Modern Alternatives**:
1. **Manual loops** for simple cases
2. **ConditionalActionChain** for fluent conditional logic
3. **ActionChainOptions** for complex action sequences (recommended)

## Best Practices Shown

1. **Use ActionChainOptions for complex patterns** - Most maintainable
2. **Always set maxAttempts** - Prevents infinite loops
3. **Use proper logging** - Track pattern execution flow
4. **StateImage.Builder() pattern** - Type-safe image configuration
5. **ObjectCollection for action targets** - Clean API usage
6. **Modern @State annotation** - Automatic registration
7. **Lombok annotations** - Reduce boilerplate (@Getter, @RequiredArgsConstructor, @Slf4j)

## Comparison Table

| Approach | Lines of Code | Readability | Flexibility | Safety | Recommended For |
|----------|---------------|-------------|-------------|--------|-----------------|
| Traditional Loop | ~15 | Good | High | Manual | Simple learning |
| ConditionalActionChain | ~12 | Excellent | Medium | Manual | Conditional logic |
| ActionChainOptions | ~8 | Excellent | Medium | Built-in | Production use |

## Running Each Approach

The `ExampleRunner` demonstrates all three approaches sequentially:

```bash
./gradlew bootRun
```

**Console Output**:
```
Method 1: Traditional loop approach
Target found!

Method 2: ConditionalActionChain approach
Target found using ConditionalActionChain!

Method 3: ActionChainOptions approach (recommended)
Target found using ActionChainOptions!
```

## Migration from Older Versions

If you're updating from Brobot v1.0:

| Old Pattern | New Pattern (v1.1.0+) |
|-------------|----------------------|
| `RepetitionOptions.builder()` | `ActionChainOptions.Builder()` |
| `ClickUntilOptions.builder()` | Manual loop or ActionChainOptions |
| `ActionOptions.builder()` | Specific *Options.Builder() |
| Direct action calls | action.perform(options, collection) |

## Next Steps

1. **Experiment with different maxAttempts** - See how retry limits work
2. **Add custom conditions** - Create your own stopping criteria
3. **Combine with states** - Integrate into state-based automation
4. **Try real UI** - Add images and test with actual applications
5. **Explore ConditionalActionChain** - See [pure-actions-quickstart](../pure-actions-quickstart) example

## Related Examples

- [pure-actions-quickstart](../pure-actions-quickstart) - Comprehensive ConditionalActionChain usage
- [quick-start](../quick-start) - Complete Brobot introduction
- [conditional-chains-examples](../../03-core-library/testing/conditional-chains-examples) - Advanced conditional patterns

## Related Documentation

- [Action Hierarchy Guide](/docs/01-getting-started/action-hierarchy.md)
- [Action Configuration](/docs/03-core-library/action-config/README.md)
- [ConditionalActionChain API](/docs/03-core-library/api/conditional-action-chain.md)
