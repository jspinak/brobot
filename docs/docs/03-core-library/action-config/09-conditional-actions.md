---
sidebar_position: 9
title: Conditional Actions
description: Learn how to implement conditional and repeated actions using ConditionalActionChain and modern ActionConfig
---

# Conditional Actions

Conditional actions are essential for robust automation. They allow your automation to adapt to different scenarios and handle dynamic UI elements. This guide shows the modern approach using `ConditionalActionChain` and action chaining.

:::warning Legacy API Notice
This guide has been updated to use the modern `ConditionalActionChain` API and `ActionConfig` hierarchy. If you're looking for information on the legacy `RepeatUntilConfig` with `ActionOptions`, see the [Migration Guide](./12-migration-guide.md).
:::

## Required Imports

All examples in this guide require these imports:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

## Setup: Dependency Injection

All examples assume this setup:

```java
@Component
public class MyConditionalActions {
    private static final Logger logger = LoggerFactory.getLogger(MyConditionalActions.class);

    @Autowired
    private Action action;

    @Autowired
    private ActionChainExecutor chainExecutor;

    // Your conditional action methods go here...
}
```

**Note**: Examples using `ConditionalActionChain` only need `action`. Examples using `ActionChainOptions` also need `chainExecutor`.

## StateImage Initialization

Before using conditional actions, create your StateImages:

```java
// In your @State class or configuration
StateImage buttonToClick = new StateImage.Builder()
    .setName("next-button")
    .addPattern("images/buttons/next.png")
    .build();

StateImage imageToAppear = new StateImage.Builder()
    .setName("success-message")
    .addPattern("images/messages/success.png")
    .build();

StateImage deleteButton = new StateImage.Builder()
    .setName("delete-button")
    .addPattern("images/buttons/delete.png")
    .build();
```

These StateImages are then passed to your conditional action methods.

## Modern Conditional Action APIs

Brobot provides several approaches for conditional actions:

1. **ConditionalActionChain** - Fluent API for most conditional workflows (recommended)
2. **ActionConfig.then()** - Chain actions with implicit conditions
3. **ActionChainOptions with setMaxRepetitions()** - Low-level control for complex patterns

This guide focuses on **ConditionalActionChain**, the most intuitive and powerful approach.

## ConditionalActionChain Overview

ConditionalActionChain provides:
- Fluent, readable syntax for conditional logic
- Built-in retry and repetition patterns
- Automatic error handling with conditional branches
- Integration with keyboard shortcuts and form operations
- No need for manual repeat-until configuration

## Basic Click-Until Patterns

### Click Until Image Appears

The most common pattern is clicking a button until something appears:

```java
public boolean clickUntilImageAppears(StateImage buttonToClick, StateImage imageToAppear) {
    // Modern approach using ConditionalActionChain
    // Each .ifNotFoundClick() represents one retry attempt
    return ConditionalActionChain
        .find(buttonToClick)
        .ifFoundClick()
        .then(imageToAppear)  // Check if target image appears
        .ifFoundLog("Success! Image appeared")
        .ifNotFoundClick(buttonToClick)  // Retry 1
        .ifNotFoundClick(buttonToClick)  // Retry 2
        .ifNotFoundClick(buttonToClick)  // Retry 3
        .ifNotFoundClick(buttonToClick)  // Retry 4
        .ifNotFoundClick(buttonToClick)  // Retry 5
        .perform(action)
        .isSuccess();
}
```

**For many retries, use ActionChainOptions instead**:

```java
public boolean clickUntilImageAppearsMany(StateImage buttonToClick, StateImage imageToAppear) {
    // Better for 10+ retries
    ActionChainOptions clickWithRetries = new ActionChainOptions.Builder(
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .setMaxRepetitions(10)  // Up to 10 clicks
        .setStopCondition(result -> {
            // Stop when imageToAppear is found
            ActionResult checkResult = action.find(imageToAppear);
            return checkResult.isSuccess();
        })
        .build();

    ActionResult result = chainExecutor.executeChain(
        clickWithRetries,
        new ActionResult(),
        buttonToClick.asObjectCollection()
    );

    return result.isSuccess();
}
```

**Alternative with explicit find configuration**:

```java
public boolean clickUntilImageAppearsConfigured(StateImage buttonToClick, StateImage imageToAppear) {
    PatternFindOptions findButton = new PatternFindOptions.Builder()
        .setSimilarity(0.8)
        .setPauseAfterEnd(0.5)
        .build();

    PatternFindOptions findImage = new PatternFindOptions.Builder()
        .setSearchDuration(2.0)
        .setSimilarity(0.85)
        .build();

    return ConditionalActionChain
        .find(findButton)
        .ifFoundClick()
        .then(findImage)
        .ifNotFoundClick(buttonToClick)  // Retry pattern
        .perform(action, new ObjectCollection.Builder()
            .withImages(buttonToClick, imageToAppear)
            .build())
        .isSuccess();
}
```

