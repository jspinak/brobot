---
sidebar_position: 9
title: Conditional Actions
description: Learn how to implement conditional and repeated actions using RepeatUntilConfig
---

# Conditional Actions with RepeatUntilConfig

Conditional actions are essential for robust automation. They allow your automation to adapt to different scenarios and handle dynamic UI elements. This guide focuses on using `RepeatUntilConfig` to implement "do-until" patterns.

## Understanding RepeatUntilConfig

RepeatUntilConfig enables you to:
- Repeat an action until a condition is met
- Set maximum repetition limits to prevent infinite loops
- Handle dynamic UI elements that require multiple attempts
- Implement polling patterns for asynchronous operations

## Basic Click-Until Patterns

### Click Until Image Appears

The most common pattern is clicking a button until something appears:

```java
public boolean clickUntilImageAppears(StateImage buttonToClick, StateImage imageToAppear) {
    // Configure the click action
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions clickOptions = new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .setPauseAfterEnd(0.5) // Pause between clicks
        .build();
    
    // Configure the verification action
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions findOptions = new ActionOptions.Builder()
        .setAction(ActionType.FIND)
        .setMaxWait(2.0) // Wait up to 2 seconds for image
        .build();
    
    // Build the repeat-until configuration
    RepeatUntilConfig config = new RepeatUntilConfig.Builder()
        .setDoAction(clickOptions)
        .setActionObjectCollection(buttonToClick.asObjectCollection())
        .setUntilAction(findOptions)
        .setConditionObjectCollection(imageToAppear.asObjectCollection())
        .setMaxActions(10) // Maximum 10 clicks
        .build();
    
    // Execute using your action framework
    return executeRepeatUntil(config);
}
```

### Click Until Elements Vanish

Sometimes you need to click elements until they disappear:

```java
public boolean clickUntilElementsVanish(ObjectCollection elementsToClick) {
    // Configure click action
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions clickOptions = new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .setPauseAfterEnd(0.5)
        .build();
    
    // Configure vanish verification
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions vanishOptions = new ActionOptions.Builder()
        .setAction(ActionType.VANISH)
        .setMaxWait(2.0)
        .build();
    
    // Build configuration
    RepeatUntilConfig config = new RepeatUntilConfig.Builder()
        .setDoAction(clickOptions)
        .setActionObjectCollection(elementsToClick)
        .setUntilAction(vanishOptions)
        .setConditionObjectCollection(elementsToClick) // Same elements
        .setMaxActions(10)
        .build();
    
    return executeRepeatUntil(config);
}
```

## Advanced Patterns

### Multi-Step Navigation

Click through a wizard until the finish button appears:

```java
public boolean clickNextUntilFinishAppears(StateImage nextButton, StateImage finishButton) {
    // Configure with specific settings for wizard navigation
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions clickOptions = new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .setPauseAfterEnd(1.0) // Longer pause for page transitions
        .setMinSimilarity(0.8) // Higher accuracy for buttons
        .build();
    
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions findOptions = new ActionOptions.Builder()
        .setAction(ActionType.FIND)
        .setMaxWait(3.0) // More time for page loads
        .setMinSimilarity(0.85) // High accuracy for finish button
        .build();
    
    RepeatUntilConfig config = new RepeatUntilConfig.Builder()
        .setDoAction(clickOptions)
        .setActionObjectCollection(nextButton.asObjectCollection())
        .setUntilAction(findOptions)
        .setConditionObjectCollection(finishButton.asObjectCollection())
        .setMaxActions(10) // Reasonable limit for wizard steps
        .build();
    
    return executeRepeatUntil(config);
}
```

### Clearing Dynamic Lists

Remove all items from a list by clicking delete buttons:

