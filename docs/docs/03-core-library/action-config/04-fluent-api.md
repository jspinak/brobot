---
sidebar_position: 4
---

# Fluent API Patterns

The Brobot Fluent API enables you to build automation sequences that read like natural language, making your code more maintainable and self-documenting.

## Introduction

The Fluent API leverages method chaining and builder patterns to create expressive automation workflows:

```java
action.perform(
    find(loginButton)
        .then(click())
        .then(find(usernameField))
        .then(type("user@example.com"))
        .then(find(submitButton))
        .then(click())
);
```

## Core Concepts

### Method Chaining

Actions can be chained using the `then()` method:

```java
ActionConfig chain = firstAction
    .then(secondAction)
    .then(thirdAction);
```

### Builder Pattern

All ActionConfig classes use builders for construction:

```java
ClickOptions click = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPauseBeforeBegin(0.5)
    .build();
```

### Execution Strategies

Two strategies control how chains execute:

1. **NESTED**: Each action searches within previous results
2. **CONFIRM**: Each action validates previous results

## Basic Patterns

### Sequential Actions

Execute actions one after another:

```java
// Click through a wizard
ActionConfig wizard = find(nextButton)
    .then(click())
    .then(find(nextButton))
    .then(click())
    .then(find(finishButton))
    .then(click());

action.perform(wizard);
```

### Conditional Success

Define custom success criteria:

```java
ActionConfig findMultiple = find(icon)
    .setSuccessCriteria(result -> result.getMatchList().size() >= 3)
    .then(click());
```

### Timed Actions

Add pauses between actions:

```java
ActionConfig slowSequence = find(button)
    .setPauseAfterEnd(2.0)  // Wait 2 seconds
    .then(click())
    .setPauseAfterEnd(1.0)  // Wait 1 second
    .then(type("Hello"));
```

## Advanced Patterns

### Nested Search

Find elements within other elements:

```java
ActionChainOptions nestedFind = new ActionChainOptions.Builder(
    find(menuBar)  // Find menu first
)
.setStrategy(ChainingStrategy.NESTED)
.then(find(fileMenu))     // Find within menu bar
.then(click())            // Click file menu
.then(find(saveOption))   // Find within dropdown
.then(click())            // Click save
.build();

actionChainExecutor.executeChain(nestedFind, new ActionResult(), images);
```

### Validation Chains

Confirm each step before proceeding:

```java
ActionChainOptions validated = new ActionChainOptions.Builder(
    click(submitButton)
)
.setStrategy(ChainingStrategy.CONFIRM)
.then(waitToVanish(loadingSpinner))  // Confirm loading done
.then(find(successMessage))           // Confirm success appeared
.build();
```

### Complex Workflows

Build sophisticated automation flows:

```java
public ActionResult performLogin(String username, String password) {
    return action.perform(
        find(loginLink)
            .then(click())
            .then(waitToVanish(loadingOverlay))
            .then(find(usernameField))
            .then(click())
            .then(clearField())
            .then(type(username))
            .then(find(passwordField))
            .then(click())
            .then(type(password))
            .then(find(rememberMe))
            .then(clickIfFound())
            .then(find(submitButton))
            .then(click())
            .then(waitFor(dashboard))
    );
}
```

## Creating Reusable Components

### Action Factories

Create factory methods for common actions:

```java
public class Actions {
    public static ClickOptions singleClick() {
        return new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build();
    }
    
    public static ClickOptions doubleClick() {
        return new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .build();
    }
    
    public static TypeOptions type(String text) {
        return new TypeOptions.Builder()
            .setText(text)
            .build();
    }
    
    public static PatternFindOptions find(double similarity) {
        return new PatternFindOptions.Builder()
            .setSimilarity(similarity)
            .build();
    }
}
```

### Custom Builders

Create domain-specific builders:

```java
public class LoginBuilder {
    private String username;
    private String password;
    private boolean rememberMe;
    
    public LoginBuilder withCredentials(String user, String pass) {
        this.username = user;
        this.password = pass;
        return this;
    }
    
    public LoginBuilder rememberMe(boolean remember) {
        this.rememberMe = remember;
        return this;
    }
    
    public ActionConfig build() {
        ActionConfig.Builder chain = find(usernameField)
            .then(click())
            .then(type(username))
            .then(find(passwordField))
            .then(click())
            .then(type(password));
            
        if (rememberMe) {
            chain = chain
                .then(find(rememberCheckbox))
                .then(click());
        }
        
        return chain
            .then(find(loginButton))
            .then(click())
            .build();
    }
}
```

## Pattern Examples

### Form Filling Pattern

