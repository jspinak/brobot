---
sidebar_position: 6
---

# Migration Examples

Real-world examples of migrating from ActionOptions to ActionConfig.

> **Quick Reference**: For a concise mapping table, see the [Quick Migration Reference](./02-migration-quick-reference.md).
>
> **Comprehensive Guide**: For a complete migration strategy, see the [Migration Guide](./12-migration-guide.md).

## Common Imports

Most migration examples require these imports:

```java
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.*;
import io.github.jspinak.brobot.action.VerificationOptions;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.ObjectCollection;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.mouse.MouseButton;
import org.sikuli.script.Pattern;
```

## Common Migration Patterns

### Basic Click Migration

**Before (ActionOptions):**
```java
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .setClickType(ClickType.Type.LEFT)
    .setPauseBeforeMouseDown(0.1)
    .setPauseAfterMouseUp(0.2)
    .build();
```

**After (ClickOptions):**
```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setPauseBeforeMouseDown(0.1)
        .setPauseAfterMouseUp(0.2)
        .build())
    .build();
```

**Note**: `MousePressOptions` uses Lombok's `builder()` method (lowercase), not `new Builder()`.

### Double-Click Migration

**Before:**
```java
ActionOptions doubleClick = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .setClickType(ClickType.Type.DOUBLE_LEFT)
    .build();
```

**After:**
```java
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .build();
```

### Find Migration

**Before:**
```java
ActionOptions findOptions = new ActionOptions.Builder()
    .setAction(ActionType.FIND)
    .setFind(ActionOptions.Find.BEST)
    .setSimilarity(0.9)
    .setSearchRegions(regions)
    .build();
```

**After:**
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.9)
    .setSearchRegions(regions)
    .build();
```

### Type Text Migration

**Before:**
```java
ActionOptions typeOptions = new ActionOptions.Builder()
    .setAction(ActionType.TYPE)
    .setModifiers("ctrl+shift")
    .setTypeDelay(0.05)
    .build();
// Text provided through ObjectCollection
```

**After:**
```java
TypeOptions typeOptions = new TypeOptions.Builder()
    .setModifiers("ctrl+shift")
    .setTypeDelay(0.05)
    .build();
// Text still provided through ObjectCollection
```

## Deprecated Features Migration

### Move Mouse After Action

**Before:**
```java
ActionOptions clickAndMove = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .setMoveMouseAfterAction(true)
    .setMoveMouseAfterActionTo(new Location(100, 100))
    .build();
```

**After (using action chaining):**
```java
ClickOptions clickAndMove = new ClickOptions.Builder()
    .then(new MouseMoveOptions.Builder()
        .build()) // Target location provided via ObjectCollection
    .build();
```

### Drag Offset Migration

**Before:**
```java
ActionOptions dragOptions = new ActionOptions.Builder()
    .setAction(ActionType.DRAG)
    .setDragToOffsetX(50)
    .setDragToOffsetY(100)
    .build();
```

**After:**
```java
// DragOptions only configures timing and mouse button.
// Source and target locations must be found separately and provided via ObjectCollection.

// Step 1: Find source element
ActionResult sourceResult = action.find(sourceImage);
Location sourceLoc = sourceResult.getBestLocation();

// Step 2: Find target element
ActionResult targetResult = action.find(targetImage);
Location targetLoc = targetResult.getBestLocation();

// Step 3: Apply offset to target
Location offsetTarget = new Location(
    targetLoc.getX() + 50,  // X offset
    targetLoc.getY() + 100  // Y offset
);

// Step 4: Create ObjectCollections
ObjectCollection source = ObjectCollection.withLocations(sourceLoc);
ObjectCollection target = ObjectCollection.withLocations(offsetTarget);

// Step 5: Configure and perform drag
DragOptions dragOptions = new DragOptions.Builder()
    .setMousePressOptions(MousePressOptions.builder()
        .setButton(MouseButton.LEFT)
        .build())
    .setDelayBetweenMouseDownAndMove(0.5)
    .build();

action.perform(dragOptions, source, target);
```

**Alternative: Using MatchAdjustmentOptions with Find**
```java
// Find target with adjustment applied
PatternFindOptions targetFind = new PatternFindOptions.Builder()
    .setMatchAdjustment(MatchAdjustmentOptions.builder()
        .setAddX(50)   // Shift X by 50 pixels
        .setAddY(100)  // Shift Y by 100 pixels
        .build())
    .build();

ActionResult targetResult = action.perform(targetFind, targetImage);
Location adjustedTarget = targetResult.getBestLocation();
```

### Multiple Find Actions

**Before:**
```java
ActionOptions multipleFindOptions = new ActionOptions.Builder()
    .setAction(ActionType.FIND)
    .addFind(ActionOptions.Find.PATTERN)
    .addFind(ActionOptions.Find.COLOR)
    .build();
```

**After (using action chaining):**
```java
PatternFindOptions chainedFind = new PatternFindOptions.Builder()
    .then(new ColorFindOptions.Builder()
        .build())
    .build();
