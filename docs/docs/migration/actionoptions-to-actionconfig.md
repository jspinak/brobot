# Migration Guide: ActionOptions to ActionConfig

## Overview

Starting with Brobot 1.1.0, the monolithic `ActionOptions` class has been replaced with specialized `ActionConfig` implementations. This change provides better type safety, clearer APIs, and improved maintainability.

## Why the Change?

The original `ActionOptions` class contained configuration for all action types in a single class with over 100 fields. This led to:
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
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.95)
    .setMaxWait(5.0)
    .build();
    
action.perform(findOptions, pattern);
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
DragOptions dragOptions = new DragOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .pauseAfterMouseDown(0.3)
        .pauseBeforeMouseUp(0.3)
        .build())
    .setDragToOffset(100, 50)
    .build();
    
action.perform(dragOptions, source, target);
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
// Use ConditionalActionChain or repeated actions
ConditionalActionChain.find(button)
    .ifFoundClick()
    .repeatUntil(() -> targetImage.exists())
    .maxAttempts(10)
    .perform(action, objectCollection);
```

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

// After
.setPressOptions(MousePressOptions.builder()
    .pauseBeforeMouseDown(0.5)
    .pauseAfterMouseDown(0.5)
    .pauseBeforeMouseUp(0.5)
    .pauseAfterMouseUp(0.5)
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

// After
.setMatchAdjustment(MatchAdjustmentOptions.builder()
    .targetPosition(position)
    .targetOffset(location)
    .addW(10)
    .addH(10)
    .build())
```

### 3. Match Fusion
Match fusion options are now in `MatchFusionOptions`:

```java
// Before
.setFusionMethod(ActionOptions.MatchFusionMethod.ABSOLUTE)
.setMaxFusionDistanceX(5)
.setMaxFusionDistanceY(5)

// After
.setMatchFusion(MatchFusionOptions.builder()
    .fusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
    .maxFusionDistanceX(5)
    .maxFusionDistanceY(5)
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
1. Check the JavaDoc for the new ActionConfig classes
2. Review the examples in the `examples` package
3. Consult the test files for usage patterns
4. Open an issue on GitHub for complex migration scenarios