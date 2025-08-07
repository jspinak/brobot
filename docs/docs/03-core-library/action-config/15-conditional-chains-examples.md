# Enhanced Conditional Action Chains

## Overview

`EnhancedConditionalActionChain` provides a powerful fluent API for building complex action sequences with conditional execution. This enhanced implementation adds the missing features from the original design, including the crucial `then()` method for sequential composition and numerous convenience methods.

## Key Features

- **Sequential Composition**: The `then()` method enables multi-step workflows
- **Convenience Methods**: Direct methods like `click()`, `type()`, `scrollDown()`
- **Keyboard Shortcuts**: Built-in support for common key combinations
- **Control Flow**: Methods for stopping chains, retrying, and error handling
- **No Explicit Waits**: Follows model-based principles - timing via action configurations
- **Proper Conditional Logic**: True if/then/else execution flow

## Basic Examples

### Simple Find and Click

```java
// Basic find and click pattern
EnhancedConditionalActionChain.find(buttonImage)
    .ifFoundClick()
    .ifNotFoundLog("Button not found")
    .perform(action, new ObjectCollection.Builder().build());
```

### Sequential Actions with then()

```java
// The then() method enables sequential workflows
EnhancedConditionalActionChain.find(menuButton)
    .ifFoundClick()
    .then(searchField)  // Move to next element
    .ifFoundClick()
    .ifFoundType("search query")
    .then(submitButton) // Continue the flow
    .ifFoundClick()
    .perform(action, new ObjectCollection.Builder().build());
```

## Real-World Scenarios

### Login Flow

```java
public ActionResult performLogin(String username, String password) {
    return EnhancedConditionalActionChain.find(loginButton)
        .ifFoundClick()
        .ifNotFoundLog("Login button not visible")
        .then(usernameField)  // Sequential action using then()
        .ifFoundClick()
        .ifFoundType(username)
        .then(passwordField)  // Continue to next field
        .ifFoundClick()
        .ifFoundType(password)
        .then(submitButton)   // Move to submit
        .ifFoundClick()
        .then(successMessage) // Check for success
        .ifFoundLog("Login successful!")
        .ifNotFoundLog("Login might have failed")
        .perform(action, new ObjectCollection.Builder().build());
}
```

### Save with Confirmation Dialog

```java
public ActionResult saveWithConfirmation() {
    StateImage saveButton = new StateImage.Builder()
        .addPattern("images/buttons/save.png")
        .build();
    
    return EnhancedConditionalActionChain.find(saveButton)
        .ifFoundClick()
        .ifNotFoundLog("Save button not found")
        .then(confirmDialog)  // Look for confirmation
        .then(yesButton)      // Find yes button within dialog
        .ifFoundClick()
        .ifNotFoundLog("No confirmation needed")
        .then(successMessage) // Verify success
        .ifFoundLog("Save successful")
        .ifNotFoundLog("Save may have failed")
        .perform(action, new ObjectCollection.Builder().build());
}
```

### Retry Pattern

```java
public ActionResult clickWithRetry(StateImage target, int maxRetries) {
    return EnhancedConditionalActionChain
        .retry(new PatternFindOptions.Builder().build(), maxRetries)
        .ifFoundClick()
        .ifFoundLog("Successfully clicked after retries")
        .ifNotFoundLog("Failed after all attempts")
        .perform(action, new ObjectCollection.Builder()
            .withImages(target)
            .build());
}
```

## Advanced Patterns

### Multi-Step Form Filling

```java
public ActionResult fillComplexForm(FormData data) {
    return EnhancedConditionalActionChain
        .find(new PatternFindOptions.Builder().build())
        .ifNotFoundLog("Form not visible")
        .ifNotFoundDo(res -> { throw new RuntimeException("Cannot proceed without form"); })
        
        // Name field - using clearAndType
        .then(nameField)
        .ifFoundClick()
        .clearAndType(data.getName())
        
        // Email field - using tab navigation
        .pressTab()
        .type(data.getEmail())
        
        // Phone field - using direct navigation
        .then(phoneField)
        .ifFoundClick()
        .ifFoundType(data.getPhone())
        
        // Submit form
        .then(submitButton)
        .ifFoundClick()
        .takeScreenshot("form-submission")
        .perform(action, new ObjectCollection.Builder().build());
}
```

