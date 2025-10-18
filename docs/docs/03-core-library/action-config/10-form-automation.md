---
sidebar_position: 10
title: Form Automation Patterns
description: Master form automation with ConditionalActionChain and modern patterns
---

# Form Automation Patterns

Form automation is one of the most common use cases for UI automation. This guide provides comprehensive patterns using modern Brobot APIs for automating various types of forms efficiently and reliably.

:::info Modern API Focus
This guide uses `ConditionalActionChain`, the recommended modern API for form automation. For lower-level control, see [Action Chaining](./07-action-chaining.md).
:::

## Required Imports

All examples in this guide require these imports:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

## Setup: Dependency Injection

All examples assume this setup:

```java
@Component
public class FormAutomationExamples {
    private static final Logger logger = LoggerFactory.getLogger(FormAutomationExamples.class);

    @Autowired
    private Action action;

    // Your form automation methods go here...
}
```

## Prerequisites

This guide assumes familiarity with:
- **[ActionConfig Overview](./01-overview.md)** - Understanding the ActionConfig system
- **[Action Chaining](./07-action-chaining.md)** - Composing multiple actions together
- **[Conditional Actions](./09-conditional-actions.md)** - Handling dynamic UI scenarios

## Basic Form Elements

### Text Input Fields

Handle different types of text inputs with proper clearing and validation using ConditionalActionChain's built-in methods:

```java
public boolean fillTextField(StateImage field, String value, boolean clearFirst) {
    // Modern approach using ConditionalActionChain
    ConditionalActionChain chain = ConditionalActionChain
        .find(field)
        .ifFoundClick();

    if (clearFirst) {
        chain.ifFoundClearAndType(value);  // Built-in clear and type
    } else {
        chain.ifFoundType(value);  // Type without clearing
    }

    return chain.perform(action).isSuccess();
}
```

**Alternative: Single-line fluent call**:

```java
// For simple cases, use direct fluent API
public boolean fillFieldQuick(StateImage field, String value) {
    return ConditionalActionChain
        .find(field)
        .ifFoundClick()
        .ifFoundClearAndType(value)
        .perform(action)
        .isSuccess();
}
```

### Dropdown Menus

Handle dropdown selections with built-in retry and scrolling:

```java
public boolean selectDropdownOption(StateImage dropdown, StateImage option) {
    // Modern approach with ConditionalActionChain
    ClickOptions clickWithDelay = new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)  // Wait for dropdown to open
        .build();

    return ConditionalActionChain
        .find(dropdown)
        .ifFoundClick(clickWithDelay)  // Open dropdown
        .then(option)
        .ifFoundClick()  // Click option if visible
        .ifNotFoundScrollDown()  // Scroll if not visible
        .ifNotFoundScrollDown()  // Retry scroll
        .ifNotFoundScrollDown()  // Retry scroll
        .then(option)  // Look again after scrolling
        .ifFoundClick()
        .perform(action, new ObjectCollection.Builder()
            .withImages(dropdown, option)
            .build())
        .isSuccess();
}
```

**Alternative: Custom scroll amount**:

```java
public boolean selectWithCustomScroll(StateImage dropdown, StateImage option, int scrollClicks) {
    return ConditionalActionChain
        .find(dropdown)
        .ifFoundClick()
        .then(option)
        .ifNotFoundDo(chain -> {
            // Scroll multiple times if needed
            for (int i = 0; i < scrollClicks; i++) {
                chain.scrollDown();
            }
        })
        .then(option)
        .ifFoundClick()
        .perform(action)
        .isSuccess();
}
```

### Checkboxes and Radio Buttons

Handle toggle states intelligently:

```java
public boolean setCheckboxState(StateImage checkbox,
                               StateImage checkedIndicator,
                               boolean desiredState) {
    // First, determine current state
    ActionResult checkResult = action.find(checkedIndicator);
    boolean isCurrentlyChecked = checkResult.isSuccess();

    // If already in desired state, done
    if (isCurrentlyChecked == desiredState) {
        return true;
    }

    // Click to toggle with verification
    ClickOptions clickWithPause = new ClickOptions.Builder()
        .setPauseAfterEnd(0.3)  // Wait for state change
        .build();

    ActionResult clickResult = action.perform(clickWithPause, checkbox.asObjectCollection());

    if (!clickResult.isSuccess()) {
        return false;
    }

    // Verify the state changed
    ActionResult verifyResult = action.find(checkedIndicator);
    boolean newState = verifyResult.isSuccess();

    return newState == desiredState;
}

public boolean selectRadioButton(StateImage radioButton,
                               StateImage selectedIndicator) {
    // Check if already selected
    ActionResult checkResult = action.find(selectedIndicator);
    if (checkResult.isSuccess()) {
        return true;
    }

    // Click to select with verification
    return ConditionalActionChain
        .find(radioButton)
        .ifFoundClick()
        .then(selectedIndicator)
        .ifFoundLog("Radio button selected successfully")
        .ifNotFoundLog("Radio button selection failed")
        .perform(action)
        .isSuccess();
}
```

**Alternative: Using ConditionalActionChain**:

```java
public boolean setCheckboxModern(StateImage checkbox, StateImage checkedIndicator, boolean desiredState) {
    // Check current state
    boolean isChecked = action.find(checkedIndicator).isSuccess();

    if (isChecked == desiredState) {
        return true;  // Already in desired state
    }

    // Toggle checkbox and verify
    return ConditionalActionChain
        .find(checkbox)
        .ifFoundClick()
        .then(checkedIndicator)
        .perform(action)
        .isSuccess() == desiredState;
}
```

## Complex Form Patterns

### Multi-Step Forms

Handle wizard-style forms with navigation using ConditionalActionChain:

```java
@Component
public class WizardFormHandler {
    private static final Logger logger = LoggerFactory.getLogger(WizardFormHandler.class);

    @Autowired
    private Action action;

    public boolean completeWizard(List<WizardStep> steps,
                                 StateImage nextButton,
                                 StateImage finishButton) {

        for (int i = 0; i < steps.size(); i++) {
            WizardStep step = steps.get(i);

            // Complete the current step
            if (!step.complete(action)) {
                logger.error("Failed at step {}: {}", i + 1, step.getName());
                return false;
            }

            // Navigate to next step or finish
            if (i < steps.size() - 1) {
                // Navigate to next step with verification
                ClickOptions clickWithDelay = new ClickOptions.Builder()
                    .setPauseAfterEnd(1.0)  // Wait for page transition
                    .build();

                boolean navigated = ConditionalActionChain
                    .find(nextButton)
                    .ifFoundClick(clickWithDelay)
                    .then(step.getNextStepIndicator())
                    .ifFoundLog("Successfully navigated to step " + (i + 2))
                    .ifNotFoundLog("Failed to navigate to step " + (i + 2))
                    .perform(action)
                    .isSuccess();

                if (!navigated) {
                    return false;
                }
            } else {
                // Last step - click finish
                boolean submitted = ConditionalActionChain
                    .find(finishButton)
                    .ifFoundClick()
                    .perform(action)
                    .isSuccess();

                if (!submitted) {
                    return false;
                }
            }
        }

        return true;
    }

    public static abstract class WizardStep {
        protected final String name;
        protected final StateImage nextStepIndicator;

        protected WizardStep(String name, StateImage nextStepIndicator) {
            this.name = name;
            this.nextStepIndicator = nextStepIndicator;
        }

        public abstract boolean complete(Action action);

        public String getName() { return name; }
        public StateImage getNextStepIndicator() { return nextStepIndicator; }
    }
}
```

### Form Validation Handling

Deal with validation errors gracefully using ConditionalActionChain:

