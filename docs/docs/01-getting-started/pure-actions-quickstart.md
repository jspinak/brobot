---
sidebar_position: 8
title: 'Pure Actions Quick Start Guide'
---

# Pure Actions Quick Start Guide

## What are Pure Actions?

Pure actions are Brobot's core action implementations (Click, Type, Move, etc.) that perform a single, specific operation without embedded Find logic. This separation enables:

- **Better testability** - Mock finding and clicking separately
- **Clearer code** - Explicit about what's happening
- **More flexibility** - Reuse find results for multiple actions
- **Action chaining** - Compose complex workflows from simple operations

## Three Ways to Use Actions

Brobot provides three ways to perform actions, from simplest to most powerful:

### 1. Convenience Methods (Easiest)

For common operations with default settings:

```java
// Click on an image (finds it first, then clicks)
StateImage button = new StateImage.Builder().addPatterns("submit-button").build();
action.click(button);

// Click at a location
action.click(new Location(100, 200));

// Type text
action.type("Hello World");

// Find an image
ActionResult result = action.find(button);
```

**Use when:** You want simple one-line operations with default behavior.

### 2. Explicit Find-Then-Act

Manually control the find and action steps:

```java
// Find first
ActionResult findResult = action.find(button);

// Then act on the results
if (findResult.isSuccess()) {
    for (Match match : findResult.getMatchList()) {
        action.click(match.getRegion());
    }
}
```

**Use when:** You need to process find results before acting, or perform multiple different actions on the same found elements.

### 3. Conditional Action Chains (Most Powerful)

Compose complex conditional workflows:

```java
ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFoundClick()
    .ifNotFoundLog("Button not found")
    .perform(action, new ObjectCollection.Builder()
        .withImages(button)
        .build());
```

**Use when:** You need conditional logic, error handling, or multi-step workflows.

## Your First Pure Action

Let's start with simple examples:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SimpleAutomation {

    private final Action action;

    public void clickButton() {
        // Simplest: Click at a known location
        action.click(new Location(100, 200));

        // Find an image
        StateImage button = new StateImage.Builder()
            .addPatterns("submit-button")  // No .png extension needed
            .build();
        action.find(button);  

        // Type some text
        action.type("Hello World");
    }
}
```

## Common Use Cases

### 1. Click at a Location

```java
// Direct click at coordinates
action.click(new Location(100, 200));

// Click with custom configuration
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .build();
action.perform(doubleClick, new ObjectCollection.Builder()
    .withLocations(new Location(100, 200))
    .build());
```

### 2. Find and Click an Image

```java
StateImage button = new StateImage.Builder()
    .addPatterns("submit-button")
    .build();

// Simple way
action.click(button);

// With conditional chain for error handling
ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFoundClick()
    .ifNotFoundLog("Submit button not found")
    .perform(action, new ObjectCollection.Builder()
        .withImages(button)
        .build());
```

### 3. Type Text

```java
// Simple typing
action.type("Hello World");

// Click field first, then type
StateImage textField = new StateImage.Builder()
    .addPatterns("username-field")
    .build();

action.click(textField);
action.type("myusername");

// Or with action chain
ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFoundClick()
    .thenType("myusername")
    .perform(action, new ObjectCollection.Builder()
        .withImages(textField)
        .build());
```

### 4. Process Multiple Matches

```java
// Find all matching elements
StateImage checkboxes = new StateImage.Builder()
    .addPatterns("checkbox")
    .build();

ActionResult findResult = action.find(checkboxes);

// Click each one
if (findResult.isSuccess()) {
    for (Match match : findResult.getMatchList()) {
        action.click(match.getRegion());
    }
}
```

### 5. Right-Click Menu

```java
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;

// Using full API for right-click
ClickOptions rightClick = new ClickOptions.Builder()
    .setMousePressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build();

StateImage fileIcon = new StateImage.Builder()
    .addPatterns("file-icon")
    .build();

// Find file, right-click it, then click delete option
ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFound(rightClick)  // Right-click the file
    .then(new PatternFindOptions.Builder().build())  // Find delete option
    .ifFoundClick()  // Click delete
    .perform(action,
        new ObjectCollection.Builder().withImages(fileIcon).build(),
        new ObjectCollection.Builder().withImages(deleteOption).build());
```

## All Convenience Methods

The Action class provides simple one-line methods for common operations:

```java
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.basic.scroll.ScrollOptions;

// Click operations
action.click(new Location(100, 200));     // Click at location
action.click(region);                      // Click region center
action.click(match);                       // Click match
action.click(stateImage);                  // Find and click

