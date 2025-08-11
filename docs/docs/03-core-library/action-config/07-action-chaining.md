---
sidebar_position: 7
title: Action Chaining
description: Learn how to chain multiple actions together using ActionChainOptions for complex automation workflows
---

# Action Chaining with ActionChainOptions

Action chaining is a powerful pattern in Brobot that allows you to compose multiple actions into complex workflows. The `ActionChainOptions` class provides a unified, type-safe way to chain actions together with different execution strategies.

## Why Action Chaining?

Traditional automation often requires executing multiple actions in sequence:
- Click a button, then verify a menu appears
- Fill out a form with multiple fields
- Navigate through a multi-step wizard
- Perform complex drag-and-drop operations

ActionChainOptions provides a clean, fluent API for these scenarios while offering advanced features like nested searches and confirmation patterns.

## Basic Concepts

### Chaining Strategies

ActionChainOptions supports two chaining strategies:

1. **NESTED**: Each action searches within the results of the previous action
   - Useful for hierarchical UI navigation
   - Example: Find a dialog, then find a button within that dialog

2. **CONFIRM**: Each action validates the results of the previous action
   - Useful for reducing false positives
   - Example: Find potential matches, then confirm with additional criteria

### Action Flow

When you chain actions:
1. The first action executes with the initial ObjectCollections
2. Subsequent actions can use results from previous actions (NESTED) or validate them (CONFIRM)
3. The chain continues until all actions complete or one fails
4. The final result contains the complete execution history

## Simple Examples

### Sequential Actions

The most basic use case is executing actions in sequence:

```java
@Autowired
private ActionChainExecutor chainExecutor;

// Click a button, then type text
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder()
        .setTypeDelay(0.1)
        .build())
    .build();

// Execute with appropriate ObjectCollections
ActionResult result = chainExecutor.executeChain(chain, new ActionResult(),
    buttonImage.asObjectCollection(),
    new ObjectCollection.Builder().withStrings("Hello World").build()
);
```

### Click and Verify

A common pattern is clicking something and verifying the result:

```java
ActionChainOptions clickVerify = new ActionChainOptions.Builder(
    new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)
        .build())
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setPauseBeforeBegin(2.0) // Wait for result
        .build())
    .build();
```

## Advanced Patterns

### Nested Search

Find elements within other elements using the NESTED strategy:

```java
// Find a dialog, then find a button within it
ActionChainOptions nestedSearch = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .build();
```

### Confirmation Pattern

Use CONFIRM strategy to validate matches:

```java
// Find with low threshold, confirm with high threshold
ActionChainOptions confirmChain = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setSimilarity(0.7) // Lower threshold
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(new PatternFindOptions.Builder()
        .setSimilarity(0.9) // Higher threshold
        .build())
    .build();
```

## Replacing Deprecated Patterns

ActionChainOptions replaces several deprecated composite action patterns:

### Instead of MultipleActionsObject

```java
// Old way
MultipleActionsObject mao = new MultipleActionsObject();
mao.add(new ActionParameters(clickOptions, buttonCollection));
mao.add(new ActionParameters(typeOptions, textCollection));

// New way
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();
```

### Instead of ActionResultCombo

```java
// Old way
ActionResultCombo combo = new ActionResultCombo();
combo.setActionOptions(clickOptions);
combo.setResultOptions(findOptions);

// New way
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new PatternFindOptions.Builder().build())
    .build();
```

## Best Practices

1. **Use appropriate strategies**: Choose NESTED for hierarchical navigation, CONFIRM for validation
2. **Set proper delays**: Use `setPauseAfterEnd()` to allow UI updates between actions
3. **Handle failures gracefully**: Check intermediate results when needed
4. **Create reusable patterns**: Build utility methods for common chains
5. **Keep chains focused**: Break complex workflows into smaller, manageable chains

## Enhanced Conditional Action Chaining

For more advanced conditional workflows with proper sequential composition, see the [Enhanced Conditional Action Chains](./conditional-chains-examples) documentation. The `ConditionalActionChain` class provides:

- The crucial `then()` method for sequential action composition
- Convenience methods like `click()`, `type()`, `scrollDown()`
- Built-in keyboard shortcuts
- Proper conditional execution logic
- No explicit `wait()` methods (following model-based principles)

## Next Steps

- Explore [Enhanced Conditional Action Chains](./conditional-chains-examples) for advanced patterns
- Learn about [Complex Workflows](./complex-workflows) for multi-step automation
- Learn about [Conditional Actions](./conditional-actions) using RepeatUntilConfig
- See [Form Automation](./form-automation) for practical examples