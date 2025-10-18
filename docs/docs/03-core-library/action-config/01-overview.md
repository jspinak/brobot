---
sidebar_position: 1
---

# ActionConfig Overview

## Introduction

ActionConfig is the new foundation for configuring automation actions in Brobot. It replaces the monolithic `ActionOptions` class with a more modular, type-safe hierarchy of configuration classes.

## Why ActionConfig?

The previous `ActionOptions` class had several limitations:

- **One size fits all**: A single class tried to handle configuration for all action types
- **Type safety**: No compile-time checking for action-specific options
- **Complexity**: Many fields were only relevant to specific actions
- **Maintenance**: Adding new actions required modifying the central ActionOptions class

ActionConfig solves these problems by:

- **Modularity**: Each action has its own configuration class
- **Type safety**: The compiler ensures you're using the right options for each action
- **Clarity**: Each configuration class only contains relevant fields
- **Extensibility**: New actions can be added without modifying existing code

## The ActionConfig Hierarchy

```
ActionConfig (abstract base)
├── BaseFindOptions (abstract)
│   ├── PatternFindOptions
│   ├── HistogramFindOptions
│   ├── MotionFindOptions
│   ├── ColorFindOptions
│   ├── TextFindOptions
│   └── VanishOptions
├── ClickOptions
├── TypeOptions
├── MouseMoveOptions
├── MouseDownOptions
├── MouseUpOptions
├── MousePressOptions
├── ScrollOptions
├── DefineRegionOptions
├── HighlightOptions
├── DragOptions
├── KeyDownOptions
├── KeyUpOptions
├── TimeOptions
└── PlaybackOptions
```

See [ActionConfig Reference](./05-reference.md) for detailed documentation of each class.

## Key Concepts

### 1. Builder Pattern

All ActionConfig classes use the builder pattern for construction:

```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

ClickOptions click = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPauseBeforeBegin(0.5)
    .build();
```

### 2. Fluent Chaining

Actions can be chained together using the `then()` method:

```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

BaseFindOptions findAndClick = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build())
    .build();
```

See [Action Chaining](./07-action-chaining.md) for advanced chaining patterns.

### 3. Composition Over Inheritance

Shared configurations like `MousePressOptions` are composed rather than inherited:

```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.manageStates.mouse.MouseButton;

ClickOptions rightClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build();
```

## Common Base Properties

All ActionConfig classes inherit these properties from the base class:

- `pauseBeforeBegin` - Delay before starting the action
- `pauseAfterEnd` - Delay after completing the action
- `illustrate` - Whether to create visual feedback for debugging
- `successCriteria` - Custom success validation logic
- `subsequentActions` - Chained actions to execute after this action

See [ActionConfig Base Class](./05-reference.md#actionconfig-base-class) for complete property documentation.

## Getting Started

To start using ActionConfig:

### Option 1: Convenience Methods (Recommended for Simple Operations)

As of Brobot 1.1.0, the simplest way to perform common actions is through convenience methods:

```java
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.sikuli.script.Pattern;
import org.sikuli.script.Location;

@Autowired
private Action action;

// Direct actions without ObjectCollection
action.click(region);
action.type("text");
action.find(pattern);
action.move(location);
```

These methods handle the ObjectCollection creation for you. See [Convenience Methods](./18-convenience-methods.md) for details.

### Option 2: Traditional ActionConfig Approach

For more complex operations or when you need fine control:

1. Choose the appropriate Options class for your action
2. Use the builder to configure it
3. Pass it to the `action.perform()` method
4. Process the `ActionResult`

Example:

```java
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

@Autowired
private Action action;

// Define the button image to find
StateImage buttonImage = new StateImage.Builder()
    .withPattern(new Pattern("button.png"))
    .build();

// Find and click the button with custom similarity
ActionResult result = action.perform(
    new PatternFindOptions.Builder()
        .setSimilarity(0.85)
        .then(new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build())
        .build(),
    buttonImage
);

if (result.isSuccess()) {
    System.out.println("Button clicked successfully!");
}
```

See [ActionResult Components](./17-actionresult-components.md) for details on processing results.

## Next Steps

- [Upgrading to Latest](../migration/upgrading-to-latest.md) - Learn how to migrate from ActionOptions
- [Code Examples](./03-examples.md) - See ActionConfig in action
- [Action Chaining](./07-action-chaining.md) - Master action chaining patterns
- [API Reference](./05-reference.md) - Detailed API documentation

## Related Documentation

- [Action Hierarchy](../../01-getting-started/action-hierarchy.md) - Understanding the Action system
- [States Guide](../../01-getting-started/states.md) - Working with states and state images
- [Builder Performance Guide](../advanced/builder-performance-guide.md) - Advanced builder techniques
- [Configuration Properties](../configuration/properties-reference.md) - Application-level configuration
