---
sidebar_position: 13
title: Examples Index
description: Complete index of ActionConfig examples and patterns
---

# ActionConfig Examples Index

This page provides a comprehensive index of all ActionConfig examples, organized by use case and complexity level.

## Quick Start Examples

### Basic Actions

#### Click Examples
```java
// Simple click
ClickOptions click = new ClickOptions.Builder().build();

// Right-click with pause
ClickOptions rightClick = new ClickOptions.Builder()
    .setClickType(ClickOptions.ClickType.RIGHT)
    .setPauseAfterEnd(0.5)
    .build();

// Double-click
ClickOptions doubleClick = new ClickOptions.Builder()
    .setClickType(ClickOptions.ClickType.DOUBLE)
    .build();
```

#### Type Examples
```java
// Basic typing
TypeOptions type = new TypeOptions.Builder()
    .setText("Hello World")
    .build();

// Typing with delay
TypeOptions slowType = new TypeOptions.Builder()
    .setText("Important text")
    .setTypeDelay(0.1)
    .build();

// Clear and type
TypeOptions clearAndType = new TypeOptions.Builder()
    .setText("New text")
    .setClearFirst(true)
    .build();
```

#### Find Examples
```java
// Find first match
PatternFindOptions findFirst = new PatternFindOptions.Builder()
    .setStrategy(FindStrategy.FIRST)
    .build();

// Find best match with similarity
PatternFindOptions findBest = new PatternFindOptions.Builder()
    .setStrategy(FindStrategy.BEST)
    .setSimilarity(0.9)
    .build();

// Find all matches
PatternFindOptions findAll = new PatternFindOptions.Builder()
    .setStrategy(FindStrategy.ALL)
    .setPauseBeforeBegin(1.0)
    .build();
```

## Action Chaining Examples

### Sequential Actions
[View detailed examples](./07-action-chaining.md)

```java
// Click and type
ActionChainOptions clickAndType = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder()
        .setText("Hello")
        .build())
    .build();

// Multi-step form filling
ActionChainOptions formFill = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().setText("John").build())
    .then(new TypeOptions.Builder().setText("\t").build())
    .then(new TypeOptions.Builder().setText("Doe").build())
    .build();
```

### Nested Actions
```java
// Find dialog, then find button within
ActionChainOptions nestedFind = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setStrategy(FindStrategy.FIRST)
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(new PatternFindOptions.Builder()
        .setStrategy(FindStrategy.FIRST)
        .build())
    .build();
```

## Conditional Actions

### Click Until Examples
[View detailed examples](./09-conditional-actions.md)

```java
// Click until image appears (max 10 clicks)
public boolean clickUntilImageAppears(StateImage button, StateImage target) {
    RepeatUntilConfig config = new RepeatUntilConfig.Builder()
        .setDoAction(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .setActionObjectCollection(button.asObjectCollection())
        .setUntilAction(new PatternFindOptions.Builder()
            .setStrategy(FindStrategy.FIRST)
            .setPauseBeforeBegin(2.0)
            .build())
        .setConditionObjectCollection(target.asObjectCollection())
        .setMaxActions(10)
        .build();
    
    return repeatUntilExecutor.execute(config);
}
```

### Wait Patterns
```java
// Wait for element to vanish
VanishOptions vanish = new VanishOptions.Builder()
    .setPauseBeforeBegin(1.0)
    .setTimeout(10.0)
    .build();

// Wait with custom intervals
public boolean waitForCondition(StateImage element, int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
        if (find(element)) {
            return true;
        }
        pause(1.0);
    }
    return false;
}
```

## Complex Workflows

### Form Automation
[View detailed examples](./10-form-automation.md)

```java
// Complete registration form
public boolean fillRegistrationForm(UserData data) {
    return new ActionChainOptions.Builder(
        // First name
        new ClickOptions.Builder().build())
        .then(new TypeOptions.Builder()
            .setText(data.getFirstName())
            .setClearFirst(true)
            .build())
        // Last name
        .then(new ClickOptions.Builder().build())
        .then(new TypeOptions.Builder()
            .setText(data.getLastName())
            .setClearFirst(true)
            .build())
        // Email
        .then(new ClickOptions.Builder().build())
        .then(new TypeOptions.Builder()
            .setText(data.getEmail())
            .setClearFirst(true)
            .build())
        // Submit
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(1.0)
            .build())
        .build();
}
```

### Navigation Patterns
[View detailed examples](./08-complex-workflows.md)

