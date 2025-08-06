package com.example.conditionalchains.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.ConditionalActionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Demonstrates basic ConditionalActionChain patterns with find operations.
 */
@Component
public class BasicFindExample {
    private static final Logger log = LoggerFactory.getLogger(BasicFindExample.class);

    @Autowired
    private ConditionalActionWrapper conditionalWrapper;
    
    @Autowired
    private Action action;
    
    // These would normally come from State classes
    private StateImage submitButton;
    private StateImage cancelButton;
    private StateImage errorDialog;
    
    public BasicFindExample() {
        initializeObjects();
    }
    
    private void initializeObjects() {
        // In a real application, these would come from your State classes
        submitButton = new StateImage();
        submitButton.setName("SubmitButton");
        submitButton.addPatterns("submit-button.png");
            
        cancelButton = new StateImage();
        cancelButton.setName("CancelButton");
        cancelButton.addPatterns("cancel-button.png");
            
        errorDialog = new StateImage();
        errorDialog.setName("ErrorDialog");
        errorDialog.addPatterns("error-dialog.png");
    }
    
    /**
     * Simple find with success/failure handling using ConditionalActionWrapper
     */
    public void basicFindWithHandling() {
        log.info("=== Basic Find Example with ConditionalActionWrapper ===");
        
        ActionResult result = conditionalWrapper.findAndClick(submitButton);
        
        if (result.isSuccess()) {
            log.info("Submit button found and clicked successfully");
        } else {
            log.warn("Submit button not found, trying cancel button");
            conditionalWrapper.findAndClick(cancelButton);
        }
    }
    
    /**
     * Using ConditionalActionChain directly for more complex logic
     */
    public void chainedFindWithFallbacks() {
        log.info("=== Chained Find Example ===");
        
        ConditionalActionChain chain = ConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Primary button found and clicked")
            .ifNotFound(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Secondary button found and clicked")
            .ifNotFoundLog("No buttons found!");
        
        // Execute with submit button first, then cancel button as fallback
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(submitButton, cancelButton)
            .build();
            
        chain.perform(action, collection);
    }
    
    /**
     * Error handling pattern with ConditionalActionWrapper
     */
    public void errorHandlingPattern() {
        log.info("=== Error Handling Example ===");
        
        // Click submit and check for errors
        ActionResult clickResult = conditionalWrapper.findAndClick(submitButton);
        
        if (clickResult.isSuccess()) {
            log.info("Submit successful, checking for errors...");
            
            // Check if error dialog appeared
            PatternFindOptions findError = new PatternFindOptions.Builder().build();
            ObjectCollection errorCollection = new ObjectCollection.Builder()
                .withImages(errorDialog)
                .build();
            ActionResult errorCheck = action.perform(findError, errorCollection);
                
            if (errorCheck.isSuccess()) {
                log.error("Error dialog detected!");
                handleError();
            } else {
                log.info("No errors detected, proceeding...");
            }
        } else {
            log.error("Failed to click submit button");
        }
    }
    
    /**
     * Complex conditional chain example
     */
    public void complexConditionalExample() {
        log.info("=== Complex Conditional Chain ===");
        
        // Use ConditionalActionChain directly for complex logic
        ConditionalActionChain complexChain = ConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Submit clicked, waiting for response...")
            .ifNotFound(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Cancel clicked instead");
            
        ObjectCollection buttons = new ObjectCollection.Builder()
            .withImages(submitButton, cancelButton)
            .build();
            
        ActionResult result = complexChain.perform(action, buttons);
            
        log.info("Chain completed with result: {}", result.isSuccess());
    }
    
    private void handleError() {
        log.info("Handling error dialog...");
        // Error handling logic here
        conditionalWrapper.findAndClick(errorDialog);
    }
    
    /**
     * Demonstrates all methods
     */
    public void runAllExamples() {
        basicFindWithHandling();
        log.info("");
        
        chainedFindWithFallbacks();
        log.info("");
        
        errorHandlingPattern();
        log.info("");
        
        complexConditionalExample();
    }
}