```

## Test Migration Example

**Before:**
```java
@Test
void testClick() {
    ActionOptions options = new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .setTimesToRepeatIndividualAction(3)
        .setPauseBetweenIndividualActions(0.5)
        .build();
    
    action.perform(options, objectCollection);
    
    verify(mouse, times(3)).click(any());
}
```

**After:**
```java
@Test
void testClick() {
    ClickOptions options = new ClickOptions.Builder()
        .setRepetition(RepetitionOptions.builder()
            .setTimesToRepeatIndividualAction(3)
            .setPauseBetweenIndividualActions(0.5)
            .build())
        .build();

    action.perform(options, objectCollection);

    verify(mouse, times(3)).click(any());
}
```

**Note**: `RepetitionOptions` uses Lombok's `builder()` method (lowercase), not `new Builder()`.

## Additional Migration Examples

### Right-Click Migration

**Before:**
```java
ActionOptions rightClick = new ActionOptions.Builder()
    .setAction(ActionType.RIGHT_CLICK)
    .build();
```

**After:**
```java
ClickOptions rightClick = new ClickOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build();
```

### Vanish/Wait Migration

**Before:**
```java
ActionOptions waitOptions = new ActionOptions.Builder()
    .setAction(ActionType.VANISH)
    .setMaxWait(5.0)
    .build();
```

**After:**
```java
VanishOptions waitOptions = new VanishOptions.Builder()
    .setTimeout(5.0)
    .build();
```

### Scroll Migration

**Before:**
```java
ActionOptions scrollOptions = new ActionOptions.Builder()
    .setAction(ActionType.SCROLL_DOWN)
    .setScrollSteps(5)
    .build();
```

**After:**
```java
ScrollOptions scrollOptions = new ScrollOptions.Builder()
    .setDirection(ScrollDirection.DOWN)
    .setClicks(5)
    .build();
```

### Verification Migration

**Before (using success criteria):**
```java
ActionOptions clickWithVerify = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .addSuccessCriteria(matches -> matches.size() > 0)
    .build();
```

**After (using VerificationOptions):**
```java
ClickOptions clickWithVerify = new ClickOptions.Builder()
    .setVerification(VerificationOptions.builder()
        .setEvent(VerificationOptions.Event.OBJECTS_APPEAR)
        .setObjectCollection(expectedElements)
        .build())
    .build();
```

## Gradual Migration Strategy

1. **Phase 1**: Identify all ActionOptions usage
   - Search codebase for `new ActionOptions.Builder()`
   - List all action types used
   - Identify complex configurations

2. **Phase 2**: Replace ActionOptions creation with specific builders
   ```java
   // Instead of creating ActionOptions, create specific configs
   ClickOptions click = new ClickOptions.Builder().build();
   PatternFindOptions find = new PatternFindOptions.Builder().build();
   ```

3. **Phase 3**: Update method signatures to accept ActionConfig
   ```java
   public void performAction(ActionConfig config, ObjectCollection objects) {
       // Implementation
   }
   ```

4. **Phase 4**: Test and validate
   - Run all tests
   - Verify behavior is unchanged
   - Remove deprecated imports

5. **Phase 5**: Remove ActionOptions usage completely
   - Delete deprecated ActionOptions references
   - Update documentation
   - Remove compatibility code

## Common Pitfalls

1. **Builder Pattern Confusion**
   - ❌ Wrong: `new MousePressOptions.Builder()`
   - ✅ Correct: `MousePressOptions.builder()` (lowercase)
   - **Rule**: Composed objects (MousePressOptions, VerificationOptions, RepetitionOptions) use lowercase `builder()`
   - **Rule**: Top-level configs (ClickOptions, PatternFindOptions) use `new Builder()`

2. **Success Criteria Type Change**
   - Old: `Predicate<Matches>`
   - New: `Predicate<ActionResult>`
   - Solution: Rewrite predicates to work with ActionResult
   - **Better**: Use VerificationOptions instead of custom predicates

3. **Drag Operations Paradigm Shift**
   - Old: DragOptions contained find logic and offsets
   - New: DragOptions only configures timing; locations provided separately
   - Solution: Find source and target separately, calculate offsets manually

4. **Missing Fields Don't Have Direct Equivalents**
   - Some ActionOptions fields don't exist in ActionConfig
   - Solution: Use action chaining or composite actions
   - Example: `moveMouseAfterAction` → use `.then(new MouseMoveOptions.Builder()...)`

5. **Runtime Type Checking**
   - Actions now check config type at runtime
   - Solution: Ensure correct config type for each action
   - Example: Don't pass ClickOptions to action.find()

For more troubleshooting guidance, see the [Troubleshooting Action Chains](./troubleshooting-chains.md) guide.

## Need Help?

### Migration Resources

- **[Quick Migration Reference](./02-migration-quick-reference.md)** - Concise mapping table for common patterns
- **[Complete Migration Guide](./12-migration-guide.md)** - Step-by-step comprehensive migration strategy
- **[API Reference](./05-reference.md)** - Complete ActionConfig API documentation

### Understanding ActionConfig

- **[ActionConfig Overview](./01-overview.md)** - Concepts and architecture
- **[Action Chaining Guide](./07-action-chaining.md)** - Using `then()` for complex workflows
- **[Code Examples](./03-examples.md)** - Practical ActionConfig examples

### Troubleshooting

- **[Troubleshooting Action Chains](./troubleshooting-chains.md)** - Common errors and solutions
- **[Builder Performance Guide](../advanced/builder-performance-guide.md)** - Optimizing builder usage