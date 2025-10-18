---
sidebar_position: 12
title: Complete Migration Guide
description: Step-by-step guide for migrating from ActionOptions to ActionConfig
---

# Migration Guide: ActionOptions to ActionConfig

This guide helps you migrate your existing Brobot automation code from the legacy `ActionOptions` API to the modern `ActionConfig` hierarchy. The migration improves type safety, API clarity, and maintainability.

> **Quick Reference**: For a concise mapping table, see the [Quick Migration Reference](./02-migration-quick-reference.md).

## Prerequisites

Before migrating, familiarize yourself with:
- **[ActionConfig Overview](./01-overview.md)** - Conceptual foundation of the new system
- **[Migration Examples](./06-migration-examples.md)** - Additional real-world migration examples

## Setup for Migration Examples

All examples in this guide assume the following setup. Add these imports and autowired dependencies to your Spring component:

```java
// Core Brobot imports
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ConditionalActionChain;

// ActionConfig classes
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;

// Composite actions
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;

// Data types
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

// Supporting classes
import io.github.jspinak.brobot.manageStates.mouse.MouseButton;

// Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Testing (for test examples)
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Component
public class MyAutomation {

    @Autowired
    private Action action;

    @Autowired
    private ActionChainExecutor chainExecutor;

    // Example StateImage objects used in examples
    private StateImage buttonImage = new StateImage.Builder()
        .setName("button")
        .addPattern("button.png")
        .build();

    private StateImage targetImage = new StateImage.Builder()
        .setName("target")
        .addPattern("target.png")
        .build();

    // Your migration code here...
}
```

## Overview of Changes

### Old API (ActionOptions)
```java
// Everything in one class - DEPRECATED
ActionOptions options = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .setFind(ActionOptions.Find.FIRST)
    .setSimilarity(0.8)
    .setPauseAfterEnd(0.5)
    .build();
```

### New API Options

#### Option 1: Convenience Methods (Simplest)
```java
// Direct actions without configuration objects
action.click(region);
action.type("text");
action.find(pattern);
```

#### Option 2: ActionConfig Classes (Full Control)
```java
// Type-specific configuration classes
ClickOptions clickOptions = new ClickOptions.Builder()
    .setPauseAfterEnd(0.5)
    .build();

PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.FIRST)
    .setSimilarity(0.8)
    .build();
```

#### Option 3: ConditionalActionChain (Modern Fluent API)
```java
// Modern conditional workflows
ConditionalActionChain
    .find(buttonImage)
    .ifFoundClick()
    .then(targetImage)
    .ifFoundLog("Success!")
    .perform(action);
```

## Choosing the Right Migration Path

Use this decision tree to choose the appropriate API:

```
Need to migrate from ActionOptions?
├─ Simple operations without config?
│  └─ Path A: Convenience Methods (action.click(), action.type())
│
├─ Need conditional logic or retries?
│  ├─ 1-5 retries, form automation, readable code?
│  │  └─ Path C: ConditionalActionChain
│  └─ 10+ retries, polling, custom conditions?
│     └─ Path B: ActionChainOptions with setMaxRepetitions
│
└─ Complex multi-step workflows?
   └─ Path B: ActionChainOptions (full control)
```

### Quick Recommendations by Use Case

| Use Case | Recommended API | Documentation |
|----------|----------------|---------------|
| Simple click/type/find | Convenience Methods | [18-convenience-methods.md](./18-convenience-methods.md) |
| Form filling | ConditionalActionChain | [09-conditional-actions.md](./09-conditional-actions.md) |
| Click-until-appears (1-5 tries) | ConditionalActionChain | [15-conditional-chains-examples.md](./15-conditional-chains-examples.md) |
| Click-until-appears (10+ tries) | ActionChainOptions | [09-conditional-actions.md](./09-conditional-actions.md) |
| Wizard navigation | ActionChainOptions | [08-complex-workflows.md](./08-complex-workflows.md) |
| Nested searches | ActionChainOptions | [07-action-chaining.md](./07-action-chaining.md) |

## Migration Paths

### Path A: Migrate to Convenience Methods (Recommended for Simple Operations)

For straightforward operations without complex configuration, migrate directly to convenience methods:

#### Before (ActionOptions)
```java
// Click action - verbose with ActionOptions
ActionOptions clickOpt = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .build();
ObjectCollection oc = new ObjectCollection.Builder()
    .withRegions(region)
    .build();
action.perform(clickOpt, oc);

// Type action - verbose with ActionOptions
ActionOptions typeOpt = new ActionOptions.Builder()
    .setAction(ActionType.TYPE)
    .build();
ObjectCollection textOc = new ObjectCollection.Builder()
    .withStrings("Hello")
    .build();
action.perform(typeOpt, textOc);
```

#### After (Convenience Methods)
```java
// Click action - simple
action.click(region);

// Type action - simple
action.type("Hello");
```

This is the simplest migration path and works for 80% of use cases. See [Convenience Methods Documentation](./18-convenience-methods.md) for complete details.

### Path B: Migrate to ActionConfig Classes (For Complex Operations)

Use this path when you need fine-grained control or complex configurations.

### Path C: Migrate to ConditionalActionChain (For Conditional Operations)

For operations with retry logic, conditional branching, or form automation, use ConditionalActionChain:

#### Before (ActionOptions with Manual Retry)
```java
// Click until image appears - manual retry loop
int attempts = 0;
while (attempts < 5) {
    ActionOptions clickOpt = new ActionOptions.Builder()
        .setAction(ActionType.CLICK)
        .build();
    action.perform(clickOpt, buttonImage);

    ActionOptions findOpt = new ActionOptions.Builder()
        .setAction(ActionType.FIND)
        .build();

    if (action.perform(findOpt, targetImage).isSuccess()) {
        break;
    }
    attempts++;
}
```

#### After (ConditionalActionChain)
```java
// Click until image appears - fluent and readable
ConditionalActionChain
    .find(buttonImage)
    .ifFoundClick()
    .then(targetImage)
    .ifNotFoundClick(buttonImage)  // Retry 1
    .ifNotFoundClick(buttonImage)  // Retry 2
    .ifNotFoundClick(buttonImage)  // Retry 3
    .then(targetImage)
    .ifFoundLog("Target appeared!")
    .ifNotFoundLog("Target did not appear after retries")
    .perform(action);
```

See [Conditional Action Chains](./15-conditional-chains-examples.md) for comprehensive examples.

## Migration Steps

### Step 1: Identify Action Types

Map your existing ActionOptions usage to the appropriate ActionConfig subclass:

| ActionType (Old) | New Config Class |
|------------------|------------------|
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

Replace ActionOptions.Find with PatternFindOptions.Strategy:

```java
// Old mapping
ActionOptions.Find.FIRST → PatternFindOptions.Strategy.FIRST
ActionOptions.Find.ALL → PatternFindOptions.Strategy.ALL
ActionOptions.Find.BEST → PatternFindOptions.Strategy.BEST
ActionOptions.Find.EACH → PatternFindOptions.Strategy.EACH

// New usage
PatternFindOptions options = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.FIRST)
    .build();
```

### Step 3: Migrate Action Code

#### Click Actions

Old:
```java
// DEPRECATED - ActionOptions class is deprecated
ActionOptions clickOptions = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .setPauseAfterEnd(0.5)
    .build();

action.perform(clickOptions, objectCollection);
```

New:
```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .setPauseAfterEnd(0.5)
    .build();

action.perform(clickOptions, objectCollection);
```

**For Left/Right Click Configuration:**
```java
// Configure mouse button using MousePressOptions
ClickOptions rightClick = new ClickOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .setPauseAfterEnd(0.5)
    .build();
```

#### Type Actions

Old:
```java
// DEPRECATED - ActionOptions class is deprecated
ActionOptions typeOptions = new ActionOptions.Builder()
    .setAction(ActionType.TYPE)
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
// DEPRECATED - ActionOptions class is deprecated
ActionOptions findOptions = new ActionOptions.Builder()
    .setAction(ActionType.FIND)
    .setFind(ActionOptions.Find.BEST)
    .setSimilarity(0.8)
    .build();
```

New:
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.8)
    .build();