```java
public class FormFiller {
    public ActionConfig fillForm(Map<String, String> fields) {
        ActionConfig.Builder chain = null;
        
        for (Map.Entry<String, String> field : fields.entrySet()) {
            ActionConfig fillField = find(field.getKey())
                .then(click())
                .then(clearField())
                .then(type(field.getValue()))
                .then(pressTab());
                
            if (chain == null) {
                chain = fillField;
            } else {
                chain = chain.then(fillField);
            }
        }
        
        return chain.build();
    }
    
    private ActionConfig clearField() {
        return type("a").withModifiers("ctrl")
            .then(type(Key.DELETE));
    }
    
    private ActionConfig pressTab() {
        return type(Key.TAB);
    }
}
```

### Retry Pattern

```java
public ActionResult retryAction(ActionConfig action, int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        ActionResult result = action.perform(action);
        if (result.isSuccess()) {
            return result;
        }
        
        // Wait before retry
        Thread.sleep(1000 * (i + 1));  // Exponential backoff
    }
    
    return new ActionResult();  // Failed after all retries
}
```

### Navigation Pattern

```java
public class Navigator {
    public ActionConfig navigateToSection(String... menuPath) {
        ActionConfig.Builder navigation = null;
        
        for (String menuItem : menuPath) {
            ActionConfig step = find(menuItem)
                .then(click())
                .then(pause(0.5));
                
            if (navigation == null) {
                navigation = step;
            } else {
                navigation = navigation.then(step);
            }
        }
        
        return navigation.build();
    }
}

// Usage
ActionConfig goToSettings = navigator.navigateToSection(
    "File", "Preferences", "Advanced Settings"
);
```

## Best Practices

### 1. Keep Chains Readable

```java
// Good: Clear, logical flow
action.perform(
    find(searchBox)
        .then(click())
        .then(clearField())
        .then(type(searchTerm))
        .then(find(searchButton))
        .then(click())
);

// Bad: Too compressed
action.perform(find(searchBox).then(click()).then(clearField()).then(type(searchTerm)).then(find(searchButton)).then(click()));
```

### 2. Extract Complex Logic

```java
// Good: Reusable, testable components
private ActionConfig loginSequence(String user, String pass) {
    return find(usernameField)
        .then(click())
        .then(type(user))
        .then(find(passwordField))
        .then(click())
        .then(type(pass))
        .then(find(submitButton))
        .then(click());
}

// Usage
action.perform(loginSequence("user@example.com", "password"));
```

### 3. Use Descriptive Names

```java
// Good: Self-documenting
ActionConfig openFileMenu = find(fileMenu).then(click());
ActionConfig selectSaveOption = find(saveOption).then(click());
ActionConfig confirmSave = find(saveButton).then(click());

ActionConfig saveWorkflow = openFileMenu
    .then(selectSaveOption)
    .then(confirmSave);
```

### 4. Handle Failures Gracefully

```java
ActionConfig robustChain = find(element)
    .setSuccessCriteria(result -> result.isSuccess())
    .then(click())
    .setSuccessCriteria(result -> {
        if (!result.isSuccess()) {
            logger.warn("Click failed, attempting recovery");
            return false;  // Will stop chain
        }
        return true;
    })
    .then(type("data"));
```

### 5. Document Complex Chains

```java
/**
 * Performs a complete checkout process:
 * 1. Adds item to cart
 * 2. Navigates to checkout
 * 3. Fills shipping information
 * 4. Enters payment details
 * 5. Confirms order
 */
public ActionConfig checkoutFlow(ShippingInfo shipping, PaymentInfo payment) {
    return addToCart()
        .then(goToCheckout())
        .then(fillShipping(shipping))
        .then(fillPayment(payment))
        .then(confirmOrder());
}
```

## Debugging Fluent Chains

### Enable Illustration

```java
ActionConfig debugChain = find(element)
    .setIllustrate(ActionConfig.Illustrate.YES)
    .then(click())
    .setIllustrate(ActionConfig.Illustrate.YES)
    .then(type("test"));
```

### Add Logging

```java
public class LoggingActions {
    public ActionConfig find(StateImage image) {
        return new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .setSuccessCriteria(result -> {
                logger.info("Find result: {} matches found", 
                    result.getMatchList().size());
                return result.isSuccess();
            })
            .build();
    }
}
```

### Inspect Execution History

```java
ActionResult result = action.perform(complexChain);

// Analyze what happened
for (ActionRecord record : result.getExecutionHistory()) {
    System.out.printf("Step %s: %s (%.2fs)%n",
        record.getActionType(),
        record.isSuccess() ? "SUCCESS" : "FAILED",
        record.getDuration()
    );
}
```

## Summary

The Fluent API makes Brobot automation code:
- **Readable**: Chains read like natural language
- **Maintainable**: Clear structure and flow
- **Reusable**: Easy to create components
- **Testable**: Each piece can be tested independently
- **Debuggable**: Clear execution flow and history

By mastering these patterns, you can create robust, maintainable automation workflows that clearly express their intent.