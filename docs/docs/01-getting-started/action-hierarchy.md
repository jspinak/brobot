---
sidebar_position: 5
title: 'The Action Hierarchy'
---

# Controlling the GUI

Brobot interacts with the GUI using the Sikuli library. This is done with 3 main levels of abstraction:

## Sikuli Wrappers

These are the methods that form the interface between Brobot and Sikuli.
Sikuli Wrappers route the operational instructions either to Sikuli methods, which control the mouse and keyboard and capture data from the screen, or to functions that mock (simulate) these methods. When calling Sikuli methods, the Wrappers convert Brobot data types to Sikuli data types.  

## Basic Actions

Basic Actions are the fundamental building blocks of GUI automation in Brobot. They perform simple, atomic operations that typically require at most one Find operation.
Examples include:

- **Find Actions** - Locate images, text, or patterns on screen
- **Click Actions** - Single, double, or right clicks at specific locations
- **Type Actions** - Keyboard input and key combinations
- **Move Actions** - Mouse movements and hover operations

Each Basic Action is implemented as a separate class that implements the `ActionInterface`,
providing a clean, type-safe API through specific configuration classes like `PatternFindOptions`,
`ClickOptions`, and `TypeOptions`.

## Complex Actions

Complex Actions (formerly called Composite Actions) combine Basic Actions to create more sophisticated operations. These are useful for:

- **Multi-step Operations** - Actions requiring multiple Find operations
- **Conditional Behaviors** - Click until something appears/disappears
- **Drag Operations** - Click, hold, move, and release sequences
- **Scrolling** - Repeated scroll actions until target is found
- **Retry Logic** - Automatic retry with different strategies

In Brobot 1.1.0, Complex Actions are built by:
1. Chaining multiple Basic Actions together
2. Using `TaskSequence` for scripted sequences
3. Creating custom action classes that orchestrate Basic Actions

### Example: Click Until Pattern Appears

There are multiple ways to implement clickUntilFound in Brobot. Here are the most clean and efficient approaches:

#### Method 1: Traditional Loop Approach
```java
// Using individual actions with retry logic
public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
        // Click on target with pause after action
        ClickOptions click = new ClickOptions.Builder()
                .setPauseAfterEnd(1.0)  // 1 second pause after click
                .build();
        action.perform(click, new ObjectCollection.Builder()
                .withImages(clickTarget).build());
        
        // Check if pattern appeared
        PatternFindOptions find = PatternFindOptions.forQuickSearch();
        ActionResult result = action.perform(find, new ObjectCollection.Builder()
                .withImages(findTarget).build());
        
        if (result.isSuccess()) {
            return true;
        }
    }
    return false;
}
```

#### Method 2: Fluent API with Action Chaining
```java
// Using the fluent API to chain click and find operations
public boolean clickUntilFoundFluent(StateImage clickTarget, StateImage findTarget) {
    // Create a chain that clicks and then looks for the target
    ClickOptions clickAndCheck = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking on " + clickTarget.getName() + "...")
            .withSuccessLog("Click executed")
            .setPauseAfterEnd(1.0)  // Wait after click
            .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Checking if " + findTarget.getName() + " appeared...")
                    .withSuccessLog(findTarget.getName() + " found!")
                    .withFailureLog(findTarget.getName() + " not yet visible")
                    .build())
            .setRepetition(new RepetitionOptions.Builder()
                    .setMaxTimesToRepeatActionSequence(10)  // Try up to 10 times
                    .setPauseBetweenActionSequences(0.5)    // Brief pause between attempts
                    .build())
            .build();
    
    // Execute the chained action with both images
    ObjectCollection targets = new ObjectCollection.Builder()
            .withImages(clickTarget, findTarget)
            .build();
    
    ActionResult result = action.perform(clickAndCheck, targets);
    return result.isSuccess();
}
```

