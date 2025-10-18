---
sidebar_position: 21
title: 'ActionConfig Factory'
---

# Action Configuration Factory

Brobot provides powerful patterns for creating and managing action configurations through the `ActionConfigFactory` and `ActionChainBuilder` classes. These tools simplify the creation of action configurations and make complex action sequences more readable and maintainable.

## Prerequisites

Before using ActionConfigFactory and ActionChainBuilder, you should be familiar with:
- [ActionConfig Overview](../../action-config/01-overview.md) - Understanding the ActionConfig architecture
- [Action Chaining](../../action-config/07-action-chaining.md) - Comprehensive guide to action chains
- [States](../../../01-getting-started/states.md) - StateObjects for organizing patterns
- [Spring Boot Integration](../auto-configuration.md) - Dependency injection patterns

For hands-on examples, see [ActionConfig Examples](../../action-config/03-examples.md).

## ActionConfigFactory

The `ActionConfigFactory` is a Spring component that centralizes the creation of all `ActionConfig` subclasses. It provides a single point of configuration and reduces coupling between actions and their configuration creation.

### Benefits

- **Type Safety**: Eliminates unsafe casting and `instanceof` checks in action implementations
- **Centralized Logic**: All configuration creation logic in one place
- **Consistent API**: Uniform way to create any action configuration
- **Override Support**: Easy application of custom settings

### Basic Usage

```java
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.ActionConfigFactory;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.config.ActionConfig;
import java.util.Map;
import java.util.HashMap;

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
- `DEFINE` - Creates `DefineRegionOptions`

> **📝 Note**: The `CLICK_UNTIL` action type has been refactored. Use `ClickOptions` combined with `VerificationOptions` and `RepetitionOptions` for click-until behavior. See the [Click Configuration Guide](../../action-config/click-config.md) for details.

### Common Overrides

All action configurations support these common overrides:

```java
import io.github.jspinak.brobot.action.config.ActionConfig;
import java.util.Map;
import java.util.HashMap;

Map<String, Object> overrides = new HashMap<>();
overrides.put("pauseBeforeBegin", 2.0);    // Pause before action starts
overrides.put("pauseAfterEnd", 1.0);       // Pause after action completes
overrides.put("illustrate", ActionConfig.Illustrate.YES);  // Force illustration
overrides.put("successCriteria", result -> result.isSuccess());  // Custom success logic
```

### Type-Specific Overrides

Each action type supports its own specific overrides:

#### Click Actions
```java
import io.github.jspinak.brobot.action.config.options.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.config.options.mouse.MouseButton;
import java.util.Map;
import java.util.HashMap;

Map<String, Object> overrides = new HashMap<>();
overrides.put("numberOfClicks", 2);  // Double-click
overrides.put("mousePressOptions", new MousePressOptions.Builder()
    .setButton(MouseButton.RIGHT)
    .build());
```

#### Drag Actions
```java
import java.util.Map;
import java.util.HashMap;

Map<String, Object> overrides = new HashMap<>();
overrides.put("delayBetweenMouseDownAndMove", 0.3);
overrides.put("delayAfterDrag", 0.7);
```

#### Type Actions
```java
import java.util.Map;
import java.util.HashMap;

Map<String, Object> overrides = new HashMap<>();
overrides.put("typeDelay", 0.1);
overrides.put("modifiers", "CTRL+SHIFT");
```

## ActionChainBuilder

The `ActionChainBuilder` provides a fluent API for creating complex action sequences. It transforms verbose manual chain construction into readable, declarative code.

### Related Documentation

- **[Combining Find Operations](../finding-objects/combining-finds.md)** - Learn about nested and confirmed find strategies
- **[Conditional Action Chaining](../../../01-getting-started/ai-brobot-project-creation.md#conditional-action-chains)** - Advanced conditional execution patterns
- **[AI Project Creation Guide](../../../01-getting-started/ai-brobot-project-creation.md)** - Complete guide with chaining examples

### Benefits

- **Readability**: Clear, self-documenting action sequences
- **Type Safety**: Compile-time checking of chain construction
- **Flexibility**: Easy modification and extension of chains
- **Reduced Errors**: Eliminates manual list building mistakes

### Basic Usage

```java
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.config.ActionConfig;