### Dynamic UI Navigation with Scrolling

```java
public ActionResult scrollToFind(StateImage target) {
    return EnhancedConditionalActionChain.find(target)
        .ifNotFound(chain -> chain.scrollDown())
        .ifNotFound(new PatternFindOptions.Builder().build())
        .ifNotFound(chain -> chain.scrollDown())
        .ifNotFound(new PatternFindOptions.Builder().build())
        .ifNotFound(chain -> chain.scrollDown())
        .ifFoundClick()
        .ifFoundLog("Found and clicked target after scrolling")
        .ifNotFoundLog("Could not find target even after scrolling")
        .perform(action, new ObjectCollection.Builder()
            .withImages(target)
            .build());
}
```

### Keyboard Shortcuts Workflow

```java
public ActionResult useKeyboardShortcuts() {
    return EnhancedConditionalActionChain
        .find(editorField)
        .ifFoundClick()
        .pressCtrlA()      // Select all
        .pressDelete()     // Delete content
        .type("New content here")
        .pressCtrlS()      // Save
        .then(savedIndicator)
        .ifFoundLog("Document saved successfully")
        .perform(action, new ObjectCollection.Builder().build());
}
```

## Conditional Patterns

### Error Handling with Control Flow

```java
public ActionResult handleErrors() {
    return EnhancedConditionalActionChain
        .find(submitButton)
        .ifFoundClick()
        .then(errorDialog)
        .ifFoundLog("Error dialog appeared")
        .ifFound(chain -> chain.takeScreenshot("error-state"))
        .ifFoundDo(res -> {
            log.error("Operation failed with error: {}", res.getText());
        })
        .stopIf(res -> res.getText() != null && 
                !res.getText().isEmpty() && 
                res.getText().get(0).contains("CRITICAL"))
        .then(retryButton)
        .ifFoundClick()
        .ifFoundLog("Retrying operation")
        .perform(action, new ObjectCollection.Builder().build());
}
```

### Wait for Element to Disappear

```java
public ActionResult waitForLoadingToComplete() {
    StateImage loadingSpinner = new StateImage.Builder()
        .addPattern("images/indicators/loading.png")
        .build();
    
    return EnhancedConditionalActionChain
        .find(submitButton)
        .ifFoundClick()
        .waitVanish(loadingSpinner)  // Wait for spinner to disappear
        .then(successMessage)
        .ifFoundLog("Operation completed successfully")
        .then(errorDialog)
        .ifFoundLog("Operation failed")
        .perform(action, new ObjectCollection.Builder().build());
}
```

### Highlighting and Debugging

```java
public ActionResult debugWorkflow() {
    return EnhancedConditionalActionChain
        .find(targetElement)
        .ifFound(chain -> chain.highlight())  // Highlight found element
        .ifFoundLog("Found target element")   // Log for debugging
        .takeScreenshot("debug-1")            // Take screenshot
        .ifFoundClick()
        .takeScreenshot("debug-2")            // Another screenshot
        .perform(action, new ObjectCollection.Builder().build());
}
```

## Model-Based Automation Principles

### No Explicit Waits

Unlike process-based automation, EnhancedConditionalActionChain does **not** include a `wait()` method. This is intentional:

```java
// WRONG - Process-based approach with explicit waits
chain.click().wait(2.0).type("text")  // Don't do this!

// CORRECT - Model-based approach with action configurations
PatternFindOptions findWithDelay = new PatternFindOptions.Builder()
    .setPauseBeforeBegin(2.0)  // Timing in action configuration
    .build();
    
EnhancedConditionalActionChain.find(findWithDelay)
    .ifFoundClick()
    .then(new TypeOptions.Builder()
        .setTypeDelay(0.1)  // Type-specific timing
        .build())
    .perform(action, objectCollection);
```

### State-Based Navigation

```java
public ActionResult navigateToState(State targetState) {
    // Focus on states, not processes
    return EnhancedConditionalActionChain
        .find(targetState.getIdentifyingImage())
        .ifFoundLog("Already in target state")
        .ifNotFound(navigationButton)
        .ifFoundClick()
        .then(targetState.getIdentifyingImage())
        .ifFoundLog("Successfully navigated to state")
        .ifNotFoundDo(res -> {
            throw new StateTransitionException("Failed to reach target state");
        })
        .perform(action, new ObjectCollection.Builder().build());
}
```

