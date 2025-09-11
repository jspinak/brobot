package com.example.conditionalchains.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Demonstrates the ConditionalActionChain with all the features from the original documentation now
 * working correctly.
 */
@Component
public class EnhancedChainExample {

    private static final Logger log = LoggerFactory.getLogger(EnhancedChainExample.class);

    private final Action action;

    // Example images for demonstration
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage submitButton;
    private StateImage successMessage;
    private StateImage errorDialog;
    private StateImage confirmDialog;
    private StateImage yesButton;
    private StateImage loadingSpinner;

    public EnhancedChainExample(Action action) {
        this.action = action;
        initializeImages();
    }

    private void initializeImages() {
        // Initialize with example images
        loginButton = new StateImage.Builder().addPattern("images/buttons/login.png").build();

        usernameField = new StateImage.Builder().addPattern("images/forms/username.png").build();

        passwordField = new StateImage.Builder().addPattern("images/forms/password.png").build();

        submitButton = new StateImage.Builder().addPattern("images/buttons/submit.png").build();

        successMessage =
                new StateImage.Builder().addPattern("images/indicators/success.png").build();

        errorDialog = new StateImage.Builder().addPattern("images/indicators/error.png").build();

        confirmDialog =
                new StateImage.Builder().addPattern("images/indicators/confirm.png").build();

        yesButton = new StateImage.Builder().addPattern("images/buttons/yes.png").build();

        loadingSpinner =
                new StateImage.Builder().addPattern("images/indicators/loading.png").build();
    }

    /**
     * Example 1: Login workflow with then() method This now works with the enhanced implementation!
     */
    public void performLoginWorkflow() {
        log.info("Starting login workflow with ConditionalActionChain");

        ActionResult result =
                ConditionalActionChain.find(loginButton)
                        .ifFoundClick()
                        .ifNotFoundLog("Login button not visible")
                        .then(usernameField) // Sequential action using then()
                        .ifFoundClick()
                        .ifFoundType("testuser")
                        .then(passwordField) // Another sequential action
                        .ifFoundClick()
                        .ifFoundType("password123")
                        .then(submitButton) // Continue the flow
                        .ifFoundClick()
                        .then(successMessage) // Check for success
                        .ifFoundLog("Login successful!")
                        .ifNotFoundLog("Login might have failed")
                        .perform(action, new ObjectCollection.Builder().build());

        log.info("Login workflow completed with result: {}", result.isSuccess());
    }

    /** Example 2: Form filling with convenience methods */
    public void fillComplexForm() {
        log.info("Filling complex form with convenience methods");

        ActionResult result =
                ConditionalActionChain.find(new PatternFindOptions.Builder().build())
                        .ifNotFoundLog("Form not visible")
                        .ifNotFoundDo(
                                res -> {
                                    throw new RuntimeException("Cannot proceed without form");
                                })

                        // First field - using clearAndType
                        .then(new StateImage.Builder().addPattern("images/forms/name.png").build())
                        .ifFoundClick()
                        .clearAndType("John Doe")

                        // Email field - using tab navigation
                        .pressTab()
                        .type("john@example.com")

                        // Phone field - using direct navigation
                        .then(new StateImage.Builder().addPattern("images/forms/phone.png").build())
                        .ifFoundClick()
                        .ifFoundType("555-1234")

                        // Submit form
                        .then(submitButton)
                        .ifFoundClick()
                        .takeScreenshot("form-submission")
                        .perform(action, new ObjectCollection.Builder().build());

        log.info("Form filling completed: {}", result.isSuccess());
    }

    /** Example 3: Error handling with conditional logic */
    public void handleErrors() {
        log.info("Demonstrating error handling");

        ActionResult result =
                ConditionalActionChain.find(submitButton)
                        .ifFoundClick()
                        .then(errorDialog)
                        .ifFoundLog("Error dialog appeared")
                        .ifFound(chain -> chain.takeScreenshot("error-state"))
                        .ifFoundDo(
                                res -> {
                                    // Custom error handling logic
                                    log.error("Operation failed with error: {}", res.getText());
                                })
                        .stopIf(
                                res ->
                                        res.getText() != null
                                                && !res.getText().isEmpty()
                                                && res.getText().get(0).contains("CRITICAL"))
                        .then(
                                new StateImage.Builder()
                                        .addPattern("images/buttons/retry.png")
                                        .build())
                        .ifFoundClick()
                        .ifFoundLog("Retrying operation")
                        .perform(action, new ObjectCollection.Builder().build());

        log.info("Error handling completed");
    }

