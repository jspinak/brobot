---
sidebar_position: 21
title: 'ActionConfig Factory'
---

# Action Configuration Factory

Brobot provides powerful patterns for creating and managing action configurations through the `ActionConfigFactory` and `ActionChainBuilder` classes. These tools simplify the creation of action configurations and make complex action sequences more readable and maintainable.

## ActionConfigFactory

The `ActionConfigFactory` is a Spring component that centralizes the creation of all `ActionConfig` subclasses. It provides a single point of configuration and reduces coupling between actions and their configuration creation.

### Benefits

- **Type Safety**: Eliminates unsafe casting and `instanceof` checks in action implementations
- **Centralized Logic**: All configuration creation logic in one place
- **Consistent API**: Uniform way to create any action configuration
- **Override Support**: Easy application of custom settings

### Basic Usage

```java
@Autowired
private ActionConfigFactory factory;

// Create with defaults
ActionConfig clickConfig = factory.create(ActionInterface.Type.CLICK);

// Create with overrides
Map<String, Object> overrides = new HashMap<>();
overrides.put("numberOfClicks", 2);
overrides.put("pauseAfterEnd", 1.0);
ActionConfig doubleClick = factory.create(ActionInterface.Type.CLICK, overrides);
```

### Supported Action Types

The factory supports all Brobot action types:

- `CLICK` - Creates `ClickOptions`
- `DRAG` - Creates `DragOptions`
- `FIND` - Creates `PatternFindOptions`
- `TYPE` - Creates `TypeOptions`
- `MOVE` - Creates `MouseMoveOptions`
- `VANISH` - Creates `VanishOptions`
- `HIGHLIGHT` - Creates `HighlightOptions`
- `SCROLL_MOUSE_WHEEL` - Creates `ScrollOptions`
- `MOUSE_DOWN` - Creates `MouseDownOptions`
- `MOUSE_UP` - Creates `MouseUpOptions`
- `KEY_DOWN` - Creates `KeyDownOptions`
- `KEY_UP` - Creates `KeyUpOptions`
- `CLASSIFY` - Creates `ColorFindOptions` with classification strategy
- `CLICK_UNTIL` - Creates `ClickUntilOptions`
- `DEFINE` - Creates `DefineRegionOptions`

### Common Overrides

All action configurations support these common overrides:

```java
Map<String, Object> overrides = new HashMap<>();
overrides.put("pauseBeforeBegin", 2.0);    // Pause before action starts
overrides.put("pauseAfterEnd", 1.0);       // Pause after action completes
overrides.put("illustrate", ActionConfig.Illustrate.YES);  // Force illustration
overrides.put("logType", LogEventType.ACTION);  // Set log event type
overrides.put("successCriteria", result -> result.isSuccess());  // Custom success logic
```

### Type-Specific Overrides

Each action type supports its own specific overrides:

#### Click Actions
```java
overrides.put("numberOfClicks", 2);  // Double-click
overrides.put("mousePressOptions", new MousePressOptions.Builder()
    .setButton(MouseButton.RIGHT)
    .build());
```

#### Drag Actions
```java
overrides.put("delayBetweenMouseDownAndMove", 0.3);
overrides.put("delayAfterDrag", 0.7);
```

#### Type Actions
```java
overrides.put("typeDelay", 0.1);
overrides.put("modifiers", "CTRL+SHIFT");
```

## ActionChainBuilder

The `ActionChainBuilder` provides a fluent API for creating complex action sequences. It transforms verbose manual chain construction into readable, declarative code.

### Related Documentation

