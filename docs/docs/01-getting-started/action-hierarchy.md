---
sidebar_position: 5
title: 'The Action Hierarchy'
---

# Controlling the GUI

Brobot interacts with the GUI using the Sikuli library. This is done with
3 main levels of abstraction:

## Sikuli Wrappers

These are the methods that form the interface between Brobot and Sikuli.
Sikuli Wrappers route the operational instructions either to Sikuli methods,
which control the mouse and keyboard and capture data from the screen, or to
functions that mock (simulate) these methods. When calling Sikuli methods,
the Wrappers convert Brobot data types to Sikuli data types.  

## Basic Actions

Basic Actions are the fundamental building blocks of GUI automation in Brobot.
They perform simple, atomic operations that typically require at most one Find operation.
Examples include:

- **Find Actions** - Locate images, text, or patterns on screen
- **Click Actions** - Single, double, or right clicks at specific locations
- **Type Actions** - Keyboard input and key combinations
- **Move Actions** - Mouse movements and hover operations

Each Basic Action is implemented as a separate class that implements the `ActionInterface`,
providing a clean, type-safe API through specific configuration classes like `PatternFindOptions`,
`ClickOptions`, and `TypeOptions`.

## Complex Actions

Complex Actions (formerly called Composite Actions) combine Basic Actions and Sikuli Wrappers
to create more sophisticated operations. These are useful for:

- **Multi-step Operations** - Actions requiring multiple Find operations
- **Conditional Behaviors** - Click until something appears/disappears
- **Drag Operations** - Click, hold, move, and release sequences
- **Scrolling** - Repeated scroll actions until target is found
- **Retry Logic** - Automatic retry with different strategies

In Brobot 1.1.0, Complex Actions are built by:
1. Chaining multiple Basic Actions together
2. Using `TaskSequence` (formerly `ActionDefinition`) for scripted sequences
3. Creating custom action classes that orchestrate Basic Actions

### Example: Click Until Pattern Appears

```java
// Using individual actions with retry logic
public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
        // Click on target with pause after action
        ClickOptions click = new ClickOptions.Builder()
                .setPauseAfterAction(1.0)  // 1 second pause after click
                .build();
        performAction(click, clickTarget);
        
        // Check if pattern appeared
        PatternFindOptions find = PatternFindOptions.forQuickSearch();
        ActionResult result = performAction(find, findTarget);
        
        if (result.isSuccess()) {
            return true;
        }
    }
    return false;
}
```

This modular approach makes it easy to create custom complex behaviors while maintaining
the benefits of type safety and clear intent provided by the ActionConfig architecture.