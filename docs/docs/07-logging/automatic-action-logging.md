---
sidebar_position: 10
title: Automatic Action Logging
description: Streamline your automation code with built-in logging capabilities
---

# Automatic Action Logging

Brobot provides logging capabilities for all actions through standard logging frameworks. This guide shows you how to implement logging in your automation projects.

## Overview

Brobot integrates with SLF4J/Logback for logging. Instead of built-in logging methods, use standard logging practices:

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyAutomation {
    
    public void performAction() {
        log.info("Searching for submit button...");
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .build();
            
        ActionResult result = action.perform(findOptions, button);
        
        if (result.isSuccess()) {
            log.info("Submit button found");
        } else {
            log.warn("Submit button not found");
        }
        
        log.info("Search completed in {}ms", result.getDuration().toMillis());
    }
}
```

## Logging Best Practices

### 1. Use Structured Logging

```java
log.info("Action completed", 
    "action", actionType,
    "target", targetName,
    "duration", duration,
    "success", result.isSuccess()
);
```

### 2. Log at Appropriate Levels

- **DEBUG**: Detailed execution flow
- **INFO**: Normal operation progress
- **WARN**: Recoverable issues
- **ERROR**: Failures requiring attention

### 3. Include Context

```java
MDC.put("workflow", "login");
MDC.put("step", "credentials");
try {
    // Perform actions
    log.info("Entering username");
} finally {
    MDC.clear();
}
```

## Action Chaining

### Basic Chaining with ActionChainOptions

Action chaining must be done through `ActionChainOptions`, not individual option builders:

```java
// Create the initial action
PatternFindOptions findInput = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .build();

// Build the chain
ActionChainOptions chain = new ActionChainOptions.Builder(findInput)
    .then(new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)
        .build())
    .then(new TypeOptions.Builder()
        .setTypeDelay(0.05)
        .build())
    .build();

// Execute the chain
ObjectCollection targets = new ObjectCollection.Builder()
    .withImages(inputField)
    .withStrings("Hello, World!")
    .build();

log.info("Starting input chain");
ActionResult result = action.perform(chain, targets);
log.info("Chain completed: {}", result.isSuccess() ? "success" : "failed");
```

### Understanding ObjectCollections

ObjectCollections are containers that hold different types of automation targets:

- **Find/Click actions**: Use StateImages, Regions, or Locations
- **Type actions**: Use Strings from the collection
- **Drag actions**: Use source and target Locations/Regions

```java
// Create a collection with mixed types
ObjectCollection targets = new ObjectCollection.Builder()
    .withImages(inputField)           // Used by Find action
    .withStrings("Hello, World!")     // Used by Type action
    .build();
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

### Form Automation with Logging

```java
@Slf4j
@Component
public class FormAutomation {
    
    @Autowired
    private Action action;
    
    public void fillAndSubmitForm(StateImage firstNameField, StateImage submitButton) {
        log.info("Starting form automation");
        
        // Create form data
        ObjectCollection formData = new ObjectCollection.Builder()
            .withImages(firstNameField, submitButton)
            .withStrings("John", "Doe", "john.doe@example.com")
            .build();
        
        // Build the action chain
        PatternFindOptions findFirstName = new PatternFindOptions.Builder()
            .build();
            
        ActionChainOptions fillForm = new ActionChainOptions.Builder(findFirstName)
            .then(new ClickOptions.Builder().build())
            .then(new TypeOptions.Builder().build())
            .then(new TypeOptions.Builder().build()) // Tab key
            .then(new TypeOptions.Builder().build()) // Last name
            .then(new TypeOptions.Builder().build()) // Tab key
            .then(new TypeOptions.Builder().build()) // Email
            .then(new PatternFindOptions.Builder().build()) // Find submit
            .then(new ClickOptions.Builder().build()) // Click submit
            .build();
        
        // Execute with logging
        log.info("Filling form fields");
        ActionResult result = action.perform(fillForm, formData);
        
        if (result.isSuccess()) {
            log.info("Form submitted successfully");
        } else {
            log.error("Form submission failed");
        }
    }
}
```

### Login Workflow

```java
@Slf4j
@Component  
public class LoginWorkflow {
    
    @Autowired
    private Action action;
    
    public boolean login(String username, String password) {
        log.info("Starting login workflow for user: {}", username);
        
        StateImage usernameField = new StateImage.Builder()
            .addPattern("username_field.png")
            .build();
            
        StateImage passwordField = new StateImage.Builder()
            .addPattern("password_field.png")
            .build();
            
        StateImage loginButton = new StateImage.Builder()
            .addPattern("login_button.png")
            .build();
        
        try {
            // Find and fill username
            log.debug("Looking for username field");
            ActionResult userResult = action.perform(
                new PatternFindOptions.Builder().build(), 
                usernameField
            );
            
            if (!userResult.isSuccess()) {
                log.error("Username field not found");
                return false;
            }
            
            log.debug("Clicking username field");
            action.perform(new ClickOptions.Builder().build(), usernameField);
            
            log.debug("Typing username");
            action.perform(
                new TypeOptions.Builder().build(),
                new ObjectCollection.Builder().withStrings(username).build()
            );
            
            // Find and fill password
            log.debug("Looking for password field");
            ActionResult passResult = action.perform(
                new PatternFindOptions.Builder().build(),
                passwordField
            );
            
            if (!passResult.isSuccess()) {
                log.error("Password field not found");
                return false;
            }
            
            log.debug("Clicking password field");
            action.perform(new ClickOptions.Builder().build(), passwordField);
            
            log.debug("Typing password");
            action.perform(
                new TypeOptions.Builder().build(),
                new ObjectCollection.Builder().withStrings(password).build()
            );
            
            // Submit
            log.debug("Clicking login button");
            ActionResult loginResult = action.perform(
                new ClickOptions.Builder()
                    .setPauseAfterEnd(2.0) // Wait for login
                    .build(),
                loginButton
            );
            
            if (loginResult.isSuccess()) {
                log.info("Login successful");
                return true;
            } else {
                log.error("Failed to click login button");
                return false;
            }
            
        } catch (Exception e) {
            log.error("Login workflow failed", e);
            return false;
        }
    }
}
```

## Integration with Spring Boot

Configure logging in your `application.yml`:

```yaml
logging:
  level:
    com.example: DEBUG
    io.github.jspinak.brobot: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: automation.log
```

## Summary

While Brobot doesn't have built-in logging methods like `withBeforeActionLog()`, it integrates seamlessly with standard Java logging frameworks. Use SLF4J with Logback or Log4j2 for comprehensive logging in your automation projects.

Key points:
- Use `@Slf4j` annotation for easy logger access
- Log at appropriate levels (DEBUG, INFO, WARN, ERROR)
- Include context with MDC for better debugging
- Chain actions using `ActionChainOptions.Builder`
- Pass text to Type actions via `ObjectCollection.withStrings()`