```

### Step 4: Update Composite Actions

#### ClickUntil Pattern → ConditionalActionChain (Recommended)

Old:
```java
ClickUntil clickUntil = new ClickUntil();
clickUntil.clickAndFind(buttonImage, targetImage);
```

New - Option 1: ConditionalActionChain (for 1-5 retries):
```java
ConditionalActionChain
    .find(buttonImage)
    .ifFoundClick()
    .then(targetImage)
    .ifNotFoundClick(buttonImage)  // Retry 1
    .ifNotFoundClick(buttonImage)  // Retry 2
    .ifNotFoundClick(buttonImage)  // Retry 3
    .then(targetImage)
    .perform(action);
```

New - Option 2: ActionChainOptions (for 10+ retries):
```java
ActionChainOptions clickWithRetries = new ActionChainOptions.Builder(
    new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)
        .build())
    .setMaxRepetitions(10)
    .setStopCondition(result -> {
        ActionResult checkResult = action.find(targetImage);
        return checkResult.isSuccess();
    })
    .build();

chainExecutor.executeChain(clickWithRetries, new ActionResult(),
    buttonImage.asObjectCollection());
```

#### Multiple Actions → ActionChainOptions

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

For more on action chaining, see [Action Chaining Guide](./07-action-chaining.md).

### Step 5: Update Test Code

Old test:
```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyBrobotTest extends BrobotTestBase {

    @Test
    public void testClickAction() {
        // DEPRECATED - ActionOptions class is deprecated
        ActionOptions options = new ActionOptions.Builder()
            .setAction(ActionType.CLICK)
            .build();

        StateImage image = new StateImage.Builder()
            .setName("test-image")
            .addPattern("test-image.png")
            .build();

        ActionResult result = action.perform(options, image.asObjectCollection());
        assertTrue(result.isSuccess());
    }
}
```

New test:
```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyBrobotTest extends BrobotTestBase {

    @Test
    public void testClickAction() {
        ClickOptions options = new ClickOptions.Builder().build();

        StateImage image = new StateImage.Builder()
            .setName("test-image")
            .addPattern("test-image.png")
            .build();

        ActionResult result = action.perform(options, image.asObjectCollection());
        assertTrue(result.isSuccess());
    }
}
```

## Common Migration Patterns

### Pattern 1: Click and Verify

Old:
```java
// Click button
ActionOptions clickOpt = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .build();
action.perform(clickOpt, button);

// Verify result
ActionOptions findOpt = new ActionOptions.Builder()
    .setAction(ActionType.FIND)
    .build();
ActionResult result = action.perform(findOpt, expectedResult);
```

New - Option 1: ConditionalActionChain (Recommended):
```java
ActionResult result = ConditionalActionChain
    .find(button)
    .ifFoundClick()
    .then(expectedResult)
    .ifFoundLog("Success!")
    .ifNotFoundLog("Verification failed")
    .perform(action);
```

New - Option 2: ActionChainOptions:
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

New - Option 1: ConditionalActionChain (Recommended):
```java
ConditionalActionChain
    .find(field1)
    .ifFoundClick()
    .ifFoundClearAndType("value1")
    .then(field2)
    .ifFoundClick()
    .ifFoundClearAndType("value2")
    .perform(action);
```

New - Option 2: ActionChainOptions:
```java
ActionChainOptions formChain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();

ObjectCollection text1Collection = new ObjectCollection.Builder()
    .withStrings("value1")
    .build();
ObjectCollection text2Collection = new ObjectCollection.Builder()
    .withStrings("value2")
    .build();

chainExecutor.executeChain(formChain, new ActionResult(),
    field1, text1Collection, field2, text2Collection);
```

For specialized form patterns, see [Form Automation Guide](./10-form-automation.md).

### Pattern 3: Conditional Actions with Retry

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

New - Option 1: ConditionalActionChain (for 1-5 retries):
```java
ConditionalActionChain
    .find(button)
    .ifFoundClick()
    .then(target)
    .ifNotFoundClick(button)  // Retry 1
    .ifNotFoundClick(button)  // Retry 2
    .ifNotFoundClick(button)  // Retry 3
    .then(target)
    .ifFoundLog("Target appeared!")
    .perform(action);
