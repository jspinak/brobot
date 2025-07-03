---
sidebar_position: 10
title: Form Automation Patterns
description: Master form automation with reusable patterns and best practices
---

# Form Automation Patterns

Form automation is one of the most common use cases for UI automation. This guide provides comprehensive patterns and reusable components for automating various types of forms efficiently and reliably.

## Basic Form Elements

### Text Input Fields

Handle different types of text inputs with proper clearing and validation:

```java
public class FormFieldHandler {
    
    public boolean fillTextField(StateImage field, String value, boolean clearFirst) {
        ActionChainOptions.Builder chainBuilder = new ActionChainOptions.Builder(
            // Click to focus the field
            new ClickOptions.Builder()
                .setPauseAfterEnd(0.2)
                .build());
        
        if (clearFirst) {
            // Select all and delete
            chainBuilder
                .then(new KeyDownOptions.Builder()
                    .setKey("ctrl")
                    .build())
                .then(new TypeOptions.Builder()
                    .setText("a")
                    .build())
                .then(new KeyUpOptions.Builder()
                    .setKey("ctrl")
                    .build())
                .then(new TypeOptions.Builder()
                    .setText("\b") // Backspace
                    .build());
        }
        
        // Type the new value
        chainBuilder.then(new TypeOptions.Builder()
            .setText(value)
            .setTypeDelay(0.05)
            .build());
        
        ActionChainOptions chain = chainBuilder.build();
        
        return chainExecutor.executeChain(chain, new ActionResult(),
            field.asObjectCollection(),
            // Empty collections for ctrl+a operations
            new ObjectCollection.Builder().build(),
            new ObjectCollection.Builder().withStrings("a").build(),
            new ObjectCollection.Builder().build(),
            new ObjectCollection.Builder().withStrings("\b").build(),
            new ObjectCollection.Builder().withStrings(value).build()
        ).isSuccess();
    }
}
```

### Dropdown Menus

Handle dropdown selections with retry logic:

```java
public boolean selectDropdownOption(StateImage dropdown, StateImage option) {
    // Click dropdown to open
    ActionChainOptions openAndSelect = new ActionChainOptions.Builder(
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.5) // Wait for dropdown to open
            .build())
        // Find the option
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .setPauseBeforeBegin(0.3)
            .build())
        // Click the option
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.3)
            .build())
        .build();
    
    ActionResult result = chainExecutor.executeChain(
        openAndSelect, new ActionResult(),
        dropdown.asObjectCollection(),
        option.asObjectCollection(),
        option.asObjectCollection()
    );
    
    // If failed, try with scrolling
    if (!result.isSuccess()) {
        return selectWithScrolling(dropdown, option);
    }
    
    return true;
}

private boolean selectWithScrolling(StateImage dropdown, StateImage option) {
    // Click to ensure dropdown is open
    click(dropdown);
    
    // Try scrolling to find the option
    for (int i = 0; i < 5; i++) {
        if (find(option)) {
            return click(option);
        }
        
        // Scroll down
        scrollMouseWheel(new ScrollOptions.Builder()
            .setDirection(ScrollOptions.Direction.DOWN)
            .setScrollSteps(3)
            .build());
        
        pause(0.3);
    }
    
    return false;
}
```

### Checkboxes and Radio Buttons

Handle toggle states intelligently:

```java
public class CheckboxHandler {
    
    public boolean setCheckboxState(StateImage checkbox, 
                                   StateImage checkedIndicator,
                                   boolean desiredState) {
        // First, determine current state
        boolean isCurrentlyChecked = find(checkedIndicator);
        
        // If already in desired state, done
        if (isCurrentlyChecked == desiredState) {
            return true;
        }
        
        // Click to toggle
        ActionResult result = click(checkbox);
        
        if (!result.isSuccess()) {
            return false;
        }
        
        // Verify the state changed
        pause(0.3);
        boolean newState = find(checkedIndicator);
        
        return newState == desiredState;
    }
    
    public boolean selectRadioButton(StateImage radioButton,
                                   StateImage selectedIndicator) {
        // Check if already selected
        if (find(selectedIndicator)) {
            return true;
        }
        
        // Click to select
        ActionResult result = click(radioButton);
        
        if (!result.isSuccess()) {
            return false;
        }
        
        // Verify selection
        pause(0.3);
        return find(selectedIndicator);
    }
}
```