// Simple find-and-click chain
ActionChainOptions chain = ActionChainBuilder
    .of(new PatternFindOptions.Builder().build())
    .then(new ClickOptions.Builder().build())
    .build();

// Using action types for clarity
ActionConfig findOptions = new PatternFindOptions.Builder().build();
ActionConfig clickOptions = new ClickOptions.Builder().build();
ActionConfig typeOptions = new TypeOptions.Builder().build();

ActionChainOptions chain2 = ActionChainBuilder
    .of(ActionInterface.Type.FIND, findOptions)
    .then(ActionInterface.Type.CLICK, clickOptions)
    .then(ActionInterface.Type.TYPE, typeOptions)
    .build();
```

### Chaining Strategies

Action chains support two strategies that control how results flow between actions:

#### NESTED Strategy (Default)
Each action searches within the results of the previous action. Perfect for hierarchical searches.

> **💡 Best Practice**: Use NESTED strategy when searching for UI elements in a containment hierarchy (e.g., button within dialog, item within menu).

```java
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.config.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

// Define the options
ActionConfig dialogFindOptions = new PatternFindOptions.Builder().build();
ActionConfig buttonFindOptions = new PatternFindOptions.Builder().build();
ActionConfig clickOptions = new ClickOptions.Builder().build();

ActionChainOptions nestedChain = ActionChainBuilder
    .of(dialogFindOptions)      // Find dialog
    .then(buttonFindOptions)    // Find button within dialog
    .then(clickOptions)         // Click the button
    .nested()  // or .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .build();
```

#### CONFIRM Strategy
Each action validates the results of the previous action. Ideal for eliminating false positives.

> **💡 Best Practice**: Use CONFIRM strategy when you need multiple validation criteria to ensure you found the correct element (e.g., pattern + color + text).

```java
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.config.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;

// Define the options
ActionConfig patternFindOptions = new PatternFindOptions.Builder().build();
ActionConfig colorFindOptions = new ColorFindOptions.Builder().build();
ActionConfig textFindOptions = new TextFindOptions.Builder().build();

ActionChainOptions confirmChain = ActionChainBuilder
    .of(patternFindOptions)     // Find by pattern
    .then(colorFindOptions)     // Confirm by color
    .then(textFindOptions)      // Confirm by text
    .confirm()  // or .withStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .build();
```

### Complex Example: Drag Operation

Here's how the ActionChainBuilder simplifies a complex drag operation:

> **⚠️ Complexity Warning**: Manual chain construction is error-prone and verbose. Always prefer ActionChainBuilder for multi-step operations.

```java
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.config.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.ActionInterface;
import java.util.List;
import java.util.ArrayList;

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
actions.add(moveToSource);
actions.add(mouseDown);
actions.add(moveToTarget);
actions.add(mouseUp);
// Manual list building is tedious and error-prone

// After: Clean, declarative chain
ActionConfig findSourceOptions = new PatternFindOptions.Builder().build();
ActionConfig findTargetOptions = new PatternFindOptions.Builder().build();
ActionConfig moveToSourceOptions = new MouseMoveOptions.Builder().build();
ActionConfig mouseDownOptions = new MouseDownOptions.Builder().setPauseAfterEnd(0.5).build();
ActionConfig moveToTargetOptions = new MouseMoveOptions.Builder().build();
ActionConfig mouseUpOptions = new MouseUpOptions.Builder().setPauseAfterEnd(0.5).build();

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

> **📊 Performance**: ActionChainBuilder has zero runtime overhead - it's just a cleaner way to construct the same ActionChainOptions object.

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

#### Static Factory Methods
- `simple(first, second)` - Create two-action chain
- `fromList(actions)` - Create from action list

### Integration Example

Here's how the factory and builder work together:

```java
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.ActionConfigFactory;
import io.github.jspinak.brobot.action.config.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import java.util.Map;

@Component
public class LoginAutomation {

    @Autowired
    private ActionConfigFactory factory;

    @Autowired
    private ActionChainExecutor executor;

    public void performLogin(ObjectCollection usernameField, String username,
                            ObjectCollection passwordField, String password,
                            ObjectCollection submitButton) {
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
        ActionResult initialResult = new ActionResult();
        ActionResult result = executor.executeChain(loginChain, initialResult,
            usernameField, passwordField, submitButton);
    }
}
```

