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
public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, int maxAttempts) {
    ClickOptions click = new ClickOptions.Builder()
            .setPauseAfterEnd(1.0)
            .build();

    ObjectCollection clickCollection = new ObjectCollection.Builder()
            .withImages(clickTarget).build();

    ObjectCollection findCollection = new ObjectCollection.Builder()
            .withImages(findTarget).build();

    for (int i = 0; i < maxAttempts; i++) {
        action.perform(click, clickCollection);
        ActionResult result = action.perform(PatternFindOptions.forQuickSearch(), findCollection);

        if (result.isSuccess()) {
            return true;
        }
    }
    return false;
}
```

#### Method 2: Fluent API with Action Chaining
```java
public boolean clickUntilFoundFluent(StateImage clickTarget, StateImage findTarget) {
    ClickOptions clickWithVerify = new ClickOptions.Builder()
        .setVerification(new VerificationOptions.Builder()
            .addVerifyImage(findTarget)  // Check if this appears
            .build())
        .setRepetition(new RepetitionOptions.Builder()
            .setMaxTimesToRepeatActionSequence(10)
            .setPauseBetweenActionSequences(0.5)
            .build())
        .build();

    ObjectCollection targets = new ObjectCollection.Builder()
        .withImages(clickTarget, findTarget)
        .build();

    ActionResult result = action.perform(clickWithVerify, targets);
    return result.isSuccess();
}
```

#### Method 3: Using ConditionalActionChain
```java
// Direct usage with ConditionalActionChain
ConditionalActionChain clickUntilFound = ConditionalActionChain
    .find(nextButton)
    .ifFoundClick()
    .then(new PatternFindOptions.Builder()
        .setRepetition(new RepetitionOptions.Builder()
            .setMaxTimesToRepeatActionSequence(10)
            .setPauseBetweenActionSequences(1.0)
            .build())
        .build())
    .withObjectCollection(new ObjectCollection.Builder()
        .withImages(finishButton)
        .build());

ActionResult result = clickUntilFound.perform(action);
```

#### Method 4: Creating a Reusable Click-Until-Found Function
```java
// Creating a clean, reusable function using ConditionalActionChain
public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, 
                                int maxAttempts, double pauseBetween) {

    // Use RepetitionOptions on the find action for retry logic
    PatternFindOptions findWithRetry = new PatternFindOptions.Builder()
        .setRepetition(new RepetitionOptions.Builder()
            .setMaxTimesToRepeatActionSequence(maxAttempts)
            .setPauseBetweenActionSequences(pauseBetween)
            .build())
        .build();

    // Build the conditional chain
    ConditionalActionChain clickAndCheck = ConditionalActionChain
        .find(clickTarget)
        .ifFoundClick()
        .then(findWithRetry)
        .withObjectCollection(new ObjectCollection.Builder()
            .withImages(findTarget)
            .build());

    // Execute and check the final result
    ActionResult result = clickAndCheck.perform(action);
    return result.isSuccess();
}

// Usage example:
boolean success = clickUntilFound(nextButton, finishButton, 10, 1.0);
```

### Choosing the Right Approach

| Method | When to Use | Pros | Cons |
|--------|------------|------|------|
| **Traditional Loop** | Simple cases, full control needed | Complete control over logic | More verbose, manual logging |
| **Fluent API Chaining** | Most common cases | Clean, automatic logging, easy to read | Requires understanding of chaining |
| **Reusable Function** | Multiple uses in codebase | DRY principle, consistent behavior | May need customization for edge cases |

### Best Practices for Click Until Found

1. **Use Logging**: The fluent API approach (Method 2) provides the best logging and debugging experience
2. **Set Reasonable Timeouts**: Set the number of attempts to balance search time and robustness
3. **Add Pauses**: Include pauses between clicks to allow the UI to respond
4. **Consider State Management**: Since it includes Find operations, clickUntilFound might add a new active state

This modular approach makes it easy to create custom complex behaviors while maintaining the benefits of type safety and clear intent provided by the ActionConfig architecture.

## Conditional Action Chaining

Brobot 1.1.0+ introduces the powerful `ConditionalActionChain` class for building sophisticated conditional execution flows. This provides a fluent API for creating complex action sequences with conditional branching, error handling, and retry logic.

### ConditionalActionChain Overview

The `ConditionalActionChain` class allows you to:
- Chain actions with conditional execution based on previous results
- Add fallback actions when primary actions fail
- Implement retry logic with different strategies
- Create branching workflows based on runtime conditions
- Handle errors gracefully with recovery actions

### Basic Conditional Chaining

```java
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ActionResult;

// Simple conditional chain: if login button found, click it and enter credentials
ConditionalActionChain loginChain = ConditionalActionChain
    .find(loginButton)      // Pass StateImage directly
    .ifFoundClick()         // Click the login button
    .then(usernameField)    // Find username field
    .ifFoundType(username)  // Type username (string)
    .then(passwordField)    // Find password field
    .ifFoundType(password)  // Type password (string)
    .then(submitButton)     // Find submit button
    .ifFoundClick()         // Click it
    .ifNotFoundLog("Login button not found - may already be logged in");

