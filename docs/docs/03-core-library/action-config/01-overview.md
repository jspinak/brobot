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
│   └── VanishOptions
├── ClickOptions
├── TypeOptions
├── MouseMoveOptions
├── MouseDownOptions
├── MouseUpOptions
├── ScrollMouseWheelOptions
├── DefineRegionOptions
├── HighlightOptions
├── DragOptions
├── ClickUntilOptions
└── PlaybackOptions
```

## Key Concepts

### 1. Builder Pattern

All ActionConfig classes use the builder pattern for construction:

```java
ClickOptions click = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPauseBeforeBegin(0.5)
    .build();
```

### 2. Fluent Chaining

Actions can be chained together using the `then()` method:

```java
BaseFindOptions findAndClick = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build())
    .build();
```

### 3. Composition Over Inheritance

Shared configurations like `MousePressOptions` are composed rather than inherited:

```java
ClickOptions rightClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setPressOptions(new MousePressOptions.Builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build();
```

## Common Base Properties

All ActionConfig classes inherit these properties from the base class:

- `pauseBeforeBegin` - Delay before starting the action
- `pauseAfterEnd` - Delay after completing the action
- `illustrate` - Whether to create visual feedback
- `successCriteria` - Custom success validation
- `subsequentActions` - Chained actions to execute

## Getting Started

To start using ActionConfig:

1. Choose the appropriate Options class for your action
2. Use the builder to configure it
3. Pass it to the `action.perform()` method
4. Process the `ActionResult`

Example:

```java
// Find and click a button
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

## Next Steps

- [Quick Migration Reference](./quick-migration-reference) - Learn how to migrate from ActionOptions
- [Code Examples](./examples) - See ActionConfig in action
- [Fluent API Guide](./fluent-api) - Master the fluent API patterns
- [API Reference](./reference) - Detailed API documentation