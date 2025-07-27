---
sidebar_position: 10
title: Automatic Action Logging
description: Streamline your automation code with built-in logging capabilities
---

# Automatic Action Logging

Brobot's automatic logging feature eliminates boilerplate code by providing built-in logging capabilities for all actions. This guide shows you how to use these features to improve observability and debugging in your automation projects.

## Overview

Traditional automation code often includes repetitive logging patterns:

```java
// Without automatic logging - verbose and repetitive
System.out.println("Searching for submit button...");
ActionResult result = action.perform(findOptions, button);
if (result.isSuccess()) {
    System.out.println("Submit button found");
} else {
    System.out.println("Submit button not found");
}
System.out.println("Search completed");
```

With automatic logging, the same functionality becomes:

```java
// With automatic logging - clean and declarative
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for submit button...")
    .withSuccessLog("Submit button found")
    .withFailureLog("Submit button not found")
    .withAfterActionLog("Search completed")
    .build();

action.perform(findOptions, button); // Logging happens automatically!
```

## Logging Types

### 1. Before Action Logging

Log a message before the action begins execution:

```java
.withBeforeActionLog("Attempting to click login button...")
```

**Use cases:**
- Indicate what the automation is about to attempt
- Provide context for debugging
- Show progress in long-running automations

### 2. Success Logging

Log a message when the action completes successfully:

```java
.withSuccessLog("Successfully submitted the form")
```

**Use cases:**
- Confirm successful operations
- Track completed steps
- Build audit trails

### 3. Failure Logging

Log a message when the action fails:

```java
.withFailureLog("Failed to find save button - check if dialog is open")
```

**Use cases:**
- Provide actionable error messages
- Guide troubleshooting
- Document failure conditions

### 4. After Action Logging

Log a message after the action completes, regardless of success or failure:

```java
.withAfterActionLog("Login attempt completed")
```

**Use cases:**
- Mark completion of operations
- Log timing information
- Ensure cleanup messages

## Message Placeholders

Logging messages support dynamic placeholders that are replaced with actual values at runtime:

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `{target}` | Name of the target element | "submit-button" |
| `{action}` | Type of action performed | "CLICK" |
| `{matchCount}` | Number of matches found | "3" |
| `{duration}` | Action duration in milliseconds | "250" |
| `{success}` | Whether action succeeded | "true" or "false" |

### Example with Placeholders

```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .withBeforeActionLog("Clicking {target}...")
    .withSuccessLog("Clicked {target} successfully")
    .withFailureLog("Failed to click {target} after {duration}ms")
    .withAfterActionLog("Click operation took {duration}ms")
    .build();
```

## Configuration Methods

### Simple Configuration

Use individual methods for each logging type:

```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withBeforeActionLog("Starting search...")
    .withSuccessLog("Element found")
    .withFailureLog("Element not found")
    .withAfterActionLog("Search complete")
    .build();
```

### Advanced Configuration

Use the `withLogging()` method for full control:

```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withLogging(logging -> logging
        .beforeActionMessage("Starting exhaustive search for {target}...")
        .successMessage("Found {matchCount} instances in {duration}ms")
        .failureMessage("No matches found with similarity >= 0.85")
        .afterActionMessage("Exhaustive search completed")
        .logBeforeAction(true)
        .logOnSuccess(true)
        .logOnFailure(true)
        .logAfterAction(true)
        .beforeActionLevel(LogEventType.ACTION)
        .successLevel(LogEventType.ACTION)
        .failureLevel(LogEventType.ERROR)
        .afterActionLevel(LogEventType.ACTION))
    .build();
```

### Disable Logging

To disable all automatic logging for an action:

```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withNoLogging()
    .build();
```

## Action Chaining and ObjectCollections

### Understanding ObjectCollections

ObjectCollections are containers that hold different types of automation targets. When you chain actions, all actions in the chain share the same ObjectCollection, but each action type uses only the objects it needs:

- **Find/Click actions**: Use StateImages, Regions, or Locations
- **Type actions**: Use Strings from the collection
- **Drag actions**: Use source and target Locations/Regions

### How ObjectCollection Sharing Works

