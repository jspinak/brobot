---
sidebar_position: 12
title: Migration Guide
description: Step-by-step guide for migrating from ActionOptions to ActionConfig
---

# Migration Guide: ActionOptions to ActionConfig

This guide helps you migrate your existing Brobot automation code from the legacy `ActionOptions` API to the modern `ActionConfig` hierarchy. The migration improves type safety, API clarity, and maintainability.

## Overview of Changes

### Old API (ActionOptions)
```java
// Everything in one class
ActionOptions options = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .setFind(ActionOptions.Find.FIRST)
    .setSimilarity(0.8)
    .setPauseAfterEnd(0.5)
    .build();
```

### New API (ActionConfig)
```java
// Type-specific configuration classes
ClickOptions clickOptions = new ClickOptions.Builder()
    .setPauseAfterEnd(0.5)
    .build();

PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(FindStrategy.FIRST)
    .setSimilarity(0.8)
    .build();
```

## Migration Steps

### Step 1: Identify Action Types

Map your existing ActionOptions usage to the appropriate ActionConfig subclass:

| ActionOptions.Action | New Config Class |
|---------------------|------------------|
| CLICK | ClickOptions |
| TYPE | TypeOptions |
| FIND | PatternFindOptions |
| DRAG | DragOptions |
| VANISH | VanishOptions |
| MOUSE_DOWN | MouseDownOptions |
| MOUSE_UP | MouseUpOptions |
| MOVE | MouseMoveOptions |
| SCROLL | ScrollOptions |
| KEY_DOWN | KeyDownOptions |
| KEY_UP | KeyUpOptions |
| HIGHLIGHT | HighlightOptions |
| DEFINE_REGION | DefineRegionOptions |

### Step 2: Update Find Strategies

Replace ActionOptions.Find with FindStrategy enum:

```java
// Old
ActionOptions.Find.FIRST → FindStrategy.FIRST
ActionOptions.Find.ALL → FindStrategy.ALL
ActionOptions.Find.BEST → FindStrategy.BEST
ActionOptions.Find.EACH → FindStrategy.EACH

// New
PatternFindOptions options = new PatternFindOptions.Builder()
    .setStrategy(FindStrategy.FIRST)
    .build();
```

### Step 3: Migrate Action Code

#### Click Actions

Old:
```java
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .setClickType(ActionOptions.ClickType.LEFT)
    .setPauseAfterEnd(0.5)
    .build();

action.perform(clickOptions, objectCollection);
```

New:
```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .setClickType(ClickOptions.ClickType.LEFT)
    .setPauseAfterEnd(0.5)
    .build();

action.perform(clickOptions, objectCollection);
```

#### Type Actions

Old:
```java
ActionOptions typeOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.TYPE)
    .setTypeDelay(0.1)
    .build();

action.perform(typeOptions, stringCollection);
```

New:
```java
TypeOptions typeOptions = new TypeOptions.Builder()
    .setTypeDelay(0.1)
    .build();

action.perform(typeOptions, stringCollection);
```

#### Find Actions

Old:
```java
ActionOptions findOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setFind(ActionOptions.Find.BEST)
    .setSimilarity(0.8)
    .build();
```

New:
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(FindStrategy.BEST)
    .setSimilarity(0.8)
    .build();
```

### Step 4: Update Composite Actions

#### ClickUntil Pattern

Old:
```java
ClickUntil clickUntil = new ClickUntil();
clickUntil.clickAndFind(buttonImage, targetImage);
```

New:
```java
RepeatUntilConfig config = new RepeatUntilConfig.Builder()
    .setDoAction(new ClickOptions.Builder().build())
    .setActionObjectCollection(buttonImage.asObjectCollection())
    .setUntilAction(new PatternFindOptions.Builder().build())
    .setConditionObjectCollection(targetImage.asObjectCollection())
    .setMaxActions(10)
    .build();

repeatUntilExecutor.execute(config);
```

#### Multiple Actions

Old:
```java
MultipleActionsObject mao = new MultipleActionsObject();
mao.add(new ActionParameters(clickOptions, buttonCollection));
mao.add(new ActionParameters(typeOptions, textCollection));
multipleActions.perform(mao);
```

New:
```java
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();

chainExecutor.executeChain(chain, new ActionResult(),
    buttonCollection, textCollection);
