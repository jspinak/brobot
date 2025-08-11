---
sidebar_position: 16
title: ConditionalActionWrapper
description: Spring-friendly wrapper for ConditionalActionChain with comparison to FindAndClick/FindAndType
---

# ConditionalActionWrapper

The ConditionalActionWrapper is a Spring-friendly utility that simplifies the use of ConditionalActionChain in Spring Boot applications. It provides convenient methods for common patterns and integrates seamlessly with dependency injection.

> **Note:** ConditionalActionWrapper now uses the enhanced `ConditionalActionChain` internally, providing access to improved convenience methods and enhanced functionality.

## Overview

ConditionalActionWrapper serves as a bridge between Spring applications and Brobot's ConditionalActionChain, offering:

- **Spring Integration**: `@Component` annotation for easy dependency injection
- **Simplified API**: Convenient methods for common patterns
- **Fluent Builder**: Clean, readable chain construction
- **Type Safety**: Works with StateObject interface

## ConditionalActionWrapper vs FindAndClick/FindAndType

Brobot provides multiple ways to combine actions. Understanding the differences helps you choose the right tool:

### FindAndClick and FindAndType

These are **composite ActionConfig** classes that bundle actions together:

```java
// FindAndClick - always finds then clicks
FindAndClick findAndClick = new FindAndClick.Builder()
    .withSimilarity(0.9)
    .withNumberOfClicks(2)
    .build();

action.perform(findAndClick, objectCollection);

// FindAndType - always finds, clicks, then types
FindAndType findAndType = new FindAndType.Builder()
    .withFindOptions(customFindOptions)
    .withTypeOptions(customTypeOptions)
    .build();

action.perform(findAndType, objectCollection);
```

**Characteristics:**
- Execute all actions sequentially (no conditional logic)
- Configuration objects (not Spring components)
- Simple, straightforward for basic sequences
- If find fails, click/type still attempt to execute

### ConditionalActionWrapper

This provides **conditional execution** with Spring integration:

```java
@Autowired
private ConditionalActionWrapper actions;

// Conditional execution - only clicks if found
actions.findAndClick(submitButton);

// Complex conditional chains
actions.createChain()
    .find(loginButton)
    .ifFoundClick()
    .ifNotFound(actions.find(alternativeButton))
    .ifFoundClick()
    .ifNotFoundLog("No login button found")
    .execute();
```

**Characteristics:**
- Conditional execution based on success/failure
- Spring component (injectable)
- Supports complex branching logic
- Better error handling and logging

## When to Use Each

### Use FindAndClick/FindAndType When:
- You always want to execute all steps
- You don't need conditional logic
- You're not using Spring dependency injection
- Simple, predictable sequences are sufficient

### Use ConditionalActionWrapper When:
- You need conditional execution paths
- You want better error handling
- You're in a Spring Boot application
- You need complex conditional chains
- You want to log success/failure at each step

## Basic Usage

### Setup

```java
@Component
public class LoginAutomation {
    private final ConditionalActionWrapper actions;
    
    @Autowired
    public LoginAutomation(ConditionalActionWrapper actions) {
        this.actions = actions;
    }
}
```

### Simple Operations

```java
// Find and click with automatic error handling
public void clickLoginButton(StateImage loginButton) {
    ActionResult result = actions.findAndClick(loginButton);
    
    if (!result.isSuccess()) {
        log.error("Failed to click login button");
    }
}

// Find and type
public void enterCredentials(StateImage usernameField, String username) {
    actions.findAndType(usernameField, username);
}
```

### Building Conditional Chains

```java
public ActionResult performLogin(StateImage loginButton, 
                               StateImage usernameField,
                               StateImage passwordField,
                               String username,
                               String password) {
    return actions.createChain()
        .find(loginButton)
        .ifFoundClick()
        .ifNotFoundLog("Login button not visible")
        .always(ConditionalActionWrapper.find())
        .find(usernameField)
        .ifFoundClick()
        .ifFoundType(username)
        .execute();
}
```

## Advanced Examples

### Error Recovery

```java
public ActionResult saveWithFallback(StateImage saveButton, 
                                   StateImage fileMenu,
                                   StateImage saveMenuItem) {
    return actions.createChain()
        // Try the save button first
        .find(saveButton)
        .ifFoundClick()
        
        // If not found, try menu approach
        .ifNotFound(actions.createChain()
            .find(fileMenu)
            .ifFoundClick()
            .find(saveMenuItem)
            .ifFoundClick()
            .execute())
        
        // Log the result
        .ifFoundLog("Document saved successfully")
        .ifNotFoundLog("Failed to save document")
        .execute();
}
```

### Multi-Step Form Filling

```java
@Service
public class FormAutomation {
    @Autowired
    private ConditionalActionWrapper actions;
    
    public ActionResult fillRegistrationForm(RegistrationData data) {
        ChainBuilder chain = actions.createChain();
        
        // Find and fill each field conditionally
        chain.find(nameField)
            .ifFoundClick()
            .ifFoundType(data.getName())
            .ifFoundLog("Entering name: " + data.getName());
            
        // Add email field
        chain.find(emailField)
            .ifFoundClick()
            .ifFoundType(data.getEmail())
            .ifFoundLog("Entering email: " + data.getEmail());
            
        // Submit only if all fields were found
        chain.find(submitButton)
            .ifFoundClick()
            .ifFoundLog("Form submitted")
            .ifNotFoundLog("Submit button not found - form incomplete");
            
        return chain.execute();
    }
}
```

