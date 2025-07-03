---
sidebar_position: 6
---

# Migration Examples

Real-world examples of migrating from ActionOptions to ActionConfig.

## Common Migration Patterns

### Basic Click Migration

**Before (ActionOptions):**
```java
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .setClickType(ClickType.Type.LEFT)
    .setPauseBeforeMouseDown(0.1)
    .setPauseAfterMouseUp(0.2)
    .build();
```

**After (ClickOptions):**
```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .setPressOptions(new MousePressOptions.Builder()
        .setPauseBeforeMouseDown(0.1)
        .setPauseAfterMouseUp(0.2)
        .build())
    .build();
```

### Double-Click Migration

**Before:**
```java
ActionOptions doubleClick = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
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
    .setAction(ActionOptions.Action.FIND)
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
    .setAction(ActionOptions.Action.TYPE)
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
    .setAction(ActionOptions.Action.CLICK)
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
    .setAction(ActionOptions.Action.DRAG)
    .setDragToOffsetX(50)
    .setDragToOffsetY(100)
    .build();
```

**After:**
```java
// Use DragOptions with chained move action
// The offset is handled by addW/addH in the move configuration
DragOptions dragOptions = new DragOptions.Builder()
    .setFromOptions(new PatternFindOptions.Builder().build())
    .setToOptions(new PatternFindOptions.Builder()
        .setMatchAdjustment(new MatchAdjustmentOptions.Builder()
            .setAddW(50)
            .setAddH(100)
            .build())
        .build())
    .build();
```

### Multiple Find Actions

**Before:**
```java
ActionOptions multipleFindOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
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
        .setAction(ActionOptions.Action.CLICK)
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
        .setRepetition(new RepetitionOptions.Builder()
            .setTimesToRepeatIndividualAction(3)
            .setPauseBetweenIndividualActions(0.5)
            .build())
        .build();
    
    action.perform(options, objectCollection);
    
    verify(mouse, times(3)).click(any());
}
```

## Using the Migration Helper

The `ActionConfigMigrationHelper` provides convenience methods:

```java
// Convert existing ActionOptions
ActionConfig config = migrationHelper.convert(oldActionOptions);

// Create common configurations
ClickOptions click = ActionConfigMigrationHelper.createDefaultClick();
ClickOptions doubleClick = ActionConfigMigrationHelper.createDoubleClick();
PatternFindOptions findAll = ActionConfigMigrationHelper.createFindAll();

// Get migration guidance
String guide = ActionConfigMigrationHelper.getMigrationGuide("moveMouseAfterAction");
```

## Gradual Migration Strategy

1. **Phase 1**: Use ActionOptionsAdapter for immediate compatibility
   ```java
   ActionConfig config = adapter.convert(actionOptions);
   ```

2. **Phase 2**: Replace ActionOptions creation with specific builders
   ```java
   // Instead of creating ActionOptions, create specific configs
   ClickOptions click = new ClickOptions.Builder().build();
   ```

3. **Phase 3**: Update method signatures to accept ActionConfig
   ```java
   public void performAction(ActionConfig config, ObjectCollection objects) {
       // Implementation
   }
   ```

4. **Phase 4**: Remove ActionOptions usage completely

## Common Pitfalls

1. **Success Criteria Type Change**
   - Old: `Predicate<Matches>`
   - New: `Predicate<ActionResult>`
   - Solution: Rewrite predicates to work with ActionResult

2. **Missing Fields**
   - Some ActionOptions fields don't have direct equivalents
   - Solution: Use action chaining or composite actions

3. **Runtime Type Checking**
   - Actions now check config type at runtime
   - Solution: Ensure correct config type for each action

## Need Help?

- Check the [API Reference](./reference) for all available options
- Use `ActionConfigMigrationHelper.getMigrationGuide()` for specific patterns
- Review the [Fluent API Guide](./fluent-api) for chaining examples