### Click Until Elements Vanish

Sometimes you need to click elements until they disappear:

```java
public boolean clickUntilElementsVanish(StateImage elementToClick) {
    // Click the element and check if it vanishes
    return ConditionalActionChain
        .find(elementToClick)
        .ifFoundClick()
        .then(elementToClick)  // Check if still visible
        .ifFoundClick()  // Still there, click again
        .ifFoundClick()  // Retry
        .ifFoundClick()  // Retry
        .ifNotFoundLog("Element vanished successfully")
        .perform(action, elementToClick.asObjectCollection())
        .isSuccess();
}
```

**Alternative using ActionChainOptions for precise control**:

```java
public boolean clickUntilVanish(StateImage deleteButton) {
    // Use setStopCondition to check when element is gone
    ActionChainOptions clickUntilGone = new ActionChainOptions.Builder(
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .setMaxRepetitions(10)
        .setStopCondition(result -> result.getMatchList().isEmpty())  // Stop when no matches
        .build();

    ActionResult result = chainExecutor.executeChain(
        clickUntilGone,
        new ActionResult(),
        deleteButton.asObjectCollection()
    );

    return result.isSuccess();
}
```

## Advanced Patterns

### Multi-Step Navigation

Click through a wizard until the finish button appears:

```java
public boolean clickNextUntilFinishAppears(StateImage nextButton, StateImage finishButton) {
    // Use ActionChainOptions for wizard navigation with many steps
    ClickOptions clickNext = new ClickOptions.Builder()
        .setPauseAfterEnd(1.0)  // Pause for page transitions
        .build();

    ActionChainOptions wizardNavigation = new ActionChainOptions.Builder(clickNext)
        .setMaxRepetitions(10)  // Maximum 10 wizard pages
        .setStopCondition(result -> {
            // Stop when finish button appears
            PatternFindOptions findFinish = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .setSearchDuration(3.0)
                .build();
            ActionResult finishCheck = action.perform(findFinish, finishButton.asObjectCollection());
            return finishCheck.isSuccess();
        })
        .build();

    ActionResult result = chainExecutor.executeChain(
        wizardNavigation,
        new ActionResult(),
        nextButton.asObjectCollection()
    );

    return result.isSuccess();
}
```

**Alternative: Simple wizard with few steps**:

```java
public boolean clickNextFewTimes(StateImage nextButton, StateImage finishButton) {
    // For wizards with 3-5 steps, ConditionalActionChain is clearer
    return ConditionalActionChain
        .find(nextButton)
        .ifFoundClick()
        .then(finishButton)
        .ifNotFoundClick(nextButton)  // Page 2
        .ifNotFoundClick(nextButton)  // Page 3
        .ifNotFoundClick(nextButton)  // Page 4
        .ifNotFoundClick(nextButton)  // Page 5
        .perform(action, new ObjectCollection.Builder()
            .withImages(nextButton, finishButton)
            .build())
        .isSuccess();
}
```

### Clearing Dynamic Lists

Remove all items from a list by clicking delete buttons:

```java
public boolean clearAllItems(StateImage deleteButton) {
    ActionChainOptions clickUntilNone = new ActionChainOptions.Builder(
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.3)
            .build())
        .setMaxRepetitions(50)  // Higher limit for large lists
        .setStopCondition(result -> result.getMatchList().isEmpty())
        .build();

    ActionResult result = chainExecutor.executeChain(
        clickUntilNone,
        new ActionResult(),
        deleteButton.asObjectCollection()
    );

    return result.isSuccess();
}
```

### Polling for Status Changes

Wait for a process to complete by checking status:

```java
public boolean waitForProcessComplete(StateImage refreshButton, StateImage completeStatus) {
    // Use ActionChainOptions for polling with timeout
    ClickOptions clickRefresh = new ClickOptions.Builder()
        .setPauseAfterEnd(2.0)  // Wait 2 seconds between refreshes
        .build();

    ActionChainOptions pollingChain = new ActionChainOptions.Builder(clickRefresh)
        .setMaxRepetitions(30)  // 30 refreshes × 2 seconds = 1 minute max
        .setStopCondition(result -> {
            // Stop when complete status appears
            PatternFindOptions findComplete = new PatternFindOptions.Builder()
                .setSearchDuration(1.0)
                .build();
            ActionResult statusCheck = action.perform(findComplete, completeStatus.asObjectCollection());
            return statusCheck.isSuccess();
        })
        .build();

    ActionResult result = chainExecutor.executeChain(
        pollingChain,
        new ActionResult(),
        refreshButton.asObjectCollection()
    );

    return result.isSuccess();
}
```