## Best Practices

### Use the Factory for Consistency
Always use `ActionConfigFactory` instead of manually constructing options:

> **💡 Architecture Tip**: The factory pattern centralizes configuration logic and makes it easier to apply organization-wide defaults.

```java
import io.github.jspinak.brobot.action.ActionConfigFactory;
import io.github.jspinak.brobot.action.config.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private ActionConfigFactory factory;

// Good - Uses factory
ActionConfig config = factory.create(ActionInterface.Type.CLICK);

// Avoid - Direct construction bypasses factory defaults
ClickOptions config = new ClickOptions.Builder().build();
```

> **⚠️ Warning**: Direct construction of options bypasses any organization-wide configuration defaults set in the factory.

### Build Readable Chains
Use descriptive variable names and comments for complex chains:

> **💡 Readability Tip**: Inline comments in chains serve as executable documentation. They help team members understand the automation flow at a glance.

```java
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.config.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;

// Pre-define action configurations with clear names
ActionConfig findFileMenu = new PatternFindOptions.Builder().build();
ActionConfig findSaveOption = new PatternFindOptions.Builder().build();
ActionConfig clickSave = new ClickOptions.Builder().build();
ActionConfig findDialog = new PatternFindOptions.Builder().build();
ActionConfig typeFilename = new TypeOptions.Builder().build();
ActionConfig clickOK = new ClickOptions.Builder().build();

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

> **💡 DRY Principle**: Define common action configurations once and reuse them throughout your automation. This ensures consistency and simplifies maintenance.

```java
import io.github.jspinak.brobot.action.ActionConfigFactory;
import io.github.jspinak.brobot.action.config.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Autowired
private ActionConfigFactory factory;

// Create a reusable double-click configuration
ActionConfig doubleClick = factory.create(ActionInterface.Type.CLICK,
    Map.of("numberOfClicks", 2, "pauseAfterEnd", 0.5));

// Pre-define find options
ActionConfig findFile1 = new PatternFindOptions.Builder().build();
ActionConfig findFile2 = new PatternFindOptions.Builder().build();

// Use reusable config in multiple chains
ActionChainOptions openFile1 = ActionChainBuilder
    .of(findFile1)
    .then(doubleClick)  // Reused configuration
    .build();

ActionChainOptions openFile2 = ActionChainBuilder
    .of(findFile2)
    .then(doubleClick)  // Same configuration, different context
    .build();
```

### Handle Chain Results
Always check the results of chain execution:

> **⚠️ Critical**: Always check ActionResult.isSuccess() before accessing result data. Chain failures should be logged and handled appropriately.

```java
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private ActionChainExecutor executor;

private static final Logger log = LoggerFactory.getLogger(YourClass.class);

// Execute chain
ActionResult initialResult = new ActionResult();
ActionResult result = executor.executeChain(chain, initialResult, objects);

if (result.isSuccess()) {
    // Handle success
    Match lastMatch = result.getBestMatch().orElse(null);
    if (lastMatch != null) {
        processMatch(lastMatch);
        log.info("Chain completed successfully");
    }
} else {
    // Handle failure - log details for debugging
    log.error("Chain failed: {}", result.getText());
    log.error("Failed at action index: {}", result.getActionIndex());
    // Consider retry logic or fallback behavior here
}
```

> **📊 Debugging Tip**: ActionResult contains detailed failure information including the action index where the chain failed. Use this for targeted debugging.

## Troubleshooting

### Issue: Factory Not Autowired (NoSuchBeanDefinitionException)

**Symptoms**: `NoSuchBeanDefinitionException: No qualifying bean of type 'ActionConfigFactory'`

**Root Cause**: Spring component scanning not finding the factory, or using factory outside Spring context

**Solution**:
```java
// Ensure your main class enables component scanning
@SpringBootApplication
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "your.package"})
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

**Prevention**: Always use `@Autowired` for ActionConfigFactory in Spring-managed components.

### Issue: Wrong Action Type in Chain