```java
public boolean clearAllItems(StateImage deleteButton) {
    // Click delete until no more delete buttons exist
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions clickOptions = new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .setPauseAfterEnd(0.3) // Quick succession
        .build();
    
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions vanishOptions = new ActionOptions.Builder()
        .setAction(ActionType.VANISH)
        .setMaxWait(1.0) // Quick check
        .build();
    
    RepeatUntilConfig config = new RepeatUntilConfig.Builder()
        .setDoAction(clickOptions)
        .setActionObjectCollection(deleteButton.asObjectCollection())
        .setUntilAction(vanishOptions)
        .setConditionObjectCollection(deleteButton.asObjectCollection())
        .setMaxActions(50) // Higher limit for large lists
        .build();
    
    return executeRepeatUntil(config);
}
```

### Polling for Status Changes

Wait for a process to complete by checking status:

```java
public boolean waitForProcessComplete(StateImage refreshButton, 
                                    StateImage completeStatus) {
    // Click refresh until status shows complete
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions clickOptions = new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .setPauseAfterEnd(2.0) // Wait between refreshes
        .build();
    
    // DEPRECATED - ActionOptions class is deprecated
    ActionOptions findOptions = new ActionOptions.Builder()
        .setAction(ActionType.FIND)
        .setMaxWait(1.0) // Quick check after refresh
        .build();
    
    RepeatUntilConfig config = new RepeatUntilConfig.Builder()
        .setDoAction(clickOptions)
        .setActionObjectCollection(refreshButton.asObjectCollection())
        .setUntilAction(findOptions)
        .setConditionObjectCollection(completeStatus.asObjectCollection())
        .setMaxActions(30) // 30 * 2 seconds = 1 minute max
        .build();
    
    return executeRepeatUntil(config);
}
```

## Type-Until Patterns

RepeatUntilConfig isn't limited to clicking. Here's a typing example:

```java
public boolean typeUntilAccepted(StateImage inputField, 
                                List<String> possibleValues,
                                StateImage successIndicator) {
    
    // Try different values until one is accepted
    for (String value : possibleValues) {
        // DEPRECATED - ActionOptions class is deprecated
        ActionOptions typeOptions = new ActionOptions.Builder()
            .setAction(ActionType.TYPE)
            .setPauseAfterEnd(0.5)
            .build();
        
        // DEPRECATED - ActionOptions class is deprecated
        ActionOptions findOptions = new ActionOptions.Builder()
            .setAction(ActionType.FIND)
            .setMaxWait(2.0)
            .build();
        
        // Clear field first
        clearField(inputField);
        
        RepeatUntilConfig config = new RepeatUntilConfig.Builder()
            .setDoAction(typeOptions)
            .setActionObjectCollection(
                new ObjectCollection.Builder()
                    .withStrings(value)
                    .build())
            .setUntilAction(findOptions)
            .setConditionObjectCollection(successIndicator.asObjectCollection())
            .setMaxActions(1) // Type once per value
            .build();
        
        if (executeRepeatUntil(config)) {
            return true; // Found acceptable value
        }
    }
    return false;
}
```

## Complex Conditional Logic

### Multiple Termination Conditions

Sometimes you need to stop on different conditions:

```java
public boolean performUntilEitherCondition(StateImage actionTarget,
                                         StateImage condition1,
                                         StateImage condition2) {
    // First try with condition1
    RepeatUntilConfig config1 = new RepeatUntilConfig.Builder()
        .setDoAction(createClickOptions())
        .setActionObjectCollection(actionTarget.asObjectCollection())
        .setUntilAction(createFindOptions())
        .setConditionObjectCollection(condition1.asObjectCollection())
        .setMaxActions(5)
        .build();
    
    if (executeRepeatUntil(config1)) {
        return true;
    }
    
    // If not found, try with condition2
    RepeatUntilConfig config2 = new RepeatUntilConfig.Builder()
        .setDoAction(createClickOptions())
        .setActionObjectCollection(actionTarget.asObjectCollection())
        .setUntilAction(createFindOptions())
        .setConditionObjectCollection(condition2.asObjectCollection())
        .setMaxActions(5)
        .build();
    
    return executeRepeatUntil(config2);
}
```