```java
@Component
public class FormValidationHandler {
    private static final Logger logger = LoggerFactory.getLogger(FormValidationHandler.class);

    @Autowired
    private Action action;

    public boolean submitFormWithValidation(StateImage submitButton,
                                          StateImage successIndicator,
                                          StateImage errorIndicator,
                                          int maxRetries) {

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            logger.info("Form submission attempt {} of {}", attempt + 1, maxRetries);

            // Submit form with delay for processing
            ClickOptions submitClick = new ClickOptions.Builder()
                .setPauseAfterEnd(1.0)  // Wait for server response
                .build();

            ActionResult submitResult = ConditionalActionChain
                .find(submitButton)
                .ifFoundClick(submitClick)
                .perform(action);

            if (!submitResult.isSuccess()) {
                logger.warn("Failed to click submit button on attempt {}", attempt + 1);
                continue;
            }

            // Check for success
            ActionResult successCheck = action.find(successIndicator);
            if (successCheck.isSuccess()) {
                logger.info("Form submitted successfully");
                return true;
            }

            // Check for validation errors
            ActionResult errorCheck = action.find(errorIndicator);
            if (errorCheck.isSuccess()) {
                logger.warn("Validation error detected on attempt {}", attempt + 1);
                // Handle specific error
                if (!handleValidationError(errorIndicator)) {
                    logger.error("Unable to resolve validation error");
                    return false;
                }
            }
        }

        logger.error("Form submission failed after {} attempts", maxRetries);
        return false;
    }

    private boolean handleValidationError(StateImage errorIndicator) {
        // Read error message if possible
        // Fix the specific field based on error
        // This would be customized per application
        logger.info("Handling validation error for: {}", errorIndicator.getName());
        return true;
    }
}
```

### Dynamic Form Fields

Handle forms that add/remove fields dynamically using ConditionalActionChain:

```java
@Component
public class DynamicFormHandler {
    private static final Logger logger = LoggerFactory.getLogger(DynamicFormHandler.class);

    @Autowired
    private Action action;

    public boolean fillDynamicList(StateImage addButton,
                                  StateImage fieldTemplate,
                                  List<String> values) {

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            logger.info("Adding dynamic field {} with value: {}", i + 1, value);

            // Click add button to create new field with delay
            ClickOptions addClick = new ClickOptions.Builder()
                .setPauseAfterEnd(0.5)  // Wait for field to appear
                .build();

            ActionResult addResult = ConditionalActionChain
                .find(addButton)
                .ifFoundClick(addClick)
                .perform(action);

            if (!addResult.isSuccess()) {
                logger.error("Failed to click add button for field {}", i + 1);
                return false;
            }

            // Find and fill the newly created field
            boolean fieldFilled = ConditionalActionChain
                .find(fieldTemplate)
                .ifFoundClick()
                .ifFoundClearAndType(value)
                .ifNotFoundLog("Failed to find field template for value: " + value)
                .perform(action)
                .isSuccess();

            if (!fieldFilled) {
                logger.error("Failed to fill dynamic field {} with value: {}", i + 1, value);
                return false;
            }
        }

        logger.info("Successfully filled {} dynamic fields", values.size());
        return true;
    }

    /**
     * Alternative implementation: Fill dynamic fields by index
     */
    public boolean fillDynamicFieldByIndex(StateImage addButton,
                                          StateImage fieldTemplate,
                                          String value,
                                          int fieldIndex) {

        // Add field if needed
        for (int i = 0; i < fieldIndex; i++) {
            ClickOptions addClick = new ClickOptions.Builder()
                .setPauseAfterEnd(0.3)
                .build();

            boolean added = ConditionalActionChain
                .find(addButton)
                .ifFoundClick(addClick)
                .perform(action)
                .isSuccess();

            if (!added) {
                logger.error("Failed to add field at index {}", i);
                return false;
            }
        }

        // Fill the specific field
        return ConditionalActionChain
            .find(fieldTemplate)
            .ifFoundClick()
            .ifFoundClearAndType(value)
            .perform(action)
            .isSuccess();
    }
}
```

## Complete Form Example (Legacy Pattern)

Here's a comprehensive example using traditional approach. For a modern approach with ConditionalActionChain, see the next section.

