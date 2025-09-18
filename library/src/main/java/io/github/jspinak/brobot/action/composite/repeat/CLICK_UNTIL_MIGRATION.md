# ClickUntil Migration Guide

## Overview
The `ClickUntil` action and `ClickUntilOptions` are deprecated in Brobot 1.1.0+. This functionality should now be implemented using the fluent API with action chaining.

## Migration Examples

### Old Way (Deprecated)
```java
// Using ClickUntil with ActionConfig
ActionConfig options = new ActionConfig.Builder()
    .setAction(ActionType.CLICK_UNTIL)
    .setClickUntil(ActionConfig.ClickUntil.OBJECTS_VANISH)
    .setMaxTimesToRepeatActionSequence(10)
    .build();
action.perform(options, closeButton);
```

### New Way (Fluent API)

#### Option 1: Using Success Criteria
```java
// Click until objects vanish
ClickOptions clickOptions = new ClickOptions.Builder()
    .setSuccessCriteria(matches -> matches.isEmpty())  // Success when no matches
    .setPauseAfterEnd(0.5)
    .build();
    
// The action will repeat until success criteria is met
action.perform(clickOptions, closeButton);
```

#### Option 2: Using Action Chaining
```java
// Click and then check if another element appears
PatternFindOptions findAndClick = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.FIRST)
    .then(new ClickOptions.Builder()
        .setClickType(ClickOptions.Type.LEFT)
        .build())
    .setSuccessCriteria(matches -> matches.isEmpty())  // Repeat until vanished
    .build();
    
action.perform(findAndClick, targetElement);
```

#### Option 3: Using RepeatUntilConfig (For Complex Cases)
```java
// For more complex scenarios with different actions and conditions
RepeatUntilConfig config = new RepeatUntilConfig.Builder()
    .setDoAction(new ClickOptions.Builder().build())
    .setActionObjectCollection(clickTargets)
    .setUntilAction(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .setConditionObjectCollection(conditionTargets)
    .setMaxActions(10)
    .build();
    
// Execute using a custom handler or action executor
```

## Common Use Cases

### 1. Dismiss Popups Until Gone
```java
ClickOptions dismissPopups = new ClickOptions.Builder()
    .setSuccessCriteria(ActionResult::isEmpty)  // Continue until no popups found
    .setMaxClicks(5)  // Safety limit
    .build();
```

### 2. Click Next Until Finish Appears
```java
// Use action chaining to click Next and check for Finish
ClickOptions clickNext = new ClickOptions.Builder()
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .setSuccessCriteria(matches -> !matches.isEmpty())  // Stop when Finish found
    .build();
```

### 3. Clear List Items
```java
ClickOptions clearItems = new ClickOptions.Builder()
    .setClickType(ClickOptions.Type.RIGHT)  // Right-click for delete
    .setSuccessCriteria(matches -> matches.size() < previousSize)  // Until list shrinks
    .build();
```

## Benefits of the New Approach

1. **Type Safety**: Each action has its own specific options class
2. **Clarity**: Success criteria is explicit and customizable
3. **Flexibility**: Can chain different actions together
4. **Composability**: Actions can be combined in various ways
5. **Testability**: Easier to unit test individual actions

## Notes

- The `ClickUntil` class remains for backward compatibility but is deprecated
- `ClickUntilOptions` is also deprecated - use specific Options classes instead
- For simple cases, using success criteria is sufficient
- For complex cases, use `RepeatUntilConfig` or custom action chains