**Symptoms**: Chain behaves unexpectedly, actions execute in wrong order or with wrong configurations

**Root Cause**: Mismatch between ActionInterface.Type and actual ActionConfig subclass

**Solution**:
```java
// WRONG - Type doesn't match config
ActionChainBuilder.of(ActionInterface.Type.CLICK, new PatternFindOptions.Builder().build())

// CORRECT - Type matches config
ActionChainBuilder.of(ActionInterface.Type.FIND, new PatternFindOptions.Builder().build())
// OR just use the config without type
ActionChainBuilder.of(new PatternFindOptions.Builder().build())
```

**Prevention**: When using ActionInterface.Type, ensure it matches the ActionConfig subclass. Or skip the type parameter and let the builder infer it.

### Issue: Override Not Applied

**Symptoms**: Configuration override in factory.create() has no effect

**Root Cause**: Override key doesn't match the actual field name, or field is not settable

**Solution**:
```java
// Check the exact field name in the Options class
// WRONG - incorrect key
Map<String, Object> overrides = Map.of("clickCount", 2);  // Field is "numberOfClicks"

// CORRECT - exact field name
Map<String, Object> overrides = Map.of("numberOfClicks", 2);
```

**Prevention**: Refer to the [ActionConfig Reference](../../action-config/05-reference.md) for correct field names.

### Issue: Chain Fails at First Action

**Symptoms**: Chain immediately fails, ActionResult.isSuccess() returns false

**Root Cause**: Initial ActionResult is not properly initialized, or first action's search region is invalid

**Solution**:
```java
// Ensure initialResult is properly created
ActionResult initialResult = new ActionResult();
initialResult.setSuccess(true);  // Mark as successful starting point

// Or use a pre-populated result if continuing from previous action
ActionResult result = executor.executeChain(chain, initialResult, objectCollections);
```

**Prevention**: Always initialize ActionResult before passing to executeChain.

### Issue: Import Errors

**Symptoms**: Cannot resolve symbol for ActionConfigFactory or ActionChainBuilder

**Root Cause**: Missing Brobot dependency or wrong import paths

**Solution**:
```java
// Correct imports for Brobot 1.1.0+
import io.github.jspinak.brobot.action.ActionConfigFactory;
import io.github.jspinak.brobot.action.config.ActionChainBuilder;
import io.github.jspinak.brobot.action.config.ActionChainOptions;
import io.github.jspinak.brobot.action.config.ActionConfig;
```

**Prevention**: Ensure Brobot 1.1.0+ is in your dependencies and imports use the correct package structure.

### Issue: TextFindOptions Not Found

**Symptoms**: Cannot resolve TextFindOptions class used in CONFIRM strategy example

**Root Cause**: The example shows a hypothetical TextFindOptions that may not exist in all Brobot versions

**Solution**: Use available find options classes:
```java
// Use available find strategy classes
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;

// For text-based finding, use PatternFindOptions with OCR
PatternFindOptions textFind = new PatternFindOptions.Builder()
    .setSearchType(PatternFindOptions.SearchType.TEXT)
    .build();
```

**Prevention**: Check the [ActionConfig Reference](../../action-config/05-reference.md) for available Options classes in your Brobot version.

## Performance Considerations

### Factory Overhead

**Performance Impact**: ✅ **Negligible**

The ActionConfigFactory has minimal overhead:
- Factory method calls are O(1) lookups in an immutable map
- No reflection or dynamic class loading
- Override application is simple Map iteration
- **Typical overhead: < 0.1ms per create() call**

```java
// Performance characteristics
factory.create(ActionInterface.Type.CLICK);  // ~0.05ms
factory.create(ActionInterface.Type.CLICK, overrides);  // ~0.1ms with overrides
```

### ActionChainBuilder Overhead

**Performance Impact**: ✅ **Zero Runtime Overhead**

ActionChainBuilder is purely compile-time:
- Builder pattern constructs ActionChainOptions at build time
- No additional objects created at runtime vs manual construction
- **No performance difference between builder and manual construction**

```java
// These two approaches have IDENTICAL runtime performance
ActionChainOptions manual = new ActionChainOptions(actions, strategy, ...);
ActionChainOptions builder = ActionChainBuilder.of(...).then(...).build();
```