```java
// Navigate through menu hierarchy
public boolean navigateToSettings() {
    return new ActionChainOptions.Builder(
        // Open menu
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        // Find settings option
        .then(new PatternFindOptions.Builder()
            .setStrategy(FindStrategy.FIRST)
            .build())
        // Click settings
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .build();
}
```

## Reusable Patterns

### Pattern Library
[View detailed examples](./11-reusable-patterns.md)

```java
// Login pattern
public class LoginPattern implements AutomationPattern {
    @Override
    public boolean execute(PatternContext context) {
        return new ActionChainOptions.Builder(
            // Username
            new ClickOptions.Builder().build())
            .then(new TypeOptions.Builder()
                .setText(context.getParameter("username", String.class))
                .build())
            // Password
            .then(new ClickOptions.Builder().build())
            .then(new TypeOptions.Builder()
                .setText(context.getParameter("password", String.class))
                .build())
            // Submit
            .then(new ClickOptions.Builder().build())
            .build();
    }
}
```

## Mouse Actions

### Drag and Drop
```java
// Using DragOptions
DragOptions drag = new DragOptions.Builder()
    .setDragDelay(0.5)
    .build();

// Custom drag with chain
ActionChainOptions customDrag = new ActionChainOptions.Builder(
    new MouseMoveOptions.Builder().build())
    .then(new MouseDownOptions.Builder().build())
    .then(new MouseMoveOptions.Builder().build())
    .then(new MouseUpOptions.Builder().build())
    .build();
```

### Scrolling
```java
// Scroll down
ScrollOptions scrollDown = new ScrollOptions.Builder()
    .setDirection(ScrollOptions.Direction.DOWN)
    .setScrollSteps(5)
    .build();

// Scroll until element visible
public boolean scrollToElement(StateImage element) {
    for (int i = 0; i < 10; i++) {
        if (find(element)) return true;
        
        scroll(new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.DOWN)
            .setScrollSteps(3)
            .build());
        
        pause(0.5);
    }
    return false;
}
```

## Keyboard Actions

### Shortcuts
```java
// Ctrl+C copy
ActionChainOptions copy = new ActionChainOptions.Builder(
    new KeyDownOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().setText("c").build())
    .then(new KeyUpOptions.Builder().setKey("ctrl").build())
    .build();

// Select all and delete
ActionChainOptions selectAllDelete = new ActionChainOptions.Builder(
    new KeyDownOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().setText("a").build())
    .then(new KeyUpOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().setText("\b").build())
    .build();
```

## Find Strategies

### Text Finding
```java
TextFindOptions findText = new TextFindOptions.Builder()
    .setMaxMatchRetries(3)
    .build();
```

### Color Finding
```java
ColorFindOptions findColor = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.KMEANS)
    .setKmeans(3)  // Find 3 dominant colors
    .build();
```

### Motion Detection
```java
MotionFindOptions findMotion = new MotionFindOptions.Builder()
    .setMotionThreshold(0.1)
    .build();
```

## Error Handling

### Retry Patterns
```java
public boolean performWithRetry(ActionConfig action, 
                               ObjectCollection target,
                               int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        ActionResult result = perform(action, target);
        if (result.isSuccess()) {
            return true;
        }
        
        logger.warn("Attempt {} failed, retrying...", i + 1);
        pause(1.0);
    }
    return false;
}
```

### Fallback Strategies
```java
public boolean clickWithFallback(StateImage primary, 
                                StateImage fallback) {
    // Try primary target
    if (click(primary)) {
        return true;
    }
    
    // Try fallback
    logger.info("Primary target failed, trying fallback");
    return click(fallback);
}
```

## Performance Optimization

### Batch Operations
```java
// Process multiple items efficiently
public void processItems(List<StateImage> items) {
    ActionChainOptions.Builder chain = null;
    
    for (StateImage item : items) {
        if (chain == null) {
            chain = new ActionChainOptions.Builder(
                new ClickOptions.Builder().build());
        } else {
            chain.then(new ClickOptions.Builder().build());
        }
    }
    
    if (chain != null) {
        executeChain(chain.build(), items);
    }
}
```

## Migration Examples

### Before and After
[View migration guide](./12-migration-guide.md)

```java
// Before (ActionOptions)
ActionOptions old = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .setPauseAfterEnd(0.5)
    .build();

// After (ActionConfig)
ClickOptions new = new ClickOptions.Builder()
    .setPauseAfterEnd(0.5)
    .build();
```

## Resources

- [ActionConfig API Reference](./05-reference.md)

## Contributing Examples

To contribute your own examples:
1. Fork the repository
2. Add your example to the appropriate section
3. Include comments explaining the use case
4. Submit a pull request

Happy automating with Brobot!