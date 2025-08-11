---
sidebar_position: 5
title: 'The Action Hierarchy'
---

# Controlling the GUI

Brobot interacts with the GUI using the Sikuli library. This is done with 3 main levels of abstraction:

## Sikuli Wrappers

These are the methods that form the interface between Brobot and Sikuli.
Sikuli Wrappers route the operational instructions either to Sikuli methods, which control the mouse and keyboard and capture data from the screen, or to functions that mock (simulate) these methods. When calling Sikuli methods, the Wrappers convert Brobot data types to Sikuli data types.  

## Basic Actions

Basic Actions are the fundamental building blocks of GUI automation in Brobot. They perform simple, atomic operations that typically require at most one Find operation.
Examples include:

- **Find Actions** - Locate images, text, or patterns on screen
- **Click Actions** - Single, double, or right clicks at specific locations
- **Type Actions** - Keyboard input and key combinations
- **Move Actions** - Mouse movements and hover operations

Each Basic Action is implemented as a separate class that implements the `ActionInterface`,
providing a clean, type-safe API through specific configuration classes like `PatternFindOptions`,
`ClickOptions`, and `TypeOptions`.

## Complex Actions

Complex Actions (formerly called Composite Actions) combine Basic Actions to create more sophisticated operations. These are useful for:

- **Multi-step Operations** - Actions requiring multiple Find operations
- **Conditional Behaviors** - Click until something appears/disappears
- **Drag Operations** - Click, hold, move, and release sequences
- **Scrolling** - Repeated scroll actions until target is found
- **Retry Logic** - Automatic retry with different strategies

In Brobot 1.1.0, Complex Actions are built by:
1. Chaining multiple Basic Actions together
2. Using `TaskSequence` for scripted sequences
3. Creating custom action classes that orchestrate Basic Actions

### Example: Click Until Pattern Appears

There are multiple ways to implement clickUntilFound in Brobot. Here are the most clean and efficient approaches:

#### Method 1: Traditional Loop Approach
```java
// Using individual actions with retry logic
public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
        // Click on target with pause after action
        ClickOptions click = new ClickOptions.Builder()
                .setPauseAfterEnd(1.0)  // 1 second pause after click
                .build();
        action.perform(click, new ObjectCollection.Builder()
                .withImages(clickTarget).build());
        
        // Check if pattern appeared
        PatternFindOptions find = PatternFindOptions.forQuickSearch();
        ActionResult result = action.perform(find, new ObjectCollection.Builder()
                .withImages(findTarget).build());
        
        if (result.isSuccess()) {
            return true;
        }
    }
    return false;
}
```

#### Method 2: Fluent API with Action Chaining
```java
// Using the fluent API to chain click and find operations
public boolean clickUntilFoundFluent(StateImage clickTarget, StateImage findTarget) {
    // Create a chain that clicks and then looks for the target
    ClickOptions clickAndCheck = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking on " + clickTarget.getName() + "...")
            .withSuccessLog("Click executed")
            .setPauseAfterEnd(1.0)  // Wait after click
            .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Checking if " + findTarget.getName() + " appeared...")
                    .withSuccessLog(findTarget.getName() + " found!")
                    .withFailureLog(findTarget.getName() + " not yet visible")
                    .build())
            .setRepetition(new RepetitionOptions.Builder()
                    .setMaxTimesToRepeatActionSequence(10)  // Try up to 10 times
                    .setPauseBetweenActionSequences(0.5)    // Brief pause between attempts
                    .build())
            .build();
    
    // Execute the chained action with both images
    ObjectCollection targets = new ObjectCollection.Builder()
            .withImages(clickTarget, findTarget)
            .build();
    
    ActionResult result = action.perform(clickAndCheck, targets);
    return result.isSuccess();
}
```

#### Method 3: Using the Built-in ClickUntilOptions (Deprecated but Available)
```java
// Using Brobot's built-in ClickUntil composite action
public boolean clickUntilFoundBuiltIn(StateImage clickTarget, StateImage findTarget) {
    // Create ClickUntilOptions configured to click until objects appear
    ClickUntilOptions clickUntil = new ClickUntilOptions.Builder()
            .setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR)
            .withBeforeActionLog("Clicking until " + findTarget.getName() + " appears...")
            .withSuccessLog(findTarget.getName() + " appeared!")
            .withFailureLog("Timeout - " + findTarget.getName() + " did not appear")
            .setRepetition(new RepetitionOptions.Builder()
                    .setMaxTimesToRepeatActionSequence(10)
                    .setPauseBetweenActionSequences(1.0)
                    .build())
            .build();
    
    // Create ObjectCollections
    // If using 1 collection: clicks objects until they appear
    // If using 2 collections: clicks collection 1 until collection 2 appears
    ObjectCollection clickCollection = new ObjectCollection.Builder()
            .withImages(clickTarget)
            .build();
    ObjectCollection appearCollection = new ObjectCollection.Builder()
            .withImages(findTarget)
            .build();
    
    // Execute with two collections - click first until second appears
    ActionResult result = action.perform(clickUntil, clickCollection, appearCollection);
    return result.isSuccess();
}
```