- **[Combining Find Operations](finding-objects/combining-finds.md)** - Learn about nested and confirmed find strategies
- **[Conditional Action Chaining](/docs/ai-brobot-project-creation#conditionalactionchain---the-foundation)** - Advanced conditional execution patterns
- **[AI Project Creation Guide](/docs/ai-brobot-project-creation)** - Complete guide with chaining examples

### Benefits

- **Readability**: Clear, self-documenting action sequences
- **Type Safety**: Compile-time checking of chain construction
- **Flexibility**: Easy modification and extension of chains
- **Reduced Errors**: Eliminates manual list building mistakes

### Basic Usage

```java
// Simple find-and-click chain
ActionChainOptions chain = ActionChainBuilder
    .of(new PatternFindOptions.Builder().build())
    .then(new ClickOptions.Builder().build())
    .build();

// Using action types for clarity
ActionChainOptions chain = ActionChainBuilder
    .of(ActionInterface.Type.FIND, findOptions)
    .then(ActionInterface.Type.CLICK, clickOptions)
    .then(ActionInterface.Type.TYPE, typeOptions)
    .build();
```

### Chaining Strategies

Action chains support two strategies that control how results flow between actions:

#### NESTED Strategy (Default)
Each action searches within the results of the previous action. Perfect for hierarchical searches.

```java
ActionChainOptions nestedChain = ActionChainBuilder
    .of(dialogFindOptions)      // Find dialog
    .then(buttonFindOptions)    // Find button within dialog
    .then(clickOptions)         // Click the button
    .nested()  // or .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .build();
```

#### CONFIRM Strategy
Each action validates the results of the previous action. Ideal for eliminating false positives.

```java
ActionChainOptions confirmChain = ActionChainBuilder
    .of(patternFindOptions)     // Find by pattern
    .then(colorFindOptions)     // Confirm by color
    .then(textFindOptions)      // Confirm by text
    .confirm()  // or .withStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .build();
```

### Complex Example: Drag Operation

Here's how the ActionChainBuilder simplifies a complex drag operation:

```java
// Before: Verbose manual construction
PatternFindOptions findSource = new PatternFindOptions.Builder().build();
PatternFindOptions findTarget = new PatternFindOptions.Builder().build();
MouseMoveOptions moveToSource = new MouseMoveOptions.Builder().build();
MouseDownOptions mouseDown = new MouseDownOptions.Builder()
    .setPauseAfterEnd(0.5).build();
MouseMoveOptions moveToTarget = new MouseMoveOptions.Builder().build();
MouseUpOptions mouseUp = new MouseUpOptions.Builder()
    .setPauseAfterEnd(0.5).build();

List<ActionConfig> actions = new ArrayList<>();
actions.add(findSource);
actions.add(findTarget);
// ... etc

// After: Clean, declarative chain
ActionChainOptions dragChain = ActionChainBuilder
    .of(ActionInterface.Type.FIND, findSourceOptions)      // Find source
    .then(ActionInterface.Type.FIND, findTargetOptions)    // Find target
    .then(ActionInterface.Type.MOVE, moveToSourceOptions)  // Move to source
    .then(ActionInterface.Type.MOUSE_DOWN, mouseDownOptions)
    .then(ActionInterface.Type.MOVE, moveToTargetOptions)  // Move to target
    .then(ActionInterface.Type.MOUSE_UP, mouseUpOptions)
    .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .pauseAfterEnd(1.0)
    .build();
```

### Builder Methods

#### Chain Construction
- `of(ActionConfig)` - Start chain with initial action
- `of(ActionInterface.Type, ActionConfig)` - Start with type and config
- `then(ActionConfig)` - Add next action
- `then(ActionInterface.Type, ActionConfig)` - Add with type
- `thenAll(ActionConfig...)` - Add multiple actions at once

#### Configuration
- `withStrategy(ChainingStrategy)` - Set chaining strategy
- `nested()` - Use NESTED strategy
- `confirm()` - Use CONFIRM strategy
- `pauseBeforeBegin(double)` - Pause before chain starts
- `pauseAfterEnd(double)` - Pause after chain completes
- `illustrate(Illustrate)` - Set illustration behavior
- `logEventType(LogEventType)` - Set logging type

#### Static Factory Methods
- `simple(first, second)` - Create two-action chain
- `fromList(actions)` - Create from action list

### Integration Example

Here's how the factory and builder work together:

```java
@Component
public class LoginAutomation {
    
    @Autowired
    private ActionConfigFactory factory;
    
    @Autowired
    private ActionChainExecutor executor;
    
    public void performLogin(String username, String password) {
        // Create configurations using factory
        ActionConfig findUsername = factory.create(ActionInterface.Type.FIND, 
            Map.of("pauseAfterEnd", 0.5));
        
        ActionConfig clickUsername = factory.create(ActionInterface.Type.CLICK);
        
        ActionConfig typeUsername = factory.create(ActionInterface.Type.TYPE,
            Map.of("typeDelay", 0.05));
        
        ActionConfig findPassword = factory.create(ActionInterface.Type.FIND);
        
        ActionConfig clickPassword = factory.create(ActionInterface.Type.CLICK);
        
        ActionConfig typePassword = factory.create(ActionInterface.Type.TYPE,
            Map.of("typeDelay", 0.05, "modifiers", ""));
        
        ActionConfig findSubmit = factory.create(ActionInterface.Type.FIND);
        
        ActionConfig clickSubmit = factory.create(ActionInterface.Type.CLICK,
            Map.of("pauseAfterEnd", 2.0));
        
        // Build the login sequence
        ActionChainOptions loginChain = ActionChainBuilder
            .of(findUsername)
            .then(clickUsername)
            .then(typeUsername)
            .then(findPassword)
            .then(clickPassword)
            .then(typePassword)
            .then(findSubmit)
            .then(clickSubmit)
            .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
            .illustrate(ActionConfig.Illustrate.YES)
            .build();
        
        // Execute the chain
        ActionResult result = executor.execute(loginChain, 
            usernameField, username, passwordField, password, submitButton);
    }
}
```

## Best Practices

### Use the Factory for Consistency
Always use `ActionConfigFactory` instead of manually constructing options:

```java
// Good
ActionConfig config = factory.create(ActionInterface.Type.CLICK);

// Avoid
ClickOptions config = new ClickOptions.Builder().build();
```

### Build Readable Chains
Use descriptive variable names and comments for complex chains:

```java
ActionChainOptions saveDocument = ActionChainBuilder
    .of(findFileMenu)         // Open File menu
    .then(findSaveOption)     // Find Save option
    .then(clickSave)          // Click Save
    .then(findDialog)         // Wait for save dialog
    .then(typeFilename)       // Enter filename
    .then(clickOK)            // Confirm save
    .nested()
    .build();
```

### Reuse Common Configurations
Create reusable configurations for common patterns:

```java
// Create a reusable double-click configuration
ActionConfig doubleClick = factory.create(ActionInterface.Type.CLICK,
    Map.of("numberOfClicks", 2, "pauseAfterEnd", 0.5));

// Use it in multiple chains
ActionChainOptions openFile1 = ActionChainBuilder
    .of(findFile1)
    .then(doubleClick)
    .build();

ActionChainOptions openFile2 = ActionChainBuilder
    .of(findFile2)
    .then(doubleClick)
    .build();
```

### Handle Chain Results
Always check the results of chain execution:

```java
ActionResult result = executor.execute(chain, objects);

if (result.isSuccess()) {
    // Handle success
    Match lastMatch = result.getLastMatch();
    processMatch(lastMatch);
} else {
    // Handle failure
    log.error("Chain failed at action: " + result.getFailedActionDescription());
}
```

## Summary

The `ActionConfigFactory` and `ActionChainBuilder` patterns significantly improve code quality in Brobot applications by:

1. **Reducing Complexity**: Hide configuration details behind simple factory methods
2. **Improving Readability**: Transform complex sequences into declarative chains
3. **Ensuring Consistency**: Centralize configuration logic in one place
4. **Preventing Errors**: Eliminate manual object construction and list building
5. **Enhancing Maintainability**: Make changes in one place affect all usages

These patterns follow the principle of making the easy path the correct path, guiding developers toward writing better automation code.