    /** Example 4: Scrolling to find element */
    public void scrollToFind() {
        log.info("Scrolling to find element");

        StateImage targetElement =
                new StateImage.Builder().addPattern("images/ui-elements/target.png").build();

        ActionResult result =
                ConditionalActionChain.find(targetElement)
                        .ifNotFound(chain -> chain.scrollDown())
                        .ifNotFound(new PatternFindOptions.Builder().build())
                        .ifNotFound(chain -> chain.scrollDown())
                        .ifNotFound(new PatternFindOptions.Builder().build())
                        .ifNotFound(chain -> chain.scrollDown())
                        .ifFoundClick()
                        .ifFoundLog("Found and clicked target after scrolling")
                        .ifNotFoundLog("Could not find target even after scrolling")
                        .perform(action, new ObjectCollection.Builder().build());

        log.info("Scroll search completed: {}", result.isSuccess());
    }

    /** Example 5: Keyboard shortcuts workflow */
    public void useKeyboardShortcuts() {
        log.info("Using keyboard shortcuts");

        ActionResult result =
                ConditionalActionChain.find(
                                new StateImage.Builder()
                                        .addPattern("images/forms/editor.png")
                                        .build())
                        .ifFoundClick()
                        .pressCtrlA() // Select all
                        .pressDelete() // Delete content
                        .type("New content here")
                        .pressCtrlS() // Save
                        .then(
                                new StateImage.Builder()
                                        .addPattern("images/indicators/saved.png")
                                        .build())
                        .ifFoundLog("Document saved successfully")
                        .perform(action, new ObjectCollection.Builder().build());

        log.info("Keyboard shortcuts workflow completed");
    }

    /** Example 6: Wait for element to disappear */
    public void waitForLoadingToComplete() {
        log.info("Waiting for loading to complete");

        ActionResult result =
                ConditionalActionChain.find(submitButton)
                        .ifFoundClick()
                        .waitVanish(loadingSpinner) // Wait for spinner to disappear
                        .then(successMessage)
                        .ifFoundLog("Operation completed successfully")
                        .then(errorDialog)
                        .ifFoundLog("Operation failed")
                        .perform(action, new ObjectCollection.Builder().build());

        log.info("Loading wait completed");
    }

    /** Example 7: Retry pattern */
    public void retryOperation() {
        log.info("Demonstrating retry pattern");

        StateImage connectButton =
                new StateImage.Builder().addPattern("images/buttons/connect.png").build();

        ActionResult result =
                ConditionalActionChain.retry(new PatternFindOptions.Builder().build(), 3)
                        .ifFoundClick()
                        .ifFoundLog("Successfully connected after retries")
                        .ifNotFoundLog("Failed to connect after all attempts")
                        .perform(
                                action,
                                new ObjectCollection.Builder().withImages(connectButton).build());

        log.info("Retry operation completed");
    }

    /** Example 8: Complex multi-step workflow from documentation */
    public void saveWithConfirmation() {
        log.info("Save with confirmation dialog");

        StateImage saveButton =
                new StateImage.Builder().addPattern("images/buttons/save.png").build();

        // This example from the documentation now works perfectly!
        ActionResult result =
                ConditionalActionChain.find(saveButton)
                        .ifFoundClick()
                        .ifNotFoundLog("Save button not found")
                        .then(confirmDialog)
                        .then(yesButton)
                        .ifFoundClick()
                        .ifNotFoundLog("No confirmation needed")
                        .then(successMessage)
                        .ifFoundLog("Save successful")
                        .ifNotFoundLog("Save may have failed")
                        .perform(action, new ObjectCollection.Builder().build());

        log.info("Save with confirmation completed: {}", result.isSuccess());
    }

    /** Run all examples to demonstrate the enhanced functionality */
    public void runAllExamples() {
        log.info("=== Running all ConditionalActionChain examples ===");

        try {
            performLoginWorkflow();
            Thread.sleep(1000);

            fillComplexForm();
            Thread.sleep(1000);

            handleErrors();
            Thread.sleep(1000);

            scrollToFind();
            Thread.sleep(1000);

            useKeyboardShortcuts();
            Thread.sleep(1000);

            waitForLoadingToComplete();
            Thread.sleep(1000);

            retryOperation();
            Thread.sleep(1000);

            saveWithConfirmation();

            log.info("=== All examples completed successfully ===");

        } catch (InterruptedException e) {
            log.error("Examples interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
