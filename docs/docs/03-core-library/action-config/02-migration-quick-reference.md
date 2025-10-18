---
sidebar_position: 2
title: Quick Migration Reference
slug: quick-migration-reference
---

# Quick Migration Reference: ActionOptions to ActionConfig

This guide helps you migrate your existing Brobot automation code from `ActionOptions` to the new `ActionConfig` hierarchy. For a comprehensive overview of the ActionConfig system, see the [ActionConfig Overview](./01-overview.md).

## Quick Reference

### Action Type Mapping

| ActionType (replaces ActionOptions.Action) | New ActionConfig Class |
|--------------------------------------------|------------------------|
| `FIND` | `PatternFindOptions`, `BaseFindOptions` |
| `CLICK` | `ClickOptions` |
| `TYPE` | `TypeOptions` |
| `DRAG` | `DragOptions` |
| `MOVE` | `MouseMoveOptions` |
| `MOUSE_DOWN` | `MouseDownOptions` |
| `MOUSE_UP` | `MouseUpOptions` |
| `DEFINE` | `DefineRegionOptions` |
| `HIGHLIGHT` | `HighlightOptions` |
| `SCROLL_MOUSE_WHEEL` | `ScrollOptions` |
| `VANISH` | `VanishOptions` |

**Note:** `CLICK_UNTIL` uses the composite pattern with `ModernRepeatUntilConfig` rather than a dedicated Options class. See [Action Chaining](./07-action-chaining.md) for details.

### Field Mapping

| ActionOptions Field | New Location |
|-------------------|--------------|
| `minScore` | `similarity` in find options |
| `clickType` | `numberOfClicks` + `MousePressOptions` |
| `textToType` | `text` in TypeOptions |
| `dragToOffsetX/Y` | Removed (use target locations) |
| `moveMouseAfterAction` | Action-specific option |

## Setup: Required Context

Most migration examples assume the following setup:

```java
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.ObjectCollection;
import org.sikuli.script.Pattern;

public class MigrationExample {
    @Autowired
    private Action action;

    // Initialize test objects
    SearchRegions searchRegions = new SearchRegions();
    StateImage stateImage = new StateImage.Builder()
        .withPattern(new Pattern("button.png"))
        .build();
    ObjectCollection verifyCollection = new ObjectCollection.Builder()
        .withStateImages(stateImage)
        .build();
}
```

## Migration Examples

### Find Action

#### Before (ActionOptions)
```java
import io.github.jspinak.brobot.action.ActionOptions; // DEPRECATED
import io.github.jspinak.brobot.action.ActionType;

// DEPRECATED - ActionOptions class is deprecated
ActionOptions findOptions = new ActionOptions.Builder()
    .setAction(ActionType.FIND)
    .setSimilarity(0.8)
    .setSearchRegions(searchRegions)
    .build();

action.perform(findOptions, stateImage);
```

#### After (ActionConfig)
```java
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .setSearchRegions(searchRegions)
    .build();

action.perform(findOptions, stateImage);
```

### Click Action

#### Before
```java
import io.github.jspinak.brobot.action.ActionOptions; // DEPRECATED
import io.github.jspinak.brobot.action.ActionType;

// DEPRECATED - ActionOptions class is deprecated
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .setClickType(ActionOptions.ClickType.DOUBLE_LEFT)
    .setPauseBeforeMouseDown(0.5)
    .build();
```

#### After
```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.manageStates.mouse.MouseButton;

ClickOptions clickOptions = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.LEFT)
        .setPauseBeforeMouseDown(0.5)
        .build())
    .build();
```

### Type Action

#### Before
```java
import io.github.jspinak.brobot.action.ActionOptions; // DEPRECATED
import io.github.jspinak.brobot.action.ActionType;

// DEPRECATED - ActionOptions class is deprecated
ActionOptions typeOptions = new ActionOptions.Builder()
    .setAction(ActionType.TYPE)
    .setTextToType("Hello World")
    .setModifierKeys(new String[]{"ctrl", "a"})
    .build();
```

#### After
```java
import io.github.jspinak.brobot.action.basic.type.TypeOptions;

TypeOptions typeOptions = new TypeOptions.Builder()
    .setText("Hello World")
    .setModifierKeys("ctrl", "a")
    .build();
```

### Drag Action

#### Before
```java
import io.github.jspinak.brobot.action.ActionOptions; // DEPRECATED
import io.github.jspinak.brobot.action.ActionType;

// DEPRECATED - ActionOptions class is deprecated
ActionOptions dragOptions = new ActionOptions.Builder()
    .setAction(ActionType.DRAG)
    .setDragToOffsetX(100)
    .setDragToOffsetY(50)
    .build();
```

#### After
```java
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

DragOptions dragOptions = new DragOptions.Builder()
    .setFromOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setToOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .build();
```

## Key Changes

### 1. Mouse Button Separation