```

New - Option 2: ActionChainOptions (for 10+ retries):
```java
ActionChainOptions clickWithRetries = new ActionChainOptions.Builder(
    new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)
        .build())
    .setMaxRepetitions(10)
    .setStopCondition(result -> {
        ActionResult checkResult = action.find(target);
        return checkResult.isSuccess();
    })
    .build();

chainExecutor.executeChain(clickWithRetries, new ActionResult(), button);
```

For more conditional patterns, see [Conditional Actions Guide](./09-conditional-actions.md).

## Deprecated Classes to Replace

These classes were **completely removed in Brobot 1.1.0** with no backward compatibility:

| Removed Class | Replacement | Notes |
|--------------|-------------|-------|
| MultipleActionsObject | ActionChainOptions or ConditionalActionChain | See Path B or Path C |
| ActionResultCombo | ActionChainOptions | See Path B |
| SelectActionObject | ConditionalActionChain | See Path C |
| ClickUntil | ConditionalActionChain or ActionChainOptions | See Pattern 3 |
| ActionParameters | Not needed | Use fluent builders |

:::warning No Backward Compatibility
Code using these classes will NOT compile in Brobot 1.1.0+. You must migrate to the new APIs.
:::

## Backward Compatibility

ActionOptions itself maintains backward compatibility during migration:

```java
// Both old and new APIs work during transition
public ActionResult perform(ActionOptions actionOptions, ObjectCollection objectCollection) {
    // Legacy implementation
}

public ActionResult perform(ActionConfig actionConfig, ObjectCollection objectCollection) {
    // New implementation
}
```

However, the deprecated composite classes (MultipleActionsObject, etc.) were completely removed in 1.1.0.

## Migration Checklist

- [ ] Identify all ActionOptions usage in your codebase
- [ ] Create a migration plan by module/package
- [ ] Update action configurations to use specific ActionConfig classes
- [ ] Replace composite action patterns with ActionChainOptions or ConditionalActionChain
- [ ] Update test cases to use new APIs and extend BrobotTestBase
- [ ] Run comprehensive tests after each module migration
- [ ] Update documentation and comments
- [ ] Remove deprecated imports once migration is complete

## Troubleshooting

### Common Issues

1. **Compilation errors after migration**
   - Ensure you're importing the correct ActionConfig subclasses
   - Check that builder methods match the new API
   - **Note**: `ClickOptions.ClickType` doesn't exist - use `MousePressOptions` with `MouseButton`

2. **Different behavior after migration**
   - Verify timing settings (pauseBeforeBegin, pauseAfterEnd)
   - Check find strategy mappings (must use `PatternFindOptions.Strategy`, not standalone `FindStrategy`)

3. **Missing functionality**
   - Some edge cases might require custom ActionConfig implementations
   - Consider using ConditionalActionChain for conditional logic
   - Contact support if critical functionality is missing

### Getting Help

If you encounter issues during migration:
1. Check the [API Reference](./05-reference.md)
2. Review the [Code Examples](./03-examples.md)
3. See the [Quick Migration Reference](./02-migration-quick-reference.md) for fast lookup
4. Consult the [Migration Examples](./06-migration-examples.md) for detailed patterns
5. Post questions in the community forum
6. File issues on GitHub

## Benefits After Migration

- **Type Safety**: Compile-time checking for action-specific options
- **Cleaner Code**: More readable and maintainable
- **Modern APIs**: Access to ConditionalActionChain fluent syntax
- **Better Performance**: Optimized action execution
- **Future-Proof**: Ready for upcoming features
- **Enhanced IDE Support**: Better autocomplete and documentation

## Next Steps

### Related Guides
- **[Action Chaining](./07-action-chaining.md)** - Understand action composition patterns
- **[Conditional Actions](./09-conditional-actions.md)** - Learn retry and conditional patterns
- **[Conditional Action Chains](./15-conditional-chains-examples.md)** - Modern fluent API examples
- **[Complex Workflows](./08-complex-workflows.md)** - Multi-step automation patterns
- **[Form Automation](./10-form-automation.md)** - Specialized form patterns

### Reference Documentation
- **[ActionConfig Overview](./01-overview.md)** - Conceptual foundation
- **[ActionConfig API Reference](./05-reference.md)** - Complete API documentation
- **[Advanced Patterns](./11-reusable-patterns.md)** - Reusable automation patterns