## Complex Form Patterns

### Multi-Step Forms

Handle wizard-style forms with navigation:

```java
public class WizardFormHandler {
    
    public boolean completeWizard(List<WizardStep> steps,
                                 StateImage nextButton,
                                 StateImage finishButton) {
        
        for (int i = 0; i < steps.size(); i++) {
            WizardStep step = steps.get(i);
            
            // Complete the current step
            if (!step.complete()) {
                logger.error("Failed at step {}: {}", i + 1, step.getName());
                return false;
            }
            
            // Navigate to next step or finish
            if (i < steps.size() - 1) {
                if (!clickAndVerify(nextButton, step.getNextStepIndicator())) {
                    return false;
                }
            } else {
                // Last step - click finish
                if (!click(finishButton)) {
                    return false;
                }
            }
            
            // Wait for page transition
            pause(1.0);
        }
        
        return true;
    }
    
    public static abstract class WizardStep {
        protected final String name;
        protected final StateImage nextStepIndicator;
        
        public abstract boolean complete();
        
        public String getName() { return name; }
        public StateImage getNextStepIndicator() { return nextStepIndicator; }
    }
}
```

### Form Validation Handling

Deal with validation errors gracefully:

```java
public class FormValidationHandler {
    
    public boolean submitFormWithValidation(StateImage submitButton,
                                          StateImage successIndicator,
                                          StateImage errorIndicator,
                                          int maxRetries) {
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            // Click submit
            ActionResult submitResult = click(submitButton);
            
            if (!submitResult.isSuccess()) {
                continue;
            }
            
            // Wait for response
            pause(1.0);
            
            // Check for success
            if (find(successIndicator)) {
                return true;
            }
            
            // Check for errors
            if (find(errorIndicator)) {
                // Handle specific error
                if (!handleValidationError(errorIndicator)) {
                    return false;
                }
            }
            
            // Retry after fixing errors
            pause(0.5);
        }
        
        return false;
    }
    
    private boolean handleValidationError(StateImage errorIndicator) {
        // Read error message if possible
        // Fix the specific field based on error
        // This would be customized per application
        return true;
    }
}
```

### Dynamic Form Fields

Handle forms that add/remove fields dynamically:

```java
public class DynamicFormHandler {
    
    public boolean fillDynamicList(StateImage addButton,
                                  StateImage fieldTemplate,
                                  List<String> values) {
        
        for (String value : values) {
            // Click add button to create new field
            if (!click(addButton)) {
                return false;
            }
            
            pause(0.5);
            
            // Find the newly created field
            ActionResult findResult = find(fieldTemplate);
            
            if (!findResult.isSuccess()) {
                return false;
            }
            
            // Fill the field
            ActionChainOptions fillChain = new ActionChainOptions.Builder(
                new ClickOptions.Builder()
                    .setPauseAfterEnd(0.2)
                    .build())
                .then(new TypeOptions.Builder()
                    .setText(value)
                    .build())
                .build();
            
            ActionResult fillResult = chainExecutor.executeChain(
                fillChain, new ActionResult(),
                findResult.getMatchedRegions().get(0).asObjectCollection(),
                new ObjectCollection.Builder().withStrings(value).build()
            );
            
            if (!fillResult.isSuccess()) {
                return false;
            }
        }
        
        return true;
    }
}
```

## Complete Form Example

Here's a comprehensive example that ties everything together:

```java
public class RegistrationFormAutomation {
    private final FormFieldHandler fieldHandler;
    private final CheckboxHandler checkboxHandler;
    
    public boolean completeRegistration(UserData userData) {
        // Fill basic information
        if (!fillBasicInfo(userData)) {
            return false;
        }
        
        // Fill address
        if (!fillAddress(userData.getAddress())) {
            return false;
        }
        
        // Set preferences
        if (!setPreferences(userData.getPreferences())) {
            return false;
        }
        
        // Accept terms
        if (!acceptTerms()) {
            return false;
        }
        
        // Submit form
        return submitForm();
    }
    
    private boolean fillBasicInfo(UserData userData) {
        // First name
        if (!fieldHandler.fillTextField(
                getStateImage("firstName"), 
                userData.getFirstName(), 
                true)) {
            return false;
        }
        
        // Last name
        if (!fieldHandler.fillTextField(
                getStateImage("lastName"), 
                userData.getLastName(), 
                true)) {
            return false;
        }
        
        // Email with validation
        return fillEmailWithValidation(userData.getEmail());
    }
    
    private boolean fillEmailWithValidation(String email) {
        StateImage emailField = getStateImage("email");
        StateImage validIndicator = getStateImage("emailValid");
        StateImage invalidIndicator = getStateImage("emailInvalid");
        
        // Fill email
        if (!fieldHandler.fillTextField(emailField, email, true)) {
            return false;
        }
        
        // Tab out to trigger validation
        type("\t");
        pause(0.5);
        
        // Check validation
        if (find(invalidIndicator)) {
            logger.error("Invalid email format: {}", email);
            return false;
        }
        
        return find(validIndicator);
    }
    
    private boolean setPreferences(UserPreferences prefs) {
        // Newsletter subscription
        if (!checkboxHandler.setCheckboxState(
                getStateImage("newsletterCheckbox"),
                getStateImage("newsletterChecked"),
                prefs.wantsNewsletter())) {
            return false;
        }
        
        // Notification preference (radio buttons)
        StateImage notificationOption = prefs.getNotificationPreference()
            .equals("email") ? getStateImage("emailNotifications") 
                            : getStateImage("smsNotifications");
        
        return checkboxHandler.selectRadioButton(
            notificationOption,
            getStateImage("selectedRadio")
        );
    }
    
    private boolean submitForm() {
        StateImage submitButton = getStateImage("submit");
        StateImage successMessage = getStateImage("registrationSuccess");
        StateImage errorMessage = getStateImage("registrationError");
        
        // Use validation handler for robust submission
        FormValidationHandler validationHandler = new FormValidationHandler();
        
        return validationHandler.submitFormWithValidation(
            submitButton,
            successMessage,
            errorMessage,
            3 // max retries
        );
    }
}
```

## Best Practices

### 1. Always Clear Fields First
```java
// Good
fieldHandler.fillTextField(field, newValue, true); // clearFirst = true

// Bad - might append to existing text
fieldHandler.fillTextField(field, newValue, false);
```

### 2. Add Appropriate Delays
- After clicks: 0.2-0.5 seconds
- After form submission: 1-3 seconds
- Between keystrokes: 0.05-0.1 seconds

### 3. Verify State Changes
Always verify that actions had the intended effect:
```java
// Good
if (!setCheckboxState(checkbox, indicator, true)) {
    logger.error("Failed to check checkbox");
    return false;
}

// Bad - assumes success
click(checkbox);
```

### 4. Handle Dynamic Loading
```java
public boolean waitForFieldEnabled(StateImage field, double timeout) {
    long startTime = System.currentTimeMillis();
    
    while ((System.currentTimeMillis() - startTime) < timeout * 1000) {
        if (isFieldEnabled(field)) {
            return true;
        }
        pause(0.5);
    }
    
    return false;
}
```

### 5. Create Reusable Components
Build a library of form handling utilities that can be reused across different forms and applications.

## Next Steps

- Explore [Reusable Patterns](./11-reusable-patterns.md) for building component libraries
- See [Migration Guide](./12-migration-guide.md) for updating legacy form automation code