### Nested Conditions

Handle complex scenarios with nested repeat-until patterns:

```java
public boolean complexConditionalWorkflow(StateImage menuButton,
                                        StateImage submenu,
                                        StateImage targetOption) {
    // Keep trying to open menu until submenu appears
    RepeatUntilConfig openMenu = new RepeatUntilConfig.Builder()
        .setDoAction(new ActionOptions.Builder()
            .setAction(ActionType.CLICK)
            .setPauseAfterEnd(0.5)
            .build())
        .setActionObjectCollection(menuButton.asObjectCollection())
        .setUntilAction(new ActionOptions.Builder()
            .setAction(ActionType.FIND)
            .build())
        .setConditionObjectCollection(submenu.asObjectCollection())
        .setMaxActions(3)
        .build();
    
    if (!executeRepeatUntil(openMenu)) {
        return false; // Failed to open menu
    }
    
    // Menu is open, now find and click target
    RepeatUntilConfig selectOption = new RepeatUntilConfig.Builder()
        .setDoAction(new ActionOptions.Builder()
            .setAction(ActionType.CLICK)
            .build())
        .setActionObjectCollection(targetOption.asObjectCollection())
        .setUntilAction(new ActionOptions.Builder()
            .setAction(ActionType.VANISH)
            .build())
        .setConditionObjectCollection(submenu.asObjectCollection())
        .setMaxActions(1)
        .build();
    
    return executeRepeatUntil(selectOption);
}
```

## Best Practices

### 1. Set Reasonable Limits
Always set appropriate `maxActions` to prevent infinite loops:
- Quick operations: 5-10 attempts
- Page transitions: 10-20 attempts  
- Long processes: 30-60 attempts

### 2. Add Appropriate Delays
Use `pauseAfterEnd` to give the UI time to respond:
- Fast operations: 0.3-0.5 seconds
- Normal operations: 0.5-1.0 seconds
- Heavy operations: 1.0-3.0 seconds

### 3. Choose the Right Condition
- Use FIND when waiting for something to appear
- Use VANISH when waiting for something to disappear
- Consider similarity thresholds for reliability

### 4. Handle Failures Gracefully
```java
RepeatUntilConfig config = buildConfig();
boolean success = executeRepeatUntil(config);

if (!success) {
    // Log the failure
    logger.warn("Failed to complete action after {} attempts", 
                config.getMaxActions());
    
    // Try alternative approach or fail gracefully
    return tryAlternativeApproach();
}
```

### 5. Monitor Performance
Track how many attempts are typically needed:
```java
public class MonitoredRepeatUntil {
    public boolean execute(RepeatUntilConfig config) {
        long startTime = System.currentTimeMillis();
        boolean result = executeRepeatUntil(config);
        long duration = System.currentTimeMillis() - startTime;
        
        logger.info("RepeatUntil completed: {} attempts, {} ms", 
                   config.getTotalSuccessfulActions(), duration);
        
        return result;
    }
}
```

## Migration from ClickUntil

If you're migrating from the deprecated ClickUntil class:

```java
// Old ClickUntil approach
clickUntil.clickAndFind(buttonImage, targetImage);

// New RepeatUntilConfig approach
RepeatUntilConfig config = new RepeatUntilConfig.Builder()
    .setDoAction(new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .build())
    .setActionObjectCollection(buttonImage.asObjectCollection())
    .setUntilAction(new ActionOptions.Builder()
        .setAction(ActionType.FIND)
        .build())
    .setConditionObjectCollection(targetImage.asObjectCollection())
    .setMaxActions(10)
    .build();
```

## Next Steps

- Explore [Form Automation](./10-form-automation.md) for practical applications
- Learn about [Reusable Patterns](./11-reusable-patterns.md) to build your automation library