#### Method 4: Creating a Reusable Click-Until-Found Function
```java
// Creating a clean, reusable function that combines the best approaches
public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, 
                               int maxAttempts, double pauseBetween) {
    // Use fluent chaining with automatic logging
    PatternFindOptions clickAndCheck = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for click target...")
            .withSuccessLog("Click target found")
            .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Clicking...")
                    .withSuccessLog("Clicked successfully")
                    .setPauseAfterEnd(pauseBetween)
                    .build())
            .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Checking if target appeared...")
                    .withSuccessLog("Target appeared!")
                    .withFailureLog("Target not yet visible")
                    .setSearchDuration(0.5) // Quick check
                    .build())
            .setRepetition(new RepetitionOptions.Builder()
                    .setMaxTimesToRepeatActionSequence(maxAttempts)
                    .setPauseBetweenActionSequences(0.5)
                    .build())
            .build();
    
    // Combine both images in one collection
    ObjectCollection targets = new ObjectCollection.Builder()
            .withImages(clickTarget, findTarget)
            .build();
    
    // Execute and check the final result
    ActionResult result = action.perform(clickAndCheck, targets);
    
    // The chain succeeds if the final find action succeeded
    return result.isSuccess() && result.getLastActionResult().isSuccess();
}

// Usage example:
boolean success = clickUntilFound(nextButton, finishButton, 10, 1.0);
```

### Choosing the Right Approach

| Method | When to Use | Pros | Cons |
|--------|------------|------|------|
| **Traditional Loop** | Simple cases, full control needed | Complete control over logic | More verbose, manual logging |
| **Fluent API Chaining** | Most common cases | Clean, automatic logging, easy to read | Requires understanding of chaining |
| **ClickUntilOptions** | Legacy code or specific composite needs | Built-in, handles edge cases | Deprecated, less flexible |
| **Reusable Function** | Multiple uses in codebase | DRY principle, consistent behavior | May need customization for edge cases |

### Best Practices for Click Until Found

1. **Use Logging**: The fluent API approach (Method 2) provides the best logging and debugging experience
2. **Set Reasonable Timeouts**: Always limit the number of attempts to prevent infinite loops
3. **Add Pauses**: Include pauses between clicks to allow the UI to respond
4. **Consider State Management**: In a state-based automation framework, clickUntilFound might trigger a state transition
5. **ObjectCollection Usage**: Remember that all actions in a chain share the same ObjectCollection

This modular approach makes it easy to create custom complex behaviors while maintaining the benefits of type safety and clear intent provided by the ActionConfig architecture.

## Enhanced Conditional Action Chaining

Brobot 1.1.0+ introduces the powerful `EnhancedConditionalActionChain` class for building sophisticated conditional execution flows. This provides a fluent API for creating complex action sequences with conditional branching, error handling, and retry logic.

> **Note:** The original `ConditionalActionChain` class is now deprecated. Use `EnhancedConditionalActionChain` for all new development.

### EnhancedConditionalActionChain Overview

The `EnhancedConditionalActionChain` class allows you to:
- Chain actions with conditional execution based on previous results
- Add fallback actions when primary actions fail
- Implement retry logic with different strategies
- Create branching workflows based on runtime conditions
- Handle errors gracefully with recovery actions

### Basic Conditional Chaining

```java
import io.github.jspinak.brobot.action.EnhancedConditionalActionChain;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.action.ActionChainOptions;

// Simple conditional chain: if login button found, click it and enter credentials
EnhancedConditionalActionChain loginChain = EnhancedConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFoundClick()
    .ifFoundDo(result -> {
        action.type(usernameField, username);
        action.type(passwordField, password);
        action.click(submitButton);
    })
    .ifNotFoundLog("Login button not found - may already be logged in");

ActionResult result = loginChain.perform(action, 
    new ObjectCollection.Builder()
        .withImages(loginButton)
        .build());
```

### Advanced Conditional Patterns

