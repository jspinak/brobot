# Conditional Action Chains Examples

## Overview

ConditionalActionChain provides a fluent API for building complex action sequences with conditional execution. This page demonstrates common patterns and real-world examples.

## Basic Examples

### Simple Find and Click

```java
// Basic find and click pattern
ConditionalActionChain.find(new PatternFindOptions.Builder().build())
    .ifFound(new ClickOptions.Builder().build())
    .perform(action, new ObjectCollection.Builder()
        .withImages(buttonImage)
        .build());
```

### Find with Error Handling

```java
ConditionalActionChain.find(findOptions)
    .ifFound(click())
    .ifNotFoundLog("Critical button not found!")
    .ifNotFoundDo(result -> {
        // Custom error handling
        alertUser("UI element missing");
        takeDebugScreenshot();
    })
    .perform(action, objectCollection);
```

## Real-World Scenarios

### Login Flow

```java
public ActionResult performLogin(String username, String password) {
    return ConditionalActionChain.find(loginButton)
        .ifFound(click())
        .ifNotFoundLog("Login button not visible")
        .ifNotFound(find(menuButton))
        .ifFound(click())
        .always(wait(1.0))
        .then(find(usernameField))
        .ifFound(click())
        .ifFound(type(username))
        .then(find(passwordField))
        .ifFound(click())
        .ifFound(type(password))
        .then(find(submitButton))
        .ifFound(click())
        .always(wait(2.0))
        .perform(action, objectCollection);
}
```

### Save with Confirmation Dialog

```java
public ActionResult saveWithConfirmation() {
    return ConditionalActionChain.find(saveButton)
        .ifFound(click())
        .ifNotFoundLog("Save button not found")
        .ifFound(wait(0.5))
        .then(find(confirmDialog))
        .ifFound(find(yesButton))
        .ifFound(click())
        .ifNotFound(log("No confirmation needed"))
        .always(wait(1.0))
        .then(find(successMessage))
        .ifFoundLog("Save successful")
        .ifNotFoundLog("Save may have failed")
        .perform(action, objectCollection);
}
```

### Retry Pattern

```java
public ActionResult clickWithRetry(StateImage target, int maxRetries) {
    ConditionalActionChain chain = ConditionalActionChain.find(
        new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .build()
    );
    
    for (int i = 0; i < maxRetries; i++) {
        chain = chain
            .ifNotFoundLog("Attempt " + (i + 1) + " failed")
            .ifNotFound(wait(1.0))
            .ifNotFound(find(target));
    }
    
    return chain
        .ifFound(click())
        .ifFoundLog("Successfully clicked after retries")
        .ifNotFoundLog("Failed after " + maxRetries + " attempts")
        .perform(action, new ObjectCollection.Builder()
            .withImages(target)
            .build());
}
```

## Advanced Patterns

### Multi-Step Form Filling

```java
public ActionResult fillForm(FormData data) {
    return ConditionalActionChain.find(formTitle)
        .ifNotFoundLog("Form not visible")
        .ifNotFound(throwError("Cannot proceed without form"))
        
        // Name field
        .then(find(nameField))
        .ifFound(click())
        .ifFound(clearAndType(data.getName()))
        
        // Email field
        .then(find(emailField))
        .ifFound(click())
        .ifFound(clearAndType(data.getEmail()))
        
        // Dropdown selection
        .then(find(countryDropdown))
        .ifFound(click())
        .ifFound(wait(0.5))
        .then(find(data.getCountry()))
        .ifFound(click())
        
        // Checkbox
        .then(find(agreeCheckbox))
        .ifFound(clickIfNotChecked())
        
        // Submit
        .then(find(submitButton))
        .ifFound(click())
        .always(takeScreenshot("form-submission"))
        .perform(action, objectCollection);
}
```

### Dynamic UI Navigation

```java
public ActionResult navigateToSection(String sectionName) {
    return ConditionalActionChain.find(hamburgerMenu)
        .ifFound(click())
        .ifNotFound(find(navigationBar))
        .always(wait(0.5))
        .then(find(sectionName))
        .ifFound(click())
        .ifNotFound(scrollDown())
        .ifNotFound(find(sectionName))
        .ifFound(click())
        .ifNotFoundLog("Section '" + sectionName + "' not found")
        .always(wait(1.0))
        .perform(action, objectCollection);
}
```

### Validation Chain

```java
public ActionResult validateAndSubmit() {
    return ConditionalActionChain.find(formFields)
        .ifFound(validateFields())
        .ifNotFoundLog("Form fields not found")
        
        // Check for errors
        .then(find(errorMessages))
        .ifFoundLog("Validation errors present")
        .ifFound(highlightErrors())
        .ifFound(stopChain())
        
        // No errors, proceed
        .ifNotFound(find(submitButton))
        .ifFound(highlight())
        .ifFound(wait(1.0))
        .ifFound(click())
        
        // Verify submission
        .then(waitVanish(submitButton))
        .ifFoundLog("Form submitted successfully")
        .ifNotFound(find(errorMessage))
        .ifFoundLog("Submission failed with error")
        .perform(action, objectCollection);
}
```

## Conditional Patterns

### If-Else Logic

