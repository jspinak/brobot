package com.example.illustration.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Demonstrates illustration configuration for a login workflow.
 * Shows how the IllustrationController works with authentication steps in Brobot v1.1.0.
 */
@Component
@Slf4j
public class LoginWorkflowExample {
    
    private final Action action;
    private final IllustrationController illustrationController;
    
    // Login workflow objects
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage submitButton;
    private StateImage errorMessage;
    private StateImage dashboard;
    
    public LoginWorkflowExample(Action action, IllustrationController illustrationController) {
        this.action = action;
        this.illustrationController = illustrationController;
        initializeObjects();
    }
    
    private void initializeObjects() {
        loginButton = new StateImage.Builder()
            .setName("LoginButton")
            .addPatterns("authentication/login-button")
            .build();
            
        usernameField = new StateImage.Builder()
            .setName("UsernameField")
            .addPatterns("authentication/username-field")
            .build();
            
        passwordField = new StateImage.Builder()
            .setName("PasswordField")
            .addPatterns("authentication/password-field")
            .build();
            
        submitButton = new StateImage.Builder()
            .setName("SubmitButton")
            .addPatterns("authentication/submit-button")
            .build();
            
        errorMessage = new StateImage.Builder()
            .setName("ErrorMessage")
            .addPatterns("authentication/error-message")
            .build();
            
        dashboard = new StateImage.Builder()
            .setName("Dashboard")
            .addPatterns("authentication/dashboard")
            .build();
    }
    
    /**
     * Execute login workflow with illustrations
     */
    public void executeLogin(String username, String password) {
        log.info("Starting login workflow with illustrations");
        
        // Illustration settings are checked per action
        
        // Click login button
        ObjectCollection loginCollection = new ObjectCollection.Builder()
            .withImages(loginButton)
            .build();
            
        ActionResult loginClick = action.perform(
            new ClickOptions.Builder().build(), 
            loginCollection
        );
        
        // Illustrate the login click if allowed
        if (illustrationController.okToIllustrate(
                new ClickOptions.Builder().build(), 
                loginCollection)) {
            illustrationController.illustrateWhenAllowed(
                loginClick, 
                new ArrayList<>(), // empty search regions
                new ClickOptions.Builder().build(),
                loginCollection);
        }
                
        if (loginClick.isSuccess()) {
            // Click username field and type username
            ObjectCollection usernameCollection = new ObjectCollection.Builder()
                .withImages(usernameField)
                .build();
                
            ActionResult usernameClick = action.perform(
                new ClickOptions.Builder().build(),
                usernameCollection
            );
            if (illustrationController.okToIllustrate(
                    new ClickOptions.Builder().build(),
                    usernameCollection)) {
                illustrationController.illustrateWhenAllowed(
                    usernameClick,
                    new ArrayList<>(),
                    new ClickOptions.Builder().build(),
                    usernameCollection);
            }
            
            // Type username
            ObjectCollection usernameText = new ObjectCollection.Builder()
                .withStrings(username)
                .build();
            action.perform(new TypeOptions.Builder().build(), usernameText);
            
            // Click password field and type password
            ObjectCollection passwordCollection = new ObjectCollection.Builder()
                .withImages(passwordField)
                .build();
                
            ActionResult passwordClick = action.perform(
                new ClickOptions.Builder().build(),
                passwordCollection
            );
            if (illustrationController.okToIllustrate(
                    new ClickOptions.Builder().build(),
                    passwordCollection)) {
                illustrationController.illustrateWhenAllowed(
                    passwordClick,
                    new ArrayList<>(),
                    new ClickOptions.Builder().build(),
                    passwordCollection);
            }
            
            // Type password
            ObjectCollection passwordText = new ObjectCollection.Builder()
                .withStrings(password)
                .build();
            action.perform(new TypeOptions.Builder().build(), passwordText);
            
            // Click submit button
            ObjectCollection submitCollection = new ObjectCollection.Builder()
                .withImages(submitButton)
                .build();
                
            ActionResult submitResult = action.perform(
                new ClickOptions.Builder().build(),
                submitCollection
            );
            if (illustrationController.okToIllustrate(
                    new ClickOptions.Builder().build(),
                    submitCollection)) {
                illustrationController.illustrateWhenAllowed(
                    submitResult,
                    new ArrayList<>(),
                    new ClickOptions.Builder().build(),
                    submitCollection);
            }
            
            // Check for errors
            ObjectCollection errorCollection = new ObjectCollection.Builder()
                .withImages(errorMessage)
                .build();
                
            ActionResult errorCheck = action.perform(
                new PatternFindOptions.Builder().build(),
                errorCollection
            );
            
            if (errorCheck.isSuccess()) {
                log.error("Login failed - error message detected");
                if (illustrationController.okToIllustrate(
                        new PatternFindOptions.Builder().build(),
                        errorCollection)) {
                    illustrationController.illustrateWhenAllowed(
                        errorCheck,
                        new ArrayList<>(),
                        new PatternFindOptions.Builder().build(),
                        errorCollection);
                }
                handleLoginError();
            } else {
                // Check for successful dashboard
                ObjectCollection dashboardCollection = new ObjectCollection.Builder()
                    .withImages(dashboard)
                    .build();
                    
                ActionResult dashboardCheck = action.perform(
                    new PatternFindOptions.Builder().build(),
                    dashboardCollection
                );
                
                if (dashboardCheck.isSuccess()) {
                    log.info("Login successful - dashboard loaded");
                    if (illustrationController.okToIllustrate(
                            new PatternFindOptions.Builder().build(),
                            dashboardCollection)) {
                        illustrationController.illustrateWhenAllowed(
                            dashboardCheck,
                            new ArrayList<>(),
                            new PatternFindOptions.Builder().build(),
                            dashboardCollection);
                    }
                    performDashboardOperations();
                }
            }
        }
    }
    
    private void handleLoginError() {
        log.info("Handling login error");
        // Error recovery logic would go here
    }
    
    private void performDashboardOperations() {
        log.info("Performing dashboard operations");
        
        // Simulate some dashboard operations
        for (int i = 0; i < 5; i++) {
            StateImage menuItem = new StateImage.Builder()
                .setName("MenuItem" + i)
                .addPatterns("ui-elements/menu-item-" + i)
                .build();
                
            ObjectCollection menuCollection = new ObjectCollection.Builder()
                .withImages(menuItem)
                .build();
                
            ActionResult menuClick = action.perform(
                new ClickOptions.Builder().build(),
                menuCollection
            );
            
            // Only illustrate if the action fails (for demonstration)
            if (!menuClick.isSuccess()) {
                if (illustrationController.okToIllustrate(
                        new ClickOptions.Builder().build(),
                        menuCollection)) {
                    illustrationController.illustrateWhenAllowed(
                        menuClick,
                        new ArrayList<>(),
                        new ClickOptions.Builder().build(),
                        menuCollection);
                }
            }
        }
    }
    
    /**
     * Demonstrate login illustration scenarios
     */
    public void runExample() {
        log.info("=== Login Workflow Illustration Example ===");
        
        // Note: In v1.1.0, illustration configuration is done through application properties
        // not through code. The IllustrationController respects those settings.
        
        log.info("Executing login workflow...");
        executeLogin("testuser", "testpass123");
        
        log.info("Login workflow example completed");
        log.info("Check the illustrations directory for generated visualizations!");
    }
}