```java
@Component
public class RegistrationFormAutomation {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationFormAutomation.class);

    @Autowired
    private Action action;

    @Autowired
    private RegistrationFormState formState;

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
        // Fill first name
        boolean firstNameFilled = ConditionalActionChain
            .find(formState.getFirstNameField())
            .ifFoundClick()
            .ifFoundClearAndType(userData.getFirstName())
            .perform(action)
            .isSuccess();

        if (!firstNameFilled) {
            return false;
        }

        // Fill last name
        boolean lastNameFilled = ConditionalActionChain
            .find(formState.getLastNameField())
            .ifFoundClick()
            .ifFoundClearAndType(userData.getLastName())
            .perform(action)
            .isSuccess();

        if (!lastNameFilled) {
            return false;
        }

        // Email with validation
        return fillEmailWithValidation(userData.getEmail());
    }

    private boolean fillEmailWithValidation(String email) {
        // Fill email and trigger validation with Tab
        boolean emailFilled = ConditionalActionChain
            .find(formState.getEmailField())
            .ifFoundClick()
            .ifFoundClearAndType(email)
            .pressTab()  // Trigger validation
            .perform(action)
            .isSuccess();

        if (!emailFilled) {
            return false;
        }

        // Check validation result
        ActionResult validCheck = action.find(formState.getEmailValidIndicator());
        ActionResult invalidCheck = action.find(formState.getEmailInvalidIndicator());

        if (invalidCheck.isSuccess()) {
            logger.error("Invalid email format: {}", email);
            return false;
        }

        return validCheck.isSuccess();
    }

    private boolean setPreferences(UserPreferences prefs) {
        // Newsletter subscription checkbox
        boolean isNewsletterChecked = action.find(formState.getNewsletterChecked()).isSuccess();

        if (isNewsletterChecked != prefs.wantsNewsletter()) {
            // Toggle checkbox
            boolean toggled = ConditionalActionChain
                .find(formState.getNewsletterCheckbox())
                .ifFoundClick()
                .perform(action)
                .isSuccess();

            if (!toggled) {
                return false;
            }
        }

        // Notification preference (radio buttons)
        StateImage notificationOption = prefs.getNotificationPreference().equals("email")
            ? formState.getEmailNotifications()
            : formState.getSmsNotifications();

        return ConditionalActionChain
            .find(notificationOption)
            .ifFoundClick()
            .then(formState.getSelectedRadio())
            .ifFoundLog("Notification preference set")
            .perform(action)
            .isSuccess();
    }

    private boolean acceptTerms() {
        return ConditionalActionChain
            .find(formState.getTermsCheckbox())
            .ifFoundClick()
            .perform(action)
            .isSuccess();
    }

    private boolean submitForm() {
        // Use validation handler for robust submission
        FormValidationHandler validationHandler = new FormValidationHandler();

        return validationHandler.submitFormWithValidation(
            formState.getSubmitButton(),
            formState.getSuccessMessage(),
            formState.getErrorMessage(),
            3 // max retries
        );
    }
}
```

## Complete Form Example with ConditionalActionChain

Here's a modern, complete registration form using ConditionalActionChain:

```java
@Component
public class ModernRegistrationForm {
    private static final Logger logger = LoggerFactory.getLogger(ModernRegistrationForm.class);

    @Autowired
    private Action action;

    // Assume StateImages are injected from your State class
    @Autowired
    private RegistrationFormState formState;

    public boolean completeRegistration(String firstName, String lastName, String email) {
        // Modern fluent form automation
        return ConditionalActionChain
            // Fill first name
            .find(formState.getFirstNameField())
            .ifFoundClick()
            .ifFoundClearAndType(firstName)

            // Fill last name
            .then(formState.getLastNameField())
            .ifFoundClick()
            .ifFoundClearAndType(lastName)

            // Fill email and validate
            .then(formState.getEmailField())
            .ifFoundClick()
            .ifFoundClearAndType(email)
            .pressTab()  // Trigger validation

            // Check validation passed
            .then(formState.getEmailValidIndicator())
            .ifNotFoundLog("Email validation failed: " + email)
            .ifNotFoundDo(chain -> chain.throwError("Invalid email"))

            // Accept terms checkbox
            .then(formState.getTermsCheckbox())
            .ifFoundClick()

            // Submit form
            .then(formState.getSubmitButton())
            .ifFoundClick()

            // Verify success
            .then(formState.getSuccessMessage())
            .ifFoundLog("Registration successful!")
            .ifNotFoundLog("Registration failed")

            .perform(action)
            .isSuccess();
    }
}
```

