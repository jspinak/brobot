---
sidebar_position: 16
title: ConditionalActionWrapper
description: Spring-friendly wrapper for ConditionalActionChain with comparison to FindAndClick/FindAndType
---

# ConditionalActionWrapper

## Required Imports

All examples in this guide assume the following imports:

```java
// Spring Framework
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

// Brobot Core
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.util.ConditionalActionWrapper;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

// Brobot ActionConfig and Composite Actions
import io.github.jspinak.brobot.action.composite.FindAndClick;
import io.github.jspinak.brobot.action.composite.FindAndType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.conditionals.ConditionalActionChain;
import io.github.jspinak.brobot.action.ObjectCollection;

// Testing (for test examples)
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

// Logging
import lombok.extern.slf4j.Slf4j;
```

## Overview

The ConditionalActionWrapper is a Spring-friendly utility that simplifies the use of [ConditionalActionChain](./15-conditional-chains-examples.md) in Spring Boot applications. It provides convenient methods for common patterns and integrates seamlessly with dependency injection.

> **Note:** ConditionalActionWrapper now uses the enhanced `ConditionalActionChain` internally, providing access to improved convenience methods and enhanced functionality.

### Prerequisites

Before using ConditionalActionWrapper, you should understand:
- **[States in Brobot](../../01-getting-started/states.md)** - Defining StateImage and StateObject elements
- **[ActionResult Components](./17-actionresult-components.md)** - Understanding action results
- **Spring Boot** - Basic dependency injection with `@Component` and `@Autowired`

:::info Assumed Variables
Unless otherwise specified, the following examples assume you have:
- `@Autowired ConditionalActionWrapper actions` - Injected wrapper instance
- StateImage/StateObject variables (e.g., `loginButton`, `submitButton`) - Pre-initialized UI elements
- Logger configured (using SLF4J or similar for logging examples)