Mouse button and click count are now separate:

```java
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.manageStates.mouse.MouseButton;

// Before: Combined in ClickType enum
.setClickType(ActionOptions.ClickType.DOUBLE_RIGHT)

// After: Separate configuration
.setNumberOfClicks(2)
.setPressOptions(MousePressOptions.builder()
    .setButton(MouseButton.RIGHT)
    .build())
```

### 2. Action Chaining

The new API supports fluent chaining:

```java
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

ActionConfig findAndClick = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build())
    .build();
```

See [Action Chaining](./07-action-chaining.md) for more advanced chaining patterns.

### 3. Verification as Composition

Verification is now composed, not inherited:

```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.VerificationOptions;

ClickOptions clickWithVerification = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setVerificationOptions(new VerificationOptions.Builder()
        .setEvent(VerificationOptions.Event.OBJECTS_VANISH)
        .setObjectCollection(verifyCollection)
        .build())
    .build();
```

### 4. Drag Returns Movement

Drag operations now return `Movement` objects instead of regions:

```java
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.datatypes.primitives.movement.Movement;

ActionResult result = action.perform(dragOptions, source, target);
Movement movement = result.getMovement().orElse(null);
```

See [ActionResult Components](./17-actionresult-components.md) for details on processing action results.

## Step-by-Step Migration

### 1. Update Imports

Replace:
```java
import io.github.jspinak.brobot.action.ActionOptions; // DEPRECATED
```

With specific imports:
```java
import io.github.jspinak.brobot.action.ActionType; // For ActionType enum
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
// ... other specific options
```

### 2. Replace Builders

Find all `new ActionOptions.Builder()` and replace with specific builders:

```java
// Find all instances of:
new ActionOptions.Builder().setAction(ActionType.CLICK)

// Replace with:
new ClickOptions.Builder()
```

### 3. Update Field Names

- `minScore` → `similarity`
- `textToType` → `text`
- Remove `dragToOffsetX/Y` (use proper drag configuration)

### 4. Handle Special Cases

#### Wait Operations
Wait is no longer an action. Use conditions or pauses:

```java
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;

// Before
// DEPRECATED - ActionOptions class is deprecated
ActionOptions wait = new ActionOptions.Builder()
    .setAction(ActionType.WAIT)
    .setMaxWait(5)
    .build();

// After - use VanishOptions or pause
VanishOptions waitForVanish = new VanishOptions.Builder()
    .setTimeout(5.0)
    .build();
```

#### Convenience Methods Alternative
As of Brobot 1.1.0, you can also use convenience methods for simple operations:

```java
// Even simpler approach for common actions
action.click(region);
action.type("text");
action.find(pattern);
```

See [Convenience Methods](./18-convenience-methods.md) for details on this approach.

## Common Pitfalls

1. **Don't instantiate abstract builders**: `BaseFindOptions.Builder` is abstract
2. **MousePressOptions is composed**: Don't try to extend it
3. **Check method names**: Some have changed (e.g., `setTimeoutInSeconds` → `setTimeout`)
4. **Verify imports**: Make sure you're using the right Options class
5. **Builder syntax**: Use `MousePressOptions.builder()` (lowercase) not `new MousePressOptions.Builder()`

For troubleshooting action chains and common errors, see [Troubleshooting Action Chains](./troubleshooting-chains.md).

## Testing Your Migration

After migrating:

1. **Compile**: Fix any compilation errors
2. **Test**: Run your test suite
3. **Verify**: Check that actions behave as expected
4. **Performance**: Ensure no performance degradation

## Gradual Migration

You can migrate incrementally:

1. Migrate one action type at a time
2. Test thoroughly after each migration
3. Use convenience methods for simple operations
4. Keep complex operations in ActionConfig for fine control

For a comprehensive migration strategy and more examples, see the [Complete Migration Guide](./12-migration-guide.md).

## Complete Working Example

Here's a full, compilable before/after example:

```java
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LoginAutomation {
    @Autowired
    private Action action;

    public void login() {
        // Define the login button
        StateImage loginButton = new StateImage.Builder()
            .withPattern(new Pattern("login-button.png"))
            .build();

        // Find and click with high similarity
        ActionResult result = action.perform(
            new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .then(new ClickOptions.Builder()
                    .setNumberOfClicks(1)
                    .build())
                .build(),
            loginButton
        );

        if (result.isSuccess()) {
            System.out.println("Login button clicked successfully!");
        }
    }
}
```

## Need Help?

- Check the [API Reference](./05-reference.md) for detailed documentation
- Review [Code Examples](./03-examples.md) for usage patterns
- Consult the [Action Chaining](./07-action-chaining.md) guide for chaining patterns
- See [Migration Examples](./06-migration-examples.md) for more real-world scenarios
- Review [ActionConfig Overview](./01-overview.md) for conceptual understanding
