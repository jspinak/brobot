---
sidebar_position: 9
title: Troubleshooting Action Chains
description: Common issues and solutions when working with action chains in Brobot
---

# Troubleshooting Action Chains

This guide covers common issues you might encounter when working with action chains and their solutions.

## Required Imports

All examples in this guide assume the following imports:

```java
// Brobot Core
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionConfig;

// ActionConfig Classes
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;

// Conditional Chains
import io.github.jspinak.brobot.action.conditionals.ConditionalActionChain;

// Datatypes
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateString.StateString;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;

// Action Records
import io.github.jspinak.brobot.model.action.ActionRecord;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

## Prerequisites

:::info Assumed Variables
Unless otherwise specified, the code examples assume you have:
- `@Autowired Action action` - Injected Brobot Action service
- StateImage variables (e.g., `dialogImage`, `buttonImage`) - Pre-initialized UI elements
- Logger instance for debugging output

See the [Complete Working Example](#complete-working-example) section for a fully compilable troubleshooting scenario.
:::

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
PatternFindOptions findWithVisual = new PatternFindOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.YES)
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
    .ifFoundClick()  // Click if found
    .type("text")    // Type text after clicking
    .ifNotFoundDo(result -> log.error("Target not found"))
    .perform(action, new ObjectCollection.Builder().build());
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

## Complete Working Example

Here's a fully compilable example demonstrating common troubleshooting scenarios:

```java
package com.example.automation;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.conditionals.ConditionalActionChain;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateString.StateString;
import io.github.jspinak.brobot.model.action.ActionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Complete troubleshooting examples for action chains.
 * Demonstrates solutions to common chain execution issues.
 */
@Slf4j
@Component
public class ChainTroubleshootingExamples {

    private final Action action;

    // StateImages for examples
    private final StateImage dialogImage;
    private final StateImage buttonImage;
    private final StateImage usernameField;
    private final StateImage passwordField;
    private final StateImage submitButton;

    @Autowired
    public ChainTroubleshootingExamples(Action action) {
        this.action = action;

        // Initialize StateImages
        this.dialogImage = new StateImage.Builder()
            .addPattern("images/dialog.png")
            .build();

        this.buttonImage = new StateImage.Builder()
            .addPattern("images/button.png")
            .build();

        this.usernameField = new StateImage.Builder()
            .addPattern("images/username-field.png")
            .build();

        this.passwordField = new StateImage.Builder()
            .addPattern("images/password-field.png")
            .build();

        this.submitButton = new StateImage.Builder()
            .addPattern("images/submit-button.png")
            .build();
    }

    /**
     * Example 1: Correct way to handle find->click->type
     * Splits chain to avoid losing StateStrings
     */
    public boolean loginWithCorrectChaining(String username, String password) {
        log.info("Demonstrating correct chain splitting for type actions");

        // Step 1: Chain find->click for images (NESTED works fine)
        PatternFindOptions findClick = new PatternFindOptions.Builder()
            .then(new ClickOptions.Builder().build())
            .build();

        // Find and click username field
        ObjectCollection usernameImageCollection = new ObjectCollection.Builder()
            .withImages(usernameField)
            .build();
        ActionResult usernameResult = action.perform(findClick, usernameImageCollection);

        if (!usernameResult.isSuccess()) {
            log.error("Failed to find/click username field");
            return false;
        }

        // Step 2: Type separately (uses StateString)
        StateString usernameString = new StateString.Builder()
            .setString(username)
            .build();
        ObjectCollection usernameStringCollection = new ObjectCollection.Builder()
            .withStrings(usernameString)
            .build();
        action.perform(new TypeOptions.Builder().build(), usernameStringCollection);

        // Repeat for password
        ActionResult passwordResult = action.perform(findClick,
            new ObjectCollection.Builder().withImages(passwordField).build());
        if (!passwordResult.isSuccess()) {
            log.error("Failed to find/click password field");
            return false;
        }

        StateString passwordString = new StateString.Builder()
            .setString(password)
            .build();
        action.perform(new TypeOptions.Builder().build(),
            new ObjectCollection.Builder().withStrings(passwordString).build());

        // Click submit
        action.perform(findClick, new ObjectCollection.Builder().withImages(submitButton).build());

        return true;
    }