```java
// Check if logged in, login if not
ConditionalActionChain.find(userAvatar)
    .ifFoundLog("Already logged in")
    .ifNotFound(find(loginButton))
    .ifFound(click())
    .ifFound(performLogin())
    .perform(action, objectCollection);
```

### Switch-Like Behavior

```java
public ActionResult handleDialog() {
    return ConditionalActionChain.find(dialogBox)
        // Try OK button first
        .ifFound(find(okButton))
        .ifFound(click())
        
        // If no OK, try Yes button
        .ifNotFound(find(yesButton))
        .ifFound(click())
        
        // If no Yes, try Continue button
        .ifNotFound(find(continueButton))
        .ifFound(click())
        
        // If none found, try Close button
        .ifNotFound(find(closeButton))
        .ifFound(click())
        
        // Last resort - press Escape
        .ifNotFound(pressEscape())
        .always(wait(0.5))
        .perform(action, objectCollection);
}
```

### Conditional Branching

```java
public ActionResult processItem(StateImage item) {
    return ConditionalActionChain.find(item)
        .ifFound(analyzeItem())
        
        // Branch based on item type
        .ifFoundDo(result -> {
            String itemType = result.getText();
            if ("document".equals(itemType)) {
                chain.then(openDocument());
            } else if ("image".equals(itemType)) {
                chain.then(viewImage());
            } else {
                chain.then(showProperties());
            }
        })
        
        .always(logAction())
        .perform(action, objectCollection);
}
```

## Error Recovery

### Graceful Degradation

```java
public ActionResult saveDocument() {
    return ConditionalActionChain
        // Try primary save method
        .find(saveButton)
        .ifFound(click())
        
        // Fallback to menu
        .ifNotFound(find(fileMenu))
        .ifFound(click())
        .ifFound(find(saveMenuItem))
        .ifFound(click())
        
        // Fallback to keyboard shortcut
        .ifNotFound(pressCtrlS())
        
        // Verify save completed
        .always(wait(1.0))
        .then(find(savedIndicator))
        .ifFoundLog("Document saved successfully")
        .ifNotFoundLog("Save status unknown")
        .perform(action, objectCollection);
}
```

### Exception Handling

```java
public ActionResult safeOperation() {
    return ConditionalActionChain.find(dangerousButton)
        .ifFound(checkPrerequisites())
        .ifFoundDo(result -> {
            if (!result.isSuccess()) {
                throw new IllegalStateException("Prerequisites not met");
            }
        })
        .ifFound(click())
        .ifFound(wait(2.0))
        .then(find(confirmationDialog))
        .ifFound(handleConfirmation())
        .ifNotFoundLog("Operation completed without confirmation")
        .always(cleanupResources())
        .perform(action, objectCollection);
}
```

## Performance Optimization

### Batch Operations

```java
public ActionResult processAllItems() {
    // Find all items once
    ActionResult items = action.find(itemPattern);
    
    if (!items.isSuccess()) {
        return items;
    }
    
    // Process each item without re-finding
    ActionResult finalResult = new ActionResult();
    for (Match item : items.getMatchList()) {
        ActionResult result = ConditionalActionChain
            .start(highlightRegion(item.getRegion()))
            .then(clickRegion(item.getRegion()))
            .then(wait(0.5))
            .then(processItemDialog())
            .perform(action, emptyCollection);
            
        finalResult.merge(result);
    }
    
    return finalResult;
}
```

### Lazy Evaluation

```java
public ActionResult efficientWorkflow() {
    return ConditionalActionChain
        // Quick check first
        .find(new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .setSearchRegion(topBar)
            .build())
        .ifFound(quickAction())
        
        // Full search only if quick check fails
        .ifNotFound(find(new PatternFindOptions.Builder()
            .setSimilarity(0.7)
            .setSearchRegion(fullScreen)
            .build()))
        .ifFound(fullAction())
        
        .perform(action, objectCollection);
}
```

## Testing Patterns

### Mock-Friendly Chains

```java
@Test
public void testLoginChain() {
    // Create test data
    ObjectCollection testCollection = new ObjectCollection.Builder()
        .withImages(mockLoginButton)
        .build();
    
    // Build and test chain
    ActionResult result = ConditionalActionChain
        .find(testFindOptions)
        .ifFound(testClickOptions)
        .ifNotFoundLog("Test: Login button not found")
        .perform(mockAction, testCollection);
    
    // Verify behavior
    assertTrue(result.isSuccess());
    assertEquals("Test: Login button not found", 
                 result.getText());
}
```

## Best Practices

1. **Keep Chains Readable**: Break complex chains into named methods
2. **Use Logging**: Add ifFoundLog/ifNotFoundLog for debugging
3. **Handle Failures**: Always provide ifNotFound alternatives
4. **Test Incrementally**: Build chains step by step
5. **Reuse Common Patterns**: Create utility methods for repeated sequences

## Common Pitfalls

1. **Forgetting always()**: Use for cleanup that must happen
2. **Long Chains**: Break into smaller, named sub-chains
3. **Missing Error Handling**: Always handle the ifNotFound case
4. **Tight Coupling**: Keep chains focused on single workflows