### Chain Execution Performance

**Performance Factors**:

1. **Number of Actions**: Linear scaling - each action adds its execution time
2. **Strategy Choice**:
   - **NESTED**: Faster for hierarchical searches (reduces search space)
   - **CONFIRM**: Slower but more accurate (runs all validations)

```java
// NESTED strategy performance
ActionChainBuilder.of(findDialog)       // Search full screen
    .then(findButton)                    // Search only in dialog (faster!)
    .nested()
    .build();
// Typical: 50-100ms per action with image matching

// CONFIRM strategy performance
ActionChainBuilder.of(findPattern)       // Search full screen
    .then(findColor)                     // Search full screen again
    .then(findText)                      // Search full screen again
    .confirm()
    .build();
// Typical: 3x slower than NESTED, but more accurate
```

### Optimization Tips

**1. Reuse ActionConfig instances**:
```java
// Good - create once, use many times
ActionConfig doubleClick = factory.create(ActionInterface.Type.CLICK,
    Map.of("numberOfClicks", 2));

for (File file : files) {
    ActionChainBuilder.of(findFile).then(doubleClick).build();  // Reuse config
}
```

**2. Use appropriate chain strategy**:
```java
// Use NESTED when you know elements are hierarchically related
ActionChainBuilder.of(findContainer).then(findItem).nested().build();

// Use CONFIRM only when you need multiple validation criteria
ActionChainBuilder.of(findByShape).then(findByColor).confirm().build();
```

**3. Minimize chain length**:
```java
// Avoid excessive chaining - split into logical units
// AVOID: 20-action mega-chain
// PREFER: 3-4 chains of 4-6 actions each
```

### Memory Considerations

**Factory**: ✅ **Minimal Memory Usage**
- Single factory instance per Spring context
- Immutable configuration map (~1KB)
- No memory leaks possible

**Chains**: ✅ **Efficient Memory Usage**
- ActionChainOptions holds list of ActionConfig references
- No copying of configuration data
- Typical chain: < 1KB memory

## Summary

The `ActionConfigFactory` and `ActionChainBuilder` patterns significantly improve code quality in Brobot applications by:

1. **Reducing Complexity**: Hide configuration details behind simple factory methods
2. **Improving Readability**: Transform complex sequences into declarative chains
3. **Ensuring Consistency**: Centralize configuration logic in one place
4. **Preventing Errors**: Eliminate manual object construction and list building
5. **Enhancing Maintainability**: Make changes in one place affect all usages

These patterns follow the principle of making the easy path the correct path, guiding developers toward writing better automation code.

## Related Documentation

### Core Concepts
- **[ActionConfig Overview](../../action-config/01-overview.md)** - Understanding ActionConfig architecture
- **[Action Chaining](../../action-config/07-action-chaining.md)** - Comprehensive guide to action chains and fluent API patterns
- **[ActionConfig Reference](../../action-config/05-reference.md)** - Complete API documentation
- **[States in Brobot](../../../01-getting-started/states.md)** - State management for automation

### Examples and Patterns
- **[ActionConfig Examples](../../action-config/03-examples.md)** - Code examples and patterns
- **[Conditional Chains Examples](../../action-config/15-conditional-chains-examples.md)** - Real-world conditional patterns
- **[Combining Find Operations](../finding-objects/combining-finds.md)** - Nested and confirmed find strategies
- **[Reusable Patterns](../../action-config/11-reusable-patterns.md)** - Creating reusable configurations

### Configuration and Testing
- **[Auto-Configuration](../auto-configuration.md)** - Spring Boot integration
- **[Testing Overview](../../../04-testing/testing-intro.md)** - Testing factory patterns
- **[Mock Mode Guide](../../../04-testing/mock-mode-guide.md)** - Using factories in tests

### Advanced Topics
- **[Troubleshooting Chains](../../action-config/troubleshooting-chains.md)** - Debugging chain execution
- **[Migration Guide](../../migration/actionoptions-to-actionconfig.md)** - Migrating from ActionOptions
- **[AI Project Creation Guide](../../../01-getting-started/ai-brobot-project-creation.md)** - Complete guide with factory examples