When you execute a chained action, the framework passes the same ObjectCollection to every action in the chain:

#### Key Concepts:

1. **Single ObjectCollection**: All actions in a chain receive the same ObjectCollection
2. **Selective Usage**: Each action type extracts only what it needs
3. **Order Matters for Strings**: Multiple Type actions consume strings in order

#### String Consumption by Type Actions

When an ObjectCollection contains multiple strings, Type actions consume them in sequence:

```java
// Example with multiple strings
ObjectCollection data = new ObjectCollection.Builder()
    .withImages(field1, field2, field3)
    .withStrings("First", "Second", "Third")  // Three strings
    .build();

// First Type action uses "First"
action.perform(new TypeOptions.Builder().build(), data);

// Second Type action uses "Second" 
action.perform(new TypeOptions.Builder().build(), data);

// Third Type action uses "Third"
action.perform(new TypeOptions.Builder().build(), data);
```

**Note**: In current implementation, all Type actions in a single chain may use all available strings. For precise control, use separate ObjectCollections or individual Type actions.

```java
// Original example showing mixed object types
ObjectCollection targets = new ObjectCollection.Builder()
    .withImages(inputField)           // Used by Find action
    .withStrings("Hello, World!")     // Used by Type action
    .withLocations(submitLocation)    // Could be used by Click
    .build();

// Create a chained action: find -> click -> type
PatternFindOptions findAndType = new PatternFindOptions.Builder()
    .withBeforeActionLog("Finding input field...")
    .then(new ClickOptions.Builder()
        .withBeforeActionLog("Clicking to focus...")
        .build())
    .then(new TypeOptions.Builder()
        .withBeforeActionLog("Typing text...")
        .build())
    .build();

// Execute the chain - all actions receive the same ObjectCollection
action.perform(findAndType, targets);
```

**Execution flow:**
1. **Find action**: Searches for `inputField` (StateImage) on screen
2. **Click action**: Clicks on the match found by the Find action
3. **Type action**: Types "Hello, World!" (String) from the ObjectCollection

### Important: Correct Method Signatures

The `action.perform()` method accepts these signatures:
- `perform(ActionConfig, ObjectCollection...)`
- `perform(ActionConfig, StateImage...)` - convenience method that wraps images in ObjectCollection
- `perform(String description, ActionConfig, ObjectCollection...)`

**Incorrect usage:**
```java
// WRONG - This signature doesn't exist!
action.perform(config, stateImage, "text");
```

**Correct usage:**
```java
// Option 1: Create ObjectCollection explicitly
ObjectCollection collection = new ObjectCollection.Builder()
    .withImages(stateImage)
    .withStrings("text")
    .build();
action.perform(config, collection);

// Option 2: Use separate actions for different object types
action.perform(findConfig, stateImage);
action.perform(typeConfig, new ObjectCollection.Builder().withStrings("text").build());
```

### Chaining Strategies

ActionChainOptions supports two execution strategies:

1. **NESTED** (default): Each action searches within the results of the previous action
   ```java
   new ActionChainOptions.Builder(initialAction)
       .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
       .then(nextAction)
       .build()
   ```

2. **CONFIRM**: Each action validates/confirms the results of the previous action
   ```java
   new ActionChainOptions.Builder(initialAction)
       .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
       .then(confirmAction)
       .build()
   ```

## Real-World Examples

### Form Automation with Proper ObjectCollection Usage

```java
public void fillAndSubmitForm(StateImage firstNameField, StateImage submitButton) {
    // Create ObjectCollection with all form data
    ObjectCollection formData = new ObjectCollection.Builder()
        .withImages(firstNameField, submitButton)
        .withStrings("John", "Doe", "john.doe@example.com")
        .build();
    
    // Chain: find field -> click -> type -> tab to next field
    PatternFindOptions fillForm = new PatternFindOptions.Builder()
        .withBeforeActionLog("Looking for first name field...")
        .withSuccessLog("First name field found")
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Clicking to focus field...")
            .build())
        .then(new TypeOptions.Builder()
            .withBeforeActionLog("Typing first name...")
            .withSuccessLog("First name entered")
            .build())
        .build();
    
    // Execute the chain with the ObjectCollection
    ActionResult result = action.perform(fillForm, formData);
    
    if (result.isSuccess()) {
        // Now find and click submit button
        PatternFindOptions submitForm = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for submit button...")
            .withSuccessLog("Submit button located")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Submitting form...")
                .withSuccessLog("Form submitted successfully")
                .withAfterActionLog("Form submission completed in {duration}ms")
                .build())
            .build();
        
        // Note: Using same ObjectCollection, but now targeting submitButton
        action.perform(submitForm, formData);
    }
}
```