## Best Practices

### 1. Use ConditionalActionChain for Forms
```java
// ✅ RECOMMENDED - Modern fluent API
ConditionalActionChain
    .find(field)
    .ifFoundClick()
    .ifFoundClearAndType("value")
    .perform(action);

// ❌ AVOID - Low-level ActionChainExecutor
ActionChainOptions chain = new ActionChainOptions.Builder(...)
    .then(...)
    .build();
chainExecutor.executeChain(chain, ...);
```

### 2. Use Built-In clearAndType()
```java
// ✅ GOOD - Built-in method
chain.ifFoundClearAndType("new value");

// ❌ BAD - Manual Ctrl+A implementation
chain.pressCtrlA().pressDelete().ifFoundType("new value");
```

### 3. Configure Delays in ActionConfig
```java
// ✅ CORRECT - Use setPauseAfterEnd()
ClickOptions clickWithDelay = new ClickOptions.Builder()
    .setPauseAfterEnd(0.5)
    .build();
action.perform(clickWithDelay, field);

// ❌ FORBIDDEN - pause() method doesn't exist
action.click(field);
pause(0.5);  // THIS METHOD DOES NOT EXIST
```

**Recommended delays**:
- After clicks: 0.2-0.5 seconds
- After form submission: 1-3 seconds
- Use `setPauseAfterEnd()` in ActionConfig

### 4. Verify State with Conditionals
```java
// ✅ GOOD - Built-in verification
ConditionalActionChain
    .find(checkbox)
    .ifFoundClick()
    .then(checkedIndicator)
    .ifFoundLog("Checkbox checked")
    .ifNotFoundLog("Checkbox check failed")
    .perform(action);

// ⚠️ OKAY - Manual verification
ActionResult result = action.click(checkbox);
if (!result.isSuccess()) {
    logger.error("Failed to click checkbox");
}
```

### 5. Use Keyboard Shortcuts
```java
// ConditionalActionChain has built-in shortcuts
chain.pressTab()       // Tab to next field
    .pressEnter()      // Submit form
    .pressEscape()     // Cancel
    .pressCtrlS()      // Save
    .pressCtrlA()      // Select all
```

### 6. Handle Validation Errors
```java
public boolean fillEmailWithValidation(StateImage emailField, String email) {
    return ConditionalActionChain
        .find(emailField)
        .ifFoundClearAndType(email)
        .pressTab()  // Trigger validation
        .then(emailValidIndicator)
        .ifNotFoundLog("Email validation failed")
        .ifNotFoundDo(chain -> {
            // Handle error - could retry, log, or throw
            logger.error("Invalid email: {}", email);
            return false;
        })
        .perform(action)
        .isSuccess();
}
```

### 7. Create Reusable Form Components
Build type-safe form components:

```java
@Component
public class FormComponents {
    @Autowired
    private Action action;

    public boolean fillTextField(StateImage field, String value) {
        return ConditionalActionChain
            .find(field)
            .ifFoundClick()
            .ifFoundClearAndType(value)
            .perform(action)
            .isSuccess();
    }

    public boolean selectCheckbox(StateImage checkbox) {
        return ConditionalActionChain
            .find(checkbox)
            .ifFoundClick()
            .perform(action)
            .isSuccess();
    }
}
```

## Next Steps

### Related Guides
- **[Conditional Action Chains](./15-conditional-chains-examples.md)** - Comprehensive conditional examples
- **[Action Chaining](./07-action-chaining.md)** - Understanding action composition
- **[Conditional Actions](./09-conditional-actions.md)** - Retry patterns and dynamic UI
- **[Complex Workflows](./08-complex-workflows.md)** - Multi-step automation patterns
- **[Reusable Patterns](./11-reusable-patterns.md)** - Building component libraries

### Reference Documentation
- **[ActionConfig Overview](./01-overview.md)** - Conceptual foundation
- **[API Reference](./05-reference.md)** - Complete API documentation
- **[Upgrading to Latest](../migration/upgrading-to-latest.md)** - Updating legacy form automation code