    /**
     * Example 2: Using NESTED strategy correctly for hierarchical searches
     */
    public ActionResult searchWithinDialog() {
        log.info("Demonstrating NESTED strategy for hierarchical search");

        // First action: Find the dialog
        PatternFindOptions findDialog = new PatternFindOptions.Builder()
            .build();

        // Create chain with NESTED strategy
        // The button will be searched ONLY within the dialog region
        ActionChainOptions nested = new ActionChainOptions.Builder(findDialog)
            .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
            .then(new PatternFindOptions.Builder().build())  // Find button within dialog
            .then(new ClickOptions.Builder().build())        // Click the button
            .build();

        ObjectCollection dialogCollection = new ObjectCollection.Builder()
            .withImages(dialogImage)
            .build();

        ObjectCollection buttonCollection = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();

        return action.perform(nested, dialogCollection, buttonCollection);
    }

    /**
     * Example 3: Using ConditionalActionChain with proper debugging
     */
    public ActionResult loginWithConditionalChain(String username, String password) {
        log.info("Demonstrating ConditionalActionChain with debugging");

        // Create StateImages collection
        ObjectCollection imageCollection = new ObjectCollection.Builder()
            .withImages(usernameField)
            .build();

        return ConditionalActionChain
            .find(usernameField)
            .ifFoundDo(result -> log.info("Found username field at: {}",
                result.getBestMatch().map(m -> m.getRegion()).orElse(null)))
            .ifFoundClick()
            .type(username)
            .then(passwordField)
            .ifFoundClick()
            .type(password)
            .then(submitButton)
            .ifFoundClick()
            .ifNotFoundDo(result -> log.error("Login flow failed at some step"))
            .perform(action, new ObjectCollection.Builder().build());
    }

    /**
     * Example 4: Debugging chain failures with execution history
     */
    public void debugChainExecution() {
        log.info("Demonstrating chain execution debugging");

        // Create a chain
        PatternFindOptions chain = new PatternFindOptions.Builder()
            .setPauseAfterEnd(0.5)
            .then(new ClickOptions.Builder()
                .setPauseBeforeBegin(0.2)
                .setPauseAfterEnd(0.5)
                .build())
            .build();

        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();

        // Execute chain
        ActionResult result = action.perform(chain, collection);

        // Analyze execution history
        log.info("Chain overall success: {}", result.isSuccess());
        log.info("Total actions in history: {}", result.getExecutionHistory().size());

        for (int i = 0; i < result.getExecutionHistory().size(); i++) {
            ActionRecord record = result.getExecutionHistory().get(i);
            log.info("Action {}: {} - Success: {}, Duration: {}ms",
                i + 1,
                record.getActionConfig().getClass().getSimpleName(),
                record.isActionSuccess(),
                record.getDuration());
        }

        if (!result.isSuccess()) {
            log.error("Chain failed. Check execution history above for which step failed.");
        }
    }
}
```

This example demonstrates:
- **Complete Spring setup** with `@Component` and `@Autowired`
- **Correct chain splitting** to avoid type action failures
- **NESTED strategy usage** for hierarchical searches
- **ConditionalActionChain** with proper method names
- **Execution history debugging** for troubleshooting failures
- **StateImage and StateString initialization**
- **Logger usage** for debugging output

To use this in your application:
1. Place image files in `src/main/resources/images/` directory
2. Ensure Brobot dependencies are configured
3. Inject this component into your automation workflows
4. Call the demonstration methods to see solutions in action

## Need More Help?

- See [Action Chaining Guide](./07-action-chaining.md) for detailed documentation
- Review [Conditional Action Chains](./15-conditional-chains-examples.md) for advanced patterns
- Check [Integration Tests](../../04-testing/integration-testing.md) for working examples
- Consult the [API Reference](./05-reference.md) for all options