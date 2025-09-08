---
sidebar_position: 9
title: Troubleshooting Action Chains
description: Common issues and solutions when working with action chains in Brobot
---

# Troubleshooting Action Chains

This guide covers common issues you might encounter when working with action chains and their solutions.

## Common Issues

### 1. Type Action Not Executing in Chain

**Problem**: When chaining find->click->type, the type action doesn't execute or receives no text to type.

**Root Cause**: The default NESTED chaining strategy creates a new ObjectCollection with only regions from previous matches, discarding StateStrings needed by the type action.

**Solution**: Split the chain or use CONFIRM strategy.

```java
// ❌ WRONG - Strings are lost
PatternFindOptions chain = new PatternFindOptions.Builder()
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())  // Gets no strings!
    .build();

// ✅ CORRECT - Split by object type
// Chain find->click (both use images)
PatternFindOptions findClick = new PatternFindOptions.Builder()
    .then(new ClickOptions.Builder().build())
    .build();
action.perform(findClick, imageCollection);

// Then type separately (uses strings)
action.perform(new TypeOptions.Builder().build(), stringCollection);
```

### 2. Chain Executes But Wrong Elements Are Targeted

**Problem**: Later actions in the chain operate on unexpected screen regions.

**Root Cause**: NESTED strategy constrains subsequent searches to regions from previous matches.

**Solution**: Understand the strategy behavior or use separate actions.

```java
// With NESTED, the second find searches INSIDE the first match
ActionChainOptions nested = new ActionChainOptions.Builder(findDialog)
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(findButton)  // Searches only within dialog region
    .build();

// If you need to search the whole screen, use separate actions
action.perform(findDialog, dialogImage);
action.perform(findButton, buttonImage);  // Searches entire screen
```

### 3. Actions Execute in Wrong Order

**Problem**: Actions seem to execute out of sequence or simultaneously.

**Root Cause**: Missing pauses between actions or incorrect chain construction.

**Solution**: Add appropriate pauses and verify chain construction.

```java
// ✅ Add pauses between actions
PatternFindOptions findClick = new PatternFindOptions.Builder()
    .setPauseAfterEnd(0.5)  // Wait after find
    .then(new ClickOptions.Builder()
        .setPauseBeforeBegin(0.2)  // Wait before click
        .setPauseAfterEnd(1.0)     // Wait after click
        .build())
    .build();
```

### 4. Chain Fails Silently

**Problem**: Chain appears to execute but nothing happens on screen.

**Root Cause**: An action in the chain failed but error wasn't properly reported.

**Solution**: Check intermediate results and add logging.

```java
// Add logging to debug chain execution
ActionResult result = action.perform(chainOptions, collection);

// Check the execution history
for (ActionRecord record : result.getExecutionHistory()) {
    log.info("Action: {}, Success: {}, Duration: {}ms",
        record.getActionConfig().getClass().getSimpleName(),
        record.isActionSuccess(),
        record.getDuration());
}

// Check if all actions succeeded
if (!result.isSuccess()) {
    log.error("Chain failed at some point");
}
```

### 5. ObjectCollection Contents Not Available

**Problem**: Later actions in chain don't receive expected objects.

**Root Cause**: Misunderstanding of how ObjectCollections flow through chains.

**Solution**: Understand strategy behavior:
- **NESTED**: Creates new collection with only regions
- **CONFIRM**: Preserves original collection

```java
// To preserve all object types through chain
ActionChainOptions preserving = new ActionChainOptions.Builder(firstAction)
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(secondAction)
    .build();
```

## Debugging Techniques

### Enable Verbose Logging

```properties
# In application.properties
logging.level.io.github.jspinak.brobot.action=DEBUG
brobot.logging.verbosity=VERBOSE
```

### Use Illustration to Visualize

```java
// Enable illustration to see what's happening
IllustrationOptions illustrate = new IllustrationOptions.Builder()
    .setIllustrate(true)
    .build();

PatternFindOptions findWithVisual = new PatternFindOptions.Builder()
    .setIllustrate(illustrate)
    .build();
```

### Test Actions Individually First

Before chaining, verify each action works independently:

```java
// Test each action separately
ActionResult findResult = action.find(image);
assert findResult.isSuccess() : "Find failed";

ActionResult clickResult = action.click(image);
assert clickResult.isSuccess() : "Click failed";

ActionResult typeResult = action.type("test");
assert typeResult.isSuccess() : "Type failed";

// Only then combine into chain
```

### Use ConditionalActionChain for Better Control

For complex workflows, ConditionalActionChain provides better debugging:

```java
ConditionalActionChain
    .find(targetImage)
    .ifFoundDo(result -> log.info("Found at: {}", result.getMatchList()))
    .andThenClick()
    .andThenType("text")
    .ifNotFoundDo(result -> log.error("Target not found"))
    .perform(action, collection);
```

## Best Practices Summary

1. **Group by Object Type**: Chain actions that use the same object type
2. **Split Complex Chains**: Break into smaller, manageable pieces
3. **Add Appropriate Delays**: Allow UI to respond between actions
4. **Check Intermediate Results**: Don't assume success
5. **Use Correct Strategy**: NESTED for hierarchical, CONFIRM for validation
6. **Test Incrementally**: Verify each part before combining
7. **Enable Logging**: Use verbose logging during development

## Quick Reference: Object Types by Action

| Action | Primary Object Types |
|--------|---------------------|
| Find | StateImage, StateRegion |
| Click | StateImage, StateRegion, StateLocation |
| Type | StateString |
| Drag | StateImage, StateRegion, StateLocation |
| Move | StateLocation, StateRegion |
| Highlight | Any visual object |
| Vanish | StateImage |
| GetText | StateRegion |

## Need More Help?

- See [Action Chaining Guide](./07-action-chaining.md) for detailed documentation
- Review [Enhanced Conditional Chains](./conditional-chains-examples) for advanced patterns
- Check [Integration Tests](/docs/testing/integration-testing) for working examples
- Consult the [API Reference](./05-reference.md) for all options