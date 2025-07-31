---
sidebar_position: 8
title: 'Pure Actions Quick Start Guide'
---

# Pure Actions Quick Start Guide

## What are Pure Actions?

Pure actions are a new approach in Brobot that separates finding elements from performing actions on them. This makes your automation code clearer, more testable, and more efficient.

## The Old Way vs The New Way

### Old Way (Embedded Find)
```java
// Find and click happen together - you can't control them separately
action.click(buttonImage);
```

### New Way (Pure Actions)
```java
// Find first
ActionResult found = action.find(buttonImage);

// Then click if found
if (found.isSuccess()) {
    action.perform(ActionType.CLICK, found.getFirstMatch());
}
```

### Even Better Way (Conditional Chains)
```java
// Elegant chaining with automatic conditional execution
ConditionalActionChain.find(findOptions)
    .ifFound(click())
    .ifNotFound(log("Button not found"))
    .perform(action, objectCollection);
```

## Your First Pure Action

Let's start with a simple example - clicking a button:

```java
// Step 1: Import the required classes
import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

// Step 2: Create your target image
StateImage submitButton = new StateImage.Builder()
    .addPattern("submit-button")  // No .png extension needed
    .build();

// Step 3: Find and click using conditional chain
ActionResult result = ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFound(new ClickOptions.Builder().build())
    .perform(action, new ObjectCollection.Builder()
        .withImages(submitButton)
        .build());
```

## Common Use Cases

### 1. Click a Button
```java
// Simplest form - using convenience method
Location buttonLocation = new Location(100, 200);
action.perform(ActionType.CLICK, buttonLocation);

// Or with an image
ConditionalActionChain.find(findOptions)
    .ifFound(click())
    .perform(action, objectCollection);
```

### 2. Type in a Field
```java
// Type at current cursor position
action.perform(ActionType.TYPE, "Hello World");

// Find field and type
ConditionalActionChain.find(findOptions)
    .ifFound(clickOptions)
    .then(typeOptions)
    .perform(action, objectCollection);
```

### 3. Highlight Found Elements
```java
// Find all matching elements and highlight them
ActionResult matches = action.find(targetPattern);
for (Match match : matches.getMatchList()) {
    action.perform(ActionType.HIGHLIGHT, match.getRegion());
}
```

### 4. Right-Click Menu
```java
ConditionalActionChain.find(findOptions)
    .ifFound(rightClickOptions)
    .then(findDeleteOptions)
    .ifFound(clickOptions)
    .perform(action, objectCollection);
```

## Convenience Methods

The Action class now provides simple one-line methods for common operations:

```java
// Click at a location
action.perform(ActionType.CLICK, new Location(100, 200));

// Highlight a region  
action.perform(ActionType.HIGHLIGHT, new Region(50, 50, 200, 100));

// Type text
action.perform(ActionType.TYPE, "Hello World");

// Double-click
action.perform(ActionType.DOUBLE_CLICK, location);

// Right-click
action.perform(ActionType.RIGHT_CLICK, region);
```

## Working with Results

Pure actions give you more control over results:

```java
// Find returns matches you can work with
ActionResult findResult = action.find(targetImage);

if (findResult.isSuccess()) {
    // Get the first match
    Match firstMatch = findResult.getFirstMatch();
    
    // Get all matches
    List<Match> allMatches = findResult.getMatchList();
    
    // Work with each match
    for (Match match : allMatches) {
        // Highlight each found instance
        action.perform(ActionType.HIGHLIGHT, match.getRegion());
        
        // Click each one
        action.perform(ActionType.CLICK, match.getRegion());
    }
}
```

## Error Handling

Pure actions make error handling explicit and clear:

```java
ConditionalActionChain.find(criticalButton)
    .ifFound(click())
    .ifNotFound(log("ERROR: Critical button not found!"))
    .ifNotFoundDo(result -> {
        // Custom error handling
        takeScreenshot("error-state");
        notifyUser("Application in unexpected state");
    })
    .perform(action, objectCollection);
```

## Best Practices

### 1. Separate Find from Action
```java
// Good: Clear separation
ActionResult found = action.find(targetImage);
if (found.isSuccess()) {
    action.perform(ActionType.CLICK, found.getFirstMatch().getLocation());
}

// Better: Use conditional chains
ConditionalActionChain.find(findOptions)
    .ifFound(clickOptions)
    .perform(action, objectCollection);
```

### 2. Handle Both Success and Failure
```java
ConditionalActionChain.find(saveButton)
    .ifFound(click())
    .ifFound(log("Document saved"))
    .ifNotFound(log("Save button not found"))
    .ifNotFound(tryAlternativeSave())
    .perform(action, objectCollection);
```

### 3. Reuse Find Results
```java
// Find once, use multiple times
ActionResult buttons = action.find(allButtons);
for (Match button : buttons.getMatchList()) {
    // Highlight with pause after
    HighlightOptions highlight = new HighlightOptions.Builder()
        .setPauseAfterEnd(0.5)  // 500ms pause after highlighting
        .build();
    action.perform(highlight, button.getRegion());
    
    // Then click
    action.perform(ActionType.CLICK, button.getRegion());
}
```

## Migration Tips

If you're migrating from the old API:

1. **Start Small**: Migrate one action at a time
2. **Both APIs Work Together**: You can use old and new actions in the same project
3. **Look for Patterns**: Similar code often uses similar migration patterns
4. **Use the Convenience Methods**: They make migration easier

Example migration:
```java
// Old code
action.click(submitButton);

// New code - Option 1 (explicit)
ActionResult found = action.find(submitButton);
if (found.isSuccess()) {
    action.perform(ActionType.CLICK, found.getFirstMatch());
}

// New code - Option 2 (chain)
ConditionalActionChain.find(findOptions)
    .ifFound(click())
    .perform(action, new ObjectCollection.Builder()
        .withImages(submitButton)
        .build());
```

## Next Steps

1. **Try the Examples**: Start with simple click and type operations
2. **Explore Conditional Chains**: Learn about ifFound, ifNotFound, and always
3. **Read the Full Documentation**: 
   - [Pure Actions API Reference](../03-core-library/action-config/14-pure-actions-api.md)
   - [Conditional Chains Examples](../03-core-library/action-config/15-conditional-chains-examples.md)
   - [Migration Guide](../03-core-library/guides/action-refactoring-migration.md)

## Getting Help

- Check the [API documentation](../03-core-library/action-config/14-pure-actions-api.md)
- See [examples](../03-core-library/action-config/15-conditional-chains-examples.md) for common patterns
- File issues on [GitHub](https://github.com/jspinak/brobot) if you encounter problems

Remember: Pure actions make your automation code clearer and more maintainable. The separation of Find from Action might seem like extra work at first, but it pays off in better testing, debugging, and code reuse.