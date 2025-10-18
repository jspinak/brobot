# Migration Guide: ActionOptions to ActionConfig

> **Quick Links**:
> [Quick Reference](../action-config/02-migration-quick-reference.md) |
> [ActionConfig Overview](../action-config/01-overview.md) |
> [API Reference](../action-config/05-reference.md) |
> [Examples](../action-config/03-examples.md)

> **Related Migration**: If you're also migrating ActionHistory code, see [ActionHistory Migration Guide](./actionhistory-migration-guide.md)

## Overview

Starting with Brobot 1.1.0, the monolithic `ActionOptions` class has been replaced with specialized `ActionConfig` implementations. This change provides better type safety, clearer APIs, and improved maintainability.

## Why the Change?

The original `ActionOptions` class contained 68 configuration fields plus numerous enum definitions (144 total members). This led to:
- Confusion about which options applied to which actions
- Runtime errors from incompatible option combinations
- Difficult-to-maintain code with unclear dependencies

The new `ActionConfig` hierarchy provides:
- **Type-specific configurations**: Each action has its own config class
- **Compile-time safety**: Invalid option combinations are caught at compile time
- **Clearer APIs**: Only relevant options are available for each action
- **Better documentation**: Each config class documents its specific options

## Migration Map

### Action Type Mapping

| Old ActionOptions.Action | New ActionConfig Class | ActionType Enum |
|-------------------------|------------------------|-----------------|
| `FIND` | `PatternFindOptions` | `ActionType.FIND` |
| `CLICK` | `ClickOptions` | `ActionType.CLICK` |
| `TYPE` | `TypeOptions` | `ActionType.TYPE` |
| `DEFINE` | `DefineRegionOptions` | `ActionType.DEFINE` |
| `HIGHLIGHT` | `HighlightOptions` | `ActionType.HIGHLIGHT` |
| `MOVE` | `MouseMoveOptions` | `ActionType.MOVE` |
| `VANISH` | `VanishOptions` | `ActionType.VANISH` |
| `MOUSE_DOWN` | `MouseDownOptions` | `ActionType.MOUSE_DOWN` |
| `MOUSE_UP` | `MouseUpOptions` | `ActionType.MOUSE_UP` |
| `KEY_DOWN` | `KeyDownOptions` | `ActionType.KEY_DOWN` |
| `KEY_UP` | `KeyUpOptions` | `ActionType.KEY_UP` |
| `SCROLL_MOUSE_WHEEL` | `ScrollOptions` | `ActionType.SCROLL_MOUSE_WHEEL` |
| `DRAG` | `DragOptions` | `ActionType.DRAG` |
| `CLICK_UNTIL` | Use action chaining with `ClickOptions` | - |

### Find Strategy Mapping

| Old ActionOptions.Find | New PatternFindOptions.Strategy |
|-----------------------|----------------------------------|
| `FIRST` | `PatternFindOptions.Strategy.FIRST` |
| `ALL` | `PatternFindOptions.Strategy.ALL` |
| `EACH` | `PatternFindOptions.Strategy.EACH` |
| `BEST` | `PatternFindOptions.Strategy.BEST` |
| `COLOR` | Use `ColorFindOptions` |
| `HISTOGRAM` | Use `HistogramFindOptions` |
| `ALL_WORDS` | Use `TextFindOptions` |

## Prerequisites and Setup

All examples in this guide assume:
1. You are working in a Spring Boot application with Brobot configured
2. The `Action` class is injected via `@Autowired`
3. You have the necessary imports for the classes you're using

### Required Imports

```java
// Core Action classes
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;

// ActionConfig implementations
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;

// Supporting classes
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.action.basic.find.MatchFusionOptions;

// Model classes
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;

// Spring annotations
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
```

### Component Setup

```java
@Component
public class MyAutomation {

    @Autowired
    private Action action;

    // Your automation methods here
}
```

## Code Migration Examples

### Example 1: Simple Click Action

**Before (ActionOptions):**
```java
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .setPauseAfterEnd(0.5)
    .build();
    
action.perform(clickOptions, stateImage);
```

**After (ActionConfig):**
```java
// Create the image to click on
StateImage stateImage = new StateImage.Builder()
    .addPatterns("button-image")
    .build();

ClickOptions clickOptions = new ClickOptions.Builder()
    .setPauseAfterEnd(0.5)
    .build();

action.perform(clickOptions, stateImage);
```

### Example 2: Find with Similarity