ActionResult result = loginChain.perform(action);  // No ObjectCollection needed
```
Or if you want to use the custom handler approach:
```java 
ConditionalActionChain loginChain = ConditionalActionChain
    .find(loginButton)
    .ifFoundClick()
    .ifFoundDo(result -> {
        action.click(usernameField);
        action.type(username);
        action.click(passwordField);
        action.type(password);
        action.click(submitButton);
    })
    .ifNotFoundLog("Login button not found");
```

### Advanced Conditional Patterns

#### Pattern 1: Multi-Step Validation
```java
// Validate each step and proceed only if successful
ConditionalActionChain wizardChain = ConditionalActionChain
    .find(step1Button)  // Just pass the StateImage
    .ifFoundClick()
    .then(step2Button)
    .ifFoundClick()
    .then(step3Button)
    .ifFoundClick()
    .then(completionMessage)  // Use then() with StateImage
    .ifFoundLog("Wizard completed successfully")
    .ifNotFoundLog("Wizard failed to complete");

// Execute
ActionResult result = wizardChain.perform(action);
```

Or if you need custom options for specific steps, you need to use the internal pattern:
```java
// If you need custom search duration for completion
ConditionalActionChain wizardChain = ConditionalActionChain
    .find(step1Button)
    .ifFoundClick()
    .then(step2Button)
    .ifFoundClick()
    .then(step3Button)
    .ifFoundClick()
    .then(new PatternFindOptions.Builder()
        .setSearchDuration(5.0)
        .build())
    .withObjectCollection(new ObjectCollection.Builder()
        .withImages(completionMessage)
        .build())  // Must manually attach the image
    .ifFoundLog("Wizard completed successfully")
    .ifNotFoundLog("Wizard failed to complete");

ActionResult result = wizardChain.perform(action);
```

find() and then() methods accept either PatternFindOptions OR StateImage, but not both as separate parameters.
When using options, you must manually attach the ObjectCollection with .withObjectCollection().


#### Pattern 2: Retry with Different Strategies
```java
// Try different approaches to close a dialog
// Method 1: Try X button first
ConditionalActionChain closeWithX = ConditionalActionChain
    .find(closeButton)  // Pass the StateImage directly
    .ifFoundClick()
    .ifFoundLog("Closed dialog with X button");

// Method 2: Try Escape key if X button fails  
ConditionalActionChain closeWithEsc = ConditionalActionChain
    .pressEscape()  // Use the convenience method
    .then(dialogImage)  // Check if dialog gone
    .ifNotFoundLog("Dialog closed with ESC key");

// Or using start():
ConditionalActionChain closeWithEscAlt = ConditionalActionChain
    .start(new TypeOptions.Builder().build())
    .withObjectCollection(new ObjectCollection.Builder()
        .withStrings(org.sikuli.script.Key.ESC)  // Pass Key.ESC as string
        .build())
    .then(dialogImage)
    .ifNotFoundLog("Dialog closed with ESC key");

// Method 3: Click at a screen location to dismiss
ConditionalActionChain closeWithClick = ConditionalActionChain
    .start(new ClickOptions.Builder().build())
    .withObjectCollection(new ObjectCollection.Builder()
        .withLocations(new Location(10, 10))  // Click at specific location
        .build())
    .then(dialogImage)
    .ifNotFoundLog("Dialog closed by clicking outside");

// Execute each chain with the Action instance
ActionResult result1 = closeWithX.perform(action);
ActionResult result2 = closeWithEsc.perform(action);
ActionResult result3 = closeWithClick.perform(action);
```

#### Pattern 3: Conditional Branching Based on Application State
```java
// Different actions based on what's visible on screen
// Since ConditionalActionChain doesn't have elseWhen, use separate chains

// Chain for home screen
ConditionalActionChain fromHome = ConditionalActionChain
    .find(homeScreen)       // Find home screen (pass StateImage directly)
    .then(menuButton)       // Find menu button
    .ifFoundClick()         // Click it
    .then(menuPanel)        // Find menu panel
    .ifFoundLog("Opened menu from home screen");

// Chain for settings screen  
ConditionalActionChain fromSettings = ConditionalActionChain
    .find(settingsScreen)   // Find settings screen
    .then(backButton)       // Find back button
    .ifFoundClick()         // Click it
    .then(homeScreen)       // Find home screen
    .ifFoundLog("Returned to home from settings");

// Chain for error dialog
ConditionalActionChain fromError = ConditionalActionChain
    .find(errorDialog)      // Find error dialog
    .then(dismissButton)    // Find dismiss button
    .ifFoundClick()         // Click dismiss
    .then(homeButton)       // Find home button
    .ifFoundClick()         // Click home button
    .then(homeScreen)       // Verify home screen
    .ifFoundLog("Recovered from error state");