```

### Step 5: Update Test Code

Old test:
```java
@Test
public void testClickAction() {
    ActionOptions options = new ActionOptions.Builder()
        .setAction(ActionOptions.Action.CLICK)
        .build();
    
    ActionResult result = action.perform(options, image);
    assertTrue(result.isSuccess());
}
```

New test:
```java
@Test
public void testClickAction() {
    ClickOptions options = new ClickOptions.Builder().build();
    
    ActionResult result = action.perform(options, image);
    assertTrue(result.isSuccess());
}
```

## Common Migration Patterns

### Pattern 1: Click and Verify

Old:
```java
// Click button
ActionOptions clickOpt = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .build();
action.perform(clickOpt, button);

// Verify result
ActionOptions findOpt = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .build();
ActionResult result = action.perform(findOpt, expectedResult);
```

New:
```java
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new PatternFindOptions.Builder().build())
    .build();

ActionResult result = chainExecutor.executeChain(chain,
    new ActionResult(), button, expectedResult);
```

### Pattern 2: Form Filling

Old:
```java
// Multiple separate actions
action.perform(clickOptions, field1);
action.perform(typeOptions, text1);
action.perform(clickOptions, field2);
action.perform(typeOptions, text2);
```

New:
```java
ActionChainOptions formChain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();

chainExecutor.executeChain(formChain, new ActionResult(),
    field1, text1, field2, text2);
```

### Pattern 3: Conditional Actions

Old:
```java
// Custom logic for retries
int attempts = 0;
while (attempts < 10) {
    action.perform(clickOptions, button);
    if (action.perform(findOptions, target).isSuccess()) {
        break;
    }
    attempts++;
}
```

New:
```java
RepeatUntilConfig config = new RepeatUntilConfig.Builder()
    .setDoAction(new ClickOptions.Builder().build())
    .setActionObjectCollection(button)
    .setUntilAction(new PatternFindOptions.Builder().build())
    .setConditionObjectCollection(target)
    .setMaxActions(10)
    .build();

repeatUntilExecutor.execute(config);
```

## Deprecated Classes to Replace

| Deprecated Class | Replacement |
|-----------------|-------------|
| MultipleActionsObject | ActionChainOptions |
| ActionResultCombo | ActionChainOptions |
| SelectActionObject | Custom ActionChainOptions |
| ClickUntil | RepeatUntilConfig |
| ActionParameters | Not needed with ActionChainOptions |

## Backward Compatibility

The framework maintains backward compatibility during migration:

```java
// Both old and new APIs work
public ActionResult perform(ActionOptions actionOptions, ObjectCollection objectCollection) {
    // Legacy implementation
}

public ActionResult perform(ActionConfig actionConfig, ObjectCollection objectCollection) {
    // New implementation
}
```

## Migration Checklist

- [ ] Identify all ActionOptions usage in your codebase
- [ ] Create a migration plan by module/package
- [ ] Update action configurations to use specific ActionConfig classes
- [ ] Replace composite action patterns with ActionChainOptions
- [ ] Update test cases to use new APIs
- [ ] Run comprehensive tests after each module migration
- [ ] Update documentation and comments
- [ ] Remove deprecated imports once migration is complete

## Troubleshooting

### Common Issues

1. **Compilation errors after migration**
   - Ensure you're importing the correct ActionConfig subclasses
   - Check that builder methods match the new API

2. **Different behavior after migration**
   - Verify timing settings (pauseBeforeBegin, pauseAfterEnd)
   - Check find strategy mappings

3. **Missing functionality**
   - Some edge cases might require custom ActionConfig implementations
   - Contact support if critical functionality is missing

### Getting Help

If you encounter issues during migration:
1. Check the [API documentation](../api-reference)
2. Review the [examples](./examples)
3. Post questions in the community forum
4. File issues on GitHub

## Benefits After Migration

- **Type Safety**: Compile-time checking for action-specific options
- **Cleaner Code**: More readable and maintainable
- **Better Performance**: Optimized action execution
- **Future-Proof**: Ready for upcoming features
- **Enhanced IDE Support**: Better autocomplete and documentation

## Next Steps

- Review [ActionConfig API Reference](../api-reference/action-config)
- Explore [Advanced Patterns](./11-reusable-patterns)
- Learn about [Performance Optimization](./14-performance)