### Retry Pattern

```java
public ActionResult clickWithRetry(StateObject target, int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
        ActionResult result = actions.findAndClick(target);
        
        if (result.isSuccess()) {
            log.info("Successfully clicked on attempt {}", i + 1);
            return result;
        }
        
        log.warn("Attempt {} failed, retrying...", i + 1);
        Thread.sleep(1000); // Wait before retry
    }
    
    log.error("Failed to click after {} attempts", maxAttempts);
    return new ActionResult.Builder().setSuccess(false).build();
}
```

## Comparison Example

Here's the same task implemented three ways:

### Using FindAndClick (No Conditions)

```java
// Simple but inflexible - always tries to click even if find fails
public void submitForm() {
    FindAndClick submitAction = new FindAndClick.Builder()
        .withSimilarity(0.9)
        .build();
        
    action.perform(submitAction, new ObjectCollection.Builder()
        .withImages(submitButton)
        .build());
}
```

### Using ConditionalActionChain (Direct API)

```java
// Flexible but verbose
public void submitForm() {
    ConditionalActionChain.find(new PatternFindOptions.Builder().build())
        .ifFoundClick()
        .ifNotFoundLog("Submit button not found")
        .perform(action, new ObjectCollection.Builder()
            .withImages(submitButton)
            .build());
}
```

### Using ConditionalActionWrapper (Spring-Friendly)

```java
// Clean, Spring-integrated, and flexible
@Autowired
private ConditionalActionWrapper actions;

public void submitForm() {
    actions.createChain()
        .find(submitButton)
        .ifFoundClick()
        .ifNotFoundLog("Submit button not found")
        .execute();
}
```

## Best Practices

### 1. Use Dependency Injection

```java
@Configuration
public class AutomationConfig {
    @Bean
    public ConditionalActionWrapper conditionalActionWrapper(Action action) {
        return new ConditionalActionWrapper(action);
    }
}
```

### 2. Create Reusable Methods

```java
@Component
public class CommonActions {
    @Autowired
    private ConditionalActionWrapper actions;
    
    public ActionResult clickIfExists(StateObject element) {
        return actions.createChain()
            .find(element)
            .ifFoundClick()
            .ifNotFoundLog("Optional element not found: " + element.getName())
            .execute();
    }
}
```

### 3. Handle Results Appropriately

```java
public boolean performCriticalAction(StateObject target) {
    ActionResult result = actions.findAndClick(target);
    
    if (!result.isSuccess()) {
        // Log error details
        log.error("Critical action failed: {}", result.getText());
        
        // Take screenshot for debugging
        captureScreenshot("critical-action-failure");
        
        // Alert monitoring system
        alertOps("Critical UI element not found: " + target.getName());
        
        return false;
    }
    
    return true;
}
```

### 4. Use Static Factory Methods

ConditionalActionWrapper provides static factory methods for common actions:

```java
import static com.example.ConditionalActionWrapper.*;

actions.createChain()
    .find(element)
    .ifFoundClick()        // Convenience method
    .ifFoundType("text")   // Convenience method
    .thenFind()            // Chain method
    .execute();
```

## Integration with Spring Boot

### Configuration Class

```java
@Configuration
@EnableAutoConfiguration
public class BrobotConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public ConditionalActionWrapper conditionalActionWrapper(Action action) {
        return new ConditionalActionWrapper(action);
    }
    
    @Bean
    @ConditionalOnProperty(
        prefix = "brobot.conditional",
        name = "logging.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public ConditionalActionLogger conditionalActionLogger() {
        return new ConditionalActionLogger();
    }
}
```

### Using in Tests

```java
@SpringBootTest
public class LoginAutomationTest {
    
    @MockBean
    private Action mockAction;
    
    @Autowired
    private ConditionalActionWrapper actions;
    
    @Test
    public void testConditionalLogin() {
        // Setup mock responses
        when(mockAction.perform(any(), any()))
            .thenReturn(successfulResult());
            
        // Test the conditional flow
        ActionResult result = actions.createChain()
            .find(loginButton)
            .ifFoundClick()
            .execute();
            
        assertTrue(result.isSuccess());
    }
}
```

## Summary

ConditionalActionWrapper enhances Brobot's automation capabilities by:

1. **Providing Spring Integration**: Easy dependency injection and configuration
2. **Simplifying Conditional Logic**: Clean API for complex conditional flows
3. **Improving Error Handling**: Built-in logging and error recovery patterns
4. **Complementing Existing Tools**: Works alongside FindAndClick/FindAndType for different use cases

Choose ConditionalActionWrapper when you need conditional execution, Spring integration, or complex automation flows. Use FindAndClick/FindAndType for simple, always-execute sequences where Spring integration isn't required.