// Execute appropriate chain based on current state
ActionResult result;
if (action.find(homeScreen).isSuccess()) {
    result = fromHome.perform(action);
} else if (action.find(settingsScreen).isSuccess()) {
    result = fromSettings.perform(action);
} else if (action.find(errorDialog).isSuccess()) {
    result = fromError.perform(action);
} else {
    log.warn("Unknown state - attempting recovery");
    action.pressEscape();
    action.pressEscape();
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
    .ifFoundClick()     // Click if found
    .ifNotFoundDo(result -> {
        // Fallback: try with lower similarity
        PatternFindOptions relaxedFind = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.7)
            .build();

        ObjectCollection targetCollection = new ObjectCollection.Builder()
            .withImages(targetImage)
            .build();

        ActionResult fallbackResult = action.perform(relaxedFind, targetCollection);
        if (fallbackResult.isSuccess()) {
            action.click(targetImage);
        }
    });

// Execute the chain (needs ObjectCollection with target image)
ObjectCollection targets = new ObjectCollection.Builder()
    .withImages(targetImage)
    .build();
ActionResult result = robustClick.perform(action, targets);

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

| Feature           | ActionChainOptions                             | ConditionalActionChain                              |
|-------------------|------------------------------------------------|-----------------------------------------------------|
| Purpose           | Chain actions with NESTED/CONFIRM strategies   | Complex conditional workflows                       |
| Conditional Logic | Limited (success/failure)                      | Full boolean conditions                             |
| Branching         | No                                             | Partial (if/ifNot, no elseWhen)                     |
| Custom Logic      | No                                             | Yes (lambda expressions via ifFoundDo/ifNotFoundDo) |
| Retry Handling    | Via RepetitionOptions                          | Via static retry() method or manual loops           |
| Use Case          | Pattern/color combining, hierarchical searches | Complex decision trees, recovery workflows          |

### When to Use Each Approach

**Use ActionChainOptions when:**
- Combining find operations (nested or confirmed)
- Performing sequential actions on the same objects
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
7. **Consider State Management**: Chains may trigger state activations

### Example: Complete Form Filling with Validation

```java
@Slf4j  // Automatically creates: private static final Logger log
@Component
public class FormAutomation {

    @Autowired
    private Action action;

    public boolean fillComplexForm(FormData data) {
        // Check if form is open
        ConditionalActionChain openForm = ConditionalActionChain
            .find(formTitle)  // Pass StateImage directly
            .ifNotFoundLog("Form not found")
            .ifNotFoundDo(result -> log.error("Cannot proceed - form not visible"));

        ActionResult formResult = openForm.perform(action);  // No ObjectCollection needed

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
            .find(validateButton)  // Find validate button
            .ifFoundClick()        // Click it
            .then(new PatternFindOptions.Builder()
                .setSearchDuration(2.0)  // Wait for validation
                .build())
            .withObjectCollection(new ObjectCollection.Builder()
                .withImages(errorMessage)  // Look for error message
                .build())
            .ifFoundDo(result -> {
                // If error message found, handle it
                ActionResult textResult = action.find(errorMessage);
                if (textResult.isSuccess() && !textResult.getMatches().isEmpty()) {
                    // Extract text from the error (if it's a text element)
                    log.error("Validation error found");
                    fixValidationError("validation_error");
                }
            })
            .ifNotFoundLog("Validation passed");

        ActionResult validateResult = validateChain.perform(action);

        // Submit if valid
        if (!action.find(errorMessage).isSuccess()) {
            ConditionalActionChain submitChain = ConditionalActionChain
                .find(submitButton)    // Find submit button
                .ifFoundClick()        // Click it
                .then(new PatternFindOptions.Builder()
                    .setSearchDuration(5.0)
                    .build())
                .withObjectCollection(new ObjectCollection.Builder()
                    .withImages(successMessage)
                    .build())
                .ifFoundLog("Form submitted successfully");

            ActionResult submitResult = submitChain.perform(action);

            return submitResult.isSuccess();
        }

        // Cancel if errors persist
        action.click(cancelButton);
        return false;
    }

    // Helper methods (stubs)
    private boolean fillRequiredFields(FormData data) { return true; }
    private void fillOptionalFields(FormData data) {}
    private void fixValidationError(String error) {}
}
```

### Related Documentation

- **[Conditional Action Chaining Guide](ai-brobot-project-creation#conditional-action-chains)** - Comprehensive guide with more examples
- **[Action Config Factory](../03-core-library/guides/configuration/action-config-factory.md)** - Creating and managing action configurations
- **[Combining Finds](../03-core-library/guides/finding-objects/combining-finds.md)** - Pattern and color combination strategies
- **[ConditionalActionChain Example](https://github.com/jspinak/brobot/tree/main/examples/03-core-library/action-config/conditional-chains-examples)** - Complete implementation examples