**Alternative: Quick status check without clicking**:

```java
public boolean waitForStatusChange(StateImage completeStatus, int maxSeconds) {
    // Poll for status without clicking refresh button
    PatternFindOptions checkStatus = new PatternFindOptions.Builder()
        .setSearchDuration(1.0)
        .setPauseAfterEnd(1.0)  // Check every second
        .build();

    ActionChainOptions statusPolling = new ActionChainOptions.Builder(checkStatus)
        .setMaxRepetitions(maxSeconds)  // Check N times
        .setStopCondition(result -> result.isSuccess())  // Stop when found
        .build();

    ActionResult result = chainExecutor.executeChain(
        statusPolling,
        new ActionResult(),
        completeStatus.asObjectCollection()
    );

    return result.isSuccess();
}
```

## Form Automation Patterns

### Type and Verify

Try typing a value and verify it was accepted:

```java
public boolean typeAndVerify(StateImage inputField, String value, StateImage successIndicator) {
    return ConditionalActionChain
        .find(inputField)
        .ifFoundClick()
        .ifFoundClearAndType(value)  // Built-in clear and type
        .then(successIndicator)
        .ifFoundLog("Value accepted: " + value)
        .ifNotFoundLog("Value rejected: " + value)
        .perform(action, new ObjectCollection.Builder()
            .withImages(inputField, successIndicator)
            .withStrings(value)
            .build())
        .isSuccess();
}
```

### Try Multiple Values

Try different values until one is accepted:

```java
public boolean typeUntilAccepted(StateImage inputField,
                                List<String> possibleValues,
                                StateImage successIndicator) {
    // Try each value until one succeeds
    for (String value : possibleValues) {
        if (typeAndVerify(inputField, value, successIndicator)) {
            return true;
        }
    }
    return false;
}
```

## Complex Conditional Logic

### Multiple Termination Conditions

Stop when either of two conditions is met:

```java
public boolean performUntilEitherCondition(StateImage actionTarget,
                                         StateImage condition1,
                                         StateImage condition2) {
    // Try to find either condition after clicking
    return ConditionalActionChain
        .find(actionTarget)
        .ifFoundClick()
        .then(condition1)
        .ifFoundLog("Condition 1 met")
        .ifNotFoundThen(condition2)  // Check second condition
        .ifFoundLog("Condition 2 met")
        .ifNotFoundClick(actionTarget)  // Retry if neither found
        .perform(action, new ObjectCollection.Builder()
            .withImages(actionTarget, condition1, condition2)
            .build())
        .isSuccess();
}
```

### Nested Conditions

Handle complex scenarios with nested patterns:

```java
public boolean complexConditionalWorkflow(StateImage menuButton,
                                        StateImage submenu,
                                        StateImage targetOption) {
    // Open menu until submenu appears, then click target
    return ConditionalActionChain
        .find(menuButton)
        .ifFoundClick()
        .then(submenu)
        .ifFoundClick(targetOption)  // Submenu found, click target
        .ifNotFoundClick(menuButton)  // Submenu not found, retry menu
        .ifNotFoundClick(menuButton)
        .ifNotFoundClick(menuButton)
        .perform(action, new ObjectCollection.Builder()
            .withImages(menuButton, submenu, targetOption)
            .build())
        .isSuccess();
}
```

## Best Practices

### 1. Use ConditionalActionChain for Readability
ConditionalActionChain provides the clearest intent:
```java
// Clear and readable
ConditionalActionChain
    .find(button)
    .ifFoundClick()
    .then(result)
    .ifNotFoundLog("Operation failed")
    .perform(action);
```

### 2. Set Reasonable Retry Limits
Chain `ifNotFound` calls to control retry attempts:
- Quick operations: 3-5 retries
- Page transitions: 5-10 retries
- Long processes: 10-30 retries

### 3. Add Appropriate Delays
Configure delays in ClickOptions or PatternFindOptions:
```java
ClickOptions withDelay = new ClickOptions.Builder()
    .setPauseAfterEnd(1.0)  // 1 second delay
    .build();
```

### 4. Use Built-In Form Operations
ConditionalActionChain has specialized form methods:
- `.ifFoundClearAndType()` - Clear field then type
- `.ifFoundType()` - Type without clearing
- `.pressEnter()`, `.pressEscape()`, `.pressCtrlS()` - Keyboard shortcuts

### 5. Handle Failures Gracefully
Use `.ifNotFound()` methods for error handling:
```java
return ConditionalActionChain
    .find(element)
    .ifFoundClick()
    .ifNotFoundLog("Element not found after retries")
    .ifNotFoundThen(fallbackElement)  // Try alternative
    .perform(action)
    .isSuccess();
```