**Before (ActionOptions):**
```java
ActionOptions findOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setFind(ActionOptions.Find.BEST)
    .setMinSimilarity(0.95)
    .setMaxWait(5.0)
    .build();
    
action.perform(findOptions, pattern);
```

**After (ActionConfig):**
```java
// Create the image to find
StateImage stateImage = new StateImage.Builder()
    .addPatterns("element-to-find")
    .build();

PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.95)
    .setSearchDuration(5.0)  // Note: setMaxWait was renamed to setSearchDuration
    .build();

action.perform(findOptions, stateImage);
```

### Example 3: Type Action with Modifiers

**Before (ActionOptions):**
```java
ActionOptions typeOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.TYPE)
    .setTypeDelay(0.1)
    .setModifiers("CTRL")
    .build();
    
action.perform(typeOptions, "a"); // CTRL+A
```

**After (ActionConfig):**
```java
TypeOptions typeOptions = new TypeOptions.Builder()
    .setTypeDelay(0.1)
    .setModifiers("CTRL")
    .build();
    
action.perform(typeOptions, "a"); // CTRL+A
```

### Example 4: Drag Operation

**Before (ActionOptions):**
```java
ActionOptions dragOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.DRAG)
    .setPauseAfterMouseDown(0.3)
    .setPauseBeforeMouseUp(0.3)
    .setDragToOffsetX(100)
    .setDragToOffsetY(50)
    .build();
    
action.perform(dragOptions, source, target);
```

**After (ActionConfig):**
```java
// Define source and target locations
Location source = new Location(100, 100);
Location target = new Location(200, 150);

DragOptions dragOptions = new DragOptions.Builder()
    .setMousePressOptions(MousePressOptions.builder()  // Note: setMousePressOptions, not setPressOptions
        .setPauseAfterMouseDown(0.3)                    // Note: methods use 'set' prefix
        .setPauseBeforeMouseUp(0.3)
        .build())
    .setDelayBetweenMouseDownAndMove(0.1)              // Configure drag timing
    .build();

// Use the convenience method for dragging between locations
action.drag(source, target);
```

### Example 5: Click Until Pattern (Deprecated)

**Before (ActionOptions):**
```java
ActionOptions clickUntil = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK_UNTIL)
    .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
    .setMaxTimesToRepeatActionSequence(10)
    .build();
```

**After (Action Chaining):**
```java
// Define your images
StateImage button = new StateImage.Builder()
    .addPatterns("click-button")
    .build();

StateImage targetImage = new StateImage.Builder()
    .addPatterns("target-element")
    .build();

ObjectCollection buttonCollection = new ObjectCollection.Builder()
    .withImages(button)
    .build();

// Option 1: Simple conditional chain
ConditionalActionChain.find(button)
    .ifFoundClick()
    .ifNotFoundLog("Button not found")
    .perform(action, buttonCollection);

// Option 2: Use retry() for repeated attempts (equivalent to CLICK_UNTIL)
ConditionalActionChain.retry(
    new ClickOptions.Builder().build(),
    10  // max attempts
).perform(action, buttonCollection);
```

> **Note**: For comprehensive ConditionalActionChain documentation, see [Conditional Action Chains Examples](../action-config/15-conditional-chains-examples.md)

### Example 6: Using ActionType Enum

**Before:**
```java
import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Action.CLICK;

action.perform(CLICK, stateImage);
```

**After:**
```java
import io.github.jspinak.brobot.action.ActionType;

action.perform(ActionType.CLICK, stateImage);
```

## Common Migration Patterns

### 1. Mouse Options
Mouse-related options are now in `MousePressOptions`:

```java
// Before
.setPauseBeforeMouseDown(0.5)
.setPauseAfterMouseDown(0.5)
.setPauseBeforeMouseUp(0.5)
.setPauseAfterMouseUp(0.5)

// After (note: methods use 'set' prefix)
.setMousePressOptions(MousePressOptions.builder()
    .setPauseBeforeMouseDown(0.5)
    .setPauseAfterMouseDown(0.5)
    .setPauseBeforeMouseUp(0.5)
    .setPauseAfterMouseUp(0.5)
    .build())
```

### 2. Match Adjustments
Match adjustment options are now in `MatchAdjustmentOptions`:

```java
// Before
.setTargetPosition(position)
.setTargetOffset(location)
.setAddW(10)
.setAddH(10)

// After (note: methods use 'set' prefix)
.setMatchAdjustment(MatchAdjustmentOptions.builder()
    .setTargetPosition(position)
    .setTargetOffset(location)
    .setAddW(10)
    .setAddH(10)
    .build())
```

