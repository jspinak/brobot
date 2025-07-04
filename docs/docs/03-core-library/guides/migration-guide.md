# Brobot ActionOptions to ActionConfig Migration Guide

## Overview

Brobot 2.0 introduces a new type-safe ActionConfig API that replaces the generic ActionOptions. This guide will help you migrate your existing code to the new API.

## Why Migrate?

- **Type Safety**: Compile-time checking prevents invalid configurations
- **Better IDE Support**: Auto-completion shows only relevant options
- **Clearer Code**: Config class names indicate the action type
- **Future Proof**: New features will only be added to ActionConfig API

## Quick Reference

| Old API | New API | Import |
|---------|---------|--------|
| `ActionOptions.Action.CLICK` | `ClickOptions` | `io.github.jspinak.brobot.action.basic.click.ClickOptions` |
| `ActionOptions.Action.FIND` | `PatternFindOptions` | `io.github.jspinak.brobot.action.basic.find.PatternFindOptions` |
| `ActionOptions.Action.TYPE` | `TypeOptions` | `io.github.jspinak.brobot.action.basic.type.TypeOptions` |
| `ActionOptions.Action.DRAG` | `DragOptions` | `io.github.jspinak.brobot.action.basic.drag.DragOptions` |
| `ActionOptions.Action.DEFINE` | `DefineRegionOptions` | `io.github.jspinak.brobot.action.basic.region.DefineRegionOptions` |
| `ActionOptions.Action.HIGHLIGHT` | `HighlightOptions` | `io.github.jspinak.brobot.action.basic.focus.HighlightOptions` |

## Migration Examples

### 1. Simple Click

**Before:**
```java
@Autowired
private Action action;

ActionOptions options = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .build();
ActionResult result = action.perform(options, objectCollection);
```

**After:**
```java
@Autowired
private ActionService actionService;

ClickOptions clickOptions = new ClickOptions.Builder()
    .setClickType(ClickOptions.Type.LEFT)
    .build();
    
ActionResult result = new ActionResult();
result.setActionConfig(clickOptions);

ActionInterface clickAction = actionService.getAction(clickOptions);
clickAction.perform(result, objectCollection);
```

### 2. Find Operations

**Before:**
```java
ActionOptions options = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setFind(ActionOptions.Find.ALL)
    .setSimilarity(0.9)
    .build();
ActionResult matches = action.perform(options, objectCollection);
```

**After:**
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setSimilarity(0.9)
    .build();
    
ActionResult matches = new ActionResult();
matches.setActionConfig(findOptions);

ActionInterface findAction = actionService.getAction(findOptions);
findAction.perform(matches, objectCollection);
```

### 3. Text/OCR Operations

**Before:**
```java
ActionOptions options = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setFind(ActionOptions.Find.ALL_WORDS)
    .build();
```

**After:**
```java
TextFindOptions textOptions = new TextFindOptions.Builder()
    .setLanguage("eng")
    .setMaxMatchRetries(3)
    .build();
```

### 4. Complex Actions

**Before:**
```java
ActionOptions dragOptions = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.DRAG)
    .setDragToOffsetX(100)
    .setDragToOffsetY(200)
    .build();
```

**After:**
```java
DragOptions dragOptions = new DragOptions.Builder()
    .setFromIndex(0)
    .setToIndex(1)
    .setOffsetX(100)
    .setOffsetY(200)
    .build();
```

## State Transitions Migration

**Before:**
```java
private boolean goToNextState() {
    return action.perform(
        ActionOptions.Action.CLICK, 
        stateImage
    ).isSuccess();
}
```

**After:**
```java
private boolean goToNextState() {
    ClickOptions click = new ClickOptions.Builder().build();
    ActionResult result = new ActionResult();
    result.setActionConfig(click);
    
    ObjectCollection objects = new ObjectCollection.Builder()
        .withImages(stateImage)
        .build();
        
    ActionInterface clickAction = actionService.getAction(click);
    clickAction.perform(result, objects);
    
    return result.isSuccess();
}
```

## JSON Configuration Migration

**Before:**
```json
{
  "actionOptions": {
    "action": "CLICK",
    "clickType": "LEFT",
    "similarity": 0.8
  }
}
```

**After:**
```json
{
  "actionConfig": {
    "@type": "ClickOptions",
    "clickType": "LEFT",
    "numberOfClicks": 1,
    "pauseAfterEnd": 0.5
  }
}
```

## Common Pitfalls and Solutions

### 1. Missing Offset Migration
- Old: `setAddX(10)` and `setAddY(20)`
- New: `setOffsetX(10)` and `setOffsetY(20)`

### 2. Find Strategy Names
- Old: `ActionOptions.Find.FIRST`
- New: `PatternFindOptions.Strategy.FIRST`

### 3. Action Repetition
- Old: `setTimesToRepeatIndividualAction(3)`
- New: Use action-specific options like `setNumberOfClicks(3)`

### 4. Pause Timing
- Old: `setPauseBetweenActions(0.5)`
- New: `setPauseBetweenClicks(0.5)` or similar specific options

## Backward Compatibility

The old ActionOptions API is still available but deprecated. We recommend migrating as soon as possible to benefit from the improvements.

## Getting Help

- Check the test files ending with "Updated" for migration examples
- Refer to JavaDoc for each ActionConfig class
- Post questions in the Brobot community forums

## Tools and Scripts

A migration script is available to help automate common conversions:
```bash
# Run from project root
./scripts/migrate-actionoptions.sh src/
```

Note: Always review automated changes and test thoroughly.