#### Pattern 1: Multi-Step Validation
```java
// Validate each step and proceed only if successful
EnhancedConditionalActionChain wizardChain = EnhancedConditionalActionChain
    .find(new PatternFindOptions.Builder().build())
    .ifFoundClick()  // Click step 1
    .then(new PatternFindOptions.Builder().build())  // Find step 2
    .ifFoundClick()  // Click step 2
    .then(new PatternFindOptions.Builder().build())  // Find step 3
    .ifFoundClick()  // Click step 3
    .then(new PatternFindOptions.Builder()  // Find completion
        .setSearchDuration(5.0)  // Wait up to 5 seconds
        .build())
    .ifFoundLog("Wizard completed successfully")
    .ifNotFoundLog("Wizard failed to complete");

// Execute with all step buttons
ObjectCollection steps = new ObjectCollection.Builder()
    .withImages(step1Button, step2Button, step3Button, completionMessage)
    .build();
    
ActionResult result = wizardChain.perform(action, steps);
```

#### Pattern 2: Retry with Different Strategies
```java
// Try different approaches to close a dialog
// Method 1: Try X button first
ConditionalActionChain closeWithX = ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())  // Find close button
    .ifFoundClick()
    .ifFoundLog("Closed dialog with X button");

// Method 2: Try Escape key if X button fails
ConditionalActionChain closeWithEsc = ConditionalActionChain
    .start(new TypeOptions.Builder()
        .setText("\u001B")  // ESC key
        .build())
    .always(new PatternFindOptions.Builder()  // Check if dialog gone
        .setSearchDuration(1.0)
        .build())
    .ifNotFoundLog("Dialog closed with ESC key");

// Method 3: Click outside dialog
ConditionalActionChain closeWithClick = ConditionalActionChain
    .start(new ClickOptions.Builder()
        .setClickLocation(ClickOptions.ClickLocation.OUTSIDE_MATCH)
        .build())
    .then(new PatternFindOptions.Builder()  // Verify dialog closed
        .setSearchDuration(1.0)
        .build())
    .ifNotFoundLog("Dialog closed by clicking outside");

// Use RepetitionOptions for retry logic
PatternFindOptions findDialog = new PatternFindOptions.Builder()
    .setRepetition(new RepetitionOptions.Builder()
        .setMaxTimesToRepeatActionSequence(3)
        .setPauseBetweenActionSequences(0.5)
        .build())
    .build();
```

#### Pattern 3: Conditional Branching Based on Application State
```java
// Different actions based on what's visible on screen
// Since ConditionalActionChain doesn't have elseWhen, use separate chains

// Chain for home screen
ConditionalActionChain fromHome = ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())  // Find home screen
    .ifFoundClick()  // Click menu button
    .then(new PatternFindOptions.Builder().build())  // Find menu panel
    .ifFoundLog("Opened menu from home screen");

// Chain for settings screen  
ConditionalActionChain fromSettings = ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())  // Find settings screen
    .ifFoundClick()  // Click back button
    .then(new PatternFindOptions.Builder().build())  // Find home screen
    .ifFoundLog("Returned to home from settings");

// Chain for error dialog
ConditionalActionChain fromError = ConditionalActionChain
    .find(new PatternFindOptions.Builder().build())  // Find error dialog
    .ifFoundClick()  // Click dismiss
    .alwaysClick()  // Click home button
    .then(new PatternFindOptions.Builder().build())  // Verify home screen
    .ifFoundLog("Recovered from error state");

// Execute appropriate chain based on current state
ActionResult result;
if (action.find(homeScreen).isSuccess()) {
    result = fromHome.perform(action, new ObjectCollection.Builder()
        .withImages(homeScreen, menuButton, menuPanel).build());
} else if (action.find(settingsScreen).isSuccess()) {
    result = fromSettings.perform(action, new ObjectCollection.Builder()
        .withImages(settingsScreen, backButton, homeScreen).build());
} else if (action.find(errorDialog).isSuccess()) {
    result = fromError.perform(action, new ObjectCollection.Builder()
        .withImages(errorDialog, dismissButton, homeButton, homeScreen).build());
} else {
    log.warn("Unknown state - attempting recovery");
    action.type(new ObjectCollection.Builder().withStrings("\u001B\u001B").build());
}
```

### Integration with ActionChainOptions

Conditional chains can be combined with `ActionChainOptions` for even more sophisticated behaviors:

```java
// Combine ActionChainOptions with ConditionalActionChain
// First, use ActionChainOptions for nested/confirmed finds
ActionChainOptions nestedFind = new ActionChainOptions.Builder(
        new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.9)
            .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(new ColorFindOptions.Builder()
        .setColorStrategy(ColorFindOptions.Color.MU)
        .setSimilarity(0.85)
        .build())
    .build();

// Then use ConditionalActionChain for conditional logic
ConditionalActionChain robustClick = ConditionalActionChain
    .start(nestedFind)  // Start with the chained find
    .ifFoundClick()  // Click if found
    .ifNotFoundDo(result -> {
        // Fallback: try with lower similarity
        PatternFindOptions relaxedFind = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.7)
            .build();
        
        ActionResult fallbackResult = action.perform(relaxedFind, targetImage);
        if (fallbackResult.isSuccess()) {
            action.click(targetImage);
        }
    });

// For retry logic, use RepetitionOptions
PatternFindOptions withRetry = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setRepetition(new RepetitionOptions.Builder()
        .setMaxTimesToRepeatActionSequence(2)
        .setPauseBetweenActionSequences(1.0)
        .build())
    .build();
```