### Multi-Step Process with Action Chaining

```java
public void completeCheckout(StateImage cartButton, StateImage checkoutButton, 
                           StateImage confirmButton, String creditCardNumber) {
    // Create ObjectCollection with all checkout data
    ObjectCollection checkoutData = new ObjectCollection.Builder()
        .withImages(cartButton, checkoutButton, confirmButton)
        .withStrings(creditCardNumber)
        .build();
    
    // Chain all checkout steps
    PatternFindOptions checkoutFlow = new PatternFindOptions.Builder()
        .withBeforeActionLog("Finding add to cart button...")
        .withSuccessLog("Cart button found")
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Adding item to cart...")
            .withSuccessLog("Item added to cart")
            .setPauseAfterEnd(1.0)  // Wait for cart update
            .build())
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for checkout button...")
            .withSuccessLog("Checkout button found")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Proceeding to checkout...")
            .withSuccessLog("Navigated to checkout page")
            .setPauseAfterEnd(2.0)  // Wait for page load
            .build())
        .then(new TypeOptions.Builder()
            .withBeforeActionLog("Entering payment information...")
            .withSuccessLog("Payment details entered")
            .build())
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for confirm button...")
            .withSuccessLog("Confirm button found")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Confirming purchase...")
            .withSuccessLog("Purchase completed successfully!")
            .withAfterActionLog("Entire checkout process took {duration}ms")
            .build())
        .build();
    
    // Execute entire checkout flow with one call
    action.perform(checkoutFlow, checkoutData);
}
```

### Complex Form with Mixed Input Types

```java
public void fillComplexForm() {
    // Form elements
    StateImage nameField = new StateImage.Builder()
        .setName("name-field").build();
    StateImage emailField = new StateImage.Builder()
        .setName("email-field").build();
    StateImage dropdownMenu = new StateImage.Builder()
        .setName("country-dropdown").build();
    StateImage checkbox = new StateImage.Builder()
        .setName("terms-checkbox").build();
    
    // All form data in one collection
    ObjectCollection formData = new ObjectCollection.Builder()
        .withImages(nameField, emailField, dropdownMenu, checkbox)
        .withStrings("Alice Smith", "alice@example.com", "United States")
        .build();
    
    // Chain for name field
    PatternFindOptions fillName = new PatternFindOptions.Builder()
        .withBeforeActionLog("Filling name field...")
        .then(new ClickOptions.Builder().build())
        .then(new TypeOptions.Builder()
            .withSuccessLog("Name entered")
            .build())
        .build();
    
    // Execute - TypeOptions will use "Alice Smith" from the ObjectCollection
    action.perform(fillName, formData);
    
    // Chain for email field with clear-first pattern
    PatternFindOptions fillEmail = new PatternFindOptions.Builder()
        .withBeforeActionLog("Filling email field...")
        .then(new ClickOptions.Builder().build())
        .then(new TypeOptions.Builder()
            .setModifiers("CTRL+A")  // Select all first
            .build())
        .then(new TypeOptions.Builder()
            .withSuccessLog("Email entered")
            .build())
        .build();
    
    // The Type actions will use strings in order from ObjectCollection
    action.perform(fillEmail, formData);
}
```

### Conditional Logging

