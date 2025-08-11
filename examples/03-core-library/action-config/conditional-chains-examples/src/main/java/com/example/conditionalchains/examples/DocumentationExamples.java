package com.example.conditionalchains.examples;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Examples directly from the Enhanced Conditional Action Chains documentation.
 * All code here should match exactly what appears in the documentation.
 */
@Component
public class DocumentationExamples {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentationExamples.class);
    private final Action action;
    
    // Example state images
    private StateImage buttonImage;
    private StateImage menuButton;
    private StateImage searchField;
    private StateImage submitButton;
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage successMessage;
    private StateImage confirmDialog;
    private StateImage yesButton;
    private StateImage saveButton;
    private StateImage nameField;
    private StateImage phoneField;
    private StateImage editorField;
    private StateImage savedIndicator;
    private StateImage errorDialog;
    private StateImage retryButton;
    private StateImage loadingSpinner;
    private StateImage targetElement;
    private StateImage navigationButton;
    
    public DocumentationExamples(Action action) {
        this.action = action;
        initializeImages();
    }
    
    private void initializeImages() {
        buttonImage = new StateImage.Builder()
            .addPattern("images/buttons/button.png")
            .build();
        menuButton = new StateImage.Builder()
            .addPattern("images/buttons/menu.png")
            .build();
        searchField = new StateImage.Builder()
            .addPattern("images/forms/search.png")
            .build();
        submitButton = new StateImage.Builder()
            .addPattern("images/buttons/submit.png")
            .build();
        loginButton = new StateImage.Builder()
            .addPattern("images/buttons/login.png")
            .build();
        usernameField = new StateImage.Builder()
            .addPattern("images/forms/username.png")
            .build();
        passwordField = new StateImage.Builder()
            .addPattern("images/forms/password.png")
            .build();
        successMessage = new StateImage.Builder()
            .addPattern("images/indicators/success.png")
            .build();
        confirmDialog = new StateImage.Builder()
            .addPattern("images/indicators/confirm.png")
            .build();
        yesButton = new StateImage.Builder()
            .addPattern("images/buttons/yes.png")
            .build();
        saveButton = new StateImage.Builder()
            .addPattern("images/buttons/save.png")
            .build();
        nameField = new StateImage.Builder()
            .addPattern("images/forms/name.png")
            .build();
        phoneField = new StateImage.Builder()
            .addPattern("images/forms/phone.png")
            .build();
        editorField = new StateImage.Builder()
            .addPattern("images/forms/editor.png")
            .build();
        savedIndicator = new StateImage.Builder()
            .addPattern("images/indicators/saved.png")
            .build();
        errorDialog = new StateImage.Builder()
            .addPattern("images/indicators/error.png")
            .build();
        retryButton = new StateImage.Builder()
            .addPattern("images/buttons/retry.png")
            .build();
        loadingSpinner = new StateImage.Builder()
            .addPattern("images/indicators/loading.png")
            .build();
        targetElement = new StateImage.Builder()
            .addPattern("images/ui-elements/target.png")
            .build();
        navigationButton = new StateImage.Builder()
            .addPattern("images/buttons/navigation.png")
            .build();
    }
    
    // ===== Basic Examples =====
    
    public void simpleFindAndClick() {
        // Basic find and click pattern
        ConditionalActionChain.find(buttonImage)
            .ifFoundClick()
            .ifNotFoundLog("Button not found")
            .perform(action, new ObjectCollection.Builder().build());
    }
    
    public void sequentialActionsWithThen() {
        // The then() method enables sequential workflows
        ConditionalActionChain.find(menuButton)
            .ifFoundClick()
            .then(searchField)  // Move to next element
            .ifFoundClick()
            .ifFoundType("search query")
            .then(submitButton) // Continue the flow
            .ifFoundClick()
            .perform(action, new ObjectCollection.Builder().build());
    }
    
    // ===== Real-World Scenarios =====
    
    public ActionResult performLogin(String username, String password) {
        return ConditionalActionChain.find(loginButton)
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
    
    public ActionResult saveWithConfirmation() {
        StateImage saveButton = new StateImage.Builder()
            .addPattern("images/buttons/save.png")
            .build();
        
        return ConditionalActionChain.find(saveButton)
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
    
    public ActionResult clickWithRetry(StateImage target, int maxRetries) {
        return ConditionalActionChain
            .retry(new PatternFindOptions.Builder().build(), maxRetries)
            .ifFoundClick()
            .ifFoundLog("Successfully clicked after retries")
            .ifNotFoundLog("Failed after all attempts")
            .perform(action, new ObjectCollection.Builder()
                .withImages(target)
                .build());
    }
    
    // ===== Advanced Patterns =====
    
    public ActionResult fillComplexForm(FormData data) {
        return ConditionalActionChain
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
    
    public ActionResult scrollToFind(StateImage target) {
        return ConditionalActionChain.find(target)
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
    
    public ActionResult useKeyboardShortcuts() {
        return ConditionalActionChain
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
    
    // ===== Conditional Patterns =====
    
    public ActionResult handleErrors() {
        return ConditionalActionChain
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
    
    public ActionResult waitForLoadingToComplete() {
        StateImage loadingSpinner = new StateImage.Builder()
            .addPattern("images/indicators/loading.png")
            .build();
        
        return ConditionalActionChain
            .find(submitButton)
            .ifFoundClick()
            .waitVanish(loadingSpinner)  // Wait for spinner to disappear
            .then(successMessage)
            .ifFoundLog("Operation completed successfully")
            .then(errorDialog)
            .ifFoundLog("Operation failed")
            .perform(action, new ObjectCollection.Builder().build());
    }
    
    public ActionResult debugWorkflow() {
        return ConditionalActionChain
            .find(targetElement)
            .ifFound(chain -> chain.highlight())  // Highlight found element
            .ifFoundLog("Found target element")   // Log for debugging
            .takeScreenshot("debug-1")            // Take screenshot
            .ifFoundClick()
            .takeScreenshot("debug-2")            // Another screenshot
            .perform(action, new ObjectCollection.Builder().build());
    }
    
    // ===== Model-Based Automation Principles =====
    
    public void demonstrateNoExplicitWaits() {
        // WRONG - Process-based approach with explicit waits
        // chain.click().wait(2.0).type("text")  // Don't do this!
        
        // CORRECT - Model-based approach with action configurations
        PatternFindOptions findWithDelay = new PatternFindOptions.Builder()
            .setPauseBeforeBegin(2.0)  // Timing in action configuration
            .build();
            
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();
        
        ConditionalActionChain.find(findWithDelay)
            .ifFoundClick()
            .then(new TypeOptions.Builder()
                .setTypeDelay(0.1)  // Type-specific timing
                .build())
            .perform(action, objectCollection);
    }
    
    public ActionResult navigateToState(StateImage targetStateImage) {
        // Focus on states, not processes
        // Note: In real code, you would get the StateImage from the State object
        return ConditionalActionChain
            .find(targetStateImage)
            .ifFoundLog("Already in target state")
            .ifNotFound(new PatternFindOptions.Builder().build())
            .then(navigationButton)
            .ifFoundClick()
            .then(targetStateImage)
            .ifFoundLog("Successfully navigated to state")
            .ifNotFoundDo(res -> {
                throw new StateTransitionException("Failed to reach target state");
            })
            .perform(action, new ObjectCollection.Builder().build());
    }
    
    // ===== Helper Classes =====
    
    public static class FormData {
        private String name;
        private String email;
        private String phone;
        
        public FormData(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
        
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }
    
    public static class StateTransitionException extends RuntimeException {
        public StateTransitionException(String message) {
            super(message);
        }
    }
    
    /**
     * Run all documentation examples to verify they compile and work
     */
    public void runAllExamples() {
        log.info("=== Running All Documentation Examples ===");
        
        try {
            // Basic Examples
            simpleFindAndClick();
            sequentialActionsWithThen();
            
            // Real-World Scenarios
            performLogin("testuser", "password123");
            saveWithConfirmation();
            clickWithRetry(buttonImage, 3);
            
            // Advanced Patterns
            FormData testData = new FormData("John Doe", "john@example.com", "555-1234");
            fillComplexForm(testData);
            scrollToFind(targetElement);
            useKeyboardShortcuts();
            
            // Conditional Patterns
            handleErrors();
            waitForLoadingToComplete();
            debugWorkflow();
            
            // Model-Based Principles
            demonstrateNoExplicitWaits();
            // navigateToState() requires a State object
            
            log.info("=== All Documentation Examples Completed Successfully ===");
            
        } catch (Exception e) {
            log.error("Error running documentation examples", e);
        }
    }
}