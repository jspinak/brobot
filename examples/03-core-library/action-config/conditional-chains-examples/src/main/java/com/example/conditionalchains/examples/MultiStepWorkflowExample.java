package com.example.conditionalchains.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.ConditionalActionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates multi-step workflows using ConditionalActionChain.
 * Shows how to build complex automation flows with proper error handling.
 */
@Component
public class MultiStepWorkflowExample {
    private static final Logger log = LoggerFactory.getLogger(MultiStepWorkflowExample.class);

    @Autowired
    private ConditionalActionWrapper conditionalWrapper;
    
    @Autowired
    private Action action;
    
    // Login workflow objects
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage submitButton;
    private StateImage dashboardIcon;
    private StateImage errorMessage;
    
    public MultiStepWorkflowExample() {
        initializeObjects();
    }
    
    private void initializeObjects() {
        // In a real application, these would come from your State classes
        loginButton = new StateImage();
        loginButton.setName("LoginButton");
        loginButton.addPatterns("login-button.png");
        
        usernameField = new StateImage();
        usernameField.setName("UsernameField");
        usernameField.addPatterns("username-field.png");
        
        passwordField = new StateImage();
        passwordField.setName("PasswordField");
        passwordField.addPatterns("password-field.png");
        
        submitButton = new StateImage();
        submitButton.setName("SubmitButton");
        submitButton.addPatterns("submit-button.png");
        
        dashboardIcon = new StateImage();
        dashboardIcon.setName("DashboardIcon");
        dashboardIcon.addPatterns("dashboard-icon.png");
        
        errorMessage = new StateImage();
        errorMessage.setName("ErrorMessage");
        errorMessage.addPatterns("error-message.png");
    }
    
    /**
     * Complete login workflow with error handling at each step
     */
    public void loginWorkflow(String username, String password) {
        log.info("=== Login Workflow Example ===");
        
        // Step 1: Click login button
        ActionResult loginResult = conditionalWrapper.findAndClick(loginButton);
        
        if (loginResult.isSuccess()) {
            log.info("Login button clicked, waiting for form...");
            
            // Step 2: Find and fill username
            ActionResult usernameResult = conditionalWrapper.findAndClick(usernameField);
            
            if (usernameResult.isSuccess()) {
                log.info("Username field found, entering credentials...");
                
                // Type username
                conditionalWrapper.findAndType(usernameField, username);
                
                // Step 3: Fill password
                fillPasswordAndSubmit(password);
            } else {
                log.error("Username field not found - login form may not have loaded");
            }
        } else {
            log.error("Failed to click login button - may not be on correct page");
        }
    }
    
    private void fillPasswordAndSubmit(String password) {
        ActionResult passwordResult = conditionalWrapper.findAndClick(passwordField);
        
        if (passwordResult.isSuccess()) {
            log.info("Password field found, entering password...");
            
            // Type password
            conditionalWrapper.findAndType(passwordField, password);
            
            // Submit the form
            submitAndVerify();
        } else {
            log.error("Password field not found");
        }
    }
    
    private void submitAndVerify() {
        ActionResult submitResult = conditionalWrapper.findAndClick(submitButton);
        
        if (submitResult.isSuccess()) {
            log.info("Form submitted, verifying login...");
            
            // Wait a moment for page to load
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Check for successful login
            verifyLoginSuccess();
        } else {
            log.error("Failed to click submit button");
        }
    }
    
    private void verifyLoginSuccess() {
        // Use ConditionalActionChain for more complex logic
        ConditionalActionChain verifyChain = ConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifFoundLog("✓ Login successful! Dashboard loaded.")
            .ifNotFound(new PatternFindOptions.Builder().build())
            .ifFoundLog("✗ Login failed - error message displayed")
            .ifNotFoundLog("Neither dashboard nor error found - unknown state");
        
        // First check dashboard, then error message
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(dashboardIcon, errorMessage)
            .build();
            
        verifyChain.perform(action, collection);
    }
    
    /**
     * Data processing workflow with multiple validation steps
     */
    public void dataProcessingWorkflow() {
        log.info("=== Data Processing Workflow Example ===");
        
        List<String> processedItems = new ArrayList<>();
        StateImage dataRow = new StateImage();
        dataRow.setName("DataRow");
        dataRow.addPatterns("data-row.png");
        
        StateImage processButton = new StateImage();
        processButton.setName("ProcessButton");
        processButton.addPatterns("process-button.png");
        
        StateImage successIndicator = new StateImage();
        successIndicator.setName("SuccessIndicator");
        successIndicator.addPatterns("success-indicator.png");
        
        // Process multiple data rows
        for (int i = 0; i < 5; i++) {
            final int rowIndex = i;
            
            // Find data row
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ObjectCollection dataRowCollection = new ObjectCollection.Builder()
                .withImages(dataRow)
                .build();
            
            ActionResult findResult = action.perform(findOptions, dataRowCollection);
            
            if (findResult.isSuccess()) {
                log.info("Processing row {}", rowIndex);
                
                // Click process button for this row
                ActionResult processResult = conditionalWrapper.findAndClick(processButton);
                
                if (processResult.isSuccess()) {
                    // Verify processing succeeded
                    ActionResult successResult = action.perform(findOptions, 
                        new ObjectCollection.Builder()
                            .withImages(successIndicator)
                            .build());
                    
                    if (successResult.isSuccess()) {
                        log.info("Row {} processed successfully", rowIndex);
                        processedItems.add("Row " + rowIndex);
                    } else {
                        log.warn("Row {} processing may have failed", rowIndex);
                    }
                }
            } else {
                log.info("No more data rows to process");
                break;
            }
        }
        
        log.info("Processing complete. Processed {} items", processedItems.size());
    }
    
    /**
     * Advanced example using ConditionalActionChain directly
     */
    public void advancedChainExample() {
        log.info("=== Advanced Chain Example ===");
        
        // Build a complex conditional chain
        ConditionalActionChain chain = ConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Found and clicked primary button")
            .ifNotFound(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Found and clicked secondary button")
            .ifNotFoundLog("No buttons found!");
        
        // Execute with multiple targets
        ObjectCollection targets = new ObjectCollection.Builder()
            .withImages(submitButton, loginButton)
            .build();
            
        chain.perform(action, targets);
    }
    
    /**
     * Demonstrates all workflows
     */
    public void runAllExamples() {
        // Run login workflow
        loginWorkflow("testuser", "testpass123");
        log.info("");
        
        // Run data processing workflow
        dataProcessingWorkflow();
        log.info("");
        
        // Run advanced chain example
        advancedChainExample();
    }
}