### 6. Prefer Conditional Chains Over Manual Loops
```java
// ❌ Avoid manual loops
for (int i = 0; i < 10; i++) {
    if (action.find(element).isSuccess()) {
        action.click(element);
        break;
    }
}

// ✅ Use ConditionalActionChain
ConditionalActionChain
    .find(element)
    .ifFoundClick()
    .ifNotFound(element)  // Automatic retry
    .perform(action);
```

### 7. Monitor Performance with ActionResult
Check intermediate results when needed:
```java
ActionResult result = ConditionalActionChain
    .find(button)
    .ifFoundClick()
    .perform(action);

logger.info("Found {} matches in {}ms",
    result.getMatchList().size(),
    result.getDuration());
```

## Keyboard Shortcuts

ConditionalActionChain provides built-in keyboard operations:

```java
// Save with Ctrl+S
ConditionalActionChain
    .find(document)
    .ifFoundClick()
    .pressCtrlS()  // Built-in shortcut
    .perform(action);

// Cancel with Escape
ConditionalActionChain
    .find(dialog)
    .ifFoundPressEscape()
    .perform(action);

// Submit with Enter
ConditionalActionChain
    .find(form)
    .ifFoundPressEnter()
    .perform(action);
```

## When to Use Each Approach

### Use ConditionalActionChain when:
- **Retries needed**: 1-5 retry attempts (use `.ifNotFound()` chains)
- **Conditional logic**: Different actions based on find results
- **Form automation**: Filling fields, validating input
- **Readability**: Declarative, self-documenting code
- **Built-in shortcuts**: Need keyboard operations (`.pressEnter()`, `.pressEscape()`)

**Example**: Click-until-appears with 3-5 retries, form filling, conditional workflows

### Use ActionChainOptions when:
- **Many retries**: 10+ repetitions needed
- **Custom conditions**: Complex `setStopCondition()` logic
- **Polling patterns**: Status checks with timeouts
- **Wizard navigation**: Multi-step processes with unknown length
- **Precise control**: Need exact repetition count and timing

**Example**: Wizard navigation (10+ pages), polling (30+ checks), clearing dynamic lists (50+ items)

### Use ActionConfig.then() when:
- **Sequential only**: No conditional branching needed
- **Simple chains**: 2-3 actions in sequence
- **No retries**: Actions run once
- **Embedded in chains**: Part of larger ConditionalActionChain or ActionChainOptions

**Example**: Find → Click → Verify sequence without retries

### Decision Tree

```
Need conditional logic?
├─ Yes → How many retries?
│  ├─ 1-5 retries → ConditionalActionChain
│  └─ 10+ retries → ActionChainOptions with setMaxRepetitions
└─ No → Simple sequence?
   ├─ Yes → ActionConfig.then()
   └─ No → ActionChainOptions with custom logic
```

## Migration from Legacy APIs

### From ActionOptions to ActionConfig

```java
// ❌ Old - ActionOptions (deprecated, doesn't exist)
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .build();

// ✅ New - ClickOptions
ClickOptions clickOptions = new ClickOptions.Builder()
    .build();
```

### From RepeatUntilConfig to ConditionalActionChain

```java
// ❌ Old - RepeatUntilConfig pattern
RepeatUntilConfig config = new RepeatUntilConfig.Builder()
    .setDoAction(clickOptions)
    .setUntilAction(findOptions)
    .setMaxActions(10)
    .build();
// executeRepeatUntil() doesn't exist!

// ✅ New - ConditionalActionChain
ConditionalActionChain
    .find(button)
    .ifFoundClick()
    .ifNotFoundClick(button)  // Retry pattern
    .perform(action);
```

See the [Migration Guide](./12-migration-guide.md) for comprehensive migration instructions.

## Next Steps

### Related Guides
- **[Conditional Action Chains](./15-conditional-chains-examples.md)** - Comprehensive ConditionalActionChain examples
- **[Action Chaining](./07-action-chaining.md)** - Understanding action composition and chaining strategies
- **[Complex Workflows](./08-complex-workflows.md)** - Building sophisticated multi-step automations
- **[Form Automation](./10-form-automation.md)** - Specialized form interaction patterns
- **[Reusable Patterns](./11-reusable-patterns.md)** - Building your automation pattern library

### Reference Documentation
- **[ActionConfig Overview](./01-overview.md)** - Conceptual foundation of ActionConfig
- **[API Reference](./05-reference.md)** - Complete ActionConfig API documentation
- **[Migration Guide](./12-migration-guide.md)** - Migrating from deprecated APIs