// Type operations
action.type("Hello World");                // Type text
action.type(stateString);                  // Type StateString

// Mouse movement
action.move(new Location(500, 300));       // Move to location
action.move(region);                       // Move to region center
action.move(match);                        // Move to match

// Find operations
action.find(stateImage);                   // Find image
action.find(pattern);                      // Find pattern
action.findWithTimeout(2.0, stateImage);   // Find with timeout

// Highlight
action.highlight(region);                  // Highlight region
action.highlight(match);                   // Highlight match

// Drag
action.drag(fromLocation, toLocation);     // Drag between locations
action.drag(fromRegion, toRegion);         // Drag between regions

// Scroll
action.scroll(ScrollOptions.Direction.UP, 3);    // Scroll up 3 steps
action.scroll(ScrollOptions.Direction.DOWN, 5);  // Scroll down 5 steps

// Wait for vanish
action.vanish(stateImage);                 // Wait for image to disappear
action.vanish(pattern);                    // Wait for pattern to disappear
```

## Working with Results

ActionResult gives you detailed information about what happened:

```java
// Find returns matches you can work with
ActionResult findResult = action.find(targetImage);

if (findResult.isSuccess()) {
    // Get the best match
    Match bestMatch = findResult.getBestMatch().get();
    System.out.println("Found at: " + bestMatch.getRegion());
    System.out.println("Similarity: " + bestMatch.getScore());

    // Get all matches
    List<Match> allMatches = findResult.getMatchList();
    System.out.println("Total matches: " + allMatches.size());

    // Work with each match
    for (Match match : allMatches) {
        // Highlight each found instance
        action.highlight(match.getRegion());

        // Click each one
        action.click(match.getRegion());
    }
} else {
    System.out.println("Image not found");
}
```

## Error Handling

Pure actions make error handling explicit and clear:

```java
ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFoundClick()
    .ifNotFoundLog("ERROR: Critical button not found!")
    .ifNotFoundDo(result -> {
        // Custom error handling
        takeScreenshot("error-state");
        notifyUser("Application in unexpected state");
    })
    .perform(action,  new ObjectCollection.Builder()
        .withImages(criticalButton)
        .build());
```

## Best Practices

### 1. Use Convenience Methods When Possible

```java
// Good: Simple and clear
action.click(button);
action.type("password");

// Overkill: Unnecessary complexity for simple case
ClickOptions clickOptions = new ClickOptions.Builder().build(); // Default options
ObjectCollection collection = new ObjectCollection.Builder()
    .withImages(button)
    .build();
action.perform(clickOptions, collection); // 5 lines for a simple click
```

### 2. Use Full API for Custom Configuration

```java
// Right tool for the job
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPauseAfterEnd(0.5)
    .build();
action.perform(doubleClick, new ObjectCollection.Builder()
    .withImages(button)
    .build());
```

### 3. Handle Both Success and Failure

```java
// Simple approach
ActionResult result = action.click(saveButton);
if (!result.isSuccess()) {
    System.out.println("Save button not found");
    tryAlternativeSave();
}

// Conditional chain approach
ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFoundClick()
    .ifFoundLog("Document saved") // Success handler
    .ifNotFoundLog("Save button not found") // Failure logging
    .ifNotFoundDo(result -> tryAlternativeSave()) // Failure action
    .perform(action, new ObjectCollection.Builder()
        .withImages(saveButton)
        .build());
```

### 4. Reuse Find Results

```java
// Find once, use multiple times
ActionResult findResult = action.find(allButtons); // Single find operation

if (findResult.isSuccess()) {
    for (Match match : findResult.getMatchList()) {
        // Highlight with custom pause
        HighlightOptions highlight = new HighlightOptions.Builder()
            .setPauseAfterEnd(0.5)  // Custom pause - needs full API
            .build();
        action.perform(highlight, new ObjectCollection.Builder()
            .withRegions(match.getRegion())
            .build());

        // Then click
        action.click(match.getRegion()); // Convenience method
    }
}
```

## Choosing the Right Approach

Here's a decision tree to help you choose:

**Start here: Do you need custom configuration?**

**NO** → Use convenience methods:
```java
action.click(button);
action.type("text");
action.find(image);
```

**YES** → Do you need conditional logic or chaining?

**NO** → Use `action.perform()` with ActionConfig:
```java
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .build();
action.perform(doubleClick, objectCollection);
```

**YES** → Use ConditionalActionChain:
```java
ConditionalActionChain
    .find(findOptions)
    .ifFoundClick()
    .ifNotFoundLog("Not found")
    .thenType("text")
    .perform(action, objectCollection);
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