Examples showing undefined variables are partial code snippets. See the [Complete Example](#complete-production-example) section for fully compilable code.
:::

### What It Provides

ConditionalActionWrapper serves as a bridge between Spring applications and Brobot's ConditionalActionChain, offering:

- **Spring Integration**: `@Component` annotation for easy dependency injection
- **Simplified API**: Convenient methods for common patterns
- **Fluent Builder**: Clean, readable chain construction
- **Type Safety**: Works with StateObject interface

:::warning API Note
The `ChainBuilder` class (returned by `createChain()`) provides `ifFound(ActionConfig)` and `ifNotFound(ActionConfig)` methods, but **does not** include the convenience methods `ifFoundClick()` or `ifFoundType()` that exist in `ConditionalActionChain`.

For full convenience method support, use [ConditionalActionChain](./15-conditional-chains-examples.md) directly. The wrapper is designed for simpler Spring integration patterns.
:::

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
- Configuration objects that define action sequences (not Spring components)
- Simple, straightforward for basic sequences
- No branching logic (ifFound/ifNotFound) at configuration level
- Execution behavior depends on action executor implementation

### ConditionalActionWrapper

This provides **conditional execution** with Spring integration:

```java
@Autowired
private ConditionalActionWrapper actions;

// Conditional execution - only clicks if found
actions.findAndClick(submitButton);  // This method works correctly

// Complex conditional chains
// Note: ChainBuilder convenience methods shown below (ifFoundClick) may not be available
// Use .ifFound(new ClickOptions.Builder().build()) for actual ChainBuilder API
actions.createChain()
    .find(loginButton)
    .ifFoundClick()  // Conceptual - see API warning note
    .ifNotFound(actions.find(alternativeButton))
    .ifFoundClick()  // Conceptual - see API warning note
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
// Note: This example shows convenience methods (ifFoundClick, ifFoundType) that exist in
// ConditionalActionChain but NOT in ChainBuilder. For the actual ChainBuilder API, use:
// .ifFound(new ClickOptions.Builder().build()) instead of .ifFoundClick()
// See the Complete Production Example section for code that uses the correct API.

public ActionResult performLogin(StateImage loginButton,
                               StateImage usernameField,
                               StateImage passwordField,
                               String username,
                               String password) {
    return actions.createChain()
        .find(loginButton)
        .ifFoundClick()  // ChainBuilder: use .ifFound(new ClickOptions.Builder().build())
        .ifNotFoundLog("Login button not visible")
        .always(ConditionalActionWrapper.find())
        .find(usernameField)
        .ifFoundClick()  // ChainBuilder: use .ifFound(new ClickOptions.Builder().build())
        .ifFoundType(username)  // ChainBuilder: use .ifFound(new TypeOptions.Builder().build())
        .execute();
}
```

## Advanced Examples

### Error Recovery

```java
// Note: Convenience methods (ifFoundClick, ifFoundLog) shown conceptually
// Actual ChainBuilder API requires .ifFound(new ClickOptions.Builder().build())
public ActionResult saveWithFallback(StateImage saveButton,
                                   StateImage fileMenu,
                                   StateImage saveMenuItem) {
    return actions.createChain()
        // Try the save button first
        .find(saveButton)
        .ifFoundClick()  // Conceptual - see API warning note

        // If not found, try menu approach
        .ifNotFound(actions.createChain()
            .find(fileMenu)
            .ifFoundClick()  // Conceptual - see API warning note
            .find(saveMenuItem)
            .ifFoundClick()  // Conceptual - see API warning note
            .execute())

        // Log the result
        .ifFoundLog("Document saved successfully")  // This method exists in ChainBuilder
        .ifNotFoundLog("Failed to save document")  // This method exists in ChainBuilder
        .execute();
}
```

### Multi-Step Form Filling

```java
// Supporting data class for form automation
public class RegistrationData {
    private final String name;
    private final String email;
    private final String phone;

    public RegistrationData(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}

@Service
public class FormAutomation {
    @Autowired
    private ConditionalActionWrapper actions;

    // StateImage fields would be initialized in constructor (not shown for brevity)
    private StateImage nameField;
    private StateImage emailField;
    private StateImage phoneField;
    private StateImage submitButton;

    public ActionResult fillRegistrationForm(RegistrationData data) {
        // Note: ChainBuilder convenience methods shown here may not be available
        // See API warning note for details
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

        // Add phone field
        chain.find(phoneField)
            .ifFoundClick()
            .ifFoundType(data.getPhone())
            .ifFoundLog("Entering phone: " + data.getPhone());

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

        // Wait before retry
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Retry interrupted", e);
            break;
        }
    }

    log.error("Failed to click after {} attempts", maxAttempts);
    return new ActionResult.Builder().setSuccess(false).build();
}
```

## Complete Production Example

Here's a fully compilable example showing ConditionalActionWrapper in a real Spring Boot application:

```java
package com.example.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.util.ConditionalActionWrapper;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

/**
 * Complete production example of ConditionalActionWrapper usage.
 * This class demonstrates:
 * - Spring component setup with dependency injection
 * - StateImage initialization
 * - ConditionalActionWrapper methods (findAndClick, findAndType)
 * - Error handling and logging
 * - Production-ready code structure
 */
@Slf4j
@Component
public class LoginAutomationExample {

    private final ConditionalActionWrapper actions;

    // StateImage objects for UI elements
    // In production, these would typically be defined in State classes
    private final StateImage loginButton;
    private final StateImage usernameField;
    private final StateImage passwordField;
    private final StateImage submitButton;
    private final StateImage successMessage;
    private final StateImage errorMessage;

    @Autowired
    public LoginAutomationExample(ConditionalActionWrapper actions) {
        this.actions = actions;

        // Initialize StateImages
        // Image files should be in src/main/resources/images/ directory
        this.loginButton = new StateImage.Builder()
            .addPattern("images/login/login-button.png")
            .setSimilarity(0.85)
            .build();

        this.usernameField = new StateImage.Builder()
            .addPattern("images/login/username-field.png")
            .setSimilarity(0.80)
            .build();

        this.passwordField = new StateImage.Builder()
            .addPattern("images/login/password-field.png")
            .setSimilarity(0.80)
            .build();

        this.submitButton = new StateImage.Builder()
            .addPattern("images/login/submit-button.png")
            .setSimilarity(0.85)
            .build();

        this.successMessage = new StateImage.Builder()
            .addPattern("images/login/success-message.png")
            .setSimilarity(0.90)
            .build();

        this.errorMessage = new StateImage.Builder()
            .addPattern("images/login/error-message.png")
            .setSimilarity(0.90)
            .build();
    }

    /**
     * Performs complete login flow with error handling.
     *
     * @param username User's username
     * @param password User's password
     * @return true if login successful, false otherwise
     */
    public boolean performLogin(String username, String password) {
        log.info("Starting login process for user: {}", username);

        // Step 1: Click login button to open login form
        ActionResult loginClick = actions.findAndClick(loginButton);
        if (!loginClick.isSuccess()) {
            log.error("Login button not found");
            return false;
        }
        log.debug("Login button clicked successfully");

        // Step 2: Enter username
        ActionResult usernameResult = actions.findAndType(usernameField, username);
        if (!usernameResult.isSuccess()) {
            log.error("Username field not found");
            return false;
        }
        log.debug("Username entered successfully");

        // Step 3: Enter password
        ActionResult passwordResult = actions.findAndType(passwordField, password);
        if (!passwordResult.isSuccess()) {
            log.error("Password field not found");
            return false;
        }
        log.debug("Password entered successfully");

        // Step 4: Click submit button
        ActionResult submitResult = actions.findAndClick(submitButton);
        if (!submitResult.isSuccess()) {
            log.error("Submit button not found");
            return false;
        }
        log.debug("Submit button clicked successfully");

        // Step 5: Verify login success or detect error
        ActionResult successCheck = actions.findAndClick(successMessage);
        if (successCheck.isSuccess()) {
            log.info("Login successful for user: {}", username);
            return true;
        }

        ActionResult errorCheck = actions.findAndClick(errorMessage);
        if (errorCheck.isSuccess()) {
            log.error("Login failed - error message displayed");
            return false;
        }

        log.warn("Login result unclear - neither success nor error message found");
        return false;
    }

    /**
     * Simple helper method to click an element if it exists.
     * Demonstrates reusable action pattern.
     *
     * @param element Element to click
     * @return true if clicked, false if not found
     */
    public boolean clickIfVisible(StateImage element) {
        ActionResult result = actions.findAndClick(element);
        if (result.isSuccess()) {
            log.debug("Element clicked successfully");
            return true;
        }
        log.debug("Element not visible, skipping click");
        return false;
    }
}
```

This example is fully compilable and demonstrates:
- **Complete Spring component setup** with `@Component` and `@Autowired`
- **StateImage initialization** with proper builders and similarity thresholds
- **ConditionalActionWrapper methods** (`findAndClick`, `findAndType`)
- **Error handling** with ActionResult checks
- **Logging** at appropriate levels (info, debug, error, warn)
- **Production-ready structure** that can be deployed immediately

To use this in your Spring Boot application:
1. Place image files in `src/main/resources/images/login/` directory
2. Ensure ConditionalActionWrapper is configured as a Spring bean
3. Inject this component into your automation workflows
4. Call `performLogin(username, password)` to execute the login flow

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
            .ifNotFoundLog("Optional element not found")
            .execute();
    }
}
```

### 3. Handle Results Appropriately

```java
// Helper methods for error handling (implement based on your needs)
private void captureScreenshot(String filename) {
    // Implementation example - adapt to your screenshot mechanism
    // Could use Brobot's screenshot capabilities or external tools
    try {
        // Screenshot logic here
        log.debug("Screenshot captured: {}", filename);
    } catch (Exception e) {
        log.error("Failed to capture screenshot", e);
    }
}