## Testing Patterns

### Mock-Friendly Chains

```java
@Test
public void testEnhancedChainFeatures() {
    // Setup mock
    Action mockAction = mock(Action.class);
    ActionResult successResult = new ActionResult();
    successResult.setSuccess(true);
    
    when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
        .thenReturn(successResult);
    
    // Test the then() method
    ActionResult result = EnhancedConditionalActionChain
        .find(loginButton)
        .ifFoundClick()
        .then(usernameField)  // Sequential composition
        .ifFoundType("testuser")
        .then(passwordField)  // Continue flow
        .ifFoundType("password")
        .perform(mockAction, new ObjectCollection.Builder().build());
    
    assertTrue(result.isSuccess());
}
```

## Complete API Reference

### Core Methods
- `find(PatternFindOptions)` / `find(StateImage)` - Start chain with find
- `then(ActionConfig)` / `then(StateImage)` - Sequential action composition
- `ifFound(ActionConfig)` - Execute if previous succeeded
- `ifNotFound(ActionConfig)` - Execute if previous failed
- `always(ActionConfig)` - Execute regardless

### Convenience Methods
- `click()` / `ifFoundClick()` - Click actions
- `type(String)` / `ifFoundType(String)` - Type text
- `clearAndType(String)` - Clear field and type
- `scrollDown()` / `scrollUp()` - Scroll actions
- `highlight()` - Highlight last found element
- `waitVanish(StateImage)` - Wait for element to disappear

### Keyboard Shortcuts
- `pressEnter()`, `pressTab()`, `pressEscape()`
- `pressCtrlS()`, `pressCtrlA()`, `pressDelete()`
- `pressKey(int keyCode)` - Press specific key
- `pressKeyCombo(int modifier, int key)` - Key combinations

### Control Flow
- `stopChain()` - Stop execution
- `stopIf(Predicate<ActionResult>)` - Conditional stop
- `retry(ActionConfig, int)` - Retry pattern
- `throwError(String)` - Throw exception

### Logging & Debugging
- `log(String)` - Log message
- `ifFoundLog(String)` / `ifNotFoundLog(String)` - Conditional logging
- `takeScreenshot(String)` - Capture screenshot

### Custom Handlers
- `ifFoundDo(Consumer<ActionResult>)` - Custom success handler
- `ifNotFoundDo(Consumer<ActionResult>)` - Custom failure handler
- `ifFound(Consumer<EnhancedConditionalActionChain>)` - Chain operations
- `ifNotFound(Consumer<EnhancedConditionalActionChain>)` - Chain operations

## Best Practices

1. **Use then() for Sequential Actions**: The then() method is essential for multi-step workflows
2. **No Explicit Waits**: Use action configurations for timing, not wait() calls
3. **Leverage Convenience Methods**: Use built-in methods like click() and type()
4. **Add Logging**: Use ifFoundLog/ifNotFoundLog for debugging
5. **Handle Failures**: Always provide ifNotFound alternatives
6. **Keep Chains Focused**: Break complex workflows into smaller methods
7. **Think States, Not Processes**: Focus on state transitions, not step-by-step procedures

## Migration from Basic ConditionalActionChain

The original ConditionalActionChain was limited. Here's how to migrate:

```java
// OLD - Limited ConditionalActionChain (no then() method!)
ConditionalActionChain.find(button)
    .ifFound(click())
    // Can't continue to next element without then()!
    
// NEW - EnhancedConditionalActionChain
EnhancedConditionalActionChain.find(button)
    .ifFoundClick()
    .then(nextElement)  // Now you can continue!
    .ifFoundClick()
    .then(anotherElement)  // And continue further!
    .ifFoundType("text")
```

## Common Pitfalls to Avoid

1. **Don't Use Explicit Waits**: No wait() method by design - use action configurations
2. **Don't Forget then()**: Use then() to move between different elements
3. **Don't Mix APIs**: Use EnhancedConditionalActionChain, not the basic version
4. **Don't Ignore State**: Think in terms of application states, not process steps
5. **Don't Skip Error Handling**: Always handle the ifNotFound case