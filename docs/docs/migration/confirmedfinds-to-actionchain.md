# Migrating from ConfirmedFinds to ActionChainOptions

## Overview

The legacy `ConfirmedFinds` class has been removed in favor of the modern `ActionChainOptions` with the `CONFIRM` chaining strategy. This provides the same functionality with a cleaner, more flexible API.

## What Was ConfirmedFinds?

`ConfirmedFinds` performed cascading find operations where each result had to be confirmed by subsequent searches. It was useful for:
- Finding UI elements within specific containers
- Validating matches by checking for expected nearby elements
- Implementing hierarchical searches

## The Modern Replacement

Use `ActionChainOptions` with `ChainingStrategy.CONFIRM`:

### Basic Example

**Old way (ConfirmedFinds):**
```java
// Legacy - no longer available
ConfirmedFinds confirmedFinds = new ConfirmedFinds(find, sceneAnalysisBuilder);
ActionResult result = new ActionResult();
confirmedFinds.perform(result, 
    containerImage.asObjectCollection(),
    buttonInContainer.asObjectCollection());
```

**New way (ActionChainOptions):**
```java
// Modern approach using ActionChainOptions
ActionChainOptions confirmChain = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.7) // Lower threshold for initial search
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setSimilarity(0.9) // Higher threshold for confirmation
        .build())
    .build();

ActionResult result = actionChainExecutor.executeChain(confirmChain, new ActionResult(),
    containerImage.asObjectCollection(),
    buttonInContainer.asObjectCollection());
```

## How CONFIRM Strategy Works

1. **Initial Find**: Performs the first find operation
2. **Confirmation**: Uses matches from first find as search regions for subsequent finds
3. **Validation**: Only returns matches that are confirmed at each step
4. **Early Termination**: Stops if no matches remain

## Common Use Cases

### 1. Find Element Within Container

```java
// Find a button inside a specific dialog
ActionChainOptions nestedButton = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setComment("Find dialog window")
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setComment("Find button within dialog")
        .build())
    .build();
```

### 2. Multi-Level Confirmation

```java
// Find element with multiple confirmations
ActionChainOptions multiConfirm = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.6) // Broad initial search
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(new PatternFindOptions.Builder()
        .setSimilarity(0.8) // Medium confirmation
        .build())
    .then(new PatternFindOptions.Builder()
        .setSimilarity(0.95) // High confidence final confirmation
        .build())
    .build();
```

### 3. Hierarchical UI Navigation

```java
// Navigate through menu hierarchy
ActionChainOptions menuNavigation = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setComment("Find File menu")
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(new PatternFindOptions.Builder()
        .setComment("Find New submenu")
        .build())
    .then(new PatternFindOptions.Builder()
        .setComment("Find Project option")
        .build())
    .build();
```

## Using ConditionalActionChain

For simpler cases, you can also use `ConditionalActionChain`:

```java
// Find and confirm with conditional logic
ConditionalActionChain.find(containerImage)
    .ifFound(new PatternFindOptions.Builder()
        .setComment("Look for button in container")
        .build())
    .ifFoundLog("Button found in container")
    .ifNotFoundLog("Button not found in container")
    .perform(action, buttonInContainer.asObjectCollection());
```

## Benefits of the New Approach

1. **Unified API**: Same pattern for all action chains
2. **More Flexibility**: Can mix different action types in chains
3. **Better Composability**: Can build complex workflows easily
4. **Type Safety**: Using ActionConfig ensures proper configuration
5. **Clear Intent**: CONFIRM strategy explicitly shows cascading validation

## Migration Checklist

- [ ] Identify all uses of `ConfirmedFinds` in your code
- [ ] Replace with `ActionChainOptions.Builder` using `CONFIRM` strategy
- [ ] Update ObjectCollection usage (same pattern works)
- [ ] Test the migration - behavior should be identical
- [ ] Remove imports of `ConfirmedFinds`

## Related Documentation

- [Action Chaining](../03-core-library/action-config/07-action-chaining.md)
- [Pure Actions API](../03-core-library/action-config/14-pure-actions-api.md)
- [Conditional Chains Examples](../03-core-library/action-config/15-conditional-chains-examples.md)