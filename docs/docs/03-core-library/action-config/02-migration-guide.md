---
sidebar_position: 2
---

# Migration Guide: ActionOptions to ActionConfig

This guide helps you migrate your existing Brobot automation code from `ActionOptions` to the new `ActionConfig` hierarchy.

## Quick Reference

### Action Type Mapping

| ActionOptions.Action | New ActionConfig Class |
|---------------------|------------------------|
| `FIND` | `PatternFindOptions`, `BaseFindOptions` |
| `CLICK` | `ClickOptions` |
| `TYPE` | `TypeOptions` |
| `DRAG` | `DragOptions` |
| `MOVE` | `MouseMoveOptions` |
| `MOUSE_DOWN` | `MouseDownOptions` |
| `MOUSE_UP` | `MouseUpOptions` |
| `DEFINE` | `DefineRegionOptions` |
| `HIGHLIGHT` | `HighlightOptions` |
| `SCROLL_MOUSE_WHEEL` | `ScrollMouseWheelOptions` |
| `VANISH` | `VanishOptions` |
| `CLICK_UNTIL` | `ClickUntilOptions` |

### Field Mapping

| ActionOptions Field | New Location |
|-------------------|--------------|
| `minScore` | `similarity` in find options |
| `clickType` | `numberOfClicks` + `MousePressOptions` |
| `textToType` | `text` in TypeOptions |
| `dragToOffsetX/Y` | Removed (use target locations) |
| `moveMouseAfterAction` | Action-specific option |

## Migration Examples

### Find Action

#### Before (ActionOptions)
```java
ActionOptions findOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setSimilarity(0.8)
    .setSearchRegions(searchRegions)
    .build();

action.perform(findOptions, stateImage);
```

#### After (ActionConfig)
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .setSearchRegions(searchRegions)
    .build();

action.perform(findOptions, stateImage);
```

### Click Action

#### Before
```java
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .setClickType(ActionOptions.ClickType.DOUBLE_LEFT)
    .setPauseBeforeMouseDown(0.5)
    .build();
```

#### After
```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPressOptions(new MousePressOptions.Builder()
        .setButton(MouseButton.LEFT)
        .setPauseBeforeMouseDown(0.5)
        .build())
    .build();
```

### Type Action

#### Before
```java
ActionOptions typeOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.TYPE)
    .setTextToType("Hello World")
    .setModifierKeys(new String[]{"ctrl", "a"})
    .build();
```

#### After
```java
TypeOptions typeOptions = new TypeOptions.Builder()
    .setText("Hello World")
    .setModifierKeys("ctrl", "a")
    .build();
```

### Drag Action

#### Before
```java
ActionOptions dragOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.DRAG)
    .setDragToOffsetX(100)
    .setDragToOffsetY(50)
    .build();
```

#### After
```java
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
// Before: Combined in ClickType enum
.setClickType(ActionOptions.ClickType.DOUBLE_RIGHT)

// After: Separate configuration
.setNumberOfClicks(2)
.setPressOptions(new MousePressOptions.Builder()
    .setButton(MouseButton.RIGHT)
    .build())
```

### 2. Action Chaining

The new API supports fluent chaining:

```java
ActionConfig findAndClick = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build())
    .build();
```

### 3. Verification as Composition

Verification is now composed, not inherited:

```java
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
ActionResult result = action.perform(dragOptions, source, target);
Movement movement = result.getMovement().orElse(null);
```

## Step-by-Step Migration

### 1. Update Imports

Replace:
```java
import io.github.jspinak.brobot.action.ActionOptions;
```

With specific imports:
```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
// ... other specific options
```

### 2. Replace Builders

Find all `new ActionOptions.Builder()` and replace with specific builders:

```java
// Find all instances of:
new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK)

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
// Before
ActionOptions wait = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.WAIT)
    .setMaxWait(5)
    .build();

// After - use VanishOptions or pause
VanishOptions waitForVanish = new VanishOptions.Builder()
    .setTimeout(5.0)
    .build();
```

#### Custom Find Operations
Replace `tempFind` with proper find configurations:

```java
// Before
actionOptions.setTempFind(customCollection);

// After
CustomFindOptions customFind = new CustomFindOptions.Builder()
    .setCustomCriteria(...)
    .build();
```

## Common Pitfalls

1. **Don't instantiate abstract builders**: `BaseFindOptions.Builder` is abstract
2. **MousePressOptions is composed**: Don't try to extend it
3. **Check method names**: Some have changed (e.g., `setTimeoutInSeconds` → `setTimeout`)
4. **Verify imports**: Make sure you're using the right Options class

## Testing Your Migration

After migrating:

1. **Compile**: Fix any compilation errors
2. **Test**: Run your test suite
3. **Verify**: Check that actions behave as expected
4. **Performance**: Ensure no performance degradation

## Gradual Migration

If you can't migrate everything at once:

1. The `ActionOptionsAdapter` provides backward compatibility
2. Migrate one action type at a time
3. Test thoroughly after each migration
4. Remove adapter once fully migrated

## Need Help?

- Check the [API Reference](./05-reference) for detailed documentation
- Review [Code Examples](./03-examples) for usage patterns
- Consult the [Fluent API Guide](./04-fluent-api) for chaining patterns