```java
public void performSearch(String searchType, StateImage target) {
    PatternFindOptions.Builder builder = new PatternFindOptions.Builder();
    
    // Configure logging based on search type
    if (searchType.equals("critical")) {
        builder.withLogging(logging -> logging
            .beforeActionMessage("Performing critical search for {target}")
            .failureMessage("CRITICAL: Failed to find {target}")
            .failureLevel(LogEventType.ERROR)
            .logBeforeAction(true)
            .logOnFailure(true));
    } else {
        builder.withLogging(logging -> logging
            .successMessage("Found {target}")
            .logOnSuccess(true)
            .logOnFailure(false));  // Don't log non-critical failures
    }
    
    action.perform(builder.build(), target);
}
```

## Integration with Unified Logging

The automatic logging feature integrates seamlessly with Brobot's unified logging system:

- Messages are routed through `BrobotLogger`
- Log levels are properly mapped
- Context (session, state, action) is automatically included
- Structured metadata is captured for analysis

### Log Output Example

When you use automatic logging, the output is formatted and routed appropriately:

```
[ACTION] Searching for submit button...
[ACTION] Submit button found
[ACTION] Attempting to click submit button...
[ACTION] Successfully clicked the submit button
[ACTION] Click action completed in 145ms
```

## Common Mistakes and Solutions

### 1. Incorrect Method Signatures

**Wrong:**
```java
// This method signature doesn't exist!
action.perform(config, stateImage, "text");
```

**Correct:**
```java
ObjectCollection collection = new ObjectCollection.Builder()
    .withImages(stateImage)
    .withStrings("text")
    .build();
action.perform(config, collection);
```

### 2. Assuming Separate ObjectCollections in Chains

**Wrong assumption:** Each action in a chain gets its own ObjectCollection

**Reality:** All actions share the same ObjectCollection

```java
// All three actions receive the same 'data' ObjectCollection
PatternFindOptions chain = new PatternFindOptions.Builder()
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();
    
action.perform(chain, data);  // 'data' is passed to all actions
```

### 3. Using Non-Existent API Methods

**Common mistakes:**
- `setMaxSearchTime()` → Use `setSearchDuration()`
- `WaitOptions` doesn't exist → Use `setPauseBeforeBegin()` or `setPauseAfterEnd()`
- `setMaxRetries()` → Use `RepetitionOptions`
- `ClickOptions.Type.HOVER` → Use `MouseMoveOptions`

### 4. Not Understanding String Order in Type Actions

**Issue:** Multiple Type actions may consume strings unpredictably

**Solution:** Use separate actions or carefully manage string order:
```java
// Better: Explicit control over which string is typed
ObjectCollection name = new ObjectCollection.Builder()
    .withStrings("John Doe").build();
ObjectCollection email = new ObjectCollection.Builder()
    .withStrings("john@example.com").build();

action.perform(typeNameConfig, name);
action.perform(typeEmailConfig, email);
```

## Best Practices

1. **Be Descriptive**: Write messages that provide context
   ```java
   // Good
   .withFailureLog("Login failed - check if credentials are correct")
   
   // Less helpful
   .withFailureLog("Failed")
   ```

2. **Use Placeholders**: Make messages dynamic and informative
   ```java
   .withSuccessLog("Found {matchCount} instances of {target} in {duration}ms")
   ```

3. **Set Appropriate Levels**: Use ERROR level for critical failures
   ```java
   .withLogging(logging -> logging
       .failureMessage("Critical component missing")
       .failureLevel(LogEventType.ERROR))
   ```

4. **Consider Performance**: Disable verbose logging in production if needed
   ```java
   .withLogging(logging -> logging
       .logBeforeAction(false)  // Skip in production
       .logAfterAction(false)   // Skip in production
       .logOnSuccess(true)      // Keep success logging
       .logOnFailure(true))     // Keep failure logging
   ```

5. **Provide Actionable Information**: Help users understand what went wrong
   ```java
   .withFailureLog("Save button not found - ensure the document is in edit mode")
   ```

## Summary

Automatic action logging in Brobot:
- **Reduces boilerplate** - No more manual if/else logging blocks
- **Improves consistency** - All actions log in the same format
- **Enhances debugging** - Clear visibility into automation flow
- **Supports customization** - Fine-grained control when needed
- **Integrates seamlessly** - Works with the unified logging system

By leveraging these logging capabilities, you can create more maintainable and observable automation code while focusing on the business logic rather than infrastructure concerns.