private void alertOps(String message) {
    // Implementation example - adapt to your monitoring system
    // Could integrate with PagerDuty, Slack, email, etc.
    log.error("ALERT: {}", message);
    // Add your alerting logic here
}

public boolean performCriticalAction(StateObject target) {
    ActionResult result = actions.findAndClick(target);

    if (!result.isSuccess()) {
        // Log error details
        log.error("Critical action failed: {}", result.getText());

        // Take screenshot for debugging
        captureScreenshot("critical-action-failure");

        // Alert monitoring system
        alertOps("Critical UI element not found");

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

    // Add custom beans for your automation needs
    // Example: Custom action listeners, state managers, etc.
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

    // StateImage for testing (would be defined in test setup)
    private StateImage loginButton;

    @Before
    public void setUp() {
        loginButton = new StateImage.Builder()
            .addPattern("images/login/login-button.png")
            .build();
    }

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

    // Helper method to create successful ActionResult for testing
    private ActionResult successfulResult() {
        return new ActionResult.Builder()
            .setSuccess(true)
            .build();
    }
}
```

## Related Documentation

### Core Concepts
- **[ConditionalActionChain Examples](./15-conditional-chains-examples.md)** - The underlying API that ConditionalActionWrapper uses
- **[States in Brobot](../../01-getting-started/states.md)** - StateImage and StateObject definitions
- **[ActionResult Components](./17-actionresult-components.md)** - Understanding action results

### Alternative Approaches
- **[Conditional Actions](./09-conditional-actions.md)** - Alternative conditional patterns
- **[Complex Workflows](./08-complex-workflows.md)** - Building multi-step automation workflows
- **[Form Automation](./10-form-automation.md)** - Specialized form handling patterns

### ActionConfig Guides
- **[ActionConfig Overview](./01-overview.md)** - Understanding the ActionConfig architecture
- **[Action Chaining](./07-action-chaining.md)** - ActionChainOptions for advanced workflows
- **[Convenience Methods](./18-convenience-methods.md)** - Simpler one-line action API

### Testing
- **[Mock Mode Guide](../../../04-testing/mock-mode-guide.md)** - Testing with mocks
- **[Integration Testing](../../../04-testing/integration-testing.md)** - Spring Boot testing patterns

## Summary

ConditionalActionWrapper enhances Brobot's automation capabilities by:

1. **Providing Spring Integration**: Easy dependency injection and configuration
2. **Simplifying Conditional Logic**: Clean API for complex conditional flows
3. **Improving Error Handling**: Built-in logging and error recovery patterns
4. **Complementing Existing Tools**: Works alongside FindAndClick/FindAndType for different use cases

Choose ConditionalActionWrapper when you need conditional execution, Spring integration, or complex automation flows. Use FindAndClick/FindAndType for simple, always-execute sequences where Spring integration isn't required.