#### Method 3: Using the Built-in ClickUntilOptions (Deprecated but Available)
```java
// Using Brobot's built-in ClickUntil composite action
public boolean clickUntilFoundBuiltIn(StateImage clickTarget, StateImage findTarget) {
    // Create ClickUntilOptions configured to click until objects appear
    ClickUntilOptions clickUntil = new ClickUntilOptions.Builder()
            .setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR)
            .withBeforeActionLog("Clicking until " + findTarget.getName() + " appears...")
            .withSuccessLog(findTarget.getName() + " appeared!")
            .withFailureLog("Timeout - " + findTarget.getName() + " did not appear")
            .setRepetition(new RepetitionOptions.Builder()
                    .setMaxTimesToRepeatActionSequence(10)
                    .setPauseBetweenActionSequences(1.0)
                    .build())
            .build();
    
    // Create ObjectCollections
    // If using 1 collection: clicks objects until they appear
    // If using 2 collections: clicks collection 1 until collection 2 appears
    ObjectCollection clickCollection = new ObjectCollection.Builder()
            .withImages(clickTarget)
            .build();
    ObjectCollection appearCollection = new ObjectCollection.Builder()
            .withImages(findTarget)
            .build();
    
    // Execute with two collections - click first until second appears
    ActionResult result = action.perform(clickUntil, clickCollection, appearCollection);
    return result.isSuccess();
}
```

#### Method 4: Creating a Reusable Click-Until-Found Function
```java
// Creating a clean, reusable function that combines the best approaches
public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, 
                               int maxAttempts, double pauseBetween) {
    // Use fluent chaining with automatic logging
    PatternFindOptions clickAndCheck = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for click target...")
            .withSuccessLog("Click target found")
            .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Clicking...")
                    .withSuccessLog("Clicked successfully")
                    .setPauseAfterEnd(pauseBetween)
                    .build())
            .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Checking if target appeared...")
                    .withSuccessLog("Target appeared!")
                    .withFailureLog("Target not yet visible")
                    .setSearchDuration(0.5) // Quick check
                    .build())
            .setRepetition(new RepetitionOptions.Builder()
                    .setMaxTimesToRepeatActionSequence(maxAttempts)
                    .setPauseBetweenActionSequences(0.5)
                    .build())
            .build();
    
    // Combine both images in one collection
    ObjectCollection targets = new ObjectCollection.Builder()
            .withImages(clickTarget, findTarget)
            .build();
    
    // Execute and check the final result
    ActionResult result = action.perform(clickAndCheck, targets);
    
    // The chain succeeds if the final find action succeeded
    return result.isSuccess() && result.getLastActionResult().isSuccess();
}

// Usage example:
boolean success = clickUntilFound(nextButton, finishButton, 10, 1.0);
```

### Choosing the Right Approach

| Method | When to Use | Pros | Cons |
|--------|------------|------|------|
| **Traditional Loop** | Simple cases, full control needed | Complete control over logic | More verbose, manual logging |
| **Fluent API Chaining** | Most common cases | Clean, automatic logging, easy to read | Requires understanding of chaining |
| **ClickUntilOptions** | Legacy code or specific composite needs | Built-in, handles edge cases | Deprecated, less flexible |
| **Reusable Function** | Multiple uses in codebase | DRY principle, consistent behavior | May need customization for edge cases |

### Best Practices for Click Until Found

1. **Use Logging**: The fluent API approach (Method 2) provides the best logging and debugging experience
2. **Set Reasonable Timeouts**: Always limit the number of attempts to prevent infinite loops
3. **Add Pauses**: Include pauses between clicks to allow the UI to respond
4. **Consider State Management**: In a state-based automation framework, clickUntilFound might trigger a state transition
5. **ObjectCollection Usage**: Remember that all actions in a chain share the same ObjectCollection

This modular approach makes it easy to create custom complex behaviors while maintaining the benefits of type safety and clear intent provided by the ActionConfig architecture.