### Comparison: ActionChainOptions vs ConditionalActionChain

| Feature | ActionChainOptions | ConditionalActionChain |
|---------|-------------------|----------------------|
| **Purpose** | Chain actions with NESTED/CONFIRM strategies | Complex conditional workflows |
| **Conditional Logic** | Limited (success/failure) | Full boolean conditions |
| **Branching** | No | Yes (if/else/elseWhen) |
| **Custom Logic** | No | Yes (lambda expressions) |
| **Retry Handling** | Via RepetitionOptions | Built-in retry with delays |
| **Use Case** | Pattern/color combining | Complex decision trees |

### When to Use Each Approach

**Use ActionChainOptions when:**
- Combining find operations (nested or confirmed)
- Performing sequential actions on the same objects
- Need automatic history recording
- Working with pattern and color matching

**Use ConditionalActionChain when:**
- Need complex conditional logic
- Implementing recovery strategies
- Creating branching workflows
- Need custom validation between steps
- Building robust error handling

### Best Practices for Conditional Chaining

1. **Keep Chains Focused**: Each chain should have a single, clear purpose
2. **Use Meaningful Names**: Name your chains based on what they accomplish
3. **Add Logging**: Include log statements in your conditions and actions
4. **Set Reasonable Timeouts**: Prevent chains from running indefinitely
5. **Test Edge Cases**: Ensure your fallback actions handle all scenarios
6. **Document Complex Logic**: Add comments explaining the flow
7. **Consider State Management**: Chains may trigger state transitions

### Example: Complete Form Filling with Validation

```java
public class FormAutomation {
    
    @Autowired
    private Action action;
    
    public boolean fillComplexForm(FormData data) {
        // Check if form is open
        ConditionalActionChain openForm = ConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifNotFoundLog("Form not found")
            .ifNotFoundDo(result -> log.error("Cannot proceed - form not visible"));
        
        ActionResult formResult = openForm.perform(action, 
            new ObjectCollection.Builder().withImages(formTitle).build());
        
        if (!formResult.isSuccess()) {
            return false;
        }
        
        // Fill required fields
        boolean requiredFilled = fillRequiredFields(data);
        if (!requiredFilled) {
            log.error("Failed to fill required fields");
            return false;
        }
        
        // Conditionally fill optional fields
        if (data.hasOptionalData()) {
            fillOptionalFields(data);
        }
        
        // Validate before submission
        ConditionalActionChain validateChain = ConditionalActionChain
            .start(new ClickOptions.Builder().build())  // Click validate
            .then(new PatternFindOptions.Builder()
                .setSearchDuration(2.0)  // Wait for validation
                .build())
            .ifFoundDo(result -> {
                // If error message found, handle it
                String errorText = action.text(errorMessage).getText();
                log.error("Validation error: {}", errorText);
                fixValidationError(errorText);
            })
            .ifNotFoundLog("Validation passed");
        
        ActionResult validateResult = validateChain.perform(action,
            new ObjectCollection.Builder()
                .withImages(validateButton, errorMessage)
                .build());
        
        // Submit if valid
        if (!action.find(errorMessage).isSuccess()) {
            ConditionalActionChain submitChain = ConditionalActionChain
                .start(new ClickOptions.Builder().build())
                .then(new PatternFindOptions.Builder()
                    .setSearchDuration(5.0)
                    .build())
                .ifFoundLog("Form submitted successfully");
            
            ActionResult submitResult = submitChain.perform(action,
                new ObjectCollection.Builder()
                    .withImages(submitButton, successMessage)
                    .build());
            
            return submitResult.isSuccess();
        }
        
        // Cancel if errors persist
        action.click(cancelButton);
        return false;
    }
}
```

### Related Documentation

- **[Conditional Action Chaining Guide](/docs/ai-brobot-project-creation#conditionalactionchain---the-foundation)** - Comprehensive guide with more examples
- **[Action Config Factory](../03-core-library/guides/action-config-factory.md)** - Creating and managing action configurations
- **[Combining Finds](../03-core-library/guides/finding-objects/combining-finds.md)** - Pattern and color combination strategies
- **[ConditionalActionChain Example](https://github.com/jspinak/brobot/tree/main/examples/03-core-library/action-config/conditional-chains-examples)** - Complete implementation examples