### 3. Match Fusion
Match fusion options are now in `MatchFusionOptions`:

```java
// Before
.setFusionMethod(ActionOptions.MatchFusionMethod.ABSOLUTE)
.setMaxFusionDistanceX(5)
.setMaxFusionDistanceY(5)

// After (note: methods use 'set' prefix)
.setMatchFusion(MatchFusionOptions.builder()
    .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
    .setMaxFusionDistanceX(5)
    .setMaxFusionDistanceY(5)
    .build())
```

### 4. Common Options
Options common to all actions remain in the base `ActionConfig`:

```java
// These work the same in all ActionConfig implementations:
.setPauseBeforeBegin(1.0)
.setPauseAfterEnd(1.0)
.setSuccessCriteria(result -> result.isSuccess())
.setIllustrate(ActionConfig.Illustrate.YES)
```

## Testing with Mock Classes

> **Testing Documentation**: For comprehensive testing guidance, see:
> - [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - MockTime, ActionDurations, BrobotTestBase
> - [Mock Mode Migration](../../04-testing/mock-mode-migration.md) - Migrating existing tests
> - [Unit Testing Guide](../../04-testing/unit-testing.md) - Unit test patterns

### MockTime Updates

```java
// Before
mockTime.wait(ActionOptions.Action.CLICK);
mockTime.wait(ActionOptions.Find.BEST);

// After
mockTime.wait(ActionType.CLICK);
mockTime.wait(PatternFindOptions.Strategy.BEST);
```

### ActionDurations Updates

```java
// Before
double duration = actionDurations.getActionDuration(ActionOptions.Action.CLICK);
double findDuration = actionDurations.getFindDuration(ActionOptions.Find.ALL);

// After
double duration = actionDurations.getActionDuration(ActionType.CLICK);
double findDuration = actionDurations.getFindStrategyDuration(PatternFindOptions.Strategy.ALL);
```

## Removed Classes

The following classes have been removed:
- `ActionOptions` - Replaced by specific ActionConfig implementations
- `ActionOptionsAdapter` - No longer needed
- `ActionConfigAdapter` - No longer needed
- `ActionOptionsForDrag` - Functionality moved to `DragOptions`
- `MultipleActionsObject` - Use action chaining instead

## Tips for Migration

1. **Start with simple actions**: Migrate basic finds and clicks first
2. **Use IDE refactoring**: Most IDEs can help with import updates
3. **Review option usage**: Some options may not be needed in the new API
4. **Test incrementally**: Migrate and test one action type at a time
5. **Check for deprecated warnings**: The compiler will flag deprecated usage

## Getting Help

If you encounter issues during migration:
1. Check the [ActionConfig API Reference](../action-config/05-reference.md) for detailed API documentation
2. Review the [ActionConfig Examples](../action-config/03-examples.md) for practical usage patterns
3. Consult the [Unit Testing Guide](../../04-testing/unit-testing.md) and [Integration Testing Guide](../../04-testing/integration-testing.md) for test patterns
4. See the [ActionConfig Overview](../action-config/01-overview.md) for architectural understanding
5. Open an issue on [GitHub](https://github.com/jspinak/brobot/issues) for complex migration scenarios

## See Also

### Core Documentation
- [ActionConfig Overview](../action-config/01-overview.md) - Architecture and concepts
- [ActionConfig API Reference](../action-config/05-reference.md) - Complete API documentation
- [ActionConfig Examples](../action-config/03-examples.md) - Practical code examples
- [Quick Migration Reference](../action-config/02-migration-quick-reference.md) - Condensed reference

### Related Migration Guides
- [ActionHistory Migration Guide](./actionhistory-migration-guide.md) - Migrating ActionHistory code
- [Mock Mode Migration](../../04-testing/mock-mode-migration.md) - Test migration patterns

### Advanced Topics
- [Action Chaining](../action-config/07-action-chaining.md) - Complex workflows
- [Conditional Action Chains](../action-config/15-conditional-chains-examples.md) - Modern CLICK_UNTIL patterns
- [Complex Workflows](../action-config/08-complex-workflows.md) - Advanced patterns

### Testing Resources
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Testing with mocks
- [Unit Testing Guide](../../04-testing/unit-testing.md) - Unit test patterns
- [Integration Testing Guide](../../04-testing/integration-testing.md) - Integration tests
- [ActionHistory Integration Testing](../../04-testing/actionhistory-integration-testing.md) - ActionHistory testing